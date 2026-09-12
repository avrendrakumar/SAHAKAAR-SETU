package com.example.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.ChatMessage
import com.example.data.ai.ChatbotRole
import com.example.data.ai.GeminiChatService
import com.example.data.ai.GroundingType
import com.example.data.ai.VoiceRecognitionHelper
import com.example.ui.components.SahayakVoiceToTextSheet
import com.example.ui.components.VoiceInputIconButton
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiChatScreen(
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val voiceHelper = remember { VoiceRecognitionHelper(context) }
    val isSpeaking by voiceHelper.isSpeaking.collectAsState()
    val currentlySpeakingId by voiceHelper.currentlySpeakingId.collectAsState()

    var selectedRole by remember { mutableStateOf(ChatbotRole.GENERAL_ASSISTANT) }
    var selectedModel by remember { mutableStateOf(ChatbotRole.GENERAL_ASSISTANT.defaultModel) }
    var useMapsGrounding by remember { mutableStateOf(false) }
    var useSearchGrounding by remember { mutableStateOf(false) }

    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showRolePicker by remember { mutableStateOf(false) }
    var showVoiceSheet by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.release()
        }
    }

    // Multi-turn conversation history
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Namaste! I am **Sahakaar Sahayak** (सहकार सहायक), your dedicated cooperative assistant. Ask me anything about home repairs, trade diagnosis, artisan wages, cooperative welfare schemes, or labor rights.\n\n✨ You can ground my answers with **Google Maps data** or **Google Search data**, or speak directly with the microphone using **gemini-3.5-transcribe**.",
                isUser = false,
                modelUsed = "gemini-3.5-flash",
                roleTitle = "Sahakaar Sahayak"
            )
        )
    }

    // Auto-scroll to bottom when messages update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun submitMessage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || isSending) return

        val userMsg = ChatMessage(text = trimmed, isUser = true)
        messages.add(userMsg)
        inputText = ""
        isSending = true

        coroutineScope.launch {
            val reply = GeminiChatService.sendMessage(
                history = messages.filter { !it.isError },
                userPrompt = trimmed,
                role = selectedRole,
                selectedModel = selectedModel,
                useMapsGrounding = useMapsGrounding,
                useSearchGrounding = useSearchGrounding
            )
            messages.add(reply)
            isSending = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBg)
    ) {
        // --- Top Bar ---
        Surface(
            color = ElegantDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onNavigateBack != null) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.size(34.dp).testTag("chat_back_button")
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = ElegantTextWhite
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Surface(
                            shape = CircleShape,
                            color = ElegantLavenderContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = ElegantLavender,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Sahakaar Sahayak",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ElegantTextWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = ElegantDarkSurfaceVariant,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                                ) {
                                    Text(
                                        text = if (useMapsGrounding || useSearchGrounding) "gemini-3.5-flash" else selectedModel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ElegantLavender,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = selectedRole.title,
                                fontSize = 11.sp,
                                color = ElegantTextSecondary
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { showVoiceSheet = true },
                            modifier = Modifier.size(36.dp).testTag("top_bar_voice_button")
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Voice Assistant - Speak to Sahayak",
                                tint = ElegantSaffron
                            )
                        }
                        IconButton(
                            onClick = { showRolePicker = true },
                            modifier = Modifier.size(36.dp).testTag("select_role_button")
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Select AI Role & System Instruction",
                                tint = ElegantLavender
                            )
                        }
                        IconButton(
                            onClick = {
                                messages.clear()
                                messages.add(
                                    ChatMessage(
                                        text = "Conversation reset. How can I assist you now?",
                                        isUser = false,
                                        modelUsed = selectedModel,
                                        roleTitle = selectedRole.title
                                    )
                                )
                            },
                            modifier = Modifier.size(36.dp).testTag("clear_chat_button")
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Clear Chat",
                                tint = ElegantTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Grounding & Model Selection Chips ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google Maps Grounding Toggle
                    FilterChip(
                        selected = useMapsGrounding,
                        onClick = {
                            useMapsGrounding = !useMapsGrounding
                            if (useMapsGrounding) selectedModel = "gemini-3.5-flash"
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (useMapsGrounding) ElegantOnLavender else ElegantTextSecondary
                            )
                        },
                        label = { Text("Google Maps", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElegantLavender,
                            selectedLabelColor = ElegantOnLavender,
                            containerColor = ElegantDarkSurfaceVariant,
                            labelColor = ElegantTextSecondary
                        ),
                        modifier = Modifier.testTag("toggle_maps_grounding")
                    )

                    // Google Search Grounding Toggle
                    FilterChip(
                        selected = useSearchGrounding,
                        onClick = {
                            useSearchGrounding = !useSearchGrounding
                            if (useSearchGrounding) selectedModel = "gemini-3.5-flash"
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (useSearchGrounding) ElegantOnLavender else ElegantTextSecondary
                            )
                        },
                        label = { Text("Google Search", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElegantLavender,
                            selectedLabelColor = ElegantOnLavender,
                            containerColor = ElegantDarkSurfaceVariant,
                            labelColor = ElegantTextSecondary
                        ),
                        modifier = Modifier.testTag("toggle_search_grounding")
                    )

                    // Model Selector Dropdown Pill
                    Surface(
                        color = ElegantDarkSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier
                            .clickable {
                                // Cycle models: 3.5-flash -> 3.1-pro-preview -> 3.1-flash-lite
                                selectedModel = when (selectedModel) {
                                    "gemini-3.5-flash" -> "gemini-3.1-pro-preview"
                                    "gemini-3.1-pro-preview" -> "gemini-3.1-flash-lite"
                                    else -> "gemini-3.5-flash"
                                }
                            }
                            .testTag("cycle_model_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Memory,
                                contentDescription = null,
                                tint = ElegantLavender,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (selectedModel) {
                                    "gemini-3.1-pro-preview" -> "3.1 Pro (Complex)"
                                    "gemini-3.1-flash-lite" -> "3.1 Lite (Fast)"
                                    else -> "3.5 Flash (General)"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextWhite
                            )
                        }
                    }
                }
            }
        }

        // --- Role Picker Bottom Sheet / Dialog ---
        if (showRolePicker) {
            RolePickerDialog(
                currentRole = selectedRole,
                onRoleSelected = { role ->
                    selectedRole = role
                    selectedModel = role.defaultModel
                    showRolePicker = false
                },
                onDismiss = { showRolePicker = false }
            )
        }

        // --- Scrollable Message Thread ---
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_messages_list")
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onCopy = { clipboardManager.setText(AnnotatedString(msg.text)) },
                    onPromptClick = { prompt -> submitMessage(prompt) },
                    onSpeak = { text, id -> voiceHelper.speakText(text, id) },
                    isSpeaking = isSpeaking,
                    speakingId = currentlySpeakingId
                )
            }

            if (isSending) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(ElegantDarkSurface, RoundedCornerShape(16.dp))
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = ElegantLavender,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (useMapsGrounding) "Querying Google Maps via gemini-3.5-flash..."
                            else if (useSearchGrounding) "Searching Google live data via gemini-3.5-flash..."
                            else "Thinking with ${selectedModel}...",
                            fontSize = 12.sp,
                            color = ElegantTextSecondary
                        )
                    }
                }
            }
        }

        // --- Quick Suggestion Prompts ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Voice action pill
            item {
                Surface(
                    color = ElegantLavenderContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantLavender),
                    modifier = Modifier
                        .clickable { showVoiceSheet = true }
                        .testTag("voice_quick_action_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            tint = ElegantLavender,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "बोलकर पूछें (Voice Input)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantLavender
                        )
                    }
                }
            }

            val suggestions = if (useMapsGrounding) {
                listOf(
                    "Find nearest hardware stores in Patna",
                    "Cooperative tool rental banks near Bailey Road",
                    "Emergency plumbing supply depot near me"
                )
            } else if (useSearchGrounding) {
                listOf(
                    "Current 2026 minimum wage for skilled masons",
                    "PM Vishwakarma toolkit ₹15,000 grant eligibility",
                    "Wholesale cement 53 grade & TMT steel prices"
                )
            } else {
                when (selectedRole) {
                    ChatbotRole.MASTER_CRAFTSMAN -> listOf(
                        "How to diagnose an MCB tripping repeatedly?",
                        "What is standard mortar ratio for 9-inch brick wall?",
                        "Fixing low water pressure in bathroom CPVC pipes"
                    )
                    ChatbotRole.COOPERATIVE_LEGAL_COUNSEL -> listOf(
                        "How are labor dispute settlements handled?",
                        "What is the statutory floor wage in Bihar?",
                        "Worker rights under Cooperative Societies bylaws"
                    )
                    ChatbotRole.WELFARE_SCHEME_EXPERT -> listOf(
                        "How do I claim PM Vishwakarma ₹15,000 toolkit?",
                        "e-Shram card accidental insurance benefits",
                        "BOCW board artisan scholarship for children"
                    )
                    ChatbotRole.INSTANT_QUOTE_ESTIMATOR -> listOf(
                        "Estimate cost: repaint 2BHK flat 900 sq ft",
                        "Daily wage quote for 2 skilled electricians",
                        "Labor charge for installing 500L water tank"
                    )
                    else -> listOf(
                        "Find skilled plumbers near Patna",
                        "Explain Sahakaar Setu cooperative welfare fund",
                        "What are minimum wage floors for artisans?"
                    )
                }
            }

            items(suggestions) { prompt ->
                Surface(
                    color = ElegantDarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier
                        .clickable { submitMessage(prompt) }
                        .testTag("suggestion_chip")
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        color = ElegantLavender,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // --- Bottom Message Input Bar with Voice Transcription ---
        Surface(
            color = ElegantDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Input Button - Launches dedicated Voice-to-Text modal sheet
                IconButton(
                    onClick = { showVoiceSheet = true },
                    modifier = Modifier.testTag("chat_voice_input_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ElegantLavenderContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Voice Assistant (बोलकर पूछें)",
                                tint = ElegantLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Input Text Field
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (useMapsGrounding) "Ask about places, hardware depots..."
                            else if (useSearchGrounding) "Ask about live wage rates & govt schemes..."
                            else "Type message or tap mic to transcribe...",
                            fontSize = 13.sp,
                            color = ElegantTextSecondary
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = ElegantTextWhite,
                        unfocusedTextColor = ElegantTextWhite,
                        cursorColor = ElegantLavender,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                // Send Button
                IconButton(
                    onClick = { submitMessage(inputText) },
                    enabled = inputText.isNotBlank() && !isSending,
                    modifier = Modifier.testTag("chat_send_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (inputText.isNotBlank() && !isSending) ElegantLavender else ElegantDarkSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank() && !isSending) ElegantOnLavender else ElegantTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Voice-to-Text Modal Sheet
        if (showVoiceSheet) {
            SahayakVoiceToTextSheet(
                onDismiss = { showVoiceSheet = false },
                onSendMessage = { spoken ->
                    submitMessage(spoken)
                },
                onInsertText = { spoken ->
                    inputText = if (inputText.isBlank()) spoken else "$inputText $spoken"
                }
            )
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onCopy: () -> Unit,
    onPromptClick: (String) -> Unit,
    onSpeak: ((String, String) -> Unit)? = null,
    isSpeaking: Boolean = false,
    speakingId: String? = null
) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) ElegantLavender else ElegantDarkSurface
    val textColor = if (isUser) ElegantOnLavender else ElegantTextWhite
    val borderColor = if (isUser) Color.Transparent else ElegantDarkBorder

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Author Tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp, end = 4.dp)
        ) {
            Text(
                text = if (isUser) "You" else (message.roleTitle ?: "Sahakaar AI"),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = ElegantTextSecondary
            )
            if (!isUser && message.modelUsed != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = ElegantLavenderContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = message.modelUsed,
                        fontSize = 9.sp,
                        color = ElegantLavender,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
            Text(
                text = timeStr,
                fontSize = 9.sp,
                color = ElegantTextSecondary.copy(alpha = 0.7f)
            )
        }

        // Message Bubble
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 16.dp
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .widthIn(max = 320.dp)
                .testTag(if (isUser) "user_chat_bubble" else "ai_chat_bubble")
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = textColor,
                    lineHeight = 19.sp
                )

                // Display Grounding Sources (Google Maps / Google Search citations)
                if (message.groundingSources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = ElegantDarkBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Verified Grounding Sources:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = ElegantLavender
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        message.groundingSources.forEach { source ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElegantDarkSurfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (source.type == GroundingType.MAPS) Icons.Default.Place else Icons.Default.Search,
                                    contentDescription = null,
                                    tint = ElegantLavender,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = source.title,
                                    fontSize = 10.sp,
                                    color = ElegantTextWhite,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Copy & Audio Readout Action for AI answers
                if (!isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onSpeak != null) {
                            IconButton(
                                onClick = { onSpeak(message.text, message.id) },
                                modifier = Modifier.size(24.dp).testTag("listen_message_button_${message.id}")
                            ) {
                                Icon(
                                    if (isSpeaking && speakingId == message.id) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = if (isSpeaking && speakingId == message.id) "Stop listening" else "Listen to response",
                                    tint = if (isSpeaking && speakingId == message.id) ElegantSaffron else ElegantLavender,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = ElegantTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RolePickerDialog(
    currentRole: ChatbotRole,
    onRoleSelected: (ChatbotRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Select AI System Instruction Role",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ElegantTextWhite
                )
                Text(
                    text = "Assigns specialized knowledge & role-based system instructions",
                    fontSize = 11.sp,
                    color = ElegantTextSecondary
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ChatbotRole.values().forEach { role ->
                    val isSelected = currentRole == role
                    Surface(
                        color = if (isSelected) ElegantLavenderContainer else ElegantDarkSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) ElegantLavender else ElegantDarkBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRoleSelected(role) }
                            .testTag("role_item_${role.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onRoleSelected(role) },
                                colors = RadioButtonDefaults.colors(selectedColor = ElegantLavender)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = role.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) ElegantLavender else ElegantTextWhite
                                )
                                Text(
                                    text = role.subtitle,
                                    fontSize = 11.sp,
                                    color = ElegantTextSecondary
                                )
                                Text(
                                    text = "Default Model: ${role.defaultModel}",
                                    fontSize = 10.sp,
                                    color = ElegantLavender.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = ElegantLavender)
            }
        },
        containerColor = ElegantDarkSurface
    )
}
