package org.example.server.Authentification;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.server.CommandManager.CommandObject;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class UserAuthentification {
    private static final Logger logger = LogManager.getLogger();
    private String username;
    private String password;
    private String passwordHash;
    private String databasePasswordHash;
    private ArrayList<String> authenticatedUsers;

    public UserAuthentification() {
        authenticatedUsers = new ArrayList<>();
    }

    private String hashPassword(String rawPassword) {
        return DigestUtils.md5Hex(rawPassword);
    }

    public boolean isValidPassword(String rawPassword) {
        return hashPassword(rawPassword).equals(databasePasswordHash);
    }

    public void loginUser(String username, String rawPassword) {
        logger.info(
                String.format("User '%s' signed in at '%s' from ip ''", username, LocalDateTime.now())
        );

    }

    public void logoutUser(String username) {
        logger.info(
                String.format("User '%s' logged out at '%s'", username, LocalDateTime.now())
        );
    }

    public void createUser(String username, String rawPassword) {
        logger.info(
                String.format("User '%s' was created", username)
        );
    }

    public boolean isAuthenticated(String username) {
        return false;
    }

    public boolean hasPermissionToEdit(String username, CommandObject commandObject) {
        return false;
    }

}
