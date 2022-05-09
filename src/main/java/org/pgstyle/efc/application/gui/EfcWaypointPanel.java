package org.pgstyle.efc.application.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pgstyle.efc.application.common.EfcComputingUnit;
import org.pgstyle.efc.application.common.EfcUtils;
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
            this.add = new JButton(EfcWaypointPanel.ADD);
            this.remove = new JButton(EfcWaypointPanel.REMOVE);
            this.mode = new JButton(waypoint.isLocation()? "L" : "P");
            this.x = new JSpinner(new SpinnerNumberModel(waypoint.x(), Integer.MIN_VALUE, Integer.MAX_VALUE, 10));
            this.y = new JSpinner(new SpinnerNumberModel(waypoint.y(), -64, 384, 2));
            this.z = new JSpinner(new SpinnerNumberModel(waypoint.z(), Integer.MIN_VALUE, Integer.MAX_VALUE, 10));
            this.h = new JSpinner(new SpinnerNumberModel(waypoint.h(), 0, 128, 2));
            this.y.setEnabled(waypoint.isLocation());

            // setup the GUI appearance
            this.setMinimumSize(EfcWaypointPanel.CELL_SIZE);
            this.setPreferredSize(EfcWaypointPanel.CELL_SIZE);
            this.setMaximumSize(EfcWaypointPanel.CELL_SIZE);
            this.setBorder(BorderFactory.createEtchedBorder());
            this.add.setFont(EfcMainFrame.MONOBOLD);
            this.add.setMargin(new Insets(0, 0, 0, 0));
            this.add.setMinimumSize(EfcWaypointPanel.ACTION_SIZE);
            this.add.setPreferredSize(EfcWaypointPanel.ACTION_SIZE);
            this.add.setMaximumSize(EfcWaypointPanel.ACTION_SIZE);
            this.add.setFocusPainted(false);
            this.remove.setFont(EfcMainFrame.MONOBOLD);
            this.remove.setMargin(new Insets(0, 0, 0, 0));
            this.remove.setMinimumSize(EfcWaypointPanel.ACTION_SIZE);
            this.remove.setPreferredSize(EfcWaypointPanel.ACTION_SIZE);
            this.remove.setMaximumSize(EfcWaypointPanel.ACTION_SIZE);
            this.remove.setFocusPainted(false);
            this.mode.setFont(EfcMainFrame.MONOBOLD);
            this.mode.setMargin(new Insets(0, 0, 0, 0));
            this.mode.setMinimumSize(EfcWaypointPanel.ACTION_SIZE);
            this.mode.setPreferredSize(EfcWaypointPanel.ACTION_SIZE);
            this.mode.setMaximumSize(EfcWaypointPanel.ACTION_SIZE);
            this.mode.setFocusPainted(false);
            this.x.setFont(EfcMainFrame.MONO);
            this.y.setFont(EfcMainFrame.MONO);
            this.z.setFont(EfcMainFrame.MONO);
            this.h.setFont(EfcMainFrame.MONO);
            ((JSpinner.NumberEditor) this.x.getEditor()).getTextField().setColumns(12);
            ((JSpinner.NumberEditor) this.z.getEditor()).getTextField().setColumns(12);
            ((JSpinner.NumberEditor) this.y.getEditor()).getTextField().setColumns(5);
            ((JSpinner.NumberEditor) this.h.getEditor()).getTextField().setColumns(5);

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
                                          .addGap(5)
                                          .addComponent(xLabel)
                                          .addComponent(this.x)
                                          .addGap(5)
                                          .addComponent(zLabel)
                                          .addComponent(this.z)
                                )
                                .addGroup(
                                    layout.createSequentialGroup()
                                          .addGap(5)
                                          .addComponent(this.mode)
                                          .addGap(5)
                                          .addComponent(yLabel)
                                          .addComponent(this.y)
                                          .addGap(5)
                                          .addComponent(hLabel)
                                          .addComponent(this.h)
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
                                          .addComponent(xLabel)
                                          .addComponent(this.x)
                                          .addComponent(zLabel)
                                          .addComponent(this.z)
                                )
                                .addGroup(
                                    layout.createParallelGroup()
                                          .addComponent(this.mode)
                                          .addComponent(yLabel)
                                          .addComponent(this.y)
                                          .addComponent(hLabel)
                                          .addComponent(this.h)
                                )
                      )
            );

            // setup events
            this.add.addActionListener(this.new CellOperation((e -> this.add())));
            this.remove.addActionListener(this.new CellOperation((e -> this.remove())));
            this.mode.addActionListener(this.new CellOperation((e -> this.mode())));

            this.add(this.add);
            this.add(this.remove);
        }

        private final JButton add;
        private final JButton remove;
        private final JButton mode;
        private final JSpinner x;
        private final JSpinner y;
        private final JSpinner z;
        private final JSpinner h;

        /**
         * Returns a waypoint from the value of this cell.
         *
         * @return a waypoint
         */
        public Waypoint getWaypoint() {
            if (this.mode.getText().equals("L")) {
                return Waypoint.location((Integer) this.x.getValue(), (Integer) this.y.getValue(), (Integer) this.z.getValue(), (Integer) this.h.getValue());
            }
            else {
                return Waypoint.point((Integer) this.x.getValue(), (Integer) this.z.getValue(), (Integer) this.h.getValue());
            }
        }

        private void add() {
            EfcWaypointPanel.this.waypoints.add(EfcWaypointPanel.this.waypoints.indexOf(this) + 1, new EfcWaypointCell());
            EfcWaypointPanel.this.reload();
        }
    
        private void remove() {
            EfcWaypointPanel.this.waypoints.remove(EfcWaypointPanel.this.waypoints.indexOf(this));
            EfcWaypointPanel.this.reload();
        }

        private void mode() {
            if (this.mode.getText().equals("L")) {
                this.mode.setText("P");
                this.y.setEnabled(false);
            }
            else {
                this.mode.setText("L");
                this.y.setEnabled(true);
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
        JPanel buttons = new JPanel(new GridLayout(2, 1));
        JButton load = new JButton("<<");
        load.setFont(EfcMainFrame.MONOBOLD);
        load.setFocusPainted(false);
        load.addActionListener(e -> this.load());
        buttons.add(load);
        JButton apply = new JButton(">>");
        apply.setFont(EfcMainFrame.MONOBOLD);
        apply.setFocusPainted(false);
        apply.addActionListener(e -> this.apply());
        buttons.add(apply);
        this.add(buttons, BorderLayout.EAST);

        this.load();
        // setup events
        // commit.addActionListener(e -> this.commit());
        // abort.addActionListener(e -> this.abort());
    }

    /** The size of a cell in the weight descriptor configuration dialog. */
    private static final Dimension CELL_SIZE = new Dimension(340, 52);
    /** The size of the action button in a cell. */
    private static final Dimension ACTION_SIZE = new Dimension(24, 24);
    /** String constant of the {@code Remove} action. */
    private static final String REMOVE = "-";
    /** String constant of the {@code Add} action. */
    private static final String ADD = "+";

    /** Reference to the main window's controller. */
    private EfcMainFrame main;
    /** Panel for holding all weight configuration cells. */
    private JPanel waypointPanel;

    private List<EfcWaypointCell> waypoints;

    public void apply() {
        this.waypoints.clear();
        Stream.of(this.waypointPanel.getComponents()).map(EfcWaypointCell.class::cast).forEach(this.waypoints::add);
        this.main.updateFlightPlan(this.waypoints.stream().map(EfcWaypointCell::getWaypoint).map(Objects::toString).collect(Collectors.joining(" ")));
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
