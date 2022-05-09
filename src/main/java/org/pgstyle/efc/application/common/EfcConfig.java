package org.pgstyle.efc.application.common;

import java.util.Objects;

/**
 * The {@code EfcConfig} is a container of configurations for the
 * {@code ElytraFlightComputer} application.
 *
 * @since efc-2
 * @version efc-2.0
 * @author PGKan
 */
public final class EfcConfig {

    /**
     * Creates a new configuration container with preloaded default
     * configurations.
     */
    public EfcConfig() {
        this.reset();
    }

    private String  flightPlan;
    /** Indicates skip engaging the randomiser and end the application directly. */
    private boolean skip;
    /** The exiting state of the application. */
    private int     state;

    /**
     * Resets the configuration container to default configuration.
     */
    public void reset() {
        this.flightPlan = "P0/0";
        this.skip = false;
        this.state = 0;
    }

    /**
     * Returns the seed stored in this configuration container.
     *
     * @return the seed stored in this configuration container
     */
    public String flightPlan() {
        return this.flightPlan;
    }

    /**
     * Sets the flightPlan of this configuration container.
     *
     * @param flightPlan the flightPlan to be stored
     */
    public void flightPlan(String flightPlan) {
        this.flightPlan = Objects.requireNonNull(flightPlan, "flightPlan == null");
    }

    /**
     * Returns {@code true} if the skip flag is set in this configuration
     * container.
     *
     * @return {@code true} if the skip flag is set; or {@code false} otherwise
     */
    public boolean skip() {
        return this.skip;
    }

    /**
     * Sets the skip flag of this configuration container.
     *
     * @param skip the skip flag
     */
    public void skip(boolean skip) {
        this.skip = skip;
    }

    /**
     * Returns the exiting state of the {@code ElytraFlightComputer} application
     * stored at this configuration container.
     *
     * @return the exiting state of the {@code ElytraFlightComputer}
     */
    public int state() {
        return this.state;
    }

    /**
     * Sets the exiting state of the {@code ElytraFlightComputer} application.
     *
     * @param state the exiting state to be stored
     */
    public void state(int state) {
        this.state = state;
    }

}
