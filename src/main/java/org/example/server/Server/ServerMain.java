package org.example.server.Server;

import java.io.IOException;

public class ServerMain {
    public static Server server;

    public static void main(String[] args) throws IOException {
        server = new Server("127.0.0.1", 4200);
        server.startListening();
    }
}
