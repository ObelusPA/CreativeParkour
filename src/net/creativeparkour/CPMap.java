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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Bed;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.creativeparkour.CPRequest.InvalidQueryResponseException;
import net.md_5.bungee.api.ChatColor;

/**
 * Represents a CreativeParkour map, with all its properties.<br>
 * Parkour maps are cubic.
 * @author Obelus
 */
public class CPMap
{
	private int id;
	private UUID uuid;
	private File fichier;
	private YamlConfiguration config;
	private CPMapState etat;
	private World monde;
	private Block locMin;
	private Block locMax;
	private UUID createur;
	private Set<UUID> contributeurs = new HashSet<UUID>();
	private List<Player> invites = new ArrayList<Player>();
	private float difficulte = -1;
	private float qualite = -1;
	private Map<String, Vote> votes = new HashMap<String, Vote>();
	private Map<UUID, AtomicInteger> attempts = new HashMap<UUID, AtomicInteger>();
	private String nom;
	private BlocSpawn spawn;
	private List<BlocSpecial> blocsSpeciaux = new ArrayList<BlocSpecial>();
	private List<BlocSpecial> blocsSpeciauxAir = new ArrayList<BlocSpecial>();
	private List<BlocSpecial> blocsSpeciauxPlaques = new ArrayList<BlocSpecial>();
	private int hauteurMort;
	private Block blocHautMort;
	private Scoreboard scoreboardCreation;
	private boolean valide;
	private boolean epingle = false;
	private List<Player> testeurs = new ArrayList<Player>();
	private SortedSet<CPTime> temps;
	private Map<String, Object> webData = null; // For maps that can be downloaded
	private RemplisseurBlocs remplisseur = null;
	boolean sneakAutorise = true;
	boolean mortLave = false;
	boolean mortEau = false;
	boolean interactionsAutorisees;
	private boolean forceNoPlates = false;


	CPMap (int id, String uuid, CPMapState etat, World monde, Block locMin, Block locMax, String nom, UUID createur, Set<UUID> contributeurs, boolean epingle, BlocSpawn spawn, List<BlocSpecial> blocsSpeciaux, int hauteurMort, boolean sneakAutorise, boolean mortLave, boolean mortEau, boolean interactionsAutorisees, List<String> listeVotes, float difficulte, float qualite, boolean forceNoPlates)
	{
		boolean saveConf = false;

		this.id = id;
		if (uuid == null || uuid.isEmpty())
		{
			this.uuid = UUID.randomUUID();
			saveConf = true;
		}
		else
			this.uuid = UUID.fromString(uuid);
		this.fichier = GameManager.getFichierMap(id);
		this.config = YamlConfiguration.loadConfiguration(fichier);
		this.etat = etat;
		this.monde = monde;
		this.locMin = locMin;
		this.locMax = locMax;
		this.createur = createur;
		this.contributeurs = contributeurs;
		this.nom = nom;
		this.epingle = epingle;
		this.spawn = spawn;
		this.interactionsAutorisees = interactionsAutorisees;
		this.mortLave = mortLave;
		this.mortEau = mortEau;
		this.sneakAutorise = sneakAutorise;
		this.forceNoPlates = forceNoPlates;
		this.blocsSpeciaux = blocsSpeciaux;
		scoreboardCreation = null;
		if (this.etat == CPMapState.CREATION)
		{
			creerScoreboardC();
			valide = false;
			restaurerPanneaux(); // Au cas où on soit après un plantage
		}
		else
		{
			valide = true;
			this.hauteurMort = hauteurMort;

			// Votes
			this.difficulte = difficulte;
			this.qualite = qualite;
			if (etat == CPMapState.DOWNLOADED && !Config.getConfig().contains("previous version")) // If it is the 2.9 update, we get rid of previous ratings for downloaded maps
			{
				saveConf = true;
			}
			else
			{
				if (listeVotes != null)
				{
					for (String v : listeVotes)
					{
						String[] va = v.split(":", 2);
						String[] notes = va[1].split(",", 2);
						int d = -1;
						int q = -1;
						try {
							d = Integer.valueOf(notes[0]);
							q = Integer.valueOf(notes[1]);
						} catch (Exception e) {
							// Rien
						}
						if (d > 0 || q > 0)
							votes.put(va[0], new Vote(d, q));
					}
					calculerNotes();
				}
			}
		}

		if (saveConf)
			sauvegarder();
	}

	@Deprecated
	CPMap (int id, String uuid, CPMapState etat, World monde, Block locMin, Block locMax, String nom, UUID createur, Set<UUID> contributeurs, boolean epingle)
	{
		boolean saveConf = false;

		this.id = id;
		this.fichier = GameManager.getFichierMap(id);
		this.config = YamlConfiguration.loadConfiguration(fichier);
		if (uuid == null || uuid.isEmpty())
		{
			this.uuid = UUID.randomUUID();
			saveConf = true;
		}
		else
			this.uuid = UUID.fromString(uuid);
		this.etat = etat;
		this.monde = monde;
		this.locMin = locMin;
		this.locMax = locMax;
		this.createur = createur;
		this.contributeurs = contributeurs;
		this.nom = nom;
		this.epingle = epingle;
		scoreboardCreation = null;
		YamlConfiguration configMaps = GameManager.getAncienneConfigMaps();
		spawn = new BlocSpawn(monde.getBlockAt(configMaps.getInt(id + ".spawn.x"), configMaps.getInt(id + ".spawn.y"), configMaps.getInt(id + ".spawn.z")), (byte) configMaps.getInt(id + ".spawn.dir"));
		if (this.etat == CPMapState.CREATION)
		{
			creerScoreboardC();
			valide = false;
		}
		else
		{
			valide = true;			
			blocsSpeciaux.add(new BlocDepart(monde.getBlockAt(configMaps.getInt(id + ".start.x"), configMaps.getInt(id + ".start.y"), configMaps.getInt(id + ".start.z"))));
			blocsSpeciaux.add(new BlocArrivee(monde.getBlockAt(configMaps.getInt(id + ".end.x"), configMaps.getInt(id + ".end.y"), configMaps.getInt(id + ".end.z"))));
			// Checkpoints
			boolean stop = false;
			for (int i=0; !stop; i++)
			{
				if (configMaps.getInt(id + ".checkpoints." + i + ".y") == 0)
				{
					stop = true;
				}
				else
				{
					blocsSpeciaux.add(new BlocCheckpoint(monde.getBlockAt(configMaps.getInt(id + ".checkpoints." + i + ".x"), configMaps.getInt(id + ".checkpoints." + i + ".y"), configMaps.getInt(id + ".checkpoints." + i + ".z")), (byte) configMaps.getInt(id + ".checkpoints." + i + ".dir"), configMaps.getString(id + ".checkpoints." + i + ".prop")));
				}
			}
			// Effets
			if (!Config.getConfig().getBoolean("map creation.disable potion effects"))
			{
				stop = false;
				for (int i=0; !stop; i++)
				{
					if (configMaps.getInt(id + ".effects." + i + ".y") == 0)
					{
						stop = true;
					}
					else
					{
						blocsSpeciaux.add(new BlocEffet(monde.getBlockAt(configMaps.getInt(id + ".effects." + i + ".x"), configMaps.getInt(id + ".effects." + i + ".y"), configMaps.getInt(id + ".effects." + i + ".z")), configMaps.getString(id + ".effects." + i + ".effect"), configMaps.getInt(id + ".effects." + i + ".duration"), configMaps.getInt(id + ".effects." + i + ".amplifier")));
					}
				}
			}
			// Gives
			stop = false;
			for (int i=0; !stop; i++)
			{
				if (configMaps.getInt(id + ".gives." + i + ".y") == 0)
				{
					stop = true;
				}
				else
				{
					blocsSpeciaux.add(new BlocGive(monde.getBlockAt(configMaps.getInt(id + ".gives." + i + ".x"), configMaps.getInt(id + ".gives." + i + ".y"), configMaps.getInt(id + ".gives." + i + ".z")), configMaps.getString(id + ".gives." + i + ".type"), configMaps.getString(id + ".gives." + i + ".action")));
				}
			}
			hauteurMort = configMaps.getInt(id + ".death height");

			// Votes
			if (etat != CPMapState.DOWNLOADED) // Si les votes sont en ligne, la liste des votes sera rempli avec les gens qui ont voté sur le site pour savoir ceux qui ont déjà voté
			{
				List<String> listeVotes = configMaps.getStringList(id + ".ratings");
				if (listeVotes != null)
				{
					for (String v : listeVotes)
					{
						String[] va = v.split(":", 2);
						votes.put(va[0], new Vote(Integer.valueOf(va[1]), -1));
					}
					calculerNotes();
				}
			}
			else
			{
				this.difficulte = Float.valueOf(configMaps.getString(id + ".difficulty"));
			}
		}

		if (saveConf)
			sauvegarder();
	}

	/**
	 * Constructor used for map that can be downloaded.
	 * @param id Map's id.
	 * @param creator Creator name.
	 * @param name Map's name.
	 * @param difficulty Map's difficulty.
	 * @param quality Map's quality.
	 * @param minVer Map's miminum compatible version (without conversion).
	 * @param conversionVer Map's minimum compatible version (after changing some incompatible blocks).
	 */
	CPMap (String id, String creator, String name, float difficulty, float quality, int minVer, int conversionVer)
	{
		this.id = Integer.MAX_VALUE;
		this.etat = null;
		this.nom = name;
		this.difficulte = difficulty;
		this.qualite = quality;
		this.webData = new HashMap<String, Object>();
		webData.put("id", id);
		webData.put("createur", creator);
		webData.put("nom", name);
		webData.put("minVer", minVer);
		webData.put("conversionVer", conversionVer);
	}

	private void creerScoreboardC()
	{
		scoreboardCreation = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = scoreboardCreation.registerNewObjective("cp_status", "dummy");
		String s = ChatColor.GOLD + "" + ChatColor.BOLD + Langues.getMessage("creation.status");
		if (s.length() > 32) { s = s.substring(0, 32); }
		obj.setDisplayName(s);
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		s = ChatColor.RED + Langues.getMessage("creation.unvalidated");
		if (s.length() > 16) { s = s.substring(0, 16); }
		obj.getScore(s).setScore(-1);
	}

	void test(Player p)
	{
		if (estEnTest() || traiterPanneaux(p)) // S'il y a déjà un testeur, on ne revérifie pas
		{
			p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("creation.test"));
			p.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + Langues.getMessage("creation.test leave"));
			ajouterTesteur(p);
			jouer(GameManager.getJoueur(p));
			sauvegarder(); // Pour retrouver les trucs si le serveur plante...
		}
	}

	void ajouterTesteur(Player p)
	{
		testeurs.add(p);
	}

	void supprimerTesteur(Player p)
	{
		testeurs.remove(p);
	}

	boolean contientTesteur(Player p)
	{
		return testeurs.contains(p);
	}

	boolean estEnTest()
	{
		return !testeurs.isEmpty();
	}

	void jouer(Joueur j)
	{
		jouer(j, true);
	}

	/**
	 * Met le joueur en mode jeu dans la map
	 * @param j Le joueur
	 * @param teleporter S'il faut le téléporter ou pas
	 */
	void jouer(Joueur j, boolean teleporter)
	{
		// Comptage des joueurs dans cette map
		int nbJoueurs = 0;
		List<Joueur> joueurs = GameManager.getJoueurs(uuid);
		for (int i=0; i < joueurs.size(); i++)
		{
			if (!joueurs.get(i).equals(j))
				nbJoueurs++;
		}
		if (Config.getConfig().getInt("game.max players per map") != -1 && nbJoueurs >= Config.getConfig().getInt("game.max players per map"))
		{
			j.setMap(null);
			j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("too many players"));
		}
		else
		{
			if (teleporter)
				j.getPlayer().teleport(spawn.getLocation().add(0.5, 0, 0.5));
			j.modeJeu();
			String c = NameManager.getNomAvecUUID(createur);
			if (c == null) c = "unknown";
			if (isPlayable())
				j.getPlayer().sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("play.welcome").replace("%map", ChatColor.ITALIC + nom + ChatColor.GREEN).replace("%creator", c));
		}
	}

	void publier(final Player p, String n)
	{
		if (valide)
		{
			if (estEnTest() || traiterPanneaux(p)) // Si elle est en test, les panneaux ne sont plus là
			{
				nom = n;
				createur = p.getUniqueId();
				testeurs.clear();
				difficulte = -1;
				qualite = -1;
				etat = CPMapState.PUBLISHED;
				sauvegarder();

				p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("creation.published"));
				// Annonce de la map
				if (Config.getConfig().getBoolean("map creation.announce new maps"))
				{
					Object[] onlinePlayers = Bukkit.getOnlinePlayers().toArray();
					for (int i=0; i < onlinePlayers.length; i++)
					{
						Player p1 = (Player) onlinePlayers[i];
						if (p1.hasPermission("creativeparkour.play") && !p1.equals(p))
						{
							// Message avec texte cliquacle pour jouer la map
							CPUtils.sendClickableMsg(p1, Langues.getMessage("creation.announce new map").replace("%player", p.getName()).replace("%map", ChatColor.ITALIC + nom + ChatColor.RESET + ChatColor.YELLOW), "/cp play " + nom, null, "%L", ChatColor.YELLOW);
						}
					}
				}

				// On met tout ceux qui ont construit la map en mode jeu
				Joueur j = GameManager.getJoueur(p);
				j.stopTimer(); // Au cas où il soit en train de tester
				this.jouer(j);
				for (UUID u : contributeurs)
				{
					Player p1 = Bukkit.getPlayer(u);
					if (p1 != null)
					{
						Joueur j1 = GameManager.getJoueur(p1);
						if (j1 != null && uuid.equals(j1.getMap()))
						{
							j1.stopTimer(); // Au cas où il soit en train de tester
							this.jouer(j1);
						}
					}
				}



				// Message d'encouragement au feedback
				/*final YamlConfiguration conf = Config.getConfJoueur(p.getUniqueId().toString());
				if (conf.getBoolean("feedback 2") != true && GameManager.nbMapsPubliees(p) >= 2) // Si c'est à plus de 4 jours de l'installation
				{
					Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
						public void run() {
							CPUtils.sendClickableMsg(p, Langues.getMessage("feedback 2"), null, "https://creativeparkour.net/contact.php", "%L", ChatColor.AQUA);
							conf.set("feedback 2", true);
							Config.saveConfJoueur(p.getUniqueId().toString());
						}
					}, 40);
				}*/
			}
		}
	}

	/**
	 * Returns the ID of this parkour map (used in some CreativeParkour commands and in files).
	 * @return Map's ID.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Returns the {@code UUID} of this parkour map (it never changes, except if it is edited by a player).
	 * @return Map's {@code UUID}.
	 */
	public UUID getUUID()
	{
		return uuid;
	}

	void setUUID(UUID uuid)
	{
		this.uuid = uuid;
	}

	/*void setEtat(EtatMap e)
	{
		etat = e;
		GameManager.configMaps.set(id + ".state", e);
		GameManager.saveConf();
	}*/

	/**
	 * Returns the state of this parkour map.
	 * @return Map's {@code CPState}
	 */
	public CPMapState getState()
	{
		return etat;
	}

	//	boolean estPartagee()
	//	{
	//		return partagee;
	//	}

	/**
	 * Returns the {@code UUID} of the player that created this parkour map.
	 * @return Map creator's Minecraft {@code UUID}. 
	 */
	public UUID getCreator()
	{
		return createur;
	}

	Set<UUID> getContributeurs()
	{
		return contributeurs;
	}
	/**
	 * Returns {@code UUID}s of players that were invited by the creator of the map to help building it.
	 * @return Contributor's Minecraft {@code UUID}s.
	 */
	public Set<UUID> getContributors()
	{
		return new HashSet<UUID>(contributeurs);
	}

	List<Player> getInvites()
	{
		return invites;
	}

	BlocSpawn getSpawn()
	{
		if (spawn != null)
			return spawn;
		else if (etat == CPMapState.CREATION)
			return new BlocSpawn(monde.getBlockAt((locMin.getX()+locMax.getX())/2, locMin.getY(), (locMin.getZ()+locMax.getZ())/2), (byte) 0);
		return null;
	}

	List<BlocSpecial> getBlocsSpeciaux()
	{
		return blocsSpeciaux;
	}

	List<BlocSpecial> getBlocsSpeciauxAir()
	{
		return blocsSpeciauxAir;
	}

	List<BlocSpecial> getBlocsSpeciauxPlaques()
	{
		return blocsSpeciauxPlaques;
	}

	List<BlocCheckpoint> getCkeckpoints()
	{
		List<BlocCheckpoint> liste = new ArrayList<BlocCheckpoint>();
		for (BlocSpecial b : blocsSpeciaux)
		{
			if (b instanceof BlocCheckpoint)
				liste.add((BlocCheckpoint) b);
		}
		return liste;
	}

	int getHauteurMort()
	{
		return hauteurMort;
	}

	/**
	 * Returns the minimum block of the map, used with the maximum block to know the cuboid of the parkour map.
	 * @return Map's minimum block (smallest X, Y and Z coordinates).
	 * @see CPMap#getMaxLoc()
	 */
	public Block getMinLoc()
	{
		return locMin;
	}
	/**
	 * Returnt the maximum block of the map, used with the minimum block to know the cuboid of the parkour map.
	 * @return Map's maximum block (highest X, Y and Z coordinates).
	 * @see CPMap#getMinLoc()
	 */
	public Block getMaxLoc()
	{
		return locMax;
	}

	/**
	 * Returns the {@code World} in which the parkour map is.
	 * @return {@code World} containing the map.
	 */
	public World getWorld()
	{
		return monde;
	}

	/**
	 * Returns the name of the parkour map, chosen by its creator.
	 * @return Map's name.
	 */
	public String getName()
	{
		return nom;
	}

	/**
	 * Returns all the blocks contained in this parkour map.
	 * @return {@code List} of all the blocks in the map.
	 */
	public List<Block> getBlocks()
	{
		//String msgDebug = id + " (" + etat + ", min(" + locMin.getX() + ";" + locMin.getY() + ";" + locMin.getZ() + "), max(" + locMax.getX() + ";" + locMax.getY() + ";" + locMax.getZ() + "))";
		//CreativeParkour.debug("BI1", "getBlocs for map " + msgDebug);
		List<Block> blocs = new ArrayList<Block>();
		for (int x = locMin.getX(); x <= locMax.getX(); x++)
		{
			for (int y = locMin.getY(); y <= locMax.getY(); y++)
			{
				for (int z = locMin.getZ(); z <= locMax.getZ(); z++)
				{
					blocs.add(monde.getBlockAt(x, y, z));
				}
			}
		}
		//CreativeParkour.debug("BI2", "End of getBlocs for map " + msgDebug);
		return blocs;
	}

	/**
	 * Returns the rdge of this parkour map (CreativeParkour maps are cubic).
	 * @return Map's edge.
	 */
	public int getSize()
	{
		int t1 = locMax.getX() - locMin.getX();
		int t2 = locMax.getY() - locMin.getY();
		int t3 = locMax.getZ() - locMin.getZ();
		if (t1 >= t2 && t1 >= t3)
			return t1;
		else if (t2 >= t1 && t2 >= t3)
			return t2;
		else
			return t3;
	}

	/**
	 * Checks if the parkour map contains the given block.
	 * @param b Block to ckeck.
	 * @return {@code true} if {@code b} is in the parkour map.
	 */
	public boolean containsBlock(Block b)
	{
		if (b.getX() >= locMin.getX() && b.getZ() >= locMin.getZ() && b.getX() <= locMax.getX() && b.getZ() <= locMax.getZ() && b.getY() >= locMin.getY() && b.getY() <= locMax.getY() && b.getWorld().equals(monde))
		{
			return true;
		}
		return false;
	}

	Scoreboard getScoreboardC()
	{
		if (valide)
		{
			String s0 = ChatColor.RED + Langues.getMessage("creation.unvalidated");
			if (s0.length() > 16) { s0 = s0.substring(0, 16); }
			scoreboardCreation.resetScores(s0);
			String s = ChatColor.GREEN + Langues.getMessage("creation.validated");
			if (s.length() > 16) { s = s.substring(0, 16); }
			scoreboardCreation.getObjective("cp_status").getScore(s).setScore(1);
		}
		else
		{
			String s0 = ChatColor.GREEN + Langues.getMessage("creation.validated");
			if (s0.length() > 16) { s0 = s0.substring(0, 16); }
			scoreboardCreation.resetScores(s0);
			String s = ChatColor.RED + Langues.getMessage("creation.unvalidated");
			if (s.length() > 16) { s = s.substring(0, 16); }
			scoreboardCreation.getObjective("cp_status").getScore(s).setScore(-1);
		}

		return scoreboardCreation;
	}

	Map<String, Object> getWebData()
	{
		return webData;
	}

	void setValide(boolean v)
	{
		valide = v;
		// Réinitialisation des temps si ce n'est plus valide
		if (!valide)
		{
			this.supprimerTemps();
			getListeTemps(true);
		}
	}

	/**
	 * Whether or not this parkour map has been tested and is valid (this is used in creation mode).
	 * @return Map's validity.
	 */
	public boolean isValid()
	{
		return valide;
	}

	/**
	 * Whether or not this parkour map is pinned in the map list (displayed with "/cp play").
	 * @return {@code true} if the map is pinned.
	 */
	public boolean isPinned()
	{
		return epingle;
	}

	/**
	 * Pins (or unpins) this parkour map in the map list.
	 * @param pin {@code true} to pin the map, {@code false} to unpin it.
	 */
	public void pin(boolean pin)
	{
		this.epingle = pin;
		sauvegarder();
	}

	/**
	 * Whether or not this map can be played with the "/cp play" command.
	 * @return {@code true} if the map is playable or {@code false} if it is in creation mode or deleted.
	 */
	public boolean isPlayable()
	{
		return etat == CPMapState.PUBLISHED || etat == CPMapState.DOWNLOADED;
	}

	synchronized void sauvegarder()
	{
		config.set("id", id);
		config.set("name", nom);
		config.set("uuid", uuid.toString());
		config.set("state", etat.toString());
		config.set("world", monde.getName());
		config.set("location min.x", locMin.getX());
		config.set("location min.y", locMin.getY());
		config.set("location min.z", locMin.getZ());
		config.set("location max.x", locMax.getX());
		config.set("location max.y", locMax.getY());
		config.set("location max.z", locMax.getZ());
		config.set("creator", createur.toString());
		List<String> contributeursS = new ArrayList<String>();
		for (UUID u : contributeurs)
		{
			contributeursS.add(u.toString());
		}
		config.set("contributors", contributeursS);
		List<String> listeVotes = new ArrayList<String>();
		Set<String> uuids = votes.keySet();
		Iterator<String> it = uuids.iterator();
		while (it.hasNext())
		{
			String cle = it.next();
			listeVotes.add(cle + ":" + votes.get(cle).toConfigString());
		}
		config.set("ratings", listeVotes);
		config.set("difficulty", String.valueOf(difficulte));
		config.set("quality", String.valueOf(qualite));
		config.set("pinned", epingle);
		if (spawn != null)
		{
			config.set("spawn.x", spawn.getBloc().getX());
			config.set("spawn.y", spawn.getBloc().getY());
			config.set("spawn.z", spawn.getBloc().getZ());
			config.set("spawn.dir", spawn.getDir());
		}
		config.set("special blocks", null);
		for (BlocSpecial bs : blocsSpeciaux)
		{
			String path = "special blocks." + CPUtils.coordsToString(bs.getBloc().getX(), bs.getBloc().getY(), bs.getBloc().getZ());
			config.set(path + ".t", "");
			ConfigurationSection c = config.getConfigurationSection(path);
			bs.setConfig(c);
			config.set(path, c);
		}
		config.set("death height", hauteurMort);
		config.set("sneak allowed", sneakAutorise);
		config.set("deadly lava", mortLave);
		config.set("deadly water", mortEau);
		config.set("interactions allowed", interactionsAutorisees);
		config.set("force no plates", forceNoPlates);

		// Sauvegarde
		try {
			config.save(fichier);
		} catch (IOException e) {
			Bukkit.getLogger().warning("An error occured while loading file '" + fichier.getPath() + "'.");
			e.printStackTrace();
		}
	}

	YamlConfiguration getConf()
	{
		return config;
	}

	void saveSpawn()
	{
		if (etat == CPMapState.CREATION)
		{
			List<Block> blocs = getBlocks();
			BlocSpawn ancienSpawn = spawn;
			spawn = null;
			for (int i=0; i < blocs.size() && spawn == null; i++)
			{
				if (blocs.get(i).getType().equals(Material.SIGN_POST) || blocs.get(i).getType().equals(Material.WALL_SIGN))
				{
					Sign sign = (Sign) blocs.get(i).getState();
					if (sign.getLine(0).toLowerCase().contains(BlocSpawn.getTag()))
					{
						spawn = new BlocSpawn(blocs.get(i), blocs.get(i).getData());
					}
				}
			}

			if (spawn != null)
			{
				sauvegarder();
			}
			else
			{
				spawn = ancienSpawn;
			}
		}
	}

	void updateTemps(Joueur j)
	{
		CPTime temps = j.getTemps();
		temps.realMilliseconds = j.getTempsReel();

		double multiplicateur = temps.realMilliseconds / 1000.0 / 60.0;
		if (multiplicateur < 1)
			multiplicateur = 1;
		if (Config.getConfig().getInt("game.milliseconds difference") > 0 && Math.abs(temps.realMilliseconds - temps.ticks * 50) > Config.getConfig().getInt("game.milliseconds difference") * multiplicateur)
		{
			j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.timer error").replace("%ticks", String.valueOf(temps.ticks)).replace("%tsec", String.valueOf(temps.inSeconds())).replace("%seconds", String.valueOf(temps.realMilliseconds / 1000.0)));
			Bukkit.getLogger().warning(Config.prefix(false) + Langues.getMessage("play.timer error log").replace("%player", j.getPlayer().getName()).replace("%ticks", String.valueOf(temps.ticks)).replace("%tsec", String.valueOf(temps.inSeconds())).replace("%seconds", String.valueOf(temps.realMilliseconds / 1000.0)));
		}
		else
		{
			CPTime tempsPreced = getTime(j.getUUID());

			boolean maj = false;
			if (tempsPreced == null)
			{
				maj = true;
			}
			else if (temps.ticks < tempsPreced.ticks)
			{
				maj = true;
				j.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + Langues.getMessage("play.time record"));
			}
			else if (etat == CPMapState.CREATION)
			{
				maj = true;
			}

			CPTime recordman = getRecord();

			if (maj)
			{
				temps.sauvegarder();
				getListeTemps(true); // Mise à jour de la liste de temps

				// Mise à jour des scoreboard des autres joueurs
				List<Joueur> joueurs = GameManager.getJoueurs(uuid);
				for (int i1=0; i1 < joueurs.size(); i1++)
				{
					joueurs.get(i1).calculerScoreboard();
				}

				// Mise à jour des panneaux de classements
				if (isPlayable())
					Panneau.majClassements(this);

				if (recordman != null && recordman.ticks > temps.ticks && !recordman.playerUUID.equals(j.getPlayer().getUniqueId()))
				{
					Player autreJoueur = Bukkit.getPlayer(recordman.playerUUID);
					if (autreJoueur != null && !Config.isBanned(autreJoueur)) // Notification à celui qui s'est fait battre son record
					{
						Joueur autreJoueurJ = GameManager.getJoueur(autreJoueur);
						if (autreJoueurJ == null)
						{
							autreJoueurJ = new Joueur(autreJoueur, false);
							GameManager.addJoueur(autreJoueurJ);
						}
						if (autreJoueurJ.getParamBool(PlayerSetting.NOTIFICATIONS) && isPlayable())
						{
							// Message avec texte cliquable pour jouer la map
							CPUtils.sendClickableMsg(autreJoueur, Langues.getMessage("play.record notification").replace("%player", j.getPlayer().getName()).replace("%map", ChatColor.ITALIC + nom + ChatColor.LIGHT_PURPLE), "/cp play " + nom, null, "%L", ChatColor.LIGHT_PURPLE);
							autreJoueurJ.infoDesactiverNotification();
						}
					}
					j.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + Langues.getMessage("play.time server record").replace("%player", recordman.getPlayerName()));
				}
			}
		}
	}

	void inviter(Player p, Player invite)
	{
		if (contributeurs.contains(invite.getUniqueId()) || createur.equals(invite.getUniqueId()))
		{
			p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.invite error 2"));
		}
		else if (invites.contains(invite))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 2"));
		}
		else
		{
			p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("creation.invitation").replace("%player", invite.getName()));
			Commandes.question(invite, Langues.getMessage("creation.invitation player").replace("%player", p.getName()), "invitation");
			if (!invites.contains(invite))
				invites.add(invite);
		}
	}

	void accepterInvitation(Player p)
	{
		invites.remove(p);
		if (!contributeurs.contains(p.getUniqueId()) && !p.getUniqueId().equals(createur))
		{
			contributeurs.add(p.getUniqueId());
			sauvegarder();
		}

		GameManager.teleporterCreation(p, this, false);
	}

	void refuserInvitation(Player p)
	{
		invites.remove(p);
	}

	void supprContibuteur(Player p, UUID contrib)
	{
		if (!contributeurs.contains(contrib))
		{
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.remove error"));
		}
		else
		{
			contributeurs.remove(contrib);
			Joueur j = GameManager.getJoueur(Bukkit.getPlayer(contrib));
			if (j != null && uuid.equals(j.getMap()))
				j.quitter(true, false);
			p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.remove ok").replace("%player", NameManager.getNomAvecUUID(contrib)));
			sauvegarder();
		}
	}

	boolean aVoteDifficulte(String uuid)
	{
		Vote v = votes.get(uuid);
		return v != null && v.getDifficulty() >= 1;
	}

	boolean aVoteQualite(String uuid)
	{
		Vote v = votes.get(uuid);
		return v != null && v.getQuality() >= 0;
	}

	void voteDifficulte(Player p, int difficulte)
	{
		if (votes.containsKey(p.getUniqueId().toString()))
		{
			votes.get(p.getUniqueId().toString()).setDifficulty(difficulte);
		}
		else
		{
			votes.put(p.getUniqueId().toString(), new Vote(difficulte, -1));
		}
		p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("play.difficulty ok"));
	}

	void voteQualite(Player p, int qualite)
	{
		if (votes.containsKey(p.getUniqueId().toString()))
		{
			votes.get(p.getUniqueId().toString()).setQuality(qualite);
		}
		else
		{
			votes.put(p.getUniqueId().toString(), new Vote(-1, qualite));
		}
		p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("play.difficulty ok"));
	}

	void ajouterVotant(String uuid, int difficulte, int qualite)
	{
		votes.put(uuid, new Vote(difficulte, qualite));
	}

	/**
	 * Calculates average map's difficulty and quality ratings (only if it is not downloaded, because downloaded maps get these numbers from creativeparkour.net).
	 */
	void calculerNotes()
	{
		if (etat != CPMapState.DOWNLOADED)
		{
			float sommeD = 0;
			float sommeQ = 0;
			int nombreD = 0;
			int nombreQ = 0;
			Set<String> uuids = votes.keySet();
			Iterator<String> it = uuids.iterator();
			while (it.hasNext())
			{
				Vote vote = votes.get(it.next());
				if (vote.getDifficulty() >= 1 && vote.getDifficulty() <= 5)
				{
					sommeD += vote.getDifficulty();
					nombreD++;
				}
				if (vote.getQuality() >= 1 && vote.getQuality() <= 5)
				{
					sommeQ += vote.getQuality();
					nombreQ++;
				}
			}
			this.difficulte = sommeD / nombreD;
			this.qualite = sommeQ / nombreQ;
		}
	}

	/**
	 * Returns the difficulty of this parkour map, calculated with players' votes.
	 * @return {@code int} value between 1 and 5 (1 = very easy, 5 = extreme).
	 */
	public float getDifficulty()
	{
		return difficulte;
	}

	void setDifficulty(float diff)
	{
		difficulte = diff;
	}

	/**
	 * Returns the quality of this parkour map, calculated with players' votes.
	 * @return {@code int} value between 1 and 5 (1 = bad, 5 = very good).
	 */
	public float getQuality()
	{
		return qualite;
	}

	void setQuality(float qual)
	{
		qualite = qual;
	}

	Map<String, Vote> getVotes()
	{
		return votes;
	}

	/**
	 * Instantly deletes the parkour map. <strong>This cannot be undone, be careful!</strong><br>
	 * Players in the map are also kicked outside of CreativeParkour.
	 */
	void supprimer()
	{
		supprimerTemps();
		etat = CPMapState.DELETED;
		nom = "";
		contributeurs = new HashSet<UUID>();
		invites = new ArrayList<Player>();
		votes = new HashMap<String, Vote>();
		blocsSpeciaux = new ArrayList<BlocSpecial>();
		difficulte = -1;
		qualite = -1;
		temps = null;
		valide = false;
		epingle = false;
		attempts.clear();
		sauvegarder();
		Panneau.supprimerMap(id);
		RewardManager.supprimerMap(id);
	}

	/**
	 * Fait modifier la map au joueur j (modification après publication)
	 * @param j
	 */
	void modifier(Joueur j)
	{
		Player p = j.getPlayer();
		etat = CPMapState.CREATION;
		creerScoreboardC();
		j.modeCreation();
		CPUtils.sendClickableMsg(p, Langues.getMessage("creation.new"), null, "https://creativeparkour.net/doc/map-creation.php", "%L", ChatColor.YELLOW);

		// Si le joueur n'est pas le créateur, on le met créateur et on met le créateur dans les contributeurs
		if (!p.getUniqueId().equals(createur))
		{
			contributeurs.add(createur);
			contributeurs.remove(p.getUniqueId()); // Si le type est déjà contributeur, il dégage de la liste
			createur = p.getUniqueId();
		}

		uuid = UUID.randomUUID();
		j.setMap(uuid);
		restaurerPanneaux();
	}

	/**
	 * Remplit les variables spawn, heuteurMort et blocsSpeciaux en analysant les panneaux posés dans la map. Les variables sont vidées s'il y a une erreur.
	 * @param p Joueur à qui envoyer les messages
	 * @return true s'il n'y a aucune erreur, false sinon.
	 */
	boolean traiterPanneaux(Player p)
	{
		p.sendMessage(Config.prefix() + ChatColor.GRAY + "" + ChatColor.ITALIC + Langues.getMessage("creation.check signs"));

		spawn = null;
		hauteurMort = 0;
		blocsSpeciaux = new ArrayList<BlocSpecial>();
		List<String> erreurs = new ArrayList<String>();
		// Variables juste là pour la vérif (pour pas avoir à les chercher dans la liste)
		List<Block> departs = new ArrayList<Block>();
		List<Block> arrivees = new ArrayList<Block>();
		Block blocDeath = null;

		// Recherche des panneaux de la map et remplissage des variables
		for (Block bloc : getBlocks())
		{
			if (bloc.getType().equals(Material.SIGN_POST) || bloc.getType().equals(Material.WALL_SIGN))
			{
				Sign sign = (Sign) bloc.getState();
				if (sign.getLine(0).toLowerCase().contains(BlocDepart.getTag()))
				{
					blocsSpeciaux.add(new BlocDepart(bloc));
					departs.add(bloc);
				}
				else if (sign.getLine(0).toLowerCase().contains(BlocArrivee.getTag()))
				{
					blocsSpeciaux.add(new BlocArrivee(bloc));
					arrivees.add(bloc);
				}
				else if (sign.getLine(0).toLowerCase().contains(BlocSpawn.getTag()))
				{
					if (spawn == null)
					{
						spawn = new BlocSpawn(bloc, bloc.getData());
					}
					else
					{
						erreurs.add(Langues.getMessage("creation.check.multiple sign error").replace("%type", ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + BlocSpawn.getTag() + ChatColor.RESET + ChatColor.RED).replace("1", ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.RED));
					}
				}
				else if (sign.getLine(0).toLowerCase().contains(BlocCheckpoint.getTag()))
				{
					blocsSpeciaux.add(new BlocCheckpoint(bloc, bloc.getData(), sign.getLine(1)));
				}
				else if (sign.getLine(0).toLowerCase().contains(BlocEffet.getTag()))
				{
					if (BlocEffet.estUnPanneauValide(sign.getLines(), p, bloc))
					{
						blocsSpeciaux.add(new BlocEffet(bloc, sign.getLine(1), Integer.valueOf(sign.getLine(2)), Integer.valueOf(sign.getLine(3))));
					}
				}
				else if (sign.getLine(0).toLowerCase().contains(BlocGive.getTag()))
				{
					blocsSpeciaux.add(new BlocGive(bloc, sign.getLine(1), sign.getLine(2)));
				}
				else if (sign.getLine(0).toLowerCase().contains(CPUtils.bracket("death")))
				{
					if (hauteurMort == 0)
					{
						hauteurMort = sign.getY();
						blocDeath = bloc;
					}
					else
					{
						erreurs.add(Langues.getMessage("creation.check.multiple sign error").replace("%type", ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + CPUtils.bracket("death") + ChatColor.RESET + ChatColor.RED).replace("1", ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.RED));
					}
				}
				else if (sign.getLine(0).toLowerCase().contains(BlocMort.getTag()))
				{
					blocsSpeciaux.add(new BlocMort(bloc));
				}
				else if (sign.getLine(0).toLowerCase().contains(BlocTP.getTag()))
				{
					Location loc = null;
					try {
						loc = new Location(bloc.getWorld(), Double.valueOf(sign.getLine(1)), Double.valueOf(sign.getLine(2)), Double.valueOf(sign.getLine(3)));
					} catch (Exception e) {
						// Rien, le message est après
					}
					if (loc == null)
						erreurs.add(Langues.getMessage("creation.check.tp error 1").replace("%loc", CPUtils.coordsToString(bloc.getX(), bloc.getY(), bloc.getZ())));
					else if (!this.containsBlock(loc.getBlock()))
						erreurs.add(Langues.getMessage("creation.check.tp error 2").replace("%loc", CPUtils.coordsToString(bloc.getX(), bloc.getY(), bloc.getZ())));
					else
						blocsSpeciaux.add(new BlocTP(bloc, loc));
				}
			}
		}

		if (departs.isEmpty())
		{
			erreurs.add(Langues.getMessage("creation.check.no sign error").replace("%type", ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + BlocDepart.getTag() + ChatColor.RESET + ChatColor.RED));
		}
		if (arrivees.isEmpty())
		{
			erreurs.add(Langues.getMessage("creation.check.no sign error").replace("%type", ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + BlocArrivee.getTag() + ChatColor.RESET + ChatColor.RED));
		}
		if (spawn == null)
		{
			erreurs.add(Langues.getMessage("creation.check.no sign error").replace("%type", ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + BlocSpawn.getTag() + ChatColor.RESET + ChatColor.RED));
		}
		boolean errHauteur = spawn != null && hauteurMort >= spawn.getBloc().getY();
		if (!errHauteur) // Pas besoin de ces vérifications si on est déjà en erreur...
		{
			// Départs
			for (int i=0; i < departs.size() && !errHauteur; i++)
			{
				if (hauteurMort > departs.get(i).getY())
					errHauteur = true;
			}
			// Arrivées
			for (int i=0; i < arrivees.size() && !errHauteur; i++)
			{
				if (hauteurMort > arrivees.get(i).getY())
					errHauteur = true;
			}
		}
		if (errHauteur)
			erreurs.add(Langues.getMessage("creation.check.sign height error").replace("%type", ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + CPUtils.bracket("death") + ChatColor.RESET + ChatColor.RED));

		List<String> messages = new ArrayList<String>();
		for (int i=0; i < erreurs.size(); i++)
		{
			if (!messages.contains(erreurs.get(i)))
			{
				messages.add(erreurs.get(i));
			}
		}
		for (int i=0; i < messages.size(); i++)
		{
			p.sendMessage(ChatColor.RED + messages.get(i));
		}

		// S'il y a des erreurs, on réinitialise les variables et on renvoir false
		if (!erreurs.isEmpty())
		{
			spawn = null;
			hauteurMort = 0;
			blocsSpeciaux = new ArrayList<BlocSpecial>();
			return false;
		}
		else
		{
			// Suppression des panneaux
			spawn.supprimerPanneau();
			if (blocDeath != null)
			{
				blocDeath.setType(Material.AIR);
				blocHautMort = blocDeath;
			}
			for (BlocSpecial b : blocsSpeciaux)
			{
				b.supprimerPanneau();
			}
			placerOuRetirerPlaques();
			return true;
		}
	}

	/**
	 * Place les panneaux dans la map en fonction des valeurs des variables (spawn, blocsSpeciaux...)
	 */
	void restaurerPanneaux()
	{
		if (spawn != null && !(spawn.getBloc().equals(GameManager.getDefaultSpawn(monde, locMin.getX(), locMax.getX(), locMin.getY(), locMin.getZ(), locMax.getZ())) && spawn.getDir() == 0)) // Si c'est le spawn par défaut, on ne met pas le panneau
			creerPanneau(spawn.getBloc(), spawn.getDir(), spawn.getPanneau());
		// Blocs spéciaux
		if (blocsSpeciaux != null)
		{
			for (BlocSpecial bs : blocsSpeciaux)
			{
				creerPanneau(bs.getBloc(), bs.getDir(), bs.getPanneau());
			}
			blocsSpeciaux.clear();
		}

		if (blocHautMort == null)
		{
			// Recherche d'un bloc libre pour la hauteur de mort
			boolean stop = false;
			for (int x=locMin.getX(); x <= locMax.getX() && !stop; x++)
			{
				for (int z=locMin.getZ(); z <= locMax.getZ() && !stop; z++)
				{
					stop = creerPanneau(monde.getBlockAt(x, hauteurMort, z), (byte) 0, new String[]{CPUtils.bracket("death"), "", "", ""});
				}
			}
		}
		else
		{
			creerPanneau(blocHautMort, (byte) 0, new String[]{CPUtils.bracket("death"), "", "", ""});
			blocHautMort = null;
		}
		hauteurMort = 0;
		
		sauvegarder();
	}

	/**
	 * Place un panneau au bloc indiqué avec le texte
	 * @param bloc Bloc où mettre le panneau
	 * @param dir Direction du panneau
	 * @param lignes
	 * @return True si le panneu a été placé, false sinon
	 */
	private boolean creerPanneau(Block bloc, byte dir, String[] lignes)
	{
		// Mise à jour du bloc
		bloc = bloc.getWorld().getBlockAt(bloc.getX(), bloc.getY(), bloc.getZ());

		boolean ok = false;
		if (bloc.getRelative(BlockFace.DOWN).getType().isSolid())
		{
			bloc.setType(Material.SIGN_POST);
			bloc.setData(dir);
			ok = true;
		}
		else
		{
			// Test des différentes faces
			BlockFace[] faces = {BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST};
			for (BlockFace f : faces)
			{
				if (bloc.getRelative(f).getType().isSolid())
				{
					bloc.setType(Material.WALL_SIGN);
					
					org.bukkit.material.Sign signData = new org.bukkit.material.Sign(Material.WALL_SIGN);
					signData.setFacingDirection(f.getOppositeFace());
					org.bukkit.block.Sign sign = (org.bukkit.block.Sign) bloc.getState();
					sign.setData(signData);
					sign.update();
					ok = true;
				}
			}
		}

		// Création du panneau
		if (ok && bloc.getState() instanceof Sign)
		{
			Sign panneau = ((Sign)bloc.getState());
			panneau.setLine(0, lignes[0]);
			panneau.setLine(1, lignes[1]);
			panneau.setLine(2, lignes[2]);
			panneau.setLine(3, lignes[3]);
			panneau.update();
		}
		return ok;
	}

	void togglePlates(Joueur j)
	{
		forceNoPlates = !forceNoPlates;
		if (!forceNoPlates)
			j.getPlayer().sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.noplates disabled"));
		else
			j.getPlayer().sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.noplates enabled"));
		// S'il joue (ou teste), mise à jour des trucs
		if (j.getEtat() == EtatJoueur.JEU || j.getEtat() == EtatJoueur.SPECTATEUR)
			placerOuRetirerPlaques();
		sauvegarder();
	}

	void placerOuRetirerPlaques()
	{
		boolean valConf = Config.getConfig().getBoolean("game.pressure plates as special blocks");
		blocsSpeciauxAir.clear();
		blocsSpeciauxPlaques.clear();
		for (BlocSpecial bs : blocsSpeciaux)
		{
			bs.placerPlaqueOuAir(forceNoPlates || !valConf);
			if (bs.estPlaquePression())
				blocsSpeciauxPlaques.add(bs);
			else
				blocsSpeciauxAir.add(bs);
		}
	}

	JsonObject getJson()
	{
		JsonObject objet = new JsonObject();

		// Ajout des blocs
		List<Block> blocs = getBlocks();
		JsonArray blocsJ = new JsonArray();
		int xMin = locMin.getX();
		int yMin = locMin.getY();
		int zMin = locMin.getZ();
		List<JsonObject> objetsBlocs = new ArrayList<JsonObject>();
		for (int i=0; i < blocs.size(); i++) {
			Block b = blocs.get(i);
			if (b.getType() != Material.AIR)
			{
				if (GameManager.blocsInterdits.contains(b.getType()))
				{
					return null;
				}
				JsonObject objetBloc = new JsonObject();
				objetBloc.addProperty("t", b.getType().name());
				objetBloc.addProperty("d", b.getData());
				if (b.getState() instanceof Sign) {
					Sign p = ((Sign)b.getState());
					JsonObject oPanneau = new JsonObject();
					oPanneau.addProperty("0", p.getLine(0));
					oPanneau.addProperty("1", p.getLine(1));
					oPanneau.addProperty("2", p.getLine(2));
					oPanneau.addProperty("3", p.getLine(3));
					objetBloc.add("lignes-panneau", oPanneau);
				}
				else if (b.getState() instanceof Banner)
				{
					Banner ba = (Banner) b.getState();
					JsonObject oBan = new JsonObject();
					oBan.addProperty("baseColor", ba.getBaseColor().name());
					List<Pattern> patterns = ba.getPatterns();
					JsonArray patternsJ = new JsonArray();
					for (int j=0; j < patterns.size(); j++)
					{
						JsonObject oPat = new JsonObject();
						oPat.addProperty("id", j);
						oPat.addProperty("color", patterns.get(j).getColor().name());
						oPat.addProperty("pattern", patterns.get(j).getPattern().name());
						patternsJ.add(oPat);
					}
					oBan.add("patterns", patternsJ);
					objetBloc.add("donnees-banniere", oBan);
				}
				else if (b.getState() instanceof Skull)
				{
					Skull sk = (Skull) b.getState();
					JsonObject oSk = new JsonObject();
					oSk.addProperty("skullType", sk.getSkullType().name());
					oSk.addProperty("rotation", sk.getRotation().name());
					// TODO Joueurs
					objetBloc.add("donnees-tete", oSk);
				}
				else if (CreativeParkour.getServVersion() >= 12 && b.getState() instanceof Bed)
				{
					Bed bed = (Bed) b.getState();
					JsonObject oBed = new JsonObject();
					oBed.addProperty("color", bed.getColor().name());
					objetBloc.add("donnees-lit", oBed);
				}
				// Recherche du bloc dans ceux déjà enregistrés (seuls les ids de cette liste sont mis, puis la liste est donnée)
				boolean trouve = false;
				JsonObject oID = new JsonObject();
				oID.addProperty("c", (b.getX() - xMin) + ";" + (b.getY() - yMin) + ";" + (b.getZ() - zMin));
				for (int j=0; j < objetsBlocs.size() && !trouve; j++)
				{
					if (objetsBlocs.get(j).equals(objetBloc))
					{
						trouve = true;
						oID.addProperty("i", j);
					}
				}
				if (!trouve)
				{
					oID.addProperty("i", objetsBlocs.size());
					objetsBlocs.add(objetBloc);
				}
				blocsJ.add(oID);
			}
		}
		objet.add("blocs", blocsJ);

		// Ajout de la liste des types de blocs
		JsonArray blocsT = new JsonArray();
		for (int i=0; i < objetsBlocs.size(); i++)
		{
			JsonObject ob = objetsBlocs.get(i);
			ob.addProperty("i", i);
			blocsT.add(ob);
		}
		objet.add("types", blocsT);

		// Ajout des entités
		JsonArray entitesJ = new JsonArray();
		for (int i=0; i < monde.getEntities().size(); i++)
		{
			Entity e = monde.getEntities().get(i);
			if ((e.getType() == EntityType.ARMOR_STAND || e.getType() == EntityType.ITEM_FRAME || e.getType() == EntityType.PAINTING || e.getType().name().equals("ENDER_CRYSTAL")) && containsBlock(e.getLocation().getBlock()))
			{
				JsonObject entite = new JsonObject();
				entite.addProperty("type", e.getType().name());
				Location loc = e.getLocation();
				entite.addProperty("x", loc.getX() - xMin);
				entite.addProperty("y", loc.getY() - yMin);
				entite.addProperty("z", loc.getZ() - zMin);
				entite.addProperty("yaw", loc.getYaw());
				entite.addProperty("pitch", loc.getPitch());
				if (e instanceof Painting)
				{
					entite.addProperty("art", ((Painting) e).getArt().name());
				}
				else if (e instanceof ItemFrame)
				{
					entite.addProperty("item", ((ItemFrame) e).getItem().getType().name());
					entite.addProperty("rotation", ((ItemFrame) e).getRotation().name());
				}
				//				else if (e instanceof ArmorStand)
				//				{
				//					ArmorStand as = (ArmorStand) e;
				//					entite.addProperty("boots", as.getBoots().getType().name());
				//					entite.addProperty("leggings", as.getLeggings().getType().name());
				//					entite.addProperty("chestplate", as.getChestplate().getType().name());
				//					entite.addProperty("helmet", as.getHelmet().getType().name());
				//					entite.addProperty("hand", as.getItemInHand().getType().name());
				//				}
				entitesJ.add(entite);
			}
		}
		objet.add("entites", entitesJ);

		// Ajout des blocs spéciaux (start, end, checkpoints...)
		JsonArray blocsSpeciaux = new JsonArray();

		JsonObject bs = new JsonObject();
		bs.addProperty("t", spawn.getTypeA());
		bs.addProperty("c", (spawn.getBloc().getX() - xMin) + ";" + (spawn.getBloc().getY() - yMin) + ";" + (spawn.getBloc().getZ() - zMin));
		bs.addProperty("dir", spawn.getDir());
		blocsSpeciaux.add(bs);

		JsonArray checkpointsA = new JsonArray();
		JsonArray effetsA = new JsonArray();
		JsonArray givesA = new JsonArray();
		JsonArray tpA = new JsonArray();
		for (BlocSpecial b : this.blocsSpeciaux)
		{
			bs = new JsonObject();
			bs.addProperty("t", b.getTypeA());
			bs.addProperty("c", (b.getBloc().getX() - xMin) + ";" + (b.getBloc().getY() - yMin) + ";" + (b.getBloc().getZ() - zMin));
			if (b.dir != 0)
				bs.addProperty("dir", b.getDir());
			if (b instanceof BlocCheckpoint)
			{
				bs.addProperty("prop",( (BlocCheckpoint) b).getProp());
				checkpointsA.add(bs);
			}
			else if (b instanceof BlocEffet)
			{
				PotionEffect e = ((BlocEffet) b).getEffet();
				bs.addProperty("effect", e.getType().getName());
				bs.addProperty("duration", e.getDuration() / 20);
				bs.addProperty("amplifier", e.getAmplifier());
				effetsA.add(bs);
			}
			else if (b instanceof BlocGive)
			{
				BlocGive g = (BlocGive) b;
				bs.addProperty("type", g.getTypeGive().toString());
				bs.addProperty("action", g.getAction());
				givesA.add(bs);
			}
			else if (b instanceof BlocTP)
			{
				BlocTP tp = (BlocTP) b;
				bs.addProperty("x", tp.getLocTP().getX() - xMin);
				bs.addProperty("y", tp.getLocTP().getY() - yMin);
				bs.addProperty("z", tp.getLocTP().getZ() - zMin);
				tpA.add(bs);
			}
			else // Spawn, départ, arrivée, mort
				blocsSpeciaux.add(bs);
		}
		blocsSpeciaux.add(checkpointsA);
		blocsSpeciaux.add(effetsA);
		blocsSpeciaux.add(givesA);
		blocsSpeciaux.add(tpA);

		objet.add("blocs speciaux", blocsSpeciaux);

		objet.addProperty("hauteurMort", hauteurMort);
		objet.addProperty("sneakAutorise", sneakAutorise);
		objet.addProperty("mortLave", mortLave);
		objet.addProperty("mortEau", mortEau);
		objet.addProperty("interactionsAutorisees", interactionsAutorisees);

		return objet;
	}

	void partager(Joueur j) throws NoSuchMethodException, SecurityException, IOException
	{
		if (Config.online())
		{
			if (!j.getUUID().equals(createur))
			{
				String nomCreateur = "unknown";
				if (createur != null)
					nomCreateur = NameManager.getNomAvecUUID(createur);
				if (nomCreateur == null || nomCreateur.isEmpty())
					nomCreateur = "unknown";
				j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.share error creator").replace("%creator", nomCreateur));
			}
			else
			{
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("ipJoueur", j.getPlayer().getAddress().getHostName());
				params.put("uuidJoueur", j.getPlayer().getUniqueId().toString());
				params.put("nomJoueur", j.getPlayer().getName());
				params.put("uuidMap", uuid.toString());
				params.put("nomMap", nom);
				params.put("difficulte", String.valueOf(difficulte));
				params.put("qualite", String.valueOf(qualite));
				params.put("createur", createur.toString() + ":" + NameManager.getNomAvecUUID(createur));
				StringBuffer contributeurs = new StringBuffer();
				for (UUID c : this.contributeurs) {
					contributeurs.append(c.toString() + ":" + NameManager.getNomAvecUUID(c) + ";");
				}
				if (contributeurs.length() > 2)  // On vire le dernier point-virgule s'il y a des gens dans la liste
					contributeurs.deleteCharAt(contributeurs.length() - 1);
				params.put("contributeurs", contributeurs.toString());
				params.put("taille", String.valueOf(getSize()));
				JsonObject json = getJson();
				if (json == null) // S'il y a un bloc interdit
				{
					j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.share error block"));
				}
				else
				{
					params.put("contenu", json.toString());
					CPRequest.effectuerRequete("maps.php", params, this, this.getClass().getMethod("reponsePartage", JsonObject.class, String.class, Player.class), j.getPlayer());
					j.getPlayer().sendMessage(Config.prefix() + ChatColor.ITALIC + Langues.getMessage("commands.share wait"));
				}
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
	public void reponsePartage(JsonObject json, String rep, Player p) throws InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponsePartage") && !CreativeParkour.erreurRequete(json, p))
		{
			if (json.get("data").getAsJsonObject().get("servInconnu") != null && json.get("data").getAsJsonObject().get("servInconnu").getAsBoolean() == true)
			{
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.share unknown server"));
			}
			else if (json.get("data").getAsJsonObject().get("mapDejaExistante") != null && json.get("data").getAsJsonObject().get("mapDejaExistante").getAsBoolean() == true)
			{
				CPUtils.sendClickableMsg(p, Langues.getMessage("commands.share error existing map"), null, CreativeParkour.lienSite() + "/user/map.php?id=" + json.get("data").getAsJsonObject().get("idMap").getAsString(), "%L", ChatColor.YELLOW);
			}
			else
			{
				CPUtils.sendClickableMsg(p, Langues.getMessage("commands.share new map"), null, CreativeParkour.lienSite() + "/user/map.php?c=" + json.get("data").getAsJsonObject().get("cle").getAsString(), "%L", ChatColor.YELLOW);
			}
		}
	}

	void exporter(Joueur j) throws UnsupportedEncodingException, IOException
	{
		if (etat == CPMapState.DOWNLOADED)
			j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.export error"));
		else
		{
			JsonObject objet = new JsonObject();
			objet.addProperty("uuidMap", uuid.toString());
			objet.addProperty("nomMap", nom);
			objet.addProperty("taille", getSize());
			objet.addProperty("difficulte", difficulte);
			objet.addProperty("qualite", qualite);
			objet.addProperty("createur", createur.toString() + ":" + NameManager.getNomAvecUUID(createur));
			StringBuffer contributeurs = new StringBuffer();
			for (UUID c : this.contributeurs) {
				contributeurs.append(c.toString() + ":" + NameManager.getNomAvecUUID(c) + ";");
			}
			if (contributeurs.length() > 2)  // On vire le dernier point-virgule s'il y a des gens dans la liste
				contributeurs.deleteCharAt(contributeurs.length() - 1);
			objet.addProperty("contributeurs", contributeurs.toString());
			objet.add("contenu", getJson());
			// Ecriture du fichier
			File dossier = new File(CreativeParkour.getPlugin().getDataFolder(), "/Exported maps");
			dossier.mkdirs();
			File file = new File(dossier, nom.replaceAll("[^a-zA-Z0-9.-]", "_") + "-" + uuid.toString() + ".cpmap");
			FileOutputStream output = new FileOutputStream(file);
			try {
				Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8");
				try {
					writer.write(objet.toString());
				} finally {
					writer.close();
				}
				j.getPlayer().sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.export success").replace("%file", file.getName()));
			} finally {
				output.close();
			}
		}
	}

	SortedSet<CPTime> getListeTemps()
	{
		return getListeTemps(false);
	}

	synchronized SortedSet<CPTime> getListeTemps(boolean forcerMAJ)
	{
		if (temps == null || forcerMAJ)
		{
			temps = new TreeSet<CPTime>();
			for (File f : GameManager.getFichiersTemps())
			{
				if (f.getName().startsWith(uuid.toString())) // Si c'est bien un temps de cette map
				{
					CPTime t = getTempsAvecFichier(f);
					if (t != null)
						temps.add(t);
				}
			}
		}
		return temps;
	}

	boolean listeTempsDispo()
	{
		return temps != null;
	}

	/**
	 * Returns the time of the given player in this parkour map.
	 * @param playerUUID {@code UUID} of the player we want the {@code CPTime}.
	 * @return Player's {@code CPTime} in the parkour map.
	 */
	public CPTime getTime(UUID playerUUID)
	{
		getListeTemps();
		for (CPTime t : temps)
		{
			if (t.playerUUID.equals(playerUUID))
				return t;
		}
		return null;
	}

	/**
	 * Returns the best time in this map.
	 * @return Best {@code CPTime} in this map.
	 */
	public CPTime getRecord()
	{
		if (getListeTemps().isEmpty())
			return null;
		else
			return temps.first();
	}

	private void supprimerTemps()
	{
		GameManager.supprimerFichiersTemps(null, uuid, false);
	}

	CPTime getTempsAvecFichier(File fichier)
	{
		try {
			YamlConfiguration yml = CPUtils.getYML(fichier);
			CPTime t = new CPTime(UUID.fromString(yml.getString("player uuid")), this, yml.getInt("ticks"));
			t.ajouterCheckpoints(yml.get("checkpoints"));
			t.ajouterPositions(yml.get("ghost"));
			t.date = new Date(yml.getLong("date"));
			if (yml.getString("state") != null)
				t.etat = EtatTemps.valueOf(yml.getString("state"));
			t.realMilliseconds = yml.getLong("real milliseconds");
			return t;
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Vide la mémoire des choses inutiles
	 */
	void vidange()
	{
		if (GameManager.getJoueurs(uuid).isEmpty())
			temps = null;
	}

	void setRemplisseur(RemplisseurBlocs remplisseur)
	{
		this.remplisseur = remplisseur;
	}

	int getDelaiRemplissage()
	{
		if (remplisseur == null)
			return 0;
		int d = remplisseur.getDureeTraitement();
		if (d == 0)
			remplisseur = null;
		return d;
	}

	void afficherOptions(Joueur j)
	{
		j.invOptionsMaps = new InventaireOptionsMap(this, j);
		j.invOptionsMaps.remplir();
		j.getPlayer().openInventory(j.invOptionsMaps.getInventaire());
	}

	/**
	 * Increments attempt number for the specified player.
	 * @param player Player's {@code UUID}.
	 */
	void addAttempt(UUID player)
	{
		AtomicInteger nb = attempts.get(player);
		if (nb != null)
			nb.incrementAndGet();
		else
			attempts.put(player, new AtomicInteger(1));

		// Incrementing general attempt count
		if (CreativeParkour.stats() != null)
			CreativeParkour.stats().parkoursTentes++;
	}

	/**
	 * Returns number of attempts per player in this map.
	 * @return Nuber of attempts ({@code AtomicInteger}) associated with player's {@code UUID}.
	 */
	Map<UUID, AtomicInteger> getAttempts()
	{
		return attempts;
	}

	/**
	 * Reset number of attempts per player.
	 */
	void resetAttempts()
	{
		attempts.clear();
	}
}