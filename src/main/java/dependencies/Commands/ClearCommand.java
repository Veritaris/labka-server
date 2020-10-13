package dependencies.Commands;

import dependencies.UserAuthorization.User;

import java.util.HashMap;

public class ClearCommand extends Commands {

    private final User user;

    public ClearCommand(String name, User user){
        this.name = name;
        this.user = user;
    }

    @Override
    public HashMap<String, String> execute(){
        return manager.clearCollection(this.user);
     }
}
