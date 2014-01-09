package codec;


public class InconsistentStateException extends RuntimeException {

    public InconsistentStateException(String message) {
        super(message);
    }
    public Exception getException() {
            return this;
    }
    public void printStackTrace() {
        super.printStackTrace();
    }
}
