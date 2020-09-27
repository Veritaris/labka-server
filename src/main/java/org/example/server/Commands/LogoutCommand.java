package org.example.server.Commands;

import java.util.ArrayList;

public class LogoutCommand extends Commands{
    private final String username;
    private final String password;

    public LogoutCommand(String name, String username, String password){
        this.name = name;
        this.username = username;
        this.password = password;
    }

    @Override
    public ArrayList<String> execute(){
        return manager.logout(username, password);
    }
}
