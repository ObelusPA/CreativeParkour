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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.Plugin;

class Stats implements Runnable, Listener
{
	private Plugin plugin;
	private Set<UUID> joueursConnus;
	private Set<UUID> joueursCPConnus;
	int secondesJouees;
	int parkoursTentes;
	int parkoursReussis;
	int nbSauts;
	private Map<String, AtomicInteger> commandes;
	private String plugins;

	Stats(Plugin plugin)
	{
		this.plugin = plugin;
		this.joueursConnus = getListeUUIDsEnLigne();
		this.joueursCPConnus = getListeUUIDsJoueursCP();
		this.secondesJouees = 0;
		this.parkoursTentes = 0;
		this.parkoursReussis = 0;
		this.nbSauts = 0;
		this.commandes = new HashMap<String, AtomicInteger>();
		// Remplissage de la liste des plugins installés
		StringBuffer pl = new StringBuffer();
		for (Plugin p : Bukkit.getPluginManager().getPlugins())
		{
			pl.append(p.getName()).append(",");
		}
		if (pl.length() > 0)
			pl.deleteCharAt(pl.length() - 1);
		this.plugins = pl.toString();
	}

	@EventHandler
	void onPlayerLogin(PlayerLoginEvent e)
	{
		if (e.getResult() == Result.ALLOWED)
		{
			joueursConnus.add(e.getPlayer().getUniqueId());
		}
	}

	void playerJoinCP(UUID uuid)
	{
		joueursCPConnus.add(uuid);
	}

	@Override
	public void run()
	{
		if (Config.getConfig().getBoolean("enable data collection"))
		{
			CreativeParkour.debug("STAT", "Sending stats...");
			HashMap<String, String> paramsPost = new HashMap<String, String>();
			paramsPost.put("nbJoueurs", String.valueOf(joueursConnus.size()));
			paramsPost.put("nbMaps", String.valueOf(GameManager.nbMaps()));
			paramsPost.put("nbJoueursCP", String.valueOf(joueursCPConnus.size()));
			paramsPost.put("secondesJouees", String.valueOf(secondesJouees));
			paramsPost.put("parkoursTentes", String.valueOf(parkoursTentes));
			paramsPost.put("parkoursReussis", String.valueOf(parkoursReussis));
			paramsPost.put("nbSauts", String.valueOf(nbSauts));
			paramsPost.put("langue", Config.getLanguage());
			paramsPost.put("onlineMode", String.valueOf(Bukkit.getOnlineMode()));
			paramsPost.put("infosPlugin", plugin.getDescription().getFullName());
			paramsPost.put("versionServeur", plugin.getServer().getVersion());
			if (plugins != null)
				paramsPost.put("plugins", plugins);
			paramsPost.put("commandes", commandesToString());
			if (!Langues.anciennesTraductions.isEmpty())
			{
				StringBuffer modifsAng = new StringBuffer();
				for (Entry<String, String> e : Langues.anciennesTraductions.entrySet())
				{
					modifsAng.append(e.getKey() + ":" + e.getValue() + ";");
				}
				if (modifsAng.length() > 0)
					modifsAng.deleteCharAt(modifsAng.length() - 1);
				paramsPost.put("anciennesTraductions", modifsAng.toString());
				Langues.anciennesTraductions.clear(); // Pour ne pas le refaire
			}
			try {
				CPRequest.effectuerRequete("stats.php", paramsPost, this, null, null);
			} catch (SecurityException e) {
				CreativeParkour.erreur("STATS", e, true);
			}
		}

		// Réinitialisation des variables
		this.joueursConnus = getListeUUIDsEnLigne();
		this.joueursCPConnus = getListeUUIDsJoueursCP();
		this.secondesJouees = 0;
		this.parkoursTentes = 0;
		this.parkoursReussis = 0;
		this.nbSauts = 0;
		this.commandes.clear();
		this.plugins = null; // Pas besoin de l'envoyer plusieurs fois
	}

	private Set<UUID> getListeUUIDsEnLigne()
	{
		List<Player> liste = new ArrayList<Player>(plugin.getServer().getOnlinePlayers());
		Set<UUID> rep = new HashSet<UUID>();
		for (int i=0; i < liste.size(); i++)
		{
			rep.add(liste.get(i).getUniqueId());
		}
		return rep;
	}

	private Set<UUID> getListeUUIDsJoueursCP()
	{
		List<Joueur> liste = GameManager.getJoueurs();
		Set<UUID> rep = new HashSet<UUID>();
		for (int i=0; i < liste.size(); i++)
		{
			rep.add(liste.get(i).getPlayer().getUniqueId());
		}
		return rep;
	}

	void ajouterCommande(String commande)
	{
		String c = commande.toLowerCase();
		AtomicInteger nb = commandes.get(c);
		if (nb != null)
			nb.incrementAndGet();
		else
			commandes.put(c, new AtomicInteger(1));
	}

	static void ajouterCommandeStats(String commande) {
		if (CreativeParkour.stats() != null)
			CreativeParkour.stats().ajouterCommande(commande);
	}

	private String commandesToString()
	{
		StringBuffer str = new StringBuffer();
		for (Iterator<String> it = commandes.keySet().iterator(); it.hasNext();)
		{
			String commande = it.next();
			int nb = commandes.get(commande).get();
			str.append(commande + ":" + nb + ";");
		}
		// Suppression du dernier point-virgule
		if (str.length() > 0)
			str.deleteCharAt(str.length() - 1);
		return str.toString();
	}
}
