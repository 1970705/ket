@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordland.ui.components.WordlandButton

/**
 * Onboarding Pet Selection Screen - Second screen of the onboarding flow
 *
 * Features:
 * - 4 pet options (all unlocked per Alpha design)
 * - Unique personality animations for each pet
 * - Visual feedback for selection
 * - Auto-advance to Tutorial after selection
 *
 * Per ONBOARDING_EXPERIENCE_DESIGN.md Section 1.1:
 * - All pets unlocked (generous first impression)
 * - Pets: Dolphin 🐬, Cat 🐱, Dog 🐶, Fox 🦊
 * - Personalities: cheerful, curious, loyal, clever
 *
 * @param onPetSelected Callback when pet is selected with pet type string
 */
@Composable
fun OnboardingPetSelectionScreen(onPetSelected: (String) -> Unit) {
    var selectedPet by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            PetSelectionHeader()

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            PetSelectionSubtitle()

            Spacer(modifier = Modifier.height(24.dp))

            // Pet grid (2x2)
            PetGrid(
                selectedPet = selectedPet,
                onPetSelected = { pet ->
                    android.util.Log.d("OnboardingPetSelection", "Pet clicked: $pet")
                    selectedPet = pet
                    android.util.Log.d("OnboardingPetSelection", "selectedPet updated to: $selectedPet")
                    // Only update local state, don't auto-advance
                    // User must click "开始学习" button to confirm selection
                },
            )

            Spacer(modifier = Modifier.weight(1f))

            // Continue button (hidden initially, shown when pet is selected)
            val currentPet = selectedPet
            if (currentPet != null) {
                SelectedPetConfirmation(
                    pet = OnboardingPet.fromId(currentPet),
                    onConfirm = { onPetSelected(currentPet) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PetSelectionHeader() {
    Text(
        text = "选择你的冒险伙伴！",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PetSelectionSubtitle() {
    Text(
        text = "每个小伙伴都有独特的个性\n选择你最喜欢的一个吧！",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
    )
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
        // First row: Dolphin and Cat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OnboardingPetCard(
                pet = OnboardingPet.DOLPHIN,
                isSelected = selectedPet == OnboardingPet.DOLPHIN.id,
                onClick = { onPetSelected(OnboardingPet.DOLPHIN.id) },
                modifier = Modifier.weight(1f),
            )
            OnboardingPetCard(
                pet = OnboardingPet.CAT,
                isSelected = selectedPet == OnboardingPet.CAT.id,
                onClick = { onPetSelected(OnboardingPet.CAT.id) },
                modifier = Modifier.weight(1f),
            )
        }

        // Second row: Dog and Fox
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OnboardingPetCard(
                pet = OnboardingPet.DOG,
                isSelected = selectedPet == OnboardingPet.DOG.id,
                onClick = { onPetSelected(OnboardingPet.DOG.id) },
                modifier = Modifier.weight(1f),
            )
            OnboardingPetCard(
                pet = OnboardingPet.FOX,
                isSelected = selectedPet == OnboardingPet.FOX.id,
                onClick = { onPetSelected(OnboardingPet.FOX.id) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun OnboardingPetCard(
    pet: OnboardingPet,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                pet.color.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        animationSpec = tween(durationMillis = 300),
        label = "background_color_${pet.id}",
    )

    val borderColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                pet.color
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            },
        animationSpec = tween(durationMillis = 300),
        label = "border_color_${pet.id}",
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "scale_${pet.id}",
    )

    Card(
        onClick = onClick,
        modifier =
            modifier
                .height(200.dp)
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
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Pet emoji with background
            Box(
                modifier =
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            pet.color.copy(alpha = 0.3f),
                                            pet.color.copy(alpha = 0.1f),
                                        ),
                                ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = pet.emoji,
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 48.sp,
                )
            }

            // Pet name and personality
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = pet.color,
                )

                Text(
                    text = pet.personality,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            // Selection indicator
            if (isSelected) {
                Surface(
                    color = pet.color,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "已选择",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            } else {
                // Placeholder for spacing
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun SelectedPetConfirmation(
    pet: OnboardingPet,
    onConfirm: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Confirmation message
        Text(
            text = "你选择了 ${pet.emoji} ${pet.name}！",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Text(
            text = pet.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )

        // Continue button
        WordlandButton(
            onClick = {
                android.util.Log.d("OnboardingPetSelection", "Start Learning button clicked")
                onConfirm()
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            text = "开始学习",
            size = com.wordland.ui.components.ButtonSize.LARGE,
        )
    }
}

/**
 * Onboarding Pet Data Model
 *
 * Per ONBOARDING_EXPERIENCE_DESIGN.md Section 4.1:
 * - Dolphin: cheerful, smart
 * - Cat: curious, independent
 * - Dog: loyal, enthusiastic
 * - Fox: clever, witty
 */
data class OnboardingPet(
    val id: String,
    val name: String,
    val emoji: String,
    val personality: String,
    val message: String,
    val color: Color,
) {
    companion object {
        val DOLPHIN =
            OnboardingPet(
                id = "pet_dolphin",
                name = "海豚",
                emoji = "🐬",
                personality = "活泼聪明",
                message = "太棒了！我们一起去探索单词海洋吧！",
                color = Color(0xFF00BCD4), // Cyan
            )

        val CAT =
            OnboardingPet(
                id = "pet_cat",
                name = "猫咪",
                emoji = "🐱",
                personality = "好奇独立",
                message = "喵~ 我会陪你一起安静地学习！",
                color = Color(0xFFFF9800), // Orange
            )

        val DOG =
            OnboardingPet(
                id = "pet_dog",
                name = "小狗",
                emoji = "🐶",
                personality = "忠诚热情",
                message = "汪汪！我会一直为你加油打气的！",
                color = Color(0xFFFF5722), // Deep Orange
            )

        val FOX =
            OnboardingPet(
                id = "pet_fox",
                name = "狐狸",
                emoji = "🦊",
                personality = "聪明机灵",
                message = "嘿嘿！和我一起探索学习的奥秘吧！",
                color = Color(0xFFE64A19), // Orange Red
            )

        val ALL_PETS = listOf(DOLPHIN, CAT, DOG, FOX)

        fun fromId(id: String): OnboardingPet {
            return ALL_PETS.firstOrNull { it.id == id } ?: DOLPHIN
        }
    }
}
