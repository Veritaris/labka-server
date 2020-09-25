package org.example.server.dependencies;

import org.example.server.dependencies.Commands.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("FieldCanBeLocal")
public class CommandProcessor {
    private final CommandObjectCreator commandObjectCreator = new CommandObjectCreator();
    private final Scanner consoleScanner = new Scanner(System.in);
    private List<String> commandArguments = new ArrayList<>();
    private CommandObject commandObjectToSend;
    private CommandExecutor commandExecutor;
    private CommandObject commandObject;
    private List<String> commandArray;
    private String collectionsPath;
    private String command;

    public CommandProcessor() {

    }

    public CommandObject processCommand(CommandObject receivedCommandObject) {
        receivedCommandObject.clearMessage();
        commandObjectToSend = new CommandObject(receivedCommandObject.getName());

        switch (receivedCommandObject.getName()) {
            case "add":
                commandObjectToSend.setMessage(
                        (new AddCommand(receivedCommandObject.getName(), receivedCommandObject.getStudyGroup())).execute()
                );
                break;

            case "clear":
                commandObjectToSend.setMessage(
                        (new ClearCommand(receivedCommandObject.getName())).execute()
                );
                break;

            case "exit":
                commandObjectToSend.setMessage(
                        (new ExitCommand(receivedCommandObject.getName())).execute()
                );
                break;

            case "filter_by_group_admin":
                commandObjectToSend.setMessage(
                        (new FilterByGroupAdminCommand(
                                receivedCommandObject.getName(),
                                receivedCommandObject.getStringArgument())
                        ).execute()
                );
                break;

            case "filter_contains_name":
                commandObjectToSend.setMessage(
                        (new FilterContainsNameCommand(
                                receivedCommandObject.getName(),
                                receivedCommandObject.getStringArgument())
                        ).execute()
                );
                break;

            case "head":
                commandObjectToSend.setMessage(
                        (new HeadCommand(receivedCommandObject.getName())).execute()
                );
                break;

            case "help":
                commandObjectToSend.setMessage(
                        (new HelpCommand(receivedCommandObject.getName())).execute()
                );
                break;

            case "history":
                commandObjectToSend.setMessage(
                        (new HistoryCommand(receivedCommandObject.getName(), this.commandExecutor)).execute()
                );
                break;

            case "info":
                commandObjectToSend.setMessage(
                        (new InfoCommand(receivedCommandObject.getName())).execute()
                );
                break;

            case "max_by_expelled_students":
                commandObjectToSend.setMessage(
                        (new MaxByExpelledStudentsCommand(receivedCommandObject.getName())).execute()
                );
                break;

            case "remove_by_id":
                commandObjectToSend.setMessage(
                        (new RemoveByIdCommand(receivedCommandObject.getName(), receivedCommandObject.getLongArgument())).execute()
                );
                break;

            case "remove_first":
                commandObjectToSend.setMessage(
                        (new RemoveFirstCommand(receivedCommandObject.getName())).execute()
                );
                break;

            case "show":
                commandObjectToSend.setMessage(
                        (new ShowCommand(receivedCommandObject.getName())).execute()
                );
                break;

            case "update":
                commandObjectToSend.setMessage(
                        (new UpdateCommand(receivedCommandObject.getName(), receivedCommandObject.getStudyGroup())).execute()
                );
                break;

            default:
                System.out.println("!Something went wrong in Commander!");
        }
        this.commandExecutor.addToHistory(receivedCommandObject.getName());
        return commandObjectToSend;
    }

    public void setCommandExecutor(String collectionsFilePath) {
        this.collectionsPath = collectionsFilePath;
        this.commandExecutor = new CommandExecutor(this.collectionsPath);
    }

    public void close(){
        this.consoleScanner.close();
    }
}
