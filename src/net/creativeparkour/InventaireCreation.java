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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;


class InventaireCreation
{
	private enum ActionInv { NOUVELLE_MAP, CHARGER_MAP, AUTRES_MAPS };

	private Map<Integer, ActionInv> objets = new Hashtable<Integer, ActionInv>();
	private Inventory inv;
	private Joueur j;
	private CPMap autreMap;

	InventaireCreation (Joueur j)
	{
		this.j = j;
		this.autreMap = GameManager.getMapEnCreation(j.getPlayer());
	}

	void remplir()
	{
		if (inv == null)
			inv = Bukkit.createInventory(j.getPlayer(), 45, Langues.getMessage("creation.title"));
		else
		{
			inv.clear();
		}

		List<String> lore = null;
		if (autreMap != null)
		{
			lore = CPUtils.diviserTexte(Langues.getMessage("creation.new map warn"), ChatColor.RED);
		}
		setObjet(11, Material.EMPTY_MAP, ChatColor.GREEN + Langues.getMessage("creation.new map"), lore, ActionInv.NOUVELLE_MAP);

		setObjet(15, Material.MAP, ChatColor.AQUA + Langues.getMessage("creation.load map"), null, ActionInv.CHARGER_MAP);

		setObjet(31, Material.PAPER, ChatColor.YELLOW + Langues.getMessage("creation.other maps"), null, ActionInv.AUTRES_MAPS);
	}

	private void setObjet(int posInv, Material material, String nom, List<String> lore, ActionInv action)
	{
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(nom);
		if (lore != null)
			meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(posInv, item);
		objets.put(posInv, action);
	}

	Inventory getInventaire()
	{
		return inv;
	}

	void clic(int slot) throws NoSuchMethodException, SecurityException
	{
		ActionInv action = objets.get(slot);
		if (action != null)
		{
			if (action == ActionInv.NOUVELLE_MAP)
			{
				j.getPlayer().closeInventory();

				if (autreMap != null)
				{
					Commandes.question(j.getPlayer(), Langues.getMessage("creation.erase question"),"écraser map");
				}
				else
				{
					GameManager.nouvelleMap(j.getPlayer(), true, null);
				}
			}
			else if (action == ActionInv.CHARGER_MAP)
			{
				j.getPlayer().closeInventory();
				if (autreMap == null)
				{
					j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.no map"));
				}
				else
				{
					GameManager.teleporterCreation(j.getPlayer(), autreMap, false);
				}
			}
			else if (action == ActionInv.AUTRES_MAPS)
			{
				j.getPlayer().closeInventory();
				j.invAutresMaps = new InventaireAutresMaps(GameManager.getMapsContributeur(j.getPlayer()), j.getPlayer());
				j.getPlayer().openInventory(j.invAutresMaps.getInventaire());
				j.invAutresMaps.setPage(1);
			}
		}
	}
}
