package net.hmmer.infection.data;

import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;

import java.util.Optional;

public class InfectionHelper {

    private static final String TEAM_NAME = "Infected";

    // Gets the infected team, creates it
    private static PlayerTeam getOrCreateTeam(Scoreboard scoreboard) {
        PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.addPlayerTeam(TEAM_NAME);
            team.setColor(Optional.of(TeamColor.GREEN));
        }
        return team;
    }

    public static void setInfected(ServerPlayer player, boolean infected) {
        Scoreboard scoreboard = player.level().getScoreboard();
        PlayerTeam team = getOrCreateTeam(scoreboard);
        String name = player.getScoreboardName();

        if (infected) {
            player.setAttached(ModAttachments.INFECTED, true);
            scoreboard.addPlayerToTeam(name, team);
        } else {
            player.removeAttached(ModAttachments.INFECTED);
            if (player.getTeam() == team) {
                scoreboard.removePlayerFromTeam(name, team);
            }
        }
    }

    public static boolean isInfected(ServerPlayer player) {
        return player.getAttachedOrElse(ModAttachments.INFECTED, false);
    }

    public static void resetInfectionForAllPlayers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // Reset Attachment Infection for all Players
            InfectionHelper.setInfected(player, false);
        }
    }

    /**
     * addEntityToInfectedTeam
     * @param entity the entity to add to Infected team
     */
    public static void addEntityToInfectedTeam(Entity entity) {
        Scoreboard scoreboard = entity.level().getScoreboard();
        PlayerTeam team = getOrCreateTeam(scoreboard);
        scoreboard.addPlayerToTeam(entity.getStringUUID(), team);
    }
}
