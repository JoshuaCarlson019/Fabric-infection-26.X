package net.hmmer.infection.game;

import net.hmmer.infection.Infection;
import net.hmmer.infection.border.BorderSettings;
import net.hmmer.infection.data.InfectionHelper;
import net.hmmer.infection.util.ResetHelper;
import net.hmmer.infection.util.TaskScheduler;
import net.hmmer.infection.util.TitleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.*;

public class InfectionGame {

    // For zombie mode
    private static boolean zombieMode = false;

    // If a game is active
    private static boolean active = false;

    // For when the countdown is active
    private static boolean countDown = false;

    // The time it takes before a non infected has to hit an infected player
    private static final long HIT_DEADLINE_TICKS = 5 * 60 * 20; // 5 minutes

    // Everyone infected this round, by UUID, so it survives relogs
    private static final Set<UUID> infectedPlayers = new HashSet<>();
    // Everyone notInfected with tick attacked to them this round, by UUID, so it survives relogs
    private static final Map<UUID, Long> lastHitTick = new HashMap<>();

    private static final Map<UUID, Map<EquipmentSlot, ItemStack>> deathGear = new HashMap<>();



    /**
     * @return true if a game is active, otherwise false
     */
    public static boolean isActive() {
        return active;
    }

    /**
     * @return true if zombieMode is enabled and a game is active, otherwise false
     */
    public static boolean isZombieMode() {
        return active && zombieMode;
    }

    /**
     * @return true if the countdown is active
     */
    public static boolean isCountDown() {
        return countDown;
    }

    /**
     * isInfected
     * @param player the server player who your checking is infected
     * @return true if the player is in fact infected, otherwise false
     */
    public static boolean isInfected(ServerPlayer player) {
        return infectedPlayers.contains(player.getUUID());
    }

    /**
     * startInfection
     * @param server the server it takes place on
     * @param infectedPlayer the infected player, player who starts off infected
     * @param pos the position where the game will take place
     * @param borderSize the world border size
     * @param serverLevel the sever level
     * @param zombies can be true or false, if true zombies spawn around the map
     * @return true if successfully started, otherwise false
     */
    public static void startInfection(ServerPlayer sender, MinecraftServer server,
                                         ServerPlayer infectedPlayer, BlockPos pos,
                                         int borderSize, ServerLevel serverLevel,
                                         boolean zombies) {

        // For if a game is already active or if a player is on the death screen or there are less than 2 players
        String reason;
        if (active || anyoneDead(server) || server.getPlayerCount() < 2) {
            reason = "A game is already active";
            if (anyoneDead(server)) {
                reason = "Someone is on the Death, please get off the death screen RIGHT NOW";
            } else if (server.getPlayerCount() < 2) {
                reason = "You need at least 2 players to start, your friendless, how unfortunate"; // don't have friends?
            }
            sender.sendSystemMessage(Component.literal(reason).withStyle(ChatFormatting.RED));
            return;
        }

        zombieMode = zombies; // sets zombieMode

        active = true; // sets game active true

        Infection.LOGGER.info("Game started");

        // Resets worldSpawnPoint
        setWorldSpawn(server, pos);

        // Successfully teleported players
        Infection.LOGGER.info("Successfully teleported players");

        // Resets some basic stuff for the game
        ResetHelper.preventPlayerFromDoingAnything(server, true); // Prevents players from doing anything,
        // can't move, can't damage other players, sets them to adventureMode
        ResetHelper.resetTimeAndWeather(server); // Resets the time and weather
        ResetHelper.removeAllEntitiesButPlayer(server); // Removes all the entities we don't need
        ResetHelper.resetPlayer(server); // Reset Player, saturation and hunger, health, and clears inventory

        InfectionHelper.resetInfectionForAllPlayers(server); // Reset Attachment Infection for all Players

        // This is so players don't die when the border changes position
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.setGameMode(GameType.CREATIVE);
        }

        // Adjusts the border
        BorderSettings.adjustTheBorder(pos.getX(), pos.getZ(), borderSize, serverLevel);

        // Sets the one chosen to infected
        infect(server, infectedPlayer);

        // First teleports infected to the position of the border
        double infectedY = findSafeY(infectedPlayer, pos.getX(), pos.getZ());
        infectedPlayer.teleportTo(pos.getX(), infectedY, pos.getZ());
        Infection.LOGGER.info("Successfully teleported infected player");

        List<ServerPlayer> others = server.getPlayerList().getPlayers().stream()
                .filter(p -> p != infectedPlayer)
                .toList();

        double startAngle = Math.random() * 360; // gets random section of the circle

        // Teleports players in a circle 4 blocks away from the one who is infected
        for (int i = 0; i < others.size(); i++) {
            ServerPlayer other = others.get(i);

            double angleDegrees = (double) (360 * i) / others.size() + startAngle; // separates the players evenly

            double x = infectedPlayer.getX() + Math.cos(Math.toRadians(angleDegrees)) * 4; // 4 means 4 blocks away
            double z = infectedPlayer.getZ() + Math.sin(Math.toRadians(angleDegrees)) * 4; // 4 means 4 blocks away

            double y = findSafeY(other, x, z);
            others.get(i).teleportTo(x, y, z);
        }

        Infection.LOGGER.info("Successfully teleported all players");

        // Puts players back into adventure
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.setGameMode(GameType.ADVENTURE);
        }

        // Hunger starts
        long now = server.getTickCount();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            lastHitTick.put(player.getUUID(), now);
        }

        // Count Down Sequence, counts down to the start of the game
        infectionCountDownSequence(server, infectedPlayer);
    }

    /**
     * infectionCountDown Sequence
     * @param server the minecraft server we are on
     * @param target the server player who was chosen as infected
     */
    private static void infectionCountDownSequence(MinecraftServer server, ServerPlayer target) {
        Component selected = Component.literal(target.getName().getString()).
                withStyle(ChatFormatting.GREEN);

        countDown = true;

        // sends out a title like /title @a title
        TitleHelper.sendOutTitle(server, selected);
        TitleHelper.sendOutSubTitle(server, Component.literal("is infected").withStyle(ChatFormatting.GREEN));
        TitleHelper.sentOutActionBar(target, Component.literal("You are infected").withStyle(ChatFormatting.GOLD));

        TaskScheduler.schedule(server, 100, () -> {
            // sends out a title like /title @a actionBar
            TitleHelper.sendOutSubTitle(server, Component.empty());
            TitleHelper.sentOutActionBar(server, Component.
                    literal("Infection begins in 15 seconds"));
        });

        // Wait 15 seconds for the game to begin (100 ticks is 5 seconds)
        // then count down to 0 for 5 seconds
        TaskScheduler.schedule(server, 300, () -> {
            TitleHelper.sendOutTitle(server, Component.literal("5"));
        });

        TaskScheduler.schedule(server, 320, () -> {
            TitleHelper.sendOutTitle(server, Component.literal("4"));
        });
        TaskScheduler.schedule(server, 340, () -> {
            TitleHelper.sendOutTitle(server, Component.literal("3"));
        });
        TaskScheduler.schedule(server, 360, () -> {
            TitleHelper.sendOutTitle(server, Component.literal("2"));
        });
        TaskScheduler.schedule(server, 380, () -> {
            TitleHelper.sendOutTitle(server, Component.literal("1"));
        });
        TaskScheduler.schedule(server, 400, () -> {

            countDown = false;

            // Can do anything now
            ResetHelper.preventPlayerFromDoingAnything(server, false);

            // Game begins
            TitleHelper.sendOutTitle(server, Component.literal("GO"));
        });
    }


    /**
     * infect
     * @param server the minecraft server the player is on
     * @param player the player to infect
     */
    public static void infect(MinecraftServer server, ServerPlayer player) {
        if (!active || isInfected(player)) return; // checks if the player already not infected
        // Remove hunger
        lastHitTick.remove(player.getUUID());
        player.removeEffect(MobEffects.HUNGER);

        infectedPlayers.add(player.getUUID());
        InfectionHelper.setInfected(player, true);
        checkForEnd(server, null);
    }

    /**
     * onPlayerDeath
     * @param server the minecraft server the player is on
     * @param player the player who died
     * @param source the damage source
     */
    public static void onPlayerDeath(MinecraftServer server, ServerPlayer player, DamageSource source) {
        if (!active || isInfected(player)) return; // makes sure a game is active and the player is an infected

        boolean killedByInfected = isKilledByInfectedOrZombie(player, source); // checks if the killer is an Infected
        // player or zombie
        if (!killedByInfected) { // not killed by an infected player or maybe zombie
            deathGear.remove(player.getUUID());
            return;
        }

        ServerLevel level = player.level();
        BlockPos deathPos = player.blockPosition();

        TitleHelper.sentOutActionBar(server, Component.
                literal(player.getName().getString() + " has been infected").withStyle(ChatFormatting.GREEN));

        // Creates a lightning bolt that spawns where that player died, makes it a visual lightning bolt so it doesn't
        // damage the environment or other players
        LightningBolt lightningBolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);

        if (lightningBolt != null) {
            lightningBolt.setPos(deathPos.getX(), deathPos.getY(), deathPos.getZ());
            lightningBolt.setVisualOnly(true);
            level.addFreshEntity(lightningBolt);
        }

        // Infect the player who died to the infection
        infect(server, player);

        // Checks if the game active is zombie mode then it spawns a strong zombie where the player died
        if (zombieMode) {
            Map<EquipmentSlot, ItemStack> gear = deathGear.getOrDefault(player.getUUID(), Map.of());
            spawnStrongZombie(level, deathPos, player, gear);
        }

        deathGear.remove(player.getUUID());
    }

    /**
     * isKilledByInfectedOrZombie
     * @param player the player who died
     * @param source the damage source
     * @return true if the player was killed by an infected or zombie, otherwise false
     */
    private static boolean isKilledByInfectedOrZombie(ServerPlayer player, DamageSource source) {
        ServerPlayer killer = findPlayerKiller(player, source); // finds the killer based on direct or indirect causes
        if (killer != null && isInfected(killer)) return true; // checks if the killer isn't infected and that we
        // have a killer

        // If in zombie mode then the player can die to zombies
        if (zombieMode) {
            if (source.getEntity() instanceof Zombie) return true;
            if (player.getKillCredit() instanceof Zombie) return true;
        }
        return false;
    }

    /**
     * onPlayerHit
     * @param attacker the attacker, the person who hit that player
     * @param victim the player who was hit
     * When it receives the player hit event it and the attacker was a non-infected and the victim is an
     * infected, it removes the hunger effect from the attacker
     */
    public static void onPlayerHit(ServerPlayer attacker, ServerPlayer victim) {
        if (!active) return; // Checks if a game is active
        if (isInfected(attacker) || !isInfected(victim)) return; // Checks if the attacker is Infected and the victim
        // isn't an infected player

        // This puts the attacker back into the lastHitTick map(UUID,CURRENT_TICK)
        lastHitTick.put(attacker.getUUID(), (long) attacker.level().getServer().getTickCount());

        // Checks if the player has the hunger effect, if they do, it removes the hunger effect
        if (attacker.hasEffect(MobEffects.HUNGER)) {
            attacker.removeEffect(MobEffects.HUNGER);

            int randomNumber = (int) (Math.random() * 3) + 1; // random number from 1 to 3
            String message = "";
            if (randomNumber == 1) {
                message = "The hunger subsides";
            }
            if (randomNumber == 2) {
                message = "Your hunger fades to nothing";
            }
            if (randomNumber == 3) {
                message = "Your aren't so hungry anymore";
            }

            TitleHelper.sentOutActionBar(attacker,
                    Component.literal(message).withStyle(ChatFormatting.GOLD));
        }
    }

    /**
     * tick
     * @param server the minecraft server
     * Runs every game tick from the events in infection.java
     */
    public static void tick(MinecraftServer server) {
        if (!active || server.getTickCount() % 20 != 0) return; // Makes it so the rest of the code runs every second

        long now = server.getTickCount(); // Gets the current tick

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (isInfected(player) || player.isSpectator()) continue; // Makes sure the player isn't infected or in
            // spectator mode

            // Checks if the players UUID is in the map lastHitTick, if not it adds them
            long last = lastHitTick.computeIfAbsent(player.getUUID(), id -> now);

            // Computes the remaining time till this player who isn't infected has to hit an infected
            long remaining = HIT_DEADLINE_TICKS - (now - last);

            // If the remaining time is at 60 seconds it sends out this message
            if (remaining > 230 && remaining <= 60 * 20) {
                TitleHelper.sentOutActionBar(player, Component.
                        literal("Your hunger grows you should hit an infected soon, in a minute").
                        withStyle(ChatFormatting.GOLD));
            }

            // If a survivor has run out of time to hit an infected then it gives them hunger
            if (now - last >= HIT_DEADLINE_TICKS && !player.hasEffect(MobEffects.HUNGER)) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, MobEffectInstance.INFINITE_DURATION, 0));

                int randomNumber = (int) (Math.random() * 3) + 1; // random number from 1 to 3
                String message = "";
                if (randomNumber == 1) {
                    message = "You've been hiding for too long, the hunger controls you";
                }
                if (randomNumber == 2) {
                    message = "Stop hiding coward, play the game how its intended";
                }
                if (randomNumber == 3) {
                    message = "Your desire for violence makes you hungry";
                }

                TitleHelper.sentOutActionBar(player, Component.literal(message).withStyle(ChatFormatting.GOLD));
                TaskScheduler.schedule(server, 100, () -> {
                    TitleHelper.sentOutActionBar(player, Component.
                            literal("Hit an infected to cure your hunger").withStyle(ChatFormatting.GOLD));
                });
            }
        }
    }

    /**
     * onPlayerJoin
     * @param server the minecraft server
     * @param player the player who joined
     */
    public static void onPlayerJoin(MinecraftServer server, ServerPlayer player) {
        // lets player move if they leave during the countDown and then rejoin
        if (!countDown) {
            ResetHelper.preventAPlayerFromDoingAnything(player, false);
        } else {
            ResetHelper.preventAPlayerFromDoingAnything(player, true);
        }

        if(!active) {
            deInfect(player);
        } else if(isInfected(player)) { // if a player reconnects infected then they stay infected
            infect(server, player);
        }
        // late players don't get infected
    }

    /**
     * onPlayerLeave
     * @param server the minecraft server
     * @param leavingPlayer the player who is leaving
     */
    public static void onPlayerLeave(MinecraftServer server, ServerPlayer leavingPlayer) {
        if (active) checkForEnd(server, leavingPlayer); // might be the last player
    }

    /**
     * checkForEnd
     * @param server the minecraft server
     * @param leavingPlayer the player who left
     */
    public static void checkForEnd(MinecraftServer server, ServerPlayer leavingPlayer) {
        // gets the remaining players who are not infected, strings
        List<String> survivorsNames = server.getPlayerList().getPlayers().stream()
                .filter(p -> p != leavingPlayer)
                .filter(p -> !isInfected(p))
                .filter(p -> !p.isSpectator())
                .map(p -> p.getName().getString())
                .toList();

        // gets the remaining players who aren't infected
        List<ServerPlayer> survivors = server.getPlayerList().getPlayers().stream()
                .filter(p -> p != leavingPlayer)
                .filter(p -> !isInfected(p))
                .filter(p -> !p.isSpectator())
                .toList();

        List<String> infected = server.getPlayerList().getPlayers().stream()
                .filter(p -> p != leavingPlayer)
                .filter(InfectionGame::isInfected)
                .filter(p -> !p.isSpectator())
                .map(p -> p.getName().getString())
                .toList();

        Infection.LOGGER.info("[Infection] Survivors left: " + survivorsNames);
        Infection.LOGGER.info("[Infection] Infected left: " + infected);

        // Ends the game if all the survivors have become infected
        if (survivors.isEmpty()) {
            end(server, false);
        }

        if (infected.isEmpty()) {
            // if only one survivor, end the game
            if (survivors.size() == 1) {
                end(server, true);
            }
            // otherwise choose a random person to be infected who isn't the leaving player
            int randomNumber = (int) (Math.random() * survivors.size());
            ServerPlayer toBeInfected = survivors.get(randomNumber);
            // Sends message saying who the new infected is when the previous infected leaves the game
            TitleHelper.sentOutActionBar(server, Component.literal(leavingPlayer.getName().getString() +
                            " left the game so new infected is " + toBeInfected.getName().getString()).
                    withStyle(ChatFormatting.GREEN));
            infect(server, toBeInfected);
        }
    }

    /**
     * end, ends the game
     * @param server the minecraft server
     * @param forceStopYesOrNo if the game was force stopped or not, for example a player uses
     * /infection stop
     */
    public static void end(MinecraftServer server, boolean forceStopYesOrNo) {
        if (!active) return; // only can end a game if a game is active
        active = false; // sets the game to inactive

        if (forceStopYesOrNo) {
            TitleHelper.sendOutSubTitle(server, Component.empty());
            TitleHelper.sendOutTitle(server, Component.literal("Game Cancelled").withStyle(ChatFormatting.YELLOW));
        } else {
            TitleHelper.sendOutSubTitle(server, Component.empty());
            TitleHelper.sendOutTitle(server, Component.literal("Infected have won").withStyle(ChatFormatting.GREEN));
        }

        // Clears hunger
        lastHitTick.clear();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.removeEffect(MobEffects.HUNGER);
        }

        // Clear zombie mode
        if (isZombieMode()) {
            zombieMode = false;
        }

        // Sets countDown false
        if (isCountDown()) {
            countDown = false;
        }

        // allows all players to move
        ResetHelper.preventPlayerFromDoingAnything(server, false);

        // Clears map
        deathGear.clear();

        // deInfects all players
        deInfectAll(server);
    }

    /**
     * findPlayerKiller
     * @param victim the victim who died
     * @param source the damage source
     * @return the ServerPlayer who killed the opponent
     */
    public static ServerPlayer findPlayerKiller(ServerPlayer victim, DamageSource source) {
        // 1. Direct death: returns whoever caused the damage
        if (source.getEntity() instanceof ServerPlayer killer && killer != victim) {
            return killer;
        }
        // 2. Indirect Death: last player who hurt the victim recently 5 seconds
        if (victim.getKillCredit() instanceof ServerPlayer killer && killer != victim) {
            return killer;
        }

        return null; // no player was involved
    }

    /**
     * setWorldSpawn, sets the world spawn
     * @param server the minecraft server
     * @param pos the block position where the world border will be set
     */
    private static void setWorldSpawn(MinecraftServer server, BlockPos pos) {
        CommandSourceStack silent = server.createCommandSourceStack().withSuppressedOutput();
        server.getCommands().performPrefixedCommand(silent,
                "setworldspawn " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
    }

    /**
     * deInfectAll
     * @param server minecraft server
     */
    public static void deInfectAll(MinecraftServer server) {
        InfectionHelper.resetInfectionForAllPlayers(server);
        // clears list
        infectedPlayers.clear();
        // Clears tasks that shouldn't be running
        TaskScheduler.clear();
    }

    /**
     * deInfect
     * @param player the server player to deInfect
     */
    public static void deInfect(ServerPlayer player) {
        InfectionHelper.setInfected(player, false);
        // clears them from the list
        infectedPlayers.remove(player.getUUID());
    }

    /**
     * getInfectedPlayers
     * @param server the minecraft server
     * @return a list of server players
     */
    public static List<ServerPlayer> getInfectedPlayers(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream()
                .filter(InfectionGame::isInfected)
                .toList();
    }

    /**
     * anyoneDead, checks if any player is in the death menu screen
     * @param server the minecraft server
     * @return returns a list of players who are on the minecraft death screen
     */
    public static boolean anyoneDead(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream().anyMatch(ServerPlayer::isDeadOrDying);
    }

    /**
     * findSafeY
     * @param player the player to find a safe y level
     * @param x the x position
     * @param z the z position
     * @return returns the safe y level
     */
    private static double findSafeY(ServerPlayer player, double x, double z) {
        ServerLevel level = player.level();

        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);
        BlockPos surfacePos = new BlockPos(blockX, surfaceY, blockZ);

        double floor = level.getBlockFloorHeight(surfacePos);
        if (!Double.isFinite(floor)) {
            return surfaceY;
        }

        return surfaceY + floor;
    }

    /**
     * spawnStrongZombies
     * @param level the server level
     * @param pos the position to spawn the strong zombie
     * @param victim the victim who died
     */
    private static void spawnStrongZombie(ServerLevel level, BlockPos pos, ServerPlayer victim,
                                          Map<EquipmentSlot, ItemStack> gear) {
        Zombie zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.EVENT);
        if (zombie == null) return;

        zombie.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        zombie.setPersistenceRequired();
        zombie.setCustomName(Component.literal("Infection " + victim.getName().getString()).
                withStyle(ChatFormatting.GREEN));
        zombie.setCustomNameVisible(true);

        // Strong
        setBase(zombie, Attributes.MAX_HEALTH, 40.0);
        zombie.setHealth(40.0f);
        setBase(zombie, Attributes.ATTACK_DAMAGE, 7.0);
        setBase(zombie, Attributes.MOVEMENT_SPEED, 0.3);
        setBase(zombie, Attributes.FOLLOW_RANGE, 64.0);

        level.addFreshEntity(zombie);

        // Sets the equipment that the victim had onto the zombie
        for (Map.Entry<EquipmentSlot, ItemStack> entry : gear.entrySet()) {
            ItemStack stack = entry.getValue().copy();
            stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            zombie.setItemSlot(entry.getKey(), stack);
            zombie.setDropChance(entry.getKey(), 0.0f);
        }

        // If player didn't die with a helmet it gives the zombie a helmet so it doesn't die during the day
        if (zombie.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            ItemStack helmet = new ItemStack(Items.IRON_HELMET);
            helmet.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            zombie.setItemSlot(EquipmentSlot.HEAD, helmet);
        }

        // adds entity to infected team so it doesn't attack others on infected team
        InfectionHelper.addEntityToInfectedTeam(zombie);

        level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                30, 0.3, 0.5, 0.3, 0.02);
    }

    public static void rememberGearFor(ServerPlayer player) {
        // Snapshot equipment immediately, before the death sequence drops it
        Map<EquipmentSlot, ItemStack> gear = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = player.getItemBySlot(slot);
            if (!item.isEmpty()) {
                gear.put(slot, item.copy());
            }
        }
        deathGear.put(player.getUUID(), gear);
    }

    /**
     * setBase
     * @param zombie the zombie
     * @param attribute the attribute you want to change
     * @param base the value you want to change for the attribute
     */
    public static void setBase(Zombie zombie, Holder<Attribute> attribute, double base) {
        AttributeInstance instance = zombie.getAttribute(attribute);
        if (instance == null) return;
        instance.setBaseValue(base);
    }
}
