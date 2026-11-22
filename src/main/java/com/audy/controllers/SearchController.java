package com.audy.controllers;

import com.audy.api.APIClientException;
import com.audy.models.Track;
import com.audy.services.DiscoveryEngine;
import com.audy.views.MainFrame;

import javax.swing.*;
import java.util.List;

/**
 * Controller for search operations (RF-01, RF-02, RF-03, RF-04).
 * Connects HomePanel and MatchesPanel to DiscoveryEngine.
 * Implements client-side debounce using javax.swing.Timer (RNF-04).
 */
public class SearchController {

    private final MainFrame mainFrame;
    private final DiscoveryEngine engine;
    private final RecommendationController recoController;
    private Timer debounceTimer;

    public SearchController(MainFrame mainFrame, DiscoveryEngine engine,
                            RecommendationController recoController) {
        this.mainFrame = mainFrame;
        this.engine = engine;
        this.recoController = recoController;
        wireListeners();
    }

    private void wireListeners() {
        // RF-02: Predictive search while typing
        mainFrame.getHomePanel().setOnPredictiveSearch(this::handlePredictiveSearch);

        // RF-03: Full search on Enter
        mainFrame.getHomePanel().setOnSearch(this::handleSearch);

        // RF-04: Track selected from predict dropdown
        mainFrame.getHomePanel().setOnTrackSelected(this::handleTrackFromPredict);

        // RF-04: Track selected from matches list
        mainFrame.getMatchesPanel().setOnTrackSelected(this::handleTrackFromMatches);
        mainFrame.getMatchesPanel().setOnBackHome(this::goHome);
    }

    private int currentPredictSeq = 0;

    // RF-02: Predictive suggestions with 350 ms client-side debounce (RNF-04).
    private void handlePredictiveSearch(String partialText) {
        if (debounceTimer != null && debounceTimer.isRunning()) {
            debounceTimer.stop();
        }
        final int seq = ++currentPredictSeq;
        debounceTimer = new Timer(350, e -> {
            new SwingWorker<List<Track>, Void>() {
                @Override protected List<Track> doInBackground() throws Exception {
                    return engine.getPredictiveSuggestions(partialText);
                }
                @Override protected void done() {
                    try {
                        if (seq != currentPredictSeq) return; // Stale query, discard
                        List<Track> suggestions = get();
                        if (suggestions != null && !suggestions.isEmpty()) {
                            mainFrame.getHomePanel().showPredictions(suggestions);
                        } else {
                            mainFrame.getHomePanel().hidePredictions();
                        }
                    } catch (Exception ex) {
                        if (seq == currentPredictSeq) {
                            mainFrame.getHomePanel().showPredictError(extractMessage(ex));
                        }
                    }
                }
            }.execute();
        });
        debounceTimer.setRepeats(false);
        debounceTimer.start();
    }

    // RF-03: Full search when user presses Enter.
    private void handleSearch(String query) {
        if (debounceTimer != null) debounceTimer.stop();
        mainFrame.getHomePanel().hidePredictions();
        mainFrame.showLoading("Searching for \u201C" + query + "\u201D\u2026");

        new SwingWorker<List<Track>, Void>() {
            @Override protected List<Track> doInBackground() throws Exception {
                return engine.searchMatches(query);
            }
            @Override protected void done() {
                try {
                    mainFrame.showMatches(get(), query);
                } catch (Exception ex) {
                    mainFrame.showError("Search error: " + extractMessage(ex),
                            () -> handleSearch(query));
                }
            }
        }.execute();
    }

    // RF-04: Track selected from predictive suggestions.
    private void handleTrackFromPredict(Track track) {
        if (debounceTimer != null) debounceTimer.stop();
        mainFrame.getHomePanel().hidePredictions();
        recoController.generateRecommendations(track);
    }

    // RF-04: Track selected from search results.
    private void handleTrackFromMatches(Track track) {
        if (debounceTimer != null) debounceTimer.stop();
        mainFrame.getHomePanel().hidePredictions();
        recoController.generateRecommendations(track);
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
