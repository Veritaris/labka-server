package dependencies.Commands;

import dependencies.CommandManager.CommandExecutor;
import java.util.HashMap;

public class HistoryCommand extends Commands{
    private final CommandExecutor commandExecutor;

    public HistoryCommand(String name, CommandExecutor commandExecutor){
        this.name = name;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public HashMap<String, String> execute(){
        return commandExecutor.getHistory();
    }
}
