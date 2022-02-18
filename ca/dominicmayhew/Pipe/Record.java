package ca.dominicmayhew.Pipe;

 /**
 * The object used by Workers to simulate use of a producer/consumer pipeline.
 * Includes fields for ID stamps from the `Creator` and `Receiver`, and time stamps for when each `Worker` requested access to the `Pipe` and when that access was completed.
 * Throws `ProcessingException`s if an attempt is made to modify a field more than once.
 * 
 * @author Dominic Mayhew                      T00688238
 * @version February 18, 2022 // COMP 3411, Assignment 5
 */

public class Record {    
    private String creatorStamp;
    private String receiverStamp = "";

    private long putRequestTime = -1;
    private long putCompleteTime = -1;
    private long getRequestTime = -1;
    private long getCompleteTime = -1;

    public static long baseTime = 0; // Set by PipeTest.main().

    /**
     * Takes only a creator ID stamp. All other fields set by setter methods after instantiation.
     * @param creatorStamp the ID stamp of the `Creator` of this object.
     */
    public Record(String creatorStamp) {
        this.creatorStamp = creatorStamp;
    }

    // IDs
    public String getCreatorStamp() { return creatorStamp; }
    public String getReceiverStamp() { return receiverStamp; }
    public void setReceiverStamp(String receiverStamp) throws ProcessingException {
        if (this.receiverStamp.equals("")) {
            this.receiverStamp = receiverStamp;
        } else {
            throw new ProcessingException("Attempted to set receiverStamp on a record more than once.", this);
        }
    }

    // Time getters
    public long getPutRequestTime() { return putRequestTime; }
    public long getPutCompleteTime() { return putCompleteTime; }
    public long getGetRequestTime() { return getRequestTime; }
    public long getGetCompleteTime() { return getCompleteTime; }

    // Time setters.
    public void setPutRequestTime(long time) throws ProcessingException {
        if (putCompleteTime != -1) {
            throw new ProcessingException("Attempted to set putRequestTime on a record more than once.", this);
        } else {
            putRequestTime = time;
        }
    }
    public void setPutCompleteTime(long time) throws ProcessingException {
        if (putCompleteTime != -1) {
            throw new ProcessingException("Attempted to set putCompleteTime more than once on a record.", this);
        } else {
            putCompleteTime = time;
        }
    }
    public void setGetRequestTime(long time) throws ProcessingException {
        if (getRequestTime != -1) {
            throw new ProcessingException("Attempted to set getRequestTime on a record more than once.", this);
        } else {
            getRequestTime = time;
        }
    }
    public void setGetCompleteTime(long time) throws ProcessingException {
        if (getCompleteTime != -1) {
            throw new ProcessingException("Attempted to set getCompleteTime on a record more than once.", this);
        } else {
            getCompleteTime = time;
        }
    }

    public String toString() {
        return
            "\tCreator:\t" + getCreatorStamp() +
            "\tPut Request:\t" + (getPutRequestTime() - baseTime) +
            "\tGet Request:\t" + (getGetRequestTime() - baseTime) +
            "\n\tReceiver:\t" + getReceiverStamp() + 
            "\tPut complete:\t" + (getPutCompleteTime() - baseTime) +
            "\tGet complete:\t" + (getGetCompleteTime() - baseTime);
    }

    public boolean equals(Record other) {
        return (creatorStamp.equals(other.getCreatorStamp()) && receiverStamp.equals(other.getReceiverStamp()));
    }
}
