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

import net.sf.borg.common.Errmsg;

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
    // protected static final int LOG_KEY = 2000000001; // key to the record
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
	    } catch (EOFException eo) {
		throw eo;
	    } catch (Exception e) {
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
	public void write(RandomAccessFile fp) throws Exception {
	    if (dbname_.length() > NAME_SIZE)
		throw (new Exception("DB name too long"));

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
		throw (new Exception("Cannot write superblock"));
	    }
	}

	// read superblock
	static public SuperBlock read(RandomAccessFile fp) throws Exception {
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
	    } catch (Exception e) {
		throw new Exception("Error reading superblock fields");
	    }

	    if (nsb.blocksize_ < BH_SIZE + 2)
		throw new Exception("Blocksize is too small");

	    return (nsb);
	}

	public int readCounter(RandomAccessFile fp) throws Exception {
	    try {
		fp.seek(4 + 4 + 4 + NAME_SIZE);
		updateCount_ = fp.readInt();
		return (updateCount_);
	    } catch (Exception e) {
		throw new Exception("Error reading update counter");
	    }

	}

	public void writeCounter(RandomAccessFile fp) throws Exception {
	    try {
		// super block always at file start
		fp.seek(4 + 4 + 4 + NAME_SIZE);

		// write it
		fp.writeInt(updateCount_);
	    } catch (Exception e2) {
		throw (new Exception("Cannot write update counter"));
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
	} else {
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
    private int free_entry(int key, boolean system) throws Exception {

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

	throw new Exception("free_entry - block not found");
    }

    // add a new key to the DB and associate a block with it
    private int add_new_entry(int key, boolean system) throws Exception {

	// reset currency pointer when changing the DB
	cur_keys_ = null;

	int bnum = find_block(key, system);
	if (bnum != -1) {
	    throw (new Exception("add_new_entry - duplicate key already exists"));
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
    private void getSharedLock() throws Exception {
	try {
	    FileChannel fc = fp_.getChannel();

	    FileLock lock;
	    try {
		lock = fc.tryLock(0, 1, true);
	    } catch (IOException e) {
		throw new Exception("Cannot lock DB " + filename_);
	    }
	    if (lock == null) {
		throw new Exception("Cannot lock DB " + filename_);
	    }
	} catch (NoSuchMethodError nsm) {
	    // running 1.3 - from Jsync conduit
	    return;
	}

    }

    // try to get an exclusive lock for single user usage
    private void getExclusiveLock() throws Exception {
	try {
	    FileChannel fc = fp_.getChannel();
	    FileLock lock;
	    try {
		lock = fc.tryLock(0, 1, false);
	    } catch (IOException e) {
		e.printStackTrace();
		throw new Exception("Cannot lock DB " + filename_);
	    }
	    if (lock == null) {
		throw new Exception("Cannot lock DB " + filename_);
	    }
	} catch (NoSuchMethodError nsm) {
	    // running 1.3 - from Jsync conduit
	    return;
	}

    }

    // wait for a shared read lock
    public void getReadLock() throws Exception {
	if (!sharedDB_)
	    return;
	FileChannel fc = fp_.getChannel();
	try {
	    if (rwlock_ != null)
		return;

	    rwlock_ = fc.lock(1, 1, true);
	} catch (IOException e) {
	    throw new Exception("Cannot lock DB " + filename_);
	}
	if (rwlock_ == null) {
	    throw new Exception("Cannot lock DB " + filename_);
	}

    }

    // wait for an exclusive write lock
    public void getWriteLock() throws Exception {
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
	} catch (IOException e) {
	    throw new Exception("Cannot lock DB " + filename_);
	}

	if (rwlock_ == null) {
	    throw new Exception("Cannot lock DB " + filename_);
	}
    }

    // release a read or write lock
    public void releaseLock() throws Exception {
	if (!sharedDB_)
	    return;
	if (rwlock_ != null) {
	    try {
		rwlock_.release();
	    } catch (IOException e) {
		throw new Exception("Cannot lock DB " + filename_);
	    } catch (NoSuchMethodError nsm) {
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

    // Are we out of sync?
    // if we're not locked before entering this method, we attempt to acquire
    // a read lock
    public boolean isDirty() throws Exception {

	// if we have exclusive access - don't bother doing anything
	if (!sharedDB_)
	    return false;

	boolean gotlock = false;

	try {
	    if (!haveLock()) {
		getReadLock();
		gotlock = true;
	    }

	    int count = sb.updateCount_;
	    int newcount = sb.readCounter(fp_);
	    if (count == newcount) {
		// if the last count for this object matches the count read
		// from the file - nothing is needed, db is in sync
		return false;
	    }
	} finally {
	    if (gotlock)
		releaseLock();
	}
	return true;
    }

    // flush all cached data and recreate
    // if we're not locked before entering this method, we attempt to acquire
    // a read lock
    public void sync() {

	// if we have exclusive access - don't bother doing anything
	if (!sharedDB_)
	    return;

	boolean gotlock = false;

	try {
	    if (!haveLock()) {
		getReadLock();
		gotlock = true;
	    }

	    int count = sb.updateCount_;
	    int newcount = sb.readCounter(fp_);
	    if (count == newcount) {
		// if the last count for this object matches the count read
		// from the file - nothing is needed, db is in sync
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
	} catch (Exception e) {
	    System.out.println(e);
	} finally {
	    if (gotlock)
		try {
		    releaseLock();
		} catch (Exception e) {

		}
	}
    }

    /**
     * constructor - opens DB of given name with given lock type
     * 
     * @param file
     *                filename of DB
     * @param locktype
     *                locking mode
     * @throws Exception
     *                 errors
     */
    public MDB(String file, int locktype, boolean shared) throws Exception {
	filename_ = file;
	lastblock_ = -1;
	sharedDB_ = shared;

	// allocate an encrpytion object
	try {
	    mc_ = new Crypt(ckey_);
	} catch (Exception e) {
	    throw new Exception("failed to initialize encryption object");
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
	    throw new Exception("DB does not exist");

	// open mode based on lock type
	try {
	    if (locktype == READ_ONLY || locktype == READ_DIRTY) {
		fp_ = new RandomAccessFile(filename_, "r");
	    } else if (locktype == READ_WRITE || locktype == ADMIN) {
		fp_ = new RandomAccessFile(filename_, "rw");
	    } else {
		throw new Exception("Invalid Locking Mode");
	    }
	} catch (Exception e) {
	    throw new Exception(e.toString());
	}

	// lock the database
	try {
	    if (sharedDB_) {
		getSharedLock();
		getReadLock();
	    } else
		getExclusiveLock();
	} catch (Exception e) {
	    throw new Exception(e.toString());
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
	    throw new Exception("DB version != software version: "
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
	    throw new Exception("Cannot lock DB. Superblock indicates a lock");
	}

	// scan the whole DB file jumping from block-header to block-header to
	// build
	// a map of keys vs. blocks. does not read throwse text fields
	build_index();

	releaseLock(); // release read lock

    }

    public void close() {
	try {
	    fp_.close();
	} catch (Exception e) {
	    System.out.println(e);
	}

    }

    // duh
    public int nextkey() {
	try {
	    sync();
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
	return (++maxkey_);
    }

    /**
     * Add a new DB row
     * 
     * @param key
     *                integer key ( > 0 and < MAX_KEY)
     * @param st
     *                data to store
     * @throws Exception
     *                 errors
     */

    // add a user row to the db - with optional encryption
    public void add(int key, int flags, String st, boolean crypt)
	    throws Exception {
	add(key, flags, st, crypt, false);
    }

    // add a system row to the DB
    protected void addSys(int key, String st) throws Exception {
	add(key, 0, st, false, true);
    }

    // add an unencrypted user row
    public void add(int key, int flags, String st) throws Exception {
	add(key, flags, st, false, false);
    }

    // main add function
    // add will store a record with a key, flags, and text string
    // it will break up the string as needed and create extended blocks if the
    // string is too large to fit in a single block
    private void add(int key, int flags, String st, boolean crypt,
	    boolean system) throws Exception {
	if (locktype_ != READ_WRITE && locktype_ != ADMIN)
	    throw new Exception("add: Cannot lock DB for write");

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
	} catch (Exception e) {
	    throw new Exception("UTF-8 not supported");
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
		} else if (system) {
		    header.flags_ |= F_SYSTEM;
		}

		bnum = main_bnum;
	    }

	    try {
		// seek to the beginning of the block in the file
		// which is sizeof(bnum blocks) after the superblock ends
		fp_.seek(SuperBlock.SB_SIZE + (sb.blocksize_ * bnum));
	    } catch (Exception e) {
		throw new Exception("add: seek failed");
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

		// String subs = st.substring(extnum * text_per_block, endindex
		// );
		// fp_.writeBytes(subs);
		int sidx = extnum * text_per_block;
		int sublen = endindex - sidx;
		// System.out.println( utf + " " + sidx + " " + sublen );
		fp_.write(utf, sidx, sublen);

		// if the string does not fill up all of the space in the block
		// -
		// write '\0' bytes to fill the block
		for (int i = 0; i <= (text_per_block - sublen); i++) {
		    fp_.writeByte(0);
		}

	    } catch (Exception e) {
		throw new Exception("add: write failed");
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
    public void delete(int key) throws Exception {
	delete(key, false);
    }

    // delete a system row
    protected void deleteSys(int key) throws Exception {
	delete(key, true);
    }

    // main delete function
    private void delete(int key, boolean system) throws Exception {

	int bnum;

	// check if DB is open for R/W
	if (locktype_ != READ_WRITE && locktype_ != ADMIN)
	    throw new Exception("delete: cannot lock DB");

	getWriteLock();
	sync();

	// remove the user flags data from the map
	if (!system)
	    flagmap_.remove(new Integer(key));

	// free the entry from the map of keys/block nums
	bnum = free_entry(key, system);

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
	    } catch (Exception e) {
		throw new Exception("delete: seek failed");
	    }

	    try {
		// read existing header - to get next ptr
		inheader = BlockHeader.read(fp_);
	    } catch (Exception e) {
		throw new Exception("delete: cannot read header");
	    }

	    try {
		// seek back to beginning of header
		fp_.seek(SuperBlock.SB_SIZE + (sb.blocksize_ * bnum));
	    } catch (Exception e) {
		throw new Exception("delete: seek2 failed");
	    }

	    // overwrite the header with a deleted header
	    try {
		delheader.write(fp_);
	    } catch (Exception e) {
		throw new Exception("delete: write failed");
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
		throw new Exception("delete: different key found");
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



    // get the flags for the current record where current is the last record
    // accessed using first/next
    // in this way, the flags can be retrieved without accessing the DB text
    public int getFlags() throws Exception {

	if (cur_keys_ == null)
	    throw new Exception("getFlags() called with no currency");
	Integer key = (Integer) cur_keys_[cur_key_idx_];
	Integer flags = (Integer) flagmap_.get(key);
	if (flags == null)
	    throw new Exception("key not found in flagmap");
	return (flags.intValue());
    }

    // look up the flags for a certain record by key
    public int getFlags(int key) throws Exception {

	Integer flags = (Integer) flagmap_.get(new Integer(key));
	if (flags == null)
	    throw new Exception("key not found in flagmap");
	return (flags.intValue());
    }

    /**
     * retrieve record text with given key
     * 
     * @param key
     *                key of record
     * @throws Exception
     *                 not found/lock error/system error
     * @return text of record if found
     */
    public String read(int key) throws Exception {
	return (read(key, false));
    }

    // get system record text
    protected String readSys(int key) throws Exception {
	return (read(key, true));
    }

    // main read function
    private String read(int key, boolean system) throws Exception {

	getReadLock();
	sync();

	// need to go to the DB - so get the block number
	int bnum = find_block(key, system);
	if (bnum == -1)
	    return null;

	ByteArrayOutputStream bao = new ByteArrayOutputStream();

	cur_crypt_ = false;
	BlockHeader header = null;
	while (bnum >= 0) {

	    // seek to the block
	    fp_.seek(SuperBlock.SB_SIZE + (sb.blocksize_ * bnum));

	    // read the header
	    header = BlockHeader.read(fp_);

	    // set flag if block is encrypted
	    if ((header.flags_ & F_CRYPT) != 0)
		cur_crypt_ = true;

	    // read the text bytes until we hit the max text in block or a 0
	    // byte
	    // and keep appending to the StringBuffer

	    for (int i = 0; i < sb.blocksize_ - BH_SIZE; i++) {
		int c = fp_.readUnsignedByte();

		if (c == 0)
		    break;

		bao.write(c);
	    }

	    // check if flags look valid
	    if (header.flags_ < 0 || header.flags_ > F_MAX) {
		throw new Exception("read: invalid flags");
	    }

	    // check if key in block header matches the one we were looking for
	    // never should fail unless DB file corrupted
	    if (key != header.key_) {
		throw new Exception("read: key mismatch");
	    }

	    // go to next block in chain
	    bnum = header.next_;
	}

	// get String from StringBuffer
	String s;
	try {
	    s = new String(bao.toByteArray(), "UTF-8");
	    // System.out.println("read: " + s );
	} catch (Exception e) {
	    throw new Exception("UTF-8 not supported");
	}

	// decrypt if needed
	if (cur_crypt_) {
	    s = mc_.decrypt(s);
	}

	releaseLock();
	return (s);

    }

    // update an unencrypted user record
    public void update(int key, int flags, String st) throws Exception {
	update(key, flags, st, false, false);
    }

    // update a user record with crypt option
    public void update(int key, int flags, String st, boolean crypt)
	    throws Exception {
	update(key, flags, st, crypt, false);
    }

    // update a system record
    protected void updateSys(int key, String st) throws Exception {
	update(key, 0, st, false, true);
    }

    // main update function
    private void update(int key, int flags, String st, boolean crypt,
	    boolean system) throws Exception {

	// check if DB opened for R/W
	if (locktype_ != READ_WRITE && locktype_ != ADMIN)
	    throw new Exception("update: cannot lock db");

	// update is simply delete then add
	// update in place would be messy because the number of blocks in the
	// chain might change - so just delete and add
	delete(key, system);
	add(key, flags, st, crypt, system);

    }

    // scan all block headers and build the index of keys to blocks
    // done on startup
    private void build_index() throws Exception {
	BlockHeader header;

	int bnum = 0;

	// seek past superblock
	fp_.seek(SuperBlock.SB_SIZE);

	// read all block headers
	while (true) {
	    try {
		// read a header
		header = BlockHeader.read(fp_);
	    } catch (EOFException e) {
		break;
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
		} else {
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
	    } catch (Exception e) {
		break;
	    }
	}

    }

}