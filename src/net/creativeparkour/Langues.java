/* CreativeParkour - Bukkit Plugin that allows everyone on the server to create, publish, share and play cool parkour maps.
    Copyright (C) 2017  ObelusPA

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.creativeparkour;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

class Langues
{
	private static Plugin plugin = CreativeParkour.getPlugin();
	private static FileConfiguration langue;
	private static FileConfiguration anglais;
	static Map<String, String> modifsAnglais = new HashMap<String, String>(); // TODO Enlever quand le nouveau système de langues sera en place

	static void enable(String lang)
	{
		// Fichier anglais
		File fichier_anglais = new File(plugin.getDataFolder(), "/languages/en.yml");
		anglais = YamlConfiguration.loadConfiguration(fichier_anglais);

		List<Traduction> trads = new ArrayList<Traduction>();

		// Anglais
		trads.add(new Traduction("en", "documentation link", "https://creativeparkour.net/doc/", "2.2.2.2"));
		trads.add(new Traduction("en", "not allowed", "You are not allowed to do this.", "1.2"));
		trads.add(new Traduction("en", "leave", "You left " + CreativeParkour.getNom() + ".", "1.1.3.2"));
		trads.add(new Traduction("en", "ban", "You are not allowed to use " + CreativeParkour.getNom() + ".", "1.1.3.2"));
		trads.add(new Traduction("en", "ban error 1", "This player is already banned.", "1.1.3.2"));
		trads.add(new Traduction("en", "ban error 3", "This player is not banned.", "1.1.3.2"));
		trads.add(new Traduction("en", "player offline", "This player does not exist or is not online.", "1.2"));
		trads.add(new Traduction("en", "unknown player", "%player does not exist.", "2.1"));
		trads.add(new Traduction("en", "inventory error", "You must empty your inventory before entering this parkour map (armor too). After that, try joining the map again.", "2.0"));
		trads.add(new Traduction("en", "location error", "You must leave the map where you are to do that. Use '/cp leave'.", "1.1.3.2"));
		trads.add(new Traduction("en", "location error 2", "You can not do that here.", "2.0"));
		trads.add(new Traduction("en", "location error 3", "You have to be creating a map to do that.", "2.4"));
		trads.add(new Traduction("en", "too many players", "There are too many players where you want to go. Please try again later.", "1.1.3.2"));
		trads.add(new Traduction("en", "too many maps", "You created too many parkour maps on this server. Get in touch with an admin to know how to remove this limit.", "2.1"));
		trads.add(new Traduction("en", "pinned", "Pinned", "1.1.3.2"));
		trads.add(new Traduction("en", "waiting for servers", "Waiting for servers...", "2.0"));
		trads.add(new Traduction("en", "error", "Sorry, an error occurred while performing this command, it will be reported.", "2.0"));
		trads.add(new Traduction("en", "http error", "An error occurred while contacting " + CreativeParkour.lienSiteCourt() + " (%error).", "2.0"));
		trads.add(new Traduction("en", "http error maintenance", "Sorry, " + CreativeParkour.lienSiteCourt() + " is in maintenance mode, please try again later.", "2.0"));
		trads.add(new Traduction("en", "feature disabled", "Sorry, your server admin has not enabled this feature. :(", "2.0"));
		trads.add(new Traduction("en", "online disabled", "Sorry, remote features are disabled on this server. If you are an admin, type \"/cp config sharing\" to enable them.", "2.4.4"));
		trads.add(new Traduction("en", "outdated server", "Sorry, this feature is only available on %ver (or greater) servers.", "2.2"));
		trads.add(new Traduction("en", "feedback 1", "Do you enjoy CreativeParkour? The developer really cares about the plugin quality. Please %Lclick here%L to send feedback, ideas to improve CreativeParkour! Thank you! :)", "2.2.3"));
		trads.add(new Traduction("en", "feedback 2", "Do you have ideas to improve the parkour map builder? Please send them to CreativeParkour's developer by %Lclicking here%L, even if they are crazy! Thank you! :)", "2.2.3"));

		trads.add(new Traduction("en", "commands.help", "Type '/cp help' for full help.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.unknown", "Unknown command.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.player", "Only players can use this command.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.playerN", "player", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.question", "What do you want to do?", "1.2"));
		trads.add(new Traduction("en", "commands.yes no", "Type '/cp yes' or '/cp no' to answer.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.yes", "yes", "1.2"));
		trads.add(new Traduction("en", "commands.no", "no", "1.2"));
		trads.add(new Traduction("en", "commands.click", "click", "1.2"));
		trads.add(new Traduction("en", "commands.click to answer", "Click to answer %answer", "1.2"));
		trads.add(new Traduction("en", "commands.no answer", "We are not expecting any answer from you...", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.play", "play", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.play tip", "You can directly play a map by typing '/cp play [map name]'", "2.2.2"));
		trads.add(new Traduction("en", "commands.play error", "This map does not exist.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.map name", "map name", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.create", "create", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.create map error", "This map does not exist or is not in creation mode.", "2.3.3"));
		trads.add(new Traduction("en", "commands.invite", "invite", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.invite error 1", "You have to be creating a map to invite or remove someone or see the contributor list.", "2.5.1"));
		trads.add(new Traduction("en", "commands.invite error 2", "You have already invited this player. They have to accept your invitation and then type '/cp create' and click 'other maps' to come.", "2.1"));
		trads.add(new Traduction("en", "commands.invite error 3", "This player is the map creator...", "2.5.1"));
		trads.add(new Traduction("en", "commands.invite error 4", "Only the map creator can do that.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.invite error 5", "This player is not allowed to build a map.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.remove", "remove", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.remove error", "This player can not already build the map with you.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.remove ok", "%player can no longer build this map with you.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.contributors", "contributors", "2.5.1"));
		trads.add(new Traduction("en", "commands.contributors info", "Type \"/cp contributors\" to see the list of players you allowed to build the map with you.", "2.5.1"));
		trads.add(new Traduction("en", "commands.contributors empty", "You invited nobody to build your map with you.", "2.5.1"));
		trads.add(new Traduction("en", "commands.contributors invite", "type \"/cp invite <player>\" to invite a player.", "2.5.1"));
		trads.add(new Traduction("en", "commands.contributors list", "Players allowed to build this map:", "2.5.1"));
		trads.add(new Traduction("en", "commands.contributors remove info", "Click to disallow %player to build this map with you.", "2.5.1"));
		trads.add(new Traduction("en", "commands.contributors pending", "pending invitation", "2.5.1"));
		trads.add(new Traduction("en", "commands.spectator", "spectator", "2.0"));
		trads.add(new Traduction("en", "commands.tp", "tp", "1.2.2"));
		trads.add(new Traduction("en", "commands.tp error", "This player is not playing in a " + CreativeParkour.getNom() + " map.", "1.2.2"));
		trads.add(new Traduction("en", "commands.tp error creation", "This player is building a map, they have to invite you by using \"/cp invite\".", "2.1.2.1"));
		trads.add(new Traduction("en", "commands.tp info", "If you want to teleport to a player that is playing in a " + CreativeParkour.getNom() + " map, use \"/cp tp <player>\".", "1.2.5"));
		trads.add(new Traduction("en", "commands.fill", "fill", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.undo", "undo", "2.0"));
		trads.add(new Traduction("en", "commands.redo", "redo", "2.0"));
		trads.add(new Traduction("en", "commands.leave", "leave", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.leave error", "You are not in a " + CreativeParkour.getNom() + " map.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.leave only", "You can only use \"/cp leave\" to leave " + CreativeParkour.getNom() + ".", "2.4.4.2"));
		trads.add(new Traduction("en", "commands.test", "test", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.test error", "You can't do that now.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.publish", "publish", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.publish name", "name", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.publish error", "You must validate your map before publishing it. Type '/cp test'.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.publish error name", "Please choose the name of your map.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.publish error name 2", "This name is already taken by another map.", "1.2"));
		trads.add(new Traduction("en", "commands.publish error creator", "Only %creator can publish this map.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.share", "share", "2.0"));
		trads.add(new Traduction("en", "commands.share announcement", "You can now share your map with the CreativeParkour community, show your parkour builder skills and challenge player ghosts from all the world! It uses the new " + CreativeParkour.lienSiteCourt() + " website and it takes less than 5 minutes.\nType \"/cp share\".", "2.2"));
		trads.add(new Traduction("en", "commands.share message", "Share this map on " + CreativeParkour.lienSiteCourt(), "2.0"));
		trads.add(new Traduction("en", "commands.share question", "Do you want to share your map on " + CreativeParkour.lienSiteCourt() + "?", "2.0"));
		trads.add(new Traduction("en", "commands.share info", "Contribute to the community, show your parkour builder skills to all the world and challenge player ghosts!", "2.2"));
		trads.add(new Traduction("en", "commands.share canceled", "Map sharing canceled.", "2.0"));
		trads.add(new Traduction("en", "commands.share wait", "Sending map data, please wait (it may take a while).", "2.0"));
		trads.add(new Traduction("en", "commands.share new map", "You have to confirm your map sharing on " + CreativeParkour.lienSiteCourt() + ". %LClick here%L to do it.", "2.0"));
		trads.add(new Traduction("en", "commands.share unknown server", "This server is not registered. If you are an admin, type \"/cp config sharing\" (you can also use this command to disable map download and sharing).", "2.2.3.1"));
		trads.add(new Traduction("en", "commands.share error creator", "Only %creator (the map creator) can share this map.", "2.0"));
		trads.add(new Traduction("en", "commands.share error block", "Your map cannot be shared because it contains unallowed blocks.", "2.0"));
		trads.add(new Traduction("en", "commands.share error existing map", "You already shared your map. %LClick here%L if you want to change its settings on the website.", "2.0"));
		trads.add(new Traduction("en", "commands.download", "download", "2.0"));
		trads.add(new Traduction("en", "commands.download loading", "Downloading map data, please wait (it may take a while).", "2.0"));
		trads.add(new Traduction("en", "commands.download wait", "Please wait a few minutes before downloading a map again.", "2.0"));
		trads.add(new Traduction("en", "commands.download wait 2", "Building \"%map\" (it can take time)...", "2.2"));
		trads.add(new Traduction("en", "commands.download error build", "Something went wrong while building this map. Maybe it has been built in a newer Minecraft or CreativeParkour version.", "2.3"));
		trads.add(new Traduction("en", "commands.download error ID", "You have to type the numerical ID of the map you want to download or its URL : \"/cpd <ID or URL>\". Visit " + CreativeParkour.lienSiteCourt() + " to find parkour maps or type \"/cp play\".", "2.4.6.5"));
		trads.add(new Traduction("en", "commands.download error disabled", "Map downloading is not enabled on this server. If you are an admin, type \"/cp config sharing\".", "2.0"));
		trads.add(new Traduction("en", "commands.edit", "edit", "1.2"));
		trads.add(new Traduction("en", "commands.edit message", "Edit this map", "1.2"));
		trads.add(new Traduction("en", "commands.edit question", "Are you sure you want to edit %map?", "1.2"));
		trads.add(new Traduction("en", "commands.edit info", "You will no longer be able to edit your last edited map until you publish %map again.\nIf you change a block, the leaderboard of this map will be deleted.", "1.2.2"));
		trads.add(new Traduction("en", "commands.edit canceled", "Map edition canceled.", "1.2"));
		trads.add(new Traduction("en", "commands.delete", "delete", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.delete message", "Delete this map", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.edit delete error", "You must be in a map to share, edit, delete or export it. You have to be the creator of this map or have the \"creativeparkour.manage\" permission.", "2.1.2"));
		trads.add(new Traduction("en", "commands.delete deleted", "%map deleted.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.delete question", "Are you sure you want to delete %map?", "1.2"));
		trads.add(new Traduction("en", "commands.delete canceled", "Map deletion canceled.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.export", "export", "2.1"));
		trads.add(new Traduction("en", "commands.export question", "Do you want to export %map?", "2.1"));
		trads.add(new Traduction("en", "commands.export info", "A new file will be created in the following folder: <your_server>/plugins/CreativeParkour/Exported maps\nThe exported map will not contain leaderboard's data.", "2.1"));
		trads.add(new Traduction("en", "commands.export canceled", "Map export canceled.", "2.1"));
		trads.add(new Traduction("en", "commands.export success", "A new file has been created for this map: %file", "2.1"));
		trads.add(new Traduction("en", "commands.export error", "You can not export downloaded maps.", "2.1"));
		trads.add(new Traduction("en", "commands.import success", "%map has been successfully imported!", "2.1"));
		trads.add(new Traduction("en", "commands.import error", "Something went wrong when importing %map, maybe it has been built in a newer Minecraft or CreativeParkour version.", "2.3"));
		trads.add(new Traduction("en", "commands.import error 2", "%file could not be imported because the map already exists.", "2.1"));
		trads.add(new Traduction("en", "commands.cannot be undone", "This cannot be undone", "1.2"));
		trads.add(new Traduction("en", "commands.pin", "pin", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.pin error", "You must be in the map you want to pin or unpin.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.pin success", "This map is now pinned.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.unpin", "unpin", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.unpin success", "This map is no longer pinned.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.notifications", "notifications", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.notifications info", "You can use \"/cp settings\" to disable notifications.", "2.5"));
		trads.add(new Traduction("en", "commands.notifications on", "Notifications enabled.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.notifications off", "Notifications disabled, type \"/cp notifications\" again to enable them.", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.messages", "messages", "1.2.3"));
		trads.add(new Traduction("en", "commands.messages choice", "Click on your favorite checkpoint message type:", "2.2.2"));
		trads.add(new Traduction("en", "commands.messages disable", "Disable checkpoint validation messages", "2.2.2"));
		trads.add(new Traduction("en", "commands.messages ok", "Checkpoint messages settings updated.", "2.2.2"));
		trads.add(new Traduction("en", "commands.messages spec", "spectator", "2.1.2.3"));
		trads.add(new Traduction("en", "commands.messages spec on", "Checkpoint messages enabled in spectator mode.", "2.1.2.3"));
		trads.add(new Traduction("en", "commands.messages spec off", "Checkpoint messages disabled in spectator mode, type \"/cp messages spectator\" again to enable them.", "2.1.2.3"));
		trads.add(new Traduction("en", "commands.messages spec to player", "To %player", "2.1.2.3"));
		trads.add(new Traduction("en", "commands.ban", "ban", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.pardon", "pardon", "1.1.3.2"));
		trads.add(new Traduction("en", "commands.removetime", "removetime", "2.1"));
		trads.add(new Traduction("en", "commands.removetime ok", "%player's time(s) deleted.", "2.1"));
		trads.add(new Traduction("en", "commands.removetime error", "You must be in the map in which you want the time to be deleted.", "2.1"));
		trads.add(new Traduction("en", "commands.getid", "getid", "2.1.2"));
		trads.add(new Traduction("en", "commands.getid ok", "%map's id: %id", "2.1.2"));
		trads.add(new Traduction("en", "commands.getid error", "This map does not exist. To get a map id, go in it and type \"/cp getid\" or directly type \"/cp getid <map name>\".", "2.1.2"));
		trads.add(new Traduction("en", "commands.managemaps", "managemaps", "2.1.2"));
		trads.add(new Traduction("en", "commands.managemaps info", "List of all the maps on this server:", "2.1.2"));
		trads.add(new Traduction("en", "commands.managemaps creator", "creator", "2.1.2"));
		trads.add(new Traduction("en", "commands.managemaps play", "get in", "2.1.2"));
		trads.add(new Traduction("en", "commands.managemaps delete", "delete", "2.1.2"));
		trads.add(new Traduction("en", "commands.managemaps click", "Click", "2.1.2"));
		trads.add(new Traduction("en", "commands.mapoptions", "mapoptions", "2.4"));
		trads.add(new Traduction("en", "commands.mapoptions error", "You cannot change map options because someone is testing the map.", "2.4"));
		trads.add(new Traduction("en", "commands.mapoptions title", "Map options", "2.4"));
		trads.add(new Traduction("en", "commands.mapoptions sneak", "Sneak", "2.4"));
		trads.add(new Traduction("en", "commands.mapoptions sneak info", "Allow players to sneak in this map", "2.4"));
		trads.add(new Traduction("en", "commands.mapoptions lava", "Deadly lava", "2.4"));
		trads.add(new Traduction("en", "commands.mapoptions lava info", "Kill players when they touch lava", "2.4"));
		trads.add(new Traduction("en", "commands.mapoptions water", "Deadly water", "2.4"));
		trads.add(new Traduction("en", "commands.mapoptions water info", "Kill players when they touch water", "2.4"));
		trads.add(new Traduction("en", "commands.mapoptions interactions", "Interactions", "2.4.6"));
		trads.add(new Traduction("en", "commands.mapoptions interactions info", "Allow players to interact with doors and trapdoors", "2.4.6"));
		trads.add(new Traduction("en", "commands.claim", "claim", "2.4"));
		trads.add(new Traduction("en", "commands.claim error", "You must leave CreativeParkour to claim your rewards (type \"/cp leave\")", "2.4"));
		trads.add(new Traduction("en", "commands.claim error 2", "You don't have rewards to claim.", "2.4"));
		trads.add(new Traduction("en", "commands.claim error 3", "You can't claim rewards in this world. If you are an admin, check CreativeParkour's config.", "2.4.2"));
		trads.add(new Traduction("en", "commands.importsel", "importsel", "2.4.3"));
		trads.add(new Traduction("en", "commands.importsel error", "You have to select an area with WorldEdit before using this command to import it in a new CreativeParkour map.", "2.4.3"));
		trads.add(new Traduction("en", "commands.importsel error block", "Some blocks were not copied because they are not allowed in CreativeParkour.", "2.4.3"));
		trads.add(new Traduction("en", "commands.register", "register", "2.4.4"));
		trads.add(new Traduction("en", "commands.register already", "You already are registered. You can use remote features like sharing your parkour maps with \"/cp share\"!", "2.4.4"));
		trads.add(new Traduction("en", "commands.register link", "%LClick here%L to finish your registration on " + CreativeParkour.lienSiteCourt() + ". It will take only a few minutes.", "2.4.4"));
		trads.add(new Traduction("en", "commands.sync", "sync", "2.4.7"));
		trads.add(new Traduction("en", "commands.sync msg", "Synchronizing data...", "2.4.7"));
		trads.add(new Traduction("en", "commands.sync done", "Map list, player names, player times and ghosts are up to date!", "2.4.7"));
		trads.add(new Traduction("en", "commands.settings", "settings", "2.5"));
		trads.add(new Traduction("en", "commands.settings info", "Remember that you can manage CreativeParkour's messages (checkpoints, notifications...) at any time with \"/cp settings\".", "2.5"));
		trads.add(new Traduction("en", "commands.settings title", "CreativeParkour settings", "2.5"));
		trads.add(new Traduction("en", "commands.settings enabled", "Enabled", "2.5"));
		trads.add(new Traduction("en", "commands.settings disabled", "Disabled", "2.5"));
		trads.add(new Traduction("en", "commands.settings enable", "Click to enable", "2.5"));
		trads.add(new Traduction("en", "commands.settings disable", "Click to disable", "2.5"));
		trads.add(new Traduction("en", "commands.settings notifications", "Notifications", "2.5"));
		trads.add(new Traduction("en", "commands.settings notifications info", "Show notifications in chat when a player breaks your record in a map (only if you were the first).", "2.5"));
		trads.add(new Traduction("en", "commands.settings reward msg", "Reward message", "2.5"));
		trads.add(new Traduction("en", "commands.settings reward msg info", "Show a message that reminds you to claim your rewards if you have any.", "2.5"));
		trads.add(new Traduction("en", "commands.settings elytra msg", "Elytra message", "2.5"));
		trads.add(new Traduction("en", "commands.settings elytra msg info", "Show a message when you get Elytra in parkours.", "2.5"));
		trads.add(new Traduction("en", "commands.settings checkpoint msg", "Checkpoint message", "2.5"));
		trads.add(new Traduction("en", "commands.settings checkpoint msg info", "Message displayed in chat when you pass a checkpoint (time differences between you and some records).", "2.5"));
		trads.add(new Traduction("en", "commands.settings checkpoint msg full", "Long message", "2.5"));
		trads.add(new Traduction("en", "commands.settings checkpoint msg reduced", "Reduced message", "2.5"));
		trads.add(new Traduction("en", "commands.settings checkpoint msg none", "No message", "2.5"));
		trads.add(new Traduction("en", "commands.settings checkpoint msg click", "Click to select your favorite message type", "2.5"));
		trads.add(new Traduction("en", "commands.settings checkpoint msg spec", "Checkpoint messages (spectator mode)", "2.5"));
		trads.add(new Traduction("en", "commands.settings checkpoint msg spec info", "Show a message when the player you spectate passes a checkpoint (with time differences).", "2.5"));

		trads.add(new Traduction("en", "help.title", "Help", "1.1.3.2"));
		trads.add(new Traduction("en", "help.page", "Page", "1.1.3.2"));
		trads.add(new Traduction("en", "help.page command", "/cp help [n] to get page n of help", "1.1.3.2"));
		trads.add(new Traduction("en", "help.help", "Displays " + CreativeParkour.getNom() + "'s help.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.play", "Play a parkour map.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.download", "Download a map from " + CreativeParkour.lienSiteCourt() + ".", "2.0"));
		trads.add(new Traduction("en", "help.create", "Create a new parkour map.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.invite", "Invite someone to build a map with you.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.remove", "Disallow someone to build a map with you.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.contributors", "Show list of players invited in your map.", "2.5.1"));
		trads.add(new Traduction("en", "help.settings", "Change your CreativeParkour settings", "2.5"));
		trads.add(new Traduction("en", "help.tp", "Teleport to someone playing a parkour map.", "1.2.5"));
		trads.add(new Traduction("en", "help.spectator", "Toggle spectator mode in a map.", "2.0"));
		trads.add(new Traduction("en", "help.fill", "Fill an area selected with the WorldEdit wand.", "2.0"));
		trads.add(new Traduction("en", "help.undo", "Undo the last action done with the wand.", "2.0"));
		trads.add(new Traduction("en", "help.redo", "Redo the last undone action.", "2.0"));
		trads.add(new Traduction("en", "help.config", "Configure the plugin.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.reload", "Reload plugin's configuration files (except 'maps.yml').", "1.1.3.2"));
		trads.add(new Traduction("en", "help.leave", "Leave a " + CreativeParkour.getNom() + " map.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.test", "Test a parkour during its creation.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.publish", "Publish a parkour map.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.share", "Share a map on " + CreativeParkour.lienSiteCourt() + ".", "2.0"));
		trads.add(new Traduction("en", "help.edit", "Edit a map.", "1.2"));
		trads.add(new Traduction("en", "help.delete", "Delete a parkour map.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.getid", "Display the ID of the map where you are or the one you typed.", "2.1.2"));
		trads.add(new Traduction("en", "help.export", "Create a file containing map data.", "2.1"));
		trads.add(new Traduction("en", "help.managemaps", "Displays map list with quick actions.", "2.1.2"));
		trads.add(new Traduction("en", "help.enable", "Enable " + CreativeParkour.getNom() + ".", "1.1.3.2"));
		trads.add(new Traduction("en", "help.disable", "Disable " + CreativeParkour.getNom() + ".", "1.1.3.2"));
		trads.add(new Traduction("en", "help.ban", "Prohibits the player to use the plugin.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.pardon", "Allows the player to use the plugin.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.pin", "Pin a map in map list.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.unpin", "Unpin a map.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.notifications", "Toggle notifications.", "1.1.3.2"));
		trads.add(new Traduction("en", "help.messages", "Reduce or disable checkpoint validation messages.", "2.2.2"));
		trads.add(new Traduction("en", "help.removetime", "Delete player's time (add \"all\" after its name to delete it in all the maps).", "2.1"));
		trads.add(new Traduction("en", "help.mapoptions", "Enable special options for your map.", "2.4"));
		trads.add(new Traduction("en", "help.claim", "Get rewards obtained in parkour.", "2.4"));
		trads.add(new Traduction("en", "help.importsel", "Import a WorldEdit selection in a new map.", "2.4.3"));
		trads.add(new Traduction("en", "help.register", "Register on " + CreativeParkour.lienSiteCourt(), "2.4.4"));
		trads.add(new Traduction("en", "help.sync", "Synchronize data with " + CreativeParkour.lienSiteCourt(), "2.4.7"));

		trads.add(new Traduction("en", "config.plugin not enabled", "The plugin is currently not working because the configuration is not completed.", "1.1.3.2"));
		trads.add(new Traduction("en", "config.config tutorial", "To configure the plugin, type \"/cp config\".", "2.4.3"));
		trads.add(new Traduction("en", "config.next", "Next", "2.4.3"));
		trads.add(new Traduction("en", "config.next hover", "Click to continue to next step", "2.4.3"));
		trads.add(new Traduction("en", "config.start", "Welcome to CreativeParkour! We will help you to quickly configure the plugin to start having fun in parkour maps with your friends. Click \"next\" below to continue.", "2.4.3"));
		trads.add(new Traduction("en", "config.storage.text", "CreativeParkour will create a new world in your server to store parkour maps. Are you okay? Click YES or NO below.", "2.4.3"));
		trads.add(new Traduction("en", "config.storage.info", "If you click NO, you will have to configure the storage world yourself, but this is discouraged.", "2.4.3"));
		trads.add(new Traduction("en", "config.storage.default world", "A new world has been created in your server folder. Click \"next\" below to continue CreativeParkour configuration.", "2.4.3"));
		trads.add(new Traduction("en", "config.storage.no default world", "You have chosen to not use the default storage world. You have to edit the file \"your_server/plugins/" + CreativeParkour.getNom() + "/configuration.yml\" to choose which world will contain parkour maps. You also have to set coordinates where you want the plugin to store parkour maps.", "2.4.3"));
		trads.add(new Traduction("en", "config.storage.no default world back", "If you want to use the default storage world, you can type \"/cp config storage\" to go back.", "2.4.3"));
		trads.add(new Traduction("en", "config.storage.no default world doc", "You can find more documentation about configuring the storage world at", "1.1.3.2"));
		trads.add(new Traduction("en", "config.storage.world creating", "Creating world...", "1.1.3.2"));
		trads.add(new Traduction("en", "config.storage.warn", "Warning: if you change storage configuration later, you may lose parkour maps or they will be corrupted and unrecoverable (but changing map size is safe).", "2.4.3"));
		trads.add(new Traduction("en", "config.dependencies.info", "It is highly recommended to install the following plugin(s) to enable all the CreativeParkour features (click them to visit their official pages):", "2.4.3"));
		trads.add(new Traduction("en", "config.dependencies.protocollib", "ProtocolLib (to enable ghost and player visibility features)", "2.4.3"));
		trads.add(new Traduction("en", "config.dependencies.vault", "Vault (to prevent permissions issues if you have a permission plugin like PermissionsEx an use money rewards)", "2.4.3"));
		trads.add(new Traduction("en", "config.dependencies.worldedit", "WorldEdit (to build maps more easily and import your old parkours in CreativeParkour)", "2.4.3"));
		trads.add(new Traduction("en", "config.permissions", "By default, all the players can use most of CreativeParkour's features, but there are many permissions to change this. See this page for full information: https://creativeparkour.net/doc/permissions.php", "2.4.3"));
		trads.add(new Traduction("en", "config.sharing.text", "CreativeParkour provides free downloadable and community-made parkour maps where everyone can challenge player ghosts and show their skills! To enable these features and be able to upload your maps to creativeparkour.net, please click YES to register your server, or click NO to disable these features.", "2.4.3"));
		trads.add(new Traduction("en", "config.sharing.announcement 2", "This map has been downloaded from " + CreativeParkour.lienSiteCourt() + ", why not creating yours and sharing it with the community too? %LClick here%L to learn how to do it.", "2.2.0.3"));
		trads.add(new Traduction("en", "config.sharing.description", "Type '/cp config sharing' to enable map downloading and sharing.", "2.1"));
		trads.add(new Traduction("en", "config.sharing.enabled", "Online map sharing is enabled. Please wait a few seconds to register your server on " + CreativeParkour.lienSiteCourt() + ".", "2.0"));
		trads.add(new Traduction("en", "config.sharing.disabled", "Online map sharing is disabled. Type \"/cp config sharing\" again if you change your mind...", "2.0"));
		trads.add(new Traduction("en", "config.sharing.new server", "For security reasons, you have to register your server. %LClick here%L to do it.", "2.0"));
		trads.add(new Traduction("en", "config.sharing.server already registered", "Your server is already registered. %LClick here%L if you want to change its settings on the website.", "2.0"));
		trads.add(new Traduction("en", "config.sharing.done", "When you are done, click \"next\" to finish CreativeParkour's configuration.", "2.4.3"));
		trads.add(new Traduction("en", "config.end", "There we go, CreativeParkour is installed on your server! You can start building your first map with \"/cp create\", or import parkours that you already have with \"/cp importsel\", or download maps and play them with \"/cp play\". CreativeParkour has many other features that you can discover in the official documentation at https://creativeparkour.net/doc\nThank you for using this plugin, have fun!", "2.4.3"));
		trads.add(new Traduction("en", "config.plugin disabled", CreativeParkour.getNom() + " has been disabled by an operator.", "1.1.3.2"));
		trads.add(new Traduction("en", "config.missing", "These settings are missing in the file 'configuration.yml'", "1.1.3.2"));
		trads.add(new Traduction("en", "config.reload", "Configuration reloaded!", "1.2"));
		trads.add(new Traduction("en", "config.enable", CreativeParkour.getNom() + " is now enabled.", "1.1.3.2"));
		trads.add(new Traduction("en", "config.disable", CreativeParkour.getNom() + " is now disabled.", "1.1.3.2"));
		trads.add(new Traduction("en", "config.language info", "If you are not English, other languages are available for CreativeParkour. Check them out at " + CreativeParkour.lienSite() + "/doc/languages.php", "2.0"));

		trads.add(new Traduction("en", "creation.title", "Map creation", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.new map", "Create a new map", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.new map warn", "Last edited map will be deleted!", "1.2"));
		trads.add(new Traduction("en", "creation.load map", "Load last edited map", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.other maps", "Other maps (invitations)", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.other maps creator", "%creator's map", "2.4.2.3"));
		trads.add(new Traduction("en", "creation.no map", "You do not have any editable map.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.erase question", "Are you sure you want to delete the last map you edited?", "1.2"));
		trads.add(new Traduction("en", "creation.erase question import", "You can only create 1 map at a time, so the last map you edited will be deleted and replaced by your new imported map. Continue?", "2.4.3"));
		trads.add(new Traduction("en", "creation.canceled", "Map creation canceled.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.new", "You have this area to build your parkour. %LClick here%L to read the map creation tutorial.", "2.0.1"));
		trads.add(new Traduction("en", "creation.invitation", "An invitation was sent to %player.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.invitation player", "%player wants to build a parkour map with you. Are you okay?", "1.2"));
		trads.add(new Traduction("en", "creation.invitation denied", "Invitation denied.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.building", "Building map (it can take time)...", "2.2"));
		trads.add(new Traduction("en", "creation.items.worldedit wand", "WorldEdit wand", "2.0"));
		trads.add(new Traduction("en", "creation.help book.p1", "       §6§lHelp book\n\n§rThis book briefly summarizes the most basic features of the parkour map builder. To discover more features and a tutorial, read a complete documentation at §3" + CreativeParkour.lienSite() + "/doc/map-creation.php", "2.1.2.3"));
		trads.add(new Traduction("en", "creation.help book.p2", "       §6§lHelp book\n§r§5§oItems:\n§r§0P3:WorldEdit\n§r§5§oSpecial signs:\n§r§0P4:Spawn point\nP5:Start & end points\nP6:Checkpoints\nP7:Optional checkpoints\nP8:Death height\n§r§5§oMiscellaneous:\n§r§0P9:Testing and publication\nP10:Leaving", "2.2"));
		trads.add(new Traduction("en", "creation.help book.p3", "      §5§l§nWorldEdit\n\n§r§0You can use the WorldEdit wand (wooden axe) to build your map. Most of the regular WorldEdit commands are available (//set, //undo, ...).", "2.2"));
		trads.add(new Traduction("en", "creation.help book.p4", "     §5§l§nSpawn point\n\n§r§0Write §o<spawn>§r§0 on a sign to set the spawn point of your map.\nThe sign disappears when you publish your map, like all the special signs.", "2.2.1"));
		trads.add(new Traduction("en", "creation.help book.p5", "§5§l§nStart & end points\n\n§r§0Write §o<start>§r§0 on a sign to set the starting point of your parkour.\nWhen a player steps on this point, their timer starts.\nWrite §o<end>§r to set the point where the timer stops.", "2.2.1"));
		trads.add(new Traduction("en", "creation.help book.p6", "     §5§l§nCheckpoints\n\n§r§0Write §o<checkpoint>§r§0 on a sign to set a checkpoint. When a player falls, they will be teleported back to the last checkpoint they passed.\nPlayers must pass all the checkpoints to finish the parkour (except optional checkpoints).", "2.2.1"));
		trads.add(new Traduction("en", "creation.help book.p7", "        §5§l§nOptional§r\n     §5§l§ncheckpoints\n\n§r§0Write §ooptional§r§0 on the second line of a checkpoint sign to make it optional.\nPlayers do not have to validate optional checkpoints to finish the parkour, but they can teleport to them.", "2.1.2"));
		trads.add(new Traduction("en", "creation.help book.p8", "     §5§l§nDeath height\n\n§r§0Write §o<death>§r§0 on a sign placed at the height at which you want players to be teleported to the last checkpoint they passed (when they fall).", "2.2.1"));
		trads.add(new Traduction("en", "creation.help book.p9", "     §5§l§nTesting and§r\n     §5§l§npublication\n§r§0You can test your map as many times as you want by typing §o/cp test§r\nYou can leave test mode with §o/cp test leave§r\nWhen you have finished the test, you can publish your map using §o/cp publish <map_name>", "2.2.1"));
		trads.add(new Traduction("en", "creation.help book.p10", "        §5§l§nLeaving§r\n\n§r§0If you are tired, you can leave " + CreativeParkour.getNom() + " at any time by typing\n§o/cp leave", "2.1.2"));
		trads.add(new Traduction("en", "creation.block not allowed", "This block is not allowed.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.monster egg", "You are using a Silverfish block, please use the corresponding regular block.", "1.2.5"));
		trads.add(new Traduction("en", "creation.sign too high", "This sign is too high.", "1.2.5"));
		trads.add(new Traduction("en", "creation.check signs", "Checking signs...", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.check.multiple sign error", "Your map must contain only 1 %type sign.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.check.no sign error", "There is no %type sign in your map.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.check.tp error 1", "Invalid location on <tp> sign located at %loc", "2.3"));
		trads.add(new Traduction("en", "creation.check.tp error 2", "Location specified on <tp> sign located at %loc is outside of the map.", "2.3"));
		trads.add(new Traduction("en", "creation.check.sign height error", "%type sign can not be placed higher than the others.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.check.sign post error", "Signs of this type must be placed on top of a block.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.signs.effect error", "Available effects are SPEED, SLOW, JUMP, CONFUSION, BLINDNESS, NIGHT_VISION and LEVITATION.", "1.2.6"));
		trads.add(new Traduction("en", "creation.signs.int error", "'duration' and 'amplifier' must be integers between 0 and 999999.", "1.2.6"));
		trads.add(new Traduction("en", "creation.test", "You are in test mode. You have to finish your parkour to validate it. When you publish your map, special signs will be hidden.", "1.2.2"));
		trads.add(new Traduction("en", "creation.test build", "You can not build because someone is testing the map.", "1.2"));
		trads.add(new Traduction("en", "creation.test completed", "You have completed the test, your map is validated. Type '/cp publish' to publish your map on the server.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.test leave", "Type '/cp test leave' to return to creation mode.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.test error", "Someone is already testing the map.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.test adjacent checkpoints", "The checkpoint validation message is only displayed once for adjacent checkpoints.", "2.2.2"));
		trads.add(new Traduction("en", "creation.status", "Map status", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.validated", "Validated", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.unvalidated", "Unvalidated", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.published", "Your map has been successfully published, the other players can come play!", "1.2.7"));
		trads.add(new Traduction("en", "creation.announce new map", "%player has created a new " + CreativeParkour.getNom() + " map: %map. %LClick here%L to play it!", "1.2"));
		trads.add(new Traduction("en", "creation.wand.first pos", "First position set.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.wand.second pos", "Second position set.", "1.1.3.2"));
		trads.add(new Traduction("en", "creation.wand.unknown block", "This block does not exist.", "2.0"));
		trads.add(new Traduction("en", "creation.wand.wrong block", "You are not allowed to use %block.", "2.0"));
		trads.add(new Traduction("en", "creation.wand.no selection", "You must select an area with your wand.", "2.1"));
		trads.add(new Traduction("en", "creation.wand.no worldedit", "This can not be done because the WorldEdit plugin is not installed on this server.", "2.0"));
		trads.add(new Traduction("en", "creation.wand.error", "Sorry, something went wrong with WorldEdit. Maybe a part of your selection is outside of the map (do not select the bedrock layer and barrier block walls). You can use \"//undo\".", "2.4.7"));
		trads.add(new Traduction("en", "creation.wand.error block", "Some blocks you are trying to place are not allowed in CreativeParkour. You can use \"//undo\" to go back.", "2.4.3"));
		trads.add(new Traduction("en", "creation.wand.selection error", "Your WorldEdit selection is not valid because it contains blocks that are not inside your map. Maybe you selected the bedrock layer or barrier block walls.", "2.4.7"));

		trads.add(new Traduction("en", "play.title", "Choose a map to play", "2.0"));
		trads.add(new Traduction("en", "play.page", "Page", "1.1.3.2"));
		trads.add(new Traduction("en", "play.sort name", "Sort by name", "2.0"));
		trads.add(new Traduction("en", "play.sort creator", "Sort by creator", "2.0"));
		trads.add(new Traduction("en", "play.sort difficulty", "Sort by difficulty", "2.0"));
		trads.add(new Traduction("en", "play.ascending sorting", "Ascending sorting", "2.0"));
		trads.add(new Traduction("en", "play.descending sorting", "Descending sorting", "2.0"));
		trads.add(new Traduction("en", "play.show local", "Show local maps", "2.0"));
		trads.add(new Traduction("en", "play.hide local", "Hide local maps", "2.0"));
		trads.add(new Traduction("en", "play.show downloadable", "Show downloadable maps", "2.0"));
		trads.add(new Traduction("en", "play.hide downloadable", "Hide downloadable maps", "2.0"));
		trads.add(new Traduction("en", "play.difficulty", "Difficulty", "2.0"));
		trads.add(new Traduction("en", "play.downloaded", "Downloaded", "2.0"));
		trads.add(new Traduction("en", "play.downloadable", "Downloadable (right-click for info)", "2.0.1"));
		trads.add(new Traduction("en", "play.download map info", "%LClick here%L to read this map's description on " + CreativeParkour.lienSiteCourt() + " (screenshot, description, leaderboard...).", "2.4.3"));
		trads.add(new Traduction("en", "play.maps", "maps", "1.1.3.2"));
		trads.add(new Traduction("en", "play.welcome", "You are playing %map, a CreativeParkour map by %creator.", "2.6"));
		trads.add(new Traduction("en", "play.time", "Time", "1.1.3.2"));
		trads.add(new Traduction("en", "play.leaderboard", "Leaderboard", "1.1.3.2"));
		trads.add(new Traduction("en", "play.leaderboard ticks", "Leaderboard (ticks)", "1.2.3"));
		trads.add(new Traduction("en", "play.timer reset", "Your timer has been reset, you have to return to start to continue this parkour.", "2.4.7"));
		trads.add(new Traduction("en", "play.time record", "You beat your record on this map!", "1.2"));
		trads.add(new Traduction("en", "play.time server record", "You broke %player's record on this map! Congratulations!", "1.2"));
		trads.add(new Traduction("en", "play.timer error", "Sorry, the server is laggy and your time can not be properly calculated, you completed the parkour in %ticks ticks (%tsec seconds) but it should be %seconds seconds.", "2.1"));
		trads.add(new Traduction("en", "play.timer error log", "The server seems laggy, %player's time was not saved (they completed a parkour in %ticks ticks (%tsec seconds) but it should be %seconds seconds). Check /tps", "2.1"));
		trads.add(new Traduction("en", "play.record notification", "%player broke your record in %map! %LClick here%L to play.", "2.6"));
		trads.add(new Traduction("en", "play.checkpoint validated", "Checkpoint validated!", "1.2"));
		trads.add(new Traduction("en", "play.checkpoint validated diff", "Checkpoint! Difference with %player: %diffs", "2.1.2.2"));
		trads.add(new Traduction("en", "play.checkpoint validated diff personal", "Checkpoint! Difference with your personal best: %diffs", "2.1.2.2"));
		trads.add(new Traduction("en", "play.checkpoint validated diff 2", "Checkpoint! Difference with %player and your personal best: %diffs / %diff2s", "2.1.2.2"));
		trads.add(new Traduction("en", "play.checkpoint validated diff reduced", "%player: %diffs", "2.2.2"));
		trads.add(new Traduction("en", "play.checkpoint validated diff personal reduced", "PB: %diffs", "2.2.2"));
		trads.add(new Traduction("en", "play.checkpoint validated diff 2 reduced", "%player / PB: %diffs / %diff2s", "2.2.2"));
		trads.add(new Traduction("en", "play.checkpoints error", "You must validate all the checkpoints to complete the parkour.", "1.1.3.2"));
		trads.add(new Traduction("en", "play.finish", "You have completed the parkour in %time seconds! Type '/cp leave' to leave.", "1.2.3"));
		trads.add(new Traduction("en", "play.items.return start", "Return to start", "1.1.3.2"));
		trads.add(new Traduction("en", "play.items.return checkpoint", "Return to last checkpoint", "1.1.3.2"));
		trads.add(new Traduction("en", "play.items.spectator", "Toggle spectator mode", "1.1.3.2"));
		trads.add(new Traduction("en", "play.items.player visibility", "Change player visibility", "2.2"));
		trads.add(new Traduction("en", "play.items.leaderboard", "Toggle leaderboard's accuracy", "1.2.3"));
		trads.add(new Traduction("en", "play.items.map options", "Map options", "1.1.3.2"));
		trads.add(new Traduction("en", "play.items.ghosts", "Player ghosts selection", "2.2"));
		trads.add(new Traduction("en", "play.items.leave test", "Leave test", "2.2.1"));
		trads.add(new Traduction("en", "play.items.right click", "right click", "1.1.3.2"));
		trads.add(new Traduction("en", "play.players visible", "Other players are now visible.", "2.2"));
		trads.add(new Traduction("en", "play.players transparent", "Other players are now transparent.", "2.2"));
		trads.add(new Traduction("en", "play.players invisible", "Other players are now invisible.", "2.2"));
		trads.add(new Traduction("en", "play.wait", "You must wait a few seconds before using this.", "2.2"));
		trads.add(new Traduction("en", "play.spectator on", "You are now in spectator mode. Left click on a player to watch them (sneak to stop).", "2.0.1"));
		trads.add(new Traduction("en", "play.spectator disable", "Type \"/cp spectator\" or \"/cp spec\" to leave spectator mode.", "2.3.1"));
		trads.add(new Traduction("en", "play.spectator off", "You no longer are in spectator mode.", "1.2"));
		trads.add(new Traduction("en", "play.your record", "Your record", "1.1.3.2"));
		trads.add(new Traduction("en", "play.no tp", "You can't teleport here.", "1.1.3.2"));
		trads.add(new Traduction("en", "play.leaderboard ticks info", "The leaderboard now displays times in ticks. 1 tick = 0.05 seconds", "1.2.3"));
		trads.add(new Traduction("en", "play.difficulty question", "How was this map?", "2.0"));
		trads.add(new Traduction("en", "play.difficulty click", "Click to vote", "2.0"));
		trads.add(new Traduction("en", "play.difficulty very easy", "Very easy", "2.0"));
		trads.add(new Traduction("en", "play.difficulty easy", "Easy", "2.0"));
		trads.add(new Traduction("en", "play.difficulty medium", "Medium", "2.0"));
		trads.add(new Traduction("en", "play.difficulty hard", "Hard", "2.0"));
		trads.add(new Traduction("en", "play.difficulty extreme", "Extreme", "2.0"));
		trads.add(new Traduction("en", "play.difficulty wait", "Please wait a few seconds, you will be asked to click a link if it is your first rating today. If you don't do it, your rating will not be saved.", "2.1"));
		trads.add(new Traduction("en", "play.difficulty confirm", "To avoid spam and incorrect ratings, you have to confirm your rating on " + CreativeParkour.lienSiteCourt() + ". %LClick here%L to do it, it will take less than 1 minute! If you don't do it, your rating will not be saved.", "2.1"));
		trads.add(new Traduction("en", "play.difficulty ok", "Your rating has been registered. Thank you!", "2.0"));
		trads.add(new Traduction("en", "play.difficulty error", "You already rated this map. Thank you!", "2.0"));
		trads.add(new Traduction("en", "play.error protocollib", "Sorry, this feature is not available on this server because the required ProtocolLib plugin is not installed.", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.ghosts", "ghosts", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.error disabled", "Sorry, ghosts are not enabled on this server. Tell an admin to edit the CreativeParkour configuration.", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.title", "Ghost selection", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.date", "Date", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.clear", "Clear selection", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.selected", "Selected", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.select", "Click to select this ghost.", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.unselect", "Click to unselect this ghost.", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.select personal", "Automatically select your personal ghost", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.select best", "Automatically select the best ghost", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.upload", "Upload ghosts to " + CreativeParkour.lienSiteCourt(), "2.2"));
		trads.add(new Traduction("en", "play.ghosts.upload info", "Only in downloaded or shared maps", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.display before", "Show ghosts 1 second before you", "2.2.2"));
		trads.add(new Traduction("en", "play.ghosts.display before info", "To see their route better", "2.2.2"));
		trads.add(new Traduction("en", "play.ghosts.enabled", "enabled", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.disabled", "disabled", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.help item", "Click to show information about ghosts.", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.help text", "A ghost is a recording of a player's parkour performance. You can select several ghosts that will be displayed as transparent players when you start the parkour. There are also options to automatically select ghosts, right click on the player head in your inventory to see them and choose the ghosts you want to see.", "2.2"));
		trads.add(new Traduction("en", "play.ghosts.report item", "Report a cheater ghost", "2.2.1"));
		trads.add(new Traduction("en", "play.ghosts.report item info", "Only for downloaded ghosts", "2.2.1"));
		trads.add(new Traduction("en", "play.ghosts.report link", "%LClick here%L to select the ghost you want to report.", "2.2.1"));
		trads.add(new Traduction("en", "play.ghosts.limit", "You cannot select that much ghosts.", "2.2.1"));
		trads.add(new Traduction("en", "play.sneak disabled", "Sneaking is disabled in this map.", "2.4"));
		trads.add(new Traduction("en", "play.deadly lava", "Lava kills you in this map.", "2.4"));
		trads.add(new Traduction("en", "play.deadly water", "Water kills you in this map.", "2.4"));
		trads.add(new Traduction("en", "play.interactions disabled", "Door and trapdoor interactions are disabled in this map.", "2.4.6"));
		trads.add(new Traduction("en", "play.interactions disabled when playing", "Door and trapdoor interactions will be disabled when playing this map.", "2.4.6"));
		trads.add(new Traduction("en", "play.elytra received", "You received Elytra.", "2.5"));
		trads.add(new Traduction("en", "play.elytra removed", "Your Elytra were removed.", "2.5"));

		trads.add(new Traduction("en", "signs.error", "'%type' type not recognized.", "1.1.3.2"));
		trads.add(new Traduction("en", "signs.create", "Create", "1.1.3.2"));
		trads.add(new Traduction("en", "signs.play", "Play", "1.1.3.2"));
		trads.add(new Traduction("en", "signs.map", "Map", "1.1.3.2"));
		trads.add(new Traduction("en", "signs.leaderboard", "Leaderboard", "2.1.1"));
		trads.add(new Traduction("en", "signs.right click", "Right click", "1.1.3.2"));
		trads.add(new Traduction("en", "signs.unknown map", "Unknown map", "2.1.2"));
		
		trads.add(new Traduction("en", "rewards.new", "You got 1 new reward:", "2.4"));
		trads.add(new Traduction("en", "rewards.claim", "Type \"/cp claim\" outside of CreativeParkour to get your rewards.", "2.4"));
		trads.add(new Traduction("en", "rewards.xp", "%amount XP points", "2.4"));
		trads.add(new Traduction("en", "rewards.info", "You have %nb unclaimed reward(s)!", "2.4"));
		trads.add(new Traduction("en", "rewards.get them", "Get them", "2.4"));
		trads.add(new Traduction("en", "rewards.get them hover", "Click to claim your rewards", "2.4"));
		trads.add(new Traduction("en", "rewards.stop", "Stop telling me", "2.4"));
		trads.add(new Traduction("en", "rewards.stop hover", "Click to disable these messages", "2.4"));
		trads.add(new Traduction("en", "rewards.info disabled", "Reward messages disabled. Type \"/cp claim messages\" to enable them again.", "2.4"));
		trads.add(new Traduction("en", "rewards.info enabled", "Reward messages enabled.", "2.4"));
		trads.add(new Traduction("en", "rewards.received", "You received %reward!", "2.4"));
		trads.add(new Traduction("en", "rewards.money error", "Could not give you money, this error is not caused by CreativeParkour.", "2.4"));
		trads.add(new Traduction("en", "rewards.item lore", "CreativeParkour reward", "2.4"));
		
		
		// Français
		trads.add(new Traduction("fr", "documentation link", "https://creativeparkour.net/doc/", "2.2.2.2"));
		trads.add(new Traduction("fr", "not allowed", "Vous n'êtes pas autorisé à faire cela.", "1.2"));
		trads.add(new Traduction("fr", "leave", "Vous avez quitté " + CreativeParkour.getNom() + ".", "1.2"));
		trads.add(new Traduction("fr", "ban", "Vous n'êtes pas autorisé à utiliser " + CreativeParkour.getNom() + ".", "1.2"));
		trads.add(new Traduction("fr", "ban error 1", "Ce joueur est déjà banni.", "1.2"));
		trads.add(new Traduction("fr", "ban error 3", "Ce joueur n'est pas banni.", "1.2"));
		trads.add(new Traduction("fr", "player offline", "Ce joueur est introuvable.", "1.2"));
		trads.add(new Traduction("fr", "unknown player", "%player est inconnu.", "2.1"));
		trads.add(new Traduction("fr", "inventory error", "Vous devez vider votre inventaire avant d'entrer dans cette map de parcours (l'armure aussi). Une fois que c'est fait, réessayez d'entrer dans la map.", "2.0"));
		trads.add(new Traduction("fr", "location error", "Vous devez quitter la carte dans laquelle vous êtes pour faire cela. Utilisez la commande '/cp leave'.", "1.2"));
		trads.add(new Traduction("fr", "location error 2", "Vous ne pouvez pas faire cela ici.", "2.0"));
		trads.add(new Traduction("fr", "location error 3", "Vous devez être en train de construire une map pour faire cela.", "2.4"));
		trads.add(new Traduction("fr", "too many players", "Il ya trop de joueurs là où vous voulez aller. Réessayez plus tard.", "1.2"));
		trads.add(new Traduction("fr", "too many maps", "YVous avez créé assez de maps sur ce serveur. Contactez un administrateur pour savoir comment supprimer cette limite.", "2.1"));
		trads.add(new Traduction("fr", "pinned", "Épinglée", "1.2"));
		trads.add(new Traduction("fr", "waiting for servers", "En attente des serveurs...", "2.0"));
		trads.add(new Traduction("fr", "error", "Désolé, une erreur est survenue en exécutant cette commande, elle sera rapportée.", "2.0"));
		trads.add(new Traduction("fr", "http error", "Une erreur est survenue en contactant " + CreativeParkour.lienSiteCourt() + " (%error).", "2.0"));
		trads.add(new Traduction("fr", "http error maintenance", "Désolé, " + CreativeParkour.lienSiteCourt() + " est en mode maintenance, veuillez réessayer plus tard.", "2.0"));
		trads.add(new Traduction("fr", "feature disabled", "Désolé, l'administrateur de ce serveur n'a pas activé cette fonctionnalité. :(", "2.0"));
		trads.add(new Traduction("fr", "online disabled", "Désolé, les fonctionnalités en ligne sont désactivées sur ce serveur. Si vous êtes un administrateur, tapez \"/cp config sharing\" pour les activer.", "2.4.4"));
		trads.add(new Traduction("fr", "outdated server", "Désolé, cette fonctionnalité n'est disponible que sur les serveurs en %ver (ou plus).", "2.2"));
		trads.add(new Traduction("fr", "feedback 1", "Vous aimez CreativeParkour ? Le débeloppeur aimerait que la qualité du plugin soit maximale, %Lcliquez ici%L pour envoyer des commentaires et des idées pour améliorer CreativeParkour. Merci ! :)", "2.2.3"));
		trads.add(new Traduction("fr", "feedback 2", "Avez-vous des idées pour améliorer ce constructeur de map ? Envoyez-les (même les plus folles) au développeur de CreativeParkour en %Lcliquant ici%L. Merci ! :)", "2.2.3"));

		trads.add(new Traduction("fr", "commands.help", "Tapez '/cp help' pour afficher l'aide.", "1.2"));
		trads.add(new Traduction("fr", "commands.unknown", "Commande inconnue.", "1.2"));
		trads.add(new Traduction("fr", "commands.player", "Seuls les joueurs peuvent utiliser cette commande.", "1.2"));
		trads.add(new Traduction("fr", "commands.playerN", "joueur", "1.2"));
		trads.add(new Traduction("fr", "commands.question", "Que souhaitez-vous faire ?", "1.2"));
		trads.add(new Traduction("fr", "commands.yes no", "Tapez '/cp yes' ou '/cp no' pour répondre.", "1.2"));
		trads.add(new Traduction("fr", "commands.yes", "oui", "1.2"));
		trads.add(new Traduction("fr", "commands.no", "non", "1.2"));
		trads.add(new Traduction("fr", "commands.click", "cliquez", "1.2"));
		trads.add(new Traduction("fr", "commands.click to answer", "Cliquez pour répondre %answer", "1.2"));
		trads.add(new Traduction("fr", "commands.no answer", "Nous n'attendons pas de réponse de votre part...", "1.2"));
		trads.add(new Traduction("fr", "commands.play", "jouer", "1.2"));
		trads.add(new Traduction("fr", "commands.play tip", "Vous pouvez directement jouer sur une map en tapant '/cp jouer [nom de la map]'", "2.2.2"));
		trads.add(new Traduction("fr", "commands.play error", "Cette map n'existe pas.", "1.2"));
		trads.add(new Traduction("fr", "commands.map name", "nom de la map", "1.2"));
		trads.add(new Traduction("fr", "commands.create", "créer", "1.2"));
		trads.add(new Traduction("fr", "commands.create map error", "Cette map n'existe pas ou n'est pas en mode création.", "2.3.3"));
		trads.add(new Traduction("fr", "commands.invite", "inviter", "1.2"));
		trads.add(new Traduction("fr", "commands.invite error 1", "Vous devez être en train de construire une map pour inviter ou retirer quelqu'un ou voir la liste des contributeurs.", "2.5.1"));
		trads.add(new Traduction("fr", "commands.invite error 2", "Vous avez déjà invité ce joueur. Il doit accepter votre invitation puis taper '/cp create' et cliquer sur 'autres maps' pour venir.", "2.0"));
		trads.add(new Traduction("fr", "commands.invite error 3", "Ce joueur est le créateur de la map...", "2.5.1"));
		trads.add(new Traduction("fr", "commands.invite error 4", "Seul le créateur de la map peut faire cela.", "2.5.1"));
		trads.add(new Traduction("fr", "commands.invite error 5", "Ce joueur n'a pas le droit de construire une map.", "1.2"));
		trads.add(new Traduction("fr", "commands.remove", "retirer", "1.2"));
		trads.add(new Traduction("fr", "commands.remove error", "Ce joueur ne peut déjà pas construire la map avec vous.", "1.2"));
		trads.add(new Traduction("fr", "commands.remove ok", "%player ne peut plus construire cette map avec vous.", "1.2"));
		trads.add(new Traduction("fr", "commands.contributors", "contributeurs", "2.5.1"));
		trads.add(new Traduction("fr", "commands.contributors info", "Tapez \"/cp contributeurs\" pour voir la liste des joueurs autorisés à construire votre map avec vous.", "2.5.1"));
		trads.add(new Traduction("fr", "commands.contributors empty", "Vous n'avez invité personne à contruire votre map.", "2.5.1"));
		trads.add(new Traduction("fr", "commands.contributors invite", "Tapez \"/cp inviter <joueur>\" pour inviter un joueur.", "2.5.1"));
		trads.add(new Traduction("fr", "commands.contributors list", "Joueurs autorisés à construire cette map :", "2.5.1"));
		trads.add(new Traduction("fr", "commands.contributors remove info", "Cliquez pour retirer à %player l'autorisation de construire cette map.", "2.5.1"));
		trads.add(new Traduction("fr", "commands.contributors pending", "invitation en attente", "2.5.1"));
		trads.add(new Traduction("fr", "commands.tp", "tp", "1.2.2"));
		trads.add(new Traduction("fr", "commands.tp error", "Ce joueur n'est pas en train de jouer dans une map de parkour.", "1.2.2"));
		trads.add(new Traduction("fr", "commands.tp error creation", "Ce joueur est en train de créer une map, il faut qu'il vous invite en utilisant \"/cp inviter\".", "2.1.2.1"));
		trads.add(new Traduction("fr", "commands.tp info", "Si vous souhaitez vous téléporter à un joueur qui joue dans une map de parcours, utilisez la commande \"/cp tp <joueur>\".", "1.2.5"));
		trads.add(new Traduction("fr", "commands.spectator", "spectateur", "2.0"));
		trads.add(new Traduction("fr", "commands.fill", "remplir", "1.2"));
		trads.add(new Traduction("fr", "commands.undo", "défaire", "2.0"));
		trads.add(new Traduction("fr", "commands.redo", "refaire", "2.0"));
		trads.add(new Traduction("fr", "commands.leave", "quitter", "1.2"));
		trads.add(new Traduction("fr", "commands.leave error", "Vous n'êtes pas dans une map de " + CreativeParkour.getNom() + ".", "1.2"));
		trads.add(new Traduction("fr", "commands.leave only", "Vous devez utiliser \"/cp quitter\" pour quitter " + CreativeParkour.getNom() + ".", "2.4.4.2"));
		trads.add(new Traduction("fr", "commands.test", "tester", "1.2"));
		trads.add(new Traduction("fr", "commands.test error", "Vous ne pouvez pas faire cela pour le moment.", "1.2"));
		trads.add(new Traduction("fr", "commands.publish", "publier", "1.2"));
		trads.add(new Traduction("fr", "commands.publish name", "nom", "1.2"));
		trads.add(new Traduction("fr", "commands.publish error", "Vous devez valider votre map avant de la publier. Tapez '/cp test'.", "1.2"));
		trads.add(new Traduction("fr", "commands.publish error name", "Veuillez choisir le nom de votre map.", "1.2"));
		trads.add(new Traduction("fr", "commands.publish error name 2", "Ce nom est déjà pris par une autre map.", "1.2"));
		trads.add(new Traduction("fr", "commands.publish error creator", "Seul %creator est autorisé à publier cette map.", "1.2.7"));
		trads.add(new Traduction("fr", "commands.share", "partager", "2.0"));
		trads.add(new Traduction("fr", "commands.share announcement", "Vous pouvez maintenant partager votre map en ligne avec la communauté, montrer vos talents de constructeur et défier des fantômes de joueurs du monde entier ! Cela utilise le nouveau site " + CreativeParkour.lienSiteCourt() + " et ça prend moins de 5 minutes.\nTapez \"/cp partager\".", "2.4.2.1"));
		trads.add(new Traduction("fr", "commands.share message", "Partager cette map sur " + CreativeParkour.lienSiteCourt(), "2.0"));
		trads.add(new Traduction("fr", "commands.share question", "Voulez-vous partager votre map sur " + CreativeParkour.lienSiteCourt() + " ?", "2.0"));
		trads.add(new Traduction("fr", "commands.share info", "Contribuez à la communauté, montrez aux joueurs du monde entier vos talents de constructeur de parcours et défiez leurs fantômes !", "2.4.2.1"));
		trads.add(new Traduction("fr", "commands.share canceled", "Partage de la map annulé.", "2.4.2.5"));
		trads.add(new Traduction("fr", "commands.share wait", "Envoi des données de la map, merci de patienter (ça peut durer un moment).", "2.0"));
		trads.add(new Traduction("fr", "commands.share new map", "Vous devez confirmer le partage de votre map sur " + CreativeParkour.lienSiteCourt() + ". %LCliquez ici%L pour le faire.", "2.0"));
		trads.add(new Traduction("fr", "commands.share unknown server", "Ce serveur n'est pas enregistré. Si vous êtes un administrateur, tapez \"/cp config sharing\" (vous pouvez aussi utiliser cette commande pour désactiver le téléchargement et partage de maps).", "2.2.3.1"));
		trads.add(new Traduction("fr", "commands.share error creator", "Seul %creator (le créateur de la map) est autorisé à partager cette map.", "2.0"));
		trads.add(new Traduction("fr", "commands.share error block", "Votre map ne peut pas être partagé car elle contient des blocs non autorisés.", "2.0"));
		trads.add(new Traduction("fr", "commands.share error existing map", "Vous avez déjà partagé votre map. %LCliquez ici%L pour modifier ses paramètres en ligne.", "2.0"));
		trads.add(new Traduction("fr", "commands.download", "télécharger", "2.0"));
		trads.add(new Traduction("fr", "commands.download loading", "Téléchargement des données de la map, merci de patienter (ça peut durer un moment).", "2.0"));
		trads.add(new Traduction("fr", "commands.download wait", "Veuillez attendre quelques minutes avant de retélécharger une map.", "2.0"));
		trads.add(new Traduction("fr", "commands.download wait 2", "Construction de \"%map\" (cela peut prendre du temps)...", "2.4.2.1"));
		trads.add(new Traduction("fr", "commands.download error build", "Un problème est survenu lors de la construction de la map. Elle a peut-être été faite dans une version plus récente de Minecraft ou CreativeParkour.", "2.3"));
		trads.add(new Traduction("fr", "commands.download error ID", "Vous devez indiquer l'identifiant numérique ou l'URL de la map que vous souhaitez télécharger : \"/cpd <identifiant ou URL>\". Vous pouvez le trouver dans la liste des maps sur " + CreativeParkour.lienSiteCourt() + ". Vous pouvez aussi utiliser la commande \"/cp play\".", "2.4.6.5"));
		trads.add(new Traduction("fr", "commands.download error disabled", "Le téléchargement de maps n'est pas activé sur ce serveur. Si vous êtes un opérateur, tapez \"/cp config sharing\".", "2.0"));
		trads.add(new Traduction("fr", "commands.edit", "modifier", "1.2"));
		trads.add(new Traduction("fr", "commands.edit message", "Modifier cette map", "1.2"));
		trads.add(new Traduction("fr", "commands.edit question", "Êtes-vous sûr(e) de vouloir modifier %map ?", "1.2"));
		trads.add(new Traduction("fr", "commands.edit info", "Vous ne pourrez plus modifier votre dernière map modifiée avant de publier %map de nouveau.\nSi vous modifiez un bloc, le classement des joueurs de cette map sera supprimé.", "1.2.2"));
		trads.add(new Traduction("fr", "commands.edit canceled", "Modification de la map annulée.", "1.2"));
		trads.add(new Traduction("fr", "commands.delete", "supprimer", "1.2"));
		trads.add(new Traduction("fr", "commands.delete message", "Supprimer cette map", "1.2"));
		trads.add(new Traduction("fr", "commands.edit delete error", "Vous devez être dans une map pour pouvoir la partager, la modifier, la supprimer ou l'exporter. Vous devez être son créateur ou avoir la permission \"creativeparkour.manage\".", "2.1.2"));
		trads.add(new Traduction("fr", "commands.delete deleted", "La map %map est supprimée.", "1.2"));
		trads.add(new Traduction("fr", "commands.delete question", "Êtes-vous sûr(e) de vouloir supprimer %map ?", "1.2"));
		trads.add(new Traduction("fr", "commands.delete canceled", "Suppression de la map annulée.", "1.2"));
		trads.add(new Traduction("fr", "commands.export", "exporter", "2.1"));
		trads.add(new Traduction("fr", "commands.export question", "Voulez-vous vraiment exporter %map ?", "2.1"));
		trads.add(new Traduction("fr", "commands.export info", "Un nouveau fichier sera créé dans ce dossier : <votre_serveur>/plugins/CreativeParkour/Exported maps\nLe fichier ne contiendra pas les données des temps des joueurs.", "2.1"));
		trads.add(new Traduction("fr", "commands.export canceled", "Export de la map annulé.", "2.1"));
		trads.add(new Traduction("fr", "commands.export success", "Un nouveau fichier a été créé pour cette map: %file", "2.1"));
		trads.add(new Traduction("fr", "commands.export error", "Vous ne pouvez pas exporter les maps téléchargées.", "2.1"));
		trads.add(new Traduction("fr", "commands.import success", "%map a été importée !", "2.1"));
		trads.add(new Traduction("fr", "commands.import error", "Une erreur est survenue lors de l'importation de %map, elle a peut-être été faite dans une version plus récente de Minecraft ou de CreativeParkour.", "2.3"));
		trads.add(new Traduction("fr", "commands.import error 2", "%file ne peut pas être importé car la map existe déjà.", "2.1"));
		trads.add(new Traduction("fr", "commands.cannot be undone", "Ne peut pas être annulé", "1.2"));
		trads.add(new Traduction("fr", "commands.pin", "épingler", "1.2"));
		trads.add(new Traduction("fr", "commands.pin error", "Il faut que vous vous trouviez dans la map que vous voulez épingler ou désépingler", "1.2"));
		trads.add(new Traduction("fr", "commands.pin success", "Cette map est maintenant épinglée.", "1.2"));
		trads.add(new Traduction("fr", "commands.unpin", "désépingler", "1.2"));
		trads.add(new Traduction("fr", "commands.unpin success", "Cette map n'est plus épinglée.", "1.2"));
		trads.add(new Traduction("fr", "commands.notifications", "notifications", "1.2"));
		trads.add(new Traduction("fr", "commands.notifications info", "Vous pouvez désactiver les notifications avec la commande \"/cp paramètres\".", "2.5"));
		trads.add(new Traduction("fr", "commands.notifications on", "Notifications activées.", "1.2"));
		trads.add(new Traduction("fr", "commands.notifications off", "Notifications désactivées, tapez \"/cp notifications\" pour les réactiver.", "1.2.3"));
		trads.add(new Traduction("fr", "commands.messages", "messages", "1.2.3"));
		trads.add(new Traduction("fr", "commands.messages choice", "Cliquez sur votre type de message préféré :", "2.2.2"));
		trads.add(new Traduction("fr", "commands.messages disable", "Désactiver ces messages", "2.2.2"));
		trads.add(new Traduction("fr", "commands.messages ok", "Paramètres des messages des points de contrôle mis à jour.", "2.2.2"));
		trads.add(new Traduction("fr", "commands.messages spec", "spectateur", "2.1.2.3"));
		trads.add(new Traduction("fr", "commands.messages spec on", "Messages de points de contrôle du mode spectateur activées.", "2.1.2.3"));
		trads.add(new Traduction("fr", "commands.messages spec off", "Messages de points de contrôle du mode spectateur désactivés, tapez \"/cp messages spectateur\" pour les réactiver.", "2.1.2.3"));
		trads.add(new Traduction("fr", "commands.messages spec to player", "À %player ", "2.1.2.3"));
		trads.add(new Traduction("fr", "commands.ban", "bannir", "1.2"));
		trads.add(new Traduction("fr", "commands.pardon", "pardonner", "1.2"));
		trads.add(new Traduction("fr", "commands.removetime", "supprimertemps", "2.1"));
		trads.add(new Traduction("fr", "commands.removetime ok", "Les temps de %player ont été supprimés.", "2.1"));
		trads.add(new Traduction("fr", "commands.removetime error", "Vous devez être dans la map de laquelle vous voulez que le temps soit supprimé.", "2.1"));
		trads.add(new Traduction("fr", "commands.getid", "getid", "2.1.2"));
		trads.add(new Traduction("fr", "commands.getid ok", "ID de la map %map : %id", "2.1.2"));
		trads.add(new Traduction("fr", "commands.getid error", "Map introuvable. Pour obtenir l'ID d'une map, allez dedans et tapez \"/cp getid\" ou alors directement \"/cp getid <nom de la map>\".", "2.1.2"));
		trads.add(new Traduction("fr", "commands.managemaps", "managemaps", "2.1.2"));
		trads.add(new Traduction("fr", "commands.managemaps info", "Liste de toutes les maps du serveur:", "2.1.2"));
		trads.add(new Traduction("fr", "commands.managemaps creator", "créateur", "2.1.2"));
		trads.add(new Traduction("fr", "commands.managemaps play", "y aller", "2.4"));
		trads.add(new Traduction("fr", "commands.managemaps delete", "supprimer", "2.1.2"));
		trads.add(new Traduction("fr", "commands.managemaps click", "Cliquez", "2.1.2"));
		trads.add(new Traduction("fr", "commands.mapoptions", "optionsmaps", "2.4"));
		trads.add(new Traduction("fr", "commands.mapoptions error", "Vous ne pouvez pas changer ces options car quelqu'un teste la map.", "2.4"));
		trads.add(new Traduction("fr", "commands.mapoptions title", "Options de la map", "2.4"));
		trads.add(new Traduction("fr", "commands.mapoptions sneak", "Accroupissement (sneak)", "2.4"));
		trads.add(new Traduction("fr", "commands.mapoptions sneak info", "Autoriser les joueurs à s'accroupir dans la map", "2.4"));
		trads.add(new Traduction("fr", "commands.mapoptions lava", "Lave mortelle", "2.4"));
		trads.add(new Traduction("fr", "commands.mapoptions lava info", "Tuer les joueurs lorsqu'ils touchent de la lave", "2.4"));
		trads.add(new Traduction("fr", "commands.mapoptions water", "Eau mortelle", "2.4"));
		trads.add(new Traduction("fr", "commands.mapoptions water info", "Tuer les joueurs lorsqu'ils touchent de l'eau", "2.4"));
		trads.add(new Traduction("fr", "commands.mapoptions interactions", "Intéractions", "2.4.6"));
		trads.add(new Traduction("fr", "commands.mapoptions interactions info", "Autoriser les joueurs à intéragir avec les portes et les trappes", "2.4.6"));
		trads.add(new Traduction("fr", "commands.claim", "recevoir", "2.4"));
		trads.add(new Traduction("fr", "commands.claim error", "Vous devez quitter CreativeParkour pour recevoir vos récompenses (tapez \"/cp quitter\")", "2.4"));
		trads.add(new Traduction("fr", "commands.claim error 2", "Vous n'avez aucune récompense disponible.", "2.4"));
		trads.add(new Traduction("fr", "commands.claim error 3", "Vous ne pouvez pas recevoir vos récompenses dans ce monde. Si vous êtes un administrateur, voyez la configuration du plugin.", "2.4.2"));
		trads.add(new Traduction("fr", "commands.importsel", "importersel", "2.4.3"));
		trads.add(new Traduction("fr", "commands.importsel error", "Vous devez sélectionner une zone avec WorldEdit avant d'utiliser cette commande pour l'importer dans une nouvelle map.", "2.4.3"));
		trads.add(new Traduction("fr", "commands.importsel error block", "Certains blocs interdits n'ont pas été copiés.", "2.4.3"));
		trads.add(new Traduction("fr", "commands.register", "inscription", "2.4.4"));
		trads.add(new Traduction("fr", "commands.register already", "Vous êtes déjà inscrit. Vous pouvez utiliser les fonctionnalités en ligne comme le partage de maps de parkour avec \"/cp partager\" !", "2.4.4"));
		trads.add(new Traduction("fr", "commands.register link", "%LCliquez ici%L pour terminer votre inscription sur " + CreativeParkour.lienSiteCourt() + ". Ça ne prend que quelques minutes.", "2.4.4"));
		trads.add(new Traduction("fr", "commands.sync", "sync", "2.4.7"));
		trads.add(new Traduction("fr", "commands.sync msg", "Synchronisation des données...", "2.4.7"));
		trads.add(new Traduction("fr", "commands.sync done", "La liste des maps, les noms des joueurs, leurs temps et fantômes sont à jour !", "2.4.7"));
		trads.add(new Traduction("fr", "commands.settings", "paramètres", "2.5"));
		trads.add(new Traduction("fr", "commands.settings info", "Vous pouvez à tout moment changer les paramètres des messages (points de contrôle, notifications...) avec \"/cp paramètres\".", "2.5"));
		trads.add(new Traduction("fr", "commands.settings title", "Paramètres de CreativeParkour", "2.5"));
		trads.add(new Traduction("fr", "commands.settings enabled", "Activé", "2.5"));
		trads.add(new Traduction("fr", "commands.settings disabled", "Désactivé", "2.5"));
		trads.add(new Traduction("fr", "commands.settings enable", "Cliquez pour activer", "2.5"));
		trads.add(new Traduction("fr", "commands.settings disable", "Cliquez pour désactiver", "2.5"));
		trads.add(new Traduction("fr", "commands.settings notifications", "Notifications", "2.5"));
		trads.add(new Traduction("fr", "commands.settings notifications info", "Afficher une notification dans le tchat quand un joueur bat votre record dans une map (seulement si vous étiez le premier du classement).", "2.5"));
		trads.add(new Traduction("fr", "commands.settings reward msg", "Message de récompenses", "2.5"));
		trads.add(new Traduction("fr", "commands.settings reward msg info", "Afficher un message qui vous rappelle de prendre vos récompenses si vous en avez.", "2.5"));
		trads.add(new Traduction("fr", "commands.settings elytra msg", "Message des élytres", "2.5"));
		trads.add(new Traduction("fr", "commands.settings elytra msg info", "Afficher un message quand vous recevez des élytres dans un parcours.", "2.5"));
		trads.add(new Traduction("fr", "commands.settings checkpoint msg", "Message de point de contrôle", "2.5"));
		trads.add(new Traduction("fr", "commands.settings checkpoint msg info", "Message affiché quand vous passez un point de contrôle (différences de temps entre vous et des records).", "2.5"));
		trads.add(new Traduction("fr", "commands.settings checkpoint msg full", "Long message", "2.5"));
		trads.add(new Traduction("fr", "commands.settings checkpoint msg reduced", "Message réduit", "2.5"));
		trads.add(new Traduction("fr", "commands.settings checkpoint msg none", "Pas de message", "2.5"));
		trads.add(new Traduction("fr", "commands.settings checkpoint msg click", "Cliquez pour choisir votre type de message préféré", "2.5"));
		trads.add(new Traduction("fr", "commands.settings checkpoint msg spec", "Messages de points de contrôle (mode spectateur)", "2.5"));
		trads.add(new Traduction("fr", "commands.settings checkpoint msg spec info", "Afficher un message quand le joueur que vous regardez passe un point de contrôle (avec les différences de temps).", "2.5"));

		trads.add(new Traduction("fr", "help.title", "Aide", "1.2"));
		trads.add(new Traduction("fr", "help.page", "Page", "1.2"));
		trads.add(new Traduction("fr", "help.page command", "/cp help [n] pour voir la page d'aide n", "1.2"));
		trads.add(new Traduction("fr", "help.help", "Affiche l'aide de " + CreativeParkour.getNom() +".", "1.2"));
		trads.add(new Traduction("fr", "help.play", "Jouer sur une map de parcours.", "1.2"));
		trads.add(new Traduction("fr", "help.download", "Télécharger une map depuis " + CreativeParkour.lienSiteCourt() + ".", "2.0"));
		trads.add(new Traduction("fr", "help.create", "Créer une nouvelle map de parcours.", "1.2"));
		trads.add(new Traduction("fr", "help.invite", "Inviter quelqu'un à construire une map avec vous.", "1.2"));
		trads.add(new Traduction("fr", "help.remove", "Interdire à quelqu'un de construire la map avec vous.", "1.2"));
		trads.add(new Traduction("fr", "help.contributors", "Afficher la liste des joueurs invités dans votre map.", "2.5.1"));
		trads.add(new Traduction("fr", "help.settings", "Changer vos paramètres de CreativeParkour", "2.5"));
		trads.add(new Traduction("fr", "help.tp", "Se téléporter à quelqu'un qui joue dans une map de parcours.", "1.2.4"));
		trads.add(new Traduction("fr", "help.spectator", "Inverser le mode spectateur dans une map.", "2.0"));
		trads.add(new Traduction("fr", "help.fill", "Remplir une zone sélectionnée avec le bâton de WorldEdit.", "2.0"));
		trads.add(new Traduction("fr", "help.undo", "Déraire la dernière action faite avec le bâton WorldEdit.", "2.0"));
		trads.add(new Traduction("fr", "help.redo", "Refaire la dernière action défaite.", "2.0"));
		trads.add(new Traduction("fr", "help.config", "Configurer le plugin.", "1.2"));
		trads.add(new Traduction("fr", "help.reload", "Recharger les fichiers de configuration du plugin (sauf 'maps.yml').", "1.2"));
		trads.add(new Traduction("fr", "help.leave", "Quitter une map de " + CreativeParkour.getNom() + ".", "1.2"));
		trads.add(new Traduction("fr", "help.test", "Tester un parcours lors de sa création.", "1.2"));
		trads.add(new Traduction("fr", "help.publish", "Publier une map.", "1.2"));
		trads.add(new Traduction("fr", "help.share", "Partager une map sur " + CreativeParkour.lienSiteCourt() + ".", "2.0"));
		trads.add(new Traduction("fr", "help.edit", "Modifier une map.", "1.2"));
		trads.add(new Traduction("fr", "help.delete", "Supprimer une map.", "1.2"));
		trads.add(new Traduction("fr", "help.getid", "Affiche l'ID de la map où vous êtes ou indiquée.", "2.1.2"));
		trads.add(new Traduction("fr", "help.export", "Créer un fichier contenant les données d'une map.", "2.1"));
		trads.add(new Traduction("fr", "help.managemaps", "Affiche la liste des maps avec des actions rapides.", "2.1.2"));
		trads.add(new Traduction("fr", "help.enable", "Activer " + CreativeParkour.getNom() + ".", "1.2"));
		trads.add(new Traduction("fr", "help.disable", "Désactiver " + CreativeParkour.getNom() + ".", "1.2"));
		trads.add(new Traduction("fr", "help.ban", "Interdit au joueur d'utiliser le plugin.", "1.2"));
		trads.add(new Traduction("fr", "help.pardon", "Autorise le joueur à utiliser le plugin.", "1.2"));
		trads.add(new Traduction("fr", "help.pin", "Épingler une map dans la liste des maps.", "1.2"));
		trads.add(new Traduction("fr", "help.unpin", "Désépingler une map.", "1.2"));
		trads.add(new Traduction("fr", "help.notifications", "Activer ou désactiver les notifications.", "1.2"));
		trads.add(new Traduction("fr", "help.messages", "Réduire ou désactiver les messages de validation des points de contrôle.", "2.2.2"));
		trads.add(new Traduction("fr", "help.removetime", "Supprimer le temps d'un joueur (ajouter \"all\" après son nom pour le supprimer de toutes les maps).", "2.1"));
		trads.add(new Traduction("fr", "help.mapoptions", "Activer des options spéciales pour votre map.", "2.4"));
		trads.add(new Traduction("fr", "help.claim", "Recevoir les récompenses obtenues dans les maps.", "2.4"));
		trads.add(new Traduction("fr", "help.importsel", "Importer une sélection WorldEdit dans une nouvelle map.", "2.4.3"));
		trads.add(new Traduction("fr", "help.register", "S'inscrire sur " + CreativeParkour.lienSiteCourt(), "2.4.4"));
		trads.add(new Traduction("fr", "help.sync", "Synchroniser les données avec " + CreativeParkour.lienSiteCourt(), "2.4.7"));

		trads.add(new Traduction("fr", "config.plugin not enabled", "Le plugin ne fonctionne pas pour le moment car il n'est pas encore configuré.", "1.2"));
		trads.add(new Traduction("fr", "config.config tutorial", "Pour configurer le plugin, tapez \"/cp config\".", "2.4.3"));
		trads.add(new Traduction("fr", "config.next", "Suivant", "2.4.3"));
		trads.add(new Traduction("fr", "config.next hover", "Cliquez pour passer à l'étape suivante", "2.4.3"));
		trads.add(new Traduction("fr", "config.start", "Bienvenue dans CreativeParkour! Nous allons vous aider à rapidement configurer le plugin pour commencer à vous amuser dans des maps de parcours uniques. Cliquez sur \"suivant\" en dessous pour commencer.", "2.4.3"));
		trads.add(new Traduction("fr", "config.storage.text", "CreativeParkour va créer un nouveau monde sur votre serveur pour y stocker les maps de parcours. Êtes-vous d'accord ? Cliquez sur OUI ou NON en dessous.", "2.4.3"));
		trads.add(new Traduction("fr", "config.storage.info", "Si vous cliquez sur NON, vous devrez configurer le monde de stockage vous-même, cela est déconseillé.", "2.4.3"));
		trads.add(new Traduction("fr", "config.storage.default world", "Un nouveau monde a été créé dans le dossier de votre serveur. Cliquez sur \"suivant\" plus bas pour continuer la configuration de CreativeParkour.", "2.4.3"));
		trads.add(new Traduction("fr", "config.storage.no default world", "Vous avez choisi de ne pas utiliser le monde de stockage par défaut. Vous devez donc éditer le fichier \"your_server/plugins/" + CreativeParkour.getNom() + "/configuration.yml\" pour choisir quel monde va contenir les maps de parcours. Vous devez aussi donner les coordonnées à partir desquelles les maps seront placées dans le monde.", "2.4.3"));
		trads.add(new Traduction("fr", "config.storage.no default world back", "Si vous voulez finalement utiliser le monde de stockage par défaut, tapez \"/cp config storage\" pour revenir en arrière.", "2.4.3"));
		trads.add(new Traduction("fr", "config.storage.no default world doc", "Vous pouvez trouver davantage d'informations sur le fonctionnement du stockage des maps sur", "1.1.3.2"));
		trads.add(new Traduction("fr", "config.storage.world creating", "Création du monde...", "1.1.3.2"));
		trads.add(new Traduction("fr", "config.storage.warn", "Attention : si vous modifiez la configuration du stockage des maps, vous risquez de perdre vos maps ou elles seront corrompues et irrécupérables.", "2.4.3"));
		trads.add(new Traduction("fr", "config.dependencies.info", "L'installation des plugins suivants est fortement recommandée pour profiter de toutes les fonctionnalités de CreativeParkour (cliquez dessus pour aller sur leurs pages officielles):", "2.4.3"));
		trads.add(new Traduction("fr", "config.dependencies.protocollib", "ProtocolLib (pour activer les fantômes et modifier la visibilité des joueurs)", "2.4.3"));
		trads.add(new Traduction("fr", "config.dependencies.vault", "Vault (pour corriger des problèmes de permissions avec des plugins comme PermissionsEx et utiliser des récompenses d'argent)", "2.4.3"));
		trads.add(new Traduction("fr", "config.dependencies.worldedit", "WorldEdit (pour construire les maps plus facilement et importer vos anciens parcours dans CreativeParkour)", "2.4.3"));
		trads.add(new Traduction("fr", "config.permissions", "Par défaut, la majorité des fonctionnalités de CreativeParkour sont accessibles à tous les joueurs, mais de nombreuses permissions permettent de changer cela. Consultez cette page pour plus d'infos : https://creativeparkour.net/doc/permissions.php", "2.4.3"));
		trads.add(new Traduction("fr", "config.sharing.text", "CreativeParkour permet de télécharger gratuitement des maps faites par la communauté où tout le monde peut affronter des fantômes et montrer ses talents de parcours ! Pour activer ces fonctionnalités et pouvoir partager vos maps sur creativeparkour.net, veuillez cliquer sur OUI pour enregistrer votre serveur, ou cliquez sur NON pour désactiver cela.", "2.4.3"));
		trads.add(new Traduction("fr", "config.sharing.announcement 2", "Cette map a été téléchargée depuis " + CreativeParkour.lienSiteCourt() + ", pourquoi ne pas partager une de vos maps aussi ? %LCliquez ici%L pour découvrir comment faire.", "2.2.0.3"));
		trads.add(new Traduction("fr", "config.sharing.description", "Tapez '/cp config sharing' pour activer le téléchargement et partage de maps en ligne.", "2.1"));
		trads.add(new Traduction("fr", "config.sharing.enabled", "Le partage en ligne des maps est activé. Veuillez patienter pour l'enregistrement de votre serveur sur " + CreativeParkour.lienSiteCourt() + ".", "2.0"));
		trads.add(new Traduction("fr", "config.sharing.disabled", "Ok... Le partage en ligne des maps est désactivé, dommage. Tapez \"/cp config sharing\" à nouveau si vous changez d'avis.", "2.0"));
		trads.add(new Traduction("fr", "config.sharing.new server", "Vous devez enregistrer votre serveur sur " + CreativeParkour.lienSiteCourt() + ". %LCliquez ici%L pour le faire.", "2.0"));
		trads.add(new Traduction("fr", "config.sharing.server already registered", "Votre serveur est déjà enregistré. %LCliquez ici%L pour modifier ses paramètres en ligne.", "2.0"));
		trads.add(new Traduction("fr", "config.sharing.done", "Quand vous avez fini, cliquez sur \"suivant\" pour terminer la configuration.", "2.4.3"));
		trads.add(new Traduction("fr", "config.end", "Voilà, CreativeParkour est installé sur votre serveur ! Vous pouvez commencer la construction de votre première map de parcours avec \"/cp créer\", ou importer des parcours déjà existants avec \"/cp importersel\", ou télécharger et jouer sur des maps avec \"/cp jouer\". CreativeParkour a plein d'autres fonctionnalités que vous pouvez découvrir dans la documentation officielle sur https://creativeparkour.net/doc\nMerci d'avoir téléchargé le plugin, amusez-vous bien !", "2.4.3"));
		trads.add(new Traduction("fr", "config.plugin disabled", CreativeParkour.getNom() + " a été désactivé par un opérateur.", "1.2"));
		trads.add(new Traduction("fr", "config.missing", "Il manque les paramètres suivants dans le fichier 'configuration.yml'", "1.2"));
		trads.add(new Traduction("fr", "config.reload", "Configuration chargée !", "1.2"));
		trads.add(new Traduction("fr", "config.enable", CreativeParkour.getNom() + " est activé.", "1.2"));
		trads.add(new Traduction("fr", "config.disable", CreativeParkour.getNom() + " est désactivé.", "1.2"));

		trads.add(new Traduction("fr", "creation.title", "Création", "1.2"));
		trads.add(new Traduction("fr", "creation.new map", "Créer une nouvelle map", "1.2"));
		trads.add(new Traduction("fr", "creation.new map warn", "La dernière map modifiée va être supprimée !", "1.2"));
		trads.add(new Traduction("fr", "creation.load map", "Charger la dernière map en cours de création", "1.2"));
		trads.add(new Traduction("fr", "creation.other maps", "Autres maps (invitations)", "1.2"));
		trads.add(new Traduction("fr", "creation.other maps creator", "Map de %creator", "2.4.2.3"));
		trads.add(new Traduction("fr", "creation.no map", "Vous n'avez pas de map à modifier.", "1.2"));
		trads.add(new Traduction("fr", "creation.erase question", "Êtes-vous sûr(e) de vouloir supprimer la dernière map que vous avez modifié ?", "1.2"));
		trads.add(new Traduction("fr", "creation.erase question import", "Vous ne pouvez créer qu'une map à la fois, la dernière map que vous avez modifié va donc être supprimée et remplacée par celle que vous importez. Voulez-vous continuer?", "2.4.3"));
		trads.add(new Traduction("fr", "creation.canceled", "Création d'une nouvelle map annulée.", "1.2"));
		trads.add(new Traduction("fr", "creation.new", "Vous êtes dans la zone où vous devez construire votre parcours. %LCliquez ici%L pour lire le tutoriel de création de map.", "2.0.1"));
		trads.add(new Traduction("fr", "creation.invitation", "Une invitation a été envoyée à %player.", "1.2"));
		trads.add(new Traduction("fr", "creation.invitation player", "%player veut construire une map de parcours avec vous. Êtes-vous d'accord ?", "1.2"));
		trads.add(new Traduction("fr", "creation.invitation denied", "Invitation refusée.", "1.2"));
		trads.add(new Traduction("fr", "creation.building", "Préparation de la map (cela peut prendre du temps)...", "2.2"));
		trads.add(new Traduction("fr", "creation.items.worldedit wand", "WorldEdit wand", "2.0"));
		trads.add(new Traduction("fr", "creation.help book.p1", "      §6§lLivre d'aide\n\n§rCe livre résume brièvement les fonctionnalités de base du constructeur de map. Pour voir d'autres fonctionnalités, une documentation complète et un tutoriel, visitez §3" + CreativeParkour.lienSite() + "/doc/map-creation.php", "2.1.2.3"));
		trads.add(new Traduction("fr", "creation.help book.p2", "      §6§lLivre d'aide\n\n§r§5§oObjets:\n§r§0P3: WorldEdit\n§r§5§oPanneaux:\n§r§0P4: Spawn\nP5: Départ & arrivée\nP6: Points de contrôle\nP7: PDC facultatifs\nP8: Hauteur de mort\n§r§5§oDivers:\n§r§0P9: Tester et publier\nP10: Quitter", "2.2"));
		trads.add(new Traduction("fr", "creation.help book.p3", "   §5§l§nWorldEdit\n\n§r§0Vous pouvez utiliser WorldEdit pour construire votre map en utilisant la hache en bois. La plupart des commandes de WorldEdit sont disponibles comme //set ou //undo.", "2.2"));
		trads.add(new Traduction("fr", "creation.help book.p4", "         §5§l§nSpawn\n\n§r§0Écrivez §o<spawn>§r§0 sur un panneau pour définir le spawn de votre map.\nLe panneau va disparître quand vous publirez votre map.", "1.2.4"));
		trads.add(new Traduction("fr", "creation.help book.p5", "§5§l§nDépart et arrivée\n\n§r§0Écrivez §o<start>§r§0 sur un panneau pour définir le départ.\nQuand un joueur le passera, son compteur démarrera.\nÉcrivez §o<end>§r pour définir l'endroit où le compteur va s'arrêter.", "1.2.4"));
		trads.add(new Traduction("fr", "creation.help book.p6", "§5§l§nPoints de contrôle\n\n§r§0Écrivez §o<checkpoint>§r§0 sur un panneau pour créer un point de contrôle.\nQuand un joueur tombe, il va être téléporté au dernier point qu'il a passé.\nLes joueurs doivent passer tous les points de contrôle sauf les facultatifs.", "1.2.4"));
		trads.add(new Traduction("fr", "creation.help book.p7", "§5§l§nPoints de contrôle§r\n      §5§l§nfacultatifs\n\n§r§0Écrivez §ooptional§r§0 sur la deuxième ligne d'un panneau checkpoint pour le rendre facultatif.\nLes joueurs n'ont pas besoin de les valider mais peuvent toujours s'y téléporter.", "1.2.4"));
		trads.add(new Traduction("fr", "creation.help book.p8", "  §5§l§nHauteur de mort\n\n§r§0Écrivez §o<death>§r§0 sur un panneau placé à la hauteur à laquelle vous voulez que les joueurs soient téléportés au dernier point de contrôle quand ils tombent.", "1.2.4"));
		trads.add(new Traduction("fr", "creation.help book.p9", " §5§l§nTester et publier\n§r§0Vous pouvez tester votre map autant de fois que vous le voulez en tapant §o/cp test§r\nPour quitter un test, tapez §o/cp test leave§r\nQuand vous avez terminé le test, vous pouvez publier votre map avec §o/cp publish <nomDeLaMap>", "1.2.4"));
		trads.add(new Traduction("fr", "creation.help book.p10", "        §5§l§nQuitter§r\n\n§r§0Si vous en avez marre, vous pouvez à tout moment quitter " + CreativeParkour.getNom() + " avec la commande §o/cp leave", "1.2.4"));
		trads.add(new Traduction("fr", "creation.block not allowed", "Ce bloc n'est pas autorisé.", "1.2"));
		trads.add(new Traduction("fr", "creation.monster egg", "Vous avez tenté de placer un bloc infesté (Silverfish), veuillez utiliser le bloc normal correspondant.", "1.2.5"));
		trads.add(new Traduction("fr", "creation.sign too high", "Ce panneau est trop haut.", "1.2.5"));
		trads.add(new Traduction("fr", "creation.check signs", "Vérification des panneaux...", "1.2.5"));
		trads.add(new Traduction("fr", "creation.check.multiple sign error", "Votre map ne peut pas contenir plus d'1 panneau %type.", "1.2"));
		trads.add(new Traduction("fr", "creation.check.no sign error", "Il n'y a pas de panneau %type dans votre map.", "1.2"));
		trads.add(new Traduction("fr", "creation.check.tp error 1", "Coordonnées invalides sur le panneau <tp> situé en %loc.", "2.3"));
		trads.add(new Traduction("fr", "creation.check.tp error 2", "L'endroit spécifié sur le panneau <tp> situé en %loc est en dehors de la map.", "2.3"));
		trads.add(new Traduction("fr", "creation.check.sign height error", "Le panneau %type ne peut pas être placé plus haut que les autres.", "1.2"));
		trads.add(new Traduction("fr", "creation.check.sign post error", "Les panneaux de ce type doivent être posés au dessus d'un bloc.", "1.2"));
		trads.add(new Traduction("fr", "creation.signs.effect error", "Les effets disponibles sont SPEED, SLOW, JUMP, CONFUSION, BLINDNESS, NIGHT_VISION et LEVITATION.", "1.2.6"));
		trads.add(new Traduction("fr", "creation.signs.int error", "'duration' et 'amplifier' doivent être des entiers compris entre 1 et 999999.", "1.2.6"));
		trads.add(new Traduction("fr", "creation.test", "Vous êtes en mode test. Vous devez aller au bout de votre parcours pour le valider. Pour revenir en mode création, tapez '/cp test leave'. Quand vous publirez votre map, les panneaux spéciaux ne seront plus visibles.", "1.2"));
		trads.add(new Traduction("fr", "creation.test build", "Vous ne pouvez pas construire car quelqu'un teste la map.", "1.2"));
		trads.add(new Traduction("fr", "creation.test completed", "Vous avez terminé le test, votre map est validée. '/cp publish' pour publier votre map sur le serveur.", "1.2"));
		trads.add(new Traduction("fr", "creation.test leave", "Tapez '/cp test leave' pour revenir en mode création.", "1.2"));
		trads.add(new Traduction("fr", "creation.test error", "Quelqu'un est déjà en train de tester la map.", "1.2"));
		trads.add(new Traduction("fr", "creation.test adjacent checkpoints", "Le message de validation des points de contrôle n'est affiché qu'une fois lorsque ceux-ci sont adjacents.", "2.2.2"));
		trads.add(new Traduction("fr", "creation.status", "Statut de la map", "1.2"));
		trads.add(new Traduction("fr", "creation.validated", "Validée", "1.2"));
		trads.add(new Traduction("fr", "creation.unvalidated", "Non validée", "1.2"));
		trads.add(new Traduction("fr", "creation.published", "Votre map a été publiée, les autres joueurs peuvent venir jouer!", "1.2.7"));
		trads.add(new Traduction("fr", "creation.announce new map", "%player a créé une nouvelle map de " + CreativeParkour.getNom() + " : %map. %LCliquez ici%L pour y jouer !", "1.2"));
		trads.add(new Traduction("fr", "creation.wand.first pos", "Position 1 définie.", "1.2"));
		trads.add(new Traduction("fr", "creation.wand.second pos", "Position 2 définie.", "1.2"));
		trads.add(new Traduction("fr", "creation.wand.unknown block", "Bloc inconnu.", "2.0"));
		trads.add(new Traduction("fr", "creation.wand.wrong block", "Vous ne pouvez pas utiliser du %block.", "2.0"));
		trads.add(new Traduction("fr", "creation.wand.no selection", "Vous devez sélectionner une zone avec votre bâton magique.", "1.2"));
		trads.add(new Traduction("fr", "creation.wand.no worldedit", "Cette action est impossible car le plugin WorldEdit n'est pas installé sur ce serveur.", "2.0"));
		trads.add(new Traduction("fr", "creation.wand.error", "Votre action n'a pas pu être correctement effectuée. Peut-être qu'une partie de votre sélection WorldEdit est en dehors de la map (ne sélectionnez pas la couche de bedrock et les murs invisibles). Utilisez \"//undo\" pour revenir en arrière.", "2.4.7"));
		trads.add(new Traduction("fr", "creation.wand.error block", "Certains des blocs que vous tentez de placer sont interdits dans CreativeParkour. Utilisez \"//undo\" pour revenir en arrière.", "2.4.3"));
		trads.add(new Traduction("fr", "creation.wand.selection error", "Votre sélection de WorldEdit est invalide car elle contient des blocs en dehors de la map. Vous avez peut-être sélectionné la couche de bedrock ou les murs invisibles.", "2.4.7"));

		trads.add(new Traduction("fr", "play.title", "Choisissez une map pour jouer", "2.0"));
		trads.add(new Traduction("fr", "play.page", "Page", "1.2"));
		trads.add(new Traduction("fr", "play.sort name", "Trier par nom", "2.0"));
		trads.add(new Traduction("fr", "play.sort creator", "Trier par nom de créateur", "2.0"));
		trads.add(new Traduction("fr", "play.sort difficulty", "Trier par difficulté", "2.0"));
		trads.add(new Traduction("fr", "play.ascending sorting", "Tri croissant", "2.0"));
		trads.add(new Traduction("fr", "play.descending sorting", "Tri décroissant", "2.0"));
		trads.add(new Traduction("fr", "play.show local", "Afficher les maps locales", "2.0"));
		trads.add(new Traduction("fr", "play.hide local", "Masquer les maps locales", "2.0"));
		trads.add(new Traduction("fr", "play.show downloadable", "Afficher les maps téléchargeables", "2.0"));
		trads.add(new Traduction("fr", "play.hide downloadable", "Masquer les maps téléchargeables", "2.0"));
		trads.add(new Traduction("fr", "play.difficulty", "Difficulté", "2.0"));
		trads.add(new Traduction("fr", "play.downloaded", "Téléchargée", "2.0"));
		trads.add(new Traduction("fr", "play.downloadable", "Téléchargeable (clic droit pour plus d'infos)", "2.0.1"));
		trads.add(new Traduction("fr", "play.download map info", "%LCliquez ici%L pour voir la description de cette map sur " + CreativeParkour.lienSiteCourt() + " (capture d'écran, description, classement...).", "2.4.3"));
		trads.add(new Traduction("fr", "play.maps", "maps", "1.2"));
		trads.add(new Traduction("fr", "play.welcome", "Vous jouez sur %map, une map de CreativeParkour par %creator.", "2.6"));
		trads.add(new Traduction("fr", "play.time", "Temps", "1.2"));
		trads.add(new Traduction("fr", "play.leaderboard", "Classement", "1.2"));
		trads.add(new Traduction("fr", "play.leaderboard ticks", "Classement (ticks)", "1.2.3"));
		trads.add(new Traduction("fr", "play.timer reset", "Votre temps a été réinitialisé, vous devez revenir au départ pour continuer ce parcours.", "2.4.7"));
		trads.add(new Traduction("fr", "play.time record", "Vous avez battu votre record sur cette map !", "1.2"));
		trads.add(new Traduction("fr", "play.time server record", "Vous avez battu le record de %player sur cette map ! Félicitations !", "1.2"));
		trads.add(new Traduction("fr", "play.timer error", "Désolé, le serveur est ralenti et votre temps ne peut pas être correctement calculé, vous avez terminé le parcours en %ticks ticks (%tsec secondes) mais ce temps devrait être de de %seconds secondes.", "2.1"));
		trads.add(new Traduction("fr", "play.timer error log", "Le serveur a l'air ralenti, le temps de %player n'a pas été enregistré (il a terminé un parcours en %ticks ticks (%tsec secondes) mais ce temps devrait être de de %seconds secondes). Regardez /tps", "2.1"));
		trads.add(new Traduction("fr", "play.record notification", "%player a battu votre record sur %map ! %LCliquez ici%L pour y jouer.", "2.6"));
		trads.add(new Traduction("fr", "play.checkpoint validated", "Point de contrôle validé !", "1.2"));
		trads.add(new Traduction("fr", "play.checkpoint validated diff", "Point de contrôle ! Différence avec %player: %diffs", "2.1.2.2"));
		trads.add(new Traduction("fr", "play.checkpoint validated diff personal", "Point de contrôle ! Différence avec votre record perso: %diffs", "2.1.2.2"));
		trads.add(new Traduction("fr", "play.checkpoint validated diff 2", "Point de contrôle ! Différence avec %player et votre record perso: %diffs / %diff2s", "2.1.2.2"));
		trads.add(new Traduction("fr", "play.checkpoints error", "Vous devez valider tous les checkpoints pour terminer le parcours.", "1.2"));
		trads.add(new Traduction("fr", "play.finish", "Vous avez terminé le parcours en %time secondes ! Tapez '/cp leave' pour quitter.", "1.2.3"));
		trads.add(new Traduction("fr", "play.items.return start", "Retourner au départ", "1.2"));
		trads.add(new Traduction("fr", "play.items.return checkpoint", "Retourner au dernier point de contrôle", "1.2"));
		trads.add(new Traduction("fr", "play.items.spectator", "Activer/désactiver le mode spectateur", "1.2"));
		trads.add(new Traduction("fr", "play.items.player visibility", "Changer la visibilité des joueurs", "2.2"));
		trads.add(new Traduction("fr", "play.items.leaderboard", "Activer/désactiver les temps précis dans le classement", "1.2.3"));
		trads.add(new Traduction("fr", "play.items.map options", "Options de la map", "1.2"));
		trads.add(new Traduction("fr", "play.items.ghosts", "Fantômes de joueurs", "2.2"));
		trads.add(new Traduction("fr", "play.items.leave test", "Arrêter le test", "2.2.1"));
		trads.add(new Traduction("fr", "play.items.right click", "clic droit", "1.2"));
		trads.add(new Traduction("fr", "play.players visible", "Les autres joueurs sont maintenant visibles.", "2.2"));
		trads.add(new Traduction("fr", "play.players transparent", "Les autres joueurs sont maintenant transparents.", "2.2"));
		trads.add(new Traduction("fr", "play.players invisible", "Les autres joueurs sont maintenant invisibles.", "2.2"));
		trads.add(new Traduction("fr", "play.wait", "Vous devez patienter quelques secondes avant d'utiliser cela.", "2.2"));
		trads.add(new Traduction("fr", "play.spectator on", "Vous êtes maintenant en mode spectateur. Faites un clic gauche sur un joueur pour voir sa vue (accroupissez-vous pour arrêter).", "2.0"));
		trads.add(new Traduction("fr", "play.spectator disable", "Tapez \"/cp spectator\" pour quitter le mode spectateur.", "2.0"));
		trads.add(new Traduction("fr", "play.spectator off", "Vous n'êtes plus en mode spectateur.", "1.2"));
		trads.add(new Traduction("fr", "play.your record", "Votre record", "1.2"));
		trads.add(new Traduction("fr", "play.no tp", "Vous ne pouvez pas vous téléporter ici.", "1.2"));
		trads.add(new Traduction("fr", "play.leaderboard ticks info", "Les temps du classement sont maintenant affichés en ticks. 1 tick = 0,05 secondes", "1.2.3"));
		trads.add(new Traduction("fr", "play.difficulty question", "Comment était ce parcours ?", "2.0"));
		trads.add(new Traduction("fr", "play.difficulty click", "Cliquez pour voter", "2.0"));
		trads.add(new Traduction("fr", "play.difficulty very easy", "Très facile", "2.0"));
		trads.add(new Traduction("fr", "play.difficulty easy", "Facile", "2.0"));
		trads.add(new Traduction("fr", "play.difficulty medium", "Moyen", "2.0"));
		trads.add(new Traduction("fr", "play.difficulty hard", "Difficile", "2.0"));
		trads.add(new Traduction("fr", "play.difficulty extreme", "Extrême", "2.0"));
		trads.add(new Traduction("fr", "play.difficulty wait", "Veuillez patienter quelques secondes, vous allez devoir cliquer sur un lien pour valider votre vote si c'est le premier aujourd'hui. Si vous ne le faites pas, il ne sera pas enregistré.", "2.1"));
		trads.add(new Traduction("fr", "play.difficulty confirm", "Pour éviter le spam et les faux votes, vous devez confirmer votre vote sur " + CreativeParkour.lienSiteCourt() + ". %LCliquez ici%L pour le faire, ça prend moins d'une minute ! Si vous ne le faites pas, il ne sera pas enregistré.", "2.1"));
		trads.add(new Traduction("fr", "play.difficulty ok", "Votre vote a été enregistré, merci !", "2.0"));
		trads.add(new Traduction("fr", "play.difficulty error", "Vous avez déjà évalué cette map, merci !", "2.0"));
		trads.add(new Traduction("fr", "play.error protocollib", "Désolé, cette fonctionnalité n'est pas disponible sur ce serveur car le plugin ProtocolLib requis n'est pas installé.", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.ghosts", "fantômes", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.error disabled", "Désolé, les fantômes sont désactivés sur ce serveur. Demandez à un administrateur de changer la configuration de CreativeParkour", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.title", "Choix des fantômes", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.date", "Date", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.clear", "Vider la sélection", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.selected", "Sélectionné", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.select", "Cliquez pour sélectionner ce fantôme.", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.unselect", "Cliquez pour désélectionner ce fantôme.", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.select personal", "Sélectionner automatiquement votre fantôme", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.select best", "Sélectionner automatiquement le meilleur fantôme", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.upload", "Envoyer les fantômes sur " + CreativeParkour.lienSiteCourt(), "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.upload info", "Seulement dans les maps téléchargées ou partagées", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.display before", "Donner aux fantômes 1 seconde d'avance", "2.2.2"));
		trads.add(new Traduction("fr", "play.ghosts.display before info", "Pour mieux voir leur parcours", "2.2.2"));
		trads.add(new Traduction("fr", "play.ghosts.enabled", "activé", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.disabled", "désactivé", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.help item", "Cliquez pour voir des informations sur les fantômes.", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.help text", "Un fantôme est l'enregistrement de la performance d'un joueur dans un parcours. Vous pouvez sélectionner plusieurs fantômes qui seront affichés en tant que joueurs transparents quand vous démarrerez le parcours. Des options pour choisir automatiquement des fantômes sont également disponibles, faites un clic droit sur la tête de votre inventaire pour les voir et sélectionner les fantômes que vous voulez.", "2.2"));
		trads.add(new Traduction("fr", "play.ghosts.report item", "Signaler un fantôme de tricheur", "2.2.1"));
		trads.add(new Traduction("fr", "play.ghosts.report item info", "Seulement pour les fantômes téléchargés", "2.2.1"));
		trads.add(new Traduction("fr", "play.ghosts.report link", "%LCliquez ici%L pour sélectionner le fantôme que vous souhaitez signaler.", "2.2.1"));
		trads.add(new Traduction("fr", "play.ghosts.limit", "Vous ne pouvez pas sélectionner autant de fantômes.", "2.2.1"));
		trads.add(new Traduction("fr", "play.sneak disabled", "L'accroupissement est désactivé dans cette map.", "2.4"));
		trads.add(new Traduction("fr", "play.deadly lava", "La lave vous tue dans cette map.", "2.4"));
		trads.add(new Traduction("fr", "play.deadly water", "L'eau vous tue dans cette map.", "2.4"));
		trads.add(new Traduction("fr", "play.interactions disabled", "Vous ne pouvez pas intéragir avec les portes et les trappes dans cette map.", "2.4.6"));
		trads.add(new Traduction("fr", "play.interactions disabled when playing", "Les intéractions avec les portes et les trappes seront désactivées en jouant dans cette map.", "2.4.6"));
		trads.add(new Traduction("fr", "play.elytra received", "Vous avez reçu des élytres.", "2.5"));
		trads.add(new Traduction("fr", "play.elytra removed", "Vos élytres vous ont été retirées.", "2.5"));

		trads.add(new Traduction("fr", "signs.error", "Le type '%type' n'est pas reconnu.", "1.2"));
		trads.add(new Traduction("fr", "signs.create", "Créer", "1.2"));
		trads.add(new Traduction("fr", "signs.play", "Jouer", "1.2"));
		trads.add(new Traduction("fr", "signs.map", "Map", "1.2"));
		trads.add(new Traduction("fr", "signs.leaderboard", "Classement", "2.1.1"));
		trads.add(new Traduction("fr", "signs.right click", "Clic droit", "1.2"));
		trads.add(new Traduction("fr", "signs.unknown map", "Map inconnue", "2.1.2"));
		
		trads.add(new Traduction("fr", "rewards.new", "Vous avez une nouvelle récompense :", "2.4"));
		trads.add(new Traduction("fr", "rewards.claim", "Tapez \"/cp recevoir\" en dehors de CreativeParkour pour recevoir vos récompenses", "2.4"));
		trads.add(new Traduction("fr", "rewards.xp", "%amount points d'XP", "2.4"));
		trads.add(new Traduction("fr", "rewards.info", "Vous avez %nb récompense(s) non réclamées !", "2.4"));
		trads.add(new Traduction("fr", "rewards.get them", "Les recevoir", "2.4"));
		trads.add(new Traduction("fr", "rewards.get them hover", "Cliquez pour recevoir vos récompenses", "2.4"));
		trads.add(new Traduction("fr", "rewards.stop", "Ne plus m'avertir", "2.4"));
		trads.add(new Traduction("fr", "rewards.stop hover", "Cliquez pour désactiver ces messages", "2.4"));
		trads.add(new Traduction("fr", "rewards.info disabled", "Messages de récompenses désactivés. Tapez \"/cp recevoir messages\" pour les réactiver.", "2.4"));
		trads.add(new Traduction("fr", "rewards.info enabled", "Messages de récompenses activés.", "2.4"));
		trads.add(new Traduction("fr", "rewards.received", "Vous avez reçu %reward !", "2.4"));
		trads.add(new Traduction("fr", "rewards.money error", "Impossible de vous envoyer de l'argent, cette erreur n'est pas causée par CreativeParkour.", "2.4"));
		trads.add(new Traduction("fr", "rewards.item lore", "Récompense de CreativeParkour", "2.4"));

		
		// Traitement
		File fichier_francais = new File(plugin.getDataFolder(), "/languages/fr.yml");
		FileConfiguration francais = YamlConfiguration.loadConfiguration(fichier_francais);
		
		String lv = Config.getConfig().getString("languages version");
		String v = CreativeParkour.getPlugin().getDescription().getVersion();
		int nbMAJ = 0;
		for (int i=0; i < trads.size(); i++)
		{
			Traduction trad = trads.get(i);
			FileConfiguration cfg;
			if (trad.getLangue().equalsIgnoreCase("fr"))
				cfg = francais;
			else
				cfg = anglais;
			
			if (!cfg.contains(trad.getPath()) || (!v.equals(lv) && trad.getVersion().equals(v))) // Si la traduction n'y est pas ou que le plugin a été mis à jour et que cette traduction a changé dans cette mise à jour
			{
				cfg.set(trad.getPath(), trad.getTexte());
				nbMAJ++;
			}
			else if (trad.getLangue().equalsIgnoreCase("en") && !trad.getTexte().equals(cfg.getString(trad.getPath())))
			{
				modifsAnglais.put(trad.getPath(), cfg.getString(trad.getPath()));
			}
		}
		if (nbMAJ > 0)
			Bukkit.getLogger().info(Config.prefix(false) + nbMAJ + " translations updated.");
		
		// Sauvegarde
		try {
			anglais.save(fichier_anglais);
		} catch (IOException e) {
			Bukkit.getLogger().warning("an error occurred while loading file 'CreativeParkour/languages/en.yml'");
			e.printStackTrace();
		}

		try {
			francais.save(fichier_francais);
		} catch (IOException e) {
			Bukkit.getLogger().warning("an error occurred while loading file 'CreativeParkour/languages/fr.yml'");
			e.printStackTrace();
		}
		
		// Sauvegarde de la version actuelle dans la configuration
		Config.updateConfig("languages version", v);
		

		if (lang.equalsIgnoreCase("en"))
		{
			langue = anglais;
		}
		else if (lang.equalsIgnoreCase("fr"))
		{
			langue = francais;
		}
		else
		{
			File f = new File(plugin.getDataFolder(), "/languages/" + lang + ".yml");
			langue = YamlConfiguration.loadConfiguration(f);
		}
	}

	static String getMessage(String nom)
	{
		return getMessage(nom, true);
	}

	static String getMessage(String nom, boolean warn)
	{
		if (langue.contains(nom))
		{
			return langue.getString(nom);
		}
		else if (warn)
		{
			CreativeParkour.debug("LANG", "The translation '" + nom + "' does not exist in your language file.");
		}
		return anglais.getString(nom);
	}
}
