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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * Represents a player location in a parkour map, plus whether or not they are sneaking, wearing Elytra and holding EnderPearls at this location.<br>
 * This is used to display good looking ghosts.
 * @author Obelus
 */
public class CPPlayerPosition
{

	private Vector vect;
	private float pitch;
	private float yaw;
	private boolean sneak;
	private boolean elytres;
	private boolean perle;

	/**
	 * Creates a new {@code CPPlayerPosition} with a location and the base block of a {@linkplain net.creativeparkour.CPMap CPMap}.
	 * @param loc {@code Location} of the player in the world.
	 * @param sneak Whether or not the player is sneaking.
	 * @param elytra Whether or not the player is wearing Elytra.
	 * @param pearl Whether or not the player is hording Ender Pearls.
	 * @param baseBlock Base block (minimum location) of the {@code CPMap} where the player is.
	 */
	public CPPlayerPosition(Location loc, boolean sneak, boolean elytra, boolean pearl, Block baseBlock)
	{
		this.vect = new Vector(loc.getX() - baseBlock.getX(), loc.getY() - baseBlock.getY(), loc.getZ() - baseBlock.getZ());
		this.pitch = loc.getPitch();
		this.yaw = loc.getYaw();
		this.sneak = sneak;
		this.elytres = elytra;
		this.perle = pearl;
	}

	/**
	 * Directly creates a new {@code CPPlayerPosition}, with a vector and some other values...
	 * @param vect {@code Vector} representing player's position from the minimum coordinate of the {@linkplain net.creativeparkour.CPMap CPMap} where they are.
	 * @param pitch Player's pitch value.
	 * @param yaw Player's yaw value.
	 * @param sneak Whether or not the player is sneaking.
	 * @param elytra Whether or not the player is wearing Elytra.
	 * @param pearl Whether or not the player is hording Ender Pearls.
	 */
	public CPPlayerPosition(Vector vect, float pitch, float yaw, boolean sneak, boolean elytra, boolean pearl)
	{
		this.vect = vect;
		this.pitch = pitch;
		this.yaw = yaw;
		this.sneak = sneak;
		this.elytres = elytra;
		this.perle = pearl;
	}

	/**
	 * Returns the world {@code Location} represented by this {@code CPPlayerPosition}, calculated from the given {@code baseLoc}.
	 * @param baseLoc {@code Location} from which the calculation is done.
	 * @return Resulting {@code Location}. 
	 */
	public Location getRealLoc(Location baseLoc)
	{
		Location l = baseLoc.clone().add(vect);
		l.setPitch(pitch);
		l.setYaw(yaw);
		return l;
	}

	/**
	 * Whether or not the player was sneaking at this {@code CPPlayerPosition}.
	 * @return {@code true} if the player was sneaking.
	 */
	public boolean isSneaking()
	{
		return sneak;
	}

	/**
	 * Whether or not the player was wearing Elytra at this {@code CPPlayerPosition}.
	 * @return {@code true} if the player was wearing Elytra.
	 */
	public boolean hasElytra()
	{
		return elytres;
	}
	
	/**
	 * Whether or not the player was holding Ender Pearls at this {@code CPPlayerPosition}.
	 * @return {@code true} if the player was holding Ender Pearls.
	 */
	public boolean hasPearl()
	{
		return perle;
	}

	/**
	 * Returns a {@code Vector} representing player's position from the minimum coordinate of the {@linkplain net.creativeparkour.CPMap CPMap} where they are.
	 * @return {@code Vector} representing player's position in a parkour map.
	 */
	public Vector getVector()
	{
		return vect.clone();
	}

	/**
	 * Returns player's pitch value at this {@code CPPlayerPosition}.
	 * @return Player's pitch value at this {@code CPPlayerPosition}.
	 */
	public float getPitch()
	{
		return pitch;
	}

	/**
	 * Returns player's yaw value at this {@code CPPlayerPosition}.
	 * @return Player's yaw value at this {@code CPPlayerPosition}.
	 */
	public float getYaw()
	{
		return yaw;
	}

	@Override
	public boolean equals(Object other){
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof CPPlayerPosition))return false;
		CPPlayerPosition otherPos = (CPPlayerPosition)other;
		if (otherPos.vect.equals(this.vect) && otherPos.pitch == this.pitch && otherPos.yaw == this.yaw && otherPos.sneak == this.sneak && otherPos.elytres == this.elytres && otherPos.perle == this.perle)
			return true;
		return false;
	}

	@Override
	public String toString()
	{
		int sneakI = sneak ? 1 : 0;
		int elytresI = elytres ? 1 : 0;
		int perleI = perle ? 1 : 0;
		return vect.getX() + ";" + vect.getY() + ";" + vect.getZ() + ";" + pitch + ";" + yaw + ";" + sneakI + ";" + elytresI + ";" + perleI;
	}
}
