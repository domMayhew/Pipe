package ca.dominicmayhew.Pipe;
public class ProcessingException extends Exception {
    public Record rec;

    public ProcessingException (String message, Record rec) {
        super(message);
        this.rec = rec;
    }
}
