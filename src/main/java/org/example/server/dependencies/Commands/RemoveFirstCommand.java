package org.example.server.dependencies.Commands;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class RemoveFirstCommand extends Commands{

    public RemoveFirstCommand(String name){
        this.name = name;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.remove_first();
    }
}
