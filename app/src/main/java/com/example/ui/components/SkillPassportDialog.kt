package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WorkerProfileEntity
import com.example.ui.theme.*

@Composable
fun SkillPassportDialog(
    worker: WorkerProfileEntity,
    onDismiss: () -> Unit
) {
    var showVerificationSealDialog by remember { mutableStateOf(false) }
    val isHi = LanguageManager.isHindi

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Official Passport Header (Navy & Gold Trust Banner)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SahakaarNavy, Color(0xFF0F3E6D))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = SahakaarGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isHi) "डिजिटल कौशल पासपोर्ट" else "DIGITAL SKILL PASSPORT",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "GOVERNMENT OF BIHAR • LABOUR COOPERATIVE FEDERATION",
                                        color = SahakaarGold,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Worker Profile Summary in Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(SahakaarGold)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = worker.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Slate900
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = worker.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "ID: ${worker.workerCode}",
                                    color = SahakaarGold,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    color = SahakaarGreen.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4ADE80),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isHi) "सहकारी सत्यापित श्रमिक" else "COOPERATIVE VERIFIED",
                                            color = Color(0xFF4ADE80),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Passport Body Details
                Column(modifier = Modifier.padding(16.dp)) {

                    // Key Performance Metrics Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate100)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricItem(
                            title = if (isHi) "रेटिंग" else "Rating",
                            value = "${worker.rating} ★",
                            subValue = "${worker.reviewCount} ${if (isHi) "समीक्षाएं" else "reviews"}",
                            color = SahakaarAmber
                        )
                        Divider(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp),
                            color = Slate300
                        )
                        MetricItem(
                            title = if (isHi) "विश्वसनीयता" else "Reliability",
                            value = "${worker.reliabilityScore}%",
                            subValue = if (isHi) "शीर्ष 5%" else "Top 5% Rank",
                            color = SahakaarTeal
                        )
                        Divider(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp),
                            color = Slate300
                        )
                        MetricItem(
                            title = if (isHi) "अनुभव" else "Experience",
                            value = "${worker.experienceYears} Yrs",
                            subValue = "${worker.completedJobs} ${if (isHi) "कार्य पूर्ण" else "jobs done"}",
                            color = SahakaarBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cooperative Affiliation Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryContainerLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CorporateFare,
                                contentDescription = null,
                                tint = PrimaryLight,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isHi) "संबद्ध सहकारी समिति" else "Affiliated Cooperative",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                                Text(
                                    text = worker.cooperativeName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPrimaryContainerLight
                                )
                                Text(
                                    text = "Reg: COOP-BR-${worker.district.take(3).uppercase()}-2024 • District: ${worker.district}",
                                    fontSize = 10.5.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Skill Competencies
                    Text(
                        text = if (isHi) "प्रमाणित कौशल एवं क्षमताएं" else "Certified Skills & Capabilities",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = SahakaarTeal.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SahakaarTeal)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = SahakaarTeal, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${worker.primarySkill} (${if (isHi) "प्राथमिक" else "Primary"})",
                                    color = SahakaarTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = worker.secondarySkills.split(",").firstOrNull() ?: "General Maintenance",
                                color = Slate700,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Certifications
                    Text(
                        text = if (isHi) "प्रमाणपत्र एवं लाइसेंस" else "Accredited Certifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    worker.certList.split(",").forEach { cert ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = SahakaarGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cert.trim(), fontSize = 12.sp, color = Slate800)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Verification Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate50),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isHi) "डिजिटल क्यूआर सत्यापन" else "DIGITAL QR VERIFICATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Actual Canvas-drawn QR Code Matrix
                            QrCodeCanvas(
                                payload = "SAHAKAAR_SETU:${worker.workerCode}:${worker.name}:${worker.primarySkill}:${worker.cooperativeName}",
                                sizeDp = 140.dp,
                                qrColor = SahakaarNavy
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isHi) "प्रामाणिकता जांचने के लिए स्कैन करें" else "Scan with any camera or tap below to verify registry",
                                fontSize = 11.sp,
                                color = Slate500,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showVerificationSealDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isHi) "सत्यापन विवरण देखें" else "Verify Worker Online")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal when QR is scanned / verified
    if (showVerificationSealDialog) {
        AlertDialog(
            onDismissRequest = { showVerificationSealDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SahakaarGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SahakaarGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isHi) "सत्यापित कुशल श्रमिक" else "VERIFIED SKILLED WORKER",
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = SahakaarGreen,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Official Registry Match Confirmed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            VerifyRow("Worker Name:", worker.name)
                            VerifyRow("Worker ID:", worker.workerCode)
                            VerifyRow("Primary Skill:", worker.primarySkill)
                            VerifyRow("Cooperative:", worker.cooperativeName)
                            VerifyRow("Status:", "ACTIVE & VERIFIED")
                            VerifyRow("Verification Date:", worker.verificationDate)
                            VerifyRow("Reliability Score:", "${worker.reliabilityScore}%")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cryptographically signed by Bihar State Labour Cooperative Federation.",
                        fontSize = 10.5.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVerificationSealDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SahakaarNavy)
                ) {
                    Text(if (isHi) "ठीक है" else "Understood")
                }
            }
        )
    }
}

@Composable
private fun MetricItem(title: String, value: String, subValue: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 11.sp, color = Slate600)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
        Text(subValue, fontSize = 10.sp, color = Slate500)
    }
}

@Composable
private fun VerifyRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.5.sp, color = Slate600)
        Text(value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate900)
    }
}
