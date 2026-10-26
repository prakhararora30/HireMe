package com.example.hireme.ui.request

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hireme.data.RepositoryProvider
import com.example.hireme.theme.*

@Composable
fun RequestConfirmationScreen(
    requestId: String,
    onBackToHome: () -> Unit
) {
    val repository = RepositoryProvider.repository
    val request = repository.getRequestById(requestId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Green Checkmark
        Canvas(modifier = Modifier.size(80.dp)) {
            drawCircle(color = Color(0xFF10B981))
            val path = Path().apply {
                moveTo(size.width * 0.25f, size.height * 0.5f)
                lineTo(size.width * 0.45f, size.height * 0.7f)
                lineTo(size.width * 0.75f, size.height * 0.3f)
            }
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(
                    width = 6.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Request Submitted!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = TextDark
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your request has been sent to nearby workers",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Request Summary Card
        if (request != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, CardBorderGray)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Service Request",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = request.subService.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepPurple,
                            fontSize = 18.sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Address",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = request.address,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextBody,
                            fontSize = 14.sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VibrantPink.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, VibrantPink)
                    ) {
                        Text(
                            text = "Waiting for Worker",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = VibrantPink,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Back to Home Button
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1.0f,
            animationSpec = tween(120),
            label = "buttonScale"
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onBackToHome
                ),
            shape = RoundedCornerShape(14.dp),
            color = DeepPurple
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Back to Home",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PureWhite
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RequestConfirmationScreenPreview() {
    RequestConfirmationScreen(
        requestId = "preview-id",
        onBackToHome = {}
    )
}
