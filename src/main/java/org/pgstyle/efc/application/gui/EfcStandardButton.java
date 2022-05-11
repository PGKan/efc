package org.pgstyle.efc.application.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class EfcStandardButton extends JButton {
    
    public EfcStandardButton(String text, ActionListener action) {
        super(text);
        this.addActionListener(action);
        this.setMargin(new Insets(2, 2, 2, 2));
        this.setFocusPainted(false);
        this.setFont(EfcMainFrame.MONOBOLD);
    }
}
