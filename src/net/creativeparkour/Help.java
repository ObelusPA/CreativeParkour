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
		
		String cmd = Config.getConfig().getBoolean("dont use cp") ? "/cpk " : "/cp ";

		h.add(gold + cmd + Langues.getMessage("help.title").toLowerCase() + trans + Langues.getMessage("help.help"));
		h.add(gold + cmd + Langues.getCommand("play") + " [" + Langues.getMessage("commands.map name") + "]" + trans + Langues.getMessage("help.play"));
		h.add(gold + cmd + Langues.getCommand("download") + " <map ID>" + trans + Langues.getMessage("help.download"));
		h.add(gold + cmd + Langues.getCommand("create") + trans + Langues.getMessage("help.create"));
		h.add(gold + cmd + Langues.getCommand("leave") + trans + Langues.getMessage("help.leave"));
		h.add(gold + cmd + Langues.getCommand("test") + trans + Langues.getMessage("help.test"));
		h.add(gold + cmd + Langues.getCommand("invite") + " <" + Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.invite"));
		h.add(gold + cmd + Langues.getCommand("remove") + " <" + Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.remove"));
		h.add(gold + cmd + Langues.getCommand("contributors") + trans + Langues.getMessage("help.contributors"));
		h.add(gold + cmd + Langues.getCommand("settings") + trans + Langues.getMessage("help.settings"));
		h.add(gold + cmd + Langues.getCommand("tp") + " <" + Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.tp"));
		h.add(gold + cmd + Langues.getCommand("spectator") + trans + Langues.getMessage("help.spectator"));
		h.add(gold + cmd + Langues.getCommand("publish") + trans + Langues.getMessage("help.publish"));
		h.add(gold + cmd + Langues.getCommand("mapoptions") + trans + Langues.getMessage("help.mapoptions"));
		h.add(gold + cmd + Langues.getCommand("importsel") + trans + Langues.getMessage("help.importsel"));
		h.add(gold + cmd + Langues.getCommand("claim") + trans + Langues.getMessage("help.claim"));
		h.add(gold + cmd + Langues.getCommand("pin") + trans + Langues.getMessage("help.pin"));
		h.add(gold + cmd + Langues.getCommand("unpin") + trans + Langues.getMessage("help.unpin"));
		h.add(gold + cmd + Langues.getCommand("share") + trans + Langues.getMessage("help.share"));
		h.add(gold + cmd + Langues.getCommand("export") + trans + Langues.getMessage("help.export"));
		h.add(gold + cmd + Langues.getCommand("edit") + trans + Langues.getMessage("help.edit"));
		h.add(gold + cmd + Langues.getCommand("delete") + trans + Langues.getMessage("help.delete"));
		h.add(gold + cmd + Langues.getCommand("ghost") + " " + Langues.getCommand("ghost play") + trans + Langues.getMessage("help.ghost play"));
		h.add(gold + cmd + Langues.getCommand("ghost") + " " + Langues.getCommand("ghost speed") + " [" +Langues.getMessage("help.multiplier") + "]" + trans + Langues.getMessage("help.ghost speed"));
		h.add(gold + cmd + Langues.getCommand("ghost") + " " + Langues.getCommand("ghost rewind") + " [" +Langues.getMessage("help.seconds") + "]" + trans + Langues.getMessage("help.ghost rewind"));
		h.add(gold + cmd + Langues.getCommand("ghost") + " " + Langues.getCommand("ghost moment") + " [" +Langues.getMessage("help.second") + "]" + trans + Langues.getMessage("help.ghost moment"));
		h.add(gold + cmd + Langues.getCommand("ghost") + " " + Langues.getCommand("ghost select") + trans + Langues.getMessage("help.ghost select"));
		h.add(gold + cmd + Langues.getCommand("ghost") + " " + Langues.getCommand("ghost watch") + " <" +Langues.getMessage("help.ghost id") + ">" + trans + Langues.getMessage("help.ghost watch"));
		h.add(gold + cmd + Langues.getCommand("noplates") + trans + Langues.getMessage("help.noplates"));
		h.add(gold + cmd + Langues.getCommand("register") + trans + Langues.getMessage("help.register"));
		h.add(gold + cmd + Langues.getCommand("sync") + trans + Langues.getMessage("help.sync"));
		h.add(gold + cmd + Langues.getCommand("managemaps") + trans + Langues.getMessage("help.managemaps"));
		h.add(gold + cmd + Langues.getCommand("getid") + " [map name]" + trans + Langues.getMessage("help.getid"));
		h.add(gold + cmd + Langues.getCommand("ban") + " <" + Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.ban"));
		h.add(gold + cmd + Langues.getCommand("pardon") + " <" + Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.pardon"));
		h.add(gold + cmd + Langues.getCommand("removetime") + " <" + Langues.getMessage("commands.playerN") + ">" + trans + Langues.getMessage("help.removetime"));

		hop.add(yellow + cmd + "config" + trans + Langues.getMessage("help.config"));
		hop.add(yellow + cmd + "language <language code>" + trans + Langues.getMessage("help.language"));
		hop.add(yellow + cmd + "enable" + trans + Langues.getMessage("help.enable"));
		hop.add(yellow + cmd + "disable" + trans + Langues.getMessage("help.disable"));
	}

	static void sendHelp(CommandSender sender, int page)
	{
		int nbLignes = h.size();
		final int commandesParPage = 7;

		if (sender.hasPermission("creativeparkour.*"))
		{
			nbLignes = nbLignes + hop.size();
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

	static List<String> getHelp()
	{
		List<String> list = new ArrayList<String>(h);
		list.addAll(hop);
		return list;
	}
}
