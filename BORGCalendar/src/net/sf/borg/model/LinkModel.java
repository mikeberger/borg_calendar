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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.XTree;
import net.sf.borg.model.db.EntityDB;
import net.sf.borg.model.db.LinkDB;
import net.sf.borg.model.db.jdbc.LinkJdbcDB;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Link;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;
import net.sf.borg.model.xml.LinkXMLAdapter;

/**
 * LinkModel manages the Link Entities, which are associations between BORG Entities and other BORG Entities,
 * files, and URLs.
 */
public class LinkModel extends Model {

    /**
     * LinkType holds the various link types. The string values are for legacy reasons
     */
    public enum LinkType {
        
    	FILELINK("file"),
    	ATTACHMENT("attachment"),
    	URL("url"),
    	APPOINTMENT("appointment"),
    	MEMO("memo"),
    	PROJECT("project"),
    	TASK("task"),
    	ADDRESS("address");
    	
        private final String value;
        private LinkType(String n) {
            value = n;
        };
        public String toString(){ return value; }

    }

    /** The singleton */
    static private LinkModel self_ = null;

    /** map of entity types to class names */
    private static HashMap<Class<? extends KeyedEntity<?>>,String> typemap = new HashMap<Class<? extends KeyedEntity<?>>,String>();

    static {
        // owner types
        typemap.put(Appointment.class, "appointment");
        typemap.put(Memo.class, "memo");
        typemap.put(Task.class, "task");
        typemap.put(Address.class, "address");
        typemap.put(Project.class, "project");
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
                    Errmsg.notice(Resource.getResourceString("att_folder_err") + path);
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
    	if( self_ == null )
			self_ = new LinkModel();
		return (self_);
    }

    /** The db */
    private EntityDB<Link> db_; // the database

    /**
     * Adds a link.
     * 
     * @param owner the owning Entity
     * @param path the path (url, filepath, or entity key)
     * @param linkType the link type
     * 
     * @throws Exception the exception
     */
    public void addLink(KeyedEntity<?> owner, String path, LinkType linkType) throws Exception {
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
        Object o = typemap.get(owner.getClass());
        if (o == null)
            throw new Exception("illegal link owner type");
        at.setOwnerType((String) o);
        at.setPath(path);
        at.setLinkType(linkType.toString());
        saveLink(at);
    }

    /**
     * Copy a file.
     * 
     * @param fromFile the from file
     * @param toFile the to file
     * 
     * @throws Exception the exception
     */
    private static void copyFile(String fromFile, String toFile) throws Exception {
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
                    ;
                }
            if (to != null)
                try {
                    to.close();
                } catch (IOException e) {
                    ;
                }
        }

    }

    /**
     * Delete a link
     * 
     * @param key the key
     * 
     * @throws Exception the exception
     */
    public void delete(int key) throws Exception {
        Link l = getLink(key);
        delete(l);
    }

    /**
     * Delete a link
     * 
     * @param l the Link
     * 
     * @throws Exception the exception
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
     * Delete all links for a particlar owning Entity given key and type
     * 
     * @param id the Entity id
     * @param type the Entity type
     * 
     * @throws Exception the exception
     */
    public void deleteLinks(int id, Class<? extends KeyedEntity<?>> type) throws Exception {
       
        Collection<Link> atts = getLinks(id, type);
        Iterator<Link> it = atts.iterator();
        while (it.hasNext()) {
            Link at = it.next();
            delete(at);
        }
    }

    /**
     * Delete links for an owning entity
     * 
     * @param owner the owning entity object
     * 
     * @throws Exception the exception
     */
    public void deleteLinks(KeyedEntity<?> owner) throws Exception {
       
        Collection<Link> atts = getLinks(owner);
        Iterator<Link> it = atts.iterator();
        while (it.hasNext()) {
            Link at = it.next();
            delete(at);
        }
    }

    /**
     * Export links to XML
     * 
     * @param fw the writer to write XML to
     * 
     * @throws Exception the exception
     */
    public void export(Writer fw) throws Exception {
        
        fw.write("<LINKS>\n");
        LinkXMLAdapter ta = new LinkXMLAdapter();
        for( Link addr : getLinks())
        {
            XTree xt = ta.toXml(addr);
            fw.write(xt.toString());
        }

        fw.write("</LINKS>");

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
     * @param key the key
     * 
     * @return the link
     * 
     * @throws Exception the exception
     */
    public Link getLink(int key) throws Exception {
        return db_.readObj(key);
    }

    /**
     * Gets all links.
     * 
     * @return all links
     * 
     * @throws Exception the exception
     */
    public Collection<Link> getLinks() throws Exception {
        return db_.readAll();
    }

    /**
     * Gets the links for an owning entity.
     * 
     * @param id the owner id
     * @param type the owner type
     * 
     * @return the links
     * 
     * @throws Exception the exception
     */
    public Collection<Link> getLinks(int id, Class<? extends KeyedEntity<?>> type) throws Exception {
        LinkDB adb = (LinkDB) db_;
        String o = typemap.get(type);
        if (o == null)
            return new ArrayList<Link>();
        return adb.getLinks(id, o);

    }

    /**
     * Gets the links for an owning entity
     * 
     * @param ownerbean the owning entity
     * 
     * @return the links
     * 
     * @throws Exception the exception
     */
    public Collection<Link> getLinks(KeyedEntity<?> ownerbean) throws Exception {
        LinkDB adb = (LinkDB) db_;
        if (ownerbean == null)
            return new ArrayList<Link>();
        Object o = typemap.get(ownerbean.getClass());
        if (o == null)
            return new ArrayList<Link>();
        return adb.getLinks(ownerbean.getKey(), (String) o);

    }

    /**
     * Import xml
     * 
     * @param xt the XML tree
     * 
     * @throws Exception the exception
     */
    public void importXml(XTree xt) throws Exception {
       
        LinkXMLAdapter aa = new LinkXMLAdapter();

        for (int i = 1;; i++) {
            XTree ch = xt.child(i);
            if (ch == null)
                break;

            if (!ch.name().equals("Link"))
                continue;
            Link addr = aa.fromXml(ch);
            addr.setKey(-1);
            saveLink(addr);
        }

        refresh();
    }

    /**
     * Move links from one object to another
     * 
     * @param oldOwner the old owner
     * @param newOwner the new owner
     * 
     * @throws Exception the exception
     */
    public void moveLinks(KeyedEntity<?> oldOwner, KeyedEntity<?> newOwner) throws Exception {
        Collection<Link> atts = getLinks(oldOwner);
        Iterator<Link> it = atts.iterator();
        while (it.hasNext()) {
            Link at = it.next();
            at.setOwnerKey(new Integer(newOwner.getKey()));
            Object o = typemap.get(newOwner.getClass());
            if (o == null)
                throw new Exception("illegal link owner type");
            at.setOwnerType((String) o);
            db_.updateObj(at);
            // System.out.println("updlink:" + at.getOwnerKey() + " " +
            // at.getOwnerType());
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
    private LinkModel()  {     
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
     * @param link the link
     * 
     * @throws Exception the exception
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
}
