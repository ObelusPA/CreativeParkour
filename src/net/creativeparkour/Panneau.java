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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

class Panneau implements Listener
{
	private static File fichier;
	private static YamlConfiguration config;
	private static List<Panneau> panneaux = new ArrayList<Panneau>();

	static void enable(Plugin plugin)
	{
		//long nano = System.nanoTime();
		//nano = Util.debugNanoTime("P1", nano);
		File ancienFichier = new File(plugin.getDataFolder(), "signs.yml");

		fichier = new File(plugin.getDataFolder(), "lobby signs.yml");
		config = YamlConfiguration.loadConfiguration(fichier);
		panneaux = new ArrayList<Panneau>();

		// Conversion de l'ancien fichier
		if (ancienFichier.exists() && !fichier.exists())
		{
			YamlConfiguration ancienneConfig = YamlConfiguration.loadConfiguration(ancienFichier);
			boolean termine = false;
			for (int i=0; !termine; i++)
			{
				TypePanneau type = traduireType(ancienneConfig.getString(i + ".type"));
				if (type == null)
					termine = true;
				else
				{
					String coords = ancienneConfig.getInt(i + ".x") + ";" + ancienneConfig.getInt(i + ".y") + ";" + ancienneConfig.getInt(i + ".z");
					String path = ancienneConfig.getString(i + ".world") + ";" + coords;
					CPMap m = null;
					if (type == TypePanneau.MAP || type == TypePanneau.LEADERBOARD)
						m = GameManager.getMap(ancienneConfig.getString(i + ".map"));
					if (m == null && (type == TypePanneau.MAP || type == TypePanneau.LEADERBOARD))
					{
						// On ne fait rien, la map n'est pas valide...
					}
					else
					{
						config.set(path + ".type", type.toString());
						config.set(path + ".world", ancienneConfig.getString(i + ".world"));
						config.set(path + ".coords", coords);
						if (m != null)
							config.set(path + ".map id", m.getId());
						if (ancienneConfig.contains(i + ".show name"))
							config.set(path + ".show name", ancienneConfig.getBoolean(i + ".show name"));
						if (ancienneConfig.contains(i + ".limit"))
							config.set(path + ".limit", ancienneConfig.getInt(i + ".limit"));
					}
				}
			}
			// Sauvegarde
			try {
				config.save(fichier);
			} catch (IOException e) {
				Bukkit.getLogger().warning("An error occurred while loading file 'lobby signs.yml'");
				e.printStackTrace();
			}
			// Renommage de l'ancien fichier
			ancienFichier.renameTo(new File(plugin.getDataFolder(), "signs (deprecated).yml"));
		}
		//nano = Util.debugNanoTime("P2", nano);

		// Le remplissage de la liste de panneaux avec les mondes déjà chargés
		for(int i=0; i < Bukkit.getWorlds().size(); i++)
		{
			chargementMonde(Bukkit.getWorlds().get(i));
		}
		//nano = Util.debugNanoTime("P3", nano);

		saveConf();
		//nano = Util.debugNanoTime("P4", nano);
	}

	@EventHandler
	void onWorldLoad(WorldLoadEvent e)
	{
		if (CreativeParkour.loaded)
			chargementMonde(e.getWorld());
	}

	static void chargementMonde(final World w)
	{
		// Tâche async
		Bukkit.getScheduler().runTaskAsynchronously(CreativeParkour.getPlugin(), new Runnable() {
			public void run() {
				// Ajout des panneaux de ce monde dans la liste
				Iterator<String> it = config.getKeys(false).iterator();
				int nbAjouts = 0;
				int i=0; // Délai pour les Tasks
				while (it.hasNext())
				{
					i += 5; // 4 Panneaux par seconde
					final ConfigurationSection confP = config.getConfigurationSection(it.next());
					if (confP.getString("world").equalsIgnoreCase(w.getName()))
					{
						final Map<Character, Integer> coords = CPUtils.parseCoordinates(confP.getString("coords"));
						final TypePanneau type = traduireType(confP.getString("type"));
						if (type != null)
						{
							// On revient en Sync
							Bukkit.getScheduler().scheduleSyncDelayedTask(CreativeParkour.getPlugin(), new Runnable() {
								public void run() {
									if (type == TypePanneau.MAP)
										panneaux.add(new PanneauMap(w, coords.get('x'), coords.get('y'), coords.get('z'), confP.getString("type"), confP.getInt("map id")));
									else if (type == TypePanneau.LEADERBOARD)
										panneaux.add(new PanneauClassement(w, coords.get('x'), coords.get('y'), coords.get('z'), confP.getString("type"), confP.getInt("map id"), confP.getBoolean("show name"), confP.getInt("limit")));
									else
										panneaux.add(new Panneau(w, coords.get('x'), coords.get('y'), coords.get('z'), confP.getString("type")));
								}
							}, i);
							nbAjouts++;
						}
					}
				}

				if (nbAjouts > 0)
					Bukkit.getLogger().info(Config.prefix(false) + nbAjouts + " sign(s) loaded in world " + w.getName() + ".");
			}
		});
	}

	@EventHandler
	void onRightClick(PlayerInteractEvent e)
	{
		try {
			if (e.getHand() != EquipmentSlot.HAND) // Pour éviter que l'événement passe 2 fois (une fois pour chaque main)
			{
				return;
			}
		} catch (NoSuchMethodError err) {
			// Rien
		}
		
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock() != null && (e.getClickedBlock().getType().equals(Material.SIGN_POST) || e.getClickedBlock().getType().equals(Material.WALL_SIGN)) && !e.getPlayer().isSneaking())
		{
			Panneau p = getPanneau(e.getClickedBlock());
			if (p != null)
			{
				if (p.type == TypePanneau.CREATE)
					e.getPlayer().performCommand("creativeparkour create");
				else if (p.type == TypePanneau.PLAY)
					e.getPlayer().performCommand("creativeparkour play");
				else if (p.type == TypePanneau.MAP || p.type == TypePanneau.LEADERBOARD)
					e.getPlayer().performCommand("creativeparkour play " + ((PanneauMap)p).map.getName());
			}
		}
	}

	@EventHandler
	void onBlockBreak(BlockBreakEvent e)
	{
		Panneau p = getPanneau(e.getBlock());
		if (p != null)
		{
			p.supprimer();
		}
	}

	@EventHandler
	void onSignChange(SignChangeEvent e) // Lors de la pose d'un panneau, on vérifie si il y a le tag cp
	{
		if (e.getPlayer().hasPermission("creativeparkour.createSigns") && e.getLine(0).equalsIgnoreCase(CPUtils.bracket("cp")) && !GameManager.estDansUneMap(e.getBlock()))
		{
			Panneau p = getPanneau(e.getBlock());
			if (p != null)
				p.supprimer();
			if (e.getLine(1).equalsIgnoreCase("create") || e.getLine(1).equalsIgnoreCase("play") || e.getLine(1).equalsIgnoreCase("map") || e.getLine(1).equalsIgnoreCase("leaderboard") || e.getLine(1).equalsIgnoreCase(Langues.getMessage("signs.create")) || e.getLine(1).equalsIgnoreCase(Langues.getMessage("signs.play")) || e.getLine(1).equalsIgnoreCase(Langues.getMessage("signs.map")) || e.getLine(1).equalsIgnoreCase(Langues.getMessage("signs.leaderboard")))
			{
				Panneau panneau = null;
				if (e.getLine(1).equalsIgnoreCase("map") || e.getLine(1).equalsIgnoreCase(Langues.getMessage("signs.map")))
				{
					int idMap = getIdMap(e.getLine(2));
					if (idMap >= 0)
						panneau = new PanneauMap(e.getBlock().getWorld(), e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), e.getLine(1), idMap);
					else
						e.setLine(0, Langues.getMessage("signs.unknown map"));
				}
				else if (e.getLine(1).equalsIgnoreCase("leaderboard") || e.getLine(1).equalsIgnoreCase(Langues.getMessage("signs.leaderboard")))
				{
					String[] s = e.getLine(3).split("\\+", 2);
					int borne = 1;
					boolean afficherNom = false;
					try {
						borne = Integer.parseInt(s[0]);
					} catch (NumberFormatException e1)
					{
						// Rien
					}
					afficherNom = s.length > 1 && s[1].equalsIgnoreCase("name");
					int idMap = getIdMap(e.getLine(2));
					if (borne > 0 && idMap >= 0)
						panneau = new PanneauClassement(e.getBlock().getWorld(), e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), e.getLine(1), idMap, afficherNom, borne);
					else if (idMap < 0)
						e.setLine(0, Langues.getMessage("signs.unknown map"));
					else
						e.setLine(3, "Error");
				}
				else
					panneau = new Panneau(e.getBlock().getWorld(), e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), e.getLine(1));
				if (panneau != null)
				{
					e.setCancelled(true); // Sinon, l'évent remodifie le panneau après

					panneaux.add(panneau);
				}
			}
			else
				e.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("signs.error").replace("%type", e.getLine(1)));
			saveConf();
		}
	}

	private static int getIdMap(String ligne)
	{
		if (ligne.toLowerCase().startsWith("id:"))
		{
			try {
				return GameManager.getMap(Integer.valueOf(ligne.substring(3))).getId();
			} catch (NumberFormatException | NullPointerException e) {
				// Rien
			}
		}
		else
		{
			CPMap m = GameManager.getMap(ligne);
			if (m != null)
				return GameManager.getMap(ligne).getId();
		}
		return -1;
	}

	private static Panneau getPanneau(Block b)
	{
		for (int i=0; i < panneaux.size(); i++)
		{
			if (b.equals(panneaux.get(i).bloc))
				return panneaux.get(i);
		}
		return null;
	}

	private static void saveConf()
	{
		for (int i=0; i < panneaux.size(); i++)
		{
			Panneau p = panneaux.get(i);
			config.set(p.path + ".type", p.type.name());
			config.set(p.path + ".world", p.bloc.getWorld().getName());
			config.set(p.path + ".coords", p.getCoords());
			if (panneaux.get(i) instanceof PanneauMap)
			{
				CPMap m = ((PanneauMap) p).map;
				if (m == null)
					config.set(p.path, null); // Suppression
				else
				{
					config.set(p.path + ".map id", ((PanneauMap) p).map.getId());
					if (panneaux.get(i) instanceof PanneauClassement)
					{
						PanneauClassement pc = (PanneauClassement) p;
						config.set(p.path + ".show name", pc.afficherNom);
						config.set(p.path + ".limit", pc.borneInf);
					}
				}
			}
		}
		try {
			config.save(fichier);
		} catch (IOException e) {
			Bukkit.getLogger().warning("An error occurred while loading file 'lobby signs.yml'");
			e.printStackTrace();
		}
	}

	private static TypePanneau traduireType(String type)
	{
		if (type == null)
			return null;
		else if (type.equalsIgnoreCase("create") || type.equalsIgnoreCase(Langues.getMessage("signs.create")))
		{
			return TypePanneau.CREATE;
		}
		else if (type.equalsIgnoreCase("play") || type.equalsIgnoreCase(Langues.getMessage("signs.play")))
		{
			return TypePanneau.PLAY;
		}
		else if (type.equalsIgnoreCase("map") || type.equalsIgnoreCase(Langues.getMessage("signs.map")))
		{
			return TypePanneau.MAP;
		}
		else if (type.equalsIgnoreCase("leaderboard") || type.equalsIgnoreCase(Langues.getMessage("signs.leaderboard")))
		{
			return TypePanneau.LEADERBOARD;
		}
		return null;
	}

	static void supprimerMap(int id)
	{
		List<Panneau> panneauxASupprimer = new ArrayList<Panneau>();
		for (Panneau p : panneaux)
		{
			if (p instanceof PanneauMap && ((PanneauMap) p).map != null && ((PanneauMap) p).map.getId() == id)
				panneauxASupprimer.add(p);
		}
		for (Panneau p : panneauxASupprimer)
		{
			p.supprimer();
		}
		saveConf();
	}

	static void majClassements(CPMap m)
	{
		if (m != null)
		{
			for (Panneau p : panneaux)
			{
				if (p instanceof PanneauClassement)
				{
					PanneauClassement pc = (PanneauClassement) p;
					if (m.equals(pc.map))
						pc.maj();
				}
			}
		}
	}
	
	static Set<CPMap> getMapsAvecPanneauClassement()
	{
		Set<CPMap> liste = new HashSet<CPMap>();
		for (Panneau p : panneaux)
		{
			if (p instanceof PanneauClassement)
				liste.add(((PanneauClassement) p).map);
		}
		return liste;
	}


	//----------------------//
	//      Pas static      //
	//----------------------//

	enum TypePanneau { CREATE, PLAY, MAP, LEADERBOARD };

	private String path;
	protected Block bloc;
	protected TypePanneau type;

	protected Panneau(World monde, int x, int y, int z, String type)
	{
		this.type = traduireType(type);
		this.bloc = monde.getBlockAt(x, y, z);
		this.path = monde.getName() + ";" + getCoords();
		if (!(bloc.getState() instanceof Sign)) // Si le panneau n'existe plus
			this.supprimer();
		else if (this.type != TypePanneau.LEADERBOARD)
		{
			Sign panneau = ((Sign)this.bloc.getState());

			// écriture des trucs
			panneau.setLine(0, ChatColor.DARK_GREEN + CreativeParkour.getNom());
			if (this.type != TypePanneau.MAP)
			{
				if (this.type == TypePanneau.CREATE)
				{
					panneau.setLine(1, ChatColor.DARK_AQUA + Langues.getMessage("signs.create"));
					panneau.setLine(2, "");
					panneau.setLine(3, ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + Langues.getMessage("signs.right click"));			
				}
				else if (this.type == TypePanneau.PLAY)
				{
					panneau.setLine(1, ChatColor.DARK_AQUA + Langues.getMessage("signs.play"));
					panneau.setLine(2, "");
					panneau.setLine(3, ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + Langues.getMessage("signs.right click"));
				}
			}
			panneau.update();
		}
	}

	Panneau() {
		// So Panneau
		// Much method
		// Wow.
	}

	private String getCoords()
	{
		return bloc.getX() + ";" + bloc.getY() + ";" + bloc.getZ();
	}

	protected void supprimer()
	{
		config.set(path, null);
		panneaux.remove(this);
	}
}

class PanneauMap extends Panneau
{
	CPMap map;

	PanneauMap(World monde, int x, int y, int z, String type, int idMap)
	{
		super(monde, x, y, z, type);
		if (this.bloc.getState() instanceof Sign)
		{
			this.map = GameManager.getMap(idMap);

			Sign panneau = ((Sign)this.bloc.getState());
			if (map == null)
			{
				panneau.setLine(0, Langues.getMessage("signs.unknown map"));
				panneau.update();
				supprimer();
			}
			if (this.type == TypePanneau.MAP)
			{
				panneau.setLine(1, ChatColor.DARK_AQUA + Langues.getMessage("signs.map"));
				panneau.setLine(2, CPUtils.truncateStringEllipsis(this.map.getName(), 15));
				panneau.setLine(3, ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + Langues.getMessage("signs.right click"));
				panneau.update();
			}
		}
	}
}

class PanneauClassement extends PanneauMap
{
	boolean afficherNom;
	int borneInf;

	PanneauClassement(World monde, int x, int y, int z, String type, int idMap, boolean afficherNom, int borneInf)
	{
		super(monde, x, y, z, type, idMap);
		if (this.bloc.getState() instanceof Sign)
		{
			this.afficherNom = afficherNom;
			this.borneInf = borneInf;
			this.maj();
		}
	}

	void maj()
	{
		Sign panneau = ((Sign)this.bloc.getState());
		if (map == null)
			panneau.setLine(0, Langues.getMessage("signs.unknown map"));
		else
		{
			int ligne = 0;
			if (afficherNom)
			{
				panneau.setLine(ligne, ChatColor.DARK_GREEN + CPUtils.truncateStringEllipsis(map.getName(), 15));
				ligne++;
			}

			SortedSet<CPTime> listeTemps = map.getListeTemps();
			int i = 0;
			Iterator<CPTime> it = listeTemps.iterator();
			while (it.hasNext() && ligne < 4)
			{
				CPTime t = it.next();
				if (i >= borneInf-1)
				{
					String temps = String.valueOf((int) t.inSeconds());
					String joueur = CPUtils.playerScoreboardName(t.playerUUID.toString());
					panneau.setLine(ligne, CPUtils.truncatedStr(ChatColor.DARK_AQUA + temps + ": " + ChatColor.RESET + joueur, 20));
					ligne++;
				}
				i++;
			}

			// Ecriture de lignes vides sur celles qui restent
			for (; ligne < 4; ligne++)
			{
				panneau.setLine(ligne, ChatColor.DARK_GRAY + "---");
			}
		}

		panneau.update();
	}
}