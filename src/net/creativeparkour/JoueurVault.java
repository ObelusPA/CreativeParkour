package net.creativeparkour;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;

class JoueurVault
{
	private static Permission perms = null;
	Player p;
	HashMap<String, Boolean> valeursPermissions = new HashMap<String, Boolean>();

	static boolean setupPermissions()
	{
		RegisteredServiceProvider<Permission> rsp = CreativeParkour.getPlugin().getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	JoueurVault(Player p)
	{
		this.p = p;
	}

	void autoriser(String permission)
	{
		if (!valeursPermissions.containsKey(permission))
			valeursPermissions.put(permission, p.hasPermission(permission));
		if (valeursPermissions.get(permission) == false) // On ne donne la permission que si le mec ne l'a pas déjà
			perms.playerAdd(p, permission);
	}

	void liberer(String permission)
	{
		if (valeursPermissions.containsKey(permission) && valeursPermissions.get(permission) == false) // On ne retire la permission que si le mec ne l'avait pas
		{
			perms.playerRemove(p, permission);
		}
	}
}
