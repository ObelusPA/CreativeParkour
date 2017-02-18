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
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

class Help
{
	private static List<String> h = new ArrayList<String>();
	private static List<String> hop = new ArrayList<String>();
	
	private static ChatColor reset = ChatColor.RESET;
	private static ChatColor bold = ChatColor.BOLD;
	private static ChatColor italic = ChatColor.ITALIC;
	private static ChatColor yellow = ChatColor.YELLOW;
	private static ChatColor gold = ChatColor.GOLD;
	private static ChatColor d_green = ChatColor.DARK_GREEN;
	private static ChatColor green = ChatColor.GREEN;
	
	private static String trans = reset + " : " + italic;
	
	static void enable()
	{
		h.clear();
		hop.clear();
		
		h.add(gold + "/cp " + Langues.getMessage("help.title").toLowerCase() + trans + Langues.getMessage("help.help"));
		h.add(gold + "/cp " + Langues.getMessage("commands.play") + " [" +Langues.getMessage("commands.map name") + "]" + trans + Langues.getMessage("help.play"));
		h.add(gold + "/cp " + Langues.getMessage("commands.download") + " <map ID>" + trans + Langues.getMessage("help.download"));
		h.add(gold + "/cp " + Langues.getMessage("commands.create") + trans + Langues.getMessage("help.create"));
		h.add(gold + "/cp " + Langues.getMessage("commands.test") + trans + Langues.getMessage("help.test"));
		h.add(gold + "/cp " + Langues.getMessage("commands.invite") + " <" +Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.invite"));
		h.add(gold + "/cp " + Langues.getMessage("commands.remove") + " <" +Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.remove"));
		h.add(gold + "/cp " + Langues.getMessage("commands.contributors") + trans + Langues.getMessage("help.contributors"));
		h.add(gold + "/cp " + Langues.getMessage("commands.settings") + trans + Langues.getMessage("help.settings"));
		h.add(gold + "/cp " + Langues.getMessage("commands.tp") + " <" +Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.tp"));
		h.add(gold + "/cp " + Langues.getMessage("commands.spectator") + trans + Langues.getMessage("help.spectator"));
		h.add(gold + "/cp " + Langues.getMessage("commands.leave") + trans + Langues.getMessage("help.leave"));
		h.add(gold + "/cp " + Langues.getMessage("commands.publish") + trans + Langues.getMessage("help.publish"));
		h.add(gold + "/cp " + Langues.getMessage("commands.mapoptions") + trans + Langues.getMessage("help.mapoptions"));
		h.add(gold + "/cp " + Langues.getMessage("commands.importsel") + trans + Langues.getMessage("help.importsel"));
		h.add(gold + "/cp " + Langues.getMessage("commands.claim") + trans + Langues.getMessage("help.claim"));
		h.add(gold + "/cp " + Langues.getMessage("commands.pin") + trans + Langues.getMessage("help.pin"));
		h.add(gold + "/cp " + Langues.getMessage("commands.unpin") + trans + Langues.getMessage("help.unpin"));
		h.add(gold + "/cp " + Langues.getMessage("commands.share") + trans + Langues.getMessage("help.share"));
		h.add(gold + "/cp " + Langues.getMessage("commands.export") + trans + Langues.getMessage("help.export"));
		h.add(gold + "/cp " + Langues.getMessage("commands.edit") + trans + Langues.getMessage("help.edit"));
		h.add(gold + "/cp " + Langues.getMessage("commands.delete") + trans + Langues.getMessage("help.delete"));
		h.add(gold + "/cp " + Langues.getMessage("commands.register") + trans + Langues.getMessage("help.register"));
		h.add(gold + "/cp " + Langues.getMessage("commands.sync") + trans + Langues.getMessage("help.sync"));
		h.add(gold + "/cp " + Langues.getMessage("commands.managemaps") + trans + Langues.getMessage("help.managemaps"));
		h.add(gold + "/cp " + Langues.getMessage("commands.getid") + " [map name]" + trans + Langues.getMessage("help.getid"));
		h.add(gold + "/cp " + Langues.getMessage("commands.ban") + " <" +Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.ban"));
		h.add(gold + "/cp " + Langues.getMessage("commands.pardon") + " <" +Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.pardon"));
		h.add(gold + "/cp " + Langues.getMessage("commands.removetime") + " <" +Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.removetime"));
		
		hop.add(yellow + "/cp config" + trans + Langues.getMessage("help.config"));
		hop.add(yellow + "/cp language <language code>" + trans + Langues.getMessage("help.language"));
		hop.add(yellow + "/cp enable" + trans + Langues.getMessage("help.enable"));
		hop.add(yellow + "/cp disable" + trans + Langues.getMessage("help.disable"));
	}
	
	static void sendHelp(CommandSender sender, int page)
	{
		int nbLignes = h.size();
		final int commandesParPage = 7;
		
		if (sender instanceof Player)
		{
			if (sender.hasPermission("creativeparkour.*"))
			{
				nbLignes = nbLignes + hop.size();
			}
		}
		
		int pageNb = 1;
		double nbPages = Math.ceil((float)nbLignes/commandesParPage);
		if (page > 1 && page <= nbPages)
		{
			pageNb = page;
		}
		
		sender.sendMessage(gold + "------ " + d_green  + bold + CreativeParkour.getNom() + " \u2022 " + Langues.getMessage("help.title") + gold + " ------");
		sender.sendMessage(italic + "" + green + Langues.getMessage("help.page") + " " + pageNb + "/"+ (int) nbPages + reset + green + " - " + Langues.getMessage("help.page command"));
		
		int l = (pageNb-1) * commandesParPage;
		int lMax = l + commandesParPage;
		if (pageNb == nbPages)
		{
			lMax = (int) (l + (nbLignes-l));
		}
		for (int i=l; i < lMax; i++)
		{
			if (i < h.size())
			{
				sender.sendMessage(h.get(i));
			}
			else if (sender.hasPermission("creativeparkour.*"))
			{
				sender.sendMessage(hop.get(i - h.size()));
			}
		}
	}
}
