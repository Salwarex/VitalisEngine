package ru.vitalis.engine.client;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClientInstance {
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);

    private static GameThread thread;

    public static void main(String[] args) {
        thread = new GameThread();

        Future<Integer> exitCode = pool.submit(thread);
        try{
            int i = exitCode.get();
            System.out.printf("Game stopped with code %d%n", i);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    public static GameThread getThread() {
        return thread;
    }
}