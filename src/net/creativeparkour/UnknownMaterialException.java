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

class UnknownMaterialException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5633482695552616452L;

	public UnknownMaterialException() {
		
	}

	public UnknownMaterialException(String arg0) {
		super(arg0);
		
	}

	public UnknownMaterialException(Throwable cause) {
		super(cause);
		
	}

	public UnknownMaterialException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public UnknownMaterialException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
