package org.pgstyle.efc.application.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pgstyle.efc.application.ApplicationException;
import org.pgstyle.efc.application.common.EfcUtils;

/**
 * The {@code CommandLineArguments} contains static methods for parsing the
 * command-line argument of the {@code ElytraFlightComputer} application. Also, an
 * instance of {@code CommandLineArguments} is the container for the parsed
 * argument for simplified access from the application.
 *
 * @version 1.0
 * @author PGKan
 */
public final class CommandLineArguments {

    /** Registered command-line argument keys. */
    private static final Map<String, String> cmdlArgs;
    private static final Set<String>         pFlags;

    static {
        cmdlArgs = new HashMap<>(17);
        // put named argument here
        cmdlArgs.put("-h", "Help");
        cmdlArgs.put("--help", "Help");
        cmdlArgs.put("--no-gui", "NoGUI");
        cmdlArgs.put("-v", "Version");
        cmdlArgs.put("--version", "Version");
        pFlags = new HashSet<>(2);
        pFlags.add("Help");
        pFlags.add("Version");
    }

    /**
     * Parses arguments from the main method and creates an argument container
     * instance containing the parsed arguments.
     *
     * @param args the arguments array from the main method
     * @return an argument container instance containing the parsed arguments
     * @throws IllegalArgumentException
     *         if the arguments contain invalid argument key; or the arguments
     *         have an incompatible type; or too many arguments passed from the
     *         main method
     */
    public static CommandLineArguments fromArgs(String[] args){
        try {
            return new CommandLineArguments(CommandLineArguments.processArguments(args));
        }
        catch (RuntimeException e) {
            throw new ApplicationException("failed to process arguments", e);
        }
    }

    /**
     * Returns {@code true} if any of the priority flags is set.
     *
     * @param keys the key set to be checked
     * @return {@code true} if any of the priority flags is set; or 
     *         {@code false} otherwise
     */
    private static boolean priorityFlags(Set<String> keys) {
        return CommandLineArguments.pFlags.stream().anyMatch(keys::contains);
    }

    /**
     * Creates an argument map containing default arguments.
     *
     * @return an argument map
     */
    private static Map<String, String> defaultArguments() {
        Map<String, String> argMap = new HashMap<>();
        // put default arguments here
        argMap.put("FlightPlan", "P0/0");
        return argMap;
    }

    /**
     * Parses arguments into an argument map.
     *
     * @param args the arguments to be parsed
     * @return an argument map contains the argument parsed
     * @throws IllegalArgumentException
     *         if the arguments contain invalid argument key; or the arguments
     *         have an incompatible type; or too many arguments passed from the
     *         main method
     */
    private static Map<String, String> processArguments(String[] args) {
        return CommandLineArguments.processArguments(Arrays.stream(args).collect(Collectors.toList()).iterator());
    }

    /**
     * Parses arguments into an argument map.
     *
     * @param args the arguments to be parsed
     * @return an argument map contains the argument parsed
     * @throws IllegalArgumentException
     *         if the arguments contain invalid argument key; or the arguments
     *         have an incompatible type; or too many arguments passed from the
     *         main method
     */
    private static Map<String, String> processArguments(Iterator<String> args) {
        Map<String, String> argMap = CommandLineArguments.defaultArguments();
        int position = 0;
        while (args.hasNext() && !CommandLineArguments.priorityFlags(argMap.keySet())) {
            String arg = args.next();
            if (arg == null) {
                continue;
            }
            if (arg.startsWith("-")) {
                try {
                    CommandLineArguments.setNamedArgument(arg, argMap, args);
                }
                catch (NoSuchElementException e) {
                    throw new IllegalArgumentException(String.format("expecting argument for key \"%s\", but found none", arg), e);
                }
            }
            else {
                CommandLineArguments.setPositionArgument(position, arg, argMap, args);
                position++;
            }
        }
        return argMap;
    }

    /**
     * Sets a named argument with key/name {@code arg} in the argument {@code map}.
     *
     * @param arg the key or name of the argument to be set
     * @param map the argument map
     * @param args the rest of the arguments
     * @throws IllegalArgumentException
     *         if the arguments contain invalid argument key; or the arguments
     *         have an incompatible type
     * @throws NoSuchElementException
     *         if the argument key demands an argument value, but the
     *         {@code args} has ran out of arguments
     */
    private static void setNamedArgument(String arg, Map<String, String> map, Iterator<String> args) {
        String name = CommandLineArguments.getFlagName(arg);
        switch (name){
        // flags
        case "Help":
        case "Version":
        case "NoGUI":
            map.put(name, name);
            break;
        case "":
            throw new IllegalArgumentException("unknown argument key: " + arg);
        default:
            throw new IllegalArgumentException("unknown argument: " + name);
        }
    }

    /**
     * Sets a positional argument at {@code position} with value as {@code arg}
     * in the argument {@code map}.
     *
     * @param position the position of the argument to be set
     * @param arg the value of the argument to be set
     * @param map the argument map
     * @param args the rest of the arguments
     * @throws IllegalArgumentException
     *         if the arguments contain invalid argument key; or the arguments
     *         have an incompatible type
     * @throws NoSuchElementException
     *         if the argument key demands an argument value, but the
     *         {@code args} has ran out of arguments
     */
    private static void setPositionArgument(int position, String arg, Map<String, String> map, Iterator<String> args){
        switch (position) {
        // keep switch clause in case of future addition of positional argument
        case 0:
            map.put("FlightPlan", arg);
            break;
        default:
            throw new IllegalArgumentException("two many arguments: " + position);
        }
    }

    /**
     * Converts an argument key into its coresponding flag name.
     *
     * @param key the argument key
     * @return the flag name coresponds to the argument {@code key}
     */
    private static String getFlagName(String key) {
        return Optional.ofNullable(CommandLineArguments.cmdlArgs.get(key)).orElse("");
    }

    /**
     * Creates an argument container with the pre-processed argument map.
     *
     * @param arguments the argument map
     * @throws NullPointerException
     *         if the argument {@code arguments} is {@code null}
     */
    public CommandLineArguments(Map<String, String> arguments) {
        Objects.requireNonNull(arguments, "arguments == null");
        this.arguments = new HashMap<>();
        this.arguments.putAll(arguments);
        if (EfcUtils.headless()) {
            // no GUI mode available, forced cli mode
            this.arguments.putIfAbsent("NoGUI", "NoGUI");
        }
    }

    /** The argument map for holding all command-line arguments. */
    private Map<String, String> arguments;

    /**
     * Checks if the specified flag is set.
     *
     * @param key the key of the flag
     * @return {@code true} if the flag is set; or {@code false} otherwise
     */
    public boolean isFlagSet(String key) {
        return arguments.containsKey(key);
    }

    /**
     * Checks if the {@code Help} flag is set.
     *
     * @return {@code true} if the {@code Help} flag is set; or {@code false}
     *         otherwise
     */
    public boolean help() {
        return this.isFlagSet("Help");
    }

    /**
     * Checks if the {@code NoGUI} flag is set.
     *
     * @return {@code true} if the {@code GUI} flag is set; or {@code false}
     *         otherwise
     */
    public boolean noGui() {
        return this.isFlagSet("NoGUI");
    }

    /**
     * Returns the {@code FlightPlan} argument.
     *
     * @return the {@code FlightPlan} argument; or an empty string if the
     *         argument has not been set
     */
    public String flightPlan() {
        return Optional.ofNullable(this.arguments.get("FlightPlan")).orElse("");
    }

    /**
     * Checks if the {@code Version} flag is set.
     *
     * @return {@code true} if the {@code Version} flag is set; or {@code false}
     *         otherwise
     */
    public boolean version() {
        return this.isFlagSet("Version");
    }

}
