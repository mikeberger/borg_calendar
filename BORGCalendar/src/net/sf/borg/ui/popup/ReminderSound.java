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

 Copyright 2010 by Mike Berger
 */
package net.sf.borg.ui.popup;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

public class ReminderSound {
	/**
	 * play the reminder sound indicated by soundOption
	 * 
	 * @param soundOption
	 *            - true (default sound), false (no-sound), system-beep, or a
	 *            filename
	 */
	static public void playReminderSound(String soundOption) {
		if (soundOption.equals("system-beep")) {
			Toolkit.getDefaultToolkit().beep();
		} else if (soundOption.equals("true")) {
			URL snd = ReminderPopupManager.class
					.getResource("/resource/blip.wav");
			AudioClip theSound;
			theSound = Applet.newAudioClip(snd);
			if (theSound != null) {
				theSound.play();
			}
		} else if (!soundOption.equals("false")) {
			try {
				File f = new File(soundOption);
				URL snd = f.toURI().toURL();
				AudioClip theSound;
				theSound = Applet.newAudioClip(snd);
				if (theSound != null) {
					theSound.play();
				}
			} catch (Exception e) {
				// no error
			}

		}
	}
}
