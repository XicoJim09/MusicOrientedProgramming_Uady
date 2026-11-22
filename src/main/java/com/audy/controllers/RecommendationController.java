package com.audy.controllers;

import com.audy.api.APIClientException;
import com.audy.models.Track;
import com.audy.services.DiscoveryEngine;
import com.audy.views.MainFrame;

import javax.swing.*;
import java.util.List;

/**
 * Controller for recommendation operations (RF-05, RF-06, RF-07, RF-08, RF-09).
 * Connects RecommendationsPanel and DetailPanel to DiscoveryEngine.
 */
public class RecommendationController {

    private final MainFrame mainFrame;
    private final DiscoveryEngine engine;

    public RecommendationController(MainFrame mainFrame, DiscoveryEngine engine) {
        this.mainFrame = mainFrame;
        this.engine = engine;
        wireListeners();
    }

    private void wireListeners() {
        // RF-09: Track selected from recommendations list → show detail
        mainFrame.getRecommendationsPanel().setOnTrackSelected(this::showDetail);
        mainFrame.getRecommendationsPanel().setOnBackHome(this::goHome);

        // Detail navigation
        mainFrame.getDetailPanel().setOnBack(this::goBackToRecommendations);
        mainFrame.getDetailPanel().setOnBackHome(this::goHome);
    }

    /**
     * RF-05/06/07/08: Generate recommendations from a reference track.
     * Executed asynchronously via SwingWorker to keep the UI responsive (RNF-01).
     */
    public void generateRecommendations(Track referenceTrack) {
        mainFrame.showLoading("Generating recommendations\u2026");

        new SwingWorker<List<Track>, Void>() {
            @Override protected List<Track> doInBackground() throws Exception {
                return engine.generateRecommendations(referenceTrack);
            }
            @Override protected void done() {
                try {
                    mainFrame.showRecommendations(referenceTrack, get());
                } catch (Exception ex) {
                    mainFrame.showError(
                            "Recommendation error: " + extractMessage(ex),
                            () -> generateRecommendations(referenceTrack));
                }
            }
        }.execute();
    }

    // RF-09: Show song detail.
    private void showDetail(Track track) {
        mainFrame.showDetail(track);
    }

    private void goBackToRecommendations() {
        mainFrame.showScreen(MainFrame.RECOMMENDATIONS);
    }

    private void goHome() {
        mainFrame.getHomePanel().clearSearch();
        mainFrame.showScreen(MainFrame.HOME);
    }

    private String extractMessage(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof APIClientException) return cause.getMessage();
        return e.getMessage() != null ? e.getMessage() : "Unknown error";
    }
}
