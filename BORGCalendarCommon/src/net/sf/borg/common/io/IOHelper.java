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



import java.io.BufferedReader;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.io.OutputStream;

import java.io.StringWriter;

import java.net.URL;

import java.net.URLConnection;



import net.sf.borg.common.app.AppHelper;

import net.sf.borg.common.util.Prefs;



/**

 * Abstracts the I/O subsystem. Also allows applets to perform I/O

 * using our in-memory scheme.

 * @author Mohan Embar

 */

public class IOHelper

{

	public static InputStream openStream(URL url) throws Exception

	{

		return url.openStream();

	}



	public static InputStream openStream(String file) throws Exception

	{

		if (!AppHelper.isApplication() || file.startsWith("mem:"))

			return new MemFileInputStream(file, files);

		else if (AppHelper.isApplication())

			return new FileInputStream(file);

		else

			throw new IllegalArgumentException(AppHelper.getType().toString());

	}



	public static OutputStream createOutputStream(URL url) throws Exception

	{

		URLConnection connection = url.openConnection();

		connection.setDoOutput(true);

		return connection.getOutputStream();

	}



	public static OutputStream createOutputStream(String file) throws Exception

	{

		if (!AppHelper.isApplication() || file.startsWith("mem:"))

			return new MemFileOutputStream(file, files);

		else if (AppHelper.getType() == AppHelper.APPLICATION)

		{

			return new FileOutputStream(file);

		}

		else

			throw new IllegalArgumentException(AppHelper.getType().toString());

	}

	

	public static String[] getMemFilesList()

	{

		return files.list();

	}



	public static String getMemFilesMemento()

	{

		// Store our preferences if possible.

		byte[] prefsData = Prefs.getMemento();

		if (prefsData != null)

			files.put(PREFS_FILE, prefsData);

		

		return files.toMemento();

	}

	

	public static boolean isMemFilesDirty()

	{

		return files.isDirty();

	}

	

	public static String loadMemoryFromURL(String urlst) throws Exception

	{

		if( urlst == null || urlst.length()==0 )

			return null;

		URL url = new URL(urlst);

		return loadMemoryFromStream(openStream(url));

	}



	public static String loadMemoryFromStream(InputStream istr)

		throws Exception

	{

		if (istr == null)

			return null;

	

		BufferedReader rdr = new BufferedReader(new InputStreamReader(istr));

		int ch;

		StringWriter wrtr = new StringWriter();

		while ((ch = rdr.read()) != -1)

		{

			wrtr.write(ch);

		}

		rdr.close();

		wrtr.close();

		String data = wrtr.toString();

		

		// Try to import this.

		setMemFilesMemento(data);

		return data;

	}



	public static void setMemFilesMemento(String strData) throws Exception

	{

		MemFiles newFiles = new MemFiles();

		newFiles.setMemento(strData);

		files = newFiles;

		

		// Update our preferences if possible.

		if (files.contains(PREFS_FILE))

			Prefs.setMemento(files.get(PREFS_FILE));

	}



	// private //

	private static final String PREFS_FILE = "mem:/prefs.dat";

	

	private static MemFiles files = new MemFiles();

	

	private IOHelper()

	{

	}

}

