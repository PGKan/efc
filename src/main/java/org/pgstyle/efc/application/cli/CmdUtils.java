package org.pgstyle.efc.application.cli;

import java.io.Console;
import java.io.PrintStream;
import java.util.Optional;

/**
 * The command line utilities class provides methods for manipulating the
 * console in command line application.
 *
 * @version 1.0
 * @author PGKan
 */
public final class CmdUtils {

    /**
     * Reads one line out of the standard input. The read is blocking.
     *
     * @return the line read from the standard input, with the last newline
     *         character removed; or {@code null} if the standard input is not
     *         available
     */
    public static String stdin() {
        return Optional.ofNullable(System.console()).map(Console::readLine).orElse(null);
    }

    /**
     * Returns the standard out stream.
     *
     * @return the standard out stream
     */
    public static PrintStream stdout() {
        return System.out;
    }

    /**
     * Returns the standard error stream.
     *
     * @return the standard error stream
     */
    public static PrintStream stderr() {
        return System.err;
    }

    /**
     * Outputs the data onto the print stream.
     *
     * @param printStream the print stream to be printed on
     * @param data the data to be printed
     */
    private static void output(PrintStream printStream, Object data) {
        printStream.print(data);
    }

    /**
     * Prints the object onto standard output.
     *
     * @param object the object to be printed
     */
    public static void stdout(Object object) {
        CmdUtils.output(System.out, object);
    }

    /**
     * Prints a formatted string onto standard output.
     *
     * @param format the string format
     * @param objects the objects to be formatted
     */
    public static void stdout(String format, Object... objects) {
        CmdUtils.stdout(String.format(format, objects));
    }

    /**
     * Prints the object onto standard error output.
     *
     * @param object the object to be printed
     */
    public static void stderr(Object object) {
        CmdUtils.output(System.err, object);
    }

    /**
     * Prints a formatted string onto standard error output.
     *
     * @param format the string format
     * @param objects the objects to be formatted
     */
    public static void stderr(String format, Object... objects) {
        CmdUtils.stderr(String.format(format, objects));
    }

    /** Unnewable @throws UnsupportedOperationException always */
    private CmdUtils() {
        throw new UnsupportedOperationException("unnewable");
    }

}
