package ca.dominicmayhew.Pipe;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs a test on a producer/consumer style shared memory pipeline and logs results.
 * The pipeline must have `put()` and `get()` methods.
 * This simulation uses the names `Creator` and `Receiver` to avoid namespace conflict with the functional interface `java.util.function.Consumer`.
 * `Params` is used to set the parameters for a given test run.
 * 
 * @author Dominic Mayhew                      T00688238
 * @version February 19, 2022 // COMP 3411, Assignment 5
 */

public class PipeTest {
    private static Pipe<Record> pipe = new Pipe<Record>(Params.BUF_SIZE, Pipe.AccessMode.semaphore);
    private static long baseTime = 0; // Assigned a value at start of program execution. Used to make time values more readable.
    private static long executionTime = 0; // Total time test was executing.
    public static ConcurrentLinkedQueue<Record> results = new ConcurrentLinkedQueue<>(); // Stores the produced records for generating results.    
    /**
     * Runs the test and logs results.
     * @param args Command line arguments -- unused.
     */
    public static void main(String args[]) {
        // Set up.
        ExecutorService pool = Executors.newFixedThreadPool(Params.numCreators + Params.numReceivers);
        baseTime = System.currentTimeMillis();
        Record.baseTime = baseTime;

        // Submit Creators to pool.
        for (int i = 0; i < Params.numCreators; i++) {
            pool.submit(new Creator(pipe));
        }
        // Give the Creators a head start.
        try {
            Thread.sleep(Params.WORK_INTERVAL);
        } catch (InterruptedException ie) {
            System.out.println("Main was interrupted before creating Receivers.");
        }
        // Submit Receivers to pool.
        for (int i = 0; i < Params.numReceivers; i++) {
            pool.submit(new Receiver(pipe));
        }

        // Shut down and print results.
        try {
            Thread.sleep(Params.TIME_OUT);
            pool.shutdownNow();
            executionTime = System.currentTimeMillis() - baseTime;
        } catch (InterruptedException ie) {
            System.out.println("Main was interrupted while sleeping during the test.\n" + ie);
        }
        
        // Collect results.
        long count = 0, putRequestTime = 0, putCompleteTime = 0, getRequestTime = 0, getCompleteTime = 0;
        for (Record rec : results) {
            putRequestTime += rec.getPutRequestTime();
            putCompleteTime += rec.getPutCompleteTime();
            getRequestTime += rec.getGetRequestTime();
            getCompleteTime += rec.getGetCompleteTime();
            count++;
        }

        // Print results.
        if (count != 0) {
            System.out.println("Test complete. Here are the results.");
            System.out.println("Parameters:\n\t#Creators: " + Params.numCreators + "\t#Receivers: " + Params.numReceivers +
                "\n\tBuffer Size: " + Params.BUF_SIZE + "\tWork Inteveral: " + Params.WORK_INTERVAL);
            System.out.println(String.format("%d records processed in %.2f seconds.", count, ((double)executionTime) / 1000));
            System.out.println(String.format("Throughput: %.2f Record/s.", ((double)count) / executionTime * 1000));
            System.out.println(String.format("Turnaround: %.2f ms/Record.", (double)(getCompleteTime - putRequestTime) / count));
            System.out.println(String.format("Creator wait time: %.2f ms/Record", (double)(putCompleteTime - putRequestTime) / count));
            System.out.println(String.format("Receiver waiting time: %.2f ms/Record", (double)(getCompleteTime - getRequestTime) / count));
            System.out.println(String.format("Time spent in buffer: %.2f ms/Record", (double)(getCompleteTime - putCompleteTime) / count));
        } else {
            System.out.println("There are no results. :( Something must have gone wrong.");
        }
    }
}

/**
 * Stores parameters used by PipeTest. Can be reset by PipeTest to run multiple variations.
 */
class Params {
    protected static int numCreators = 10;
    protected static int numReceivers = 10;
    protected static int BUF_SIZE = 20;
    protected static int WORK_INTERVAL = 20; // Used by Workers to simulate a processing delay.
    protected static int TIME_OUT = 10000; // How long to run the test.

    /**
     * Returns a random `int` between (WORK_INTERVAL / 2) and (WORK_INTERVAL + WORK_INTERVAL / 2).
     * Used by Workers to simulate a processing period.
     * @return a random integer centered on WORK_INTERVAL.
     */
    protected static int getWorkInterval() {
        Random rand = new Random();
        return rand.nextInt(Params.WORK_INTERVAL) + Params.WORK_INTERVAL / 2;
    }
}