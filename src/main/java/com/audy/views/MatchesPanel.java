package com.audy.views;

import com.audy.models.Track;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Displays search results (matches) after the user presses Enter (RF-03).
 * Shows empty state when no results are found (RF-10).
 */
public class MatchesPanel extends JPanel {

    private final JPanel resultsContainer;
    private final JLabel titleLabel;
    private Consumer<Track> onTrackSelected;
    private Runnable onBackHome;

    public MatchesPanel() {
        setBackground(UIHelper.BG_PRIMARY);
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel header = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        titleLabel = UIHelper.createLabel("Search Results", UIHelper.HEADING_FONT, UIHelper.TEXT_PRIMARY);
        JButton homeBtn = UIHelper.createButton("\u2190 Home", UIHelper.BG_DARK, Color.WHITE);
        homeBtn.setPreferredSize(new Dimension(110, 35));
        homeBtn.addActionListener(e -> { if (onBackHome != null) onBackHome.run(); });

        header.add(titleLabel, BorderLayout.WEST);
        header.add(homeBtn, BorderLayout.EAST);

        // --- Scrollable results ---
        resultsContainer = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(resultsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    // --- Public API ---

    public void setOnTrackSelected(Consumer<Track> handler) { this.onTrackSelected = handler; }
    public void setOnBackHome(Runnable handler)             { this.onBackHome = handler; }

    /** Populates the panel with search results. */
    public void setResults(List<Track> tracks, String query) {
        resultsContainer.removeAll();
        titleLabel.setText("Results for \u201C" + query + "\u201D");

        if (tracks == null || tracks.isEmpty()) {
            resultsContainer.add(buildEmptyState());
        } else {
            for (Track track : tracks) {
                resultsContainer.add(buildTrackCard(track));
                resultsContainer.add(Box.createVerticalStrut(2));
            }
        }
        resultsContainer.revalidate();
        resultsContainer.repaint();
    }

    // --- Private helpers ---

    private JPanel buildTrackCard(Track track) {
        JPanel card = UIHelper.createPanel(UIHelper.BG_SECONDARY);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 30, 2, 30),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIHelper.BORDER, 1),
                        BorderFactory.createEmptyBorder(12, 16, 12, 16)
                )
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel info = UIHelper.createPanel(UIHelper.BG_SECONDARY);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(UIHelper.createLabel(track.getTitle(), UIHelper.BODY_FONT, UIHelper.TEXT_PRIMARY));
        info.add(UIHelper.createLabel(track.getArtist().getName(), UIHelper.SMALL_FONT, UIHelper.TEXT_SECONDARY));

        JLabel pop = UIHelper.createLabel("Pop: " + track.getPopularity(),
                UIHelper.SMALL_FONT, UIHelper.ACCENT);

        card.add(info, BorderLayout.CENTER);
        card.add(pop, BorderLayout.EAST);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onTrackSelected != null) onTrackSelected.accept(track);
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(UIHelper.HOVER); info.setBackground(UIHelper.HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(UIHelper.BG_SECONDARY); info.setBackground(UIHelper.BG_SECONDARY);
            }
        });
        return card;
    }

    private JPanel buildEmptyState() {
        JPanel wrapper = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        wrapper.setLayout(new GridBagLayout());

        JPanel inner = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel msg = UIHelper.createLabel("No matching songs found.",
                UIHelper.BODY_FONT, UIHelper.TEXT_SECONDARY);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn = UIHelper.createButton("New Search", UIHelper.ACCENT, Color.WHITE);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> { if (onBackHome != null) onBackHome.run(); });

        inner.add(msg);
        inner.add(Box.createVerticalStrut(15));
        inner.add(btn);
        wrapper.add(inner);
        return wrapper;
    }
}
