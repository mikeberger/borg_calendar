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
package net.sf.borg.common;

import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.KeyStroke;

/**
 * Common logic for dealing with Resource Bundles. Resource strings are stored
 * in a standard resource bundle, but may have extra keyboard shortcut
 * information added which defines the keyboard shortcuts for manu items and
 * buttons.
 */
public class Resource {

	/** the borg version */
	private static String version_ = null;

	/**
	 * Get a resource string from the borg bundle. Translates escaped newlines
	 * to real newlines. String will contain any keyboard shortcut info
	 * 
	 * @param key
	 *            the resource key
	 * 
	 * @return the resource string or "??key??" if the string is not found
	 */
	private static String getRawResourceString(String key) {
		try {
			String res = ResourceBundle.getBundle("resource/borg_resource")
					.getString(key);

			if (res.indexOf("\\n") == -1)
				return (res);

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < res.length(); i++) {

				if (res.charAt(i) == '\\' && (i < res.length() - 1)
						&& res.charAt(i + 1) == 'n') {
					i++;
					sb.append('\n');
				} else {
					sb.append(res.charAt(i));
				}
			}

			return (sb.toString());
		} catch (MissingResourceException m) {
			return ("??" + key + "??");
		}
	}

	/**
	 * Get a resource string from the borg resource bundle. Translates escaped newlines
	 * to real newlines.
	 * 
	 * @param resourceKey
	 *            the resource key
	 * 
	 * @return the resource string
	 */
	public static String getResourceString(String resourceKey) {
		ComponentParms parms = parseParms(resourceKey);
		return parms.getText();
	}

	/**
	 * Gets the borg version, which is stored in its own properties file
	 * 
	 * @return the version
	 */
	public static String getVersion() {
		if (version_ == null) {
			try {
				// get the version and build info from a properties file in the
				// jar file
				InputStream is = Resource.class.getResource("/properties")
						.openStream();
				Properties props = new Properties();
				props.load(is);
				is.close();
				version_ = props.getProperty("borg.version");
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

		return (version_);
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
	public static ComponentParms parseParms(String resourceKey) {
		String parmsText = getRawResourceString(resourceKey);

		if (parmsText.startsWith("Goto"))
			parmsText = parmsText.substring(0);

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

	/**
	 * ComponentParms contains the text and keyboard shortcut info for a resource string.
	 * Most resource strings do not have keyboard shortcut info.
	 */
	public static class ComponentParms {

		/**
		 * Gets the key event.
		 * 
		 * @return the key event
		 */
		public final int getKeyEvent() {
			return keyEvent;
		}

		/**
		 * Gets the key stroke.
		 * 
		 * @return the key stroke
		 */
		public final KeyStroke getKeyStroke() {
			return keyStroke;
		}

		/**
		 * Gets the text.
		 * 
		 * @return the text
		 */
		public final String getText() {
			return text;
		}

		// "internal" //
		/**
		 * Instantiates a new component parms.
		 * 
		 * @param text
		 *            the text
		 * @param keyEvent
		 *            the key event
		 * @param keyStroke
		 *            the key stroke
		 */
		public ComponentParms(String text, int keyEvent, KeyStroke keyStroke) {
			this.text = text;
			this.keyEvent = keyEvent;
			this.keyStroke = keyStroke;
		}

		// private //
		/** The text. */
		private String text;

		/** The key event. */
		private int keyEvent;

		/** The key stroke. */
		private KeyStroke keyStroke;
	}

	// end nested class ComponentParms
	// //////////////////////////////////////////////////////////////

}
