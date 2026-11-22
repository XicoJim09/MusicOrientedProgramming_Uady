package com.audy;

import com.audy.api.MusicBrainzAPIClient;
import com.audy.api.SpotifyAPIClient;
import com.audy.controllers.RecommendationController;
import com.audy.controllers.SearchController;
import com.audy.services.DiscoveryEngine;
import com.audy.views.MainFrame;

import javax.swing.*;

/**
 * AUDY application entry point.
 * Initialises the modular API clients (RNF-05), the discovery engine,
 * and launches the Swing UI with MVC controllers (F4, F5).
 */
public class Main {
    public static void main(String[] args) {
        // Modular API clients (RNF-05)
        SpotifyAPIClient spotifyApi       = new SpotifyAPIClient();
        MusicBrainzAPIClient musicBrainzApi = new MusicBrainzAPIClient();

        // Service layer
        DiscoveryEngine engine = new DiscoveryEngine(spotifyApi, musicBrainzApi);

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            MainFrame mainFrame = new MainFrame();

            // MVC Controllers (F5)
            RecommendationController recoCtrl =
                    new RecommendationController(mainFrame, engine);
            new SearchController(mainFrame, engine, recoCtrl);

            mainFrame.setVisible(true);
        });
    }
}
