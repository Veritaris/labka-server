package org.example.server.Commands;

import java.util.ArrayList;

public class InfoCommand extends Commands{

    public InfoCommand(String name){
        this.name = name;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.info();
    }
}
