@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.WordlandApplication
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.FogLevel
import com.wordland.domain.model.MapViewMode
import com.wordland.ui.components.FogOverlay
import com.wordland.ui.components.ViewModeTransition
import com.wordland.ui.uistate.WorldMapUiState
import com.wordland.ui.viewmodel.ViewTransitionState
import com.wordland.ui.viewmodel.WorldMapViewModel

/**
 * World Map Screen
 * Displays either traditional island list view or world map canvas view
 *
 * @param onNavigateBack Callback when user presses back
 * @param onNavigateToLevelSelect Callback when user selects an island
 * @param viewModel The WorldMapViewModel
 */
@Composable
fun WorldMapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelSelect: (String) -> Unit,
    viewModel: WorldMapViewModel =
        viewModel(
            factory = AppServiceLocator.provideFactory(),
        ),
) {
    // Initialize with user ID
    LaunchedEffect(Unit) {
        viewModel.initialize(WordlandApplication.USER_ID)
    }

    val uiState by viewModel.uiState.collectAsState()
    val transitionState by viewModel.viewTransitionState.collectAsState()

    Scaffold(
        topBar = {
            WorldMapAppBar(
                onNavigateBack = onNavigateBack,
                viewMode = (uiState as? WorldMapUiState.Ready)?.viewMode ?: MapViewMode.ISLAND_VIEW,
                onViewModeToggle = { viewModel.toggleViewModeWithAnimation() },
                transitionState = transitionState,
            )
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is WorldMapUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is WorldMapUiState.Ready -> {
                // Enhanced transition with slide animation (500ms)
                ViewModeTransition(
                    viewMode = state.viewMode,
                    transitionState = transitionState,
                    modifier = Modifier.fillMaxSize(),
                    islandContent = {
                        IslandViewContent(
                            state = state,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                            onRegionClick = { regionId ->
                                viewModel.onRegionTap(WordlandApplication.USER_ID, regionId)
                                onNavigateToLevelSelect(regionId)
                            },
                        )
                    },
                    worldContent = {
                        WorldMapViewContent(
                            state = state,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                            onRegionClick = { regionId ->
                                viewModel.onRegionTap(WordlandApplication.USER_ID, regionId)
                                onNavigateToLevelSelect(regionId)
                            },
                        )
                    },
                )
            }

            is WorldMapUiState.RegionSelected -> {
                // This would show a detail view for the selected region
                // For now, just navigate to level select
                LaunchedEffect(state.region.id) {
                    onNavigateToLevelSelect(state.region.islandId)
                }
            }

            is WorldMapUiState.Error -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "出错了",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        state.retry?.let { retry ->
                            Button(onClick = retry) {
                                Text("重试")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * App bar for World Map Screen with view mode toggle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorldMapAppBar(
    onNavigateBack: () -> Unit,
    viewMode: MapViewMode,
    onViewModeToggle: () -> Unit,
    transitionState: ViewTransitionState = ViewTransitionState(),
) {
    // Enhanced button animations
    val rotation by animateFloatAsState(
        targetValue = if (viewMode == MapViewMode.WORLD_VIEW) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "rotation",
    )

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (viewMode == MapViewMode.ISLAND_VIEW) "岛屿地图" else "WORLDLAND",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (viewMode == MapViewMode.ISLAND_VIEW) "🏝️" else "🌍",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        actions = {
            // Enhanced view toggle button with rotation animation
            val backgroundColor by animateColorAsState(
                targetValue =
                    if (viewMode == MapViewMode.WORLD_VIEW) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                animationSpec = tween(durationMillis = 300),
                label = "backgroundColor",
            )

            Surface(
                onClick = onViewModeToggle,
                color = backgroundColor,
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .graphicsLayer { rotationZ = rotation },
                    contentAlignment = Alignment.Center,
                ) {
                    // Animated emoji icon with smooth transition
                    AnimatedContent(
                        targetState = if (viewMode == MapViewMode.ISLAND_VIEW) "🏝️" else "🌍",
                        transitionSpec = {
                            fadeIn(
                                animationSpec = tween(200, easing = FastOutSlowInEasing),
                            ) togetherWith
                                fadeOut(
                                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                                )
                        },
                        label = "icon",
                    ) { icon ->
                        Text(
                            text = icon,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Help/info button (for future use)
            IconButton(
                onClick = {
                    // Future: show map legend or help
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Map Legend",
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    )
}

/**
 * Traditional island list view content
 */
@Composable
private fun IslandViewContent(
    state: WorldMapUiState.Ready,
    modifier: Modifier = Modifier,
    onRegionClick: (String) -> Unit,
) {
    LazyColumn(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "探索岛屿 🏝️",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // Exploration progress indicator
        item {
            val explorationPercent = (state.getExplorationPercentage() * 100).toInt()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "探索进度: $explorationPercent%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    LinearProgressIndicator(
                        progress = state.getExplorationPercentage(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                    )
                }
            }
        }

        items(state.regions) { region ->
            RegionCard(
                region = region,
                isExplored = state.explorationState?.hasExplored(region.id) == true,
                onClick = { onRegionClick(region.islandId) },
            )
        }
    }
}

/**
 * World map canvas view content
 */
@Composable
private fun WorldMapViewContent(
    state: WorldMapUiState.Ready,
    modifier: Modifier = Modifier,
    onRegionClick: (String) -> Unit,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .background(Color(0xFF87CEEB)), // Sky blue background
    ) {
        val mapWidth = maxWidth
        val mapHeight = maxHeight

        // Draw ocean
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOcean(size.width, size.height)
        }

        // Draw regions
        state.regions.forEach { region ->
            val isExplored = state.explorationState?.hasExplored(region.id) == true
            MapRegionItem(
                region = region,
                isExplored = isExplored,
                mapWidth = mapWidth,
                mapHeight = mapHeight,
                onClick = { onRegionClick(region.islandId) },
            )
        }

        // Draw fog overlay
        FogOverlay(
            regions = state.regions,
            mapWidth = mapWidth,
            mapHeight = mapHeight,
        )

        // Exploration stats overlay
        Card(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
            ) {
                Text(
                    text = "探索进度",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = "${(state.getExplorationPercentage() * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${state.explorationState?.exploredRegions?.size ?: 0}/${state.regions.size} 区域",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

/**
 * Individual map region item
 */
@Composable
private fun MapRegionItem(
    region: com.wordland.domain.model.MapRegion,
    isExplored: Boolean,
    mapWidth: androidx.compose.ui.unit.Dp,
    mapHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current

    // Convert normalized coordinates to pixels
    val x = with(density) { mapWidth.toPx() * region.position.x }
    val y = with(density) { mapHeight.toPx() * region.position.y }

    val regionSize = with(density) { 80.dp.toPx() }

    Box(
        modifier =
            Modifier
                .offset(
                    with(density) { (x - regionSize / 2).toDp() },
                    with(density) { (y - regionSize / 2).toDp() },
                )
                .size(with(density) { regionSize.toDp() })
                .clickable(
                    enabled = region.isUnlocked,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        // Region icon and name
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text =
                    when {
                        !region.isUnlocked -> "🔒"
                        region.fogLevel == FogLevel.HIDDEN -> "🌫️"
                        isExplored -> "✅"
                        else -> region.icon
                    },
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = region.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = if (isExplored) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

/**
 * Region card for island list view
 */
@Composable
private fun RegionCard(
    region: com.wordland.domain.model.MapRegion,
    isExplored: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = region.isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when {
                        !region.isUnlocked -> MaterialTheme.colorScheme.surfaceVariant
                        isExplored -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .background(
                            color =
                                if (isExplored) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text =
                        when {
                            !region.isUnlocked -> "🔒"
                            isExplored -> "✅"
                            else -> region.icon
                        },
                    style = MaterialTheme.typography.displaySmall,
                )
            }

            Spacer(Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = region.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isExplored) FontWeight.Bold else FontWeight.Normal,
                )
                Text(
                    text =
                        when {
                            !region.isUnlocked -> "未解锁"
                            isExplored -> "已探索"
                            else -> "可探索"
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Arrow
            if (region.isUnlocked) {
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Draw ocean background on canvas
 */
private fun DrawScope.drawOcean(
    width: Float,
    height: Float,
) {
    drawRect(Color(0xFF1E90FF)) // Ocean blue
}

/**
 * Draw fog overlay on canvas
 */
private fun DrawScope.drawFogOverlay(
    state: WorldMapUiState.Ready,
    width: Float,
    height: Float,
) {
    // Draw fog for hidden/locked regions
    state.regions.forEach { region ->
        if (region.fogLevel == FogLevel.HIDDEN || region.fogLevel == FogLevel.LOCKED) {
            val centerX = region.position.x * width
            val centerY = region.position.y * height
            val fogRadius = 100f

            drawCircle(
                color = Color.Gray.copy(alpha = 0.7f),
                radius = fogRadius,
                center = Offset(centerX, centerY),
            )
        }
    }
}
