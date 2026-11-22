# F10 · Individual Contribution Assessment & Methodology

This document establishes the evaluation methodology and the individual contribution percentages for all 6 members of the AUDY team across each of the three project deliveries and for the overall project, in strict compliance with criterion **F10** of the university rubric.

---

## 1. Weighted Contribution Formula

To evaluate contributions objectively based on verifiable repository artifacts in GitHub, a multi-dimensional weighted formula was established across 5 verifiable dimensions:

$$\text{Individual Score} = 0.20 \cdot A + 0.20 \cdot D + 0.30 \cdot P + 0.15 \cdot T + 0.15 \cdot Doc$$

Where:
- **$A$ (Analysis & Requirements)**: Definition of RF/RNF, API constraint analysis, and consolidation of use case specifications.
- **$D$ (Design & Prototyping)**: iPad sketches, Figma interaction workflows, and UML class modeling.
- **$P$ (Programming & Architecture)**: Development of entity models, Swing views, MVC controllers, API clients, and the custom JSON parser.
- **$T$ (Testing & Verification)**: Authoring the automated test suite (`RequirementsVerification`), network debugging, and live integration testing.
- **$Doc$ (Project Management & Documentation)**: Sprint logs, traceability matrices, presentation scripts, and repository maintenance.

---

## 2. Contribution Matrix per Delivery & Overall Project

All percentages for each delivery and in the cumulative project average strictly sum to **100.0%**:

| Team Member | Primary Role | Delivery 1 (%) | Delivery 2 (%) | Delivery 3 (%) | Overall Project (%) | Primary Verifiable Contributions |
|---|---|:---:|:---:|:---:|:---:|---|
| **Antochiw Flores Andrés** | Technical Leader & Designer | 18.0 % | 17.0 % | 16.5 % | **17.17 %** | iPad sketches, architectural coordination, Swing view styling, and UI integration review. |
| **Thibault Raphael Didier Alexandre** | Software Architect & Lead Developer | 16.0 % | 18.0 % | 20.0 % | **18.00 %** | MVC implementation, `DeezerAPIClient`, `JsonParser`, zero hardcoding refactor, and UI bug resolution. |
| **Gil Juárez Joshua Nabil** | Product Owner | 17.0 % | 17.0 % | 16.0 % | **16.67 %** | Backlog definition, Figma prototypes, acceptance criteria validation, and user testing. |
| **Jiménez Hernández Xicoténcatl** | Scrum Master | 16.5 % | 16.0 % | 16.0 % | **16.17 %** | Ceremony moderation, sprint logs, roadblock resolution, and traceability matrix tracking. |
| **Couoh Mis Guillermo Moises** | Developer & Quality Assurance | 16.5 % | 16.0 % | 16.0 % | **16.17 %** | RF/RNF refinement, test case design in `RequirementsVerification`, and Makefile automation. |
| **Medina Uitz Eleazar** | Designer & Documentation Lead | 16.0 % | 16.0 % | 15.5 % | **15.83 %** | User story authoring, visual design resources, final presentation script, and documentation. |
| **TOTAL** | — | **100.0 %** | **100.0 %** | **100.0 %** | **100.0 %** | Exact 100.0% sum verified for each project milestone. |

---

## 3. Explanatory Rationale & Adjustments

1. **Equitable Collaborative Distribution**:
   The team maintained balanced participation throughout the semester, with justifiable shifts corresponding to the software engineering lifecycle: higher emphasis on requirements and visual design during Deliveries 1 and 2, and higher emphasis on Java coding, API debugging, and automated verification in Delivery 3.
2. **Technical Adjustment in Delivery 3**:
   The adjustment in Delivery 3 reflects the intensive engineering required to resolve the critical Spotify Web API limitations (HTTP 400 errors and blocked recommendation endpoints), build the zero-credential Deezer engine, and engineer the non-focus-stealing autocomplete dropdown.
