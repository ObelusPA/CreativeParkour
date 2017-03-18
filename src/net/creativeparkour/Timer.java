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

import org.bukkit.scheduler.BukkitRunnable;

class Timer extends BukkitRunnable {

	private int counter;
	private Joueur j;

	Timer(Joueur joueur)
	{
		counter = 0;
		j = joueur;
	}

	public void run() {
		if (j.getEtat() != EtatJoueur.JEU)
			this.cancel();
		else if (counter < 216000) // Au bout de 3 heures, on arrête
		{
			counter++;
			j.timerMaj(counter, this);
		} else {
			this.cancel();
			j.quitter(true, false);
		}
	}

}