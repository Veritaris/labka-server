package dependencies.UserAuthorization;

public class Session {
    private String username = null;
    private String token = null;
    private boolean isLoggedIn = false;

    public Session() {

    }

    public void addUser(User user) {
        this.username = user.getUsername();
        this.token = user.getToken();
        this.isLoggedIn = true;
    }

    public void removeUser() {
        this.username = null;
        this.token = null;
        this.isLoggedIn = false;
    }
}
