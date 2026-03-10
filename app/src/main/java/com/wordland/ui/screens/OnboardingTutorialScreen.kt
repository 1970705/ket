package com.wordland.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.domain.model.OnboardingUiState
import com.wordland.ui.components.WordlandButton

/**
 * Onboarding Tutorial Screen - Placeholder
 *
 * TODO: compose-ui-designer-3 will implement the full UI
 */
@Composable
fun OnboardingTutorialScreen(
    viewModel: com.wordland.ui.viewmodel.OnboardingViewModel =
        viewModel(
            factory = AppServiceLocator.provideFactory(),
        ),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Restore state when entering screen
    LaunchedEffect(Unit) {
        // Always call startOnboarding to restore state from database
        viewModel.startOnboarding()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "教学关卡",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            when (val state = uiState) {
                is OnboardingUiState.Tutorial -> {
                    Text(
                        text = "单词 ${state.currentWordIndex + 1} / ${state.totalWords}",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = state.question.translation,
                                style = MaterialTheme.typography.displayMedium,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = state.question.getDisplayAnswer(),
                                style = MaterialTheme.typography.displayLarge.copy(letterSpacing = 8.sp),
                                color = MaterialTheme.colorScheme.primary,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "提示剩余: ${state.question.hintsRemaining}/3",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    var answer by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = answer,
                        onValueChange = {
                            answer = it
                            viewModel.updateAnswer(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("输入答案") },
                        singleLine = true,
                    )

                    WordlandButton(
                        onClick = {
                            viewModel.submitAnswer(answer)
                            answer = ""
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        text = "提交",
                    )
                }
                is OnboardingUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    Text("状态: ${uiState::class.simpleName}")
                }
            }
        }
    }
}
