# Audy: Musical Recommendation and Filtering System based on Song Metadata Analysis

**Audy** is a music discovery tool developed for the Object-Oriented Programming course at UADY.

The system allows users to start from a song they already know and discover similar music, as well as explore related genres, subgenres, artists, and other musical connections.

## Quick Start

### Prerequisites

- **Java 11** or higher (JDK)
- **Spotify Developer Account** with Client ID and Client Secret ([Create one here](https://developer.spotify.com/dashboard))
- Internet connection for API access

### Configuration

1. Set your Spotify API credentials as environment variables:

   **Windows (PowerShell):**
   ```powershell
   $env:SPOTIFY_CLIENT_ID = "your_client_id_here"
   $env:SPOTIFY_CLIENT_SECRET = "your_client_secret_here"
   ```

   **Linux / macOS:**
   ```bash
   export SPOTIFY_CLIENT_ID="your_client_id_here"
   export SPOTIFY_CLIENT_SECRET="your_client_secret_here"
   ```

2. See `config.properties.example` for reference.

### Build & Run

**Using Make (Linux/macOS):**
```bash
make          # Compile and run
make test     # Run requirements verification tests
make clean    # Remove compiled files
```

**Using javac directly (Windows):**
```powershell
# Compile
$sources = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
javac -d out -sourcepath src/main/java $sources

# Run the application
java -cp out com.audy.Main

# Run tests
java -cp out com.audy.test.RequirementsVerification
```

## Project Structure

```
MusicOrientedProgramming_Uady/
├── src/main/java/com/audy/
│   ├── Main.java                          # Application entry point
│   ├── api/                               # API client layer (RNF-05)
│   │   ├── APIClientException.java        # Custom exception (RF-11, RNF-02)
│   │   ├── MusicBrainzAPIClient.java      # MusicBrainz genre fallback
│   │   └── SpotifyAPIClient.java          # Spotify search, metadata, recommendations
│   ├── controllers/                       # MVC Controllers (F5)
│   │   ├── RecommendationController.java  # RF-05 to RF-09
│   │   └── SearchController.java         # RF-01 to RF-04, debounce
│   ├── models/                            # Data models
│   │   ├── Artist.java                    # Artist with genres
│   │   └── Track.java                     # Track with metadata, deduplication
│   ├── services/                          # Business logic
│   │   └── DiscoveryEngine.java           # Discovery, filtering, recommendations
│   ├── test/                              # Verification tests (F8)
│   │   └── RequirementsVerification.java  # 37 automated tests
│   ├── util/                              # Utilities
│   │   └── JsonParser.java               # Lightweight JSON parser
│   └── views/                             # Swing UI (F4)
│       ├── DetailPanel.java               # Song detail (RF-09)
│       ├── HomePanel.java                 # Home + search (RF-01, RF-02)
│       ├── MainFrame.java                 # Main window, CardLayout navigation
│       ├── MatchesPanel.java              # Search results (RF-03)
│       ├── RecommendationsPanel.java      # Recommendations list (RF-08)
│       └── UIHelper.java                  # Styling constants and factories
├── docs/                                  # Project documentation
│   ├── README.md                          # Detailed documentation index
│   ├── F1-requisitos-finales.md           # Final requirements (F1)
│   ├── F3-motor-recomendaciones.md        # Recommendation engine (F3)
│   ├── F5-arquitectura-mvc.md             # MVC architecture (F5)
│   ├── F6-uml-evolucion.md               # UML & design evolution (F6)
│   ├── F7-casos-de-uso-actualizados.md    # Updated use cases (F7)
│   ├── F8-verificacion-requisitos.md      # Requirements verification (F8)
│   ├── F9-bitacora-sprint3.md             # Sprint 3 log (F9)
│   ├── F10-contribucion-individual.md     # Individual contributions (F10)
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

## Documentation

The full project documentation is available here:
[View project documentation](docs/README.md)

### Third Delivery Documents (F1–F12)

| Task | Document | Description |
| --- | --- | --- |
| F1 | [Requisitos Finales](docs/F1-requisitos-finales.md) | Consolidated final requirements |
| F3 | [Motor de Recomendaciones](docs/F3-motor-recomendaciones.md) | Engine rules and parameters |
| F5 | [Arquitectura MVC](docs/F5-arquitectura-mvc.md) | MVC layers and user flow |
| F6 | [UML y Evolución](docs/F6-uml-evolucion.md) | Final class diagram and evolution |
| F7 | [Casos de Uso](docs/F7-casos-de-uso-actualizados.md) | Updated use cases |
| F8 | [Verificación](docs/F8-verificacion-requisitos.md) | Test results and traceability |
| F9 | [Bitácora Sprint 3](docs/F9-bitacora-sprint3.md) | Sprint log and task tracking |
| F10 | [Contribución Individual](docs/F10-contribucion-individual.md) | Individual contribution metrics |

## AUDY Video-Presentation

[![AUDY-Object Oriented Programming System (First Sequence)](https://img.youtube.com/vi/VIDEO_ID/0.jpg)](https://www.youtube.com/watch?v=vXwVwmr5FMA)

## Technical Highlights

- **No external dependencies** — Pure Java 11+ (java.net.http, javax.swing)
- **Custom JSON parser** — Recursive-descent parser replacing fragile regex
- **Adaptive popularity filtering** — 3-tier system: Mainstream, Mid-tier, Underground
- **Dual API fallback** — Spotify → MusicBrainz for genre enrichment
- **MVC architecture** — Clean separation: Models, Views (Swing), Controllers, Services
- **37 automated tests** — Covering JSON parsing, deduplication, error handling, edge cases
