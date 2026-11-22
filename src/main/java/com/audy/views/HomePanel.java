package com.audy.views;

import com.audy.models.Track;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Home screen: AUDY branding, search bar with predictive dropdown (RF-01, RF-02).
 */
public class HomePanel extends JPanel {

    private static final String PLACEHOLDER = "Search for a song\u2026";

    private final JTextField searchField;
    private final JPopupMenu predictDropdown;

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

        // --- Predictive dropdown ---
        predictDropdown = new JPopupMenu();
        predictDropdown.setBackground(UIHelper.BG_PRIMARY);

        // --- Events ---
        searchField.addActionListener(e -> fireSearch());
        searchBtn.addActionListener(e -> fireSearch());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { firePredict(); }
            public void removeUpdate(DocumentEvent e)  { firePredict(); }
            public void changedUpdate(DocumentEvent e)  { firePredict(); }
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

    // --- Public API for controllers ---

    public void setOnSearch(Consumer<String> handler)           { this.onSearch = handler; }
    public void setOnPredictiveSearch(Consumer<String> handler) { this.onPredictiveSearch = handler; }
    public void setOnTrackSelected(Consumer<Track> handler)     { this.onTrackSelected = handler; }

    /** Updates the predictive dropdown with suggestions (RF-02). */
    public void showPredictions(List<Track> tracks) {
        predictDropdown.removeAll();
        if (tracks == null || tracks.isEmpty()) {
            JMenuItem noResults = new JMenuItem("No suggestions found");
            noResults.setEnabled(false);
            noResults.setFont(UIHelper.SMALL_FONT);
            predictDropdown.add(noResults);
        } else {
            for (Track track : tracks) {
                String label = track.getTitle() + " \u2014 " + track.getArtist().getName();
                JMenuItem item = new JMenuItem(label);
                item.setFont(UIHelper.BODY_FONT);
                item.addActionListener(e -> {
                    predictDropdown.setVisible(false);
                    if (onTrackSelected != null) onTrackSelected.accept(track);
                });
                predictDropdown.add(item);
            }
        }
        if (searchField.isShowing()) {
            predictDropdown.show(searchField, 0, searchField.getHeight());
        }
    }

    /** Shows an error inside the predict dropdown (RF-11). */
    public void showPredictError(String message) {
        predictDropdown.removeAll();
        JMenuItem err = new JMenuItem("\u26A0 " + message);
        err.setEnabled(false);
        err.setForeground(UIHelper.ERROR_RED);
        err.setFont(UIHelper.SMALL_FONT);
        predictDropdown.add(err);
        if (searchField.isShowing()) {
            predictDropdown.show(searchField, 0, searchField.getHeight());
        }
    }

    /** Resets the search field to its placeholder state. */
    public void clearSearch() {
        searchField.setText(PLACEHOLDER);
        searchField.setForeground(UIHelper.TEXT_SECONDARY);
        predictDropdown.setVisible(false);
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
            predictDropdown.setVisible(false);
        }
    }
}
