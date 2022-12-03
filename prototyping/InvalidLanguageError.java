package greg.prototyping;

public class InvalidLanguageError extends Exception {
    public InvalidLanguageError() {
        super("The language specified is invalid.");
    }
    public InvalidLanguageError(String msg) {
        super(msg);
    }
}