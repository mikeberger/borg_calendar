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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;


/**
 * GUI control to easy choose foreground or background color.
 * Indicates color to be stored by its own foreground or background.
 * 
 * @author bsv
 * 
 */
public class JButtonKnowsBgColor extends JButton {
	private static final long serialVersionUID = 1L;
	// colorProperty is ONE color, but can be indicated by fore or back color
	protected Color colorProperty;
	// bg=true means "choosed color is background color"
	// bg=false means "choosed color is foreground color"
	protected boolean bg;
	
	public JButtonKnowsBgColor( String p_text, Color p_color, boolean p_bg ){
		setText( p_text );
		setColorProperty( p_color );
		setBg( p_bg );
		setColorByProperty();
		addActionListener(new ModalListener());
	      
	}
	
	public void setColorByProperty(){
		if( isBg() ){
			setBackground( getColorProperty() );
		} else {
			setForeground( getColorProperty() );
		}
	}

	// for testing purposes only
	public static void main(String[] args) {
		JButtonKnowsBgColor jbkbc = new JButtonKnowsBgColor( "choose back", Color.RED, true );
		JButtonKnowsBgColor jbkbc1 = new JButtonKnowsBgColor( "choose fore", Color.BLUE, false );
		JFrame jf = new JFrame();
		jf.setLayout( new BorderLayout() );
		jf.getContentPane().add( jbkbc, BorderLayout.NORTH );
		jf.getContentPane().add( jbkbc1, BorderLayout.CENTER );
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setSize( 100, 200 );
		jf.setVisible(true);
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
	}
	/**
	 * @return Returns the bg.
	 */
	protected boolean isBg() {
		return bg;
	}
	/**
	 * @param bg The bg to set.
	 */
	protected void setBg(boolean bg) {
		this.bg = bg;
	}

	private class ModalListener implements ActionListener{
		public void actionPerformed(ActionEvent event){
			Color selected = JColorChooser.showDialog(
				null, 
				isBg()?"Set background":"Set foreground", 
				getColorProperty());
			setColorProperty(selected);
			setColorByProperty();
      }
   }

}
