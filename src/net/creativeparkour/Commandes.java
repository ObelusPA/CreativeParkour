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
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

class Commandes implements CommandExecutor, Listener
{
	private static Map<String, Commande> commands;
	private static List<JoueurCommande> joueursCommande = new ArrayList<JoueurCommande>();

	static void enable()
	{
		commands = new HashMap<String, Commande>();

		addCommand("play", "creativeparkour.play", true);
		addCommand("create", "creativeparkour.create", true);
		addCommand("leave", null, true);
		addCommand("difficulty", "creativeparkour.rate.difficulty", true);
		addCommand("quality", "creativeparkour.rate.quality", true);
		addCommand("invite", "creativeparkour.invite", true);
		addCommand("remove", "creativeparkour.invite", true);
		addCommand("contributors", "creativeparkour.invite", true);
		addCommand("download", "creativeparkour.download", false);
		addCommand("cpd", "cpd", null, "creativeparkour.download", false);
		addCommand("tp", "creativeparkour.play", true);
		addCommand("test", "creativeparkour.create", true);
		addCommand("mapoptions", "creativeparkour.create", true);
		addCommand("spectator", "creativeparkour.spectate", true);
		addCommand("publish", "creativeparkour.create", true);
		addCommand("delete", null, true);
		addCommand("share", "creativeparkour.share", true);
		addCommand("edit", "creativeparkour.create", true);
		addCommand("export", "creativeparkour.manage", true);
		addCommand("settings", null, true);
		addCommand("importsel", "creativeparkour.manage", true);
		addCommand("ghost", "creativeparkour.ghosts.see", true);
		addCommand("register", null, true);
		addCommand("removetime", "creativeparkour.manage", true);
		addCommand("getid", null, true);
		addCommand("claim", null, true);
		addCommand("managemaps", "creativeparkour.manage", true);
		addCommand("ban", "creativeparkour.manage", false);
		addCommand("pardon", "creativeparkour.manage", false);
		addCommand("pin", "creativeparkour.manage", true);
		addCommand("unpin", "creativeparkour.manage", true);
		addCommand("notifications", null, true);
		addCommand("messages", null, true);
		addCommand("sync", "creativeparkour.manage", false);
		addCommand("noplates", "creativeparkour.manage", true);
		addCommand("version", null, false);
		addCommand("config", "creativeparkour.*", true);
		addCommand("language", "creativeparkour.*", false);
		addCommand("help", null, false);
		addCommand("yes", null, true);
		addCommand("no", null, true);
		addCommand("enable", "creativeparkour.*", false);
		addCommand("disable", "creativeparkour.*", false);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (Config.getConfig().getBoolean("dont use cp") && label.equalsIgnoreCase("cp"))
		{
			return true;
		}
		else
		{
			try {
				if (args.length > 0 || cmd.getName().equalsIgnoreCase("cpd"))
				{
					if (Config.pluginActive() || commandeEffectuee(sender, cmd, args, "config", false))
					{
						if (commandeEffectuee(sender, cmd, args, "play"))
						{
							if (commandeAutorisee(sender, "play"))
							{
								if (Config.getMonde() == null)
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.plugin not enabled"));
									if (sender.hasPermission("creativeparkour.*"))
									{
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.config tutorial"));
									}
								}
								else
								{
									String suiteArgs = new String("");
									for (int i=1; i < args.length; i++)
									{
										suiteArgs += " " + args[i];
									}
									GameManager.jouer(getPlayer(sender), suiteArgs.trim());
								}
							}
							return true;
						}
						else if (commandeEffectuee(getPlayer(sender), cmd, args, "create"))
						{
							if (commandeAutorisee(sender, "create"))
							{
								if (Config.getMonde() == null)
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.plugin not enabled"));
									if (sender.hasPermission("creativeparkour.*"))
									{
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.config tutorial"));
									}
								}
								else
								{
									if (args.length > 1 && sender.hasPermission("creativeparkour.manage"))
									{
										try {
											CPMap m = GameManager.getMap(Integer.valueOf(args[1]));
											if (m != null && m.getState() == CPMapState.CREATION)
											{
												m.accepterInvitation(getPlayer(sender));
												return true;
											}
										} catch (NumberFormatException e) {
											// Rien
										}
										// Si on arrive là, ça n'a pas marché
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.create map error"));
									}

									GameManager.creerMap(getPlayer(sender));
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "leave"))
						{
							if (commandeAutorisee(sender, "leave"))
							{
								GameManager.supprJoueur(getPlayer(sender), true);
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "difficulty"))
						{
							if (commandeAutorisee(sender, "difficulty"))
							{
								if (Config.getConfig().getBoolean("game.enable map rating") && args.length > 1 && sender.hasPermission("creativeparkour.rate.difficulty"))
								{
									try {
										int nb = Integer.valueOf(args[1]);
										if (nb >= 1 && nb <= 5)
											GameManager.voteDifficulte(getPlayer(sender), nb);
									} catch (NumberFormatException e) {
										// Rien
									}
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "quality"))
						{
							if (commandeAutorisee(sender, "quality"))
							{
								if (Config.getConfig().getBoolean("game.enable map rating") && args.length > 1 && sender.hasPermission("creativeparkour.rate.quality"))
								{
									try {
										int nb = Integer.valueOf(args[1]);
										if (nb >= 1 && nb <= 5)
											GameManager.voteQualite(getPlayer(sender), nb);
									} catch (NumberFormatException e) {
										// Rien
									}
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "invite"))
						{
							if (commandeAutorisee(sender, "invite"))
							{
								Joueur j = GameManager.getJoueur(getPlayer(sender));
								if (j == null || j.getMapObjet() == null || j.getMapObjet().getState() != CPMapState.CREATION)
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 1"));
								}
								else if (!j.getMapObjet().getCreator().equals(j.getUUID()) && !sender.hasPermission("creativeparkour.manage"))
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 4"));
								}
								else if (args.length > 1)
								{
									Player p1 = Bukkit.getPlayer(args[1]);
									if (p1 == null)
									{
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
										sender.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getCommand("invite") + " <" + Langues.getMessage("commands.playerN") + ">");
									}
									else if (!p1.hasPermission("creativeparkour.create") || Config.isBanned(p1))
									{
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 5"));
									}
									else if (j.getMapObjet().getCreator().equals(p1.getUniqueId()))
									{
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 3"));
									}
									else
									{
										j.getMapObjet().inviter(j.getPlayer(), p1);
										CPUtils.sendInfoMessage(j.getPlayer(), Langues.getMessage("commands.contributors info"));
									}
								}
								else
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
									sender.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getCommand("invite") + " <" + Langues.getMessage("commands.playerN") + ">");
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "remove"))
						{
							if (commandeAutorisee(sender, "remove"))
							{
								Joueur j = GameManager.getJoueur(getPlayer(sender));
								if (j == null || j.getMap() == null)
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.test error"));
								}
								else if (j.getMapObjet().getState() != CPMapState.CREATION)
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 1"));
								}
								else if (!j.getMapObjet().getCreator().equals(j.getUUID()) && !sender.hasPermission("creativeparkour.manage"))
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 4"));
								}
								else if (args.length > 1)
								{
									UUID p1 = NameManager.getUuidAvecNom(args[1]);
									if (p1 == null)
									{
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
										sender.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getCommand("remove") + " <" + Langues.getMessage("commands.playerN") + ">");
									}
									else if (j.getMapObjet().getCreator().equals(p1))
									{
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 3"));
									}
									else
									{
										j.getMapObjet().supprContibuteur(j.getPlayer(), p1);
									}
								}
								else
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
									sender.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getCommand("remove") + " <" + Langues.getMessage("commands.playerN") + ">");
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "contributors"))
						{
							if (commandeAutorisee(sender, "contributors"))
							{
								Joueur j = GameManager.getJoueur(getPlayer(sender));
								if (j == null || j.getMapObjet() == null || j.getMapObjet().getState() != CPMapState.CREATION)
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 1"));
								}
								else if (!j.getMapObjet().getCreator().equals(j.getUUID()) && !sender.hasPermission("creativeparkour.manage"))
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 4"));
								}
								else 
								{
									j.afficherListeContributeurs();
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "download") || commandeEffectuee(sender, cmd, args, "cpd"))
						{		
							if (commandeAutorisee(sender, "download"))
							{
								if (Config.getMonde() == null)
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.plugin not enabled"));
									if (sender.hasPermission("creativeparkour.*"))
									{
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.config tutorial"));
									}
								}
								else
								{
									String suiteArgs = new String("");
									int i = 1;
									if (cmd.getName().equalsIgnoreCase("cpd"))
										i = 0;
									for (; i < args.length; i++)
									{
										suiteArgs += " " + args[i];
									}
									GameManager.telechargerMap(sender, suiteArgs.trim());
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "tp"))
						{
							if (commandeAutorisee(sender, "tp"))
							{
								Player p = getPlayer(sender);
								if (args.length > 1)
								{
									Player p1 = Bukkit.getPlayer(args[1]);
									if (p1 == null)
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
										p.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getCommand("tp") + " <" + Langues.getMessage("commands.playerN") + ">");
									}
									else if (!p.hasPermission("creativeparkour.play") || Config.isBanned(p))
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getCommand("ban"));
									}
									else if (!p1.equals(p))
									{
										GameManager.teleporter(p, p1);
									}
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
									p.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getCommand("tp") + " <" + Langues.getMessage("commands.playerN") + ">");
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "test"))
						{
							if (commandeAutorisee(sender, "test"))
							{
								Player p = getPlayer(sender);
								if (args.length > 1)
								{
									if (args[1].equalsIgnoreCase("leave") || args[1].equalsIgnoreCase("quit") || args[1].equalsIgnoreCase("ragequit") || args[1].equalsIgnoreCase(Langues.getCommand("leave")))
									{
										GameManager.quitterTest(p);
										return true;
									}
								}
								GameManager.tester(p);
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "mapoptions"))
						{
							if (commandeAutorisee(sender, "mapoptions"))
							{
								Joueur j = GameManager.getJoueur(getPlayer(sender));
								if (j != null)
								{
									CPMap m = j.getMapObjet();
									if (m != null && m.getState() == CPMapState.CREATION)
									{
										m.afficherOptions(j);
										return true;
									}
								}
								sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error 3"));
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "spectator"))
						{
							if (commandeAutorisee(sender, "spectator"))
							{
								Joueur j = GameManager.getJoueur(getPlayer(sender));
								if (j != null)
								{
									CPMap m = j.getMapObjet();
									if (m != null && m.isPlayable())
									{
										j.modeSpectateur(j.getEtat() != EtatJoueur.SPECTATEUR); // Echange de son mode (jeu/spectateur)
										return true;
									}
								}
								sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error 2"));
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "publish"))
						{
							if (commandeAutorisee(sender, "publish"))
							{
								if (args.length > 1)
								{
									String nom = new String();
									for (int i=1; i < args.length; i++)
									{
										String espace = " ";
										if (i == 1) espace = "";
										nom = nom + espace + args[i];
									}
									GameManager.publier(getPlayer(sender), nom);
								}
								else
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.publish error name"));
									sender.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getCommand("publish") + " <" + Langues.getMessage("commands.publish name") + ">");
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "delete"))
						{
							if (commandeAutorisee(sender, "delete"))
							{
								Player p = getPlayer(sender);
								CPMap m = null;
								if (args.length > 1)
								{
									try {
										m = GameManager.getMap(Integer.valueOf(args[1]));
									} catch (NumberFormatException e) {}
								}
								if (m == null)
								{
									Joueur j = GameManager.getJoueur(p);
									if (j != null)
										m = j.getMapObjet();
								}
								if (m != null && m.getState() != CPMapState.DELETED && (p.hasPermission("creativeparkour.manage") || m.getCreator().equals(p.getUniqueId())))
								{
									String n = m.getName();
									if (n == null || n.isEmpty())
									{
										n = "unnamed";
									}
									question(p, Langues.getMessage("commands.delete question").replace("%map", ChatColor.ITALIC + n + ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD), "supprimer map", String.valueOf(m.getId()));
								}
								else
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.edit delete error"));
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "share", false)
								|| commandeEffectuee(sender, cmd, args, "edit", false)
								|| commandeEffectuee(sender, cmd, args, "export", false))
						{

							if (commandeAutorisee(sender, "edit", false) || commandeAutorisee(sender, "share", false) || commandeAutorisee(sender, "export", false))
							{
								Joueur j = GameManager.getJoueur(getPlayer(sender));
								// Si le joueur joue et a la permission manage ou est le créateur de la map
								if (j != null && j.getMap() != null && (sender.hasPermission("creativeparkour.manage") || GameManager.getMap(j.getMap()).getCreator().equals(j.getUUID())))
								{
									// Recherche du nom de la map
									String n = GameManager.getMap(j.getMap()).getName();
									if (n == null || n.isEmpty())
									{
										n = "unnamed";
									}

									if (GameManager.getMap(j.getMap()).isPlayable())
									{
										if (commandeEffectuee(sender, cmd, args, "edit", true))
										{
											if (commandeAutorisee(sender, "edit", true))
											{
												sender.sendMessage(Config.prefix() + ChatColor.GOLD + ChatColor.BOLD + Langues.getMessage("commands.edit question").replace("%map", ChatColor.ITALIC + n + ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD));
												sender.sendMessage(Langues.getMessage("commands.edit info").replace("%map", ChatColor.ITALIC + n + ChatColor.RESET));
												question(j.getPlayer(), null, "modifier map");
											}
											return true;
										}
										else if (commandeEffectuee(sender, cmd, args, "share", true))
										{
											if (commandeAutorisee(sender, "share", true))
											{
												sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("error deprecated"));
												return true;
												
												/*sender.sendMessage(Config.prefix() + ChatColor.GOLD + ChatColor.BOLD + Langues.getMessage("commands.share question"));
												sender.sendMessage(Langues.getMessage("commands.share info"));
												question(j.getPlayer(), null, "partager map");*/
											}
											return true;
										}
										else if (commandeEffectuee(sender, cmd, args, "export", true))
										{
											if (commandeAutorisee(sender, "export", true))
											{
												sender.sendMessage(Config.prefix() + ChatColor.GOLD + ChatColor.BOLD + Langues.getMessage("commands.export question").replace("%map", ChatColor.ITALIC + n + ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD));
												sender.sendMessage(Langues.getMessage("commands.export info"));
												question(j.getPlayer(), null, "exporter map");
											}
											return true;
										}
									}
								}
								// Si rien n'a été retourné
								sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.edit delete error"));
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "settings"))
						{
							if (commandeAutorisee(sender, "settings"))
							{
								Player p = getPlayer(sender);
								Joueur j = GameManager.getJoueur(p);
								if (j == null)
								{
									j = new Joueur (p, false);
									GameManager.addJoueur(j);
								}
								j.ouvrirParametres();
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "importsel"))
						{
							if (commandeAutorisee(sender, "importsel"))
							{
								if (Config.getMonde() == null)
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.plugin not enabled"));
									if (sender.hasPermission("creativeparkour.*"))
									{
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.config tutorial"));
									}
								}
								else
								{
									GameManager.importerSelection(getPlayer(sender), false);
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "ghost"))
						{
							if (commandeAutorisee(sender, "ghost"))
							{
								if (!CreativeParkour.auMoins1_9())
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("outdated server").replace("%ver", "1.9"));
								}
								else
								{
									if (args.length < 2)
									{
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("too few arguments"));
										String h1 = Langues.getCommand("ghost") + " " + Langues.getMessage("commands.ghost play");
										String h2 = Langues.getCommand("ghost") + " " + Langues.getMessage("commands.ghost speed");
										String h3 = Langues.getCommand("ghost") + " " + Langues.getMessage("commands.ghost rewind");
										String h4 = Langues.getCommand("ghost") + " " + Langues.getMessage("commands.ghost moment");
										String h5 = Langues.getCommand("ghost") + " " + Langues.getMessage("commands.ghost select");
										String h6 = Langues.getCommand("ghost") + " " + Langues.getMessage("commands.ghost watch");
										for (String s : Help.getHelp())
										{
											if (s.contains(h1) || s.contains(h2) || s.contains(h3) || s.contains(h4) || s.contains(h5) || s.contains(h6))
											{
												sender.sendMessage(s);
											}
										}
									}
									else
									{

										Joueur j = GameManager.getJoueur(getPlayer(sender));
										if (args[1].equalsIgnoreCase("play") || args[1].equalsIgnoreCase("p") || args[1].equalsIgnoreCase(Langues.getMessage("commands.ghost play")))
										{
											if (j == null || j.getMapObjet() == null || !j.getMapObjet().isPlayable())
												sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error 4"));
											else
												j.startGhosts();
										}
										else if (args[1].equalsIgnoreCase("speed") || args[1].equalsIgnoreCase("s") || args[1].equalsIgnoreCase(Langues.getMessage("commands.ghost speed")))
										{
											if (j == null || j.getMapObjet() == null || !j.getMapObjet().isPlayable())
												sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error 4"));
											else
											{
												if (args.length < 3) // Si pas de nombre, on met *2 ou *1 selon ce qui était déjà choisi
												{
													int nb = j.ghostIncrementation == 1 ? 2 : 1;
													j.ghostIncrementation = nb;
													sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.ghost speed ok").replace("%nb", String.valueOf(nb)));
												}
												else
												{
													try {
														j.ghostIncrementation = Integer.valueOf(args[2]);
														sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.ghost speed ok").replace("%nb", args[2]));
													} catch (NumberFormatException e) {
														sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.ghost speed error"));
													}
												}
											}
										}
										else if (args[1].equalsIgnoreCase("moment") || args[1].equalsIgnoreCase("m") || args[1].equalsIgnoreCase(Langues.getMessage("commands.ghost moment")))
										{
											if (j == null || j.getMapObjet() == null || !j.getMapObjet().isPlayable())
												sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error 4"));
											else
											{
												try {
													j.setGhostsMoment(Math.round(Float.valueOf(args[2]) * 20));
													sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.ghost moment ok").replace("%moment", args[2]));
												} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
													sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.ghost moment error"));
												}
											}
										}
										else if (args[1].equalsIgnoreCase("rewind") || args[1].equalsIgnoreCase("r") || args[1].equalsIgnoreCase(Langues.getMessage("commands.ghost rewind")))
										{
											if (j == null || j.getMapObjet() == null || !j.getMapObjet().isPlayable())
												sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error 4"));
											else
											{
												if (args.length < 3) // Si pas de nombre, 10 secondes
												{
													j.rewindGhosts(10 * 20);
													sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.ghost rewind ok").replace("%seconds", "10"));
												}
												else
												{
													try {
														j.rewindGhosts(Math.round(Float.valueOf(args[2]) * 20));
														sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.ghost rewind ok").replace("%seconds", args[2]));
													} catch (NumberFormatException e) {
														sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.ghost rewind error"));
													}
												}
											}
										}
										else if (args[1].equalsIgnoreCase("select") || args[1].equalsIgnoreCase("sel") || args[1].equalsIgnoreCase(Langues.getMessage("commands.ghost select")))
										{
											if (j == null || j.getMapObjet() == null || !j.getMapObjet().isPlayable())
												sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error 4"));
											else
												j.openGhostSelection();
										}
										else if (args[1].equalsIgnoreCase("watch") || args[1].equalsIgnoreCase("w") || args[1].equalsIgnoreCase(Langues.getMessage("commands.ghost watch")))
										{
											if (args.length < 3)
												sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.ghost watch error id"));
											else
												GameManager.watchGhost(getPlayer(sender), args[2]);
										}
									}
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "register"))
						{
							if (commandeAutorisee(sender, "register"))
							{
								sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("error deprecated"));
								return true;
								
								/*if (Config.online())
								{
									CPUtils.registerOnline(getPlayer(sender));
								}
								else
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("online disabled"));
								}*/
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "removetime"))
						{
							if (commandeAutorisee(sender, "removetime"))
							{
								if (args.length <= 1)
									sender.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getCommand("removetime") + " <" + Langues.getMessage("commands.playerN") + ">");
								else
								{
									UUID victime = NameManager.getUuidAvecNom(args[1]);
									if (victime == null)
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("unknown player").replace("%player", args[1]));
									else
									{
										boolean tous = args.length > 2 && args[2].equalsIgnoreCase("all");
										GameManager.retirerTemps(getPlayer(sender), victime, tous);
									}
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "getid"))
						{
							if (commandeAutorisee(sender, "getid"))
							{
								String suiteArgs = new String("");
								for (int i=1; i < args.length; i++)
								{
									suiteArgs += " " + args[i];
								}
								GameManager.getIdMap(getPlayer(sender), suiteArgs.trim());
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "claim"))
						{
							if (commandeAutorisee(sender, "claim"))
							{
								Player p = getPlayer(sender);
								if (args.length > 1 && (args[1].equalsIgnoreCase("messages") || args[1].equalsIgnoreCase(Langues.getMessage("commandes.messages"))))
									RewardManager.inverserMessages(p);
								else
									RewardManager.claim(p);
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "managemaps"))
						{
							if (commandeAutorisee(sender, "managemaps"))
							{
								GameManager.manageMaps(getPlayer(sender));
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "ban"))
						{
							if (commandeAutorisee(sender, "ban"))
							{
								if (args.length > 1)
								{
									Player p1 = Bukkit.getPlayer(args[1]);
									UUID uuidP1 = NameManager.getUuidAvecNom(args[1]);
									if (uuidP1 == null)
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
									else
									{
										if (Config.isBanned(uuidP1))
											sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.ban error"));
										else
										{
											Config.getConfJoueur(uuidP1.toString()).set("banned", true);
											Config.saveConfJoueur(uuidP1.toString());
											if (p1 != null)
											{
												Joueur j = GameManager.getJoueur(p1);
												if (j != null && j.getMap() != null)
												{
													j.quitter(true, false);
													p1.sendMessage(Config.prefix() + ChatColor.RED + "" + ChatColor.BOLD + Langues.getMessage("ban"));
												}
											}
											sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.ban banned").replace("%player", NameManager.getNomAvecUUID(uuidP1)).replace("%uuid", uuidP1.toString()));
										}
									}
								}
								else
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "pardon"))
						{
							if (commandeAutorisee(sender, "pardon"))
							{
								if (args.length > 1)
								{
									UUID uuidP1 = NameManager.getUuidAvecNom(args[1]);
									if (uuidP1 != null)
									{
										if (!Config.isBanned(uuidP1))
											sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.pardon error"));
										else
										{
											Config.getConfJoueur(uuidP1.toString()).set("banned", false);
											Config.saveConfJoueur(uuidP1.toString());
											sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.pardon pardoned").replace("%player", NameManager.getNomAvecUUID(uuidP1)).replace("%uuid", uuidP1.toString()));
										}
									}
									else
										sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
								}
								else
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "pin", false) || commandeEffectuee(sender, cmd, args, "unpin", false))
						{
							if (commandeAutorisee(sender, "pin"))
							{
								Joueur j = GameManager.getJoueur(getPlayer(sender));
								if (j != null && j.getMap() != null)
								{
									if (commandeEffectuee(sender, cmd, args, "pin"))
									{
										GameManager.getMap(j.getMap()).pin(true);
										sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.pin success"));
									}
									else if (commandeEffectuee(sender, cmd, args, "unpin"))
									{
										GameManager.getMap(j.getMap()).pin(false);
										sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.unpin success"));
									}
								}
								else
								{
									sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.pin error"));
								}
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "notifications"))
						{
							if (commandeAutorisee(sender, "notifications"))
							{
								Player p = getPlayer(sender);
								Joueur j = GameManager.getJoueur(p);
								if (j == null)
								{
									j = new Joueur (p, false);
									GameManager.addJoueur(j);
								}
								j.inverserNotifications();
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "messages"))
						{
							if (commandeAutorisee(sender, "messages"))
							{
								Player p = getPlayer(sender);
								Joueur j = GameManager.getJoueur(p);
								if (j == null)
								{
									j = new Joueur (p, false);
									GameManager.addJoueur(j);
								}
								if (args.length > 1)
								{
									if (args[1].equalsIgnoreCase(Langues.getMessage("commands.messages spec")))
										j.inverserMessagesSpec();
									else
									{
										try {
											j.setParam(PlayerSetting.MESSAGES_CP, TypeMessage.valueOf(args[1]));
											sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.messages ok"));
										} catch (IllegalArgumentException e) {
											// Rien
										}
									}
								}
								else
									j.configurerMessages();
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "sync"))
						{
							if (commandeAutorisee(sender, "sync"))
							{
								GameManager.synchroWeb(sender);
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "noplates"))
						{
							if (commandeAutorisee(sender, "noplates"))
							{
								Joueur j = GameManager.getJoueur(getPlayer(sender));
								if (j != null)
								{
									CPMap m = j.getMapObjet();
									if (m != null)
									{
										m.togglePlates(j);
										return true;
									}
								}
								sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error 4"));
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "version"))
						{
							if (commandeAutorisee(sender, "version"))
							{
								sender.sendMessage(CreativeParkour.getPlugin().getDescription().getFullName());
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "config"))
						{
							if (commandeAutorisee(sender, "config"))
							{
								Player p = getPlayer(sender);
								if (args.length >= 2)
								{
									try {
										Config.configurer(p, EtapeConfig.valueOf(args[1].toUpperCase()));
										return true;
									} catch (IllegalArgumentException e) {
										// Rien
									}
								}
								Config.configurer(p, EtapeConfig.START); // Si le return d'avant n'est pas passé
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "language"))
						{
							if (commandeAutorisee(sender, "language"))
							{
								if (args.length >= 2)
								{
									Player p = getPlayer(sender);
									String l = Langues.transformerCodeLangue(args[1]);
									if (!l.equals(Config.getLanguage()))
									{
										Config.updateConfig("language", l);
										Langues.load(sender);

										// Mise à jour des inventaires des joueurs qui sont dans des maps
										for (Joueur j : GameManager.joueurs)
										{
											GameManager.reintegrerMapOuQuitter(j.getPlayer(), false);
										}
									}
									else if (p != null && !Config.joueursConfiguration.contains(p))
										sender.sendMessage(Config.prefix() + Langues.getMessage("commands.language unchanged").replace("%language", Config.getLanguage()));

									if (p != null && Config.joueursConfiguration.contains(p))
										Config.configurer(p, EtapeConfig.STORAGE);
									return true;
								}
								sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.language error"));
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "help", true))
						{
							if (commandeAutorisee(sender, "help"))
							{
								int page = 1;
								try{
									if (args.length > 1 && Integer.valueOf(args[1]) > 0)
									{
										page = Integer.valueOf(args[1]);
									}
								} catch (NumberFormatException e) {
									page = 1;
								}
								Help.sendHelp(sender, page);
							}
							return true;
						}
						else if (commandeEffectuee(sender, cmd, args, "yes") || commandeEffectuee(sender, cmd, args, "no"))
						{
							if (commandeAutorisee(sender, "yes") || commandeAutorisee(sender, "no"))
							{
								boolean reponse = false;
								if (commandeEffectuee(sender, cmd, args, "yes", false)) { reponse = true; }
								else if (commandeEffectuee(sender, cmd, args, "no", false)) { reponse = false; }
								else { return false; }
								Player p = getPlayer(sender);
								for (int i=0; i < joueursCommande.size(); i++)
								{
									if (joueursCommande.get(i).getP().equals(p))
									{
										if (joueursCommande.get(i).getQ().equalsIgnoreCase("config monde défaut"))
										{
											Config.repondreQMonde(p, reponse);
										}
										else if (joueursCommande.get(i).getQ().equalsIgnoreCase("config partage"))
										{
											Config.repondreQPartage(p, reponse);
										}
										else if (joueursCommande.get(i).getQ().equalsIgnoreCase("écraser map"))
										{
											GameManager.nouvelleMap(p, reponse, null);
										}
										else if (joueursCommande.get(i).getQ().equalsIgnoreCase("écraser map import"))
										{
											if (reponse)
												GameManager.importerSelection(p, true);
											else
												p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.canceled"));
										}
										else if (joueursCommande.get(i).getQ().equalsIgnoreCase("supprimer map"))
										{
											UUID uuidMap = null;
											if (!joueursCommande.get(i).getArgs().isEmpty())
											{
												try {
													uuidMap = GameManager.getMap(Integer.valueOf(joueursCommande.get(i).getArgs())).getUUID();
												} catch (NumberFormatException e) { }
											}
											if (uuidMap == null)
												uuidMap = GameManager.getJoueur(p).getMap();
											if (reponse) GameManager.supprimerMap(uuidMap, p);
											else
												p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.delete canceled"));
										}
										else if (joueursCommande.get(i).getQ().equalsIgnoreCase("modifier map"))
										{
											Joueur j = GameManager.getJoueur(p);
											if (reponse) GameManager.modifierMap(j, j.getMap());
											else
												p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.edit canceled"));
										}
										else if (joueursCommande.get(i).getQ().equalsIgnoreCase("partager map"))
										{
											Joueur j = GameManager.getJoueur(p);
											if (reponse) GameManager.partagerMap(j, j.getMap());
											else
												p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.share canceled"));
										}
										else if (joueursCommande.get(i).getQ().equalsIgnoreCase("exporter map"))
										{
											Joueur j = GameManager.getJoueur(p);
											if (reponse) GameManager.getMap(j.getMap()).exporter(j);
											else
												p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("commands.export canceled"));
										}
										else if (joueursCommande.get(i).getQ().equalsIgnoreCase("invitation"))
										{
											GameManager.repondreInvitation(p, reponse);
										}
										joueursCommande.remove(joueursCommande.get(i));
										return true;
									}
								}
								p.sendMessage(Config.prefix() + Langues.getMessage("commands.no answer"));
							}
							return true;
						}
					}

					if (commandeEffectuee(sender, cmd, args, "enable"))
					{
						if (commandeAutorisee(sender, "enable"))
						{
							if (!Config.getConfig().getBoolean("plugin enabled"))
							{
								Config.updateConfig("plugin enabled", true);
								Config.enable(false);
							}
							sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("config.enable"));
						}
						return true;
					}
					else if (commandeEffectuee(sender, cmd, args, "disable"))
					{
						if (commandeAutorisee(sender, "disable"))
						{
							Config.updateConfig("plugin enabled", false);
							Config.disable();
							Config.reload();
							sender.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("config.disable"));
						}
						return true;
					}
					else if (!Config.pluginActive())
					{
						sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.plugin disabled"));
						return true;
					}
					sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.unknown") + " " + Langues.getMessage("commands.help info"));
					Stats.ajouterCommandeStats(args[0]);
					return true;
				}
				else
				{
					Help.sendHelp(sender, 1);
					return true;
				}
			} catch (Exception e) {
				sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("error"));
				CreativeParkour.erreur("COMMANDES", e, true);
				return true;
			}
		}
	}

	@EventHandler
	void onTabComplete(TabCompleteEvent e)
	{
		String buffer = e.getBuffer().toLowerCase();
		if (buffer.startsWith("/cp ") || buffer.startsWith("/cpk ") || buffer.startsWith("cp ") || buffer.startsWith("cpk "))
		{
			String arg = buffer.substring(buffer.indexOf(' ') + 1);
			if (!arg.contains(" "))
			{
				Set<String> completions = new HashSet<String>();
				for (Commande c : commands.values())
				{
					if (commandeAutorisee(e.getSender(), c.name, false))
					{
						for (String alias : c.argAliases)
						{
							if (arg.isEmpty() || alias.startsWith(arg))
								completions.add(alias);
						}
					}
				}
				e.setCompletions(new ArrayList<String>(completions));
			}
		}
	}

	/**
	 * Whether or not {@code sender} performed the command.
	 * @param sender The command sender.
	 * @param cmd The command.
	 * @param args Command arguments.
	 * @param cmdName Name of the command to check (should have been added to the list before).
	 * @return {@code true} if {@code sender} performed the specified command.
	 */
	private static boolean commandeEffectuee(CommandSender sender, Command cmd, String[] args, String cmdName)
	{
		return commandeEffectuee(sender, cmd, args, cmdName, true);
	}

	/**
	 * Whether or not {@code sender} performed the command.
	 * @param sender The command sender.
	 * @param cmd The command.
	 * @param args Command arguments.
	 * @param cmdName Name of the command to check (should have been added to the list before).
	 * @param count {@code true} if the command should be counted for statistics.
	 * @return {@code true} if {@code sender} performed the specified command.
	 */
	private static boolean commandeEffectuee(CommandSender sender, Command cmd, String[] args, String cmdName, boolean count)
	{
		Commande c = commands.get(cmdName);
		if (c == null)
			return false;
		if (!c.cmd.equalsIgnoreCase(cmd.getName()))
			return false;
		if (c.arg1 == null || c.arg1.equalsIgnoreCase(args[0]) || c.argAliases.contains(args[0].toLowerCase()))
		{
			if (count)
				Stats.ajouterCommandeStats(c.name);
			return true;
		}
		return false;
	}

	/**
	 * Whether or not {@code sender} is allowed to perform the command.
	 * @param sender The command sender.
	 * @param cmdName The command.
	 * @return {@code true} if {@code sender} is allowed to perform the specified command.
	 */
	private static boolean commandeAutorisee(CommandSender sender, String cmdName)
	{
		return commandeAutorisee(sender, cmdName, true);
	}

	/**
	 * Whether or not {@code sender} is allowed to perform the command.
	 * @param sender The command sender.
	 * @param cmdName The command.
	 * @param message Whether or not send a message to {@code sender} if they are not permitted to use the command.
	 * @return {@code true} if {@code sender} is allowed to perform the specified command.
	 */
	private static boolean commandeAutorisee(CommandSender sender, String cmdName, boolean message)
	{
		Commande c = commands.get(cmdName);
		if (c != null)
		{
			if (!c.onlyPlayers || sender instanceof Player)
			{
				if (sender instanceof Player && Config.isBanned((Player) sender) && !cmdName.equalsIgnoreCase("pardon")) // If banned
				{
					if (message)
						sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("ban"));
				}
				else
				{
					if (c.permission == null || sender.hasPermission(c.permission))
						return true;
					else if (message)
						sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
				}
			}
			else if (message)
				sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.player"));
		}
		return false;
	}

	/**
	 * Adds a "cp" command to the list.
	 * @param name Command's name (in language files) <strong>and</strong> first argument.
	 * @param permission Permission to use the command or {@code null} if everyone is allowed.
	 * @param onlyPlayers {@code true} if only in-game players can use the command, or {@code false} if it can be used in console.
	 */
	private static void addCommand(String name, String permission, boolean onlyPlayers)
	{
		addCommand(name, "creativeparkour", name, permission, onlyPlayers);
	}

	/**
	 * Adds a command to the list.
	 * @param name Command's name (in language files).
	 * @param cmd The command (before arguments).
	 * @param arg1 First argument or {@code null} if the command only uses {@code cmd}.
	 * @param permission Permission to use the command or {@code null} if everyone is allowed.
	 * @param onlyPlayers {@code true} if only in-game players can use the command, or {@code false} if it can be used in console.
	 */
	private static void addCommand(String name, String cmd, String arg1, String permission, boolean onlyPlayers)
	{
		commands.put(name, new Commande(name, cmd, arg1, permission, onlyPlayers));
	}

	/**
	 * Converts a {@code CommandSender} to a {@code Player}.
	 * @param sender The {@code CommandSender}.
	 * @return The {@code Player} or {@code null} if it is not a {@code Player}
	 */
	private static Player getPlayer(CommandSender sender)
	{
		if (sender instanceof Player)
			return (Player) sender;
		return null;
	}

	/**
	 * Pose une question OUI/NON au joueur
	 * @param p Joueur concerné
	 * @param question Question à poser. Si null, elle n'est pas affichée
	 * @param qID Identifiant de la question
	 */
	static void question(Player p, String question, String qID)
	{
		question(p, question, qID, null);
	}

	/**
	 * Pose une question OUI/NON au joueur
	 * @param p Joueur concerné
	 * @param question Question à poser. Si null, elle n'est pas affichée
	 * @param qID Identifiant de la question
	 * @param args Arguments supplémentaires de la commande
	 */
	static void question(Player p, String question, String qID, String args)
	{
		if (question != null)
			p.sendMessage(Config.prefix() + ChatColor.GOLD + ChatColor.BOLD + question);
		p.spigot().sendMessage(new ComponentBuilder(" ➥ ").color(ChatColor.YELLOW).append("[" + Langues.getCommand("yes").toUpperCase() + "]").color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour yes")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.YELLOW + Langues.getMessage("commands.click to answer").replace("%answer", ChatColor.GREEN + "" + ChatColor.BOLD + Langues.getCommand("yes").toUpperCase())).create())).append(" [" + Langues.getCommand("no").toUpperCase() + "]").color(ChatColor.RED).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour no")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.YELLOW + Langues.getMessage("commands.click to answer").replace("%answer", ChatColor.RED + "" + ChatColor.BOLD + Langues.getCommand("no").toUpperCase())).create())).create());

		addJoueurCommande(p, qID, args);
	}

	/**
	 * Demande "Que souhaitez-vous faire ?" au joueur
	 * @param p Joueur
	 */
	static void texteQuestion(Player p)
	{
		p.sendMessage(Config.prefix() + ChatColor.GOLD + ChatColor.BOLD + Langues.getMessage("commands.question"));
	}

	private static void addJoueurCommande(Player p, String q, String args)
	{
		// Suppression du joueur de la liste s'il y est déjà
		for (Iterator<JoueurCommande> it = joueursCommande.iterator(); it.hasNext();)
		{
			JoueurCommande jc = it.next();
			if (jc.getP().equals(p))
				it.remove();
		}

		// Ajout du nouveau
		joueursCommande.add(new JoueurCommande(p, q, args));
	}
}

class Commande
{
	String name;
	String cmd;
	String arg1;
	/**
	 * Lower case aliases for the first argument.
	 */
	List<String> argAliases;
	String permission;
	boolean onlyPlayers;

	/**
	 * A CreativeParkour command
	 * @param name Command's name (in language files).
	 * @param cmd The command (before arguments).
	 * @param arg1 First argument or {@code null} if the command only uses {@code cmd}.
	 * @param permission Permission to use the command or {@code null} if everyone is allowed.
	 * @param onlyPlayers {@code true} if only in-game players can use the command, or {@code false} if it can be used in console.
	 */
	Commande(String name, String cmd, String arg1, String permission, boolean onlyPlayers)
	{
		this.name = name;
		this.cmd = cmd;
		this.arg1 = arg1;
		this.permission = permission;
		this.onlyPlayers = onlyPlayers;
		argAliases = new ArrayList<String>();
		if (arg1 != null && Langues.messagesEN.getProperty("commands." + name) != null)
		{
			for (String s : Langues.messagesEN.getProperty("commands." + name).split(";"))
			{
				argAliases.add(s.toLowerCase());
			}
			if (!Config.getLanguage().startsWith("en"))
			{
				String msg = Langues.getMessage("commands." + name);
				if (msg != null)
				{
					for (String s : msg.split(";"))
					{
						argAliases.add(s.toLowerCase());
					}
				}
			}
		}
	}
}
