package dependencies.Commands;

import dependencies.UserAuthorization.User;

import java.util.ArrayList;

public class InfoCommand extends Commands{

    private final User user;

    public InfoCommand(String name, User user){
        this.name = name;
        this.user = user;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.info(user);
    }
}
