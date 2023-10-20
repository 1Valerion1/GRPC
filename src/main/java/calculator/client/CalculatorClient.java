package calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public final class CalculatorClient {
    private static void doSum(ManagedChannel channel) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the two numbers to sum");
        int firstNumber = scanner.nextInt();
        int secondNumber = scanner.nextInt();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
        SumResponse response = stub.sum(SumRequest.newBuilder().setFirstNumber(firstNumber).setSecondNumber(secondNumber).build());

        System.out.println("Sum " + firstNumber + " + " + secondNumber + " = " + response.getResult());
    }


    private static void doPrimes(ManagedChannel channel) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the number to find primes");
        int number = scanner.nextInt();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        stub.primes(PrimeRequest.newBuilder().setNumber(number).build()).forEachRemaining(response ->
                System.out.println(response.getPrimeFactor())
        );
    }


    private static void doAvg(ManagedChannel channel) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the numbers to calculate average (comma-separated please)");

        String input = scanner.nextLine();
        int[] numbers = Arrays.stream(input.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<AvgRequest> stream = stub.avg(new StreamObserver<AvgResponse>() {
            @Override
            public void onNext(AvgResponse response) {
                System.out.println("Avg = " + response.getResult());
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Arrays.stream(numbers).forEach(number ->
                stream.onNext(AvgRequest.newBuilder().setNumber(number).build())
        );

        stream.onCompleted();
        latch.await();
    }

    private static void doMax(ManagedChannel channel) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the numbers to find the maximum (comma-separated please)");

        String input = scanner.nextLine();
        int[] numbers = Arrays.stream(input.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<MaxRequest> stream = stub.max(new StreamObserver<MaxResponse>() {
            @Override
            public void onNext(MaxResponse response) {
                System.out.println("Max = " + response.getMax());
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Arrays.stream(numbers).forEach(number ->
                stream.onNext(MaxRequest.newBuilder().setNumber(number).build())
        );

        stream.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doSqrt(CalculatorServiceGrpc.CalculatorServiceBlockingStub stub) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the number for sqrt");
        int number = scanner.nextInt();

        try {
            SqrtResponse response = stub.sqrt(SqrtRequest.newBuilder().setNumber(number).build());
            System.out.println("Sqrt " + number + " = " + response.getResult());
        } catch (RuntimeException e) {
            if (e instanceof StatusRuntimeException) {
                StatusRuntimeException statusException = (StatusRuntimeException) e;
                if (statusException.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                    System.out.println("Cannot get the square root of a negative number");
                } else {
                    System.out.println("Got an unexpected exception for sqrt");
                    statusException.printStackTrace();
                }
            } else {
                System.out.println("Got an unexpected exception for sqrt");
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        Scanner input = new Scanner(System.in);


        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        try {
            boolean running = true;
            while (running) {
                System.out.println("Enter operation: ");
                String operation = input.nextLine();

                switch (operation) {
                    case "sum": {
                        doSum(channel);
                        break;
                    }
                    case "primes": {
                        doPrimes(channel);
                        break;
                    }
                    case "avg": {
                        doAvg(channel);
                        break;
                    }
                    case "max": {
                        doMax(channel);
                        break;
                    }
                    case "sqrt": {
                        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
                        doSqrt(stub);
                        break;
                    }
                    // Если пользователь вводит 'stop', закончите цикл.
                    case "stop": {
                        running = false;
                        break;
                    }
                    default: {
                        System.out.println("Keyword Invalid: " + operation);
                        break;
                    }
                }
            }
        } finally {
            System.out.println("Shutting Down");
            channel.shutdownNow();
        }
    }
}
