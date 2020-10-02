package server.Server;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ServerMain {
    public static Server server;

    public static void main(String[] args) throws IOException, InterruptedException {
        server = new Server();
        server.startListening();
    }
}
