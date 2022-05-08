package org.pgstyle.efc.application;

/**
 * An {@code ApplicationException} indicates an error during the execution of
 * the {@code MinecraftFlightComputer} application.
 *
 * @since rst-2
 * @version rst-2.0
 * @author PGKan
 */
public final class ApplicationException extends RuntimeException {

    /**
     * Initialise the exception with no message and cause.
     */
    public ApplicationException() {
        this(null);
    }

    /**
     * Initialise the exception with a message.
     *
     * @param message the detail message
     */
    public ApplicationException(String message) {
        this(message, null);
    }

    /**
     * Initialise the exception with a message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
