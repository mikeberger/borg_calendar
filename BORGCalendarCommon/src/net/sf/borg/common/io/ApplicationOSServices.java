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



import java.awt.Toolkit;

import java.awt.datatransfer.DataFlavor;

import java.awt.datatransfer.StringSelection;

import java.awt.datatransfer.Transferable;

import java.io.File;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.InputStream;



import javax.swing.JFileChooser;



import net.sf.borg.common.util.Resource;



public class ApplicationOSServices implements IOSServices

{

public ApplicationOSServices()

{}

	

// IOSServices overrides

public final String getClipboardData()

{

	try

	{

		Transferable tr =

			Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);

		return (String) tr.getTransferData(DataFlavor.stringFlavor);

	}

	catch (Exception e)

	{

		return null;

	}

}



public final void setClipboardData(String data)

{

	try

	{

		StringSelection ss = new StringSelection(data);

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(

			ss, ss);

	}

	catch (Exception e)

	{

	}

}



public final InputStream fileOpen(String startDirectory, String title)

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



public final void fileSave(

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

}

