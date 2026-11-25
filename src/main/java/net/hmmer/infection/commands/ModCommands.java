package net.hmmer.infection.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hmmer.infection.Infection;
import net.hmmer.infection.border.BorderRecord;
import net.hmmer.infection.border.BorderSettings;
import net.hmmer.infection.game.InfectionGame;
import net.hmmer.infection.network.OpenMenuPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import javax.swing.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ModCommands {
    private static final BorderRecord borderStuff = new BorderRecord(BorderSettings.INITIAL_BORDER_SIZE,
            BorderSettings.INITIAL_BORDER_SHRINK_SIZE, BorderSettings.INITIAL_BORDER_TIME_TILL,
            BorderSettings.INITIAL_BORDER_TIME_TO_SHRINK);

    public static void registerCommands() {
        Infection.LOGGER.info("Registering commands for " + Infection.MOD_ID);

        Predicate<CommandSourceStack> isOp = source ->
                source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);

        CommandRegistrationCallback.EVENT.register((dispatcher,
                                                    buildContext, selection) -> {

            // /Infection command params
            // /Infection start <player>
            dispatcher.register(Commands.literal("infection")
                    .then(Commands.literal("infected") // returns the list of players who are infected
                            .executes(context -> { // doesn't require op
                                CommandSourceStack source = context.getSource();

                                if(!checkIfInfectionActive(source)) return 0; // check if active, if not sends a message

                                List<ServerPlayer> infected =
                                        InfectionGame.getInfectedPlayers(source.getServer());

                                if (infected.isEmpty()) {
                                    source.sendSystemMessage(Component.literal("No players are infected."));
                                    return 0;
                                }

                                String names = infected.stream()
                                        .map(p -> p.getName().getString())
                                        .collect(Collectors.joining(", "));

                                source.sendSystemMessage(Component.literal("Infected (" + infected.size() + "): ")
                                        .withStyle(ChatFormatting.DARK_GREEN)
                                        .append(Component.literal(names).withStyle(ChatFormatting.GREEN)));

                                return infected.size();
                            }))
                    .then(Commands.literal("status") // doesn't require op
                            .executes(context -> {
                                String status = InfectionGame.isActive() ? "Game active" : "Game inactive";
                                context.getSource().sendSystemMessage(Component.literal(status));
                                return 1;
                            }))
                    .then(Commands.literal("help") // doesn't require op
                            .executes(context -> {
                                CommandSourceStack source = context.getSource();
                                if (source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                                    source.sendSystemMessage(Component.literal("================[Infection Help]================"));
                                    source.sendSystemMessage(Component.literal("infected: tells you how many players are infected"));
                                    source.sendSystemMessage(Component.literal("status: tells you whether a game is active"));
                                    source.sendSystemMessage(Component.literal("menu: opens a menu to help you start an infection"));
                                    source.sendSystemMessage(Component.literal("start <player> <boolean zombies>: also starts an infection"));
                                    source.sendSystemMessage(Component.literal("If you just leave start the game will choose a random player"));
                                    source.sendSystemMessage(Component.literal("<player> select a player to infect"));
                                    source.sendSystemMessage(Component.literal("<boolean zombies> true if you want zombie mode enabled"));
                                    source.sendSystemMessage(Component.literal("stop: stops an infection game currently running"));
                                    source.sendSystemMessage(Component.literal("================================================"));

                                } else {
                                    source.sendSystemMessage(Component.literal("========[Infection Help]========"));
                                    source.sendSystemMessage(Component.literal("infected: tells you how many players are infected"));
                                    source.sendSystemMessage(Component.literal("status: tells you whether a game is active"));
                                    source.sendSystemMessage(Component.literal("================================================"));
                                }
                                return 1;
                            }))
                    .then(Commands.literal("menu")
                            .requires(isOp)
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                ServerPlayNetworking.send(player, OpenMenuPayload.INSTANCE);
                                return 1;
                            }))
                    .then(Commands.literal("start")
                            .requires(isOp)
                            .executes(context -> {
                                CommandSourceStack source = context.getSource();

                                // lets players know that no player was selected and is choosing random player
                                source.getServer().getPlayerList().broadcastSystemMessage(Component.
                                        literal("No player selected, choosing random player"), false);

                                InfectionGame.startInfection(source.getPlayer(), source.getServer(),
                                        randomPlayer(source), getPosition(source),
                                        borderStuff,
                                        source.getLevel(), false);
                                return 1;
                            })
                            .then(Commands.argument("player", EntityArgument.player())
                                    .executes(context -> {
                                        CommandSourceStack source = context.getSource();

                                        InfectionGame.startInfection(source.getPlayer() ,source.getServer(),
                                                EntityArgument.getPlayer(context, "player"),
                                                getPosition(source),
                                                borderStuff,
                                                source.getLevel(), false);
                                        return 1;
                                    })
                                    .then(Commands.literal("zombies")
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();

                                            InfectionGame.startInfection(source.getPlayer(), source.getServer(),
                                                    randomPlayer(source), getPosition(source),
                                                    borderStuff,
                                                    source.getLevel(), true);
                                            return 1;
                                        }))))
                    .then(Commands.literal("stop")
                            .requires(isOp)
                            .executes(context -> {
                                CommandSourceStack source = context.getSource();

                                checkIfInfectionActive(source); // checks if a game is active then sends a failure
                                // message

                                InfectionGame.end(source.getServer(),
                                        true);
                                return 1;
                            }))
            );
        });
    }

    private static ServerPlayer randomPlayer(CommandSourceStack source) {
        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) {
            source.sendFailure(Component.literal("No players are infected."));
            return null;
        }

        return players.get(source.getLevel().getRandom().nextInt(players.size()));
    }

    private static BlockPos getPosition(CommandSourceStack source) {
        return new BlockPos((int) source.getPlayer().getX(),
                (int) source.getPlayer().getY(), (int) source.getPlayer().getZ());
    }

    private static boolean checkIfInfectionActive(CommandSourceStack source) {
        if (!InfectionGame.isActive()) {
            source.sendFailure(Component.literal("No game of Infection is active."));
            return false;
        } else {
            return true;
        }
    }
}
