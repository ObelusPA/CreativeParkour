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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.Metrics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Used by CreativeParkour to interact with <a href="https://creativeparkour.net">creativeparkour.net</a>. Nothing useful for the API.
 * @author Obelus
 */
public class CPRequest implements Runnable
{

	private static List<BukkitTask> tasks = new ArrayList<BukkitTask>();
	private static List<String> methodesEnAttente = new ArrayList<String>();

	static void effectuerRequete(String adresse, Map<String, String> paramsPost2, Object objetAppelant, Method methodeRetour, Player joueurConcerne)
	{
		paramsPost2.put("versionPlugin", CreativeParkour.getPlugin().getDescription().getVersion());
		paramsPost2.put("uuidServ", Config.getServUUID());
		// ***********************************************************************************************************************************************************************************************************
		// NEVER TRY TO ACCESS THIS URL YOURSELF. Just don't try.
		// This adress is used for remote features and statistics. Everything is detailed on the plugin page: https://dev.bukkit.org/projects/creativeparkour#title-7-3
		// ***********************************************************************************************************************************************************************************************************
		CPRequest r = new CPRequest("https://creativeparkour.net/api/" + CreativeParkour.getPlugin().getDescription().getVersion() + "/" + adresse, paramsPost2, objetAppelant, methodeRetour, joueurConcerne);
		// ***********************************************************************************************************************************************************************************************************
		// NEVER TRY TO ACCESS THIS URL YOURSELF
		// ***********************************************************************************************************************************************************************************************************
		BukkitTask t = Bukkit.getServer().getScheduler().runTaskAsynchronously(CreativeParkour.getPlugin(), r);
		tasks.add(t);
		r.setTask(t);
		if (methodeRetour != null)
		{
			methodesEnAttente.add(methodeRetour.getName());
		}
	}

	static String messageAttente()
	{
		return ChatColor.GRAY + "" + ChatColor.ITALIC + Langues.getMessage("waiting for servers");
	}

	static void annulerRequetes()
	{
		for (int i=0; i < tasks.size(); i++)
		{
			tasks.get(i).cancel();
		}
	}
	
	/**
	 * Vérifie que la requête correspondant à la méthode de retour donnée ait bien été effectuée plus tôt.
	 * @param nom Nom de la méthode
	 * @return true si la requête avait été effectuée
	 * @throws InvalidQueryResponseException
	 */
	static boolean verifMethode(String nom) throws InvalidQueryResponseException
	{
		if (methodesEnAttente.remove(nom))
			return true;
		else
			throw new InvalidQueryResponseException("Unknown request. Third-party plugins can not use this method.");
	}


	/**
	 * Thrown when trying to call a query response method without creating and running the {@code Request} before.<br>
	 * Those query response methods should not be used by third-party plugins using the API.
	 */
	public static class InvalidQueryResponseException extends Exception
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -2744323846442918388L;

		public InvalidQueryResponseException(String message) {
			super(message);
		}
	}



	private Plugin plugin;
	private String adresse;
	private Map<String, String> paramsPost;
	private Method methodeRetour;
	private Object objetAppelant;
	private Player joueurConcerne;
	private BukkitTask task;

	private CPRequest(String adresse, Map<String, String> paramsPost2, Object objetAppelant, Method methodeRetour, Player joueurConcerne)
	{
		this.plugin = CreativeParkour.getPlugin();
		this.adresse = adresse;
		this.paramsPost = paramsPost2;
		this.methodeRetour = methodeRetour;
		this.objetAppelant = objetAppelant;
		this.joueurConcerne = joueurConcerne;
	}

	private void setTask(BukkitTask t)
	{
		this.task = t;
	}

	@Override
	public void run()
	{
		JsonObject objJson = null;
		String reponse = new String();
		int repCode = 600;
		try {
			String urlParameters = "";
			for (Entry<String, String> entry : paramsPost.entrySet()) {
				if (entry.getValue() == null)
					entry.setValue("null");
				urlParameters += entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8") + "&";
			}


			// Connect to the website
			HttpURLConnection connection = (HttpsURLConnection) new URL(adresse).openConnection();
			int tentatives = 0;
			boolean ok = false;
			while (tentatives  < 2 && !ok)
			{
				try {

					byte[] compressed = Metrics.gzip(urlParameters);

					// Headers
					connection.setRequestMethod("POST");
					connection.addRequestProperty("User-Agent", CreativeParkour.getPlugin().getDescription().getFullName());
					//connection.addRequestProperty("Content-Type", "application/json");
					//connection.addRequestProperty("Content-Encoding", "gzip");
					connection.addRequestProperty("Accept-Encoding", "gzip, deflate");
					connection.addRequestProperty("Content-Length", Integer.toString(compressed.length));
					//connection.addRequestProperty("Accept", "application/json");
					//connection.addRequestProperty("Connection", "close");
					connection.setRequestProperty("charset", "utf-8");
					connection.setRequestProperty("Transfer-Encoding","chunked");

					connection.setDoOutput(true);

					// Write the data
					OutputStream os = connection.getOutputStream();
					os.write(compressed);
					os.flush();

					// Now read the response
					repCode = connection.getResponseCode();
					final BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream())));
					String line;
					while ((line = reader.readLine()) != null) {
						reponse += line + "\n";
					}

					// close resources
					os.close();
					reader.close();
					ok = true;
				} catch (Exception e) {
					adresse = adresse.replace("https://", "http://");
					connection = (HttpURLConnection) new URL(adresse).openConnection();
					tentatives++;
					if (tentatives == 2)
					{
						e.printStackTrace();
					}
				}
			}

			JsonParser parser = new JsonParser();
			objJson = parser.parse(reponse).getAsJsonObject();
		} catch (IllegalStateException | JsonSyntaxException e) {
			// Rien
		} catch (Exception e) {
			reponse += CreativeParkour.exceptionToString(e);
		}

		if (methodeRetour != null)
		{
			// Invocation de la méthode retour
			final String reponseFinal = reponse;
			final JsonObject objJsonFinal = objJson;
			final int repCodeFinal = repCode;
			try {
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						try {
							if (repCodeFinal == 200)
								methodeRetour.invoke(objetAppelant, objJsonFinal, reponseFinal, joueurConcerne);
							else if (joueurConcerne != null)
							{
								String raison = "HTTP error " + String.valueOf(repCodeFinal);
								if (repCodeFinal == 403)
									raison = "Forbidden";
								if (repCodeFinal != 503)
									joueurConcerne.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("http error").replace("%error", raison));
								else
									joueurConcerne.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("http error maintenance"));
							}
							else
								CreativeParkour.debug("REQ", Config.prefix(false) + Langues.getMessage("http error").replace("%error", String.valueOf(repCodeFinal)));
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							CreativeParkour.erreur("REQUETE", e, true);
						}
					}
				});
			} catch (IllegalPluginAccessException e) { }
		}

		tasks.remove(this.task);
	}
}