package server.Server;

import java.io.*;
import java.nio.channels.DatagramChannel;

import dependencies.CommandManager.CommandObjectCreator;
import server.Authorization.Authorization;
import dependencies.CommandManager.CommandObject;
import dependencies.CommandManager.CommandProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import server.DatabaseManager.DatabaseManager;

import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Properties;
import java.util.concurrent.*;

@SuppressWarnings({"InfiniteLoopStatement", "FieldCanBeLocal"})
public class Server {
    private static final Logger logger = LogManager.getLogger();

    private final Properties serverConfig = new Properties();

    private final String configPath;
    private Authorization authLib;

    private InputStream inputStream = null;

    public String host;
    public int port;

    private final DatagramChannel datagramChannel;
    private final SocketAddress serverAddress;
    private final DatagramSocket datagramSocket;

    private final Receiver messageReceiver;
    private final Sender messageSender;

    private CommandObject commandObjectToSend;
    private final ExecutorService receiverThreadPool;
    private final ExecutorService processThreadPool;

    private final String dbUsername;
    private final String dbPassword;
    private final String dbURL;
    private final String dbDriver;
    private final String dbName;
    private final String dbSchemaName;

    private final DatabaseManager databaseManager;
    private final CommandProcessor commandProcessor;

    public Server(String propertyPath) throws IOException {
        Configurator.initialize(null, "log4j2.xml");
        this.configPath = propertyPath;

        try {
            inputStream = new FileInputStream(this.configPath);

        } catch (FileNotFoundException e) {
            logger.error("Server cannot run without config file, please, create config.properties");
            System.exit(1);
        }

        try {
            serverConfig.load(inputStream);
        } catch (IOException e) {
            logger.error("Something went wrong:");
            logger.error(e.getStackTrace());
            System.exit(1);
        }

        this.host = serverConfig.getProperty("server_url");
        try {
            this.port = Integer.parseInt(serverConfig.getProperty("server_port"));
        } catch (NumberFormatException e) {
            logger.error(String.format("Server port must be int, got '%s'", serverConfig.getProperty("server_port")));
        }

        this.dbUsername = serverConfig.getProperty("database_user");
        this.dbPassword = serverConfig.getProperty("database_password");
        this.dbURL = serverConfig.getProperty("database_url");
        this.dbDriver = serverConfig.getProperty("database_driver");
        this.dbName = serverConfig.getProperty("database_name");
        this.dbSchemaName = serverConfig.getProperty("schema_name");

        logger.info(String.format("Creating server at '%s:%s'...", this.host, this.port));

        logger.info(String.format(
                "Connecting to database at '%s' with credentials: username='%s', password='%s'...",
                this.dbURL, this.dbUsername, new String(new char[this.dbPassword.length()]).replace('\0', '*')
        ));
        databaseManager = new DatabaseManager(this.dbDriver, this.dbURL, this.dbName, this.dbUsername, this.dbPassword, this.dbSchemaName);
        databaseManager.init();
        logger.info("Connected.");

        this.datagramChannel = DatagramChannel.open();
        this.serverAddress = new InetSocketAddress(this.host, this.port);
        this.datagramSocket = this.datagramChannel.socket();
        this.datagramSocket.bind(serverAddress);

        this.authLib = new Authorization(this.databaseManager);
        this.commandProcessor = new CommandProcessor(this.databaseManager, this.authLib);

        logger.info("Server created!");

        receiverThreadPool = Executors.newFixedThreadPool(8);
        processThreadPool = Executors.newFixedThreadPool(8);

        this.messageReceiver = new Receiver(this.datagramChannel);
        this.messageSender = new Sender(this.datagramChannel);
    }

    public void startListening() throws InterruptedException {
        logger.info(String.format("Server at '%s:%s' started listening", this.host, this.port));

        while (true) {
            Future<CommandObject> receivedCommandObject = receiverThreadPool.submit(this.messageReceiver.handleMessage);

            try {
                commandObjectToSend = processThreadPool.submit(() -> commandProcessor.processCommandThread(receivedCommandObject.get())).get();
            } catch (ExecutionException e) {
                commandObjectToSend = CommandObjectCreator.createErrorObject("500", "Something went wrong. Please, retry later");
                logger.error(e.getMessage());
                for (StackTraceElement stacktraceLine : e.getStackTrace()) {
                    logger.error(String.format("\t%s",stacktraceLine));
                }
            }

            this.messageSender.sendMessage(commandObjectToSend, this.messageReceiver.getClientAddress());
        }
    }
}
