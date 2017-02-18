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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

class Commandes implements CommandExecutor
{
	private static List<JoueurCommande> joueursCommande = new ArrayList<JoueurCommande>();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (Config.getConfig().getBoolean("dont use cp") && label.equalsIgnoreCase("cp"))
		{
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("creativeparkour") || cmd.getName().equalsIgnoreCase("cpd"))
		{
			try {
				if (sender instanceof Player)
				{
					Player p = (Player) sender;
					if (args.length > 0 || cmd.getName().equalsIgnoreCase("cpd"))
					{
						if (Config.pluginActive() || args[0].equalsIgnoreCase("config"))
						{
							if (cmd.getName().equalsIgnoreCase("cpd") || args[0].equalsIgnoreCase("download") || args[0].equalsIgnoreCase(Langues.getMessage("commands.download")))
							{								
								Stats.ajouterCommandeStats("download");
								if (Config.getMonde() == null)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.plugin not enabled"));
									if (p.hasPermission("creativeparkour.*"))
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.config tutorial"));
									}
									return true;
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
									GameManager.telechargerMap(p, suiteArgs.trim());
									return true;
								}
							}
							else if (args[0].equalsIgnoreCase("play") || args[0].equalsIgnoreCase(Langues.getMessage("commands.play")) || args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("list"))
							{
								Stats.ajouterCommandeStats("play");
								if (Config.getMonde() == null)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.plugin not enabled"));
									if (p.hasPermission("creativeparkour.*"))
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.config tutorial"));
									}
									return true;
								}
								else
								{
									String suiteArgs = new String("");
									for (int i=1; i < args.length; i++)
									{
										suiteArgs += " " + args[i];
									}
									GameManager.jouer(p, suiteArgs.trim());
									return true;
								}
							}
							else if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase(Langues.getMessage("commands.create")))
							{
								Stats.ajouterCommandeStats("create");
								if (Config.getMonde() == null)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.plugin not enabled"));
									if (p.hasPermission("creativeparkour.*"))
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.config tutorial"));
									}
									return true;
								}
								else
								{
									if (args.length > 1 && p.hasPermission("creativeparkour.manage"))
									{
										try {
											CPMap m = GameManager.getMap(Integer.valueOf(args[1]));
											if (m != null && m.getState() == CPMapState.CREATION)
											{
												m.accepterInvitation(p);
												return true;
											}
										} catch (NumberFormatException e) {
											// Rien
										}
										// Si on arrive là, ça n'a pas marché
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.create map error"));
									}

									GameManager.creerMap(p);
									return true;
								}
							}
							else if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase(Langues.getMessage("commands.invite")))
							{
								Stats.ajouterCommandeStats("invite");
								Joueur j = GameManager.getJoueur(p);
								if (j == null || j.getMapObjet() == null || j.getMapObjet().getState() != CPMapState.CREATION)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 1"));
								}
								else if (!j.getMapObjet().getCreator().equals(p.getUniqueId()) && !p.hasPermission("creativeparkour.manage"))
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 4"));
								}
								else if (args.length > 1)
								{
									Player p1 = Bukkit.getPlayer(args[1]);
									if (p1 == null)
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
										p.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getMessage("commands.invite") + " <" + Langues.getMessage("commands.playerN") + ">");
									}
									else if (!p1.hasPermission("creativeparkour.create") || !Config.peutJouer(p1))
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 5"));
									}
									else if (j.getMapObjet().getCreator().equals(p1.getUniqueId()))
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 3"));
									}
									else
									{
										j.getMapObjet().inviter(p, p1);
										CPUtils.sendInfoMessage(p, Langues.getMessage("commands.contributors info"));
									}
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
									p.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getMessage("commands.invite") + " <" + Langues.getMessage("commands.playerN") + ">");
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase(Langues.getMessage("commands.remove")))
							{
								Stats.ajouterCommandeStats("remove");
								Joueur j = GameManager.getJoueur(p);
								if (j == null || j.getMap() == null)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.test error"));
								}
								else if (j.getMapObjet().getState() != CPMapState.CREATION)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 1"));
								}
								else if (!j.getMapObjet().getCreator().equals(p.getUniqueId()) && !p.hasPermission("creativeparkour.manage"))
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 4"));
								}
								else if (args.length > 1)
								{
									UUID p1 = NameManager.getUuidAvecNom(args[1]);
									if (p1 == null)
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
										p.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getMessage("commands.remove") + " <" + Langues.getMessage("commands.playerN") + ">");
									}
									else if (j.getMapObjet().getCreator().equals(p1))
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 3"));
									}
									else
									{
										j.getMapObjet().supprContibuteur(p, p1);
									}
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
									p.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getMessage("commands.remove") + " <" + Langues.getMessage("commands.playerN") + ">");
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("contributors") || args[0].equalsIgnoreCase(Langues.getMessage("commands.contributors")))
							{
								Stats.ajouterCommandeStats("contributors");
								Joueur j = GameManager.getJoueur(p);
								if (j == null || j.getMapObjet() == null || j.getMapObjet().getState() != CPMapState.CREATION)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 1"));
								}
								else if (!j.getMapObjet().getCreator().equals(p.getUniqueId()) && !p.hasPermission("creativeparkour.manage"))
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.invite error 4"));
								}
								else 
								{
									j.afficherListeContributeurs();
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase(Langues.getMessage("commands.tp")))
							{
								Stats.ajouterCommandeStats("tp");
								if (args.length > 1)
								{
									Player p1 = Bukkit.getPlayer(args[1]);
									if (p1 == null)
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
										p.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getMessage("commands.tp") + " <" + Langues.getMessage("commands.playerN") + ">");
									}
									else if (!p.hasPermission("creativeparkour.play") || !Config.peutJouer(p))
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.ban"));
									}
									else if (!p1.equals(p))
									{
										GameManager.teleporter(p, p1);
									}
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
									p.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getMessage("commands.tp") + " <" + Langues.getMessage("commands.playerN") + ">");
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("test") || args[0].equalsIgnoreCase("validate") || args[0].equalsIgnoreCase(Langues.getMessage("commands.test")))
							{
								Stats.ajouterCommandeStats("test");
								if (args.length > 1)
								{
									if (args[1].equalsIgnoreCase("leave") || args[1].equalsIgnoreCase("quit") || args[1].equalsIgnoreCase("ragequit") || args[1].equalsIgnoreCase(Langues.getMessage("commands.leave")))
									{
										GameManager.quitterTest(p);
										return true;
									}
								}
								GameManager.tester(p);
								return true;
							}
							else if (args[0].equalsIgnoreCase("mapoptions") || args[0].equalsIgnoreCase(Langues.getMessage("commands.mapoptions")))
							{
								Stats.ajouterCommandeStats("mapoptions");
								Joueur j = GameManager.getJoueur(p);
								if (j != null)
								{
									CPMap m = j.getMapObjet();
									if (m != null && m.getState() == CPMapState.CREATION)
									{
										m.afficherOptions(j);
										return true;
									}
								}
								p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error 3"));
								return true;
							}
							else if (args[0].equalsIgnoreCase("spectator") || args[0].equalsIgnoreCase("spectate") || args[0].equalsIgnoreCase("spec") || args[0].equalsIgnoreCase(Langues.getMessage("commands.spectator")))
							{
								Stats.ajouterCommandeStats("spectator");
								if (p.hasPermission("creativeparkour.spectate"))
								{
									Joueur j = GameManager.getJoueur(p);
									if (j != null)
									{
										CPMap m = j.getMapObjet();
										if (m != null && m.isPlayable())
										{
											j.modeSpectateur(j.getEtat() != EtatJoueur.SPECTATEUR); // Echange de son mode (jeu/spectateur)
											return true;
										}
									}
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("location error 2"));
								}
								else
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
								return true;
							}
							else if (args[0].equalsIgnoreCase("publish") || args[0].equalsIgnoreCase(Langues.getMessage("commands.publish")))
							{
								Stats.ajouterCommandeStats("publish");
								if (args.length > 1)
								{
									String nom = new String();
									for (int i=1; i < args.length; i++)
									{
										String espace = " ";
										if (i == 1) espace = "";
										nom = nom + espace + args[i];
									}
									GameManager.publier(p, nom);
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.publish error name"));
									p.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getMessage("commands.publish") + " <" + Langues.getMessage("commands.publish name") + ">");
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase(Langues.getMessage("commands.delete")))
							{
								Stats.ajouterCommandeStats("delete");
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
								return true;
							}
							else if (args[0].equalsIgnoreCase("share") || args[0].equalsIgnoreCase(Langues.getMessage("commands.share"))
									|| args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase(Langues.getMessage("commands.edit"))
									|| args[0].equalsIgnoreCase("export") || args[0].equalsIgnoreCase(Langues.getMessage("commands.export")))
							{
								Joueur j = GameManager.getJoueur(p);
								// Si le joueur joue et a la permission manage ou est le créateur de la map
								if (j != null && j.getMap() != null && (p.hasPermission("creativeparkour.manage") || GameManager.getMap(j.getMap()).getCreator().equals(p.getUniqueId())))
								{
									// Recherche du nom de la map
									String n = GameManager.getMap(j.getMap()).getName();
									if (n == null || n.isEmpty())
									{
										n = "unnamed";
									}

									if (GameManager.getMap(j.getMap()).isPlayable())
									{
										if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase(Langues.getMessage("commands.edit")))
										{
											Stats.ajouterCommandeStats("edit");
											p.sendMessage(Config.prefix() + ChatColor.GOLD + ChatColor.BOLD + Langues.getMessage("commands.edit question").replace("%map", ChatColor.ITALIC + n + ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD));
											p.sendMessage(Langues.getMessage("commands.edit info").replace("%map", ChatColor.ITALIC + n + ChatColor.RESET));
											question(p, null, "modifier map");
											return true;
										}
										else if (args[0].equalsIgnoreCase("share") || args[0].equalsIgnoreCase(Langues.getMessage("commands.share")))
										{
											Stats.ajouterCommandeStats("share");
											if (p.hasPermission("creativeparkour.share"))
											{
												p.sendMessage(Config.prefix() + ChatColor.GOLD + ChatColor.BOLD + Langues.getMessage("commands.share question"));
												p.sendMessage(Langues.getMessage("commands.share info"));
												question(p, null, "partager map");
											}
											else
												p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
											return true;
										}
										else if (args[0].equalsIgnoreCase("export") || args[0].equalsIgnoreCase(Langues.getMessage("commands.export")))
										{
											Stats.ajouterCommandeStats("export");
											if (p.hasPermission("creativeparkour.manage"))
											{
												p.sendMessage(Config.prefix() + ChatColor.GOLD + ChatColor.BOLD + Langues.getMessage("commands.export question").replace("%map", ChatColor.ITALIC + n + ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD));
												p.sendMessage(Langues.getMessage("commands.export info"));
												question(p, null, "exporter map");
											}
											else
												p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
											return true;
										}
									}
								}
								// Si rien n'a été retourné
								p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.edit delete error"));
								return true;
							}
							else if (args[0].equalsIgnoreCase("settings") || args[0].equalsIgnoreCase("preferences") || args[0].equalsIgnoreCase("options") || args[0].equalsIgnoreCase(Langues.getMessage("commands.settings")))
							{
								Stats.ajouterCommandeStats("settings");
								Joueur j = GameManager.getJoueur(p);
								if (j == null)
								{
									j = new Joueur (p, false);
									GameManager.addJoueur(j);
								}
								j.ouvrirParametres();
								return true;
							}
							else if (args[0].equalsIgnoreCase("importsel") || args[0].equalsIgnoreCase(Langues.getMessage("commands.importsel")))
							{
								Stats.ajouterCommandeStats("importsel");
								if (Config.getMonde() == null)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.plugin not enabled"));
									if (p.hasPermission("creativeparkour.*"))
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.config tutorial"));
									}
								}
								else
								{
									if (p.hasPermission("creativeparkour.manage"))
									{
										GameManager.importerSelection(p, false);
									}
									else
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("difficulty"))
							{
								Stats.ajouterCommandeStats("difficulty");
								if (Config.getConfig().getBoolean("game.enable map rating") && args.length > 1)
								{
									try {
										int nb = Integer.valueOf(args[1]);
										if (nb >= 1 && nb <= 5)
											GameManager.voteDifficulte(p, nb);
									} catch (NumberFormatException e) {
										// Rien
									}
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("register") || args[0].equalsIgnoreCase(Langues.getMessage("commands.register")))
							{
								Stats.ajouterCommandeStats("register");
								if (Config.online())
								{
									CPUtils.registerOnline(p);
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("online disabled"));
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("removetime") || args[0].equalsIgnoreCase("deletetime") || args[0].equalsIgnoreCase(Langues.getMessage("commands.removetime")))
							{
								Stats.ajouterCommandeStats("removetime");
								if (p.hasPermission("creativeparkour.manage"))
								{
									if (args.length <= 1)
										p.sendMessage(ChatColor.YELLOW + "/cp " + Langues.getMessage("commands.removetime") + " <" + Langues.getMessage("commands.playerN") + ">");
									else
									{
										UUID victime = NameManager.getUuidAvecNom(args[1]);
										if (victime == null)
											p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("unknown player").replace("%player", args[1]));
										else
										{
											boolean tous = args.length > 2 && args[2].equalsIgnoreCase("all");
											GameManager.retirerTemps(p, victime, tous);
										}
									}
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("getid") || args[0].equalsIgnoreCase(Langues.getMessage("commands.getid")))
							{
								Stats.ajouterCommandeStats("getid");
								String suiteArgs = new String("");
								for (int i=1; i < args.length; i++)
								{
									suiteArgs += " " + args[i];
								}
								GameManager.getIdMap(p, suiteArgs.trim());
								return true;
							}
							else if (args[0].equalsIgnoreCase("claim") || args[0].equalsIgnoreCase(Langues.getMessage("commands.claim")))
							{
								Stats.ajouterCommandeStats("claim");
								if (args.length > 1 && (args[1].equalsIgnoreCase("messages") || args[1].equalsIgnoreCase(Langues.getMessage("commandes.messages"))))
									RewardManager.inverserMessages(p);
								else
									RewardManager.claim(p);
								return true;
							}
							else if (args[0].equalsIgnoreCase("managemaps") || args[0].equalsIgnoreCase(Langues.getMessage("commands.managemaps")))
							{
								Stats.ajouterCommandeStats("managemaps");
								if (p.hasPermission("creativeparkour.manage"))
								{
									GameManager.manageMaps(p);
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase(Langues.getMessage("commands.ban")))
							{
								Stats.ajouterCommandeStats("ban");
								if (p.hasPermission("creativeparkour.manage"))
								{
									if (args.length > 1)
									{
										Player p1 = Bukkit.getPlayer(args[1]);
										if (p1 == null)
										{
											p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
										}
										else
										{
											if (Config.getConfig().getList("banned players").contains(p1.getUniqueId().toString()))
											{
												p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("ban error 1"));
											}
											else
											{
												List<String> l = Config.getConfig().getStringList("banned players");
												l.add(p1.getUniqueId().toString());
												Config.updateConfig("banned players", l);
												Joueur j = GameManager.getJoueur(p1);
												if (j != null && j.getMap() != null)
												{
													j.quitter(true, false);
													p1.sendMessage(Config.prefix() + ChatColor.RED + "" + ChatColor.BOLD + Langues.getMessage("ban"));
												}
												p.sendMessage(Config.prefix() + ChatColor.GREEN + p1.getName() + " (" + p1.getUniqueId().toString() + ") banned !");
											}
										}
									}
									else
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
									}
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("pardon") || args[0].equalsIgnoreCase(Langues.getMessage("commands.pardon")))
							{
								Stats.ajouterCommandeStats("pardon");
								if (p.hasPermission("creativeparkour.manage"))
								{
									if (args.length > 1)
									{
										List<String> l = Config.getConfig().getStringList("banned players");
										Player p1 = Bukkit.getPlayer(args[1]);
										if (p1 != null && l.contains(p1.getUniqueId().toString()))
										{
											l.remove(p1.getUniqueId().toString());
											Config.updateConfig("banned players", l);
											p.sendMessage(Config.prefix() + ChatColor.GREEN + p1.getName() + " (" + p1.getUniqueId().toString() + ") pardoned !");
										}
										else
										{
											p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("ban error 3"));
										}
									}
									else
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("player offline"));
									}
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("pin") || args[0].equalsIgnoreCase(Langues.getMessage("commands.pin")) || args[0].equalsIgnoreCase("unpin") || args[0].equalsIgnoreCase(Langues.getMessage("commands.unpin")))
							{
								Stats.ajouterCommandeStats("pin");
								if (p.hasPermission("creativeparkour.manage"))
								{
									Joueur j = GameManager.getJoueur(p);
									if (j != null && j.getMap() != null)
									{
										if (args[0].equalsIgnoreCase("pin") || args[0].equalsIgnoreCase(Langues.getMessage("commands.pin")))
										{
											GameManager.getMap(j.getMap()).pin(true);
											p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.pin success"));
										}
										else if (args[0].equalsIgnoreCase("unpin") || args[0].equalsIgnoreCase(Langues.getMessage("commands.unpin")))
										{
											GameManager.getMap(j.getMap()).pin(false);
											p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("commands.unpin success"));
										}
									}
									else
									{
										p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.pin error"));
									}
								}
								else
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("notifications") || args[0].equalsIgnoreCase(Langues.getMessage("commands.notifications")))
							{
								Stats.ajouterCommandeStats("notifications");
								Joueur j = GameManager.getJoueur(p);
								if (j == null)
								{
									j = new Joueur (p, false);
									GameManager.addJoueur(j);
								}
								j.inverserNotifications();
								return true;
							}
							else if (args[0].equalsIgnoreCase("messages") || args[0].equalsIgnoreCase(Langues.getMessage("commands.messages")))
							{
								Stats.ajouterCommandeStats("messages");
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
								return true;
							}
							else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("quit") || args[0].equalsIgnoreCase("ragequit") || args[0].equalsIgnoreCase(Langues.getMessage("commands.leave")))
							{
								Stats.ajouterCommandeStats("leave");
								GameManager.supprJoueur(p, true);
								return true;
							}
							else if (args[0].equalsIgnoreCase("fill") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase(Langues.getMessage("commands.fill")))
							{
								Stats.ajouterCommandeStats("fill");
								if (CreativeParkour.getWorldEdit() == null)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.wand.no worldedit"));
									return true;
								}
								if (!p.hasPermission("creativeparkour.worldedit"))
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
									return true;
								}
								if (args.length > 1)
								{
									p.performCommand("/set " + args[1]);
								}
								else
								{
									p.sendMessage(ChatColor.RED + "/cp " + Langues.getMessage("commands.fill") + " <block>:[data]");
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("undo") || args[0].equalsIgnoreCase(Langues.getMessage("commands.undo")))
							{
								Stats.ajouterCommandeStats("undo");
								if (CreativeParkour.getWorldEdit() == null)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.wand.no worldedit"));
									return true;
								}
								if (!p.hasPermission("creativeparkour.worldedit"))
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
									return true;
								}
								p.performCommand("/undo");
								return true;
							}
							else if (args[0].equalsIgnoreCase("redo") || args[0].equalsIgnoreCase(Langues.getMessage("commands.redo")))
							{
								Stats.ajouterCommandeStats("redo");
								if (CreativeParkour.getWorldEdit() == null)
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.wand.no worldedit"));
									return true;
								}
								if (!p.hasPermission("creativeparkour.worldedit"))
								{
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
									return true;
								}
								p.performCommand("/redo");
								return true;
							}
							else if (args[0].equalsIgnoreCase("sync") || args[0].equalsIgnoreCase(Langues.getMessage("commands.sync")))
							{
								Stats.ajouterCommandeStats("sync");
								if (p.hasPermission("creativeparkour.manage"))
								{
									GameManager.synchroWeb(p);
								}
								else {
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("version"))
							{
								Stats.ajouterCommandeStats("version");
								p.sendMessage(CreativeParkour.getPlugin().getDescription().getFullName());
								return true;
							}
							else if (args[0].equalsIgnoreCase("configure") || args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase(Langues.getMessage("commands.config")))
							{
								Stats.ajouterCommandeStats("config");
								if (p.hasPermission("creativeparkour.*"))
								{
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
								else {
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
								}
								return true;
							}
							else if (args[0].equalsIgnoreCase("language") || args[0].equalsIgnoreCase(Langues.getMessage("commands.language")))
							{
								Stats.ajouterCommandeStats("language");
								if (p.hasPermission("creativeparkour.*"))
								{
									if (args.length >= 2)
									{
										String l = Langues.transformerCodeLangue(args[1]);
										if (!l.equals(Config.getLanguage()))
										{
											Config.setLanguage(l);
											Langues.load(p);
											
											// Mise à jour des inventaires des joueurs qui sont dans des maps
											for (Joueur j : GameManager.joueurs)
											{
												GameManager.reintegrerMapOuQuitter(j.getPlayer(), false);
											}
										}
										else if (!Config.joueursConfiguration.contains(p))
											p.sendMessage(Config.prefix() + Langues.getMessage("commands.language unchanged").replace("%language", Config.getLanguage()));

										if (Config.joueursConfiguration.contains(p))
											Config.configurer(p, EtapeConfig.STORAGE);
										return true;
									}
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.language error"));
								}
								else {
									p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
								}
								return true;
							}
							else if ((args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase(Langues.getMessage("help.title"))))
							{
								Stats.ajouterCommandeStats("help");
								int page = 1;
								try{
									if (args.length > 1 && Integer.valueOf(args[1]) > 0)
									{
										page = Integer.valueOf(args[1]);
									}
								} catch (NumberFormatException e) {
									page = 1;
								}
								Help.sendHelp(p, page);
								return true;
							}
							else if (args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("no"))
							{
								boolean reponse = false;
								if (args[0].equalsIgnoreCase("yes")) { reponse = true; }
								else if (args[0].equalsIgnoreCase("no")) { reponse = false; }
								else { return false; }
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
								return true;
							}
						}

						if (args[0].equalsIgnoreCase("enable") && p.hasPermission("creativeparkour.*"))
						{
							Stats.ajouterCommandeStats("enable");
							if (!Config.getConfig().getBoolean("plugin enabled"))
							{
								Config.updateConfig("plugin enabled", true);
								Config.enable(false);
							}
							p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("config.enable"));
							return true;
						}
						else if (args[0].equalsIgnoreCase("disable") && p.hasPermission("creativeparkour.*"))
						{
							Stats.ajouterCommandeStats("disable");
							Config.updateConfig("plugin enabled", false);
							Config.disable();
							Config.reload();
							p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage("config.disable"));
							return true;
						}
						else if (!Config.pluginActive())
						{
							sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("config.plugin disabled"));
							return true;
						}
						p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.unknown") + " " + Langues.getMessage("commands.help"));
						Stats.ajouterCommandeStats(args[0]);
						return true;
					}
					else
					{
						Help.sendHelp(p, 1);
						return true;
					}
				}
				else
				{
					sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.player"));
					return true;
				}
			} catch (Exception e) {
				sender.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("error"));
				CreativeParkour.erreur("COMMANDES", e, true);
				return true;
			}
		}
		return false;
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
		p.spigot().sendMessage(new ComponentBuilder(" ➥ ").color(ChatColor.YELLOW).append("[" + Langues.getMessage("commands.yes").toUpperCase() + "]").color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour yes")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.YELLOW + Langues.getMessage("commands.click to answer").replace("%answer", ChatColor.GREEN + "" + ChatColor.BOLD + Langues.getMessage("commands.yes").toUpperCase())).create())).append(" [" + Langues.getMessage("commands.no").toUpperCase() + "]").color(ChatColor.RED).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour no")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.YELLOW + Langues.getMessage("commands.click to answer").replace("%answer", ChatColor.RED + "" + ChatColor.BOLD + Langues.getMessage("commands.no").toUpperCase())).create())).create());

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
