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

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

class NameManager
{
	private static Map<UUID, String> noms = new Hashtable<UUID, String>();

	static String getNomAvecUUID(UUID uuid)
	{
		String nom = Bukkit.getOfflinePlayer(uuid).getName();
		String nomCache = noms.get(uuid);
		if (nom != null && !nom.isEmpty()) // Si le serveur connaît le nom
		{
			if (!nom.equals(nomCache)) // S'il n'est pas dans le cache ou que ce n'est pas le même, mise à jour
			{
				noms.put(uuid, nom);
				enregistrerNomJoueur(uuid.toString(), nom);
			}
			return nom;
		}
		else // Si le serveur ne connaît pas le nom
		{
			if (nomCache != null && !nomCache.isEmpty())
				return nomCache;
			else
			{
				// On cherche le nom et on le met dans le cache
				nom = Config.getConfJoueur(uuid.toString()).getString("name");
				if (nom == null)
					return null;
				noms.put(uuid, nom);
				return nom;
			}
		}
	}

	static UUID getUuidAvecNom(String nom)
	{
		Player p = Bukkit.getPlayer(nom);
		if (p != null)
			return p.getUniqueId();

		for (Entry<String, YamlConfiguration> e : Config.getConfigsJoueurs().entrySet())
		{
			if (nom.equalsIgnoreCase(e.getValue().getString("name")))
				return UUID.fromString(e.getKey());
		}
		return null;
	}

	static void enregistrerNomJoueur(String uuid, String nom)
	{
		Config.getConfJoueur(uuid).set("name", nom);
		Config.saveConfJoueur(uuid);
	}
}
