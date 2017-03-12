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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.packetwrapper.WrapperPlayServerEntityHeadRotation;
import com.comphenix.packetwrapper.WrapperPlayServerEntityLook;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerNamedEntitySpawn;
import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.packetwrapper.WrapperPlayServerRelEntityMove;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import net.md_5.bungee.api.ChatColor;

/**
 * Représente un fantôme, mais seulement l'entité pour un joueur
 */
class Fantome
{
	private UUID uuidFantome;
	private String nomFantome;
	private CPTime temps;
	private Location locBase;
	private CPPlayerPosition posActuelle;
	private int entityID;
	private Joueur regardeur;
	private int i;
	private BukkitTask task;
	private byte valPreced;
	private int derniereMajScoreboard;

	Fantome (CPTime temps, Joueur regardeur)
	{
		this.uuidFantome = temps.playerUUID;
		this.nomFantome = CPUtils.truncatedStr(NameManager.getNomAvecUUID(uuidFantome), 16);
		this.temps = temps;
		this.locBase = temps.map.getMinLoc().getLocation();
		this.entityID = 0;
		this.regardeur = regardeur;
		this.i = 4; // On lui donne une petite avance car il y a un décalage inconnu
		if (regardeur.getParamBool(PlayerSetting.AFFICHER_FANTOMES_AVANT))
			this.i += 20; // Avance d'une seconde
		this.valPreced = 0;
		this.derniereMajScoreboard = 0;
	}

	void demarrer()
	{
		task = Bukkit.getScheduler().runTaskTimer(CreativeParkour.getPlugin(), new Runnable() {
			public void run() {
				incrementation();
			}
		}, 0, 1);
	}

	void arreter()
	{
		task.cancel();

		// Suppression de l'entité
		WrapperPlayServerEntityDestroy entite = new WrapperPlayServerEntityDestroy();
		int[] entityIDs = { entityID };
		entite.setEntityIds(entityIDs);
		entite.sendPacket(regardeur.getPlayer());

		Player playerFantome = Bukkit.getPlayer(uuidFantome);
		if (playerFantome == null || !regardeur.getPlayer().canSee(playerFantome)) // Pour pas que ça enlève le vrai joueur de la liste
		{
			WrapperPlayServerPlayerInfo playerInfo = new WrapperPlayServerPlayerInfo();
			playerInfo.setAction(PlayerInfoAction.REMOVE_PLAYER);
			List<PlayerInfoData> data = new ArrayList<PlayerInfoData>();
			data.add(new PlayerInfoData(new WrappedGameProfile(uuidFantome, nomFantome), 0, NativeGameMode.NOT_SET, WrappedChatComponent.fromText(nomFantome)));
			playerInfo.setData(data);
			playerInfo.sendPacket(regardeur.getPlayer());
		}
	}

	protected void incrementation()
	{
		CPPlayerPosition nouvellePos = temps.ghostData.get(i);
		Player player = regardeur.getPlayer();
		if (nouvellePos != null) // Si on a des données pour ce tick
		{
			if (entityID <= 0) // Si on ne l'a pas encore spawné
			{
				// Apparition du joueur pour la première fois
				Player playerFantome = Bukkit.getPlayer(uuidFantome);
				if (playerFantome == null || !regardeur.getPlayer().canSee(playerFantome)) // Pour pas que ça enlève le vrai joueur de la liste
				{
					WrapperPlayServerPlayerInfo playerInfo = new WrapperPlayServerPlayerInfo();
					playerInfo.setAction(PlayerInfoAction.ADD_PLAYER);
					List<PlayerInfoData> data = new ArrayList<PlayerInfoData>();
					WrappedGameProfile profile = new WrappedGameProfile(uuidFantome, nomFantome);
					WrappedSignedProperty textures = PlayerProfiles.getTextures(uuidFantome);
					if (textures != null)
						profile.getProperties().put("textures", textures);
					data.add(new PlayerInfoData(profile, 0, NativeGameMode.NOT_SET, WrappedChatComponent.fromText(nomFantome)));
					playerInfo.setData(data);
					playerInfo.sendPacket(player);
				}

				posActuelle = nouvellePos;
				spawnerEntite(posActuelle.getRealLoc(locBase), player);

				// Ajout du fantome à l'équipe du mec qui regarde
				regardeur.ajouterJoueurEquipe(nomFantome);
			}
			else
			{
				// On bouge le fantôme
				Vector nouvelleLoc = nouvellePos.getVector();
				Vector ancienneLoc = posActuelle.getVector();
				int dx = (int) ((nouvelleLoc.getX() * 32 - ancienneLoc.getX() * 32) * 128);
				int dy = (int) ((nouvelleLoc.getY() * 32 - ancienneLoc.getY() * 32) * 128);
				int dz = (int) ((nouvelleLoc.getZ() * 32 - ancienneLoc.getZ() * 32) * 128);
				if (Math.abs(dx) < 32768 && Math.abs(dy) < 32768 && Math.abs(dz) < 32768) // S'il bouge de moins de 8 blocs, RelEntityMove
				{
					WrapperPlayServerRelEntityMove move = new WrapperPlayServerRelEntityMove();
					move.setEntityID(entityID);
					move.setDx(dx);
					move.setDy(dy);
					move.setDz(dz);
					move.sendPacket(player);
				}
				else // S'il bouge de plus de 8 blocs, téléportation (avec spawn car ça pue la merde)
				{
					/*WrapperPlayServerEntityTeleport tp = new WrapperPlayServerEntityTeleport();
					tp.setEntityID(entityID);
					Location l = nouvellePos.getLocReelle(locBase);
					tp.setX(l.getX());
					tp.setY(l.getY());
					tp.setY(l.getY());
					tp.setPitch(l.getPitch());
					tp.setYaw(l.getYaw());
					tp.setOnGround(true);
					tp.sendPacket(player);*/

					spawnerEntite(nouvellePos.getRealLoc(locBase), player);
					valPreced = 0; // On réinitialise ça pour réappliquer les "effets"
				}

				// Regard
				WrapperPlayServerEntityLook look = new WrapperPlayServerEntityLook();
				look.setEntityID(entityID);
				look.setPitch(nouvellePos.getPitch());
				look.setYaw(nouvellePos.getYaw());
				look.sendPacket(player);
				WrapperPlayServerEntityHeadRotation rotation = new WrapperPlayServerEntityHeadRotation();
				rotation.setEntityID(entityID);
				rotation.setHeadYaw((byte) (nouvellePos.getYaw() * 256.0F / 360.0));
				rotation.sendPacket(player);

				// Sneak
				byte val = 0;
				if (regardeur.getEtat() == EtatJoueur.JEU) // Pas de transparence si le joueur est spectateur
					val += CPUtils.invisibilityVal;
				if (nouvellePos.isSneaking())
					val += CPUtils.sneakVal;
				// Elytres
				if (nouvellePos.hasElytra())
					val += CPUtils.elytraVal;
				if (val != valPreced)
				{
					WrapperPlayServerEntityMetadata sneak = new WrapperPlayServerEntityMetadata();
					sneak.setEntityID(entityID);
					List<WrappedWatchableObject> data = new ArrayList<WrappedWatchableObject>();
					data.add(new WrappedWatchableObject(new WrappedDataWatcherObject(0, Registry.get(Byte.class)), val));
					sneak.setMetadata(data);
					sneak.sendPacket(player);
					valPreced = val;
				}

				if (posActuelle.hasPearl() != nouvellePos.hasPearl())
				{
					WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment();
					equipment.setEntityID(entityID);
					equipment.setSlot(ItemSlot.MAINHAND);
					equipment.setItem(nouvellePos.hasPearl() ? new ItemStack(Material.ENDER_PEARL) : null);
					equipment.sendPacket(player);
				}


				posActuelle = nouvellePos;
			}
		}

		if (i >= temps.ticks || i < 0)
			arreter();
		else if (nouvellePos == null) // Si pas de position, on attent qu'il y en ait une pour éviter de ne tomber que sur du null en cas de multiplicateur
			i += Integer.signum(regardeur.ghostIncrementation); // 1 ou -1 selon le signe
		else
			i += regardeur.ghostIncrementation;
		if (i - derniereMajScoreboard >= 20) // Affichage (ou enlevage) des temps des fantômes en direct du scoreboard du regardeur
		{
			derniereMajScoreboard = i;
			String s = CPUtils.scoreboardName(CPUtils.ucfirst(Langues.getMessage("play.ghosts.ghosts")), ChatColor.BLUE);
			if (regardeur.getEtat() == EtatJoueur.SPECTATEUR)
			{
				regardeur.getObjective().getScore(s).setScore((int) (i * 0.05));
			}
			else // Suppression du score
			{
				regardeur.scoreboard.resetScores(s);
			}
		}
	}

	void spawnerEntite(Location l, Player player)
	{
		WrapperPlayServerNamedEntitySpawn entite = new WrapperPlayServerNamedEntitySpawn();
		if (entityID <= 0)
			entityID = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
		entite.setEntityID(entityID);
		entite.setPosition(l.toVector());
		entite.setPlayerUUID(uuidFantome);
		entite.setPitch(l.getPitch());
		entite.setYaw(l.getYaw());
		entite.sendPacket(player);
	}

	void rewind(int ticks)
	{
		i -= ticks;
		i = Math.max(i, 0); // Pour ne pas avoir un nombre négatif
		derniereMajScoreboard = i;
	}

	void setTick(int tick)
	{
		i = Math.max(tick, 0);
		derniereMajScoreboard = i;
	}
}
