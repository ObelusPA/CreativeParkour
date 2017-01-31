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

import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

class PlayerVisibilityManager
{
	private static ProtocolManager protocolManager;
	private static boolean enabled;

	static void enable()
	{
		protocolManager = ProtocolLibrary.getProtocolManager();
		enabled = true;
		protocolManager.addPacketListener(
				new PacketAdapter(CreativeParkour.getPlugin(), ListenerPriority.NORMAL, 
						PacketType.Play.Server.ENTITY_METADATA) {
					@Override
					public void onPacketSending(PacketEvent event) {		        
						WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event.getPacket());
						Joueur j = GameManager.getJoueur(event.getPlayer());
						if (j != null && j.getMap() != null && j.getEtat() == EtatJoueur.JEU)
						{
							Joueur j2 = GameManager.getJoueur(packet.getEntityID());
							if (!j.equals(j2) && j2 != null && j2.getMap() != null && j2.getMap().equals(j.getMap()))
							{
								if (j.visibiliteJoueurs() == VisibiliteJoueurs.TRANSPARENT || j.visibiliteJoueurs() == VisibiliteJoueurs.INVISIBLE)
								{
									Byte input = null;
									// Find the flag value
									for (WrappedWatchableObject object : packet.getMetadata()) {
										if (object.getIndex() == 0) {
											input = (Byte) object.getValue();
											break;
										}
									}

									if (input != null)
									{
										packet = new WrapperPlayServerEntityMetadata(packet.getHandle().deepClone());
										List<WrappedWatchableObject> data = new ArrayList<WrappedWatchableObject>();
										data.add(new WrappedWatchableObject(new WrappedDataWatcherObject(0, Registry.get(Byte.class)), (byte) (input + CPUtils.invisibilityVal)));
										packet.setMetadata(data);
										event.setPacket(packet.getHandle());
									}
								}
							}
						}
					}
				});

		protocolManager.addPacketListener(
				new PacketAdapter(CreativeParkour.getPlugin(), ListenerPriority.NORMAL, 
						PacketType.Play.Server.ENTITY_EQUIPMENT) {
					@Override
					public void onPacketSending(PacketEvent event) {		        
						WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(event.getPacket());
						Joueur j = GameManager.getJoueur(event.getPlayer());
						if (j != null && j.getMap() != null)
						{
							Joueur j2 = GameManager.getJoueur(packet.getEntityID());
							if (!j.equals(j2) && j2 != null && j2.getMap() != null && j2.getMap().equals(j.getMap()))
							{
								if (j.visibiliteJoueurs() == VisibiliteJoueurs.INVISIBLE)
								{
									if (packet.getSlot() == ItemSlot.MAINHAND)
									{
										packet.setItem(null);
									}
								}
							}
						}
					}
				});
	}

	static boolean isEnabled()
	{
		return enabled;
	}

	static void majVisibiliteJoueurs(Joueur joueur) {
		for (Joueur j : GameManager.getJoueurs(joueur.getMap()))
		{
			if (!j.equals(joueur))
			{
				WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata();
				metadata.setEntityID(j.getPlayer().getEntityId());
				List<WrappedWatchableObject> data = new ArrayList<WrappedWatchableObject>();
				data.add(new WrappedWatchableObject(new WrappedDataWatcherObject(0, Registry.get(Byte.class)), j.getValPacketMetadata()));
				metadata.setMetadata(data);
				metadata.sendPacket(joueur.getPlayer());


				WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment();
				equipment.setEntityID(j.getPlayer().getEntityId());
				equipment.setSlot(ItemSlot.MAINHAND);
				equipment.setItem(CPUtils.itemInHand(j.getPlayer()));
				equipment.sendPacket(joueur.getPlayer());
			}
		}
	}
}
