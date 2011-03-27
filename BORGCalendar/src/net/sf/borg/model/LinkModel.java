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
 
Copyright 2003 by Mike Berger
 */
package net.sf.borg.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.db.EntityDB;
import net.sf.borg.model.db.LinkDB;
import net.sf.borg.model.db.jdbc.LinkJdbcDB;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CheckList;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Link;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;

/**
 * LinkModel manages the Link Entities, which are associations between BORG
 * Entities and other BORG Entities, files, and URLs.
 */
public class LinkModel extends Model {

	/**
	 * class XmlContainer is solely for JAXB XML export/import to keep the same
	 * XML structure as before JAXB was used
	 */
	@XmlRootElement(name = "LINKS")
	private static class XmlContainer {
		public Collection<Link> Link;
	}

	/**
	 * LinkType holds the various link types. The string values are for legacy
	 * reasons
	 */
	public enum LinkType {

		FILELINK("file"), ATTACHMENT("attachment"), URL("url"), APPOINTMENT(
				"appointment"), MEMO("memo"), PROJECT("project"), TASK("task"), ADDRESS(
				"address"), CHECKLIST("net.sf.borg.ui.checklist");

		private final String value;

		private LinkType(String n) {
			value = n;
		}

		@Override
		public String toString() {
			return value;
		}

	}

	/** The singleton */
	static private LinkModel self_ = new LinkModel();

	/** map of entity types to class names */
	private static HashMap<Class<?>, LinkType> typemap = new HashMap<Class<?>, LinkType>();

	static {
		// owner types
		typemap.put(Appointment.class, LinkType.APPOINTMENT);
		typemap.put(Memo.class, LinkType.MEMO);
		typemap.put(Task.class, LinkType.TASK);
		typemap.put(Address.class, LinkType.ADDRESS);
		typemap.put(Project.class, LinkType.PROJECT);
		typemap.put(CheckList.class, LinkType.CHECKLIST);
	}

	/**
	 * get the folder where attachments are stored
	 * 
	 * @return the attachment folder path
	 */
	public static String attachmentFolder() {
		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		if (dbtype.equals("hsqldb")) {
			String path = Prefs.getPref(PrefName.HSQLDBDIR) + "/attachments";
			File f = new File(path);
			if (!f.exists()) {
				if (!f.mkdir()) {
					Errmsg.notice(Resource.getResourceString("att_folder_err")
							+ path);
					return null;
				}
			}
			return path;
		}
		if (dbtype.equals("h2")) {
			String path = Prefs.getPref(PrefName.H2DIR) + "/attachments";
			File f = new File(path);
			if (!f.exists()) {
				if (!f.mkdir()) {
					Errmsg.notice(Resource.getResourceString("att_folder_err")
							+ path);
					return null;
				}
			}
			return path;
		}
		return null;
	}

	/**
	 * Gets the singleton.
	 * 
	 * @return the singleton
	 */
	public static LinkModel getReference() {
		return (self_);
	}

	/** The db */
	private EntityDB<Link> db_; // the database

	/**
	 * Adds a link.
	 * 
	 * @param owner
	 *            the owning Entity
	 * @param pathIn
	 *            the path (url, filepath, or entity key)
	 * @param linkType
	 *            the link type
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void addLink(KeyedEntity<?> owner, String pathIn, LinkType linkType)
			throws Exception {
		String path = pathIn;
		if (owner == null) {
			Errmsg.notice(Resource.getResourceString("att_owner_null"));
			return;
		}

		if (linkType == LinkType.ATTACHMENT) {

			String atfolder = attachmentFolder();
			if (atfolder == null)
				throw new Exception("attachments not supported");

			// need to copy file and create new path
			File orig = new File(path);
			String fname = orig.getName();
			String newpath = atfolder + "/" + fname;

			int i = 1;
			while (true) {
				File newfile = new File(newpath);
				if (!newfile.exists())
					break;

				fname = Integer.toString(i) + orig.getName();
				newpath = atfolder + "/" + fname;
				i++;
			}

			copyFile(path, newpath);
			path = fname;

		}

		Link at = newLink();
		at.setKey(-1);
		at.setOwnerKey(new Integer(owner.getKey()));
		LinkType type = typemap.get(owner.getClass());
		if (type == null)
			throw new Exception("illegal link owner type");
		at.setOwnerType(type.toString());
		at.setPath(path);
		at.setLinkType(linkType.toString());
		saveLink(at);
	}

	/**
	 * Copy a file.
	 * 
	 * @param fromFile
	 *            the from file
	 * @param toFile
	 *            the to file
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static void copyFile(String fromFile, String toFile)
			throws Exception {
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
					// empty
				}
			if (to != null)
				try {
					to.close();
				} catch (IOException e) {
					// empty
				}
		}

	}

	/**
	 * Delete a link
	 * 
	 * @param key
	 *            the key
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void delete(int key) throws Exception {
		Link l = getLink(key);
		delete(l);
	}

	/**
	 * Delete a link
	 * 
	 * @param l
	 *            the Link
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void delete(Link l) throws Exception {
		if (l.getLinkType().equals(LinkType.ATTACHMENT.toString())) {
			// delete attached file
			File f = new File(attachmentFolder() + "/" + l.getPath());
			f.delete();
		}
		db_.delete(l.getKey());
		refresh();
	}

	/**
	 * Delete links for an owning entity
	 * 
	 * @param owner
	 *            the owning entity object
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void deleteLinksFromEntity(KeyedEntity<?> owner) throws Exception {

		Collection<Link> atts = getLinks(owner);
		Iterator<Link> it = atts.iterator();
		while (it.hasNext()) {
			Link at = it.next();
			delete(at);
		}
	}

	/**
	 * Delete links that target a given entity
	 * 
	 * @param target
	 *            the target entity object
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void deleteLinksToEntity(Object target) throws Exception {

		if( target == null)
			return;
		
		LinkType type = typemap.get(target.getClass());
		if (type == null)
			return;

		Collection<Link> links = getLinks();
		for (Link link : links) {
			if (link.getLinkType().equals(type.toString())) {
				if ((type == LinkType.MEMO && ((Memo) target).getMemoName()
						.equals(link.getPath()))
						|| (type == LinkType.CHECKLIST && ((CheckList) target)
								.getCheckListName().equals(link.getPath()))) {
					delete(link);
				} else if (target instanceof KeyedEntity<?>) {
					int key = ((KeyedEntity<?>) target).getKey();
					if (link.getPath().equals(Integer.toString(key))) {
						delete(link);
					}
				}
			}

		}
	}

	/**
	 * Export links to XML
	 * 
	 * @param fw
	 *            the writer to write XML to
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void export(Writer fw) throws Exception {

		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Marshaller m = jc.createMarshaller();
		XmlContainer container = new XmlContainer();
		container.Link = getLinks();
		m.marshal(container, fw);

	}

	/**
	 * Gets the dB.
	 * 
	 * @return the dB
	 */
	public EntityDB<Link> getDB() {
		return (db_);
	}

	/**
	 * Gets a link.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the link
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Link getLink(int key) throws Exception {
		return db_.readObj(key);
	}

	/**
	 * Gets all links.
	 * 
	 * @return all links
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Link> getLinks() throws Exception {
		return db_.readAll();
	}

	/**
	 * Gets the links for an owning entity
	 * 
	 * @param ownerbean
	 *            the owning entity
	 * 
	 * @return the links
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Link> getLinks(KeyedEntity<?> ownerbean) throws Exception {
		LinkDB adb = (LinkDB) db_;
		if (ownerbean == null)
			return new ArrayList<Link>();
		LinkType type = typemap.get(ownerbean.getClass());
		if (type == null)
			return new ArrayList<Link>();
		return adb.getLinks(ownerbean.getKey(), type.toString());

	}

	/**
	 * Import xml.
	 * 
	 * @param is
	 *            the input stream containing the XML
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void importXml(InputStream is) throws Exception {

		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Unmarshaller u = jc.createUnmarshaller();

		XmlContainer container = (XmlContainer) u
				.unmarshal(is);
		
		if( container.Link == null ) return;

		// use key from import file if importing into empty db
		int nextkey = db_.nextkey();
		boolean use_keys = (nextkey == 1) ? true : false;
		for (Link link : container.Link) {
			if (!use_keys)
				link.setKey(nextkey++);
			db_.addObj(link);
		}

		refresh();
	}

	/**
	 * Move links from one object to another
	 * 
	 * @param oldOwner
	 *            the old owner
	 * @param newOwner
	 *            the new owner
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void moveLinks(KeyedEntity<?> oldOwner, KeyedEntity<?> newOwner)
			throws Exception {
		Collection<Link> atts = getLinks(oldOwner);
		Iterator<Link> it = atts.iterator();
		while (it.hasNext()) {
			Link at = it.next();
			at.setOwnerKey(new Integer(newOwner.getKey()));
			LinkType type = typemap.get(newOwner.getClass());
			if (type == null)
				throw new Exception("illegal link owner type");
			at.setOwnerType(type.toString());
			db_.updateObj(at);
		}
	}

	/**
	 * return a new link object
	 * 
	 * @return the link
	 */
	public Link newLink() {
		return (db_.newObj());
	}

	/**
	 * Instantiates a new link model.
	 */
	private LinkModel() {
		db_ = new LinkJdbcDB();
	}

	/**
	 * Refresh listeners
	 */
	public void refresh() {
		refreshListeners();
	}

	/**
	 * Save a link.
	 * 
	 * @param link
	 *            the link
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void saveLink(Link link) throws Exception {
		int num = link.getKey();

		if (num == -1) {
			int newkey = db_.nextkey();
			link.setKey(newkey);
			db_.addObj(link);
		} else {
			db_.updateObj(link);

		}

		// inform views of data change
		refresh();
	}
	
	@Override
	public String getExportName() {
		return "LINKS";
	}

	@Override
	public String getInfo() throws Exception {
		return Resource.getResourceString("links") + ": "
		+ getLinks().size();
	}
}
