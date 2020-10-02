package dependencies.Commands;

import dependencies.UserAuthorization.User;

import java.util.ArrayList;

public class RemoveByIdCommand extends Commands {

    private final User user;
    private final Long groupID;

    public RemoveByIdCommand(String name, Long groupID, User user){
        this.name = name;
        this.groupID = groupID;
        this.user = user;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.remove_by_id(groupID, user);
    }
}
