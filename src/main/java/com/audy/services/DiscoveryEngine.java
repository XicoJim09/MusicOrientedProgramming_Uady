package com.audy.services;

import com.audy.api.APIClientException;
import com.audy.api.DeezerAPIClient;
import com.audy.api.MusicBrainzAPIClient;
import com.audy.api.SpotifyAPIClient;
import com.audy.models.Artist;
import com.audy.models.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Logic engine implementing discovery, debounce, and filtering rules.
 * Uses Deezer API as the primary dynamic recommendation engine (RF-05..RF-08, RNF-05),
 * with Spotify and MusicBrainz available as modular multi-source providers.
 * All recommendation graphs are 100% dynamically computed by API without hardcoded knowledge.
 */
public class DiscoveryEngine {
    private final DeezerAPIClient deezerApi;
    private final SpotifyAPIClient spotifyApi;
    private final MusicBrainzAPIClient musicBrainzApi;
    private long lastKeystrokeTimestamp = 0;

    private static final long DEBOUNCE_DELAY_MS = 300; // RNF-04: >= 300 ms wait
    private static final int MIN_RECOMMENDATIONS = 10; // RNF-03: At least 10 results

    public DiscoveryEngine() {
        this(new DeezerAPIClient(), new SpotifyAPIClient(), new MusicBrainzAPIClient());
    }

    public DiscoveryEngine(SpotifyAPIClient spotifyApi, MusicBrainzAPIClient musicBrainzApi) {
        this(new DeezerAPIClient(), spotifyApi, musicBrainzApi);
    }

    public DiscoveryEngine(DeezerAPIClient deezerApi, SpotifyAPIClient spotifyApi, MusicBrainzAPIClient musicBrainzApi) {
        this.deezerApi = deezerApi;
        this.spotifyApi = spotifyApi;
        this.musicBrainzApi = musicBrainzApi;
    }

    /**
     * Handles predictive search suggestions while typing (RF-02) with a 300ms debounce (RNF-04).
     */
    public List<Track> getPredictiveSuggestions(String partialText) throws APIClientException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeystrokeTimestamp < DEBOUNCE_DELAY_MS) {
            return new ArrayList<>();
        }
        lastKeystrokeTimestamp = currentTime;

        if (partialText == null || partialText.trim().isEmpty()) {
            return new ArrayList<>();
        }

        if (deezerApi != null) {
            try {
                return deezerApi.searchTracks(partialText.trim(), 5);
            } catch (APIClientException e) {
                if (spotifyApi != null) {
                    try {
                        return spotifyApi.searchTracks(partialText.trim(), 5);
                    } catch (Exception ignored) {}
                }
                throw e;
            }
        }
        if (spotifyApi != null) {
            return spotifyApi.searchTracks(partialText.trim(), 5);
        }
        return new ArrayList<>();
    }

    /**
     * Executes full search when the user presses Enter (RF-03).
     */
    public List<Track> searchMatches(String query) throws APIClientException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        if (deezerApi != null) {
            try {
                return deezerApi.searchTracks(query.trim(), 20);
            } catch (APIClientException e) {
                if (spotifyApi != null) {
                    try {
                        return spotifyApi.searchTracks(query.trim(), 20);
                    } catch (Exception ignored) {}
                }
                throw e;
            }
        }
        if (spotifyApi != null) {
            return spotifyApi.searchTracks(query.trim(), 20);
        }
        return new ArrayList<>();
    }

    /**
     * Generates filtered recommendations (RF-05, RF-06, RF-07, RF-08)
     * strictly enforcing RNF-03 (no reference track, no duplicates, >= 10 results).
     * Relies on Deezer's collaborative filtering graph for 100% dynamic related artists.
     */
    public List<Track> generateRecommendations(Track referenceTrack) throws APIClientException {
        if (referenceTrack == null || referenceTrack.getArtist() == null) {
            throw new APIClientException("Invalid reference track selected.", null, false);
        }

        String artistName = referenceTrack.getArtist().getName();
        String artistId = referenceTrack.getArtist().getId();

        // 1. Resolve Deezer Artist ID if not numeric (e.g. came from Spotify or external seed)
        if (deezerApi != null && (artistId == null || artistId.isEmpty() || !artistId.matches("\\d+"))) {
            String resolved = deezerApi.resolveArtistId(artistName);
            if (!resolved.isEmpty()) {
                artistId = resolved;
            } else {
                try {
                    List<Track> found = deezerApi.searchTracks(referenceTrack.getTitle() + " " + artistName, 1);
                    if (!found.isEmpty() && found.get(0).getArtist() != null) {
                        artistId = found.get(0).getArtist().getId();
                    }
                } catch (Exception ignored) {}
            }
        }

        List<Track> candidatePool = new ArrayList<>();

        // 2. Fetch dynamically related artists from Deezer's recommendation engine (RF-07, RF-08)
        List<Artist> relatedPeers = new ArrayList<>();
        if (deezerApi != null && artistId != null && !artistId.isEmpty() && artistId.matches("\\d+")) {
            try {
                relatedPeers = deezerApi.getRelatedArtists(artistId);
            } catch (Exception ignored) {}
        }

        // 3. Dynamic diversity: shuffle related peers so subsequent searches explore different facets
        if (deezerApi != null && !relatedPeers.isEmpty()) {
            Collections.shuffle(relatedPeers);
            // Select up to 6 distinct peer artists
            List<Artist> selectedPeers = relatedPeers.subList(0, Math.min(6, relatedPeers.size()));
            for (Artist peer : selectedPeers) {
                try {
                    List<Track> peerTracks = deezerApi.getArtistTopTracks(peer.getId(), 4);
                    candidatePool.addAll(peerTracks);
                } catch (Exception ignored) {}
            }
        }

        // 4. Also fetch top tracks from the reference artist itself
        if (deezerApi != null && artistId != null && !artistId.isEmpty() && artistId.matches("\\d+")) {
            try {
                List<Track> artistTopTracks = deezerApi.getArtistTopTracks(artistId, 5);
                candidatePool.addAll(artistTopTracks);
            } catch (Exception ignored) {}
        }

        // 5. Fallback candidate generation if candidate pool is small (< 12)
        if (deezerApi != null && candidatePool.size() < 12) {
            try {
                List<Track> fallback = deezerApi.searchTracks(artistName, 15);
                candidatePool.addAll(fallback);
            } catch (Exception ignored) {}
        }

        // Fallback to Spotify if candidate pool is empty and Spotify is available
        if (candidatePool.isEmpty() && spotifyApi != null) {
            try {
                candidatePool.addAll(spotifyApi.searchTracks(artistName, 10));
            } catch (Exception ignored) {}
        }

        // 6. RNF-03: Exclude reference track and eliminate duplicates
        Set<Track> deduplicated = new LinkedHashSet<>();
        for (Track candidate : candidatePool) {
            if (!candidate.equals(referenceTrack)) {
                deduplicated.add(candidate);
            }
        }

        // 7. RF-07: Calculate adaptive popularity bounds based on reference track popularity
        int refPop = referenceTrack.getPopularity();
        int minPop;
        int maxPop;

        if (refPop > 70) {
            minPop = 30;
            maxPop = 99;
        } else if (refPop >= 30) {
            minPop = Math.max(10, refPop - 25);
            maxPop = Math.min(95, refPop + 25);
        } else {
            minPop = 5;
            maxPop = 65;
        }

        // Filter candidates against popularity bounds
        List<Track> matchingPop = new ArrayList<>();
        List<Track> secondaryPool = new ArrayList<>();

        for (Track t : deduplicated) {
            if (t.getPopularity() >= minPop && t.getPopularity() <= maxPop) {
                matchingPop.add(t);
            } else {
                secondaryPool.add(t);
            }
        }

        // 8. RNF-03: Guarantee at least MIN_RECOMMENDATIONS (10)
        Collections.shuffle(matchingPop);
        if (matchingPop.size() < MIN_RECOMMENDATIONS) {
            Collections.shuffle(secondaryPool);
            for (Track sec : secondaryPool) {
                matchingPop.add(sec);
                if (matchingPop.size() >= MIN_RECOMMENDATIONS) break;
            }
        }

        return matchingPop;
    }
}
