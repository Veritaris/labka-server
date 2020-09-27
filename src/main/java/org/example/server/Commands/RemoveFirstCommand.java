package org.example.server.Commands;

import java.util.ArrayList;

public class RemoveFirstCommand extends Commands{

    public RemoveFirstCommand(String name){
        this.name = name;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.remove_first();
    }
}
