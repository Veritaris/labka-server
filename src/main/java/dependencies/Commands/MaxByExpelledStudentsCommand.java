package dependencies.Commands;

import java.util.HashMap;

public class MaxByExpelledStudentsCommand extends Commands{

    public MaxByExpelledStudentsCommand(String name){

        this.name = name;
    }
    @Override
    public HashMap<String, String> execute(){
        return manager.max_by_expelled_students();
    }
}
