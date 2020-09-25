package org.example.server.Server;

import java.nio.channels.DatagramChannel;

import org.example.server.dependencies.CommandProcessor;
import org.example.server.dependencies.CommandObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.io.IOException;
import java.nio.file.Paths;

@SuppressWarnings({"InfiniteLoopStatement", "FieldCanBeLocal"})
public class Server {
    private static final Logger logger = LogManager.getLogger();

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

    public Server(String host, int port) throws IOException {
        Configurator.initialize(null, "log4j2.xml");
        logger.info(String.format("Creating server at '%s:%s'...", host, port));

        this.host = host;
        this.port = port;

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
