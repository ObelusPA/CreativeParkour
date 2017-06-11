package net.creativeparkour;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

class MainListener implements Listener
{
	@EventHandler
	void onBlockBreak(BlockBreakEvent e)
	{
		Player p = e.getPlayer();
		if (!GameManager.peutConstruire(e.getBlock(), p, true))
		{
			e.setCancelled(true);
		}
		else
		{
			Joueur j = GameManager.getJoueur(p);
			if (j != null && j.getEtat() == EtatJoueur.CREATION && j.worldEdit() != null && CPUtils.itemInHand(p) != null && CPUtils.itemInHand(p).getType() == Config.getWorldEditItem())
			{
				j.worldEdit().verifSelection(j);
			}
		}
	}	

	@EventHandler
	void onBlockDispense(BlockDispenseEvent e)
	{
		if (GameManager.estDansUneMap(e.getBlock()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	void onBlockFromTo(BlockFromToEvent e)
	{
		boolean fromDansMap = GameManager.estDansUneMap(e.getBlock());
		boolean toDansMap = GameManager.estDansUneMap(e.getToBlock());
		if ((fromDansMap && !toDansMap) || (!fromDansMap && toDansMap)) // Interdiction aux liquides de sortir ou entrer dans les maps
		{
			e.setCancelled(true);
		}
		else if (toDansMap)
		{
			// Recherche de la map, si elle est jouable, les liquides sont immobilisés
			for (CPMap m : GameManager.maps.values())
			{
				if (m.containsBlock(e.getToBlock()))
				{
					if (m.isPlayable())
						e.setCancelled(true);
					break;
				}
			}
		}
	}

	@EventHandler
	void onBlockIgnite(BlockIgniteEvent e)
	{
		if (GameManager.estDansUneMap(e.getBlock()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	void onBlockPhysics(BlockPhysicsEvent e)
	{
		Material mat = e.getBlock().getType();
		if (mat == Material.VINE)
		{
			CPMap m = GameManager.getMap(e.getBlock());
			if (m != null && m.isPlayable())
				e.setCancelled(true);
		}
	}

	@EventHandler
	void onBlockPistonExtend(BlockPistonExtendEvent e)
	{

		for (int i=0; i < e.getBlocks().size(); i++)
		{
			if (GameManager.estDansUneMap(e.getBlock()))
			{
				if (!GameManager.estDansUneMap(e.getBlocks().get(i)) || !GameManager.estDansUneMap(e.getBlocks().get(i).getRelative(e.getDirection())))
					e.setCancelled(true);
			}
			else
			{
				if (GameManager.estDansUneMap(e.getBlocks().get(i)) || GameManager.estDansUneMap(e.getBlocks().get(i).getRelative(e.getDirection())))
					e.setCancelled(true);
			}
		}
	}


	@EventHandler
	void onBlockPlace(BlockPlaceEvent e)
	{
		if (!GameManager.peutConstruire(e.getBlock(), e.getPlayer()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	void onBlockRedstone(BlockRedstoneEvent e)
	{
		if (Config.getConfig().getBoolean("game.freeze redstone") && e.getBlock().getWorld().equals(Config.getMonde()))
		{
			if (Config.getMonde().getPlayers().isEmpty())
			{
				e.setNewCurrent(e.getOldCurrent());
			}
			else
			{
				for (CPMap m : GameManager.maps.values())
				{
					if (m.containsBlock(e.getBlock()))
					{
						boolean joueurDansMap = false;
						for (int i=0; i < GameManager.joueurs.size() && !joueurDansMap; i++)
						{
							if (m.getUUID() != null && m.getUUID().equals(GameManager.joueurs.get(i).getMap()))
								joueurDansMap = true;
						}
						if (!joueurDansMap)
							e.setNewCurrent(e.getOldCurrent());
						break;
					}
				}
			}
		}
	}

	@EventHandler
	void onBlockSpread(BlockSpreadEvent e)
	{
		if (GameManager.estDansUneMap(e.getBlock()))
			e.setCancelled(true);
	}

	@EventHandler
	void onCreatureSpawn(CreatureSpawnEvent e)
	{
		if (GameManager.estDansUneMap(e.getLocation().getBlock()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	void onEntityDamage(EntityDamageEvent e)
	{
		if (e.getEntityType().equals(EntityType.PLAYER))
		{
			Player p = (Player) e.getEntity();
			Joueur j = GameManager.getJoueur(p);
			if (j != null && j.getMap() != null)
			{
				if (j.damage)
					j.damage = false;
				else
				{
					e.setCancelled(true);
					p.setFireTicks(0);
				}
			}
		}
		else
		{
			Block b = e.getEntity().getLocation().getBlock();
			for (CPMap m : GameManager.maps.values())
			{
				if (m.containsBlock(b) && m.isPlayable())
				{
					e.setCancelled(true);
					break;
				}
			}
		}
	}

	@EventHandler
	void onEntityExplode(EntityExplodeEvent e)
	{
		if (GameManager.estDansUneMap(e.getLocation().getBlock()))
		{
			e.setCancelled(true);
		}
		else
		{
			List<Block> blocsAAnnuler = new ArrayList<Block>();
			for (int i=0; i < e.blockList().size(); i++)
			{
				if (GameManager.estDansUneMap(e.blockList().get(i)))
				{
					blocsAAnnuler.add(e.blockList().get(i));
				}
			}
			for (int i=0; i < blocsAAnnuler.size(); i++)
			{
				if (e.blockList().contains(blocsAAnnuler.get(i)))
					e.blockList().remove(blocsAAnnuler.get(i));
			}
		}
	}

	@EventHandler
	void onEntityShootBow(EntityShootBowEvent e)
	{
		if (GameManager.estDansUneMap(e.getEntity().getLocation().getBlock()))
			e.setCancelled(true);
	}

	@EventHandler
	void onExpBottle(ExpBottleEvent e)
	{
		if (GameManager.estDansUneMap(e.getEntity().getLocation().getBlock()))
			e.setExperience(0);
	}

	@EventHandler
	void onFoodLevelChange(FoodLevelChangeEvent e)
	{
		if (e.getEntityType().equals(EntityType.PLAYER))
		{
			Player p = (Player) e.getEntity();
			Joueur j = GameManager.getJoueur(p);
			if (j != null && j.getMap() != null)
			{
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	void onHangingBreak(HangingBreakEvent e)
	{
		// Recherche du si c'est dans une map
		Block b = e.getEntity().getLocation().getBlock();
		for (CPMap m : GameManager.maps.values())
		{
			if (m.containsBlock(b) && m.isPlayable())
			{
				e.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler
	void onInventoryClick(InventoryClickEvent e)
	{
		Player p = (Player) e.getWhoClicked();
		if (e.getInventory() != null)
		{
			try {
				Joueur j = GameManager.getJoueur(p);
				if (j != null)
				{
					if (j.invSelection != null && e.getCurrentItem() != null && e.getInventory().equals(j.invSelection.getInventaire()))
					{
						e.setCancelled(true);
						j.invSelection.clic(e.getSlot(), e.getClick());
						e.setCancelled(true);
					}
					else if (j.invCreation != null && e.getCurrentItem() != null && e.getInventory().equals(j.invCreation.getInventaire()))
					{
						e.setCancelled(true);
						j.invCreation.clic(e.getSlot());
					}
					else if (j.invAutresMaps != null && e.getCurrentItem() != null && e.getInventory().equals(j.invAutresMaps.getInventaire()))
					{
						e.setCancelled(true);
						j.invAutresMaps.clic(e.getSlot(), e.getClick());
					}
					else if (j.invFantomes != null && e.getCurrentItem() != null && e.getInventory().equals(j.invFantomes.getInventaire()))
					{
						e.setCancelled(true);
						j.invFantomes.clic(e.getSlot(), e.getClick());
						e.setCancelled(true);
					}
					else if (j.invOptionsMaps != null && e.getCurrentItem() != null && e.getInventory().equals(j.invOptionsMaps.getInventaire()))
					{
						e.setCancelled(true);
						j.invOptionsMaps.clic(e.getSlot());
					}
					else if (j.invParametres != null && e.getCurrentItem() != null && e.getInventory().equals(j.invParametres.getInventaire()))
					{
						e.setCancelled(true);
						j.invParametres.clic(e.getSlot());
					}
					else if (j.getEtat() == EtatJoueur.JEU || j.getEtat() == EtatJoueur.SPECTATEUR)
					{
						e.setCancelled(true);
						if (j.getEtat() == EtatJoueur.SPECTATEUR)
						{
							p.sendMessage(Config.prefix() + ChatColor.YELLOW + Langues.getMessage("play.spectator disable"));
						}
					}
				}
			} catch (Exception exc) {
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("error"));
				CreativeParkour.erreur("INVENTORYCLICK", exc, true);
			}
		}
	}
	@EventHandler
	void onInventoryClose(InventoryCloseEvent e)
	{
		if (e.getPlayer() instanceof Player)
		{
			Joueur j = GameManager.getJoueur((Player) e.getPlayer());
			if (j != null)
			{
				j.invSelection = null;
				j.invCreation = null;
				j.invAutresMaps = null;
				if (j.invFantomes != null)
					j.downloadGhosts();
				j.invFantomes = null;
				j.invOptionsMaps = null;
				j.invParametres = null;
			}
		}
	}
	@EventHandler
	void onItemDrop(PlayerDropItemEvent e)
	{
		Joueur j = GameManager.getJoueur(e.getPlayer());
		if (j != null && j.getEtat() == EtatJoueur.JEU)
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	void onPlayerBucketEmpty(PlayerBucketEmptyEvent e)
	{
		if (!GameManager.peutConstruire(e.getBlockClicked().getRelative(e.getBlockFace()), e.getPlayer()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	void onPlayerGameModeChange(PlayerGameModeChangeEvent e)
	{
		Joueur j = GameManager.getJoueur(e.getPlayer());
		if (j != null && j.getMap() != null)
		{
			// Seulement si c'est moins de 5 secondes après être entré dans la map
			if (j.getDernierJeu() != null && new Date().getTime() - j.getDernierJeu().getTime() < 5000  && ((j.getEtat() == EtatJoueur.JEU && !e.getNewGameMode().equals(GameMode.ADVENTURE)) || (j.getEtat() == EtatJoueur.SPECTATEUR && !e.getNewGameMode().equals(GameMode.SPECTATOR)) || (j.getEtat() == EtatJoueur.CREATION && !e.getNewGameMode().equals(GameMode.CREATIVE))))
				e.setCancelled(true);
		}
	}

	@EventHandler
	void onPlayerInteract(PlayerInteractEvent e)
	{
		try {
			if (e.getHand() != EquipmentSlot.HAND) // Pour éviter que l'événement passe 2 fois (une fois pour chaque main)
			{
				return;
			}
		} catch (NoSuchMethodError err) {
			// Rien
		}
	
		Block b = e.getClickedBlock();
		Player p = e.getPlayer();
		Joueur j = GameManager.getJoueur(p);
		CPMap m = null;
		if (j != null)
		{
			m = j.getMapObjet();
		}
	
		if (e.getAction() == Action.PHYSICAL && b.getType() == Material.SOIL && m != null) // S'il saute sur de la terre labourée, on annule
		{
			e.setCancelled(true);
		}
		else
		{
			ItemStack objetMain = CPUtils.itemInHand(p);
			if (m != null && j.aFusees() && !CPUtils.itemStackIsEmpty(objetMain) && objetMain.getType() == Material.FIREWORK)
			{
				boolean gliding = false;
				try {
					gliding = j.getPlayer().isGliding();
				} catch (NoSuchMethodError err) {
					// Nothing
				}
				if (!gliding)
				{
					j.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.firework error"));
					e.setCancelled(true);
				}
				else
					j.donnerFusees();
			}
			else if (m != null && !CPUtils.itemStackIsEmpty(objetMain) && objetMain.getItemMeta() != null && objetMain.getItemMeta().getDisplayName() != null && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && (j.getEtat() == EtatJoueur.JEU || j.getEtat() == EtatJoueur.SPECTATEUR))
			{
				e.setCancelled(true);
				if (objetMain.getItemMeta().getDisplayName().contains(Langues.getMessage("play.items.return start")))
				{
					j.tpAvecSpectateurs(m.getSpawn().getLocation().add(0.5, 0, 0.5));
					p.getInventory().setItem(1, null);
					j.stopTimer();
					j.retirerElytres();
					j.retirerPerles();
					j.modeSpectateur(false); // Désactivation du mode spectateur s'il y est
					j.giveMontre();
					p.playSound(p.getLocation(), CPUtils.getSound("ENTITY_ENDERMEN_TELEPORT", "ENDERMAN_TELEPORT"), 1, 1);
				}
				else if (objetMain.getItemMeta().getDisplayName().contains(Langues.getMessage("play.items.return checkpoint")))
				{
					if (j.getCheckpoints().size() > 0 && j.getTask() != null)
					{
						j.tpAvecSpectateurs(j.getCheckpoints().get(j.getCheckpoints().size()-1).getLocation().add(0.5, 0, 0.5));
					}
					else
					{
						j.tpAvecSpectateurs(m.getSpawn().getLocation().add(0.5, 0, 0.5));
						j.stopTimer();
					}
					j.modeSpectateur(false); // Désactivation du mode spectateur s'il y est
					p.playSound(p.getLocation(), CPUtils.getSound("ENTITY_ENDERMEN_TELEPORT", "ENDERMAN_TELEPORT"), 1, 1);
				}
				else if (m != null && (m.isPlayable() || m.contientTesteur(p)) && objetMain.getItemMeta().getDisplayName().contains(Langues.getMessage("play.items.player visibility")))
				{
					j.changerVisibiliteJoueurs(true);
				}
				else if (m != null && m.isPlayable() && objetMain.getItemMeta().getDisplayName().contains(Langues.getMessage("play.items.leaderboard")))
				{
					j.inverserLeaderboards();
					p.playSound(p.getLocation(), CPUtils.getSound("ENTITY_ITEM_PICKUP", "ITEM_PICKUP"), 1, 1);
				}
				else if (m != null && m.isPlayable() && objetMain.getItemMeta().getDisplayName().contains(Langues.getMessage("play.items.spectator")))
				{
					if (p.hasPermission("creativeparkour.spectate"))
					{
						j.modeSpectateur(j.getEtat() != EtatJoueur.SPECTATEUR); // Echange de son mode (jeu/spectateur)
						p.playSound(p.getLocation(), CPUtils.getSound("ENTITY_ITEM_PICKUP", "ITEM_PICKUP"), 1, 1);
					}
				}
				else if (m != null && m.isPlayable() && objetMain.getItemMeta().getDisplayName().contains(Langues.getMessage("play.items.map options")))
				{
					Commandes.texteQuestion(p);
					String n = m.getName();
					if (n == null || n.isEmpty())
					{
						n = "unnamed";
					}
					if (Config.online() && m.getCreator().equals(p.getUniqueId()))
						p.spigot().sendMessage(new ComponentBuilder(" ➥ ").color(ChatColor.YELLOW).append(Langues.getMessage("commands.share message")).color(ChatColor.AQUA).bold(true)
								.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(CPUtils.dividedTextToString(CPUtils.divideText(Langues.getMessage("commands.share info").replace("%map", ChatColor.ITALIC + n + ChatColor.RESET), null))).create()))
								.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour share")).create());
					p.spigot().sendMessage(new ComponentBuilder(" ➥ ").color(ChatColor.YELLOW).append(Langues.getMessage("commands.edit message")).color(ChatColor.LIGHT_PURPLE).bold(true)
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(CPUtils.dividedTextToString(CPUtils.divideText(Langues.getMessage("commands.edit info").replace("%map", ChatColor.ITALIC + n + ChatColor.RESET), null))).create()))
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour edit")).create());
					p.spigot().sendMessage(new ComponentBuilder(" ➥ ").color(ChatColor.YELLOW).append(Langues.getMessage("commands.delete message")).color(ChatColor.RED).bold(true)
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Langues.getMessage("commands.cannot be undone")).color(ChatColor.RED).create()))
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/creativeparkour delete")).create());
				}
				else if (m != null && m.isPlayable() && objetMain.getItemMeta().getDisplayName().contains(Langues.getMessage("play.items.ghosts")))
				{
					if (Config.fantomesPasInterdits())
					{
						if (!CreativeParkour.auMoins1_9())
						{
							p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("outdated server").replace("%ver", "1.9"));
						}
						else if (!CreativeParkour.protocollibPresent()) // Si les fantômes sont activés mais que ProtocolLib n'est pas là
						{
							p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.error protocollib"));
							if (p.isOp())
								p.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "https://www.spigotmc.org/resources/protocollib.1997");
						}
						else
						{
							j.openGhostSelection();
						}
					}
				}
				else if (objetMain.getItemMeta().getDisplayName().toLowerCase().contains(Langues.getMessage("play.items.leave test").toLowerCase()) && m.contientTesteur(p))
				{
					GameManager.quitterTest(p);
				}
				else if (objetMain.getItemMeta().getDisplayName().toLowerCase().contains(Langues.getCommand("leave").toLowerCase()))
				{
					j.quitter(true, false);
					p.playSound(p.getLocation(), CPUtils.getSound("ENTITY_ENDERMEN_TELEPORT", "ENDERMAN_TELEPORT"), 1, 1);
				}
			}
			else if (b != null && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST) || b.getType().equals(Material.DROPPER) || b.getType().equals(Material.DISPENSER) || b.getType().equals(Material.FURNACE) || b.getType().equals(Material.ANVIL) || b.getType().equals(Material.ENCHANTMENT_TABLE) || b.getType().name().contains("SHULKER")) && GameManager.estDansUneMap(b))
			{
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
				e.setCancelled(true);
			}
			else if (b != null && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (b.getType().name().contains("DOOR") || b.getType().name().contains("FENCE_GATE")) && GameManager.estDansUneMap(b))
			{
				if (m == null)
				{
					p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
					e.setCancelled(true);
				}
				else if (!m.interactionsAutorisees)
				{
					if (j.getEtat() == EtatJoueur.CREATION)
					{
						// On dévalide la map
						m.setValide(false);
						j.getPlayer().setScoreboard(m.getScoreboardC());
	
						CPUtils.sendInfoMessage(p, Langues.getMessage("play.interactions disabled when playing"));
					}
					else
					{
						p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.interactions disabled"));
						e.setCancelled(true);
					}
				}
			}
			if (j != null && j.worldEdit() != null && j.getMap() != null && b != null && e.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				j.worldEdit().verifSelection(j);
			}
		}
	}

	@EventHandler
	void onPlayerInteractEntity(PlayerInteractEntityEvent e)
	{
		Joueur j = GameManager.getJoueur(e.getPlayer());
		if (j != null && j.getMap() != null && j.getMapObjet().isPlayable())
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	void onPlayerJoin(PlayerJoinEvent e)
	{
		// Si le joueur est toujours dans une map, on l'intégre dans la map s'il a le droit
		GameManager.reintegrerMapOuQuitter(e.getPlayer(), Config.getConfig().getBoolean("game.exit on login"));
	}

	@EventHandler
	void onPlayerMove(PlayerMoveEvent e)
	{
		final Player p = e.getPlayer();
		final Joueur j = GameManager.getJoueur(p);
		final Location l = e.getTo();
		final Block b = e.getTo().getBlock();
		CPMap m = null;
		if (j != null)
		{
			// Sécurité pour NullPointerException
			m = j.getMapObjet();
		}
		if (m == null && l.getWorld().equals(Config.getMonde()) && GameManager.estDansUneMap(l.getBlock()))
		{
			// Si un joueur tente d'entrer dans une map
			p.teleport(e.getFrom());
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
		}
		else if (j != null && m != null && l.getWorld().equals(m.getWorld()) && (!m.containsBlock(b) || l.getY() >= m.getMaxLoc().getY()))
		{
			// Si le joueur sort de la map
			e.setCancelled(true);
			if (j.aElytres())
			{
				ItemStack item = p.getInventory().getArmorContents()[2];
				try {
					if (item != null && item.getType() == Material.ELYTRA)
					{
						p.getInventory().setArmorContents(null);
						new BukkitRunnable() {				
							public void run()
							{
								j.donnerElytres();
							}
						}.runTaskLater(CreativeParkour.getPlugin(), 2);
					}
				} catch (NoSuchFieldError error) {
					// Rien
				}
			}
		}
		else if (j != null && m != null && j.getEtat() == EtatJoueur.JEU)
		{
			if (p.isSneaking() && !m.sneakAutorise)
			{
				// Si le joueur sneak alors que c'est interdit
				e.setCancelled(true);
				if (j.dernierMsgSneak == null || new Date().getTime() > j.dernierMsgSneak.getTime() + 1000) // Pour éviter le spam
				{
					p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.sneak disabled"));
					j.dernierMsgSneak = new Date();
				}
			}
			else if (l.getY() <= m.getHauteurMort() + 0.4)
			{
				// Si le joueur tombe
				List<BlocCheckpoint> checkpoints = j.getCheckpoints();
				if (checkpoints.size() > 0)
				{
					j.tpAvecSpectateurs(checkpoints.get(checkpoints.size()-1).getLocation().add(0.5, 0, 0.5));
				}
				else
				{
					j.tpAvecSpectateurs(m.getSpawn().getLocation().add(0.5, 0, 0.5));
					j.giveMontre();
					j.retirerElytres();
					j.retirerPerles();
					j.stopTimer();
				}
				p.playSound(p.getLocation(), CPUtils.getSound("ENTITY_ENDERMEN_TELEPORT", "ENDERMAN_TELEPORT"), 1, 1);
			}
			else
			{
				// Recherche de lave ou eau mortelles autour du joueur
				boolean mort = false;
				if (m.mortLave || m.mortEau)
				{
					List<Block> blocs = new ArrayList<Block>(); // Blocs à vérifier
					blocs.add(b);
					BlockFace [] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_WEST, BlockFace.SOUTH_EAST};
					for (BlockFace bf : faces)
					{
						blocs.add(b.getRelative(bf));
					}
					// Vérif de chaque bloc de la liste
					for (Block rel : blocs)
					{
						if (CPUtils.blockTouched(rel, l))
						{
							if (m.mortLave && rel.getType() == Material.STATIONARY_LAVA)
							{
								mort = true;
								j.tuer();
								if (!j.infoMortLave)
								{
									CPUtils.sendInfoMessage(p, Langues.getMessage("play.deadly lava"));
									j.infoMortLave = true;
								}
								break;
							}
							else if (m.mortEau && rel.getType() == Material.STATIONARY_WATER)
							{
								mort = true;
								j.tuer();
								if (!j.infoMortEau)
								{
									CPUtils.sendInfoMessage(p, Langues.getMessage("play.deadly water"));
									j.infoMortEau = true;
								}
								break;
							}
						}
					}
				}

				if (!mort)
				{
					// Recherche de blocs spéciaux passés
					for (BlocSpecial bs : m.getBlocsSpeciaux())
					{
						if (bs.estPasse(l))
							bs.faireAction(j);
					}
				}
			}

			// Comptage des sauts du joueur
			if (CreativeParkour.stats() != null)
			{
				if (e.getTo().getY() > e.getFrom().getY())
				{
					if (!j.enSaut)
					{
						j.enSaut = true;
						CreativeParkour.stats().nbSauts++;
					}
				}
				else
					j.enSaut = false;
			}
		}
	}

	@EventHandler
	void onPlayerPickupItem(PlayerPickupItemEvent e)
	{
		Joueur j = GameManager.getJoueur(e.getPlayer());
		if (j != null && j.getMap() != null && j.getEtat() == EtatJoueur.JEU)
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	void onPlayerQuit(PlayerQuitEvent e)
	{
		Joueur j = GameManager.getJoueur(e.getPlayer());
		if (j != null && j.getMap() != null)
		{
			j.setEtat(EtatJoueur.DECONNECTE);
			j.quitter(false, false);
			GameManager.joueurs.remove(j);
		}
	}

	//	@EventHandler
	//	void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) // TODO Remettre quand tout le monde aura la 1.9
	//	{
	//		Player p = e.getPlayer();
	//		Joueur j = GameManager.getJoueur(p);
	//		if (j != null && j.getMap() != null)
	//		{
	//			e.setCancelled(true);
	//		}
	//	}

	@EventHandler
	void onPlayerTeleport(PlayerTeleportEvent e)
	{
		Joueur j = GameManager.getJoueur(e.getPlayer());
		Location l = e.getTo();
		CPMap m = null;
		if (j != null)
		{
			m = j.getMapObjet();
		}
		if ((m == null || !m.containsBlock(l.getBlock())) && l.getWorld().equals(Config.getMonde()) && GameManager.estDansUneMap(l.getBlock()))
		{
			e.setCancelled(true);
			e.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("not allowed"));
			e.getPlayer().sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + Langues.getMessage("commands.tp info"));
		}
		else if (j != null && m != null && (!m.containsBlock(l.getBlock())))
		{
			if (e.getCause().equals(TeleportCause.SPECTATE) || e.getCause().equals(TeleportCause.ENDER_PEARL))
			{
				e.setCancelled(true);
				e.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("play.no tp"));
				if (j.aPerles())
					j.donnerPerles();
			}
			else
			{
				if (Config.getConfig().getBoolean("game.only leave with creativeparkour command"))
				{
					e.setCancelled(true);
					e.getPlayer().sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("commands.leave only"));
				}
				else
					GameManager.supprJoueur(j, false);
			}
		}
		else if (j != null && m != null && e.getCause().equals(TeleportCause.ENDER_PEARL) && j.aPerles())
		{
			j.donnerPerles();
			if (l.getY() - 1 <= m.getHauteurMort())
				e.setCancelled(true);
		}
		else if (j == null && GameManager.estDansUneMap(e.getFrom().getBlock()) && !GameManager.estDansUneMap(e.getTo().getBlock()))
		{
			new Joueur(e.getPlayer(), false).restaurerTrucs();
		}
	}

	@EventHandler
	void onSignChange(SignChangeEvent e)
	{
		Player p = e.getPlayer();
		Joueur j = GameManager.getJoueur(p);
		boolean panneauSpecialDebout = e.getLine(0).toLowerCase().contains(BlocSpawn.getTag());

		if (j != null && j.getEtat() == EtatJoueur.CREATION && e.getBlock().getY() >= j.getMapObjet().getMaxLoc().getY() - 1) // S'il est juste au dessous du "plafond" de la map
		{
			e.getBlock().breakNaturally();
			p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.sign too high"));
		}
		else
		{
			if (j != null && j.getEtat() == EtatJoueur.CREATION && e.getBlock().getType().equals(Material.WALL_SIGN) && panneauSpecialDebout)
			{
				p.sendMessage(Config.prefix() + ChatColor.RED + Langues.getMessage("creation.check.sign post error"));
				e.getBlock().setType(Material.SIGN_POST);
				Sign s = (Sign) e.getBlock().getState();
				s.setLine(0, e.getLine(0));
				s.update();
				if (e.getBlock().getRelative(BlockFace.DOWN).isEmpty())
				{
					e.getBlock().getRelative(BlockFace.DOWN).setType(Material.STAINED_CLAY);
				}
			}
			if (j != null && j.getEtat() == EtatJoueur.CREATION && e.getBlock().getType().equals(Material.SIGN_POST) && e.getLine(0).toLowerCase().contains(BlocEffet.getTag()))
			{
				BlocEffet.estUnPanneauValide(e.getLines(), p, e.getBlock());
			}
		}
	}

	@EventHandler
	void onStructureGrow(StructureGrowEvent e)
	{
		if (e.getWorld().equals(Config.getMonde()))
		{
			if (GameManager.estDansUneMap(e.getLocation().getBlock()))
			{
				if (!e.isFromBonemeal()) // Annulation des pousses d'arbre naturelles
					e.setCancelled(true);
				else
				{
					List<BlockState> blocs = e.getBlocks();
					for (int i=0; i < blocs.size(); i++)
					{
						if (!GameManager.estDansUneMap(blocs.get(i).getBlock())) // Coupe des bouts qui dépassent
							blocs.get(i).setType(Material.AIR);
					}
				}
			}
			else
			{
				List<BlockState> blocs = e.getBlocks();
				for (int i=0; i < blocs.size(); i++)
				{
					if (GameManager.estDansUneMap(blocs.get(i).getBlock())) // Annulation de la posse si des bouts dépassent
					{
						e.setCancelled(true);
						return;
					}
				}
			}
		}
	}


	@EventHandler
	void onVehicleMove(VehicleCreateEvent e)
	{
		if (GameManager.estDansUneMap(e.getVehicle().getLocation().getBlock()))
		{
			e.getVehicle().remove();
		}
	}

	@EventHandler
	void onWeatherChange(WeatherChangeEvent e)
	{
		if (CreativeParkour.loaded && Config.pluginActive() && e.getWorld().getName().equalsIgnoreCase(Config.getConfig().getString("map storage.map storage world")))
		{
			e.setCancelled(true);
		}
	}
}
