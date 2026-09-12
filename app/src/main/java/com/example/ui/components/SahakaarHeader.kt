package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun SahakaarTopAppBar(
    currentRole: String,
    currentLanguage: AppLanguage,
    userName: String? = null,
    isLoggedIn: Boolean = false,
    unreadNotificationCount: Int = 0,
    onLanguageToggle: () -> Unit,
    onRoleChange: (String) -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenAiAssistant: () -> Unit = {},
    onToggleTextSize: () -> Unit = {},
    onLogout: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ElegantDarkBg)
            .statusBarsPadding()
    ) {
        // Main Branding Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // App Emblem
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElegantDarkSurface)
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_sahakaar_logo),
                        contentDescription = "Sahakaar Setu Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = AppStrings.get("app_name", currentLanguage).uppercase(),
                            color = ElegantLavender,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )
                        if (isLoggedIn) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElegantMintGreen.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantMintGreen.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "LOGGED IN",
                                    color = ElegantMintGreen,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (!isLoggedIn) {
                            "Guest Citizen"
                        } else if (!userName.isNullOrBlank()) {
                            "Namaste, $userName"
                        } else {
                            when (currentRole) {
                                "WORKER" -> "Namaste, Raj"
                                "COOPERATIVE_ADMIN" -> "Patna Central Co-op"
                                "FEDERATION_ADMIN" -> "Bihar Labour Fed"
                                "SUPER_ADMIN" -> "System Admin"
                                "INSTITUTION" -> "Institutional Client"
                                else -> "Namaste, Citizen"
                            }
                        },
                        color = ElegantTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                    )
                }
            }

            // Right Action Controls (Language toggle & Profile avatar / Logout)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Text Size Resize Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ElegantDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onToggleTextSize() }
                        .testTag("btn_header_text_resize")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Resize Text Size",
                            tint = ElegantLavender,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "A±",
                            color = ElegantLavender,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Language toggle pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ElegantDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onLanguageToggle() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Change Language",
                            tint = ElegantLavender,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.HINDI) "हिंदी" else "ENG",
                            color = ElegantTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                if (isLoggedIn) {
                    // Notification Bell with Badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSurface)
                            .border(1.dp, ElegantDarkBorder, CircleShape)
                            .clickable { onOpenNotifications() }
                            .testTag("btn_header_notifications"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = ElegantLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(ElegantEmergencyRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Profile Avatar Button
                    Surface(
                        shape = CircleShape,
                        color = ElegantDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantLavender),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onOpenProfile() }
                            .testTag("btn_header_profile")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (currentRole) {
                                    "WORKER" -> Icons.Default.Engineering
                                    "COOPERATIVE_ADMIN" -> Icons.Default.AdminPanelSettings
                                    "FEDERATION_ADMIN" -> Icons.Default.AccountBalance
                                    "SUPER_ADMIN" -> Icons.Default.Security
                                    else -> Icons.Default.Person
                                },
                                contentDescription = "Profile",
                                tint = ElegantLavender,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    // Log In Button for Guest mode
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ElegantLavender,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onLoginClick() }
                            .testTag("btn_header_login")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = "Log In",
                                tint = ElegantOnLavender,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Log In",
                                color = ElegantOnLavender,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
