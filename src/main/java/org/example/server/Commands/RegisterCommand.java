package org.example.server.Commands;


import java.util.ArrayList;

public class RegisterCommand extends Commands{
    private final String username;
    private final String rawPassword;

    public RegisterCommand(String name, String username, String rawPassword){
        this.name = name;
        this.username = username;
        this.rawPassword = rawPassword;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.register(username, rawPassword);
    }
}