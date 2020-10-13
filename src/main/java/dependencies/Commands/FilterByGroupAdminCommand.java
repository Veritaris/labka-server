package dependencies.Commands;

import java.util.HashMap;

public class FilterByGroupAdminCommand extends Commands {
    String argument;

    public FilterByGroupAdminCommand(String name, String argument){
        this.name = name;
        this.argument = argument;
    }
    @Override
    public HashMap<String, String> execute(){
        return manager.filter_by_group_admin(argument);
    }
}
