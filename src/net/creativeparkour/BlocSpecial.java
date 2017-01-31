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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Représente un bloc spécial avec une direction (bytes des panneaux)
 */
abstract class BlocSpecial
{
	private Block bloc;
	protected byte dir = 0;

	BlocSpecial(Block b)
	{
		this.bloc = b;
		this.dir = 0;
	}

	BlocSpecial(Block b, byte dir)
	{
		this.bloc = b;
		this.dir = dir;
	}

	void setDir(byte dir)
	{
		this.dir = dir;
	}

	Block getBloc()
	{
		return bloc;
	}

	byte getDir()
	{
		return dir;
	}

	/**
	 * @return Valeur à mettre dans la Location du joueur pour qu'il soit dans le même sens que dir
	 */
	float getYaw()
	{
		return (float) (dir / 16.0 * 360 + 180) % 360;
	}

	/**
	 * @return Location de ce bloc avec la bonne orientation (dir)
	 */
	Location getLocation()
	{
		return new Location(bloc.getWorld(), bloc.getX(), bloc.getY(), bloc.getZ(), getYaw(), 0);
	}

	boolean estPasse(Location l)
	{
		if (this instanceof BlocMort)
		{
			if (l.getBlock().equals(bloc))
				return true;
			return false;
		}
		else
			return CPUtils.blockTouched(bloc, l);
	}

	void supprimerPanneau()
	{
		bloc.setType(Material.AIR);
	}

	abstract void faireAction(Joueur j);

	abstract void setConfig(ConfigurationSection conf);

	abstract String getTypeA();

	abstract String[] getPanneau();
}
