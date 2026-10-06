package dev.padrewin.moneypouchdeluxe.database.migrations;

import dev.padrewin.colddev.database.DataMigration;
import dev.padrewin.colddev.database.DatabaseConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Per player statistics: how many pouches of each kind they opened and how much they won, per
 * economy (a pouch's economy can change in pouches.yml, so both are part of the key).
 */
public class _1_Create_Stats extends DataMigration {

    public _1_Create_Stats() {
        super(1);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablePrefix + "stats (" +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "pouch VARCHAR(100) NOT NULL, " +
                    "economy VARCHAR(100) NOT NULL, " +
                    "opened BIGINT NOT NULL DEFAULT 0, " +
                    "won BIGINT NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (uuid, pouch, economy)" +
                    ")");
        }
    }

}
