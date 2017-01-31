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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.world.World;

import net.md_5.bungee.api.ChatColor;

class JoueurWorldEdit
{
	WorldEditPlugin we;
	Player p;
	World world;
	boolean permSet;
	PermissionAttachment attachment;
	JoueurVault joueurVault;
	List<String> listePerms = new ArrayList<String>();

	JoueurWorldEdit(Joueur j, org.bukkit.World world)
	{
		this.we = (WorldEditPlugin) CreativeParkour.getWorldEdit();
		this.p = j.getPlayer();
		this.world = new BukkitWorld(world);
		if (CreativeParkour.vaultPresent())
			joueurVault = new JoueurVault(p);
		clear(true);
		this.listePerms.add("worldedit.wand");
		this.listePerms.add("worldedit.wand.toggle");
		this.listePerms.add("worldedit.region.set");
		this.listePerms.add("worldedit.history.undo");
		this.listePerms.add("worldedit.history.redo");
		this.listePerms.add("worldedit.history.clear");
		this.listePerms.add("worldedit.selection.pos");
		this.listePerms.add("worldedit.region.replace");
		this.listePerms.add("worldedit.region.hollow");
		this.listePerms.add("worldedit.region.center");
		this.listePerms.add("worldedit.region.naturalize");
		this.listePerms.add("worldedit.region.walls");
		this.listePerms.add("worldedit.region.faces");
		this.listePerms.add("worldedit.region.smooth");
		this.listePerms.add("worldedit.region.stack");
		this.listePerms.add("worldedit.analysis.count");
		this.listePerms.add("worldedit.selection.size");
		this.listePerms.add("worldedit.selection.expand");
		this.listePerms.add("worldedit.generation.cylinder");
		this.listePerms.add("worldedit.generation.sphere");
		this.listePerms.add("worldedit.generation.pyramid");
		this.listePerms.add("worldedit.clipboard.cut");
		this.listePerms.add("worldedit.clipboard.copy");
		this.listePerms.add("worldedit.clipboard.paste");
		this.listePerms.add("worldedit.clipboard.flip");
		this.listePerms.add("worldedit.clipboard.rotate");
		WorldEditEvents.register();
	}

	void clear(boolean clearHistory)
	{
		LocalSession ls = we.getSession(p);
		if (clearHistory)
			ls.clearHistory();
		ls.getRegionSelector(world).clear();
	}

	void autoriser()
	{
		if (joueurVault == null)
		{
			attachment = p.addAttachment(CreativeParkour.getPlugin());
			for (String perm : listePerms)
			{
				attachment.setPermission(perm, true);
			}
		}
		else
		{
			for (String perm : listePerms)
			{
				joueurVault.autoriser(perm);
			}
		}
	}

	void desactiver()
	{
		if (joueurVault == null)
			p.removeAttachment(attachment);
		else
		{
			for (String perm : listePerms)
			{
				joueurVault.liberer(perm);
			}
		}
	}

	void verifSelection(Joueur j) // On met le joueur pour éviter de le chercher tout le temps
	{
		if (j.getPlayer().equals(p))
		{
			Selection sel = we.getSelection(p);
			if (sel != null)
			{
				CPMap m = j.getMapObjet();
				if (m != null)
				{
					Location min = sel.getMinimumPoint();
					Location max = sel.getMaximumPoint();
					if (min.getBlockX() < m.getMinLoc().getX() || min.getBlockY() < m.getMinLoc().getY() || min.getBlockZ() < m.getMinLoc().getZ() || 
							min.getBlockX() > m.getMaxLoc().getX() || min.getBlockY() > m.getMaxLoc().getY() || min.getBlockZ() > m.getMaxLoc().getZ() ||
							max.getBlockX() < m.getMinLoc().getX() || max.getBlockY() < m.getMinLoc().getY() || max.getBlockZ() < m.getMinLoc().getZ() || 
							max.getBlockX() > m.getMaxLoc().getX() || max.getBlockY() > m.getMaxLoc().getY() || max.getBlockZ() > m.getMaxLoc().getZ())
					{
						p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.wand.selection error"));
						clear(false);
					}
				}
			}
		}
	}
}
