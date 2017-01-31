package net.creativeparkour;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.creativeparkour.CPRequest.InvalidQueryResponseException;

class PlayerProfiles {

	private static Map<UUID, WrappedSignedProperty> textures;
	private static Date dateLoad;

	static void enable()
	{
		textures = new HashMap<UUID, WrappedSignedProperty>();
		dateLoad = new Date();
	}

	static WrappedSignedProperty getTextures(UUID joueur)
	{
		return textures.get(joueur);
	}

	static void chargerProfils(Set<CPTime> liste)
	{
		List<UUID> uuids = new ArrayList<UUID>();
		for (CPTime t : liste)
		{
			uuids.add(t.playerUUID);
		}
		chargerProfils(uuids);
	}

	static void chargerProfils(List<UUID> joueurs)
	{
		if (Config.getConfig().getBoolean("game.fetch ghosts skins") && joueurs.size() > 0)
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
				if (!textures.containsKey(uuid))
					uuids.append(uuid.toString() + ";");
			}
			if (uuids.length() > 0) // On ne fait rien sinon
			{
				uuids.deleteCharAt(uuids.length() - 1);
				paramsPost.put("uuids", uuids.toString());

				try {
					CPRequest.effectuerRequete("player-profiles.php", paramsPost, null, PlayerProfiles.class.getMethod("reponseProfils", JsonObject.class, String.class, Player.class), null);
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
	public static void reponseProfils(JsonObject json, String rep, Player inutile) throws InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponseProfils") && !CreativeParkour.erreurRequete(json, null) && json.get("data") != null)
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
						ajouterTextures(uuid, obj.get("textures").getAsString(), obj.get("signature").getAsString());
					}
				}
			}
		}
	}

	static void ajouterTextures(UUID uuid, String texturesS, String signature)
	{
		if (uuid != null)
		{
			if (texturesS != null && !texturesS.isEmpty() && signature != null && !signature.isEmpty())
				textures.put(uuid, new WrappedSignedProperty("textures", texturesS, signature));
			else
				textures.put(uuid, null); // On le met quand même pour ne pas refaire la requête plein de fois (c'est peut-être un offline-mode)
		}
	}
}
