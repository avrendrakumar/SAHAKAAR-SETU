package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun RoleSwitchHeader(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    notifications: List<NotificationEntity>,
    onNotificationClick: (NotificationEntity) -> Unit
) {
    var showNotifsDialog by remember { mutableStateOf(false) }
    val isHi = LanguageManager.isHindi

    Surface(
        color = SahakaarNavy,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Main Top Bar: Logo, App Name, Tagline & Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SahakaarGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = "Logo",
                            tint = Slate900,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SAHAKAAR SETU",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SahakaarTeal,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "COOP OS",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isHi) "भरोसेमंद कौशल • उचित कार्य • सशक्त समाज" else "Trusted Skills. Fair Work. Stronger Communities.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                // Language Toggle & Notification Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language button
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { LanguageManager.toggleLanguage() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Translate",
                                tint = SahakaarGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isHi) "ENG" else "हिन्दी",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Notifications
                    IconButton(
                        onClick = { showNotifsDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (notifications.isNotEmpty()) {
                                    Badge(containerColor = SahakaarCrimson) {
                                        Text("${notifications.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Role Selector Chips (Customer, Worker, Coop Admin, Federation, Super Admin)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserRole.values().forEach { role ->
                    val isSelected = currentRole == role
                    val label = if (isHi) role.hindiName else role.displayName
                    val icon = when (role) {
                        UserRole.CUSTOMER -> Icons.Default.Person
                        UserRole.WORKER -> Icons.Default.Engineering
                        UserRole.COOPERATIVE_ADMIN -> Icons.Default.CorporateFare
                        UserRole.FEDERATION_ADMIN -> Icons.Default.AccountBalance
                        UserRole.SUPER_ADMIN -> Icons.Default.AdminPanelSettings
                    }

                    Surface(
                        color = if (isSelected) SahakaarGold else Color.White.copy(alpha = 0.12f),
                        contentColor = if (isSelected) Slate900 else Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable { onRoleSelected(role) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNotifsDialog) {
        AlertDialog(
            onDismissRequest = { showNotifsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = SahakaarBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isHi) "सूचनाएं" else "Notifications (${notifications.size})", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    if (notifications.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(if (isHi) "कोई नई सूचना नहीं है" else "No recent notifications", color = Slate500)
                        }
                    } else {
                        notifications.forEach { notif ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate100),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        showNotifsDialog = false
                                        onNotificationClick(notif)
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                        Text(notif.timeAgo, fontSize = 11.sp, color = Slate500)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(notif.message, fontSize = 12.sp, color = Slate700)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifsDialog = false }) {
                    Text(if (isHi) "बंद करें" else "Close")
                }
            }
        )
    }
}
