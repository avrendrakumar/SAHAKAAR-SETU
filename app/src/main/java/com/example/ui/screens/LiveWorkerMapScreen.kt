package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CooperativeEntity
import com.example.data.model.WorkerProfileEntity
import com.example.ui.components.LanguageManager
import com.example.ui.theme.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveWorkerMapScreen(
    workers: List<WorkerProfileEntity>,
    cooperatives: List<CooperativeEntity>,
    onWorkerSelected: (WorkerProfileEntity) -> Unit
) {
    var filterAvailable by remember { mutableStateOf(true) }
    var filterBusy by remember { mutableStateOf(true) }
    var filterEmergency by remember { mutableStateOf(false) }
    var selectedSkillFilter by remember { mutableStateOf("All") }

    var selectedWorkerOnMap by remember { mutableStateOf<WorkerProfileEntity?>(null) }
    val isHi = LanguageManager.isHindi

    // Map Pan & Zoom states
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Reference center in Bihar (Patna 25.6093, 85.1376)
    val refLat = 25.6100
    val refLon = 85.1400

    val filteredWorkers = remember(workers, filterAvailable, filterBusy, filterEmergency, selectedSkillFilter) {
        workers.filter { w ->
            val statusMatch = (filterAvailable && w.isOnline && !w.isBusy) ||
                    (filterBusy && w.isBusy) ||
                    (!filterAvailable && !filterBusy && true)
            val emergMatch = if (filterEmergency) w.isEmergencyReady else true
            val skillMatch = if (selectedSkillFilter == "All") true else w.primarySkill.equals(selectedSkillFilter, ignoreCase = true)
            statusMatch && emergMatch && skillMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Filter Bar
        Surface(
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SahakaarBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHi) "लाइव श्रमिक एवं मांग मानचित्र" else "LIVE WORKER & DEMAND MAP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                    }
                    Text(
                        text = "${filteredWorkers.size} ${if (isHi) "श्रमिक सक्रिय" else "Workers Visible"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SahakaarTeal
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Checkbox / Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filterAvailable,
                        onClick = { filterAvailable = !filterAvailable },
                        label = { Text(if (isHi) "उपलब्ध (${workers.count { it.isOnline && !it.isBusy }})" else "Available (${workers.count { it.isOnline && !it.isBusy }})", fontSize = 11.5.sp) },
                        leadingIcon = { Box(modifier = Modifier.size(8.dp).background(SahakaarGreen, CircleShape)) }
                    )

                    FilterChip(
                        selected = filterBusy,
                        onClick = { filterBusy = !filterBusy },
                        label = { Text(if (isHi) "व्यस्त (${workers.count { it.isBusy }})" else "Busy (${workers.count { it.isBusy }})", fontSize = 11.5.sp) },
                        leadingIcon = { Box(modifier = Modifier.size(8.dp).background(SahakaarAmber, CircleShape)) }
                    )

                    FilterChip(
                        selected = filterEmergency,
                        onClick = { filterEmergency = !filterEmergency },
                        label = { Text(if (isHi) "🚨 आपातकालीन" else "🚨 Emergency Only", fontSize = 11.5.sp) }
                    )

                    listOf("All", "Plumbing", "Electrical", "Carpentry", "Painting", "AC Repair").forEach { skill ->
                        FilterChip(
                            selected = selectedSkillFilter == skill,
                            onClick = { selectedSkillFilter = skill },
                            label = { Text(skill, fontSize = 11.5.sp) }
                        )
                    }
                }
            }
        }

        // Interactive Map Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFE5EEF4))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.6f, 3.5f)
                            offset += pan
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2f + offset.x
                val centerY = canvasHeight / 2f + offset.y

                // Scale factor: 1 degree approx 5000 pixels at 1.0x
                val coordScale = 6500f * scale

                // 1. Draw Grid Roads & Geographic Water Bodies (Ganga River simulation)
                // Draw river curve across northern latitude
                val riverY = centerY - (25.6250 - refLat).toFloat() * coordScale
                drawLine(
                    color = Color(0xFFBAE6FD),
                    start = Offset(0f, riverY),
                    end = Offset(canvasWidth, riverY + 30f),
                    strokeWidth = 35f * scale
                )

                // Background City Grid Lines
                val gridSpacing = 60f * scale
                var x = (offset.x % gridSpacing)
                while (x < canvasWidth) {
                    drawLine(
                        color = Color(0xFFD0DCE5),
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1.5f
                    )
                    x += gridSpacing
                }
                var y = (offset.y % gridSpacing)
                while (y < canvasHeight) {
                    drawLine(
                        color = Color(0xFFD0DCE5),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.5f
                    )
                    y += gridSpacing
                }

                // 2. Draw Demand Heat Zones (Red / Orange translucent circles)
                // Zone 1: Patna West (Danapur/Kankarbagh)
                val zone1X = centerX - 0.020f * coordScale
                val zone1Y = centerY + 0.015f * coordScale
                drawCircle(
                    color = SahakaarCrimson.copy(alpha = 0.18f),
                    radius = 85f * scale,
                    center = Offset(zone1X, zone1Y)
                )
                drawCircle(
                    color = SahakaarCrimson.copy(alpha = 0.4f),
                    radius = 85f * scale,
                    center = Offset(zone1X, zone1Y),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                )

                // Zone 2: Patna Central
                val zone2X = centerX + 0.005f * coordScale
                val zone2Y = centerY - 0.005f * coordScale
                drawCircle(
                    color = SahakaarAmber.copy(alpha = 0.20f),
                    radius = 95f * scale,
                    center = Offset(zone2X, zone2Y)
                )

                // 3. Draw Cooperative Hubs (Navy Pentagons / Circles with flag)
                cooperatives.forEach { coop ->
                    val cx = centerX + ((coop.longitude - refLon).toFloat() * coordScale)
                    val cy = centerY - ((coop.latitude - refLat).toFloat() * coordScale)

                    // Draw coop hub marker
                    drawCircle(
                        color = SahakaarNavy,
                        radius = 16f * scale.coerceIn(0.8f, 1.5f),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = SahakaarGold,
                        radius = 8f * scale.coerceIn(0.8f, 1.5f),
                        center = Offset(cx, cy)
                    )
                }

                // 4. Draw Workers
                filteredWorkers.forEach { worker ->
                    val wx = centerX + ((worker.longitude - refLon).toFloat() * coordScale)
                    val wy = centerY - ((worker.latitude - refLat).toFloat() * coordScale)

                    val pinColor = when {
                        worker.isEmergencyReady && filterEmergency -> SahakaarCrimson
                        worker.isBusy -> SahakaarAmber
                        worker.isOnline -> SahakaarGreen
                        else -> Slate500
                    }

                    // Outer pulse ring
                    drawCircle(
                        color = pinColor.copy(alpha = 0.25f),
                        radius = 14f * scale.coerceIn(0.8f, 1.6f),
                        center = Offset(wx, wy)
                    )
                    // Inner pin head
                    drawCircle(
                        color = pinColor,
                        radius = 7.5f * scale.coerceIn(0.8f, 1.6f),
                        center = Offset(wx, wy)
                    )
                    // White core
                    drawCircle(
                        color = Color.White,
                        radius = 3.5f * scale.coerceIn(0.8f, 1.6f),
                        center = Offset(wx, wy)
                    )
                }
            }

            // Legend Overlay (Top-Right)
            Surface(
                color = Color.White.copy(alpha = 0.92f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate300),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(if (isHi) "संकेतक सूची" else "MAP LEGEND", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Slate600)
                    Spacer(modifier = Modifier.height(4.dp))
                    LegendRow(SahakaarGreen, if (isHi) "उपलब्ध श्रमिक" else "Available Worker")
                    LegendRow(SahakaarAmber, if (isHi) "व्यस्त श्रमिक" else "Busy on Job")
                    LegendRow(SahakaarCrimson, if (isHi) "आपातकालीन मांग क्षेत्र" else "Emergency Demand Zone")
                    LegendRow(SahakaarNavy, if (isHi) "सहकारी समिति केंद्र" else "Cooperative Hub")
                }
            }

            // Map Controls (Zoom in / Zoom out / Reset)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { scale = (scale * 1.3f).coerceAtMost(3.5f) },
                    modifier = Modifier.size(40.dp),
                    containerColor = Color.White,
                    contentColor = Slate900
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
                }
                FloatingActionButton(
                    onClick = { scale = (scale / 1.3f).coerceAtLeast(0.6f) },
                    modifier = Modifier.size(40.dp),
                    containerColor = Color.White,
                    contentColor = Slate900
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
                }
                FloatingActionButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    modifier = Modifier.size(40.dp),
                    containerColor = SahakaarNavy,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Center", modifier = Modifier.size(18.dp))
                }
            }

            // Selected Worker Card Preview at bottom
            if (filteredWorkers.isNotEmpty()) {
                val previewWorker = selectedWorkerOnMap ?: filteredWorkers.first()
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.92f)
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(SahakaarGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = previewWorker.name.take(2).uppercase(),
                                fontWeight = FontWeight.Black,
                                color = Slate900
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(previewWorker.name, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Slate900)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⭐ ${previewWorker.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SahakaarAmber)
                            }
                            Text(
                                text = "${previewWorker.primarySkill} • ${previewWorker.cooperativeName}",
                                fontSize = 11.sp,
                                color = Slate600,
                                maxLines = 1
                            )
                            Text(
                                text = if (previewWorker.isBusy) "Current Status: BUSY" else "Current Status: AVAILABLE (1.8 km away)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (previewWorker.isBusy) SahakaarAmber else SahakaarGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onWorkerSelected(previewWorker) },
                            colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (isHi) "पासपोर्ट देखें" else "Passport", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 10.sp, color = Slate700)
    }
}
