package org.example.server.Commands;

import java.util.ArrayList;

public class LoginCommand extends Commands{
    private final String username;
    private final String rawPassword;

    public LoginCommand(String name, String username, String rawPassword){
        this.name = name;
        this.username = username;
        this.rawPassword = rawPassword;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.login(username, rawPassword);
    }
}
