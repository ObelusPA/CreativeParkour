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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

class RemplisseurBlocs extends BukkitRunnable
{
	static final int CHUNKS_PAR_APPEL = 1;

	Map<Chunk, List<BlocDansChunk>> chunksBlocs;
	private int nbChunks;
	private CommandSender sender = null;
	private int pourcent;
	private int pourcentageMin;

	/**
	 * @param blocs Liste des blocs à mettre avec leurs positions
	 * @param world Monde où placer les blocs
	 * @param sender Joueur à qui dire la progression (null pour désactiver)
	 * @param pourcentageMin Pourcentage de départ
	 */
	RemplisseurBlocs(Map<Vector, MaterialData> blocs, World world, CommandSender sender, int pourcentageMin)
	{
		chunksBlocs = new HashMap<Chunk, List<BlocDansChunk>>();

		for (Entry<Vector, MaterialData> entry : blocs.entrySet())
		{
			Vector vect = entry.getKey();
			Chunk chunk = world.getChunkAt((int) Math.floor(vect.getBlockX() / 16.0), (int) Math.floor(vect.getBlockZ() / 16.0)); // TODO Utiliser Math.floorDiv quand tout le monde utilisera Java 8
			BlocDansChunk bdc = new BlocDansChunk(chunk, vect, entry.getValue());
			List<BlocDansChunk> liste = chunksBlocs.get(chunk);
			if (liste == null)
			{
				liste = new ArrayList<BlocDansChunk>();
				chunksBlocs.put(chunk, liste);
			}
			liste.add(bdc);
		}

		this.nbChunks = chunksBlocs.size();
		this.sender = sender;
		this.pourcent = pourcentageMin;
	}

	RemplisseurBlocs(Map<Vector, MaterialData> blocs, World world)
	{
		this(blocs, world, null, 0);
	}

	@Override
	public void run()
	{
		if (chunksBlocs.isEmpty())
			this.cancel();
		else
		{
			int i=0;
			Iterator<Entry<Chunk, List<BlocDansChunk>>> it = chunksBlocs.entrySet().iterator();
			while (it.hasNext())
			{
				Entry<Chunk, List<BlocDansChunk>> e = it.next();
				i++;
				if (i > CHUNKS_PAR_APPEL) // On ne fait que CHUNKS_PAR_APPEL chunks par appel
					break;
				else
				{
					Chunk chunk = e.getKey();
					for (BlocDansChunk b : e.getValue())
					{
						Block block = chunk.getBlock(b.xChunk, b.yChunk, b.zChunk);
						block.setType(b.nouveauMat);
						block.setData(b.data);
					}
					it.remove();
				}
			}

			// Progression
			if (sender != null)
			{
				int chunksFaits = nbChunks - chunksBlocs.size();
				float progression = pourcentageMin + ((float) chunksFaits / nbChunks) * 100;
				if (progression > pourcent + 10)
				{
					pourcent += 10;
					sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + pourcent + " %");
				}
			}
		}
	}

	int getDureeTraitement()
	{
		return (int) Math.ceil(chunksBlocs.size() / (double) CHUNKS_PAR_APPEL);
	}


	private class BlocDansChunk
	{
		byte xChunk;
		byte yChunk;
		byte zChunk;
		Material nouveauMat;
		byte data;

		BlocDansChunk (Chunk chunk, Vector loc, MaterialData matData)
		{
			xChunk = (byte) (loc.getBlockX() - chunk.getX() * 16);
			yChunk = (byte) loc.getBlockY();
			zChunk = (byte) (loc.getBlockZ() - chunk.getZ() * 16);
			nouveauMat = matData.getItemType();
			this.data = matData.getData();
		}
	}
}
