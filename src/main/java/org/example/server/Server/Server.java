package org.example.server.Server;

import java.io.*;
import java.nio.channels.DatagramChannel;

import org.example.server.Authentification.UserAuthentication;
import org.example.server.CommandManager.CommandObjectCreator;
import org.example.server.CommandManager.CommandProcessor;
import org.example.server.CommandManager.CommandObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.example.server.DatabaseManager.DatabaseManager;
import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;

@SuppressWarnings({"InfiniteLoopStatement", "FieldCanBeLocal"})
public class Server {
    private static final Logger logger = LogManager.getLogger();

    private final Properties serverConfig = new Properties();

    private final String configPath = "server.properties";
    private UserAuthentication authLib;

    private InputStream inputStream = null;

    public String host;
    public int port;

    private final DatagramChannel datagramChannel;
    private final SocketAddress serverAddress;
    private final DatagramSocket datagramSocket;

    private final Receiver messageReceiver;
    private final Sender messageSender;

    private CommandObject receivedCommandObject;
    private CommandObject commandObjectToSend;

    private final String dbUsername;
    private final String dbPassword;
    private final String dbURL;
    private final String dbName;
    private final String dbSchemaName;

    private final DatabaseManager databaseManager;
    private final CommandProcessor commandProcessor = new CommandProcessor();

    public Server() throws IOException, SQLException {
        Configurator.initialize(null, "log4j2.xml");

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
        this.dbName = serverConfig.getProperty("database_name");
        this.dbSchemaName = serverConfig.getProperty("schema_name");

        this.authLib = new UserAuthentication();

        logger.info(String.format("Creating server at '%s:%s'...", this.host, this.port));

        logger.info(String.format(
                "Connecting to database at '%s' with credentials: username='%s', password='%s'...",
                this.dbURL, this.dbUsername, new String(new char[this.dbPassword.length()]).replace('\0', '*')
        ));
        databaseManager = new DatabaseManager(this.dbURL, this.dbUsername, this.dbPassword, this.dbSchemaName);
        databaseManager.init();
        logger.info("Connected.");


        this.datagramChannel = DatagramChannel.open();
        this.serverAddress = new InetSocketAddress(this.host, this.port);
        this.datagramSocket = this.datagramChannel.socket();
        this.datagramSocket.bind(serverAddress);

        this.commandProcessor.setCommandExecutor(Paths.get(".").toAbsolutePath().normalize().toString() + String.format("/%s", "users.json"));
        this.commandProcessor.setAuthLib(this.authLib);
        this.commandProcessor.setDatabaseManager(this.databaseManager);

        logger.info("Server created!");

        this.messageReceiver = new Receiver(this.datagramChannel);
        this.messageSender = new Sender(this.datagramChannel);
    }

    public void startListening() throws IOException {
        logger.info(String.format("Server at '%s:%s' started listening", this.host, this.port));

        while (true) {
            receivedCommandObject = this.messageReceiver.handleMessage();

            commandObjectToSend = commandProcessor.processCommand(receivedCommandObject);

            this.messageSender.sendMessage(commandObjectToSend, this.messageReceiver.getClientAddress());
        }
    }
}
