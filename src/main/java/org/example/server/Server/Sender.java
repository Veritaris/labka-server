package org.example.server.Server;

import org.example.server.dependencies.CommandObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

@SuppressWarnings("FieldCanBeLocal")
public class Sender {
    private static final Logger logger = LogManager.getLogger();
    private final DatagramChannel datagramChannel;
    private ByteArrayOutputStream baos;
    private ObjectOutputStream oos;

    public Sender(DatagramChannel datagramChannel) {
        this.datagramChannel = datagramChannel;
    }

    public void sendMessage(CommandObject commandObject, SocketAddress address) throws IOException {
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(commandObject);
        oos.flush();
        byte[] payload = baos.toByteArray();
        baos.flush();
        try {
            this.datagramChannel.send(ByteBuffer.wrap(payload), address);
            logger.info(String.format("Sending response to %s: %s", address, commandObject));
        } catch (IOException e) {
            System.out.printf("Something wrong with given message: %s\n", commandObject.toString());
        }
    }
}
