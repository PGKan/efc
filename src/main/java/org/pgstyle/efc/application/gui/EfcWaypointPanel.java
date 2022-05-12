package org.pgstyle.efc.application.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;

import org.pgstyle.efc.application.common.EfcComputingUnit;
import org.pgstyle.efc.application.common.EfcUtils;
import org.pgstyle.efc.model.Path;
import org.pgstyle.efc.model.Waypoint;

/**
 * Frame controller for the config function of weight descriptors of the
 * {@code org.pgstyle.efc.random.WeightedRandomiser WeightedRandomiser}.
 *
 * @since Efc-2
 * @version Efc-2.0
 * @author PGKan
 */
public class EfcWaypointPanel extends JPanel {

    /**
     * Weight descriptor container and controller. It is a subelement of a
     * {@code EfcWaypointPanel}. It contains an action button for adding and
     * removing cells and the input area for the weight and statement.
     */
    private class EfcWaypointCell extends JPanel {

        /**
         * Event handler of the action button of a {@code EfcWeightCell}.
         */
        private class CellOperation implements ActionListener {


            public CellOperation(Consumer<ActionEvent> consumer) {
                this.consumer = consumer;
            }

            private final Consumer<ActionEvent> consumer;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                consumer.accept(e);
                // manipulate weight cells will not update the config dialog
                // automatically, trigger the update of GUI component
                this.redraw();
            }

            /**
             * Triggers redraw of the descriptors component of the weights
             * configuration dialog.
             */
            private void redraw() {
                EfcWaypointPanel.this.redraw();
                EfcWaypointPanel.this.waypointPanel.repaint();
            }
        }

        /**
         * Initialises a weight descriptor container unit.
         */
        public EfcWaypointCell() {
            this(Waypoint.point(0, 0, 128));
        }

        public EfcWaypointCell(Waypoint waypoint) {
            // create the subelements
            this.add = new EfcStandardButton(EfcWaypointPanel.ADD, this.new CellOperation((e -> this.add())));
            this.remove = new EfcStandardButton(EfcWaypointPanel.REMOVE, this.new CellOperation((e -> this.remove())));
            this.mode = new EfcStandardButton(waypoint.isLocation()? EfcWaypointPanel.L : EfcWaypointPanel.P, this.new CellOperation((e -> this.mode())));
            this.longitude = new JSpinner(new SpinnerNumberModel(waypoint.x(), Integer.MIN_VALUE, Integer.MAX_VALUE, 10));
            this.level = new JSpinner(new SpinnerNumberModel(waypoint.y(), -64, 384, 2));
            this.latitude = new JSpinner(new SpinnerNumberModel(waypoint.z(), Integer.MIN_VALUE, Integer.MAX_VALUE, 10));
            this.head = new JSpinner(new SpinnerNumberModel(waypoint.h(), 0, 128, 2));
            this.level.setEnabled(waypoint.isLocation());

            // setup the GUI appearance
            this.setBorder(BorderFactory.createEtchedBorder());
            EfcWaypointPanel.applySize(this, EfcWaypointPanel.CELL_SIZE);
            EfcWaypointPanel.applySize(this.add, EfcWaypointPanel.ACTION_SIZE);
            EfcWaypointPanel.applySize(this.remove, EfcWaypointPanel.ACTION_SIZE);
            EfcWaypointPanel.applySize(this.mode, EfcWaypointPanel.ACTION_SIZE);
            this.longitude.setFont(EfcMainFrame.MONO);
            this.level.setFont(EfcMainFrame.MONO);
            this.latitude.setFont(EfcMainFrame.MONO);
            this.head.setFont(EfcMainFrame.MONO);
            ((JSpinner.NumberEditor) this.longitude.getEditor()).getTextField().setColumns(12);
            ((JSpinner.NumberEditor) this.latitude.getEditor()).getTextField().setColumns(12);
            ((JSpinner.NumberEditor) this.level.getEditor()).getTextField().setColumns(5);
            ((JSpinner.NumberEditor) this.head.getEditor()).getTextField().setColumns(5);

            JLabel xLabel = new JLabel("X");
            JLabel yLabel = new JLabel("Y");
            JLabel zLabel = new JLabel("Z");
            JLabel hLabel = new JLabel("H");
            xLabel.setFont(EfcMainFrame.MONOBOLD);
            yLabel.setFont(EfcMainFrame.MONOBOLD);
            zLabel.setFont(EfcMainFrame.MONOBOLD);
            hLabel.setFont(EfcMainFrame.MONOBOLD);

            // setup layout
            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createSequentialGroup()
                      .addGroup(
                          layout.createSequentialGroup()
                                .addComponent(this.add)
                                .addComponent(this.remove)
                      )
                      .addGroup(
                          layout.createParallelGroup(Alignment.LEADING)
                                .addGroup(
                                    layout.createSequentialGroup()
                                          .addGap(5).addComponent(xLabel).addComponent(this.longitude)
                                          .addGap(5).addComponent(zLabel).addComponent(this.latitude)
                                )
                                .addGroup(
                                    layout.createSequentialGroup()
                                          .addGap(5).addComponent(this.mode)
                                          .addGap(5).addComponent(yLabel).addComponent(this.level)
                                          .addGap(5).addComponent(hLabel).addComponent(this.head)
                                )
                      )
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(Alignment.CENTER)
                      .addGroup(
                          layout.createParallelGroup(Alignment.CENTER)
                                .addComponent(this.add)
                                .addComponent(this.remove)
                      )
                      .addGroup(
                          layout.createSequentialGroup()
                                .addGroup(
                                    layout.createParallelGroup()
                                          .addComponent(xLabel).addComponent(this.longitude)
                                          .addComponent(zLabel).addComponent(this.latitude)
                                )
                                .addGroup(
                                    layout.createParallelGroup()
                                          .addComponent(this.mode)
                                          .addComponent(yLabel).addComponent(this.level)
                                          .addComponent(hLabel).addComponent(this.head)
                                )
                      )
            );
        }

        private final EfcStandardButton add;
        private final EfcStandardButton remove;
        private final EfcStandardButton mode;
        private final JSpinner longitude;
        private final JSpinner level;
        private final JSpinner latitude;
        private final JSpinner head;

        /**
         * Returns a waypoint from the value of this cell.
         *
         * @return a waypoint
         */
        public Waypoint getWaypoint() {
            if (this.mode.getText().equals(EfcWaypointPanel.L)) {
                return Waypoint.location(((Number) this.longitude.getValue()).doubleValue(), ((Number) this.level.getValue()).doubleValue(), ((Number) this.latitude.getValue()).doubleValue(), ((Number) this.head.getValue()).intValue());
            }
            else {
                return Waypoint.point(((Number) this.longitude.getValue()).doubleValue(), ((Number) this.latitude.getValue()).doubleValue(), ((Number) this.head.getValue()).intValue());
            }
        }

        private void add() {
            EfcWaypointPanel.this.waypoints.add(EfcWaypointPanel.this.waypoints.indexOf(this) + 1, new EfcWaypointCell());
            EfcWaypointPanel.this.reload();
        }
    
        private void remove() {
            if (EfcWaypointPanel.this.waypoints.size() > 1) {
                EfcWaypointPanel.this.waypoints.remove(EfcWaypointPanel.this.waypoints.indexOf(this));
                EfcWaypointPanel.this.reload();
            }
            else {
                EfcWaypointPanel.this.main.write("Cannot remove all waypoint");
            }
        }

        private void mode() {
            if (this.mode.getText().equals(EfcWaypointPanel.L)) {
                this.mode.setText(EfcWaypointPanel.P);
                this.level.setEnabled(false);
            }
            else {
                this.mode.setText(EfcWaypointPanel.L);
                this.level.setEnabled(true);
            }
        }
    
        /**
         * Returns the string representation of this cell. Will return the
         * normalised form if possible.
         *
         * @return the normalised descriptor text of this cell; or the raw
         *         descriptor text if the descriptor statement contains syntax
         *         error
         */
        @Override
        public String toString() {
            return this.getWaypoint().toString();
        }

    }

    /**
     * Initialises the weight descriptor configuration dialog.
     *
     * @param main the controller of the main GUI
     */
    public EfcWaypointPanel(EfcMainFrame main) {
        this.main = main;
        this.waypoints = new ArrayList<>();

        // subelements
        this.waypointPanel = new JPanel();
        this.waypointPanel.setLayout(new BoxLayout(waypointPanel, BoxLayout.PAGE_AXIS));
        JScrollPane pane = new JScrollPane(waypointPanel,
                                           ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setPreferredSize(new Dimension(CELL_SIZE.width + 20, CELL_SIZE.height * 8));
        pane.setAutoscrolls(true);
        pane.getVerticalScrollBar().setUnitIncrement(24);
        this.add(pane);

        JPanel buttons = new JPanel(new GridLayout(4, 1));
        buttons.add(new EfcStandardButton("RST", e -> this.main.loadDefault()));
        buttons.add(new EfcStandardButton("  <<  ", e -> this.load()));
        buttons.add(new EfcStandardButton("  >>  ", e -> this.apply()));
        buttons.add(new EfcStandardButton("REV", e -> this.reverse()));
        this.add(buttons, BorderLayout.EAST);

    }

    public static Component applySize(Component c, Dimension size) {
        c.setMinimumSize(size);
        c.setPreferredSize(size);
        c.setMaximumSize(size);
        return c;
    }

    /** The size of a cell in the weight descriptor configuration dialog. */
    private static final Dimension CELL_SIZE = new Dimension(340, 52);
    /** The size of the action button in a cell. */
    private static final Dimension ACTION_SIZE = new Dimension(24, 24);
    private static final String REMOVE = "-";
    private static final String ADD = "+";
    private static final String L = "L";
    private static final String P = "P";

    /** Reference to the main window's controller. */
    private EfcMainFrame main;
    /** Panel for holding all weight configuration cells. */
    private JPanel waypointPanel;
    private List<EfcWaypointCell> waypoints;

    public void apply() {
        this.waypoints.clear();
        Stream.of(this.waypointPanel.getComponents()).map(EfcWaypointCell.class::cast).forEach(this.waypoints::add);
        List<Waypoint> waypointList = this.waypoints.stream().map(EfcWaypointCell::getWaypoint).collect(Collectors.toList());
        this.main.updateFlightPlan(waypointList.stream().map(Objects::toString).collect(Collectors.joining(" ")));
        List<Path> paths = new ArrayList<>();
        Path path = null;
        for (Waypoint waypoint : waypointList) {
            if (Objects.isNull(path)) {
                path = Path.startsAt(waypoint);
            }
            else if (waypoint.isLocation()) {
                paths.add(path.to(waypoint));
                path = Path.startsAt(waypoint);
            }
            else {
                path.to(waypoint);
            }
        }
        if (Objects.nonNull(path) && path.length() > 1) {
            paths.add(path);
        }
        this.main.rewrite(EfcComputingUnit.calculateFlightParameter(paths).values().stream().map(Object::toString).collect(Collectors.joining()));
    }

    public void load() {
        String flightPlan = this.main.getFlightPlan();
        try {
            this.waypoints.clear();
            EfcComputingUnit.getWaypoints(flightPlan).stream().map(EfcWaypointCell::new).forEach(this.waypoints::add);
            this.reload();
        }
        catch (RuntimeException e) {
            this.main.write(e.getMessage());
        }
    }

    private void reverse() {
        Collections.reverse(this.waypoints);
        this.reload();
    }

    private void reload() {
        this.waypointPanel.removeAll();
        this.waypoints.stream().forEach(this.waypointPanel::add);
        this.redraw();
    }

    private void redraw() {
        this.revalidate();
        this.repaint();
    }

    /**
     * Closes this configuration dialog and do not update the weight descriptor.
     */
    public void abort() {
        this.setVisible(false);
    }

    /**
     * Closes this configuration dialog and update the weight descriptor.
     */
    public void commit() {
        String descriptor = this.toString();
        this.main.write("updated weights descriptor");
        this.main.write(descriptor);
        this.setVisible(false);
    }

    /**
     * Returns the weight descriptor string of the settings from this
     * configuration dialog.
     * @return the weight descriptor string of the settings from this
     *         configuration dialog
     */
    @Override
    public String toString() {
        return Arrays.stream(this.waypointPanel.getComponents())
                     .map(EfcWaypointCell.class::cast)
                     .map(EfcWaypointCell::toString)
                     .collect(Collectors.joining(";"));
    }

}
