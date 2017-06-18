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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import com.google.gson.JsonObject;

import net.creativeparkour.CPRequest.InvalidQueryResponseException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

enum EtapeConfig { START, STORAGE, DEPENDENCIES, PERMISSIONS, SHARING, END };

class Config implements Listener
{
	private static Plugin plugin = CreativeParkour.getPlugin();

	private static YamlConfiguration configGenerale;
	private static File fichier_config;
	private static File dossier_joueurs;
	private static Map<String, YamlConfiguration> configsJoueurs = new HashMap<String, YamlConfiguration>();
	private static List<String> messagesLogin = new ArrayList<String>();
	private static List<String> parametresManquants = new ArrayList<String>();
	private static World monde;
	private static boolean pluginDesactive;
	static List<Player> joueursConfiguration = new ArrayList<Player>(); // Joueurs en train de configurer le plugin avec "/cp config"

	static void enable(boolean reload)
	{
		configGenerale = null;
		fichier_config = null;
		messagesLogin.clear();
		parametresManquants.clear();
		monde = null;

		fichier_config = new File(plugin.getDataFolder(), "configuration.yml");
		configGenerale = YamlConfiguration.loadConfiguration(fichier_config);
		configGenerale.options().header("CreativeParkour's configuration\nFull documentation here: " + CreativeParkour.lienSite() + "/doc/configuration.php");

		String path = new String();

		// On reprend le petit préfixe des anciennes versions s'il était activé :
		String defPrefix = configGenerale.getBoolean("small tag", false) ? (ChatColor.YELLOW + "[" + ChatColor.GOLD + "CP" + ChatColor.YELLOW + "]") : ChatColor.YELLOW + "[" + ChatColor.GOLD + "CreativeParkour" + ChatColor.YELLOW + "]";		

		// Fichier configuration.yml
		path = "plugin enabled"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "enable auto updater"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "enable data collection"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "language"; if(!configGenerale.contains(path)) { configGenerale.set(path, "enUS"); }
		path = "prefix"; if(!configGenerale.contains(path)) { configGenerale.set(path, defPrefix ); }
		path = "sign brackets"; if(!configGenerale.contains(path)) { configGenerale.set(path, "triangle"); }
		path = "debug"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); }
		path = "previous version"; if(!configGenerale.contains(path)) { configGenerale.set(path, null); }
		path = "memory dump interval"; if(!configGenerale.contains(path)) { configGenerale.set(path, 90); }
		path = "dont use cp"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); }
		path = "installation date"; if(!configGenerale.contains(path)) { configGenerale.set(path, new Date().getTime()); }

		path = "map storage.map storage world"; if(!configGenerale.contains(path)) { configGenerale.set(path, "world_name"); parametresManquants.add(path); }
		path = "map storage.use plugin world"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); parametresManquants.add(path); }
		path = "map storage.storage location x min"; if(!configGenerale.contains(path) || configGenerale.getString(path).equals("?")) { configGenerale.set(path, "?"); parametresManquants.add(path); }
		path = "map storage.storage location y min"; if(!configGenerale.contains(path) || configGenerale.getString(path).equals("?")) { configGenerale.set(path, "?"); parametresManquants.add(path); }
		path = "map storage.storage location z min"; if(!configGenerale.contains(path) || configGenerale.getString(path).equals("?")) { configGenerale.set(path, "?"); parametresManquants.add(path); }
		path = "map storage.map size"; if(!configGenerale.contains(path) || configGenerale.getString(path).equals("?")) { configGenerale.set(path, "?"); parametresManquants.add(path); }
		path = "map storage.gap"; if(!configGenerale.contains(path)) { configGenerale.set(path, 30); }

		path = "map creation.allow redstone"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "map creation.allow fluids"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "map creation.disable potion effects"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); }
		path = "map creation.announce new maps"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "map creation.maps per player limit"; if(!configGenerale.contains(path)) { configGenerale.set(path, 1000); }
		path = "map creation.worldedit item"; if(!configGenerale.contains(path)) { configGenerale.set(path, Material.WOOD_AXE.name()); }

		path = "map selection.display records"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }

		path = "game.max players per map"; if(!configGenerale.contains(path)) { configGenerale.set(path, -1); }
		path = "game.max players in storage world"; if(!configGenerale.contains(path)) { configGenerale.set(path, -1); }
		path = "game.save inventory"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "game.force empty inventory"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); }
		List<String> exclusions = new ArrayList<String>();
		exclusions.add("example_world_1");
		exclusions.add("example_world_2");
		path = "game.inventory recovery world exclusions"; if(!configGenerale.contains(path)) { configGenerale.set(path, exclusions); }
		World world0 = Bukkit.getWorlds().get(0);
		path = "game.exit location.world"; if(!configGenerale.contains(path)) { configGenerale.set(path, world0.getName()); }
		path = "game.exit location.x"; if(!configGenerale.contains(path)) { configGenerale.set(path, world0.getSpawnLocation().getX()); }
		path = "game.exit location.y"; if(!configGenerale.contains(path)) { configGenerale.set(path, world0.getSpawnLocation().getY()); }
		path = "game.exit location.z"; if(!configGenerale.contains(path)) { configGenerale.set(path, world0.getSpawnLocation().getZ()); }
		path = "game.always teleport to exit location"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); }
		path = "game.exit on login"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); }
		path = "game.update players before teleporting"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); }
		path = "game.only leave with creativeparkour command"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); }
		path = "game.negative leaderboard"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); }
		path = "game.enable map rating"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "game.freeze redstone"; if(!configGenerale.contains(path)) { configGenerale.set(path, false); }
		path = "game.milliseconds difference"; if(!configGenerale.contains(path)) { configGenerale.set(path, 1000 * 10); }
		path = "game.enable ghosts"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "game.max ghosts"; if(!configGenerale.contains(path)) { configGenerale.set(path, 15); }
		path = "game.fetch ghosts skins"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "game.sharing info in downloaded maps"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }

		path = "online.enabled"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "online.server uuid"; if(!configGenerale.contains(path)) { configGenerale.set(path, UUID.randomUUID().toString()); }
		path = "online.show downloadable maps"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "online.upload ghosts"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "online.download ghosts"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }

		path = "rewards.currency"; if(!configGenerale.contains(path)) { configGenerale.set(path, "MONEY × %amount"); }
		Set<String> claimWorlds = new HashSet<String>();
		claimWorlds.add("world");claimWorlds.add("world_nether");claimWorlds.add("world_the_end");
		for (World w : Bukkit.getWorlds())
		{
			if (!w.getName().equalsIgnoreCase("CreativeParkourMaps"))
				claimWorlds.add(w.getName());
		}
		path = "rewards.claim worlds all"; if(!configGenerale.contains(path)) { configGenerale.set(path, true); }
		path = "rewards.claim worlds"; if(!configGenerale.contains(path)) { configGenerale.set(path, new ArrayList<String>(claimWorlds)); }

		if (configGenerale.getBoolean("plugin enabled"))
		{
			pluginDesactive = false;
		}
		else
		{
			pluginDesactive = true;
		}


		if (configGenerale.getBoolean("game.force empty inventory")) // Si on force à vider les inventaires, ça ne sert à rien de les enregistrer
		{
			configGenerale.set("game.save inventory", false);
		}

		if (!configGenerale.getBoolean("enable data collection"))
		{
			configGenerale.set("game.fetch ghosts skins", false);
		}

		// Mise à jour des anciens types de langues
		configGenerale.set(path, Langues.transformerCodeLangue(configGenerale.getString(path)));


		// Config joueurs
		dossier_joueurs = new File(plugin.getDataFolder(), "/Players");
		// Création de la version 2 des fichiers joueurs
		File fichier_joueurs = new File(plugin.getDataFolder(), "players.yml");
		if (!dossier_joueurs.exists() && fichier_joueurs.exists())
		{
			YamlConfiguration configJoueurs = YamlConfiguration.loadConfiguration(fichier_joueurs);
			Bukkit.getLogger().info(Config.prefix(false) + "Converting players from v1 to v2...");
			Set<String> liste = configJoueurs.getKeys(false);
			Iterator<String> it = liste.iterator();
			while (it.hasNext())
			{
				String uuid = it.next();
				File fichierJ = getFichierJoueur(uuid);
				ConfigurationSection cs = configJoueurs.getConfigurationSection(uuid);
				Set<String> liste2 = cs.getKeys(false);
				YamlConfiguration nvelleConf = YamlConfiguration.loadConfiguration(fichierJ);
				Iterator<String> it2 = liste2.iterator();
				while (it2.hasNext())
				{
					String key = it2.next();
					nvelleConf.set(key, cs.get(key));
				}
				try {
					nvelleConf.save(fichierJ);
				} catch (IOException e) {
					Bukkit.getLogger().warning("An error occured while loading file '" + fichierJ.getPath() + "'.");
					e.printStackTrace();
				}
			}

			fichier_joueurs.renameTo(new File(plugin.getDataFolder(), "players (deprecated, see the new Players folder).yml"));
		}
		dossier_joueurs.mkdirs();


		Langues.load(null);

		if (parametresManquants.size() > 0)
		{
			messagesLogin.add(Langues.getMessage("config.plugin not enabled"));
			messagesLogin.add(Langues.getMessage("config.config tutorial"));
		}

		if (!Bukkit.getOnlineMode())
		{
			Bukkit.getLogger().warning(Config.prefix(false) + "Your server is in offline mode. Make sure that players' UUIDs never change or severe problems can occur.");
		}

		save();

		if (!pluginDesactive && configGenerale.getString("map storage.map storage world") != null && !configGenerale.getString("map storage.map storage world").equalsIgnoreCase("world_name"))
		{
			monde = Bukkit.createWorld(new WorldCreator(configGenerale.getString("map storage.map storage world")));
			if (configGenerale.getBoolean("map storage.use plugin world"))
			{
				monde.setPVP(false);
				monde.setAutoSave(true);
				monde.setDifficulty(Difficulty.PEACEFUL);
				monde.setTime(6000);
				monde.setStorm(false);
				monde.setGameRuleValue("doDaylightCycle", "false");
				monde.setGameRuleValue("doFireTick", "false");
				monde.setGameRuleValue("doModLoot", "false");
				monde.setGameRuleValue("doMobSpawning", "false");
				monde.setGameRuleValue("doTileDrops", "false");
				monde.setGameRuleValue("mobGriefing", "false");
			}
		}
		else
		{
			monde = null;
		}

		for (int i=0; i < messagesLogin.size(); i++)
		{
			Bukkit.getLogger().info(Config.prefix(false) + messagesLogin.get(i));
		}
		Object[] onlinePlayers = Bukkit.getOnlinePlayers().toArray();
		for (int i=0; i < onlinePlayers.length; i++)
		{
			if (((Player) onlinePlayers[i]).hasPermission("creativeparkour.*"))
			{
				for (int j=0; j < messagesLogin.size(); j++)
				{
					((Player) onlinePlayers[i]).sendMessage(Config.prefix() + messagesLogin.get(j));
				}
			}
		}

		if (!reload && !pluginDesactive)
		{
			GameManager.enable(plugin);
			Panneau.enable(plugin);
		}
	}

	static void disable()
	{
		GameManager.disable();
	}

	static void reload()
	{
		enable(true);
	}

	static FileConfiguration getConfig()
	{
		return configGenerale;
	}

	static String getLanguage()
	{
		return configGenerale.getString("language", "enUS");
	}

	static World getMonde()
	{
		return monde;
	}

	@EventHandler
	void onPlayerJoin(PlayerJoinEvent e) {
		Player p = (Player) e.getPlayer();
		if (p.hasPermission("creativeparkour.*"))
		{
			for (int i=0; i < messagesLogin.size(); i++)
			{
				p.sendMessage(Config.prefix() + messagesLogin.get(i));
			}
		}
	}

	static void updateConfig(String p, Object val)
	{
		configGenerale.set(p, val);
		save();
	}

	private static void save()
	{
		try {
			configGenerale.save(fichier_config);
		} catch (IOException e) {
			Bukkit.getLogger().warning("An error occurred while loading file 'CreativeParkour/configuration.yml'");
			e.printStackTrace();
		}
	}

	static boolean pluginActive()
	{
		if (pluginDesactive)
		{
			return false;
		}
		else if (!pluginDesactive)
		{
			return true;
		}
		return false;
	}

	static boolean online()
	{
		return configGenerale.getBoolean("online.enabled");
	}

	static boolean isBanned(Player p)
	{
		return isBanned(p.getUniqueId());
	}

	static boolean isBanned(UUID uuid)
	{
		return getConfJoueur(uuid.toString()).getBoolean("banned", false);
	}

	static String getServUUID()
	{
		return configGenerale.getString("online.server uuid");
	}

	static Date getDateInstall()
	{
		return new Date(configGenerale.getLong("installation date"));
	}

	/**
	 * @return true si les fantômes ne sont pas interdits (même si ProtocolLib n'est pas là)
	 */
	static boolean fantomesPasInterdits()
	{
		return configGenerale.getBoolean("game.enable ghosts");
	}

	static Location getExitLocation()
	{
		return new Location(Bukkit.getWorld(configGenerale.getString("game.exit location.world")), configGenerale.getDouble("game.exit location.x"), configGenerale.getDouble("game.exit location.y"), configGenerale.getDouble("game.exit location.z"));
	}

	static Material getWorldEditItem()
	{
		Material m = null;
		try {
			m = Material.getMaterial(configGenerale.getString("map creation.worldedit item"));
		} catch (Exception e) {
			Bukkit.getLogger().warning(Config.prefix(false) + configGenerale.getString("map creation.worldedit item") + " is not a valid item (configuration.yml).");
		}
		return m == null ? Material.WOOD_AXE : m;
	}


	// Config joueurs

	private static File getFichierJoueur(String uuid)
	{
		return new File(dossier_joueurs, uuid + ".yml");
	}

	/**
	 * Gourmand en ressources, à éviter
	 */
	static Map<String, YamlConfiguration> getConfigsJoueurs()
	{
		Map<String, YamlConfiguration> confs = new HashMap<String, YamlConfiguration>();
		for (File f : CPUtils.filesInFolder(dossier_joueurs))
		{
			confs.put(f.getName().replace(".yml", ""), YamlConfiguration.loadConfiguration(f));
		}
		return confs;
	}

	static YamlConfiguration getConfJoueur(String uuid)
	{
		YamlConfiguration conf = configsJoueurs.get(uuid);
		if (conf == null)
		{
			conf = YamlConfiguration.loadConfiguration(getFichierJoueur(uuid));
			configsJoueurs.put(uuid, conf);
		}
		return conf;
	}

	static void saveConfJoueur(String uuid)
	{
		File f = getFichierJoueur(uuid);
		YamlConfiguration conf = configsJoueurs.get(uuid);
		if (conf != null)
		{
			try {
				conf.save(f);
			} catch (IOException e) {
				Bukkit.getLogger().warning("An error occured while loading file '" + f.getPath() + "'.");
				e.printStackTrace();
			}
		}
	}

	static void ajouterConfJoueur(String uuid, YamlConfiguration conf)
	{
		configsJoueurs.put(uuid, conf);
	}

	static void supprConfJoueur(String uuid)
	{
		configsJoueurs.remove(uuid);
	}

	// ----------------------------------------------------------------------------------------------------------------------
	// Assistant de configuration :

	static void configurer(Player p, EtapeConfig etape)
	{
		String head = "\n" + ChatColor.YELLOW + "------ " + ChatColor.GOLD + ChatColor.BOLD + "CreativeParkour configuration" + ChatColor.YELLOW + " ------";
		if (etape == EtapeConfig.STORAGE)
		{
			p.sendMessage(head);
			p.sendMessage(Langues.getMessage("config.storage.text"));
			p.sendMessage(ChatColor.GRAY + Langues.getMessage("config.storage.info"));
			Commandes.question(p, null, "config monde défaut");
		}
		else if (etape == EtapeConfig.DEPENDENCIES)
		{
			boolean pasProtocolLib = false;
			boolean pasVault = false;
			boolean pasWorldEdit = false;
			if (CreativeParkour.auMoins1_9() && ! CreativeParkour.protocollibPresent())
				pasProtocolLib = true;
			if (!CreativeParkour.vaultPresent())
				pasVault = true;
			if (CreativeParkour.getWorldEdit() == null)
				pasWorldEdit = true;

			if (pasProtocolLib || pasVault || pasWorldEdit)
			{
				p.sendMessage(head);
				p.sendMessage(Langues.getMessage("config.dependencies.info"));
				if (pasProtocolLib)
					p.spigot().sendMessage(new ComponentBuilder(" • " + Langues.getMessage("config.dependencies.protocollib"))
							.event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/protocollib.1997/")).create());
				if (pasVault)
					p.spigot().sendMessage(new ComponentBuilder(" • " + Langues.getMessage("config.dependencies.vault"))
							.event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://dev.bukkit.org/bukkit-plugins/vault/")).create());
				if (pasWorldEdit)
					p.spigot().sendMessage(new ComponentBuilder(" • " + Langues.getMessage("config.dependencies.worldedit"))
							.event(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://dev.bukkit.org/bukkit-plugins/worldedit/")).create());

				p.spigot().sendMessage(boutonNext(EtapeConfig.PERMISSIONS));
			}
			else
				configurer(p, EtapeConfig.PERMISSIONS);
		}
		else if (etape == EtapeConfig.PERMISSIONS)
		{

			p.sendMessage(head);
			p.sendMessage(Langues.getMessage("config.permissions"));
			p.spigot().sendMessage(boutonNext(EtapeConfig.SHARING));
		}
		else if (etape == EtapeConfig.SHARING)
		{
			p.sendMessage(head);
			p.sendMessage(Langues.getMessage("config.sharing.text"));
			Commandes.question(p, null, "config partage");
		}
		else if (etape == EtapeConfig.END)
		{
			p.sendMessage(head);
			p.sendMessage(ChatColor.GREEN + Langues.getMessage("config.end"));
			joueursConfiguration.remove(p);
		}
		else
		{
			joueursConfiguration.add(p);
			// Début
			p.sendMessage(head);
			p.sendMessage("Welcome to CreativeParkour! We will help you to quickly configure the plugin to start having fun in parkour maps with your friends."); // No translation here (language selection is after anyway)
			p.sendMessage("First, click your language below:");

			String info = "Click to set CreativeParkour to %lang";
			ComponentBuilder cb = new ComponentBuilder("English").bold(true).color(ChatColor.AQUA)
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour language enUS"))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info.replace("%lang", ChatColor.ITALIC + "English")).create()));
			cb.append(" / ").bold(false).color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, null)).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null));

			cb.append("Français").color(ChatColor.AQUA)
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour language frFR"))
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info.replace("%lang", ChatColor.ITALIC + "French")).create()));
			if (System.getProperty("user.language").equalsIgnoreCase("fr"))
				cb.bold(true);
			cb.append(" / ").bold(false).color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, null)).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null));

			cb.append("Deutsch").color(ChatColor.AQUA)
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour language deDE"))
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info.replace("%lang", ChatColor.ITALIC + "German")).create()));
			if (System.getProperty("user.language").equalsIgnoreCase("de"))
				cb.bold(true);
			cb.append(" / ").bold(false).color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, null)).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null));

			cb.append("한국어").color(ChatColor.AQUA)
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour language koKR"))
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info.replace("%lang", ChatColor.ITALIC + "Korean")).create()));
			if (System.getProperty("user.language").equalsIgnoreCase("ko"))
				cb.bold(true);
			cb.append(" / ").bold(false).color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, null)).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null));

			cb.append("Polski").color(ChatColor.AQUA)
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour language plPL"))
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info.replace("%lang", ChatColor.ITALIC + "Polish")).create()));
			if (System.getProperty("user.language").equalsIgnoreCase("pl"))
				cb.bold(true);
			cb.append(" / ").bold(false).color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, null)).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null));

			cb.append("Русский").color(ChatColor.AQUA)
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour language ruRU"))
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info.replace("%lang", ChatColor.ITALIC + "Russian")).create()));
			if (System.getProperty("user.language").equalsIgnoreCase("ru"))
				cb.bold(true);
			cb.append(" / ").bold(false).color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, null)).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null));

			cb.append("简体中文").color(ChatColor.AQUA)
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour language zhCN"))
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info.replace("%lang", ChatColor.ITALIC + "Simplified Chinese")).create()));
			if (System.getProperty("user.language").equalsIgnoreCase("zh"))
				cb.bold(true);
			cb.append(" / ").bold(false).color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, null)).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null));

			cb.append("Español").color(ChatColor.AQUA)
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour language esES"))
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info.replace("%lang", ChatColor.ITALIC + "Spanish")).create()));
			if (System.getProperty("user.language").equalsIgnoreCase("es"))
				cb.bold(true);
			cb.append(" / ").bold(false).color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, null)).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null));

			cb.append("Español latinoamericano").color(ChatColor.AQUA)
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour language esMX"))
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info.replace("%lang", ChatColor.ITALIC + "Latin American Spanish")).create()));

			p.spigot().sendMessage(cb.create());

			p.sendMessage(ChatColor.GRAY + "Some languages are translated by the community, you can help them by reviewing translations, translating missing phrases, or translating the plugin to another language at https://dev.bukkit.org/projects/creativeparkour/localization");
		}
	}

	private static BaseComponent[] boutonNext(EtapeConfig etape)
	{
		return new ComponentBuilder(" ➥ ").color(ChatColor.YELLOW)
				.append("[" + Langues.getMessage("config.next") + "]").color(ChatColor.AQUA)
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour config " + etape.name().toLowerCase()))
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.ITALIC + Langues.getMessage("config.next hover")).create())).create();
	}

	static void repondreQMonde(Player p, boolean reponse)
	{
		if (reponse == true)
		{
			p.sendMessage(ChatColor.ITALIC + Langues.getMessage("config.storage.world creating"));
			Bukkit.getLogger().info(Config.prefix(false) + "Creating world 'CreativeParkourMaps'...");

			WorldCreator wc = new WorldCreator("CreativeParkourMaps").type(WorldType.FLAT).generateStructures(false);
			monde = Bukkit.createWorld(wc);
			monde.setPVP(false);
			monde.setAutoSave(true);
			monde.setDifficulty(Difficulty.PEACEFUL);
			monde.setTime(6000);
			monde.setStorm(false);
			monde.setGameRuleValue("doDaylightCycle", "false");
			monde.setGameRuleValue("doFireTick", "false");
			monde.setGameRuleValue("doModLoot", "false");
			monde.setGameRuleValue("doMobSpawning", "false");
			monde.setGameRuleValue("doTileDrops", "false");
			monde.setGameRuleValue("mobGriefing", "false");
			monde.setSpawnLocation(0, 4, 0);

			updateConfig("map storage.map storage world", "CreativeParkourMaps");
			updateConfig("map storage.use plugin world", true);
			updateConfig("map storage.storage location x min", 0);
			updateConfig("map storage.storage location y min", 10);
			updateConfig("map storage.storage location z min", 0);
			updateConfig("map storage.map size", 64);

			Bukkit.getLogger().info(Config.prefix(false) + "World successfully created, yeah!");

			p.sendMessage(ChatColor.GREEN + Langues.getMessage("config.storage.default world"));
			p.sendMessage(ChatColor.RED + Langues.getMessage("config.storage.warn"));
		}
		else
		{
			updateConfig("map storage.use plugin world", false);
			p.sendMessage(ChatColor.GREEN + Langues.getMessage("config.storage.no default world"));
			p.sendMessage(ChatColor.ITALIC + Langues.getMessage("config.storage.no default world doc") + " https://creativeparkour.net/doc/configuration.php#map%20storage");
			p.sendMessage(ChatColor.GRAY + Langues.getMessage("config.storage.no default world back"));
		}
		p.spigot().sendMessage(boutonNext(EtapeConfig.DEPENDENCIES));
	}

	static void repondreQPartage(Player p, boolean reponse) throws NoSuchMethodException, SecurityException
	{
		if (reponse == true)
		{
			configGenerale.set("online.enabled", true);
			p.sendMessage(ChatColor.GREEN + Langues.getMessage("config.sharing.enabled"));
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("ipServ", Bukkit.getServer().getIp());
			params.put("nomServ", Bukkit.getServer().getServerName());
			params.put("ipJoueur", p.getAddress().getHostName());
			params.put("uuidJoueur", p.getUniqueId().toString());
			params.put("nomJoueur", p.getName());
			CPRequest.effectuerRequete("servers.php", params, null, Config.class.getMethod("reponseServeurPartage", JsonObject.class, String.class, Player.class), p);
			p.sendMessage(CPRequest.messageAttente());
		}
		else
		{
			configGenerale.set("online.enabled", false);
			p.sendMessage(ChatColor.YELLOW + Langues.getMessage("config.sharing.disabled"));
			if (joueursConfiguration.contains(p))
				p.spigot().sendMessage(boutonNext(EtapeConfig.END));
		}
		save();
	}

	/**
	 * Method called when <a href="https://creativeparkour.net">creativeparkour.net</a> responds to a query.<br>
	 * <em>Third-party plugins cannot use this method through CreativeParkour's API (it will throw an {@code InvalidQueryResponseException}).</em>
	 * @throws InvalidQueryResponseException If the {@code Request} has not been registered before.
	 */
	public static void reponseServeurPartage(JsonObject json, String rep, Player p) throws InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponseServeurPartage") && !CreativeParkour.erreurRequete(json, p))
		{
			if (json.get("data").getAsJsonObject().get("servDejaExistant") != null && json.get("data").getAsJsonObject().get("servDejaExistant").getAsBoolean() == true)
			{
				CPUtils.sendClickableMsg(p, Langues.getMessage("config.sharing.server already registered"), null, CreativeParkour.lienSite() + "/user/uuid-to-id.php?servUUID=" + getServUUID() + "&page=" + "%2Fuser%2Fserver.php%3Fid%3D", "%L", ChatColor.YELLOW);
			}
			else
			{
				CPUtils.sendClickableMsg(p, Langues.getMessage("config.sharing.new server"), null, CreativeParkour.lienSite() + "/user/server.php?c=" + json.get("data").getAsJsonObject().get("cle").getAsString(), "%L", ChatColor.YELLOW);
			}
		}
		if (joueursConfiguration.contains(p))
		{
			p.sendMessage(Langues.getMessage("config.sharing.done"));
			p.spigot().sendMessage(boutonNext(EtapeConfig.END));
		}
	}

	static String prefix()
	{
		return prefix(true);
	}
	static String prefix(boolean colored)
	{
		if (colored)
			return configGenerale.getString("prefix") + ChatColor.RESET + " ";
		else
			return "[CreativeParkour] ";
	}
}
