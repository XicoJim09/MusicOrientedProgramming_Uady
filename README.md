# Audy: Musical Recommendation and Filtering System based on Song Metadata Analysis

**Audy** is a music discovery tool developed for the Object-Oriented Programming course at UADY.

The system allows users to start from a song they already know and discover similar music, as well as explore related genres, subgenres, artists, and other musical connections.

## Quick Start

### Prerequisites

- **Java 11** or higher (JDK)
- Internet connection for API access
- *(Optional)* **Spotify Developer Account** if testing Spotify provider ([Create one here](https://developer.spotify.com/dashboard))

### Zero-Configuration Out of the Box (Deezer Engine)

Audy comes pre-configured with the **Deezer Music API** as the primary dynamic discovery engine (RNF-05). **No API keys or environment variables are required** to start searching and discovering music immediately!

```powershell
# Compile
$sources = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
javac -d out -sourcepath src/main/java $sources

# Run the application
java -cp out com.audy.Main

# Run requirements verification test suite (37 tests)
java -cp out com.audy.test.RequirementsVerification
```

### Optional: Spotify Configuration

If you wish to use the modular Spotify client:
1. Copy `config.properties.example` to `config.properties`.
2. Enter your `spotify.client.id` and `spotify.client.secret`.
Or set the environment variables:
- `SPOTIFY_CLIENT_ID`
- `SPOTIFY_CLIENT_SECRET`

### Build & Run (Linux / macOS)

```bash
make          # Compile and run
make test     # Run requirements verification tests
make clean    # Remove compiled files
```

## Project Structure

```
MusicOrientedProgramming_Uady/
├── src/main/java/com/audy/
│   ├── Main.java                          # Application entry point
│   ├── api/                               # Modular API client layer (RNF-05)
│   │   ├── APIClientException.java        # Custom exception (RF-11, RNF-02)
│   │   ├── DeezerAPIClient.java           # Deezer search, dynamic relations, top tracks & previews
│   │   ├── MusicBrainzAPIClient.java      # MusicBrainz genre & artist fallback
│   │   └── SpotifyAPIClient.java          # Spotify search & metadata
│   ├── controllers/                       # MVC Controllers (F5)
│   │   ├── RecommendationController.java  # RF-05 to RF-09 background worker
│   │   └── SearchController.java         # RF-01 to RF-04, 350ms debounce
│   ├── models/                            # Data models
│   │   ├── Artist.java                    # Artist with genres
│   │   └── Track.java                     # Track with metadata, deduplication & audio preview
│   ├── services/                          # Business logic
│   │   └── DiscoveryEngine.java           # Dynamic discovery, filtering & recommendations
│   ├── test/                              # Verification tests (F8)
│   │   └── RequirementsVerification.java  # 37 automated tests
│   ├── util/                              # Utilities
│   │   └── JsonParser.java               # Lightweight recursive-descent JSON parser
│   └── views/                             # Swing UI (F4)
│       ├── DetailPanel.java               # Song detail & external player launcher (RF-09)
│       ├── HomePanel.java                 # Home + search with non-blocking predict dropdown (RF-01, RF-02)
│       ├── MainFrame.java                 # Main window, CardLayout navigation
│       ├── MatchesPanel.java              # Search results (RF-03)
│       ├── RecommendationsPanel.java      # Recommendations list with direct Open links (RF-08)
│       └── UIHelper.java                  # Styling constants and factories
├── docs/                                  # Project documentation
│   ├── README.md                          # Detailed documentation index
│   ├── casos-de-uso/                      # Use case diagrams
│   ├── diseno/                            # Interaction flow, mockups, sketches
│   └── requirements/                      # RF & RNF documents
├── Makefile                               # Build automation
├── config.properties.example              # API credentials template
└── .gitignore                             # Git exclusions
```

## Team

| **Team Members** | **Roles in the project** |
| --- | --- |
| **Gil Juárez Joshua Nabil** | Product Owner |
| **Jiménez Hernández Xicoténcatl** | Scrum Master |
| **Antochiw Flores Andrés** (Leader) | Designer |
| **Thibault Raphael Didier Alexandre** | Designer |
| **Couoh Mis Guillermo Moises** | Designer |
| **Medina Uitz Eleazar** | Designer |

## Project Documentation (Third Delivery F1–F12)

The comprehensive technical documentation is organized inside the [`docs/final-delivery/`](docs/final-delivery/README.md) directory, written in **100% technical English**:

| Task | Document | Description |
|---|---|---|
| **F1** | [Final Requirements](docs/final-delivery/F1-final-requirements.md) | Consolidated 11 RF and 5 RNF, acceptance criteria, and latency bounds |
| **F2** | [API Connections & Justification](docs/final-delivery/F2-api-connections.md) | Multi-API integration (Deezer, Spotify, MusicBrainz) & technical justification |
| **F3** | [Recommendation Engine](docs/final-delivery/F3-recommendation-engine.md) | 100% dynamic collaborative graph, peer sampling, adaptive popularity (RF-07) |
| **F5** | [MVC Architecture](docs/final-delivery/F5-mvc-architecture.md) | Layer responsibilities, user journey flows, and persistence / BD* clarification |
| **F6** | [UML & Design Evolution](docs/final-delivery/F6-uml-and-design-evolution.md) | Final UML class diagram, 3-sprint evolution, and architectural justification |
| **F7** | [Updated Use Cases](docs/final-delivery/F7-use-cases.md) | Use case specifications CU-01 to CU-03 with main and alternative flows |
| **F8** | [Requirements Verification](docs/final-delivery/F8-requirements-verification.md) | Full traceability matrix and 37 automated test results (37/37 PASS) |
| **F9** | [Sprint 3 Log](docs/final-delivery/F9-sprint3-log.md) | Planning, challenges encountered (Spotify limits, Swing grabs), and decisions |
| **F10** | [Individual Contribution](docs/final-delivery/F10-individual-contribution.md) | Mathematical formula and weighted contribution percentages (100% total) |
| **F12** | [Final Presentation & Demo Guide](docs/final-delivery/F12-presentation-script.md) | 10-minute technical English script, live demo walkthrough, and course feedback |

## Technical Highlights

- **100% Dynamic Recommendation Graph** — No hardcoded knowledge base; recommendations are dynamically extracted from Deezer's collaborative listening graph and peer artists.
- **Zero external dependencies** — Pure Java 11+ (`java.net.http`, `javax.swing`) for guaranteed portability and immediate compilation.
- **Custom JSON parser** — Robust recursive-descent parser without Jackson/Gson dependencies.
- **Multi-API Modular Architecture (RNF-05)** — Seamless integration across Deezer API, Spotify Web API, and MusicBrainz API.
- **Adaptive popularity filtering (RF-07)** — Real popularity scores (0–100) mapped from Deezer stream ranks, dynamically bounded according to the seed song.
- **Debounced non-interfering autocomplete (RNF-04)** — Custom non-focus-stealing floating window ensuring fluid typing without mouse grab conflicts.
- **Direct Web & Preview Playback** — External player links (`https://www.deezer.com/track/...`) and 30-second audio previews.
- **37 automated verification tests (F8)** — Covering JSON parsing, deduplication, debounce timing, error recovery, and model constraints.
