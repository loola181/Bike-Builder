@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.roxballs.bikebuild

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private enum class BuilderStage {
    Intro,
    Assembly,
    Complete,
}

private data class BikeAnchor(
    val partId: String,
    val xFraction: Float,
    val yFraction: Float,
)

private data class ScenePartOverlay(
    val centerXFraction: Float,
    val centerYFraction: Float,
    val widthFraction: Float,
    val heightFraction: Float,
    val rotation: Float = 0f,
)

private val bikeAnchors = listOf(
    BikeAnchor("frame", 0.49f, 0.51f),
    BikeAnchor("fork", 0.67f, 0.50f),
    BikeAnchor("handlebar", 0.71f, 0.28f),
    BikeAnchor("seat", 0.35f, 0.28f),
    BikeAnchor("front_wheel", 0.77f, 0.73f),
    BikeAnchor("rear_wheel", 0.23f, 0.73f),
    BikeAnchor("pedals", 0.48f, 0.61f),
    BikeAnchor("chain", 0.36f, 0.66f),
    BikeAnchor("brakes", 0.83f, 0.53f),
)

@Composable
fun BikeBuilderApp() {
    val scenario = remember { defaultBikeScenario() }
    val allPartIds = remember(scenario) { scenario.steps.map { it.part.id } }

    var stage by rememberSaveable { mutableStateOf(BuilderStage.Intro) }
    var currentStepIndex by rememberSaveable { mutableIntStateOf(0) }
    var installedPartIds by rememberSaveable { mutableStateOf(listOf<String>()) }
    var mistakes by rememberSaveable { mutableIntStateOf(0) }
    var message by rememberSaveable { mutableStateOf(scenario.intro) }

    val currentStep = scenario.steps.getOrNull(currentStepIndex)
    val progress = if (scenario.steps.isEmpty()) 0f else installedPartIds.size.toFloat() / scenario.steps.size.toFloat()

    fun resetToIntro() {
        stage = BuilderStage.Intro
        currentStepIndex = 0
        installedPartIds = emptyList()
        mistakes = 0
        message = scenario.intro
    }

    fun startAssembly() {
        stage = BuilderStage.Assembly
        currentStepIndex = 0
        installedPartIds = emptyList()
        mistakes = 0
        message = "Шаг 1. Начни с рамы. Она станет основой велосипеда."
    }

    fun handlePartClick(part: BikePart) {
        val step = currentStep ?: return
        if (part.id in installedPartIds) return

        if (part.id == step.part.id) {
            val nextInstalled = installedPartIds + part.id
            val nextIndex = currentStepIndex + 1
            installedPartIds = nextInstalled

            if (nextIndex >= scenario.steps.size) {
                stage = BuilderStage.Complete
                message = "Готово. Велосипед собран и готов к поездке."
            } else {
                currentStepIndex = nextIndex
                val nextPart = scenario.steps[nextIndex].part.title
                message = "Готово. ${step.part.title} уже на месте. Теперь ставим: $nextPart."
            }
        } else {
            mistakes += 1
            message = "Пока нужна деталь «${step.part.title}». Она подсвечена на сцене и внизу."
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (stage) {
            BuilderStage.Intro -> IntroScreen(
                scenario = scenario,
                allPartIds = allPartIds,
                onStart = ::startAssembly,
            )
            BuilderStage.Assembly -> AssemblyScreen(
                scenario = scenario,
                currentStep = currentStep,
                installedPartIds = installedPartIds,
                mistakes = mistakes,
                progress = progress,
                message = message,
                onPartClick = ::handlePartClick,
            )
            BuilderStage.Complete -> CompletionScreen(
                scenario = scenario,
                allPartIds = allPartIds,
                installedPartIds = installedPartIds,
                mistakes = mistakes,
                onRestart = ::startAssembly,
                onReset = ::resetToIntro,
            )
        }
    }
}

@Composable
private fun IntroScreen(
    scenario: BikeAssemblyScenario,
    allPartIds: List<String>,
    onStart: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                shadowElevation = 14.dp,
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                    ) {
                        Text("Начать сборку")
                    }
                    Text(
                        text = "${allPartIds.size} шагов • без сложных жестов • одна деталь за раз",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color(0xFFF3E7D2),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                )
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IntroHeroCard(
                scenario = scenario,
                allPartIds = allPartIds,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FeatureCard(
                    title = "Пошагово",
                    text = "Каждый раз только одна нужная деталь.",
                    modifier = Modifier.weight(1f),
                )
                FeatureCard(
                    title = "Понятно",
                    text = "Нужная деталь показана крупно и ставится одним нажатием.",
                    modifier = Modifier.weight(1f),
                )
            }

            HowToCard(
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun AssemblyScreen(
    scenario: BikeAssemblyScenario,
    currentStep: AssemblyStep?,
    installedPartIds: List<String>,
    mistakes: Int,
    progress: Float,
    message: String,
    onPartClick: (BikePart) -> Unit,
) {
    val installedSet = remember(installedPartIds) { installedPartIds.toSet() }
    val currentPartId = currentStep?.part?.id

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Сборка велосипеда") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color(0xFFF1E2C9),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                )
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CompactStatusBar(
                stepIndex = installedPartIds.size + 1,
                totalSteps = scenario.steps.size,
                installedCount = installedPartIds.size,
                mistakes = mistakes,
                progress = progress,
                message = message,
            )

            StepGuideCard(
                step = currentStep,
                onInstall = {
                    currentStep?.let { onPartClick(it.part) }
                },
            )

            BikeSceneCard(
                currentPartId = currentPartId,
                currentPartTitle = currentStep?.part?.title,
                installedPartIds = installedSet,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CompletionScreen(
    scenario: BikeAssemblyScenario,
    allPartIds: List<String>,
    installedPartIds: List<String>,
    mistakes: Int,
    onRestart: () -> Unit,
    onReset: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Сборка завершена") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 14.dp,
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                ) {
                    Text("Собрать еще раз")
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color(0xFFF0E1C7),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                )
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF27462E),
                                    Color(0xFF182E21),
                                ),
                            ),
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        color = Color(0xFFF3A13A),
                        shape = MaterialTheme.shapes.extraLarge,
                    ) {
                        Text(
                            text = "Готово",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF1F2522),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "Велосипед собран",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFFFAF2),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Ты прошел весь маршрут от рамы до тормозов. Теперь на сцене стоит полностью собранный велосипед.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFF7EEDC).copy(alpha = 0.88f),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AssemblyMetricTile(
                            title = "деталей",
                            value = "${installedPartIds.size}/${scenario.steps.size}",
                            modifier = Modifier.weight(1f),
                        )
                        AssemblyMetricTile(
                            title = "ошибки",
                            value = mistakes.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        AssemblyMetricTile(
                            title = "статус",
                            value = "100%",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            BikeSceneCard(
                currentPartId = null,
                currentPartTitle = null,
                installedPartIds = allPartIds.toSet(),
                modifier = Modifier.fillMaxWidth(),
            )

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Что ты собрал",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Все основные узлы уже стоят на месте. Ниже можно быстро пробежать глазами по деталям, которые ты поставил.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.66f),
                        shape = MaterialTheme.shapes.extraLarge,
                    ) {
                        Text(
                            text = scenario.steps.joinToString(separator = " • ") { it.part.title },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    scenario.steps.chunked(4).forEach { rowSteps ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            rowSteps.forEach { step ->
                                CompletionPartTile(
                                    part = step.part,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(4 - rowSteps.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("На старт")
            }
        }
    }
}

@Composable
private fun IntroHeroCard(
    scenario: BikeAssemblyScenario,
    allPartIds: List<String>,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Text(
                    text = "Легкий старт",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(
                text = "Собери свой первый велосипед",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Понятный тренажер без перетаскивания и перегруза. На каждом шаге показана только одна нужная деталь.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PreviewBikePanel(allPartIds = allPartIds)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IntroMetric(
                    title = "шагов",
                    value = allPartIds.size.toString(),
                    modifier = Modifier.weight(1f),
                )
                IntroMetric(
                    title = "нажатие на шаг",
                    value = "1",
                    modifier = Modifier.weight(1f),
                )
                IntroMetric(
                    title = "лишних жестов",
                    value = "0",
                    modifier = Modifier.weight(1f),
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = scenario.subtitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Стартуем с рамы и доходим до тормозов. Ошибиться не страшно: приложение мягко подскажет следующий шаг.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewBikePanel(allPartIds: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(312.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF5B7D49),
                            Color(0xFF416937),
                            Color(0xFF254A28),
                        ),
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_art),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = Color(0xFFF8F0DE).copy(alpha = 0.94f),
                shadowElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Новый стиль",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Стартовый экран в тоне новой иконки.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = Color(0xFF1F2F20).copy(alpha = 0.82f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Что тебя ждет",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFF8F0DE),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Рама → Вилка → Руль → Колеса → Привод → Тормоза",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF8F0DE).copy(alpha = 0.9f),
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = Color(0xFFF3A13A).copy(alpha = 0.95f),
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = allPartIds.size.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF1F2522),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "деталей",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1F2522),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactStatusBar(
    stepIndex: Int,
    totalSteps: Int,
    installedCount: Int,
    mistakes: Int,
    progress: Float,
    message: String,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF26452D),
                            Color(0xFF1E3327),
                        ),
                    ),
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Сборка в процессе",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFF7EEDC),
                    )
                    Text(
                        text = "Шаг ${stepIndex.coerceAtMost(totalSteps)} из $totalSteps",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFFFFAF2),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Surface(
                    color = Color(0xFFF3A13A),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1F2522),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            AnimatedContent(
                targetState = message,
                label = "statusMessage",
            ) { currentMessage ->
                Text(
                    text = currentMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFF7EEDC).copy(alpha = 0.88f),
                    maxLines = 3,
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF3A13A),
                trackColor = Color.White.copy(alpha = 0.16f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AssemblyMetricTile(
                    title = "Готово",
                    value = installedCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                AssemblyMetricTile(
                    title = "Осталось",
                    value = (totalSteps - installedCount).coerceAtLeast(0).toString(),
                    modifier = Modifier.weight(1f),
                )
                AssemblyMetricTile(
                    title = "Ошибки",
                    value = mistakes.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BikeSceneCard(
    currentPartId: String?,
    currentPartTitle: String?,
    installedPartIds: Set<String>,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = if (currentPartTitle == null) "Собранный велосипед" else "Куда встанет деталь",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (currentPartTitle == null) {
                            "Все узлы уже стоят на своих местах."
                        } else {
                            "Смотри на оранжевую подсветку. Она показывает место для детали «$currentPartTitle»."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    color = if (currentPartId == null) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Text(
                        text = if (currentPartId == null) "Готово" else "Подсказка",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = Color.Transparent,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(224.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF8EEDC),
                                    Color(0xFFF2E1BE),
                                    Color(0xFFE4C894),
                                ),
                            ),
                            shape = MaterialTheme.shapes.extraLarge,
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawWorkshopBackdrop()
                    }
                    StudioBikeScene(
                        currentPartId = currentPartId,
                        installedPartIds = installedPartIds,
                        modifier = Modifier.matchParentSize(),
                        previewMode = false,
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.66f),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentPartId != null) {
                        PartArtworkCard(
                            partId = currentPartId,
                            modifier = Modifier.size(48.dp),
                            containerColor = Color.White.copy(alpha = 0.72f),
                        )
                    }
                    Text(
                        text = if (currentPartId == null) {
                            "Все точки закрыты. Велосипед собран."
                        } else {
                            "Это только подсказка по месту установки. Основное действие выше: нажми на картинку детали или на кнопку установки."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioBikeScene(
    currentPartId: String?,
    installedPartIds: Set<String>,
    modifier: Modifier = Modifier,
    previewMode: Boolean,
) {
    val glowColor = MaterialTheme.colorScheme.secondary
    val glowCoreColor = MaterialTheme.colorScheme.onSecondaryContainer
    val installedColor = MaterialTheme.colorScheme.primary
    val pulse = rememberInfiniteTransition(label = "bikeGlow")
    val glowScale by pulse.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 960, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowScale",
    )
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.46f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 960, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )

    BoxWithConstraints(modifier = modifier) {
        val activeAnchor = bikeAnchors.firstOrNull { it.partId == currentPartId }
        val installedAnchors = bikeAnchors.filter { it.partId in installedPartIds }
        val currentOverlay = currentPartId?.let(::scenePartOverlay)

        // Stock silhouette source:
        // https://openclipart.org/detail/256522/high-quality-bicycle-silhouette
        Image(
            painter = painterResource(id = R.drawable.bike_stock),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .padding(
                    horizontal = if (previewMode) 8.dp else 6.dp,
                    vertical = if (previewMode) 14.dp else 10.dp,
                ),
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(
                Color(0xFF374148).copy(alpha = if (previewMode) 0.95f else 0.88f),
            ),
        )

        if (!previewMode && installedAnchors.isNotEmpty()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                installedAnchors.forEach { anchor ->
                    val center = Offset(
                        x = size.width * anchor.xFraction,
                        y = size.height * anchor.yFraction,
                    )
                    drawCircle(
                        color = installedColor.copy(alpha = 0.16f),
                        radius = size.minDimension * 0.04f,
                        center = center,
                    )
                    drawCircle(
                        color = installedColor,
                        radius = size.minDimension * 0.015f,
                        center = center,
                    )
                }
            }
        }

        if (!previewMode && currentPartId != null && currentOverlay != null) {
            Image(
                painter = painterResource(id = partArtworkRes(currentPartId)),
                contentDescription = null,
                modifier = Modifier
                    .width(maxWidth * currentOverlay.widthFraction)
                    .height(maxHeight * currentOverlay.heightFraction)
                    .align(Alignment.TopStart)
                    .offset(
                        x = maxWidth * currentOverlay.centerXFraction - (maxWidth * currentOverlay.widthFraction) / 2f,
                        y = maxHeight * currentOverlay.centerYFraction - (maxHeight * currentOverlay.heightFraction) / 2f,
                    )
                    .rotate(currentOverlay.rotation)
                    .alpha(0.88f),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(glowColor.copy(alpha = 0.95f)),
            )
        }

        if (activeAnchor != null) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val center = Offset(
                    x = size.width * activeAnchor.xFraction,
                    y = size.height * activeAnchor.yFraction,
                )
                drawCircle(
                    color = glowColor.copy(alpha = glowAlpha),
                    radius = size.minDimension * 0.06f * glowScale,
                    center = center,
                )
                drawCircle(
                    color = glowColor.copy(alpha = glowAlpha + 0.18f),
                    radius = size.minDimension * 0.095f * glowScale,
                    center = center,
                    style = Stroke(width = 6f),
                )
                drawCircle(
                    color = glowCoreColor.copy(alpha = 0.95f),
                    radius = size.minDimension * 0.012f,
                    center = center,
                )
            }

        }
    }
}

private fun scenePartOverlay(partId: String): ScenePartOverlay? = when (partId) {
    "frame" -> ScenePartOverlay(0.46f, 0.47f, 0.31f, 0.16f, -7f)
    "fork" -> ScenePartOverlay(0.68f, 0.49f, 0.12f, 0.28f, 0f)
    "handlebar" -> ScenePartOverlay(0.72f, 0.26f, 0.18f, 0.08f, 0f)
    "seat" -> ScenePartOverlay(0.34f, 0.28f, 0.15f, 0.09f, -4f)
    "front_wheel" -> ScenePartOverlay(0.77f, 0.66f, 0.25f, 0.25f)
    "rear_wheel" -> ScenePartOverlay(0.23f, 0.66f, 0.25f, 0.25f)
    "pedals" -> ScenePartOverlay(0.49f, 0.61f, 0.16f, 0.16f, 6f)
    "chain" -> ScenePartOverlay(0.38f, 0.66f, 0.16f, 0.08f, 0f)
    "brakes" -> ScenePartOverlay(0.82f, 0.54f, 0.12f, 0.12f, 0f)
    else -> null
}

private fun DrawScope.drawWorkshopBackdrop() {
    val benchTop = size.height * 0.18f
    drawCircle(
        color = Color.White.copy(alpha = 0.22f),
        radius = size.minDimension * 0.16f,
        center = Offset(size.width * 0.13f, size.height * 0.18f),
    )
    drawRect(
        color = Color(0xFFD6BE94).copy(alpha = 0.36f),
        topLeft = Offset(0f, size.height * 0.86f),
        size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.2f),
    )
    drawLine(
        color = Color.White.copy(alpha = 0.28f),
        start = Offset(size.width * 0.08f, benchTop),
        end = Offset(size.width * 0.22f, benchTop),
        strokeWidth = 8f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Color.White.copy(alpha = 0.28f),
        start = Offset(size.width * 0.8f, size.height * 0.22f),
        end = Offset(size.width * 0.92f, size.height * 0.22f),
        strokeWidth = 8f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawBikeIllustration(
    currentPartId: String?,
    installedPartIds: Set<String>,
    previewMode: Boolean,
) {
    val rearWheel = Offset(size.width * 0.23f, size.height * 0.73f)
    val frontWheel = Offset(size.width * 0.77f, size.height * 0.73f)
    val wheelRadius = size.minDimension * 0.18f
    val seatTop = Offset(size.width * 0.36f, size.height * 0.35f)
    val headTop = Offset(size.width * 0.60f, size.height * 0.33f)
    val headBottom = Offset(size.width * 0.64f, size.height * 0.47f)
    val bottomBracket = Offset(size.width * 0.47f, size.height * 0.61f)
    val handlebarCenter = Offset(size.width * 0.71f, size.height * 0.28f)
    val frontBrake = Offset(size.width * 0.82f, size.height * 0.53f)
    val rearSprocket = Offset(size.width * 0.33f, size.height * 0.67f)

    val ghost = Color(0xFF67727B).copy(alpha = if (previewMode) 0.4f else 0.28f)
    val steel = Color(0xFF47535C)
    val active = Color(0xFFCA7A26)
    val installed = Color(0xFF4E7B4C)
    val tire = Color(0xFF5B6068)
    val shadow = Color(0x992B2E33)

    fun colorFor(partId: String): Color = when {
        partId in installedPartIds -> installed
        partId == currentPartId -> active
        else -> ghost
    }

    drawOval(
        color = shadow.copy(alpha = 0.12f),
        topLeft = Offset(size.width * 0.16f, size.height * 0.82f),
        size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.08f),
    )

    drawWheel(rearWheel, wheelRadius, colorFor("rear_wheel"), tire)
    drawWheel(frontWheel, wheelRadius, colorFor("front_wheel"), tire)

    val frameStroke = 15f
    drawLine(colorFor("frame"), rearWheel, bottomBracket, strokeWidth = frameStroke, cap = StrokeCap.Round)
    drawLine(colorFor("frame"), bottomBracket, headBottom, strokeWidth = frameStroke, cap = StrokeCap.Round)
    drawLine(colorFor("frame"), seatTop, headTop, strokeWidth = frameStroke, cap = StrokeCap.Round)
    drawLine(colorFor("frame"), rearWheel, seatTop, strokeWidth = frameStroke, cap = StrokeCap.Round)
    drawLine(colorFor("frame"), seatTop, bottomBracket, strokeWidth = frameStroke, cap = StrokeCap.Round)

    drawLine(colorFor("fork"), headBottom, frontWheel, strokeWidth = 12f, cap = StrokeCap.Round)
    drawLine(colorFor("fork"), headTop, headBottom, strokeWidth = 12f, cap = StrokeCap.Round)

    drawLine(colorFor("handlebar"), headTop, handlebarCenter, strokeWidth = 10f, cap = StrokeCap.Round)
    drawLine(
        colorFor("handlebar"),
        Offset(handlebarCenter.x - 40f, handlebarCenter.y + 6f),
        Offset(handlebarCenter.x + 30f, handlebarCenter.y - 4f),
        strokeWidth = 10f,
        cap = StrokeCap.Round,
    )

    drawLine(colorFor("seat"), seatTop, Offset(seatTop.x, seatTop.y - 34f), strokeWidth = 9f, cap = StrokeCap.Round)
    drawLine(
        colorFor("seat"),
        Offset(seatTop.x - 28f, seatTop.y - 38f),
        Offset(seatTop.x + 16f, seatTop.y - 38f),
        strokeWidth = 10f,
        cap = StrokeCap.Round,
    )

    drawCircle(
        color = steel.copy(alpha = 0.18f),
        radius = 28f,
        center = bottomBracket,
    )
    drawCircle(
        color = colorFor("pedals"),
        radius = 18f,
        center = bottomBracket,
        style = Stroke(width = 8f),
    )
    drawLine(
        colorFor("pedals"),
        Offset(bottomBracket.x - 34f, bottomBracket.y - 18f),
        Offset(bottomBracket.x + 34f, bottomBracket.y + 18f),
        strokeWidth = 8f,
        cap = StrokeCap.Round,
    )

    drawLine(
        colorFor("chain"),
        Offset(bottomBracket.x + 18f, bottomBracket.y + 8f),
        Offset(rearSprocket.x + 18f, rearSprocket.y + 6f),
        strokeWidth = 7f,
        cap = StrokeCap.Round,
    )
    drawLine(
        colorFor("chain"),
        Offset(bottomBracket.x + 10f, bottomBracket.y - 10f),
        Offset(rearSprocket.x + 16f, rearSprocket.y - 8f),
        strokeWidth = 7f,
        cap = StrokeCap.Round,
    )

    drawCircle(
        color = colorFor("chain"),
        radius = 12f,
        center = rearSprocket,
        style = Stroke(width = 5f),
    )

    drawArc(
        color = colorFor("brakes"),
        startAngle = -35f,
        sweepAngle = 80f,
        useCenter = false,
        topLeft = Offset(frontBrake.x - 26f, frontBrake.y - 26f),
        size = androidx.compose.ui.geometry.Size(52f, 52f),
        style = Stroke(width = 7f, cap = StrokeCap.Round),
    )
    drawLine(
        colorFor("brakes"),
        headTop,
        frontBrake,
        strokeWidth = 4f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawWheel(
    center: Offset,
    radius: Float,
    accentColor: Color,
    tireColor: Color,
) {
    drawCircle(
        color = tireColor.copy(alpha = 0.22f),
        radius = radius + 10f,
        center = center,
        style = Stroke(width = 12f),
    )
    drawCircle(
        color = accentColor,
        radius = radius,
        center = center,
        style = Stroke(width = 11f),
    )
    repeat(7) { index ->
        val angle = index * 360f / 7f
        val end = Offset(
            x = center.x + kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * radius * 0.9f,
            y = center.y + kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * radius * 0.9f,
        )
        drawLine(
            color = accentColor.copy(alpha = 0.78f),
            start = center,
            end = end,
            strokeWidth = 3.5f,
            cap = StrokeCap.Round,
        )
    }
    drawCircle(
        color = accentColor,
        radius = 10f,
        center = center,
    )
}

@Composable
private fun StepGuideCard(
    step: AssemblyStep?,
    onInstall: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        if (step == null) {
            Text(
                text = "Сборка завершена.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFFAF2),
                                Color(0xFFF6E9D0),
                            ),
                        ),
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = Color(0xFFF3A13A).copy(alpha = 0.96f),
                        shape = MaterialTheme.shapes.extraLarge,
                    ) {
                        Text(
                            text = "Сейчас ставим",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF1F2522),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    StatusChip(text = step.part.description)
                }

                Text(
                    text = step.part.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = step.instruction,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onInstall),
                    color = Color.White.copy(alpha = 0.72f),
                    shape = MaterialTheme.shapes.extraLarge,
                    shadowElevation = 4.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PartArtworkCard(
                            partId = step.part.id,
                            modifier = Modifier.size(118.dp),
                            containerColor = Color.White.copy(alpha = 0.82f),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Нажми на картинку детали",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = step.part.purpose,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.74f),
                                shape = MaterialTheme.shapes.extraLarge,
                            ) {
                                Text(
                                    text = "Один шаг = одно нажатие",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onInstall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                ) {
                    Text("Поставить ${step.part.title.lowercase()}")
                }
            }
        }
    }
}

@Composable
private fun AssemblyMetricTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFFFAF2),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF7EEDC).copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HowToCard(
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Как это работает",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            HowToRow(number = "1", text = "Смотри на большую картинку детали. Это и есть текущий шаг.")
            HowToRow(number = "2", text = "Нажимай на картинку или на кнопку установки. Ничего тянуть не нужно.")
            HowToRow(number = "3", text = "Сцена велосипеда внизу просто показывает, куда встала деталь.")
        }
    }
}

@Composable
private fun HowToRow(
    number: String,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Text(
                text = number,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IntroMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CompletionPartTile(
    part: BikePart,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFFFFAF2),
        shape = MaterialTheme.shapes.large,
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PartArtworkCard(
                partId = part.id,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            )
            Text(
                text = part.title,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun PartArtworkCard(
    partId: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = MaterialTheme.shapes.large,
    ) {
        Image(
            painter = painterResource(id = partArtworkRes(partId)),
            contentDescription = null,
            modifier = Modifier.padding(7.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@DrawableRes
private fun partArtworkRes(partId: String): Int = when (partId) {
    "frame" -> R.drawable.part_stock_frame
    "fork" -> R.drawable.part_stock_fork
    "handlebar" -> R.drawable.part_stock_handlebar
    "seat" -> R.drawable.part_stock_seat
    "front_wheel", "rear_wheel" -> R.drawable.part_stock_wheel
    "pedals" -> R.drawable.part_stock_pedals
    "chain" -> R.drawable.part_stock_chain
    "brakes" -> R.drawable.part_stock_brakes
    else -> R.drawable.part_stock_frame
}
