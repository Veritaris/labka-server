package org.example.server.DatabaseManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.server.Collection.*;
import org.example.server.Utils.UserObject;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@SuppressWarnings({"FieldCanBeLocal", "DuplicatedCode"})
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

    public void executeSQL(String sqlString){
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password); Statement targetStatement = dbConnection.createStatement()) {
            targetStatement.execute(sqlString);
            logger.info(String.format("Executed SQL: %s", sqlString));
        } catch (SQLException e) {
            for (StackTraceElement stacktraceLine : e.getStackTrace()) {
                logger.error(stacktraceLine);
            }
        } finally {
            try {
                if (dbConnection != null) {
                    dbConnection.close();
                }
            } catch (SQLException e) {
                for (StackTraceElement line: e.getStackTrace()) {
                    logger.error(line);
                }
            }
        }
    }

    public void init() {
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

    public StudyGroup getGroup(int id) {
        String getGroupSQL = String.format("select * from %s.users where username='%s'", this.schemaName, id);
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(getGroupSQL);

            if (resultSet.next()) {
                return constructStudyGroup(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addGroup(String name, String semester, String x, String y, String students_amount, String nationality, String height, String weight, String adminName, String students_to_expel, String expelled_students, String creation_date, String author) {

        String admin_hash = DigestUtils.md5Hex(String.format("%s%s%s%s", nationality, height, weight, adminName));
        String addAdminSQL = String.format(
                "insert into %s.admins values (nextval('%s.admin_id_seq'), %s, %s, %s, %s)",
                this.schemaName, this.schemaName, nationality, height, weight, adminName
        );
        String addGroupSQL = String.format(
                "insert into %s.study_groups values (nextval('%s.study_groups_id_seq'), %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                this.schemaName, this.schemaName, name, semester, x, y, students_amount, admin_hash, students_to_expel, expelled_students, creation_date, author
        );

        executeSQL(addAdminSQL);
        executeSQL(addGroupSQL);
    }


    public void updateGroup(String id, String name, String semester, String x, String y, String students_amount, String nationality, String height, String weight, String adminName, String students_to_expel, String expelled_students) {

        String admin_hash = DigestUtils.md5Hex(String.format("%s%s%s%s", nationality, height, weight, adminName));

        String addAdminSQL = String.format(
                "update %s.admins set (nextval('%s.admin_id_seq'), %s, %s, %s, %s)",
                this.schemaName, this.schemaName, nationality, height, weight, adminName
        );
        String addGroupSQL = String.format(
                        "insert %s.study_groups set " +
                                "name=%s," +
                                "semester=%s," +
                                "x=%s," +
                                "y=%s," +
                                "students_amount=%s," +
                                "admin_hash=%s," +
                                "students_to_expel=%s," +
                                "expelled_students=%s " +
                                "where id=%s",
                                this.schemaName, name, semester, x, y, students_amount, admin_hash,
                                students_to_expel, expelled_students, id
                        );

        executeSQL(addAdminSQL);
        executeSQL(addGroupSQL);
    }

    private Person getAdmin(String adminHash) {
        Person admin = null;
        String getUserSQL = String.format("select * from %s.admins where admin_hash='%s'", this.schemaName, adminHash);
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(getUserSQL);

            if (resultSet.next()) {
                Country nationality = Country.valueOf(resultSet.getString("nationality"));
                double height = resultSet.getDouble("height");
                int weight = resultSet.getInt("weight");
                String name = resultSet.getString("name");
                admin =  new Person(name, height, weight, nationality);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return admin;
    }

    private StudyGroup constructStudyGroup(ResultSet dbGroupData) {
        StudyGroup studyGroup = null;
        try {
            Long group_id = dbGroupData.getLong("id");
            String name = dbGroupData.getString("name");
            Semester semester = Semester.valueOf(dbGroupData.getString("semester"));
            long x = dbGroupData.getLong("x");
            long y = dbGroupData.getLong("y");
            Coordinates coordinates = new Coordinates(x, y);
            int studentsCount = dbGroupData.getInt("students_amount");
            Person admin = getAdmin(dbGroupData.getString("admin_hash"));
            int toExpelAmount = dbGroupData.getInt("students_to_expel");
            int expelledStudentsAmount = dbGroupData.getInt("expelled_students");
            LocalDateTime creationDate =  (dbGroupData.getDate("creation_date").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

            studyGroup = new StudyGroup(group_id, name, semester, coordinates, studentsCount, admin, toExpelAmount, expelledStudentsAmount, creationDate);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studyGroup;
    }

}
