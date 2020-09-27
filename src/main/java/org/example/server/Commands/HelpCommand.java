package org.example.server.Commands;

import java.util.ArrayList;

public class HelpCommand extends Commands{

    public HelpCommand(String name){
        this.name = name;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.help();
    }
}
