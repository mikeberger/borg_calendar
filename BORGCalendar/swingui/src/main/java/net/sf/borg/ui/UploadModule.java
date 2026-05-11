// New class: BORGCalendar/swingui/src/main/java/net/sf/borg/ui/UploadModule.java - by copilot

package net.sf.borg.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.Model;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.db.jdbc.DbDirtyManager;
import net.sf.borg.model.sync.google.GDrive;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.util.IconHelper;

public class UploadModule implements Module, Prefs.Listener, Model.Listener {

	//static private final java.util.logging.Logger log = 
	//	java.util.logging.Logger.getLogger("net.sf.borg");
	

	private JButton uploadToolbarButton = null;
	
	public UploadModule() {
	}

	@Override
	public Component getComponent() {
		return null;
	}

	@Override
	public String getModuleName() {
		return "UPLOAD";
	}

	@Override
	public ViewType getViewType() {
		return null;
	}

	@Override
	public void initialize(MultiView parent) {
		Prefs.addListener(this);
		DbDirtyManager.getReference().addListener(new DbDirtyManager.DbDirtyListener() {
			@Override
			public void onDbDirty() {
				onDatabaseUpdate();
			}
			
			@Override
			public void onDbClean() {
				onDatabaseUploaded();
			}
		});
		// Add toolbar button for upload status
		uploadToolbarButton = MultiView.getMainView().addToolBarItem(
				IconHelper.getIcon("/resource/Up16.gif"), 
				"Upload", 
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						try {
							GDrive.getReference().vacuumAndUpload();
						} catch (Exception e) {
							Errmsg.getErrorHandler().errmsg(e);
						}
					}
				});

		String usetray = Prefs.getPref(PrefName.USESYSTRAY);
		if (usetray.equals("true")) {
			try {
				updateUploadButton();
				showUploadTrayIcon();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void print() {
		// do nothing
	}

	@Override
	public void prefsChanged() {
		try {
			updateUploadButton();
			showUploadTrayIcon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when database is updated via SQLite hook.
	 * Set the flag indicating uploads are pending.
	 */
	public void onDatabaseUpdate() {
		try {
			updateUploadButton();
			showUploadTrayIcon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when database is successfully uploaded to Google Drive.
	 * Clear the pending flag and hide the upload indicators.
	 */
	public void onDatabaseUploaded() {
		try {
			updateUploadButton();
			showUploadTrayIcon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateUploadButton() throws Exception {
		if (DbDirtyManager.getReference().isDirty() && GDrive.getReference().isUploading()) {
			//uploadToolbarButton.setText("Upload");
			uploadToolbarButton.setVisible(true);
		} else {
			uploadToolbarButton.setVisible(false);
		}
	}

	private void showUploadTrayIcon() {
		if (TrayIconProxy.hasTrayIcon()) {
			try {
				if (DbDirtyManager.getReference().isDirty() && GDrive.getReference().isUploading()) {
					// Show the upload tray icon
					TrayIconProxy.enableUploadTrayIcon();
				} else {
					// Hide the upload tray icon
					TrayIconProxy.disableUploadTrayIcon();
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	@Override
	public void update(ChangeEvent event) {
		// Any model change means database was updated
		onDatabaseUpdate();
	}
}