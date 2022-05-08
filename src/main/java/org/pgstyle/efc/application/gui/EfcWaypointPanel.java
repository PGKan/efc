package org.pgstyle.efc.application.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pgstyle.efc.application.common.EfcUtils;
import org.pgstyle.efc.model.Waypoint;

/**
 * Frame controller for the config function of weight descriptors of the
 * {@link org.pgstyle.efc.random.WeightedRandomiser WeightedRandomiser}.
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
    private class EfcWeightCell extends JPanel {

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
                EfcWaypointPanel.this.waypoints.revalidate();
                EfcWaypointPanel.this.waypoints.repaint();
            }
        }

        /**
         * Event handler of the weight text input of a {@code EfcWeightCell}.
         */
        private class CellValidation implements DocumentListener {

            @Override
            public void insertUpdate(DocumentEvent e) {
                this.changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // perform realtime syntax checking for weight descriptor statement
                // syntax check on initialisation will be performed in the realise stage
                if (EfcWeightCell.this.isRealised()) {
                    try {
                        // use the syntax checking function in the normalise method
                        // EfcUtils.normalise(new SimpleEntry<>(EfcWeightCell.this.getStatement(), EfcWeightCell.this.getWeight()));
                        // EfcWeightCell.this.statement.setBackground(Color.WHITE);
                    }
                    catch (RuntimeException ex) {
                        // syntax error visual notification
                        EfcWeightCell.this.statement.setBackground(Color.PINK);
                    }
                }
            }
        }

        /**
         * Initialises a weight descriptor container unit.
         *
         * @param realise directly realises this cell when {@code true}
         */
        public EfcWeightCell(boolean realise) {
            // lock realising state
            this.realised = false;
            // create the subelements
            this.add = new JButton(EfcWaypointPanel.ADD);
            this.remove = new JButton(EfcWaypointPanel.REMOVE);
            this.l = new JCheckBox();
            this.x = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 10));
            this.y = new JSpinner(new SpinnerNumberModel(0, -64, 384, 2));
            this.z = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 10));
            this.h = new JSpinner(new SpinnerNumberModel(0, 0, 128, 2));

            // setup the GUI appearance
            this.setMinimumSize(EfcWaypointPanel.CELL_SIZE);
            this.setPreferredSize(EfcWaypointPanel.CELL_SIZE);
            this.setMaximumSize(EfcWaypointPanel.CELL_SIZE);
            this.setBorder(BorderFactory.createEtchedBorder());
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.add.setFont(EfcMainFrame.MONOBOLD);
            this.add.setMargin(new Insets(0, 0, 0, 0));
            this.add.setMinimumSize(EfcWaypointPanel.ACTION_SIZE);
            this.add.setPreferredSize(EfcWaypointPanel.ACTION_SIZE);
            this.add.setMaximumSize(EfcWaypointPanel.ACTION_SIZE);
            this.add.setFocusPainted(false);
            this.remove.setFont(EfcMainFrame.MONOBOLD);
            this.remove.setVisible(false);
            this.remove.setMargin(new Insets(0, 0, 0, 0));
            this.remove.setMinimumSize(EfcWaypointPanel.ACTION_SIZE);
            this.remove.setPreferredSize(EfcWaypointPanel.ACTION_SIZE);
            this.remove.setMaximumSize(EfcWaypointPanel.ACTION_SIZE);
            this.remove.setFocusPainted(false);

            // setup events
            this.add.addActionListener(this.new CellOperation((e -> this.add())));
            this.remove.addActionListener(this.new CellOperation((e -> this.remove())));


            this.action = new JButton();
            this.weight = new JSpinner(new SpinnerNumberModel(0, 0, 128, 2));
            this.statement = new JTextField();
            ((JSpinner.NumberEditor) this.weight.getEditor()).getTextField().setColumns(4);
            this.statement.getDocument().addDocumentListener(this.new CellValidation());

            this.add(this.add);
            this.add(this.remove);
            if (realise) {
                this.realise(false, 0, 0, 0, 128);
            }
        }

        private final JButton add;
        private final JButton remove;
        private final JCheckBox l;
        private final JSpinner x;
        private final JSpinner y;
        private final JSpinner z;
        private final JSpinner h;

        private final JButton action;
        private final JTextField statement;
        private final JSpinner weight;

        private boolean realised;

        /**
         * Returns {@code true} if this cell has finished realising.
         *
         * @return {@code true} if this cell has finished realising; or
         *         {@code false} otherwise
         */
        public boolean isRealised() {
            return this.realised;
        }

        /**
         * Returns a waypoint from the value of this cell.
         *
         * @return a waypoint
         */
        public Waypoint getWaypoint() {
            if (this.l.isSelected()) {
                return Waypoint.at((Integer) this.x.getValue(), (Integer) this.y.getValue(), (Integer) this.z.getValue(), (Integer) this.h.getValue());
            }
            else {
                return Waypoint.at((Integer) this.x.getValue(), (Integer) this.z.getValue(), (Integer) this.h.getValue());
            }
        }

        private void add() {
            if (this.isRealised()) {
                EfcWaypointPanel.this.waypoints.add(new EfcWeightCell(true));
            }
            else {
                this.realise(false, 0, 0, 0, 128);
            }
        }
    
        private void remove() {
            EfcWaypointPanel.this.waypoints.remove(this);
        }
    
        /**
         * Realises this cell with the given values.
         *
         * @param weight the weight of the descriptor
         * @param statement the statement of the descriptor
         */
        public void realise(boolean l, int x, int y, int z, int h) {
            this.add(this.remove);
            this.add(this.l);
            this.add(this.x);
            this.add(this.y);
            this.add(this.z);
            this.add(this.h);

            this.l.setSelected(l);
            this.x.setValue(x);
            this.y.setValue(y);
            this.z.setValue(z);
            this.h.setValue(h);

            this.realised = true;
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
            try {
                return this.weight.getValue() + ":" + EfcUtils.normalise(this.statement.getText());
            }
            catch (RuntimeException e) {
                // syntax error
                return this.weight.getValue() + ":" + this.statement.getText();
            }
        }

        /**
         * Performs the cell action bound the the action button.
         */
        private void action() {
            this.action.getActionListeners()[0].actionPerformed(null);
        }

    }

    /**
     * Initialises the weight descriptor configuration dialog.
     *
     * @param main the controller of the main GUI
     * @param parent the reference to the parent JFrame (EfcMainFrame)
     */
    public EfcWaypointPanel(EfcMainFrame main) {
        this.main = main;
        // subelements
        this.waypoints = new JPanel();
        this.waypoints.setPreferredSize(new Dimension(CELL_SIZE.width, CELL_SIZE.height * 20));
        this.waypoints.setLayout(new BoxLayout(waypoints, BoxLayout.PAGE_AXIS));
        JScrollPane pane = new JScrollPane(waypoints,
                                           ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setAutoscrolls(true);
        pane.getVerticalScrollBar().setUnitIncrement(24);
        this.add(pane);
        JPanel buttons = new JPanel(new GridLayout(2, 1));
        JButton load = new JButton("<<");
        load.setFont(EfcMainFrame.MONOBOLD);
        load.setFocusPainted(false);
        buttons.add(load);
        JButton apply = new JButton(">>");
        apply.setFont(EfcMainFrame.MONOBOLD);
        apply.setFocusPainted(false);
        buttons.add(apply);
        this.add(buttons, BorderLayout.EAST);

        this.load(Collections.EMPTY_LIST);
        // setup events
        // commit.addActionListener(e -> this.commit());
        // abort.addActionListener(e -> this.abort());
    }

    /** The size of a cell in the weight descriptor configuration dialog. */
    private static final Dimension CELL_SIZE = new Dimension(250, 24);
    /** The size of the action button in a cell. */
    private static final Dimension ACTION_SIZE = new Dimension(24, 24);
    /** String constant of the {@code Remove} action. */
    private static final String REMOVE = "-";
    /** String constant of the {@code Add} action. */
    private static final String ADD = "+";

    /** Reference to the main window's controller. */
    private EfcMainFrame main;
    /** Panel for holding all weight configuration cells. */
    private JPanel waypoints;

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
     * Opens this configuration dialog and load in the settings from the main
     * GUI.
     *
     * @param weights the weights descriptor string
     */
    public void config(String weights) {
        this.load(EfcUtils.dissect(weights));
        this.setVisible(true);
    }

    /**
     * Loads in the weight descriptor cells.
     *
     * @param list the list of descriptor entries
     */
    private void load(List<Entry<String, Integer>> list) {
        this.waypoints.removeAll();
        this.waypoints.add(new EfcWeightCell(false));
        for (int i = 0; i < list.size(); i++) {
            Entry<String, Integer> entry = list.get(i);
            EfcWeightCell cell = (EfcWeightCell) this.waypoints.getComponent(i);
            cell.action();
            // cell.realise(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Returns the weight descriptor string of the settings from this
     * configuration dialog.
     * @return the weight descriptor string of the settings from this
     *         configuration dialog
     */
    @Override
    public String toString() {
        return Arrays.stream(this.waypoints.getComponents())
                     .map(EfcWeightCell.class::cast)
                     .filter(EfcWeightCell::isRealised)
                     .map(EfcWeightCell::toString)
                     .collect(Collectors.joining(";"));
    }

}
