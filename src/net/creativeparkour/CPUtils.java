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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.creativeparkour.CPRequest.InvalidQueryResponseException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

/**
 * Contains many simple but useful methods used in CreativeParkour.
 * @author Obelus
 */
public class CPUtils
{
	/**
	 * Value used in server protocol (with ProtocolLib, see <a href="http://wiki.vg/Entities#Entity">wiki.vg/Entities#Entity</a>).
	 */
	public static final byte sneakVal = 1 << 1;
	/**
	 * Value used in server protocol (with ProtocolLib, see <a href="http://wiki.vg/Entities#Entity">wiki.vg/Entities#Entity</a>).
	 */
	public static final byte invisibilityVal = 1 << 5;
	/**
	 * Value used in server protocol (with ProtocolLib, see <a href="http://wiki.vg/Entities#Entity">wiki.vg/Entities#Entity</a>).
	 */
	public static final byte elytraVal = (byte) (1 << 7);
	/**
	 * Value used in server protocol (with ProtocolLib, see <a href="http://wiki.vg/Entities#Entity">wiki.vg/Entities#Entity</a>).
	 */
	public static final byte sprintVal = 1 << 3;

	/**
	 * Truncates {@code s} string if it is longer than {@code maxLength}.
	 * @param s Input string to truncate.
	 * @param maxLength Maximum length of the output string.
	 * @return Truncated string.
	 */
	public static String truncatedStr(String s, int maxLength)
	{
		if (s.length() <= maxLength)
			return s;
		else
			return s.substring(0, maxLength);
	}

	/**
	 * Truncates a string and adds ellipsis if it is too long.
	 * @param s {@code String} to truncate.
	 * @param maxLength Length of the truncated string (with the ellipsis character).
	 * @return Truncated string with ellipsis at the end.
	 */
	public static String truncateStringEllipsis(String s, int maxLength)
	{
		if (s.length() <= maxLength)
			return s;
		return truncatedStr(s, maxLength-1) + "…";
	}

	/**
	 * Returns the item in player's hand (cross compatible between Minecraft versions).
	 * @param p Player we want to know the handled item.
	 * @return Item held by the player.
	 */
	public static ItemStack itemInHand(Player p) // TODO Remove when nobody uses 1.8 anymore
	{
		try {
			return p.getInventory().getItemInMainHand();
		} catch (NoSuchMethodError e) {
			return p.getItemInHand();
		}
	}

	/**
	 * Returns the sound corresponding one of the provided names (because some names changed in Minecraft 1.9).
	 * @param name Sound's name in the latest Minecraft version.
	 * @param oldName Sound's name before 1.9.
	 * @return The actual sound.
	 */
	public static Sound getSound(String name, String oldName)
	{
		try {
			return Sound.valueOf(name);
		} catch (IllegalArgumentException e) {
			return Sound.valueOf(oldName);
		}
	}

	/**
	 * Returns a player's name (corresponding to a given {@code UUID}), truncated if necessary to fit in scoreboards.
	 * @param uuid {@code String} representation of the {@code UUID} of the player we want the name.
	 * @return Player's name adapted to scoreboards.
	 */
	public static String playerScoreboardName(String uuid)
	{
		return playerScoreboardName(uuid, null);
	}

	/**
	 * Returns a player's name (corresponding to a given {@code UUID}), truncated if necessary to fit in scoreboards.
	 * @param uuid {@code String} representation of the {@code UUID} of the player we want the name.
	 * @param color {@code ChatColor} in which we want to display the name.
	 * @return Player's name adapted to scoreboards.
	 */
	public static String playerScoreboardName(String uuid, ChatColor color)
	{
		String nom = NameManager.getNomAvecUUID(UUID.fromString(uuid));
		if (nom == null)
			nom = "Unknown-" + uuid;
		return scoreboardName(nom, color);
	}

	/**
	 * Returns the given player name, truncated if necessary to fit in scoreboards.
	 * @param name Player name to truncate (or any string, it does not matter).
	 * @param color {@code ChatColor} in which we want to display the name.
	 * @return {@code name} adapted to scoreboards.
	 */
	public static String scoreboardName(String name, ChatColor color)
	{
		if (name == null)
			name = "Unknown";
		String c = "";
		if (color != null)
			c = color.toString();
		name = truncatedStr(c + name, 16);
		return name;
	}

	static Map<String, String> separerUuidNom(String s)
	{
		Map<String, String> map = new HashMap<String, String>();
		String[] a = s.split(":", 2);
		map.put("uuid", a[0]);
		map.put("nom", a[1]);
		return map;
	}

	/**
	 * Parses the given string to get coordinates (the string must be like "<em>&lt;X coord&gt;</em>;<em>&lt;Y coord&gt;</em>;<em>&lt;Z coord&gt;</em>").
	 * @param s String to parse (like <em>22;42;44</em>).
	 * @return Map containing X, Y and Z coordinates.<br>
	 * <em>map.get('x')</em> is the X coordinate.<br>
	 * <em>map.get('y')</em> is the Y coordinate.<br>
	 * <em>map.get('z')</em> is the Z coordinate.
	 */
	public static Map<Character, Integer> parseCoordinates(String s)
	{
		Map<Character, Integer> map = new HashMap<Character, Integer>();
		String[] a = s.split(";", 3);
		map.put('x', Integer.valueOf(a[0]));
		map.put('y', Integer.valueOf(a[1]));
		map.put('z', Integer.valueOf(a[2]));
		return map;
	}

	/**
	 * Splits a parkour map UUID and a player UUID in a CreativeParkour time file.
	 * @param fileName Name of the file.
	 * @return Parkour map {@code UUID} ("map" key) and player's {@code UUID} ("player" key).
	 */
	public static Map<String, UUID> timeFileUUIDs(String fileName)
	{
		Map<String, UUID> map = new HashMap<String, UUID>();
		fileName = fileName.replace(".yml", "");
		String[] a = fileName.split("_", 2);
		map.put("map", UUID.fromString(a[0]));
		map.put("player", UUID.fromString(a[1]));
		return map;
	}

	static JsonObject getJsonObjectPropre(JsonElement jsonElement)
	{
		if (jsonElement.isJsonObject())
			return jsonElement.getAsJsonObject();
		else
		{
			JsonParser parser = new JsonParser();
			String strContenu = jsonElement.getAsString();
			return parser.parse(strContenu).getAsJsonObject();
		}
	}

	/**
	 * Loads a YML file and returns its {@code YamlConfiguration}.
	 * @param file The file to load.
	 * @return {@code YamlConfiguration} contained in {@code file}.
	 */
	public static YamlConfiguration getYML(File file)
	{
		if (file.exists())
			return YamlConfiguration.loadConfiguration(file);
		return null;
	}

	/**
	 * Sends a clickable message (with a link or a command) to a player.
	 * @param p Player to send the message to.
	 * @param text Complete message text.
	 * @param command Executed command when the player clicks the link (or {@code null} to send an URL).
	 * @param url Link's URL (if command = {@code null}).
	 * @param linkTags Tag delimiting the link.
	 * @param backgroundColor {@code ChatColor} of the other text than the link delimited by the tags.
	 */
	public static void sendClickableMsg(Player p, String text, String command, String url, String linkTags, ChatColor backgroundColor)
	{
		try {
			ComponentBuilder cb = new ComponentBuilder(Config.prefix());
			// Ajout du lien sur tout le message
			if (command != null)
				cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
			else
				cb.event(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
			text = text.replace("\"", "\\\""); // Echappement des guillemets
			int longB = linkTags.length();
			int i1 = text.indexOf(linkTags);
			int i2 = text.indexOf(linkTags, i1 + longB);
			cb.append(text.substring(0, i1)).color(backgroundColor);
			cb.append(text.substring(i1 + longB, i2)).color(ChatColor.AQUA).bold(true);
			cb.append(text.substring(i2 + longB)).color(backgroundColor).bold(false);
			p.spigot().sendMessage(cb.create());
		} catch (Exception e) {
			Bukkit.getLogger().warning(Config.prefix(false) + "Incorrect translation \"" + text + "\" : two " + linkTags + " expected around a link. Remove this translation in your language file to reset it.");
			p.sendMessage(Config.prefix() + backgroundColor + text);
		}
	}

	/**
	 * Sends an information message to a player (<span style="color:MediumTurquoise">ⓘ</span> <em style="color:DarkGray">&lt;message&gt;</em>).
	 * @param p Player to sent the message to.
	 * @param text Text of the message.
	 */
	public static void sendInfoMessage(Player p, String text)
	{
		p.sendMessage(ChatColor.AQUA + "ⓘ " + ChatColor.GRAY + "" + ChatColor.ITALIC + text);
	}

	/**
	 * Returns a string with the first character of str capitalized.
	 * @param str The input string.
	 * @return Resulting string.
	 */
	public static String ucfirst(String str)
	{
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	/**
	 * Creates a string representing the 3 given coordinates.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 * @return Resulting string ("<em>&lt;X coord&gt;</em>;<em>&lt;Y coord&gt;</em>;<em>&lt;Z coord&gt;</em>")
	 */
	public static String coordsToString(int x, int y, int z)
	{
		return x + ";" + y + ";" + z;
	}

	/**
	 * Returns {@code true} if a player at the given location is touching the given block.
	 * @param block {@code Block} to test
	 * @param l {@code Location} to test
	 * @return Whether or not a player at the {@code location} would touch the {@code block}.
	 */
	public static boolean blockTouched(Block block, Location l)
	{
		float marge = 0.3F;
		return l.getX() >= (block.getX() - marge) && l.getX() <= (block.getX() + 1 + marge) && l.getY() >= (block.getY() - 0.5) && l.getY() <= block.getY() + 1 && l.getZ() >= (block.getZ() - marge) && l.getZ() <= (block.getZ() + 1 + marge);
	}

	/**
	 * Puts the {@code s} string between symbols chosen in CreativeParkour's configuration.
	 * @param s {@code String} to bracket.
	 * @return Given string surrounded by brackets.
	 */
	public static String bracket(String s)
	{
		if (Config.getConfig().getString("sign brackets").equalsIgnoreCase("square"))
		{
			return "[" + s + "]";
		}
		else if (Config.getConfig().getString("sign brackets").equalsIgnoreCase("round"))
		{
			return "(" + s + ")";
		}
		else // Par défaut
		{
			return "<" + s + ">";
		}
	}

	/**
	 * Affiche en debug la différence de nanosecondes entre le temps actuel et le temps précédent
	 * @param debugID Nom du message de debug
	 * @param preced Temps précédent en nanosecondes
	 * @return Nanosecondes actuelles à si on veut rappeler cette fonction plus tard 
	 */
	static long debugNanoTime(String debugID, long preced)
	{
		long nveau = System.nanoTime();
		CreativeParkour.debug(debugID, "time=" + (nveau - preced));
		return nveau;
	}

	static void registerOnline(Player p) throws NoSuchMethodException, SecurityException
	{
		if (Config.online())
		{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("ipJoueur", p.getAddress().getHostName());
			params.put("uuidJoueur", p.getUniqueId().toString());
			params.put("nomJoueur", p.getName());
			CPRequest.effectuerRequete("register.php", params, null, CPUtils.class.getMethod("reponseRegister", JsonObject.class, String.class, Player.class), p);
			p.sendMessage(CPRequest.messageAttente());
		}
	}

	
	/**
	 * <em>Third-party plugins cannot use this method through CreativeParkour's API (it will throw an {@code InvalidQueryResponseException}).</em><br>
	 * Method called when <a href="https://creativeparkour.net" target="_blank">creativeparkour.net</a> responds to a query.
	 * @param json
	 * @param rep
	 * @param p
	 * @throws InvalidQueryResponseException If the {@code Request} has not been registered before.
	 */
	public static void reponseRegister(JsonObject json, String rep, Player p) throws InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponseRegister") && !CreativeParkour.erreurRequete(json, p))
		{
			if (json.get("data").getAsJsonObject().get("dejaInscrit") != null && json.get("data").getAsJsonObject().get("dejaInscrit").getAsBoolean() == true)
			{
				p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.register already"));
			}
			else if (json.get("data").getAsJsonObject().get("cle") != null)
			{
				CPUtils.sendClickableMsg(p, Langues.getMessage("commands.register link"), null, CreativeParkour.lienSite() + "/user/register.php?c=" + json.get("data").getAsJsonObject().get("cle").getAsString(), "%L", ChatColor.GREEN);
			}
		}
	}

	/**
	 * Returns the list of YML files in the given folder.
	 * @param folder A folder...
	 * @return List of files in {@code folder}.
	 */
	public static List<File> filesInFolder(File folder)
	{
		File[] liste = folder.listFiles();
		if (liste == null)
			return new ArrayList<File>();
		// Création d'une nouvelle liste avec que les fichiers .yml
		List<File> listePropre = new ArrayList<File>();
		for (File f : liste)
		{
			if (f.getName().endsWith(".yml"))
				listePropre.add(f);
		}
		return listePropre;
	}

	/**
	 * Returns true if the given {@code ItemStack} is empty (because sometimes it is null, sometimes it contains {@code Material.AIR}).
	 * @param item {@code ItemStack} to check.
	 * @return Whether or not {@code item} is empty.
	 */
	public static boolean itemStackIsEmpty(ItemStack item)
	{
		return item == null || item.getType() == Material.AIR;
	}

	/**
	 * Divides the text in several lines by splitting it at some spaces.<br>
	 * This is supposed to be used to make long texts readable in {@code ItemStack} lore.
	 * @param text Text to split.
	 * @param color Text's color (or {@code null} to not set any color).
	 * @return Paragraph lines in a list.
	 */
	public static List<String> divideText(String text, ChatColor color)
	{
		StringBuffer s = new StringBuffer(text.trim());
		List<String> l = new ArrayList<String>();
		int index = -1;
		do
		{
			index = s.indexOf(" ", 32);
			if (index > 0)
			{
				l.add(s.substring(0, index));
				s.delete(0, index + 1);
			}
			else if (index + 1 < s.length())
				l.add(s.toString());
		}
		while (index > 0);

		// Ajout des couleurs
		if (color != null)
		{
			for (int i=0; i < l.size(); i++)
			{
				l.set(i, color + l.get(i));
			}
		}

		return l;
	}
	
	/**
	 * Divides the text in several lines by splitting it at some spaces.<br>
	 * This is supposed to be used to make long texts readable in {@code ItemStack} lore.
	 * @param text Text to split.
	 * @param color Text's color (or {@code null} to not set any color).
	 * @return Paragraph lines in a list.
	 * @see net.creativeparkour.CPUtils#divideText(String, ChatColor)
	 */
	@Deprecated
	public static List<String> diviserTexte(String text, ChatColor color)
	{
		return divideText(text, color);
	}
	
	/**
	 * Returns all the {@code String}s in the {@code List} in one single {@code String}, with line breaks between lines.
	 * @param lines {@code List} of paragraph's lines.
	 * @return A {@code String} containing all the lines.
	 */
	public static String dividedTextToString(List<String> lines)
	{
		StringBuffer s = new StringBuffer();
		for (String l : lines)
		{
			s.append(l).append("\n");
		}
		// Removing the last "\n"
		if (s.length() > 0)
			s.deleteCharAt(s.length() - 1);
		return s.toString();
	}
}
