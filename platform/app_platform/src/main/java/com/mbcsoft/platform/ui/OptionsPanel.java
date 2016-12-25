package com.mbcsoft.platform.ui;

import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.mbcsoft.platform.common.Errmsg;
import com.mbcsoft.platform.common.PrefName;
import com.mbcsoft.platform.common.Prefs;

/**
 * 
 * abstract base class for tabs in the options view
 * 
 */
public abstract class OptionsPanel extends JPanel {
	private static final long serialVersionUID = -4942616624428977307L;

	/**
	 * set a boolean preference from a checkbox
	 * 
	 * @param box
	 *            the checkbox
	 * @param pn
	 *            the preference name
	 */
	static public void setBooleanPref(JCheckBox box, PrefName pn) {
		if (box.isSelected()) {
			Prefs.putPref(pn, "true");
		} else {
			Prefs.putPref(pn, "false");
		}
	}

	/**
	 * set a check box from a boolean preference
	 * 
	 * @param box
	 *            the checkbox
	 * @param pn
	 *            the preference name
	 */
	static public void setCheckBox(JCheckBox box, PrefName pn) {
		String val = Prefs.getPref(pn);
		if (val.equals("true")) {
			box.setSelected(true);
		} else {
			box.setSelected(false);
		}
	}

	/**
		 * return the panel's display name
		 */
		public abstract String getPanelName();

	/**
		 * save options from the UI to the preference store
		 */
		public abstract void applyChanges();

	/**
		 * load options from the preference store into the UI
		 */
		public abstract void loadOptions();

	/**
	 * Prompt the user to choose a folder
	 * 
	 * @return the folder path or null
	 */
	static String chooseDir() {

		String path = null;
		while (true) {
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(new File("."));
			chooser.setDialogTitle("Please choose directory for database files");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return (null);
			}

			path = chooser.getSelectedFile().getAbsolutePath();
			File dir = new File(path);
			String err = null;
			if (!dir.exists()) {
				err = "Directory [" + path + "] does not exist";
			} else if (!dir.isDirectory()) {
				err = "Directory [" + path + "] is not a directory";
			}

			if (err == null) {
				break;
			}

			Errmsg.getErrorHandler().notice(err);
		}

		return (path);
	}
}
