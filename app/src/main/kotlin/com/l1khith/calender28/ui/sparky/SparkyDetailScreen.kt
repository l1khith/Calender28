package com.l1khith.calender28.ui.sparky

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.data.EvolutionStage
import com.l1khith.calender28.data.PersonalityTrait
import com.l1khith.calender28.data.SparkyAchievement
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.SoundEffectHelper
import com.l1khith.calender28.viewmodel.SparkyUiEvent
import com.l1khith.calender28.viewmodel.SparkyViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SparkyDetailScreen(
    sparkyViewModel: SparkyViewModel,
    onBack: () -> Unit,
    onOpenShop: () -> Unit
) {
    val context = LocalContext.current
    val sparkyState by sparkyViewModel.sparkyState.collectAsStateWithLifecycle()
    val currentMood by sparkyViewModel.currentMood.collectAsStateWithLifecycle()
    val moodMessage by sparkyViewModel.moodMessage.collectAsStateWithLifecycle()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val achievements = remember(sparkyState) {
        sparkyViewModel.getAchievements()
    }
    val unlockedCount = achievements.count { it.isUnlocked }

    LaunchedEffect(Unit) {
        sparkyViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SparkyUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is SparkyUiEvent.AchievementUnlocked -> {
                    snackbarHostState.showSnackbar("Achievement unlocked: ${event.title}! +${event.reward} coins")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Sparky,
                            contentDescription = null,
                            tint = MatrixColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${sparkyState.name}'s Profile",
                            fontWeight = FontWeight.Bold,
                            color = MatrixColors.TextHeader,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MatrixColors.TextHeader
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        renameInput = sparkyState.name
                        showRenameDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = MatrixColors.TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MatrixColors.Surface)
            )
        },
        containerColor = MatrixColors.Surface
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero Character Display
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Xl,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clickable {
                                    SoundEffectHelper.playFireSound(context)
                                    sparkyViewModel.interactWithSparky()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            SparkyAnimation(
                                mood = currentMood,
                                stage = sparkyState.stage,
                                size = 180.dp,
                                equippedSkin = sparkyState.equippedSkin
                            )
                        }

                        Text(
                            text = "\"$moodMessage\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MatrixColors.TextHeader,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MatrixColors.PrimaryContainer.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MatrixColors.Primary.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MatrixColors.Primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Level ${sparkyState.level}",
                                        color = MatrixColors.Primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MatrixColors.SecondaryContainer.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MatrixColors.Secondary.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = AppIcons.SparkyCrown,
                                        contentDescription = null,
                                        tint = MatrixColors.Secondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${sparkyState.stage.displayName} Stage",
                                        color = MatrixColors.Secondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // XP Bar
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Level Progress",
                                    color = MatrixColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${sparkyState.currentLevelXp} / 100 XP",
                                    color = MatrixColors.Primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            LinearProgressIndicator(
                                progress = { sparkyState.progressToNextLevel },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MatrixColors.Primary,
                                trackColor = MatrixColors.SurfaceContainerHigh
                            )
                        }

                        // Open Shop Action Button
                        Button(
                            onClick = onOpenShop,
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            shape = MatrixShapes.Md,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MatrixColors.PrimaryContainer,
                                contentColor = MatrixColors.OnPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = AppIcons.SparkyShop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Sparky Shop",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Evolution Roadmap
            item {
                Text(
                    text = "EVOLUTION ROADMAP",
                    color = MatrixColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        EvolutionStage.entries.forEach { stage ->
                            val isCurrent = sparkyState.stage == stage
                            val isReached = sparkyState.totalHabitsCompleted >= stage.minHabits

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCurrent) MatrixColors.Primary.copy(alpha = 0.2f)
                                                else if (isReached) MatrixColors.Secondary.copy(alpha = 0.15f)
                                                else MatrixColors.SurfaceContainerHigh
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isReached) Icons.Default.CheckCircle else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (isCurrent) MatrixColors.Primary else if (isReached) MatrixColors.Secondary else MatrixColors.TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = stage.displayName,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrent) MatrixColors.Primary else MatrixColors.TextHeader,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (stage.minHabits == 0) "Starting Stage" else "Requires ${stage.minHabits} completed habits",
                                            color = MatrixColors.TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MatrixColors.Primary.copy(alpha = 0.2f),
                                        border = BorderStroke(0.8.dp, MatrixColors.Primary)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            color = MatrixColors.Primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Personality Traits Radar / Meters
            item {
                Text(
                    text = "PERSONALITY TRAITS",
                    color = MatrixColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PersonalityTrait.entries.forEach { trait ->
                            val score = sparkyState.traits[trait] ?: 50
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        PersonalityBadge(trait = trait)
                                        Text(
                                            text = trait.subtitle,
                                            color = MatrixColors.TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "$score / 100",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MatrixColors.TextHeader
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { score / 100f },
                                    modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                    color = MatrixColors.Secondary,
                                    trackColor = MatrixColors.SurfaceContainerHigh
                                )
                            }
                        }
                    }
                }
            }

            // Achievements Section (25 Achievements)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACHIEVEMENTS",
                        color = MatrixColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MatrixColors.Primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "$unlockedCount / ${achievements.size} Unlocked",
                            color = MatrixColors.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            items(achievements) { achievement ->
                val isClaimed = sparkyState.claimedAchievements.contains(achievement.id)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Md,
                    colors = CardDefaults.cardColors(
                        containerColor = if (achievement.isUnlocked) MatrixColors.SurfaceContainerLow else MatrixColors.SurfaceContainerLow.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (achievement.isUnlocked && !isClaimed) MatrixColors.Primary.copy(alpha = 0.6f)
                        else MatrixColors.OutlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (achievement.isUnlocked) MatrixColors.PrimaryContainer
                                        else MatrixColors.SurfaceContainerHigh
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (achievement.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (achievement.isUnlocked) MatrixColors.Primary else MatrixColors.TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = achievement.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (achievement.isUnlocked) MatrixColors.TextHeader else MatrixColors.TextSecondary
                                )
                                Text(
                                    text = achievement.description,
                                    fontSize = 11.sp,
                                    color = MatrixColors.TextSecondary
                                )
                            }
                        }

                        // Claim or Reward status
                        if (isClaimed) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MatrixColors.SurfaceContainerHigh
                            ) {
                                Text(
                                    text = "CLAIMED",
                                    color = MatrixColors.TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else if (achievement.isUnlocked) {
                            Button(
                                onClick = {
                                    SoundEffectHelper.playCoinSound(context)
                                    sparkyViewModel.claimAchievement(achievement.id)
                                },
                                shape = MatrixShapes.Sm,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MatrixColors.Primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = AppIcons.Coin,
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${achievement.rewardCoins}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Text(
                                text = "+${achievement.rewardCoins} coins",
                                color = MatrixColors.TextSecondary.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = {
                Text(
                    text = "Rename Companion",
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { if (it.length <= 20) renameInput = it },
                    label = { Text("Companion Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        sparkyViewModel.renameSparky(renameInput)
                        showRenameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
