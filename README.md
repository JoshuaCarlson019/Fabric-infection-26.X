# Infection Mod for Java Minecraft 26.3

A mod that lets you run infection games in minecraft

# What is Infection?

Infection is a game where one person starts off infected and has to kill the other players on the server. For ease of words I'll call the other players survivors. When an infected kills a survivor, that player becomes an infected and also has to hunt down the other players on the server. This repeats until there is none survivors remaining which results in the infected winning.

# Download:

## v0.0.2
https://github.com/JoshuaCarlson019/Infection-Mod-for-Java-Minecraft-Version-26.3/releases/download/v0.0.2/infection-0.0.2.jar

## v0.0.1 
https://github.com/JoshuaCarlson019/Infection-Mod-for-Java-Minecraft-Version-26.3/releases/download/v0.0.1/infection-0.0.1.jar

# Current Version
## v0.0.2

Includes Basic Infection or Infection with zombies
## Types

### Basic infection 

An Infection game where one player is infected and has to kill the other players.

### Infection + Zombies

An Infection game where one player is still infected but zombies spawn around the survivors and if killed by a zombie become infected. The player who died to an infected will have a stronger zombie spawn on them with the gear that player had.

# Commands

Commands you can use with the infection mod:

### /infection infected

Sends a message to the player who sent the command with the list of players who are currently infected during a game.

Permission: Anyone

### /infection status

Sends a message to the player who sent the command whether a game of infection is active or not.

Permission: Anyone

### /infection help

Sends a message to the player who sent the command a guide describing what each of the commands due based on your permissions.

Permission: Anyone

### /infection menu

The player who sent the command gets a menu screen to easily modify certain parameters of the infection game.

Permission: Operator

### /infection start

Starts an infection game with a random player selected as the infected with the world border set at their location, size 500. Also zombie mode is initially disabled.

### Parameters for /infection start
#### The first parameter /infection start playerName 

This allows you to choose which player you want to infect.

Example:
/infection start @p

This infects the player who used the command.

#### Second parameter /infection start playerName zombies

You can also enable zombie mode which spawns zombies around the survivors and does some extra stuff as well.

Example:
/infection start @p zombies

This would make the player using the command infected and also enable zombie mode.

Permission: Operator

### /infection stop

Stops an infection game currently running, cancelling it.

Permission: Operator

# Requirements:
Fabric API: fabric-api-0.165.0+26.3

Link: https://www.curseforge.com/minecraft/mc-mods/fabric-api/files/9295874

Minecraft version: 26.3

# Setup 

## Server-side:

You need fabric-api-0.165.0+26.3 on the server in the mods folder along with it being a fabric server. Also put this mod into the mods file for the server to use this mod.

## Client-side:

For this to work on a server everyone who joins needs there mod launcher to have fabric-api-0.165.0+26.3 in the mods folder along with my mod.

# Previous versions

## v0.0.1

Similar to v0.0.2 but doesn't have new border properties
Removed hunger system mechanic
