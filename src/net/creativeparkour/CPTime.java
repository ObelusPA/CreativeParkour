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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

enum EtatTemps { LOCAL, DOWNLOADED, TO_DOWNLOAD };

/**
 * Represents a player's performance in a particular parkour map.
 * @author Obelus
 */
public class CPTime implements Comparable<CPTime>
{
	int ticks;
	UUID playerUUID;
	CPMap map;
	Date date;
	Map<Block, Integer> checkpoints;
	NavigableMap<Integer, CPPlayerPosition> ghostData;
	long realMilliseconds;
	EtatTemps etat;
	boolean fantomeAnnule;

	CPTime(UUID uuidJoueur, CPMap m, int ticks)
	{
		this.ticks = ticks;
		this.playerUUID = uuidJoueur;
		this.map = m;
		this.checkpoints = new HashMap<Block, Integer>();
		this.ghostData = new TreeMap<Integer, CPPlayerPosition>();
		this.etat = EtatTemps.LOCAL;
		this.fantomeAnnule = false;
	}

	/**
	 * Returns the number of server ticks the player took to complete the parkour.
	 * @return Ticks the player took to complete the parkour.
	 */
	public int getTicks() {
		return ticks;
	}

	/**
	 * Returns the {@code UUID} of the player that made this {@code CPTime}.
	 * @return Player's Minecraft {@code UUID}.
	 */
	public UUID getPlayerUUID() {
		return playerUUID;
	}

	/**
	 * Returns the name of the player that made this {@code CPTime}.
	 * @return Player name.
	 */
	public String getPlayerName() {
		return NameManager.getNomAvecUUID(playerUUID);
	}

	/**
	 * Returns the CreativeParkour map in which the player made this {@code CPTime}.
	 * @return {@code CPMap} where this {@code CPTime} was made.
	 */
	public CPMap getMap() {
		return map;
	}

	/**
	 * Returns the {@code Date} when the player completed the parkour.
	 * @return {@code Date} when this time was made.
	 */
	public Date getDate() {
		return (Date) date.clone();
	}

	/**
	 * Returns the number of ticks when the player passed each ckeckpoint of the parkour.
	 * @return Number of ticks when the player passed each ckeckpoint of the parkour. {@code Map}'s key ({@code Block}) is the checkpoint, {@code Map}'s value ({@code Integer}) is the number of ticks.
	 */
	public Map<Block, Integer> getCheckpoints() {
		return new HashMap<Block, Integer>(checkpoints);
	}

	/**
	 * Returns {@code true} if this {@code CPTime} has a ghost (if the player's locations were recorded suring their course).
	 * @return Whether of not this {@code CPTime} has a ghost.
	 */
	public boolean hasGhost()
	{
		return (ghostData != null && !ghostData.isEmpty()) || etat == EtatTemps.TO_DOWNLOAD;
	}

	/**
	 * Returns the {@code CPPlayerPosition} where the players was at each tick.
	 * @return {@code NavigableMap} containing player's {@code CPPlayerPosition} at each tick ({@code NavigableMap}'s keys are the ticks).
	 * @see CPPlayerPosition
	 */
	public NavigableMap<Integer, CPPlayerPosition> getGhostData() {
		return new TreeMap<Integer, CPPlayerPosition>(ghostData);
	}

	/**
	 * Returns the number of milliseconds the player took to complete the parkour, calculated with server's clock instead of server ticks.
	 * @return Milliseconds the player took to complete the parkour.
	 */
	public long getRealMilliseconds() {
		return realMilliseconds;
	}

	/**
	 * Returns the name of the file in which this {@code CPTime} is stored in the Times folder.
	 * @return File name corresponding to this {@code CPTime}.
	 */
	public String getFileName()
	{
		return map.getUUID() + "_" + playerUUID + ".yml";
	}

	/**
	 * Returns the number of seconds the player took to complete the parkour (calculated with the number of ticks).
	 * @return The {@code ticks} value converted in seconds
	 */
	public float inSeconds()
	{
		return (float) (ticks / 20.0);
	}

	void ajouterCheckpoint(Block b)
	{
		if (!fantomeAnnule)
			checkpoints.put(b, ticks);
	}

	void ajouterCheckpoints(JsonArray array)
	{
		Map<String, Integer> liste = new HashMap<String, Integer>();
		for (JsonElement e : array)
		{
			JsonObject obj = e.getAsJsonObject();
			Map<Character, Integer> coords = CPUtils.parseCoordinates(obj.get("coords").getAsString());
			Block min = map.getMinLoc();
			liste.put((min.getX() + coords.get('x')) + ";" + (min.getY() + coords.get('y')) + ";" + (min.getZ() + coords.get('z')), obj.get("ticks").getAsInt());
		}
		ajouterCheckpoints(liste);
	}

	void ajouterCheckpoints(Object objet)
	{
		if (objet != null)
		{
			if (objet instanceof MemorySection)
			{
				MemorySection ms = (MemorySection) objet;
				World monde = map.getWorld();
				Set<String> keys = ms.getKeys(false);
				Iterator<String> it = keys.iterator();
				while (it.hasNext())
				{
					String k = it.next();
					Map<Character, Integer> coords = CPUtils.parseCoordinates(k);
					checkpoints.put(monde.getBlockAt(coords.get('x'), coords.get('y'), coords.get('z')), ms.getInt(k));
				}
			}
			else if (objet instanceof Map<?,?>)
			{
				@SuppressWarnings("unchecked")
				Map<String, Integer> hm = (Map<String, Integer>) objet;
				World monde = map.getWorld();
				Iterator<String> it = hm.keySet().iterator();
				while (it.hasNext())
				{
					String k = it.next();
					Map<Character, Integer> coords = CPUtils.parseCoordinates(k);
					checkpoints.put(monde.getBlockAt(coords.get('x'), coords.get('y'), coords.get('z')), hm.get(k));
				}
			}
		}
	}

	private HashMap<String, Integer> getCheckpointsAvecCoords()
	{
		HashMap<String, Integer> liste = new HashMap<String, Integer>();
		Iterator<Block> it = checkpoints.keySet().iterator();
		while (it.hasNext())
		{
			Block b = it.next();
			liste.put(b.getX() + ";" + b.getY() + ";" + b.getZ(), checkpoints.get(b));
		}
		return liste;
	}

	void ajouterPosition(CPPlayerPosition pos)
	{
		if (!fantomeAnnule && (ghostData.size() == 0 || !ghostData.lastEntry().getValue().equals(pos))) // Si le joueur a bougé
		{
			ghostData.put(ticks, pos);
		}
	}

	void ajouterPositions(JsonArray array)
	{
		Map<Integer, String> liste = new HashMap<Integer, String>();
		for (JsonElement e : array)
		{
			JsonObject obj = e.getAsJsonObject();
			liste.put(obj.get("tick").getAsInt(), obj.get("pos").getAsString());
		}
		ajouterPositions(liste);
	}

	void ajouterPositions(Object objet)
	{
		if (objet != null)
		{
			if (objet instanceof MemorySection)
			{
				MemorySection ms = (MemorySection) objet;
				Set<String> keys = ms.getKeys(false);
				Iterator<String> it = keys.iterator();
				while (it.hasNext())
				{
					String k = it.next();
					Map<Character, Double> donnees = separerDonneesFantome(ms.getString(k));
					ghostData.put(Integer.valueOf(k), new CPPlayerPosition(new Vector(donnees.get('x'), donnees.get('y'), donnees.get('z')), donnees.get('P').floatValue(), donnees.get('Y').floatValue(), donnees.get('s') != 0, donnees.get('e') != 0, donnees.get('p') != 0));
				}
			}
			else if (objet instanceof Map<?,?>)
			{
				@SuppressWarnings("unchecked")
				Map<Integer, String> hm = (Map<Integer, String>) objet;
				Iterator<Integer> it = hm.keySet().iterator();
				while (it.hasNext())
				{
					int k = it.next();
					Map<Character, Double> donnees = separerDonneesFantome(hm.get(k));
					ghostData.put(k, new CPPlayerPosition(new Vector(donnees.get('x'), donnees.get('y'), donnees.get('z')), donnees.get('P').floatValue(), donnees.get('Y').floatValue(), donnees.get('s') != 0, donnees.get('e') != 0, donnees.get('p') != 0));
				}
			}
		}
	}

	private Map<Character, Double> separerDonneesFantome(String s)
	{
		Map<Character, Double> map = new HashMap<Character, Double>();
		String[] a = s.split(";");
		map.put('x', Double.valueOf(a[0]));
		map.put('y', Double.valueOf(a[1]));
		map.put('z', Double.valueOf(a[2]));
		map.put('P', Double.valueOf(a[3]));
		map.put('Y', Double.valueOf(a[4]));
		map.put('s', Double.valueOf(a[5]));
		map.put('e', Double.valueOf(a[6]));
		map.put('p', a.length >= 8 ? Double.valueOf(a[7]) : 0);
		return map;
	}

	private NavigableMap<Integer, String> getFantomeAvecCoords()
	{
		NavigableMap<Integer, String> liste = new TreeMap<Integer, String>();
		Iterator<Integer> it = ghostData.keySet().iterator();
		while (it.hasNext())
		{
			int i = it.next();
			liste.put(i, ghostData.get(i).toString());
		}
		return liste;
	}

	void sauvegarder()
	{
		sauvegarder(new Date());
	}

	void sauvegarder(Date date)
	{
		this.date = date;
		File dossier = new File(CreativeParkour.getPlugin().getDataFolder(), "/Times");
		File fichier = new File(dossier, this.getFileName());
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(fichier);
		yml.set("map name", map.getName());
		yml.set("map uuid", map.getUUID().toString());
		yml.set("player uuid", playerUUID.toString());
		yml.set("ticks", ticks);
		yml.set("real milliseconds", realMilliseconds);
		yml.set("date", this.date.getTime());
		yml.set("state", etat.toString());
		yml.set("checkpoints", this.getCheckpointsAvecCoords());
		yml.set("ghost", this.getFantomeAvecCoords());

		// Sauvegarde
		try {
			yml.save(fichier);
		} catch (IOException e) {
			Bukkit.getLogger().warning("An error occured while loading file '" + fichier.getPath() + "'.");
			e.printStackTrace();
		}
	}

	JsonObject getJson()
	{
		JsonObject objet = new JsonObject();

		objet.addProperty("uuidMap", map.getUUID().toString());
		objet.addProperty("uuidJoueur", playerUUID.toString());
		objet.addProperty("nomJoueur", NameManager.getNomAvecUUID(playerUUID));
		objet.addProperty("date", date.getTime());
		objet.addProperty("ticks", ticks);
		objet.addProperty("millisecondes", realMilliseconds);

		JsonObject donnees = new JsonObject(); // On met les checkpoints et les positions là dedans car c'est ce qui est stocké dans le fichier sur le serveur
		// Checkpoints
		JsonArray aCheckpoints = new JsonArray();
		for(Entry<Block, Integer> checkpoint : checkpoints.entrySet())
		{
			JsonObject oCheckpoint = new JsonObject();
			Block b = checkpoint.getKey();
			Block lMin = map.getMinLoc();
			oCheckpoint.addProperty("coords", (b.getX() - lMin.getX()) + ";" + (b.getY() - lMin.getY()) + ";" + (b.getZ() - lMin.getZ()));
			oCheckpoint.addProperty("ticks", checkpoint.getValue());
			aCheckpoints.add(oCheckpoint);
		}
		donnees.add("checkpoints", aCheckpoints);

		// Positions du fantôme
		JsonArray aPositions = new JsonArray();
		for (Entry<Integer, CPPlayerPosition> position : ghostData.entrySet())
		{
			JsonObject oPosition = new JsonObject();
			oPosition.addProperty("tick", position.getKey());
			oPosition.addProperty("pos", position.getValue().toString());
			aPositions.add(oPosition);
		}
		donnees.add("positions", aPositions);

		objet.add("data", donnees);

		return objet;
	}

	void annulerFantomeCheckpoints()
	{
		fantomeAnnule = true;
		checkpoints.clear();
		ghostData.clear();
	}

	/* Compares the ticks values
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(CPTime t2)
	{
		int retour = ((Integer)this.ticks).compareTo(t2.ticks);
		if (retour != 0)
			return retour;
		retour = ((Long)this.realMilliseconds).compareTo(t2.realMilliseconds);
		if (retour != 0)
			return retour;
		return this.date.compareTo(t2.date);
	}

	@Override
	public String toString()
	{
		return "CPTime [playerUUID=" + playerUUID + ", mapUUID=" + map.getUUID() + "ticks=" + ticks + "]";
	}
}
