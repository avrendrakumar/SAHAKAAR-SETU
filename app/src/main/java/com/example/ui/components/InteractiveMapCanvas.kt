package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.ChatMessage
import com.example.data.ai.ChatbotRole
import com.example.data.ai.GeminiChatService
import com.example.data.model.CooperativeEntity
import com.example.data.model.WorkerProfileEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveWorkerMap(
    workers: List<WorkerProfileEntity>,
    cooperatives: List<CooperativeEntity>,
    modifier: Modifier = Modifier,
    onWorkerSelected: ((WorkerProfileEntity) -> Unit)? = null
) {
    var filterAvailable by remember { mutableStateOf(true) }
    var filterBusy by remember { mutableStateOf(true) }
    var filterEmergencyOnly by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    var selectedWorker by remember { mutableStateOf<WorkerProfileEntity?>(null) }

    val filteredWorkers = remember(workers, filterAvailable, filterBusy, filterEmergencyOnly, selectedCategoryFilter) {
        workers.filter { w ->
            val matchesAvail = if (filterAvailable && filterBusy) true
            else if (filterAvailable) (w.isAvailable && !w.isBusy)
            else if (filterBusy) w.isBusy
            else false

            val matchesEmergency = if (filterEmergencyOnly) w.isEmergencyReady else true

            val matchesCategory = if (selectedCategoryFilter == "ALL") true
            else w.primarySkill.equals(selectedCategoryFilter, ignoreCase = true)

            matchesAvail && matchesEmergency && matchesCategory
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Filter Chips Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterAvailable,
                onClick = { filterAvailable = !filterAvailable },
                label = { Text("Available", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElegantLavender,
                    selectedLabelColor = ElegantOnLavender,
                    containerColor = ElegantDarkSurface,
                    labelColor = ElegantTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filterAvailable,
                    borderColor = ElegantDarkBorder,
                    selectedBorderColor = ElegantLavender
                ),
                leadingIcon = {
                    Box(modifier = Modifier.size(8.dp).background(ElegantMintGreen, CircleShape))
                }
            )

            FilterChip(
                selected = filterBusy,
                onClick = { filterBusy = !filterBusy },
                label = { Text("Busy / On Duty", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElegantLavender,
                    selectedLabelColor = ElegantOnLavender,
                    containerColor = ElegantDarkSurface,
                    labelColor = ElegantTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filterBusy,
                    borderColor = ElegantDarkBorder,
                    selectedBorderColor = ElegantLavender
                ),
                leadingIcon = {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFF59E0B), CircleShape))
                }
            )

            FilterChip(
                selected = filterEmergencyOnly,
                onClick = { filterEmergencyOnly = !filterEmergencyOnly },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElegantEmergencyRed,
                    selectedLabelColor = Color.White,
                    containerColor = ElegantDarkSurface,
                    labelColor = ElegantTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filterEmergencyOnly,
                    borderColor = ElegantDarkBorder,
                    selectedBorderColor = ElegantEmergencyRed
                ),
                label = { Text("🚨 Emergency Only", fontSize = 11.sp) }
            )

            listOf("ALL", "Electrical", "Plumbing", "Carpentry", "Masonry", "AC Repair").forEach { cat ->
                FilterChip(
                    selected = selectedCategoryFilter == cat,
                    onClick = { selectedCategoryFilter = cat },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElegantLavender,
                        selectedLabelColor = ElegantOnLavender,
                        containerColor = ElegantDarkSurface,
                        labelColor = ElegantTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategoryFilter == cat,
                        borderColor = ElegantDarkBorder,
                        selectedBorderColor = ElegantLavender
                    ),
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        // Map Viewport (Interactive stylized vector canvas)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF14161A))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(filteredWorkers) {
                        detectTapGestures { offset ->
                            // Hit test pins: find closest worker
                            val w = size.width
                            val h = size.height
                            var closest: WorkerProfileEntity? = null
                            var minD = 30f * 30f

                            filteredWorkers.forEach { worker ->
                                val px = ((worker.lng - 85.0) / 0.5 * w).toFloat().coerceIn(40f, w - 40f)
                                val py = ((26.3 - worker.lat) / 1.5 * h).toFloat().coerceIn(40f, h - 40f)
                                val d2 = (offset.x - px) * (offset.x - px) + (offset.y - py) * (offset.y - py)
                                if (d2 < minD) {
                                    minD = d2
                                    closest = worker
                                }
                            }
                            selectedWorker = closest
                            if (closest != null && onWorkerSelected != null) {
                                onWorkerSelected(closest!!)
                            }
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                // Draw river / canal (symbolic Ganga through Patna & Bihar)
                val riverPath = Path().apply {
                    moveTo(0f, canvasH * 0.35f)
                    cubicTo(
                        canvasW * 0.3f, canvasH * 0.25f,
                        canvasW * 0.7f, canvasH * 0.45f,
                        canvasW, canvasH * 0.30f
                    )
                    lineTo(canvasW, canvasH * 0.38f)
                    cubicTo(
                        canvasW * 0.7f, canvasH * 0.53f,
                        canvasW * 0.3f, canvasH * 0.33f,
                        0f, canvasH * 0.43f
                    )
                    close()
                }
                drawPath(riverPath, color = Color(0xFF1B314B))

                // Draw road network grids
                for (x in 60..canvasW.toInt() step 90) {
                    drawLine(
                        color = Color(0xFF23272F),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), canvasH),
                        strokeWidth = 2f
                    )
                }
                for (y in 50..canvasH.toInt() step 70) {
                    drawLine(
                        color = Color(0xFF23272F),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(canvasW, y.toFloat()),
                        strokeWidth = 2f
                    )
                }

                // Draw Demand Zones (soft heat circles)
                drawCircle(
                    color = Color(0x33DC2626), // Zone 1 High Demand
                    radius = 55f,
                    center = Offset(canvasW * 0.38f, canvasH * 0.58f)
                )
                drawCircle(
                    color = Color(0x22F59E0B), // Zone 2 Medium Demand
                    radius = 45f,
                    center = Offset(canvasW * 0.65f, canvasH * 0.40f)
                )

                // Draw Cooperative Headquarters Hubs (Purple Diamonds)
                cooperatives.take(3).forEachIndexed { index, _ ->
                    val cx = canvasW * (0.25f + index * 0.28f)
                    val cy = canvasH * (0.45f + (index % 2) * 0.20f)
                    drawCircle(color = ElegantLavender, radius = 12f, center = Offset(cx, cy))
                    drawCircle(color = ElegantDarkBg, radius = 5f, center = Offset(cx, cy))
                }

                // Draw Worker Pins
                filteredWorkers.forEach { worker ->
                    val px = ((worker.lng - 85.0) / 0.5 * canvasW).toFloat().coerceIn(30f, canvasW - 30f)
                    val py = ((26.3 - worker.lat) / 1.5 * canvasH).toFloat().coerceIn(30f, canvasH - 30f)

                    val pinColor = when {
                        !worker.isAvailable -> Color(0xFF64748B)
                        worker.isBusy -> Color(0xFFF59E0B)
                        worker.isEmergencyReady -> Color(0xFFDC2626)
                        else -> ElegantMintGreen
                    }

                    // Outer pulse ring for emergency
                    if (worker.isEmergencyReady && worker.isAvailable) {
                        drawCircle(
                            color = Color(0x44DC2626),
                            radius = 16f,
                            center = Offset(px, py)
                        )
                    }

                    // Pin head
                    drawCircle(color = pinColor, radius = 9f, center = Offset(px, py))
                    drawCircle(color = Color.White, radius = 3.5f, center = Offset(px, py))

                    // Selected glow
                    if (selectedWorker?.id == worker.id) {
                        drawCircle(
                            color = ElegantLavender,
                            radius = 15f,
                            center = Offset(px, py),
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }

            // Legend Overlay (Top right corner)
            Surface(
                color = ElegantDarkSurface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("LIVE FEED (${filteredWorkers.size} WORKERS)", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = ElegantTextSecondary)
                    Spacer(modifier = Modifier.height(3.dp))
                    MapLegendItem(ElegantMintGreen, "Available")
                    MapLegendItem(Color(0xFFF59E0B), "Busy")
                    MapLegendItem(Color(0xFFDC2626), "Emergency")
                    MapLegendItem(ElegantLavender, "Co-op Hub")
                }
            }

            // Zoom indicator badge
            Surface(
                color = ElegantDarkSurfaceVariant.copy(alpha = 0.90f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Bihar Region • Patna / Muz / Gaya",
                    color = ElegantTextWhite,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Selected Worker Quick Info Card
        selectedWorker?.let { worker ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ElegantTextWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("(${worker.primarySkill})", color = ElegantLavender, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = "${worker.zone} • ${worker.experienceYears} Yrs Exp • ★ ${worker.customerRating}",
                            fontSize = 11.sp,
                            color = ElegantTextSecondary
                        )
                    }

                    AvailabilityIndicator(isAvailable = worker.isAvailable, isBusy = worker.isBusy)
                }
            }
        }

        // Google Maps Grounding Depot & Supply Finder Card
        Spacer(modifier = Modifier.height(10.dp))
        GoogleMapsDepotFinderCard()
    }
}

@Composable
fun GoogleMapsDepotFinderCard() {
    val coroutineScope = rememberCoroutineScope()
    var queryText by remember { mutableStateOf("Hardware and tool stores near Bailey Road, Patna") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<ChatMessage?>(null) }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = ElegantLavenderContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = ElegantLavender,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hardware & Tool Depots",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ElegantTextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = ElegantLavenderContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Google Maps Data",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantLavender,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Live grounding via gemini-3.5-flash",
                            fontSize = 11.sp,
                            color = ElegantTextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ElegantTextSecondary
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = { Text("e.g. Electrical stores near me...", fontSize = 12.sp, color = ElegantTextMuted) },
                        trailingIcon = {
                            VoiceInputIconButton(
                                onTranscribedText = { txt -> queryText = txt },
                                buttonTag = "maps_grounding_voice_btn"
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantTextWhite,
                            unfocusedTextColor = ElegantTextWhite,
                            focusedBorderColor = ElegantLavender,
                            unfocusedBorderColor = ElegantDarkBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (queryText.isNotBlank()) {
                                isSearching = true
                                coroutineScope.launch {
                                    val res = GeminiChatService.sendMessage(
                                        history = emptyList(),
                                        userPrompt = "Locate hardware tools and repair supplies: $queryText",
                                        role = ChatbotRole.GENERAL_ASSISTANT,
                                        selectedModel = "gemini-3.5-flash",
                                        useMapsGrounding = true
                                    )
                                    searchResult = res
                                    isSearching = false
                                }
                            }
                        },
                        enabled = queryText.isNotBlank() && !isSearching,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender)
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ElegantOnLavender, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                searchResult?.let { res ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = ElegantDarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = res.text,
                                fontSize = 12.sp,
                                color = ElegantTextWhite,
                                lineHeight = 16.sp
                            )
                            if (res.groundingSources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Google Maps Grounded Locations:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = ElegantLavender
                                )
                                res.groundingSources.forEach { source ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = ElegantLavender, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = source.title, fontSize = 10.sp, color = ElegantTextWhite)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, fontSize = 9.sp, color = ElegantTextWhite)
    }
}
