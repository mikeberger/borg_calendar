/*
 * This file is part of BORG.
 * 
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 * 
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.model.db.file.mdb;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import net.sf.borg.common.util.Crypt;
import net.sf.borg.common.util.Errmsg;

import net.sf.borg.common.util.XTree;
import net.sf.borg.model.db.DBException;

// MDB - a random access DB (or perhaps 1 table in a DB)
// MDB stores text data of unlimited size using an integer key
// So, the schema for MDB is simply: int key; short userflags; - some boolean
// flags that are indexed text userdata
// Supports true random access. Records are keyed using a
// user supplied key - no duplicates allowed. When DB object
// is constructed, the file is quickly scanned and an index (table of contents)
// is created in memory. After that, insert, delete, update operations
// are done at a record level.
// Record size is fixed. Data larger than record size is stored
// under a chain of records. Freed records are left in place
// and marked as free for later reuse - file "holes" are not eliminated.
// Data is ASCII strings of any size. DB is not sensitive
// to the data content. DB is not sensitive to key values.
// Each record can contain 2 bytes of user boolean flags. These flags
// are read during the initial indexing scan - when no text is read
// These flags can give the user info about record without the need to
// perform the more expensive text read
// File structure
// |=========================================|
// | Superblock - contains info about the DB |
// | esp. size of the DB records |
// |-----------------------------------------|
// | Block |
// |-----------------------------------------|
// | Block |
// |-----------------------------------------|
// | Block |
// |-----------------------------------------|
// 
// Block structure
// |=========================================|
// | Header - contains the user KEY, optional|
// | link to the next block if the record |
// | is part of a chain, and flags |
// |-----------------------------------------|
// | Data |
// |=========================================|

public class MDB {

 
    /**
     * a file chooser filter that selects JDB files
     */
    public static class DBFilter extends FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            if (ext != null) {
                if (ext.equals("jdb")) {
                    return true;
                }

                return false;
            }
            return false;
        }

        // The description of this filter
        public String getDescription() {
            return "JDB Files";
        }

    }

    // Block Flags
    private static final int BH_SIZE = 4 + 4 + 4; // 3 integers

    private static final int F_DELETE = 0x01; // Block is unused (available)

    private static final int F_EXTEND = 0x02; // Block is part of a chain of
    // blocks - not the first

    private static final int F_CRYPT = 0x04; // Block is encrypted

    private static final int F_SYSTEM = 0x08; // system block

    private static final int F_MAX = 0x0F;

    // system keys - user cannot use keys this large - reserved for internal MDB
    // use
    //protected static final int LOG_KEY = 2000000001; // key to the record
    // that
    // holds the

    // log file name

    private static final int VERSION = 8; // 6 - add user flags, 7 = separate
    // SYS blocks

    // 8 - add update count to superblock for shared access

    // lock mode for constructor and for persistent lock flag
    public static final int UNLOCKED = 0; // no lock

    public static final int READ_ONLY = 1; // exclusive read lock, allow no
    // updates

    public static final int READ_WRITE = 2; // exclusive read/write

    public static final int READ_DIRTY = 3; // gets no lock, can read only, no
    // guarantee that

    public static final int ADMIN = 5; // unconditional access, used to remove

    // other locks

    // Header data contained in each block
    static private class BlockHeader {
        public int key_; // user key

        public int next_; // number of next "extension" block if

        // there is chaining
        public int flags_; // flags (deleted, extension, crypt, system)

        public int userflags_; // user defined flags

        static public BlockHeader read(RandomAccessFile fp) throws Exception,
                EOFException {
            BlockHeader bh = new BlockHeader();
            try {

                // read the 3 ints that make up the block header
                bh.key_ = fp.readInt();
                bh.next_ = fp.readInt();
                int allflags = fp.readInt();

                // the third int is half system flags and half user flags
                bh.flags_ = allflags & 0xFFFF;
                bh.userflags_ = (allflags & 0xFFFF0000) >>> 16;
            }
            catch (EOFException eo) {
                throw eo;
            }
            catch (Exception e) {
                throw e;
            }

            return (bh);
        }

        public void write(RandomAccessFile fp) throws Exception {
            try {

                // write out the 3 ints that make up the block header
                fp.writeInt(key_);
                fp.writeInt(next_);

                // the flags int must be combined from the system and user flags
                int allflags = (flags_ + ((userflags_ & 0xFFFF) << 16));
                fp.writeInt(allflags);

            }

            catch (Exception e2) {
                throw (new Exception("Cannot write superblock"));
            }
        }
    }

    // the superblock describes info about the entire DB file
    private static class SuperBlock {
        static final int NAME_SIZE = 20; // Size of DB name

        static final int FILLSIZE = 8; // filler space - not used yet

        // sizes of superblock is 3 ints (blocksize, version, lock) + name +
        // filler
        // Java int size is fixed to 4 bytes by definition!!
        static final int SB_SIZE = 4 + 4 + 4 + NAME_SIZE + 4 + FILLSIZE;

        public String dbname_;

        public int blocksize_;

        public int version_;

        public int locktype_;

        public int updateCount_;

        private SuperBlock(int blocksize, int version, int locktype) {
            dbname_ = "MDB Database"; // default name
            blocksize_ = blocksize;
            version_ = version;
            locktype_ = locktype;
            updateCount_ = 0;
        }

        // write superblock
        public void write(RandomAccessFile fp) throws DBException {
            if (dbname_.length() > NAME_SIZE)
                throw (new DBException("DB name too long"));

            // create byte array for name + padding to get to NAME_SIZE + filler
            byte b[] = dbname_.getBytes();
            byte pad[] = new byte[NAME_SIZE - b.length];
            byte fill[] = new byte[FILLSIZE];

            try {
                // super block always at file start
                fp.seek(0);

                // write it
                fp.writeInt(blocksize_);
                fp.writeInt(version_);
                fp.writeInt(locktype_);
                fp.write(b);
                fp.write(pad);
                fp.writeInt(updateCount_);
                fp.write(fill);
            }

            catch (Exception e2) {
                throw (new DBException("Cannot write superblock"));
            }
        }

        // read superblock
        static public SuperBlock read(RandomAccessFile fp) throws DBException {
            SuperBlock nsb = new SuperBlock(100, VERSION, READ_WRITE);
            try {
                // always at file start
                fp.seek(0);
                nsb.blocksize_ = fp.readInt();
                nsb.version_ = fp.readInt();
                nsb.locktype_ = fp.readInt();
                fp.seek(4 + 4 + 4 + NAME_SIZE);
                nsb.updateCount_ = fp.readInt();

                // don't need to read name for anything
            }
            catch (Exception e) {
                throw new DBException("Error reading superblock fields");
            }

            if (nsb.blocksize_ < BH_SIZE + 2)
                throw new DBException("Blocksize is too small");

            return (nsb);
        }

        public int readCounter(RandomAccessFile fp) throws DBException {
            try {
                fp.seek(4 + 4 + 4 + NAME_SIZE);
                updateCount_ = fp.readInt();
                return (updateCount_);
            }
            catch (Exception e) {
                throw new DBException("Error reading update counter");
            }

        }

        public void writeCounter(RandomAccessFile fp) throws DBException {
            try {
                // super block always at file start
                fp.seek(4 + 4 + 4 + NAME_SIZE);

                // write it
                fp.writeInt(updateCount_);
            }
            catch (Exception e2) {
                throw (new DBException("Cannot write update counter"));
            }
        }
    }

    //
    // fields
    //

    private String filename_; // the actual DB filename

    private SuperBlock sb; // the superblock read from the db

    private int locktype_; // locktype that we opened the db with

    private int maxkey_; // highest key in the db

    private boolean sharedDB_ = false;

    private FileLock rwlock_ = null;

    private HashMap index_;

    private HashMap sysindex_;

    // mapping of keys to user flags
    private HashMap flagmap_;

    private int cur_key_idx_; // currency pointer into cur_keys_ array to

    // hold current key across first/next calls
    private Object cur_keys_[]; // array of all keys to speed up first/next
    // traversal

    private boolean cur_crypt_; // flag to indicate if the current record is
    // encrypted

    // List of free blocks in the DB file
    private ArrayList freelist_;

    // the file itself
    private RandomAccessFile fp_;

    // last block in file - keeps track of where to add new blocks
    private int lastblock_;

    // key for weak-encryption for private records
    static private String ckey_ = "543216789192837465";

    // the encryption object - just does weak encryption - but prevents casual
    // string scan on db file for encrypted records.
    private Crypt mc_;

    //
    // end fields
    //

    // add a mapping of a key to a block into either the user or system
    // maps
    private void add_kpair(int key, int bnum, boolean system) {

        if (system)
            sysindex_.put(new Integer(key), new Integer(bnum));
        else
            index_.put(new Integer(key), new Integer(bnum));
    }

    // look up the block number corresponding to a key
    // in the system or user maps
    private int find_block(int key, boolean system) {
        Integer bnum;
        if (system) {
            bnum = (Integer) sysindex_.get(new Integer(key));
        }
        else {
            bnum = (Integer) index_.get(new Integer(key));
        }

        if (bnum != null)
            return (bnum.intValue());

        return (-1);
    }

    // find a free block in the DB or return the last block + 1 if the
    // DB needs to grow
    private int get_free_block() {
        Integer bnum;
        int i = freelist_.size();
        if (i > 0) {
            bnum = (Integer) freelist_.remove(i - 1);
            return (bnum.intValue());
        }

        lastblock_++;
        return (lastblock_);
    }

    // move a block from one of the maps to the freelist
    private int free_entry(int key, boolean system) throws DBException {

        // reset currency pointer when changing the DB
        cur_keys_ = null;
        Integer k = new Integer(key);
        Integer bnum;
        if (system)
            bnum = (Integer) sysindex_.remove(k);
        else
            bnum = (Integer) index_.remove(k);

        if (bnum != null) {
            freelist_.add(bnum);
            return (bnum.intValue());
        }

        throw (new DBException("free_entry - block not found",
                DBException.RET_NOT_FOUND));
    }

    // add a new key to the DB and associate a block with it
    private int add_new_entry(int key, boolean system) throws DBException {

        // reset currency pointer when changing the DB
        cur_keys_ = null;

        int bnum = find_block(key, system);
        if (bnum != -1) {
            throw (new DBException(
                    "add_new_entry - duplicate key already exists",
                    DBException.RET_DUPLICATE));
        }

        int fb = get_free_block();
        add_kpair(key, fb, system);
        return (fb);

    }

    // create a new MDB database file
    static public void create(String dbname, String filename, int blocksize)
            throws Exception {
        RandomAccessFile fp;

        // blocksize is the size of a block - header + text space
        // make sure the user allocates 2 bytes text space for a block
        // this is way too small anyway - should be around 80-100 text bytes
        // per block - depends on the average text size for a DB
        if (blocksize < BH_SIZE + 2)
            throw (new Exception("Blocksize is too small"));

        // create the file
        File f = new File(filename);
        if (f.exists())
            throw (new Exception("File Already Exists"));
        fp = new RandomAccessFile(filename, "rw");

        // write out a superblock
        SuperBlock sb = new SuperBlock(blocksize, VERSION, UNLOCKED);
        sb.dbname_ = dbname;
        sb.write(fp);

    }

    // remove lock files when destroyed
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    // try to get a shared lock to lock for multi-user usage
    private void getSharedLock() throws DBException {
        try {
            FileChannel fc = fp_.getChannel();

            FileLock lock;
            try {
                lock = fc.tryLock(0, 1, true);
            }
            catch (IOException e) {
                throw new DBException("Cannot lock DB " + filename_,
                        DBException.RET_CANT_LOCK);
            }
            if (lock == null) {
                throw new DBException("Cannot lock DB " + filename_,
                        DBException.RET_CANT_LOCK);
            }
        }
        catch (NoSuchMethodError nsm) {
            // running 1.3 - from Jsync conduit
            return;
        }

    }

    // try to get an exclusive lock for single user usage
    private void getExclusiveLock() throws DBException {
        try {
            FileChannel fc = fp_.getChannel();
            FileLock lock;
            try {
                lock = fc.tryLock(0, 1, false);
            }
            catch (IOException e) {
                throw new DBException("Cannot lock DB " + filename_,
                        DBException.RET_CANT_LOCK);
            }
            if (lock == null) {
                throw new DBException("Cannot lock DB " + filename_,
                        DBException.RET_CANT_LOCK);
            }
        }
        catch (NoSuchMethodError nsm) {
            // running 1.3 - from Jsync conduit
            return;
        }

    }

    // wait for a shared read lock
    public void getReadLock() throws DBException {
        if (!sharedDB_)
            return;
        FileChannel fc = fp_.getChannel();
        try {
            if (rwlock_ != null)
                return;

            rwlock_ = fc.lock(1, 1, true);
        }
        catch (IOException e) {
            throw new DBException("Cannot lock DB " + filename_,
                    DBException.RET_CANT_LOCK);
        }
        if (rwlock_ == null) {
            throw new DBException("Cannot lock DB " + filename_,
                    DBException.RET_CANT_LOCK);
        }

    }

    // wait for an exclusive write lock
    public void getWriteLock() throws DBException {
        if (!sharedDB_)
            return;

        FileChannel fc = fp_.getChannel();
        try {
            // check if we already have this lock
            if (rwlock_ != null && !rwlock_.isShared())
                return;

            // release any read lock to upgrade to write
            if (rwlock_ != null)
                rwlock_.release();
            rwlock_ = fc.lock(1, 1, false);
        }
        catch (IOException e) {
            throw new DBException("Cannot lock DB " + filename_,
                    DBException.RET_CANT_LOCK);
        }

        if (rwlock_ == null) {
            throw new DBException("Cannot lock DB " + filename_,
                    DBException.RET_CANT_LOCK);
        }
    }

    // release a read or write lock
    public void releaseLock() throws DBException {
        if (!sharedDB_)
            return;
        if (rwlock_ != null) {
            try {
                rwlock_.release();
            }
            catch (IOException e) {
                throw new DBException("Cannot lock DB " + filename_,
                        DBException.RET_CANT_LOCK);
            }
            catch (NoSuchMethodError nsm) {
                // running 1.3 - from Jsync conduit
                return;
            }
        }
        rwlock_ = null;
    }

    public boolean haveLock() {
        if (rwlock_ != null)
            return (true);

        return (false);
    }

    // flush all cached data and recreate
    // we must be locked before entering this method
    public void sync() throws DBException {

        // if we have exclusive access - don't bother doing anything
        if (!sharedDB_)
            return;

        boolean gotlock = false;
        if (!haveLock()) {
            getReadLock();
            gotlock = true;
        }

        int count = sb.updateCount_;
        int newcount = sb.readCounter(fp_);
        if (count == newcount) {
            // if the last count for this object matches the count read
            // from the file - nothing is needed, db is in sync
            if (gotlock)
                releaseLock();
            return;
        }

        // flush all cached data and re-sync
        index_.clear();
        sysindex_.clear();
        flagmap_.clear();
        cur_key_idx_ = -1;
        cur_keys_ = null;
        freelist_.clear();
        lastblock_ = -1;
        build_index();
        if (gotlock)
            releaseLock();
    }

    /**
     * constructor - opens DB of given name with given lock type
     * 
     * @param file
     *            filename of DB
     * @param locktype
     *            locking mode
     * @throws DBException
     *             errors
     */
    public MDB(String file, int locktype, boolean shared) throws DBException {
        filename_ = file;
        lastblock_ = -1;
        sharedDB_ = shared;

        // allocate an encrpytion object
        try {
            mc_ = new Crypt(ckey_);
        }
        catch (Exception e) {
            throw new DBException("failed to initialize encryption object");
        }

        // init some stuff
        locktype_ = locktype;
        maxkey_ = 0;

        index_ = new HashMap();
        sysindex_ = new HashMap(10);
        flagmap_ = new HashMap();
        freelist_ = new ArrayList();
        cur_keys_ = null;
        cur_key_idx_ = -1;

        // open the DB file
        File f = new File(filename_);
        if (!f.exists())
            throw new DBException("DB does not exist");

        // open mode based on lock type
        try {
            if (locktype == READ_ONLY || locktype == READ_DIRTY) {
                fp_ = new RandomAccessFile(filename_, "r");
            }
            else if (locktype == READ_WRITE || locktype == ADMIN) {
                fp_ = new RandomAccessFile(filename_, "rw");
            }
            else {
                throw new DBException("Invalid Locking Mode");
            }
        }
        catch (Exception e) {
            throw new DBException(e.toString());
        }

        // lock the database
        try {
            if (sharedDB_) {
                getSharedLock();
                getReadLock();
            }
            else
                getExclusiveLock();
        }
        catch (Exception e) {
            throw new DBException(e.toString());
        }

        // read the superblock
        sb = SuperBlock.read(fp_);

        // transition to version 8
        if (sb.version_ < 8) {
            sb.updateCount_ = 0;
            sb.version_ = VERSION;
            sb.write(fp_);
        }

        if (VERSION != sb.version_)
            throw new DBException("DB version != software version: "
                    + sb.version_ + "!=" + VERSION);

        // check persistent SB lock - not really used
        // the persistent lock has nothing to do with the temporary lock files
        // it was used to lock a DB so that it could not be opened without
        // a special close process. it was meant to indicate that a DB was
        // probably
        // a secondary copy and perhaps should not be used unless the primary
        // was lost
        // didn't want to accidentally start making real changes to a throw-away
        // backup copy
        // obsolete
        if (((sb.locktype_ == READ_WRITE) && ((locktype == READ_WRITE) || (locktype == READ_ONLY)))
                || ((sb.locktype_ == READ_ONLY) && (locktype == READ_WRITE))) {
            throw new DBException(
                    "Cannot lock DB. Superblock indicates a lock",
                    DBException.RET_CANT_LOCK);
        }

        // scan the whole DB file jumping from block-header to block-header to
        // build
        // a map of keys vs. blocks. does not read throwse text fields
        if (build_index() != DBException.RET_SUCCESS)
            throw new DBException("Could not build index from file");

        releaseLock(); // release read lock

    }

    public void close() {
        try {
            fp_.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }

    }

    // duh
    public int nextkey() {
        try {
            sync();
        }
        catch (Exception e) {
            Errmsg.errmsg(e);
        }
        return (++maxkey_);
    }

    /**
     * Add a new DB row
     * 
     * @param key
     *            integer key ( > 0 and < MAX_KEY)
     * @param st
     *            data to store
     * @throws DBException
     *             errors
     */

    // add a user row to the db - with optional encryption
    public void add(int key, int flags, String st, boolean crypt)
            throws DBException {
        add(key, flags, st, crypt, false);
    }

    // add a system row to the DB
    protected void addSys(int key, String st) throws DBException {
        add(key, 0, st, false, true);
    }

    // add an unencrypted user row
    public void add(int key, int flags, String st) throws DBException {
        add(key, flags, st, false, false);
    }

    // main add function
    // add will store a record with a key, flags, and text string
    // it will break up the string as needed and create extended blocks if the
    // string is too large to fit in a single block
    private void add(int key, int flags, String st, boolean crypt,
            boolean system) throws DBException {
        if (locktype_ != READ_WRITE && locktype_ != ADMIN)
            throw new DBException("add: Cannot lock DB for write",
                    DBException.RET_READ_ONLY);

        getWriteLock();
        sync();

        int bnum, main_bnum;

        // add the first block - get free block number
        main_bnum = add_new_entry(key, system);

        // figure out how much text can fit per block
        int text_per_block = sb.blocksize_ - BH_SIZE - 1;

        // allocate a new header
        BlockHeader header = new BlockHeader();

        // set the header flags
        header.flags_ = 0;
        header.userflags_ = flags;

        // encrypt the string if needed
        if (crypt) {
            st = mc_.encrypt(st);
        }

        byte[] utf;

        // UTF-8 encoding is used in the DB
        try {
            utf = st.getBytes("UTF-8");
        }
        catch (Exception e) {
            throw new DBException("UTF-8 not supported");
        }

        int ulen = utf.length;

        // build blocks as needed - IN REVERSE ORDER so that each can be stored
        // with the proper next value. So blocks are stored with the end of the
        // string
        // stored first (timewise). Each new block will point to the one stored
        // before it. The final linked chain will start at the beginning of the
        // string and end with block holding the end of the string
        int last_block = -1;
        for (int extnum = (ulen - 1) / text_per_block; extnum >= 0; extnum--) {

            // fill in key and next in header
            header.key_ = key;
            header.next_ = last_block;

            // Write an EXTENSION Block
            if (extnum > 0) {
                // get a free block - can't use main_bnum - thats for the first
                // block in the chain that holds the start of the text string
                bnum = get_free_block();
                header.flags_ = F_EXTEND;
            }
            // Write the first block using main_bnum
            else {
                header.flags_ = 0;

                // the SYSTEM and CRYPT flags are only set in the header of
                // the first block and aren't needed in any extension blocks
                if (crypt) {
                    header.flags_ |= F_CRYPT;
                }
                else if (system) {
                    header.flags_ |= F_SYSTEM;
                }

                bnum = main_bnum;
            }

            try {
                // seek to the beginning of the block in the file
                // which is sizeof(bnum blocks) after the superblock ends
                fp_.seek(SuperBlock.SB_SIZE + (sb.blocksize_ * bnum));
            }
            catch (Exception e) {
                throw new DBException("add: seek failed", DBException.RET_FATAL);
            }

            // write the record
            try {

                // write the header
                header.write(fp_);

                // figure out which portion of the string goes in this
                // block and write it
                int endindex = ((extnum + 1) * text_per_block);

                if (endindex > ulen) {
                    endindex = ulen;
                }

                //String subs = st.substring(extnum * text_per_block, endindex
                // );
                //fp_.writeBytes(subs);
                int sidx = extnum * text_per_block;
                int sublen = endindex - sidx;
                //System.out.println( utf + " " + sidx + " " + sublen );
                fp_.write(utf, sidx, sublen);

                // if the string does not fill up all of the space in the block
                // -
                // write '\0' bytes to fill the block
                for (int i = 0; i <= (text_per_block - sublen); i++) {
                    fp_.writeByte(0);
                }

            }
            catch (Exception e) {
                throw new DBException("add: write failed",
                        DBException.RET_FATAL);
            }

            // set last_block in case we loop around and have to store
            // another that points to this one
            last_block = bnum;

            // if we have stored a user key larger that the max we already
            // have, then update the max.
            if (key > maxkey_ && !system)
                maxkey_ = key;

        }

        // write a log record and update the map of user flags with the user's
        // flags
        if (!system) {
            flagmap_.put(new Integer(key), new Integer(flags));
        }

        if (sharedDB_) {
            sb.updateCount_++;
            sb.writeCounter(fp_);
        }
        releaseLock();
    }

    /** delete a row */
    public void delete(int key) throws DBException {
        delete(key, false);
    }

    // delete a system row
    protected void deleteSys(int key) throws DBException {
        delete(key, true);
    }

    // main delete function
    private void delete(int key, boolean system) throws DBException {

        int bnum;

        // check if DB is open for R/W
        if (locktype_ != READ_WRITE && locktype_ != ADMIN)
            throw new DBException("delete: cannot lock DB",
                    DBException.RET_READ_ONLY);

        getWriteLock();
        sync();

        // remove the user flags data from the map
        if (!system)
            flagmap_.remove(new Integer(key));

        try {
            // free the entry from the map of keys/block nums
            bnum = free_entry(key, system);
        }
        catch (DBException e) {
            releaseLock();
            if (e.getRetCode() == DBException.RET_NOT_FOUND)
                throw new DBException("delete: record not found",
                        DBException.RET_NOT_FOUND);

            throw e;
        }

        // build a new block header to mark the record as deleted
        BlockHeader inheader;
        BlockHeader delheader = new BlockHeader();
        delheader.key_ = -1;
        delheader.next_ = -1;
        delheader.flags_ = F_DELETE;

        // write deleted block headers to all records in the chain
        while (bnum >= 0) {

            try {
                // seek to block
                fp_.seek(SuperBlock.SB_SIZE + (sb.blocksize_ * bnum));
            }
            catch (Exception e) {
                throw new DBException("delete: seek failed",
                        DBException.RET_FATAL);
            }

            try {
                // read existing header - to get next ptr
                inheader = BlockHeader.read(fp_);
            }
            catch (Exception e) {
                throw new DBException("delete: cannot read header",
                        DBException.RET_FATAL);
            }

            try {
                // seek back to beginning of header
                fp_.seek(SuperBlock.SB_SIZE + (sb.blocksize_ * bnum));
            }
            catch (Exception e) {
                throw new DBException("delete: seek2 failed",
                        DBException.RET_FATAL);
            }

            // overwrite the header with a deleted header
            try {
                delheader.write(fp_);
            }
            catch (Exception e) {
                throw new DBException("delete: write failed",
                        DBException.RET_FATAL);
            }

            // add the block to the free list if it is an extended block
            // the main block is added to the free list in free_entry()
            if ((inheader.flags_ & F_EXTEND) != 0) {
                freelist_.add(new Integer(bnum));
            }

            // go to next block in chain
            bnum = inheader.next_;

            // sanity check - check if next block in chain has same key as
            // one before. if not, it may be different data so error, DB is
            // corrupted
            if (key != inheader.key_) {
                throw new DBException("delete: different key found",
                        DBException.RET_FATAL);
            }

        }

        if (sharedDB_) {
            sb.updateCount_++;
            sb.writeCounter(fp_);
        }
        releaseLock();
    }

    public Vector keys() {
        Vector v = new Vector(index_.keySet());
        return (v);
    }

    protected Vector syskeys() {
        Vector v = new Vector(sysindex_.keySet());
        return (v);
    }

    /**
     * get the first key in the DB. establishes a currency for use with next.
     * this wil be reset by any db modification. Only should do read in a
     * first/next loop
     * 
     * @throws DBException
     *             not found - no records in DB
     * @return key of first record
     */
    public int first() throws DBException {

        if (index_.size() == 0) {
            throw new DBException("first record not found",
                    DBException.RET_NOT_FOUND);
        }

        // get array of all keys from the main index map
        if (cur_keys_ == null) {
            cur_keys_ = index_.keySet().toArray();
        }

        // return the first key and set cur_key_idx to point to it
        cur_key_idx_ = 0;
        Integer key = (Integer) cur_keys_[0];
        return (key.intValue());
    }

    /**
     * returns key of next DB record. see first
     * 
     * @throws DBException
     *             not found - end of DB reached
     * @return key of next record
     */
    public int next() throws DBException {

        // error if first was never called
        if (cur_keys_ == null)
            throw new DBException("next called with no currency");

        // get next key in the array of keys
        cur_key_idx_++;
        if (cur_key_idx_ >= cur_keys_.length) {
            throw new DBException("next record not found",
                    DBException.RET_NOT_FOUND);
        }
        Integer key = (Integer) cur_keys_[cur_key_idx_];
        return (key.intValue());
    }

    // get the flags for the current record where current is the last record
    // accessed using first/next
    // in this way, the flags can be retrieved without accessing the DB text
    public int getFlags() throws DBException {

        if (cur_keys_ == null)
            throw new DBException("getFlags() called with no currency");
        Integer key = (Integer) cur_keys_[cur_key_idx_];
        Integer flags = (Integer) flagmap_.get(key);
        if (flags == null)
            throw new DBException("key not found in flagmap",
                    DBException.RET_NOT_FOUND);
        return (flags.intValue());
    }

    // look up the flags for a certain record by key
    public int getFlags(int key) throws DBException {

        Integer flags = (Integer) flagmap_.get(new Integer(key));
        if (flags == null)
            throw new DBException("key not found in flagmap",
                    DBException.RET_NOT_FOUND);
        return (flags.intValue());
    }

    /**
     * retrieve record text with given key
     * 
     * @param key
     *            key of record
     * @throws DBException
     *             not found/lock error/system error
     * @return text of record if found
     */
    public String read(int key) throws DBException {
        return (read(key, false));
    }

    // get system record text
    protected String readSys(int key) throws DBException {
        return (read(key, true));
    }

    // main read function
    private String read(int key, boolean system) throws DBException {

        getReadLock();
        sync();

        // need to go to the DB - so get the block number
        int bnum = find_block(key, system);
        if (bnum == -1)
            throw new DBException(
                    "find_block failed: " + Integer.toString(key),
                    DBException.RET_NOT_FOUND);

        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        cur_crypt_ = false;
        BlockHeader header = null;
        while (bnum >= 0) {
            try {
                // seek to the block
                fp_.seek(SuperBlock.SB_SIZE + (sb.blocksize_ * bnum));
            }
            catch (Exception e) {
                throw new DBException("read: seek failed",
                        DBException.RET_FATAL);
            }

            try {
                // read the header
                header = BlockHeader.read(fp_);

                // set flag if block is encrypted
                if ((header.flags_ & F_CRYPT) != 0)
                    cur_crypt_ = true;
            }
            catch (Exception e) {
                throw new DBException("read: cannot read header",
                        DBException.RET_FATAL);
            }

            try {
                // read the text bytes until we hit the max text in block or a 0
                // byte
                // and keep appending to the StringBuffer

                for (int i = 0; i < sb.blocksize_ - BH_SIZE; i++) {
                    int c = fp_.readUnsignedByte();

                    if (c == 0)
                        break;

                    bao.write(c);
                }

            }
            catch (Exception e) {
                throw new DBException("read: cannot read block",
                        DBException.RET_FATAL);
            }

            // check if flags look valid
            if (header.flags_ < 0 || header.flags_ > F_MAX) {
                throw new DBException("read: invalid flags",
                        DBException.RET_FATAL);
            }

            // check if key in block header matches the one we were looking for
            // never should fail unless DB file corrupted
            if (key != header.key_) {
                throw new DBException("read: key mismatch",
                        DBException.RET_FATAL);
            }

            // go to next block in chain
            bnum = header.next_;
        }

        // get String from StringBuffer
        String s;
        try {
            s = new String(bao.toByteArray(), "UTF-8");
            //System.out.println("read: " + s );
        }
        catch (Exception e) {
            throw new DBException("UTF-8 not supported");
        }

        // decrypt if needed
        if (cur_crypt_) {
            s = mc_.decrypt(s);
        }

        releaseLock();
        return (s);

    }

    // update an unencrypted user record
    public void update(int key, int flags, String st) throws DBException {
        update(key, flags, st, false, false);
    }

    // update a user record with crypt option
    public void update(int key, int flags, String st, boolean crypt)
            throws DBException {
        update(key, flags, st, crypt, false);
    }

    // update a system record
    protected void updateSys(int key, String st) throws DBException {
        update(key, 0, st, false, true);
    }

    // main update function
    private void update(int key, int flags, String st, boolean crypt,
            boolean system) throws DBException {

        // check if DB opened for R/W
        if (locktype_ != READ_WRITE && locktype_ != ADMIN)
            throw new DBException("update: cannot lock db",
                    DBException.RET_READ_ONLY);

        // update is simply delete then add
        // update in place would be messy because the number of blocks in the
        // chain might change - so just delete and add
        delete(key, system);
        add(key, flags, st, crypt, system);

    }

    // scan all block headers and build the index of keys to blocks
    // done on startup
    private int build_index() {
        BlockHeader header;

        int bnum = 0;

        try {
            // seek past superblock
            fp_.seek(SuperBlock.SB_SIZE);
        }
        catch (Exception e) {
            System.out.println("build_index: cannot seek");
            return (DBException.RET_FATAL );
        }

        // read all block headers
        while (true) {
            try {
                // read a header
                header = BlockHeader.read(fp_);
            }
            catch (EOFException e) {
                break;
            }
            catch (Exception e) {
                System.out.println("build_index: cannot read header");
                return (DBException.RET_FATAL );
            }

            // validate that flags look ok
            if (header.flags_ < 0 || header.flags_ > F_MAX) {
                System.out
                        .println("build_index(): Bad flags found for record ("
                                + header.key_ + ") - ignoring record");
            }
            // if block is deleted - add to freelist
            else if ((header.flags_ & F_DELETE) != 0) {
                freelist_.add(new Integer(bnum));
            }
            // if block is not an extended block - i.e. it is a single block
            // or first in a chain, then save it to the user or system indexes
            else if ((header.flags_ & F_EXTEND) == 0) {
                if ((header.flags_ & F_SYSTEM) != 0) {
                    add_kpair(header.key_, bnum, true);
                }
                else {
                    add_kpair(header.key_, bnum, false);

                    // for user blocks, add the user flags to an index
                    flagmap_.put(new Integer(header.key_), new Integer(
                            header.userflags_));

                    // keep track of the highest key
                    if (header.key_ > maxkey_)
                        maxkey_ = header.key_;
                }

            }

            // go to next block and update total number of blocks
            lastblock_ = bnum;
            bnum++;

            try {
                // seek to next block
                fp_.seek(SuperBlock.SB_SIZE + bnum * sb.blocksize_);
            }
            catch (Exception e) {
                break;
            }
        }

        return (DBException.RET_SUCCESS );
    }

    /**
     * prints detailed info for a DB to stdout
     * 
     * @param verbose
     *            print application block data
     * @throws DBException
     *             errors
     * @return scan text -suitable for printing
     */
    String scan(boolean verbose) throws DBException {

        // lots of flags and counters initialized here

        int dels = 0;
        int ext = 0;
        int sysblocks = 0;

        int bnum = 0;

        // print superblock info
        String s;
        s = "dbname: ";
        s += sb.dbname_ + "\n";

        s += "version: ";
        s += sb.version_ + "\n";

        s += "blocksize: ";
        s += sb.blocksize_ + "\n";

        s += "locktype: ";
        s += sb.locktype_ + "\n";

        StringBuffer output = new StringBuffer(s);

        // put a nice heading on the output
        output.append("BLOCK\tKEY\tNEXT\tFLAGS\tUSER\tDATA\n");
        output.append("-----\t---\t----\t-----\t----\t----\n");
        try {
            // seek past superblock to first regular block
            fp_.seek(SuperBlock.SB_SIZE);
        }
        catch (Exception e) {
            throw new DBException(e.toString());
        }

        // do not use regular MDB functions
        // we are dumping the contents of the DB - even if corrupted
        // so read raw info and output it
        while (true) {

            int k;
            try {
                // read the header key, next, flags
                // or whatever is in the file at this point if corrupted
                k = fp_.readInt();
                int n = fp_.readInt();
                int all = fp_.readInt();
                int f = all & 0xFFFF;
                int uf = (all & 0xFFFF0000) >>> 16;

                // keep counters of deleted, system, extended blocks for report
                if ((f & F_DELETE) != 0)
                    dels++;
                else {
                    if ((f & F_SYSTEM) != 0) {
                        sysblocks++;
                    }

                    if ((f & F_EXTEND) != 0) {
                        ext++;
                    }
                }

                boolean system = (f & F_SYSTEM) != 0;

                // for system blocks, output the key as a string
                // instead of some big integer that a user wouldn't know
                if (system || verbose) {

                    output.append(bnum + "\t");
                    output.append(k);

                    // show the system flags
                    String flgs = "";
                    if ((f & F_CRYPT) != 0)
                        flgs += "C";
                    if ((f & F_EXTEND) != 0)
                        flgs += "E";
                    if ((f & F_SYSTEM) != 0)
                        flgs += "S";
                    if ((f & F_DELETE) != 0)
                        flgs = "D";

                    String nxt = "";
                    if (n != -1)
                        nxt = Integer.toString(n);

                    // output the data for this header
                    output.append("\t" + nxt + "\t" + flgs + "\t"
                            + Integer.toString(uf));

                }
            }
            catch (EOFException eo) {
                break;
            }
            catch (Exception e) {
                throw new DBException(e.toString());
            }

            bnum++;

            try {
                ByteArrayOutputStream st = new ByteArrayOutputStream();

                // gather the block's text
                int i;
                for (i = 0; i < sb.blocksize_ - BH_SIZE; i++) {
                    int c = fp_.readUnsignedByte();
                    if (c == 0)
                        break;
                    st.write(c);
                }

                try {
                    s = new String(st.toByteArray(), "UTF-8");
                }
                catch (Exception e) {
                    throw new DBException("UTF-8 not supported");
                }

                // skip filler bytes to we should be at the next header
                fp_.skipBytes(sb.blocksize_ - BH_SIZE - i - 1);

                // output the string data for the block
                if (verbose) {
                    output.append("\t" + s + "\n");
                }

            }
            catch (Exception e) {
                throw new DBException(e.toString());
            }
        }

        // output some nice counts at the end
        output.append("\nTotal Blocks: " + Integer.toString(bnum)
                + "\nExtended: " + Integer.toString(ext) + "\nDeleted: "
                + Integer.toString(dels));

        return (output.toString());

    }

    /**
     * set persistent lock
     * 
     * @param locktype
     *            lock mode
     * @throws DBException
     *             lock error/system error
     */
    // write a lock flag into the superblock of the DB
    // so that the DB is locked even when no process is
    // attached. not really used anymore
    public void set_lock(int locktype) throws DBException {
        SuperBlock nsb = SuperBlock.read(fp_);
        nsb.locktype_ = locktype;
        nsb.write(fp_);
    }

    /**
     * export DB to an XTree string
     * 
     * @throws DBException
     *             error
     * @return XTree string containing DB info
     */
    public String passivate() throws DBException {

        StringBuffer o = new StringBuffer();
        o.ensureCapacity(100);
        o.append("<MDB>\n");
        XTree sbt = new XTree();
        sbt.name("SB");
        sbt.appendChild("DBNAME", sb.dbname_);
        sbt.appendChild("BLOCKSIZE", Integer.toString(sb.blocksize_));
        sbt.appendChild("VERSION", Integer.toString(sb.version_));
        //sb.appendChild("LOCK", locktype_ );

        o.append(sbt.toString());

        int key;
        String st;

        Vector sk = syskeys();
        for (int si = 0; si < sk.size(); si++) {

            Integer ki = (Integer) sk.elementAt(si);
            st = readSys(ki.intValue());
            XTree en = new XTree();
            en.name("ENTRY");
            en.appendChild("KEY", ki.toString());
            en.appendChild("DATA", st);
            en.appendChild("SYSTEM", "Y");
            o.append(en.toString());

        }

        try {
            for (key = first();; key = next()) {
                st = read(key);

                XTree en = new XTree();

                en.name("ENTRY");
                en.appendChild("KEY", Integer.toString(key));
                en.appendChild("USERFLAGS", Integer.toString(getFlags()));
                if (cur_crypt_ == true)
                    en.appendChild("CRYPT", "Y");
                en.appendChild("DATA", st);

                o.append(en.toString());
            }
        }
        catch (DBException e) {
            if (e.getRetCode() != DBException.RET_NOT_FOUND)
                throw e;
        }
        o.append("</MDB>");

        return (o.toString());
    }

    // create a DB from an XML export
    public static void import_db(File ifile, File dbfile) throws DBException {
        XTree xt = new XTree();
        try {
            XTree.readFromFile(ifile.getAbsolutePath());
        }
        catch (Exception e) {
            throw (new DBException("Could not read input file..."
                    + e.toString()));
        }

        String dbname = xt.child("MDB").child("SB").child("DBNAME").value();
        String blksz = xt.child("MDB").child("SB").child("BLOCKSIZE").value();
        String ver = xt.child("MDB").child("SB").child("VERSION").value();
        int v = Integer.parseInt(ver);
        if (v != VERSION)
            throw (new DBException("Export file version != code version: "
                    + ver + " != " + VERSION));
        int bs = 100;
        if (!blksz.equals("")) {
            bs = Integer.parseInt(blksz);
        }

        if (dbname.equals(""))
            dbname = "MDB Database";
        MDB db;
        try {
            MDB.create(dbname, dbfile.getAbsolutePath(), bs);
            db = new MDB(dbfile.getAbsolutePath(), MDB.READ_WRITE, false);

            for (int i = 1;; i++) {
                XTree en = xt.child("MDB").child("ENTRY", i);
                if (!en.exists())
                    break;

                int ky = Integer.parseInt(en.child("KEY").value());
                int uf = 0;
                XTree ufxt = en.child("USERFLAGS");
                if (ufxt.exists())
                    uf = Integer.parseInt(ufxt.value());
                String data = en.child("DATA").value();
                boolean crypt = false;
                String cr = en.child("CRYPT").value();
                if (cr.equals("Y"))
                    crypt = true;

                int fromidx = 0;
                String ndata = "";

                while (true) {
                    int idx = data.indexOf("\\n", fromidx);
                    if (idx == -1) {
                        ndata += data.substring(fromidx);
                        break;
                    }
                    else if (idx == fromidx) {
                        fromidx += 2;
                        continue;
                    }

                    ndata += data.substring(fromidx, idx - 1);
                    ndata += " ";
                    fromidx = idx + 2;
                }

                boolean system = false;
                String sy = en.child("SYSTEM").value();
                if (sy.equals("Y"))
                    system = true;

                if (system)
                    db.addSys(ky, ndata);
                else
                    db.add(ky, uf, ndata, crypt);

            }

            db.close();

        }
        catch (Exception e) {
            throw new DBException(e.toString());
        }
    }

    static public String repair(String filename, int blocksize)
            throws Exception {

        File f = new File(filename);
        if (!f.exists())
            throw new Exception("DB does not exist");
        RandomAccessFile fp = new RandomAccessFile(filename, "rw");
        SuperBlock sb = SuperBlock.read(fp);

        // print superblock info
        StringBuffer s = new StringBuffer();
        s.append("dbname: ");
        s.append(sb.dbname_ + "\n");
        s.append("version: " + sb.version_ + "\n");
        s.append("blocksize: " + sb.blocksize_ + "\n");
        s.append("locktype: " + sb.locktype_ + "\n");

        if (blocksize != 0 && sb.blocksize_ != blocksize) {
            s.append("*** Fixing blocksize ***\n");
            sb.blocksize_ = blocksize;
            sb.write(fp);
        }

        // seek past superblock to first regular block
        fp.seek(SuperBlock.SB_SIZE);

        // calculate number of blocks
        long filelen = f.length();
        filelen = filelen - SuperBlock.SB_SIZE;
        int numblocks = (int) (filelen / sb.blocksize_);
        s.append("Number of rows = " + numblocks + "\n");

        // do not use regular MDB functions
        // we are dumping the contents of the DB - even if corrupted
        // so read raw info and output it
        int invalid = 0;
        for (int block = 0; block < numblocks; block++) {
            boolean valid = true;
            fp.seek(SuperBlock.SB_SIZE + block * sb.blocksize_);
            BlockHeader head = BlockHeader.read(fp);

            // check that flags are valid
            if (head.flags_ > F_MAX) {
                valid = false;
                s.append("Invalid flags for key = " + head.key_ + "["
                        + head.flags_ + "]\n");
            }
            else if ((head.flags_ & F_DELETE) != 0) {
                continue;
            }

            if (head.next_ < -1 || head.next_ >= numblocks) {
                valid = false;
                s.append("Invalid next block for key = " + head.key_ + "["
                        + head.next_ + "]\n");
            }

            if (valid)
                continue;
            StringBuffer st = new StringBuffer();

            // gather the block's text
            int i;
            for (i = 0; i < sb.blocksize_ - BH_SIZE; i++) {
                char c = (char) fp.readUnsignedByte();
                if (c == 0)
                    break;
                st.append(c);
            }

            // mark block as deleted
            s.append("marking block " + block + " key=" + head.key_ + " text ["
                    + st.toString() + "] as deleted ***\n");
            fp.seek(SuperBlock.SB_SIZE + block * sb.blocksize_);
            head.flags_ |= F_DELETE;
            head.write(fp);
            invalid++;

        }

        fp.close();

        MDB db = new MDB(filename, ADMIN, false);
        s.append("Reading all Strings\n");
        Vector keys = db.keys();
        for (int i = 0; i < keys.size(); i++) {
            int rowkey = ((Integer) keys.elementAt(i)).intValue();
            try {
                db.read(rowkey);
            }
            catch (DBException e) {
                if (e.getRetCode() != DBException.RET_NOT_FOUND) {
                    invalid++;
                    s.append(e);
                    s.append("\nCould not read record, key = " + rowkey
                            + " attempting delete\n");
                    try {
                        db.delete(rowkey);
                    }
                    catch (DBException e2) {
                    }
                }
            }
        }

        db.close();
        db = null;

        s.append("Rows repaired = " + invalid + "\nDone\n");
        return (s.toString());
    }

}