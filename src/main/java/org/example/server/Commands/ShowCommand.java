package org.example.server.Commands;

import java.util.ArrayList;

public class ShowCommand extends Commands{

    public ShowCommand(String name){
        this.name = name;
    }
    @Override
    public ArrayList<String> execute(){
        return manager.show();
    }
}
