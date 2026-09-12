package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WorkerProfileEntity
import com.example.ui.theme.*

@Composable
fun SkillPassportCard(
    worker: WorkerProfileEntity,
    modifier: Modifier = Modifier,
    onBookClick: (() -> Unit)? = null
) {
    var showQrVerificationModal by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Elegant Skill Passport Banner (as in Elegant Dark reference)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFD1E4FF), Color(0xFFBEDBFF))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // QR Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFF00315C), RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = "QR",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF00315C)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Skill Passport",
                                color = Color(0xFF00315C),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                text = "${worker.primarySkill} • Level 4",
                                color = Color(0xFF00315C).copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF00315C)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = worker.cooperativeName,
                                    color = Color(0xFF00315C),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // QR Modal Trigger
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF00315C),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showQrVerificationModal = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "View QR",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verify",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Worker Details Section
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Worker Avatar Placeholder with initials
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(ElegantLavenderContainer)
                            .border(1.5.dp, ElegantLavender, CircleShape)
                    ) {
                        Text(
                            text = worker.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = worker.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ElegantTextWhite
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            VerificationBadge(status = worker.verificationStatus)
                        }

                        Text(
                            text = "ID: ${worker.workerIdTag}",
                            fontFamily = FontFamily.Monospace,
                            color = ElegantTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Primary Trade: ${worker.primarySkill}",
                            color = ElegantLavender,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Row (Rating, Reliability, Experience, Completed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElegantDarkSurfaceVariant, RoundedCornerShape(16.dp))
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(label = "RATING", value = "★ ${worker.customerRating}", valueColor = SahakaarGold)
                    MetricItem(label = "RELIABILITY", value = "${worker.reliabilityScore}%", valueColor = ElegantMintGreen)
                    MetricItem(label = "EXPERIENCE", value = "${worker.experienceYears} Yrs", valueColor = ElegantLavender)
                    MetricItem(label = "JOBS DONE", value = "${worker.completedJobs}", valueColor = ElegantTextPrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Certifications & Secondary Skills
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Cert",
                        tint = SahakaarGold,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Certifications: ${worker.certifications}",
                        color = ElegantTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Handyman,
                        contentDescription = "Skills",
                        tint = ElegantLavender,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Competencies: ${worker.secondarySkills}",
                        color = ElegantTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                if (onBookClick != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBookClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantLavender,
                            contentColor = ElegantOnLavender
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Book",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Select Worker (₹${worker.dailyWageRate.toInt()}/day)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Modal when QR code is scanned or tapped
    if (showQrVerificationModal) {
        Dialog(onDismissRequest = { showQrVerificationModal = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Official Seal Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .background(ElegantMintGreenContainer, CircleShape)
                            .border(1.dp, ElegantMintGreen, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Seal",
                            tint = ElegantMintGreen,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "VERIFIED WORKER",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = ElegantMintGreen,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Labour Cooperative Skill Identity Validated",
                        fontSize = 12.sp,
                        color = ElegantTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code visual matrix rendered via Canvas
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        QrCodeGraphic(seed = worker.workerIdTag.hashCode())
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Details Card inside modal
                    Surface(
                        color = ElegantDarkSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            PassportDetailRow("Name:", worker.name)
                            PassportDetailRow("Worker ID:", worker.workerIdTag)
                            PassportDetailRow("Affiliated Co-op:", worker.cooperativeName)
                            PassportDetailRow("Primary Trade:", worker.primarySkill)
                            PassportDetailRow("Govt Certification:", worker.certifications)
                            PassportDetailRow("Reliability Score:", "${worker.reliabilityScore}% verified")
                            PassportDetailRow("Background Check:", "Cleared & Biometric Registered")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showQrVerificationModal = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantLavender,
                            contentColor = ElegantOnLavender
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Close Verification Card", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Slate600)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun PassportDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            fontSize = 11.sp,
            color = Slate900,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 180.dp)
        )
    }
}

@Composable
fun QrCodeGraphic(seed: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val gridSize = 15
        val cellSize = size.width / gridSize
        val rand = kotlin.random.Random(seed)

        // Draw outer finder corners
        drawRect(Color.Black, Offset(0f, 0f), Size(cellSize * 4, cellSize * 4))
        drawRect(Color.White, Offset(cellSize, cellSize), Size(cellSize * 2, cellSize * 2))
        drawRect(Color.Black, Offset(cellSize * 1.5f, cellSize * 1.5f), Size(cellSize, cellSize))

        drawRect(Color.Black, Offset(size.width - cellSize * 4, 0f), Size(cellSize * 4, cellSize * 4))
        drawRect(Color.White, Offset(size.width - cellSize * 3, cellSize), Size(cellSize * 2, cellSize * 2))
        drawRect(Color.Black, Offset(size.width - cellSize * 2.5f, cellSize * 1.5f), Size(cellSize, cellSize))

        drawRect(Color.Black, Offset(0f, size.height - cellSize * 4), Size(cellSize * 4, cellSize * 4))
        drawRect(Color.White, Offset(cellSize, size.height - cellSize * 3), Size(cellSize * 2, cellSize * 2))
        drawRect(Color.Black, Offset(cellSize * 1.5f, size.height - cellSize * 2.5f), Size(cellSize, cellSize))

        // Draw pseudo-random payload dots
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val isCorner = (i < 5 && j < 5) || (i >= gridSize - 5 && j < 5) || (i < 5 && j >= gridSize - 5)
                if (!isCorner && rand.nextBoolean()) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(i * cellSize, j * cellSize),
                        size = Size(cellSize * 0.9f, cellSize * 0.9f)
                    )
                }
            }
        }
    }
}
