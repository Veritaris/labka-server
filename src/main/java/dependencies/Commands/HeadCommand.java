package dependencies.Commands;

import java.util.HashMap;

public class HeadCommand extends Commands {

    public HeadCommand(String name){
        this.name = name;
    }
    @Override
    public HashMap<String, String> execute(){
        return manager.head();
    }
}
