# F9 · Sprint 3 Management Log & Process Documentation

This log records the planning, weekly engineering activities, team agreements, technical roadblocks encountered, design decisions, and actual outcomes achieved during **Sprint 3 (Final Release)** for AUDY.

---

## 1. Sprint 3 General Information

- **Sprint Duration**: 4 weeks.
- **Team Roster (6 Members)**:
  1. **Gil Juárez Joshua Nabil** — *Product Owner*
  2. **Jiménez Hernández Xicoténcatl** — *Scrum Master*
  3. **Antochiw Flores Andrés** — *Technical Leader & Designer*
  4. **Thibault Raphael Didier Alexandre** — *Software Architect & Lead Developer*
  5. **Couoh Mis Guillermo Moises** — *Developer & Quality Assurance*
  6. **Medina Uitz Eleazar** — *Designer & Documentation Lead*

---

## 2. Weekly Engineering Activities & Incident Tracking

### Week 1: Requirements Consolidation & MVC Scaffolding
- **Activities**:
  - Formalized the 11 RF and 5 RNF, defining quantitative network bounds (Task F1).
  - Built a custom recursive-descent JSON parser (`JsonParser.java`) to eliminate external third-party library dependencies.
  - Designed the Swing view structure (`MainFrame`, `HomePanel`, `MatchesPanel`, `RecommendationsPanel`, `DetailPanel`).
- **Roadblock Encountered**: The legacy regex-based JSON parser failed when processing escaped quotes and nested JSON arrays returned by streaming APIs.
- **Resolution**: Developed a robust recursive-descent parser in `JsonParser.java` handling nested objects, arrays, escaped characters, and signed numbers.

### Week 2: Spotify Developer Mode Blockers & UI Focus Conflicts
- **Activities**:
  - Integrated `SearchController` with a 350 ms client-side debounce.
  - Connected the initial Spotify Web API client for searching and recommendations.
- **Roadblocks Encountered**:
  1. *Spotify HTTP 400 Error*: Querying `/v1/search` with `limit=20` threw `HTTP 400 (Invalid limit)` on standard developer accounts.
  2. *Missing Popularity Attributes*: Spotify dev mode ceased returning track popularity, causing flat placeholder values (50).
  3. *Autocomplete Dropdown Freeze*: Swing's standard `JPopupMenu` component seized native mouse grabs and focus, causing suggestions to freeze when typing or adding characters.
- **Resolutions**:
  - Implemented automatic chunked pagination in `SpotifyAPIClient` to strictly query in blocks $\le 10$.
  - Replaced `JPopupMenu` with an undecorated floating `JWindow` configured with `setFocusableWindowState(false)`. This guarantees that keystrokes are never dropped and suggestion clicks fire instantaneously.

### Week 3: The Deezer Pivot & Zero Hardcoding Mandate
- **Activities**:
  - Evaluated static genre/artist clusters (`MusicalKnowledgeBase.java`).
  - **Explicit User Directive**: *"No hardcoded knowledge in the code; all similarity must be dynamically retrieved from cloud APIs or databases. Implement the Deezer option."*
  - Permanently deleted `MusicalKnowledgeBase.java`.
  - Engineered [`DeezerAPIClient.java`](file:///c:/MEXIQUE/POO/MusicOrientedProgramming_Uady/src/main/java/com/audy/api/DeezerAPIClient.java) and integrated it into `DiscoveryEngine.java`.
- **Roadblocks Encountered**:
  - Spotify completely blocked `/v1/recommendations` for non-extended accounts.
  - Windows browser execution via `cmd /c start ""` failed due to argument escaping issues.
- **Resolutions**:
  - Adopted the **Deezer Web API** as the primary recommendation engine: requires zero API keys, accesses the live collaborative listening graph (`/artist/{id}/related`), and provides authentic streaming rank metrics (0 to 1,000,000).
  - Refactored `DetailPanel.openBrowserUrl` to use a multi-tiered fallback: `Desktop.browse` $\rightarrow$ `rundll32 url.dll,FileProtocolHandler` $\rightarrow$ automatic system clipboard copy. Added direct `[Open ↗]` buttons to recommendation cards.

### Week 4: Automated Verification, Polish & Release
- **Activities**:
  - Executed the automated test suite in [`RequirementsVerification.java`](file:///c:/MEXIQUE/POO/MusicOrientedProgramming_Uady/src/main/java/com/audy/test/RequirementsVerification.java): **37 PASS / 0 FAIL**.
  - Verified live recommendation generation across varied genres (*Smells Like Teen Spirit*, *Bohemian Rhapsody*, *Tarot*).
  - Verified recommendation diversity: dynamic shuffling of related peers guarantees that subsequent queries return varied discovery lists.
  - Completed all technical documentation in English in `docs/final-delivery/`.

---

## 3. Engineering Decisions & Technical Results Summary

| Challenge / Technical Problem | Implemented Engineering Solution | Concrete Outcome |
|---|---|---|
| Spotify dev mode restrictions (HTTP 400 and blocked recommendations) | Developed `DeezerAPIClient` (zero credentials, direct access to similarity graph). Retained Spotify as modular secondary client (RNF-05). | 100% dynamic, reproducible system requiring zero private credential setup. |
| Strict mandate of zero hardcoded knowledge | Deleted `MusicalKnowledgeBase.java` and wired `DiscoveryEngine` directly to Deezer's collaborative filtering endpoint (`/artist/{id}/related`). | High-affinity recommendations backed by millions of real listener sessions. |
| Dropdown freezing on typing or character additions | Replaced `JPopupMenu` with floating `JWindow` having `setFocusableWindowState(false)`. | Fluid typing, no focus loss, and instantaneous click response. |
| External player launcher failures on Windows | Multi-tiered fallback in `DetailPanel.openBrowserUrl` with direct clipboard copy guarantee. | Infallible track opening across Windows, macOS, and Linux. |
| Stale/identical recommendations across searches | Randomized peer sampling (`Collections.shuffle`) across 20 related artists. | Fresh, serendipitous recommendations on repeated queries. |
