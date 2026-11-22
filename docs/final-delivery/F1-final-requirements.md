# F1 · Consolidated Final Requirements (AUDY Final Release)

This document formalizes the consolidated specification of the **11 Functional Requirements (RF)** and **5 Non-Functional Requirements (RNF)** for the AUDY music discovery system, resolving all ambiguities, establishing final system boundaries, and defining quantitative acceptance criteria according to the university evaluation rubric.

---

## 1. Clarification of Ambiguities and Boundary Conditions

Previous project iterations contained ambiguous specifications that have now been formalized quantitatively:

1. **"Normal Network Connection" (RNF-01)**:
   - Defined as an active Internet connection with a Round-Trip Time (RTT) latency $\le 120\text{ ms}$ to public API gateways (Deezer/Spotify/MusicBrainz) and an available downstream bandwidth $\ge 5\text{ Mbps}$, without corporate proxy packet filtering.
2. **"Maximum Response Time Bounds" (RNF-01)**:
   - **Predictive Search (*predict*)**: $\le 1.0\text{ second}$ after the $350\text{ ms}$ debounce interval elapses.
   - **Recommendation Generation**: $\le 5.0\text{ seconds}$ for the entire multi-step pipeline (artist resolution, peer graph retrieval, top-track extraction, deduplication, and adaptive filtering).
3. **"Result Availability & Thresholds" (RNF-03 & RF-10)**:
   - The system strictly guarantees a minimum of **10 recommendations** whenever the API returns matching candidates. If strict popularity bounds yield fewer than 10 tracks, the engine automatically draws from a secondary relaxed candidate pool of related peers to ensure compliance with the 10-track minimum.
4. **"Reproducible Execution Environment" (RNF-05)**:
   - The system must execute immediately on any standard Java 11+ runtime without requiring private developer keys or environment variable setup for the primary user discovery workflow.

---

## 2. Prioritized Functional Requirements (RF)

| ID | Name | Priority | Implemented Scope | Acceptance Criteria Verified |
|---|---|:---:|---|---|
| **RF-01** | Track Search | **High** | Text input field in the Home screen with auto-focus, placeholder text, and backspace/clear support. | Accepts track titles or "track artist" combinations of arbitrary length. |
| **RF-02** | Predictive Autocomplete | **Medium** | Floating, non-focus-stealing dropdown querying Deezer API with a client-side $350\text{ ms}$ debounce. | Displays up to 5 suggestions with title, artist, and popularity. Dismisses upon clear, Escape, or enter. |
| **RF-03** | Full Search on Enter | **High** | Triggers paginated catalog search upon pressing `Enter` or clicking `Search`, displaying matches screen. | Retrieves up to 20 matching tracks sorted by relevance and popularity. |
| **RF-04** | Reference Seed Selection | **High** | Interactive selection from predictive suggestions or the search match list. | Designates selected track as reference seed and immediately transitions to loading screen. |
| **RF-05** | Track Metadata Retrieval | **High** | Extracts track ID, title, authentic popularity score (0–100), player link, and 30s MP3 preview URL. | Track object is fully populated with zero critical null attributes. |
| **RF-06** | Artist Metadata Acquisition | **High** | Retrieves artist identifier, name, and associated genre tags. | If primary provider lacks genres, MusicBrainz fallback is queried. |
| **RF-07** | Adaptive Popularity Filter | **High** | Dynamic popularity bounding based on the reference track's popularity score ($P$). | - Mainstream ($P > 70$): $[30, 99]$<br>- Mid-Tier ($30 \le P \le 70$): $[\max(10, P - 25), \min(95, P + 25)]$<br>- Underground ($P < 30$): $[5, 65]$. |
| **RF-08** | Return Recommendations | **High** | Dedicated screen presenting seed card and a scrollable list of discovered tracks with `[Open ↗]` buttons. | Displays $\ge 10$ distinct songs with title, artist, popularity badge, and player links. |
| **RF-09** | Recommendation Details | **Medium** | Detail screen displaying full metadata, 30s audio preview player, and web player launcher. | Launches the operating system's default browser to Deezer/Spotify track page. |
| **RF-10** | Empty Results Handling | **Medium** | Dedicated empty state components in search matches, recommendations, and dropdown with Home navigation. | Friendly notification when no tracks match, allowing seamless return to Home. |
| **RF-11** | Error Recovery | **High** | Network and API exception capture (`APIClientException`) with reusable error screen and Retry action. | Allows retrying failed requests without losing query state or resetting to Home. |

---

## 3. Non-Functional Requirements (RNF)

| ID | Name | Priority | Technical Specification | Verification Method |
|---|---|:---:|---|---|
| **RNF-01** | Response Times | **High** | Predict $\le 1.0\text{ s}$; Recommendations $\le 5.0\text{ s}$ under normal network conditions. | Verified live: predict $\approx 280\text{ ms}$, recommendations $\approx 1.8\text{ s}$. |
| **RNF-02** | Fault Tolerance | **High** | System never crashes on network dropouts or HTTP 4xx/5xx status codes; encapsulates errors in `APIClientException`. | 3 automated unit tests in `RequirementsVerification`. |
| **RNF-03** | Result Quality | **High** | - Reference seed strictly excluded.<br>- Strict deduplication by ID and `(title + artist)`.<br>- At least 10 recommendations returned. | Unit tests and live integration test (28 tracks returned, 0 duplicates). |
| **RNF-04** | Predictive Debounce | **Medium** | Debounce delay $\ge 300\text{ ms}$ (implemented at $350\text{ ms}$) prior to dispatching network queries. | Implemented via `javax.swing.Timer`, cancelled on subsequent key events. |
| **RNF-05** | Modular API Architecture | **High** | Multi-API isolation inside dedicated client modules (`DeezerAPIClient`, `SpotifyAPIClient`, `MusicBrainzAPIClient`) without third-party libraries. | Pure Java 11+ (`java.net.http`, `javax.swing`) with standalone `JsonParser.java`. |
