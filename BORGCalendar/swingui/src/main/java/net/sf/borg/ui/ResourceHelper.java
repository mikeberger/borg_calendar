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

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.borg.common.Resource;

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

	/**
	 * set the text and pnemonic for a JMenu
	 * 
	 * @param mnu
	 *            the menu
	 * @param resourceKey
	 *            the resource string with optional pnemonic data
	 */
	public static void setText(JMenu mnu, String resourceKey) {
		mnu.setText(Resource.getResourceString(resourceKey));
		/*
		ComponentParms parms = parseParms(resourceKey);
		mnu.setText(parms.getText());
		if (parms.getKeyEvent() != -1)
			mnu.setMnemonic(parms.getKeyEvent());
			*/
	}

	/**
	 * set the text and pnemonic for a JMenuItem
	 * 
	 * @param mnuitm
	 *            the JMenuItem
	 * @param resourceKey
	 *            the resource string with optional pnemonic data
	 */
	public static void setText(JMenuItem mnuitm, String resourceKey) {
		mnuitm.setText(Resource.getResourceString(resourceKey));
/*
		ComponentParms parms = parseParms(resourceKey);
		mnuitm.setText(parms.getText());
		if (parms.getKeyEvent() != -1)
			mnuitm.setMnemonic(parms.getKeyEvent());
		if (parms.getKeyStroke() != null)
			mnuitm.setAccelerator(parms.getKeyStroke());*/
	}

	/**
	 * set the text and pnemonic for a button
	 * 
	 * @param button
	 *            the button
	 * @param resourceKey
	 *            the resource string with optional pnemonic data
	 */
	public static void setText(AbstractButton button, String resourceKey) {
		button.setText(Resource.getResourceString(resourceKey));

/*		ComponentParms parms = parseParms(resourceKey);
		button.setText(parms.getText());
		if (parms.getKeyEvent() != -1)
			button.setMnemonic(parms.getKeyEvent()); */
	}

	/**
	 * set the text and pnemonic for a label
	 * 
	 * @param label
	 *            the label
	 * @param resourceKey
	 *            the resource string with optional pnemonic data
	 */
	public static void setText(JLabel label, String resourceKey) {
		label.setText(Resource.getResourceString(resourceKey));

	/*	ComponentParms parms = parseParms(resourceKey);
		label.setText(parms.getText());
		if (parms.getKeyEvent() != -1)
			label.setDisplayedMnemonic(parms.getKeyEvent()); */
	}

	
	
	/**
	 * get a resource string and parse out the various parts - the text and the
	 * keyboard shortcut into
	 * 
	 * @param resourceKey
	 *            the resource key
	 * 
	 * @return the ComponentParms object
	 */
	/*
	public static ComponentParms parseParms(String resourceKey) {
		String parmsText = Resource.getRawResourceString(resourceKey);

		String text = parmsText;
		int mnemonic = -1;
		KeyStroke accel = null;
		int pos;
		if ((pos = parmsText.indexOf('|')) != -1) {
			text = parmsText.substring(0, pos);
			String parmsTextRem = parmsText.substring(pos + 1);
			String mnemonicText = parmsTextRem;

			if ((pos = parmsTextRem.indexOf('|')) != -1) {
				mnemonicText = parmsTextRem.substring(0, pos);
				String accelText = parmsTextRem.substring(pos + 1);
				accel = KeyStroke.getKeyStroke(accelText);
			}

			if (mnemonicText.length() > 0)
				mnemonic = KeyStroke.getKeyStroke(mnemonicText).getKeyCode();
		}
		return new ComponentParms(text, mnemonic, accel);
	}
*/
	/**
	 * ComponentParms contains the text and keyboard shortcut info for a
	 * resource string. Most resource strings do not have keyboard shortcut
	 * info.
	 */
	/*
	public static class ComponentParms {

		public final int getKeyEvent() {
			return keyEvent;
		}

		public final KeyStroke getKeyStroke() {
			return keyStroke;
		}

		public final String getText() {
			return text;
		}

		public ComponentParms(String text, int keyEvent, KeyStroke keyStroke) {
			this.text = text;
			this.keyEvent = keyEvent;
			this.keyStroke = keyStroke;
		}

		private String text;

		private int keyEvent;

		private KeyStroke keyStroke;
	}
*/

}
