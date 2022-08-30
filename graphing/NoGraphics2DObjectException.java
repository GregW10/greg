package greg.graphing;

public class NoGraphics2DObjectException extends Exception {
    public NoGraphics2DObjectException() {
        super("No Graphics2D object present.");
    }
    public NoGraphics2DObjectException(String message) {
        super(message);
    }
}