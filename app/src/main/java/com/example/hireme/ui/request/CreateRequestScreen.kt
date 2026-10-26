package com.example.hireme.ui.request

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hireme.data.RepositoryProvider
import com.example.hireme.data.SubService
import com.example.hireme.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CreateRequestScreen(
    subServiceId: String,
    categoryId: String,
    onRequestCreated: (requestId: String) -> Unit,
    onBackClick: () -> Unit
) {
    val repository = RepositoryProvider.repository
    val subServicesList by repository.getSubServices(categoryId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val subService: SubService? = subServicesList.find { it.id == subServiceId }

    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        // Top section
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Transparent, CircleShape)
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Book Service",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp
            ),
            color = TextDark
        )
        Text(
            text = "Fill in the details below",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Selected service info card
        if (subService != null) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = PureWhite,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, CardBorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = subService.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${subService.basePrice}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepPurple
                    )
                }
            }
        } else {
            Text(text = "Service not found", color = ErrorRed)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Input Fields
        Text(
            text = "Address",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDark,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = InputFieldBg,
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            BasicTextField(
                value = address,
                onValueChange = {
                    address = it
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Additional Notes / Description",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDark,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = InputFieldBg,
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            BasicTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = ErrorRed,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Submit Button
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1.0f,
            animationSpec = tween(120),
            label = "button_scale"
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DeepPurple,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = subService != null
                ) {
                    if (address.isBlank()) {
                        errorMessage = "Address cannot be empty"
                    } else if (subService != null) {
                        try {
                            val request = repository.createRequest(subService, address, description)
                            onRequestCreated(request.id)
                        } catch (e: Exception) {
                            errorMessage = "Failed to create request"
                        }
                    }
                }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Submit Request",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = PureWhite
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateRequestScreenPreview() {
    CreateRequestScreen(
        subServiceId = "sub_1",
        categoryId = "cat_1",
        onRequestCreated = {},
        onBackClick = {}
    )
}
