package org.example.server.dependencies.Commands;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class HeadCommand extends Commands {

    public HeadCommand(String name){
        this.name = name;
    }
    @Override
    public ArrayList<String> execute(){
        return manager.head();
    }
}
