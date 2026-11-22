package com.audy.views;

import com.audy.models.Track;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.URI;

/**
 * Displays detailed metadata of a selected recommendation (RF-09).
 * Provides external player links and 30s audio previews.
 */
public class DetailPanel extends JPanel {

    private final JLabel titleLabel;
    private final JLabel artistLabel;
    private final JLabel popularityLabel;
    private final JLabel genresLabel;
    private final JButton playerButton;
    private final JButton previewButton;
    private Runnable onBack;
    private Runnable onBackHome;

    public DetailPanel() {
        setBackground(UIHelper.BG_PRIMARY);
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel header = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel headerTitle = UIHelper.createLabel("Song Details", UIHelper.HEADING_FONT, UIHelper.TEXT_PRIMARY);

        JPanel headerBtns = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        headerBtns.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton backBtn = UIHelper.createButton("\u2190 Back", UIHelper.BG_SECONDARY, UIHelper.TEXT_PRIMARY);
        backBtn.setPreferredSize(new Dimension(100, 35));
        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });
        JButton homeBtn = UIHelper.createButton("Home", UIHelper.BG_DARK, Color.WHITE);
        homeBtn.setPreferredSize(new Dimension(100, 35));
        homeBtn.addActionListener(e -> { if (onBackHome != null) onBackHome.run(); });
        headerBtns.add(backBtn);
        headerBtns.add(homeBtn);

        header.add(headerTitle, BorderLayout.WEST);
        header.add(headerBtns, BorderLayout.EAST);

        // --- Card ---
        JPanel content = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        content.setLayout(new GridBagLayout());

        JPanel card = UIHelper.createPanel(UIHelper.BG_SECONDARY);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER, 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(520, 340));

        titleLabel      = UIHelper.createLabel("", new Font("SansSerif", Font.BOLD, 22), UIHelper.TEXT_PRIMARY);
        artistLabel     = UIHelper.createLabel("", UIHelper.HEADING_FONT, UIHelper.TEXT_SECONDARY);
        popularityLabel = UIHelper.createLabel("", UIHelper.BODY_FONT, UIHelper.ACCENT);
        genresLabel     = UIHelper.createLabel("", UIHelper.SMALL_FONT, UIHelper.TEXT_SECONDARY);

        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        popularityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genresLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        playerButton  = UIHelper.createButton("Open in Deezer \u2197", UIHelper.ACCENT, Color.WHITE);
        previewButton = UIHelper.createButton("Play Preview \u25B6", UIHelper.BG_DARK, Color.WHITE);

        JPanel buttons = UIHelper.createPanel(UIHelper.BG_SECONDARY);
        buttons.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.add(playerButton);
        buttons.add(previewButton);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(artistLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(popularityLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(genresLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(buttons);
        content.add(card);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    // --- Public API ---

    public void setOnBack(Runnable handler)     { this.onBack = handler; }
    public void setOnBackHome(Runnable handler)  { this.onBackHome = handler; }

    /** Populates the detail card with track metadata. */
    public void setTrack(Track track) {
        titleLabel.setText(track.getTitle());
        artistLabel.setText(track.getArtist().getName());
        popularityLabel.setText("Popularity: " + track.getPopularity() + " / 100");
        genresLabel.setText(track.getGenres() != null && !track.getGenres().isEmpty()
                ? "Genres: " + String.join(", ", track.getGenres())
                : "Source: Deezer Music API");

        // Dynamic player button label
        String playerUrl = track.getPlayerUrl();
        if (playerUrl != null && playerUrl.contains("deezer.com")) {
            playerButton.setText("Open on Deezer \u2197");
        } else if (playerUrl != null && playerUrl.contains("spotify.com")) {
            playerButton.setText("Open on Spotify \u2197");
        } else {
            playerButton.setText("Play Track \u2197");
        }

        // Rewire player button
        for (java.awt.event.ActionListener al : playerButton.getActionListeners()) {
            playerButton.removeActionListener(al);
        }
        playerButton.setEnabled(playerUrl != null && !playerUrl.trim().isEmpty());
        playerButton.addActionListener(e -> openUrl(track.getPlayerUrl()));

        // Rewire preview button
        for (java.awt.event.ActionListener al : previewButton.getActionListeners()) {
            previewButton.removeActionListener(al);
        }
        boolean hasPreview = track.getPreviewUrl() != null && !track.getPreviewUrl().trim().isEmpty();
        previewButton.setVisible(hasPreview);
        if (hasPreview) {
            previewButton.addActionListener(e -> openUrl(track.getPreviewUrl()));
        }
    }

    private void openUrl(String url) {
        openBrowserUrl(this, url);
    }

    public static void openBrowserUrl(Component parent, String url) {
        if (url == null || url.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No player link available for this track.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean opened = false;

        // 1. Standard Java Desktop BROWSE action
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                opened = true;
            }
        } catch (Throwable ignored) {}

        // 2. Windows shell protocol handler / Unix handlers
        if (!opened) {
            String os = System.getProperty("os.name", "").toLowerCase();
            try {
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                    opened = true;
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", url});
                    opened = true;
                } else {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                    opened = true;
                }
            } catch (Throwable ignored) {}
        }

        // 3. cmd /c start fallback on Windows
        if (!opened) {
            try {
                new ProcessBuilder("cmd.exe", "/c", "start", "", url).start();
                opened = true;
            } catch (Throwable ignored) {}
        }

        // 4. Always copy to clipboard as an infallible guarantee
        try {
            StringSelection selection = new StringSelection(url);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        } catch (Throwable ignored) {}

        if (!opened) {
            JOptionPane.showMessageDialog(parent,
                    "Browser could not be launched directly.\nThe track link has been copied to your clipboard:\n" + url,
                    "Link Copied", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
