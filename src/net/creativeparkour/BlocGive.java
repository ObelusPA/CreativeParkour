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

enum TypeGive { ELYTRA, ENDER_PEARL, FIREWORK };

class BlocGive extends BlocSpecial
{
	private TypeGive type;
	private boolean donner; // True si le bloc donne l'objet, false s'il l'enlève

	BlocGive (Block bloc, String type, String donner)
	{
		super(bloc);
		if (type.toLowerCase().contains("elytra"))
			this.type = TypeGive.ELYTRA;
		else if (type.toLowerCase().contains("firework"))
			this.type = TypeGive.FIREWORK;
		else
			this.type = TypeGive.ENDER_PEARL;
		if (donner.toLowerCase().contains("take") || donner.toLowerCase().contains("remove"))
			this.donner = false;
		else
			this.donner = true;
	}

	@Override
	void faireAction(Joueur j)
	{
		try {
			if (type == TypeGive.ELYTRA)
			{
				if (donner)
				{
					if (!j.aElytres() && j.getParamBool(PlayerSetting.MSG_ELYTRES)) // Le message n'est envoyé qu'une fois
						CPUtils.sendInfoMessage(j.getPlayer(), Langues.getMessage("play.elytra received"));
					j.donnerElytres();
				}
				else
				{
					if (j.aElytres() && j.getParamBool(PlayerSetting.MSG_ELYTRES))
						CPUtils.sendInfoMessage(j.getPlayer(), Langues.getMessage("play.elytra removed"));
					j.retirerElytres();
				}
			}
			else if (type == TypeGive.FIREWORK)
			{
				if (donner)
					j.donnerFusees();
				else
					j.retirerFusees();
			}
			else
			{
				if (donner)
					j.donnerPerles();
				else
					j.retirerPerles();
			}
		} catch (Error e) {
			// Rien
		}
	}

	String getAction()
	{
		if (donner)
			return "give";
		else
			return "take";
	}

	TypeGive getTypeGive()
	{
		return type;
	}

	@Override
	void setConfig(ConfigurationSection conf)
	{
		conf.set("t", getType());
		conf.set("type", type.toString());
		conf.set("action", getAction());
	}

	static String getType() {
		return "gives";
	}

	@Override
	String getTypeA() {
		return getType();
	}

	@Override
	String[] getPanneau() {
		return new String[]{getTag(), type.toString(), getAction(), ""};
	}

	static String getTag()
	{
		return CPUtils.bracket("give");
	}
}
