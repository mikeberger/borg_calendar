/*
 * Created on 17.12.2004
 *
 */
package net.sf.borg.common.ui;

import javax.swing.*;
//import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


/**
 * GUI control to easy choose foreground or background color.
 * Indicates color to be stored by its own foreground or background.
 * 
 * @author bsv
 * 
 */
public class JButtonKnowsBgColor extends JButton {
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
		};
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
