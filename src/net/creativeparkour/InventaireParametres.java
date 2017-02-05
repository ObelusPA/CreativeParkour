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


class InventaireParametres
{
	private Map<Integer, PlayerSetting> objets = new Hashtable<Integer, PlayerSetting>(); // L'entier est le slot
	private Inventory inv;
	private Joueur j;

	InventaireParametres (Joueur j)
	{
		this.j = j;
	}

	void remplir()
	{
		Player p = j.getPlayer();

		if (inv == null)
			inv = Bukkit.createInventory(p, 45, Langues.getMessage("commands.settings title"));
		else
		{
			inv.clear();
		}

		setCoupleBool(PlayerSetting.NOTIFICATIONS, CreativeParkour.auMoins1_9() ? 2 : 3, Material.NAME_TAG, (short) 0, Langues.getMessage("commands.settings notifications"), Langues.getMessage("commands.settings notifications info"));
		setCoupleBool(PlayerSetting.MSG_REWARD, CreativeParkour.auMoins1_9() ? 4 : 5, Material.CHEST, (short) 0, Langues.getMessage("commands.settings reward msg"), Langues.getMessage("commands.settings reward msg info"));
		if (CreativeParkour.auMoins1_9())
			setCoupleBool(PlayerSetting.MSG_ELYTRES, 6, Material.ELYTRA, (short) 0, Langues.getMessage("commands.settings elytra msg"), Langues.getMessage("commands.settings elytra msg info"));
		setCoupleBool(PlayerSetting.MESSAGES_CP_SPEC, 32, Material.STAINED_GLASS, (short) 10, Langues.getMessage("commands.settings checkpoint msg spec"), Langues.getMessage("commands.settings checkpoint msg spec info"));

		// Truc spécial pour MESSAGES_CP :
		TypeMessage tm = j.typeMessages();
		setObjetInfo(30, Material.WOOL, (short) 10, Langues.getMessage("commands.settings checkpoint msg"), Langues.getMessage("commands.settings checkpoint msg info"));
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) (tm != TypeMessage.NONE ? 10 : 8));
		ItemMeta meta = item.getItemMeta();
		String name = ChatColor.GREEN + Langues.getMessage("commands.settings checkpoint msg full");
		if (tm == TypeMessage.REDUCED)
			name = ChatColor.GREEN + Langues.getMessage("commands.settings checkpoint msg reduced");
		else if (tm == TypeMessage.NONE)
			name = ChatColor.RED + Langues.getMessage("commands.settings checkpoint msg none");
		meta.setDisplayName(name);
		meta.setLore(CPUtils.divideText(Langues.getMessage("commands.settings checkpoint msg click"), ChatColor.YELLOW));
		item.setItemMeta(meta);
		inv.setItem(39, item);
		objets.put(39, PlayerSetting.MESSAGES_CP);

	}

	private void setCoupleBool(PlayerSetting param, int posInv, Material mat, short data, String nom, String descr)
	{
		setObjetInfo(posInv, mat, data, nom, descr);
		setObjetParam(posInv + 9, param);
	}

	private void setObjetInfo(int posInv, Material mat, short data, String nom, String descr)
	{
		ItemStack item = new ItemStack(mat, 1, data);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + nom);
		meta.setLore(CPUtils.divideText(descr, null));
		item.setItemMeta(meta);
		inv.setItem(posInv, item);
	}

	private void setObjetParam(int posInv, PlayerSetting param)
	{
		boolean valeur = j.getParamBool(param);
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
		objets.put(posInv, param);
	}

	Inventory getInventaire()
	{
		return inv;
	}

	void clic(int slot) throws NoSuchMethodException, SecurityException
	{
		PlayerSetting ps = objets.get(slot);
		if (ps != null)
		{
			if (ps == PlayerSetting.NOTIFICATIONS || 
					ps == PlayerSetting.MSG_REWARD || 
					ps == PlayerSetting.MSG_ELYTRES || 
					ps == PlayerSetting.MESSAGES_CP_SPEC)
			{
				j.inverserParam(ps);
				remplir();
			}
			else if (ps == PlayerSetting.MESSAGES_CP)
			{
				j.getPlayer().closeInventory();
				j.configurerMessages();
			}
		}
	}
}