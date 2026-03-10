@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordland.data.repository.PetRepositoryImpl
import com.wordland.ui.components.WordlandButton
import com.wordland.ui.theme.AccentOrange
import com.wordland.ui.theme.AccentPink
import com.wordland.ui.theme.MoveValleyBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Pet selection screen for onboarding
 * User can select a pet companion to accompany them on their learning journey
 */
@Composable
fun PetSelectionScreen(
    onPetSelected: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val petRepository = remember { PetRepositoryImpl(context) }

    var selectedPet by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            PetSelectionAppBar(onNavigateBack = onNavigateBack)
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title and subtitle
            HeaderSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Pet grid
            PetGrid(
                selectedPet = selectedPet,
                onPetSelected = { petId ->
                    selectedPet = petId
                },
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Confirm button
            WordlandButton(
                onClick = {
                    selectedPet?.let { petId ->
                        // Save pet selection
                        scope.launch(Dispatchers.IO) {
                            petRepository.saveSelectedPet(petId)
                        }
                        // Navigate to next screen
                        onPetSelected(petId)
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                enabled = selectedPet != null,
                text = "开始学习",
                size = com.wordland.ui.components.ButtonSize.LARGE,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PetSelectionAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "选择小伙伴",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "返回",
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

@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "选择你的小伙伴",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "它将陪伴你学习英语",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PetGrid(
    selectedPet: String?,
    onPetSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PetCard(
                pet = Pet.CAT,
                isSelected = selectedPet == Pet.CAT.id,
                onClick = { onPetSelected(Pet.CAT.id) },
                modifier = Modifier.weight(1f),
            )
            PetCard(
                pet = Pet.DOG,
                isSelected = selectedPet == Pet.DOG.id,
                onClick = { onPetSelected(Pet.DOG.id) },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PetCard(
                pet = Pet.BIRD,
                isSelected = selectedPet == Pet.BIRD.id,
                onClick = { onPetSelected(Pet.BIRD.id) },
                modifier = Modifier.weight(1f),
            )

            // Placeholder for balance (or could add more pets later)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PetCard(
    pet: Pet,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                pet.color.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor",
    )

    val borderColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                pet.color
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        animationSpec = tween(durationMillis = 300),
        label = "borderColor",
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "scale",
    )

    Card(
        onClick = onClick,
        modifier =
            modifier
                .height(180.dp)
                .scale(scale),
        border =
            BorderStroke(
                width = if (isSelected) 3.dp else 1.dp,
                color = borderColor,
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 8.dp else 2.dp,
            ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Pet emoji
            Text(
                text = pet.emoji,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.size(72.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pet name
            Text(
                text = pet.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = pet.color,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Pet description
            Text(
                text = pet.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // Selection indicator
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = pet.color,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "已选择",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * Pet data class representing available companion pets
 */
data class Pet(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val color: Color,
) {
    companion object {
        val CAT =
            Pet(
                id = "pet_cat",
                name = "猫咪",
                emoji = "🐱",
                description = "优雅聪明\n喜欢安静学习",
                color = AccentPink,
            )

        val DOG =
            Pet(
                id = "pet_dog",
                name = "狗狗",
                emoji = "🐶",
                description = "热情友好\n为你加油打气",
                color = AccentOrange,
            )

        val BIRD =
            Pet(
                id = "pet_bird",
                name = "小鸟",
                emoji = "🐦",
                description = "活泼可爱\n陪你快乐学习",
                color = MoveValleyBlue,
            )

        val ALL_PETS = listOf(CAT, DOG, BIRD)
    }
}
