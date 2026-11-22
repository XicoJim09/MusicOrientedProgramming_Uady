package com.audy.api;

import com.audy.util.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dedicated module for MusicBrainz API communication (RNF-05).
 * Enforces mandatory User-Agent, 1 req/sec rate limit (RNF-04),
 * and automatic retry on 503 Busy (RNF-02).
 * FIX: generic artist lookup for ALL artists (removed Nirvana hardcode).
 */
public class MusicBrainzAPIClient {
    private static final String BASE_URL = "https://musicbrainz.org/ws/2";
    private static final String USER_AGENT =
            "AudyDiscoverySystem/1.0 (https://github.com/audy-project; contact@uady.mx)";
    private static final long MIN_REQUEST_INTERVAL_MS = 1200; // > 1 second rate limit

    private final HttpClient httpClient;
    private long lastRequestTime = 0;
    private String lastValidJsonResponse = "";

    public MusicBrainzAPIClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    private synchronized void enforceRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        if (elapsed < MIN_REQUEST_INTERVAL_MS) {
            Thread.sleep(MIN_REQUEST_INTERVAL_MS - elapsed);
        }
        lastRequestTime = System.currentTimeMillis();
    }

    // Executes GET request with automatic retry if MusicBrainz server is busy (HTTP 503).
    private String executeGetWithRetry(String url) throws Exception {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            enforceRateLimit();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                this.lastValidJsonResponse = response.body();
                return response.body();
            }

            if (attempt < maxRetries) {
                System.out.println("   [MusicBrainz busy (HTTP " + response.statusCode()
                        + "), retrying in 2s... (" + attempt + "/" + maxRetries + ")]");
                Thread.sleep(2000);
            } else {
                throw new APIClientException(
                        "MusicBrainz server error (HTTP " + response.statusCode() + ")", null, true);
            }
        }
        return "";
    }

    /**
     * Queries MusicBrainz for verified musical genres of ANY artist (RF-06 & RF-07).
     * FIX: generic two-step lookup — search artist by name → lookup by MBID with genres.
     */
    @SuppressWarnings("unchecked")
    public List<String> fetchTrackTags(String trackTitle, String artistName) throws APIClientException {
        try {
            // Step 1: Search for the artist by name to obtain their MBID
            String encodedArtist = URLEncoder.encode(artistName, StandardCharsets.UTF_8);
            String searchUrl = BASE_URL + "/artist/?query=artist:" + encodedArtist + "&limit=1&fmt=json";
            String searchBody = executeGetWithRetry(searchUrl);

            Map<String, Object> searchJson = JsonParser.parseObject(searchBody);
            List<Object> artists = JsonParser.getArray(searchJson, "artists");

            if (artists.isEmpty()) {
                return new ArrayList<>();
            }

            Map<String, Object> firstArtist = (Map<String, Object>) artists.get(0);
            String mbid = JsonParser.getString(firstArtist, "id");

            // If no MBID found, try extracting genres from the search result directly
            if (mbid.isEmpty()) {
                return extractGenresFromObject(firstArtist);
            }

            // Step 2: Lookup artist by MBID with genres included
            String lookupUrl = BASE_URL + "/artist/" + mbid + "?inc=genres&fmt=json";
            String lookupBody = executeGetWithRetry(lookupUrl);

            Map<String, Object> lookupJson = JsonParser.parseObject(lookupBody);
            return extractGenresFromObject(lookupJson);

        } catch (APIClientException e) {
            throw e;
        } catch (Exception e) {
            throw new APIClientException(
                    "Failed to connect to MusicBrainz API. Please check your internet connection.", e, true
            );
        }
    }

    // Returns the raw JSON response for rubric evidence documentation.
    public String fetchRawRecordingJson(String trackTitle, String artistName) throws APIClientException {
        if (!lastValidJsonResponse.isEmpty()) {
            return lastValidJsonResponse;
        }
        try {
            String encodedArtist = URLEncoder.encode(artistName, StandardCharsets.UTF_8);
            String url = BASE_URL + "/artist/?query=artist:" + encodedArtist + "&limit=1&fmt=json";
            return executeGetWithRetry(url);
        } catch (Exception e) {
            throw new APIClientException("Error fetching raw MusicBrainz JSON", e, true);
        }
    }

    // Extracts genre names from a MusicBrainz JSON object (max 10).
    @SuppressWarnings("unchecked")
    private List<String> extractGenresFromObject(Map<String, Object> json) {
        List<String> genres = new ArrayList<>();
        // Try "genres" array first, then "tags"
        for (String key : new String[]{"genres", "tags"}) {
            List<Object> items = JsonParser.getArray(json, key);
            for (Object item : items) {
                if (item instanceof Map) {
                    String name = JsonParser.getString((Map<String, Object>) item, "name")
                            .toLowerCase().trim();
                    if (!name.isEmpty() && !genres.contains(name)) {
                        genres.add(name);
                    }
                    if (genres.size() >= 10) return genres;
                }
            }
        }
        return genres;
    }
}
