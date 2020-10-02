package dependencies.Commands;


import dependencies.CommandManager.CommandExecutor;

public abstract class Commands implements CommandInterface{

    String name;
    protected CommandExecutor manager = CommandExecutor.getInstance();
}
