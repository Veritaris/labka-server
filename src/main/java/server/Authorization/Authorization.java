package server.Authorization;

import dependencies.UserAuthorization.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.DatabaseManager.DatabaseManager;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
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

    public HashMap<String, String> loginUser(String username, String rawPassword) {
        HashMap<String, String> response = new HashMap<>();
        User userToLogin = new User(username, rawPassword);

        if (this.databaseManager.getUser(username) != null){
            if (userToLogin.equals(this.databaseManager.getUser(username))) {
                this.authenticatedUsers.add(userToLogin.getToken());
                logger.info(
                        String.format("User '%s' signed in at '%s'", username, LocalDateTime.now())
                );
                response.put("status", "200");
                response.put("message", "Logged in");
                return response;
            }
        }
        response.put("status", "401");
        response.put("message", "Wrong login or password");
        return response;
    }

    public HashMap<String, String> logoutUser(User user) {
        HashMap<String, String> response = new HashMap<>();
        this.authenticatedUsers.remove(user.getToken());
        logger.info(
                String.format("User '%s' logged out at '%s'", user.getUsername(), LocalDateTime.now())
        );
        response.put("status", "200");
        response.put("message", "Logged out");
        return response;
    }

    public HashMap<String, String> registerUser(String username, String rawPassword) {
        HashMap<String, String> response = new HashMap<>();
        if (this.databaseManager.getUser(username) == null) {
            if (this.databaseManager.addUser(username, hashPassword(rawPassword), UUID.randomUUID())) {
                logger.info(
                        String.format("User '%s' was created", username)
                );
                response.put("status", "200");
                response.put("message", "Registered");
                return response;
            } else {
                response.put("status", "500");
                response.put("message","Internal error");
                return response;
            }
        }
        response.put("status", "403");
        response.put("message", "This username is not available");
        return response;
    }

    public boolean isAuthenticated(User user) {
        return this.authenticatedUsers.contains(user.getToken());
    }

    public boolean hasPermissionToEdit(User user, Long groupID) {
        return this.databaseManager.getGroupAuthor(groupID).equals(user.getUsername());
    }
}
