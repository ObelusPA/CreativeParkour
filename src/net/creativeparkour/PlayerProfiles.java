package net.creativeparkour;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.creativeparkour.CPRequest.InvalidQueryResponseException;

class PlayerProfiles {

	private static Map<UUID, WrappedSignedProperty> textures;
	private static Date dateLoad;
	private static boolean enabled;

	static void enable()
	{
		textures = new HashMap<UUID, WrappedSignedProperty>();
		dateLoad = new Date();
		// Si pas de fantômes ou offline-mode, pas la peine d'activer ce bordel
		if (!Config.getConfig().getBoolean("game.fetch ghosts skins") || !CreativeParkour.protocollibPresent() || !Config.fantomesPasInterdits() || !CreativeParkour.auMoins1_9() || !Bukkit.getOnlineMode())
		{
			enabled = false;
			return;
		}
		enabled = true;


		// Téléchargement des profils après un délai
		Bukkit.getScheduler().runTaskTimerAsynchronously(CreativeParkour.getPlugin(), new Runnable() {
			public void run() {
				CreativeParkour.debug("PP1", "Updating player profiles...");
				long limiteUpdate = new Date().getTime() - 1000 * 60 * 60 * 24 * 3;
				List<UUID> textToDownload = new ArrayList<UUID>();
				for (Entry<String, YamlConfiguration> e : Config.getConfigsJoueurs().entrySet())
				{
					YamlConfiguration conf = e.getValue();
					if (!offlinePlayer(conf))
					{
						if (conf.getLong("profile.update", 0) < limiteUpdate) // S'il a été mis à jour il y a plus de 3 jours, on le retélécharge
						{
							textToDownload.add(UUID.fromString(e.getKey()));
						}
					}
				}
				chargerProfils(textToDownload, false, true);
			}
		}, 20 * 45, 20 * 60 * 60 * 24 * 3); // 45 secondes, 3 jours
	}

	static WrappedSignedProperty getTextures(UUID joueur)
	{
		if (!enabled)
			return null;
		
		WrappedSignedProperty r = textures.get(joueur);
		if (r != null)
			return r;

		// Recherche dans les fichiers
		YamlConfiguration conf = Config.getConfJoueur(joueur.toString());
		if (conf != null)
		{
			if (!offlinePlayer(conf)) // S'il a des textures (un compte premium)
			{
				String texturesS = conf.getString("profile.textures");
				String signature = conf.getString("profile.signature");
				if (texturesS != null && !texturesS.isEmpty() && signature != null && !signature.isEmpty())
				{
					WrappedSignedProperty text = new WrappedSignedProperty("textures", texturesS, signature);
					textures.put(joueur, text);
					return text;
				}
			}
			else
				textures.put(joueur, null);
		}
		return null;
	}


	/**
	 * @param liste Players' times to download textures
	 * @param memoriser Whether or not put textures in memory (or just write them in files).
	 */
	static void chargerProfils(Set<CPTime> liste, boolean memoriser)
	{
		List<UUID> uuids = new ArrayList<UUID>();
		for (CPTime t : liste)
		{
			uuids.add(t.playerUUID);
		}
		chargerProfils(uuids, memoriser, false);
	}

	/**
	 * @param joueurs Players to download textures
	 * @param memoriser Whether or not put textures in memory (or just write them in files).
	 * @param forcerTelechargement If true, it downloads the texture without checking if it already exists.
	 */
	static void chargerProfils(List<UUID> joueurs, boolean memoriser, boolean forcerTelechargement)
	{
		if (enabled && Config.getConfig().getBoolean("game.fetch ghosts skins") && joueurs.size() > 0)
		{
			if (dateLoad.getTime() < new Date().getTime() - 1000 * 60 * 60 * 25) // Vidange toutes les 25 heures
			{
				textures.clear();
				dateLoad = new Date();
			}

			// Requête
			Map<String, String> paramsPost = new HashMap<String, String>();
			StringBuffer uuids = new StringBuffer();

			for (UUID uuid : joueurs)
			{
				if (forcerTelechargement || (getTextures(uuid) == null && !textures.containsKey(uuid))) // Si on ne l'a vraiment pas (et que c'est un compte premium avec des textures)
					uuids.append(uuid.toString() + ";");
			}
			if (uuids.length() > 0) // On ne fait rien sinon
			{
				uuids.deleteCharAt(uuids.length() - 1);
				paramsPost.put("uuids", uuids.toString());

				try {
					CPRequest.effectuerRequete("player-profiles.php", paramsPost, null, PlayerProfiles.class.getMethod(memoriser ? "reponseProfils" : "reponseProfilsSansMemorisation", JsonObject.class, String.class, Player.class), null);
				} catch (NoSuchMethodException | SecurityException e) {
					CreativeParkour.erreur("PLAYERPROFILES", e, true);
				}
			}
		}
	}

	/**
	 * <em>Third-party plugins cannot use this method through CreativeParkour's API (it will throw an {@code InvalidQueryResponseException}).</em><br>
	 * Method called when <a href="https://creativeparkour.net" target="_blank">creativeparkour.net</a> responds to a query.
	 * @param json
	 * @param rep
	 * @param inutile
	 * @throws InvalidQueryResponseException If the {@code Request} has not been registered before.
	 */
	public static void reponseProfilsSansMemorisation(JsonObject json, String rep, Player inutile) throws InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponseProfilsSansMemorisation"))
			reponseProfils(json, rep, false);
	}

	/**
	 * <em>Third-party plugins cannot use this method through CreativeParkour's API (it will throw an {@code InvalidQueryResponseException}).</em><br>
	 * Method called when <a href="https://creativeparkour.net" target="_blank">creativeparkour.net</a> responds to a query.
	 * @param json
	 * @param rep
	 * @param inutile
	 * @param memoriser Whether or not put textures in memory (or just write them in files).
	 * @throws InvalidQueryResponseException If the {@code Request} has not been registered before.
	 */
	public static void reponseProfils(JsonObject json, String rep, Player inutile) throws InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponseProfils"))
			reponseProfils(json, rep, true);
	}

	private static void reponseProfils(JsonObject json, String rep, boolean memoriser) throws InvalidQueryResponseException
	{
		if (!CreativeParkour.erreurRequete(json, null) && json.get("data") != null)
		{
			if (json.get("data").isJsonObject())
			{
				JsonElement o = json.get("data").getAsJsonObject().get("profils");
				if (o != null && o.isJsonArray())
				{
					JsonArray liste = o.getAsJsonArray();
					for (JsonElement e : liste)
					{
						JsonObject obj = e.getAsJsonObject();
						UUID uuid = null;
						try {
							uuid = UUID.fromString(obj.get("uuid").getAsString());
						} catch (IllegalArgumentException ex) {
							// Rien
						}
						ajouterTextures(uuid, obj.get("textures").getAsString(), obj.get("signature").getAsString(), memoriser);
					}
				}
			}
		}
	}

	/**
	 * @param uuid Player UUID
	 * @param texturesS Textures
	 * @param signature Signature
	 * @param memoriser Whether or not put textures in memory (or just write them in files).
	 */
	static void ajouterTextures(UUID uuid, String texturesS, String signature, boolean memoriser)
	{
		if (enabled && uuid != null)
		{
			YamlConfiguration conf = Config.getConfJoueur(uuid.toString());
			if (texturesS != null && !texturesS.isEmpty() && signature != null && !signature.isEmpty())
			{
				if (memoriser)
					textures.put(uuid, new WrappedSignedProperty("textures", texturesS, signature));
				conf.set("profile.textures", texturesS);
				conf.set("profile.signature", signature);
			}
			else
			{
				if (memoriser)
					textures.put(uuid, null); // On le met quand même pour ne pas refaire la requête plein de fois (c'est peut-être un offline-mode)
				conf.set("profile.textures", false);
				conf.set("profile.signature", false);
			}
			//CreativeParkour.debug("PP2", NameManager.getNomAvecUUID(uuid) + "'s profile received.");
			conf.set("profile.update", new Date().getTime());
			Config.saveConfJoueur(uuid.toString());
		}
	}

	/**
	 * Returns {@code true} if the player is not a premium Minecraft account and does not have textures.
	 * @param confJoueur CrreativeParkour configuration for the player.
	 * @return
	 */
	private static boolean offlinePlayer(YamlConfiguration confJoueur)
	{
		return confJoueur.isBoolean("profile.textures") && confJoueur.getBoolean("profile.textures") == false;
	}
}
