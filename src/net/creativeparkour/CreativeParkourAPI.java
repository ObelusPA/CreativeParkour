package net.creativeparkour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Main CreativeParkour API class, everything starts here!<br>
 * There are also other methods in {@linkplain net.creativeparkour.CPUtils CPUtils}, but everything that directly interacts with CreativeParkour is here.
 * @author Obelus
 */
public class CreativeParkourAPI
{
	/**
	 * Returns a list of a player's times in all CreativeParkour maps.
	 * @param playerUUID {@code UUID} of the player we want the times.
	 * @return List of times of the player in all the maps
	 * @throws UnloadedPluginException If CreativeParkour is not loaded yet.
	 * @see CPTime
	 * @since 2.6
	 */
	public static List<CPTime> getPlayerTimes(UUID playerUUID) throws UnloadedPluginException
	{
		return getPlayersTimes(new ArrayList<UUID>(Arrays.asList(playerUUID)));
	}

	/**
	 * Returns a list of times in all CreativeParkour maps for a list of players.
	 * @param playersUUIDs {@code UUID} list of players we want the times.
	 * @return List of parkour times of the players in the {@code playersUUIDs} list.
	 * @throws UnloadedPluginException If CreativeParkour is not loaded yet.
	 * @see CPTime
	 * @since 2.6
	 */
	public static List<CPTime> getPlayersTimes(List<UUID> playersUUIDs) throws UnloadedPluginException
	{
		check();
		List<CPTime> times = new ArrayList<CPTime>();
		for (CPMap m : GameManager.maps.values())
		{
			if (m.listeTempsDispo()) // If the time list is already loaded
			{
				for (CPTime t : m.getListeTemps())
				{
					if (t.etat != EtatTemps.TO_DOWNLOAD && playersUUIDs.contains(t.playerUUID))
						times.add(t);
				}
			}
			else // Else, it is useless to load all the times, we only want those of some players
			{
				for (UUID uuid : playersUUIDs)
				{
					CPTime t = m.getTempsAvecFichier(GameManager.getFichierTemps(m.getUUID() + "_" + uuid));
					if (t != null)
						times.add(t);
				}
			}
		}
		return times;
	}


	/**
	 * Returns all the {@code CPTime}s for the given parkour map.<br>
	 * <em>Warning: this may take time if there are a lot of players!</em>
	 * @param parkourMap CreativeParkour map we want players' times.
	 * @return {@code SortedSet} of all the {@code CPTime}s for the map (from the world record to the longest time).
	 * @throws UnloadedPluginException If CreativeParkour is not loaded yet.
	 * @see CPTime
	 * @since 2.6
	 */
	public static SortedSet<CPTime> getTimes(CPMap parkourMap) throws UnloadedPluginException
	{
		check();
		return new TreeSet<CPTime>(parkourMap.getListeTemps());
	}

	/**
	 * Returns all the CreativeParkour maps on the server, regardless of their {@linkplain net.creativeparkour.CPMapState CPMapState} (CREATION, PUBLISHED, DOWNLOADED, DELETED), so you should check their state with {@linkplain net.creativeparkour.CPMap#getState() map.getState()}.
	 * @return Parkour map {@code List}.
	 * @throws UnloadedPluginException If CreativeParkour is not loaded yet.
	 * @since 2.6
	 */
	public static List<CPMap> getParkourMaps() throws UnloadedPluginException
	{
		check();
		return new ArrayList<CPMap>(GameManager.maps.values());
	}
	
	/**
	 * Returns the parkour map represented by the given {@code UUID}.
	 * @param mapUUID {@code UUID} of the wanted CreativeParkour map.
	 * @return Corresponding {@code CPMap}.
	 * @throws UnloadedPluginException If CreativeParkour is not loaded yet.
	 * @since 2.6
	 */
	public static CPMap getParkourMap(UUID mapUUID) throws UnloadedPluginException
	{
		check();
		return GameManager.getMap(mapUUID);
	}

	/**
	 * Deletes from memory unused {@linkplain net.creativeparkour.CPTime CPTime}s (because large ghost data may be loaded before) and some other stuff.
	 * @throws UnloadedPluginException If CreativeParkour is not loaded yet.
	 * @since 2.6
	 */
	public static void dumpMemory() throws UnloadedPluginException
	{
		check();
		GameManager.vidangeMemoire();
	}

	/**
	 * Finds a player name with their Minecraft {@code UUID}. It asks the server for it or searches in CreativeParkour files (they may contain names from downloaded ghosts that never joined the server).
	 * @param playerUUID {@code UUID} of the player we want the name.
	 * @return Player's name or {@code null} if unknown.
	 * @since 2.6
	 */
	public static String getPlayerName(UUID playerUUID)
	{
		return NameManager.getNomAvecUUID(playerUUID);
	}

	/**
	 * Finds a player {@code UUID} with their name. It asks the server for it or searches in CreativeParkour files (they may contain UUIDs from downloaded ghosts that never joined the server).
	 * @param playerName Player's name (not case-sensitive).
	 * @return Player's {@code UUID} or {@code null} if unknown.
	 * @since 2.6
	 */
	public static UUID getPlayerUUID(String playerName)
	{
		return NameManager.getUuidAvecNom(playerName);
	}


	/**
	 * Checks if CreativeParkour is loaded, so if the API can be used (CreativeParkour loads after most of other plugins, you should not try to access this API on server start).
	 * @return {@code true} if CreativeParkour is loaded.
	 * @since 2.6
	 */
	public static boolean pluginIsLoaded()
	{
		return CreativeParkour.loaded;
	}
	private static void check() throws UnloadedPluginException
	{
		if (!pluginIsLoaded())
		{
			throw new UnloadedPluginException("CreativeParkour is not loaded yet.");
		}
	}


	/**
	 * Thrown when trying to use CreativeParkour's API if it is not loaded (CreativeParkour loads after most of other plugins, you should not try to access this API on server start).
	 */
	public static class UnloadedPluginException extends Exception
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1836084984546635856L;

		public UnloadedPluginException(String message) {
			super(message);
		}
	}
}