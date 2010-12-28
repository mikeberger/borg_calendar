package net.sf.borg.plugin.sync;

import java.io.InputStream;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.borg.common.Errmsg;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.model.db.jdbc.JdbcDBUpgrader;

/**
 * class to track all appointment model changes since the last sync it will
 * persist this information to a file
 */
public class SyncLog extends Model implements Model.Listener {

	static private SyncLog singleton = null;

	static public SyncLog getReference() {
		if (singleton == null)
			singleton = new SyncLog();
		return singleton;
	}

	public SyncLog() {
		new JdbcDBUpgrader(
				"select id from syncmap",
				"CREATE CACHED TABLE syncmap (id integer NOT NULL,action varchar(25) NOT NULL,PRIMARY KEY (id))")
				.upgrade();
		AppointmentModel.getReference().addListener(this);
	}

	@Override
	public void update(ChangeEvent newEvent) {

		if (newEvent == null)
			return;

		try {

			Integer newKey = (Integer) newEvent.getObject();
			ChangeEvent existingEvent = get(newKey.intValue());

			// any condition not listed is either a no-op or cannot occur
			if (existingEvent == null) {
				this.insert(newEvent);
			} else {
				Integer existingKey = (Integer) existingEvent.getObject();

				if (existingEvent.getAction() == ChangeEvent.ChangeAction.ADD
						&& newEvent.getAction() == ChangeEvent.ChangeAction.DELETE) {
					this.delete(existingKey.intValue());
				} else if (existingEvent.getAction() == ChangeEvent.ChangeAction.CHANGE
						&& newEvent.getAction() == ChangeEvent.ChangeAction.DELETE) {
					ChangeEvent event = new ChangeEvent(newKey,
							ChangeEvent.ChangeAction.DELETE);
					this.delete(existingKey.intValue());
					this.insert(event);
				} else if (existingEvent.getAction() == ChangeEvent.ChangeAction.DELETE
						&& newEvent.getAction() == ChangeEvent.ChangeAction.ADD) {
					ChangeEvent event = new ChangeEvent(newKey,
							ChangeEvent.ChangeAction.CHANGE);
					this.delete(existingKey.intValue());
					this.insert(event);
				}

			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	private ChangeEvent createFrom(ResultSet r) throws SQLException {
		int id = r.getInt("id");
		ChangeEvent.ChangeAction action = ChangeEvent.ChangeAction.valueOf(r
				.getString("action"));
		ChangeEvent event = new ChangeEvent(new Integer(id), action);
		return event;
	}

	public ChangeEvent get(int id) throws Exception {

		ChangeEvent ret = null;

		PreparedStatement stmt = JdbcDB.getConnection().prepareStatement(
				"SELECT * FROM syncmap WHERE id = ?");
		stmt.setInt(1, id);

		ResultSet r = null;
		try {
			r = stmt.executeQuery();
			if (r.next()) {
				ret = createFrom(r);
			}
			return ret;
		} finally {
			if (r != null)
				r.close();
			stmt.close();
		}
	}

	public List<ChangeEvent> getAll() throws Exception {

		List<ChangeEvent> ret = new ArrayList<ChangeEvent>();

		PreparedStatement stmt = JdbcDB.getConnection().prepareStatement(
				"SELECT * FROM syncmap");

		ResultSet r = null;
		try {
			r = stmt.executeQuery();
			while (r.next()) {
				ret.add(createFrom(r));
			}
			return ret;
		} finally {
			if (r != null)
				r.close();
			if (stmt != null)
				stmt.close();
		}
	}

	public void insert(ChangeEvent event) throws Exception {
		PreparedStatement stmt = JdbcDB.getConnection().prepareStatement(
				"INSERT INTO syncmap ( id, action) " + " VALUES " + "( ?, ?)");

		Integer key = (Integer) event.getObject();
		stmt.setInt(1, key.intValue());
		stmt.setString(2, event.getAction().toString());
		stmt.executeUpdate();
		stmt.close();

	}

	public void delete(int id) throws Exception {
		PreparedStatement stmt = JdbcDB.getConnection().prepareStatement(
				"DELETE FROM syncmap WHERE id = ?");

		stmt.setInt(1, id);
		stmt.executeUpdate();
		stmt.close();

	}

	public void deleteAll() throws Exception {
		PreparedStatement stmt = JdbcDB.getConnection().prepareStatement(
				"DELETE FROM syncmap");

		stmt.executeUpdate();
		stmt.close();

	}
	
	@XmlRootElement(name = "SYNCMAP")
	private static class XmlContainer {
		public Collection<ChangeEvent> ChangeEvents;
	}

	@Override
	public void export(Writer fw) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Marshaller m = jc.createMarshaller();
		XmlContainer container = new XmlContainer();
		container.ChangeEvents = getAll();
		m.marshal(container, fw);
	}

	@Override
	public void importXml(InputStream is) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Unmarshaller u = jc.createUnmarshaller();

		XmlContainer container = (XmlContainer) u
				.unmarshal(is);

		for (ChangeEvent evt : container.ChangeEvents) {
			insert(evt);
		}

	}

	@Override
	public String getExportName() {
		return "SYNCMAP";
	}

	@Override
	public String getInfo() throws Exception {
		return "Synclogs: " + getAll().size();
	}

}
