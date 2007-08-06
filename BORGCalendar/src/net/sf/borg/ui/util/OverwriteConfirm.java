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
 * Copyright 2005 by Mike Berger
 */
package net.sf.borg.ui.util;

import java.io.File;

import javax.swing.JOptionPane;

public class OverwriteConfirm {

	// displays an overwrite confirm dialog if the file exists
	// returns true if it is ok to write the file
	static public boolean checkOverwrite(String fname) {
		
		File f = new File(fname);
		if( !f.exists()) return true;
		
		int ret = JOptionPane.showConfirmDialog(null,
				net.sf.borg.common.Resource.getResourceString("overwrite_warning")
						+ fname + " ?",
				"confirm_overwrite", JOptionPane.OK_CANCEL_OPTION);
		if (ret != JOptionPane.OK_OPTION)
			return false;
		
		return(true);
	}
}