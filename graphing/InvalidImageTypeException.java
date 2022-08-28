package greg.graphing;

public class InvalidImageTypeException extends Exception {
    public InvalidImageTypeException() {
        super("Invalid image type.");
    }
    public InvalidImageTypeException(String message) {
        super(message);
    }
}