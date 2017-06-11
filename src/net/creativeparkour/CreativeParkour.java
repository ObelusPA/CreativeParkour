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
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.gravitydevelopment.updater.Updater;
import net.md_5.bungee.api.ChatColor;

/**
 * Main CreativeParkour class, nothing useful here for the API.
 * @author Obelus
 */
public class CreativeParkour extends JavaPlugin implements Listener
{
	static boolean loaded = false;
	private static JavaPlugin worldEdit = null;
	private static boolean vault = false;
	private static boolean protocollib = false;
	private static Stats stats = null;

	@Override
	public void onEnable()
	{
		// Avertissement Bukkit
		if (getServer().getVersion().toLowerCase().contains("bukkit"))
		{
			Bukkit.getLogger().warning("********** CREATIVEPARKOUR DEPRECIATION WARNING **********");
			Bukkit.getLogger().warning("It seems that you are using Bukkit. CreativeParkour is no longer compatible with it, please use Spigot. https://www.spigotmc.org/wiki/buildtools/");
		}

		// Auto updater
		if (YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "configuration.yml")).getBoolean("enable auto updater"))
		{
			// Only this class is used to update the plugin, it connects to "https://api.curseforge.com"
			// This is the updater from Gravity: https://bukkit.org/threads/updater-2-3-easy-safe-and-policy-compliant-auto-updating-for-your-plugins-new.96681/
			new Updater(this, 82018, this.getFile(), Updater.UpdateType.DEFAULT, true);
		}

		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new Config(), this);
		getServer().getPluginManager().registerEvents(new MainListener(), this);
		getServer().getPluginManager().registerEvents(new Panneau(), this);
		getServer().getPluginManager().registerEvents(new RewardManager(), this);
		if (auMoins1_9())
			getServer().getPluginManager().registerEvents(new Commandes(), this);


		// Vérification de ProtocolLib en premier car on a besoin de savoir pendant l'initialisation
		protocollib = Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
		if (!protocollib && Config.fantomesPasInterdits())
		{
			Bukkit.getLogger().info(Config.prefix(false) + "ProtocolLib plugin not detected on this server. You must install it to enable player visibility and ghost-related features in CreativeParkour.");
		}

		Config.enable(false);

		getCommand("creativeparkour").setExecutor(new Commandes());
		getCommand("cpd").setExecutor(new Commandes());

		loaded = true;

		// WorldEdit et Vault
		try {
			worldEdit = (JavaPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
			if (getServer().getPluginManager().getPlugin("Vault") != null)
			{
				JoueurVault.setupPermissions();
				vault = true;
			}
			else if (getServer().getPluginManager().getPlugin("PermissionsEx") != null && worldEdit != null) // Avertissement si le mec a PermissionsEx mais pas Vault
				Bukkit.getLogger().warning(Config.prefix(false) + "You have PermissionsEx, but not Vault, so there may be permission issues with the WorldEdit wand in CreativeParkour. Please install the Vault plugin to fix this.");
		} catch (Exception | Error e) {
			// Rien
		}

		if (Config.getConfig().getBoolean("enable data collection"))
		{
			stats = new Stats(this);
			getServer().getScheduler().runTaskTimer(this, stats, 20 * 60 * 10, 20 * 60 * 60 * 6); // Délai de 10 minutes, puis intervalle de 6 heures
			getServer().getPluginManager().registerEvents(stats, this);
		}

		debug("INIT1", "Debug is enabled in " + getNom() + ", you can disable it in configuration.yml");
		debug("INIT2", "Java version: " + System.getProperty("java.version"));

		Config.updateConfig("previous version", getVersion());
	}

	@Override
	public void onDisable()
	{
		try {
			CPRequest.annulerRequetes();
		} catch (NoClassDefFoundError e) {
			// Rien
		}
		Bukkit.getLogger().info(Config.prefix(false) + "CreativeParkour disabled, thank you for using it!");
	}

	static String getNom()
	{
		return "CreativeParkour";
	}

	static Plugin getPlugin()
	{
		return Bukkit.getPluginManager().getPlugin("CreativeParkour");
	}

	static String lienSite()
	{
		return "https://" + lienSiteCourt();
	}

	static String lienSiteCourt()
	{
		return "creativeparkour.net";
	}

	static void debug(String id, String msg)
	{
		if (Config.getConfig().getBoolean("debug"))
		{
			Bukkit.getLogger().info(Config.prefix(false) + "[DEBUG:" + id + "] " + msg);
		}
	}

	static JavaPlugin getWorldEdit()
	{
		if (worldEdit != null && worldEdit.isEnabled())
			return worldEdit;
		return null;
	}

	static boolean vaultPresent()
	{
		return vault;
	}

	static boolean protocollibPresent()
	{
		return protocollib;
	}

	/**
	 * Returns server's Minecraft version without "1.".
	 * @return Server's version (eg: if the server is in 1.12, it returns 12).
	 */
	static int getServVersion()
	{
		Pattern r = Pattern.compile("\\(MC:\\s1\\.(\\d+).*\\)");
		Matcher m = r.matcher(Bukkit.getServer().getVersion());
		if (m.find())
			return Integer.valueOf(m.group(1));
		else
			return 0;
	}

	static boolean auMoins1_9()
	{
		return getServVersion() >= 9;
	}

	static String getVersion()
	{
		return getPlugin().getDescription().getVersion();
	}

	static Stats stats()
	{
		return stats;
	}

	/**
	 * Transforme une exception en String
	 * @param e Exception
	 * @return String
	 */
	static String exceptionToString(Throwable e)
	{
		return ExceptionUtils.getStackTrace(e);
	}

	/**
	 * Envoie l'erreur au site si c'est autorisé, l'affiche dans la console sinon
	 * @param code Code de l'erreur
	 * @param e Exception
	 * @param envoyer Si true, on tente de l'envoyer au site, sinon on l'affiche juste
	 */
	static void erreur(String code, Throwable e, boolean envoyer)
	{
		String err = exceptionToString(e);
		Bukkit.getLogger().warning(Config.prefix(false) + "An error occurred, sending it to " + lienSiteCourt() + "...\nError: " + err);

		if (Config.getConfig().getBoolean("enable data collection") && envoyer)
		{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("versionServeur", Bukkit.getServer().getVersion());
			params.put("onlineMode", String.valueOf(Bukkit.getOnlineMode()));
			params.put("erreur", "[E:" + code + "] " + err);
			try {
				CPRequest.effectuerRequete("erreurs.php", params, null, null, null);
			} catch (SecurityException e1) {
				erreur("0", e, false);
			}
		}
		else
		{
			Bukkit.getLogger().warning(Config.prefix(false) + "The last error (error " + code + ") can not be reported, please send it to obelus@creativeparkour.net");
		}
	}

	static boolean erreurRequete(JsonObject json, CommandSender sender)
	{
		if (json == null)
		{
			if (sender != null)
				sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("http error").replace("%error", "internal server error"));
			return true;
		}
		else {
			JsonElement raisonErreur = json.get("error reason");
			if (raisonErreur != null && sender != null)
				sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("http error").replace("%error", raisonErreur.getAsString()));
			return !json.get("STATUS").getAsString().equalsIgnoreCase("OK");
		}
	}
}
