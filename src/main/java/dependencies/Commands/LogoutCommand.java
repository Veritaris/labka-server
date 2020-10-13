package dependencies.Commands;

import dependencies.UserAuthorization.User;

import java.util.HashMap;

public class LogoutCommand extends Commands{
    private final String username;
    private final String password;

    public LogoutCommand(String name, User user){
        this.name = name;
        this.username = user.getUsername();
        this.password = user.getPassword();
    }

    @Override
    public HashMap<String, String> execute(){
        return manager.logout(username, password);
    }
}
