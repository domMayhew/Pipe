package ca.dominicmayhew.Pipe;

/**
 * Abstract Superclass for `Creator` and `Receiver`. Provides some shared fields and methods.
 * 
 * @author Dominic Mayhew                      T00688238
 * @version February 18, 2022 // COMP 3411, Assignment 5
 */

public abstract class Worker<T> implements Runnable {
    private int workerId;
    protected int processedCount = 0;
    protected Pipe<T> pipe;

    /**
     * Takes a Worker ID # and the Pipe that this Worker is attached to.
     * Worker ID is provided by the subclass to allow for separate ID spaces for `Creator` and `Receiver` objects.
     * @param workerId the ID # of the Worker.
     * @param pipe the Pipe this Worker is attached to.
     */
    public Worker(int workerId, Pipe<T> pipe) {
        this.workerId = workerId;
        this.pipe = pipe;
    }

    /**
     * The Runnable interface to be submitted to an `ExecutorService`.
     */
    public abstract void run();
    /**
     * Used to mark each record with a unique stamp from the `Creator` and the `Processor`.
     * @return a unique String processing stamp representing the Worker and the number of items it has previously processed.
     */
    protected String getStamp() { return workerId + "." + processedCount; }
    /**
     * Returns the ID integer of this `Worker`.
     * @return the ID integer of this `Worker`.
     */
    protected int getId() { return workerId; }

    /**
     * Causes the worker to spin (busy wait) for an interval as prescribed by `Params`.
     */
    protected void spin() {
        int sleepTime = Params.getWorkInterval();
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < sleepTime)
            ;
    }

    /**
     * Provides uniform exception logging.
     * @param pe the `ProcessingException` thrown.
     */
    protected void printExceptionMessage(ProcessingException pe) {
        System.out.println(this + ": " + pe.getMessage() + "\n" + pe.rec + "\n\t\t" + this + " Shutting down.");
    }
}
