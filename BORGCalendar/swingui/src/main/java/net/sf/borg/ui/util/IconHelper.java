package net.sf.borg.ui.util;

import java.awt.Image;

import javax.swing.ImageIcon;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

public class IconHelper {
	
	public static ImageIcon getIcon(String path) {
		
		int iconSize = Prefs.getIntPref(PrefName.ICONSIZE);
		
		ImageIcon icon = new  ImageIcon(IconHelper.class.getResource(
				path));
		
		if( iconSize == icon.getIconHeight()) return icon;
		
		Image img = icon.getImage();
		Image resizeImage = img.getScaledInstance(iconSize, iconSize, java.awt.Image.SCALE_SMOOTH);
		return new ImageIcon(resizeImage);
		
	}

}
