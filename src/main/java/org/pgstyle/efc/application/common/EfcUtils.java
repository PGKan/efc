package org.pgstyle.efc.application.common;

import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@code RstUtils} class contains some useful methods for the random string
 * tools.
 *
 * @since rst-2
 * @version rst-2.0
 * @author PGKan
 */
public final class EfcUtils {

    /** The system-dependent line separator. */
    public static final String NEWLINE = System.lineSeparator();
    /** String constant of backward slash. */
    public static final String BACK_SLASH = "\\";
    /** The list of escapable RST control characters. */
    public static final String ESCAPABLE = "\\.:;";
    /** The list of RST control separators. */
    public static final String SEPARATORS = ":;";

    /**
     * Splits the argument {@code string} along characters in the argument
     * {@code delimiters}.
     *
     * @param string the string to be splitted
     * @param delimiters the delimiters for splitting the string
     * @return a array of {@code String} contains each segments of the string
     * @throws IllegalArgumentException
     *         if the argument {@code delimiters} contains invalid delimiter
     * @throws NullPointerException
     *         if the argument {@code string} or {@code delimiters} is
     *         {@code null}
     */
    public static String[] safeSplit(String string, char[] delimiters) {
        Objects.requireNonNull(string, "string == null");
        Objects.requireNonNull(delimiters, "delimiters == null");
        String dels = String.valueOf(delimiters);
        if (dels.contains(EfcUtils.BACK_SLASH)) {
            // since backward slash can escape a delimiter,
            // the backward slash itself is forbidden as a delimiter
            throw new IllegalArgumentException("invalid delimiter");
        }

        List<String> splitted = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            Character current = string.charAt(i);
            if (current.equals('\\')) {
                // escaped character, do not consume the backward slash
                builder.append(current).append(string.charAt(++i));
            }
            else if (dels.chars().anyMatch(Integer.valueOf(string.charAt(i))::equals)) {
                // matched delimiter, store the string segment and reset the buffer
                splitted.add(builder.toString());
                builder.setLength(0);
            }
            else {
                builder.append(current);
            }
        }
        splitted.add(builder.toString());
        return splitted.size() == 1 && splitted.get(0).isEmpty() ?
               new String[0] : splitted.stream().toArray(String[]::new);
    }

    /**
     * Converts the given object into its string representation within a pair of
     * double quote ({@code "}) if the object isn't {@code null}.
     *
     * @param object the object
     * @return the quoted string representation of the object if the object
     *         isn't {@code null}; or string literal {@code null} if the
     *         object is {@code null}.
     */
    public static String toQuotedString(Object object) {
        return Optional.ofNullable(object).map(Object::toString).map(s -> "\"" + s + "\"").orElse("null");
    }

    /**
     * Creates a stream of characters from the string
     *
     * @param string the characters source
     * @return a newly created stream of characters
     */
    public static Stream<Character> charStream(String string) {
        return string.chars().mapToObj(i -> (char) i);
    }

    /**
     * Reads resource file content into a string.
     *
     * @param path the path of the resource
     * @return the file content in a string
     * @throws IOException
     *         if the resource file is not readable
     * @throws IllegalArgumentException
     *         if the resource file do not exist
     * @throws NullPointerException
     *         if the argument {@code path} is {@code null}
     */
    public static String loadResourceAsString(String path) throws IOException {
        Objects.requireNonNull(path, "path == null");
        return new String(EfcUtils.read(EfcUtils.class.getResourceAsStream(path)), StandardCharsets.UTF_8);
    }

    /**
     * Merge the arguments {@code array1} and {@code array2} together with
     * {@code array1}
     * in the front.
     *
     * @param array1
     *        the first array
     * @param array2
     *        the second array
     * @return a new array with the merged data stored
     * @throws NullPointerException
     *         if the argument {@code array1} or {@code array2} is
     *         {@code null}.
     */
    public static byte[] merge(byte[] array1, byte[] array2) {
        Objects.requireNonNull(array1, "array1 == null");
        Objects.requireNonNull(array2, "array2 == null");
        byte[] array = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, array, 0, array1.length);
        System.arraycopy(array2, 0, array, array1.length, array2.length);
        return array;
    }

    /**
     * Get a partition of the argument {@code array} with specified start index
     * and length. Without extending the size of array if the length is greater
     * than the count of available elements.
     *
     * @param array
     *        the array
     * @param start
     *        the starting index
     * @param length
     *        the targeted count of element
     * @return a new array with the specified element stored
     * @throws IllegalArgumentException
     *         if the argument {@code array} is {@code null}; or argument
     *         {@code start} or {@code length} is negative.
     */
    public static byte[] partition(byte[] array, int start, int length) {
        return EfcUtils.partition(array, start, length, false);
    }

    /**
     * Get a partition of the argument {@code array} with specified start index
     * and length.
     *
     * @param array
     *        the array
     * @param start
     *        the starting index
     * @param length
     *        the targeted count of element
     * @param padding
     *        the length of return array is strictly defined by the argument
     *        {@code length} if set to {@code true}
     * @return a new array with the specified element stored
     * @throws IllegalArgumentException
     *         if the argument {@code array} is {@code null}; or argument
     *         {@code start} or {@code length} is negative.
     */
    public static byte[] partition(byte[] array, int start, int length, boolean padding) {
        Objects.requireNonNull(array, "argument array must not null");
        return padding ? Arrays.copyOfRange(array, start, start + length)
            : Arrays.copyOfRange(array, start, Math.min(start + length, array.length));
    }

    /**
     * Heavy buffered {@link InputStream} read, read all available content in
     * an {@code InputStream} into a byte array. This method is faster and more
     * robust than {@link InputStream#read(byte[])}. Using the default byte
     * buffer size of 2 <sup>16</sup>.
     *
     * @param inputStream
     *        the source {@code InputStream}
     * @return a byte array contains all data available in the
     *         {@code InputStream}
     * @throws IOException
     *         if the input stream is not readable
     * @throws IllegalArgumentException
     *         if the argument inputStream is null
     */
    public static byte[] read(InputStream inputStream) throws IOException {
        return EfcUtils.read(inputStream, Short.MAX_VALUE + 1 << 1);
    }

    /**
     * Read the content of an {@link InputStream} into the buffer array. This
     * method work the same way as {@link InputStream#read(byte[])} but more
     * robust with concurrent stream reading.
     *
     * @param inputStream
     *        the source {@code InputStream}
     * @param buffer
     *        the destination of read content
     * @return the count of byte read from the {@code InputStream}
     * @throws IOException
     *         if an I/O error occurs during reading the input stream
     */
    public static int read(InputStream inputStream, byte[] buffer) throws IOException {
        int i = 0;
        inputStream = EfcUtils.bufferedWrapper(inputStream);
        int b;
        while (i < buffer.length && (b = inputStream.read()) != -1) {
            buffer[i++] = (byte) b;
        }
        return i;
    }

    /**
     * Heavy buffered {@link InputStream} read, read all available content in
     * an {@code InputStream} into a byte array. This method is faster and more
     * robust than {@link InputStream#read(byte[])}. The size of byte buffer can
     * affect the speed, higher the buffer size faster the read speed up to the
     * size of available content length. The default buffer size is 2
     * <sup>16</sup>. This is recommended to use a byte buffer size larger than
     * 1024.
     *
     * @param inputStream
     *        the source {@code InputStream}
     * @param bufferSize
     *        the size of byte buffer
     * @return a byte array contains all data available in the
     *         {@code InputStream}
     * @throws IOException
     *         if the input stream is readable
     * @throws IllegalArgumentException
     *         if the argument inputStream is {@code null}; or bufferSize is
     *         negative or zero.
     */
    public static byte[] read(InputStream inputStream, int bufferSize) throws IOException {
        byte[] bytes = new byte[0];
        byte[] buffer = new byte[bufferSize];
        inputStream = EfcUtils.bufferedWrapper(inputStream);
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            bytes = EfcUtils.merge(bytes, EfcUtils.partition(buffer, 0, length));
        }
        return bytes;
    }

    /**
     * Writes string into the print stream with line formatting.
     *
     * @param printStream the stream to be written
     * @param output the string the be written
     * @return actual count of bytes written
     */
    public static long write(PrintStream printStream, String output) {
        final int step = 64;
        int iteration = 0;
        for (int i = 0; i < output.length(); i += step, iteration++) {
            printStream.println(output.substring(i, Math.min(i + step, output.length())));
        }
        return iteration * System.lineSeparator().length() + output.length();
    }

    /**
     * Opens a file as a print stream.
     *
     * @param file the file to be open
     * @return a print stream
     * @throws IOException if any I/O error occurred
     */
    public static PrintStream openFile(File file) throws IOException {
        return new PrintStream(new FileOutputStream(file), true, "utf-8");
    }

    /**
     * Tests the unavailability of GUI.
     *
     * @return {@code true} if GUI is unavailable; or
     *         {@code false} otherwise
     */
    public static boolean headless() {
        return GraphicsEnvironment.isHeadless();
    }

    /**
     * Tests the availability of {@code System.in}.
     *
     * @return {@code true} if {@code System.in} is available; or
     *         {@code false} otherwise
     */
    public static boolean sysin() {
        try {
            ((BufferedInputStream) System.in).available();
        } catch (IOException e) {
            // no system is available
            return false;
        }
        return true;
    }

    /**
     * Wrap the {@code InputStream} in a {@code BufferedInputStream} if needed.
     *
     * @param inputStream
     *        the origin {@code InputStream}
     * @return a wrapped {@code BufferedInputStream}; or {@code inputStream}
     *         itself if it has already been a {@code BufferedInputStream}.
     */
    private static InputStream bufferedWrapper(InputStream inputStream) {
        if (inputStream instanceof BufferedInputStream) {
            return inputStream;
        }
        return new BufferedInputStream(inputStream);
    }

    /**
     * <p>
     * Creates a simple message of the given throwable object.
     * </p>
     * <p>
     * The implementation of this method is as below:
     * </p>
     * <pre><code>throwable.getClass().getSimpleName() + ":" + throwable.getMessage()</code></pre>
     *
     * @param throwable the throwable object
     * @return the simple message of the throwable object
     * @throws NullPointerException
     *         if the argument {@code throwable} is {@code null}
     */
    public static String messageOf(Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable == null");
        return throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
    }

    /**
     * Creates a stack trace of the given throwable object.
     *
     * @param throwable the throwable object
     * @return the stack trace of the throwable object
     * @throws NullPointerException
     *         if the argument {@code throwable} is {@code null}
     */
    public static String stackTraceOf(Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable == null");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        throwable.printStackTrace(ps);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Dissects the raw weight descriptor.
     *
     * @param raw the raw weight descriptor
     * @return a list of weight entries
     */
    public static final List<Entry<String, Integer>> dissect(String raw) {
        List<Entry<String, Integer>> list = new ArrayList<>();
        for (String statement : EfcUtils.safeSplit(raw, new char[]{ ';' })) {
            if (statement.isEmpty()) {
                continue;
            }
            if (!statement.contains(":")) {
                list.add(new SimpleEntry<>(statement, Integer.MIN_VALUE));
            }
            else {
                String weight = statement.substring(0, statement.indexOf(":")).trim();
                String characters = statement.substring(statement.indexOf(":") + 1).trim();
                try {
                    list.add(new SimpleEntry<>(characters, Integer.parseInt(weight)));
                }
                catch (NumberFormatException e) {
                    list.add(new SimpleEntry<>(statement, 0));
                }
            }
        }
        return list;
    }

    /**
     * Normalises a weight entry. All continue characters will be compressed,
     * ranged character sequence will be merged in possible, and unnecessary
     * escape will be descaped.
     *
     * @param weight the weight entry to be normalised
     * @return the normalised weight entry
     * @throws IllegalArgumentException
     *         if a syntax error exists in the weight statement
     * @throws NullPointerException
     *         if the weight entry or the weight statement in the entry is
     *         {@code null}
     */
    public static final Entry<String, Integer> normalise(Entry<String, Integer> weight) {
        Objects.requireNonNull(weight, "weight == null");
        return new SimpleEntry<>(EfcUtils.normalise(weight.getKey()), weight.getValue());
    }

    /**
     * Normalises a weight statement. All continue characters will be compressed,
     * ranged character sequence will be merged in possible, and unnecessary
     * escape will be descaped.
     *
     * @param statement the weight statement to be normalised
     * @return the normalised weight statement
     * @throws IllegalArgumentException
     *         if a syntax error exists in the weight statement
     * @throws NullPointerException
     *         if the weight statement is {@code null}
     */
    public static String normalise(String statement) {
        Objects.requireNonNull(statement, "statement == null");
        try {
            return EfcUtils.normaliseUnbounded(statement);
        }
        catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("unexpected encounter of EOS at column: " + e.getMessage().substring(e.getMessage().indexOf(": ") + 2), e);
        }
    }

    /**
     * Normalises a weight statement.
     *
     * @param statement the weight statement to be normalised
     * @return the normalised weight statement
     * @throws IllegalArgumentException
     *         if a syntax error exists in the weight statement
     * @throws StringIndexOutOfBoundsException
     *         if unexpected EOS occurred
     */
    private static String normaliseUnbounded(String statement) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < statement.length(); i++) {
            boolean escape = EfcUtils.requireNonSeperator(statement, i) == '\\' && EfcUtils.isEscapable(statement.charAt(++i));
            if (escape) {
                // only necessary escape will keep the backward slash
                string.append(EfcUtils.BACK_SLASH);
            }
            string.append(statement.charAt(i));
            // look forward for range operator
            if (statement.indexOf("..", i + 1) - i == 1) {
                char end = statement.charAt(i += 3);
                string.append("..").append(end);
                if (end == '\\') {
                    string.append(statement.charAt(++i));
                }
                else {
                    EfcUtils.requireNonEscapable(statement, i);
                }
            }
            else if (!escape && statement.charAt(i) == '.') {
                // uncaptured and unescaped range operator
                throw new IllegalArgumentException("unexpected range operator at column: " + i);
            }
        }
        // recompressing will merge ranged sequences and convert new sequences
        return EfcUtils.compress(EfcUtils.expand(string.toString()));
    }

    /**
     * Compresses the characters sequence with ranged operator where if possible.
     *
     * @param characters the characters sequence to be compressed
     * @return the compressed characters sequence
     */
    public static String compress(String characters) {
        if (characters.isEmpty()) {
            return characters;
        }
        // clean up characters sequence, remove duplicate and sort by code
        characters = EfcUtils.charStream(characters).sorted().distinct().map(String::valueOf).collect(Collectors.joining());
        StringBuilder compressed = new StringBuilder();
        int start = 0;
        int end = 0;
        for (int i = 0; i < characters.length(); i++) {
            if (start != i && characters.charAt(end) != (characters.charAt(i) - 1)) {
                // continuous sequence is broken, compress current sequence
                compressed.append(EfcUtils.compress(characters.charAt(start), characters.charAt(end)));
                start = end = i;
            }
            else {
                end = i;
            }
        }
        // compress current unbroken sequence until end of string
        compressed.append(EfcUtils.compress(characters.charAt(start), characters.charAt(end)));
        return compressed.toString();
    }

    /**
     * Creates a ranged characters sequence of the start and end characters.
     *
     * @param start the starting character
     * @param end the ending character
     * @return the ranged characters sequence
     * @throws IllegalArgumentException
     *         if the ending character is larger than the starting character
     */
    private static String compress(char start, char end) {
        if (end < start) {
            throw new IllegalArgumentException(String.format("invalid ranged sequence: start:'%c', end:'%c'", start, end));
        }
        StringBuilder string = new StringBuilder(4);
        string.append(EfcUtils.escape(start));
        if (end - start >= 2) {
            // only creates ranged sequence when the two characters are far enough
            string.append("..");
        }
        if (end - start >= 1) {
            string.append(EfcUtils.escape(end));
        }
        return string.toString();
    }

    /**
     * Expands a characters sequence into its characters components.
     *
     * @param characters the characters sequence to be expanded
     * @return the expanded characters sequence
     * @throws IllegalArgumentException
     *         if the ending character of ranged statement is larger than its
     *         starting character; or if the characters sequence contains syntax
     *         error
     */
    public static String expand(String characters) {
        StringBuilder expanded = new StringBuilder();
        for (int i = 0; i < characters.length(); i++) {
            char current = characters.charAt(i);
            if (current == '\\') {
                // check for unexpected escape characters
                current = EfcUtils.requireEscapable(characters, ++i);
            }
            if (characters.indexOf("..", i) - i == 1) {
                // capture ranged statement and expand it
                char start = current;
                char last = characters.charAt(i += 3);
                last = last == '\\' ? EfcUtils.requireEscapable(characters, ++i) : EfcUtils.requireNonEscapable(characters, i);
                expanded.append(EfcUtils.expand(start, last));
            }
            else {
                expanded.append(current);
            }
        }
        return expanded.toString();
    }

    /**
     * Creates an expanded characters sequence of {@code start} to {@code end}.
     *
     * @param start the start of the ranged sequence
     * @param end the end of the ranged sequence
     * @return the expanded characters sequence
     * @throws IllegalArgumentException
     *         if the ending character of ranged statement is larger than its
     *         starting character
     */
    private static String expand(char start, char end) {
        if (end < start) {
            throw new IllegalArgumentException(String.format("invalid ranged sequence: %c, %c", start, end));
        }
        StringBuilder expanded = new StringBuilder(end - start + 1);
        for (; start <= end; start++) {
            expanded.append(start);
        }
        return expanded.toString();
    }

    /**
     * Escapes any escapable characters in the string.
     *
     * @param string the string to be escaped
     * @return the escaped string
     */
    public static String escape(String string) {
        return EfcUtils.charStream(string).map(EfcUtils::escape).collect(Collectors.joining());
    }

    /**
     * Returns the escaped character of the character {@code c} if it is
     * escapable.
     *
     * @param c the character to be escaped
     * @return the escaped character of the character {@code c} if it is
     *         escapable; or the character {@code c} itself if it is not
     *         escapable
     */
    private static String escape(char c) {
        return EfcUtils.isEscapable(c) ? EfcUtils.BACK_SLASH + c : String.valueOf(c);
    }

    /**
     * Returns {@code true} if the character is escapable.
     *
     * @param c the character to be tested
     * @return {@code true} if the character {@code c} is escapable; or
     *         {@code false} otherwise
     */
    private static boolean isEscapable(char c) {
        return EfcUtils.ESCAPABLE.contains(String.valueOf(c));
    }

    /**
     * Throws an {@code IllegalArgumentException} if the character at index is
     * not a escapable character.
     *
     * @param characters the characters sequence
     * @param index the index of character to be checked
     * @return the character at index
     * @throws IllegalArgumentException
     *         if the character at index is not a escapable character
     */
    private static char requireEscapable(String characters, int index) {
        char target = characters.charAt(index);
        if (!EfcUtils.isEscapable(target)) {
            throw new IllegalArgumentException(String.format("unexpected escaped character: \"\\%c\", at column: %d", target, index));
        }
        return target;
    }

    /**
     * Throws an {@code IllegalArgumentException} if the character at index is a
     * escapable character.
     *
     * @param characters the characters sequence
     * @param index the index of character to be checked
     * @return the character at index
     * @throws IllegalArgumentException
     *         if the character at index is a escapable character
     */
    private static char requireNonEscapable(String characters, int index) {
        char target = characters.charAt(index);
        if (EfcUtils.isEscapable(target)) {
            throw new IllegalArgumentException(String.format("unexpected control character: '%c', at column: %d", target, index));
        }
        return target;
    }

    /**
     * Throws an {@code IllegalArgumentException} if the character at index is a
     * seperator.
     *
     * @param characters the characters sequence
     * @param index the index of character to be checked
     * @return the character at index
     * @throws IllegalArgumentException
     *         if the character at index is a seperator
     */
    private static char requireNonSeperator(String characters, int index) {
        char target = characters.charAt(index);
        if (EfcUtils.SEPARATORS.contains(String.valueOf(target))) {
            throw new IllegalArgumentException(String.format("unexpected seperator: %c, at column: %d", target, index));
        }
        return target;
    }

    /** Unnewable @throws UnsupportedOperationException always */
    private EfcUtils() {
        throw new UnsupportedOperationException("unnewable");
    }

}
