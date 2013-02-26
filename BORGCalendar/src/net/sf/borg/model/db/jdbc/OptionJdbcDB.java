package net.sf.borg.model.db.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import net.sf.borg.model.db.OptionDB;
import net.sf.borg.model.entity.Option;

class OptionJdbcDB implements OptionDB {

	/**
	 * Gets an option value from the options table
	 * 
	 * @param oname
	 *            the option name
	 * 
	 * @return the option value
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public String getOption(String oname) throws Exception {
		String ret = null;
		PreparedStatement stmt = JdbcDB.getConnection().prepareStatement(
				"SELECT value FROM options WHERE name = ?");
		stmt.setString(1, oname);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			ret = rs.getString("value");
		}
		rs.close();
		stmt.close();

		return (ret);
	}

	/**
	 * Gets all options from the options table.
	 * 
	 * @return a collection of options
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public Collection<Option> getOptions() throws Exception {
		ArrayList<Option> keys = new ArrayList<Option>();
		PreparedStatement stmt = JdbcDB.getConnection().prepareStatement(
				"SELECT name, value FROM options");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			keys.add(new Option(rs.getString("name"), rs.getString("value")));
		}

		rs.close();
		stmt.close();

		return (keys);

	}

	/**
	 * Sets an option in the options table.
	 * 
	 * @param option
	 *            the option to set
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void setOption(Option option) throws Exception {
		String oname = option.getKey();
		String value = option.getValue();

		try {
			PreparedStatement stmt = JdbcDB.getConnection().prepareStatement(
					"DELETE FROM options WHERE name = ?");
			stmt.setString(1, oname);
			stmt.executeUpdate();
			stmt.close();

		} catch (Exception e) {
			// empty
		}

		if (value == null || value.equals(""))
			return;

		PreparedStatement stmt = JdbcDB.getConnection().prepareStatement(
				"INSERT INTO options ( name, value ) " + "VALUES ( ?, ?)");

		stmt.setString(1, oname);
		stmt.setString(2, value);

		stmt.executeUpdate();
		stmt.close();

	}

}
