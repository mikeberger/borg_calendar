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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.SendJavaMail;
import net.sf.borg.control.EmailReminder;

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
	 * @param backup_email
	 *            - send email containing the backup
	 * @throws Exception
	 */
	public static void exportToZip(String dir, boolean backup_email)
			throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String uniq = sdf.format(new Date());

		String backupFilename = dir + "/borg" + uniq + ".zip";
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
				backupFilename));
		Writer fw = new OutputStreamWriter(out, "UTF8");

		for (Model model : Model.getExistingModels()) {
			// links must be last
			if (model instanceof LinkModel) {
				continue;
			}

			if (model.getExportName() == null)
				continue;

			out.putNextEntry(new ZipEntry(model.getExportName() + ".xml"));
			model.export(fw);
			fw.flush();
			out.closeEntry();

		}

		// links must be last for import to work
		out.putNextEntry(new ZipEntry(LinkModel.getReference().getExportName()
				+ ".xml"));
		LinkModel.getReference().export(fw);
		fw.flush();
		out.closeEntry();

		out.close();

		if (backup_email) {

			if (Prefs.getBoolPref(PrefName.EMAILENABLED) == false)
				return;

			// get the SMTP host and address
			String host = Prefs.getPref(PrefName.EMAILSERVER);
			String addr = Prefs.getPref(PrefName.EMAILADDR);
			StringTokenizer stk = new StringTokenizer(addr, ",;");
			if (stk.hasMoreTokens())
				addr = stk.nextToken();
			SendJavaMail.sendMailWithAttachments(host,
					Resource.getResourceString("borg_backup"),
					Resource.getResourceString("borg_backup"), addr, addr,
					Prefs.getPref(PrefName.EMAILUSER), EmailReminder.gep(),
					new String[] { backupFilename });

		}
	}

	/**
	 * Import from a single xml input stream.
	 * 
	 * @param model
	 *            the Model to use for the import
	 * @param is
	 *            the input stream
	 * @throws Exception
	 */
	public static void importFromXmlFile(Model model, InputStream is)
			throws Exception {

		model.importXml(is);

		// show any newly imported categories
		CategoryModel.getReference().syncCategories();
		CategoryModel.getReference().showAll();
	}

	/**
	 * get the model that can import the given XML
	 * 
	 * @param in
	 *            - BufferedReader for the XML
	 * @return the Model
	 * @throws IOException
	 */
	public static Model getImportModelForXML(BufferedReader in)
			throws IOException {

		for (int i = 0; i < 10; i++) {
			String line = in.readLine();
			if (line != null)
			{
				for (Model model : Model.getExistingModels()) {
					
					if (model.getExportName() != null && line.contains(model.getExportName())) {
						in.close();
						return model;
					}
				}
			}
		}

		in.close();

		return null;

	}

	/**
	 * OMG - the JAXB unmarshaller keeps closing the entire Zip Input Stream
	 * after unmarshalling a single entry. Need to define this horrible class to
	 * prevent this. Actually it is pretty elegant compared to other options -
	 * like using temp files or reading entire entries into memory to clone
	 * streams
	 * 
	 */
	private static class UncloseableZipInputStream extends ZipInputStream {

		public UncloseableZipInputStream(InputStream in) {
			super(in);
		}

		public void close() {
			// do nothing - prevent the stream from being closed
		}

		public void myClose() throws IOException {
			super.close();
		}

	}

	/**
	 * map legacy import file names to new ones
	 */
	static private final Map<String, String> legacyFileMap = new HashMap<String, String>();
	static {
		legacyFileMap.put("borg.xml", "APPTS.xml");
		legacyFileMap.put("task.xml", "TASKS.xml");
		legacyFileMap.put("addr.xml", "ADDRESSES.xml");
		legacyFileMap.put("memo.xml", "MEMOS.xml");
		legacyFileMap.put("link.xml", "LINKS.xml");
		legacyFileMap.put("checklist.xml", "CHECKLISTS.xml");
	}

	/**
	 * Import an entire backup Zip file
	 * 
	 * @param zipFileName
	 *            the backup file name
	 * @throws Exception
	 */
	static public void importFromZip(String zipFileName) throws Exception {
		UncloseableZipInputStream in = new UncloseableZipInputStream(
				new FileInputStream(zipFileName));

		// force loading of LinkModel in case the UI hasn't had reason to load
		// it
		LinkModel.getReference();

		// assumes that links are last entry
		for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in
				.getNextEntry()) {

			String legacyFileName = legacyFileMap.get(entry.getName());

			boolean import_done = false;
			for (Model model : Model.getExistingModels()) {
				if (entry.getName().equals(model.getExportName() + ".xml")
						|| (legacyFileName != null && legacyFileName
								.equals(model.getExportName() + ".xml"))) {
					importFromXmlFile(model, in);
					import_done = true;
					break;
				}
			}

			if (import_done == false) {
				throw new Exception("Unknown file in ZIP - " + entry.getName()
						+ " ...skipping");
			}

		}

		// really close the zip file
		in.myClose();
	}
}
