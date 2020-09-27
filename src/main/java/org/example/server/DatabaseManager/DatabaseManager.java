package org.example.server.DatabaseManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.server.Utils.UserObject;

import java.sql.*;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger();
    private static Connection dbConnection = null;
    private final String username;
    private final String password;
    private final String url;
    private final String schemaName;
    private final InitCommands databaseInitCommands;

    public DatabaseManager(String url, String username, String password, String schemaName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.schemaName = schemaName;
        logger.info("Loading database init commands...");
        databaseInitCommands = new InitCommands();
        logger.info(String.format("%s init commands has been loaded", databaseInitCommands.getCommands().size()));
    }

    public void executeSQL(String sqlString) throws SQLException {
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password); Statement targetStatement = dbConnection.createStatement()) {
            targetStatement.execute(sqlString);
            logger.info(String.format("Executed SQL: %s", sqlString));
        } catch (SQLException e) {
            for (StackTraceElement stacktraceLine : e.getStackTrace()) {
                logger.error(stacktraceLine);
            }
        } finally {
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    public void init() throws SQLException {
        for (String schemaName: databaseInitCommands.getCommands().keySet()) {
            executeSQL(databaseInitCommands.getCommands().get(schemaName));
        }
    }

    public UserObject getUser(String username) {
        String getUserSQL = String.format("select * from %s.users where username='%s'", this.schemaName, username);
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(getUserSQL);

            if (resultSet.next()) {
                String name = resultSet.getString("username");
                String password = resultSet.getString("password");
                return new UserObject(name, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet getGroup(int id) {
        String getGroupSQL = String.format("select * from %s.users where username='%s'", this.schemaName, id);
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(getGroupSQL);

            if (resultSet.next()) {
                return resultSet;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addUser(String username, String password, UUID uuid) {
        String addUserSQL = String.format(
                "insert into %s.users values (nextval('%s.users_id_seq'), '%s', '%s', '%s')",
                this.schemaName, this.schemaName, username, password, uuid
        );
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            statement.execute(addUserSQL);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addGroup() {
        String addGroupSQL = "insert into ? " +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String addAdminSQL = "insert into ? " +
                "values (?, ?, ?, ?)";

        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            PreparedStatement addAdminStatement = dbConnection.prepareStatement(addAdminSQL);
            PreparedStatement addGroupStatement = dbConnection.prepareStatement(addGroupSQL);

            addAdminStatement.setString(1, String.format("%s.admins", this.schemaName));
            addAdminStatement.setString(2, username);
            addAdminStatement.setString(3, password);
            addAdminStatement.setString(4, password);

            addGroupStatement.setString(1, String.format("%s.study_groups", this.schemaName));
            addGroupStatement.setString(2, password);
            addGroupStatement.setString(3, password);
            addGroupStatement.setString(4, password);
            addGroupStatement.setString(5, password);
            addGroupStatement.setString(6, password);
            addGroupStatement.setString(7, password);
            addGroupStatement.setString(8, password);
            addGroupStatement.setString(9, password);

            Statement groupStatement = dbConnection.createStatement();
            Statement adminStatement = dbConnection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
