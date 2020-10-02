package dependencies.Commands;

import dependencies.UserAuthorization.User;

import java.util.ArrayList;

public class ClearCommand extends Commands {

    private final User user;

    public ClearCommand(String name, User user){
        this.name = name;
        this.user = user;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.clearCollection(this.user);
     }
}
