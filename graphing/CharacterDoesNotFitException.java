package greg.graphing;

public class CharacterDoesNotFitException extends RuntimeException {
    CharacterDoesNotFitException() {
        super("Character does not fit.");
    }
    CharacterDoesNotFitException(String message) {
        super(message);
    }
}