package dependencies.Commands;

import dependencies.UserAuthorization.User;

import java.util.ArrayList;

public class RemoveFirstCommand extends Commands{

    private final User user;

    public RemoveFirstCommand(String name, User user){
        this.name = name;
        this.user = user;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.remove_first(user);
    }
}
