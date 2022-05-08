package org.pgstyle.efc.application.cli;

import java.util.Objects;

import org.pgstyle.efc.application.common.EfcConfig;

/**
 * The {@code EfcConfigurator} is the controller for loading
 * {@code CommandLineArguments} and create the configuration container instance
 * of {@code EfcConfig}. And the {@code EfcConfigurator} also provides
 * command-line interactive configuration, which can use the command-line
 * interface interactively to allow user to configure the {@code EfcConfig} with
 * guided operations.
 *
 * @since efc-2
 * @version efc-2.0
 * @author PGKan
 */
public final class EfcConfigurator {

    /**
     * Creates an {@code EfcConfigurator} and loads in command-line arguments.
     *
     * @param cmdlArgs the command-line arguments container
     * @throws IllegalArgumentException
     *         if the loaded command-line arguments container contains invalid
     *         argument conbinations
     * @throws NullPointerException
     *         if the command-line arguments container is {@code null}
     */
    public EfcConfigurator(CommandLineArguments cmdlArgs) {
        Objects.requireNonNull(cmdlArgs, "cmdlArgs == null");
        this.efcConfig = new EfcConfig();
        this.efcConfig.flightPlan(cmdlArgs.flightPlan());
    }

    /** The configuration container carries all configuration. */
    private EfcConfig efcConfig;

    /**
     * Returns the configuration container of this configurator.
     *
     * @return the configuration container of this configurator
     */
    public EfcConfig getConfig() {
        return this.efcConfig;
    }

}
