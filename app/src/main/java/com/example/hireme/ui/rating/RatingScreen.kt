package com.example.hireme.ui.rating

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hireme.data.RepositoryProvider
import com.example.hireme.theme.*
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RatingScreen(
    requestId: String,
    onRatingSubmitted: () -> Unit,
    onBackClick: () -> Unit
) {
    val repository = RepositoryProvider.repository
    val request = remember(requestId) { repository.getRequestById(requestId) }
    
    var selectedRating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, CardBorderGray, CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Rate Service",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    color = TextDark
                )
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Service Summary Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, CardBorderGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = request?.subService?.name ?: "Service Name",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Worker: ${request?.workerName ?: "Worker"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = TextMuted
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Status: ${request?.status?.name ?: "COMPLETED"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = VibrantPink,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        
        // Star Rating
        Text(
            text = "How was your experience?",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextDark
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 1..5) {
                StarIcon(
                    filled = i <= selectedRating,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp)
                        .clickable {
                            selectedRating = i
                            showError = false
                        }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (selectedRating > 0) {
            Text(
                text = "$selectedRating out of 5 stars",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = TextMuted
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Comment Field
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = InputFieldBg,
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                if (comment.isEmpty()) {
                    Text(
                        text = "Share your experience (optional)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            color = TextMuted
                        )
                    )
                }
                BasicTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = TextBody
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        if (showError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please select a rating",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = ErrorRed
                )
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Submit Button
        var isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1.0f,
            animationSpec = tween(120), label = "scale"
        )
        
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DeepPurple,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .scale(scale)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            if (selectedRating < 1) {
                                showError = true
                            } else {
                                scope.launch {
                                    repository.submitRating(requestId, selectedRating, comment)
                                    onRatingSubmitted()
                                }
                            }
                        }
                    )
                }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Submit Rating",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StarIcon(filled: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path()
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.width / 2f
        val innerRadius = outerRadius / 2.5f
        val numPoints = 5

        var angle = -PI / 2.0 // Start at top
        val angleIncrement = PI / numPoints

        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = center.x + r * cos(angle).toFloat()
            val y = center.y + r * sin(angle).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            angle += angleIncrement
        }
        path.close()

        if (filled) {
            drawPath(path = path, color = VibrantPink, style = Fill)
        } else {
            drawPath(path = path, color = CardBorderGray, style = Fill)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RatingScreenPreview() {
    MaterialTheme {
        RatingScreen(
            requestId = "req_1",
            onRatingSubmitted = {},
            onBackClick = {}
        )
    }
}
