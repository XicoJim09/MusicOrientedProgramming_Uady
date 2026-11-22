# AUDY Final Delivery Documentation (F1 – F12)

This directory consolidates the official documentation for the **Third & Final Delivery** of the AUDY music discovery system, authored in **100% technical English** in compliance with the course rubric of Object-Oriented Programming at UADY.

---

## Deliverables Index

| Task ID | Document Name | Purpose & Contents |
|---|---|---|
| **F1** | [F1 · Final Requirements](F1-final-requirements.md) | Consolidated 11 Functional Requirements (RF) and 5 Non-Functional Requirements (RNF), quantitative network boundaries, and acceptance criteria. |
| **F2** | [F2 · API Connections & Justification](F2-api-connections.md) | Technical architecture of Deezer, Spotify, and MusicBrainz clients; formal justification of the Deezer pivot; why Spotify is retained. |
| **F3** | [F3 · Recommendation Engine](F3-recommendation-engine.md) | Specification of the 100% dynamic collaborative filtering graph, randomized peer sampling, strict deduplication, and adaptive popularity thresholds (RF-07). |
| **F5** | [F5 · MVC Architecture & User Journey](F5-mvc-architecture.md) | Strict Model-View-Controller layer separation, interactive user flows, and clarification on the cloud-native database / persistence concept («BD*»). |
| **F6** | [F6 · Final UML & Design Evolution](F6-uml-and-design-evolution.md) | Complete final system UML class diagram (Mermaid), architectural evolution across the 3 deliveries, and OOP justification. |
| **F7** | [F7 · Use Cases & Acceptance Criteria](F7-use-cases.md) | Detailed use case specifications (CU-01, CU-02, CU-03) with main, alternative, and exception flows mapped to RFs. |
| **F8** | [F8 · Requirements Verification](F8-requirements-verification.md) | Comprehensive traceability matrix and execution results for the 37 automated unit tests (**37/37 PASS**). |
| **F9** | [F9 · Sprint 3 Management Log](F9-sprint3-log.md) | Weekly engineering activities, roadblocks encountered (Spotify limits, Swing focus conflicts), and architectural resolutions. |
| **F10** | [F10 · Individual Contribution Assessment](F10-individual-contribution.md) | Multi-dimensional weighted formula and contribution percentages across all deliveries summing exactly 100.0%. |
| **F12** | [F12 · Presentation & Live Demo Script](F12-presentation-script.md) | Script for the 10-minute technical English presentation, live demo walkthrough, developed competencies, and course feedback. |

---

## Quick Compilation & Test Verification

```powershell
# Compile all Java sources
$sources = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
javac -d out -sourcepath src/main/java $sources

# Launch AUDY application
java -cp out com.audy.Main

# Execute automated test suite (37 tests)
java -cp out com.audy.test.RequirementsVerification
```
