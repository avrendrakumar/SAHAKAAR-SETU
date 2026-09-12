package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.NotificationEntity
import com.example.data.model.UserEntity
import com.example.data.model.WorkerProfileEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationCenterDialog(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unreadCount = notifications.count { !it.isRead }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            border = BorderStroke(1.dp, ElegantDarkBorder),
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElegantLavender.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = ElegantLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Notifications",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ElegantTextWhite
                            )
                            Text(
                                text = if (unreadCount > 0) "$unreadCount unread notifications" else "All caught up",
                                fontSize = 11.sp,
                                color = ElegantTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_notifications")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ElegantTextSecondary
                        )
                    }
                }

                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = onMarkAllAsRead,
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("btn_mark_all_read")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = ElegantLavender,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mark all as read",
                            fontSize = 12.sp,
                            color = ElegantLavender,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(
                    color = ElegantDarkBorder,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = ElegantTextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No notifications yet",
                                color = ElegantTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notif ->
                            NotificationItemCard(
                                notification = notif,
                                onMarkAsRead = { onMarkAsRead(notif.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    notification: NotificationEntity,
    onMarkAsRead: () -> Unit
) {
    val dateStr = remember(notification.timestamp) {
        try {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            sdf.format(Date(notification.timestamp))
        } catch (e: Exception) {
            "Recent"
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (!notification.isRead) ElegantDarkSurfaceVariant else ElegantDarkBg,
        border = BorderStroke(
            1.dp,
            if (!notification.isRead) ElegantLavender.copy(alpha = 0.4f) else ElegantDarkBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElegantLavender)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ElegantTextWhite
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = ElegantTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                fontSize = 12.sp,
                color = ElegantTextPrimary,
                lineHeight = 16.sp
            )

            if (!notification.isRead) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Mark Read",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantLavender,
                        modifier = Modifier
                            .clickable { onMarkAsRead() }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileDialog(
    user: UserEntity,
    workerProfile: WorkerProfileEntity? = null,
    onDismiss: () -> Unit,
    onUpdateProfile: (name: String, email: String, state: String, district: String, village: String) -> Unit = { _, _, _, _, _ -> },
    onSaveWorkerProfile: ((
        name: String, email: String, phone: String, city: String, state: String, district: String, village: String,
        primarySkill: String, secondarySkills: String, experienceYears: Int, certifications: String,
        dailyWageRate: Double, isAvailable: Boolean, isEmergencyReady: Boolean, zone: String
    ) -> Unit)? = null,
    onSaveCustomerProfile: ((
        name: String, email: String, phone: String, city: String, state: String, district: String, village: String
    ) -> Unit)? = null,
    onChangePassword: (oldPlain: String, newPlain: String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ElegantDarkBg)
                .systemBarsPadding()
        ) {
            UserProfileComponent(
                user = user,
                workerProfile = workerProfile,
                onSaveWorkerProfile = { n, em, ph, c, s, d, v, ps, ss, exp, cert, wage, avail, emerg, z ->
                    if (onSaveWorkerProfile != null) {
                        onSaveWorkerProfile(n, em, ph, c, s, d, v, ps, ss, exp, cert, wage, avail, emerg, z)
                    } else {
                        onUpdateProfile(n, em, s, d, v)
                    }
                },
                onSaveCustomerProfile = { n, em, ph, c, s, d, v ->
                    if (onSaveCustomerProfile != null) {
                        onSaveCustomerProfile(n, em, ph, c, s, d, v)
                    } else {
                        onUpdateProfile(n, em, s, d, v)
                    }
                },
                onChangePassword = onChangePassword,
                onLogout = {
                    onDismiss()
                    onLogout()
                },
                onClose = onDismiss
            )
        }
    }
}
