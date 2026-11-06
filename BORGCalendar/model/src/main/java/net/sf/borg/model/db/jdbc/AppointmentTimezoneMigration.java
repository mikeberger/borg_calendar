package net.sf.borg.model.db.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.logging.Logger;

/**
 * Adds the appointment timezone column to databases created by older releases.
 * Existing appointment timestamps are deliberately never rewritten.
 */
final class AppointmentTimezoneMigration {

    private static final Logger log = Logger.getLogger("net.sf.borg");
    private static final String LEGACY_ZONE_PROPERTY = "borg.appointment.legacyTimezone";

    private AppointmentTimezoneMigration() {
    }

    static void upgrade() {
        try {
            Connection connection = JdbcDB.getConnection();
            if (!hasTimezoneColumn(connection.getMetaData())) {
                JdbcDB.execSQL("ALTER TABLE appointments ADD timezone varchar(100)");
                log.info("Added appointments.timezone; existing appointment dates were preserved");
            }
            backfillConfiguredLegacyTimezone(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not upgrade appointment timezone schema", e);
        } catch (Exception e) {
            throw new IllegalStateException("Could not upgrade appointment timezone schema", e);
        }
    }

    private static boolean hasTimezoneColumn(DatabaseMetaData metadata) throws SQLException {
        try (ResultSet tables = metadata.getTables(null, null, "%", new String[] {"TABLE"})) {
            while (tables.next()) {
                if (!"appointments".equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
                    continue;
                }
                String schema = tables.getString("TABLE_SCHEM");
                String table = tables.getString("TABLE_NAME");
                try (ResultSet columns = metadata.getColumns(null, schema, table, "%")) {
                    while (columns.next()) {
                        if ("timezone".equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Legacy rows cannot disclose their original zone. Users may explicitly set
     * -Dborg.appointment.legacyTimezone=Area/Location before first startup to
     * assign one. Without that setting old rows remain NULL and their appt_date
     * values are left unchanged.
     */
    private static void backfillConfiguredLegacyTimezone(Connection connection) throws SQLException {
        String configuredZone = System.getProperty(LEGACY_ZONE_PROPERTY);
        if (configuredZone == null || configuredZone.trim().isEmpty()) {
            return;
        }
        String zone = configuredZone.trim();
        try {
            ZoneId.of(zone);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid IANA timezone in -D" + LEGACY_ZONE_PROPERTY + ": " + zone, e);
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE appointments SET timezone = ? WHERE timezone IS NULL OR timezone = ''")) {
            statement.setString(1, zone);
            int updated = statement.executeUpdate();
            if (updated > 0) {
                log.info("Assigned configured legacy appointment timezone " + zone + " to " + updated + " rows");
            }
        }
    }
}
