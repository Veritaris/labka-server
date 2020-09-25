package org.example.server.dependencies.Commands;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class ShowCommand extends Commands{

    public ShowCommand(String name){
        this.name = name;
    }
    @Override
    public ArrayList<String> execute(){
        return manager.show();
    }
}
