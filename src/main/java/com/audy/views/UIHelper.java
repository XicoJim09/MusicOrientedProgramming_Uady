package com.audy.views;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class providing consistent styling across all AUDY views.
 * Centralizes colors, fonts, and reusable component factories.
 */
public class UIHelper {

    // --- Color palette (matches mockup design) ---
    public static final Color BG_PRIMARY     = new Color(255, 255, 255);
    public static final Color BG_SECONDARY   = new Color(245, 245, 245);
    public static final Color BG_DARK        = new Color(30, 30, 30);
    public static final Color TEXT_PRIMARY    = new Color(30, 30, 30);
    public static final Color TEXT_SECONDARY  = new Color(120, 120, 120);
    public static final Color ACCENT         = new Color(29, 185, 84);     // Spotify green
    public static final Color ERROR_RED      = new Color(220, 53, 69);
    public static final Color BORDER         = new Color(220, 220, 220);
    public static final Color HOVER          = new Color(235, 235, 235);

    // --- Fonts ---
    public static final Font TITLE_FONT   = new Font("SansSerif", Font.BOLD, 36);
    public static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 20);
    public static final Font BODY_FONT    = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font SMALL_FONT   = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font BUTTON_FONT  = new Font("SansSerif", Font.BOLD, 14);

    // --- Dimensions ---
    public static final int WINDOW_WIDTH  = 920;
    public static final int WINDOW_HEIGHT = 660;

    private UIHelper() {} // Prevent instantiation

    /** Creates a styled button. */
    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(BUTTON_FONT);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 38));
        return btn;
    }

    /** Creates a styled label. */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /** Creates a panel with the given background. */
    public static JPanel createPanel(Color bg) {
        JPanel panel = new JPanel();
        panel.setBackground(bg);
        return panel;
    }

    /** Creates a search text field with placeholder text (RF-01). */
    public static JTextField createSearchField(String placeholder) {
        JTextField field = new JTextField(30);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(400, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setText(placeholder);
        field.setForeground(TEXT_SECONDARY);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_SECONDARY);
                }
            }
        });
        return field;
    }
}
