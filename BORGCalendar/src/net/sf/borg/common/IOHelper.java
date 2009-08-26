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

package net.sf.borg.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * standard routines for file I/O with prompting
 */
public class IOHelper {
	
	

	/**
	 * Prompt the user to choose a file to open
	 * 
	 * @param startDirectory the start directory
	 * @param title the window title
	 * 
	 * @return the input stream
	 * 
	 * @throws Exception the exception
	 */
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

	/**
	 * prompt the user to pick a file for saving and save date to the file
	 * 
	 * @param startDirectory the start directory
	 * @param istr the stream to write out to the file
	 * @param defaultFilename the default filename
	 * 
	 * @throws Exception the exception
	 */
	public static void fileSave(
		String startDirectory,
		InputStream istr,
		String defaultFilename)
		throws Exception
	{
		JFileChooser chooser = new JFileChooser();
	            
		chooser.setCurrentDirectory( new File(startDirectory) );
		chooser.setDialogTitle(Resource.getResourceString("Save"));
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


	/**
	 * Creates an output stream to a URL
	 * 
	 * @param url the url
	 * 
	 * @return the output stream
	 * 
	 * @throws Exception the exception
	 */
	public static OutputStream createOutputStream(URL url) throws Exception {
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		return connection.getOutputStream();
	}

	/**
	 * create an output stream to a file, creating parent dirs as needed
	 * 
	 * @param file the file
	 * 
	 * @return the output stream
	 * 
	 * @throws Exception the exception
	 */
	public static OutputStream createOutputStream(String file) throws Exception {
		File fil = new File(file);
		fil.getParentFile().mkdirs();
		// create the containing directory if it doesn't
		// already exist.
		return new FileOutputStream(fil);

	}

	
	/**
	 * displays an overwrite confirm dialog if a file exists
	 * 
	 * @param fname the filename
	 * 
	 * @return true, if the user says it's ok to overwrite
	 */
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
