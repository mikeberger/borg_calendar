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
 * banner.java
 *
 * Created on January 25, 2003, 8:25 PM
 */

package net.sf.borg.ui.util;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Splash Screen with status text
 */
public class SplashScreen extends JFrame {

	private static final long serialVersionUID = 1L;

	/** The image label to show the splash image */
	private JLabel imageLabel;

	/** The status text. */
	private JTextField statusText;

	/**
	 * constructor
	 */
	public SplashScreen() {
		
		setUndecorated(true);
		initComponents();
		pack();
		
		// size according to image and center on screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = imageLabel.getPreferredSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2),
				screenSize.height / 2 - (labelSize.height / 2));
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {

		imageLabel = new JLabel();
		statusText = new JTextField();

		getContentPane().setLayout(new java.awt.GridBagLayout());

		setLocationRelativeTo(this);

		// just put an image in imageLabel
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		imageLabel.setIcon(new ImageIcon(getClass().getResource(
				"/resource/borg.jpg")));
		imageLabel.setIconTextGap(0);
		imageLabel.setOpaque(true);

		statusText.setColumns(25);
		statusText.setMinimumSize(new java.awt.Dimension(40, 19));

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc1 = GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0);
		gbc1.insets = new Insets(0,0,0,0);
		topPanel.add(imageLabel,gbc1);
		
		GridBagConstraints gbc2 = GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 0.0, 0.0);
		gbc2.insets = new Insets(0,0,0,0);
		topPanel.add(statusText, gbc2);
		
		statusText.setEditable(false);

		this.setSize(364, 322);

		this.setContentPane(topPanel);

		pack();
	}

	/**
	 * Sets the text.
	 * 
	 * @param tx the new text
	 */
	public void setText(String tx) {
		statusText.setText(tx);
	}
} 
