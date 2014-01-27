package codec;


public class InconsistentStateException extends RuntimeException {

    public InconsistentStateException(String message) {
        super(message);
    }

    public void printStackTrace() {
        super.printStackTrace();
    }
}
