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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
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

		out.putNextEntry(new ZipEntry("link.xml"));
		LinkModel.getReference().export(fw);
		fw.flush();
		out.closeEntry();

		out.close();
	}

	/**
	 * Import from a single xml file.
	 * 
	 * @param type
	 *            the object type
	 * @param fileName
	 *            the file name
	 * @throws Exception
	 */
	public static void importFromXmlFile(String type, String fileName)
			throws Exception {
		if (type.equals("ADDRESSES")) {
			AddressModel.getReference().importXml(fileName);
		} else if (type.equals("LINKS")) {
			LinkModel.getReference().importXml(fileName);
		} else if (type.equals("MEMOS")) {
			MemoModel.getReference().importXml(fileName);
		} else if (type.equals("CHECKLISTS")) {
			CheckListModel.getReference().importXml(fileName);
		} else if (type.equals("TASKS")) {
			TaskModel.getReference().importXml(fileName);
		} else if (type.equals("APPTS")) {
			AppointmentModel.getReference().importXml(fileName);
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
}
