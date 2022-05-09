package org.pgstyle.efc.application;

import java.util.concurrent.Callable;

import org.pgstyle.efc.application.cli.CmdUtils;
import org.pgstyle.efc.application.cli.CommandLineArguments;
import org.pgstyle.efc.application.cli.EfcConfigurator;
import org.pgstyle.efc.application.common.EfcConfig;
import org.pgstyle.efc.application.common.EfcResources;
import org.pgstyle.efc.application.common.EfcUtils;
import org.pgstyle.efc.application.gui.EfcMainFrame;

public final class ElytraFlightComputer implements Callable<Integer> {

    /** Exiting state: 0 Success */
    public static final int SUCCESS   = 0;
    /** Exiting state: 255 Document Failure */
    public static final int FAIL_DOC  = 255;
    /** Exiting state: 1 Argument Failure */
    public static final int FAIL_ARG  = 1;
    /** Exiting state: 2 Initialisation Failure */
    public static final int FAIL_INIT = 2;
    /** Exiting state: 3 Write Failure */
    public static final int FAIL_WRITE = 3;
    /** Exiting state: 4 Interrupted */
    public static final int FAIL_INTR = 4;

    /**
     * Creates an application controller with loaded command-line arguments.
     *
     * @param cmdlArgs the loaded command-line arguments
     */
    public ElytraFlightComputer(CommandLineArguments cmdlArgs) {
        this.cmdlArgs = cmdlArgs;
    }

    /** Command-line argument storage */
    private final CommandLineArguments cmdlArgs;

    /**
     * Use the application controller.
     *
     * @return the exiting state, a non-zero state indicate error has occurred
     */
    @Override
    public Integer call() {
        // standard command line support -h/-v
        if (this.cmdlArgs.help()) {
            return ElytraFlightComputer.help();
        }
        if (this.cmdlArgs.version()) {
            return ElytraFlightComputer.version();
        }
        EfcConfig config = new EfcConfigurator(this.cmdlArgs).getConfig();
        ElytraFlightComputer.head();
        if (!this.cmdlArgs.noGui()) {
            // start GUI mode
            CmdUtils.stdout("Start in GUI mode..." + EfcUtils.NEWLINE);
            EfcMainFrame frame = new EfcMainFrame(config);
            int code = ElytraFlightComputer.SUCCESS;
            try {
                synchronized (this) {
                    while (!this.cmdlArgs.noGui()) {
                        this.wait();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (frame.isClosed()) {
                    code = ElytraFlightComputer.SUCCESS;
                }
                else {
                    code = ElytraFlightComputer.FAIL_INTR;
                }
            }
            CmdUtils.stdout("GUI mode exited with code %d%n", code);
            return code;
        }
        else {
            // start CLI mode
            return ElytraFlightComputer.main(config);
        }
    }

    /**
     * CLI mode entrypoint of the {@code MinecraftFlightComputer}.
     *
     * @param efcConfig the configuration container loaded from command-line
     *                  arguments
     * @return the exiting state, a non-zero state indicate error has occurred
     */
    private static int main(EfcConfig efcConfig) {
        if (efcConfig.skip()) {
            return ElytraFlightComputer.SUCCESS;
        }
        // try {
        //     RandomStringGenerator rsg = new RandomStringGenerator(efcConfig);
        //     try (PrintStream ps = Objects.nonNull(efcConfig.output()) ? EfcUtils.openFile(efcConfig.output()) : CmdUtils.stdout()) {
        //         do {
        //             EfcUtils.write(ps, rsg.step());
        //         } while (rsg.available());
        //     }
        // }
        // catch (RuntimeException e) {
        //     CmdUtils.stderr("failed to engage randomiser%n%s", EfcUtils.stackTraceOf(e));
        //     return ElytraFlightComputer.FAIL_INIT;
        // } catch (IOException e) {
        //     CmdUtils.stderr("failed write result%n%s", EfcUtils.stackTraceOf(e));
        //     return ElytraFlightComputer.FAIL_WRITE;
        // }
        return ElytraFlightComputer.SUCCESS;
    }

    /**
     * Prints the header text.
     */
    public static void head() {
        ElytraFlightComputer.printResourceText("efc.text.head");
    }

    /**
     * Prints the help text.
     *
     * @return the exiting state, a non-zero state indicate error has occurred
     */
    public static int help() {
        return ElytraFlightComputer.printResourceText("efc.text.help");
    }

    /**
     * Prints the version info text.
     *
     * @return the exiting state, a non-zero state indicate error has occurred
     */
    public static int version() {
        return ElytraFlightComputer.printResourceText("efc.text.version");
    }

    /**
     * Prints the text in a resource file.
     *
     * @param key the key of the resource
     * @return the exiting state, a non-zero state indicate error has occurred
     */
    private static int printResourceText(String key) {
        if (EfcResources.exist(key)) {
            CmdUtils.stdout("%s%n", EfcResources.get(key));
            return ElytraFlightComputer.SUCCESS;
        }
        else {
            CmdUtils.stderr("%s%n", EfcResources.get(key));
            return ElytraFlightComputer.FAIL_DOC;
        }
    }

}
