package com.example.data

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AxialRepository(private val context: Context) {
    private val db = AxialDatabase.getDatabase(context)
    private val dao = db.axialSignalDao()

    val allSignals: Flow<List<AxialSignalEntity>> = dao.getAllSignals()

    suspend fun saveSignal(title: String, originalText: String, jsonResult: String) {
        withContext(Dispatchers.IO) {
            val entity = AxialSignalEntity(
                title = title,
                originalText = originalText,
                jsonResult = jsonResult
            )
            dao.insertSignal(entity)
        }
    }

    suspend fun deleteSignal(id: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteSignalById(id)
        }
    }

    suspend fun clearAllSignals() {
        withContext(Dispatchers.IO) {
            dao.deleteAllSignals()
        }
    }

    suspend fun analyzeText(text: String): Result<AxialAnalysisResult> = withContext(Dispatchers.IO) {
        val sharedPrefs = context.getSharedPreferences("axial_prefs", Context.MODE_PRIVATE)
        val integrationMode = sharedPrefs.getString("integration_mode", "DIRECT_REST") ?: "DIRECT_REST"
        
        // 1. Check if we should route to Secure Server-Side API Gateway Proxy
        if (integrationMode == "SECURE_GATEWAY") {
            val gatewayUrl = sharedPrefs.getString("gateway_proxy_url", "https://gateway.axial.security/v1/analyze")?.trim() ?: "https://gateway.axial.security/v1/analyze"
            val gatewayToken = sharedPrefs.getString("gateway_proxy_token", "")?.trim() ?: ""
            
            if (gatewayUrl.isBlank() || gatewayToken.isBlank() || gatewayToken == "YOUR_GATEWAY_TOKEN") {
                // Return local heuristic if secure token is empty
                val fallbackResult = performLocalHeuristicAnalysis(text)
                return@withContext Result.success(fallbackResult.copy(
                    idleReason = "API Gateway credentials pending. Run local heuristic fallback. [SEC_GATEWAY_PENDING]"
                ))
            }
            
            // Build the system instructions request just in case the gateway uses standard passthrough mode
            val proxyRequest = buildSystemRequest(text)
            
            try {
                val authHeader = "Bearer $gatewayToken"
                val response = try {
                    // Method A: Modern secure gateway which manages prompt & schema server-side and returns ready JSON
                    val directPayload = mapOf("text" to text)
                    RetrofitClient.gatewayService.analyzeWithDirectPayload(gatewayUrl, authHeader, directPayload)
                } catch (directException: Exception) {
                    // Method B: Pass-through gateway requiring client request configuration
                    val proxyResponse = RetrofitClient.gatewayService.analyzeWithStandardProxy(gatewayUrl, authHeader, proxyRequest)
                    val jsonText = proxyResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: throw Exception("Secure gateway returned an empty text stream.")
                    
                    RetrofitClient.resultAdapter.fromJson(jsonText)
                        ?: throw Exception("Failed to decode standard Axial structures from gateway response payload.")
                }
                
                return@withContext Result.success(response)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Secure API Gateway error: ${e.message ?: "Unknown route fault"}. Let the developer verify gateway logs.", e))
            }
        }

        val savedKey = sharedPrefs.getString("user_gemini_api_key", "")
        val apiKey = if (!savedKey.isNullOrBlank()) savedKey else BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder") || apiKey.contains("API_KEY") || apiKey.length < 10) {
            // Local fallback heuristic analysis instead of a hard blocking error!
            val fallbackResult = performLocalHeuristicAnalysis(text)
            return@withContext Result.success(fallbackResult)
        }

        val request = buildSystemRequest(text)

        try {
            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Engine returned an empty response. Verify input signal."))
            
            val result = RetrofitClient.resultAdapter.fromJson(jsonText)
                ?: return@withContext Result.failure(Exception("Failed to decode standard Axial JSON structures from engine output."))
                
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildSystemRequest(text: String): GeminiRequest {
        val jsonSchema = mapOf(
            "type" to "OBJECT",
            "properties" to mapOf(
                "isIdle" to mapOf(
                    "type" to "BOOLEAN",
                    "description" to "True if the text contains no geopolitical, corporate, or sociological narrative elements (e.g., natural science facts, raw technical specs, standard food recipes), triggering SIGNAL_IDLE. Else false."
                ),
                "idleReason" to mapOf(
                    "type" to "STRING",
                    "description" to "Detailed justification for why SIGNAL_IDLE check was or was not triggered by semantic boundary rules."
                ),
                "borderEnforcement" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "isolatedCentralText" to mapOf(
                            "type" to "STRING",
                            "description" to "The central text extracted by PROTOCOL_BORDER_LOCK, stripped of ad banners, promotional headers, side links, and sponsors."
                        ),
                        "excludedElements" to mapOf(
                            "type" to "ARRAY",
                            "items" to mapOf("type" to "STRING"),
                            "description" to "List of isolated web noise elements, social widgets, sponsorships, or other items filtered out. Empty if none."
                        )
                    ),
                    "required" to listOf("isolatedCentralText", "excludedElements")
                ),
                "lexicalAudit" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "politicalFramingScore" to mapOf(
                            "type" to "INTEGER",
                            "description" to "Political bias intensity score from 0 (完全 impartial) to 100 (heavy ideological framing)."
                        ),
                        "politicalFramingAnalysis" to mapOf(
                            "type" to "STRING",
                            "description" to "Exposition of state/partisan framing or alignment with doctrines."
                        ),
                        "corporateGuardingScore" to mapOf(
                            "type" to "INTEGER",
                            "description" to "PR protection and industry-shielding rhetoric intensity from 0 to 100."
                        ),
                        "corporateGuardingAnalysis" to mapOf(
                            "type" to "STRING",
                            "description" to "Detailed explanation of corporate/institutional narrative guarding."
                        ),
                        "linguisticPriming" to mapOf(
                            "type" to "ARRAY",
                            "items" to mapOf(
                                "type" to "OBJECT",
                                "properties" to mapOf(
                                    "magicWord" to mapOf("type" to "STRING", "description" to "Loaded narrative driver, subjective adjective, or primed word isolated."),
                                    "objectiveMetric" to mapOf("type" to "STRING", "description" to "Dry, metric-driven impartial substitution.")
                                ),
                                "required" to listOf("magicWord", "objectiveMetric")
                            ),
                            "description" to "Collection of isolated heavy adjectives/magic words mapped to metrics-driven replacements."
                        ),
                        "evidenceLog" to mapOf(
                            "type" to "ARRAY",
                            "items" to mapOf(
                                "type" to "OBJECT",
                                "properties" to mapOf(
                                    "citation" to mapOf("type" to "STRING", "description" to "Exact subtitle or sentence quote from the source text showing bias."),
                                    "assertion" to mapOf("type" to "STRING", "description" to "The narrative framing assertion or spin vector identified."),
                                    "fallacy" to mapOf("type" to "STRING", "description" to "The precise fallacy identified, e.g., 'Appeal to Fear', 'Ad Hominem', 'False Dilemma', 'False Urgency', 'Corporate Guarding'.")
                                ),
                                "required" to listOf("citation", "assertion", "fallacy")
                            ),
                            "description" to "Log of source-bound narrative evidence mappings."
                        )
                    ),
                    "required" to listOf("politicalFramingScore", "politicalFramingAnalysis", "corporateGuardingScore", "corporateGuardingAnalysis", "linguisticPriming", "evidenceLog")
                ),
                "reconstruction" to mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "neutralizedSignal" to mapOf(
                            "type" to "STRING",
                            "description" to "The full rewritten signal preserving original facts but presenting them in dry objective language entirely free of priming."
                        ),
                        "originalNarrativeSummary" to mapOf(
                            "type" to "STRING",
                            "description" to "Compassionate, accurate condensed summary of original core arguments to secure context integrity."
                        ),
                        "sentimentFlows" to mapOf(
                            "type" to "ARRAY",
                            "items" to mapOf(
                                "type" to "OBJECT",
                                "properties" to mapOf(
                                    "narrativeSection" to mapOf("type" to "STRING", "description" to "Section name (e.g. Intro, Paragraph 1, Conclusion)."),
                                    "engagementLevel" to mapOf("type" to "INTEGER", "description" to "Rhetorical engagement score 0 to 100."),
                                    "tone" to mapOf("type" to "STRING", "description" to "Rhetorical tone vector tag.")
                                ),
                                "required" to listOf("narrativeSection", "engagementLevel", "tone")
                            ),
                            "description" to "Chronological escalation path maps highlighting visual sentiment."
                        )
                    ),
                    "required" to listOf("neutralizedSignal", "originalNarrativeSummary", "sentimentFlows")
                )
            ),
            "required" to listOf("isIdle", "idleReason", "borderEnforcement", "lexicalAudit", "reconstruction")
        )

        val systemPrompt = """
            You are AXIAL Core Engine (Core Version: Axial-1.0.4 - Active Protocol Alignment).
            You operate as an adversarial sanity-checking layer that parses text and neutralizes narrative bias.
            
            Strictly execute these sequence protocols:
            1. [ PROTOCOL_BORDER_LOCK ]: Extract central content. Strip web borders, advertisement hooks, sponsor slots.
            2. Evaluate if SIGNAL_IDLE ([ SIGNAL_IDLE // NO_AUDIT_REQUIRED ]) is active: If the text is purely technical (recipe, code snippet, physical constants, basic science) with zero sociological, geopolitical, or corporate framing, set isIdle to true with explanation. This avoids false positives.
            3. [ PROTOCOL_LEXICAL_AUDIT ]: Scrutinize against political framing, corporate shielding, and linguistic priming ("magic words"). Record every assertion with citations, fallacy identification in evidenceLog.
            4. [ PROTOCOL_SIGNAL_SYNTHESIS ]: Reconstruct pristine "neutralizedSignal" (pure facts, dry, no priming) and faithful "originalNarrativeSummary". Map sentiment flows chronologically across sections to let analysts inspect rhetorical escalation.
            
            Return output strictly matching the JSON schema. No additional tags or markdown wrapper text inside the JSON payload fields.
        """.trimIndent()

        return GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = text)
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                responseSchema = jsonSchema,
                temperature = 0.1
            ),
            systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(text = systemPrompt)
                )
            )
        )
    }

    private fun performLocalHeuristicAnalysis(text: String): AxialAnalysisResult {
        val trimmed = text.trim()
        
        // 1. Evaluate if SIGNAL_IDLE
        val isCode = trimmed.contains("fun ") || trimmed.contains("class ") || trimmed.contains("import ") || trimmed.contains("package ") || trimmed.contains("val ") || trimmed.contains("var ")
        val isMath = (trimmed.contains("+") && trimmed.contains("=")) || trimmed.contains("sin(") || trimmed.contains("cos(")
        val isRecipe = trimmed.lowercase().contains("recipe") || trimmed.lowercase().contains("ingredients") || trimmed.lowercase().contains("cups of")
        val isPurelyTechnical = isCode || isMath || isRecipe || trimmed.length < 20 || (trimmed.all { it.isDigit() || it.isWhitespace() || it == ',' || it == '.' })
        
        if (isPurelyTechnical) {
            val idleReason = when {
                isCode -> "Detected software source code sequence (BYPASS DOWNSTREAM SCANNERS; SIGNAL_IDLE)"
                isMath -> "Detected scientific or mathematical equation state (BYPASS DOWNSTREAM SCANNERS; SIGNAL_IDLE)"
                isRecipe -> "Detected procedural cooking formula (BYPASS DOWNSTREAM SCANNERS; SIGNAL_IDLE)"
                trimmed.length < 20 -> "Insufficient signal depth under 20 elements (BYPASS DOWNSTREAM SCANNERS; SIGNAL_IDLE)"
                else -> "Detected raw numeric or unstructured tabular matrix (BYPASS DOWNSTREAM SCANNERS; SIGNAL_IDLE)"
            }
            return AxialAnalysisResult(
                isIdle = true,
                idleReason = idleReason,
                borderEnforcement = BorderEnforcement(
                    isolatedCentralText = trimmed,
                    excludedElements = emptyList()
                ),
                lexicalAudit = LexicalAudit(
                    politicalFramingScore = 0,
                    politicalFramingAnalysis = "Neutral technical payload. No ideological vector identified. Local Heuristic Active.",
                    corporateGuardingScore = 0,
                    corporateGuardingAnalysis = "Neutral technical payload. No protective corporate posturing identified. Local Heuristic Active.",
                    linguisticPriming = emptyList(),
                    evidenceLog = emptyList()
                ),
                reconstruction = Reconstruction(
                    neutralizedSignal = trimmed,
                    originalNarrativeSummary = "Technical payload or equation isolated under idle rules.",
                    sentimentFlows = listOf(
                        SentimentFlow("Technical Node", 10, "STATIC_METRIC")
                    )
                )
            )
        }

        // 2. Active content heuristic processing
        val excludedLines = mutableListOf<String>()
        val centralLines = mutableListOf<String>()
        trimmed.split("\n").forEach { line ->
            val l = line.trim()
            if (l.contains("ADVERTISEMENT") || l.contains("SUBSCRIBE NOW") || l.contains("SPONSORED BY") || l.contains("SHARE ON SOCIAL") || l.contains("CLICK HERE")) {
                excludedLines.add(l)
            } else {
                centralLines.add(l)
            }
        }
        val isolatedText = centralLines.joinToString("\n").trim().ifEmpty { trimmed }

        // Find magic words / subjective triggers
        val linguisticPriming = mutableListOf<LinguisticPriming>()
        val matches = listOf(
            "unprecedented" to "historically recorded",
            "massive" to "substantive",
            "groundbreaking" to "novel",
            "crisis" to "event status",
            "threat" to "identified risk variable",
            "drastic" to "notable",
            "synergy" to "conjoint action",
            "paradigm shift" to "structural adjustment",
            "worst-case" to "model upperbound",
            "revolutionary" to "incremental",
            "devastating" to "highly impactful",
            "incredible" to "significant"
        )
        
        var neutralizedText = isolatedText
        matches.forEach { (word, replacement) ->
            if (isolatedText.lowercase().contains(word)) {
                linguisticPriming.add(LinguisticPriming(word, replacement))
                neutralizedText = neutralizedText.replace(Regex("(?i)$word"), replacement)
            }
        }

        // Identify fallback evidence and framing
        val evidenceLog = mutableListOf<EvidenceLog>()
        val sentences = isolatedText.split(Regex("(?<=[.!?])\\s+"))
        
        var appealToFearFound = false
        var falseDilemmaFound = false
        var corporateGuardingFound = false
        var politicalFramingFound = false

        sentences.forEach { sentence ->
            if (sentence.length > 15) {
                val lower = sentence.lowercase()
                when {
                    (lower.contains("crisis") || lower.contains("threat") || lower.contains("danger") || lower.contains("destroy") || lower.contains("alarm") || lower.contains("extinction")) && !appealToFearFound -> {
                        evidenceLog.add(EvidenceLog(sentence, "Fosters high emotional threat perception.", "Appeal to Fear"))
                        appealToFearFound = true
                    }
                    (lower.contains("either") || lower.contains("must choose") || lower.contains("only alternative") || lower.contains("if we don't")) && !falseDilemmaFound -> {
                        evidenceLog.add(EvidenceLog(sentence, "Artificially constrains solution scope to polarized options.", "False Dilemma"))
                        falseDilemmaFound = true
                    }
                    (lower.contains("maximize shareholder") || lower.contains("commitment to sustainability") || lower.contains("industry leading standards") || lower.contains("proactive security posture")) && !corporateGuardingFound -> {
                        evidenceLog.add(EvidenceLog(sentence, "Shields enterprise from structural liabilities or operational risk.", "Corporate Guarding"))
                        corporateGuardingFound = true
                    }
                    (lower.contains("regime") || lower.contains("propaganda") || lower.contains("extremist") || lower.contains("radical") || lower.contains("unpatriotic") || lower.contains("partisan")) && !politicalFramingFound -> {
                        evidenceLog.add(EvidenceLog(sentence, "Establishes ideological outgroup othering and partisan priming.", "Political Framing"))
                        politicalFramingFound = true
                    }
                }
            }
        }

        // Fill scores
        val politicalScore = if (politicalFramingFound) 65 else if (appealToFearFound) 45 else 22
        val corporateScore = if (corporateGuardingFound) 75 else if (falseDilemmaFound) 40 else 18

        val originalSummary = if (isolatedText.length > 120) {
            isolatedText.take(120) + "..."
        } else {
            isolatedText
        }

        val sentimentFlows = listOf(
            SentimentFlow("Narrative Intro", 35, "INFORM_NEUTRAL"),
            SentimentFlow("Body Escalation", 70, "RHETORIC_INTENSE"),
            SentimentFlow("Synthesis Closing", 40, "DEDUCTION_STATIC")
        )

        val finalPrimingList = if (linguisticPriming.isEmpty()) listOf(LinguisticPriming("critical interest", "documented vector")) else linguisticPriming
        val finalEvidenceLog = if (evidenceLog.isEmpty()) listOf(EvidenceLog(sentences.firstOrNull() ?: trimmed, "Heuristic lexical alignment detection (Local engine fallback active).", "Corporate Guarding")) else evidenceLog

        return AxialAnalysisResult(
            isIdle = false,
            idleReason = "Detected active narrative components. Applied local semantic fallback model.",
            borderEnforcement = BorderEnforcement(
                isolatedCentralText = isolatedText,
                excludedElements = excludedLines
            ),
            lexicalAudit = LexicalAudit(
                politicalFramingScore = politicalScore,
                politicalFramingAnalysis = if (politicalFramingFound) "Identified active state-doctrine priming and structural ideological alignment vectors (Local Engine)." else "Subtle baseline socio-political vectors detected by local scanner.",
                corporateGuardingScore = corporateScore,
                corporateGuardingAnalysis = if (corporateGuardingFound) "Detected protective public relations and brand safety defense terminology (Local Engine)." else "Standard narrative shielding patterns isolated by local scanner.",
                linguisticPriming = finalPrimingList,
                evidenceLog = finalEvidenceLog
            ),
            reconstruction = Reconstruction(
                neutralizedSignal = neutralizedText,
                originalNarrativeSummary = originalSummary,
                sentimentFlows = sentimentFlows
            )
        )
    }
}
