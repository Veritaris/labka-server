package server.Server;

import java.io.IOException;

public class ServerMain {
    public static Server server;

    public static void main(String[] args) throws IOException, InterruptedException {
        server = new Server(args[0]);
        server.startListening();
    }
}
