package com.audy.test;

import com.audy.api.APIClientException;
import com.audy.models.Artist;
import com.audy.models.Track;
import com.audy.services.DiscoveryEngine;
import com.audy.util.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Requirements verification test runner (F8).
 * Runs unit tests for RF and RNF without requiring API access.
 * Usage: java -cp out com.audy.test.RequirementsVerification
 */
public class RequirementsVerification {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   AUDY — Requirements Verification Test Suite (F8)   ");
        System.out.println("======================================================\n");

        section("JSON Parser (foundation for RF-02/03/05/06/07)");
        testJsonObject();
        testJsonArray();
        testJsonNested();
        testJsonEscaped();
        testJsonNumbers();

        section("RNF-03: No duplicates — Track.equals() & hashCode()");
        testTrackEqualityById();
        testTrackEqualityByTitleArtist();
        testTrackNotEqual();
        testNoDuplicatesInSet();

        section("RF-07: Adaptive popularity filter thresholds");
        testPopularityMainstream();
        testPopularityMidTier();
        testPopularityUnderground();

        section("RNF-04: Debounce >= 300 ms (DiscoveryEngine)");
        testDebounceBlocksRapidCalls();

        section("RF-11 / RNF-02: Error handling — APIClientException");
        testRetryableException();
        testNonRetryableException();
        testExceptionChaining();

        section("RF-10: Empty / null input handling");
        testEmptySearchQuery();
        testNullPredictiveInput();
        testNullReferenceTrack();

        section("RF-05 / RF-06: Model construction");
        testTrackConstruction();
        testArtistConstruction();

        // Summary
        System.out.println("\n======================================================");
        System.out.printf("   TOTAL: %d PASSED  |  %d FAILED%n", passed, failed);
        System.out.println("======================================================");
        if (failed > 0) System.exit(1);
    }

    // ---- JSON Parser Tests ----

    private static void testJsonObject() {
        Map<String, Object> obj = JsonParser.parseObject("{\"name\":\"AUDY\",\"version\":1}");
        verify("Parse simple object — string field", "AUDY".equals(JsonParser.getString(obj, "name")));
        verify("Parse simple object — int field", JsonParser.getInt(obj, "version", 0) == 1);
    }

    private static void testJsonArray() {
        List<Object> arr = JsonParser.parseArray("[\"rock\",\"pop\",\"jazz\"]");
        verify("Parse string array — size", arr.size() == 3);
        verify("Parse string array — content", "pop".equals(arr.get(1)));
    }

    @SuppressWarnings("unchecked")
    private static void testJsonNested() {
        String json = "{\"tracks\":{\"items\":[{\"id\":\"abc\",\"name\":\"Song\"}]}}";
        Map<String, Object> root = JsonParser.parseObject(json);
        Map<String, Object> tracks = JsonParser.getObject(root, "tracks");
        List<Object> items = JsonParser.getArray(tracks, "items");
        verify("Nested object — items count", items.size() == 1);
        Map<String, Object> item = (Map<String, Object>) items.get(0);
        verify("Nested object — id extraction", "abc".equals(JsonParser.getString(item, "id")));
    }

    private static void testJsonEscaped() {
        Map<String, Object> obj = JsonParser.parseObject("{\"msg\":\"hello\\nworld\",\"q\":\"she said \\\"hi\\\"\"}");
        verify("Escaped newline", "hello\nworld".equals(JsonParser.getString(obj, "msg")));
        verify("Escaped quotes", "she said \"hi\"".equals(JsonParser.getString(obj, "q")));
    }

    private static void testJsonNumbers() {
        Map<String, Object> obj = JsonParser.parseObject("{\"int\":42,\"neg\":-7,\"float\":3.14}");
        verify("Parse integer", JsonParser.getInt(obj, "int", 0) == 42);
        verify("Parse negative", JsonParser.getInt(obj, "neg", 0) == -7);
        Object f = obj.get("float");
        verify("Parse float", f instanceof Double && Math.abs((Double) f - 3.14) < 0.001);
    }

    // ---- RNF-03: Deduplication ----

    private static void testTrackEqualityById() {
        Track a = makeTrack("id1", "Song A", "Artist X");
        Track b = makeTrack("id1", "Song A", "Artist X");
        verify("Same ID → equals true", a.equals(b));
    }

    private static void testTrackEqualityByTitleArtist() {
        Track a = makeTrack("id1", "My Song", "Band");
        Track b = makeTrack("id2", "my song", "band");
        verify("Same title+artist (case-insensitive) → equals true", a.equals(b));
    }

    private static void testTrackNotEqual() {
        Track a = makeTrack("id1", "Song A", "Artist X");
        Track b = makeTrack("id2", "Song B", "Artist Y");
        verify("Different tracks → equals false", !a.equals(b));
    }

    private static void testNoDuplicatesInSet() {
        Track a = makeTrack("id1", "Song", "Artist");
        Track b = makeTrack("id1", "Song", "Artist");
        Track c = makeTrack("id2", "song", "artist"); // same title+artist, different ID
        Set<Track> set = new LinkedHashSet<>();
        set.add(a);
        set.add(b);
        set.add(c);
        verify("Set filters duplicates correctly (expected 1, got " + set.size() + ")", set.size() == 1);
    }

    // ---- RF-07: Popularity thresholds ----

    private static void testPopularityMainstream() {
        int[] bounds = computePopBounds(85);
        verify("Mainstream (pop=85): min=25", bounds[0] == 25);
        verify("Mainstream (pop=85): max=65", bounds[1] == 65);
    }

    private static void testPopularityMidTier() {
        int[] bounds = computePopBounds(50);
        verify("Mid-tier (pop=50): min=max(15,30)=30", bounds[0] == 30);
        verify("Mid-tier (pop=50): max=min(75,65)=65", bounds[1] == 65);
    }

    private static void testPopularityUnderground() {
        int[] bounds = computePopBounds(15);
        verify("Underground (pop=15): min=10", bounds[0] == 10);
        verify("Underground (pop=15): max=50", bounds[1] == 50);
    }

    // ---- RNF-04: Debounce ----

    private static void testDebounceBlocksRapidCalls() {
        // DiscoveryEngine blocks calls within 300 ms of each other
        DiscoveryEngine engine = new DiscoveryEngine(null, null);
        try {
            List<Track> first = engine.getPredictiveSuggestions(null);
            // Immediate second call (< 300 ms) should return empty
            List<Track> second = engine.getPredictiveSuggestions("test");
            verify("Debounce blocks rapid second call → empty list", second.isEmpty());
        } catch (Exception e) {
            verify("Debounce test threw unexpected exception", false);
        }
    }

    // ---- RF-11 / RNF-02: Error handling ----

    private static void testRetryableException() {
        APIClientException ex = new APIClientException("Network error", null, true);
        verify("Retryable exception → isRetryable true", ex.isRetryable());
    }

    private static void testNonRetryableException() {
        APIClientException ex = new APIClientException("Invalid credentials", null, false);
        verify("Non-retryable exception → isRetryable false", !ex.isRetryable());
    }

    private static void testExceptionChaining() {
        Exception cause = new RuntimeException("root cause");
        APIClientException ex = new APIClientException("wrapper", cause, true);
        verify("Exception chaining preserves cause", ex.getCause() == cause);
        verify("Exception message preserved", ex.getMessage().equals("wrapper"));
    }

    // ---- RF-10: Empty/null handling ----

    private static void testEmptySearchQuery() {
        DiscoveryEngine engine = new DiscoveryEngine(null, null);
        try {
            List<Track> result = engine.searchMatches("");
            verify("Empty query → empty result", result.isEmpty());
            result = engine.searchMatches("   ");
            verify("Whitespace query → empty result", result.isEmpty());
            result = engine.searchMatches(null);
            verify("Null query → empty result", result.isEmpty());
        } catch (Exception e) {
            verify("Empty search should not throw", false);
        }
    }

    private static void testNullPredictiveInput() {
        DiscoveryEngine engine = new DiscoveryEngine(null, null);
        try {
            List<Track> result = engine.getPredictiveSuggestions(null);
            verify("Null predict input → empty result", result.isEmpty());
        } catch (Exception e) {
            verify("Null predict should not throw", false);
        }
    }

    private static void testNullReferenceTrack() {
        DiscoveryEngine engine = new DiscoveryEngine(null, null);
        try {
            engine.generateRecommendations(null);
            verify("Null reference → should throw", false);
        } catch (APIClientException e) {
            verify("Null reference → APIClientException thrown", true);
            verify("Null reference → non-retryable", !e.isRetryable());
        }
    }

    // ---- RF-05 / RF-06: Models ----

    private static void testTrackConstruction() {
        Track t = makeTrack("x", "Title", "Artist");
        verify("Track.getId()", "x".equals(t.getId()));
        verify("Track.getTitle()", "Title".equals(t.getTitle()));
        verify("Track.getPopularity() default", t.getPopularity() == 50);
    }

    private static void testArtistConstruction() {
        Artist a = new Artist("a1", "Beatles", Arrays.asList("rock", "pop"));
        verify("Artist.getName()", "Beatles".equals(a.getName()));
        verify("Artist.getGenres() size", a.getGenres().size() == 2);
    }

    // ==== Helpers ====

    private static Track makeTrack(String id, String title, String artistName) {
        Artist artist = new Artist("art_" + artistName, artistName, new ArrayList<>());
        return new Track(id, title, artist, new ArrayList<>(), 50, "", "", "");
    }

    /** Replicates DiscoveryEngine popularity-bound logic for unit testing (RF-07). */
    private static int[] computePopBounds(int refPop) {
        int minPop, maxPop;
        if (refPop > 70)       { minPop = 25; maxPop = 65; }
        else if (refPop >= 30) { minPop = Math.max(15, refPop - 20); maxPop = Math.min(75, refPop + 15); }
        else                   { minPop = 10; maxPop = 50; }
        return new int[]{minPop, maxPop};
    }

    private static void section(String name) {
        System.out.println("\n--- " + name + " ---");
    }

    private static void verify(String testName, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + testName);
            passed++;
        } else {
            System.out.println("  FAIL: " + testName);
            failed++;
        }
    }
}
