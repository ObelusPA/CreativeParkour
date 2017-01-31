package net.creativeparkour;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

class RewardManager implements Listener
{
	private enum TypeRecompense {ITEM, XP, MONEY};
	static File file;
	private static YamlConfiguration config;
	private static Set<String> mondesClaim;

	static void enable()
	{
		file = new File(CreativeParkour.getPlugin().getDataFolder(), "rewards.yml");
		config = YamlConfiguration.loadConfiguration(file);

		Set<String> keys = getKeys();
		if (!file.exists())
			CreativeParkour.debug("REW1", "rewards.yml does not exist.");
		else
			CreativeParkour.debug("REW1", "Rewards : " + keys.toString());
		if (keys.isEmpty()) // S'il n'y a rien ou que des exemples
		{
			CreativeParkour.debug("REW2", "Resetting rewards.yml");
			// Epée
			config.set("example1.type", "ITEM");
			config.set("example1.map", 2);
			config.set("example1.once", true);
			config.set("example1.amount", 1);
			config.set("example1.itemname", Material.IRON_HELMET.name());
			config.set("example1.itemdata", 0);
			List<String> liste = new ArrayList<String>();
			liste.add(Enchantment.OXYGEN.getName() + ":1");
			liste.add(Enchantment.DURABILITY.getName() + ":2");
			config.set("example1.itemenchants", liste);
			// XP
			config.set("example2.type", "XP");
			config.set("example2.map", "all");
			config.set("example2.once", false);
			config.set("example2.amount", 10);
			// Pognon
			config.set("example3.type", "MONEY");
			config.set("example3.map", "0,6");
			config.set("example3.once", false);
			config.set("example3.amount", 150);
			config.set("example3.cooldown", 60);
		}

		config.options().header("Documentation: https://creativeparkour.net/doc/rewards.php");

		saveConf();

		// Remplissage de la liste des mondes avec claim autorisé
		mondesClaim = new HashSet<String>();
		for (String s : Config.getConfig().getStringList("rewards.claim worlds"))
		{
			mondesClaim.add(s.toLowerCase());
		}
	}

	static void mapTerminee(CPMap map, Joueur j)
	{
		boolean ok = false;
		// Recherche des récompenses pour cette map
		for (String key : getKeys())
		{
			ConfigurationSection recomp = config.getConfigurationSection(key);
			if (getType(recomp) != null)
			{
				if (recomp.getString("map").equalsIgnoreCase("all") || Arrays.asList(recomp.getString("map").split(",")).contains(String.valueOf(map.getId())))
				{
					YamlConfiguration confJ = j.getConf();
					List<String> obtenus = confJ.getStringList("rewards.obtained");
					List<String> cooldowns = confJ.getStringList("rewards.cooldowns");
					// Recherche du truc dans les cooldowns
					boolean cooldownOK = true;
					if (recomp.getLong("cooldown") > 0)
					{
						Iterator<String> it = cooldowns.iterator();
						while (it.hasNext())
						{
							String[] a = it.next().split(":", 2);
							if (a[0].equalsIgnoreCase(key))
							{
								Date dLim = new Date(Long.valueOf(a[1]) + recomp.getLong("cooldown") * 1000 * 60);
								if (dLim.before(new Date())) // Si la date de cooldown est passée
								{
									it.remove();
								}
								else
								{
									cooldownOK = false;
								}
								break;
							}
						}
					}

					if (cooldownOK && (recomp.getBoolean("once") == false || !confJ.getStringList("rewards.obtained").contains(key)))
					{
						try {
							String msg = ChatColor.GOLD + Langues.getMessage("rewards.new") + " " + ChatColor.ITALIC + recompenseToString(recomp);

							if (recomp.getBoolean("once"))
							{
								obtenus.add(key);
								confJ.set("rewards.obtained", obtenus);
							}
							else if (recomp.getInt("cooldown") > 0)
							{
								cooldowns.add(key + ":" + new Date().getTime());
								confJ.set("rewards.cooldowns", cooldowns);
							}
							List<String> attente = confJ.getStringList("rewards.waiting");
							attente.add(key);
							confJ.set("rewards.waiting", attente);
							j.saveConf();

							j.getPlayer().sendMessage(msg);
							ok = true;
						} catch (Exception e) {
							e.printStackTrace();
							msgErreur(key);
						}
					}
				}
			}
		}

		if (ok)
			CPUtils.sendInfoMessage(j.getPlayer(), Langues.getMessage("rewards.claim"));
	}

	private static String recompenseToString(ConfigurationSection recomp) throws Exception
	{
		TypeRecompense type = getType(recomp);
		if (type == TypeRecompense.ITEM)
		{
			Material material = getMaterial(recomp);
			String nomObjet = CreativeParkour.vaultPresent() ? CPVaultUtils.getItemName(material, (short) recomp.getInt("itemdata")) : material.name().replace("_", " ").toLowerCase();
			return recomp.getInt("amount") + " × " + nomObjet;
		}
		else if (type == TypeRecompense.XP)
		{
			return Langues.getMessage("rewards.xp").replace("%amount", recomp.getString("amount"));
		}
		else if (type == TypeRecompense.MONEY)
		{
			if (!CreativeParkour.vaultPresent())
			{
				Bukkit.getLogger().warning(Config.prefix(false) + "Please install the Vault plugin to use \"MONEY\" rewards.");
				return null;
			}
			return Config.getConfig().getString("rewards.currency").replace("%amount", recomp.getString("amount"));
		}
		return null;
	}

	private static Material getMaterial(ConfigurationSection recomp)
	{
		Material material = null;
		if (CreativeParkour.vaultPresent())
			material = CPVaultUtils.getMaterial(recomp.getString("itemname"));
		if (material == null)
			material = Material.getMaterial(recomp.getString("itemname").replace(" ", "_").toUpperCase());
		return material;
	}

	private static TypeRecompense getType(ConfigurationSection recomp) throws IllegalArgumentException
	{
		return TypeRecompense.valueOf(recomp.getString("type"));
	}

	static void claim(Player p)
	{
		Joueur j = GameManager.getJoueur(p);
		if (j != null && j.getMap() != null) // S'il est dans CreativeParkour
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.claim error"));
		else
		{
			// Vérif du monde
			if (!estUnMondeAutorise(p.getWorld()))
			{
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.claim error 3"));
			}
			else
			{
				List<String> liste = Config.getConfJoueur(p.getUniqueId().toString()).getStringList("rewards.waiting");
				Iterator<String> it = liste.iterator();
				if (liste.isEmpty())
					p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.claim error 2"));
				else
				{
					while (it.hasNext())
					{
						String key = it.next();
						ConfigurationSection recomp = config.getConfigurationSection(key);
						try {
							if (recomp != null)
							{
								TypeRecompense type = getType(recomp);
								if (type == TypeRecompense.ITEM)
								{
									Material material = getMaterial(recomp);
									short data = (short) recomp.getInt("itemdata");
									ItemStack item = new ItemStack(material, recomp.getInt("amount"), data);
									boolean ench = false;
									for (String enchant : recomp.getStringList("itemenchants"))
									{
										ench = true;
										String[] a = enchant.split(":", 2);
										item.addEnchantment(Enchantment.getByName(a[0].replace(" ", "_").toUpperCase()), Integer.valueOf(a[1]));
									}
									if (ench)
									{
										ItemMeta meta = item.getItemMeta();
										List<String> lore = new ArrayList<String>();
										lore.add(ChatColor.GOLD + "" + ChatColor.ITALIC + Langues.getMessage("rewards.item lore"));
										meta.setLore(lore);
										item.setItemMeta(meta);
									}
									p.getInventory().addItem(item);
								}
								else if (type == TypeRecompense.XP)
								{
									p.giveExp(recomp.getInt("amount"));
								}
								else if (type == TypeRecompense.MONEY)
								{
									if (CreativeParkour.vaultPresent())
									{
										if (!CPVaultUtils.giveMoney(p, recomp.getInt("amount")))
											p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("rewards.money error"));
									}
								}

								// Message
								p.sendMessage(ChatColor.GOLD + Langues.getMessage("rewards.received").replace("%reward", recompenseToString(recomp)));
							}

							// Suppression de la récompense
							it.remove();

						} catch (Exception e) {
							e.printStackTrace();
							msgErreur(key);
						}
					}

					Config.getConfJoueur(p.getUniqueId().toString()).set("rewards.waiting", liste);
					Config.saveConfJoueur(p.getUniqueId().toString());
				}
			}
		}
	}

	@EventHandler
	void onPlayerTeleport(final PlayerTeleportEvent e)
	{
		World w = e.getTo().getWorld();
		if (!w.equals(e.getFrom().getWorld()) && !w.getName().equalsIgnoreCase("CreativeParkourMaps") && estUnMondeAutorise(w) && Config.getConfJoueur(e.getPlayer().getUniqueId().toString()).getBoolean("rewards.messages", true)) // Si changement de monde et messages activés
		{
			final List<String> liste = Config.getConfJoueur(e.getPlayer().getUniqueId().toString()).getStringList("rewards.waiting");
			if (!liste.isEmpty())
			{
				Bukkit.getScheduler().runTaskLater(CreativeParkour.getPlugin(), new Runnable() {
					public void run() {
						e.getPlayer().spigot().sendMessage(new ComponentBuilder(Config.prefix() + ChatColor.GOLD + Langues.getMessage("rewards.info").replace("%nb", String.valueOf(liste.size()))).append(" ").append("[" + Langues.getMessage("rewards.get them") + "]").color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour claim")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Langues.getMessage("rewards.get them hover")).create())).append(" ").append("[" + Langues.getMessage("rewards.stop") + "]").color(ChatColor.GRAY).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour claim messages")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Langues.getMessage("rewards.stop hover")).create())).create());
					}
				}, 2);
			}
		}
	}

	private static Set<String> getKeys()
	{
		Set<String> keys = config.getKeys(false);
		keys.remove("example1");
		keys.remove("example2");
		keys.remove("example3");
		return keys;
	}

	private static void msgErreur(String nomRecompense)
	{
		Bukkit.getLogger().warning(Config.prefix(false) + "Something is wrong in reward \"" + nomRecompense + "\", check documentation.");
	}

	static void inverserMessages(Player p)
	{
		String path = PlayerSetting.MSG_REWARD.path();
		YamlConfiguration conf = Config.getConfJoueur(p.getUniqueId().toString());
		boolean val = !conf.getBoolean(path, true);
		conf.set(path, val);
		Config.saveConfJoueur(p.getUniqueId().toString());
		p.sendMessage(Config.prefix() + ChatColor.GREEN + Langues.getMessage(val ? "rewards.info enabled" : "rewards.info disabled"));
	}

	static void supprimerMap(int id)
	{
		for (String key : getKeys())
		{
			if (config.getInt(key + ".map", -1) == id)
			{
				config.set(key + ".map", "deleted");
			}
		}
		saveConf();
	}

	static void saveConf()
	{
		try {
			config.save(file);
		} catch (IOException e) {
			Bukkit.getLogger().warning("an error occurred while loading file 'CreativeParkour/rewards.yml'");
			e.printStackTrace();
		}
	}

	private static boolean estUnMondeAutorise(World w)
	{
		return Config.getConfig().getBoolean("rewards.claim worlds all") ? true : mondesClaim.contains(w.getName().toLowerCase());
	}
}