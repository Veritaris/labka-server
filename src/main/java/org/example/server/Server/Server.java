package org.example.server.Server;

import java.io.*;
import java.nio.channels.DatagramChannel;

import org.example.server.CommandManager.CommandProcessor;
import org.example.server.CommandManager.CommandObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.file.Paths;
import java.util.Properties;

@SuppressWarnings({"InfiniteLoopStatement", "FieldCanBeLocal"})
public class Server {
    private static final Logger logger = LogManager.getLogger();

    private Properties serverConfig = new Properties();

    private final String configPath = "server.properties";

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

    private final CommandProcessor commandProcessor = new CommandProcessor();

    public Server() throws IOException {
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

        logger.info(String.format("Creating server at '%s:%s'...", this.host, this.port));

        this.datagramChannel = DatagramChannel.open();
        this.serverAddress = new InetSocketAddress(this.host, this.port);
        this.datagramSocket = this.datagramChannel.socket();
        this.datagramSocket.bind(serverAddress);

        this.commandProcessor.setCommandExecutor(Paths.get(".").toAbsolutePath().normalize().toString() + String.format("/%s", "users.json"));

        logger.info("Server created!");

        this.messageReceiver = new Receiver(this.datagramChannel);
        this.messageSender = new Sender(this.datagramChannel);
    }

    public void startListening() throws IOException {
        logger.info(String.format("Server at '%s:%s' started listening\n", this.host, this.port));

        while (true) {
            commandObjectToSend = null;
            receivedCommandObject = null;
            receivedCommandObject = this.messageReceiver.handleMessage();

            commandObjectToSend = commandProcessor.processCommand(receivedCommandObject);

            this.messageSender.sendMessage(commandObjectToSend, this.messageReceiver.getClientAddress());
        }
    }
}
