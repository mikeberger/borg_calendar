// BORGCalendar/model/src/main/java/net/sf/borg/model/db/jdbc/DbDirtyManager.java - hy copilot

package net.sf.borg.model.db.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the dirty flag for the SQLite database using SQLite's update hook.
 * The update hook is triggered whenever INSERT, UPDATE, or DELETE occurs.
 */
public class DbDirtyManager {
	
	static private final Logger log = Logger.getLogger("net.sf.borg");
	
	static private DbDirtyManager singleton = null;
	
	/**
	 * Listener interface for dirty database notifications
	 */
	public interface DbDirtyListener {
		/**
		 * Called when the database becomes dirty (has been modified)
		 */
		void onDbDirty();
		
		/**
		 * Called when the database is cleaned (uploaded/synced)
		 */
		void onDbClean();
	}
	
	private volatile boolean dbDirty = false;
	private volatile boolean enabled = true;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private final List<DbDirtyListener> listeners = new ArrayList<>();
	
	/**
	 * Get the singleton instance
	 */
	static public DbDirtyManager getReference() {
		if (singleton == null) {
			singleton = new DbDirtyManager();
		}
		return singleton;
	}
	
	private DbDirtyManager() {
	}
	
	
	/**
	 * Initialize the SQLite update hook on the given connection.
	 * This must be called after the SQLite connection is established.
	 * 
	 * @param connection the SQLite connection
	 * @throws Exception if hook setup fails
	 */
	public void initUpdateHook(Connection connection) throws Exception {
		if (connection == null) {
			throw new Exception("Connection cannot be null");
		}
		
		try {
			// SQLite JDBC driver provides access to the native SQLite update hook
			// through a custom interface. Use the connection's underlying SQLite connection.
			org.sqlite.SQLiteConnection sqliteConn = connection.unwrap(org.sqlite.SQLiteConnection.class);
		
			
			sqliteConn.addUpdateListener((type, db, table, rowId) -> {
				log.fine("SQLite update hook fired: type=" + type + " database=" + db + " table=" + table + " rowid=" + rowId);
				setDirty();
			});
			
			log.info("SQLite update hook initialized");
		} catch (Exception e) {
			log.warning("Failed to initialize SQLite update hook: " + e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Check if the database has pending updates
	 * 
	 * @return true if database is dirty, false otherwise
	 */
	public boolean isDirty() {
		return dbDirty;
	}
	
	/**
	 * Mark the database as dirty (has been modified since last upload).
	 * Automatically called by the SQLite update hook, but can also be called manually.
	 */
	public void setDirty() {
		if (!dbDirty && enabled) {
			log.fine("Database marked as dirty");
			dbDirty = true;
			notifyDirtyListeners();
		}
	}
	
	/**
	 * Mark the database as clean (successfully uploaded).
	 * Call this after successful database upload to Google Drive.
	 */
	public void setClean() {
		if (dbDirty) {
			log.fine("Database marked as clean");
			dbDirty = false;
			notifyCleanListeners();
		}
	}
	
	/**
	 * Add a listener to be notified of dirty/clean state changes
	 * 
	 * @param listener the listener to add
	 */
	public void addListener(DbDirtyListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Remove a listener
	 * 
	 * @param listener the listener to remove
	 */
	public void removeListener(DbDirtyListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	private void notifyDirtyListeners() {
		synchronized (listeners) {
			for (DbDirtyListener listener : listeners) {
				listener.onDbDirty();
			}
		}
	}
	
	private void notifyCleanListeners() {
		synchronized (listeners) {
			for (DbDirtyListener listener : listeners) {
				listener.onDbClean();
			}
		}
	}
}