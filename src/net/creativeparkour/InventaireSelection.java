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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;


class InventaireSelection
{
	private enum ActionInv { PAGE_PRECEDENTE, PAGE_SUIVANTE, TRI_ALPHABETIQUE, TRI_CREATEUR, TRI_DIFFICULTE, TRIER_CROISSANT, TRIER_DECROISSANT, AFFICHER_TELECHARGEABLES, MASQUER_TELECHARGEABLES, AFFICHER_LOCALES, MASQUER_LOCALES };

	private Map<Integer, CPMap> elements = new Hashtable<Integer, CPMap>();
	private Map<Integer, ActionInv> speciaux = new Hashtable<Integer, ActionInv>();
	private List<CPMap> maps;
	List<CPMap> mapsAffichees = new ArrayList<CPMap>();
	private Comparator<CPMap> comparator;
	private boolean afficherTelechargeables;
	private boolean afficherLocales;
	private int triCroissant = 1;
	private int page = 1;
	private Inventory inv;
	private final int tailleInv = 54;
	private Player p;

	InventaireSelection (List<CPMap> listeMaps, Player p)
	{
		this.comparator = new Comparator<CPMap>() {
			@Override public int compare(CPMap m1, CPMap m2)
			{
				int c = comparerEpingle(m1, m2);
				if (c != 0) return c;
				else return triCroissant * Integer.compare(m1.getId(), m2.getId());
			} 
		};
		this.maps = listeMaps;
		this.afficherTelechargeables = Config.getConfig().getBoolean("online.show downloadable maps") && p.hasPermission("creativeparkour.download");
		this.afficherLocales = true;
		this.p = p;
		this.inv = Bukkit.createInventory(p, tailleInv, Langues.getMessage("play.title"));
		if (afficherLocales)
			mapsAffichees.addAll(maps);
		if (!Config.online())
			GameManager.mapsTelechargeables.clear();
		if (afficherTelechargeables)
			mapsAffichees.addAll(GameManager.mapsTelechargeables);
		trier();
	}

	void trier()
	{
		Collections.sort(mapsAffichees, comparator);
	}

	void mettreAJourTelechargeables()
	{
		if (afficherTelechargeables)
		{
			mapsAffichees.clear();
			if (afficherLocales)
				mapsAffichees.addAll(maps);
			mapsAffichees.addAll(GameManager.mapsTelechargeables);
			trier();
			setPage(page);
		}
	}

	void setPage(int page)
	{
//		long nano = System.nanoTime();
		this.page = page;
		final int nbMaps = mapsAffichees.size();
//		nano = Util.debugNanoTime("IS1", nano);

		// Calcul du nombre de pages
		int nbPages = (int) (Math.ceil((double)nbMaps/45));
		if (nbPages == 0)
			nbPages = 1;
		if (page > nbPages)
			page = nbPages;
		final int limiteMin = 45 * (page-1);
		final int limiteMax = (45 * page > nbMaps) ? nbMaps : 45 * page;

		if (inv == null)
			inv = Bukkit.createInventory(p, tailleInv, Langues.getMessage("play.title"));
//		nano = Util.debugNanoTime("IS2", nano);

		final Map<Integer, ItemStack> itemsAMettre = new HashMap<Integer, ItemStack>();
		final int pageF = page;
		final int nbPagesF = nbPages;
		Bukkit.getScheduler().runTaskAsynchronously(CreativeParkour.getPlugin(), new Runnable() {
			public void run() {
				boolean continuer = true;
				int nbItems = 0;
				elements.clear();
				speciaux.clear();
//				nano = Util.debugNanoTime("IS3", nano);
				int slot = 0;
				//		long nano2 = System.nanoTime();
				for (int i=limiteMin; continuer && i < nbMaps; i++)
				{
					//			nano2 = Util.debugNanoTime("IS4a", nano2);
					CPMap m = mapsAffichees.get(i);
					ItemStack item = new ItemStack(Material.MAP);
					ItemMeta meta = item.getItemMeta();
					String gras = "";
					if (m.isPinned())
						gras += ChatColor.BOLD;
					meta.setDisplayName(ChatColor.AQUA + gras + m.getName());

					List<String> lore = new ArrayList<String>();
					float difficulte = m.getDifficulty();
					if (Config.getConfig().getBoolean("game.enable map rating") && difficulte > 0)
					{
						ChatColor couleur = ChatColor.WHITE;
						if (difficulte >= 1.5)
							couleur = ChatColor.GREEN;
						if (difficulte >= 2.5)
							couleur = ChatColor.YELLOW;
						if (difficulte >= 3.5)
							couleur = ChatColor.RED;
						if (difficulte >= 4.5)
							couleur = ChatColor.DARK_RED;
						String difficulteS = CPUtils.truncatedStr(String.valueOf(difficulte), 3);
						if (difficulteS.endsWith(".0"))
							difficulteS = difficulteS.substring(0, 1);
						lore.add(ChatColor.WHITE + Langues.getMessage("play.difficulty") + ": " + couleur + difficulteS + "/5");
					}
					String nomCreateur = null;
					if (m.getDonneesWeb() == null) // Si est une map locale
						nomCreateur = NameManager.getNomAvecUUID(m.getCreator());
					else // Si c'est une map téléchargeable
						nomCreateur = m.getDonneesWeb().get("createur");
					if (nomCreateur == null)
						nomCreateur = "Unknown";
					lore.add(ChatColor.LIGHT_PURPLE + nomCreateur);
					//			nano2 = Util.debugNanoTime("IS4b", nano2);
					for (UUID u : m.getContributeurs())
					{
						lore.add(ChatColor.DARK_PURPLE + NameManager.getNomAvecUUID(u));
					}
					if (m.getDonneesWeb() == null)
					{
						//				nano2 = Util.debugNanoTime("IS4c", nano2);
						// Recherche du record du joueur
						if (Config.getConfig().getBoolean("map selection.display records"))
						{
							float secondes = 0;
							if (m.listeTempsDispo()) // Si la liste des temps de la map est déjà chargé, on prend dedans
							{
								CPTime t = m.getTime(p.getUniqueId());
								if (t != null)
									secondes = t.inSeconds();
							}
							else
							{
								YamlConfiguration record = CPUtils.getYML(GameManager.getFichierTemps(m.getUUID().toString() + "_" + p.getUniqueId().toString()));
								if (record != null)
									secondes = (float) (record.getInt("ticks") / 20.0);
							}
							//					nano2 = Util.debugNanoTime("IS4e", nano2);
							if (secondes > 0)
							{
								lore.add(ChatColor.YELLOW + Langues.getMessage("play.your record") + ": " + secondes + "s");
							}
						}

						if (m.getState() == CPMapState.DOWNLOADED)
							lore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + Langues.getMessage("play.downloaded"));
						if (m.isPinned() && p.hasPermission("creativeparkour.manage"))
							lore.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + Langues.getMessage("pinned"));
						//				nano2 = Util.debugNanoTime("IS4f", nano2);
					}
					else
						lore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + Langues.getMessage("play.downloadable"));
					meta.setLore(lore);
					item.setItemMeta(meta);
					itemsAMettre.put(slot, item);
					elements.put(slot, m);
					slot++;
					nbItems++;

					if (nbItems >= limiteMax || slot >= 45)  
						continuer = false;
					//			nano2 = Util.debugNanoTime("IS4g", nano2);
				}
//				nano = Util.debugNanoTime("IS4", nano);

				if (pageF > 1)
				{
					ItemStack item = new ItemStack(Material.SLIME_BALL);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(ChatColor.GREEN + Langues.getMessage("play.page") + " " + (pageF - 1));
					item.setItemMeta(meta);
					itemsAMettre.put(tailleInv - 9, item);
					speciaux.put(tailleInv - 9, ActionInv.PAGE_PRECEDENTE);
				}
				if (pageF < nbPagesF)
				{
					ItemStack item = new ItemStack(Material.ENDER_PEARL);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(ChatColor.GREEN + Langues.getMessage("play.page") + " " + (pageF + 1));
					item.setItemMeta(meta);
					itemsAMettre.put(tailleInv - 1, item);
					speciaux.put(tailleInv - 1, ActionInv.PAGE_SUIVANTE);
				}
				ItemStack item = new ItemStack(Material.EYE_OF_ENDER);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.GREEN + Langues.getMessage("play.page") + " " + pageF + "/" + nbPagesF);
				List<String> lore = new ArrayList<String>();
				lore.add(nbMaps + " " + Langues.getMessage("play.maps"));
				meta.setLore(lore);
				item.setItemMeta(meta);
				itemsAMettre.put(tailleInv - 5, item);

				// Objets d'affichage
				short d = 13;
				String texte = Langues.getMessage("play.descending sorting");
				if (triCroissant == -1)
				{
					d = 9;
					texte = Langues.getMessage("play.ascending sorting");
				}
				item = new ItemStack(Material.INK_SACK, 1, d);
				meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.YELLOW + texte);
				item.setItemMeta(meta);
				itemsAMettre.put(tailleInv - 6, item);
				ActionInv a = ActionInv.TRIER_DECROISSANT;
				if (triCroissant == -1)
					a = ActionInv.TRIER_CROISSANT;
				speciaux.put(tailleInv - 6, a);

				if (Config.online() && p.hasPermission("creativeparkour.download"))
				{
					d = 8;
					texte = Langues.getMessage("play.show local");
					if (afficherLocales)
					{
						d = 10;
						texte = Langues.getMessage("play.hide local");
					}
					item = new ItemStack(Material.INK_SACK, 1, d);
					meta = item.getItemMeta();
					meta.setDisplayName(ChatColor.YELLOW + texte);
					item.setItemMeta(meta);
					itemsAMettre.put(tailleInv - 7, item);
					a = ActionInv.AFFICHER_LOCALES;
					if (afficherLocales)
						a = ActionInv.MASQUER_LOCALES;
					speciaux.put(tailleInv - 7, a);

					d = 8;
					texte = Langues.getMessage("play.show downloadable");
					if (afficherTelechargeables)
					{
						d = 10;
						texte = Langues.getMessage("play.hide downloadable");
					}
					item = new ItemStack(Material.INK_SACK, 1, d);
					meta = item.getItemMeta();
					meta.setDisplayName(ChatColor.YELLOW+ texte);
					item.setItemMeta(meta);
					itemsAMettre.put(tailleInv - 8, item);
					a = ActionInv.AFFICHER_TELECHARGEABLES;
					if (afficherTelechargeables)
						a = ActionInv.MASQUER_TELECHARGEABLES;
					speciaux.put(tailleInv - 8, a);
				}

				// Objets de tri
				item = new ItemStack(Material.INK_SACK, 1, (short) 5);
				meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.LIGHT_PURPLE + Langues.getMessage("play.sort creator"));
				item.setItemMeta(meta);
				itemsAMettre.put(tailleInv - 3, item);
				speciaux.put(tailleInv - 3, ActionInv.TRI_CREATEUR);

				item = new ItemStack(Material.INK_SACK, 1, (short) 5);
				meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.LIGHT_PURPLE + Langues.getMessage("play.sort name"));
				item.setItemMeta(meta);
				itemsAMettre.put(tailleInv - 4, item);
				speciaux.put(tailleInv - 4, ActionInv.TRI_ALPHABETIQUE);

				if (Config.getConfig().getBoolean("game.enable map rating"))
				{
					item = new ItemStack(Material.INK_SACK, 1, (short) 5);
					meta = item.getItemMeta();
					meta.setDisplayName(ChatColor.LIGHT_PURPLE + Langues.getMessage("play.sort difficulty"));
					item.setItemMeta(meta);
					itemsAMettre.put(tailleInv - 2, item);
					speciaux.put(tailleInv - 2, ActionInv.TRI_DIFFICULTE);
				}
//				nano = Util.debugNanoTime("IS5", nano);
				
				// Remplissage de l'inventaire avec les objets
				Bukkit.getScheduler().scheduleSyncDelayedTask(CreativeParkour.getPlugin(), new Runnable() {
					public void run() {
						inv.clear();
						for (Entry<Integer, ItemStack> e : itemsAMettre.entrySet())
						{
							inv.setItem(e.getKey(), e.getValue());
						}
					}
				});
			}
		});
	}

	Inventory getInventaire()
	{
		return inv;
	}

	void clic(int slot, ClickType clickType) throws NoSuchMethodException, SecurityException
	{
		CPMap m = elements.get(slot);
		ActionInv action = speciaux.get(slot);
		if (m != null)
		{
			if (m.getDonneesWeb() == null)
				GameManager.jouer(p, m, false, true);
			else
			{
				p.closeInventory();
				if (clickType == ClickType.RIGHT)
					CPUtils.sendClickableMsg(p, Langues.getMessage("play.download map info"), null, CreativeParkour.lienSite() + "/map.php?id=" + m.getDonneesWeb().get("id"), "%L", ChatColor.YELLOW);
				else
					GameManager.telechargerMap(p, m.getDonneesWeb().get("id"));
			}
		}
		else if (action != null)
		{
			if (action == ActionInv.PAGE_PRECEDENTE)
				setPage(page - 1);
			else if (action == ActionInv.PAGE_SUIVANTE)
				setPage(page + 1);
			else if (action == ActionInv.TRI_ALPHABETIQUE)
			{
				comparator = new Comparator<CPMap>() {
					@Override public int compare(CPMap m1, CPMap m2)
					{
						return triCroissant * m1.getName().toLowerCase().compareTo(m2.getName().toLowerCase());
					} 
				};
				trier();
				setPage(1);
			}
			else if (action == ActionInv.TRI_CREATEUR)
			{
				comparator = new Comparator<CPMap>() {
					@Override public int compare(CPMap m1, CPMap m2)
					{
						String s1;
						String s2;
						if (m1.getDonneesWeb() != null)
							s1 = m1.getDonneesWeb().get("createur");
						else
							s1 = NameManager.getNomAvecUUID(m1.getCreator());

						if (m2.getDonneesWeb() != null)
							s2 = m2.getDonneesWeb().get("createur");
						else
							s2 = NameManager.getNomAvecUUID(m2.getCreator());
						if (s1 == null)
							s1 = "Unknown";
						if (s2 == null)
							s2 = "Unknown";
						return triCroissant * s1.compareTo(s2);
					} 
				};
				trier();
				setPage(1);
			}
			else if (action == ActionInv.TRI_DIFFICULTE)
			{
				comparator = new Comparator<CPMap>() {
					@Override public int compare(CPMap m1, CPMap m2)
					{
						float d1 = m1.getDifficulty();
						float d2 = m2.getDifficulty();
						if (d1 > 0 && d2 <= 0)
							return -1;
						else if (d1 <= 0 && d2 > 0)
							return 1;
						else if (d1 < 0 && d2 < 0)
							return 0;
						return triCroissant * Float.compare(d1, d2);
					} 
				};
				trier();
				setPage(1);
			}
			else if (action == ActionInv.AFFICHER_TELECHARGEABLES)
			{
				afficherTelechargeables = true;
				if (!Config.online())
					GameManager.mapsTelechargeables.clear();
				else
					mapsAffichees.addAll(GameManager.mapsTelechargeables);
				trier();
				setPage(page);
			}
			else if (action == ActionInv.MASQUER_TELECHARGEABLES)
			{
				afficherTelechargeables = false;
				mapsAffichees.removeAll(GameManager.mapsTelechargeables);
				setPage(page);
			}
			else if (action == ActionInv.AFFICHER_LOCALES)
			{
				afficherLocales = true;
				mapsAffichees.addAll(maps);
				trier();
				setPage(page);
			}
			else if (action == ActionInv.MASQUER_LOCALES)
			{
				afficherLocales = false;
				mapsAffichees.removeAll(maps);
				setPage(page);
			}
			else if (action == ActionInv.TRIER_CROISSANT)
			{
				triCroissant = 1;
				trier();
				setPage(page);
			}
			else if (action == ActionInv.TRIER_DECROISSANT)
			{
				triCroissant = -1;
				trier();
				setPage(page);
			}
		}
	}

	private int comparerEpingle(CPMap m1, CPMap m2)
	{
		if (m1.isPinned() && !m2.isPinned())
			return triCroissant * -1;
		else if (!m1.isPinned() && m2.isPinned())
			return triCroissant * 1;
		else
			return 0;
	}
}
