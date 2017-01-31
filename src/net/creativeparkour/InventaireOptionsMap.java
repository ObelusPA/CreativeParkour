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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;


class InventaireOptionsMap
{
	private enum ActionInv { INVERSER_SNEAK, INVERSER_LAVE, INVERSER_EAU, INVERSER_INTERACTIONS };

	private Map<Integer, ActionInv> objets = new Hashtable<Integer, ActionInv>(); // L'entier est le slot
	private CPMap map;
	private Inventory inv;
	private Joueur j;

	InventaireOptionsMap (CPMap map, Joueur j)
	{
		this.map = map;
		this.j = j;
	}

	void remplir()
	{
		Player p = j.getPlayer();

		if (inv == null)
			inv = Bukkit.createInventory(p, 36, Langues.getMessage("commands.mapoptions title"));
		else
		{
			inv.clear();
		}

		setObjetInfo(10, Material.WEB, Langues.getMessage("commands.mapoptions sneak"), Langues.getMessage("commands.mapoptions sneak info"));
		setObjetSelection(19, map.sneakAutorise, ActionInv.INVERSER_SNEAK);

		setObjetInfo(12, Material.LAVA_BUCKET, Langues.getMessage("commands.mapoptions lava"), Langues.getMessage("commands.mapoptions lava info"));
		setObjetSelection(21, map.mortLave, ActionInv.INVERSER_LAVE);

		setObjetInfo(14, Material.WATER_BUCKET, Langues.getMessage("commands.mapoptions water"), Langues.getMessage("commands.mapoptions water info"));
		setObjetSelection(23, map.mortEau, ActionInv.INVERSER_EAU);
		
		setObjetInfo(16, Material.WOOD_DOOR, Langues.getMessage("commands.mapoptions interactions"), Langues.getMessage("commands.mapoptions interactions info"));
		setObjetSelection(25, map.interactionsAutorisees, ActionInv.INVERSER_INTERACTIONS);
	}

	private void setObjetInfo(int posInv, Material mat, String nom, String descr)
	{
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + nom);
		meta.setLore(CPUtils.diviserTexte(descr, null));
		item.setItemMeta(meta);
		inv.setItem(posInv, item);
	}

	private void setObjetSelection(int posInv, boolean valeur, ActionInv action)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) (valeur ? 10 : 8));
		ItemMeta meta = item.getItemMeta();
		String name = ChatColor.GREEN + Langues.getMessage("commands.settings enabled");
		String msgLore = ChatColor.WHITE + Langues.getMessage("commands.settings disable");
		if (!valeur)
		{
			name = ChatColor.RED + Langues.getMessage("commands.settings disabled");
			msgLore = ChatColor.WHITE + Langues.getMessage("commands.settings enable");
		}
		meta.setDisplayName(name);
		List<String> lore = new ArrayList<String>();
		lore.add(msgLore);
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
			if (map.estEnTest())
			{
				j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.mapoptions error"));
				j.getPlayer().closeInventory();
			}
			else
			{
				if (action == ActionInv.INVERSER_SNEAK)
				{
					map.sneakAutorise = !map.sneakAutorise;
				}
				else if (action == ActionInv.INVERSER_LAVE)
				{
					map.mortLave = !map.mortLave;
				}
				else if (action == ActionInv.INVERSER_EAU)
				{
					map.mortEau = !map.mortEau;
				}
				else if (action == ActionInv.INVERSER_INTERACTIONS)
				{
					map.interactionsAutorisees = !map.interactionsAutorisees;
				}
				map.setValide(false);
				map.sauvegarder();
				j.getPlayer().setScoreboard(map.getScoreboardC());
				remplir();
			}
		}
	}
}