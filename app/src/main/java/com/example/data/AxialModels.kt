package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BorderEnforcement(
    val isolatedCentralText: String = "",
    val excludedElements: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LinguisticPriming(
    val magicWord: String = "",
    val objectiveMetric: String = ""
)

@JsonClass(generateAdapter = true)
data class EvidenceLog(
    val citation: String = "",
    val assertion: String = "",
    val fallacy: String = ""
)

@JsonClass(generateAdapter = true)
data class LexicalAudit(
    val politicalFramingScore: Int = 0,
    val politicalFramingAnalysis: String = "",
    val corporateGuardingScore: Int = 0,
    val corporateGuardingAnalysis: String = "",
    val linguisticPriming: List<LinguisticPriming> = emptyList(),
    val evidenceLog: List<EvidenceLog> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SentimentFlow(
    val narrativeSection: String = "",
    val engagementLevel: Int = 0,
    val tone: String = ""
)

@JsonClass(generateAdapter = true)
data class Reconstruction(
    val neutralizedSignal: String = "",
    val originalNarrativeSummary: String = "",
    val sentimentFlows: List<SentimentFlow> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AxialAnalysisResult(
    val isIdle: Boolean = false,
    val idleReason: String = "",
    val borderEnforcement: BorderEnforcement = BorderEnforcement(),
    val lexicalAudit: LexicalAudit = LexicalAudit(),
    val reconstruction: Reconstruction = Reconstruction()
)
