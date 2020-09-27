package org.example.server.DatabaseManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger();
    private static Connection dbConnection = null;

    public DatabaseManager(){
    }

    public static Connection createConnection() {
        try {
            dbConnection = DriverManager.getConnection("jdbc:postgresql://hostname:port/dbname", "username", "password");
        } catch (SQLException e) {
            logger.error(e.getStackTrace());
            e.printStackTrace();
        }
        return dbConnection;
    }

    public void closeConnection() {
        try {
            dbConnection.close();
        } catch (SQLException e) {
            logger.error(e.getStackTrace());
            e.printStackTrace();
        }
    }
}
