package org.example;

import calculator.server.CalculatorServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

/**
 * Hello world!
 *
 */

public class ConverteServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8082;

        Server server = ServerBuilder.forPort(port)
                .addService(new CurrencyConversionServiceImpl())
                .build();

        server.start();
        System.out.println("Server Started");
        System.out.println("Listening on port: " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Server Stopped");
        }));

        server.awaitTermination();
    }
}
