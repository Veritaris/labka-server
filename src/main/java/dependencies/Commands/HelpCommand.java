package dependencies.Commands;

import java.util.HashMap;

public class HelpCommand extends Commands{

    public HelpCommand(String name){
        this.name = name;
    }

    @Override
    public HashMap<String, String> execute(){
        return manager.help();
    }
}
