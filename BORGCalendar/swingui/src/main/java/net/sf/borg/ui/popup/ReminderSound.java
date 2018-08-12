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

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

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
			URL snd = ReminderPopupManager.class.getResource("/resource/blip.wav");
			try {
				Clip sound = AudioSystem.getClip();
				sound.open(AudioSystem.getAudioInputStream(snd));
				sound.start();

			} catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
				// no error
			}
			
		} else if (!soundOption.equals("false")) {
			try {
				Clip sound = AudioSystem.getClip();
				sound.open(AudioSystem.getAudioInputStream(new File(soundOption)));
				sound.start();
			} catch (Exception e) {
				// no error
			}

		}
	}
}
