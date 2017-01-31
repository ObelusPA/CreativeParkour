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

import java.util.Map;

import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

class CubeDeBlocs
{
	private Map<Vector, MaterialData> blocs;
	private int taille;
	
	CubeDeBlocs(Map<Vector, MaterialData> blocs, int taille)
	{
		this.blocs = blocs;
		this.taille = taille;
	}

	Map<Vector, MaterialData> getBlocs()
	{
		return blocs;
	}

	int getTaille()
	{
		return taille;
	}
}
