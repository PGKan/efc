package org.pgstyle.efc.main;

import org.pgstyle.efc.application.ElytraFlightComputer;
import org.pgstyle.efc.application.cli.CommandLineArguments;

/**
 * Application entrypoint.
 */
public final class Main {

    /**
     * Application entrypoint.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.exit(new ElytraFlightComputer(CommandLineArguments.fromArgs(args)).call());
    }
}
