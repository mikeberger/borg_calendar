package net.sf.borg.plugin.sync;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.borg.common.Errmsg;
import net.sf.borg.plugin.sync.google.GoogleSync;
import net.sf.borg.plugin.sync.google.GoogleSync.SyncMode;
import net.sf.borg.plugin.sync.google.GoogleSyncOptionsPanel;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.options.OptionsView;

public class SyncModule implements Module {

	@Override
	public Component getComponent() {
		return null;
	}

	@Override
	public String getModuleName() {
		return "Sync";
	}

	@Override
	public ViewType getViewType() {
		return null;
	}

	@Override
	public void initialize(MultiView parent) {

		System.out.println("Loading Sync Module");

		JMenu m = new JMenu();
		m.setText(getModuleName());

		JMenuItem googleMI = new JMenuItem();
		googleMI.setText("Google Sync/Overwrite");
		googleMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GoogleSync.sync(SyncMode.SYNC_OVERWRITE);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		m.add(googleMI);
		
		googleMI = new JMenuItem();
		googleMI.setText("Google Sync");
		googleMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GoogleSync.sync(SyncMode.SYNC);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		m.add(googleMI);
		
		JMenuItem googleMI2 = new JMenuItem();
		googleMI2.setText("Google Overwrite");
		googleMI2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GoogleSync.sync(SyncMode.OVERWRITE);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		m.add(googleMI2);

		parent.addPluginSubMenu(m);

		OptionsView.getReference().addPanel(new GoogleSyncOptionsPanel());
		
		SyncLog.getReference();

	}

	@Override
	public void print() {
		// do nothing
	}

}
