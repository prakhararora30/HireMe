package com.example.hireme.ui.payment

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
import androidx.compose.material3.*
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
import com.example.hireme.theme.*

@Composable
fun PaymentScreen(
    requestId: String,
    onPaymentComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val repository = RepositoryProvider.repository
    val request = remember(requestId) { repository.getRequestById(requestId) }
    
    var paymentDone by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .clickable { if (!paymentDone) onBackClick() },
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
                text = "Payment",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
            )
        }
        
        if (paymentDone) {
            // Success State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓", 
                        color = PureWhite, 
                        fontSize = 48.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Payment Successful!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.96f else 1.0f,
                    animationSpec = tween(120),
                    label = "scale"
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scale(scale),
                    shape = RoundedCornerShape(14.dp),
                    color = VibrantPink,
                    onClick = onPaymentComplete,
                    interactionSource = interactionSource
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Back to Home",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        )
                    }
                }
            }
        } else {
            // Payment Form
            if (request != null) {
                val basePrice = request.subService.basePrice
                val platformFee = 49
                val total = basePrice + platformFee
                
                var selectedMethod by remember { mutableStateOf("UPI") }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    // Service summary card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = PureWhite,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, CardBorderGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Service Summary",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = request.subService.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                )
                                Text(
                                    text = "₹$basePrice",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepPurple
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Payment breakdown card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = PureWhite,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, CardBorderGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Service charge", color = TextBody)
                                Text(text = "₹$basePrice", color = TextDark)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Platform fee", color = TextBody)
                                Text(text = "₹$platformFee", color = TextDark)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = BorderGray, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                )
                                Text(
                                    text = "₹$total",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VibrantPink
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "Select Payment Method",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Payment method selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val methods = listOf("UPI", "Card", "Wallet")
                        methods.forEach { method ->
                            val isSelected = selectedMethod == method
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = if (isSelected) DeepPurple else PureWhite,
                                border = if (isSelected) null else BorderStroke(1.dp, DeepPurple),
                                onClick = { selectedMethod = method }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = method,
                                        color = if (isSelected) PureWhite else DeepPurple,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = "Pay to HireMe",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.96f else 1.0f,
                        animationSpec = tween(120),
                        label = "scale"
                    )
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .scale(scale)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = VibrantPink,
                        onClick = {
                            repository.makePayment(requestId, selectedMethod)
                            paymentDone = true
                        },
                        interactionSource = interactionSource
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Pay ₹$total",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading...")
                }
            }
        }
    }
}

@Preview
@Composable
fun PaymentScreenPreview() {
    PaymentScreen(
        requestId = "1",
        onPaymentComplete = {},
        onBackClick = {}
    )
}
