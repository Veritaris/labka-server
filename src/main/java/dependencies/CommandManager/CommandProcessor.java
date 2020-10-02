package dependencies.CommandManager;

import dependencies.UserAuthorization.User;
import server.Authorization.Authorization;
import dependencies.Commands.*;
import server.DatabaseManager.DatabaseManager;

@SuppressWarnings("FieldCanBeLocal")
public class CommandProcessor {
    private CommandObject commandObjectToSend;
    public CommandExecutor commandExecutor;
    private DatabaseManager databaseManager;
    private Authorization authLib;
    private String command;
    private User user;
//    private ExecutorService processThreadPool;

    public CommandProcessor(DatabaseManager databaseManager, Authorization authLib) {
        this.databaseManager = databaseManager;
        this.authLib = authLib;
        this.commandExecutor = new CommandExecutor(databaseManager, authLib);
//        processThreadPool = Executors.newFixedThreadPool(8);
    }

//    public CommandObject processCommand (CommandObject receivedCommandObject) throws ExecutionException, InterruptedException {
//        CommandObject commandObjectToSend = processThreadPool.execute(processCommandThread).get();
//
//    };

    public void init(DatabaseManager databaseManager, Authorization authorization) {
        this.databaseManager = databaseManager;
        this.authLib = authorization;
        this.authLib.setDatabaseManager(this.databaseManager);
    }

    public CommandObject processCommandThread(CommandObject receivedCommandObject){
        command = receivedCommandObject.getName();
        user = receivedCommandObject.getSender();

        if (!authLib.isAuthenticated(user)) {
            commandObjectToSend = new CommandObject(command);
            switch (command) {
                case "register":
                    commandObjectToSend.setMessage(
                            new RegisterCommand(command, user).execute()
                    );
                    return commandObjectToSend;

                case "login":
                    commandObjectToSend.setMessage(
                            new LoginCommand(command, user).execute()
                    );
                    return commandObjectToSend;
                default:
                    return CommandObjectCreator.createErrorObject("401", "User not authorized").setStringMessage("Unauthorised");
            }
        }

        receivedCommandObject.clearMessage();
        commandObjectToSend = new CommandObject(command);

        switch (command) {
            case "register":
                commandObjectToSend.setMessage(
                        new RegisterCommand(command, user).execute()
                );
                break;

            case "login":
                commandObjectToSend.setMessage(
                        new LoginCommand(command, user).execute()
                );
                break;

            case "logout":
                commandObjectToSend.setMessage(
                        new LogoutCommand(command, user).execute()
                );
                break;

            case "add":
                commandObjectToSend.setMessage(
                        new AddCommand(command, receivedCommandObject.getStudyGroup(), user.getUsername()).execute()
                );
                break;

            case "clear":
                commandObjectToSend.setMessage(
                        new ClearCommand(command, user).execute()
                );
                break;

            case "exit":
                commandObjectToSend.setMessage(
                        new ExitCommand(command).execute()
                );
                break;

            case "filter_by_group_admin":
                commandObjectToSend.setMessage(
                        new FilterByGroupAdminCommand(command, receivedCommandObject.getStringArgument()).execute()
                );
                break;

            case "filter_contains_name":
                commandObjectToSend.setMessage(
                        new FilterContainsNameCommand(command, receivedCommandObject.getStringArgument()).execute()
                );
                break;

            case "head":
                commandObjectToSend.setMessage(
                        new HeadCommand(command).execute()
                );
                break;

            case "help":
                commandObjectToSend.setMessage(
                        new HelpCommand(command).execute()
                );
                break;

            case "history":
                commandObjectToSend.setMessage(
                        new HistoryCommand(command, this.commandExecutor).execute()
                );
                break;

            case "info":
                commandObjectToSend.setMessage(
                        new InfoCommand(command, user).execute()
                );
                break;

            case "max_by_expelled_students":
                commandObjectToSend.setMessage(
                        new MaxByExpelledStudentsCommand(command).execute()
                );
                break;

            case "remove_by_id":
                commandObjectToSend.setMessage(
                        new RemoveByIdCommand(command, receivedCommandObject.getGroupID(), user).execute()
                );
                break;

            case "remove_first":
                commandObjectToSend.setMessage(
                        new RemoveFirstCommand(command, user).execute()
                );
                break;

            case "show":
                commandObjectToSend.setMessage(
                        new ShowCommand(command).execute()
                );
                break;

            case "update":
                commandObjectToSend.setMessage(
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
