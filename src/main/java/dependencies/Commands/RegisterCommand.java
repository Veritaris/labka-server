package dependencies.Commands;


import dependencies.UserAuthorization.User;

import java.util.HashMap;

public class RegisterCommand extends Commands{
    private final String username;
    private final String rawPassword;

    public RegisterCommand(String name, User user){
        this.name = name;
        this.username = user.getUsername();
        this.rawPassword = user.getPassword();
    }

    @Override
    public HashMap<String, String> execute(){
        return manager.register(this.username, this.rawPassword);
    }
}