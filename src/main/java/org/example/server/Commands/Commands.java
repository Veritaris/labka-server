package org.example.server.Commands;


import org.example.server.CommandManager.CommandExecutor;

import java.nio.file.Paths;

public abstract class Commands implements CommandInterface{

    String name;
    protected CommandExecutor manager = CommandExecutor.getInstance(Paths.get(".").toAbsolutePath().normalize().toString() + String.format("/%s", "users.json"));
}
