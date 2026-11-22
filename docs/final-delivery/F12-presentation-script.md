# F12 · Final Presentation & Live Demonstration Guide (10 Minutes)

This document provides the structured script and slide-by-slide timeline for the **10-minute final project defense of AUDY**, delivered in **technical English** in strict adherence to the university evaluation rubric.

---

## 1. Presentation Overview & Time Allocation

| Section | Topic | Allocated Time | Speaker(s) |
|---|---|:---:|---|
| **1** | Introduction & Problem Vision | 1:00 min | Product Owner / Scrum Master |
| **2** | System Architecture & Evolution (MVC & APIs) | 2:00 min | Technical Leader & Architect |
| **3** | Recommendation Engine & The Deezer Pivot | 1:30 min | Architect / Developer |
| **4** | Live Interactive Demonstration | 3:00 min | All Members |
| **5** | Verification & Automated Test Suite | 1:00 min | Test Lead |
| **6** | Competencies Acquired & Course Improvements | 1:30 min | Team Representative |
| **TOTAL** | — | **10:00 min** | — |

---

## 2. Slide-by-Slide Script (Technical English)

### Slide 1: Title & Team
- **Spoken Text**:
  > *"Good morning, Professor and classmates. Today we are proud to present the final release of **AUDY**, a musical recommendation and discovery system developed in pure Java for the Object-Oriented Programming course at UADY. Our team consists of Joshua Gil, Xicoténcatl Jiménez, Andrés Antochiw, Alexandre Thibault, Guillermo Couoh, and Eleazar Medina."*

### Slide 2: Problem Vision & Core Objective
- **Key Concepts**: Overcoming generic, obsolete, or purely commercial algorithmic bubbles.
- **Spoken Text**:
  > *"Conventional streaming recommendation algorithms often trap listeners inside repetitive mainstream loops. AUDY was created to empower users to take any song they already love as a musical seed, and explore genuine connections across artists, subgenres, and musical movements through intelligent, non-trivial recommendations."*

### Slide 3: Evolution Across Deliveries (Deliveries 1 $\rightarrow$ 2 $\rightarrow$ 3)
- **Key Concepts**: Conceptual entities $\rightarrow$ Modular backend $\rightarrow$ Complete MVC Swing system.
- **Spoken Text**:
  > *"Our system underwent three major evolutionary stages. In Delivery 1, we defined our domain models and conceptual scopes. In Delivery 2, we separated concerns into dedicated API clients and designed the visual language on iPad and Figma. Finally, in Delivery 3, we built the complete Model-View-Controller desktop application, created a zero-dependency recursive-descent JSON parser, and integrated a 100% dynamic cloud recommendation graph without any hardcoded knowledge."*

### Slide 4: Multi-API Architecture & The Deezer Pivot
- **Key Concepts**: Spotify Developer Mode limitations, zero-friction deployment, RNF-05 multi-provider compliance.
- **Spoken Text**:
  > *"A critical engineering decision occurred during Sprint 3. Spotify's standard developer mode introduced severe restrictions: deprecating recommendation endpoints, returning flat popularity scores, and enforcing an arbitrary 10-track limit that triggered HTTP 400 errors. Furthermore, requiring private OAuth credentials created friction for peer evaluation.*
  > *To uphold the university requirement of zero hardcoded knowledge, we pivoted to the **Deezer Web API** as our primary discovery engine. Deezer requires zero credentials, exposes genuine collaborative filtering similarity graphs based on millions of listening sessions, and provides authentic streaming rank metrics. Spotify and MusicBrainz remain modular secondary clients, ensuring robust multi-API resilience and full compliance with requirement RNF-05."*

### Slide 5: The Recommendation Engine & Adaptive Popularity
- **Key Concepts**: Dynamic peer sampling, adaptive popularity thresholds (RF-07), strict deduplication (RNF-03).
- **Spoken Text**:
  > *"When a reference song is chosen, AUDY queries Deezer to retrieve up to 20 related peers from its machine-learning graph. To guarantee freshness and serendipity, our algorithm randomly shuffles these peers on every query, so searching the same track today and tomorrow reveals different musical gems. We extract top tracks from both the peers and the reference artist, eliminate duplicates, filter out the reference track, and apply an adaptive popularity filter that dynamically bounds results based on the seed's mainstream or underground nature."*

### Slide 6: Technical Challenges Overcome
- **Key Concepts**: Swing AWT mouse-grab race condition, audio preview playback, cross-platform URL execution.
- **Spoken Text**:
  > *"During development, we conquered three complex technical hurdles:*
  > *1. **Predictive Search Dropdown**: Standard `JPopupMenu` components suffered from focus-stealing conflicts in Swing. We engineered a floating `JWindow` with `setFocusableWindowState(false)`, ensuring fluid typing and instantaneous mouse selection.*
  > *2. **Popularity Diversity**: We transformed Deezer’s 1-million-point streaming rank into a proportional 0-to-100 score, replacing flat 50-point placeholders with authentic metrics.*
  > *3. **Platform-Resilient Playback**: We built a fail-safe launcher supporting Desktop BROWSE, Windows Shell, and system clipboard fallbacks alongside 30-second live MP3 previews."*

---

## 3. Live Demonstration Script (3 Minutes)

1. **Step 1: Application Launch**
   - Execute: `java -cp out com.audy.Main`
   - Point out the clean Swing interface, custom typography, and the search bar.
2. **Step 2: Predictive Autocomplete**
   - Type `"nirv"`. Highlight that after 350 ms, the non-blocking dropdown appears smoothly with *Smells Like Teen Spirit*, *Come As You Are*, etc., displaying diverse popularity scores (98, 95).
   - Add the letter `"a"` to demonstrate that typing is never interrupted and suggestions update instantaneously.
3. **Step 3: Generating Recommendations**
   - Click on *Smells Like Teen Spirit*. Show the animated loading screen.
   - The recommendations screen appears with the seed track pinned at the top and 28 distinct songs from related rock/grunge peers (*The Smashing Pumpkins, Foo Fighters, Rage Against the Machine, Pearl Jam, Queen, The Cranberries*).
   - Point out that all popularity scores are distinct (e.g., 95, 81, 76, 69) and at least 10 results are displayed (RNF-03).
4. **Step 4: External Playback**
   - Click on `[Open ↗]` on one of the cards. Show the browser immediately opening the live Deezer track page.
   - Click on the card to open `DetailPanel` and demonstrate the 30-second preview playback.
5. **Step 5: Dynamic Diversity Check**
   - Return to Home, search *Smells Like Teen Spirit* again. Show that the new recommendation set contains different peers and songs, demonstrating genuine algorithmic dynamism.

---

## 4. Verification & Testing (1 Minute)

- Execute: `java -cp out com.audy.test.RequirementsVerification`
- Point out the test summary:
  ```text
  ======================================================
     AUDY — Requirements Verification Test Suite (F8)   
     TOTAL: 37 PASSED  |  0 FAILED
  ======================================================
  ```
- Mention that the test suite covers JSON parsing, Track deduplication, debounce timing, adaptive popularity thresholds, and error handling without requiring mock servers.

---

## 5. Competencies Developed & Course Improvement Proposals

### Competencies Developed
1. **Advanced Object-Oriented Design**: Application of MVC, Strategy, and Adapter patterns in a production-grade Java application.
2. **Asynchronous Desktop Programming**: Managing background worker threads (`SwingWorker`) and Event Dispatch Thread (EDT) safety in Swing.
3. **API Integration & Resilient Architecture**: Designing fail-safe HTTP clients with zero third-party libraries using standard Java 11 `HttpClient`.

### Constructive Proposals for Course Improvement
1. **Early Real-World API Sandbox Guidelines**: Provide early warnings in syllabus regarding commercial API shifts (such as Spotify's deprecation of developer endpoints) to guide students toward open APIs like Deezer or MusicBrainz earlier in the semester.
2. **Modern GUI Architecture Modules**: Introduce dedicated workshops on Swing concurrency (`SwingWorker`, EDT thread safety) and modern Java FX/Swing hybrid UI patterns to avoid common pitfalls with focus-grabbing dropdowns.
3. **Continuous Integration (CI) Templates**: Integrate standard GitHub Actions templates for automated `javac` and test suite execution on pull requests.
