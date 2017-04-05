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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

class Langues
{
	private static Plugin plugin = CreativeParkour.getPlugin();
	static Properties messages;
	static Properties messagesEN;

	/**
	 * Loads the language specified in config.
	 * @param sender Player to send configmation messages to, or {@code null}.
	 */
	static void load(CommandSender sender)
	{
		// Chargement de la langue choisie dans la config
		Bukkit.getLogger().info(Config.prefix(false) + "Loading messages in your language...");
		messagesEN = chargerLangue("enUS");
		if (messagesEN == null)
		{
			Bukkit.getLogger().severe(Config.prefix(false) + "Unable to load English messages, please try to download the latest CreativeParkour version.");
			Bukkit.getPluginManager().disablePlugin(plugin);
			return;
		}
		File customLang = new File(plugin.getDataFolder(), "/" + Config.getLanguage());
		if (customLang.exists())
		{
			try {
				messages = chargerLangue(new FileInputStream(customLang), Config.getLanguage());
			} catch (FileNotFoundException e) {
				Bukkit.getLogger().warning("Something went wrong while loading language file " + customLang.getName() + ".");
				e.printStackTrace();
			}
		}
		else if (Config.getLanguage().equals("enUS"))
		{
			messages = messagesEN;
			if (sender != null)
				sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.language loaded").replace("%language", Config.getLanguage()));
		}
		else
		{
			messages = chargerLangue(Config.getLanguage());
			if (messages == null)
			{
				messages = messagesEN;
				Bukkit.getLogger().warning(Config.prefix(false) + "The language you specified is not supported yet. You can help translating the plugin at https://dev.bukkit.org/projects/creativeparkour/localization");
				if (sender != null)
					sender.sendMessage(Config.prefix() + ChatColor.RED + "The language you specified is not supported yet. You can help translating the plugin at https://dev.bukkit.org/projects/creativeparkour/localization");
			}
			else if (messages.size() < messagesEN.size())
			{
				int pourcent = Math.round(Math.min(99, (float) messages.size() / messagesEN.size() * 100)); // Rounded, but not to 100 %
				String msg = Langues.getMessage("commands.language incomplete").replace("%percentage", String.valueOf(pourcent));
				Bukkit.getLogger().info(Config.prefix(false) + msg);
				if (sender != null)
					sender.sendMessage(Config.prefix() + ChatColor.YELLOW + msg);
			}
			else if (sender != null)
				sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.language loaded").replace("%language", Config.getLanguage()));
		}

		// (Re)loading help and commands with the selected language
		Help.enable();
		Commandes.enable();

		// Ajout d'un fichier indiquant que les anciens fichiers de langue ne servent plus
		try {
			List<String> lines = Arrays.asList("CreativeParkour no longer uses these language files, messages are now included in CreativeParkour.jar.", "You can translate the plugin to your language at https://dev.bukkit.org/projects/creativeparkour/localization", "You can delete the \"languages\" folder.");
			Path file = new File(plugin.getDataFolder(), "/languages/THIS FOLDER IS DEPRECATED.txt").toPath();
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			// Rien
		}
	}

	static String getMessage(String nom)
	{
		return messages.getProperty(nom, messagesEN.getProperty(nom));
	}

	private static Properties chargerLangue(String codeLangue)
	{
		InputStream input = plugin.getResource("lang/" + codeLangue + ".lang");
		if (input == null)
		{
			return null;
		}

		return chargerLangue(input, codeLangue);
	}

	private static Properties chargerLangue(InputStream input, String nomLangue)
	{
		Properties prop = new Properties();
		try {
			prop.load(new InputStreamReader(input, Charset.forName("UTF-8")));
		} catch (IOException e) {
			CreativeParkour.erreur("LANG", e, true);
			return null;
		} finally {
			if (input != null)
			{
				try {
					input.close();
				} catch (IOException e) {
					CreativeParkour.erreur("LANG", e, true);
					return null;
				}
			}
		}

		// Remplacement des "_" par des espaces
		Properties prop2 = new Properties();
		for (Entry<Object, Object> e : prop.entrySet())
		{
			if (e.getKey() instanceof String && e.getValue() instanceof String)
			{
				String k = (String) e.getKey();
				String v = (String) e.getValue();
				// Les messages traduits doivent obligatoirement contenir "CreativeParkour" si c'était dans le message original et que le préfixe a été modifié
				if (Config.prefix().toLowerCase().contains("creativeparkour") || messagesEN == null || !messagesEN.containsKey(k.replace("_", " ")) || !((String)messagesEN.get(k)).toLowerCase().contains("creativeparkour") || v.toLowerCase().contains("creativeparkour"))
					prop2.put(k.replace("_", " "), v);
			}
		}

		Bukkit.getLogger().info(Config.prefix(false) + prop2.size() + " " + nomLangue + " phrases loaded.");
		return prop2;
	}

	/**
	 * Transforms a simple language code (like "de") to the code used for CreativeParkour translations (eg: "deDE")
	 * @param code The language code
	 * @return The "true" language code, or the provided {@code String} if unknown.
	 */
	static String transformerCodeLangue(String code)
	{
		code = code.toLowerCase();

		if (code.startsWith("en"))
			return "enUS";
		else if (code.startsWith("de"))
			return "deDE";
		else if (code.startsWith("es"))
			return "esES";
		else if (code.startsWith("fr"))
			return "frFR";
		else if (code.startsWith("it"))
			return "itIT";
		else if (code.startsWith("ja"))
			return "jaJP";
		else if (code.startsWith("ko"))
			return "koKR";
		else if (code.startsWith("pl"))
			return "plPL";
		else if (code.startsWith("pt"))
			return "ptBR";
		else if (code.startsWith("ru"))
			return "ruRU";
		else if (code.startsWith("zh"))
			return "zhCN";

		return code;
	}

	static String getCommand(String name)
	{
		return getMessage("commands." + name).split(";")[0];
	}
}
