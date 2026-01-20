package net.sf.borg.control;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

public class UpgradeCheck {

	static private final PrefName DBCHECKDONE = new PrefName("dbcheckdone", "false");
	static private final PrefName CALDAVCHECKDONE = new PrefName("caldavcheckdone", "false");
	static private final PrefName CALDAV_SERVER = new PrefName("caldav-server", "");

	public static void main(String args[]) {
		//checkUpgrade();
		showDialogWithDismissCheckbox(
				"<html>You are using an H2 database.<br>If your database was created by BORG 1.X, and you have not already done so,<br> you need to export the database using BORG 1.X and then import as an H2 or Sqlite"
						+ " database in BORG 2.0.<br>" + "</html>",
						new PrefName("xxx", "false"), false);
		System.exit(0);
	}

	// check if BORG is upgrading and perform any upgrade related actions
	static void checkUpgrade() {

		/*
		 * Check for Upgrade to 2.0
		 */

		// Database Transition Warning for HSQL and H2
		String dbtype = Prefs.getPref(PrefName.DBTYPE);

		// TBD

		// CALDAV warning
		String caldavServer = Prefs.getPref(CALDAV_SERVER);
		if (caldavServer != null && !caldavServer.isEmpty()) {

			showDialogWithDismissCheckbox(
					"<html>BORG 2.0 does not support CALDAV.<br>Please remain on BORG 1.X if you want to use CALDAV<br></html>",
					CALDAVCHECKDONE, false);
		}

		if (dbtype.equals("h2")) {

			// check for first time user. if db has never been set, never show the warning
			String h2dir = Prefs.getPref(PrefName.H2DIR);
			if (h2dir.equals("not-set")) {
				Prefs.putPref(DBCHECKDONE, "true");
			} else {

				showDialogWithDismissCheckbox(
						"<html>You are using an H2 database.<br>If your database was created by BORG 1.X, and you have not already converted to 2_0<br><br>RECOMMENDED:" 
					    + "<ol><li>export the database using BORG 1.X into a backup zip file</li><li>change BORG 1.X to use Sqlite as the database</li>"
						+ "<li>import your backup zip file into BORG 2_0</li><</ol>"
					    + "<br>If you truly want to keep H2 as your database:"
						+ "<ol><li>export the database using BORG 1.X into a backup zip file</li>"
						+ "<li>go to your database folder and rename or delete your current database file. you cannot import into an existing database</li>"
						+ "<li>import your backup zip file into BORG 2_0</li></ol><br>Do not proceed if you need to return to BORG 1_X"
						+ "</html>",
						DBCHECKDONE, false);
			}
		} else if (dbtype.contains("hsql")) {
			showDialogWithDismissCheckbox(
					"<html>You are using an HSQLDB database.<br>BORG 2.0 does not support this.<br>You should: <ol><li>export the database using BORG 1.X into a backup zip file</li><li>change BORG 1.X to use Sqlite as the database</li>"
					+ "<li>import your backup zip file into BORG 1_X</li><li>Run BORG 2_0</li></ol><br><br> DO NOT PROCEEED<br>" + "</html>",
					DBCHECKDONE, true);
		} else
			Prefs.putPref(DBCHECKDONE, "true");


	}

	private static void showDialogWithDismissCheckbox(String text, PrefName prefName, boolean mustExit) {

		// check if already seen and dismissed
		if (Prefs.getBoolPref(prefName))
			return;

		/*
		 * gemini wrote 99% of the following, including the comments
		 */

		// Create the main frame (or just use null for parent if not needed)
		// We'll use a hidden JFrame as the owner to properly center the dialog.
		JFrame ownerFrame = new JFrame();
		ownerFrame.setSize(0, 0); // Keep it invisible
		ownerFrame.setVisible(false);


		final JDialog dialog = new JDialog(ownerFrame, "Upgrade Warning", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		dialog.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		        System.exit(0); // Force the application to close
		    }
		});

		// 2. Create the main content panel
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(20, 20)); // Padding between components
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20)); // Overall padding

		// --- Text Area ---
		JLabel textLabel = new JLabel(text);
		// textLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

		// Use a wrapper panel for the text to mimic a JOptionPane-style layout
		// (optional)
		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		textPanel.add(textLabel);
		contentPanel.add(textPanel, BorderLayout.CENTER);

		// --- Checkbox Area ---
		final JCheckBox dontShowAgain = new JCheckBox("Do not show this again");
		dontShowAgain.setFocusPainted(false);
		if (!mustExit)
			contentPanel.add(dontShowAgain, BorderLayout.SOUTH);

		// --- Button Panel ---
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

		// 3. Create "Exit the program" button
		Icon exitIcon = UIManager.getIcon("OptionPane.informationIcon"); // Using a standard information icon
		if (exitIcon == null) {
			System.out.println("Could not find UIManager information icon, falling back to a dummy icon.");
			exitIcon = new ImageIcon(); // Fallback
		}
		JButton exitButton = new JButton("Exit the program", exitIcon);
		exitButton.addActionListener(e -> {
			// Get the checkbox state before exiting
			if (dontShowAgain.isSelected()) {
				System.out.println("User chose 'Do not show this again' before exiting.");
				Prefs.putPref(prefName, "true");
			}
			System.out.println("Exiting the program.");
			System.exit(0); // Terminate the application
		});

		// 4. Create "Proceed Anyway" button with the stop sign icon

		// A. Get a red stop sign icon (from system icons if available, or load one)
		// Using UIManager to get a standard icon for demonstration, or load from file.
		// For a more robust solution, you might load an image file:
		// Icon stopIcon = new ImageIcon("path/to/stop_sign.png");

		// For standard icons, we can try using a built-in one like error/warning icon:
		Icon stopIcon = UIManager.getIcon("OptionPane.errorIcon");
		if (stopIcon == null) {
			// Fallback if the UIManager doesn't provide a suitable icon (less common)
			System.out.println("Could not find UIManager icon, falling back to a dummy icon.");
			stopIcon = new ImageIcon(); // Empty icon
		}

		JButton proceedButton = new JButton("Proceed Anyway", stopIcon);
		// Optional: Set the button to be the default action
		dialog.getRootPane().setDefaultButton(proceedButton);

		proceedButton.addActionListener(e -> {
			// Get the checkbox state
			System.out.println("User chose 'Proceed Anyway'.");
			if (dontShowAgain.isSelected()) {
				System.out.println("User chose 'Do not show this again' before exiting.");
				Prefs.putPref(prefName, "true");
			}
			// In a real app, you would save the 'shouldHide' state here.
			dialog.dispose(); // Close the dialog
		});

		// 5. Add buttons to the button panel
		buttonPanel.add(exitButton);
		if (!mustExit)
			buttonPanel.add(proceedButton);

		// Add all main components to the dialog
		dialog.add(contentPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		// 6. Configure and show the dialog
		dialog.pack(); // Size the dialog based on its contents
		dialog.setLocationRelativeTo(ownerFrame); // Center the dialog on the screen (relative to the invisible owner)
		dialog.setVisible(true);

	}

}
