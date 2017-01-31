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


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

class BlocEffet extends BlocSpecial
{
	PotionEffect effet;
	
	BlocEffet(Block b, String effect, int duree, int force)
	{
		super(b);
		effect = effect.replace(" ", "_");
		
		PotionEffectType effectType = null;
		if (effect.equalsIgnoreCase("SPEED")) effectType = PotionEffectType.SPEED;
		else if (effect.equalsIgnoreCase("SLOW") || effect.equalsIgnoreCase("SLOWNESS")) effectType = PotionEffectType.SLOW;
		else if (effect.equalsIgnoreCase("JUMP") || effect.equalsIgnoreCase("JUMP_BOOST")) effectType = PotionEffectType.JUMP;
		else if (effect.equalsIgnoreCase("CONFUSION") || effect.equalsIgnoreCase("NAUSEA")) effectType = PotionEffectType.CONFUSION;
		else if (effect.equalsIgnoreCase("BLINDNESS")) effectType = PotionEffectType.BLINDNESS;
		else if (effect.equalsIgnoreCase("NIGHT_VISION")) effectType = PotionEffectType.NIGHT_VISION;
		else if (effect.equalsIgnoreCase("LEVITATION")) effectType = PotionEffectType.LEVITATION;
		
		effet = new PotionEffect(effectType, duree * 20, force);
	}

	@Override
	void faireAction(Joueur j)
	{
		if (effet.getDuration() == 0)
		{
			j.getPlayer().removePotionEffect(effet.getType());
		}
		else
		{
			j.getPlayer().addPotionEffect(effet);
		}
	}

	PotionEffect getEffet()
	{
		return effet;
	}
	
	private static boolean estUnEffet(String s)
	{
		s = s.replace(" ", "_");
		if (s.equalsIgnoreCase("SPEED") || s.equalsIgnoreCase("SLOW") || s.equalsIgnoreCase("SLOWNESS") || s.equalsIgnoreCase("JUMP") || s.equalsIgnoreCase("JUMP_BOOST") || s.equalsIgnoreCase("CONFUSION") || s.equalsIgnoreCase("NAUSEA") || s.equalsIgnoreCase("BLINDNESS") || s.equalsIgnoreCase("NIGHT_VISION") || (CreativeParkour.auMoins1_9() && s.equalsIgnoreCase("LEVITATION")))
			return true;
		else
			return false;
	}
	
	static boolean estUnPanneauValide(String[] lignes, Player p, Block b)
	{
		boolean erreur = false;
		String loc = new String();
		if (p != null) loc = ChatColor.GRAY + "" + b.getX() + "; " + b.getY() + "; " + b.getZ() + " ➔ ";
		if (Config.getConfig().getBoolean("map creation.disable potion effects"))
		{
			if (p != null) p.sendMessage(Config.prefix() + loc + ChatColor.RED + Langues.getMessage("feature disabled"));
			erreur = true;
			b.setType(Material.AIR);
		}
		else
		{
			if (!estUnEffet(lignes[1]))
			{
				if (p != null) p.sendMessage(Config.prefix() + loc + ChatColor.RED + Langues.getMessage("creation.signs.effect error"));
				erreur = true;
			}
			
			try {
				if (Integer.valueOf(lignes[2]) < 0 || Integer.valueOf(lignes[3]) < 0)
				{
					if (p != null) p.sendMessage(Config.prefix() + loc + ChatColor.RED + Langues.getMessage("creation.signs.int error"));
					erreur = true;
				}
			} catch (NumberFormatException exception) {
				if (p != null) p.sendMessage(Config.prefix() + loc + ChatColor.RED + Langues.getMessage("creation.signs.int error"));
				erreur = true;
			}
		}
		return !erreur;
	}
	
	static void supprimerEffets(Player p)
	{
		p.removePotionEffect(PotionEffectType.SPEED);
		p.removePotionEffect(PotionEffectType.SLOW);
		p.removePotionEffect(PotionEffectType.JUMP);
		p.removePotionEffect(PotionEffectType.CONFUSION);
		p.removePotionEffect(PotionEffectType.BLINDNESS);
		p.removePotionEffect(PotionEffectType.NIGHT_VISION);
		try {
			p.removePotionEffect(PotionEffectType.LEVITATION);
		} catch (NoSuchFieldError e) {
			// Rien
		}
	}

	@Override
	void setConfig(ConfigurationSection conf)
	{
		conf.set("t", getType());
		conf.set("effect", effet.getType().getName());
		conf.set("duration", (int) effet.getDuration() / 20);
		conf.set("amplifier", effet.getAmplifier());
	}

	static String getType() {
		return "effects";
	}

	@Override
	String getTypeA() {
		return getType();
	}

	@Override
	String[] getPanneau() {
		return new String[]{getTag(), effet.getType().getName(), String.valueOf(effet.getDuration() / 20), String.valueOf(effet.getAmplifier())};
	}

	static String getTag()
	{
		return CPUtils.bracket("effect");
	}
}
