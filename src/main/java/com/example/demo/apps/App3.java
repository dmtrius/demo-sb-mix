package com.example.demo.apps;

import java.io.*;
import java.net.Socket;

public class App3 {
    public static void main(String[] args) {
        try (
                Socket socket = new Socket("localhost", 8888);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ) {
            out.println("Hello from client!");
            String line = reader.readLine();
            System.out.println("Received: " + line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
