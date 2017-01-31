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

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

class BlocDepart extends BlocSpecial
{
	BlocDepart(Block b)
	{
		super(b);
	}

	@Override
	void faireAction(Joueur j)
	{
		if (j.getScore() == null || j.estArrive())
		{
			j.depart();
			j.getPlayer().playSound(j.getPlayer().getLocation(), CPUtils.getSound("ENTITY_ARROW_HIT_PLAYER", "SUCCESSFUL_HIT"), 1, 0);
		}
	}

	@Override
	void setConfig(ConfigurationSection conf)
	{
		conf.set("t", getType());
	}

	static String getType() {
		return "start";
	}

	@Override
	String getTypeA() {
		return getType();
	}

	@Override
	String[] getPanneau() {
		return new String[]{getTag(), "", "", ""};
	}

	static String getTag()
	{
		return CPUtils.bracket("start");
	}
}
