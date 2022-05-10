package org.pgstyle.efc.application.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.pgstyle.efc.application.cli.CmdUtils;
import org.pgstyle.efc.application.common.EfcConfig;
import org.pgstyle.efc.application.common.EfcResources;
import org.pgstyle.efc.application.common.EfcUtils;
import org.pgstyle.rst2.application.gui.EfcStandardButton;

/**
 * <p>
 * Frame controller of graphical interface of the {@code RandomStringTools}.
 * </p>
 * <p>
 * Refactor of the {@code org.pgs.efc.windows.MainWindow} class.
 * </p>
 *
 * @since efc-1
 * @version efc-2.0
 * @author PGKan
 */
public final class EfcMainFrame {

    public static final Font MONO;
    public static final Font MONOBOLD;
    public static final Image ICON;

    static {
        // enable font anti-aliasing to prevent bad looking font edges
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");
        Function<String, Font> getFont = key -> {
            try {
                return Font.createFont(Font.TRUETYPE_FONT, EfcResources.getStream(key)).deriveFont(Font.PLAIN, 13);
            } catch (FontFormatException | IOException e) {
                CmdUtils.stderr(EfcUtils.stackTraceOf(e));
                return null;
            }
        };
        MONO = getFont.apply("efc.font.mono");
        MONOBOLD = getFont.apply("efc.font.monobold");
        Image icon = null;
        try {
            icon = ImageIO.read(EfcResources.getStream("efc.icon.efc"));
        } catch (IOException | RuntimeException e) {
            CmdUtils.stderr(EfcUtils.stackTraceOf(e));
        }
        ICON = icon;
    }

    /**
     * Initialises the main window of the {@code RandomStringTools}
     * @param config the configuration loaded from the command line
     */
    public EfcMainFrame(EfcConfig config) {
        this.efcConfig = config;
        // setup the GUI appearance
        this.frame = new JFrame("EFC - Elytra Flight Computer");
        this.frame.setSize(640, 480);
        this.frame.setResizable(false);
        this.frame.setLocationRelativeTo(null);
        this.frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.frame.getContentPane().setLayout(new BorderLayout());
        this.frame.setIconImage(EfcMainFrame.ICON);

        // setup close events
        Thread main = Thread.currentThread();
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public synchronized void windowClosing(WindowEvent e) {
                EfcMainFrame.this.closed = true;
                main.interrupt();
            }
        });

        // create subelements
        // this.weightsConfig = new EfcWeightsFrame(this, this.frame);
        this.waypoints = new EfcWaypointPanel(this);
        JPanel ioPanel = new JPanel();
        ioPanel.setLayout(new BorderLayout());
        ioPanel.add(this.makeInputPanel(), BorderLayout.NORTH);
        ioPanel.add(this.makeOutputPanel(), BorderLayout.CENTER);
        this.frame.add(this.waypoints, BorderLayout.WEST);
        this.frame.add(ioPanel, BorderLayout.CENTER);

        // load setting before showing the window
        this.frame.pack();
        this.loadDefault();
        this.waypoints.load();
        this.frame.setVisible(true);
    }

    /** Stored configurations. */
    private EfcConfig efcConfig;
    private EfcWaypointPanel waypoints;
    /** Closing state of the main window. */
    private boolean closed;

    /** Reference to the main window. */
    private JFrame frame;
    /** The input field of the encoded flight plan. */
    private JTextArea flightPlan;
    /** Text area for outputting generated string or program messages. */
    private JTextArea output;

    /**
     * Returns {@code true} if the main window is closed.
     *
     * @return {@code true} if the main window is closed; of {@code false}
     *         otherwise
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * Get flight plan from the flight plan text area.
     */
    public String getFlightPlan() {
        return this.flightPlan.getText();
    }

    /**
     * Commits all configurations and engages the randomiser.
     */
    public void updateFlightPlan(String flightPlan) {
        this.flightPlan.setText(flightPlan);
    }

    /**
     * Resets the output text field of the main window and prints string onto it.
     *
     * @param string the string to be printed
     */
    public void rewrite(String string) {
        this.output.setText("");
        this.write(string);
    }

    /**
     * Prints string onto the output text field of the main window.
     *
     * @param string the string to be printed
     */
    public void write(String string) {
        this.output.append(string);
        this.output.append(System.lineSeparator());
        this.output.setCaretPosition(this.output.getDocument().getLength());
    }

    /**
     * Creates the subelements for input box.
     *
     * @return a {@code JPanel} containing the subelements
     */
    private JPanel makeInputPanel() {
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        // subelements
        JLabel label = new JLabel("Flight Plan:");
        label.setFont(EfcMainFrame.MONOBOLD);
        this.flightPlan = new JTextArea("");
        JScrollPane pane = new JScrollPane(this.flightPlan,
                                                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.flightPlan.setRows(4);
        this.flightPlan.setFont(MONO);
        this.flightPlan.setLineWrap(true);
        this.flightPlan.setEditable(true);
        this.flightPlan.setBorder(BorderFactory.createEtchedBorder());

        // setup layout
        EfcMainFrame.setupPanelLayout(layout,
                                      label,
                                      new EfcStandardButton("CPY", e -> this.copy(this.flightPlan)),
                                      new EfcStandardButton("OUT", e -> this.saveFlightPlan()),
                                      pane);

        return panel;
    }

    /**
     * Creates the subelements for output box.
     *
     * @return a {@code JPanel} containing the subelements
     */
    private JPanel makeOutputPanel() {
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        // subelements
        JLabel label = new JLabel("Output:");
        label.setFont(EfcMainFrame.MONOBOLD);
        this.output = new JTextArea("");
        JScrollPane pane = new JScrollPane(this.output,
                                                 ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.output.setColumns(64);
        this.output.setFont(MONO);
        this.output.setLineWrap(true);
        this.output.setEditable(false);
        this.output.setBorder(BorderFactory.createEtchedBorder());

        // setup layout
        EfcMainFrame.setupPanelLayout(layout,
                                      label,
                                      new EfcStandardButton("CPY", e -> this.copy(this.output)),
                                      new EfcStandardButton("OUT", e -> this.saveOutput()),
                                      pane);

        return panel;
    }

    private static void setupPanelLayout(GroupLayout layout, JLabel label, EfcStandardButton copy, EfcStandardButton save, JScrollPane pane) {
        // setup layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(
                            layout.createSequentialGroup()
                                        .addGap(5).addComponent(label)
                                        .addGap(5).addComponent(copy)
                                        .addGap(5).addComponent(save)
                        )
                        .addGroup(
                            layout.createSequentialGroup()
                                  .addGap(5).addComponent(pane).addGap(5)
                        )
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                        .addGroup(
                            layout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(label)
                                        .addComponent(copy)
                                        .addComponent(save)
                        )
                        .addGroup(
                            layout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(pane)
                        )
                        .addGap(5)
        );
    }

    /**
     * Loads the configuration from command line into the main window.
     *
     * @param config the configuration container
     */
    public void loadDefault() {
        this.flightPlan.setText(this.efcConfig.flightPlan());
        // print to output text area
        this.write("loaded settings from EfcConfig/CommandLineArguments");
        this.write("flightPlan = " + this.efcConfig.flightPlan());
        this.waypoints.load();
    }

    /**
     * Copies text from the output text area to the system clipboard.
     */
    private void copy(JTextArea target) {
        target.select(0, Integer.MAX_VALUE);
        target.requestFocus();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(target.getText()), null);
    }

    /**
     * Saves text from the input text area to a specified file.
     */
    private void saveFlightPlan() {
        // select file in current working directory
        JFileChooser fc = new JFileChooser(".");
        // efp is the default file extension, but other also supported
        fc.setSelectedFile(Paths.get("flightplan.efp").toFile());
        fc.setFileFilter(new FileNameExtensionFilter("EFC Flight Plan (*.efp)", "efp"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Plain Text (*.txt)", "txt"));
        // show save dialog and perform save action
        this.save(fc, this.flightPlan);
    }

    /**
     * Saves text from the output text area to a specified file.
     */
    private void saveOutput() {
        // select file in current working directory
        JFileChooser fc = new JFileChooser(".");
        // efc is the default file extension, but other also supported
        fc.setSelectedFile(Paths.get("output.efc").toFile());
        fc.setFileFilter(new FileNameExtensionFilter("EFC Output (*.efc)", "efc"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Plain Text (*.txt)", "txt"));
        // show save dialog and perform save action
        this.save(fc, this.output);
    }

    private void save(JFileChooser fc, JTextArea ta) {
        if (fc.showDialog(this.frame, "Save") == JFileChooser.APPROVE_OPTION) {
            // setup file extension if required but not given
            FileFilter filter = fc.getFileFilter();
            String file = filter.accept(fc.getSelectedFile()) ?
                              fc.getSelectedFile().toString() :
                              fc.getSelectedFile().toString() + "." + ((FileNameExtensionFilter) filter).getExtensions()[0];
            try (PrintStream ps = EfcUtils.openFile(Paths.get(file).toFile())) {
                this.rewrite(String.format("Wrote %d bytes to '%s'", EfcUtils.write(ps, ta.getText().trim()), file));
            }
            catch (IOException e) {
                this.rewrite(EfcUtils.stackTraceOf(e));
                this.write("Failed to write file, " + EfcUtils.messageOf(e));
            }
        }
    }

}
