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

import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Bed;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class RemplisseurBlocsDelai extends BukkitRunnable
{
	private Map<Integer, List<JsonObject>> blocs;
	private Map<Integer, JsonObject> types;
	private World m;
	private int xMin, yMin, zMin, xMax, yMax, zMax;
	private int i;

	RemplisseurBlocsDelai(Map<Integer, List<JsonObject>> blocs, Map<Integer, JsonObject> types, World world, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax)
	{
		this.blocs = blocs;
		this.types = types;
		this.m = world;
		this.xMin = xMin;
		this.yMin = yMin;
		this.zMin = zMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.zMax = zMax;
		this.i = 0;
	}

	@Override
	public void run()
	{
		if (blocs.isEmpty())
			this.cancel();
		else
		{
			List<JsonObject> liste = blocs.get(i);
			if (liste != null && !liste.isEmpty())
			{
				for (JsonObject jsO : liste)
				{
					Map<Character, Integer> coords = CPUtils.parseCoordinates(jsO.get("c").getAsString());
					final Block b = m.getBlockAt(xMin + coords.get('x'), yMin + coords.get('y'), zMin + coords.get('z'));
					if (b.getX() < xMin || b.getY() < yMin || b.getZ() < zMin || b.getX() > xMax || b.getY() > yMax || b.getZ() > zMax) // Si le bloc n'est pas dans la map
					{
						this.cancel();
						throw new SecurityException();
					}
					else
					{
						final JsonObject type = types.get(jsO.get("i").getAsInt());
						final Material mat = Material.getMaterial(type.get("t").getAsString());
						b.setType(mat);
						b.setData(type.get("d").getAsByte());

						if (b.getState() != null)
						{
							if (b.getState() instanceof Sign)
							{
								Sign pa = (Sign) b.getState();
								JsonObject oPanneau = type.get("lignes-panneau").getAsJsonObject();
								pa.setLine(0, oPanneau.get("0").getAsString());
								pa.setLine(1, oPanneau.get("1").getAsString());
								pa.setLine(2, oPanneau.get("2").getAsString());
								pa.setLine(3, oPanneau.get("3").getAsString());
								pa.update();
							}
							else if (b.getState() instanceof Banner)
							{
								Banner ba = (Banner) b.getState();
								JsonObject oBan = type.get("donnees-banniere").getAsJsonObject();
								ba.setBaseColor(DyeColor.valueOf(oBan.get("baseColor").getAsString()));
								JsonArray patternsJ = oBan.get("patterns").getAsJsonArray();
								for (JsonElement pattern : patternsJ)
								{
									JsonObject oPat = pattern.getAsJsonObject();
									ba.addPattern(new Pattern(DyeColor.valueOf(oPat.get("color").getAsString()), PatternType.valueOf(oPat.get("pattern").getAsString())));
								}
								ba.update(true);
							}
							else if (b.getState() instanceof Skull)
							{
								Skull sk = (Skull) b.getState();
								JsonObject oTete = type.get("donnees-tete").getAsJsonObject();
								sk.setRotation(BlockFace.valueOf(oTete.get("rotation").getAsString()));
								try {
									sk.setSkullType(SkullType.valueOf(oTete.get("skullType").getAsString()));
								} catch (IllegalArgumentException e) {
									// Nothing, dragons' heads are replaces by skeleton skulls in 1.8.
								}
								sk.update();
							}
							else if (CreativeParkour.getServVersion() >= 12 && b.getState() instanceof Bed)
							{
								Bed bed = (Bed) b.getState();
								if (type.has("donnees-lit"))
								{
									JsonObject oBed = type.get("donnees-lit").getAsJsonObject();
									bed.setColor(DyeColor.valueOf(oBed.get("color").getAsString()));
									bed.update(true);
								}
							}
						}
					}
				}
			}

			blocs.remove(i);
		}
		this.i++;
	}
}
