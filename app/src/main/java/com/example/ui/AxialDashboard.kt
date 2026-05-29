package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun AxialDashboard(viewModel: AxialViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisResult by viewModel.analysisResult.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val allHistory by viewModel.allHistory.collectAsStateWithLifecycle()
    val inputSignal by viewModel.inputSignal.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("axial_prefs", Context.MODE_PRIVATE) }
    var showSecurityWarningDialog by remember {
        mutableStateOf(!sharedPrefs.getBoolean("has_acknowledged_sec_warning", false))
    }

    if (showSecurityWarningDialog) {
        AlertDialog(
            onDismissRequest = {}, // Must be acknowledged to use the app
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Security Status Check",
                        tint = AxialRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "SECURITY COMPLIANCE SEC-C10",
                        color = AxialTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Security Warning: Your Gemini API key is managed securely through the Secrets panel in AI Studio and accessed in the codebase via BuildConfig. Please be aware that Android APKs can be decompiled, and embedded properties might be extracted. Do not share the generated APK file publicly to prevent potential unauthorized usage of your API properties. If preparing for an enterprise production rollout, you should transition from direct client REST calls to a secure server-side API gateway proxy.",
                        color = AxialTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Justify
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "[ SYSTEM METADATA: GITHUB OPEN SOURCE ]",
                        color = AxialPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Because this codebase is pushing to a public repository, actual active keys are dynamically queried from untracked local .env configuration systems. Any assets embedded inside compiled APK units remain extractable by static decompilers. Exercise full alignment with zero-trust design.",
                        color = AxialTextSecondary.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sharedPrefs.edit().putBoolean("has_acknowledged_sec_warning", true).apply()
                        showSecurityWarningDialog = false
                    },
                    modifier = Modifier.testTag("security_warning_acknowledge_button")
                ) {
                    Text(
                        text = "ACKNOWLEDGE_PROTOCOL",
                        color = AxialPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            containerColor = AxialSurfaceElevated,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .border(BorderStroke(1.dp, AxialBorder), shape = RoundedCornerShape(24.dp))
                .testTag("security_warning_dialog")
        )
    }

    // Error handling
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("axial_main_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AxialBottomNavbar(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TelemetryHeader()
            
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Absolute watermark as a subtle floating background element
                Text(
                    text = "MIND.SET AGI INITIATIVE",
                    color = AxialTextPrimary.copy(alpha = 0.04f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                        .rotate(90f)
                )

                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                    },
                    label = "tab_animation"
                ) { tab ->
                    when (tab) {
                        "analyze" -> SignalIntakeScreen(
                            inputSignal = inputSignal,
                            isAnalyzing = isAnalyzing,
                            onInputChange = { viewModel.setInputSignal(it) },
                            onTriggerAnalyze = { viewModel.analyzeCurrentSignal() }
                        )
                        "details" -> DiagnosticForensicsScreen(
                            result = analysisResult,
                            onBackToIntake = { viewModel.setTab("analyze") }
                        )
                        "history" -> HistoricLedgerScreen(
                            historyList = allHistory,
                            onSelectSignal = { viewModel.selectHistorySignal(it) },
                            onDeleteSignal = { viewModel.deleteHistoricalSignal(it) },
                            onPurgeAll = { viewModel.clearAllHistory() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AxialSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "AXIAL // SYSTEM REPORT",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AxialPrimary,
                letterSpacing = 1.5.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AxialAccent, shape = RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ACTIVE PROTOCOL ALIGNMENT [ Axial-1.0.4 ]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = AxialTextSecondary
                )
            }
        }
        
        Box(
            modifier = Modifier
                .border(1.dp, AxialPrimary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .background(AxialSurfaceElevated)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "AGI.MIND.SET",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = AxialPrimary
            )
        }
    }
}

@Composable
fun AxialBottomNavbar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = AxialSurface,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, AxialBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AxialNavButton(
                icon = Icons.Default.Transform,
                label = "INTAKE",
                isActive = currentTab == "analyze",
                tag = "nav_tab_intake",
                onClick = { onTabSelected("analyze") }
            )
            AxialNavButton(
                icon = Icons.Default.Analytics,
                label = "FORENSICS",
                isActive = currentTab == "details",
                tag = "nav_tab_forensics",
                onClick = { onTabSelected("details") }
            )
            AxialNavButton(
                icon = Icons.Default.History,
                label = "LEDGER",
                isActive = currentTab == "history",
                tag = "nav_tab_ledger",
                onClick = { onTabSelected("history") }
            )
        }
    }
}

@Composable
fun AxialNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val tintColorValue = if (isActive) AxialPrimary else AxialTextSecondary
    val scale = if (isActive) 1.05f else 1.0f

    Column(
        modifier = Modifier
            .testTag(tag)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColorValue,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = tintColorValue,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SignalIntakeScreen(
    inputSignal: String,
    isAnalyzing: Boolean,
    onInputChange: (String) -> Unit,
    onTriggerAnalyze: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // App intro
        Text(
            text = "AXIAL Impartiality Engine",
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AxialPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "High-fidelity semantic parsing designed to neutralize narrative bias in media text signals.",
            style = MaterialTheme.typography.bodyMedium,
            color = AxialTextSecondary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Signal Entry Area
        Text(
            text = "TRANS-RECEPTOR INPUT CAPTURE",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = AxialPrimary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = inputSignal,
            onValueChange = onInputChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .testTag("signal_input_field"),
            placeholder = {
                Text(
                    text = "Pasted corporate release, geopolitical editorial, or standard science facts context to probe...",
                    color = AxialTextSecondary.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AxialTextPrimary,
                unfocusedTextColor = AxialTextPrimary,
                focusedContainerColor = AxialSurface,
                unfocusedContainerColor = AxialBg,
                focusedBorderColor = AxialPrimary,
                unfocusedBorderColor = AxialBorder,
                cursorColor = AxialPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = TextStyle(fontSize = 14.sp, fontFamily = FontFamily.SansSerif)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Analysis control
        if (isAnalyzing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AxialSurface, shape = RoundedCornerShape(8.dp))
                    .border(1.dp, AxialPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    color = AxialPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "PROTOCOL_BORDER_LOCK -> [ SECURING CORE TEXT BODY ]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = AxialAccent,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Performing multi-vector lexical audit, detecting corporate shielding, measuring political partisan framing, and synthesizing neutralized sovereign signals...",
                    fontSize = 11.sp,
                    color = AxialTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Button(
                onClick = onTriggerAnalyze,
                colors = ButtonDefaults.buttonColors(containerColor = AxialPrimary, contentColor = AxialBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("analyze_signal_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DECRYPT & ENFORCE BORDERS [ ANALYZE ]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // PRESET FEEDS FOR QUICK VALIDATION
        Text(
            text = "SYSTEM TEST CONTEXT PRESETS",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = AxialTextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        presetFeeds.forEachIndexed { index, feed ->
            PresetCard(feed = feed) {
                onInputChange(feed.text)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun PresetCard(feed: PresetFeed, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AxialSurface),
        border = BorderStroke(1.dp, AxialBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(feed.color, shape = RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = feed.title,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AxialTextPrimary
                )
                Text(
                    text = feed.typeDescription,
                    fontSize = 10.sp,
                    color = AxialTextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select Preset",
                tint = AxialPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DiagnosticForensicsScreen(
    result: AxialAnalysisResult?,
    onBackToIntake: () -> Unit
) {
    if (result == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Awaiting analytical stream input",
                tint = AxialSecondary,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "AWAITING ACTIVE STREAM INPUT",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AxialPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Paste text inside the INTAKE portal, calibrate presets, or select an asset from the SQLite audit Ledger history.",
                style = MaterialTheme.typography.bodyMedium,
                color = AxialTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onBackToIntake,
                colors = ButtonDefaults.buttonColors(containerColor = AxialPrimary, contentColor = AxialBg)
            ) {
                Text(
                    text = "GOTO INTAKE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        return
    }

    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("forensics_result_page")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // NEUTRALITY INDEX HERO OVERVIEW
        if (!result.isIdle) {
            item {
                val neutralityIndex = 100f - ((result.lexicalAudit.politicalFramingScore + result.lexicalAudit.corporateGuardingScore) / 2f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AxialSurface, shape = RoundedCornerShape(24.dp))
                        .border(BorderStroke(1.dp, AxialBorder), shape = RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "[ PROTOCOL_LEXICAL_AUDIT ]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = AxialPrimary,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "Neutrality Index",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                    color = AxialTextPrimary
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%.1f", neutralityIndex),
                                    fontWeight = FontWeight.Light,
                                    fontSize = 32.sp,
                                    color = AxialTextPrimary,
                                    letterSpacing = (-1).sp
                                )
                                Text(
                                    text = "%",
                                    fontSize = 14.sp,
                                    color = AxialTextSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp, start = 1.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Sentiment Flow Visualization Bars (from design template)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val barPercentages = listOf(0.20f, 0.35f, 0.90f, 0.25f, 0.15f, 0.40f, 1.00f, 0.60f, 0.30f, 0.45f)
                            val barColors = listOf(
                                AxialBorder,
                                AxialBorder,
                                AxialRed.copy(alpha = 0.8f),
                                AxialBorder,
                                AxialBorder,
                                AxialBorder,
                                AxialPrimary.copy(alpha = 0.8f),
                                AxialBorder,
                                AxialBorder,
                                AxialBorder
                            )
                            barPercentages.forEachIndexed { i, fraction ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(fraction)
                                        .background(
                                            color = barColors[i],
                                            shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                                        )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "T_START",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = AxialTextSecondary.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "T_END",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = AxialTextSecondary.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // IDLE PROTOCOL STATUS
        if (result.isIdle) {
            item {
                IdleNotificationLock(reason = result.idleReason)
            }
        }

        // BORDER ENFORCEMENT SUMMARY
        item {
            CardSegmentHeader(
                protocol = "[ PROTOCOL_BORDER_LOCK ]",
                title = "Semantic Border Decryption",
                isOk = true
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AxialSurface, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .border(
                        BorderStroke(1.dp, AxialBorder),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CORE TEXT PASSAGE EXTRACTED RESULT:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = AxialPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = result.borderEnforcement.isolatedCentralText,
                        fontSize = 12.sp,
                        color = AxialTextPrimary,
                        modifier = Modifier
                            .background(AxialBg, RoundedCornerShape(4.dp))
                            .border(1.dp, AxialBorder)
                            .padding(10.dp)
                    )
                    
                    if (result.borderEnforcement.excludedElements.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "OFF-BORDER ELEMENTS FILTERED OUT:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = AxialRed,
                            fontWeight = FontWeight.Bold
                        )
                        result.borderEnforcement.excludedElements.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(AxialBg, shape = RoundedCornerShape(50))
                                )
                                Text(
                                    text = "Filtered Ad/Promo component: \"$item\"",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AxialTextSecondary
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No sponsor banners or ad links encountered in the signal.",
                            fontSize = 10.sp,
                            color = AxialAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // LEXICAL AUDIT SCORINGS
        if (!result.isIdle) {
            item {
                CardSegmentHeader(
                    protocol = "[ PROTOCOL_LEXICAL_AUDIT ]",
                    title = "Narrative Vector Auditing",
                    isOk = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AxialSurface, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .border(
                            BorderStroke(1.dp, AxialBorder),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Scoring bars
                        TelemetryScoreBar(
                            label = "Ideological framing / Partisan spin",
                            score = result.lexicalAudit.politicalFramingScore,
                            analysis = result.lexicalAudit.politicalFramingAnalysis,
                            activeColor = AxialRed
                        )

                        Divider(color = AxialBorder, thickness = 1.dp)

                        TelemetryScoreBar(
                            label = "PR corporate guarding / protective spin",
                            score = result.lexicalAudit.corporateGuardingScore,
                            analysis = result.lexicalAudit.corporateGuardingAnalysis,
                            activeColor = AxialAmber
                        )
                    }
                }
            }

            // Sentiment Chronological Flows Drawing on Canvas
            item {
                CardSegmentHeader(
                    protocol = "[ SENTIMENT FLOWS MAP ]",
                    title = "Chronological Thematic Escalation",
                    isOk = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AxialSurface, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .border(
                            BorderStroke(1.dp, AxialBorder),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "ENGAGEMENT AND RHETORICAL SENSATIONALISM PATHWAY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = AxialPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        SentimentFlowsChart(flows = result.reconstruction.sentimentFlows)
                    }
                }
            }

            // Foreground highlights & linguistic primings
            item {
                CardSegmentHeader(
                    protocol = "[ PRIMING RESOLVER ]",
                    title = "Linguistic Priming & Magic Word Metrics",
                    isOk = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AxialSurface, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .border(
                            BorderStroke(1.dp, AxialBorder),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (result.lexicalAudit.linguisticPriming.isEmpty()) {
                            Text(
                                text = "Zero loaded adjectives or primary indicators indexed.",
                                color = AxialTextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = "ISOLATED LOADED ADJECTIVES REPLACED BY METRIC IMPARTIALS:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = AxialPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            result.lexicalAudit.linguisticPriming.forEach { priming ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AxialBg, RoundedCornerShape(4.dp))
                                        .border(1.dp, AxialBorder)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(0.4f)) {
                                        Text(
                                            text = "Loaded Adjective",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = AxialRed
                                        )
                                        Text(
                                            text = priming.magicWord,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AxialRed
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Neutralized to",
                                        tint = AxialAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(0.55f)
                                            .padding(start = 12.dp)
                                    ) {
                                        Text(
                                            text = "Objective Metric",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = AxialAccent
                                        )
                                        Text(
                                            text = priming.objectiveMetric,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AxialAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FORENSIC EVIDENCE LOG WITH CLICK-TO-TRACING CITATIONS
            item {
                CardSegmentHeader(
                    protocol = "[ EVIDENCE_LEDGER // FORENSIC LOG ]",
                    title = "Interactive Evidence Citations & Fallacies",
                    isOk = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AxialSurface, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .border(
                            BorderStroke(1.dp, AxialBorder),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CLICK AN AUDIT CITATION BELOW TO RESOLVE COGNITIVE FALLACIES:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = AxialPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        EvidenceLogList(evidenceLogs = result.lexicalAudit.evidenceLog)
                    }
                }
            }
        }

        // SYNTHESIS & RECONSTRUCTION
        item {
            CardSegmentHeader(
                protocol = "[ PROTOCOL_SIGNAL_SYNTHESIS ]",
                title = "Neutralized Reconstructive Synthesis",
                isOk = true
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AxialSurface, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .border(
                        BorderStroke(1.dp, AxialBorder),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Original Claim summary
                    Column {
                        Text(
                            text = "ORIGINAL ARGUMENT SUMMARY (CLAIM INTEGRITY PRESERVED):",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = AxialTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = result.reconstruction.originalNarrativeSummary,
                            fontSize = 11.sp,
                            color = AxialTextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AxialBg.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .border(1.dp, AxialBorder)
                                .padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // RECONSTRUCTED IMPARTIAL SIGNAL
                    Column {
                        val clipboard = LocalClipboardManager.current
                        val localContext = LocalContext.current
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NEUTRALIZED SOVEREIGN RECONSTRUCTION:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = AxialAccent,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(result.reconstruction.neutralizedSignal))
                                    Toast.makeText(localContext, "Cloned to operational clipboard.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Reconstructed Signal",
                                    tint = AxialAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = result.reconstruction.neutralizedSignal,
                            fontSize = 13.sp,
                            color = AxialPrimary,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AxialBg, RoundedCornerShape(4.dp))
                                .border(BorderStroke(1.dp, AxialAccent.copy(alpha = 0.4f)), RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun IdleNotificationLock(reason: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("idle_lock_notification"),
        colors = CardDefaults.cardColors(containerColor = AxialSurface),
        border = BorderStroke(1.dp, AxialAmber)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = AxialAmber
                )
                Text(
                    text = "[ SIGNAL_IDLE // NO_AUDIT_REQUIRED ]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AxialAmber
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The engine has completed the Semantic Border audit and determined that this signal contains zero corporate/lobbying interests, institutional protective narratives, or political bias frames.",
                fontSize = 12.sp,
                color = AxialTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Classification Context: \"$reason\"",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = AxialAmber
            )
        }
    }
}

@Composable
fun CardSegmentHeader(
    protocol: String,
    title: String,
    isOk: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(AxialSurfaceElevated, AxialSurface)
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .border(
                BorderStroke(1.dp, AxialBorder),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = protocol,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AxialPrimary,
                letterSpacing = 1.sp
            )
            Text(
                text = title,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AxialTextPrimary
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isOk) AxialAccent else AxialRed, shape = RoundedCornerShape(50))
            )
            Text(
                text = if (isOk) "VALID_ALIGN" else "STANDBY",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = if (isOk) AxialAccent else AxialRed
            )
        }
    }
}

@Composable
fun TelemetryScoreBar(
    label: String,
    score: Int,
    analysis: String,
    activeColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AxialTextPrimary
            )
            Text(
                text = "$score / 100",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = activeColor
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal score status meter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(AxialBg, RoundedCornerShape(4.dp))
                .border(1.dp, AxialBorder, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = score / 100f)
                    .background(activeColor, RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = analysis,
            fontSize = 11.sp,
            color = AxialTextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SentimentFlowsChart(flows: List<SentimentFlow>) {
    if (flows.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(AxialBg, RoundedCornerShape(4.dp))
                .border(1.dp, AxialBorder),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NO RHETORICAL ESCALATION SECTIONS INDEXED",
                color = AxialTextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(AxialBg, RoundedCornerShape(4.dp))
                .border(1.dp, AxialBorder)
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            // Draw visual sentiment flows dynamically on a raw canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val pointCount = flows.size
                
                // Draw dashed grid guidelines
                val gridYLevels = listOf(0.0f, 0.25f, 0.50f, 0.75f, 1.0f)
                gridYLevels.forEach { percentage ->
                    val y = height - (percentage * height)
                    drawLine(
                        color = AxialBorder.copy(alpha = 0.6f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // Calculate nodes
                val xSpacing = if (pointCount > 1) width / (pointCount - 1) else width
                val points = flows.mapIndexed { idx, flow ->
                    val x = idx * xSpacing
                    val levelNormalized = flow.engagementLevel.coerceIn(0, 100) / 100f
                    val y = height - (levelNormalized * height)
                    Offset(x, y)
                }

                // Draw connecting glowing line
                if (points.size > 1) {
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = AxialPrimary,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3f
                        )
                    }
                }

                // Draw circles on nodes and glow highlights
                points.forEachIndexed { i, point ->
                    val flow = flows[i]
                    val colorNode = if (flow.engagementLevel > 60) AxialRed else if (flow.engagementLevel > 30) AxialAmber else AxialAccent
                    
                    // Outer glow
                    drawCircle(
                        color = colorNode.copy(alpha = 0.3f),
                        radius = 8.dp.toPx(),
                        center = point
                    )
                    // Inner dot
                    drawCircle(
                        color = colorNode,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        // Section labels beneath
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            flows.forEach { flow ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1.0f)
                ) {
                    Text(
                        text = flow.narrativeSection.take(8).uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = AxialTextPrimary
                    )
                    Text(
                        text = "${flow.engagementLevel}%",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = if (flow.engagementLevel > 60) AxialRed else AxialPrimary
                    )
                    Text(
                        text = flow.tone,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 7.sp,
                        color = AxialTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun EvidenceLogList(evidenceLogs: List<EvidenceLog>) {
    if (evidenceLogs.isEmpty()) {
        Text(
            text = "Zero citations loaded under active parameters.",
            fontSize = 11.sp,
            color = AxialTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
        return
    }

    var selectedIndex by remember { mutableStateOf(-1) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        evidenceLogs.forEachIndexed { index, log ->
            val isSelected = index == selectedIndex
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedIndex = if (isSelected) -1 else index }
                    .background(
                        if (isSelected) AxialSurfaceElevated else AxialBg,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isSelected) AxialPrimary else AxialBorder
                        ),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(10.dp)
            ) {
                // Citation Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CITATION AUDIT ID #${index + 1}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) AxialPrimary else AxialTextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = AxialRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(2.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.fallacy.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = AxialRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "\"${log.citation}\"",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Light,
                    color = AxialTextPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Expandable detailed diagnostic assertion
                if (isSelected) {
                    Divider(color = AxialBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))
                    Text(
                        text = "AXIAL SANITY REPORT VECTOR:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = AxialPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.assertion,
                        fontSize = 11.sp,
                        color = AxialTextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Click to trace forensic alignment diagnostic...",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AxialPrimary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoricLedgerScreen(
    historyList: List<AxialSignalEntity>,
    onSelectSignal: (AxialSignalEntity) -> Unit,
    onDeleteSignal: (Int) -> Unit,
    onPurgeAll: () -> Unit
) {
    if (historyList.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                tint = AxialSecondary,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "LEDGER CLEAR & EMPTY",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AxialPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No previously aligned media signals have been logged in the secure SQLite database yet.",
                color = AxialTextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ledger_screen")
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYSTEM AUDIT HISTORY",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = AxialPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            
            TextButton(
                onClick = onPurgeAll,
                colors = ButtonDefaults.textButtonColors(contentColor = AxialRed)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PURGE LOG",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(historyList) { signal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSignal(signal) }
                        .testTag("signal_history_card_${signal.id}"),
                    colors = CardDefaults.cardColors(containerColor = AxialSurface),
                    border = BorderStroke(1.dp, AxialBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = signal.title,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AxialPrimary,
                                modifier = Modifier.weight(0.85f)
                            )
                            IconButton(
                                onClick = { onDeleteSignal(signal.id) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .weight(0.15f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete from history_ledger",
                                    tint = AxialTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "Original inputs: ${if (signal.originalText.length > 80) signal.originalText.take(80) + "..." else signal.originalText}",
                            fontSize = 11.sp,
                            color = AxialTextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "TRACED LOG INDEX: #${signal.id}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                color = AxialTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data structures for presets
data class PresetFeed(
    val title: String,
    val typeDescription: String,
    val color: Color,
    val text: String
)

val presetFeeds = listOf(
    PresetFeed(
        title = "Preset 1: Partisan Framing Test",
        typeDescription = "Exaggerated states release containing heavily biased framing & partisan drift",
        color = AxialRed,
        text = "AXIAL NETWORK ALERT: GROUNDBREAKING BREAKTHROUGH! Today, the state's incredible, visionary, and flawless administration proudly smashed all economic records, crushing the lazy, malicious, and gridlocked opposition in a disastrous defeat. Our highly decorated industry lead, who has absolutely zero self-interest, confirmed this is a magnificent miracle of leadership. Settle into the future now before you are left in absolute ruin!"
    ),
    PresetFeed(
        title = "Preset 2: Corporate Guarding",
        typeDescription = "Corporate PR campaign structured to shield companies from regulatory leaks",
        color = AxialAmber,
        text = "SPONSORED UPDATE: NeoTech Industries has released its revolutionary automated guardian firmware. While minor critics complain about tiny, irrelevant data leaks, these claims are completely unfounded slanders from insignificant fringe amateurs. Academic experts supported by our strategic development fund confirm our technology is pristine, completely safe, and must be adopted instantly to prevent global technological collapse. Click the checkout widget below to purchase your license now!"
    ),
    PresetFeed(
        title = "Preset 3: Pure Impartial Text (Null Signal Lock)",
        typeDescription = "Thermodynamics laws detailing science text with zero political/corporate bias",
        color = AxialAccent,
        text = "The second law of thermodynamics states that the total entropy of an isolated system can never decrease over time. It can remain constant in ideal reversible processes. In macroscopic systems, entropy naturally increases, leading to thermal equilibrium where no temperature differences exist."
    )
)
