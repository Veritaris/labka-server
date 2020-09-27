package org.example.server.Server;

import org.example.server.CommandManager.CommandObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

@SuppressWarnings("FieldCanBeLocal")
public class Receiver {
    private static final Logger logger = LogManager.getLogger();
    private CommandObject receivedCommandObject;
    private final DatagramChannel datagramChannel;
    private SocketAddress clientAddress;
    private ByteArrayInputStream bais;
    private ObjectInputStream ois;

    private ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
    private byte[] message = new byte[8 * 1024];

    public Receiver(DatagramChannel datagramChannel) {
        this.datagramChannel = datagramChannel;
    }

    public CommandObject handleMessage() throws IOException {
        try {
            this.clientAddress = this.datagramChannel.receive(buffer);
            buffer.flip();
            message = new byte[buffer.limit()];
            buffer.get(message, 0, buffer.limit());
            bais = new ByteArrayInputStream(message);
            ois = new ObjectInputStream(bais);
            receivedCommandObject = (CommandObject) ois.readObject();
            buffer.clear();
            logger.info(String.format("Received packet: ip %s, message %s", this.getClientAddress(), receivedCommandObject.toString()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return receivedCommandObject;
    }

    public SocketAddress getClientAddress() {
        return this.clientAddress;
    }
}
