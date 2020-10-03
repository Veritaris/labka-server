package server.Authorization;

import dependencies.UserAuthorization.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.DatabaseManager.DatabaseManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("FieldMayBeFinal")
public class Authorization {
    private static final Logger logger = LogManager.getLogger();
    private HashSet<String> authenticatedUsers;
    private DatabaseManager databaseManager;

    public Authorization(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        authenticatedUsers = new HashSet<String>() {
        }

        ;
    }

    private String hashPassword(String rawPassword) {
        return DigestUtils.md5Hex(rawPassword);
    }

    public String loginUser(String username, String rawPassword) {
        User userToLogin = new User(username, rawPassword);

        if (this.databaseManager.getUser(username) != null){
            if (userToLogin.equals(this.databaseManager.getUser(username))) {
                this.authenticatedUsers.add(userToLogin.getToken());
                logger.info(
                        String.format("User '%s' signed in at '%s' from ip ''", username, LocalDateTime.now())
                );
                return "You was logged in";
            }
        }
        return "Wrong login or password";
    }

    public String logoutUser(User user) {
        this.authenticatedUsers.remove(user.getToken());
        logger.info(
                String.format("User '%s' logged out at '%s'", user.getUsername(), LocalDateTime.now())
        );
        return "You has been logged out.";
    }

    public String registerUser(String username, String rawPassword) {
        if (this.databaseManager.getUser(username) == null) {
            if (this.databaseManager.addUser(username, hashPassword(rawPassword), UUID.randomUUID())) {
                logger.info(
                        String.format("User '%s' was created", username)
                );
                loginUser(username, rawPassword);
                return String.format("User '%s' was successfully created", username);
            } else {
                logger.error("Unknown error");
                return "Internal error";
            }
        }
        return String.format("User with username '%s' already exists", username);
    }

    public boolean isAuthenticated(User user) {
        return this.authenticatedUsers.contains(user.getToken());
    }

    public boolean hasPermissionToEdit(User user, Long groupID) {
        return this.databaseManager.getGroupAuthor(groupID).equals(user.getUsername());
    }
}
