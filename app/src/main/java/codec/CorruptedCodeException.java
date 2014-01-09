package codec;

public class CorruptedCodeException extends Exception {
    public CorruptedCodeException() {
        super();
    }

    public CorruptedCodeException(String message) {
        super(message);
    }
}
