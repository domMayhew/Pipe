package ca.dominicmayhew.Pipe;

 /**
 * A simulated consumer that receives `Records` from a `Pipe`.
 * `Records` are marked with a processing stamp, the time that the `Receiver` requested access to the Pipe, and the time that access was completed.
 * Following access to the pipe, the `Receiver` busy waits for a prescribed interval to represent processing time.
 * Will run until the thread is interrupted.
 * 
 * @author Dominic Mayhew                      T00688238
 * @version February 18, 2022 // COMP 3411, Assignment 5
 */
class Receiver extends Worker<Record> {
    private static int receiverCount = 0;

    /**
     * Takes the pipe this `Receiver` will attach to.
     * @param pipe the pipe this `Receiver` will attach to.
     */
    public Receiver (Pipe<Record> pipe) {
        super(receiverCount++, pipe);
    }

    /**
     * The `Runnable` interface to be submitted to an `ExecutorService`.
     * `Records` are marked with a processing stamp, the time that the `Receiver` requested access to the Pipe, and the time that access was completed.
     * Following access to the pipe, the `Receiver` busy waits for a prescribed interval to represent processing time.
     * Will run until the thread is interrupted.
     */
    public void run() {
        while (true) {
            try {
                long getRequestTime = System.currentTimeMillis();
                Record nextRecord = pipe.get();
                nextRecord.setGetCompleteTime(System.currentTimeMillis());
                nextRecord.setGetRequestTime(getRequestTime);
                receive(nextRecord);
                PipeTest.results.add(nextRecord);
            } catch (ProcessingException pe) {
                printExceptionMessage(pe);
                return;
            } catch (InterruptedException ie) {
                return;
            }
        }
    }

    /**
     * Busy waits for a prescribed interval, then stamps the `Record`.
     * @param nextRecord the `Record` to stamp.
     * @throws ProcessingException if the `Record` already has a receiver stamp (i.e. if it has already been received from the buffer).
     */
    private void receive(Record nextRecord) throws ProcessingException {
        // Simulate a work period.
        spin();
        // Process Record.
        nextRecord.setReceiverStamp(getStamp());
        processedCount++;
    }

    /**
     * Returns a readable String representation of this `Receiver`.
     * @return a readable String representation of this `Receiver`.
     */
    public String toString() {
        return "Receiver #" + getId();
    }
}