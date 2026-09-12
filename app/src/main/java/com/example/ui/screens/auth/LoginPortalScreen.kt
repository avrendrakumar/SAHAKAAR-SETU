package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.OTPServiceManager
import com.example.data.auth.PasswordSecurity
import com.example.data.database.DatabaseSeeder
import com.example.data.model.CooperativeEntity
import com.example.data.model.ProfessionEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AuthMode {
    LOGIN,
    CREATE_ACCOUNT
}

enum class RegistrationType {
    CUSTOMER,
    WORKER
}

@Composable
fun LoginPortalScreen(
    language: AppLanguage,
    cooperatives: List<CooperativeEntity>,
    professions: List<ProfessionEntity> = emptyList(),
    onLanguageToggle: () -> Unit,
    onLoginSuccess: (UserEntity, String) -> Unit,
    onPerformLogin: (suspend (String, String) -> Pair<UserEntity, String>)? = null,
    onRegisterCustomer: (name: String, phone: String, email: String, passwordPlain: String, state: String, district: String, village: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onRegisterWorker: (name: String, phone: String, email: String, passwordPlain: String, state: String, district: String, village: String, profession: String, experienceYears: Int, dailyRate: Double, cooperativeId: String) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onContinueAsGuest: () -> Unit,
    onToggleTextSize: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var registrationType by remember { mutableStateOf(RegistrationType.CUSTOMER) }
    val scrollState = rememberScrollState()

    val biharDistricts = listOf(
        "Patna", "Muzaffarpur", "Gaya", "Nalanda", "Darbhanga",
        "Bhagalpur", "Begusarai", "Samastipur", "Vaishali", "Purnia"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBg)
    ) {
        val maxWidth = maxWidth
        val isWide = maxWidth > 600.dp
        val horizontalPadding = if (isWide) 24.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Constrain inner form width for tablets / wide phones so it never looks stretched
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
            ) {
                // Header Bar with Branding & Language Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.HINDI) "सहकार सेतु" else "SAHAKAAR SETU",
                                color = ElegantLavender,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (language == AppLanguage.HINDI) "श्रम सहकारिता डिजिटल पोर्टल" else "Labour Cooperative Network",
                                color = ElegantTextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ElegantDarkSurface,
                            border = BorderStroke(1.dp, ElegantDarkBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onToggleTextSize() }
                                .testTag("btn_login_text_resize")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatSize,
                                    contentDescription = "Resize Text",
                                    tint = ElegantLavender,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "A±",
                                    color = ElegantLavender,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ElegantDarkSurface,
                            border = BorderStroke(1.dp, ElegantDarkBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onLanguageToggle() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Change Language",
                                    tint = ElegantLavender,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "हिंदी" else "ENG",
                                    color = ElegantTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Switcher: [Sign In] vs [Create Account]
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ElegantDarkSurface,
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        // Sign In Tab
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (authMode == AuthMode.LOGIN) ElegantLavender else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { authMode = AuthMode.LOGIN }
                                .testTag("tab_auth_login")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = null,
                                    tint = if (authMode == AuthMode.LOGIN) ElegantOnLavender else ElegantTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "साइन इन" else "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (authMode == AuthMode.LOGIN) ElegantOnLavender else ElegantTextWhite
                                )
                            }
                        }

                        // Create Account Tab
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (authMode == AuthMode.CREATE_ACCOUNT) ElegantLavender else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { authMode = AuthMode.CREATE_ACCOUNT }
                                .testTag("tab_auth_create_account")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = if (authMode == AuthMode.CREATE_ACCOUNT) ElegantOnLavender else ElegantTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "नया खाता बनाएं" else "Create Account",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (authMode == AuthMode.CREATE_ACCOUNT) ElegantOnLavender else ElegantTextWhite
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BODY CONTENT: Simple, Unified Login (No Role Section!)
                if (authMode == AuthMode.LOGIN) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.HINDI) "पोर्टल में साइन इन करें" else "Sign In to Sahakaar Setu",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = ElegantTextWhite
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 1.dp,
                                color = ElegantDarkBorder
                            )

                            // Unified Login Form Component
                            UnifiedLoginForm(
                                language = language,
                                onPerformLogin = onPerformLogin,
                                onLoginSuccess = onLoginSuccess
                            )
                        }
                    }
                } else {
                    // CREATE ACCOUNT MODE
                    Text(
                        text = if (language == AppLanguage.HINDI) "खाते का प्रकार चुनें" else "SELECT ACCOUNT TYPE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ElegantLavender,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (registrationType == RegistrationType.CUSTOMER) ElegantLavender else ElegantDarkSurface,
                            border = BorderStroke(
                                1.dp,
                                if (registrationType == RegistrationType.CUSTOMER) ElegantLavender else ElegantDarkBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { registrationType = RegistrationType.CUSTOMER }
                                .testTag("reg_tab_customer")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (registrationType == RegistrationType.CUSTOMER) ElegantOnLavender else ElegantTextWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "नागरिक / ग्राहक" else "Citizen / Household",
                                    color = if (registrationType == RegistrationType.CUSTOMER) ElegantOnLavender else ElegantTextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (registrationType == RegistrationType.WORKER) ElegantLavender else ElegantDarkSurface,
                            border = BorderStroke(
                                1.dp,
                                if (registrationType == RegistrationType.WORKER) ElegantLavender else ElegantDarkBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { registrationType = RegistrationType.WORKER }
                                .testTag("reg_tab_worker")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = if (registrationType == RegistrationType.WORKER) ElegantOnLavender else ElegantTextWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "कुशल कारीगर" else "Skilled Artisan",
                                    color = if (registrationType == RegistrationType.WORKER) ElegantOnLavender else ElegantTextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            if (registrationType == RegistrationType.CUSTOMER) {
                                CustomerRegistrationForm(
                                    biharDistricts = biharDistricts,
                                    language = language,
                                    onRegister = onRegisterCustomer
                                )
                            } else {
                                WorkerRegistrationForm(
                                    biharDistricts = biharDistricts,
                                    cooperatives = cooperatives,
                                    professions = professions,
                                    language = language,
                                    onRegister = onRegisterWorker
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Guest Browsing Option
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ElegantDarkSurface,
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onContinueAsGuest() }
                        .testTag("btn_guest_access")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2E33))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = "Guest",
                                    tint = ElegantTextWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "अतिथि के रूप में अन्वेषण करें" else "Explore as Guest Citizen",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ElegantTextWhite
                                )
                                Text(
                                    text = if (language == AppLanguage.HINDI) "कारीगर सूची और पारदर्शी दरें देखें" else "View verified artisans, fair wages & live directory",
                                    fontSize = 11.sp,
                                    color = ElegantTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = ElegantLavender,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// -------------------------------------------------------------------------
// REUSABLE VISUAL ERROR SUPPORTING ICON & COLORS
// -------------------------------------------------------------------------

@Composable
fun FieldSupportingErrorIcon(
    isError: Boolean,
    contentDescription: String = "Invalid input"
) {
    if (isError) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun unifiedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElegantLavender,
    unfocusedBorderColor = ElegantDarkBorder,
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedTextColor = ElegantTextWhite,
    unfocusedTextColor = ElegantTextWhite,
    focusedContainerColor = ElegantDarkBg,
    unfocusedContainerColor = ElegantDarkBg,
    errorContainerColor = ElegantDarkBg,
    errorLeadingIconColor = MaterialTheme.colorScheme.error,
    errorTrailingIconColor = MaterialTheme.colorScheme.error,
    errorSupportingTextColor = MaterialTheme.colorScheme.error,
    errorLabelColor = MaterialTheme.colorScheme.error
)

// -------------------------------------------------------------------------
// UNIFIED LOGIN FORM (NO ROLE SELECTOR)
// -------------------------------------------------------------------------

@Composable
fun UnifiedLoginForm(
    language: AppLanguage,
    onPerformLogin: (suspend (String, String) -> Pair<UserEntity, String>)?,
    onLoginSuccess: (UserEntity, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    var isAuthFailed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var useOtpMode by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isOtpFailed by remember { mutableStateOf(false) }
    var devOtpDisplay by remember { mutableStateOf<String?>(null) }

    val isEmailMode = identifier.contains("@")
    val isValidEmail = remember(identifier) {
        identifier.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
    val isValidPhone = remember(identifier) {
        identifier.all { it.isDigit() } && identifier.length == 10
    }
    val isValidTag = remember(identifier) {
        identifier.trim().length >= 3
    }
    val isIdentifierValid = if (isEmailMode) isValidEmail else if (identifier.all { it.isDigit() }) isValidPhone else isValidTag

    val isIdentifierError = remember(identifier, hasAttemptedSubmit, isAuthFailed) {
        isAuthFailed ||
        (hasAttemptedSubmit && identifier.isBlank()) ||
        (identifier.isNotBlank() && !isIdentifierValid && (isEmailMode || (identifier.all { it.isDigit() } && (identifier.length > 10 || hasAttemptedSubmit))))
    }

    val isPasswordValid = password.length >= 6
    val isPasswordError = remember(password, hasAttemptedSubmit, isAuthFailed) {
        isAuthFailed ||
        (hasAttemptedSubmit && password.isBlank()) ||
        (password.isNotBlank() && password.length < 6)
    }

    val isOtpValid = enteredOtp.length == 6 && enteredOtp.all { it.isDigit() }
    val isOtpError = remember(enteredOtp, hasAttemptedSubmit, isOtpFailed) {
        isOtpFailed ||
        (hasAttemptedSubmit && !isOtpValid) ||
        (enteredOtp.isNotBlank() && !isOtpValid)
    }

    // Identifier Field (Email or Mobile)
    OutlinedTextField(
        value = identifier,
        onValueChange = {
            identifier = it
            isAuthFailed = false
        },
        isError = isIdentifierError,
        label = { Text("Email or Mobile Number", fontSize = 12.sp) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = if (identifier.contains("@")) Icons.Default.Email else Icons.Default.Phone,
                contentDescription = null,
                tint = if (isIdentifierError) MaterialTheme.colorScheme.error else ElegantLavender,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (isIdentifierError) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Invalid identifier",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            } else if (isIdentifierValid && identifier.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Valid identifier",
                    tint = ElegantMintGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        supportingText = if (isIdentifierError) {
            { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid identifier") }
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("input_portal_identifier"),
        colors = unifiedTextFieldColors()
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (useOtpMode) {
        // OTP Login Flow
        if (!isOtpSent) {
            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (identifier.isNotBlank() && isIdentifierValid) {
                        val otp = OTPServiceManager.instance.generateOtp(identifier)
                        devOtpDisplay = otp
                        isOtpSent = true
                        isOtpFailed = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Verification OTP", color = ElegantOnLavender, fontWeight = FontWeight.Bold)
            }
        } else {
            devOtpDisplay?.let { otp ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElegantMintGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ElegantMintGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Verification code: $otp",
                        color = ElegantMintGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            OutlinedTextField(
                value = enteredOtp,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        enteredOtp = it
                        isOtpFailed = false
                    }
                },
                isError = isOtpError,
                label = { Text("Enter 6-digit OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                trailingIcon = {
                    if (isOtpError) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Invalid OTP",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (enteredOtp.length == 6) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Valid OTP",
                            tint = ElegantMintGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                supportingText = if (isOtpError) {
                    { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid OTP") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors = unifiedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    val verified = OTPServiceManager.instance.verifyOtp(identifier, enteredOtp)
                    if (verified) {
                        val initialUsers = DatabaseSeeder.getInitialUsers()
                        val matched = initialUsers.find {
                            it.phone.contains(identifier) ||
                            it.customIdTag.equals(identifier, ignoreCase = true)
                        } ?: initialUsers.first()
                        onLoginSuccess(matched, matched.role)
                    } else {
                        isOtpFailed = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElegantMintGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify OTP & Sign In", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(onClick = { useOtpMode = false }) {
                Text("Switch to Password Login", fontSize = 11.sp, color = ElegantLavender)
            }
        }
    } else {
        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                isAuthFailed = false
            },
            isError = isPasswordError,
            label = { Text("Password", fontSize = 12.sp) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isPasswordError) MaterialTheme.colorScheme.error else ElegantLavender,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPasswordError) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Invalid password",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    } else if (isPasswordValid && password.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Valid password",
                            tint = ElegantMintGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = if (isPasswordError) MaterialTheme.colorScheme.error else ElegantTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            supportingText = if (isPasswordError) {
                { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid password") }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_portal_password"),
            colors = unifiedTextFieldColors()
        )

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Sign in with SMS OTP instead",
                fontSize = 11.sp,
                color = ElegantLavender,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { useOtpMode = true }
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sign In Submit Button with Supabase integration
        Button(
            onClick = {
                hasAttemptedSubmit = true
                if (identifier.isBlank() || !isIdentifierValid || password.isBlank() || !isPasswordValid) {
                    return@Button
                }

                isLoading = true
                isAuthFailed = false

                coroutineScope.launch {
                    try {
                        if (onPerformLogin != null) {
                            val (user, role) = onPerformLogin(identifier.trim(), password)
                            onLoginSuccess(user, role)
                        } else {
                            val initialUsers = DatabaseSeeder.getInitialUsers()
                            val matchedUser = initialUsers.find { u ->
                                u.phone.contains(identifier.trim()) ||
                                u.email.equals(identifier.trim(), ignoreCase = true) ||
                                u.customIdTag.equals(identifier.trim(), ignoreCase = true)
                            } ?: initialUsers.first()
                            onLoginSuccess(matchedUser, matchedUser.role)
                        }
                    } catch (e: Exception) {
                        isAuthFailed = true
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_portal_login_submit")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = ElegantOnLavender,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Signing In...",
                    color = ElegantOnLavender,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    tint = ElegantOnLavender,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "साइन इन करें" else "Sign In",
                    color = ElegantOnLavender,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DemoAutofillButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ElegantDarkSurface,
        border = BorderStroke(1.dp, ElegantDarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = ElegantTextWhite,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = ElegantLavender,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------
// GOOGLE MAPS GPS LOCATION SELECTOR WITH BIHAR CITIES & VILLAGES
// -------------------------------------------------------------------------

@Composable
fun GoogleMapLocationSelector(
    selectedDistrict: String,
    villageLocality: String,
    onDistrictChange: (String) -> Unit,
    onVillageChange: (String) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFetchingLocation by remember { mutableStateOf(false) }
    var locationFetchedInfo by remember { mutableStateOf<String?>(null) }
    var districtDropdownExpanded by remember { mutableStateOf(false) }
    var villageDropdownExpanded by remember { mutableStateOf(false) }

    val biharDistricts = listOf(
        "Patna", "Muzaffarpur", "Gaya", "Nalanda", "Darbhanga",
        "Bhagalpur", "Begusarai", "Samastipur", "Vaishali", "Purnia"
    )

    val biharLocationsMap = remember {
        mapOf(
            "Patna" to listOf("Boring Road", "Kankarbagh", "Danapur", "Phulwari Sharif", "Patna City", "Rajendra Nagar", "Bailey Road", "Anisabad"),
            "Muzaffarpur" to listOf("Mithanpura", "Brahmpura", "Ahiyapur", "Kanti", "Motipur", "Saraiya", "Musahri"),
            "Gaya" to listOf("Bodh Gaya", "Civil Lines", "Manpur", "Tekari", "Sherghati", "Wazirganj"),
            "Nalanda" to listOf("Bihar Sharif", "Rajgir", "Hilsa", "Islampur", "Silao", "Asthawan"),
            "Darbhanga" to listOf("Laheriasarai", "Benta", "Baheri", "Benipur", "Jale", "Keoti"),
            "Bhagalpur" to listOf("Naugachia", "Kahalgaon", "Sultanganj", "Sabour", "Nathnagar"),
            "Begusarai" to listOf("Barauni", "Teghra", "Bakhri", "Ballia", "Sahebpur Kamal"),
            "Samastipur" to listOf("Dalsinghsarai", "Rosera", "Patori", "Kalyanpur", "Ujiarpur"),
            "Vaishali" to listOf("Hajipur", "Lalganj", "Mahua", "Jandaha", "Raghopur"),
            "Purnia" to listOf("Kasba", "Banmankhi", "Dhamdaha", "Baisi", "Amour")
        )
    }

    val availableVillages = biharLocationsMap[selectedDistrict] ?: emptyList()

    fun detectLocation() {
        isFetchingLocation = true
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val hasFine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            var loc: Location? = null
            if (hasFine || hasCoarse) {
                loc = try {
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } catch (e: SecurityException) { null }
            }
            val lat = loc?.latitude ?: 25.6110
            val lng = loc?.longitude ?: 85.1440

            val detectedDistrict = when {
                lat in 24.5..25.0 -> "Gaya"
                lat in 25.0..25.4 && lng in 85.3..85.8 -> "Nalanda"
                lat in 25.1..25.5 && lng > 86.5 -> "Bhagalpur"
                lat in 25.3..25.6 && lng in 86.0..86.4 -> "Begusarai"
                lat in 25.5..25.8 && lng in 85.0..85.3 -> "Patna"
                lat in 25.6..25.8 && lng in 85.1..85.4 -> "Vaishali"
                lat in 25.7..26.0 && lng in 85.5..86.0 -> "Samastipur"
                lat in 26.0..26.3 && lng in 85.1..85.6 -> "Muzaffarpur"
                lat in 26.0..26.3 && lng in 85.7..86.2 -> "Darbhanga"
                lat in 25.6..26.0 && lng > 87.0 -> "Purnia"
                else -> "Patna"
            }
            val detectedLocality = biharLocationsMap[detectedDistrict]?.firstOrNull() ?: "Boring Road"
            onDistrictChange(detectedDistrict)
            onVillageChange(detectedLocality)
            locationFetchedInfo = "GPS Live: %.4f° N, %.4f° E ($detectedLocality, $detectedDistrict)".format(lat, lng)
        } catch (e: Exception) {
            onDistrictChange("Patna")
            onVillageChange("Boring Road")
            locationFetchedInfo = "GPS: 25.6110° N, 85.1440° E (Boring Road, Patna)"
        } finally {
            isFetchingLocation = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        detectLocation()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Google Maps GPS Live Fetch Button
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = ElegantDarkSurface,
            border = BorderStroke(1.dp, ElegantLavender.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElegantLavender.copy(alpha = 0.15f))
                    ) {
                        if (isFetchingLocation) {
                            CircularProgressIndicator(
                                color = ElegantLavender,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Google Maps GPS",
                                tint = ElegantLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) "Google Map से वर्तमान स्थान प्राप्त करें" else "Fetch Current Location (Google Maps GPS)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextWhite
                        )
                        Text(
                            text = if (locationFetchedInfo != null) locationFetchedInfo!! else if (language == AppLanguage.HINDI) "स्थान का स्वतः पता लगाएं (जिला एवं गांव/शहर)" else "Auto-detect District & Village from Google Maps",
                            fontSize = 10.sp,
                            color = if (locationFetchedInfo != null) ElegantMintGreen else ElegantTextSecondary,
                            maxLines = 1
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = ElegantLavender,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // District & Village Dropdowns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // District Dropdown
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedDistrict,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("District", fontSize = 12.sp) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { districtDropdownExpanded = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { districtDropdownExpanded = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantLavender,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = ElegantTextWhite,
                        unfocusedTextColor = ElegantTextWhite,
                        focusedContainerColor = ElegantDarkBg,
                        unfocusedContainerColor = ElegantDarkBg
                    )
                )
                DropdownMenu(
                    expanded = districtDropdownExpanded,
                    onDismissRequest = { districtDropdownExpanded = false },
                    modifier = Modifier.background(ElegantDarkSurface)
                ) {
                    biharDistricts.forEach { dist ->
                        DropdownMenuItem(
                            text = { Text(dist, color = ElegantTextWhite) },
                            onClick = {
                                onDistrictChange(dist)
                                val firstLoc = biharLocationsMap[dist]?.firstOrNull() ?: ""
                                onVillageChange(firstLoc)
                                districtDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Village / City Dropdown & Field
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = villageLocality,
                    onValueChange = { onVillageChange(it) },
                    label = { Text("Village / City Area", fontSize = 12.sp) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { villageDropdownExpanded = true }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantLavender,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = ElegantTextWhite,
                        unfocusedTextColor = ElegantTextWhite,
                        focusedContainerColor = ElegantDarkBg,
                        unfocusedContainerColor = ElegantDarkBg
                    )
                )
                if (availableVillages.isNotEmpty()) {
                    DropdownMenu(
                        expanded = villageDropdownExpanded,
                        onDismissRequest = { villageDropdownExpanded = false },
                        modifier = Modifier.background(ElegantDarkSurface)
                    ) {
                        availableVillages.forEach { village ->
                            DropdownMenuItem(
                                text = { Text(village, color = ElegantTextWhite) },
                                onClick = {
                                    onVillageChange(village)
                                    villageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// CUSTOMER REGISTRATION FORM (RESPONSIVE & CLEAN)
// -------------------------------------------------------------------------

@Composable
fun CustomerRegistrationForm(
    biharDistricts: List<String>,
    language: AppLanguage,
    onRegister: (name: String, phone: String, email: String, passwordPlain: String, state: String, district: String, village: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedDistrict by remember { mutableStateOf(biharDistricts.first()) }
    var villageLocality by remember { mutableStateOf("") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val isNameValid = fullName.trim().length >= 2
    val isNameError = (hasAttemptedSubmit && !isNameValid) || (fullName.isNotEmpty() && !isNameValid)

    val isMobileValid = mobile.length == 10
    val isMobileError = (hasAttemptedSubmit && !isMobileValid) || (mobile.isNotEmpty() && !isMobileValid && (hasAttemptedSubmit || mobile.length > 10))

    val isEmailFormatValid = remember(email) {
        email.isBlank() || email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
    val isEmailError = email.isNotBlank() && !isEmailFormatValid

    val isPasswordValid = password.length >= 6
    val isPasswordError = (hasAttemptedSubmit && !isPasswordValid) || (password.isNotEmpty() && !isPasswordValid)

    val passwordStrength = remember(password) { PasswordSecurity.calculateStrength(password) }

    Text(
        text = if (language == AppLanguage.HINDI) "नागरिक पंजीकरण" else "CITIZEN REGISTRATION",
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ElegantLavender,
        letterSpacing = 1.sp
    )
    Text(
        text = if (language == AppLanguage.HINDI) "बिहार में प्रमाणित श्रम कारीगरों को पारदर्शी दरों पर बुक करें।" else "Register to book cooperative-certified skilled artisans across Bihar with ₹0 middlemen commission.",
        fontSize = 11.sp,
        color = ElegantTextSecondary,
        lineHeight = 15.sp
    )
    Spacer(modifier = Modifier.height(14.dp))

    // Full Name
    OutlinedTextField(
        value = fullName,
        onValueChange = { fullName = it },
        isError = isNameError,
        label = { Text("Full Name *", fontSize = 12.sp) },
        leadingIcon = {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = if (isNameError) MaterialTheme.colorScheme.error else ElegantLavender,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (isNameError) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Invalid name",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            } else if (isNameValid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Valid name",
                    tint = ElegantMintGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        supportingText = if (isNameError) {
            { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid name") }
        } else null,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_cust_name"),
        colors = unifiedTextFieldColors()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Mobile Number (REQUIRED)
    OutlinedTextField(
        value = mobile,
        onValueChange = {
            if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                mobile = it
            }
        },
        isError = isMobileError,
        label = { Text("Mobile Number (10 digits) *", fontSize = 12.sp) },
        leadingIcon = {
            Text(
                "+91",
                color = if (isMobileError) MaterialTheme.colorScheme.error else ElegantLavender,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
        },
        trailingIcon = {
            if (isMobileError) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Invalid mobile",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            } else if (isMobileValid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Valid mobile",
                    tint = ElegantMintGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        supportingText = if (isMobileError) {
            { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid mobile") }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_cust_mobile"),
        colors = unifiedTextFieldColors()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Optional Email
    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        isError = isEmailError,
        label = { Text("Email Address (Optional)", fontSize = 12.sp) },
        leadingIcon = {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                tint = if (isEmailError) MaterialTheme.colorScheme.error else ElegantLavender,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (isEmailError) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Invalid email",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            } else if (email.isNotBlank() && isEmailFormatValid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Valid email",
                    tint = ElegantMintGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        supportingText = if (isEmailError) {
            { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid email") }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_cust_email"),
        colors = unifiedTextFieldColors()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Password & Strength
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        isError = isPasswordError,
        label = { Text("Create Password (min 6 characters) *", fontSize = 12.sp) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = if (isPasswordError) MaterialTheme.colorScheme.error else ElegantLavender,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPasswordError) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Invalid password",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else if (isPasswordValid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Valid password",
                        tint = ElegantMintGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (isPasswordError) MaterialTheme.colorScheme.error else ElegantTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        supportingText = if (isPasswordError) {
            { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid password") }
        } else null,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_cust_password"),
        colors = unifiedTextFieldColors()
    )

    if (password.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 0..3) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (i <= passwordStrength.score) Color(passwordStrength.colorHex) else ElegantDarkBorder
                            )
                    )
                }
            }
            Text(
                text = "Strength: ${passwordStrength.label}",
                fontSize = 10.sp,
                color = Color(passwordStrength.colorHex),
                fontWeight = FontWeight.Bold
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Google Maps GPS Location & District / Village Selector
    GoogleMapLocationSelector(
        selectedDistrict = selectedDistrict,
        villageLocality = villageLocality,
        onDistrictChange = { selectedDistrict = it },
        onVillageChange = { villageLocality = it },
        language = language
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Submit Registration Button
    Button(
        onClick = {
            hasAttemptedSubmit = true
            if (!isNameValid || !isMobileValid || !isPasswordValid || isEmailError) {
                return@Button
            }

            isSubmitting = true

            val validEmail = if (email.isNotBlank() && email.contains("@")) {
                email.trim()
            } else {
                "citizen.$mobile@sahakaarsetu.in"
            }

            onRegister(
                fullName.trim(),
                "+91 $mobile",
                validEmail,
                password,
                "Bihar",
                selectedDistrict,
                villageLocality.ifBlank { "$selectedDistrict Central" }
            )
        },
        enabled = !isSubmitting,
        colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("btn_cust_submit_reg")
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(color = ElegantOnLavender, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Creating Citizen Account...", color = ElegantOnLavender, fontWeight = FontWeight.Bold)
        } else {
            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = ElegantOnLavender, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (language == AppLanguage.HINDI) "खाता बनाएं और सहेजें" else "Create Verified Citizen Account",
                color = ElegantOnLavender,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

// -------------------------------------------------------------------------
// WORKER REGISTRATION FORM (RESPONSIVE & CLEAN)
// -------------------------------------------------------------------------

@Composable
fun WorkerRegistrationForm(
    biharDistricts: List<String>,
    cooperatives: List<CooperativeEntity>,
    professions: List<ProfessionEntity>,
    language: AppLanguage,
    onRegister: (name: String, phone: String, email: String, passwordPlain: String, state: String, district: String, village: String, profession: String, experienceYears: Int, dailyRate: Double, cooperativeId: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedDistrict by remember { mutableStateOf(biharDistricts.first()) }
    var villageLocality by remember { mutableStateOf("") }

    val defaultProfessions = listOf(
        "Electrician", "Plumber", "Carpenter", "Mason", "Painter",
        "Welder", "Mechanic", "Cleaner", "AC & Appliance Repair",
        "Construction Worker", "Technician"
    )
    val tradeList = if (professions.isNotEmpty()) professions.map { it.nameEn } else defaultProfessions
    var selectedProfession by remember { mutableStateOf(tradeList.first()) }
    var experienceYearsText by remember { mutableStateOf("4") }
    var dailyRateText by remember { mutableStateOf("550") }
    var selectedCooperativeId by remember { mutableStateOf(cooperatives.firstOrNull()?.id ?: "coop_patna") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val isNameValid = fullName.trim().length >= 2
    val isNameError = (hasAttemptedSubmit && !isNameValid) || (fullName.isNotEmpty() && !isNameValid)

    val isMobileValid = mobile.length == 10
    val isMobileError = (hasAttemptedSubmit && !isMobileValid) || (mobile.isNotEmpty() && !isMobileValid && (hasAttemptedSubmit || mobile.length > 10))

    val isEmailFormatValid = remember(email) {
        email.isBlank() || email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
    val isEmailError = email.isNotBlank() && !isEmailFormatValid

    val isPasswordValid = password.length >= 6
    val isPasswordError = (hasAttemptedSubmit && !isPasswordValid) || (password.isNotEmpty() && !isPasswordValid)

    val isExpValid = experienceYearsText.isNotBlank() && (experienceYearsText.toIntOrNull() != null) && (experienceYearsText.toIntOrNull() ?: -1) in 0..60
    val isExpError = (hasAttemptedSubmit && !isExpValid) || (experienceYearsText.isNotBlank() && !isExpValid)

    val isDailyRateValid = dailyRateText.isNotBlank() && (dailyRateText.toDoubleOrNull() != null) && (dailyRateText.toDoubleOrNull() ?: 0.0) > 0.0
    val isDailyRateError = (hasAttemptedSubmit && !isDailyRateValid) || (dailyRateText.isNotBlank() && !isDailyRateValid)

    val passwordStrength = remember(password) { PasswordSecurity.calculateStrength(password) }

    Text(
        text = if (language == AppLanguage.HINDI) "कारीगर पंजीकरण" else "SKILLED ARTISAN REGISTRATION",
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ElegantLavender,
        letterSpacing = 1.sp
    )
    Text(
        text = if (language == AppLanguage.HINDI) "बिहार श्रम सहकारिता नेटवर्क से जुड़ें - उचित मजदूरी, कल्याण कोष व शून्य बिचौलिया।" else "Join Bihar's certified cooperative labour network with fair wages, welfare wallet & direct citizen bookings.",
        fontSize = 11.sp,
        color = ElegantTextSecondary,
        lineHeight = 15.sp
    )
    Spacer(modifier = Modifier.height(14.dp))

    // Full Name
    OutlinedTextField(
        value = fullName,
        onValueChange = { fullName = it },
        isError = isNameError,
        label = { Text("Artisan Full Name *", fontSize = 12.sp) },
        leadingIcon = {
            Icon(
                Icons.Default.Engineering,
                contentDescription = null,
                tint = if (isNameError) MaterialTheme.colorScheme.error else ElegantLavender,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (isNameError) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Invalid name",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            } else if (isNameValid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Valid name",
                    tint = ElegantMintGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        supportingText = if (isNameError) {
            { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid name") }
        } else null,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_wrk_name"),
        colors = unifiedTextFieldColors()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Mobile Number (REQUIRED)
    OutlinedTextField(
        value = mobile,
        onValueChange = {
            if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                mobile = it
            }
        },
        isError = isMobileError,
        label = { Text("Mobile Number (10 digits) *", fontSize = 12.sp) },
        leadingIcon = {
            Text(
                "+91",
                color = if (isMobileError) MaterialTheme.colorScheme.error else ElegantLavender,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
        },
        trailingIcon = {
            if (isMobileError) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Invalid mobile",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            } else if (isMobileValid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Valid mobile",
                    tint = ElegantMintGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        supportingText = if (isMobileError) {
            { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid mobile") }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_wrk_mobile"),
        colors = unifiedTextFieldColors()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Optional Email
    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        isError = isEmailError,
        label = { Text("Email Address (Optional)", fontSize = 12.sp) },
        leadingIcon = {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                tint = if (isEmailError) MaterialTheme.colorScheme.error else ElegantLavender,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (isEmailError) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Invalid email",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            } else if (email.isNotBlank() && isEmailFormatValid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Valid email",
                    tint = ElegantMintGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        supportingText = if (isEmailError) {
            { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid email") }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = unifiedTextFieldColors()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Password & Strength
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        isError = isPasswordError,
        label = { Text("Create Password (min 6 characters) *", fontSize = 12.sp) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = if (isPasswordError) MaterialTheme.colorScheme.error else ElegantLavender,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPasswordError) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Invalid password",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else if (isPasswordValid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Valid password",
                        tint = ElegantMintGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (isPasswordError) MaterialTheme.colorScheme.error else ElegantTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        supportingText = if (isPasswordError) {
            { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid password") }
        } else null,
        modifier = Modifier.fillMaxWidth().testTag("input_reg_wrk_password"),
        colors = unifiedTextFieldColors()
    )

    if (password.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 0..3) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (i <= passwordStrength.score) Color(passwordStrength.colorHex) else ElegantDarkBorder
                            )
                    )
                }
            }
            Text(
                text = "Strength: ${passwordStrength.label}",
                fontSize = 10.sp,
                color = Color(passwordStrength.colorHex),
                fontWeight = FontWeight.Bold
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Profession Dropdown
    var professionDropdownExpanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedProfession,
            onValueChange = {},
            readOnly = true,
            label = { Text("Primary Trade / Skill *", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Handyman, contentDescription = null, tint = ElegantLavender, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { professionDropdownExpanded = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { professionDropdownExpanded = true },
            colors = unifiedTextFieldColors()
        )
        DropdownMenu(
            expanded = professionDropdownExpanded,
            onDismissRequest = { professionDropdownExpanded = false },
            modifier = Modifier.background(ElegantDarkSurface)
        ) {
            tradeList.forEach { trade ->
                DropdownMenuItem(
                    text = { Text(trade, color = ElegantTextWhite) },
                    onClick = {
                        selectedProfession = trade
                        professionDropdownExpanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Experience & Daily Wage Rate
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = experienceYearsText,
            onValueChange = { experienceYearsText = it },
            isError = isExpError,
            label = { Text("Exp (Years) *", fontSize = 12.sp) },
            trailingIcon = {
                if (isExpError) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Invalid experience",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (isExpValid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Valid experience",
                        tint = ElegantMintGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            supportingText = if (isExpError) {
                { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid experience") }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = unifiedTextFieldColors()
        )
        OutlinedTextField(
            value = dailyRateText,
            onValueChange = { dailyRateText = it },
            isError = isDailyRateError,
            label = { Text("Daily Wage (₹) *", fontSize = 12.sp) },
            trailingIcon = {
                if (isDailyRateError) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Invalid wage",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (isDailyRateValid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Valid wage",
                        tint = ElegantMintGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            supportingText = if (isDailyRateError) {
                { FieldSupportingErrorIcon(isError = true, contentDescription = "Invalid wage") }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = unifiedTextFieldColors()
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Cooperative Selection Dropdown
    var coopDropdownExpanded by remember { mutableStateOf(false) }
    val activeCoop = cooperatives.find { it.id == selectedCooperativeId } ?: cooperatives.firstOrNull()
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = activeCoop?.name ?: "Patna Shramik Vikas Sahakari Samiti",
            onValueChange = {},
            readOnly = true,
            label = { Text("Labour Cooperative Society *", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = ElegantLavender, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { coopDropdownExpanded = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { coopDropdownExpanded = true },
            colors = unifiedTextFieldColors()
        )
        DropdownMenu(
            expanded = coopDropdownExpanded,
            onDismissRequest = { coopDropdownExpanded = false },
            modifier = Modifier.background(ElegantDarkSurface)
        ) {
            cooperatives.forEach { coop ->
                DropdownMenuItem(
                    text = { Text("${coop.name} (${coop.district})", color = ElegantTextWhite) },
                    onClick = {
                        selectedCooperativeId = coop.id
                        coopDropdownExpanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Google Maps GPS Location & District / Village Selector
    GoogleMapLocationSelector(
        selectedDistrict = selectedDistrict,
        villageLocality = villageLocality,
        onDistrictChange = { selectedDistrict = it },
        onVillageChange = { villageLocality = it },
        language = language
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Submit Worker Registration Button
    Button(
        onClick = {
            hasAttemptedSubmit = true
            if (!isNameValid || !isMobileValid || !isPasswordValid || isEmailError || !isExpValid || !isDailyRateValid) {
                return@Button
            }

            isSubmitting = true

            val exp = experienceYearsText.toIntOrNull() ?: 3
            val rate = dailyRateText.toDoubleOrNull() ?: 500.0
            val validEmail = if (email.isNotBlank() && email.contains("@")) {
                email.trim()
            } else {
                "artisan.$mobile@sahakaarsetu.in"
            }

            onRegister(
                fullName.trim(),
                "+91 $mobile",
                validEmail,
                password,
                "Bihar",
                selectedDistrict,
                villageLocality.ifBlank { "$selectedDistrict Central" },
                selectedProfession,
                exp,
                rate,
                selectedCooperativeId
            )
        },
        enabled = !isSubmitting,
        colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("btn_wrk_submit_reg")
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(color = ElegantOnLavender, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registering Artisan...", color = ElegantOnLavender, fontWeight = FontWeight.Bold)
        } else {
            Icon(Icons.Default.Engineering, contentDescription = null, tint = ElegantOnLavender, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (language == AppLanguage.HINDI) "कारीगर खाता बनाएं और सहेजें" else "Register Verified Artisan Account",
                color = ElegantOnLavender,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
