package greg.graphing;

public class CharacterDoesNotFitException extends Exception {
    CharacterDoesNotFitException() {
        super("Character does not fit.");
    }
    CharacterDoesNotFitException(String message) {
        super(message);
    }
}