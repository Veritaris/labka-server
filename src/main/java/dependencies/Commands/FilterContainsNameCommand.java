package dependencies.Commands;

import java.util.HashMap;

public class FilterContainsNameCommand extends Commands{

    String argument;

    public FilterContainsNameCommand(String name, String argument){
        this.name = name;
        this.argument = argument;
    }

    @Override
    public HashMap<String, String> execute(){
        return manager.filter_contains_name(argument);
    }
}
