package com.example.hireme.ui.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.hireme.R
import com.example.hireme.data.RepositoryProvider
import com.example.hireme.data.UserRole
import com.example.hireme.data.network.AuthException
import com.example.hireme.data.network.ServerUnreachableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.hireme.theme.BorderGray
import com.example.hireme.theme.DeepPurple
import com.example.hireme.theme.HireMeTheme
import com.example.hireme.theme.InputFieldBg
import com.example.hireme.theme.PageBackground
import com.example.hireme.theme.PureWhite
import com.example.hireme.theme.TextDark
import com.example.hireme.theme.TextMuted
import com.example.hireme.theme.VibrantPink

/**
 * Login Screen matching Screen 2 in the user's reference image:
 * - Light background
 * - Back Arrow (←)
 * - "Welcome Back" title & "Log in to continue" subtitle
 * - "Email or Phone Number" & "Password" input fields
 * - "Forgot Password?" right-aligned red link
 * - Deep Purple "Log In" Button
 * - Social Login Circles (Google multicolor, Apple black, Facebook blue #1877F2)
 * - "Sign up for HireMe" footer link
 */
@Composable
fun LoginScreen(
    onLoginClick: (role: UserRole) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onBackClick: () -> Unit = onRegisterClick,
    modifier: Modifier = Modifier
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun performLogin() {
        focusManager.clearFocus()
        if (isLoading) return
        if (emailOrPhone.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both credentials."
            return
        }

        errorMessage = null
        coroutineScope.launch {
            isLoading = true
            try {
                val user = withContext(Dispatchers.IO) {
                    RepositoryProvider.repository.login(emailOrPhone.trim(), password, selectedRole)
                }
                onLoginClick(user.role)
            } catch (e: ServerUnreachableException) {
                val msg = e.message ?: "Cannot connect to server. The server is unreachable at the moment."
                errorMessage = msg
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            } catch (e: AuthException) {
                errorMessage = e.message ?: "Invalid email or password."
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "loginBtnScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header Column
            Column {
                // Title & Subtitle
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    ),
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Log in to continue",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Role Toggle: Customer / Worker
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

                Spacer(modifier = Modifier.height(20.dp))

                // Email or Phone Input Field
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

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input Field
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
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            performLogin()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Forgot Password Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = VibrantPink,
                        modifier = Modifier.clickable(onClick = onForgotPasswordClick)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
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
                            Text(
                                text = "⚠️",
                                fontSize = 16.sp
                            )
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

                Spacer(modifier = Modifier.height(28.dp))

                // Deep Purple Log In Button
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
                            onClick = { performLogin() }
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
                                text = "Log In",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = PureWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Social Logins Row matching uploaded image (Google, Apple, Facebook #1877F2)
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
            }

            // Footer Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                // "Back" text on bottom left to return to Landing Page
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = DeepPurple,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable(onClick = onBackClick)
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                )

                // "Sign up for HireMe" centered
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = "Sign up for ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextDark
                    )
                    Text(
                        text = "HireMe",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = VibrantPink,
                        modifier = Modifier.clickable(onClick = onRegisterClick)
                    )
                }
            }
        }
    }
}

/**
 * Clean Light Mode Input Field.
 */
@Composable
fun CleanLightInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = InputFieldBg,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(0xFF1E293B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    cursorBrush = SolidColor(DeepPurple),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isPassword && onTogglePasswordVisibility != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPasswordVisible) "Hide" else "Show",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = Color(0xFF64748B),
                    modifier = Modifier.clickable(onClick = onTogglePasswordVisibility)
                )
            }
        }
    }
}

enum class SocialType { GOOGLE, APPLE, FACEBOOK }

/**
 * Social Circle Button matching uploaded reference image:
 * - Google: Multicolor 'G' in white circle with border
 * - Apple: Solid Black Apple logo in white circle with border
 * - Facebook: Solid Blue circle (#1877F2) with white 'f'
 */
@Composable
fun SocialCircleButton(type: SocialType) {
    val bgColor = when (type) {
        SocialType.GOOGLE, SocialType.APPLE -> PureWhite
        SocialType.FACEBOOK -> Color(0xFF1877F2) // Official Facebook Blue
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (type == SocialType.FACEBOOK) 0.dp else 1.dp,
                color = BorderGray,
                shape = CircleShape
            )
            .clickable { }
    ) {
        when (type) {
            SocialType.GOOGLE -> {
                Image(painterResource(R.drawable.ic_google), "Google", Modifier.size(24.dp))
            }
            SocialType.APPLE -> {
                Image(painterResource(R.drawable.ic_apple), "Apple", Modifier.size(24.dp))
            }
            SocialType.FACEBOOK -> {
                Image(painterResource(R.drawable.ic_facebook), "Facebook", Modifier.size(24.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 780)
@Composable
fun LoginScreenReferencePreview() {
    HireMeTheme {
        LoginScreen(onLoginClick = {}, onRegisterClick = {})
    }
}
