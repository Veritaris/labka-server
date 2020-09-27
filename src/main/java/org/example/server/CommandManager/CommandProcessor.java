package org.example.server.CommandManager;

import org.example.server.Authentification.UserAuthentication;
import org.example.server.Commands.*;
import org.example.server.DatabaseManager.DatabaseManager;

@SuppressWarnings("FieldCanBeLocal")
public class CommandProcessor {
    private CommandObject commandObjectToSend;
    private CommandExecutor commandExecutor;
    private String collectionsPath;
    private UserAuthentication authLib;

    public CommandProcessor() {

    }

    public CommandObject processCommand(CommandObject receivedCommandObject) {

        if (!authLib.isAuthenticated(receivedCommandObject.getSenderUsername(), receivedCommandObject.getSenderPassword())) {
            return CommandObjectCreator.createErrorObject("401", "User not authorized");
        }

        receivedCommandObject.clearMessage();
        commandObjectToSend = new CommandObject(receivedCommandObject.getName());

        switch (receivedCommandObject.getName()) {
            case "add":
                commandObjectToSend.setMessage(
                        (new AddCommand(receivedCommandObject.getName(), receivedCommandObject.getStudyGroup(), receivedCommandObject.getSenderUsername())).execute()
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

    public void setAuthLib(UserAuthentication authLib) {
        this.authLib = authLib;
        this.commandExecutor.setAuthLib(this.authLib);
    }

    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.authLib.setDatabaseManager(databaseManager);
        this.commandExecutor.setDatabaseManager(databaseManager);
    }
}
