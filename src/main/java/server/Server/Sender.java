package server.Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dependencies.CommandManager.CommandObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinTask;

@SuppressWarnings("FieldCanBeLocal")
public class Sender {
    private static final Logger logger = LogManager.getLogger();
    private final ExecutorService senderThreadPool;
    private DatagramChannel datagramChannel;
    private ByteArrayOutputStream baos;
    private ObjectOutputStream oos;
    private CommandObject commandObject;
    private SocketAddress address;

    public Sender(DatagramChannel datagramChannel) {
        this.datagramChannel = datagramChannel;
        this.senderThreadPool = Executors.newWorkStealingPool(8);

    }

    public void sendMessage(CommandObject commandObject, SocketAddress address) {
        this.commandObject = commandObject;
        this.address = address;
        this.senderThreadPool.execute(executeSendMessage);
    }

    private final Runnable executeSendMessage = () -> {
        baos = new ByteArrayOutputStream();
        byte[] payload = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(this.commandObject);
            oos.flush();
            payload = baos.toByteArray();
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert payload != null;
            this.datagramChannel.send(ByteBuffer.wrap(payload), this.address);
            logger.info(String.format("Sending response to %s: %s", this.address, commandObject).replace("\\n", ""));
        } catch (IOException e) {
            System.out.printf("Something wrong with given message: %s\n", commandObject.toString());
        }
    };
}
