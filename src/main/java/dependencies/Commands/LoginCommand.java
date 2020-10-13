package dependencies.Commands;

import dependencies.UserAuthorization.User;

import java.util.HashMap;

public class LoginCommand extends Commands{
    private final String username;
    private final String rawPassword;

    public LoginCommand(String name, User user){
        this.name = name;
        this.username = user.getUsername();
        this.rawPassword = user.getPassword();
    }

    @Override
    public HashMap<String, String> execute(){
        return manager.login(username, rawPassword);
    }
}
