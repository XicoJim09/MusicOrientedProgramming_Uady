package com.audy.api;

import com.audy.models.Artist;
import com.audy.models.Track;
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
 * Dedicated client for the Deezer Web API (RNF-05).
 * Free public API without API keys or authentication requirements.
 * Provides live search, dynamic artist relations (collaborative filtering),
 * real popularity rank, and 30s audio previews.
 */
public class DeezerAPIClient {
    private static final String API_BASE_URL = "https://api.deezer.com";
    private HttpClient httpClient;

    public DeezerAPIClient() {
        // HttpClient is lazily initialized on first network request
    }

    private synchronized HttpClient getClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(6))
                    .build();
        }
        return httpClient;
    }

    private String get(String url) throws APIClientException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = getClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new APIClientException("Deezer API error (HTTP " + response.statusCode() + ")", null, true);
            }
            return response.body();
        } catch (APIClientException e) {
            throw e;
        } catch (Exception e) {
            throw new APIClientException("Network error connecting to Deezer API.", e, true);
        }
    }

    /**
     * Searches for tracks on Deezer (RF-01, RF-02, RF-03).
     */
    public List<Track> searchTracks(String query, int limit) throws APIClientException {
        if (query == null || query.trim().isEmpty()) return new ArrayList<>();
        String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String url = API_BASE_URL + "/search?q=" + encoded + "&limit=" + Math.max(1, limit);
        String body = get(url);

        Map<String, Object> json = JsonParser.parseObject(body);
        List<Object> data = JsonParser.getArray(json, "data");
        return parseTrackList(data);
    }

    /**
     * Searches for an artist by name and returns their Deezer ID.
     */
    public String resolveArtistId(String artistName) {
        if (artistName == null || artistName.trim().isEmpty()) return "";
        try {
            String encoded = URLEncoder.encode(artistName.trim(), StandardCharsets.UTF_8);
            String url = API_BASE_URL + "/search/artist?q=" + encoded + "&limit=5";
            String body = get(url);
            Map<String, Object> json = JsonParser.parseObject(body);
            List<Object> data = JsonParser.getArray(json, "data");
            if (data == null || data.isEmpty()) return "";

            int maxFans = -1;
            String bestId = "";
            for (Object item : data) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> obj = (Map<String, Object>) item;
                    String id = JsonParser.getString(obj, "id");
                    String name = JsonParser.getString(obj, "name");
                    int fans = JsonParser.getInt(obj, "nb_fan", 0);
                    if (name.equalsIgnoreCase(artistName.trim())) {
                        return id;
                    }
                    if (fans > maxFans) {
                        maxFans = fans;
                        bestId = id;
                    }
                }
            }
            return bestId;
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Fetches dynamically related artists from Deezer's recommendation engine (RF-07, RF-08).
     * Returns up to 20 related artists dynamically computed from user listening graphs.
     */
    @SuppressWarnings("unchecked")
    public List<Artist> getRelatedArtists(String artistId) throws APIClientException {
        if (artistId == null || artistId.trim().isEmpty()) return new ArrayList<>();
        String url = API_BASE_URL + "/artist/" + artistId.trim() + "/related";
        String body = get(url);

        Map<String, Object> json = JsonParser.parseObject(body);
        List<Object> data = JsonParser.getArray(json, "data");
        List<Artist> artists = new ArrayList<>();

        for (Object item : data) {
            if (item instanceof Map) {
                Map<String, Object> obj = (Map<String, Object>) item;
                String id = JsonParser.getString(obj, "id");
                String name = JsonParser.getString(obj, "name");
                if (!id.isEmpty() && !name.isEmpty()) {
                    artists.add(new Artist(id, name, new ArrayList<>()));
                }
            }
        }
        return artists;
    }

    /**
     * Retrieves the top tracks for a specific artist (RF-05, RF-08).
     */
    public List<Track> getArtistTopTracks(String artistId, int limit) throws APIClientException {
        if (artistId == null || artistId.trim().isEmpty()) return new ArrayList<>();
        String url = API_BASE_URL + "/artist/" + artistId.trim() + "/top?limit=" + Math.max(1, limit);
        String body = get(url);

        Map<String, Object> json = JsonParser.parseObject(body);
        List<Object> data = JsonParser.getArray(json, "data");
        return parseTrackList(data);
    }

    @SuppressWarnings("unchecked")
    private List<Track> parseTrackList(List<Object> data) {
        List<Track> tracks = new ArrayList<>();
        if (data == null) return tracks;
        for (Object item : data) {
            if (item instanceof Map) {
                Track t = parseTrackObject((Map<String, Object>) item);
                if (t != null) tracks.add(t);
            }
        }
        return tracks;
    }

    @SuppressWarnings("unchecked")
    private Track parseTrackObject(Map<String, Object> obj) {
        String id = JsonParser.getString(obj, "id");
        String title = JsonParser.getString(obj, "title");
        String previewUrl = JsonParser.getString(obj, "preview");
        String playerUrl = JsonParser.getString(obj, "link");

        // Deezer rank is 0..1,000,000. Map to 0..100 popularity score
        int rank = JsonParser.getInt(obj, "rank", 500000);
        int popularity = Math.max(5, Math.min(99, rank / 10000));

        // Artist info
        Artist artist = new Artist("unknown", "Unknown Artist", new ArrayList<>());
        Map<String, Object> artistObj = JsonParser.getObject(obj, "artist");
        if (!artistObj.isEmpty()) {
            String artistId = JsonParser.getString(artistObj, "id");
            String artistName = JsonParser.getString(artistObj, "name");
            artist = new Artist(artistId, artistName, new ArrayList<>());
        }

        // Album cover
        String coverImage = "";
        Map<String, Object> albumObj = JsonParser.getObject(obj, "album");
        if (!albumObj.isEmpty()) {
            coverImage = JsonParser.getString(albumObj, "cover_medium");
            if (coverImage.isEmpty()) coverImage = JsonParser.getString(albumObj, "cover");
        }

        if (id.isEmpty() || title.isEmpty()) return null;
        return new Track(id, title, artist, new ArrayList<>(), popularity, previewUrl, playerUrl, coverImage);
    }
}
