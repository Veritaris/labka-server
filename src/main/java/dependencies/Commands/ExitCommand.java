package dependencies.Commands;

import java.util.HashMap;

public class ExitCommand extends Commands{

    public ExitCommand(String name){
        this.name =name;
    }

    @Override
    public HashMap<String, String> execute(){
        return manager.exit();
    }
}
