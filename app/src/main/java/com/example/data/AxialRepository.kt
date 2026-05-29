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
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder")) {
            return@withContext Result.failure(Exception("Gemini API key is not configured in the Secrets panel of AI Studio."))
        }

        // Schema constraints mapping exactly to AxialAnalysisResult data structures
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

        val request = GeminiRequest(
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
}
