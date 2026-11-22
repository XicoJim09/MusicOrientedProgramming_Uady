package com.audy.views;

import com.audy.models.Track;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Displays the reference track and its generated recommendations (RF-08).
 * Shows empty state when no recommendations are found (RF-10).
 */
public class RecommendationsPanel extends JPanel {

    private final JPanel referenceContainer;
    private final JPanel recsContainer;
    private Consumer<Track> onTrackSelected;
    private Runnable onBackHome;

    public RecommendationsPanel() {
        setBackground(UIHelper.BG_PRIMARY);
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel header = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel title = UIHelper.createLabel("Recommendations", UIHelper.HEADING_FONT, UIHelper.TEXT_PRIMARY);
        JButton homeBtn = UIHelper.createButton("\u2190 Home", UIHelper.BG_DARK, Color.WHITE);
        homeBtn.setPreferredSize(new Dimension(110, 35));
        homeBtn.addActionListener(e -> { if (onBackHome != null) onBackHome.run(); });

        header.add(title, BorderLayout.WEST);
        header.add(homeBtn, BorderLayout.EAST);

        // --- Body: reference + recommendations ---
        JPanel body = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        body.setLayout(new BorderLayout());

        referenceContainer = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        referenceContainer.setLayout(new BoxLayout(referenceContainer, BoxLayout.Y_AXIS));
        referenceContainer.setBorder(BorderFactory.createEmptyBorder(0, 30, 10, 30));

        recsContainer = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        recsContainer.setLayout(new BoxLayout(recsContainer, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(recsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        body.add(referenceContainer, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    // --- Public API ---

    public void setOnTrackSelected(Consumer<Track> handler) { this.onTrackSelected = handler; }
    public void setOnBackHome(Runnable handler)             { this.onBackHome = handler; }

    /** Populates the panel with the reference track and its recommendations. */
    public void setData(Track reference, List<Track> recommendations) {
        referenceContainer.removeAll();
        recsContainer.removeAll();

        // Reference card
        referenceContainer.add(UIHelper.createLabel("Based on:",
                UIHelper.SMALL_FONT, UIHelper.TEXT_SECONDARY));
        referenceContainer.add(UIHelper.createLabel(
                reference.getTitle() + " \u2014 " + reference.getArtist().getName(),
                UIHelper.HEADING_FONT, UIHelper.ACCENT));
        referenceContainer.add(UIHelper.createLabel("Popularity: " + reference.getPopularity(),
                UIHelper.SMALL_FONT, UIHelper.TEXT_SECONDARY));
        referenceContainer.add(Box.createVerticalStrut(10));
        referenceContainer.add(new JSeparator());

        if (recommendations == null || recommendations.isEmpty()) {
            recsContainer.add(buildEmptyState());
        } else {
            JLabel count = UIHelper.createLabel(
                    recommendations.size() + " songs discovered",
                    UIHelper.SMALL_FONT, UIHelper.TEXT_SECONDARY);
            count.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));
            recsContainer.add(count);

            for (int i = 0; i < recommendations.size(); i++) {
                recsContainer.add(buildRecCard(recommendations.get(i), i + 1));
                recsContainer.add(Box.createVerticalStrut(2));
            }
        }

        referenceContainer.revalidate();
        recsContainer.revalidate();
        repaint();
    }

    // --- Private helpers ---

    private JPanel buildRecCard(Track track, int index) {
        JPanel card = UIHelper.createPanel(UIHelper.BG_SECONDARY);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 30, 2, 30),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIHelper.BORDER, 1),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                )
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel num = UIHelper.createLabel(String.valueOf(index), UIHelper.BODY_FONT, UIHelper.TEXT_SECONDARY);
        num.setPreferredSize(new Dimension(30, 20));

        JPanel info = UIHelper.createPanel(UIHelper.BG_SECONDARY);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(UIHelper.createLabel(track.getTitle(), UIHelper.BODY_FONT, UIHelper.TEXT_PRIMARY));
        info.add(UIHelper.createLabel(track.getArtist().getName(), UIHelper.SMALL_FONT, UIHelper.TEXT_SECONDARY));

        JLabel pop = UIHelper.createLabel("Pop: " + track.getPopularity(), UIHelper.SMALL_FONT, UIHelper.ACCENT);

        card.add(num, BorderLayout.WEST);
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

        JLabel msg = UIHelper.createLabel("No recommendations found.",
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
