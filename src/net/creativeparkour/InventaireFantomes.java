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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonObject;

import net.creativeparkour.CPRequest.InvalidQueryResponseException;
import net.md_5.bungee.api.ChatColor;


class InventaireFantomes
{
	private enum ActionInv { PAGE_PRECEDENTE, PAGE_SUIVANTE, VIDER_SELECTION, CHOISIR_MEILLEUR, PAS_CHOISIR_MEILLEUR, CHOISIR_PERSO, PAS_CHOISIR_PERSO, ACTIVER_ENVOI, DESACTIVER_ENVOI, AFFICHER_AIDE, SIGNALER, AFFICHER_AVANT, PAS_AFFICHER_AVANT };

	private Map<Integer, CPTime> elements = new Hashtable<Integer, CPTime>(); // Integer = slot de la tête
	private Map<Integer, ActionInv> speciaux = new Hashtable<Integer, ActionInv>();
	private List<CPTime> fantomes;
	private int page = 1;
	private Inventory inv;
	private Joueur j;
	private final SimpleDateFormat formatDate;
	private boolean fantomesTelecharges;

	InventaireFantomes (SortedSet<CPTime> sortedSet, Joueur j)
	{
		this.fantomesTelecharges = false;
		this.fantomes = new ArrayList<CPTime>();
		for (CPTime t : sortedSet)
		{
			if (t.hasGhost())
				this.fantomes.add(t);
			if (t.etat == EtatTemps.DOWNLOADED || t.etat == EtatTemps.TO_DOWNLOAD)
				this.fantomesTelecharges = true;
		}
		this.j = j;
		this.formatDate = new SimpleDateFormat ("dd MMMM yyyy, hh:mm a zzz");
		this.formatDate.setTimeZone(TimeZone.getDefault());
	}

	void mettreAJour()
	{
		setPage(page);
	}

	void setPage(int page)
	{
		Player p = j.getPlayer();
		this.page = page;
		final int nbFantomes = fantomes.size();

		final int tailleInv = 54;
		// Calcul du nombre de pages
		int nbPages = (int) (Math.ceil((double)nbFantomes/45));
		if (nbPages == 0)
			nbPages = 1;
		if (page > nbPages)
			page = nbPages;
		final int limiteMin = 45 * (page-1);
		final int limiteMax = (45 * page > nbFantomes) ? nbFantomes : 45 * page;

		if (inv == null)
			inv = Bukkit.createInventory(p, tailleInv, Langues.getMessage("play.ghosts.title"));
		
		final Map<Integer, ItemStack> itemsAMettre = new HashMap<Integer, ItemStack>();
		final int pageF = page;
		final int nbPagesF = nbPages;
		Bukkit.getScheduler().runTaskAsynchronously(CreativeParkour.getPlugin(), new Runnable() {
			public void run() {
		boolean continuer = true;
		int nbItems = 0;
		elements.clear();
		speciaux.clear();
		Collections.sort(fantomes);
		int slot = 0;
		for (int i=limiteMin; continuer && i < nbFantomes; i++)
		{
			CPTime t = fantomes.get(i);
			ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
			SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			String nomFantome = t.getPlayerName();
			meta.setOwner(nomFantome);
			meta.setDisplayName(ChatColor.AQUA + nomFantome);
			List<String> lore = new ArrayList<String>();
			// Secondes
			lore.add(ChatColor.YELLOW + "" + t.inSeconds() + "s");
			// Date
			if (t.date != null && t.date.getTime() > 0)
			{
				lore.add(Langues.getMessage("play.ghosts.date") + ": " + formatDate.format(t.date));
			}
			// Si le fantome est choisi ou pas
			if (j.tempsFantomesChoisis.contains(t))
			{
				lore.add(ChatColor.GREEN + Langues.getMessage("play.ghosts.selected"));
				lore.add(ChatColor.ITALIC + "" + ChatColor.GRAY + Langues.getMessage("play.ghosts.unselect"));
			}
			else
				lore.add(ChatColor.ITALIC + "" + ChatColor.GRAY + Langues.getMessage("play.ghosts.select"));
			meta.setLore(lore);
			item.setItemMeta(meta);
			itemsAMettre.put(slot, item);
			elements.put(slot, t);
			slot++;
			nbItems++;

			if (nbItems >= limiteMax || slot >= 45)
				continuer = false;
		}

		if (pageF > 1)
		{
			ItemStack item = new ItemStack(Material.SLIME_BALL);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + Langues.getMessage("play.page") + " " + (pageF - 1));
			item.setItemMeta(meta);
			itemsAMettre.put(tailleInv - 9, item);
			speciaux.put(tailleInv - 9, ActionInv.PAGE_PRECEDENTE);
		}
		if (pageF < nbPagesF)
		{
			ItemStack item = new ItemStack(Material.ENDER_PEARL);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + Langues.getMessage("play.page") + " " + (pageF + 1));
			item.setItemMeta(meta);
			itemsAMettre.put(tailleInv - 1, item);
			speciaux.put(tailleInv - 1, ActionInv.PAGE_SUIVANTE);
		}
		ItemStack item = new ItemStack(Material.EYE_OF_ENDER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + Langues.getMessage("play.page") + " " + pageF + "/" + nbPagesF);
		List<String> lore = new ArrayList<String>();
		lore.add(nbFantomes + " " + Langues.getMessage("play.ghosts.ghosts"));
		lore.add(ChatColor.WHITE + "" + ChatColor.ITALIC + Langues.getMessage("play.ghosts.help item"));
		meta.setLore(lore);
		item.setItemMeta(meta);
		itemsAMettre.put(tailleInv - 5, item);
		speciaux.put(tailleInv - 5, ActionInv.AFFICHER_AIDE);

		if (fantomesTelecharges && Config.online())
		{
			item = new ItemStack(Material.INK_SACK, 1, (short) 1);
			meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.RED + Langues.getMessage("play.ghosts.report item"));
			lore = new ArrayList<String>();
			lore.add(ChatColor.WHITE + "" + ChatColor.ITALIC + Langues.getMessage("play.ghosts.report item info"));
			meta.setLore(lore);
			item.setItemMeta(meta);
			itemsAMettre.put(tailleInv - 2, item);
			ActionInv a = ActionInv.SIGNALER;
			speciaux.put(tailleInv - 2, a);
		}


		// Objets d'options
		String texte = Langues.getMessage("play.ghosts.display before");
		ActionInv a;
		if (!j.getParamBool(PlayerSetting.AFFICHER_FANTOMES_AVANT))
			a = ActionInv.AFFICHER_AVANT;
		else
			a = ActionInv.PAS_AFFICHER_AVANT;
		texte += " (" + Langues.getMessage(a == ActionInv.AFFICHER_AVANT ? "play.ghosts.disabled" : "play.ghosts.enabled") + ")";
		item = new ItemStack(Material.INK_SACK, 1, (short) (a == ActionInv.AFFICHER_AVANT ? 8 : 10));
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + texte);
		lore = new ArrayList<String>();
		lore.add(ChatColor.WHITE + "" + ChatColor.ITALIC + Langues.getMessage("play.ghosts.display before info"));
		meta.setLore(lore);
		item.setItemMeta(meta);
		itemsAMettre.put(tailleInv - 4, item);
		speciaux.put(tailleInv - 4, a);


		item = new ItemStack(Material.INK_SACK, 1, (short) 5);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.LIGHT_PURPLE + Langues.getMessage("play.ghosts.clear"));
		item.setItemMeta(meta);
		itemsAMettre.put(tailleInv - 3, item);
		a = ActionInv.VIDER_SELECTION;
		speciaux.put(tailleInv - 3, a);


		texte = Langues.getMessage("play.ghosts.select personal");
		if (!j.getParamBool(PlayerSetting.CHOISIR_FANTOME_PERSO))
			a = ActionInv.CHOISIR_PERSO;
		else
			a = ActionInv.PAS_CHOISIR_PERSO;
		texte += " (" + Langues.getMessage(a == ActionInv.CHOISIR_PERSO ? "play.ghosts.disabled" : "play.ghosts.enabled") + ")";
		item = new ItemStack(Material.INK_SACK, 1, (short) (a == ActionInv.CHOISIR_PERSO ? 8 : 10));
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + texte);
		item.setItemMeta(meta);
		itemsAMettre.put(tailleInv - 7, item);
		speciaux.put(tailleInv - 7, a);

		texte = Langues.getMessage("play.ghosts.select best");
		if (!j.getParamBool(PlayerSetting.CHOISIR_MEILLEUR_FANTOME))
			a = ActionInv.CHOISIR_MEILLEUR;
		else
			a = ActionInv.PAS_CHOISIR_MEILLEUR;
		texte += " (" + Langues.getMessage(a == ActionInv.CHOISIR_MEILLEUR ? "play.ghosts.disabled" : "play.ghosts.enabled") + ")";
		item = new ItemStack(Material.INK_SACK, 1, (short) (a == ActionInv.CHOISIR_MEILLEUR ? 8 : 10));
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + texte);
		item.setItemMeta(meta);
		itemsAMettre.put(tailleInv - 6, item);
		speciaux.put(tailleInv - 6, a);

		if (Config.online() && Config.getConfig().getBoolean("online.upload ghosts"))
		{
			texte = Langues.getMessage("play.ghosts.upload");
			if (!j.getParamBool(PlayerSetting.ENVOYER_FANTOMES))
				a = ActionInv.ACTIVER_ENVOI;
			else
				a = ActionInv.DESACTIVER_ENVOI;
			texte += " (" + Langues.getMessage(a == ActionInv.ACTIVER_ENVOI ? "play.ghosts.disabled" : "play.ghosts.enabled") + ")";
			item = new ItemStack(Material.INK_SACK, 1, (short) (a == ActionInv.ACTIVER_ENVOI ? 8 : 10));
			meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.YELLOW + texte);
			lore = new ArrayList<String>();
			lore.add(ChatColor.WHITE + "" + ChatColor.ITALIC + Langues.getMessage("play.ghosts.upload info"));
			meta.setLore(lore);
			item.setItemMeta(meta);
			itemsAMettre.put(tailleInv - 8, item);
			speciaux.put(tailleInv - 8, a);
		}
		
		// Remplissage de l'inventaire avec les objets
		Bukkit.getScheduler().scheduleSyncDelayedTask(CreativeParkour.getPlugin(), new Runnable() {
			public void run() {
				inv.clear();
				for (Entry<Integer, ItemStack> e : itemsAMettre.entrySet())
				{
					inv.setItem(e.getKey(), e.getValue());
				}
			}
		});
	}
});
	}

	Inventory getInventaire()
	{
		return inv;
	}

	void clic(int slot, ClickType clickType) throws NoSuchMethodException, SecurityException
	{
		CPTime t = elements.get(slot);
		ActionInv action = speciaux.get(slot);
		if (t != null)
		{
			if (j.tempsFantomesChoisis.contains(t))
				j.tempsFantomesChoisis.remove(t);
			else
			{
				if (j.tempsFantomesChoisis.size() >= Config.getConfig().getInt("game.max ghosts"))
					j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.ghosts.limit"));
				else
					j.tempsFantomesChoisis.add(t);
			}
			mettreAJour();
			j.majTeteFantomes();
		}
		else if (action != null)
		{
			if (action == ActionInv.PAGE_PRECEDENTE)
				setPage(page - 1);
			else if (action == ActionInv.PAGE_SUIVANTE)
				setPage(page + 1);
			else if (action == ActionInv.CHOISIR_MEILLEUR)
			{
				j.setParam(PlayerSetting.CHOISIR_MEILLEUR_FANTOME, true);
				mettreAJour();
			}
			else if (action == ActionInv.PAS_CHOISIR_MEILLEUR)
			{
				j.setParam(PlayerSetting.CHOISIR_MEILLEUR_FANTOME, false);
				mettreAJour();
			}
			else if (action == ActionInv.CHOISIR_PERSO)
			{
				j.setParam(PlayerSetting.CHOISIR_FANTOME_PERSO, true);
				mettreAJour();
			}
			else if (action == ActionInv.PAS_CHOISIR_PERSO)
			{
				j.setParam(PlayerSetting.CHOISIR_FANTOME_PERSO, false);
				mettreAJour();
			}
			else if (action == ActionInv.ACTIVER_ENVOI)
			{
				j.setParam(PlayerSetting.ENVOYER_FANTOMES, true);
				mettreAJour();
			}
			else if (action == ActionInv.DESACTIVER_ENVOI)
			{
				j.setParam(PlayerSetting.ENVOYER_FANTOMES, false);
				mettreAJour();
			}
			else if (action == ActionInv.AFFICHER_AVANT)
			{
				j.setParam(PlayerSetting.AFFICHER_FANTOMES_AVANT, true);
				mettreAJour();
			}
			else if (action == ActionInv.PAS_AFFICHER_AVANT)
			{
				j.setParam(PlayerSetting.AFFICHER_FANTOMES_AVANT, false);
				mettreAJour();
			}
			else if (action == ActionInv.VIDER_SELECTION)
			{
				j.tempsFantomesChoisis.clear();
				mettreAJour();
				j.majTeteFantomes();
			}
			else if (action == ActionInv.AFFICHER_AIDE)
			{
				j.getPlayer().closeInventory();
				j.getPlayer().sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("play.ghosts.help text"));
			}
			else if (action == ActionInv.SIGNALER)
			{
				j.getPlayer().closeInventory();
				signaler();
			}
		}
	}

	void signaler() throws NoSuchMethodException, SecurityException
	{
		if (Config.online())
		{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("ipJoueur", j.getPlayer().getAddress().getHostName());
			params.put("uuidJoueur", j.getPlayer().getUniqueId().toString());
			params.put("nomJoueur", j.getPlayer().getName());
			params.put("uuidMap", j.getMapObjet().getUUID().toString());
			CPRequest.effectuerRequete("report-ghost.php", params, this, this.getClass().getMethod("reponseSignalement", JsonObject.class, String.class, Player.class), j.getPlayer());
			j.getPlayer().sendMessage(Config.prefix() + CPRequest.messageAttente());
		}
		else
			j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("feature disabled"));
	}

	
	/**
	 * <em>Third-party plugins cannot use this method through CreativeParkour's API (it will throw an {@code InvalidQueryResponseException}).</em><br>
	 * Method called when <a href="https://creativeparkour.net" target="_blank">creativeparkour.net</a> responds to a query.
	 * @param json
	 * @param rep
	 * @param p
	 * @throws InvalidQueryResponseException If the {@code Request} has not been registered before.
	 */
	public void reponseSignalement(JsonObject json, String rep, Player p) throws InvalidQueryResponseException
	{
		if (CPRequest.verifMethode("reponseSignalement") && !CreativeParkour.erreurRequete(json, p))
		{
			if (json.get("data").getAsJsonObject().has("cle"))
				CPUtils.sendClickableMsg(p, Langues.getMessage("play.ghosts.report link"), null, CreativeParkour.lienSite() + "/user/report-ghost.php?c=" + json.get("data").getAsJsonObject().get("cle").getAsString(), "%L", ChatColor.YELLOW);
		}
	}
}