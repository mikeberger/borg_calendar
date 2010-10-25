/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */

package net.sf.borg.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;


/**
 * A button for choosing colors. Shows an icon with the color.
 * This replaces the old color chooser button that could paint it's background different colors.
 * That worked inconsistently for various look and feels that also painted the backgrounds.
 */
public class ColorChooserButton extends JButton {
	
	static private class ColorIcon implements Icon {

		private Color color = Color.BLACK;
		private final int height = 10;
		private final int width = 30;

		/**
		 * Instantiates a new toggle button icon.
		 * 
		 * @param col
		 *            the color
		 */
		public ColorIcon(Color col) {
			color = col;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#getIconHeight()
		 */
		@Override
		public int getIconHeight() {
			return height;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#getIconWidth()
		 */
		@Override
		public int getIconWidth() {
			return width;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.Icon#paintIcon(java.awt.Component,
		 * java.awt.Graphics, int, int)
		 */
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.BLACK);
			g2.drawRect(x, y, width, height);
			g2.setColor(color);
			g2.fillRect(x, y, width, height);
		}
	}

	private static final long serialVersionUID = 1L;
	protected Color colorProperty;
	
	
	public ColorChooserButton( String p_text, Color p_color ){
		setText( p_text );
		setColorProperty( p_color );
		addActionListener(new ModalListener());
	      
	}
	
	/**
	 * @return Returns the color.
	 */
	public Color getColorProperty() {
		return colorProperty;
	}
	/**
	 * @param color The color to set.
	 */
	public void setColorProperty(Color color) {
		this.colorProperty = color;
		setIcon( new ColorIcon(color));
	}
	
	private class ModalListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event){
			Color selected = JColorChooser.showDialog(
				null, 
				"", 
				getColorProperty());
			setColorProperty(selected);
      }
   }

}
