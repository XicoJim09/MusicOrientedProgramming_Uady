package com.audy.views;

import com.audy.models.Track;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Home screen: AUDY branding, search bar with smooth predictive suggestions (RF-01, RF-02).
 * Uses a non-focus-stealing floating window (JWindow) to ensure typing is never interrupted
 * and suggestion clicks are 100% responsive without AWT menu-grab conflicts.
 */
public class HomePanel extends JPanel {

    private static final String PLACEHOLDER = "Search for a song\u2026";

    private final JTextField searchField;
    private JWindow predictWindow;
    private JPanel predictContainer;

    private Consumer<String> onSearch;
    private Consumer<String> onPredictiveSearch;
    private Consumer<Track>  onTrackSelected;

    public HomePanel() {
        setBackground(UIHelper.BG_PRIMARY);
        setLayout(new GridBagLayout());

        JPanel center = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // --- Logo ---
        JLabel logo = UIHelper.createLabel("AUDY", UIHelper.TITLE_FONT, UIHelper.TEXT_PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = UIHelper.createLabel("Music Discovery System",
                UIHelper.BODY_FONT, UIHelper.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = UIHelper.createLabel(
                "<html><div style='text-align:center;width:420px'>"
                + "Enter a song you like to discover similar music, genres, and artists you might enjoy."
                + "</div></html>",
                UIHelper.SMALL_FONT, UIHelper.TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Search bar ---
        JPanel searchRow = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        searchRow.setLayout(new FlowLayout(FlowLayout.CENTER));

        searchField = UIHelper.createSearchField(PLACEHOLDER);

        JButton searchBtn = UIHelper.createButton("Search", UIHelper.ACCENT, Color.WHITE);
        searchBtn.setPreferredSize(new Dimension(110, 40));

        searchRow.add(searchField);
        searchRow.add(searchBtn);

        // --- Events ---
        searchField.addActionListener(e -> {
            hidePredictions();
            fireSearch();
        });
        searchBtn.addActionListener(e -> {
            hidePredictions();
            fireSearch();
        });

        // Typing updates
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                hidePredictions();
                firePredict();
            }
            public void removeUpdate(DocumentEvent e) {
                hidePredictions();
                firePredict();
            }
            public void changedUpdate(DocumentEvent e) {
                hidePredictions();
                firePredict();
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hidePredictions();
                }
            }
        });

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // If focus lost to another application or window, hide popup
                Component opp = e.getOppositeComponent();
                if (opp != null && predictWindow != null && SwingUtilities.isDescendingFrom(opp, predictWindow)) {
                    return; // Focus transferred into popup
                }
                // Delay slightly so a mouse press in popup executes first
                Timer t = new Timer(150, evt -> hidePredictions());
                t.setRepeats(false);
                t.start();
            }
        });

        // --- Layout ---
        center.add(Box.createVerticalStrut(20));
        center.add(logo);
        center.add(Box.createVerticalStrut(6));
        center.add(subtitle);
        center.add(Box.createVerticalStrut(20));
        center.add(desc);
        center.add(Box.createVerticalStrut(30));
        center.add(searchRow);
        add(center);
    }

    private void ensurePredictWindow() {
        if (predictWindow == null) {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            predictWindow = new JWindow(parentWindow);
            predictWindow.setFocusableWindowState(false); // Never steals keyboard focus from searchField

            predictContainer = new JPanel();
            predictContainer.setLayout(new BoxLayout(predictContainer, BoxLayout.Y_AXIS));
            predictContainer.setBackground(UIHelper.BG_SECONDARY);
            predictContainer.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER, 1));

            predictWindow.setContentPane(predictContainer);

            if (parentWindow != null) {
                parentWindow.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentMoved(ComponentEvent e) { hidePredictions(); }
                    @Override
                    public void componentResized(ComponentEvent e) { hidePredictions(); }
                });
            }
        }
    }

    // --- Public API for controllers ---

    public void setOnSearch(Consumer<String> handler)           { this.onSearch = handler; }
    public void setOnPredictiveSearch(Consumer<String> handler) { this.onPredictiveSearch = handler; }
    public void setOnTrackSelected(Consumer<Track> handler)     { this.onTrackSelected = handler; }

    /** Updates the predictive dropdown with suggestions (RF-02). */
    public void showPredictions(List<Track> tracks) {
        if (!isShowing() || !searchField.isShowing() || !searchField.isFocusOwner()) {
            hidePredictions();
            return;
        }

        ensurePredictWindow();
        predictContainer.removeAll();

        if (tracks == null || tracks.isEmpty()) {
            JPanel emptyItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
            emptyItem.setBackground(UIHelper.BG_SECONDARY);
            JLabel label = UIHelper.createLabel("No matches found", UIHelper.SMALL_FONT, UIHelper.TEXT_SECONDARY);
            emptyItem.add(label);
            predictContainer.add(emptyItem);
        } else {
            for (Track track : tracks) {
                JPanel item = buildPredictionRow(track);
                predictContainer.add(item);
            }
        }

        predictContainer.revalidate();
        predictContainer.repaint();

        Point loc = searchField.getLocationOnScreen();
        int width = searchField.getWidth();
        predictWindow.pack();
        predictWindow.setSize(width, predictWindow.getPreferredSize().height);
        predictWindow.setLocation(loc.x, loc.y + searchField.getHeight() + 3);
        predictWindow.setVisible(true);
    }

    private JPanel buildPredictionRow(Track track) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(UIHelper.BG_SECONDARY);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIHelper.BORDER),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setBackground(UIHelper.BG_SECONDARY);

        JLabel titleLbl = UIHelper.createLabel(track.getTitle(), UIHelper.BODY_FONT, UIHelper.TEXT_PRIMARY);
        JLabel artistLbl = UIHelper.createLabel(track.getArtist().getName(), UIHelper.SMALL_FONT, UIHelper.TEXT_SECONDARY);
        textCol.add(titleLbl);
        textCol.add(artistLbl);

        JLabel popBadge = UIHelper.createLabel("Pop: " + track.getPopularity(), UIHelper.SMALL_FONT, UIHelper.ACCENT);
        popBadge.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

        row.add(textCol, BorderLayout.CENTER);
        row.add(popBadge, BorderLayout.EAST);

        Runnable doSelect = () -> {
            hidePredictions();
            searchField.setText(track.getTitle());
            if (onTrackSelected != null) {
                onTrackSelected.accept(track);
            }
        };

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                doSelect.run();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(UIHelper.HOVER);
                textCol.setBackground(UIHelper.HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(UIHelper.BG_SECONDARY);
                textCol.setBackground(UIHelper.BG_SECONDARY);
            }
        };

        row.addMouseListener(ma);
        titleLbl.addMouseListener(ma);
        artistLbl.addMouseListener(ma);
        popBadge.addMouseListener(ma);

        return row;
    }

    /** Explicitly closes the predictive dropdown. */
    public void hidePredictions() {
        if (predictWindow != null && predictWindow.isVisible()) {
            predictWindow.setVisible(false);
        }
    }

    /** Shows an error inside the predict dropdown (RF-11). */
    public void showPredictError(String message) {
        if (!isShowing() || !searchField.isShowing() || !searchField.isFocusOwner()) return;

        ensurePredictWindow();
        predictContainer.removeAll();

        JPanel errPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        errPanel.setBackground(UIHelper.BG_SECONDARY);
        JLabel err = UIHelper.createLabel("\u26A0 " + message, UIHelper.SMALL_FONT, UIHelper.ERROR_RED);
        errPanel.add(err);
        predictContainer.add(errPanel);

        Point loc = searchField.getLocationOnScreen();
        predictWindow.pack();
        predictWindow.setSize(searchField.getWidth(), predictWindow.getPreferredSize().height);
        predictWindow.setLocation(loc.x, loc.y + searchField.getHeight() + 3);
        predictWindow.setVisible(true);
    }

    /** Resets the search field to its placeholder state. */
    public void clearSearch() {
        searchField.setText(PLACEHOLDER);
        searchField.setForeground(UIHelper.TEXT_SECONDARY);
        hidePredictions();
    }

    // --- Private helpers ---

    private String getSearchText() {
        String text = searchField.getText();
        return PLACEHOLDER.equals(text) ? "" : text.trim();
    }

    private void fireSearch() {
        String query = getSearchText();
        if (!query.isEmpty() && onSearch != null) onSearch.accept(query);
    }

    private void firePredict() {
        String text = getSearchText();
        if (text.length() >= 2 && onPredictiveSearch != null) {
            onPredictiveSearch.accept(text);
        } else {
            hidePredictions();
        }
    }
}
