package org.pgstyle.efc.application.gui;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;

public class EfcStandardSpinner extends JSpinner {

    public EfcStandardSpinner(SpinnerModel model) {
        super(model);
        this.textField = ((JSpinner.DefaultEditor) this.getEditor()).getTextField();
        this.setFont(EfcMainFrame.MONO);
        this.addMouseWheelListener(event -> {
            this.textField.requestFocus();
            if (this.isEnabled() && this.isVisible() && this.textField.isFocusOwner()) {
                try {
                    JSpinner spinner = ((JSpinner) event.getComponent());
                    spinner.setValue(event.getWheelRotation() < 0 ? spinner.getNextValue() : spinner.getPreviousValue());
                }
                catch (RuntimeException ex) {
                    // NOP
                }
            }
        });
    }

    private final JTextField textField;

    public void setColumns(int columns) {
        ((JSpinner.DefaultEditor) this.getEditor()).getTextField().setColumns(columns);
    }
}
