package com.example.hireme.ui.subservice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hireme.R
import com.example.hireme.data.RepositoryProvider
import com.example.hireme.data.SubService
import com.example.hireme.theme.*

@Composable
fun SubServiceScreen(
    categoryId: String,
    categoryName: String,
    onSubServiceClick: (SubService) -> Unit,
    onBackClick: () -> Unit
) {
    val allSubServices by RepositoryProvider.repository.getSubServices(categoryId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filterChips = remember { listOf("All", "Popular", "Deep Clean", "Quick") }

    // Filter subservices based on search query and selected filter tab
    val filteredServices = remember(allSubServices, searchQuery, selectedFilter) {
        allSubServices.filter { service ->
            val matchesSearch = searchQuery.isBlank() ||
                service.name.contains(searchQuery, ignoreCase = true) ||
                service.description.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "Popular" -> service.basePrice >= 499
                "Deep Clean" -> service.name.contains("Deep", ignoreCase = true) || service.name.contains("Complete", ignoreCase = true)
                "Quick" -> service.basePrice <= 499 || service.duration.contains("1")
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .navigationBarsPadding()
    ) {
        // Dynamic Hero Header with Image 2 (banner_background.png) Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Header Image 2 background
            Image(
                painter = painterResource(id = R.drawable.banner_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Top Row: Back Button & Category Title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PureWhite)
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "←",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                color = PureWhite
                            )
                        )
                        Text(
                            text = "${allSubServices.size} services available",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = PureWhite.copy(alpha = 0.85f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Search Bar Input Inside Header
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PureWhite,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔍", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search ${categoryName.lowercase()} services...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPlaceholder,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextDark,
                                    fontSize = 14.sp
                                ),
                                singleLine = true
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "✕",
                                fontSize = 16.sp,
                                color = TextMuted,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { searchQuery = "" }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Horizontal Category Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterChips) { chip ->
                        val isSelected = chip == selectedFilter
                        val chipBg = if (isSelected) PureWhite else PureWhite.copy(alpha = 0.25f)
                        val chipTextColor = if (isSelected) DeepPurple else PureWhite

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(chipBg)
                                .clickable { selectedFilter = chip }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = chip,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipTextColor
                            )
                        }
                    }
                }
            }
        }

        // Sub-Services List
        if (filteredServices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching services found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching with a different term or clear filters.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredServices) { subService ->
                    SubServiceCard(
                        subService = subService,
                        categoryName = categoryName,
                        onClick = { onSubServiceClick(subService) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubServiceCard(
    subService: SubService,
    categoryName: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(120),
        label = "cardScale"
    )

    // Select dynamic icon emoji based on category or service name
    val serviceIcon = remember(subService.name, categoryName) {
        val nameLower = subService.name.lowercase()
        when {
            nameLower.contains("deep") || nameLower.contains("clean") -> "✨"
            nameLower.contains("kitchen") -> "🍳"
            nameLower.contains("bathroom") -> "🚿"
            nameLower.contains("ac") || nameLower.contains("cool") -> "❄️"
            nameLower.contains("water") || nameLower.contains("leak") -> "💧"
            nameLower.contains("wire") || nameLower.contains("electric") || nameLower.contains("switch") -> "⚡"
            nameLower.contains("hair") || nameLower.contains("beauty") -> "💇"
            nameLower.contains("cook") -> "👨‍🍳"
            nameLower.contains("driver") -> "🚗"
            else -> "🛠️"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, CardBorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Service Vector Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(LightPurple.copy(alpha = 0.15f), VibrantPink.copy(alpha = 0.15f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = serviceIcon, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Rating & Duration Badge Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "★", fontSize = 11.sp, color = Color(0xFFD97706))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${subService.rating}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "⏱ ${subService.duration}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subService.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 17.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subService.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Price & Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Base Price",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₹${subService.basePrice}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepPurple,
                            fontSize = 20.sp
                        )
                    )
                }

                // Gradient "Book Now →" Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(DeepPurple, VibrantPink)
                            )
                        )
                        .clickable { onClick() }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Book Now →",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PureWhite
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubServiceScreenPreview() {
    SubServiceScreen(
        categoryId = "cat_1",
        categoryName = "Cleaning",
        onSubServiceClick = {},
        onBackClick = {}
    )
}
