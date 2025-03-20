package com.example.demo.apps;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class App2 {
    public static void main(String[] args) throws IOException {
        try (
                ServerSocket server = new ServerSocket(8888);
                Socket socket = server.accept();
                PrintWriter w = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                ) {
            String line = in.readLine();
            System.out.println(line);
            w.println("Hello from server! I received: " + line);
        }
    }
}
