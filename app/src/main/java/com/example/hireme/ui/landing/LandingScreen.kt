package com.example.hireme.ui.landing

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hireme.R
import com.example.hireme.theme.DeepPurple
import com.example.hireme.theme.HireMeTheme
import com.example.hireme.theme.PageBackground
import com.example.hireme.theme.PureWhite
import com.example.hireme.theme.TextBody
import com.example.hireme.theme.VibrantPink

/**
 * Landing Screen matching the user's reference image:
 * - Pure light background (#FFFFFF)
 * - House Roof & Handshake Artwork (Purple + Pink)
 * - "HireMe" title + "Trusted help, right when you need it." subtitle
 * - Full-width Vibrant Pink "Get Started" Pill Button
 */
@Composable
fun LandingScreen(
    onGetStartedClick: () -> Unit,
    onSignInClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "btnScale"
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
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Center Logo & Artwork Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_clean),
                    contentDescription = "HireMe Logo",
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Brand Name "HireMe"
                Text(
                    text = "HireMe",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 42.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = DeepPurple,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle
                Text(
                    text = "Trusted help, right when\nyou need it.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    ),
                    color = TextBody,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Wide Pink "Get Started" Button
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = VibrantPink,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(buttonScale)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onGetStartedClick
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = PureWhite
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 780)
@Composable
fun LandingScreenReferencePreview() {
    HireMeTheme {
        LandingScreen(onGetStartedClick = {})
    }
}
