package com.audy.views;

import com.audy.models.Track;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Main application window using CardLayout for screen navigation (F4, F5).
 * Acts as a facade for controllers to switch between screens.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    // Named panels
    private final HomePanel homePanel;
    private final MatchesPanel matchesPanel;
    private final RecommendationsPanel recommendationsPanel;
    private final DetailPanel detailPanel;

    // Screen identifiers
    public static final String HOME            = "HOME";
    public static final String MATCHES         = "MATCHES";
    public static final String RECOMMENDATIONS = "RECOMMENDATIONS";
    public static final String DETAIL          = "DETAIL";
    public static final String LOADING         = "LOADING";
    public static final String ERROR           = "ERROR";

    // Dynamic panels (recreated per use)
    private JPanel loadingPanel;
    private JPanel errorPanel;
    private Runnable retryAction;

    public MainFrame() {
        super("AUDY \u2014 Music Discovery System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(UIHelper.WINDOW_WIDTH, UIHelper.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(780, 500));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        homePanel              = new HomePanel();
        matchesPanel           = new MatchesPanel();
        recommendationsPanel   = new RecommendationsPanel();
        detailPanel            = new DetailPanel();
        loadingPanel           = buildLoadingPanel("Loading\u2026");
        errorPanel             = buildErrorPanel("An error occurred.");

        contentPanel.add(homePanel,              HOME);
        contentPanel.add(matchesPanel,           MATCHES);
        contentPanel.add(recommendationsPanel,   RECOMMENDATIONS);
        contentPanel.add(detailPanel,            DETAIL);
        contentPanel.add(loadingPanel,           LOADING);
        contentPanel.add(errorPanel,             ERROR);

        setContentPane(contentPanel);
    }

    // --- Navigation API for controllers ---

    public void showScreen(String name) {
        cardLayout.show(contentPanel, name);
    }

    public void showLoading(String message) {
        contentPanel.remove(loadingPanel);
        loadingPanel = buildLoadingPanel(message);
        contentPanel.add(loadingPanel, LOADING);
        showScreen(LOADING);
    }

    public void showError(String message, Runnable onRetry) {
        this.retryAction = onRetry;
        contentPanel.remove(errorPanel);
        errorPanel = buildErrorPanel(message);
        contentPanel.add(errorPanel, ERROR);
        showScreen(ERROR);
    }

    public void showMatches(List<Track> tracks, String query) {
        matchesPanel.setResults(tracks, query);
        showScreen(MATCHES);
    }

    public void showRecommendations(Track reference, List<Track> recommendations) {
        recommendationsPanel.setData(reference, recommendations);
        showScreen(RECOMMENDATIONS);
    }

    public void showDetail(Track track) {
        detailPanel.setTrack(track);
        showScreen(DETAIL);
    }

    // --- Panel getters for controller wiring ---

    public HomePanel              getHomePanel()              { return homePanel; }
    public MatchesPanel           getMatchesPanel()           { return matchesPanel; }
    public RecommendationsPanel   getRecommendationsPanel()   { return recommendationsPanel; }
    public DetailPanel            getDetailPanel()            { return detailPanel; }

    // --- Factory helpers for dynamic panels ---

    private JPanel buildLoadingPanel(String message) {
        JPanel panel = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        panel.setLayout(new GridBagLayout());

        JPanel center = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel icon = UIHelper.createLabel("\u266B", new Font("SansSerif", Font.PLAIN, 52), UIHelper.ACCENT);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = UIHelper.createLabel(message, UIHelper.HEADING_FONT, UIHelper.TEXT_PRIMARY);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(icon);
        center.add(Box.createVerticalStrut(20));
        center.add(msg);
        panel.add(center);
        return panel;
    }

    private JPanel buildErrorPanel(String message) {
        JPanel panel = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        panel.setLayout(new GridBagLayout());

        JPanel center = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel icon = UIHelper.createLabel("\u26A0", new Font("SansSerif", Font.PLAIN, 52), UIHelper.ERROR_RED);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = UIHelper.createLabel(
                "<html><div style='text-align:center;width:340px'>" + message + "</div></html>",
                UIHelper.BODY_FONT, UIHelper.TEXT_PRIMARY);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttons = UIHelper.createPanel(UIHelper.BG_PRIMARY);
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));

        JButton retryBtn = UIHelper.createButton("Retry", UIHelper.ACCENT, Color.WHITE);
        retryBtn.addActionListener(e -> { if (retryAction != null) retryAction.run(); });

        JButton homeBtn = UIHelper.createButton("Home", UIHelper.BG_DARK, Color.WHITE);
        homeBtn.addActionListener(e -> showScreen(HOME));

        buttons.add(retryBtn);
        buttons.add(homeBtn);

        center.add(icon);
        center.add(Box.createVerticalStrut(15));
        center.add(msg);
        center.add(Box.createVerticalStrut(25));
        center.add(buttons);
        panel.add(center);
        return panel;
    }
}
