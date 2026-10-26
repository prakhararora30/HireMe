package com.example.hireme.ui.auth

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hireme.R
import com.example.hireme.data.RepositoryProvider
import com.example.hireme.data.UserRole
import com.example.hireme.data.network.AuthException
import com.example.hireme.data.network.ServerUnreachableException
import androidx.compose.foundation.border
import com.example.hireme.theme.BorderGray
import com.example.hireme.theme.DeepPurple
import com.example.hireme.theme.HireMeTheme
import com.example.hireme.theme.InputFieldBg
import com.example.hireme.theme.PureWhite
import com.example.hireme.theme.TextDark
import com.example.hireme.theme.TextMuted
import com.example.hireme.theme.TextSubtle
import com.example.hireme.theme.VibrantPink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Register Screen - Two-Tone Layout matching the exact design language of Image 1 (LoginScreen):
 * - Top Header: Purple gradient background illustration with clean logo, "Get Started" & "Join our community".
 * - Bottom Sheet: Pure White surface with rounded top corners.
 * - Role Toggle: Customer vs Worker segmented pill flush 14dp matching LoginScreen.
 * - Input Fields: Clean light fields (#F1F5F9) matching CleanLightInputField in LoginScreen.
 * - Password Fields: Text "Show"/"Hide" toggle matching LoginScreen.
 * - Checkbox: "I agree to the Terms of Service & Privacy Policy".
 * - Sign Up CTA: DeepPurple pill button matching LoginScreen.
 * - Social Login: Google, Apple, and Facebook (solid #1877F2 blue) identical to LoginScreen.
 * - Footer: "Back" on bottom left and "Already have an account? Log In" centered.
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: (fullName: String, email: String, role: UserRole) -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit = onLoginClick,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var acceptTerms by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun performRegister() {
        focusManager.clearFocus()
        if (isLoading) return
        validateAndSubmit(fullName, emailOrPhone, password, confirmPassword, acceptTerms) { err ->
            if (err != null) {
                errorMessage = err
                return@validateAndSubmit
            }

            errorMessage = null
            coroutineScope.launch {
                isLoading = true
                try {
                    val user = withContext(Dispatchers.IO) {
                        RepositoryProvider.repository.register(fullName.trim(), emailOrPhone.trim(), password, selectedRole)
                    }
                    onRegisterSuccess(fullName, emailOrPhone, user.role)
                } catch (e: ServerUnreachableException) {
                    val msg = e.message ?: "Cannot connect to server. The server is unreachable at the moment."
                    errorMessage = msg
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } catch (e: AuthException) {
                    errorMessage = e.message ?: "Registration failed."
                } catch (e: Exception) {
                    val isNetworkError = e is java.io.IOException ||
                            e.message?.contains("connect", ignoreCase = true) == true ||
                            e.message?.contains("unreachable", ignoreCase = true) == true ||
                            e.message?.contains("timeout", ignoreCase = true) == true

                    val msg = if (isNetworkError) {
                        "Cannot connect to server. The server is unreachable at the moment."
                    } else {
                        e.message ?: "Cannot connect to server. The server is unreachable at the moment."
                    }
                    errorMessage = msg
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "signUpBtnScale"
    )

    var headerHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val overlapDp = 28.dp
    val topPaddingDp = with(density) {
        if (headerHeightPx > 0) (headerHeightPx.toDp() - overlapDp).coerceAtLeast(0.dp) else 164.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        // Top Header: Image 2 as background for this header section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .onGloballyPositioned { coordinates ->
                    headerHeightPx = coordinates.size.height
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_register),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.matchParentSize()
            )

            // Header Content: Logo + "Get Started" + "Join our community"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 44.dp, start = 24.dp, end = 24.dp)
            ) {
                // Single clean rounded-corner box for the logo
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PureWhite,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_clean),
                            contentDescription = "HireMe Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp
                    ),
                    color = PureWhite
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Join our community",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = PureWhite.copy(alpha = 0.85f)
                )
            }
        }

        // Bottom Curved White Sheet
        Surface(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = PureWhite,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = topPaddingDp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .navigationBarsPadding()
            ) {
                // Role Toggle: Customer / Worker (Exact match to LoginScreen)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = InputFieldBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, BorderGray, RoundedCornerShape(14.dp))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selectedRole == UserRole.CUSTOMER) DeepPurple else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable { selectedRole = UserRole.CUSTOMER }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Customer",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = if (selectedRole == UserRole.CUSTOMER) PureWhite else TextMuted
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selectedRole == UserRole.WORKER) DeepPurple else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable { selectedRole = UserRole.WORKER }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Worker",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = if (selectedRole == UserRole.WORKER) PureWhite else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Field 1: Full Name (CleanLightInputField matching LoginScreen)
                CleanLightInputField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = null
                    },
                    placeholder = "Full Name",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 2: Email or Phone (CleanLightInputField matching LoginScreen)
                CleanLightInputField(
                    value = emailOrPhone,
                    onValueChange = {
                        emailOrPhone = it
                        errorMessage = null
                    },
                    placeholder = "Email or Phone Number",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 3: Password (with Show/Hide Toggle matching LoginScreen)
                CleanLightInputField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    placeholder = "Password",
                    isPassword = true,
                    isPasswordVisible = passwordVisible,
                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 4: Confirm Password (with Show/Hide Toggle matching LoginScreen)
                CleanLightInputField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    placeholder = "Confirm Password",
                    isPassword = true,
                    isPasswordVisible = confirmPasswordVisible,
                    onTogglePasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { performRegister() }
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Terms & Privacy Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = { acceptTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = DeepPurple,
                            checkmarkColor = PureWhite,
                            uncheckedColor = BorderGray
                        ),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "I agree to the ",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextSubtle
                        )
                        Text(
                            text = "Terms of Service",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            ),
                            color = DeepPurple
                        )
                        Text(
                            text = " & ",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextSubtle
                        )
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            ),
                            color = VibrantPink
                        )
                    }
                }

                // Error Message Banner
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(text = "⚠️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 18.sp
                                ),
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sign Up Button matching LoginScreen's DeepPurple button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isLoading) DeepPurple.copy(alpha = 0.7f) else DeepPurple,
                    shadowElevation = if (isLoading) 1.dp else 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scale(buttonScale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = !isLoading,
                            onClick = { performRegister() }
                        )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = PureWhite,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Sign Up",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = PureWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Social Sign Up Row matching LoginScreen (Google, Apple, Facebook solid #1877F2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialCircleButton(type = SocialType.GOOGLE)
                    Spacer(modifier = Modifier.width(20.dp))
                    SocialCircleButton(type = SocialType.APPLE)
                    Spacer(modifier = Modifier.width(20.dp))
                    SocialCircleButton(type = SocialType.FACEBOOK)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Footer: "Already have an account? Log In"
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(onClick = onLoginClick)
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = TextDark
                    )
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = VibrantPink
                    )
                }
            }
        }
    }
}

private fun validateAndSubmit(
    fullName: String,
    email: String,
    pass: String,
    confirmPass: String,
    terms: Boolean,
    onResult: (String?) -> Unit
) {
    if (fullName.isBlank()) {
        onResult("Please enter your full name.")
    } else if (email.isBlank()) {
        onResult("Please enter your email or phone number.")
    } else if (pass.isBlank()) {
        onResult("Please enter a password.")
    } else if (pass != confirmPass) {
        onResult("Passwords do not match.")
    } else if (!terms) {
        onResult("Please accept the terms & privacy policy.")
    } else {
        onResult(null)
    }
}



@Preview(showBackground = true, widthDp = 380, heightDp = 780)
@Composable
fun RegisterScreenOption2Preview() {
    HireMeTheme {
        RegisterScreen(onRegisterSuccess = { _, _, _ -> }, onLoginClick = {})
    }
}
