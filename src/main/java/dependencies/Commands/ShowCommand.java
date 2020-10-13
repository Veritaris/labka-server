package dependencies.Commands;

import java.util.HashMap;

public class ShowCommand extends Commands{

    public ShowCommand(String name){
        this.name = name;
    }
    @Override
    public HashMap<String, String> execute(){
        return manager.show();
    }
}
