# AXIAL // White Paper & Technical User Manual
## Decentralized Adversarial Narrative Defusal Engine & Secure Gateway Node Architecture
> **Document Classification:** Cryptographic Systems & Semantic Security Specification  
> **Core Version:** Axial-v1.0.4  
> **Status:** Released Node Deployment  

---

### I. Abstract

Distributed media syndication, institutional journalism, and corporate press updates are heavily saturated with structural bias, semantic priming, and defensive public relations posturing. These biases distort public information spaces and manipulate cognitive patterns through linguistic "magic words" (loaded adjectives) and logical fallacies. 

**AXIAL** is designed to defuse these narrative vectors. It acts as a zero-trust, adversarial pre-processing layer that intercepts, audits, and neutralizes raw text signals. By mapping semantic clusters, isolating structural boundaries, and rewriting prose in dry, objective, metrics-driven language, AXIAL establishes a neutral, informative baseline.

This document serves as the official **Systems White Paper and Operations Manual** for the AXIAL mobile node subsystem. It details our transition from vulnerable, direct client-side generative REST calls to an decoupled, highly secure server-side API Gateway Proxy topology, solving critical reverse-engineering exposure vulnerabilities in client deployments.

---

### II. The Reverse-Engineering Threat Model & Secure Routing Transition

#### 1. Legacy Architecture Vulnerability
In previous iterations (Legacy Model), raw mobile nodes communicated directly with Google Generative Language endpoints using a client-managed API key. While straightforward, this design presented severe operational hazards:
* **Obfuscation Limits:** Even with advanced compiler-level optimization, ProGuard rules, and Keystore storage, runtime API keys stored or entered on mobile clients are vulnerable to dynamic dump attacks and system-level interceptions.
* **APK Decompilation:** Malicious actors who obtain compiled Android APK packages can run reverse-engineering tools (e.g., `apktool`, `jadx`) to reconstruct compiled variables, intercepting proprietary default AI keys from compiled bytecode frameworks.
* **Rate-Limit Exhaustion:** Exposed developer keys can quickly be harvested, resulting in immediate exhaustion of rate quotas and unauthorized operational costs.

#### 2. The API Gateway Proxy Secure Topology
AXIAL v1.0.4 resolves these vulnerabilities by deprecating direct client REST configurations in favor of a **Secure Server-Side API Gateway Proxy** integration mode.

```
+------------------------------------------------------------------------------------------------+
|                                    GATEWAY PROXY TOPOLOGY                                      |
+------------------------------------------------------------------------------------------------+

                            Direct REST Mode (Decompile Vulnerable)
  ┌─────────────────┐             Client API Key Over Query              ┌──────────────────┐
  │   AXIAL Node    │ ─────────────────────────────────────────────────► │ Google Gemini REST│
  └─────────────────┘                                                    │ (Direct Endpoint)│
                                                                         └──────────────────┘
                            Secure API Gateway Proxy Mode (Decoupled)
  ┌─────────────────┐  Analyze Payload + Bearer JWT   ┌───────────────┐     Formulates System    ┌──────────────────┐
  │   AXIAL Node    │ ──────────────────────────────► │  API Gateway  │ ───────────────────────► │ Google Gemini REST│
  │  (No Google Key)│                                 │ (Server-Side) │  Injects Secret Key     │  (Secure Cloud)  │
  └─────────────────┘                                 └───────────────┘                          └──────────────────┘
```

Under this decoupled architecture:
1. **Encapsulation:** The Google Gemini API credential is kept entirely offline, resident in the server-side gateway environment variables.
2. **Authorization:** Mobile clients authorize via a short-lived **JSON Web Token (JWT) Bearer Token** issued by the systems administrator. The node passes this token in the header of POST transactions.
3. **Payload Sanitization:** The client sends the raw semantic signal. The server-side API Gateway securely formats the model prompts, binds the structural target JSON schema, injects system instructions, and proxies the query to the generative backend.
4. **Resiliency:** Should a node JWT be leaked, administrators can instantly revoke individual tokens at the gateway level without needing to roll out or regenerate the primary Google API keys.

---

### III. System Architecture & Component Mapping

The AXIAL ecosystem is partitioned into clear, reactive operational layers utilizing the Model-View-ViewModel (MVVM) software design pattern:

```
┌─────────────────────────────────────────────────────────────┐
|                       Jetpack Compose UI                    │
├──────────────────────────────┬──────────────────────────────┤
│  Signal Intake UI (Scanner)  │   Database Ledger (History)  │
└──────────────┬───────────────┴───────────────▲──────────────┘
               │                               │
               ▼                               │
┌──────────────────────────────────────────────┴──────────────┐
│                  ViewModel & State Flows                     │
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      AxialRepository                        │
├──────────────────────────────┬──────────────────────────────┤
│        Retrofit Client       │        Room Database         │
│  (Direct API / Secure Gate)   │     (Local SQL Ledger)       │
└──────────────────────────────┴──────────────────────────────┘
```

#### 1. Repository Routing Engine (`AxialRepository`)
The repository evaluates the active system `integration_mode` from local hardware shared preferences on every execution:
* **`DIRECT_REST` Mode:** Coordinates queries directly to Google's endpoints. Uses user overrides or compiled `BuildConfig` keys.
* **`SECURE_GATEWAY` Mode:** Directs requests to the custom configured reverse gateway proxy URL. It replaces key query parameters with a standard `Authorization: Bearer <token>` header payload.

#### 2. Network Interface Core (`RetrofitClient` & `GatewayApiService`)
Network transactions are managed using the Square Retrofit interface library paired with an asynchronous OkHttpClient backplane.
The gateway service defines two distinct transactional protocols to accommodate varying proxy capabilities:

* **Method A (Parsing Broker):**
  For advanced gateways that host the AI prompt and target schemas server-side, reducing client-side logic. The client transmits a lightweight direct payload map containing only the target text stream. The gateway processes the transaction and returns a direct `AxialAnalysisResult` instance.
  ```kotlin
  @POST fun analyzeWithDirectPayload(
      @Url url: String,
      @Header("Authorization") authHeader: String,
      @Body payload: Map<String, String>
  ): AxialAnalysisResult
  ```

* **Method B (Transport Broker):**
  For pure routing gateways that act as an authentication, rate-limiting, and key-injection loop to proxy raw Gemini queries securely. The client compiles the `GeminiRequest` itself, and receives a validated `GeminiResponse` stream transit.
  ```kotlin
  @POST fun analyzeWithStandardProxy(
      @Url url: String,
      @Header("Authorization") authHeader: String,
      @Body request: GeminiRequest
  ): GeminiResponse
  ```

---

### IV. Core Semantic Protocols

When processing a validated active signal, the AXIAL Core Engine executes four sequential protocols to ensure contextual integrity:

#### 1. [ PROTOCOL_BORDER_LOCK ]
Raw inputs harvested from web sweeps, RSS streams, or system clipboards are stripped of non-central noise elements:
- Sponsoring advertising banners and promotional hooks.
- Extraneous metadata and cross-reference links.
- Social sharing hooks and layout-related tracking parameters.

#### 2. [ SIGNAL_IDLE ] Evaluation
To minimize processing resources and prevent downstream false-positive alarms, inputs are put through a localized syntax screener. If the text consists purely of natural science constants, mathematics, source code (Kotlin/C/Python), or standard declarative recipes, the engine triggers a `SIGNAL_IDLE` bypass. It outputs a bypass reason and suspends downstream narrative auditing.

#### 3. [ PROTOCOL_LEXICAL_AUDIT ]
Active signals are subjected to strict parsing across three primary narrative vectors:
- **Political Bias Classification:** Tracks partisan framing intensity scores (`0-100`) and underlying ideological doctrine alignment.
- **Corporate Shielding Audit:** Evaluates corporate protective statements, brand liability hedging, and greenwashing descriptors.
- **Subjective Priming Isolation:** Pins emotional adjectives ("magic words") and converts them to cold, objective substitutions.
- **Evidence Log compilation:** Formulates structured arrays mapping source-text quotes directly to verified system logical fallacies (e.g. *Appeal to Fear*, *False Dilemma*, *Ad Hominem*, or *Corporate Guarding*).

#### 4. [ PROTOCOL_SIGNAL_SYNTHESIS ]
Generates the output products:
- **Neutralized Signal:** The original text reconstructed in dry, unbiased, cold factual prose.
- **Integrity Guard Summary:** A concise summary of the claims, ensuring semantic parity without bias.
- **Tactical Sentiment Flow:** Chronological tracing of conversational triggers and engagement elevations mapped visual segment-by-segment.

---

### V. Operator Deployment Guide & User Manual

To transition your AXIAL deployment node from raw direct REST client calls to the secure intermediate proxy system:

#### Step 1: Deploy server-side Gateway Proxy Node
Set up a containerized or serverless hosting gateway (e.g., Node.js / Python / Go) on your secure server cloud topology. Securely bind your master Google Gemini API Key in your server's system environment variables:
```bash
GEMINI_API_KEY="AIzaSyYourPrimarySecureSecretKeyHost..."
```
Your server-side gateway should expose an endpoint (e.g., `/v1/analyze`) that takes:
* Header: `Authorization: Bearer YOUR_GENERATED_NODE_TOKEN`
* Body: `{"text": "..."}` or `{"contents": [...]}`

The gateway forwards the request to Google's backend, performs parsing verification, and returns the audited standard `AxialAnalysisResult` payload structure.

#### Step 2: Open the Core Routing Console
1. Boot the AXIAL Android interface client on target hardware.
2. In the telemetry banner header, locate the security routing status. Under direct REST mode, it displays **`API_KEY_SEC`**. Tap this indicator button.
3. The client will open the **Core Routing Console** dialog.

#### Step 3: Toggle Routing Pipeline
1. At the top of the console, select the **API Gateway Proxy** routing segment tab.
2. Under **Gateway Endpoint URL**, enter the URL of your deployed secure proxy node, eg:
   `https://gateway.axial.security/v1/analyze`
3. Under **Authorization JWT Bearer Token**, paste the secure node access key issued by your system admin.
4. Tap **`SAVE_CONFIGURATION`**.

#### Step 4: Verify Telemetry Synchronization
Once saved:
- The telemetry header indicator will automatically initialize.
- The active route state will transition to **`ACTIVE ROUTE: SECURE GATEWAY`**.
- The main trigger diagnostic button will update its label to **`GATE_ROUTE`**.
- All subsequent text analyses will route securely through your gateway proxy without revealing raw credentials to client memory or decompile dump analyzers.

---

### VI. System Configuration Specs

* **Target Deployment SDK:** Android SDK API 26 (`Oreo`) to API 34 (`Android 14`).
* **Database Ledger:** SQL Jetpack Room SQLite layer.
* **Test Platform Specs:** Headless Unit Testing mapped via JVM Robolectric frameworks and Roborazzi visual screen capture regression tests.

---
*AXIAL Core Protocol -- Decentralizing Semantic Guardrails, Safeguarding Cognitive Sovereignty Dev-Node.*
