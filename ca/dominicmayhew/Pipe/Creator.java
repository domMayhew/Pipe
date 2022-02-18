package ca.dominicmayhew.Pipe;

 /**
 * A simulated producer that puts `Records` onto a `Pipe`.
 * `Records` are marked with a processing stamp, the time that the `Creator` requested access to the Pipe, and the time that access was completed.
 * During creation of the record, prior to requesting access to the pipe, the `Creator` busy waits for a prescribed interval to represent processing time.
 * Will run until the thread is interrupted.
 * 
 * @author Dominic Mayhew                      T00688238
 * @version February 18, 2022 // COMP 3411, Assignment 5
 */

class Creator extends Worker<Record> {
    private static int creatorCount = 0;

    /**
     * Takes the pipe that this `Creator` will attach to.
     * @param pipe the pipe that this `Creator` will attach to.
     */
    public Creator (Pipe<Record> pipe) {
        super(creatorCount++, pipe);
    }

    /**
     * The `Runnable` interface to be submitted to an `ExecutorService`.
     * Creating a `Record` involves busy waiting for a prescribed interval to simulate processing time.
     * `Records` are marked with a processing stamp, the time that the `Creator` requested access to the pipe, and the time that access was completed.
     * Will run until the thread is interrupted.
     */
    @Override
    public void run() {
        while (true) {
            Record nextRecord;
            try {
                nextRecord = create();
                nextRecord.setPutRequestTime(System.currentTimeMillis());
                pipe.put(nextRecord);
                nextRecord.setPutCompleteTime(System.currentTimeMillis());
            } catch (ProcessingException pe) {
                printExceptionMessage(pe);
                return;
            } catch (InterruptedException ie) {
                return;
            }
        }
    }

    /**
     * Busy waits for a prescribed interval, then creates a record with a unique creation stamp.
     * @return the created `Record`.
     */
    private Record create() {
        // Simulate work period.
        spin();
        // Create new record.
        Record nextRecord = new Record(getStamp());
        processedCount++;
        return nextRecord;
    }

    /**
     * Returns a readable string representing this `Creator`.
     * @reutrn a readable string representing this `Creator`.
     */
    public String toString() {
        return "Creator #" + getId();
    }
}