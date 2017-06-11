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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import net.creativeparkour.CPRequest.InvalidQueryResponseException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

class GameManager implements Listener
{
	private static File dossier_maps;

	private static File dossier_temps;
	private static File fichier_exclusions_temps;
	static YamlConfiguration exclusions_temps;

	static final List<Joueur> joueurs = new ArrayList<Joueur>();
	static final Map<Integer, CPMap> maps = new HashMap<Integer, CPMap>();
	static final List<Material> blocsInterdits = new ArrayList<Material>();
	static final List<Material> exceptionsOPs = new ArrayList<Material>();
	static final List<CPMap> mapsTelechargeables = new ArrayList<CPMap>();
	private static boolean taskListe = false; // True si une task périodique est lancée pour mettre à jour la liste de maps téléchargeables
	static int nbTricheurs = 0;
	static List<UUID> receveursNotifsTriche;

	static void enable(Plugin plugin)
	{
		joueurs.clear();
		maps.clear();

		dossier_maps = new File(plugin.getDataFolder(), "/Maps");

		dossier_temps = new File(plugin.getDataFolder(), "/Times");

		fichier_exclusions_temps = new File(plugin.getDataFolder(), "deleted times.yml");
		exclusions_temps = YamlConfiguration.loadConfiguration(fichier_exclusions_temps);

		// Création de la version 2 des fichiers maps
		File fichier_maps = new File(plugin.getDataFolder(), "maps.yml");
		if (!dossier_maps.exists() && fichier_maps.exists())
		{
			YamlConfiguration configMaps = YamlConfiguration.loadConfiguration(fichier_maps);
			Bukkit.getLogger().info(Config.prefix(false) + "Converting maps from v1 to v2...");
			boolean termine = false;
			for (int i=0; !termine; i++)
			{
				if (configMaps.getString(i + ".state") == null)
				{
					termine = true;
				}
				else
				{
					World w = Bukkit.getWorld(configMaps.getString(i + ".world"));
					Set<UUID> invites = new HashSet<UUID>();
					for (int i1=0; configMaps.getList(i + ".contributors") != null && i1 < configMaps.getList(i + ".contributors").size(); i1++)
					{
						invites.add(UUID.fromString((String) configMaps.getList(i + ".contributors").get(i1)));
					}
					if (w != null)
					{
						CPMap map = new CPMap(i, configMaps.getString(i + ".uuid"), CPMapState.valueOf(configMaps.getString(i + ".state").toUpperCase()), 
								w, w.getBlockAt(configMaps.getInt(i + ".location min.x"), configMaps.getInt(i + ".location min.y"), configMaps.getInt(i + ".location min.z")), 
								w.getBlockAt(configMaps.getInt(i + ".location max.x"), configMaps.getInt(i + ".location max.y"), configMaps.getInt(i + ".location max.z")), 
								configMaps.getString(i + ".name"), UUID.fromString(configMaps.getString(i + ".creator")), 
								invites, configMaps.getBoolean(i + ".pinned"));
						map.sauvegarder();
					}
				}
			}

			fichier_maps.renameTo(new File(plugin.getDataFolder(), "maps (deprecated, see the new Maps folder).yml"));
		}


		dossier_maps.mkdirs();

		// Chargement des maps
		for (File f : getFichiersMaps())
		{
			YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
			if (yml.getString("world") != null)
			{
				World w = Bukkit.getWorld(yml.getString("world"));
				if (w != null)
				{
					Set<UUID> invites = new HashSet<UUID>();
					for (int i1=0; yml.getList("contributors") != null && i1 < yml.getList("contributors").size(); i1++)
					{
						invites.add(UUID.fromString((String) yml.getList("contributors").get(i1)));
					}

					List<BlocSpecial> blocsSpeciaux = new ArrayList<BlocSpecial>();
					ConfigurationSection ymlBS = yml.getConfigurationSection("special blocks");
					if (ymlBS != null)
					{
						for (String key : ymlBS.getKeys(false))
						{
							String type = ymlBS.getString(key + ".t");
							Map<Character, Integer> c = CPUtils.parseCoordinates(key);
							Block bloc = w.getBlockAt(c.get('x'), c.get('y'), c.get('z'));
							if (type.equalsIgnoreCase(BlocDepart.getType())) // Départs
							{
								blocsSpeciaux.add(new BlocDepart(bloc));
							}
							else if (type.equalsIgnoreCase(BlocArrivee.getType())) // Arrivées
							{
								blocsSpeciaux.add(new BlocArrivee(bloc));
							}
							else if (type.equalsIgnoreCase(BlocCheckpoint.getType())) // Checkpoints
							{
								blocsSpeciaux.add(new BlocCheckpoint(bloc, (byte) ymlBS.getInt(key + ".dir"), ymlBS.getString(key + ".prop")));
							}
							else if (type.equalsIgnoreCase(BlocEffet.getType())) // Effets
							{
								blocsSpeciaux.add(new BlocEffet(bloc, ymlBS.getString(key + ".effect"), ymlBS.getInt(key + ".duration"), ymlBS.getInt(key + ".amplifier")));
							}
							else if (type.equalsIgnoreCase(BlocGive.getType())) // Gives
							{
								blocsSpeciaux.add(new BlocGive(bloc, ymlBS.getString(key + ".type"), ymlBS.getString(key + ".action")));
							}
							else if (type.equalsIgnoreCase(BlocMort.getType())) // Morts
							{
								blocsSpeciaux.add(new BlocMort(bloc));
							}
							else if (type.equalsIgnoreCase(BlocTP.getType())) // TP
							{
								blocsSpeciaux.add(new BlocTP(bloc, new Location(w, ymlBS.getDouble(key + ".x"), ymlBS.getDouble(key + ".y"), ymlBS.getDouble(key + ".z"))));
							}
						}
					}

					int id = yml.getInt("id");
					float difficulty = -1;
					float quality = -1;
					try {
						difficulty = Float.valueOf(yml.getString("difficulty"));
						quality = Float.valueOf(yml.getString("quality"));
					} catch (Exception e) {
						// Rien
					}
					CPMap m = new CPMap (id, 
							yml.getString("uuid"), 
							CPMapState.valueOf(yml.getString("state").toUpperCase()), 
							w, 
							w.getBlockAt(yml.getInt("location min.x"), yml.getInt("location min.y"), yml.getInt("location min.z")), 
							w.getBlockAt(yml.getInt("location max.x"), Math.min(yml.getInt("location max.y"), 126), yml.getInt("location max.z")), 
							yml.getString("name"), 
							UUID.fromString(yml.getString("creator")), 
							invites, 
							yml.getBoolean("pinned"), 
							new BlocSpawn(w.getBlockAt(yml.getInt("spawn.x"), yml.getInt("spawn.y"), yml.getInt("spawn.z")), (byte) yml.getInt("spawn.dir")), 
							blocsSpeciaux, 
							yml.getInt("death height"),
							yml.getBoolean("sneak allowed", true),
							yml.getBoolean("deadly lava", false),
							yml.getBoolean("deadly water", false),
							yml.getBoolean("interactions allowed", true),
							yml.getStringList("ratings"),
							difficulty,
							quality);
					maps.put(id, m);

					// Régénération du contour s'il n'est pas là aux coordonnées min
					if (m.getMinLoc().getRelative(BlockFace.DOWN).getType() != Material.BEDROCK || m.getMinLoc().getRelative(BlockFace.NORTH).getType() != Material.BARRIER || m.getMinLoc().getRelative(BlockFace.WEST).getType() != Material.BARRIER)
					{
						Bukkit.getLogger().info(Config.prefix(false) + "Regenerating outline for the map \"" + m.getName() + "\"...");
						new RemplisseurBlocs(genererContours(w, m.getMinLoc().getX(), m.getMinLoc().getY(), m.getMinLoc().getZ(), m.getMaxLoc().getX(), m.getMaxLoc().getY(), m.getMaxLoc().getZ()), w).runTaskTimer(CreativeParkour.getPlugin(), 20, 1);
					}
				}
			}
		}


		File fichier_temps = new File(plugin.getDataFolder(), "times.yml");
		YamlConfiguration temps = YamlConfiguration.loadConfiguration(fichier_temps);

		// Création de la version 2 du fichier des temps
		if (!fichier_temps.exists() && !dossier_temps.exists())
		{
			File ancien_temps = new File(plugin.getDataFolder(), "players_times.yml");
			if (ancien_temps.exists())
			{
				Bukkit.getLogger().info(Config.prefix(false) + "Converting times from v1 to v2...");
				YamlConfiguration ancienTemps = YamlConfiguration.loadConfiguration(ancien_temps);
				// Conversion des temps des secondes aux ticks et remplissage du nouveau fichier
				for (int i=0; i < maps.size(); i++) // IDs des maps
				{
					for (int j=0; ancienTemps.contains(i + "." + j + ".player"); j++) // Pour chaque joueur (de 0 à x)
					{
						if (ancienTemps.contains(i + "." + j + ".time"))
						{
							ancienTemps.set(i + "." + j + ".ticks", ancienTemps.getInt(i + "." + j + ".time") * 20);
							ancienTemps.set(i + "." + j + ".time", null);
						}

						// Ajout du temps dans le nouveau fichier
						temps.set(i + ".map name", maps.get(i).getName());
						temps.set(i + ".map uuid", maps.get(i).getUUID().toString());
						String uuidJoueur = ancienTemps.getString(i + "." + j + ".player");
						temps.set(i + ".times." + uuidJoueur + ".ticks", ancienTemps.getInt(i + "." + j + ".ticks"));
						temps.set(i + ".times." + uuidJoueur + ".player name", NameManager.getNomAvecUUID(UUID.fromString(uuidJoueur)));
						temps.set(i + ".times." + uuidJoueur + ".real milliseconds", ancienTemps.getInt(i + "." + j + ".ticks") * 50);
					}
				}
				// Sauvegarde
				try {
					temps.save(fichier_temps);
				} catch (IOException e) {
					Bukkit.getLogger().warning("An error occured while loading file 'CreativeParkour/times.yml'");
					e.printStackTrace();
				}
				ancien_temps.renameTo(new File(plugin.getDataFolder(), "players_times (deprecated).yml"));
			}
		}

		// Création de la version 3 des fichiers temps
		if (!dossier_temps.exists() && fichier_temps.exists())
		{
			Bukkit.getLogger().info(Config.prefix(false) + "Converting times from v2 to v3...");
			// Création d'objets Temps et sauvegerde de ceux-ci
			for (CPMap map : maps.values()) // IDs des maps
			{
				if (temps.contains(map.getId() + ".map uuid"))
				{
					ConfigurationSection csTemps = temps.getConfigurationSection(map.getId() + ".times");
					Set<String> keys = csTemps.getKeys(false);
					Iterator<String> it = keys.iterator();
					while (it.hasNext())
					{
						String uuidJ = it.next();
						CPTime t = new CPTime(UUID.fromString(uuidJ), map, csTemps.getInt(uuidJ + ".ticks"));
						t.ajouterCheckpoints(csTemps.get(uuidJ + ".checkpoints"));
						t.realMilliseconds = csTemps.getLong(uuidJ + ".real milliseconds");
						t.sauvegarder(new Date(csTemps.getLong(uuidJ + ".date")));
					}
				}
			}

			fichier_temps.renameTo(new File(plugin.getDataFolder(), "times (deprecated, see the new Times folder).yml"));
		}

		dossier_temps.mkdirs();


		String s = new String();
		if (maps.size() != 1) { s = "s"; }
		Bukkit.getLogger().info(Config.prefix(false) + maps.size() + " CreativeParkour map" + s + " loaded.");

		// Blocs interdits
		if (!Config.getConfig().getBoolean("map creation.allow redstone"))
		{
			blocsInterdits.add(Material.REDSTONE_BLOCK);
			blocsInterdits.add(Material.REDSTONE_WIRE);
			blocsInterdits.add(Material.REDSTONE_COMPARATOR_OFF);
			blocsInterdits.add(Material.REDSTONE_COMPARATOR_ON);
			blocsInterdits.add(Material.DIODE_BLOCK_OFF);
			blocsInterdits.add(Material.DIODE_BLOCK_ON);
			blocsInterdits.add(Material.WOOD_PLATE);
			blocsInterdits.add(Material.STONE_PLATE);
			blocsInterdits.add(Material.IRON_PLATE);
			blocsInterdits.add(Material.GOLD_PLATE);
			blocsInterdits.add(Material.TRIPWIRE_HOOK);
			blocsInterdits.add(Material.DAYLIGHT_DETECTOR);
			blocsInterdits.add(Material.DAYLIGHT_DETECTOR_INVERTED);
			blocsInterdits.add(Material.REDSTONE_TORCH_ON);
			blocsInterdits.add(Material.REDSTONE_TORCH_OFF);
			blocsInterdits.add(Material.STONE_BUTTON);
			blocsInterdits.add(Material.WOOD_BUTTON);
			blocsInterdits.add(Material.LEVER);
			blocsInterdits.add(Material.DETECTOR_RAIL);
			try {
				blocsInterdits.add(Material.OBSERVER);
			} catch (NoSuchFieldError e) {
				// Rien
			}
		}
		if (!Config.getConfig().getBoolean("map creation.allow fluids"))
		{
			blocsInterdits.add(Material.WATER);
			blocsInterdits.add(Material.STATIONARY_WATER);
			blocsInterdits.add(Material.LAVA);
			blocsInterdits.add(Material.STATIONARY_LAVA);
			blocsInterdits.add(Material.ICE);
		}
		blocsInterdits.add(Material.TNT);
		blocsInterdits.add(Material.MOB_SPAWNER);
		blocsInterdits.add(Material.MONSTER_EGGS);
		blocsInterdits.add(Material.DRAGON_EGG);
		blocsInterdits.add(Material.ENDER_CHEST);
		blocsInterdits.add(Material.COMMAND);
		blocsInterdits.add(Material.PORTAL);
		blocsInterdits.add(Material.ENDER_PORTAL);

		exceptionsOPs.add(Material.COMMAND);
		try {
			blocsInterdits.add(Material.COMMAND_CHAIN);
			blocsInterdits.add(Material.COMMAND_REPEATING);
			blocsInterdits.add(Material.END_GATEWAY);

			exceptionsOPs.add(Material.COMMAND_CHAIN);
			exceptionsOPs.add(Material.COMMAND_REPEATING);
		} catch (NoSuchFieldError e) {
			// Rien
		}

		// Téléchargement des maps téléchargeables
		if (Config.online())
		{
			Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
				public void run() {
					synchroWeb();
				}
			}, 500); // 25 secondes
		}

		// Programmation de la vidange mémoire
		if (Config.getConfig().getInt("memory dump interval") > 0)
		{
			int intervalle = Config.getConfig().getInt("memory dump interval") * 60 * 20;
			Bukkit.getScheduler().runTaskTimer(CreativeParkour.getPlugin(), new Runnable() {
				public void run() {
					vidangeMemoire();
				}
			}, intervalle, intervalle);
		}

		RewardManager.enable();
		PlayerProfiles.enable();


		// Réintégration s'il y a des joueurs dans les maps
		Object[] onlinePlayers = Bukkit.getOnlinePlayers().toArray();
		for (int i=0; i < onlinePlayers.length; i++)
		{
			reintegrerMapOuQuitter((Player) onlinePlayers[i], false);
		}
	}

	static void disable()
	{
		for (int i=0; i < joueurs.size(); i++)
		{
			if (joueurs.get(i).getMap() != null)
			{
				joueurs.get(i).quitter(true, false);
			}
		}
	}

	/**
	 * Contacte creativeparkour.net pour mettre à jour la liste des maps téléchargeables, mettre à jour les temps des joueurs, mettre à jour leurs noms, et envoyer les nouveaux fantômes
	 */
	protected static void synchroWeb()
	{
		synchroWeb(null);
	}

	/**
	 * Contacte creativeparkour.net pour mettre à jour la liste des maps téléchargeables, mettre à jour les temps des joueurs, mettre à jour leurs noms, et envoyer les nouveaux fantômes
	 * @param sender Joueur auquel envoyer les infos sur l'état de la requête (ou null si pas besoin)
	 */
	protected static void synchroWeb(final CommandSender sender)
	{
		if (Config.online())
		{
			if (sender != null)
				sender.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.sync msg"));
			Bukkit.getScheduler().runTaskAsynchronously(CreativeParkour.getPlugin(), new Runnable() {
				public void run() {
					Map<String, String> paramsPost = new HashMap<String, String>();

					StringBuffer uuidsMaps = new StringBuffer();
					for (CPMap m : maps.values())
					{
						if (m.isPlayable())
							uuidsMaps.append(m.getUUID().toString() + ";");
					}
					if (uuidsMaps.length() > 0)
						uuidsMaps.deleteCharAt(uuidsMaps.length() - 1);
					paramsPost.put("uuidsMapsLocales", uuidsMaps.toString());

					StringBuffer uuidsJoueurs = new StringBuffer();
					for (Entry<String, YamlConfiguration> e : Config.getConfigsJoueurs().entrySet())
					{
						String uuid = e.getKey();
						uuidsJoueurs.append( uuid + ":" + e.getValue().getString("name") + ";");
					}
					if (uuidsJoueurs.length() > 0)
						uuidsJoueurs.deleteCharAt(uuidsJoueurs.length() - 1);
					paramsPost.put("joueursConnus", uuidsJoueurs.toString());

					StringBuffer fantomesLocaux = new StringBuffer();
					if (Config.fantomesPasInterdits() && (Config.getConfig().getBoolean("online.upload ghosts") || Config.getConfig().getBoolean("online.download ghosts")))
					{
						List<File> fichiers = GameManager.getFichiersTemps();
						if (fichiers != null && fichiers.size() > 0)
						{
							for (File f : fichiers)
							{
								YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
								String state = yml.getString("state");
								if ((yml.getConfigurationSection("ghost") != null && yml.getConfigurationSection("ghost").getKeys(false).size() > 0) || (state != null && EtatTemps.valueOf(state) == EtatTemps.TO_DOWNLOAD)) // S'il y a un fantôme ou qu'il est à télécharger
									fantomesLocaux.append(f.getName().replace(".yml", "") + ":" + yml.getLong("date") + ";");
							}
							if (fantomesLocaux.length() > 0)
								fantomesLocaux.deleteCharAt(fantomesLocaux.length() - 1);
						}
					}
					paramsPost.put("fantomesLocaux", fantomesLocaux.toString());

					// Envoi de la liste des fantômes supprimés pour pas qu'ils ne se retéléchargent
					List<String> exclusions = exclusions_temps.getStringList("list");
					StringBuffer tempsExclus = new StringBuffer();
					for (String s : exclusions)
					{
						tempsExclus.append(s + ";");
					}
					if (tempsExclus.length() > 0)
						tempsExclus.deleteCharAt(tempsExclus.length() - 1);
					paramsPost.put("fantomesSupprimes", tempsExclus.toString());

					paramsPost.put("envoiFantomesAutorise", Config.getConfig().getString("online.upload ghosts"));
					paramsPost.put("telechargementFantomesAutorise", Config.getConfig().getString("online.download ghosts"));

					if (sender != null)
						sender.sendMessage(CPRequest.messageAttente());
					try {
						CPRequest.effectuerRequete("list.php", paramsPost, null, GameManager.class.getMethod("reponseListe", JsonObject.class, String.class, CommandSender.class), sender);
					} catch (NoSuchMethodException | SecurityException e) {
						CreativeParkour.erreur("LIST", e, true);
					}
				}
			});
		}
		else if (sender != null)
		{
			sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("online disabled"));
		}
	}


	/**
	 * <em>Third-party plugins cannot use this method through CreativeParkour's API (it will throw an {@code InvalidQueryResponseException}).</em><br>
	 * Method called when <a href="https://creativeparkour.net" target="_blank">creativeparkour.net</a> responds to a query.
	 * @param json
	 * @param rep
	 * @param sender
	 * @throws InvalidQueryResponseException If the {@code Request} has not been registered before.
	 */
	public static void reponseListe(JsonObject json, String rep, CommandSender sender) throws InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponseListe") && !CreativeParkour.erreurRequete(json, sender) && json.get("data") != null)
		{
			Bukkit.getScheduler().runTaskAsynchronously(CreativeParkour.getPlugin(), new Runnable() {
				public void run() {
					mapsTelechargeables.clear();
					// Liste des maps téléchargeables
					if (json.get("data").isJsonObject())
					{
						JsonElement o = json.get("data").getAsJsonObject().get("maps");
						if (o != null && o.isJsonArray())
						{
							JsonArray liste = o.getAsJsonArray();
							for (JsonElement m : liste)
							{
								JsonObject map = m.getAsJsonObject();
								if (map.get("verConversion").getAsInt() <= CreativeParkour.getServVersion()) // We only want compatible maps
									mapsTelechargeables.add(new CPMap(map.get("id").getAsString(), map.get("createur").getAsString(), map.get("nom").getAsString(), map.get("difficulte").getAsFloat(), map.get("qualite").getAsFloat(), map.get("versionMin").getAsInt(), map.get("verConversion").getAsInt()));
							}

							// Mise à jour des inventaires de sélection de maps des joueurs qui l'ont ouvert
							if (!mapsTelechargeables.isEmpty())
							{
								Bukkit.getScheduler().scheduleSyncDelayedTask(CreativeParkour.getPlugin(), new Runnable() {
									public void run() {
										for (Joueur j : joueurs)
										{
											if (j.invSelection != null && j.getPlayer().hasPermission("creativeparkour.download"))
												j.invSelection.mettreAJourTelechargeables();
										}
									}
								});
							}
						}

						// Liste des maps partagées par le serveur, pour la suite...
						o = json.get("data").getAsJsonObject().get("mapsPartagees");
						List<String> mapsPartagees = new ArrayList<String>();
						if (o != null && o.isJsonArray())
						{
							Type listType = new TypeToken<List<String>>() {}.getType();
							mapsPartagees = new Gson().fromJson(o, listType);
						}

						// Mise à jour des votes, et remplissage d'une liste pour envoyer les votes locaux
						StringBuffer notesAEnvoyer = new StringBuffer();
						o = json.get("data").getAsJsonObject().get("notes");
						if (o != null && o.isJsonArray())
						{
							JsonArray liste = o.getAsJsonArray();
							for (JsonElement e : liste)
							{
								JsonObject obj = e.getAsJsonObject();
								CPMap m = getMap(UUID.fromString(obj.get("uuidMap").getAsString()));
								if (m != null && (m.getState() == CPMapState.DOWNLOADED || mapsPartagees.contains(m.getUUID().toString())))
								{
									m.setDifficulty(obj.get("d").getAsFloat());
									m.setQuality(obj.get("q").getAsFloat());
									m.sauvegarder();

									// Votes à envoyer
									for (Entry<String, Vote> entry : m.getVotes().entrySet())
									{
										if (entry.getValue().getDifficulty() > 0 || entry.getValue().getQuality() > 0)
											notesAEnvoyer.append(m.getUUID().toString()).append('_').append(entry.getKey()).append(':').append(entry.getValue().toConfigString()).append(";");
									}
								}
							}
						}
						if (notesAEnvoyer.length() > 0)
							notesAEnvoyer.deleteCharAt(notesAEnvoyer.length() - 1);

						// Mise à jour des noms des joueurs
						o = json.get("data").getAsJsonObject().get("nomsChanges");
						if (o != null && o.isJsonArray())
						{
							JsonArray liste = o.getAsJsonArray();
							for (JsonElement e : liste)
							{
								JsonObject obj = e.getAsJsonObject();
								String uuid = obj.get("uuid").getAsString();
								String nom = obj.get("nom").getAsString();
								String nom2 = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
								if (nom2 == null || nom2.isEmpty()) // Il ne faut pas mettre à jour les joueurs qui sont sur ce serveur
								{
									NameManager.enregistrerNomJoueur(uuid, nom);
								}
							}
						}

						List<CPMap> mapsAMettreAJour = new ArrayList<CPMap>();
						// Suppression des fantômes à supprimer
						o = json.get("data").getAsJsonObject().get("fantomesASupprimer");
						if (o != null && o.isJsonArray())
						{
							JsonArray liste = o.getAsJsonArray();
							for (JsonElement e : liste)
							{
								String nomFantome = e.getAsString();
								UUID uuidMap = UUID.fromString(nomFantome.split("_")[0]);
								CPMap map = getMap(uuidMap);
								if (map != null)
								{
									File f = getFichierTemps(nomFantome);
									if (f != null)
									{
										CPTime t = map.getTempsAvecFichier(f);
										if (t != null && t.etat != EtatTemps.LOCAL)
										{
											supprimerFichiersTemps(t.playerUUID, uuidMap, false);
											mapsAMettreAJour.add(map);
										}
									}
								}
							}
						}

						// Enregistrement des fantômes envoyés (en partie) par le site
						if (Config.getConfig().getBoolean("online.download ghosts"))
						{
							o = json.get("data").getAsJsonObject().get("fantomesATelecharger");
							if (o != null && o.isJsonArray())
							{
								JsonArray liste = o.getAsJsonArray();
								for (JsonElement e : liste)
								{
									JsonObject obj = e.getAsJsonObject();
									CPMap m = getMap(UUID.fromString(obj.get("uuidMap").getAsString()));
									if (m != null)
									{
										CPTime t = new CPTime(UUID.fromString(obj.get("uuidJoueur").getAsString()), m, obj.get("ticks").getAsInt());
										t.etat = EtatTemps.TO_DOWNLOAD;
										t.realMilliseconds = obj.get("millisecondes").getAsLong();
										t.sauvegarder(new Date(obj.get("date").getAsLong()));
										mapsAMettreAJour.add(m);

										NameManager.enregistrerNomJoueur(obj.get("uuidJoueur").getAsString(), obj.get("nomJoueur").getAsString());
									}
								}
							}
						}

						// Mise à jour des temps dans les maps

						Bukkit.getScheduler().scheduleSyncDelayedTask(CreativeParkour.getPlugin(), new Runnable() {
							public void run() {
								for (CPMap m : mapsAMettreAJour)
								{
									m.getListeTemps(true);
									Panneau.majClassements(m);
									// Mise à jour des leaderboard des joueurs
									for (Joueur j : getJoueurs(m.getUUID()))
									{
										j.calculerScoreboard();
										j.choixFantomesPreferes();
										j.majTeteFantomes();
									}
								}
							}
						});


						// Enregistrement du nombre de tricheurs
						o = json.get("data").getAsJsonObject().get("nbTricheurs");
						if (o != null)
						{
							nbTricheurs = o.getAsInt();

							// Enregistrement des joueurs qui peuvent recevoir des notifications de triche
							o = json.get("data").getAsJsonObject().get("receveursNotifsTriche");
							receveursNotifsTriche = new ArrayList<UUID>();
							if (o != null && o.isJsonArray())
							{
								JsonArray liste = o.getAsJsonArray();
								for (JsonElement e : liste)
								{
									receveursNotifsTriche.add(UUID.fromString(e.getAsString()));
								}
							}
						}


						// Envoi des notes et des fantômes
						Map<String, String> paramsPost = new HashMap<String, String>();
						if (notesAEnvoyer.length() > 0)
							paramsPost.put("notes", notesAEnvoyer.toString());

						// Traitement des fantômes à envoyer
						Map<String, Boolean> autorisationsJoueurs = new HashMap<String, Boolean>();
						o = json.get("data").getAsJsonObject().get("fantomesAEnvoyer");
						if (o != null && o.isJsonArray() && Config.getConfig().getBoolean("online.upload ghosts"))
						{
							JsonArray liste = o.getAsJsonArray();
							int i = 0;
							for (JsonElement e : liste)
							{
								String nomFantome = e.getAsString();
								CPMap m = getMap(CPUtils.timeFileUUIDs(nomFantome).get("map"));
								if (m != null)
								{
									CPTime t = m.getTempsAvecFichier(getFichierTemps(nomFantome));
									String uuidJoueur = t.playerUUID.toString();
									if (!autorisationsJoueurs.containsKey(uuidJoueur))
										autorisationsJoueurs.put(uuidJoueur, Config.getConfJoueur(uuidJoueur).getBoolean(PlayerSetting.ENVOYER_FANTOMES.path()));
									if (autorisationsJoueurs.get(uuidJoueur)) // Si le joueur a autorisé l'envoi de fantômes
										paramsPost.put("fantome-" + i, t.getJson().toString());
									i++;
								}
							}
						}

						if (paramsPost.size() > 0)
						{
							try {
								CPRequest.effectuerRequete("data.php", paramsPost, null, null, null);
							} catch (SecurityException e) {
								CreativeParkour.erreur("DATA", e, true);
							}
						}

						vidangeMemoire();
					}

					if (sender != null)
						sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.sync done"));
				}
			});
		}
	}

	static void telechargerFantomes(List<String> liste)
	{
		if (Config.online() && Config.getConfig().getBoolean("online.download ghosts"))
		{
			Map<String, String> paramsPost = new HashMap<String, String>();

			for (int i=0; i < liste.size(); i++)
			{
				paramsPost.put("fantome-" + i, liste.get(i));
			}

			try {
				CPRequest.effectuerRequete("ghosts-download.php", paramsPost, null, GameManager.class.getMethod("reponseTelechargementFantomes", JsonObject.class, String.class, Player.class), null);
			} catch (NoSuchMethodException | SecurityException e) {
				CreativeParkour.erreur("GHOSTSDOWNLOAD", e, true);
			}
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
	public static void reponseTelechargementFantomes(JsonObject json, String rep, Player p) throws InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponseTelechargementFantomes") && !CreativeParkour.erreurRequete(json, null) && json.get("data") != null)
		{
			if (json.get("data").isJsonObject())
			{
				// Enregistrement des fantômes envoyés par le site
				JsonElement o = json.get("data").getAsJsonObject().get("fantomes");
				if (o != null && o.isJsonArray())
				{
					JsonArray liste = o.getAsJsonArray();
					for (JsonElement e : liste)
					{
						JsonObject obj = e.getAsJsonObject();
						// Enregistrement du profil, direct
						PlayerProfiles.ajouterTextures(UUID.fromString(obj.get("uuidJoueur").getAsString()), obj.get("texturesProfil").getAsString(), obj.get("signatureProfil").getAsString(), true);

						// Traitement du temps
						CPMap m = getMap(UUID.fromString(obj.get("uuidMap").getAsString()));
						if (m != null)
						{
							for (CPTime t : m.getListeTemps())
							{
								try {
									if (t.etat == EtatTemps.TO_DOWNLOAD && t.playerUUID.toString().equalsIgnoreCase(obj.get("uuidJoueur").getAsString()))
									{
										t.ticks = obj.get("ticks").getAsInt();
										t.realMilliseconds = obj.get("millisecondes").getAsLong();
										t.etat = EtatTemps.DOWNLOADED;
										t.ajouterCheckpoints(obj.get("donnees").getAsJsonObject().get("checkpoints").getAsJsonArray());
										t.ajouterPositions(obj.get("donnees").getAsJsonObject().get("positions").getAsJsonArray());

										t.sauvegarder(new Date(obj.get("date").getAsLong()));

										break;
									}
								} catch (Error er) {
									CreativeParkour.erreur("TELECHARGEMENT FANTOME-" + m.getUUID().toString() + "_" + t.playerUUID.toString(), er, true);
								} catch (Exception ex) {
									CreativeParkour.erreur("TELECHARGEMENT FANTOME-" + m.getUUID().toString() + "_" + t.playerUUID.toString(), ex, true);
								}
							}
						}
					}
				}
			}

			Joueur j = getJoueur(p);
			if (j != null)
				j.downloadingGhosts = false;
		}
	}

	/**
	 * Supprime de la mémoire les temps des maps vides et les joueurs qui ne jouent plus
	 */
	synchronized static void vidangeMemoire()
	{
		for (Iterator<Joueur> iterator = joueurs.iterator(); iterator.hasNext();)
		{
			Joueur j = iterator.next();
			if (j.getMap() == null && j.invSelection == null)
			{
				Config.supprConfJoueur(j.getUUID().toString());
				iterator.remove();
			}
		}
		for (CPMap m : maps.values())
		{
			m.vidange();
		}
	}

	static void retirerTemps(Player p, UUID victime, boolean tous)
	{
		Joueur j = getJoueur(p);
		CPMap m = null;
		if (j != null)
			m = j.getMapObjet();
		if (m == null && !tous)
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.removetime error"));
		}
		else
		{
			if (tous)
				supprimerFichiersTemps(victime, null, true);
			else
				supprimerFichiersTemps(victime, m.getUUID(), true);
			p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.removetime ok").replace("%player", NameManager.getNomAvecUUID(victime)));

			// Mise à jour de tous les panneaux de classements
			// Mise à jour des liste de temps des maps
			if (!tous && m != null)
			{
				if (m.listeTempsDispo())
					m.getListeTemps(true);
				Panneau.majClassements(m);
			}
			else
			{
				for (CPMap m1 : maps.values())
				{
					if (m1.listeTempsDispo())
						m1.getListeTemps(true);
					Panneau.majClassements(m1);
				}
			}

			// Suppression du joueur des scoreboard des autres gens
			for (int i=0; i < joueurs.size(); i++)
			{
				if (tous || m.equals(joueurs.get(i).getMapObjet()))
					joueurs.get(i).calculerScoreboard();
			}
		}
	}

	static void supprimerFichiersTemps(UUID uuidJoueur, UUID uuidMap, boolean ajouterExclusion)
	{
		if (uuidJoueur == null && uuidMap == null)
			throw new SecurityException();
		else
		{
			List<String> exclusions = exclusions_temps.getStringList("list");
			for (File f : getFichiersTemps())
			{
				Map<String, UUID> uuids = CPUtils.timeFileUUIDs(f.getName());
				if ((uuidJoueur == null || uuids.get("player").equals(uuidJoueur)) && (uuidMap == null || uuids.get("map").equals(uuidMap)))
				{
					f.delete();
					String nomFichier = f.getName().replace(".yml", "");
					if (!exclusions.contains(nomFichier) && ajouterExclusion)
						exclusions.add(nomFichier);
				}
			}
			exclusions_temps.set("list", exclusions);
			try {
				exclusions_temps.save(fichier_exclusions_temps);
			} catch (IOException e) {
				Bukkit.getLogger().warning("An error occured while loading file 'CreativeParkour/deleted times.yml'");
				e.printStackTrace();
			}
		}
	}

	static List<File> getFichiersMaps()
	{
		return CPUtils.filesInFolder(dossier_maps);
	}

	static List<File> getFichiersTemps()
	{
		return CPUtils.filesInFolder(dossier_temps);
	}

	static void jouer(Player p, String nomMap) throws IOException
	{
		long nano = System.nanoTime();
		CPMap m = getMap(nomMap);
		if (m != null && m.isPlayable())
			jouer(p, m, false, true);
		else
		{
			if (!nomMap.isEmpty())
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.play error"));
			nano = CPUtils.debugNanoTime("PLAY1", nano);

			// Importation d'éventuelles maps dans le dossier "Automatically import maps"
			File folder = new File(CreativeParkour.getPlugin().getDataFolder(), "/Automatically import maps");
			folder.mkdirs();
			File dossierDest = new File(CreativeParkour.getPlugin().getDataFolder(), "/Not imported maps");
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++)
			{
				File f = listOfFiles[i];
				if (f.isFile() && f.getName().endsWith(".cpmap"))
				{
					GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(f));
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
						try {
							JsonObject jsData = new JsonParser().parse(br.readLine()).getAsJsonObject();
							if (mapExistante(jsData.get("uuidMap").getAsString()))
							{
								if (p.isOp())
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.import error 2").replace("%file", f.getName()));
							}
							else
							{
								CPMap map = construireMapTelechargee(jsData, CPMapState.PUBLISHED, p);
								if (p.isOp())
								{
									if (map == null)
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.import error").replace("%map", f.getName()));
									}
									else
									{
										p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.import success").replace("%map", map.getName()));
										dossierDest = new File(CreativeParkour.getPlugin().getDataFolder(), "/Imported maps");
									}
								}
							}
						} finally {
							br.close();
						}
					} finally {
						gzip.close();
					}

					dossierDest.mkdirs();
					Files.move(f, new File(dossierDest, f.getName()));
				}
			}
			nano = CPUtils.debugNanoTime("PLAY2", nano);

			selectionMap(p); // Ouverture de l'inventaire avec la liste des maps
		}

		// Mise à jour de la liste des maps téléchargeables en boucle si c'est le premier coup
		if (!taskListe && Config.online() && p.hasPermission("creativeparkour.download"))
		{
			taskListe = true;
			Bukkit.getScheduler().runTaskTimer(CreativeParkour.getPlugin(), new Runnable() {
				public void run() {
					synchroWeb();
				}
			}, 20, 144000); // Dans 1 seconde puis toutes les 2 heures
		}
	}

	static void jouer(Player p, CPMap map, boolean pasSauvegarderInventaire, boolean teleporter)
	{
		Joueur j = getJoueur(p);
		if (Config.isBanned(p) || !p.hasPermission("creativeparkour.play"))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("ban"));
		}
		else if (map != null)
		{
			if (j == null || j.getMap() == null)
				j = getJoueurAutorise(p, "play", !pasSauvegarderInventaire);
			else
				j.quitter(false, true);
			if (j != null)
			{
				j.setMap(map.getUUID());
				map.jouer(j, teleporter);

				map.addAttempt(p.getUniqueId());
			}
		}
	}

	private static void selectionMap(Player p)
	{
		long nano = System.nanoTime();
		// Etablissement de la liste des maps avec celles qui sont épinglées en premier
		List<CPMap> listeMaps = new ArrayList<CPMap>();
		for (CPMap m : maps.values())
		{
			if (m.isPlayable())
			{
				listeMaps.add(m);
			}
		}
		nano = CPUtils.debugNanoTime("PLAY3", nano);

		Joueur j = getJoueur(p);
		if (j == null)
		{
			CPUtils.sendInfoMessage(p, Langues.getMessage("commands.play tip"));
			j = new Joueur(p, false);
			addJoueur(j);
		}
		nano = CPUtils.debugNanoTime("PLAY4", nano);
		j.invSelection = new InventaireSelection(listeMaps, p);
		nano = CPUtils.debugNanoTime("PLAY5", nano);
		j.invSelection.setPage(1);
		nano = CPUtils.debugNanoTime("PLAY6", nano);
		p.openInventory(j.invSelection.getInventaire());
		nano = CPUtils.debugNanoTime("PLAY7", nano);
	}

	static void creerMap(Player p)
	{
		// Comptage des joueurs dans le monde
		int nbJoueurs = 0;
		for (int i=0; i < joueurs.size(); i++)
		{
			if (joueurs.get(i).getPlayer().getWorld().equals(Bukkit.getWorld(Config.getConfig().getString("map storage.map storage world"))))
			{
				nbJoueurs++;
			}
		}

		CreativeParkour.debug("CREATE", p.getName() + " has " + nbMapsPubliees(p) + " published maps. creativeparkour.infinite=" + p.hasPermission("creativeparkour.infinite") + "; map creation.maps per player limit=" + Config.getConfig().getInt("map creation.maps per player limit"));
		if (Config.isBanned(p) || !p.hasPermission("creativeparkour.create"))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
		}
		else if (!p.hasPermission("creativeparkour.infinite") && nbMapsPubliees(p) > Config.getConfig().getInt("map creation.maps per player limit"))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("too many maps"));
		}
		else if (Config.getConfig().getInt("game.max players in storage world") != -1 && nbJoueurs >= Config.getConfig().getInt("game.max players in storage world"))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("too many players"));
		}
		else
		{
			Joueur j = getJoueur(p);
			if (j == null)
			{
				j = new Joueur(p, false);
				addJoueur(j);
			}

			j.invCreation = new InventaireCreation(j);
			j.invCreation.remplir();
			p.openInventory(j.invCreation.getInventaire());
		}
	}

	private static Map<Vector, MaterialData> genererContours(World world, int xMap, int yMap, int zMap, int xMaxMap, int yMaxMap, int zMaxMap)
	{
		Map<Vector, MaterialData> listeBlocs = new HashMap<Vector, MaterialData>();
		// Couche de bedrock et toit de barrières
		for (int x = xMap; x <= xMaxMap; x++)
		{
			for (int z = zMap; z <= zMaxMap; z++)
			{
				if (world.getBlockAt(x, yMap-1, z).getType() != Material.BEDROCK)
					listeBlocs.put(new Vector(x, yMap-1, z), new MaterialData(Material.BEDROCK));
				if (world.getBlockAt(x, yMaxMap + 1, z).getType() != Material.BARRIER)
					listeBlocs.put(new Vector(x, yMaxMap + 1, z), new MaterialData(Material.BARRIER));
			}
		}

		// Murs sur l'axe X
		for (int x = xMap - 1; x <= xMaxMap + 1; x++) // -1 et +1 pour faire les coins
		{
			for (int y = yMap - 1; y <= yMaxMap + 1; y++)
			{
				if (world.getBlockAt(x, y, zMap - 1).getType() != Material.BARRIER)
					listeBlocs.put(new Vector(x, y, zMap - 1), new MaterialData(Material.BARRIER));
				if (world.getBlockAt(x, y, zMaxMap + 1).getType() != Material.BARRIER)
					listeBlocs.put(new Vector(x, y, zMaxMap + 1), new MaterialData(Material.BARRIER));
			}
		}

		// Murs sur l'axe Z
		for (int z = zMap; z <= zMaxMap; z++)
		{
			for (int y = yMap - 1; y <= yMaxMap + 1; y++)
			{
				if (world.getBlockAt(xMap - 1, y, z).getType() != Material.BARRIER)
					listeBlocs.put(new Vector(xMap - 1, y, z), new MaterialData(Material.BARRIER));
				if (world.getBlockAt(xMaxMap + 1, y, z).getType() != Material.BARRIER)
					listeBlocs.put(new Vector(xMaxMap + 1, y, z), new MaterialData(Material.BARRIER));
			}
		}
		return listeBlocs;
	}

	static void telechargerMap(CommandSender sender, String arg) throws NoSuchMethodException, SecurityException
	{
		if (!Config.online())
			sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.download error disabled"));
		else if (!NumberUtils.isNumber(arg) && !arg.toLowerCase().contains("?id=") /*&& !idMap.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")*/) // Si ce n'est ni un nombre, ni un URL, ni un UUID
			sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.download error ID"));
		else
		{
			Player p = (sender instanceof Player) ? (Player) sender : null;
			Joueur j = null;
			if (p != null)
			{
				j = getJoueur(p);
				if (j == null)
				{
					j = new Joueur(p, false);
					addJoueur(j);
				}
			}
			if (j == null || j.peutTelecharger())
			{
				HashMap<String, String> params = new HashMap<String, String>();
				if (p != null)
				{
					params.put("ipJoueur", p.getAddress().getHostName());
					params.put("uuidJoueur", p.getUniqueId().toString());
				}
				params.put("idMap", arg);
				params.put("versionServ", String.valueOf(CreativeParkour.getServVersion()));
				CPRequest.effectuerRequete("download.php", params, null, GameManager.class.getMethod("reponseTelecharger", JsonObject.class, String.class, CommandSender.class), sender);
				sender.sendMessage(Config.prefix() + ChatColor.ITALIC + Langues.getMessage("commands.download loading"));
			}
		}
	}

	/**
	 * <em>Third-party plugins cannot use this method through CreativeParkour's API (it will throw an {@code InvalidQueryResponseException}).</em><br>
	 * Method called when <a href="https://creativeparkour.net" target="_blank">creativeparkour.net</a> responds to a query.
	 * @param json
	 * @param rep
	 * @param sender
	 * @throws InvalidQueryResponseException If the {@code Request} has not been registered before.
	 */
	public static void reponseTelecharger(JsonObject json, String rep, CommandSender sender) throws IllegalArgumentException, InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponseTelecharger"))
		{
			if (CreativeParkour.erreurRequete(json, sender))
			{
				if (sender instanceof Player)
				{
					Joueur j = getJoueur((Player) sender);
					if (j != null)
						j.permettreTelechargement(1000 * 10); // Dans 10 secondes
				}
			}
			else
			{
				JsonObject jsData = json.get("data").getAsJsonObject();
				if (jsData.get("servInconnu") != null && jsData.get("servInconnu").getAsBoolean() == true)
				{
					sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.share unknown server"));
				}
				else if (mapExistante(jsData.get("uuidMap").getAsString()))
				{
					if (sender instanceof Player)
					{
						Player p = (Player) sender;
						jouer(p, getMap(UUID.fromString(jsData.get("uuidMap").getAsString())), false, true);
						Joueur j = getJoueur(p);
						if (j != null)
							j.permettreTelechargement(1000 * 20); // Dans 20 secondes
					}
					else
						sender.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.download already downloaded"));
				}
				else if (jsData.get("verConversion").getAsInt() > CreativeParkour.getServVersion())
				{
					String details = "NULL";
					try {
						details = jsData.get("detailsCompat").getAsString().replace(";", ", ");
					} catch (Exception e) {
						// Rien
					}
					sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.download error incompatible").replace("%elements", details));
					if (sender instanceof Player)
					{
						Joueur j = getJoueur((Player) sender);
						if (j != null)
							j.permettreTelechargement(1000 * 5); // Dans 5 secondes
					}
				}
				else
				{
					sender.sendMessage(Config.prefix() + ChatColor.GRAY + "" + ChatColor.ITALIC + Langues.getMessage("commands.download wait 2").replace("%map", jsData.get("nomMap").getAsString()));
					final CPMap map = construireMapTelechargee(jsData, CPMapState.DOWNLOADED, sender);
					final Joueur j = (sender instanceof Player) ? getJoueur((Player) sender) : null;

					if (map == null)
					{
						sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.download error build"));
						if (j != null)
							j.permettreTelechargement(1000 * 20); // Dans 20 secondes
					}
					else
					{
						// Après le remplissage de la map, le joueur peut jouer
						Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
							public void run() {
								if (sender instanceof Player)
								{
									Joueur j1 = j;
									if (j1 == null || j1.getMap() == null)
										j1 = sauvInventaire((Player) sender, "play");
									if (j1 != null)
									{
										j1.setMap(map.getUUID());
										map.jouer(j1);
									}
								}
								else
									sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.download done").replace("%map", map.getName()));
							}
						}, map.getDelaiRemplissage());
					}
				}
			}

			// Mise à jour de la liste des maps téléchargeables et téléchargement des fantômes
			synchroWeb();
		}
	}

	private static CPMap construireMapTelechargee(JsonObject jsData, CPMapState etatAMettre, CommandSender sender)
	{
		final String uuid = jsData.get("uuidMap").getAsString();
		String nomMap = jsData.get("nomMap").getAsString();
		if (!mapExistante(uuid))
		{
			// Ajout d'un chiffre au nom s'il est déjà pris
			int nb = 1;
			for (CPMap map : maps.values())
			{
				String nomTemp = nomMap;
				if (nb > 1)
					nomTemp += " " + nb;
				if (nomTemp.equalsIgnoreCase(map.getName()))
				{
					nb++;
				}
			}
			if (nb > 1)
				nomMap += " " + nb;

			int taille = jsData.get("taille").getAsInt();
			// Création de la map
			int id = getIdMapDispo(taille);
			final World m = Config.getMonde();
			Block blocMin;
			CPMap mapID = maps.get(id);
			if (mapID != null && mapID.getState() == CPMapState.DELETED) // Si c'est le remplacement d'une map (la taille a déjà été vérifiée dans getIdMapDispo()
				blocMin = mapID.getMinLoc();
			else
			{
				blocMin = getBlocMapDispo(taille);
			}


			final int xMin = blocMin.getX();
			final int yMin = blocMin.getY();
			final int zMin = blocMin.getZ();
			int xMax = xMin + taille;
			int yMax = Math.min(yMin + taille, 126); // Au maximum 126 (1 de marge pour le toit en barrière invisible)
			int zMax = zMin + taille;
			RemplisseurBlocs remplisseur = null;
			try {
				JsonObject jsContenu = CPUtils.getJsonObjectPropre(jsData.get("contenu"));

				// Suppression des entités
				List<Entity> listeEntites = new ArrayList<Entity>();
				for (int i=0; i < m.getEntities().size(); i++)
				{
					Entity e = m.getEntities().get(i);
					if (!(e instanceof Player) && e.getLocation().getX()>=xMin && e.getLocation().getX()<=xMax && e.getLocation().getY()>=yMin && e.getLocation().getY()<=yMax && e.getLocation().getZ()>=zMin && e.getLocation().getZ()<=zMax)
					{
						listeEntites.add(e);
					}
				}
				for (int i=0; i < listeEntites.size(); i++)
				{
					listeEntites.get(i).remove();
				}

				// Traitement des blocs
				Map<Vector, MaterialData> listeBlocs = new HashMap<Vector, MaterialData>();
				int blocsFaits = 0;
				int moitie = Math.round((taille * (taille+1) * taille) / 2);
				// Ajout d'air partout
				for (int x = xMin; x <= xMax; x++)
				{
					for (int y = yMin; y <= yMax; y++)
					{
						for (int z = zMin; z <= zMax; z++)
						{
							if (m.getBlockAt(x, y, z).getType() != Material.AIR)
								listeBlocs.put(new Vector(x, y, z), new MaterialData(Material.AIR));

							blocsFaits++;
							if (blocsFaits == moitie)
							{
								sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "10 %");
							}
						}
					}
				}
				// Couche de bedrock
				listeBlocs.putAll(genererContours(m, xMin, yMin, zMin, xMax, yMax, zMax));
				sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "20 %");

				// Remplacement des blocs d'air qu'il faut par des blocs
				// Remplissage de la liste des types
				Map<Integer, JsonObject> types = new HashMap<Integer, JsonObject>();
				JsonArray jsTypes = jsContenu.get("types").getAsJsonArray();
				for (JsonElement t : jsTypes)
				{
					JsonObject jsO = t.getAsJsonObject();
					types.put(jsO.get("i").getAsInt(), jsO);
				}
				JsonArray blocs = jsContenu.get("blocs").getAsJsonArray();
				List<JsonObject> blocsDelai = new ArrayList<JsonObject>();
				List<JsonObject> blocsRedstone = new ArrayList<JsonObject>();
				boolean blocsConvertis = false;
				for (JsonElement jsB : blocs)
				{
					JsonObject jsO = jsB.getAsJsonObject();
					JsonObject type = types.get(jsO.get("i").getAsInt());
					Material mat = null;
					try {
						mat = Material.getMaterial(type.get("t").getAsString());
					} catch (NoSuchFieldError e) {
						// Nothing, see below
					}
					if (mat == null)
					{
						// Trying to replace the unknown Material with a compatible one
						switch (type.get("t").getAsString()) {
						case "END_BRICKS":
							mat = Material.SANDSTONE;
							type.addProperty("d", 2);
							break;
						case "PURPUR_BLOCK":
						case "PURPUR_PILLAR":
						case "PURPUR_DOUBLE_SLAB":
						case "RED_NETHER_BRICK":
							mat = Material.NETHER_BRICK;
							break;
						case "PURPUR_STAIRS":
							mat = Material.NETHER_BRICK_STAIRS;
							break;
						case "PURPUR_SLAB":
							mat = Material.STEP;
							type.addProperty("d", 6);
							break;
						case "BEETROOT_BLOCK":
							mat = Material.CARROT;
							break;
						case "MAGMA":
							mat = Material.NETHERRACK;
							break;
						case "NETHER_WART_BLOCK":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 14);
							break;
						case "BONE_BLOCK":
							mat = Material.QUARTZ_BLOCK;
							break;
						case "STRUCTURE_BLOCK":
							mat = Material.OBSIDIAN;
							break;
						case "STRUCTURE_VOID":
							mat = Material.AIR;
							break;
						case "WHITE_SHULKER_BOX":
						case "WHITE_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 0);
							break;
						case "ORANGE_SHULKER_BOX":
						case "ORANGE_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 1);
							break;
						case "MAGENTA_SHULKER_BOX":
						case "MAGENTA_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 2);
							break;
						case "LIGHT_BLUE_SHULKER_BOX":
						case "LIGHT_BLUE_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 3);
							break;
						case "YELLOW_SHULKER_BOX":
						case "YELLOW_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 4);
							break;
						case "LIME_SHULKER_BOX":
						case "LIME_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 5);
							break;
						case "PINK_SHULKER_BOX":
						case "PINK_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 6);
							break;
						case "GRAY_SHULKER_BOX":
						case "GRAY_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 7);
							break;
						case "SILVER_SHULKER_BOX":
						case "SILVER_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 8);
							break;
						case "CYAN_SHULKER_BOX":
						case "CYAN_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 9);
							break;
						case "PURPLE_SHULKER_BOX":
						case "PURPLE_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 10);
							break;
						case "BLUE_SHULKER_BOX":
						case "BLUE_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 11);
							break;
						case "BROWN_SHULKER_BOX":
						case "BROWN_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 12);
							break;
						case "GREEN_SHULKER_BOX":
						case "GREEN_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 13);
							break;
						case "RED_SHULKER_BOX":
						case "RED_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 14);
							break;
						case "BLACK_SHULKER_BOX":
						case "BLACK_GLAZED_TERRACOTTA":
							mat = Material.STAINED_CLAY;
							type.addProperty("d", 15);
							break;
						case "CONCRETE":
							mat = Material.STAINED_CLAY;
							break;
						case "CONCRETE_POWDER":
							mat = Material.SAND;
							break;
						}

						// If mat was changed, we warn the player that some blocks are changed
						// And we also change the Json because it is reused after
						if (mat != null)
						{
							blocsConvertis = true;
							type.addProperty("t", mat.toString());
						}
					}
					if (mat == null)
						throw new UnknownMaterialException("material \"" + type.get("t").getAsString() + "\" does not exist in your Minecraft version.");
					if (mat == Material.SIGN_POST || 
							mat == Material.WALL_SIGN || 
							mat == Material.STANDING_BANNER || 
							mat == Material.WALL_BANNER || 
							mat == Material.VINE || 
							mat == Material.LADDER ||
							mat == Material.SKULL ||
							mat == Material.BEACON ||
							mat == Material.CARPET ||
							mat == Material.SNOW ||
							mat == Material.GLOWSTONE ||
							mat == Material.REDSTONE_LAMP_ON ||
							mat == Material.SEA_LANTERN ||
							mat == Material.JACK_O_LANTERN ||
							mat == Material.BED_BLOCK ||
							mat.name().contains("PISTON") ||
							mat.name().contains("STAIR") ||
							mat.name().contains("CHORUS") ||
							mat.name().contains("DOOR"))
					{
						blocsDelai.add(jsO);

						if (mat == Material.BEACON && uuid.equalsIgnoreCase("e7d54103-66ec-42a1-9895-560c77e2cdf1")) // Dans Beacon Barrage, on ajoute des blocs sous les balises...
						{
							Map<Character, Integer> coords = CPUtils.parseCoordinates(jsO.get("c").getAsString());
							listeBlocs.put(new Vector(xMin + coords.get('x'), yMin + coords.get('y') - 1, zMin + coords.get('z')), new MaterialData(mat));
							listeBlocs.put(new Vector(xMin + coords.get('x') - 1, yMin + coords.get('y') - 1, zMin + coords.get('z')), new MaterialData(mat));
							listeBlocs.put(new Vector(xMin + coords.get('x') - 1, yMin + coords.get('y') - 1, zMin + coords.get('z') - 1), new MaterialData(mat));
							listeBlocs.put(new Vector(xMin + coords.get('x') - 1, yMin + coords.get('y') - 1, zMin + coords.get('z') + 1), new MaterialData(mat));
							listeBlocs.put(new Vector(xMin + coords.get('x') + 1, yMin + coords.get('y') - 1, zMin + coords.get('z')), new MaterialData(mat));
							listeBlocs.put(new Vector(xMin + coords.get('x') + 1, yMin + coords.get('y') - 1, zMin + coords.get('z') - 1), new MaterialData(mat));
							listeBlocs.put(new Vector(xMin + coords.get('x') + 1, yMin + coords.get('y') - 1, zMin + coords.get('z') + 1), new MaterialData(mat));
							listeBlocs.put(new Vector(xMin + coords.get('x'), yMin + coords.get('y') - 1, zMin + coords.get('z') - 1), new MaterialData(mat));
							listeBlocs.put(new Vector(xMin + coords.get('x'), yMin + coords.get('y') - 1, zMin + coords.get('z') + 1), new MaterialData(mat));
						}
					}
					else if (mat == Material.LEVER ||
							(mat.name().contains("REDSTONE") && !mat.name().contains("LAMP") && !mat.name().contains("ORE")) ||
							mat.name().contains("BUTTON") ||
							mat.name().contains("DIODE") ||
							mat.name().contains("TORCH") ||
							mat.name().contains("_PLATE"))
					{
						blocsRedstone.add(jsO);
					}
					else
					{
						Map<Character, Integer> coords = CPUtils.parseCoordinates(jsO.get("c").getAsString());
						Vector vect = new Vector(xMin + coords.get('x'), yMin + coords.get('y'), zMin + coords.get('z'));
						final Block b = m.getBlockAt(xMin + coords.get('x'), yMin + coords.get('y'), zMin + coords.get('z'));
						if (b.getX() < xMin || b.getY() < yMin || b.getZ() < zMin || b.getX() > xMax || b.getY() > yMax || b.getZ() > zMax) // Si le bloc n'est pas dans la map
						{
							throw new SecurityException();
						}
						else
						{
							listeBlocs.put(vect, new MaterialData(mat, type.get("d").getAsByte()));
						}
					}
				}
				sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "30 %");

				// Placement des blocs
				remplisseur = new RemplisseurBlocs(listeBlocs, m, sender, 30);
				remplisseur.runTaskTimer(CreativeParkour.getPlugin(), 1, 1);

				// Autres blocs
				Map<Integer, List<JsonObject>> listeDelais = new HashMap<Integer, List<JsonObject>>();
				int delai = remplisseur.getDureeTraitement() + 1;
				int cpt64 = 0; // Le délai augmente dès que ce truc est un multiple de 64
				for (JsonObject jsO : blocsDelai)
				{
					if (!listeDelais.containsKey(delai))
					{
						listeDelais.put(delai, new ArrayList<JsonObject>());
					}
					listeDelais.get(delai).add(jsO);

					// Incrémentation
					if (cpt64 % 64 == 0)
						delai++;
					cpt64++;
				}
				// Blocs redstone après les autres, 2 fois
				delai++;
				listeDelais.put(delai, new ArrayList<JsonObject>());
				listeDelais.put(delai + 20, new ArrayList<JsonObject>());
				for (JsonObject jsO : blocsRedstone)
				{
					listeDelais.get(delai).add(jsO);
					listeDelais.get(delai + 20).add(jsO);
				}
				new RemplisseurBlocsDelai(listeDelais, types, m, xMin, yMin, zMin, xMax, yMax, zMax).runTaskTimer(CreativeParkour.getPlugin(), 1, 1);


				// Entités
				final JsonArray entites = jsContenu.get("entites").getAsJsonArray();
				Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
					public void run() {
						for (JsonElement jsE : entites)
						{
							JsonObject jsO = jsE.getAsJsonObject();
							Location loc = new Location(m, xMin + jsO.get("x").getAsDouble(), yMin + jsO.get("y").getAsDouble(), zMin + jsO.get("z").getAsDouble(), jsO.get("yaw").getAsFloat(), jsO.get("pitch").getAsFloat());
							try {
								Entity e = m.spawnEntity(loc, EntityType.valueOf(jsO.get("type").getAsString()));
								if (e instanceof Painting)
								{
									Painting painting = (Painting) e;
									painting.setArt(Art.getByName(jsO.get("art").getAsString()));
								}
								else if (e instanceof ItemFrame)
								{
									ItemFrame itemFrame = (ItemFrame) e;
									try {
										itemFrame.setItem(new ItemStack(Material.getMaterial(jsO.get("item").getAsString())));
									} catch (NullPointerException e0) {
										// Nothing
									}
									itemFrame.setRotation(Rotation.valueOf(jsO.get("rotation").getAsString()));
								}
							} catch (IllegalArgumentException e) {
								// Nothing
							}
						}
					}
				}, delai);


				String uuidCreateur = CPUtils.separerUuidNom(jsData.get("createur").getAsString()).get("uuid");
				NameManager.enregistrerNomJoueur(uuidCreateur, CPUtils.separerUuidNom(jsData.get("createur").getAsString()).get("nom"));


				// Blocs spéciaux
				List<BlocSpecial> blocsSpeciaux = new ArrayList<BlocSpecial>();
				BlocSpawn spawn = null;
				JsonArray blocsS = jsContenu.get("blocs speciaux").getAsJsonArray();
				// Regroupement de tous les blocs sand un seul Array pour faciliter les choses après (c'était divisé dans d'anciennes versions car la programmeur est un con)
				JsonArray vrac = new JsonArray();
				for (JsonElement bloc : blocsS)
				{
					if (!bloc.isJsonArray()) // Que pour spawn, départs, arrivées, mort
					{
						vrac.add(bloc);
					}
					else
					{
						for (JsonElement bloc2 : bloc.getAsJsonArray())
							vrac.add(bloc2);
					}
				}
				// Traitement des blocs spéciaux
				for (JsonElement bloc : vrac)
				{
					JsonObject jsO = bloc.getAsJsonObject();
					String type = jsO.get("t").getAsString();
					Map<Character, Integer> coords = CPUtils.parseCoordinates(jsO.get("c").getAsString());
					Block block = m.getBlockAt(xMin + coords.get('x'), yMin + coords.get('y'), zMin + coords.get('z'));
					byte dir = jsO.has("dir") ? jsO.get("dir").getAsByte() : 0;

					if (type.equalsIgnoreCase(BlocSpawn.getType())) // Spawn
					{
						spawn = new BlocSpawn(block, dir);
					}
					else if (type.equalsIgnoreCase(BlocDepart.getType())) // Départs
					{
						blocsSpeciaux.add(new BlocDepart(block));
					}
					else if (type.equalsIgnoreCase(BlocArrivee.getType())) // Arrivées
					{
						blocsSpeciaux.add(new BlocArrivee(block));
					}
					else if (type.equalsIgnoreCase(BlocCheckpoint.getType())) // Checkpoints
					{
						blocsSpeciaux.add(new BlocCheckpoint(block, dir, jsO.get("prop").getAsString()));
					}
					else if (type.equalsIgnoreCase(BlocEffet.getType())) // Effets
					{
						blocsSpeciaux.add(new BlocEffet(block, jsO.get("effect").getAsString(), jsO.get("duration").getAsInt(), jsO.get("amplifier").getAsInt()));
					}
					else if (type.equalsIgnoreCase(BlocGive.getType())) // Gives
					{
						blocsSpeciaux.add(new BlocGive(block, jsO.get("type").getAsString(), jsO.get("action").getAsString()));
					}
					else if (type.equalsIgnoreCase(BlocMort.getType())) // Morts
					{
						blocsSpeciaux.add(new BlocMort(block));
					}
					else if (type.equalsIgnoreCase(BlocTP.getType())) // TP
					{
						blocsSpeciaux.add(new BlocTP(block, new Location(m, xMin + jsO.get("x").getAsDouble(), yMin + jsO.get("y").getAsDouble(), zMin + jsO.get("z").getAsDouble())));
					}
				}

				Set<UUID> contributeurs = new HashSet<UUID>();
				if (jsData.get("contributeurs").isJsonArray())
				{
					JsonArray c = jsData.get("contributeurs").getAsJsonArray();
					for (JsonElement contributeur : c)
					{
						String contributeurS = contributeur.getAsString();
						if (!contributeurS.isEmpty())
						{
							String uuidContrib = CPUtils.separerUuidNom(contributeurS).get("uuid");
							contributeurs.add(UUID.fromString(uuidContrib));
							NameManager.enregistrerNomJoueur(uuidContrib, CPUtils.separerUuidNom(contributeurS).get("nom"));
						}
					}
				}

				// Nettoyage du spawn pour pas que le mec tombe
				spawn.getBloc().setType(Material.AIR);
				spawn.getBloc().getRelative(BlockFace.UP).setType(Material.AIR);
				spawn.getBloc().getRelative(BlockFace.DOWN).setType(Material.BEDROCK);

				final CPMap map = new CPMap(id, uuid, etatAMettre, m, 
						m.getBlockAt(xMin, yMin, zMin), m.getBlockAt(xMax, yMax, zMax), 
						nomMap, UUID.fromString(uuidCreateur), contributeurs, false,
						spawn, blocsSpeciaux, jsContenu.get("hauteurMort").getAsInt(),
						(jsContenu.has("sneakAutorise") ? jsContenu.get("sneakAutorise").getAsBoolean() : true),
						(jsContenu.has("mortLave") ? jsContenu.get("mortLave").getAsBoolean() : false),
						(jsContenu.has("mortEau") ? jsContenu.get("mortEau").getAsBoolean() : false),
						(jsContenu.has("interactionsAutorisees") ? jsContenu.get("interactionsAutorisees").getAsBoolean() : true),
						null, jsData.get("difficulte").getAsFloat(), jsData.get("qualite").getAsFloat());
				maps.put(id, map);
				map.sauvegarder();

				map.setRemplisseur(remplisseur);

				if (blocsConvertis)
					sender.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("play.download converted"));

				return map;

			} catch (Exception | Error e) {
				CreativeParkour.erreur("TELECHARGER/IMPORTER-" + uuid, e, true);
				if (remplisseur != null)
					remplisseur.cancel();
				File f = getFichierMap(id);
				if (f != null && f.exists())
					f.delete();
			}
		}
		return null;
	}

	private static boolean mapExistante(String uuid)
	{
		CPMap map = getMap(UUID.fromString(uuid));
		return map != null && map.getState() != CPMapState.DELETED;
	}

	static void teleporterCreation(Player p, CPMap m, boolean pasSauvegarderInventaire)
	{
		teleporterCreation(p, m, pasSauvegarderInventaire, true);
	}

	static void teleporterCreation(Player p, CPMap m, boolean pasSauvegarderInventaire, boolean teleporter)
	{
		Joueur j = getJoueur(p);
		if (j == null || j.getMap() == null)
			j = getJoueurAutorise(p, "create", !pasSauvegarderInventaire);
		else
			j.quitter(false, true);
		if (j != null)
		{
			j.setMap(m.getUUID());
			j.modeCreation();
			if (teleporter)
				j.getPlayer().teleport(m.getSpawn().getLocation().add(0.5, 0, 0.5));
			CreativeParkour.debug("TC", m.getSpawn().getLocation().add(0.5, 0, 0.5).toString());
		}
	}

	static List<CPMap> getMapsContributeur(Player p)
	{
		List<CPMap> liste = new ArrayList<CPMap>();
		for (CPMap m : maps.values())
		{
			if (m.getState() == CPMapState.CREATION && m.getContributeurs().contains(p.getUniqueId()))
			{
				liste.add(m);
			}
		}
		return liste;
	}

	static void nouvelleMap(Player p, boolean reponse, CubeDeBlocs blocsDef)
	{
		if (reponse == false)
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.canceled"));
		}
		else
		{
			Joueur j = getJoueur(p);
			if (j == null || j.getMap() == null)
				j = sauvInventaire(p, "create");
			else
				j.quitter(false, true);
			if (j != null)
			{
				p.sendMessage(Config.prefix() + ChatColor.GRAY + "" + ChatColor.ITALIC + Langues.getMessage("creation.building"));
				// Suppression de l'éventuelle ancienne map
				CPMap ancienne = getMapEnCreation(p);
				if (ancienne != null)
					supprimerMap(ancienne.getUUID(), null);

				int taille = Config.getConfig().getInt("map storage.map size");
				int margeBlocsDef = 8;
				int margeCotes = margeBlocsDef;
				if (blocsDef != null)
				{
					int tailleMin = blocsDef.getTaille() + margeBlocsDef * 2;
					if (tailleMin > taille)
						taille = tailleMin;
					else
						margeCotes = (taille / 2) - (blocsDef.getTaille() / 2);
				}
				int id = getIdMapDispo(taille);
				World m = Config.getMonde();
				Block blocMin;
				if (maps.containsKey(id) && maps.get(id).getState() == CPMapState.DELETED) // Si c'est le remplacement d'une map (la taille a déjà été vérifiée dans getIdMapDispo()
					blocMin = maps.get(id).getMinLoc();
				else
				{
					CreativeParkour.debug("MG1", "Searching a free area to build a map...");
					blocMin = getBlocMapDispo(taille);
					CreativeParkour.debug("MG2", "Found!");
				}

				String uuid = UUID.randomUUID().toString();

				int xMin = blocMin.getX();
				int yMin = blocMin.getY();
				int zMin = blocMin.getZ();
				int xMax = xMin + taille;
				int yMax = Math.min(yMin + taille, 126); // Au maximum 126 (1 de marge pour le toit en barrière invisible)
				int zMax = zMin + taille;
				Block spawn = m.getBlockAt((xMin+xMax)/2, yMin, (zMin+zMax)/2);

				List<Entity> entites = new ArrayList<Entity>();
				for (int i=0; i < m.getEntities().size(); i++)
				{
					Entity e = m.getEntities().get(i);
					if (!(e instanceof Player) && e.getLocation().getX()>=xMin && e.getLocation().getX()<=xMax && e.getLocation().getY()>=yMin && e.getLocation().getY()<=yMax && e.getLocation().getZ()>=zMin && e.getLocation().getZ()<=zMax)
					{
						entites.add(e);
					}
				}
				for (int i=0; i < entites.size(); i++)
				{
					entites.get(i).remove();
				}

				Map<Vector, MaterialData> blocs = new HashMap<Vector, MaterialData>();
				for (int x = xMin; x <= xMax; x++)
				{
					for (int y = yMin; y <= yMax; y++)
					{
						for (int z = zMin; z <= zMax; z++)
						{
							blocs.put(new Vector(x, y, z), new MaterialData(Material.AIR));
						}
					}
				}

				// Ajout des éventuels blocs prédéfinis
				boolean blocInterdit = false;
				if (blocsDef != null)
				{
					Vector marge = new Vector(xMin + margeCotes, yMin + margeBlocsDef, zMin + margeCotes);
					for (Entry<Vector, MaterialData> e : blocsDef.getBlocs().entrySet())
					{
						if (blocsInterdits.contains(e.getValue().getItemType()))
							blocInterdit = true;
						else
							blocs.put(e.getKey().add(marge), e.getValue()); // Remplacement dans la liste
					}
				}

				blocs.putAll(genererContours(m, xMin, yMin, zMin, xMax, yMax, zMax));

				new RemplisseurBlocs(blocs, m).runTaskTimer(CreativeParkour.getPlugin(), 1, 1);

				CPMap map = new CPMap(id, uuid, CPMapState.CREATION, m, 
						blocMin, m.getBlockAt(xMax, yMax, zMax), 
						new String(), p.getUniqueId(), new HashSet<UUID>(), false, new BlocSpawn(spawn, (byte) 0), new ArrayList<BlocSpecial>(), 0, true, false, false, true, null, -1, -1);

				maps.put(id, map);
				map.sauvegarder();

				// Nettoyage du spawn
				spawn.setType(Material.AIR);
				spawn.getRelative(BlockFace.UP).setType(Material.AIR);
				spawn.getRelative(BlockFace.DOWN).setType(Material.BEDROCK);

				j.setMap(map.getUUID());
				p.teleport(spawn.getLocation().add(0.5, 0, 0.5));
				j.modeCreation();

				CPUtils.sendClickableMsg(p, Langues.getMessage("creation.new"), null, "https://creativeparkour.net/doc/map-creation.php", "%L", ChatColor.YELLOW);
				CreativeParkour.debug("MG4", "Finished building the new parkour map.");

				if (blocInterdit)
					p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.importsel error block"));
			}
		}
	}

	private static int getIdMapDispo(int taille)
	{
		int idDispo = 0;
		for (CPMap m : maps.values())
		{
			// Si toutes les maps sont prises ou qu'elle est supprimée et de la même taille que la taille des maps de la config
			if (m.getConf().get("state") == null || (m.getConf().getString("state").equalsIgnoreCase("deleted") && (m.getConf().getInt("location max.x") - m.getConf().getInt("location min.x") == taille)))
			{
				return m.getConf().getInt("id");
			}
			if (m.getConf().getInt("id") == idDispo)
				idDispo++;
		}
		return idDispo;
	}

	private static Block getBlocMapDispo(int taille)
	{
		World w = Config.getMonde();
		if (maps.size() == 0) // Si aucune map, on renvoie le bloc de départ
		{
			return w.getBlockAt(Config.getConfig().getInt("map storage.storage location x min"), Config.getConfig().getInt("map storage.storage location y min"), Config.getConfig().getInt("map storage.storage location z min"));
		}
		for (CPMap m : maps.values())
		{
			Block locMin = m.getMinLoc();
			int tailleMap = m.getSize();
			int ecartMaps = Config.getConfig().getInt("map storage.gap");
			int ecartReduit = (int) Math.round(ecartMaps / 1.5);

			// On regarde si l'emplacement suivant en X est libre
			Block locMinX = locMin.getRelative(tailleMap + ecartMaps, 0, 0);
			Block locMaxX = locMin.getRelative(tailleMap + ecartMaps + taille, taille, taille);
			boolean libre = true;
			if (estDansUneMap(locMinX.getRelative(0, 0, -ecartReduit)) || estDansUneMap(locMinX.getRelative(-ecartReduit, 0, 0))) // Vérification de l'espacement avec une éventuelle autre map
				libre = false;
			boolean verifier = true; // Pour ne tester qu'un bloc sur 2
			for (int x = locMinX.getX() - 1; x <= locMaxX.getX() + 1 && libre; x++)
			{
				for (int y = locMinX.getY() - 1; y <= locMaxX.getY() + 1 && libre; y++)
				{
					for (int z = locMinX.getZ() - 1; z <= locMaxX.getZ() + 1 && libre; z++)
					{
						if (verifier)
						{
							if (estDansUneMap(w.getBlockAt(x, y, z)))
								libre = false;
						}
						verifier = !verifier;
					}
				}
			}
			if (libre)
				return locMinX;

			// On regarde si l'emplacement suivant en Z est libre
			Block locMinZ = locMin.getRelative(0, 0, tailleMap + ecartMaps);
			Block locMaxZ = locMin.getRelative(taille, taille, tailleMap + ecartMaps + taille);
			libre = true;
			if (estDansUneMap(locMinX.getRelative(0, 0, -ecartReduit)) || estDansUneMap(locMinX.getRelative(-ecartReduit, 0, 0))) // Vérification de l'espacement avec une éventuelle autre map
				libre = false;
			for (int x = locMinZ.getX(); x <= locMaxZ.getX() && libre; x++)
			{
				for (int y = locMinZ.getY(); y <= locMaxZ.getY() && libre; y++)
				{
					for (int z = locMinZ.getZ(); z <= locMaxZ.getZ() && libre; z++)
					{
						if (estDansUneMap(w.getBlockAt(x, y, z)))
							libre = false;
					}
				}
			}
			if (libre)
				return locMinZ;
		}
		return null;
	}

	static CPMap getMap(UUID u)
	{
		if (u == null)
			return null;
		for (CPMap m : maps.values())
		{
			if (m.getUUID().equals(u))
			{
				return m;
			}
		}
		return null;
	}

	static CPMap getMap(String nomMap)
	{
		if (nomMap != null && !nomMap.isEmpty())
		{
			for (CPMap m : maps.values())
			{
				if (m != null && nomMap.equalsIgnoreCase(m.getName()))
				{
					return m;
				}
			}
		}
		return null;
	}

	static CPMap getMap(int id)
	{
		return maps.get(id);
	}

	/**
	 * Retourne la map dans laquelle est le bloc ou null si aucune ne correspond
	 */
	static CPMap getMap(Block b)
	{
		for (CPMap m : maps.values())
		{
			if (m.containsBlock(b))
			{
				return m;
			}
		}
		return null;
	}

	/**
	 * @param p Joueur
	 * @return La map en création du joueur ou null s'il n'en a pas
	 */
	static CPMap getMapEnCreation(Player p)
	{
		for (CPMap m : maps.values())
		{
			if (m.getCreator().equals(p.getUniqueId()) && m.getState() == CPMapState.CREATION)
			{
				return m;
			}
		}
		return null;
	}

	static boolean estDansUneMap(Block b)
	{
		return getMap(b) != null ? true : false;
	}

	static void getIdMap(Player p, String nomMap)
	{
		CPMap m = null;
		if (!nomMap.isEmpty())
		{
			m = getMap(nomMap);
		}
		if (m == null)
		{
			Joueur j = getJoueur(p);
			if (j != null && j.getMapObjet() != null)
				m = j.getMapObjet();
		}
		if (m != null)
		{
			String n = m.getName();
			if (n == null || n.isEmpty())
				n = "unnamed";
			p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.getid ok").replace("%map", ChatColor.ITALIC + n + ChatColor.RESET + ChatColor.YELLOW).replace("%id", ChatColor.GREEN + "" + m.getId() + ChatColor.RESET + ChatColor.YELLOW));
		}
		else
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.getid error"));
	}

	static int nbMaps()
	{
		int nb = 0;
		for (CPMap m : maps.values())
		{
			if (m.getState() == CPMapState.PUBLISHED)
				nb++;
		}
		return nb;
	}

	static int nbMapsPubliees(Player p)
	{
		int nb = 0;
		for (CPMap m : maps.values())
		{
			if (m.getState() == CPMapState.PUBLISHED && m.getCreator().equals(p.getUniqueId()))
				nb++;
		}
		return nb;
	}

	static void supprimerMap(UUID uuid, Player p)
	{
		if (uuid != null)
		{
			CPMap m = getMap(uuid);
			String n = m.getName();
			if (n == null || n.isEmpty())
				n = "unnamed";
			for (int i=0; i < joueurs.size(); i++)
			{
				if (uuid.equals(joueurs.get(i).getMap()))
				{
					joueurs.get(i).quitter(true, false);

					if (p == null || !joueurs.get(i).getPlayer().equals(p))
						joueurs.get(i).getPlayer().sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.delete deleted").replace("%map", n));
				}
			}
			if (p != null)
				p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.delete deleted").replace("%map", n));
			m.supprimer();

			synchroWeb();
		}
	}

	static void modifierMap(Joueur j, UUID id)
	{
		if (id != null)
		{
			CPMap m = getMap(id);
			// Ejection des joueurs qui jouent et qui ne sont pas celui qui modifie
			for (int i=0; i < joueurs.size(); i++)
			{
				if (!joueurs.get(i).equals(j) && id.equals(joueurs.get(i).getMap()))
				{
					joueurs.get(i).quitter(true, false);
				}
			}
			m.modifier(j);
		}
	}

	static void partagerMap(Joueur j, UUID map) throws NoSuchMethodException, SecurityException, IOException
	{
		if (Config.online())
		{
			if (map != null)
			{
				getMap(map).partager(j);
			}
		}
		else
		{
			j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("feature disabled"));
			if (j.getPlayer().isOp())
				j.getPlayer().sendMessage(ChatColor.YELLOW + Langues.getMessage("config.sharing.description"));
		}
	}

	static Joueur getJoueur(Player p)
	{ 
		if (p == null)
			return null;
		for (int i=0; i < joueurs.size(); i++)
		{
			if (joueurs.get(i).getPlayer().getUniqueId().equals(p.getUniqueId()))
			{
				return joueurs.get(i);
			}
		}
		return null;
	}

	static Joueur getJoueur(int entityID)
	{ 
		for (int i=0; i < joueurs.size(); i++)
		{
			if (joueurs.get(i).getPlayer().getEntityId() == entityID)
			{
				return joueurs.get(i);
			}
		}
		return null;
	}

	static List<Joueur> getJoueurs(UUID map)
	{
		List<Joueur> joueursMap = new ArrayList<Joueur>();
		for (int i=0; i < joueurs.size(); i++)
		{
			if (map.equals(joueurs.get(i).getMap()))
				joueursMap.add(joueurs.get(i));
		}
		return joueursMap;
	}

	static List<Joueur> getJoueurs()
	{
		return joueurs;
	}

	static void addJoueur(Joueur j)
	{
		if (!joueurs.contains(j)) joueurs.add(j);
	}

	/**
	 * @param p
	 * @param permission La permission qu'il doit avoir
	 * @return Le Joueur correspondant ou null si le mec n'est pas autorisé à jouer
	 */
	static Joueur sauvInventaire(Player p, String permission)
	{
		return getJoueurAutorise(p, permission, true);
	}

	/**
	 * @param p
	 * @param permission La permission qu'il doit avoir
	 * @param update S'il faut sauvegarder son inventaire ou pas (ne le fait pas forcément de toute façon)
	 * @return Le Joueur correspondant ou null si le mec n'est pas autorisé à jouer
	 */
	static Joueur getJoueurAutorise(Player p, String permission, boolean update)
	{
		if (!p.hasPermission("creativeparkour." + permission))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
			return null;
		}
		else if (Config.isBanned(p))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("ban"));
			return null;
		}
		else if (Config.getConfig().getBoolean("game.force empty inventory") && !inventaireVide(p.getInventory()))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("inventory error"));
			return null;
		}
		Joueur j = getJoueur(p);
		if (j == null)
		{
			j = new Joueur(p, update);
			addJoueur(j);
		}
		else if (j.getMap() == null && update)
			j.update(p);
		return j;
	}

	private static boolean inventaireVide(PlayerInventory i)
	{
		for(ItemStack item : i.getContents())
		{
			if (!CPUtils.itemStackIsEmpty(item))
				return false;
		}
		for(ItemStack item : i.getArmorContents())
		{
			if (!CPUtils.itemStackIsEmpty(item))
				return false;
		}
		return true;
	}

	static void supprJoueur(Player p, boolean tp)
	{
		Joueur j = getJoueur(p);
		if (j != null)
		{
			supprJoueur(j, tp);
		}
		else
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.leave error"));
		}
	}

	static void supprJoueur(Joueur j, boolean tp)
	{
		if (j != null && j.getEtat() != null && j.getMap() != null)
		{
			j.quitter(tp, false);
		}
		else
		{
			j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.leave error"));
		}
	}

	static void tester(Player p)
	{
		Joueur j = getJoueur(p);
		if (j != null && j.getMapObjet() != null && j.getMapObjet().getState() == CPMapState.CREATION)
		{
			if (j.getEtat() == EtatJoueur.CREATION)
				j.getMapObjet().test(p);
			else if (j.getEtat() == EtatJoueur.JEU)
				quitterTest(p);
		}
		else
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.test error"));
		}
	}

	static void quitterTest(Player p)
	{
		Joueur j = getJoueur(p);
		if (j != null && j.getMap() != null && j.getEtat() == EtatJoueur.JEU && getMap(j.getMap()).contientTesteur(p))
		{
			CPMap m = getMap(j.getMap());
			p.teleport(m.getSpawn().getLocation().add(0.5, 0, 0.5));
			j.modeCreation();
			m.supprimerTesteur(p);
			if (CreativeParkour.protocollibPresent() && CreativeParkour.auMoins1_9())
				PlayerVisibilityManager.majVisibiliteJoueurs(j);
		}
		else
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.test error"));
		}
	}

	static void publier(Player p, String n)
	{
		Joueur j = getJoueur(p);
		if (j != null && j.getMap() != null && getMap(j.getMap()).getState() == CPMapState.CREATION)
		{
			CPMap m = getMap(j.getMap());
			if (m.getCreator().equals(p.getUniqueId()))
			{
				if (m.isValid())
				{
					// Recherche de si le nom est déjà pris
					boolean erreur = false;
					for (CPMap map : maps.values())
					{
						if (!m.getUUID().equals(map.getUUID()) && n.equalsIgnoreCase(map.getName()))
							erreur = true;
					}
					if (!erreur)
						m.publier(p, n);
					else
						p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.publish error name 2"));
				}
				else
				{
					p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.publish error"));
				}
			}
			else
			{
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.publish error creator").replace("%creator", NameManager.getNomAvecUUID(m.getCreator())));
			}
		}
		else
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.test error"));
		}
	}

	/**
	 * Téléporte le joueur p au joueur p1 si celui-ci est en train de jouer
	 * @param p
	 * @param p1
	 */
	static void teleporter(final Player p, final Player p1)
	{
		Joueur j1 = getJoueur(p1);
		if (j1 == null || j1.getMap() == null) // Si le joueur p1 n'est pas dans une map
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.tp error"));
		}
		else
		{
			CPMap m = j1.getMapObjet();
			Joueur j = getJoueur(p);
			if (m.getState() != CPMapState.CREATION)
			{
				if (j == null || j.getMap() == null)
					j = sauvInventaire(p, "play");
				else
					j.quitter(false, true);
				if (j != null)
				{
					j.setMap(m.getUUID());
					m.jouer(j);
					// Délai pour que les autres trucs se fassent...
					final Joueur jf = j;
					Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
						public void run() {
							jf.modeSpectateur(true);
							p.teleport(p1);
						}
					}, 5);
				}
			}
			else if (p.hasPermission("creativeparkour.manage") || m.getCreator().equals(p.getUniqueId()) || m.getContributeurs().contains(p.getUniqueId()))
			{
				m.accepterInvitation(p);
			}
			else
			{
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.tp error creation"));
				return;
			}

			// Réactivation du mode fly au tick suivant pour que les autres plugins ferment leurs gueules
			new BukkitRunnable() {				
				public void run()
				{
					p.setAllowFlight(true);
					p.setFlying(true);
				}
			}.runTaskLater(CreativeParkour.getPlugin(), 1);
		}
	}

	static void voteDifficulte(Player p, int diff)
	{
		Joueur j = getJoueur(p);
		if (j != null && j.peutVoter())
		{
			CPMap m = getMap(j.getMap());
			if (m != null)
			{
				m.voteDifficulte(p, diff);
				m.calculerNotes();
				m.sauvegarder();
			}
		}
	}

	static void voteQualite(Player p, int qualite)
	{
		Joueur j = getJoueur(p);
		if (j != null && (p.hasPermission("creativeparkour.manage") || j.peutVoter()))
		{
			CPMap m = getMap(j.getMap());
			if (m != null)
			{
				m.voteQualite(p, qualite);
				m.calculerNotes();
				m.sauvegarder();
			}
		}
	}

	/**
	 * Fait entrer le joueur dans la map où il se trouve s'il a le droit ou le téléporte à un endroit par défaut
	 * @param p Le joueur en question
	 * @param forcerSortie Si true, il est forcément viré de la map sans la réintégrer
	 */
	static void reintegrerMapOuQuitter(final Player p, boolean forcerSortie)
	{
		CPMap m = getMap(p.getLocation().getBlock());
		if (m != null)
		{
			if (!forcerSortie)
			{
				if (m.isPlayable())
				{
					p.getInventory().clear(); // On supprime son inventaire pour pas qu'il se fasse virer si "force empty inventory" est activé
					// S'il était en mode spectateur, il y reste (dans 5 ticks pour que le reste se fasse avant, mais appel maintenant pour vérifier le mode avant qu'il ne change)
					if (p.getGameMode() == GameMode.SPECTATOR)
					{
						Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
							public void run() {
								Joueur j = getJoueur(p);
								if (j != null && j.getEtat() == EtatJoueur.JEU)
									j.modeSpectateur(true);
							}
						}, 5);
					}
					jouer(p, m, true, false);
				}
				else if (m.getState() == CPMapState.CREATION && (m.getCreator().equals(p.getUniqueId()) || m.getContributeurs().contains(p.getUniqueId())))
				{
					p.getInventory().clear(); // On supprime son inventaire pour pas qu'il se fasse virer si "force empty inventory" est activé
					teleporterCreation(p, m, true, false);
				}
			}

			// S'il n'a pas pu entrer dans la map, on le téléporte à un endroit par défaut
			Joueur j = getJoueur(p);
			if (j == null || j.getMap() == null)
			{
				// On téléporte là où le joueur était seulement si ce n'est pas désactivé et qu'on connait cet emplacement
				Location locTP = Config.getExitLocation();
				if (!Config.getConfig().getBoolean("game.always teleport to exit location"))
				{
					Object o = Config.getConfJoueur(p.getUniqueId().toString()).get("data.location");
					if (o instanceof Location)
						locTP = (Location) o;
				}
				p.getInventory().clear();
				p.teleport(locTP);
				CreativeParkour.debug("INV7", p.getName() + "'s inventory cleared.");
			}
		}
	}

	static boolean peutConstruire(Block block, Player player)
	{
		return peutConstruire(block, player, false);
	}

	static boolean peutConstruire(Block b, Player p, boolean autoriserBlocsInterdits)
	{
		Joueur j = getJoueur(p);
		CPMap m = null;
		if (j != null)
		{
			m = j.getMapObjet();
		}
		if (j != null && m != null && (j.getEtat() != EtatJoueur.CREATION))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
			return false;
		}
		if (j != null && m != null && (!m.containsBlock(b)))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
			return false;
		}
		if (m == null && b.getWorld().equals(Config.getMonde()) && estDansUneMap(b))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
			return false;
		}
		if (m != null && m.estEnTest())
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.test build"));
			return false;
		}
		if (!autoriserBlocsInterdits && m != null && blocsInterdits.contains(b.getType()) && !(p.isOp() && exceptionsOPs.contains(b.getType())))
		{
			if (b.getType().equals(Material.MONSTER_EGGS))
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.monster egg"));
			else
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.block not allowed"));
			return false;
		}
		if (m != null && b.getType().equals(Material.AIR) && !Config.getConfig().getBoolean("map creation.allow fluids") && (CPUtils.itemInHand(p).getType().equals(Material.WATER_BUCKET) || CPUtils.itemInHand(p).getType().equals(Material.LAVA_BUCKET)))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.block not allowed"));
			return false;
		}
		if (m == null && (b.getType().equals(Material.BEDROCK) || b.getType().equals(Material.BARRIER)) && (estDansUneMap(b.getRelative(BlockFace.UP)) || estDansUneMap(b.getRelative(BlockFace.DOWN)) || estDansUneMap(b.getRelative(BlockFace.NORTH)) || estDansUneMap(b.getRelative(BlockFace.SOUTH)) || estDansUneMap(b.getRelative(BlockFace.EAST)) || estDansUneMap(b.getRelative(BlockFace.WEST)))) // Si c'est la bedrock ou les barrières invisibles autour d'une map
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
			return false;
		}

		if (m != null && m.getState() == CPMapState.CREATION && m.isValid())
		{
			m.setValide(false);
			p.setScoreboard(m.getScoreboardC());
		}
		return true;
	}

	static void repondreInvitation(Player p, boolean reponse)
	{
		if (!reponse)
		{
			p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("creation.invitation denied"));
			for (CPMap m : maps.values())
			{
				if (m.getInvites().contains(p))
				{
					m.refuserInvitation(p);
					break;
				}
			}
		}
		else
		{
			for (CPMap m : maps.values())
			{
				if (m.getInvites().contains(p))
				{
					m.accepterInvitation(p);
					break;
				}
			}
		}
	}

	static void manageMaps(Player p)
	{
		p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.managemaps info"));
		for (CPMap m : maps.values())
		{
			if (m.getState() != CPMapState.DELETED)
			{
				ChatColor couleurNom = ChatColor.GOLD;
				String n = m.getName();
				if (n == null || n.isEmpty())
				{
					if (m.getState() == CPMapState.CREATION)
					{
						n = "creation";
						couleurNom = ChatColor.BLUE;
					}
					else
						n = "unnamed";
				}
				ComponentBuilder cb = new ComponentBuilder("ID: ").color(ChatColor.YELLOW)
						.append(String.valueOf(m.getId())).color(ChatColor.AQUA)
						.append("; ").color(ChatColor.YELLOW)
						.append(n).color(couleurNom)
						.append("; " + Langues.getMessage("commands.managemaps creator") + ": " + NameManager.getNomAvecUUID(m.getCreator())).color(ChatColor.YELLOW)
						.append(" ➔ ").color(ChatColor.YELLOW);
				if ((m.isPlayable() && !n.isEmpty()) || m.getState() == CPMapState.CREATION)
				{
					String commande = m.getState() == CPMapState.CREATION ? "/creativeparkour create " + m.getId() : "/creativeparkour play " + m.getName();
					cb.append("[" + Langues.getMessage("commands.managemaps play") + "] ").color(ChatColor.GREEN)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Langues.getMessage("commands.managemaps click")).italic(true).color(ChatColor.WHITE).create()))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commande));
				}


				cb.append("[" + Langues.getMessage("commands.managemaps delete") + "]").color(ChatColor.RED)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Langues.getMessage("commands.managemaps click")).italic(true).color(ChatColor.WHITE).create()))
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour delete " + m.getId()));
				p.spigot().sendMessage(cb.create());
			}
		}
	}

	static File getFichierTemps(String nomFichier)
	{
		return new File(dossier_temps, nomFichier + ".yml");
	}

	static File getFichierMap(int idMap)
	{
		return new File(dossier_maps, idMap + ".yml");
	}

	static YamlConfiguration getAncienneConfigMaps()
	{
		File fichier_maps = new File(CreativeParkour.getPlugin().getDataFolder(), "maps.yml");
		return YamlConfiguration.loadConfiguration(fichier_maps); 
	}

	static void importerSelection(Player p, boolean forcerEcrasement)
	{
		if (CreativeParkour.getWorldEdit() == null)
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.wand.no worldedit"));
		else
		{
			CubeDeBlocs blocs = WorldEditSelections.getSelectionCubique(p);
			if (blocs == null)
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.importsel error"));
			else
			{
				if (getMapEnCreation(p) != null && !forcerEcrasement)
				{
					Commandes.question(p, Langues.getMessage("creation.erase question import"), "écraser map import");
				}
				else
				{
					nouvelleMap(p, true, blocs);
				}
			}
		}
	}

	/**
	 * Adds a ghost to player's selection.
	 * @param p The player.
	 * @param ghostID Ghost's id, found on <a href="https://creativeparkour.net">creativeparkour.net</a>.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	static void watchGhost(Player p, String ghostID) throws NoSuchMethodException, SecurityException
	{
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("ghostID", ghostID);
		CPRequest.effectuerRequete("ghost-info.php", params, null, GameManager.class.getMethod("reponseWatchGhost", JsonObject.class, String.class, Player.class), p);
		p.sendMessage(CPRequest.messageAttente());
	}

	/**
	 * <em>Third-party plugins cannot use this method through CreativeParkour's API (it will throw an {@code InvalidQueryResponseException}).</em><br>
	 * Method called when <a href="https://creativeparkour.net" target="_blank">creativeparkour.net</a> responds to a query.
	 * @param json
	 * @param rep
	 * @param p
	 * @throws InvalidQueryResponseException If the {@code Request} has not been registered before.
	 */
	public static void reponseWatchGhost(JsonObject json, String rep, Player p) throws InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponseWatchGhost") && !CreativeParkour.erreurRequete(json, p))
		{
			JsonObject data = json.get("data").getAsJsonObject();
			if (!data.get("trouve").getAsBoolean())
			{
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.ghost watch error id"));
			}
			else
			{
				CPMap m = getMap(UUID.fromString(data.get("uuidMap").getAsString()));
				if (m == null || !m.isPlayable())
				{
					int idMap = data.get("idMap").getAsInt();
					if (idMap > 0) // Si la map est téléchargeable
						p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.ghost watch error map download").replace("%id", String.valueOf(idMap)));
					else
						p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.ghost watch error map nondownloadable"));
				}
				else
				{
					Joueur j = getJoueur(p);
					if (j == null || !m.equals(j.getMapObjet())) // S'il n'est pas dans celle map, on l'y envoie
					{
						jouer(p, m, false, true);
						j = getJoueur(p);
					}
					if (j != null)
					{
						CPTime t = m.getTime(UUID.fromString(data.get("uuidJoueur").getAsString()));
						if (t == null || t.ticks != data.get("ticks").getAsInt())
							p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.ghost watch error id"));
						else
						{
							j.tempsFantomesChoisis.add(t);
							p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.ghost watch ok").replace("%player", t.getPlayerName()));
						}
					}
				}
			}
		}
	}
}
