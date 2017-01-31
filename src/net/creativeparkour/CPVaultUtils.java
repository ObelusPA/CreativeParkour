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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.item.Items;

/**
 * Contains useful methods related to the Vault API.
 * @author Obelus
 */
public class CPVaultUtils
{
    private static Economy economy = null;
    
	/**
	 * Return the Vault name of the given item.
	 * @param material Item's Bukkit {@code Material}.
	 * @param data Item's data value.
	 * @return Item's Vault name.
	 */
	public static String getItemName(Material material, short data)
	{
		return Items.itemByType(material, data).getName();
	}
	
	/**
	 * Returns the Bukkit {@code Material} corresponding to the given item name.
	 * @param itemName Vault item name.
	 * @return Corresponding {@code Material}.
	 */
	public static Material getMaterial(String itemName)
	{
		return Items.itemByString(itemName).getType();
	}
	
	/**
	 * Gives money to a player via the Vault API.
	 * @param player Player we want to give money.
	 * @param amount Money amount we want to give.
	 * @return {@code true} if the transaction was successful.
	 */
	public static boolean giveMoney(OfflinePlayer player, int amount)
	{
		if (economy == null)
			setupEconomy();
		return economy.depositPlayer(player, Math.abs(amount)).transactionSuccess();
	}
	
	private static boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
}
