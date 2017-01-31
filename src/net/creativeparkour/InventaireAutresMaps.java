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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;


class InventaireAutresMaps
{
	private enum ActionInv { PAGE_PRECEDENTE, PAGE_SUIVANTE};

	private Map<Integer, CPMap> elements = new Hashtable<Integer, CPMap>();
	private Map<Integer, ActionInv> speciaux = new Hashtable<Integer, ActionInv>();
	private List<CPMap> maps;
	private int page = 1;
	private Inventory inv;
	private final int tailleInv = 54;
	private Player p;

	InventaireAutresMaps (List<CPMap> listeMaps, Player p)
	{
		this.maps = listeMaps;
		Collections.sort(maps, new Comparator<CPMap>() {
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
				return s1.compareTo(s2);
			} 
		});
		this.p = p;
		this.inv = Bukkit.createInventory(p, tailleInv, Langues.getMessage("creation.other maps"));
	}

	void setPage(int page)
	{
		this.page = page;
		int nbMaps = maps.size();

		// Calcul du nombre de pages
		int nbPages = (int) (Math.ceil((double)nbMaps/45));
		if (nbPages == 0)
			nbPages = 1;
		if (page > nbPages)
			page = nbPages;
		int limiteMin = 45 * (page-1);
		int limiteMax = 45 * page;
		if (limiteMax > nbMaps) limiteMax = nbMaps;

		if (inv == null)
			inv = Bukkit.createInventory(p, tailleInv, Langues.getMessage("creation.other maps"));
		else
		{
			inv.clear();
		}

		boolean continuer = true;
		int nbItems = 0;
		elements.clear();
		int slot = 0;
		for (int i=limiteMin; continuer && i < nbMaps; i++)
		{
			CPMap m = maps.get(i);
			ItemStack item = new ItemStack(Material.MAP);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.YELLOW + Langues.getMessage("creation.other maps creator").replace("%creator", NameManager.getNomAvecUUID(m.getCreator())));
			item.setItemMeta(meta);
			inv.addItem(item);
			elements.put(slot, m);
			slot++;
			nbItems++;

			if (nbItems == limiteMax || slot >= 45)
				continuer = false;
		}

		if (page > 1)
		{
			setObjetSpecial(tailleInv - 9, Material.SLIME_BALL, ChatColor.GREEN + Langues.getMessage("play.page") + " " + (page - 1), null, ActionInv.PAGE_PRECEDENTE);
		}
		if (page < nbPages)
		{
			setObjetSpecial(tailleInv - 1, Material.ENDER_PEARL, ChatColor.GREEN + Langues.getMessage("play.page") + " " + (page + 1), null, ActionInv.PAGE_SUIVANTE);
		}
		ItemStack item = new ItemStack(Material.EYE_OF_ENDER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + Langues.getMessage("play.page") + " " + page + "/" + nbPages);
		List<String> lore = new ArrayList<String>();
		lore.add(nbMaps + " " + Langues.getMessage("play.maps"));
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(tailleInv - 5, item);
	}

	private void setObjetSpecial(int posInv, Material material, String nom, List<String> lore, ActionInv action)
	{
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(nom);
		if (lore != null)
			meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(posInv, item);
		speciaux.put(posInv, action);
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
			GameManager.teleporterCreation(p, m, false);
		}
		else if (action != null)
		{
			if (action == ActionInv.PAGE_PRECEDENTE)
				setPage(page - 1);
			else if (action == ActionInv.PAGE_SUIVANTE)
				setPage(page + 1);
		}
	}
}