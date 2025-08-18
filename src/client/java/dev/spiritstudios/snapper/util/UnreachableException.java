package dev.spiritstudios.snapper.util;

/**
 * An exception indicating that a piece of code should be unreachable.
 * Commonly used in switch statements to indicate that all possible cases have been handled.
 * <p>
 * If this exception is thrown, it should be considered a bug in the code.
 */
public class UnreachableException extends RuntimeException {
    public UnreachableException() {
        super("This error should be impossible. If you see this, please report it!");
    }

    public UnreachableException(String message) {
        super(message);
    }
}