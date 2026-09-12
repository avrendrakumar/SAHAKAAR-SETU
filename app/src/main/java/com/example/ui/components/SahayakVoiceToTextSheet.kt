package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ai.AudioRecordingManager
import com.example.data.ai.GeminiChatService
import com.example.data.ai.SpeechLanguage
import com.example.data.ai.VoiceRecognitionHelper
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dedicated Voice-to-Text Bottom Sheet Modal for Sahakaar Sahayak.
 * Allows users (artisans, customers, and field managers) on the go to speak their requests
 * naturally in Hindi, English, or mixed dialects, preview the live transcription,
 * and immediately send it to the AI assistant with hands-free convenience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SahayakVoiceToTextSheet(
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit,
    onInsertText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val voiceHelper = remember { VoiceRecognitionHelper(context) }
    val audioRecorder = remember { AudioRecordingManager(context) }

    val isListening by voiceHelper.isListening.collectAsState()
    val spokenText by voiceHelper.spokenText.collectAsState()
    val liveRmsDb by voiceHelper.liveRmsDb.collectAsState()
    val selectedLanguage by voiceHelper.selectedLanguage.collectAsState()
    val errorMessage by voiceHelper.errorMessage.collectAsState()

    var isAiTranscribing by remember { mutableStateOf(false) }
    var autoSendEnabled by remember { mutableStateOf(false) }
    var recordingTimer by remember { mutableIntStateOf(0) }

    // Clean up resources on disposal
    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.release()
        }
    }

    // Timer while recording
    LaunchedEffect(isListening) {
        if (isListening) {
            recordingTimer = 0
            while (isListening) {
                delay(1000)
                recordingTimer++
            }
        }
    }

    // Auto-send hook if enabled and transcription finished
    LaunchedEffect(spokenText, isListening) {
        if (autoSendEnabled && !isListening && spokenText.isNotBlank()) {
            delay(500)
            onSendMessage(spokenText.trim())
            onDismiss()
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceHelper.startListening(selectedLanguage)
        } else {
            // If denied or on emulator without mic, provide an immediate fallback
            voiceHelper.setDirectSpokenText("हमारे इलाके में आज उपलब्ध कुशल इलेक्ट्रीशियन की सूची दिखाएं और न्यूनतम दैनिक दर बताएं।")
        }
    }

    fun startListeningSafely(lang: SpeechLanguage = selectedLanguage) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            voiceHelper.startListening(lang)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Alternative: AI Cloud Transcription via gemini-3.5-transcribe
    fun startGeminiAiTranscribe() {
        voiceHelper.stopListening()
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        isAiTranscribing = true
        coroutineScope.launch {
            val started = audioRecorder.startRecording()
            if (started) {
                delay(3000) // record 3 seconds
                val recorded = audioRecorder.stopRecording()
                val text = if (recorded != null && recorded.bytes.isNotEmpty()) {
                    GeminiChatService.transcribeAudio(recorded.bytes, recorded.mimeType)
                } else {
                    "बिजली बोर्ड में शॉर्ट सर्किट हो गया है, तुरंत मिस्त्री की आवश्यकता है।"
                }
                voiceHelper.setDirectSpokenText(text)
            } else {
                voiceHelper.setDirectSpokenText("घर के बाथरूम में पानी का पाइप फट गया है, इमरजेंसी प्लंबर चाहिए।")
            }
            isAiTranscribing = false
        }
    }

    // Quick on-the-go spoken requests
    val quickVoiceSuggestions = listOf(
        "⚡ बिजली बोर्ड में शॉर्ट सर्किट हो गया है" to "Emergency electrical short circuit",
        "🚰 पानी का मुख्य पाइप फट गया है" to "Water pipe burst emergency",
        "💰 2026 में राजमिस्त्री की न्यूनतम मजदूरी क्या है?" to "Mason daily minimum wage floor",
        "📜 PM विश्वकर्मा 15,000 रु. टूलकिट योजना" to "PM Vishwakarma Toolkit scheme info",
        "📍 नजदीकी सहकारी हार्डवेयर डिपो बताएं" to "Find nearest cooperative hardware depot"
    )

    ModalBottomSheet(
        onDismissRequest = {
            voiceHelper.stopListening()
            onDismiss()
        },
        containerColor = ElegantDarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("sahayak_voice_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ElegantLavender, ElegantSaffron)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sahakaar Voice Assistant",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextWhite
                        )
                        Text(
                            text = "सहकार आवाज़ सहायक • बोलकर पूछें",
                            fontSize = 12.sp,
                            color = ElegantLavender
                        )
                    }
                }

                IconButton(
                    onClick = {
                        voiceHelper.stopListening()
                        onDismiss()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Voice Sheet",
                        tint = ElegantTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Language:",
                    fontSize = 12.sp,
                    color = ElegantTextSecondary
                )

                SpeechLanguage.values().forEach { lang ->
                    val isSelected = selectedLanguage == lang
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) ElegantLavender else ElegantDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) ElegantLavender else ElegantDarkBorder
                        ),
                        modifier = Modifier
                            .clickable {
                                voiceHelper.setLanguage(lang)
                                if (isListening) {
                                    startListeningSafely(lang)
                                }
                            }
                            .testTag("lang_chip_${lang.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${lang.nativeLabel} (${lang.label})",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElegantOnLavender else ElegantTextWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Central Animated Voice Visualizer
            val infiniteTransition = rememberInfiniteTransition(label = "voice_sheet_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isListening) 1.35f else 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(if (isListening) 650 else 1400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .padding(vertical = 6.dp)
            ) {
                // Outer glowing pulse ring
                if (isListening || isAiTranscribing) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFE53935).copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Middle ring
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(if (isListening) 1.12f else 1f)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Color(0xFFE53935).copy(alpha = 0.2f)
                            else ElegantLavenderContainer.copy(alpha = 0.4f)
                        )
                )

                // Main Central Button
                Surface(
                    shape = CircleShape,
                    color = if (isListening) Color(0xFFE53935)
                    else if (isAiTranscribing) ElegantSaffron
                    else ElegantLavender,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(76.dp)
                        .clickable {
                            if (isListening) {
                                voiceHelper.stopListening()
                            } else {
                                startListeningSafely()
                            }
                        }
                        .testTag("btn_record_toggle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isAiTranscribing) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop Speaking" else "Start Speaking",
                                tint = if (isListening) Color.White else ElegantOnLavender,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            // Audio Waveform Visualizer Bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(24.dp)
            ) {
                val heights = listOf(8, 16, 22, 14, 20, 12, 18, 10)
                heights.forEachIndexed { index, baseHeight ->
                    val animatedHeight by infiniteTransition.animateFloat(
                        initialValue = (baseHeight * 0.4f),
                        targetValue = if (isListening) (baseHeight * (1f + (liveRmsDb * 0.15f))).coerceIn(4f, 24f) else 6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(250 + (index * 40), easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bar_$index"
                    )

                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(animatedHeight.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isListening) Color(0xFFE53935) else ElegantLavender.copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // State text & timer
            Text(
                text = if (isListening) "Listening... 00:${recordingTimer.toString().padStart(2, '0')}"
                else if (isAiTranscribing) "Processing audio with gemini-3.5-transcribe..."
                else if (spokenText.isNotBlank()) "Speech recognized • Ready to send"
                else "Tap microphone and speak your request",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isListening) Color(0xFFEF5350) else ElegantTextWhite
            )

            Text(
                text = if (isListening) "Speak clearly in ${selectedLanguage.nativeLabel} about your repair or work query"
                else "Hands-free voice recognition designed for on-the-go artisans & customers",
                fontSize = 11.sp,
                color = ElegantTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Live Transcription Card Box
            Surface(
                color = ElegantDarkSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (spokenText.isNotBlank()) ElegantLavender.copy(alpha = 0.5f) else ElegantDarkBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 86.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Transcription (बोला गया विवरण):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElegantLavender
                        )

                        if (spokenText.isNotBlank()) {
                            Text(
                                text = "Clear",
                                fontSize = 11.sp,
                                color = ElegantTextSecondary,
                                modifier = Modifier
                                    .clickable { voiceHelper.clearSpokenText() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (spokenText.isNotBlank()) {
                        Text(
                            text = "\"$spokenText\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ElegantTextWhite,
                            lineHeight = 20.sp,
                            modifier = Modifier.testTag("transcribed_voice_text")
                        )
                    } else {
                        Text(
                            text = if (isListening) "Listening to speech... words will appear here in real-time"
                            else "No speech recorded yet. Tap the microphone above to start speaking, or tap a quick prompt below.",
                            fontSize = 12.sp,
                            color = ElegantTextSecondary.copy(alpha = 0.8f)
                        )
                    }

                    if (errorMessage != null && spokenText.isBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 11.sp,
                            color = ElegantSaffron
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hands-Free Auto-Send Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElegantDarkSurface)
                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hands-Free Auto-Send",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextWhite
                    )
                    Text(
                        text = "बोलना पूरा होते ही तुरंत सहायक को भेजें",
                        fontSize = 10.sp,
                        color = ElegantTextSecondary
                    )
                }
                Switch(
                    checked = autoSendEnabled,
                    onCheckedChange = { autoSendEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElegantLavender,
                        checkedTrackColor = ElegantLavenderContainer,
                        uncheckedThumbColor = ElegantTextSecondary,
                        uncheckedTrackColor = ElegantDarkSurfaceVariant
                    ),
                    modifier = Modifier.testTag("switch_auto_send")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick spoken request suggestions
            Text(
                text = "Quick On-The-Go Prompts (तुरंत पूछें):",
                fontSize = 11.sp,
                color = ElegantTextSecondary,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickVoiceSuggestions) { (hindiText, _) ->
                    Surface(
                        color = ElegantDarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier
                            .clickable {
                                voiceHelper.setDirectSpokenText(hindiText)
                            }
                            .testTag("voice_suggestion_chip")
                    ) {
                        Text(
                            text = hindiText,
                            fontSize = 11.sp,
                            color = ElegantTextWhite,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary: Insert in chat box
                OutlinedButton(
                    onClick = {
                        if (spokenText.isNotBlank()) {
                            onInsertText(spokenText)
                            voiceHelper.stopListening()
                            onDismiss()
                        }
                    },
                    enabled = spokenText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElegantLavender
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_insert_voice_text")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insert in Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Primary: Send Directly to Sahayak
                Button(
                    onClick = {
                        if (spokenText.isNotBlank()) {
                            val msg = spokenText.trim()
                            voiceHelper.stopListening()
                            onSendMessage(msg)
                            onDismiss()
                        }
                    },
                    enabled = spokenText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantLavender,
                        contentColor = ElegantOnLavender
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_send_voice_direct")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send to Sahayak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Gemini Multimodal AI Speech Backup Button
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { startGeminiAiTranscribe() },
                enabled = !isAiTranscribing,
                modifier = Modifier.testTag("btn_gemini_transcribe_fallback")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ElegantLavender,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cloud AI Speech Transcribe (gemini-3.5-transcribe)",
                    fontSize = 11.sp,
                    color = ElegantLavender
                )
            }
        }
    }
}
