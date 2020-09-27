package org.example.server.Authentification;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.server.CommandManager.CommandObject;
import org.example.server.DatabaseManager.DatabaseManager;
import org.example.server.Utils.UserObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class UserAuthentication {
    private static final Logger logger = LogManager.getLogger();
    private String username;
    private String password;
    private String passwordHash;
    private String databasePasswordHash;
    private ArrayList<String> authenticatedUsers;
    private DatabaseManager databaseManager;

    public UserAuthentication() {
        authenticatedUsers = new ArrayList<>();
    }

    private String hashPassword(String rawPassword) {
        return DigestUtils.md5Hex(rawPassword);
    }

    private String hashUser(String username, String rawPassword) {
        return DigestUtils.md5Hex(String.format("%salt%s", username, rawPassword));
    }

    public String loginUser(String username, String rawPassword) {
        UserObject userToLogin = new UserObject(username, hashPassword(rawPassword));
        if (this.databaseManager.getUser(username) != null){
            if (userToLogin.equals(this.databaseManager.getUser(username))) {
                this.authenticatedUsers.add(hashUser(username, hashPassword(rawPassword)));
                logger.info(
                        String.format("User '%s' signed in at '%s' from ip ''", username, LocalDateTime.now())
                );
                return "You was logged in";
            }
        }
        return "Wrong login or password";
    }

    public String logoutUser(String username, String rawPassword) {
        this.authenticatedUsers.remove(hashUser(username, hashPassword(rawPassword)));
        logger.info(
                String.format("User '%s' logged out at '%s'", username, LocalDateTime.now())
        );
        return "You has been logged out.";
    }

    public String createUser(String username, String rawPassword) {
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

    public boolean isAuthenticated(String username, String password) {
        return this.authenticatedUsers.contains(hashUser(username, hashPassword(password)));
    }

    public boolean hasPermissionToEdit(String username, CommandObject commandObject) {
        return false;
    }

    public ArrayList<String> getAuthenticatedUsers(){
        return this.authenticatedUsers;
    }

    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

}
