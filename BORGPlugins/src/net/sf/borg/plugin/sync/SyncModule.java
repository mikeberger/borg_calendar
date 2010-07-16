package net.sf.borg.plugin.sync;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.borg.common.Errmsg;
import net.sf.borg.plugin.sync.google.GoogleSync;
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
		googleMI.setText("Google");
		googleMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GoogleSync.sync();
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		m.add(googleMI);

		JMenuItem dumpMI = new JMenuItem();
		dumpMI.setText("Dump");
		dumpMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					GoogleSync.dump();
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		m.add(dumpMI);

		parent.addPluginSubMenu(m);

		OptionsView.getReference().addPanel(new GoogleSyncOptionsPanel());

	}

	@Override
	public void print() {
		// do nothing
	}

}
