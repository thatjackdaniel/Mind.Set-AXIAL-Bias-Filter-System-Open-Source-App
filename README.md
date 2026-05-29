# AXIAL
## Adversarial Narrative Defusal Engine & Signal Audit Framework
> **Classification:** Technical Systems Design Whitepaper  
> **Status:** Core Version: Axial-1.0.4  
> **Repository Model:** Open Source / Distributed GitHub Node  

---

### I. Abstract
AXIAL addresses the semantic drift, biased cognitive priming, and institutional framing prevalent in distributed news syndication, journalism, and enterprise reports. Operating as a decentralized, zero-trust adversarial pre-processing layer, AXIAL intercepts arbitrary incoming text streams (lexical signals) and subjects them to highly granular structural auditing. 

By mapping high-order semantic vectors, highlighting linguistic "magic words," identifying logical fallacies, and plotting narrative heatmaps, AXIAL reconstructs a dry, strictly objective baseline signal. This whitepaper outlines the architectural standards, cryptographic boundary locks, state logic, and deep language processing pipeline powering AXIAL’s mobile subsystem.

---

### II. Core Operational Protocols

The AXIAL synthesis engine operates sequentially across four discrete processing phases to guarantee complete contextual purity.

```
       [ INCOMING TEXT SIGNAL ]
                  │
                  ▼
   1. [ PROTOCOL_BORDER_LOCK ]  ───► (Strips layout widgets, advertising anchors)
                  │
                  ▼
   2. [ SIGNAL_IDLE EVALUATION ] ──► (Screens standard math, recipes & factual lists)
                  │
                  ▼
   3. [ PROTOCOL_LEXICAL_AUDIT ] ──► (Identifies Fallacies, Bias Vectors & Priming)
                  │
                  ▼
   4. [ PROTOCOL_SIGNAL_SYNTHESIS ] ─► (Neutralizes prose, compiles Sentiment Flow)
                  │
                  ▼
      [ PERSISTED DATABASE LEDGER ]
```

#### 1. [ PROTOCOL_BORDER_LOCK ]
Incoming raw text signals harvested from web resources, syndication feeds, or clipboard states are often polluted with non-central visual, structural, and commercial components. The Border Lock protocol isolates the central core narrative content and strips:
* Sponsored advertising placements
* Institutional social metrics and widgets
* Layout-related sidebar links & metadata
* Interactive clickbait anchors

#### 2. [ SIGNAL_IDLE ] (Boundary Rule Bypass)
To prevent processing system overload and false-positive auditing, AXIAL integrates a factual exemption engine. When raw signals match high-certainty scientific specifications, physical constants, mathematical equations, software source code, or standard procedural recipes, the systems flags the diagnostic state as `SIGNAL_IDLE` with immediate reason documentation, halting the downstream adversarial scanner.

#### 3. [ PROTOCOL_LEXICAL_AUDIT ]
When a narrative signal is verified active, it is scrutinized across several narrative alignment markers:
* **Political Framing Intensity (`0–100`):** Evaluation of ideological grooming, partisan alignment, state-doctrine priming, and structural framing.
* **Corporate Guarding Intensity (`0–100`):** Recognition of PR protective language, institutional liabilities shielding, greenwashing, and defensive market framing.
* **Linguistic Priming Isolation:** Dynamic search matching highly subjective adjectives (termed "magic words") with dry, metric-driven impartial substitutions.
* **Evidence Log Array:** Structured mapping of source citations to detected narrative framing tactics and formal logical fallacy categorizations (such as *Appeal to Fear*, *False Dilemma*, *Ad Hominem*, or *Corporate Guarding*).

#### 4. [ PROTOCOL_SIGNAL_SYNTHESIS ]
In the final step, the audited signal is subjected to dry factual rewriting and structural mapping:
* **Factual Neutralization:** Re-drafting the text stream in cold, scientific language stripped entirely of rhetorical priming, subjective coloring, or emotion-inducing cadences.
* **Integrity Guard Summary:** A concise, highly representative summary of original central claims to preserve information parity while eliminating bias.
* **Tactical Sentiment Flow Analysis:** Segment-by-segment chronological recording of rhetorical engagement levels and tone vectors across the document layout, providing graphic analyst readouts.

---

### III. System Architecture & Component Mapping

AXIAL is built upon modern Android platform guidelines to ensure sub-second response times, offline-first structural reliability, and reactive data streams.

```
┌─────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                    │
├──────────────────────────────┬──────────────────────────────┤
│  Signal Intake UI (Scanner) │  Database Ledger (History)  │
└──────────────┬───────────────┴───────────────▲──────────────┘
               │                               │
               ▼                               │
┌──────────────────────────────────────────────┴──────────────┐
│                    ViewModel & State Flows                  │
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      AxialRepository                        │
├──────────────────────────────┬──────────────────────────────┤
│         Gemini API           │        Room Database         │
│     (REST RESTful Core)      │     (Local SQL Ledger)       │
└──────────────────────────────┴──────────────────────────────┘
```

#### 1. Persistence Subsystem (Room DB Ledger)
All successfully mapped and audited signals are committed to a localized SQLite engine via Android's Room Persistence library. This architecture guarantees:
* **Failsafe Historic Audits:** Instant local lookup of previous analysis configurations.
* **Transactional Reliability:** Direct data stream binding using Kotlin `Flow` patterns tracking structural changes in real-time.
* **Clean Purging Protocols:** A centralized ledger purge process to handle data hygiene demands.

#### 2. Reactive Presentation Layer (MVVM & Compose)
* **Jetpack Compose:** Devoiced from legacy XML schemas, the UI utilizes direct, single-pass coordinate compilation conforming to Material Design 3 guidelines.
* **Sophisticated Dark Aesthetic:** Pure `#050505` canvases framed by `#1F1F1F` borders, high-contrast `#00FF41` active indices, and `#E5E5E7` typographic accents minimizing physical strain under active watch conditions.
* **StateFlow Coherence:** ViewModels project read-only state interfaces preventing concurrent write hazards or race conditions.

---

### IV. Technical Specification & Implementation Guide

* **SDK Requirements:** `minSdk = 26`, `targetSdk = 34`
* **Primary Language:** Kotlin `v1.9+`
* **Dependency Tree (Core):**
  * Core Architecture: AndroidX Lifecycle, ViewModel Compose, Flow integration
  * Database Engine: SQLite mapped by Android Jetpack Room with Kotlin Symbol Processing (`KSP`)
  * Networking: Retrofit HTTP client with OkHttp interceptor setups
  * Serialization: Kotlinx-Serialization JSON compiler plugin alignment
  * Test Framework: JUnit 4, Robolectric headless JVM, Roborazzi screenshot verification

---

### V. Security Engineering & Compliance Note

As an open-source framework intended for public staging and distribution via GitHub, AXIAL enforces strict separation of code structure from live credentials. 

#### Cryptographic Key Isolation
Any Gemini API properties or operational secrets are parsed at build-time using the **Secrets Gradle Plugin** combined with an untracked, git-ignored local `.env` configuration file, ensuring developer endpoints cannot leak to the public repository structure.

> #### Security Warning & Compliance Note
> **Security Warning: Your Gemini API key is managed securely through the Secrets panel in AI Studio and accessed in the codebase via BuildConfig. Please be aware that Android APKs can be decompiled, and embedded properties might be extracted. Do not share the generated APK file publicly to prevent potential unauthorized usage of your API properties. If preparing for an enterprise production rollout, you should transition from direct client REST calls to a secure server-side API gateway proxy.**

Developers compiling customized nodes for open publication are explicitly instructed to avoid hardcoding production secrets, to enforce proper reverse-engineering obfuscation policies (ProGuard/R8 rules), and to establish server-side sentinel proxies for all commercial deployments.
