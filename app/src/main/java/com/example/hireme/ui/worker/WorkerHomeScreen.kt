package com.example.hireme.ui.worker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hireme.data.RepositoryProvider
import com.example.hireme.data.RequestStatus
import com.example.hireme.data.ServiceRequest
import com.example.hireme.theme.*

@Composable
fun WorkerHomeScreen(
    onLogout: () -> Unit = {}
) {
    val repository = RepositoryProvider.repository
    val user = repository.getCurrentUser()
    val workerName = user?.name ?: "Worker"

    var isAccepting by remember { mutableStateOf(false) }

    val pendingRequests by repository.getPendingRequests().collectAsStateWithLifecycle(initialValue = emptyList())
    val myRequests by repository.getMyWorkerRequests().collectAsStateWithLifecycle(initialValue = emptyList())
    
    val activeJobs = myRequests.filter { it.status == RequestStatus.ACCEPTED }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = PageBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(LightPurple, MagentaGradient, VibrantPink)
                        )
                    )
                }
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Hello, $workerName!",
                        color = PureWhite,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Worker Dashboard",
                        color = PureWhite,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage your service requests",
                        color = PureWhite.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, CardBorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Accept Requests",
                        color = TextDark,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = isAccepting,
                        onCheckedChange = { isAccepting = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isAccepting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Toggle on to see available requests",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (activeJobs.isNotEmpty()) {
                        item {
                            Text(
                                text = "My Active Jobs",
                                color = TextDark,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        items(activeJobs) { request ->
                            RequestCard(
                                request = request,
                                buttonText = "Mark Complete",
                                buttonColor = VibrantPink,
                                onClick = {
                                    repository.completeRequest(request.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    item {
                        Text(
                            text = "Available Requests",
                            color = TextDark,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    if (pendingRequests.isEmpty()) {
                        item {
                            Text(
                                text = "No available requests at the moment.",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(pendingRequests) { request ->
                            RequestCard(
                                request = request,
                                buttonText = "Accept",
                                buttonColor = Color(0xFF10B981),
                                onClick = {
                                    repository.acceptRequest(request.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    request: ServiceRequest,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CardBorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = request.subService.name,
                color = TextDark,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Customer: ${request.customerName}",
                color = TextBody,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Address: ${request.address}",
                color = TextBody,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${request.subService.basePrice}",
                color = DeepPurple,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedButton(
                text = buttonText,
                color = buttonColor,
                onClick = onClick
            )
        }
    }
}

@Composable
fun AnimatedButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(120),
        label = "ButtonScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = color
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = PureWhite,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview
@Composable
fun WorkerHomeScreenPreview() {
    WorkerHomeScreen()
}
