/*
  This file is part of BORG.

  BORG is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  BORG is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with BORG; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

  Copyright 2003 by Mike Berger
 */

package net.sf.borg.ui;

import net.sf.borg.common.Resource;

import javax.swing.*;

/*
 * Disable mnemonic feature in 1.8. Feature was never properly maintained - causing problems.
 */
/**
 * Helps parse resource strings (containing text and optional mnemonics and
 * accelerators) for GUI widgets.
 * 
 * @author membar
 */
public class ResourceHelper {


	public static void setText(JMenu mnu, String resourceKey) {
		mnu.setText(Resource.getResourceString(resourceKey));

	}


	public static void setText(JMenuItem mnuitm, String resourceKey) {
		mnuitm.setText(Resource.getResourceString(resourceKey));

	}


	public static void setText(AbstractButton button, String resourceKey) {
		button.setText(Resource.getResourceString(resourceKey));

	}


	public static void setText(JLabel label, String resourceKey) {
		label.setText(Resource.getResourceString(resourceKey));

	}



}
