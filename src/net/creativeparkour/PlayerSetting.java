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

enum VisibiliteJoueurs { VISIBLE, TRANSPARENT, INVISIBLE };
enum TypeMessage { FULL, REDUCED, NONE };

enum PlayerSetting {
	MESSAGES_CP ("checkpoints info", TypeMessage.FULL),
	MESSAGES_CP_SPEC ("checkpoints info spec", true),
	NOTIFICATIONS ("notifications", true),
	MSG_ELYTRES ("elytra message", true),
	MSG_REWARD ("reward.messages", true),
	VISIBILITE_JOUEURS ("players visibility", VisibiliteJoueurs.VISIBLE),
	ENVOYER_FANTOMES ("ghosts.upload", true),
	CHOISIR_MEILLEUR_FANTOME ("ghosts.select best", true),
	CHOISIR_FANTOME_PERSO ("ghosts.select personal", false),
	AFFICHER_FANTOMES_AVANT ("ghosts.display before", false);

	private String path; // Chemin de l'option dans le fichier du joueur
	private Object def; // Valeur par défaut de l'option

	PlayerSetting (String path, Object def)
	{
		this.path = path;
		this.def = def;
	}

	String path() { return path; }
	Object def() { return def; }
}
