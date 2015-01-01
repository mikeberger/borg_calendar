package net.sf.borg.ui.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

public class MemoryPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JButton gcButton = new JButton();
	private JProgressBar memoryBar = new JProgressBar();

	public MemoryPanel() {

		memoryBar.setStringPainted(true);
		gcButton = new JButton(new ImageIcon(getClass().getResource(
				"/resource/Delete16.gif")));

		gcButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.gc();
			}
		});

		this.setLayout(new GridBagLayout());
		this.add(memoryBar,
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
		this.add(gcButton, GridBagConstraintsFactory.create(1, 0));

		int to = Prefs.getIntPref(PrefName.MEMBAR_TIMEOUT);
		if (to > 0) {
			new Timer(to, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateBar();
				}
			}).start();
		}
	}

	private void updateBar() {

		long totalMB = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		long freeMB = Runtime.getRuntime().freeMemory() / (1024 * 1024);

		memoryBar.setMinimum(0);
		memoryBar.setMaximum((int) totalMB);
		int used = (int) (totalMB - freeMB);
		memoryBar.setValue(used);

		memoryBar.setString(used + "/" + totalMB + " MB");

	}

}