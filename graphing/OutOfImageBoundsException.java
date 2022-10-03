package greg.graphing;

public class OutOfImageBoundsException extends RuntimeException {
    OutOfImageBoundsException() {
        super("Text/Label component within Figure object does not fit entirely on image.");
    }
    OutOfImageBoundsException(String message) {
        super(message);
    }
}