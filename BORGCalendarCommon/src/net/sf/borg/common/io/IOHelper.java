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
 
 Copyright 2004 by Mohan Embar - http://www.thisiscool.com/
 */

package net.sf.borg.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFileChooser;

import net.sf.borg.common.util.Resource;

/**
 * Abstracts the I/O subsystem. Also allows applets to perform I/O using our
 * in-memory scheme.
 * 
 * @author Mohan Embar
 */
public class IOHelper {
	
	

	public static InputStream fileOpen(String startDirectory, String title)
		throws Exception
	{
		JFileChooser chooser = new JFileChooser();
	            
		chooser.setCurrentDirectory( new File(startDirectory) );
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	            
		int returnVal = chooser.showOpenDialog(null);
		if(returnVal != JFileChooser.APPROVE_OPTION)
			return null;
	            
		String s = chooser.getSelectedFile().getAbsolutePath();
		return new FileInputStream(s);
	}

	public static void fileSave(
		String startDirectory,
		InputStream istr,
		String defaultFilename)
		throws Exception
	{
		JFileChooser chooser = new JFileChooser();
	            
		chooser.setCurrentDirectory( new File(startDirectory) );
		chooser.setDialogTitle(Resource.getPlainResourceString("Save"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	            
		int returnVal = chooser.showOpenDialog(null);
		if(returnVal != JFileChooser.APPROVE_OPTION)
			return;
	            
		String s = chooser.getSelectedFile().getAbsolutePath();
		FileOutputStream ostr = new FileOutputStream(s);
		
		int b;
		while ((b = istr.read()) != -1)
			ostr.write(b);
			
		istr.close();
		ostr.close();
	}
	
	public static InputStream openStream(URL url) throws Exception {
		return url.openStream();
	}

	public static InputStream openStream(String file) throws Exception {
		return new FileInputStream(file);
	}

	public static OutputStream createOutputStream(URL url) throws Exception {
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		return connection.getOutputStream();
	}

	public static OutputStream createOutputStream(String file) throws Exception {
		File fil = new File(file);
		fil.getParentFile().mkdirs();
		// create the containing directory if it doesn't
		// already exist.
		return new FileOutputStream(fil);

	}

	private IOHelper() {
	}
}
