package net.hmmer.infection.util;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.ArrayList;
import java.util.List;

public class ResetHelper {
    public static void resetPlayer(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // Reset saturation and hunger:
            player.getFoodData().setSaturation(5.0f);
            player.getFoodData().setFoodLevel(20);

            // Reset Health
            player.setHealth(player.getMaxHealth());

            // Clear Inventory
            player.getInventory().clearContent();

            // removes item if they hold it with their mouse
            player.containerMenu.setCarried(ItemStack.EMPTY);
            player.containerMenu.broadcastChanges(); // sync to client

            // Clear all effects
            player.removeAllEffects();
        }
    }

    public static void preventPlayerFromDoingAnything(MinecraftServer server, boolean yesOrNo) {
        if (yesOrNo) {
            // can't move
            PreventPlayerMovementHelper.freezeAll(server);

            // gamemode Adventure
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.setGameMode(GameType.ADVENTURE);
            }

            // Can't attack other players
            for (ServerLevel level : server.getAllLevels()) {
                level.getGameRules().set(GameRules.PVP, false, server);
            }
        } else {
            // can move
            PreventPlayerMovementHelper.unfreezeAll(server);

            // gamemode Survival
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.setGameMode(GameType.SURVIVAL);
            }

            // Can attack other players
            for (ServerLevel level : server.getAllLevels()) {
                level.getGameRules().set(GameRules.PVP, true, server);
            }
        }

    }

    public static void preventAPlayerFromDoingAnything(ServerPlayer player, boolean yesOrNo) {
        if (yesOrNo) {
            // can't move
            PreventPlayerMovementHelper.freeze(player);

            // gamemode Adventure
            player.setGameMode(GameType.ADVENTURE);

        } else {
            // can move
            PreventPlayerMovementHelper.unfreeze(player);

            // gamemode Survival
            player.setGameMode(GameType.SURVIVAL);
        }
    }

    /**
     * Removes every entity besides paintings and ItemFrames and the Player
     * @param server the Minecraft Server
     */
    public static void removeAllEntitiesButPlayer(MinecraftServer server) {
        // Removes all entities besides the player
        for (ServerLevel level : server.getAllLevels()) {
            List<Entity> toRemove = new ArrayList<>();

            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Player) continue;
                if (entity instanceof ItemFrame || entity instanceof Painting) continue;

                toRemove.add(entity);
            }

            for (Entity entity : toRemove) {
                entity.discard();
            }
        }
    }

    public static void revokeAllPlayerAchievements(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerAdvancements advancements = player.getAdvancements();
            for (AdvancementHolder advancement : server.getAdvancements().getAllAdvancements()) {
                AdvancementProgress progress = advancements.getOrStartProgress(advancement);

                for (String criterion : progress.getCompletedCriteria()) {
                    advancements.revoke(advancement, criterion);
                }
            }
        }
    }

    public static void resetTimeAndWeather(MinecraftServer server) {

        // Resets the time
        CommandSourceStack silentSource = server.createCommandSourceStack().withSuppressedOutput();
        server.getCommands().performPrefixedCommand(silentSource, "time set 0");

        // Resets the weather
        server.getCommands().performPrefixedCommand(silentSource, "weather clear");
    }
}
