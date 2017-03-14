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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;

enum EtatJoueur { CREATION, JEU, SPECTATEUR, DECONNECTE };

class Joueur
{
	private Player player;
	private UUID uuid;
	private UUID mapUUID;
	private CPMap mapObjet; // A n'utiliser que pour des raisons de performance
	private EtatJoueur etat;
	private boolean estArrive;
	private BukkitTask task;
	private Objective objective;
	private Objective objectiveTicks;
	private Score score;
	private CPTime temps;
	private Date dateDebut;
	private Date dateFin;
	protected Scoreboard scoreboard;
	private Team equipeJoueurs;
	private boolean scoreboardPrecise;
	private boolean scoreboardPreciseAvantDepart;
	private List<BlocCheckpoint> checkpoints = new ArrayList<BlocCheckpoint>();
	SortedSet<CPTime> tempsFantomesChoisis = new TreeSet<CPTime>();
	private List<Fantome> fantomesVus = new ArrayList<Fantome>();
	private boolean elytres;
	private boolean perles;
	private boolean fusees;
	private Map<PlayerSetting, Object> options = new HashMap<PlayerSetting, Object>(); // Object : boolean ou String (paramètres spéciaux)
	private boolean infoDesactiverNotifications;
	private boolean infoDesactiverMessages;
	private boolean infoDesactiverMessagesSpec;
	private Date derniereInversion;
	private Date dernierJeu;
	private Date dernierTelechargement;
	Date dernierMsgSneak;
	private Date dernierMsgCheckpoints;
	InventaireSelection invSelection = null;
	InventaireCreation invCreation = null;
	InventaireAutresMaps invAutresMaps = null;
	InventaireFantomes invFantomes = null;
	InventaireOptionsMap invOptionsMaps = null;
	InventaireParametres invParametres = null;
	private JoueurWorldEdit joueurWE = null;
	private boolean avertissementWE;
	private boolean infoCheckpointsAdjacents;
	boolean enSaut = false;
	boolean damage = false;
	boolean infoMortLave = false;
	boolean infoMortEau = false;
	boolean infoClaim = false;
	int ghostIncrementation = 1;
	boolean downloadingGhosts = false; // If the player is downloading ghosts

	/**
	 * @param p Player
	 */
	Joueur(Player p, boolean update)
	{
		player = p;
		uuid = p.getUniqueId();
		if (update)
			update(p);
		derniereInversion = new Date(new Date().getTime() - 3000);

		// Chargement des paramètres
		YamlConfiguration conf = getConf();
		for (PlayerSetting ps : PlayerSetting.values())
		{
			if (conf.contains(ps.path())) // Si ça existe, on le charge
				options.put(ps, conf.get(ps.path()));
			else // Sinon, valeur par défaut
				setParam(ps, ps.def());
		}

		infoDesactiverMessages = true;
		infoDesactiverMessagesSpec = true;
	}

	void update(Player player)
	{
		this.player = player;
		YamlConfiguration conf = getConf();
		conf.set("data.location", player.getLocation());
		if (Config.getConfig().getBoolean("game.save inventory"))
		{
			conf.set("data.inventory.contents", player.getInventory().getContents());
			conf.set("data.inventory.armor", player.getInventory().getArmorContents());
		}
		conf.set("data.food", player.getFoodLevel());
		conf.set("data.saturation", player.getSaturation());
		conf.set("data.health", player.getHealth());
		conf.set("data.game mode", player.getGameMode().name());
		conf.set("data.allow flight", player.getAllowFlight());
		conf.set("data.flying", player.isFlying());
		conf.set("data.fall distance", player.getFallDistance());
		Config.saveConfJoueur(uuid.toString());
		dernierJeu = new Date();
		CreativeParkour.debug("INV6", player.getName() + "'s inventory saved.");
	}

	void modeCreation()
	{
		etat = EtatJoueur.CREATION;

		player.setScoreboard(GameManager.getMap(mapUUID).getScoreboardC());
		BlocEffet.supprimerEffets(player);
		if (task != null)
		{
			task.cancel();
			task = null;
		}

		final List<ItemStack> items = new ArrayList<ItemStack>();
		ItemStack livre = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bm = (BookMeta) livre.getItemMeta();
		bm.setAuthor("Obelus");
		bm.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + Langues.getMessage("help.title"));
		List<String> pages = new ArrayList<String>();
		boolean continuer = true;
		for (int i=1; continuer; i++)
		{
			String p = Langues.getMessage("creation.help book.p" + i);
			if (p != null)
			{
				pages.add(p);
			}
			else
			{
				continuer = false;
			}
		}
		bm.setPages(pages);
		livre.setItemMeta(bm);

		items.add(livre);
		boolean we = CreativeParkour.getWorldEdit() != null && player.hasPermission("creativeparkour.worldedit");
		if (we) items.add(new ItemStack(Config.getWorldEditItem()));
		items.add(new ItemStack(Material.SIGN));

		if (we)
		{
			ItemMeta im = items.get(1).getItemMeta();
			im.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + Langues.getMessage("creation.items.worldedit wand"));
			im.addEnchant(Enchantment.DIG_SPEED, 10, true);
			items.get(1).setItemMeta(im);
		}

		// Petit délai avant de filer les objets et de mettre en créatif pour éviter que d'autres plugins ne fassent chier
		Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
			public void run() {
				if (etat == EtatJoueur.CREATION) // On vérifie quand même qu'il ne se soit pas fait sortir durant les 4 ticks
				{
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					CreativeParkour.debug("INV1", player.getName() + "'s inventory cleared.");
					player.setGameMode(GameMode.CREATIVE);
					player.setAllowFlight(true);
					player.setFlying(true);
					for (int i=0; i < items.size(); i++)
					{
						player.getInventory().setItem(i, items.get(i));
					}
				}
			}
		}, 4);

		// WorldEdit (délai d'une seconde pour que le joueur soit dans le bon monde)
		final Joueur j = this;
		Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
			public void run() {
				JavaPlugin jp = CreativeParkour.getWorldEdit();
				if (jp != null && getMapObjet() != null && player.hasPermission("creativeparkour.worldedit"))
				{
					joueurWE = new JoueurWorldEdit(j, getMapObjet().getWorld());
					joueurWE.autoriser();
					avertissementWE = false;
				}
			}
		}, 20);
	}

	void modeJeu()
	{
		etat = EtatJoueur.JEU;

		final CPMap m = getMapObjet();

		player.closeInventory();
		CreativeParkour.debug("INV4", player.getName() + "'s inventory cleared.");
		player.removePotionEffect(PotionEffectType.POISON);
		player.removePotionEffect(PotionEffectType.HUNGER);
		BlocEffet.supprimerEffets(player);
		player.setFoodLevel(20);

		task = null;
		score = null;
		temps = null;
		deleteGhosts();
		dateDebut = null;
		dateFin = null;
		estArrive = false;
		checkpoints.clear();
		elytres = false;
		perles = false;
		fusees = false;
		infoCheckpointsAdjacents = false;
		infoMortLave = false;
		infoMortEau = false;
		ghostIncrementation = 1;
		downloadingGhosts = false;

		supprJoueurWE();

		// Remplissage des fantômes automatiquement sélectionnés


		// Items
		// Petit délai avant de faire des trucs pour éviter que les autres plugins fassent chier
		Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
			public void run() {
				player.setFlying(false);
				player.setAllowFlight(false);
				player.setGameMode(GameMode.ADVENTURE);

				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				player.getInventory().setHeldItemSlot(0);

				PlayerInventory inv = player.getInventory();
				ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 10);
				ItemMeta im = item.getItemMeta();
				im.setDisplayName(ChatColor.GREEN + Langues.getMessage("play.items.return start") + ChatColor.GRAY + " (" + Langues.getMessage("play.items.right click") + ")");
				item.setItemMeta(im);
				inv.setItem(0, item);

				int slot = 8;

				item = new ItemStack(Material.INK_SACK, 1, (short) 1);
				im = item.getItemMeta();
				String msg = Langues.getMessage("commands.leave");
				if (m.contientTesteur(player)) //S'il ne teste pas la map
					msg = Langues.getMessage("play.items.leave test");
				im.setDisplayName(ChatColor.RED + CPUtils.ucfirst(msg) + ChatColor.GRAY + " (" + Langues.getMessage("play.items.right click") + ")");
				item.setItemMeta(im);
				inv.setItem(slot, item);
				slot--;

				item = new ItemStack(Material.INK_SACK, 1, (short) 11);
				im = item.getItemMeta();
				im.setDisplayName(ChatColor.YELLOW + Langues.getMessage("play.items.player visibility") + ChatColor.GRAY + " (" + Langues.getMessage("play.items.right click") + ")");
				item.setItemMeta(im);
				inv.setItem(slot, item);
				slot--;

				if (!m.contientTesteur(player)) //S'il ne teste pas la map
				{
					if (player.hasPermission("creativeparkour.spectate"))
					{
						item = new ItemStack(Material.FEATHER, 1);
						im = item.getItemMeta();
						im.setDisplayName(ChatColor.WHITE + Langues.getMessage("play.items.spectator") + ChatColor.GRAY + " (" + Langues.getMessage("play.items.right click") + ")");
						item.setItemMeta(im);
						inv.setItem(slot, item);
						slot--;
					}

					if (Config.fantomesPasInterdits() && player.hasPermission("creativeparkour.ghosts.see")) // Si les fantomes sont activés ou que le problème est que ProtocolLib n'est pas là
					{
						item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
						im = item.getItemMeta();
						im.setDisplayName(ChatColor.BLUE + Langues.getMessage("play.items.ghosts") + ChatColor.GRAY + " (" + Langues.getMessage("play.items.right click") + ")");
						item.setItemMeta(im);
						inv.setItem(slot, item);
						slot--;
					}


					if (m.getCreator().equals(uuid) || player.hasPermission("creativeparkour.manage")) // Si c'est le créateur ou qu'il a la permission, objet options
					{
						item = new ItemStack(Material.WORKBENCH, 1);
						im = item.getItemMeta();
						im.setDisplayName(ChatColor.AQUA + Langues.getMessage("play.items.map options") + ChatColor.GRAY + " (" + Langues.getMessage("play.items.right click") + ")");
						item.setItemMeta(im);
						inv.setItem(slot, item);
						slot--;
					}

					giveMontre();

					YamlConfiguration conf = getConf();
					if (m.getCreator().equals(uuid) && conf.getBoolean("sharing announcement") != true)
					{
						player.sendMessage(Config.prefix() + ChatColor.AQUA + Langues.getMessage("commands.share announcement"));
						conf.set("sharing announcement", true);
						saveConf();
					}

					choixFantomesPreferes();
					majTeteFantomes();
				}

			}
		}, 4);

		// Scoreboard
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		// Secondes
		objective = scoreboard.registerNewObjective("cp_time", "dummy");
		String titre;
		if (m.isPlayable()) titre = Langues.getMessage("play.leaderboard");
		else titre = Langues.getMessage("play.time");
		String s = ChatColor.GOLD + "" + ChatColor.BOLD + titre;
		if (s.length() > 32) { s = s.substring(0, 32); }
		objective.setDisplayName(s);
		afficherScoresSecondes();
		// Ticks
		objectiveTicks = scoreboard.registerNewObjective("cp_ticks", "dummy");
		titre = Langues.getMessage("play.leaderboard ticks");
		s = ChatColor.GOLD + "" + ChatColor.BOLD + titre;
		if (s.length() > 32) { s = s.substring(0, 32); }
		objectiveTicks.setDisplayName(s);

		player.setScoreboard(scoreboard);

		// Tri des scores
		calculerScoreboard();

		// Equipe
		equipeJoueurs = scoreboard.registerNewTeam(CPUtils.truncatedStr("cp_" + player.getName() + "_" + m.getId(), 16));
		try {
			equipeJoueurs.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
		} catch (NoClassDefFoundError e) {
			// Rien
		}
		equipeJoueurs.setCanSeeFriendlyInvisibles(true);
		equipeJoueurs.setAllowFriendlyFire(false);
		List<Joueur> joueursMap = GameManager.getJoueurs(mapUUID);
		for (Joueur j : joueursMap)
		{
			equipeJoueurs.addEntry(j.getPlayer().getName());
			j.ajouterJoueurEquipe(player.getName());
		}

		changerVisibiliteJoueurs(visibiliteJoueurs(), false);

		if (CreativeParkour.protocollibPresent() && CreativeParkour.auMoins1_9() && !PlayerVisibilityManager.isEnabled())
			PlayerVisibilityManager.enable();
	}

	void modeSpectateur(boolean activer)
	{
		if (activer) // Activation du mode spectateur
		{
			etat = EtatJoueur.SPECTATEUR;
			player.setGameMode(GameMode.SPECTATOR);
			retirerElytres();
			retirerPerles();
			changerVisibiliteJoueurs(VisibiliteJoueurs.VISIBLE, false);
			stopTimer();
			player.getInventory().setItem(1, null); // Suppression de l'item checkpoint
			player.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("play.spectator on"));
			player.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("play.spectator disable"));
			giveMontre();
			scoreboard.resetScores(CPUtils.playerScoreboardName(uuid.toString(), ChatColor.GREEN)); // Suppression du score actuel du scoreboard (c'est 0 de toute façon)
		}
		else if (etat == EtatJoueur.SPECTATEUR) // Désactivation du mode spectateur
		{
			etat = EtatJoueur.JEU;
			player.setGameMode(GameMode.ADVENTURE);
			// Téléportation au départ
			player.teleport(GameManager.getMap(mapUUID).getSpawn().getLocation().add(0.5, 0, 0.5));
			stopTimer();
			player.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("play.spectator off"));
		}
		supprJoueurWE();
	}

	Player getPlayer()
	{
		return player;
	}

	UUID getUUID()
	{
		return uuid;
	}

	UUID getMap()
	{
		return mapUUID;
	}

	CPMap getMapObjet()
	{
		if (mapUUID == null)
			return null;
		else if (mapUUID != null && (mapObjet == null || !mapObjet.getUUID().equals(mapUUID)))
			mapObjet = GameManager.getMap(mapUUID);

		return mapObjet;
	}

	void setMap(UUID m)
	{
		mapUUID = m;
		if (CreativeParkour.stats() != null)
			CreativeParkour.stats().playerJoinCP(uuid);
	}

	EtatJoueur getEtat()
	{
		return etat;
	}

	Objective getObjective()
	{
		return objective;
	}

	Score getScore()
	{
		return score;
	}

	CPTime getTemps()
	{
		return temps;
	}

	long getTempsReel()
	{
		return dateFin.getTime() - dateDebut.getTime();
	}

	void calculerScoreboard()
	{
		if (scoreboard != null && getMapObjet() != null)
		{
			// Suppression des scores existants (sauf celui en vert du joueur)
			Set<String> entries = scoreboard.getEntries();
			Iterator<String> it = entries.iterator();
			while (it.hasNext())
			{
				String entry = it.next();
				if (!entry.contains(ChatColor.GREEN.toString()))
					scoreboard.resetScores(entry);
				// Suppression des scores de joueurs regardés par le spectateur s'il ne le regarde effectivement pas
				if (entry.contains(ChatColor.BLUE.toString()) && !(player.getSpectatorTarget() != null && entry.contains(player.getSpectatorTarget().getName())))
					scoreboard.resetScores(entry);
			}

			SortedSet<CPTime> temps = getMapObjet().getListeTemps();
			int i = 0;
			Iterator<CPTime> it1 = temps.iterator();
			while (it1.hasNext() && i < 14)
			{
				CPTime t = it1.next();
				String nom = CPUtils.playerScoreboardName(t.playerUUID.toString());
				int facteur = 1;
				if (Config.getConfig().getBoolean("game.negative leaderboard"))
					facteur = -1;
				objective.getScore(nom).setScore(facteur * (int) t.inSeconds());
				objectiveTicks.getScore(nom).setScore(facteur * t.ticks);
				i++;
			}

			// Suppression des fantômes inconnus (ils ont peut-être été supprimés par un joueur)
			for (Iterator<CPTime> iterator = tempsFantomesChoisis.iterator(); iterator.hasNext();) {
				CPTime t = iterator.next();
				if (!temps.contains(t))
					iterator.remove();
			}
		}
	}

	void afficherScoresSecondes()
	{
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		scoreboardPrecise = false;
	}

	void afficherScoresTicks()
	{
		objectiveTicks.setDisplaySlot(DisplaySlot.SIDEBAR);
		scoreboardPrecise = true;
		if (!scoreboardPreciseAvantDepart) // Si c'est un réaffichage automatique, pas de message
			player.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("play.leaderboard ticks info"));
	}

	void inverserLeaderboards()
	{
		if (new Date().getTime() > derniereInversion.getTime() + 1000)
		{
			derniereInversion = new Date();
			if (scoreboardPrecise)
				afficherScoresSecondes();
			else
				afficherScoresTicks();
			scoreboardPreciseAvantDepart = scoreboardPrecise;
		}
		else
		{
			player.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.wait"));
		}
	}

	void ajouterJoueurEquipe(String nom)
	{
		if (equipeJoueurs != null)
			equipeJoueurs.addEntry(nom);
	}

	Date getDernierJeu()
	{
		return dernierJeu;
	}

	void quitter(boolean tp, boolean autreMap) // autreMap doit être mis à true si le joueur va directement dans une autre map
	{
		CPMap m = getMapObjet();
		if (m != null)
		{
			m.saveSpawn();
			if (m.contientTesteur(player))
				m.supprimerTesteur(player);
			mapUUID = null;
			mapObjet = null;
		}
		supprJoueurWE();
		if (task != null) task.cancel();
		deleteGhosts();
		if (equipeJoueurs != null)
			equipeJoueurs.unregister();
		equipeJoueurs = null;
		if (etat != EtatJoueur.DECONNECTE)
		{
			estArrive = false;
			task = null;
			score = null;
			temps = null;
			dateDebut = null;
			dateFin = null;
			checkpoints.clear();
			elytres = false;
			perles = false;
			fusees = false;
			BlocEffet.supprimerEffets(player);

			// Ejection de ceux qui sont sur lui en spectateur
			for (Player p : Bukkit.getOnlinePlayers())
			{
				if (player.equals(p.getSpectatorTarget()))
					p.setSpectatorTarget(null);
			}

			if (!autreMap)
			{
				player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				CreativeParkour.debug("INV10", player.getName() + "'s inventory cleared.");
				boolean restauration = false;
				if (tp)
				{
					// On téléporte là où le joueur était seulement si ce n'est pas désactivé et qu'on connait cet emplacement
					Location locTP = Config.getExitLocation();
					if (!Config.getConfig().getBoolean("game.always teleport to exit location"))
					{
						Object o = getConf().get("data.location");
						if (o instanceof Location);
						{
							Location l = (Location) o;
							if (l != null && !GameManager.estDansUneMap(l.getBlock()) && Bukkit.getWorld(l.getWorld().getName()) != null) // Si ce n'est pas dans une map et que le monde est chargé
								locTP = l;
						}
					}
					if (Config.getConfig().getBoolean("game.update players before teleporting"))
					{
						restaurerTrucs();
						restauration = true;
						// On met un délai d'un tick avant la téléportation
						if (CreativeParkour.getPlugin().isEnabled()) // Parce que le plugin est désactivé quand le serveur s'éteint et qu'on vire les joueurs
						{
							final Location locTPf = locTP;
							Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
								public void run() {
									player.teleport(locTPf);
								}
							}, 1);
						}
						else
							player.teleport(locTP);
					}
					else
						player.teleport(locTP);
				}
				if (!restauration)
				{
					if (CreativeParkour.getPlugin().isEnabled()) // Parce que le plugin est désactivé quand le serveur s'éteint et qu'on vire les joueurs
					{
						Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
							public void run() {
								restaurerTrucs();
							}
						}, 2);
					}
					else
						restaurerTrucs();
				}
				player.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("leave"));

				// Message d'encouragement au feedback
				final YamlConfiguration conf = getConf();
				if (player.hasPermission("creativeparkour.manage") && conf.getBoolean("feedback 1") != true && Config.getDateInstall().getTime() + 1000 * 60 * 60 * 24 * 4  < new Date().getTime()) // Si c'est à plus de 4 jours de l'installation
				{
					Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
						public void run() {
							CPUtils.sendClickableMsg(player, Langues.getMessage("feedback 1"), null, "https://creativeparkour.net/contact.php", "%L", ChatColor.AQUA);
							conf.set("feedback 1", true);
							saveConf();
						}
					}, 50);
				}
				else if (!Config.getLanguage().equals("enUS") && !Config.getLanguage().equals("frFR") && new Random().nextInt(conf.getInt("languages info chance", 15)) == 0) // Une chance sur 15, ou plus si le message a déjà été affiché
				{
					Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
						public void run() {
							player.sendMessage(Config.prefix() + ChatColor.AQUA + Langues.getMessage("languages info"));
							conf.set("languages info chance", conf.getInt("languages info chance", 15) * 2);
							saveConf();
						}
					}, 50);
				}
			}
		}
		etat = null;
	}

	protected void restaurerTrucs()
	{
		YamlConfiguration conf = getConf();
		GameMode gm = null;
		try {
			gm = GameMode.valueOf(conf.getString("data.game mode"));
		} catch (Exception e) {
			// Rien
		}
		if (gm != null)
			player.setGameMode(gm);
		else
			player.setGameMode(Bukkit.getDefaultGameMode());
		if (Config.getConfig().getBoolean("game.save inventory") && !Config.getConfig().getStringList("game.inventory recovery world exclusions").contains(player.getWorld().getName()))
		{
			Object o = conf.get("data.inventory.contents");
			if (o instanceof ArrayList<?>)
			{
				ArrayList<?> liste = (ArrayList<?>) o;
				o = liste.toArray(new ItemStack[liste.size()]);
			}
			if (o instanceof ItemStack[])
			{
				player.getInventory().setContents((ItemStack[]) o);
				CreativeParkour.debug("INV8", player.getName() + "'s inventory replaced by previous inventory.");
			}
			o = conf.get("data.inventory.armor");
			if (o instanceof ArrayList<?>)
			{
				ArrayList<?> liste = (ArrayList<?>) o;
				o = liste.toArray(new ItemStack[liste.size()]);
			}
			if (o instanceof ItemStack[])
			{
				player.getInventory().setArmorContents((ItemStack[]) o);
			}
		}
		player.setFoodLevel(conf.getInt("data.food", 20));
		player.setSaturation(conf.getInt("data.saturation", 0));
		player.setHealth(conf.getInt("data.health", 20));
		player.setAllowFlight(conf.getBoolean("data.allow flight", false));
		player.setFlying(conf.getBoolean("data.flying", false));
		player.setFallDistance((float) conf.getDouble("data.fall distance", 0));

		conf.set("data", null);
		Config.saveConfJoueur(uuid.toString());

		CreativeParkour.debug("INV9", player.getName() + "'s previous inventory deleted.");
		player.saveData();
	}

	boolean peutQuitter()
	{
		if ((Config.getConfig().getBoolean("game.save inventory") && getConf().contains("data.inventory")) && getConf().contains("data.location"))
		{
			return true;
		}
		return false;
	}

	void setPlayer(Player p)
	{
		player = p;
	}

	void setEtat(EtatJoueur e)
	{
		etat = e;
	}

	BukkitTask getTask()
	{
		return task;
	}

	boolean estArrive()
	{
		return estArrive;
	}

	void depart()
	{
		CPMap m = getMapObjet();
		dateDebut = new Date();
		retirerMontre();
		afficherScoresSecondes();
		score = objective.getScore(CPUtils.playerScoreboardName(uuid.toString(), ChatColor.GREEN));
		score.setScore(0);
		temps = new CPTime(uuid, m, 0);
		dateFin = null;
		if (estArrive)
			m.addAttempt(uuid);
		estArrive = false;
		BlocEffet.supprimerEffets(player);
		task = new Timer(this).runTaskTimer(CreativeParkour.getPlugin(), 1, 1);
		startGhosts();
	}

	void arrivee()
	{
		CPMap m = getMapObjet();
		boolean ok = true;
		for (BlocCheckpoint b : m.getCkeckpoints())
		{
			if (!b.isOptional() && !checkpoints.contains(b))
			{
				ok = false;
			}
		}
		if (!ok)
		{
			if (dernierMsgCheckpoints == null || new Date().getTime() > dernierMsgCheckpoints.getTime() + 1000) // Pour éviter le spam
			{
				player.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.checkpoints error"));
				dernierMsgCheckpoints = new Date();
			}
		}
		else
		{
			dateFin = new Date();
			task.cancel();
			player.getInventory().setItem(1, null);
			checkpoints.clear();
			task = null;
			estArrive = true;
			BlocEffet.supprimerEffets(player);
			if (m.getState() == CPMapState.CREATION)
			{
				m.setValide(true);
				player.setScoreboard(GameManager.getMap(mapUUID).getScoreboardC());
				Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
					public void run() {
						if (etat == EtatJoueur.JEU)
							player.setScoreboard(scoreboard);
					}
				}, 20 * 4);
				player.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("creation.test completed"));
				player.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + Langues.getMessage("creation.test leave"));
			}
			else
			{
				player.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("play.finish").replace("%time", String.valueOf(temps.inSeconds())));

				// Mise à jour du score dans le scoreboard ticks
				String s = CPUtils.truncatedStr(ChatColor.GREEN + player.getName(), 16);

				int facteur = 1;
				if (Config.getConfig().getBoolean("game.negative leaderboard"))
					facteur = -1;
				objectiveTicks.getScore(s).setScore(facteur * temps.ticks);

				// On lui donne l'objet pour mettre la scoreboard en mode ticks
				giveMontre();

				YamlConfiguration conf = getConf();
				if (m.getState() == CPMapState.DOWNLOADED && Config.online() && player.hasPermission("creativeparkour.share") && Config.getConfig().getBoolean("game.sharing info in downloaded maps") && conf.getBoolean("sharing announcement 2") != true)
				{
					CPUtils.sendClickableMsg(player, Langues.getMessage("config.sharing.announcement 2"), null, "https://creativeparkour.net/doc/add-map.php", "%L", ChatColor.AQUA);
					conf.set("sharing announcement 2", true);
					Config.saveConfJoueur(uuid.toString());
				}

				if (Config.getConfig().getBoolean("game.enable map rating"))
				{
					boolean voterDifficulte = player.hasPermission("creativeparkour.rate.difficulty") && !m.aVoteDifficulte(uuid.toString());
					boolean voterQualite = player.hasPermission("creativeparkour.rate.quality") && !m.aVoteQualite(uuid.toString());

					if (voterDifficulte || voterQualite)
					{
						player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Langues.getMessage("play.difficulty question"));

						if (voterDifficulte)
						{
							player.spigot().sendMessage(new ComponentBuilder(Langues.getMessage("play.difficulty") + ": ").italic(true).color(ChatColor.YELLOW)
									.append("[" + Langues.getMessage("play.difficulty very easy") + "]").italic(false).color(ChatColor.WHITE).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour difficulty 1")).event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(Langues.getMessage("play.difficulty click")).color(ChatColor.YELLOW).create())).append(" ")
									.append("[" + Langues.getMessage("play.difficulty easy") + "]").color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour difficulty 2")).append(" ")
									.append("[" + Langues.getMessage("play.difficulty medium") + "]").color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour difficulty 3")).append(" ")
									.append("[" + Langues.getMessage("play.difficulty hard") + "]").color(ChatColor.RED).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour difficulty 4")).append(" ")
									.append("[" + Langues.getMessage("play.difficulty extreme") + "]").color(ChatColor.DARK_RED).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour difficulty 5")).create());
						}
						if (voterQualite)
						{
							player.spigot().sendMessage(new ComponentBuilder(Langues.getMessage("play.quality") + ": ").italic(true).color(ChatColor.YELLOW)
									.append(ChatColor.GOLD + "✮" + ChatColor.GRAY + "✮✮✮✮").italic(false).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour quality 1")).event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(Langues.getMessage("play.difficulty click")).color(ChatColor.YELLOW).create())).append(" ❘ ").color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null))
									.append(ChatColor.GOLD + "✮✮" + ChatColor.GRAY + "✮✮✮").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour quality 2")).append(" ❘ ").color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null))
									.append(ChatColor.GOLD + "✮✮✮" + ChatColor.GRAY + "✮✮").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour quality 3")).append(" ❘ ").color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null))
									.append(ChatColor.GOLD + "✮✮✮✮" + ChatColor.GRAY + "✮").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour quality 4")).append(" ❘ ").color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, null))
									.append(ChatColor.GOLD + "✮✮✮✮✮").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour quality 5")).create());
						}
					}
				}
			}
			m.updateTemps(this);
			infoDesactiverMessages();
			player.playSound(player.getLocation(), CPUtils.getSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP"), 1, 1);
			if (m.isPlayable())
			{
				choixFantomesPreferes();
				majTeteFantomes();
			}

			if (m.isPlayable()) // On ne donne pas les récompenses quand on teste la map
				RewardManager.mapTerminee(m, this);

			if (CreativeParkour.stats() != null)
				CreativeParkour.stats().parkoursReussis++;
		}
	}

	/**
	 * Donne la montre pour changer la leaderboard au joueur
	 */
	void giveMontre()
	{
		if (getMapObjet() != null && getMapObjet().isPlayable() && (CPUtils.itemStackIsEmpty(player.getInventory().getItem(1)) || player.getInventory().getItem(1).getType() == Material.WATCH))
		{
			PlayerInventory inv = player.getInventory();
			ItemStack item = new ItemStack(Material.WATCH);
			ItemMeta im = item.getItemMeta();
			im.setDisplayName(ChatColor.GOLD + Langues.getMessage("play.items.leaderboard") + ChatColor.GRAY + " (" + Langues.getMessage("play.items.right click") + ")");
			item.setItemMeta(im);
			inv.setItem(1, item);

			// On remet la scoreboard en ticks au joueur s'il l'avait avant de jouer
			if (scoreboardPreciseAvantDepart)
				afficherScoresTicks();
		}
	}

	boolean peutTelecharger()
	{
		if (!player.hasPermission("creativeparkour.download"))
		{
			player.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
			return false;
		}
		//		else if (mapUUID != null)
		//		{
		//			player.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error"));
		//			return false;
		//		}
		else if (dernierTelechargement != null && new Date().getTime() < dernierTelechargement.getTime() + 30000) // 30 secondes
		{
			player.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.download wait"));
			return false;
		}
		dernierTelechargement = new Date();
		return true;
	}

	void permettreTelechargement(int delai)
	{
		dernierTelechargement = new Date(new Date().getTime() - (1000 * 60 * 2 - delai));
	}

	private void retirerMontre()
	{
		if (player.getInventory().getItem(1) != null && player.getInventory().getItem(1).getType().equals(Material.WATCH))
			player.getInventory().setItem(1, null);
	}

	void retirerElytres()
	{
		player.getInventory().setArmorContents(null);
		elytres = false;
	}

	void retirerPerles()
	{
		player.getInventory().remove(Material.ENDER_PEARL);
		perles = false;
	}

	void retirerFusees()
	{
		player.getInventory().remove(Material.FIREWORK);
		fusees = false;
	}

	void donnerElytres()
	{
		ItemStack[] armorContents = { null, null, new ItemStack(Material.ELYTRA), null };
		player.getInventory().setArmorContents(armorContents);
		elytres = true;
	}

	void donnerPerles()
	{
		player.getInventory().setItem(2, new ItemStack(Material.ENDER_PEARL, 16));
		perles = true;
	}

	void donnerFusees()
	{
		player.getInventory().setItem(3, new ItemStack(Material.FIREWORK, 64));
		fusees = true;
	}

	boolean aElytres()
	{
		return elytres;
	}

	boolean aPerles()
	{
		return perles;
	}

	boolean aFusees()
	{
		return fusees;
	}

	void addCheckpoint(BlocCheckpoint b)
	{
		checkpoints.add(b);
		temps.ajouterCheckpoint(b.getBloc());
		// Affichage (seulement si le checkpoint d'avant n'est pas adjacent)
		if (checkpoints.size() < 2 || checkpoints.get(checkpoints.size() - 2).getLocation().distance(b.getLocation()) > 1)
		{
			String reduit = "";
			if (typeMessages() == TypeMessage.REDUCED)
				reduit = " reduced";
			CPTime meilleurTemps = getMapObjet().getRecord();
			CPTime recordPerso = getMapObjet().getTime(uuid);
			String diff = null;
			String msg = null;
			if (meilleurTemps != null && meilleurTemps.checkpoints.containsKey(b.getBloc()))
			{
				int ecart = temps.ticks - meilleurTemps.checkpoints.get(b.getBloc());
				diff = String.valueOf(ecart / 20.0);
				if (ecart <= 0)
					diff = ChatColor.GREEN + diff;
				else
					diff = ChatColor.RED + "+" + diff;

				if (recordPerso == null)
				{
					msg = ChatColor.YELLOW + Langues.getMessage("play.checkpoint validated diff" + reduit).replace("%player", meilleurTemps.getPlayerName()).replace("%diff", diff + ChatColor.YELLOW);
					if (typeMessages() != TypeMessage.NONE)
						player.sendMessage(Config.prefix() + msg);
				}
			}
			if (recordPerso != null && recordPerso.checkpoints.containsKey(b.getBloc()))
			{
				int ecart2 = temps.ticks - recordPerso.checkpoints.get(b.getBloc());
				String diff2 = String.valueOf(ecart2 / 20.0);
				if (ecart2 <= 0)
					diff2 = ChatColor.GREEN + diff2;
				else
					diff2 = ChatColor.RED + "+" + diff2;
				if (meilleurTemps.equals(recordPerso) || diff == null)
					msg = ChatColor.YELLOW + Langues.getMessage("play.checkpoint validated diff personal" + reduit).replace("%player", meilleurTemps.getPlayerName()).replace("%diff", diff2 + ChatColor.YELLOW);
				else
					msg = ChatColor.YELLOW + Langues.getMessage("play.checkpoint validated diff 2" + reduit).replace("%player", meilleurTemps.getPlayerName()).replace("%diff2", diff2 + ChatColor.YELLOW).replace("%diff", diff + ChatColor.YELLOW);
				if (typeMessages() != TypeMessage.NONE)
					player.sendMessage(Config.prefix() + msg);
			}
			if (msg == null)
			{
				if (typeMessages() == TypeMessage.FULL)
					player.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("play.checkpoint validated"));
			}
			else
			{
				// Affichage du message aux spectateurs si l'option est activée
				for (Joueur j : GameManager.getJoueurs(mapUUID))
				{
					if (j.getPlayer().getSpectatorTarget() != null && j.getPlayer().getSpectatorTarget().equals(player) && j.getParamBool(PlayerSetting.MESSAGES_CP_SPEC))
					{
						j.getPlayer().sendMessage(Config.prefix() + ChatColor.GRAY + Langues.getMessage("commands.messages spec to player").replace("%player", player.getName()) + ": " + msg.replace(ChatColor.YELLOW.toString(), ChatColor.GRAY.toString() + ChatColor.ITALIC));
						j.infoDesactiverMessagesSpec();
					}
				}
			}

			if (CPUtils.itemStackIsEmpty(player.getInventory().getItem(1)))
			{
				ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 5);
				ItemMeta im = item.getItemMeta();
				im.setDisplayName(ChatColor.DARK_PURPLE + Langues.getMessage("play.items.return checkpoint") + ChatColor.GRAY + " (" + Langues.getMessage("play.items.right click") + ")");
				item.setItemMeta(im);
				player.getInventory().setItem(1, item);
			}
			player.playSound(player.getLocation(), CPUtils.getSound("ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP"), 1, 1);
		}
		else if (getMapObjet().contientTesteur(player) && !infoCheckpointsAdjacents) // Si le mec teste, on lui dit que c'est normal que ça ne s'affiche pas
		{
			infoCheckpointsAdjacents = true;
			CPUtils.sendInfoMessage(player, Langues.getMessage("creation.test adjacent checkpoints"));
		}
	}

	List<BlocCheckpoint> getCheckpoints()
	{
		return checkpoints;
	}

	void timerMaj(int counter, Timer t)
	{
		if (counter % 20 == 0) // Si c'est une seconde pile
		{
			if (getMapObjet() == null || etat != EtatJoueur.JEU || Config.isBanned(player))
			{
				t.cancel();
			}
			else
			{
				int facteur = 1;
				if (Config.getConfig().getBoolean("game.negative leaderboard"))
					facteur = -1;
				try {
					score.setScore(facteur * (counter / 20));
				} catch (Exception e) {
					if (task != null)
						task.cancel();
				}

				// Affichage du temps dans les scoreboard des spectateurs et suppression pour ceux qui ne regardent plus
				List<Joueur> joueursMap = GameManager.getJoueurs(mapUUID);
				String s = CPUtils.playerScoreboardName(uuid.toString(), ChatColor.BLUE);
				for (Joueur j : joueursMap)
				{
					if (mapUUID.equals(j.getMap()))
					{
						if (j.getPlayer().getSpectatorTarget() != null && j.getPlayer().getSpectatorTarget().equals(player))
						{
							j.afficherScoresSecondes();
							j.objective.getScore(s).setScore(score.getScore());
						}
						else if (j.scoreboard != null)
							j.scoreboard.resetScores(s);
					}
				}

				if (CreativeParkour.stats() != null)
					CreativeParkour.stats().secondesJouees++;
			}
		}
		// Mise à jour du temps
		if (temps == null)
			stopTimer();
		else
			temps.ticks = counter;

		// Enregistrement de la position pour le fantôme
		if (counter % 2 == 0 && temps != null && !temps.fantomeAnnule && player.hasPermission("creativeparkour.ghosts.save") && Config.fantomesPasInterdits()) // Tous les 2 ticks
		{
			if (player.isFlying()) // Si le joueur vole, on annule son fantôme et ses checkpoints
			{
				temps.annulerFantomeCheckpoints();
			}
			else
			{
				boolean elytres = false;
				try {
					elytres = player.isGliding();
				} catch (NoSuchMethodError e) {
					// Rien
				}
				temps.ajouterPosition(new CPPlayerPosition(player.getLocation(), player.isSneaking(), elytres, CPUtils.itemInHand(player).getType().equals(Material.ENDER_PEARL), getMapObjet().getMinLoc()));
			}
		}
	}

	void stopTimer()
	{
		if (task != null)
		{
			task.cancel();
			if (score != null) score.setScore(0);
			score = null;
			dateDebut = null;
			dateFin = null;
			checkpoints.clear();
			BlocEffet.supprimerEffets(player);

			// Suppression du temps des scoreboard des spectateurs
			List<Joueur> joueursMap = GameManager.getJoueurs(mapUUID);
			String s = CPUtils.playerScoreboardName(uuid.toString(), ChatColor.BLUE);
			for (Joueur j : joueursMap)
			{
				if (mapUUID.equals(j.getMap()) && j.scoreboard != null)
				{
					j.scoreboard.resetScores(s);
				}
			}
		}
	}

	JoueurWorldEdit worldEdit()
	{
		return joueurWE;
	}

	private void supprJoueurWE()
	{
		if (joueurWE != null)
		{
			joueurWE.clear(true);
			joueurWE.desactiver();
			joueurWE = null;
		}
	}

	/**
	 * Retourne la valeur d'un paramètre du joueur
	 * @param ps Paramètre recherché
	 * @return Valeur du paramètre
	 */
	private Object getParam(PlayerSetting ps)
	{
		return options.get(ps);
	}

	/**
	 * Retourne la valeur d'un paramètre du joueur (booléen uniquement)
	 * @param ps Paramètre recherché
	 * @return Valeur du paramètre (booléen)
	 */
	boolean getParamBool(PlayerSetting ps)
	{
		return (boolean) getParam(ps);
	}

	/**
	 * Modifie la valeur d'un paramètre du joueur
	 * @param ps Paramètre à modifier
	 * @param val Valeur à mettre
	 */
	void setParam(PlayerSetting ps, Object val)
	{
		if (val instanceof Enum)
			val = ((Enum<?>) val).name();
		options.put(ps, val);
		saveConf();
	}

	VisibiliteJoueurs visibiliteJoueurs()
	{
		try {
			return VisibiliteJoueurs.valueOf((String) getParam(PlayerSetting.VISIBILITE_JOUEURS));
		} catch (Exception e) {
			return (VisibiliteJoueurs) PlayerSetting.VISIBILITE_JOUEURS.def();
		}
	}

	TypeMessage typeMessages()
	{
		try {
			return TypeMessage.valueOf((String) getParam(PlayerSetting.MESSAGES_CP));
		} catch (Exception e) {
			return (TypeMessage) PlayerSetting.MESSAGES_CP.def();
		}
	}

	/**
	 * Inverse un paramètre du joueur de type booléen
	 * @param ps Paramètre à modifier
	 */
	void inverserParam(PlayerSetting ps)
	{
		boolean val = !((boolean) options.get(ps));
		setParam(ps, val);
	}

	void changerVisibiliteJoueurs(boolean msg)
	{
		VisibiliteJoueurs mode = VisibiliteJoueurs.VISIBLE;
		if (visibiliteJoueurs() == VisibiliteJoueurs.VISIBLE)
			mode = VisibiliteJoueurs.TRANSPARENT;
		else if (visibiliteJoueurs() == VisibiliteJoueurs.TRANSPARENT)
			mode = VisibiliteJoueurs.INVISIBLE;
		changerVisibiliteJoueurs(mode, msg);
	}

	void changerVisibiliteJoueurs(VisibiliteJoueurs mode, boolean msg)
	{
		setParam(PlayerSetting.VISIBILITE_JOUEURS, mode);
		if (CreativeParkour.auMoins1_9())
		{
			if (CreativeParkour.protocollibPresent())
			{
				equipeJoueurs.setCanSeeFriendlyInvisibles(mode != VisibiliteJoueurs.INVISIBLE);
				// Envoi de packets pour mettre à jour
				PlayerVisibilityManager.majVisibiliteJoueurs(this);


				if (msg) // S'il faut envoyer un message
				{
					if (visibiliteJoueurs() == VisibiliteJoueurs.VISIBLE)
						player.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("play.players visible"));
					else if (visibiliteJoueurs() == VisibiliteJoueurs.TRANSPARENT)
						player.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("play.players transparent"));
					else if (visibiliteJoueurs() == VisibiliteJoueurs.INVISIBLE)
						player.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("play.players invisible"));
					player.playSound(player.getLocation(), CPUtils.getSound("ENTITY_ITEM_PICKUP", "ITEM_PICKUP"), 1, 1);
				}
			}
			else if (msg)
			{
				player.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.error protocollib"));
				if (player.isOp())
					player.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "https://www.spigotmc.org/resources/protocollib.1997");
			}

		}
		else if (msg)
		{
			player.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("outdated server").replace("%ver", "1.9"));
		}
	}

	void inverserNotifications()
	{
		inverserParam(PlayerSetting.NOTIFICATIONS);
		if (getParamBool(PlayerSetting.NOTIFICATIONS))
			player.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.notifications on"));
		else
			player.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.notifications off"));
	}

	void infoDesactiverNotification()
	{
		if (infoDesactiverNotifications && getParamBool(PlayerSetting.NOTIFICATIONS))
		{
			player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + Langues.getMessage("commands.notifications info"));
			infoDesactiverNotifications = false;
		}
	}

	/**
	 * Demande au joueur quel type de messages il veut utiliser
	 */
	void configurerMessages()
	{
		ComponentBuilder cb = new ComponentBuilder(Config.prefix() + ChatColor.GOLD + ChatColor.BOLD + Langues.getMessage("commands.messages choice"));
		cb.color(ChatColor.GOLD).bold(true).append("\n").reset();
		cb.append(" ➥ ").color(ChatColor.YELLOW).append(Langues.getMessage("play.checkpoint validated diff 2").replace("%diff2", ChatColor.RED + "+1.61" + ChatColor.RESET).replace("%diff", ChatColor.GREEN + "-3.14" + ChatColor.RESET).replace("%player", "PlayerName")).color(ChatColor.WHITE)
		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour messages FULL"));
		cb.append("\n").reset();
		cb.append(" ➥ ").color(ChatColor.YELLOW).append(Langues.getMessage("play.checkpoint validated diff 2 reduced").replace("%diff2", ChatColor.RED + "+1.61" + ChatColor.RESET).replace("%diff", ChatColor.GREEN + "-3.14" + ChatColor.RESET).replace("%player", "PlayerName")).color(ChatColor.WHITE)
		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour messages REDUCED"));
		cb.append("\n").reset();
		cb.append(" ➥ ").color(ChatColor.YELLOW).append(Langues.getMessage("commands.messages disable")).color(ChatColor.WHITE).italic(true)
		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour messages NONE"));
		player.spigot().sendMessage(cb.create());
	}

	void infoDesactiverMessages()
	{
		if (infoDesactiverMessages && typeMessages() == TypeMessage.FULL)
		{
			CPUtils.sendInfoMessage(player, Langues.getMessage("commands.settings info"));
			infoDesactiverMessages = false;
		}
	}

	void inverserMessagesSpec()
	{
		boolean messagesSpecActives = !getParamBool(PlayerSetting.MESSAGES_CP_SPEC);
		setParam(PlayerSetting.MESSAGES_CP_SPEC, messagesSpecActives);
		if (messagesSpecActives)
			player.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.messages spec on"));
		else
			player.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.messages spec off"));
		saveConf();
	}

	void infoDesactiverMessagesSpec()
	{
		if (infoDesactiverMessagesSpec && getParamBool(PlayerSetting.MESSAGES_CP_SPEC))
		{
			CPUtils.sendInfoMessage(player, Langues.getMessage("commands.settings info"));
			infoDesactiverMessagesSpec = false;
		}
	}

	void avertissementWorldEdit(final String message)
	{
		if (!avertissementWE)
		{
			avertissementWE = true;
			Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
				public void run() {
					player.sendMessage(Config.prefix() + ChatColor.RED + message);
					avertissementWE = false;
				}
			}, 10);
		}
	}

	YamlConfiguration getConf()
	{
		YamlConfiguration conf = Config.getConfJoueur(uuid.toString());
		if (conf == null || !conf.isSet("name")) // Pour éviter que ça renvoie null
		{
			conf.set("name", player.getName());
			Config.ajouterConfJoueur(uuid.toString(), conf);
		}
		return conf;
	}

	void saveConf()
	{
		YamlConfiguration conf = Config.getConfJoueur(uuid.toString());
		conf.set("name", player.getName());
		for (Entry<PlayerSetting, Object> o : options.entrySet())
		{
			conf.set(o.getKey().path(), o.getValue());
		}
		Config.saveConfJoueur(uuid.toString());
	}

	byte getValPacketMetadata()
	{
		byte val = 0;
		if (player.isSneaking())
			val += CPUtils.sneakVal;
		try {
			if (player.isGliding())
				val += CPUtils.elytraVal;
		} catch (NoSuchMethodError e) {
			// Rien
		}
		if (player.isSprinting())
			val += CPUtils.sprintVal;
		return val;
	}

	void tpAvecSpectateurs(Location loc)
	{
		Location loc1 = player.getLocation();
		player.teleport(loc);
		if (loc.distance(loc1) > 45) // Si c'est à plus de 45 blocs, les spectateurs vont planter
		{
			for (final Joueur j : GameManager.getJoueurs(mapUUID))
			{
				if (player.equals(j.getPlayer().getSpectatorTarget()))
				{
					j.getPlayer().setSpectatorTarget(null);
					j.getPlayer().teleport(player);
					Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
						public void run() {
							j.getPlayer().setSpectatorTarget(player);
						}
					}, 2);
				}
			}
		}
	}

	/**
	 * Lui marave la gueule et le fait revenir en arrière dans la map
	 */
	void tuer()
	{
		if (getMapObjet() != null && etat == EtatJoueur.JEU)
		{
			if (checkpoints.size() > 0)
			{
				tpAvecSpectateurs(checkpoints.get(checkpoints.size()-1).getLocation().add(0.5, 0, 0.5));
			}
			else
			{
				tpAvecSpectateurs(getMapObjet().getSpawn().getLocation().add(0.5, 0, 0.5));
				giveMontre();
				retirerElytres();
				retirerPerles();
				stopTimer();
			}
			if (player.getHealth() > 4)
			{
				damage = true;
				player.damage(player.getHealth() - 2);
			}
			new Regeneration(player).runTaskTimer(CreativeParkour.getPlugin(), 5, 1);
		}
	}

	void verifMortEauLave()
	{

	}

	void ouvrirParametres()
	{
		invParametres = new InventaireParametres(this);
		invParametres.remplir();
		player.openInventory(invParametres.getInventaire());
	}

	void afficherListeContributeurs()
	{
		CPMap m = getMapObjet();
		if (m != null)
		{
			if (m.getContributeurs().isEmpty() && m.getInvites().isEmpty())
			{
				player.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.contributors empty"));
			}
			else
			{
				player.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.contributors list"));
				for (UUID uuid : m.getContributeurs())
				{
					String nomJ = NameManager.getNomAvecUUID(uuid);
					ComponentBuilder cb = new ComponentBuilder(" • ").color(ChatColor.YELLOW)
							.append(nomJ).color(ChatColor.WHITE).append(" ")
							.append("[" + CPUtils.ucfirst(Langues.getMessage("commands.remove")) + "]").color(ChatColor.RED)
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Langues.getMessage("commands.contributors remove info").replace("%player", nomJ)).italic(true).color(ChatColor.WHITE).create()))
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour remove " + nomJ));
					player.spigot().sendMessage(cb.create());;
				}
				for (Player p : m.getInvites())
				{
					ComponentBuilder cb = new ComponentBuilder(" • ").color(ChatColor.YELLOW)
							.append(p.getName()).color(ChatColor.WHITE).append(" ")
							.append("(" +Langues.getMessage("commands.contributors pending") + ")").color(ChatColor.GRAY);
					player.spigot().sendMessage(cb.create());
				}
			}

			CPUtils.sendInfoMessage(player, Langues.getMessage("commands.contributors invite"));
		}
	}

	/**
	 * Returns {@code true} if the player finished the parkour and is can vote.
	 * @return {@code true} if the player can vote.
	 */
	boolean peutVoter()
	{
		CPMap m = getMapObjet();
		if (m != null)
		{
			if (estArrive)
				return true;
			// Recherche de s'il a un temps dans la map
			for (CPTime t : m.getListeTemps())
			{
				if (t.playerUUID.equals(uuid))
					return true;
			}
		}
		return false;
	}

	/**
	 * Opens the ghost selection inventory
	 */
	void openGhostSelection()
	{
		invFantomes = new InventaireFantomes(getMapObjet().getListeTemps(false), this);
		invFantomes.setPage(1);
		player.openInventory(invFantomes.getInventaire());
	}

	/**
	 * Creates and starts playing ghosts the player selected.
	 */
	void startGhosts()
	{
		if (CreativeParkour.protocollibPresent() && CreativeParkour.auMoins1_9() && Config.fantomesPasInterdits() && player.hasPermission("creativeparkour.ghosts.see") && getMapObjet() != null && getMapObjet().isPlayable())
		{
			if (tempsFantomesChoisis.size() > Config.getConfig().getInt("game.max ghosts"))
				player.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.ghosts.limit"));
			else
			{
				if (!downloadingGhosts)
					downloadGhosts();
				deleteGhosts();
				for (CPTime t : tempsFantomesChoisis)
				{
					t = getMapObjet().getTime(t.playerUUID); // On reprend le temps de la map (il est peut-être plus à jour)
					if (t != null && t.hasGhost())
					{
						Fantome f = new Fantome(t, this);
						f.demarrer();
						fantomesVus.add(f);
					}
				}
				if (!fantomesVus.isEmpty() && visibiliteJoueurs() == VisibiliteJoueurs.INVISIBLE)
					changerVisibiliteJoueurs(VisibiliteJoueurs.TRANSPARENT, true);
			}
		}
	}

	/**
	 * Deletes all the ghosts the player is watching.
	 */
	private void deleteGhosts()
	{
		for (Fantome f : fantomesVus)
		{
			f.arreter();
		}
		fantomesVus.clear();
	}

	/**
	 * Puts ghosts at the specified tick.
	 * @param tick Tick to put ghosts at.
	 */
	void setGhostsMoment(int tick)
	{
		for (Fantome f : fantomesVus)
		{
			f.setTick(tick);
		}
	}

	/**
	 * Puts ghosts the player is viewing back in time.
	 * @param ticks Number of ticks to rewind.
	 */
	void rewindGhosts(int ticks)
	{
		for (Fantome f : fantomesVus)
		{
			f.rewind(ticks);
		}
	}

	void majTeteFantomes()
	{
		if (mapUUID != null && getMapObjet().isPlayable() && player.getInventory().contains(Material.SKULL_ITEM))
		{
			for (ItemStack item : player.getInventory().getContents())
			{
				if (item != null && item.getType() == Material.SKULL_ITEM)
				{
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					if (tempsFantomesChoisis.size() > 0)
						meta.setOwner(tempsFantomesChoisis.first().getPlayerName());
					else
						meta.setOwner(null);
					item.setItemMeta(meta);
				}
			}
		}
	}

	void choixFantomesPreferes()
	{
		if (CreativeParkour.protocollibPresent() && CreativeParkour.auMoins1_9() && Config.fantomesPasInterdits() && player.hasPermission("creativeparkour.ghosts.see"))
		{
			if (getParamBool(PlayerSetting.CHOISIR_MEILLEUR_FANTOME))
			{
				// Recherche du premier temps qui a un fantôme
				for (CPTime t : getMapObjet().getListeTemps())
				{
					if (t.hasGhost())
					{
						tempsFantomesChoisis.add(t);
						break;
					}
				}
			}
			if (getParamBool(PlayerSetting.CHOISIR_FANTOME_PERSO))
			{
				for (CPTime t : getMapObjet().getListeTemps())
				{
					if (t.playerUUID.equals(uuid) && t.hasGhost())
					{
						tempsFantomesChoisis.add(t);
						break;
					}
				}
			}
			if (!tempsFantomesChoisis.isEmpty())
				downloadGhosts();
		}
	}

	/**
	 * Downloads potentially selected ghosts.
	 */
	void downloadGhosts()
	{
		downloadingGhosts = true;
		List<String> tempsATelecharger = new ArrayList<String>();
		int delaiProfils = 4;
		for (CPTime t : tempsFantomesChoisis)
		{
			if (t.etat == EtatTemps.TO_DOWNLOAD)
				tempsATelecharger.add(t.getFileName().replace(".yml", ""));
		}
		if (!tempsATelecharger.isEmpty())
		{
			GameManager.telechargerFantomes(tempsATelecharger);
			delaiProfils = 20;
		}

		Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
			public void run() {
				PlayerProfiles.chargerProfils(tempsFantomesChoisis, true); // ça ne fera pas de requête si on les a déjà
			}
		}, delaiProfils); // Petit délai pour ne pas avoir plein de requêtes en même temps
	}

	private class Regeneration extends BukkitRunnable {

		private Player p;

		Regeneration(Player p)
		{
			this.p = p;
		}

		public void run() {
			double h = p.getHealth() + 1;
			if (h >= 20)
			{
				p.setHealth(20);
				this.cancel();
			}
			else
				p.setHealth(h);
		}

	}
}
