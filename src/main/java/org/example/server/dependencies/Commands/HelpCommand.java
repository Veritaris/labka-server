package org.example.server.dependencies.Commands;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class HelpCommand extends Commands{

    public HelpCommand(String name){
        this.name = name;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.help();
    }
}
