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
/*
 * popups.java
 *
 * Created on January 16, 2004, 3:08 PM
 */

package net.sf.borg.ui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.borg.model.Model.ChangeEvent;

/**
 * Manages the lifecycle of Reminder Popup windows.
 */
public class ReminderPopupManager extends ReminderManager {

	// map that maps each known reminder instance to the associated popup window
	// windows
	private HashMap<ReminderInstance, ReminderPopup> pops = new HashMap<ReminderInstance, ReminderPopup>();

	/**
	 * Gets the singleton.
	 * 
	 * @return the singleton
	 */
	public static ReminderManager getReference() {
		if (singleton == null) {
			singleton = new ReminderPopupManager();
		}
		return singleton;
	}

	/**
	 * constructor
	 */
	private ReminderPopupManager() {
		super();
	}

	/**
	 * stop the timer and remove all popups
	 */
	@Override
	public void remove() {
		super.remove();

		Set<Entry<ReminderInstance, ReminderPopup>> entrySet = pops.entrySet();
		for (Entry<ReminderInstance, ReminderPopup> popupMapEntry : entrySet) {

			ReminderPopup popup = popupMapEntry.getValue();
			if (popup == null || !popup.isDisplayable())
				continue;

			popup.dispose();
		}
	}

	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Model.Listener#refresh()
	 */
	@Override
	public void refresh() {

		// list of keys to appointments that no longer need popups
		ArrayList<ReminderInstance> deletedPopupKeys = new ArrayList<ReminderInstance>();

		// set of popup map entries
		Set<Entry<ReminderInstance, ReminderPopup>> entrySet = pops.entrySet();

		// loop through the existing popups
		for (Entry<ReminderInstance, ReminderPopup> mapEntry : entrySet) {

			ReminderInstance apptInstance = mapEntry.getKey();

			// get the popup window
			ReminderPopup popupWindow = mapEntry.getValue();

			// if frame is gone (killed already), then skip it
			if (popupWindow == null)
				continue;

			// skip if popup has been disposed - but still in map
			if (!popupWindow.isDisplayable()) {
				// remove from map
				// map should be last reference to the frame so garbage
				// collection should now be free to clean it up
				mapEntry.setValue(null);
				continue;
			}

			//if (apptInstance.isHidden())
			//	continue;

			if (apptInstance.reloadAndCheckForChanges()) {
				popupWindow.dispose();
				deletedPopupKeys.add(apptInstance);
			}

			if (!apptInstance.shouldBeShown()) {
				// dispose of popup and add to delete list
				popupWindow.dispose();
				deletedPopupKeys.add(apptInstance);
			}

		}

		// delete the popup map entries for popups that we disposed of
		for (ReminderInstance inst : deletedPopupKeys) {
			pops.remove(inst);
		}

	}

	/**
	 * show all popups in the list
	 */
	@Override
	public void showAll() {
		Set<Entry<ReminderInstance, ReminderPopup>> entrySet = pops.entrySet();
		for (Entry<ReminderInstance, ReminderPopup> popupMapEntry : entrySet) {

			ReminderPopup popup = popupMapEntry.getValue();

			// if frame is gone (killed already), then skip it
			if (popup == null)
				continue;

			// skip if popup is disposed - but still in map
			if (!popup.isDisplayable()) {
				// remove from map
				// map should be last reference to the frame so garbage
				// collection should now be free to clean it up
				popupMapEntry.setValue(null);
				continue;
			}

			// pop it up
			popup.setVisible(true);
			popup.toFront();

		}
	}

	/**
	 * Hide all popup windows
	 */
	@Override
	public void hideAll() {
		Set<Entry<ReminderInstance, ReminderPopup>> entrySet = pops.entrySet();
		for (Entry<ReminderInstance, ReminderPopup> popupMapEntry : entrySet) {

			ReminderPopup popup = popupMapEntry.getValue();

			// if frame is gone (killed already), then skip it
			if (popup == null)
				continue;

			// skip if popup is disposed - but still in map
			if (!popup.isDisplayable()) {
				// remove from map
				// map should be last reference to the frame so garbage
				// collection should now be free to clean it up
				popupMapEntry.setValue(null);
				continue;
			}

			// hide
			popup.setVisible(false);

		}
	}

	@Override
	public void addToUI(ReminderInstance instance) {

		// skip appt if it is already in the pops list
		// this means that it is already showing - or was shown
		// and killed already
		if (pops.containsKey(instance))
			return;

		// create a new popup and add it to the
		// popup map along with the appt key
		ReminderPopup popup = new ReminderPopup(instance);
		pops.put(instance, popup);

		popup.updateMessage();

	}

	@Override
	public void periodicUpdate() {
		// if any popups that are already displayed are due for showing - make a
		// sound and raise the popup

		// iterate through existing popups
		Set<Entry<ReminderInstance, ReminderPopup>> entrySet = pops.entrySet();
		for (Entry<ReminderInstance, ReminderPopup> popupMapEntry : entrySet) {

			ReminderInstance instance = popupMapEntry.getKey();
			ReminderPopup popup = popupMapEntry.getValue();

			// if popup is gone (killed already), then skip it
			if (popup == null)
				continue;

			// skip if popup is disposed - but still in map
			if (!popup.isDisplayable()) {
				// remove from map
				// map should be last reference to the frame so garbage
				// collection should now be free to clean it up
				popupMapEntry.setValue(null);
				continue;
			}
			if (instance.isHidden())
				continue;

			// untimed todo
			if (instance.isNote() && instance.isTodo()) {

				if (popup.getReminderInstance().isShown()
						&& !shouldShowUntimedTodosNow())
					continue;

			}

			// set the time to go message
			popup.updateMessage();

		}

	}

}
