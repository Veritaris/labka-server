package org.example.server.Commands;


import org.example.server.Collection.StudyGroup;

import java.util.ArrayList;

public class AddCommand extends Commands{
    private StudyGroup argument;

    public AddCommand(String name, StudyGroup arg){
        this.name = name;
        argument = arg;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.add(argument);
    }
}
