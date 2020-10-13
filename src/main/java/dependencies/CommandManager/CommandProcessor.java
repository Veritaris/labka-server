package dependencies.CommandManager;

import dependencies.UserAuthorization.User;
import server.Authorization.Authorization;
import dependencies.Commands.*;
import server.DatabaseManager.DatabaseManager;

@SuppressWarnings("FieldCanBeLocal")
public class CommandProcessor {
    private CommandObject commandObjectToSend;
    public CommandExecutor commandExecutor;
    private final Authorization authLib;
    private String command;
    private User user;

    public CommandProcessor(DatabaseManager databaseManager, Authorization authLib) {
        this.authLib = authLib;
        this.commandExecutor = new CommandExecutor(databaseManager, authLib);
    }

    public CommandObject processCommandThread(CommandObject receivedCommandObject){
        command = receivedCommandObject.getName();
        user = receivedCommandObject.getSender();

        if (command.equals("connect")) {
            return new CommandObject("connected");
        }

        if (!authLib.isAuthenticated(user)) {
            commandObjectToSend = new CommandObject(command);
            switch (command) {
                case "register":
                    commandObjectToSend.setBody(new RegisterCommand(command, user).execute());
                    return commandObjectToSend;

                case "login":
                    commandObjectToSend.setBody(new LoginCommand(command, user).execute());
                    return commandObjectToSend;
                default:
                    return CommandObjectCreator.createErrorObject("401", "User not authorized");
            }
        }

        commandObjectToSend = new CommandObject(command);

        switch (command) {
            case "logout":
                commandObjectToSend.setBody(
                        new LogoutCommand(command, user).execute()
                );
                break;

            case "add":
                commandObjectToSend.setBody(
                        new AddCommand(command, receivedCommandObject.getStudyGroup(), user.getUsername()).execute()
                );
                break;

            case "clear":
                commandObjectToSend.setBody(
                        new ClearCommand(command, user).execute()
                );
                break;

            case "exit":
                commandObjectToSend.setBody(
                        new ExitCommand(command).execute()
                );
                break;

            case "filter_by_group_admin":
                commandObjectToSend.setBody(
                        new FilterByGroupAdminCommand(command, receivedCommandObject.getStringArgument()).execute()
                );
                break;

            case "filter_contains_name":
                commandObjectToSend.setBody(
                        new FilterContainsNameCommand(command, receivedCommandObject.getStringArgument()).execute()
                );
                break;

            case "head":
                commandObjectToSend.setBody(
                        new HeadCommand(command).execute()
                );
                break;

            case "help":
                commandObjectToSend.setBody(
                        new HelpCommand(command).execute()
                );
                break;

            case "history":
                commandObjectToSend.setBody(
                        new HistoryCommand(command, this.commandExecutor).execute()
                );
                break;

            case "info":
                commandObjectToSend.setBody(
                        new InfoCommand(command, user).execute()
                );
                break;

            case "max_by_expelled_students":
                commandObjectToSend.setBody(
                        new MaxByExpelledStudentsCommand(command).execute()
                );
                break;

            case "remove_by_id":
                commandObjectToSend.setBody(
                        new RemoveByIdCommand(command, receivedCommandObject.getGroupID(), user).execute()
                );
                break;

            case "remove_first":
                commandObjectToSend.setBody(
                        new RemoveFirstCommand(command, user).execute()
                );
                break;

            case "show":
                commandObjectToSend.setBody(
                        new ShowCommand(command).execute()
                );
                break;

            case "update":
                commandObjectToSend.setBody(
                        new UpdateCommand(command, receivedCommandObject.getStudyGroup(), user).execute()
                );
                break;

            default:
                System.out.println("!Something went wrong in Commander!");
        }
        this.commandExecutor.addToHistory(command);
        return commandObjectToSend;
    }
}
