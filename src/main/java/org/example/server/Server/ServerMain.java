package org.example.server.Server;

import java.io.IOException;
import java.sql.SQLException;

public class ServerMain {
    public static Server server;

    public static void main(String[] args) throws IOException, SQLException {
        server = new Server();
        server.startListening();
    }
}
