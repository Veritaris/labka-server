package server.DatabaseManager;

import dependencies.UserAuthorization.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dependencies.Collection.*;
import server.Exceptions.SameAdminException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;


@SuppressWarnings({"FieldCanBeLocal", "DuplicatedCode", "FieldMayBeFinal"})
public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger();
    private ArrayList<StudyGroup> groups;
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

    public void executeSQL(String sqlString) {
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password); Statement targetStatement = dbConnection.createStatement()) {
            try {
                targetStatement.execute(sqlString);
            } catch (Exception e) {
                logger.error(e.getMessage());
                for (StackTraceElement stacktraceLine : e.getStackTrace()) {
                    logger.error(String.format("\t%s",stacktraceLine));
                }
            }
            logger.info(String.format("Executed SQL: %s", sqlString));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            for (StackTraceElement stacktraceLine : e.getStackTrace()) {
                logger.error(String.format("\t%s",stacktraceLine));
            }
            System.exit(1);
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
        for (String query: databaseInitCommands.getCommands().keySet()) {
            executeSQL(String.format(databaseInitCommands.getCommands().get(query), this.schemaName, this.schemaName));
        }
    }

    public User getUser(String username) {
        String getUserSQL = String.format("select * from %s.users where username='%s'", this.schemaName, username);
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(getUserSQL);

            if (resultSet.next()) {
                String name = resultSet.getString("username");
                String password = resultSet.getString("password");
                return User.getDBUser(username, password);
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

    public String getGroupAuthor(Long id) {
        String getGroupQuery = String.format("select * from %s.study_groups where id='%s'", this.schemaName, id);
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(getGroupQuery);

            if (resultSet.next()) {
                return resultSet.getString("author");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearAll(User user) {
        executeSQL(String.format("delete from %s.study_groups where author='%s'", this.schemaName, user.getUsername()));
    }

    public void deleteById(Long id) {
        executeSQL(String.format("delete from %s.study_groups where id=%s", this.schemaName, id));
    }

    public ArrayList<StudyGroup> getAllGroups() {
        String getGroupQuery = String.format("select * from %s.study_groups", this.schemaName);
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(getGroupQuery);
            groups = new ArrayList<>();

            while (resultSet.next()) {
                groups.add(constructStudyGroup(resultSet));
            }
            return groups;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StudyGroup getGroup(Long id) {
        String getGroupQuery = String.format("select * from %s.study_groups where id='%s'", this.schemaName, id);
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(getGroupQuery);

            if (resultSet.next()) {
                return constructStudyGroup(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addGroup(String name, Semester semester, Long x, Long y, Integer students_amount, Country nationality,
                         Double height, Integer weight, String adminName, Integer students_to_expel, Integer expelled_students, LocalDate creation_date,
                         String author) throws SameAdminException{

        String admin_hash = DigestUtils.md5Hex(String.format("%s%s%s%s", nationality, height, weight, adminName));
        String addAdminQuery = String.format(
                "insert into %s.admins values (nextval('%s.admin_id_seq'), '%s', '%s', '%s', '%s', '%s')",
                this.schemaName, this.schemaName, nationality, height, weight, adminName, admin_hash
        );
        String addGroupQuery = String.format(
                "insert into %s.study_groups values (nextval('%s.study_groups_id_seq'), '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                this.schemaName, this.schemaName, name, semester, x, y, students_amount, admin_hash, students_to_expel, expelled_students, creation_date, author
        );

        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format("select admin_hash from %s.study_groups where admin_hash='%s'", this.schemaName, admin_hash));

            if (resultSet.next()) {
                throw new SameAdminException("Admin can be related only to one group");
            } else {
                executeSQL(addGroupQuery);
                executeSQL(addAdminQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateGroup(Long id, String name, Semester semester, Long x, Long y, Integer students_amount, Country nationality, Double height, Integer weight, String adminName, Integer students_to_expel, Integer expelled_students) {

        String admin_hash = DigestUtils.md5Hex(String.format("%s%s%s%s", nationality, height, weight, adminName));

        String updateAdminQuery = String.format(
                "insert into %s.admins values (nextval('%s.admin_id_seq'), '%s', '%s', '%s', '%s', '%s')",
                this.schemaName, this.schemaName, nationality, height, weight, adminName, admin_hash
        );
        String updateGroupQuery = String.format(
                        "update %s.study_groups set " +
                                "name='%s'," +
                                "semester='%s'," +
                                "x='%s'," +
                                "y='%s'," +
                                "students_amount='%s'," +
                                "students_to_expel='%s'," +
                                "expelled_students='%s' " +
                                "where id='%s'",
                                this.schemaName, name, semester, x, y, students_amount,
                                students_to_expel, expelled_students, id
                        );
        String updateGroupAdminQuery = String.format(
                "update %s.study_groups set " +
                        "admin_hash='%s' " +
                        "where id='%s'",
                this.schemaName, admin_hash, id
        );

        executeSQL(updateGroupQuery);



        if (admin_hash.equals(getGroup(id).getGroupAdmin().getPersonHash())) {
            executeSQL(updateAdminQuery);
            executeSQL(updateGroupAdminQuery);
        }
    }

    private Person getAdmin(String adminHash) {
        Person admin = null;
        String getUserQuery = String.format("select * from %s.admins where admin_hash='%s'", this.schemaName, adminHash);
        try (Connection dbConnection = DriverManager.getConnection(this.url, this.username, this.password)) {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(getUserQuery);

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
            LocalDate creationDate =  dbGroupData.getDate("creation_date").toLocalDate();

            studyGroup = new StudyGroup(group_id, name, semester, coordinates, studentsCount, admin, toExpelAmount, expelledStudentsAmount, creationDate);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studyGroup;
    }

}
