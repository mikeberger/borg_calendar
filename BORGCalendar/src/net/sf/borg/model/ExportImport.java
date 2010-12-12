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

 Copyright 2003-2010 by Mike Berger
 */
package net.sf.borg.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * contains common import/export utilities
 * 
 */
public class ExportImport {

	/**
	 * Export all models to XML files inside a time-stamped zipfile.
	 * 
	 * @param dir
	 *            the directory to create the zip file in
	 * @throws Exception
	 */
	public static void exportToZip(String dir) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String uniq = sdf.format(new Date());
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(dir
				+ "/borg" + uniq + ".zip"));
		Writer fw = new OutputStreamWriter(out, "UTF8");

		out.putNextEntry(new ZipEntry("borg.xml"));
		AppointmentModel.getReference().export(fw);
		fw.flush();
		out.closeEntry();

		out.putNextEntry(new ZipEntry("task.xml"));
		TaskModel.getReference().export(fw);
		fw.flush();
		out.closeEntry();

		out.putNextEntry(new ZipEntry("addr.xml"));
		AddressModel.getReference().export(fw);
		fw.flush();
		out.closeEntry();

		out.putNextEntry(new ZipEntry("memo.xml"));
		MemoModel.getReference().export(fw);
		fw.flush();
		out.closeEntry();

		out.putNextEntry(new ZipEntry("checklist.xml"));
		CheckListModel.getReference().export(fw);
		fw.flush();
		out.closeEntry();

		// links must be last for import to work
		out.putNextEntry(new ZipEntry("link.xml"));
		LinkModel.getReference().export(fw);
		fw.flush();
		out.closeEntry();

		out.close();
	}

	/**
	 * Import from a single xml input stream.
	 * 
	 * @param type
	 *            the object type
	 * @param is
	 *            the input stream
	 * @throws Exception
	 */
	public static void importFromXmlFile(String type, InputStream is)
			throws Exception {
		if (type.equals("ADDRESSES")) {
			AddressModel.getReference().importXml(is);
		} else if (type.equals("LINKS")) {
			LinkModel.getReference().importXml(is);
		} else if (type.equals("MEMOS")) {
			MemoModel.getReference().importXml(is);
		} else if (type.equals("CHECKLISTS")) {
			CheckListModel.getReference().importXml(is);
		} else if (type.equals("TASKS")) {
			TaskModel.getReference().importXml(is);
		} else if (type.equals("APPTS")) {
			AppointmentModel.getReference().importXml(is);
		}

		// show any newly imported categories
		CategoryModel.getReference().syncCategories();
		CategoryModel.getReference().showAll();
	}

	/**
	 * get the type of object from an import file XML
	 * @param fileName the filename
	 * @return the object type
	 * @throws IOException
	 */
	public static String getImportObjectType(String fileName) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(
				new File(fileName)));

		String type = "";
		for (int i = 0; i < 10; i++) {
			String line = in.readLine();
			if (line == null)
				break;
			if (line.contains("<ADDRESSES>")) {
				type = "ADDRESSES";
				break;
			} else if (line.contains("<MEMOS>")) {
				type = "MEMOS";
				break;
			} else if (line.contains("<CHECKLISTS>")) {
				type = "CHECKLISTS";
				break;
			} else if (line.contains("<LINKS>")) {
				type = "LINKS";
				break;
			} else if (line.contains("<TASKS>")) {
				type = "TASKS";
				break;
			} else if (line.contains("<APPTS>")) {
				type = "APPTS";
				break;
			}
		}

		in.close();
		
		return type;

	}
	
	/**
	 * OMG - the JAXB unmarshaller keeps closing the entire Zip Input Stream after unmarshalling a single
	 * entry. Need to define this horrible class to prevent this. Actually it is pretty elegant
	 * compared to other options - like using temp files or reading entire entries into memory
	 * to clone streams
	 *
	 */
	private static class UncloseableZipInputStream extends ZipInputStream {

		public UncloseableZipInputStream(InputStream in) {
			super(in);
		}
		
		public void close()
		{
			// do nothing - prevent the stream from being closed
		}
		
		public void myClose() throws IOException
		{
			super.close();
		}
		
	}
	
	/**
	 * Import an entire backup Zip file
	 * @param zipFileName the backup file name
	 * @throws Exception
	 */
	static public void importFromZip(String zipFileName) throws Exception
	{
		UncloseableZipInputStream in = new UncloseableZipInputStream(new FileInputStream(zipFileName));
		
		// assumes that links are last entry
		for( ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry())
		{
			if( entry.getName().contains("borg"))
				importFromXmlFile("APPTS", in);
			else if( entry.getName().contains("task"))
				importFromXmlFile("TASKS", in);
			else if( entry.getName().contains("addr"))
				importFromXmlFile("ADDRESSES", in);
			else if( entry.getName().contains("memo"))
				importFromXmlFile("MEMOS", in);
			else if( entry.getName().contains("checklist"))
				importFromXmlFile("CHECKLISTS", in);
			else if( entry.getName().contains("link"))
				importFromXmlFile("LINKS", in);
			else
				throw new Exception("Unknown file in ZIP - " + entry.getName() + " ...skipping");

		}
		
		// really close the zip file
		in.myClose();
	}
}
