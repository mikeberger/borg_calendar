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
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A holder for all in-memory "files".
 * @author Mohan Embar
 */
class MemFiles implements Serializable
{
	MemFiles()
	{}
	
	final boolean isDirty()
	{
		return dirty;
	}
	
	final String[] list()
	{
		return (String[]) mapFiles.keySet().toArray(new String[0]);
	}

	final boolean contains(String file)
	{
		return mapFiles.containsKey(file);
	}

	final byte[] get(String file) throws FileNotFoundException
	{
		byte[] data = (byte[]) mapFiles.get(file);
		if (data == null)
			throw new FileNotFoundException(file);
		return data;
	}
	
	final void put(String file, byte[] data)
	{
		mapFiles.put(file, data);
		dirty = true;
	}
	
	final void remove(String file)
	{
		mapFiles.remove(file);
		dirty = true;
	}

	final void clear()
	{
		mapFiles.clear();
		dirty = true;
	}
	
	final String toMemento()
	{
		StringBuffer sbf = new StringBuffer();
		Iterator itr = mapFiles.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			String file = (String) entry.getKey();
			byte[] data = (byte[]) entry.getValue();
			sbf.append('>');
			sbf.append(file);
			sbf.append('\n');
			sbf.append(toHexString(data));
			sbf.append('\n');
		}
		return sbf.toString();
	}
	
	final void setMemento(String strFiles) throws Exception
	{
		StringBuffer sbfData = new StringBuffer();
		BufferedReader rdr = new BufferedReader(new StringReader(strFiles));
		String file = null;
		String line = null;
		mapFiles.clear();
		while ((line = rdr.readLine()) != null)
		{
			line = line.trim();
			if (line.startsWith(">"))
			{
				if (file != null)
				{
					byte[] data = toBytes(sbfData.toString());
					mapFiles.put(file,data);
				}
				sbfData.setLength(0);
				file = line.substring(1); 
			}
			else
				sbfData.append(line);
		}
		if (file != null)
		{
			byte[] data = toBytes(sbfData.toString());
			mapFiles.put(file,data);
		}
		rdr.close();
		dirty = false;
	}
	
	// private //
	private Map mapFiles = new TreeMap();
	private boolean dirty = false;

	private static byte[] toBytes(String str) {
		byte[] buf = new byte[str.length() >> 1];
		for (int i = 0; i < buf.length; ++i)
		{
			char ch = str.charAt(i);
			if (ch <= ' ') continue;
				// ignore whitespace
				
			int i2 = i << 1;
			buf[i] =
				(byte) Integer.parseInt(str.substring(i2, i2 + 2), 16);
		}
		return buf;
	}

	private static String toHexString(byte[] buf) {
		StringBuffer sbf = new StringBuffer(buf.length << 1);
		for (int i = 0; i < buf.length; ++i) {
			if (i>0 && (i % 32) == 0) sbf.append('\n');
			int by = buf[i];
			if (by < 0) by += 256;
			String hexByte = Integer.toHexString(by);
			if (by < 16)
				sbf.append('0');
			sbf.append(hexByte);
		}
		return sbf.toString();
	}
}
