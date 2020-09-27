package org.example.server.Utils;

@SuppressWarnings("FieldCanBeLocal")
public class UserObject {
    private final String username;
    private final String password;

    public UserObject(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean equals(UserObject userObject) {
        return (this.username.equals(userObject.getUsername()) && this.password.equals(userObject.getPassword()));
    }

    @Override
    public String toString() {
        return String.format("User <%s>", this.username);
    }
}
