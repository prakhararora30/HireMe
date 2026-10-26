package com.example.hireme.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.widget.Toast
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.hireme.util.LocationHelper
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hireme.R
import com.example.hireme.data.CategoryIds
import com.example.hireme.data.RepositoryProvider
import com.example.hireme.data.RequestStatus
import com.example.hireme.data.ServiceRequest
import com.example.hireme.theme.CardBorderGray
import com.example.hireme.theme.DeepPurple
import com.example.hireme.theme.HireMeTheme
import com.example.hireme.theme.PureWhite
import com.example.hireme.theme.TextDark
import com.example.hireme.theme.TextInput
import com.example.hireme.theme.TextMuted
import com.example.hireme.theme.TextPlaceholder
import com.example.hireme.theme.VibrantPink
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.rememberCoroutineScope
import com.example.hireme.data.SubService
import com.example.hireme.data.UserRole
import kotlinx.coroutines.launch

data class HomeCategoryItem(
    val id: String,
    val title: String,
    val iconType: HomeCategoryIconType
)

enum class HomeCategoryIconType {
    HOME_CARE,
    BEAUTY,
    HEALTH_CARE,
    APPLIANCES,
    ELECTRIC,
    PLUMBING,
    AUTOMOTIVE,
    FURNITURE,
    INSTALLATION
}

data class RecommendationItem(
    val id: String,
    val title: String,
    val categoryId: String,
    val provider: String = "Provider",
    val rating: Float,
    val price: Int,
    val type: RecommendationType
)

enum class RecommendationType {
    CLEANING,
    PLUMBING,
    ELECTRICIAN,
    PAINTING
}

data class ChatItem(
    val id: String,
    val name: String,
    val message: String,
    val time: String,
    val unreadCount: Int = 0,
    val isUnreadBadge: Boolean = false,
    val isOnline: Boolean = false,
    val isHighlighted: Boolean = false,
    val avatarColor: Color,
    val avatarInitial: String
)

/**
 * Main HireMe Screen:
 * - Tab 0 (Home): Modern dashboard with "Hello, Ayesha!", Create/Accept Request pill toggle,
 *   Recommendations carousel, and Recent Chats matching the uploaded reference image.
 * - Tab 1 (Categories): Full 9-category service grid with Image 2 gradient header.
 * - Tab 2 (Messages): Chat messages screen.
 * - Tab 3 (Profile): User profile screen.
 * - 4-Item Bottom Navigation Bar matching the reference: Home, Categories, Messages, Profile.
 */
@Composable
fun HomeScreen(
    onServiceClick: (HomeCategoryItem) -> Unit = {},
    onSubServiceClick: (SubService) -> Unit = {},
    onRequestClick: (ServiceRequest) -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Categories, 2: Messages, 3: Profile
    var selectedLocation by remember { mutableStateOf("Ghaziabad, Sector 12") }

    val categories = remember {
        listOf(
            HomeCategoryItem(CategoryIds.HOME_CARE, "Home Care", HomeCategoryIconType.HOME_CARE),
            HomeCategoryItem(CategoryIds.BEAUTY, "Beauty", HomeCategoryIconType.BEAUTY),
            HomeCategoryItem(CategoryIds.HEALTH_CARE, "Health Care", HomeCategoryIconType.HEALTH_CARE),
            HomeCategoryItem(CategoryIds.APPLIANCES, "Appliances", HomeCategoryIconType.APPLIANCES),
            HomeCategoryItem(CategoryIds.ELECTRIC, "Electric", HomeCategoryIconType.ELECTRIC),
            HomeCategoryItem(CategoryIds.PLUMBING, "Plumbing", HomeCategoryIconType.PLUMBING),
            HomeCategoryItem(CategoryIds.AUTOMOTIVE, "Automotive", HomeCategoryIconType.AUTOMOTIVE),
            HomeCategoryItem(CategoryIds.FURNITURE, "Furniture", HomeCategoryIconType.FURNITURE),
            HomeCategoryItem(CategoryIds.INSTALLATION, "Installation", HomeCategoryIconType.INSTALLATION)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC))
    ) {
        // Tab Content
        when (selectedTab) {
            0 -> HomeDashboardView(
                selectedLocation = selectedLocation,
                onLocationChanged = { selectedLocation = it },
                onServiceClick = { catId, catTitle ->
                    val item = categories.find { it.id == catId } ?: categories.first()
                    onServiceClick(item)
                },
                onSubServiceClick = onSubServiceClick,
                onRequestClick = onRequestClick
            )
            1 -> NotificationsScreenView()
            2 -> CategoriesGridView(
                categories = categories,
                selectedLocation = selectedLocation,
                onLocationChanged = { selectedLocation = it },
                onServiceClick = onServiceClick,
                onRequestClick = onRequestClick
            )
            3 -> MessagesScreenView()
            4 -> ProfileScreenView(onLogout = onLogout)
        }

        // 4-Item Bottom Navigation Bar matching reference
        HireMeBottomNav(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. HOME DASHBOARD VIEW (Matching media_1788963081808.png)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeDashboardView(
    selectedLocation: String = "Ghaziabad, Sector 12",
    onLocationChanged: (String) -> Unit = {},
    onServiceClick: (categoryId: String, title: String) -> Unit,
    onSubServiceClick: (SubService) -> Unit = {},
    onRequestClick: (ServiceRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = RepositoryProvider.repository
    val currentUser = repository.getCurrentUser()
    val isWorker = currentUser?.role == UserRole.WORKER
    val userName = remember(currentUser) {
        val raw = currentUser?.name?.trim() ?: ""
        if (raw.isBlank() || raw.all { it.isDigit() } || raw.startsWith("+")) {
            "Prakhar"
        } else {
            raw
        }
    }

    // Two-way role mode: Customer -> Create Request (true), Worker -> Accept Request (false)
    var isCreateMode by remember(isWorker) { mutableStateOf(!isWorker) }
    val coroutineScope = rememberCoroutineScope()

    // Customer active requests
    val myRequests by repository.getMyRequests().collectAsStateWithLifecycle(initialValue = emptyList())
    val activeRequests = myRequests.filter { it.status != RequestStatus.PAID }

    // Worker requests: Pending requests to accept & Active jobs
    val pendingRequests by repository.getPendingRequests().collectAsStateWithLifecycle(initialValue = emptyList())
    val myWorkerRequests by repository.getMyWorkerRequests().collectAsStateWithLifecycle(initialValue = emptyList())
    val activeJobs = myWorkerRequests.filter { it.status == RequestStatus.ACCEPTED || it.status == RequestStatus.IN_PROGRESS }

    // SubServices fetched dynamically from MongoDB Database (Image 4 attribute: name)
    val dbSubServices by repository.getAllSubServices().collectAsStateWithLifecycle(initialValue = emptyList())
    val displaySubServices = remember(dbSubServices) {
        if (dbSubServices.isNotEmpty()) {
            dbSubServices
        } else {
            listOf(
                SubService("hc_1", CategoryIds.HOME_CARE, "Deep Cleaning", "Full home deep-clean for rooms, floors, windows & balcony", 1499),
                SubService("hc_2", CategoryIds.HOME_CARE, "Kitchen Cleaning", "Degreasing cabinets, chimney, gas stove, sink & wall tiles", 799),
                SubService("hc_3", CategoryIds.HOME_CARE, "Bathroom Cleaning", "Tile stain removal, commode scrubbing, fixtures & sanitization", 499),
                SubService("app_1", CategoryIds.APPLIANCES, "AC Service & Repair", "Jet pump service, gas leak check & cooling problem fix", 699),
                SubService("elec_1", CategoryIds.ELECTRIC, "Wiring & Cabling", "Internal wiring repair, extension boards & safety check", 599),
                SubService("auto_2", CategoryIds.AUTOMOTIVE, "Complete Car Wash", "High-pressure foam exterior wash & vacuum", 699)
            )
        }
    }

    val recentChats = remember {
        listOf(
            ChatItem(
                id = "c1",
                name = "Ayesha Khan",
                message = "Most you are message...",
                time = "1:42 PM",
                unreadCount = 2,
                avatarColor = Color(0xFFFED7AA),
                avatarInitial = "A"
            ),
            ChatItem(
                id = "c2",
                name = "Rapena Rahard",
                message = "Yans, in you message...",
                time = "3:42 PM",
                isUnreadBadge = true,
                isOnline = true,
                isHighlighted = true,
                avatarColor = Color(0xFFD1FAE5),
                avatarInitial = "R"
            ),
            ChatItem(
                id = "c3",
                name = "Roohia Mizhn",
                message = "Head your message...",
                time = "3:45 PM",
                isOnline = false,
                avatarColor = Color(0xFFFEF08A),
                avatarInitial = "R"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // ── SECTION 1 (Image 1): Top App Bar, Greeting & Dual Mode Switch ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 10.dp)
        ) {
            // Hamburger Menu Icon in a soft rounded container matching reference
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF0F4F8))
                    .clickable { }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_menu_tiered),
                    contentDescription = "Menu",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Location Selector (1-tap GPS / Fused Location + Dropdown)
            val context = LocalContext.current
            var showLocationMenu by remember { mutableStateOf(false) }
            var isLocating by remember { mutableStateOf(false) }

            // UI Safety Net: Automatically reset loading spinner if it ever exceeds 5.5s
            LaunchedEffect(isLocating) {
                if (isLocating) {
                    delay(5500L)
                    isLocating = false
                }
            }

            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (fineGranted || coarseGranted) {
                    isLocating = true
                    LocationHelper.fetchCurrentLocation(context) { result ->
                        isLocating = false
                        result.onSuccess { loc ->
                            onLocationChanged(loc.formattedText)
                            Toast.makeText(context, "📍 ${loc.formattedText}", Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            Toast.makeText(context, "⚠️ ${err.localizedMessage ?: "Could not detect location"}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "⚠️ Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            val requestLocation: () -> Unit = {
                if (LocationHelper.hasLocationPermission(context)) {
                    isLocating = true
                    LocationHelper.fetchCurrentLocation(context) { result ->
                        isLocating = false
                        result.onSuccess { loc ->
                            onLocationChanged(loc.formattedText)
                            Toast.makeText(context, "📍 ${loc.formattedText}", Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            Toast.makeText(context, "⚠️ ${err.localizedMessage ?: "Could not detect location"}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFF0F4F8))
                        .clickable { showLocationMenu = !showLocationMenu }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color = DeepPurple
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_pushpin_pink),
                            contentDescription = "Location Pin",
                            modifier = Modifier.size(width = 15.dp, height = 17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = if (isLocating) "Detecting..." else selectedLocation,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 145.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_dropdown_triangle),
                        contentDescription = "Select Location",
                        modifier = Modifier.size(width = 9.dp, height = 6.dp)
                    )
                }

                DropdownMenu(
                    expanded = showLocationMenu,
                    onDismissRequest = { showLocationMenu = false },
                    modifier = Modifier
                        .background(PureWhite)
                        .width(260.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "🎯", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Use Current Location",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DeepPurple,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                    Text(
                                        text = "Auto-detect via GPS & Fused Location",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    )
                                }
                            }
                        },
                        onClick = {
                            showLocationMenu = false
                            requestLocation()
                        }
                    )

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    listOf(
                        "Ghaziabad, Sec 12",
                        "Indirapuram, Sector 4",
                        "Noida, Sector 62",
                        "Vaishali, Sector 5",
                        "Vasundhara, Sector 10"
                    ).forEach { loc ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = loc,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (loc == selectedLocation) FontWeight.Bold else FontWeight.Normal,
                                            color = if (loc == selectedLocation) DeepPurple else Color(0xFF1E293B),
                                            fontSize = 13.5.sp
                                        )
                                    )
                                    if (loc == selectedLocation) {
                                        Text(
                                            text = "✓",
                                            color = DeepPurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onLocationChanged(loc)
                                showLocationMenu = false
                            }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // User Avatar (portrait circle with user initial)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7E22CE))
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "P",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = PureWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Greeting Headline: "Hello, $userName!" (Image 1)
        Text(
            text = "Hello, $userName!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 26.sp
            ),
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dual Mode Toggle: "Create Request" / "Accept Request" (Image 1)
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFE2E8F0),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Create Request Pill Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(if (isCreateMode) VibrantPink else Color.Transparent)
                        .clickable { isCreateMode = true }
                ) {
                    Text(
                        text = "Create Request",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = if (isCreateMode) PureWhite else Color(0xFF475569)
                    )
                }

                // Accept Request Pill Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(if (!isCreateMode) VibrantPink else Color.Transparent)
                        .clickable { isCreateMode = false }
                ) {
                    Text(
                        text = "Accept Request",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = if (!isCreateMode) PureWhite else Color(0xFF475569)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Content switching based on pill toggle
        if (isCreateMode) {
            // ── SECTION 2 (Image 2): POPULAR SERVICES & BOOK AGAIN ──

            Text(
                text = "POPULAR SERVICES",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    fontSize = 13.sp
                ),
                color = Color(0xFF64748B),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2x2 Grid of Popular Services
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: Cleaning
                    PopularServiceGridCard(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.ic_broom,
                        iconBgColor = Color(0xFFF3E8FF),
                        title = "Cleaning",
                        priceText = "From ₹499",
                        onClick = { onServiceClick(CategoryIds.HOME_CARE, "Home Care") }
                    )
                    // Card 2: AC & Fridge
                    PopularServiceGridCard(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.ic_snowflake,
                        iconBgColor = Color(0xFFE0F2FE),
                        title = "AC & Fridge",
                        priceText = "From ₹699",
                        onClick = { onServiceClick(CategoryIds.APPLIANCES, "Appliances") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 3: Electrician
                    PopularServiceGridCard(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.ic_bolt,
                        iconBgColor = Color(0xFFFEF3C7),
                        title = "Electrician",
                        priceText = "From ₹299",
                        onClick = { onServiceClick(CategoryIds.ELECTRIC, "Electric") }
                    )
                    // Card 4: Plumber
                    PopularServiceGridCard(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.ic_wrench,
                        iconBgColor = Color(0xFFDCFCE7),
                        title = "Plumber",
                        priceText = "From ₹349",
                        onClick = { onServiceClick(CategoryIds.PLUMBING, "Plumbing") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "BOOK AGAIN",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    fontSize = 13.sp
                ),
                color = Color(0xFF64748B),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Book Again Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 0.5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "RK",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFF334155)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Rajesh Kumar",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Plumber • ★ 4.9 (42 jobs done)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { onServiceClick(CategoryIds.PLUMBING, "Plumbing") },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF9333EA)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9333EA)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Rebook",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── SECTION 3 (Image 3): TRENDING IN CITY & SAFETY STRIP ──
            val city = selectedLocation.split(",").firstOrNull()?.trim() ?: "Ghaziabad"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Trending in $city",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    ),
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Popular",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp
                    ),
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Deep Kitchen Cleaning (BESTSELLER)
                item {
                    TrendingServiceCard(
                        bannerBgColor = Color(0xFFFAF5FF),
                        bannerEmoji = "🫧",
                        badgeText = "BESTSELLER",
                        badgeBgColor = Color(0xFF059669),
                        ratingText = "★ 4.8",
                        reviewsCount = "(1.2k)",
                        title = "Deep Kitchen Cleaning",
                        price = "₹799",
                        onAddClick = {
                            val s = displaySubServices.find { it.id == "hc_2" }
                                ?: SubService("hc_2", CategoryIds.HOME_CARE, "Kitchen Cleaning", "Deep cleaning", 799)
                            onSubServiceClick(s)
                        }
                    )
                }

                // Card 2: AC Jet Servicing (HOT)
                item {
                    TrendingServiceCard(
                        bannerBgColor = Color(0xFFF0F9FF),
                        bannerEmoji = "❄️",
                        badgeText = "HOT",
                        badgeBgColor = Color(0xFFDB2777),
                        ratingText = "★ 4.9",
                        reviewsCount = "(3.4k)",
                        title = "AC Jet Servicing",
                        price = "₹699",
                        onAddClick = {
                            val s = displaySubServices.find { it.id == "app_1" }
                                ?: SubService("app_1", CategoryIds.APPLIANCES, "AC Service & Repair", "Jet pump service", 699)
                            onSubServiceClick(s)
                        }
                    )
                }

                // Card 3: Electrician / Switchboard
                item {
                    TrendingServiceCard(
                        bannerBgColor = Color(0xFFFFFBEB),
                        bannerEmoji = "⚡",
                        badgeText = "POPULAR",
                        badgeBgColor = Color(0xFF059669),
                        ratingText = "★ 4.9",
                        reviewsCount = "(2.1k)",
                        title = "Switchboard & Wiring Fix",
                        price = "₹299",
                        onAddClick = {
                            val s = displaySubServices.find { it.id == "elec_1" }
                                ?: SubService("elec_1", CategoryIds.ELECTRIC, "Wiring & Cabling", "Internal wiring repair", 599)
                            onSubServiceClick(s)
                        }
                    )
                }

                // Remaining dynamic subservices from database
                items(displaySubServices.filterNot { it.id == "hc_2" || it.id == "app_1" || it.id == "elec_1" }) { subService ->
                    TrendingServiceCard(
                        bannerBgColor = Color(0xFFF8FAFC),
                        bannerEmoji = "🔧",
                        badgeText = "VERIFIED",
                        badgeBgColor = Color(0xFF7E22CE),
                        ratingText = "★ 4.8",
                        reviewsCount = "(980)",
                        title = subService.name,
                        price = "₹${subService.basePrice}",
                        onAddClick = { onSubServiceClick(subService) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // HireMe Safety & Guarantee Strip (Image 3)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD1FAE5))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_shield_check),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color(0xFF0284C7))
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "HireMe Safety & Guarantee",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Verified Pros • Upfront Pricing • 30-Day Cover",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Inquiries & Chats section
            Text(
                text = "Recent Inquiries & Chats",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = TextDark,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                recentChats.forEach { chat ->
                    RecentChatRow(chat = chat)
                }
            }

        } else {
            // ── Accept Request mode: Worker view ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Active Jobs accepted by this worker
                if (activeJobs.isNotEmpty()) {
                    Text(
                        text = "My Active Jobs",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    activeJobs.forEach { job ->
                        WorkerJobCard(
                            request = job,
                            actionText = "Mark Complete",
                            actionColor = VibrantPink,
                            onActionClick = {
                                coroutineScope.launch {
                                    repository.completeRequest(job.id)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Available Requests waiting to be accepted
                Text(
                    text = "Available Requests",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (pendingRequests.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📋", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No requests waiting for acceptance right now.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "New requests created by customers will appear here.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted
                                )
                            )
                        }
                    }
                } else {
                    pendingRequests.forEach { req ->
                        WorkerJobCard(
                            request = req,
                            actionText = "Accept Request",
                            actionColor = Color(0xFF10B981),
                            onActionClick = {
                                coroutineScope.launch {
                                    repository.acceptRequest(req.id)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

/**
 * 2x2 Popular Service Card matching Image 2
 */
@Composable
private fun PopularServiceGridCard(
    iconRes: Int,
    iconBgColor: Color,
    title: String,
    priceText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 0.5.dp,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor)
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * Trending Service Card matching Image 3
 */
@Composable
private fun TrendingServiceCard(
    bannerBgColor: Color,
    bannerEmoji: String,
    badgeText: String,
    badgeBgColor: Color,
    ratingText: String,
    reviewsCount: String,
    title: String,
    price: String,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 0.5.dp,
        modifier = modifier
            .width(180.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top banner area
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bannerBgColor)
            ) {
                Text(
                    text = bannerEmoji,
                    fontSize = 32.sp
                )
                // Top right badge (BESTSELLER / HOT / etc.)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(badgeBgColor)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ratingText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = Color(0xFF9333EA)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reviewsCount,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                ),
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Price & Add+ button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF0F172A)
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF9333EA))
                        .clickable(onClick = onAddClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Add +",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = PureWhite
                    )
                }
            }
        }
    }
}

/**
 * Recommendation Card using dynamic SubService fetched from MongoDB database (Image 4).
 * Displays subService.name (attribute 'name'), basePrice, provider label, and rating.
 */
@Composable
fun RecommendationSubServiceCard(
    subService: SubService,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val graphicType = when (subService.categoryId) {
        CategoryIds.PLUMBING -> RecommendationType.PLUMBING
        CategoryIds.ELECTRIC -> RecommendationType.ELECTRICIAN
        CategoryIds.APPLIANCES -> RecommendationType.PAINTING
        else -> RecommendationType.CLEANING
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(148.dp)
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column {
            // Illustration Header matching category
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                RecommendationGraphic(type = graphicType)
            }

            // Info Section: Uses subService.name (attribute 'name' from database)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Text(
                    text = subService.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    ),
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Verified Pro",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp
                    ),
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Star rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⭐", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "4.8",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = TextDark
                        )
                    }

                    // Price from database
                    Text(
                        text = "₹${subService.basePrice}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp
                        ),
                        color = TextDark
                    )
                }
            }
        }
    }
}

/**
 * Worker Card for viewing and accepting customer service requests or completing active jobs.
 */
@Composable
fun WorkerJobCard(
    request: ServiceRequest,
    actionText: String,
    actionColor: Color,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.subService.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = TextDark
                )
                Text(
                    text = "₹${request.subService.basePrice}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    ),
                    color = DeepPurple
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Customer: ${request.customerName}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp
                ),
                color = TextDark
            )

            if (request.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "📍 ${request.address}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    color = Color(0xFF64748B)
                )
            }

            if (request.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = actionColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clickable(onClick = onActionClick)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = PureWhite
                    )
                }
            }
        }
    }
}

/**
 * Rich colorful graphic for the recommendation cards matching the photographic feel in reference.
 */
@Composable
fun RecommendationGraphic(
    type: RecommendationType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (type) {
            RecommendationType.CLEANING -> {
                // Soft teal / cyan gradient background
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )
                // Maid apron silhouette
                val apron = Path().apply {
                    moveTo(w * 0.35f, h * 0.25f)
                    lineTo(w * 0.65f, h * 0.25f)
                    lineTo(w * 0.75f, h * 0.85f)
                    lineTo(w * 0.25f, h * 0.85f)
                    close()
                }
                drawPath(path = apron, color = Color(0xFF0284C7))

                // Yellow cleaning gloves
                drawCircle(color = Color(0xFFFBBF24), radius = 12.dp.toPx(), center = Offset(w * 0.32f, h * 0.55f))
                drawCircle(color = Color(0xFFFBBF24), radius = 12.dp.toPx(), center = Offset(w * 0.68f, h * 0.55f))

                // Cleaning bucket with bubbles
                drawRoundRect(
                    color = Color(0xFF38BDF8),
                    topLeft = Offset(w * 0.40f, h * 0.60f),
                    size = androidx.compose.ui.geometry.Size(w * 0.20f, h * 0.25f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
                drawCircle(color = PureWhite.copy(alpha = 0.8f), radius = 4.dp.toPx(), center = Offset(w * 0.46f, h * 0.55f))
                drawCircle(color = PureWhite.copy(alpha = 0.8f), radius = 3.dp.toPx(), center = Offset(w * 0.54f, h * 0.52f))
            }

            RecommendationType.PLUMBING -> {
                // Soft warm gray / copper gradient background
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )
                // Sink chrome U-pipe
                val pipe = Path().apply {
                    moveTo(w * 0.32f, h * 0.20f)
                    lineTo(w * 0.32f, h * 0.55f)
                    cubicTo(w * 0.32f, h * 0.85f, w * 0.68f, h * 0.85f, w * 0.68f, h * 0.55f)
                    lineTo(w * 0.68f, h * 0.20f)
                }
                drawPath(path = pipe, color = Color(0xFF94A3B8), style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))

                // Plumber's red wrench
                val wrench = Path().apply {
                    moveTo(w * 0.25f, h * 0.70f)
                    lineTo(w * 0.75f, h * 0.30f)
                }
                drawPath(path = wrench, color = Color(0xFFEF4444), style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(color = Color(0xFFDC2626), radius = 6.dp.toPx(), center = Offset(w * 0.75f, h * 0.30f))
            }

            RecommendationType.ELECTRICIAN -> {
                // Soft blue / amber gradient background
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFEF3C7), Color(0xFFDBEAFE)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )
                // Blue electrician technician coat
                val coat = Path().apply {
                    moveTo(w * 0.30f, h * 0.30f)
                    lineTo(w * 0.70f, h * 0.30f)
                    lineTo(w * 0.80f, h * 0.85f)
                    lineTo(w * 0.20f, h * 0.85f)
                    close()
                }
                drawPath(path = coat, color = Color(0xFF1E40AF))

                // Glowing light bulb
                drawCircle(color = Color(0xFFFBBF24), radius = 14.dp.toPx(), center = Offset(w * 0.50f, h * 0.50f))
                drawCircle(color = PureWhite.copy(alpha = 0.6f), radius = 7.dp.toPx(), center = Offset(w * 0.48f, h * 0.47f))
            }

            RecommendationType.PAINTING -> {
                // Soft pink / purple gradient background
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFCE7F3), Color(0xFFF3E8FF)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )
                // Diagonal fresh paint streak
                val paintStroke = Path().apply {
                    moveTo(w * 0.20f, h * 0.30f)
                    lineTo(w * 0.80f, h * 0.60f)
                }
                drawPath(path = paintStroke, color = Color(0xFFEC4899), style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round))

                // Roller handle
                drawLine(color = Color(0xFF64748B), start = Offset(w * 0.80f, h * 0.60f), end = Offset(w * 0.60f, h * 0.85f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            }
        }
    }
}

/**
 * Recent Chat Row matching the design in media_1788963081808.png:
 * - Avatar with indicator dot
 * - Name & message snippet
 * - Timestamp & Unread badge
 * - Supports highlighted soft lavender background for selected chats (Row 2 in reference)
 */
@Composable
fun RecentChatRow(
    chat: ChatItem,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (chat.isHighlighted) Color(0xFFEDE9FE) else Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .clickable { }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            // Avatar with optional online dot
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(46.dp)
            ) {
                // Avatar circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(chat.avatarColor)
                ) {
                    Text(
                        text = chat.avatarInitial,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFF1E293B)
                    )
                }

                // Green Online Dot (like row 2 in reference)
                if (chat.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(PureWhite)
                            .padding(1.5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and message preview
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chat.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp
                    ),
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Time and Badge
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = chat.time,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Badge: Red unread number badge OR Purple "unread" pill
                when {
                    chat.unreadCount > 0 -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = PureWhite
                            )
                        }
                    }
                    chat.isUnreadBadge -> {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = DeepPurple
                        ) {
                            Text(
                                text = "unread",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Circular user avatar badge with portrait silhouette.
 */
@Composable
fun UserAvatarBadge(
    initial: String = "A",
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .border(1.5.dp, Color(0xFFEC4899), CircleShape)
            .background(Color(0xFFFDE047))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Head
            drawCircle(color = Color(0xFF78350F), radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.42f))
            // Face
            drawCircle(color = Color(0xFFFDBA74), radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.44f))
            // Body / Shirt
            drawArc(
                color = Color(0xFFEC4899),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.15f, h * 0.65f),
                size = androidx.compose.ui.geometry.Size(w * 0.70f, h * 0.60f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. CATEGORIES GRID VIEW (Image 2 Gradient Header + 9 Category Cards)
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// DATA MODELS FOR CATEGORIES DYNAMIC SECTIONS
// ─────────────────────────────────────────────────────────────────────────────

private data class CategoryFeedServiceItem(
    val title: String,
    val description: String,
    val duration: String,
    val rating: String,
    val price: Int
)

private data class MostBookedServiceCardItem(
    val title: String,
    val duration: String,
    val rating: String,
    val price: Int
)

private data class TopRatedWorkerItem(
    val name: String,
    val trade: String,
    val rating: String,
    val jobsCount: Int,
    val badgeCode: String,
    val badgeColor: Color
)

@Composable
fun CategoriesGridView(
    categories: List<HomeCategoryItem>,
    selectedLocation: String = "Ghaziabad, Sector 12",
    onLocationChanged: (String) -> Unit = {},
    onServiceClick: (HomeCategoryItem) -> Unit,
    onRequestClick: (ServiceRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryChip by remember { mutableStateOf("Home Care") }
    var toastFeedback by remember { mutableStateOf<String?>(null) }

    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val mostBookedServices = remember {
        listOf(
            MostBookedServiceCardItem("AC Jet Service", "45 mins", "★ 4.88 (2.4k)", 499),
            MostBookedServiceCardItem("Bathroom Clean", "60 mins", "★ 4.82 (1.8k)", 399),
            MostBookedServiceCardItem("Switch & Fan Fix", "30 mins", "★ 4.90 (980)", 199),
            MostBookedServiceCardItem("Kitchen Degrease", "90 mins", "★ 4.85 (1.5k)", 699),
            MostBookedServiceCardItem("Car Foam Wash", "45 mins", "★ 4.80 (750)", 499)
        )
    }

    val subServicesByCat = remember {
        mapOf(
            "Home Care" to listOf(
                CategoryFeedServiceItem("Full House Deep Cleaning", "All rooms, floors, windows & balcony", "4-5 hrs", "4.9 (1.2k)", 1499),
                CategoryFeedServiceItem("Kitchen Deep Degreasing", "Chimney, cabinets, stove & tiles", "2 hrs", "4.8 (850)", 799),
                CategoryFeedServiceItem("Bathroom Cleaning (2 Baths)", "Tile descaling, fixtures & sanitation", "90 mins", "4.8 (2.1k)", 649),
                CategoryFeedServiceItem("Sofa & Carpet Shampooing", "Deep fabric vacuum & odor extraction", "60 mins", "4.7 (620)", 499),
                CategoryFeedServiceItem("Water Tank Cleaning", "Sludge drain, scrub & UV disinfection", "45 mins", "4.9 (410)", 599)
            ),
            "Electric" to listOf(
                CategoryFeedServiceItem("Fan Installation & Repair", "Ceiling fan replacement & speed regulator fix", "30 mins", "4.9 (1.1k)", 199),
                CategoryFeedServiceItem("Wiring & Short Circuit Fix", "Complete diagnosis & safety rewiring", "60 mins", "4.8 (940)", 499),
                CategoryFeedServiceItem("Switchboard Replacement", "Socket, switch repair & modular setup", "40 mins", "4.9 (780)", 299),
                CategoryFeedServiceItem("Inverter & MCB Tripping", "Fuse, MCB box & inverter wiring check", "45 mins", "4.8 (520)", 399)
            ),
            "Plumbing" to listOf(
                CategoryFeedServiceItem("Tap & Shower Repair", "Leak fix, cartridge & nozzle cleaning", "30 mins", "4.8 (890)", 199),
                CategoryFeedServiceItem("Drain Blockage Clearing", "Kitchen sink, bathroom floor pipe unclog", "45 mins", "4.9 (1.4k)", 349),
                CategoryFeedServiceItem("Water Motor & Pump Fix", "Pressure motor repair & capacitor check", "60 mins", "4.7 (460)", 599),
                CategoryFeedServiceItem("Toilet Jet & Cistern Repair", "Flush cistern leak & jet spray replacement", "35 mins", "4.8 (670)", 249)
            ),
            "Beauty" to listOf(
                CategoryFeedServiceItem("Glow Facial & Cleanup", "Herbal scrub, steaming & gold massage", "60 mins", "4.9 (2.3k)", 799),
                CategoryFeedServiceItem("Full Arms + Legs Waxing", "Rica honey wax with skin soothing lotion", "45 mins", "4.8 (1.7k)", 599),
                CategoryFeedServiceItem("Pedicure & Manicure Spa", "Cuticle care, scrub, massage & polish", "60 mins", "4.9 (980)", 699),
                CategoryFeedServiceItem("Hair Cut & Blow Dry", "Styling, split-end trim & volume dry", "40 mins", "4.8 (1.2k)", 449)
            ),
            "Appliances" to listOf(
                CategoryFeedServiceItem("AC Jet Pump Service", "Outdoor & indoor unit jet pump deep clean", "45 mins", "4.9 (3.1k)", 499),
                CategoryFeedServiceItem("Washing Machine Repair", "Spin motor, drum bearing & drainage fix", "60 mins", "4.8 (1.1k)", 449),
                CategoryFeedServiceItem("Refrigerator Cooling Fix", "Gas charging, thermostat & compressor", "50 mins", "4.8 (920)", 599),
                CategoryFeedServiceItem("Microwave Oven Repair", "Magnetron, panel repair & heating check", "40 mins", "4.7 (510)", 399)
            )
        )
    }

    val topPros = remember {
        listOf(
            TopRatedWorkerItem("Ramesh Sharma", "Master Electrician", "4.96", 184, "EL", Color(0xFF10B981)),
            TopRatedWorkerItem("Sunita Verma", "Salon & Skin Expert", "4.92", 312, "BT", Color(0xFFEC4899)),
            TopRatedWorkerItem("Anil Kumar", "Senior Plumber", "4.89", 145, "PL", Color(0xFF3B82F6))
        )
    }

    val headerHeight = 295.dp
    val sheetOverlap = 26.dp
    val sheetTop = headerHeight - sheetOverlap
    val density = LocalDensity.current
    val sheetTopPx = remember(density, sheetTop) { with(density) { sheetTop.toPx() } }
    var sheetOffsetPx by remember { mutableFloatStateOf(sheetTopPx) }
    val sheetScrollState = rememberScrollState()

    val nestedScrollConnection = remember(sheetTopPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                // When scrolling UP (delta < 0), glide sheet UP over header first
                if (delta < 0f && sheetOffsetPx > 0f) {
                    val newOffset = (sheetOffsetPx + delta).coerceAtLeast(0f)
                    val consumedY = newOffset - sheetOffsetPx
                    sheetOffsetPx = newOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                // When scrolling DOWN (delta > 0), after sheet internal content reaches top, glide sheet back DOWN
                if (delta > 0f && sheetOffsetPx < sheetTopPx) {
                    val newOffset = (sheetOffsetPx + delta).coerceAtMost(sheetTopPx)
                    val consumedY = newOffset - sheetOffsetPx
                    sheetOffsetPx = newOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .background(PureWhite)
    ) {
        // 1. Stationary Background Header Banner (stays in its place in the background)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_home_header),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopEnd,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                val context = LocalContext.current
                var showLocationMenu by remember { mutableStateOf(false) }
                var isLocating by remember { mutableStateOf(false) }

                // UI Safety Net: Automatically reset loading spinner if it ever exceeds 5.5s
                LaunchedEffect(isLocating) {
                    if (isLocating) {
                        delay(5500L)
                        isLocating = false
                    }
                }

                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (fineGranted || coarseGranted) {
                        isLocating = true
                        LocationHelper.fetchCurrentLocation(context) { result ->
                            isLocating = false
                            result.onSuccess { loc ->
                                onLocationChanged(loc.formattedText)
                                toastFeedback = "📍 Location detected: ${loc.formattedText}"
                            }.onFailure { err ->
                                toastFeedback = "⚠️ ${err.localizedMessage ?: "Could not detect location"}"
                            }
                        }
                    } else {
                        toastFeedback = "⚠️ Location permission denied"
                    }
                }

                val requestCurrentLocation: () -> Unit = {
                    if (LocationHelper.hasLocationPermission(context)) {
                        isLocating = true
                        LocationHelper.fetchCurrentLocation(context) { result ->
                            isLocating = false
                            result.onSuccess { loc ->
                                onLocationChanged(loc.formattedText)
                                toastFeedback = "📍 Location detected: ${loc.formattedText}"
                            }.onFailure { err ->
                                toastFeedback = "⚠️ ${err.localizedMessage ?: "Could not detect location"}"
                            }
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                Box(modifier = Modifier.zIndex(10f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    ) {
                        // 1. Direct 1-tap real-time GPS location fetch on clicking Pin or City Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    if (!isLocating) {
                                        requestCurrentLocation()
                                    }
                                }
                                .padding(end = 4.dp)
                        ) {
                            if (isLocating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(15.dp),
                                    strokeWidth = 2.dp,
                                    color = PureWhite
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_location_pin),
                                    contentDescription = "Location Pin",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = if (isLocating) "Detecting location..." else selectedLocation,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.sp
                                ),
                                color = PureWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // 2. Tapping Dropdown Arrow toggles manual sector picker
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showLocationMenu = !showLocationMenu }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_dropdown_arrow),
                                contentDescription = "Select Location",
                                modifier = Modifier.size(width = 10.dp, height = 6.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLocationMenu,
                        onDismissRequest = { showLocationMenu = false },
                        modifier = Modifier
                            .background(PureWhite)
                            .width(260.dp)
                    ) {
                        // Option 1: Use Current Location (GPS / Fused Location)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isLocating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = DeepPurple
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Detecting GPS...",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = DeepPurple,
                                                fontSize = 13.5.sp
                                            )
                                        )
                                    } else {
                                        Text(text = "🎯", fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Use Current Location",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepPurple,
                                                    fontSize = 13.5.sp
                                                )
                                            )
                                            Text(
                                                text = "Auto-detect via GPS & Fused Location",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 10.5.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                showLocationMenu = false
                                requestCurrentLocation()
                            }
                        )

                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Manual Sector Options
                        listOf(
                            "Ghaziabad, Sector 12",
                            "Indirapuram, Sector 4",
                            "Noida, Sector 62",
                            "Vaishali, Sector 5",
                            "Vasundhara, Sector 10"
                        ).forEach { loc ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = loc,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (loc == selectedLocation) FontWeight.Bold else FontWeight.Normal,
                                                color = if (loc == selectedLocation) DeepPurple else Color(0xFF1E293B),
                                                fontSize = 13.5.sp
                                            )
                                        )
                                        if (loc == selectedLocation) {
                                            Text(
                                                text = "✓",
                                                color = DeepPurple,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onLocationChanged(loc)
                                    showLocationMenu = false
                                    toastFeedback = "Location changed to $loc"
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Everyday help,\none tap away.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = 34.sp
                    ),
                    color = PureWhite
                )

                Spacer(modifier = Modifier.height(44.dp))

                // Search Bar placed in the lower part of the background image
                Surface(
                    shape = RoundedCornerShape(23.dp),
                    color = PureWhite,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .zIndex(10f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_search_outline),
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 15.sp,
                                        color = Color(0xFF8E8E93)
                                    )
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = TextInput,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(DeepPurple),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // 2. Foreground Hovering Sheet: Scrolls ABOVE the stationary header (Hover Effect)
        Surface(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = PureWhite,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .offset { IntOffset(0, sheetOffsetPx.roundToInt()) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(sheetScrollState)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp)
            ) {
                    // Drag Handle pill visually emphasizing the floating hover sheet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                        )
                    }

                    // ════════════════════════════════════════════════════════════════
                    // STAGE 1 (Image 1: 3x3 Categories + Deals Banner + Most Booked)
                    // ════════════════════════════════════════════════════════════════
                    Text(
                            text = "Categories",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // 3x3 Category Grid (rendered in 3 rows for flawless scrolling)
                        val rows = filteredCategories.chunked(3)
                        rows.forEach { rowCategories ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowCategories.forEach { category ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ReferenceCategoryCard(
                                            category = category,
                                            onClick = { onServiceClick(category) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                val emptySlots = 3 - rowCategories.size
                                repeat(emptySlots) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Summer Deals Promo Banner (from Image 1)
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    toastFeedback = "Coupon HIRE25 applied! 25% OFF discount ready."
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF5C1380), Color(0xFFEC4899))
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        // Yellow Tag
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFEF08A), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "SUMMER DEALS",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF854D0E)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "AC Jet Service & Repair",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            color = PureWhite
                                        )
                                        Text(
                                            text = "Flat 25% OFF | Code: HIRE25",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp
                                            ),
                                            color = Color(0xFFFCE7F3)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = PureWhite,
                                        shadowElevation = 1.dp,
                                        modifier = Modifier.clickable {
                                            toastFeedback = "AC Jet Service booked with 25% OFF!"
                                        }
                                    ) {
                                        Text(
                                            text = "Book Now",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            color = DeepPurple,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Most Booked Services Header (from Image 1)
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Most Booked Services",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "View All >",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = VibrantPink,
                                modifier = Modifier.clickable {
                                    toastFeedback = "Opening all trending services..."
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Horizontal Scrollable Cards (from Image 1)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(mostBookedServices) { service ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier.width(160.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Pill duration
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFF1F5F9), RoundedCornerShape(50))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = service.duration,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = Color(0xFF475569)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = service.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = service.rating,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = Color(0xFFD97706)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Rs ${service.price}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            ),
                                            color = DeepPurple
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Pink "+ Add Service" button
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFFFDF2F8),
                                            border = BorderStroke(1.dp, Color(0xFFF472B6)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    toastFeedback = "${service.title} added to cart!"
                                                }
                                        ) {
                                            Text(
                                                text = "+ Add Service",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                ),
                                                color = Color(0xFFBE185D),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ════════════════════════════════════════════════════════════════
                        // STAGE 2 (Image 2: Category Chips + Live Banner + SubServices)
                        // ════════════════════════════════════════════════════════════════
                        Spacer(modifier = Modifier.height(26.dp))
                        Text(
                            text = "Select Category:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Horizontal Filter Chips (Home Care, Electric, Plumbing, Beauty, Appliances)
                        val chipsList = listOf("Home Care", "Electric", "Plumbing", "Beauty", "Appliances")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(chipsList) { chipName ->
                                val isSelected = chipName == selectedCategoryChip
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) DeepPurple else Color(0xFFF8FAFC),
                                    border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.clickable { selectedCategoryChip = chipName }
                                ) {
                                    Text(
                                        text = chipName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = if (isSelected) PureWhite else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Availability Banner (from Image 2)
                        val proCount = when (selectedCategoryChip) {
                            "Home Care" -> "18 Verified Cleaners"
                            "Electric" -> "14 Verified Electricians"
                            "Plumbing" -> "11 Verified Plumbers"
                            "Beauty" -> "16 Verified Beauticians"
                            "Appliances" -> "15 Verified Technicians"
                            else -> "12 Verified Experts"
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LIVE: $proCount available nearby right now",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF065F46)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Sub-Services List Header (from Image 2)
                        val activeSubServices = subServicesByCat[selectedCategoryChip] ?: emptyList()
                        Text(
                            text = "$selectedCategoryChip Services (${activeSubServices.size} Available)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Dynamic Sub-Services Cards (from Image 2)
                        activeSubServices.forEach { item ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            color = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp
                                            ),
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Time: ${item.duration}  •  Rating: ${item.rating}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = Color(0xFF7C3AED)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Rs ${item.price}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            ),
                                            color = DeepPurple
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = VibrantPink,
                                        modifier = Modifier.clickable {
                                            toastFeedback = "Booking started for ${item.title} (Rs ${item.price})!"
                                        }
                                    ) {
                                        Text(
                                            text = "Book Now",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            color = PureWhite,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Re-order Card (from Image 2)
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFDF4FF),
                            border = BorderStroke(1.dp, Color(0xFFF0ABFC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    toastFeedback = "Re-booking AC Repair from 15 Aug..."
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Quick Re-order: Last booking on 15 Aug",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF86198F)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tap to rebook AC Repair in 1-tap ->",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = Color(0xFFC026D3)
                                )
                            }
                        }

                        // ════════════════════════════════════════════════════════════════
                        // STAGE 3 (Image 3: Top Rated Professionals Nearby)
                        // ════════════════════════════════════════════════════════════════
                        Spacer(modifier = Modifier.height(26.dp))
                        Text(
                            text = "Top Rated Professionals Nearby",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        topPros.forEach { pro ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Avatar Badge
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = pro.badgeColor,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = pro.badgeCode,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 16.sp
                                                    ),
                                                    color = PureWhite
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = pro.name,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                ),
                                                color = Color(0xFF0F172A)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${pro.trade}  •  Rating: ${pro.rating} (${pro.jobsCount} jobs)",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.sp
                                                ),
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // "Hire Pro" Pill Button
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = PureWhite,
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable {
                                            toastFeedback = "Connecting you directly with ${pro.name}..."
                                        }
                                    ) {
                                        Text(
                                            text = "Hire Pro",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            color = DeepPurple,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
        // Floating Action Feedback Toast
        toastFeedback?.let { message ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 74.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = message,
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "OK",
                        color = VibrantPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable { toastFeedback = null }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. NOTIFICATIONS, MESSAGES & PROFILE VIEWS
// ─────────────────────────────────────────────────────────────────────────────

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val iconEmoji: String,
    val iconBgColor: Color,
    val isUnread: Boolean,
    val type: String
)

@Composable
fun NotificationsScreenView(modifier: Modifier = Modifier) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Bookings", "Offers", "System")

    var notifications by remember {
        mutableStateOf(
            listOf(
                NotificationItem(
                    id = "n1",
                    title = "Booking Confirmed! 🎉",
                    description = "Ramesh Sharma (Master Electrician) has confirmed your request for Fan Installation & Repair at 2:30 PM today.",
                    timeAgo = "10 mins ago",
                    iconEmoji = "⚡",
                    iconBgColor = Color(0xFFFEF3C7),
                    isUnread = true,
                    type = "Bookings"
                ),
                NotificationItem(
                    id = "n2",
                    title = "Summer Deals 25% OFF 🔥",
                    description = "Use coupon code HIRE25 to get instant 25% off on Full House Deep Cleaning & Kitchen Degreasing.",
                    timeAgo = "2 hours ago",
                    iconEmoji = "🏷️",
                    iconBgColor = Color(0xFFFCE7F3),
                    isUnread = true,
                    type = "Offers"
                ),
                NotificationItem(
                    id = "n3",
                    title = "New Message from Sunita Verma 💬",
                    description = "\"Hi, I have reached Sector 12 and will be at your doorstep in 5 minutes.\"",
                    timeAgo = "5 hours ago",
                    iconEmoji = "💬",
                    iconBgColor = Color(0xFFE0E7FF),
                    isUnread = true,
                    type = "Bookings"
                ),
                NotificationItem(
                    id = "n4",
                    title = "Service Completed & Paid ✓",
                    description = "Payment of ₹399 for Microwave Oven Repair was successful. Rate your service provider now.",
                    timeAgo = "Yesterday",
                    iconEmoji = "💳",
                    iconBgColor = Color(0xFFD1FAE5),
                    isUnread = false,
                    type = "Bookings"
                ),
                NotificationItem(
                    id = "n5",
                    title = "System Update Completed",
                    description = "HireMe v2.4 is live with enhanced real-time GPS location accuracy and instant matching.",
                    timeAgo = "2 days ago",
                    iconEmoji = "⚙️",
                    iconBgColor = Color(0xFFF1F5F9),
                    isUnread = false,
                    type = "System"
                )
            )
        )
    }

    val filteredNotifications = remember(selectedFilter, notifications) {
        if (selectedFilter == "All") notifications
        else notifications.filter { it.type == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 90.dp)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp
                    ),
                    color = TextDark
                )
                Text(
                    text = "You have ${notifications.count { it.isUnread }} unread alerts",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(20),
                color = Color(0xFFF3E8FF),
                modifier = Modifier.clickable {
                    notifications = notifications.map { it.copy(isUnread = false) }
                }
            ) {
                Text(
                    text = "Mark all read",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = DeepPurple,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Filter Pills Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 14.dp)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) DeepPurple else Color(0xFFF1F5F9),
                    modifier = Modifier.clickable { selectedFilter = filter }
                ) {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = if (isSelected) PureWhite else Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Notifications List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredNotifications) { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (item.isUnread) PureWhite else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (item.isUnread) Color(0xFFE2E8F0) else Color(0xFFF1F5F9)),
                    shadowElevation = if (item.isUnread) 2.dp else 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            notifications = notifications.map {
                                if (it.id == item.id) it.copy(isUnread = false) else it
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Icon Box
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(item.iconBgColor)
                        ) {
                            Text(text = item.iconEmoji, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (item.isUnread) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = TextDark,
                                    modifier = Modifier.weight(1f)
                                )

                                if (item.isUnread) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(VibrantPink)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = if (item.isUnread) Color(0xFF334155) else Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = item.timeAgo,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessagesScreenView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp
            ),
            color = TextDark
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your customer and service provider conversations appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}

@Composable
fun ProfileScreenView(
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = RepositoryProvider.repository
    val user = repository.getCurrentUser()
    val userName = remember(user) {
        val raw = user?.name?.trim() ?: ""
        if (raw.isBlank() || raw.all { it.isDigit() } || raw.startsWith("+")) {
            "Prakhar"
        } else {
            raw
        }
    }
    val userEmail = user?.email?.takeIf { it.isNotBlank() } ?: "prakhar2877@gmail.com"
    val userPhone = "+917705023222"

    var selectedProfileTab by remember { mutableIntStateOf(0) } // 0: About you, 1: Account
    var showLogoutDialog by remember { mutableStateOf(false) }

    val aboutScrollState = rememberScrollState()
    val accountScrollState = rememberScrollState()

    val primaryBlack = Color(0xFF000000)
    val textMuted = Color(0xFF64748B)
    val textLightMuted = Color(0xFF94A3B8)
    val dividerGray = Color(0xFFE2E8F0)
    val sectionBand = Color(0xFFF8FAFC)
    val cardBg = Color(0xFFF8FAFC)
    val cardBorder = Color(0xFFE2E8F0)
    val brandAccent = Color(0xFFEC4899) // VibrantPink - the ONE extra accent color

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Log Out",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = primaryBlack
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of your HireMe account?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = textMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Log Out",
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "Cancel", color = textMuted)
                }
            },
            containerColor = PureWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .statusBarsPadding()
    ) {
        // ── 1. Top Sub-Tabs (About you vs Account) ────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
        ) {
            // Tab 0: About you
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedProfileTab = 0 }
                    .padding(top = 14.dp)
            ) {
                Text(
                    text = "About you",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = if (selectedProfileTab == 0) primaryBlack else textLightMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(if (selectedProfileTab == 0) primaryBlack else Color.Transparent)
                )
            }

            // Tab 1: Account
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedProfileTab = 1 }
                    .padding(top = 14.dp)
            ) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = if (selectedProfileTab == 1) primaryBlack else textLightMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(if (selectedProfileTab == 1) primaryBlack else Color.Transparent)
                )
            }
        }
        HorizontalDivider(color = dividerGray, thickness = 1.dp)

        // ── 2. Scrollable Body ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(if (selectedProfileTab == 0) aboutScrollState else accountScrollState)
        ) {
            if (selectedProfileTab == 0) {
                // ══════════════ TAB 1: ABOUT YOU ══════════════

                // Profile Identity Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Avatar: Modern pitch-black circle with crisp white icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF111827))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(42.dp),
                                colorFilter = ColorFilter.tint(PureWhite)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = primaryBlack
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Intermediate",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = textMuted
                            )
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(primaryBlack)
                    )
                }

                // "Complete your profile" Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Complete your profile",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = primaryBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This helps builds trust, encouraging members to hire and work with you.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = textMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "1 out of 6 complete",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = primaryBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // 6-Segment Progress Bar
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (i in 0 until 6) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(CircleShape)
                                        .background(if (i == 0) primaryBlack else Color(0xFFE2E8F0))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Add profile picture",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = primaryBlack,
                            modifier = Modifier.clickable { }
                        )
                    }
                }

                // "Edit personal details"
                Text(
                    text = "Edit personal details",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = primaryBlack,
                    modifier = Modifier
                        .clickable { }
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
                HorizontalDivider(color = dividerGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))

                // Section: Verify your profile
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "Verify your profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = primaryBlack
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Verify Govt ID
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 8.dp)
                    ) {
                        CircledPlusIcon(color = primaryBlack)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Verify your Govt. ID",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = primaryBlack
                        )
                    }

                    // Confirm Email
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 8.dp)
                    ) {
                        CircledPlusIcon(color = primaryBlack)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Confirm email $userEmail",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = primaryBlack
                        )
                    }

                    // Phone Verified
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        CircledCheckmarkIcon(color = brandAccent)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = userPhone,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = primaryBlack
                        )
                    }
                }

                // Thick Section Separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(sectionBand)
                )

                // Section: Your service reliability
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "Your service reliability",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = primaryBlack
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            colorFilter = ColorFilter.tint(primaryBlack)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Sometimes cancels bookings as a customer",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = textMuted
                        )
                    }
                }

                // Thick Section Separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(sectionBand)
                )

                // Section: About you (bio & preferences)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "About you",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = primaryBlack
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Add a mini bio
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 8.dp)
                    ) {
                        CircledPlusIcon(color = primaryBlack)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Add a mini bio",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = primaryBlack
                        )
                    }

                    // Edit preferences
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 8.dp)
                    ) {
                        CircledPlusIcon(color = primaryBlack)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Edit service preferences",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = primaryBlack
                        )
                    }
                }

                // Thick Section Separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(sectionBand)
                )

                // Section: Vehicles / Saved Addresses
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "Vehicles",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = primaryBlack
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Add vehicle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 8.dp)
                    ) {
                        CircledPlusIcon(color = primaryBlack)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Add vehicle",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = primaryBlack
                        )
                    }
                }

            } else {
                // ══════════════ TAB 2: ACCOUNT ══════════════

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Group 1: Profile & Preferences
                    ProfileAccountRow(title = "Ratings")
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                    ProfileAccountRow(title = "Saved professionals")
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                    ProfileAccountRow(title = "Help")
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                    ProfileAccountRow(title = "Communication preferences")
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                    ProfileAccountRow(title = "Password")
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                    ProfileAccountRow(title = "Postal address")

                    // Divider between Group 1 and Group 2
                    HorizontalDivider(color = dividerGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))

                    // Group 2: Payouts & Payments
                    ProfileAccountRow(title = "Payout methods")
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                    ProfileAccountRow(title = "Payouts")
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                    ProfileAccountRow(title = "Payment methods")
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                    ProfileAccountRow(title = "Payments & refunds")

                    // Divider between Group 2 and Group 3
                    HorizontalDivider(color = dividerGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))

                    // Group 3: Legal
                    ProfileAccountRow(title = "Terms and Conditions")
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                    ProfileAccountRow(title = "Data protection")

                    // Divider between Group 3 and Group 4
                    HorizontalDivider(color = dividerGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))

                    // Group 4: Logout & Close Account
                    Text(
                        text = "Log out",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = primaryBlack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true }
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                    Text(
                        text = "Close my account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = textMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
            }

            // Bottom spacing for bottom navigation bar
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ProfileAccountRow(
    title: String,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = Color(0xFF000000)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(Color(0xFF94A3B8))
        )
    }
}

@Composable
private fun CircledPlusIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF000000)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(24.dp)
            .border(width = 2.dp, color = color, shape = CircleShape)
    ) {
        Canvas(modifier = Modifier.size(11.dp)) {
            val stroke = 2.dp.toPx()
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun CircledCheckmarkIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFEC4899)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(24.dp)
            .background(color = color, shape = CircleShape)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            val stroke = 2.2.dp.toPx()
            val path = Path().apply {
                moveTo(size.width * 0.22f, size.height * 0.54f)
                lineTo(size.width * 0.44f, size.height * 0.76f)
                lineTo(size.width * 0.80f, size.height * 0.26f)
            }
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. 5-TAB BOTTOM NAVIGATION BAR - DESIGN 1: RAISED CENTER HERO TAB
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 5-Tab Bottom Navigation Bar (Design 1 - Raised Center Hero Tab):
 * - Tab 0: Home (House icon, active DeepPurple)
 * - Tab 1: Alerts (Bell icon with pink badge "3")
 * - Tab 2: Services (Center floating action button with vibrant purple-to-pink gradient,
 *           white 3dp border, category 9-dot grid icon, and "Services" label below)
 * - Tab 3: Messages (Chat bubble icon with emerald online dot)
 * - Tab 4: Profile (User avatar outline icon)
 */
@Composable
fun HireMeBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Outer Box holds the main bar surface and the protruding center floating button
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main Navigation Bar Surface with top padding to accommodate the protruding button
        Surface(
            color = PureWhite,
            shadowElevation = 12.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Tab 0: Home
                NavTabItem(
                    title = "Home",
                    isSelected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    Image(
                        painter = painterResource(id = R.drawable.ic_nav_home),
                        contentDescription = "Home",
                        modifier = Modifier.size(23.dp),
                        colorFilter = ColorFilter.tint(tint)
                    )
                }

                // Tab 1: Alerts / Notifications
                NavTabItem(
                    title = "Alerts",
                    isSelected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    Box(contentAlignment = Alignment.TopEnd) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_nav_notifications),
                            contentDescription = "Alerts",
                            modifier = Modifier.size(23.dp),
                            colorFilter = ColorFilter.tint(tint)
                        )
                        // Pink unread badge pill with text "3"
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .offset(x = 8.dp, y = (-4).dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(VibrantPink)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "3",
                                style = TextStyle(
                                    color = PureWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 10.sp
                                )
                            )
                        }
                    }
                }

                // Tab 2: Center Slot (Reserved for Raised Action, label placed at bottom)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(2) }
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = "Services",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.ExtraBold else FontWeight.SemiBold
                        ),
                        color = if (selectedTab == 2) DeepPurple else Color(0xFF64748B),
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 7.dp)
                    )
                }

                // Tab 3: Messages
                NavTabItem(
                    title = "Messages",
                    isSelected = selectedTab == 3,
                    onClick = { onTabSelected(3) },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    Box(contentAlignment = Alignment.TopEnd) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_nav_chat),
                            contentDescription = "Messages",
                            modifier = Modifier.size(23.dp),
                            colorFilter = ColorFilter.tint(tint)
                        )
                        // Online indicator emerald dot
                        Box(
                            modifier = Modifier
                                .offset(x = 3.dp, y = (-1).dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .border(1.5.dp, PureWhite, CircleShape)
                        )
                    }
                }

                // Tab 4: Profile
                NavTabItem(
                    title = "Profile",
                    isSelected = selectedTab == 4,
                    onClick = { onTabSelected(4) },
                    modifier = Modifier.weight(1f)
                ) { tint ->
                    Image(
                        painter = painterResource(id = R.drawable.ic_nav_profile),
                        contentDescription = "Profile",
                        modifier = Modifier.size(23.dp),
                        colorFilter = ColorFilter.tint(tint)
                    )
                }
            }
        }

        // Floating Raised Center Hero Button (Protruding above surface)
        val isCenterSelected = selectedTab == 2
        val centerScale by animateFloatAsState(
            targetValue = if (isCenterSelected) 1.08f else 1.0f,
            animationSpec = tween(durationMillis = 180),
            label = "centerScale"
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .scale(centerScale)
                .size(52.dp)
                .shadow(
                    elevation = if (isCenterSelected) 12.dp else 8.dp,
                    shape = CircleShape,
                    spotColor = DeepPurple.copy(alpha = 0.5f),
                    ambientColor = VibrantPink.copy(alpha = 0.3f)
                )
                .border(3.dp, PureWhite, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE82894), DeepPurple)
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onTabSelected(2) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_nav_categories),
                contentDescription = "Explore Services",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(PureWhite)
            )
        }
    }
}

@Composable
private fun NavTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (tint: Color) -> Unit
) {
    val activeColor = DeepPurple
    val inactiveColor = Color(0xFF64748B)
    val color = if (isSelected) activeColor else inactiveColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        icon(color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HEADER & CATEGORY COMPOSABLES (Kept for Categories Tab)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ReferenceCategoryCard(
    category: HomeCategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "cardScale"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .height(106.dp)
            .scale(scale)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            GradientCategoryIcon(
                type = category.iconType,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GradientCategoryIcon(
    type: HomeCategoryIconType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val gradientBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF06B6D4),
                Color(0xFF3B82F6),
                Color(0xFF8B5CF6),
                Color(0xFF9333EA)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (type) {
            HomeCategoryIconType.HOME_CARE -> {
                val house = Path().apply {
                    moveTo(w * 0.50f, h * 0.20f)
                    lineTo(w * 0.80f, h * 0.44f)
                    lineTo(w * 0.72f, h * 0.44f)
                    lineTo(w * 0.72f, h * 0.76f)
                    lineTo(w * 0.28f, h * 0.76f)
                    lineTo(w * 0.28f, h * 0.44f)
                    lineTo(w * 0.20f, h * 0.44f)
                    close()
                }
                drawPath(path = house, brush = gradientBrush)

                drawRoundRect(
                    color = PureWhite,
                    topLeft = Offset(w * 0.42f, h * 0.52f),
                    size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )

                val sparkle = Path().apply {
                    moveTo(w * 0.78f, h * 0.22f)
                    lineTo(w * 0.81f, h * 0.29f)
                    lineTo(w * 0.88f, h * 0.32f)
                    lineTo(w * 0.81f, h * 0.35f)
                    lineTo(w * 0.78f, h * 0.42f)
                    lineTo(w * 0.75f, h * 0.35f)
                    lineTo(w * 0.68f, h * 0.32f)
                    lineTo(w * 0.75f, h * 0.29f)
                    close()
                }
                drawPath(path = sparkle, brush = gradientBrush)
            }

            HomeCategoryIconType.BEAUTY -> {
                val hair = Path().apply {
                    moveTo(w * 0.32f, h * 0.22f)
                    cubicTo(w * 0.20f, h * 0.22f, w * 0.22f, h * 0.44f, w * 0.26f, h * 0.52f)
                    cubicTo(w * 0.28f, h * 0.58f, w * 0.34f, h * 0.65f, w * 0.30f, h * 0.78f)
                    cubicTo(w * 0.45f, h * 0.72f, w * 0.55f, h * 0.58f, w * 0.48f, h * 0.46f)
                    cubicTo(w * 0.62f, h * 0.48f, w * 0.72f, h * 0.38f, w * 0.78f, h * 0.30f)
                    cubicTo(w * 0.68f, h * 0.32f, w * 0.58f, h * 0.30f, w * 0.52f, h * 0.24f)
                    close()
                }
                drawPath(path = hair, brush = gradientBrush)

                val lock1 = Path().apply {
                    moveTo(w * 0.44f, h * 0.54f)
                    cubicTo(w * 0.62f, h * 0.62f, w * 0.74f, h * 0.50f, w * 0.82f, h * 0.42f)
                }
                drawPath(path = lock1, brush = gradientBrush, style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round))

                val lock2 = Path().apply {
                    moveTo(w * 0.42f, h * 0.66f)
                    cubicTo(w * 0.60f, h * 0.74f, w * 0.72f, h * 0.62f, w * 0.80f, h * 0.54f)
                }
                drawPath(path = lock2, brush = gradientBrush, style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round))
            }

            HomeCategoryIconType.HEALTH_CARE -> {
                val box = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(w * 0.24f, h * 0.36f, w * 0.76f, h * 0.78f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    )
                }
                drawPath(path = box, brush = gradientBrush)

                val handle = Path().apply {
                    moveTo(w * 0.38f, h * 0.36f)
                    lineTo(w * 0.38f, h * 0.24f)
                    lineTo(w * 0.62f, h * 0.24f)
                    lineTo(w * 0.62f, h * 0.36f)
                }
                drawPath(path = handle, brush = gradientBrush, style = stroke)

                val crossV = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(w * 0.45f, h * 0.46f, w * 0.55f, h * 0.68f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                        )
                    )
                }
                drawPath(path = crossV, color = PureWhite)

                val crossH = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(w * 0.35f, h * 0.52f, w * 0.65f, h * 0.62f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                        )
                    )
                }
                drawPath(path = crossH, color = PureWhite)
            }

            HomeCategoryIconType.APPLIANCES -> {
                val washer = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(w * 0.25f, h * 0.22f, w * 0.75f, h * 0.82f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    )
                }
                drawPath(path = washer, brush = gradientBrush, style = stroke)

                drawLine(brush = gradientBrush, start = Offset(w * 0.25f, h * 0.36f), end = Offset(w * 0.75f, h * 0.36f), strokeWidth = 1.8.dp.toPx())
                drawRect(brush = gradientBrush, topLeft = Offset(w * 0.32f, h * 0.26f), size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.06f))
                drawCircle(brush = gradientBrush, radius = w * 0.035f, center = Offset(w * 0.64f, h * 0.29f))

                drawCircle(brush = gradientBrush, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.58f), style = stroke)
                drawCircle(brush = gradientBrush, radius = w * 0.11f, center = Offset(w * 0.50f, h * 0.58f))
            }

            HomeCategoryIconType.ELECTRIC -> {
                val plug = Path().apply {
                    moveTo(w * 0.40f, h * 0.40f)
                    lineTo(w * 0.62f, h * 0.40f)
                    lineTo(w * 0.68f, h * 0.52f)
                    lineTo(w * 0.52f, h * 0.68f)
                    lineTo(w * 0.40f, h * 0.62f)
                    close()
                }
                drawPath(path = plug, brush = gradientBrush)

                drawLine(brush = gradientBrush, start = Offset(w * 0.52f, h * 0.38f), end = Offset(w * 0.60f, h * 0.22f), strokeWidth = 2.8.dp.toPx(), cap = StrokeCap.Round)
                drawLine(brush = gradientBrush, start = Offset(w * 0.64f, h * 0.48f), end = Offset(w * 0.72f, h * 0.32f), strokeWidth = 2.8.dp.toPx(), cap = StrokeCap.Round)

                val cord = Path().apply {
                    moveTo(w * 0.44f, h * 0.64f)
                    lineTo(w * 0.32f, h * 0.76f)
                    cubicTo(w * 0.26f, h * 0.82f, w * 0.20f, h * 0.74f, w * 0.26f, h * 0.68f)
                }
                drawPath(path = cord, brush = gradientBrush, style = stroke)

                val spark = Path().apply {
                    moveTo(w * 0.72f, h * 0.48f)
                    lineTo(w * 0.84f, h * 0.44f)
                    lineTo(w * 0.76f, h * 0.56f)
                    lineTo(w * 0.86f, h * 0.52f)
                }
                drawPath(path = spark, brush = gradientBrush, style = stroke)
            }

            HomeCategoryIconType.PLUMBING -> {
                val pipe = Path().apply {
                    moveTo(w * 0.40f, h * 0.24f)
                    lineTo(w * 0.52f, h * 0.24f)
                    lineTo(w * 0.52f, h * 0.54f)
                    lineTo(w * 0.72f, h * 0.54f)
                    lineTo(w * 0.72f, h * 0.66f)
                    lineTo(w * 0.40f, h * 0.66f)
                    close()
                }
                drawPath(path = pipe, brush = gradientBrush)

                drawRoundRect(
                    brush = gradientBrush,
                    topLeft = Offset(w * 0.36f, h * 0.20f),
                    size = androidx.compose.ui.geometry.Size(w * 0.20f, h * 0.08f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx())
                )

                drawRoundRect(
                    brush = gradientBrush,
                    topLeft = Offset(w * 0.70f, h * 0.50f),
                    size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx())
                )

                val drop = Path().apply {
                    moveTo(w * 0.46f, h * 0.74f)
                    lineTo(w * 0.51f, h * 0.84f)
                    cubicTo(w * 0.51f, h * 0.90f, w * 0.41f, h * 0.90f, w * 0.41f, h * 0.84f)
                    close()
                }
                drawPath(path = drop, brush = gradientBrush)
            }

            HomeCategoryIconType.AUTOMOTIVE -> {
                val carBody = Path().apply {
                    moveTo(w * 0.14f, h * 0.62f)
                    cubicTo(w * 0.14f, h * 0.56f, w * 0.22f, h * 0.50f, w * 0.32f, h * 0.48f)
                    lineTo(w * 0.42f, h * 0.36f)
                    cubicTo(w * 0.45f, h * 0.33f, w * 0.68f, h * 0.33f, w * 0.74f, h * 0.36f)
                    lineTo(w * 0.84f, h * 0.50f)
                    lineTo(w * 0.90f, h * 0.58f)
                    cubicTo(w * 0.92f, h * 0.62f, w * 0.90f, h * 0.68f, w * 0.84f, h * 0.68f)
                    lineTo(w * 0.18f, h * 0.68f)
                    close()
                }
                drawPath(path = carBody, brush = gradientBrush)

                val windows = Path().apply {
                    moveTo(w * 0.44f, h * 0.38f)
                    lineTo(w * 0.72f, h * 0.38f)
                    lineTo(w * 0.80f, h * 0.50f)
                    lineTo(w * 0.35f, h * 0.50f)
                    close()
                }
                drawPath(path = windows, color = PureWhite)

                drawCircle(color = PureWhite, radius = w * 0.10f, center = Offset(w * 0.32f, h * 0.68f))
                drawCircle(brush = gradientBrush, radius = w * 0.08f, center = Offset(w * 0.32f, h * 0.68f))
                drawCircle(color = PureWhite, radius = w * 0.035f, center = Offset(w * 0.32f, h * 0.68f))

                drawCircle(color = PureWhite, radius = w * 0.10f, center = Offset(w * 0.74f, h * 0.68f))
                drawCircle(brush = gradientBrush, radius = w * 0.08f, center = Offset(w * 0.74f, h * 0.68f))
                drawCircle(color = PureWhite, radius = w * 0.035f, center = Offset(w * 0.74f, h * 0.68f))
            }

            HomeCategoryIconType.FURNITURE -> {
                val backrest = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(w * 0.28f, h * 0.22f, w * 0.72f, h * 0.54f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    )
                }
                drawPath(path = backrest, brush = gradientBrush)

                val seat = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(w * 0.22f, h * 0.50f, w * 0.78f, h * 0.66f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    )
                }
                drawPath(path = seat, brush = gradientBrush)

                val armLeft = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(w * 0.16f, h * 0.38f, w * 0.26f, h * 0.64f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    )
                }
                drawPath(path = armLeft, brush = gradientBrush, style = stroke)

                val armRight = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(w * 0.74f, h * 0.38f, w * 0.84f, h * 0.64f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    )
                }
                drawPath(path = armRight, brush = gradientBrush, style = stroke)

                drawLine(brush = gradientBrush, start = Offset(w * 0.26f, h * 0.66f), end = Offset(w * 0.20f, h * 0.82f), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(brush = gradientBrush, start = Offset(w * 0.74f, h * 0.66f), end = Offset(w * 0.80f, h * 0.82f), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
            }

            HomeCategoryIconType.INSTALLATION -> {
                val drillBody = Path().apply {
                    moveTo(w * 0.28f, h * 0.28f)
                    lineTo(w * 0.68f, h * 0.28f)
                    lineTo(w * 0.68f, h * 0.44f)
                    lineTo(w * 0.46f, h * 0.44f)
                    lineTo(w * 0.42f, h * 0.68f)
                    lineTo(w * 0.30f, h * 0.68f)
                    lineTo(w * 0.34f, h * 0.44f)
                    lineTo(w * 0.28f, h * 0.44f)
                    close()
                }
                drawPath(path = drillBody, brush = gradientBrush)

                drawRoundRect(
                    brush = gradientBrush,
                    topLeft = Offset(w * 0.24f, h * 0.68f),
                    size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.12f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )

                drawRect(
                    brush = gradientBrush,
                    topLeft = Offset(w * 0.68f, h * 0.32f),
                    size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.08f)
                )
                drawLine(
                    brush = gradientBrush,
                    start = Offset(w * 0.76f, h * 0.36f),
                    end = Offset(w * 0.88f, h * 0.36f),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = PureWhite,
                    start = Offset(w * 0.44f, h * 0.46f),
                    end = Offset(w * 0.42f, h * 0.52f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun ActiveRequestBanner(
    request: ServiceRequest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = PureWhite,
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderGray, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.subService.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = request.address,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val (statusText, statusColor) = when (request.status) {
                RequestStatus.PENDING -> "Pending" to Color(0xFFF59E0B)
                RequestStatus.ACCEPTED -> "Accepted" to Color(0xFF3B82F6)
                RequestStatus.IN_PROGRESS -> "In Progress" to Color(0xFF8B5CF6)
                RequestStatus.COMPLETED -> "Rate Now" to Color(0xFF10B981)
                RequestStatus.RATED -> "Pay Now" to VibrantPink
                RequestStatus.PAID -> "Done" to Color(0xFF6B7280)
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = statusColor.copy(alpha = 0.12f),
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 780)
@Composable
fun NewHomeScreenReferencePreview() {
    HireMeTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 780)
@Composable
fun CategoriesGridViewPreview() {
    HireMeTheme {
        val categories = remember {
            listOf(
                HomeCategoryItem(CategoryIds.HOME_CARE, "Home Care", HomeCategoryIconType.HOME_CARE),
                HomeCategoryItem(CategoryIds.BEAUTY, "Beauty", HomeCategoryIconType.BEAUTY),
                HomeCategoryItem(CategoryIds.HEALTH_CARE, "Health Care", HomeCategoryIconType.HEALTH_CARE),
                HomeCategoryItem(CategoryIds.APPLIANCES, "Appliances", HomeCategoryIconType.APPLIANCES),
                HomeCategoryItem(CategoryIds.ELECTRIC, "Electric", HomeCategoryIconType.ELECTRIC),
                HomeCategoryItem(CategoryIds.PLUMBING, "Plumbing", HomeCategoryIconType.PLUMBING),
                HomeCategoryItem(CategoryIds.AUTOMOTIVE, "Automotive", HomeCategoryIconType.AUTOMOTIVE),
                HomeCategoryItem(CategoryIds.FURNITURE, "Furniture", HomeCategoryIconType.FURNITURE),
                HomeCategoryItem(CategoryIds.INSTALLATION, "Installation", HomeCategoryIconType.INSTALLATION)
            )
        }
        CategoriesGridView(
            categories = categories,
            onServiceClick = {},
            onRequestClick = {}
        )
    }
}
