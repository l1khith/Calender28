package com.l1khith.calender28.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.CoinTransactionEntity
import com.l1khith.calender28.data.SparkyCatalog
import com.l1khith.calender28.data.SparkyShopCategory
import com.l1khith.calender28.data.SparkyShopItem
import com.l1khith.calender28.data.TransactionReason
import com.l1khith.calender28.ui.sparky.SparkyAnimation
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.viewmodel.CoinUiEvent
import com.l1khith.calender28.viewmodel.CoinViewModel
import com.l1khith.calender28.viewmodel.SparkyViewModel
import kotlinx.coroutines.flow.collectLatest

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinStoreScreen(
    coinViewModel: CoinViewModel,
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit,
    initialTab: Int = 0,
    sparkyViewModel: com.l1khith.calender28.viewmodel.SparkyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.l1khith.calender28.viewmodel.AppViewModelProvider.Factory)
) {
    val coinBalance by coinViewModel.coinBalance.collectAsStateWithLifecycle()
    val transactions by coinViewModel.recentTransactions.collectAsStateWithLifecycle()
    val isProActive by coinViewModel.isProActive.collectAsStateWithLifecycle()
    val isPurchasing by coinViewModel.isPurchasing.collectAsStateWithLifecycle()
    val sparkyState by sparkyViewModel.sparkyState.collectAsStateWithLifecycle()
    val currentMood by sparkyViewModel.currentMood.collectAsStateWithLifecycle()

    var selectedStoreTab by remember { mutableStateOf(initialTab) }
    var promoInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        coinViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CoinUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is CoinUiEvent.CoinsEarned -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is CoinUiEvent.PremiumUnlocked -> {
                    snackbarHostState.showSnackbar(
                        message = "Congratulations! Pro is now active!",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        sparkyViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is com.l1khith.calender28.viewmodel.SparkyUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is com.l1khith.calender28.viewmodel.SparkyUiEvent.AchievementUnlocked -> {
                    snackbarHostState.showSnackbar("Achievement unlocked: ${event.title}! +${event.reward} coins")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(MatrixColors.Surface)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (selectedStoreTab == 0) AppIcons.Coin else AppIcons.SparkyShop,
                                contentDescription = null,
                                tint = if (selectedStoreTab == 0) Color.Unspecified else MatrixColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedStoreTab == 0) "CalCoin Store" else "Sparky Shop",
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MatrixColors.Surface
                    )
                )

                // TabRow with Vector Icons (Zero Emojis)
                PrimaryTabRow(
                    selectedTabIndex = selectedStoreTab,
                    containerColor = MatrixColors.Surface,
                    contentColor = MatrixColors.Primary,
                    divider = { HorizontalDivider(color = MatrixColors.OutlineVariant.copy(alpha = 0.5f)) }
                ) {
                    Tab(
                        selected = selectedStoreTab == 0,
                        onClick = { selectedStoreTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.Coin,
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("CalCoin Store", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedStoreTab == 1,
                        onClick = { selectedStoreTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.SparkyShop,
                                    contentDescription = null,
                                    tint = if (selectedStoreTab == 1) MatrixColors.Primary else MatrixColors.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Sparky Shop", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                }
            }
        },
        containerColor = MatrixColors.Surface
    ) { paddingValues ->
        if (selectedStoreTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 1. Balance Banner Card
                item {
                    BalanceCard(balance = coinBalance)
                }

            // 2. Pro Unlock Card
            item {
                PremiumUnlockCard(
                    coinBalance = coinBalance,
                    isProActive = isProActive,
                    isPurchasing = isPurchasing,
                    onUnlockWithCoins = { coinViewModel.purchasePremiumWithCoins() },
                    onOpenPaywall = onOpenPaywall
                )
            }

            // 3. Promo Code Card
            item {
                PromoCodeCard(
                    promoInput = promoInput,
                    onPromoChange = { promoInput = it },
                    onRedeem = {
                        if (promoInput.isNotBlank()) {
                            keyboardController?.hide()
                            coinViewModel.redeemPromoCode(promoInput)
                            promoInput = ""
                        }
                    }
                )
            }

            // 4. How to Earn Card
            item {
                HowToEarnCard()
            }

            // 5. Transaction Ledger Header
            item {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MatrixColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Transaction Ledger",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MatrixColors.TextHeader
                    )
                }
            }

            // 6. Transactions List
            if (transactions.isEmpty()) {
                item {
                    EmptyTransactionsCard()
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionItemRow(tx = tx)
                }
            }
        }
    } else {
        SparkyShopContent(
            sparkyViewModel = sparkyViewModel,
            coinBalance = coinBalance,
            paddingValues = paddingValues
        )
    }
}
}

@Composable
private fun BalanceCard(balance: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MatrixColors.Primary.copy(alpha = 0.4f), MatrixShapes.Lg),
        shape = MatrixShapes.Lg,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MatrixColors.Primary.copy(alpha = 0.15f),
                            MatrixColors.Secondary.copy(alpha = 0.10f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "CURRENT BALANCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MatrixColors.TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = AppIcons.Coin,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$balance",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MatrixColors.TextHeader
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CalCoins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MatrixColors.Primary,
                        modifier = Modifier.align(Alignment.Bottom).padding(bottom = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Earn CalCoins daily by checking habits and completing recurring tasks.",
                    fontSize = 13.sp,
                    color = MatrixColors.TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PremiumUnlockCard(
    coinBalance: Int,
    isProActive: Boolean,
    isPurchasing: Boolean,
    onUnlockWithCoins: () -> Unit,
    onOpenPaywall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MatrixColors.Secondary.copy(alpha = 0.5f), MatrixShapes.Lg),
        shape = MatrixShapes.Lg,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainer)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = MatrixColors.Secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Unlock Pro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MatrixColors.TextHeader
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ProPerkItem(text = "100% Ad-Free Experience")
                ProPerkItem(text = "Unlimited 28-Day Habit Trackers")
                ProPerkItem(text = "All 5 Dark Theme Palettes")
                ProPerkItem(text = "Full ICS & CSV Data Export")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isProActive) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Md,
                    color = MatrixColors.Primary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MatrixColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pro is Active on this Device",
                            fontWeight = FontWeight.Bold,
                            color = MatrixColors.Primary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Button(
                    onClick = onUnlockWithCoins,
                    enabled = coinBalance >= 1500 && !isPurchasing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Md,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MatrixColors.Secondary,
                        contentColor = Color.Black,
                        disabledContainerColor = MatrixColors.SurfaceContainerHigh,
                        disabledContentColor = MatrixColors.TextSecondary
                    )
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (coinBalance >= 1500) Icons.Default.MonetizationOn else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (coinBalance >= 1500) "Redeem for 1,500 CalCoins" else "Need 1,500 Coins ($coinBalance / 1,500)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onOpenPaywall,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Md,
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MatrixColors.TextHeader
                    )
                ) {
                    Text("Or Try Testing Track Free")
                }
            }
        }
    }
}

@Composable
private fun ProPerkItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MatrixColors.Primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = MatrixColors.TextHeader
        )
    }
}

@Composable
private fun PromoCodeCard(
    promoInput: String,
    onPromoChange: (String) -> Unit,
    onRedeem: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MatrixColors.OutlineVariant, MatrixShapes.Lg),
        shape = MatrixShapes.Lg,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = MatrixColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Redeem Promo Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MatrixColors.TextHeader
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promoInput,
                    onValueChange = onPromoChange,
                    placeholder = { Text("Enter code (e.g. SHIPATON2026)", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = MatrixShapes.Md,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onRedeem() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MatrixColors.Primary,
                        unfocusedBorderColor = MatrixColors.OutlineVariant,
                        focusedTextColor = MatrixColors.TextHeader,
                        unfocusedTextColor = MatrixColors.TextHeader
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onRedeem,
                    shape = MatrixShapes.Md,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MatrixColors.Primary,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Redeem", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun HowToEarnCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.5f), MatrixShapes.Lg),
        shape = MatrixShapes.Lg,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MatrixColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "How to Earn CalCoins",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MatrixColors.TextHeader
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            EarnRuleRow("Daily App Open", "+1 coin")
            EarnRuleRow("Log Day in Habit", "+1 coin")
            EarnRuleRow("Complete Any Task", "+1 coin")
            EarnRuleRow("Check Recurring Task", "+2 coins")
            EarnRuleRow("Focus Session Complete", "+5 coins")
            EarnRuleRow("Complete Habit Cycle", "+10 coins")
            EarnRuleRow("10 / 50 / 100 Cycles Milestone", "+100 / +500 / +1000 coins")
        }
    }
}

@Composable
private fun EarnRuleRow(action: String, reward: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = action, fontSize = 12.sp, color = MatrixColors.TextSecondary)
        Text(text = reward, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MatrixColors.Primary)
    }
}

@Composable
private fun TransactionItemRow(tx: CoinTransactionEntity) {
    val isPositive = tx.amount >= 0
    val reason = TransactionReason.fromString(tx.reason)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, MatrixColors.OutlineVariant.copy(alpha = 0.4f), MatrixShapes.Md),
        shape = MatrixShapes.Md,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reason.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MatrixColors.TextHeader
                )
                if (!tx.note.isNullOrBlank()) {
                    Text(
                        text = tx.note,
                        fontSize = 12.sp,
                        color = MatrixColors.TextSecondary
                    )
                }
                // Convert Gregorian ISO timestamp to Fixed Calendar date for display
                val displayDate = try {
                    val isoDate = tx.timestamp.take(10) // "yyyy-MM-dd"
                    val parts = isoDate.split("-")
                    if (parts.size == 3) {
                        val cal = java.util.Calendar.getInstance().apply {
                            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                        }
                        val fixed = com.l1khith.calender28.utils.FixedCalendarHelper.fromTimestamp(cal.timeInMillis)
                        fixed.toString()
                    } else {
                        tx.timestamp.take(10)
                    }
                } catch (_: Exception) {
                    tx.timestamp.take(10)
                }
                Text(
                    text = displayDate,
                    fontSize = 11.sp,
                    color = MatrixColors.TextSecondary.copy(alpha = 0.7f)
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isPositive) MatrixColors.Primary.copy(alpha = 0.15f) else MatrixColors.Error.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (isPositive) "+${tx.amount}" else "${tx.amount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isPositive) MatrixColors.Primary else MatrixColors.Error,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MatrixShapes.Md,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No coin transactions recorded yet.\nStart by checking tasks or logging habits!",
                fontSize = 13.sp,
                color = MatrixColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SparkyShopContent(
    sparkyViewModel: SparkyViewModel,
    coinBalance: Int,
    paddingValues: PaddingValues
) {
    val sparkyState by sparkyViewModel.sparkyState.collectAsStateWithLifecycle()
    val currentMood by sparkyViewModel.currentMood.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<SparkyShopCategory?>(null) }

    val filteredItems = remember(selectedCategory) {
        if (selectedCategory == null) SparkyCatalog.items
        else SparkyCatalog.items.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Sparky Live Showcase & Balance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MatrixColors.Primary.copy(alpha = 0.4f), MatrixShapes.Lg),
                shape = MatrixShapes.Lg,
                colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MatrixColors.Primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Lvl ${sparkyState.level} - ${sparkyState.stage.displayName}",
                                color = MatrixColors.Primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.Coin,
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$coinBalance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MatrixColors.TextHeader
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SparkyAnimation(
                        mood = currentMood,
                        stage = sparkyState.stage,
                        size = 110.dp,
                        equippedSkin = sparkyState.equippedSkin
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = sparkyState.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MatrixColors.TextHeader
                    )

                    val activeCosmetics = buildList {
                        sparkyState.equippedHat?.let { hatId ->
                            SparkyCatalog.items.find { it.id == hatId }?.let { add(it.name) }
                        }
                        sparkyState.equippedSkin?.let { skinId ->
                            SparkyCatalog.items.find { it.id == skinId }?.let { add(it.name) }
                        }
                    }
                    if (activeCosmetics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Wearing: ${activeCosmetics.joinToString(" + ")}",
                            fontSize = 12.sp,
                            color = MatrixColors.Primary
                        )
                    }
                }
            }
        }

        // 2. Filter Category Chips (Zero Emojis - Material / AppIcons)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MatrixColors.Primary.copy(alpha = 0.2f),
                        selectedLabelColor = MatrixColors.Primary
                    )
                )
                SparkyShopCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    val icon = when (category) {
                        SparkyShopCategory.HATS -> AppIcons.SparkyCrown
                        SparkyShopCategory.SKINS -> Icons.Default.Palette
                        SparkyShopCategory.BOOSTERS -> Icons.Default.Bolt
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category.displayName, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MatrixColors.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = MatrixColors.Primary
                        )
                    )
                }
            }
        }

        // 3. Catalog Items
        items(filteredItems, key = { it.id }) { item ->
            val isUnlocked = when (item.category) {
                SparkyShopCategory.HATS -> sparkyState.unlockedHats.contains(item.id)
                SparkyShopCategory.SKINS -> sparkyState.unlockedSkins.contains(item.id)
                SparkyShopCategory.BOOSTERS -> sparkyState.unlockedBoosters.contains(item.id)
            }
            val isEquipped = when (item.category) {
                SparkyShopCategory.HATS -> sparkyState.equippedHat == item.id
                SparkyShopCategory.SKINS -> sparkyState.equippedSkin == item.id
                else -> false
            }

            SparkyShopItemCard(
                item = item,
                isUnlocked = isUnlocked,
                isEquipped = isEquipped,
                canAfford = coinBalance >= item.price,
                onBuy = { sparkyViewModel.buyShopItem(item) },
                onToggleEquip = { sparkyViewModel.toggleEquip(item) }
            )
        }
    }
}

@Composable
private fun SparkyShopItemCard(
    item: SparkyShopItem,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit,
    onToggleEquip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isEquipped) MatrixColors.Primary else MatrixColors.OutlineVariant.copy(alpha = 0.5f),
                MatrixShapes.Md
            ),
        shape = MatrixShapes.Md,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (item.category) {
                SparkyShopCategory.HATS -> AppIcons.SparkyCrown
                SparkyShopCategory.SKINS -> Icons.Default.Palette
                SparkyShopCategory.BOOSTERS -> Icons.Default.Bolt
            }
            Surface(
                shape = CircleShape,
                color = Color(item.accentColor).copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(item.accentColor),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MatrixColors.TextHeader
                    )
                    if (isEquipped) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MatrixColors.Primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Equipped",
                                color = MatrixColors.Primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = MatrixColors.TextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = AppIcons.Coin,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.price} CalCoins",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MatrixColors.Primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            when {
                item.category == SparkyShopCategory.BOOSTERS -> {
                    if (isUnlocked) {
                        Surface(
                            shape = MatrixShapes.Sm,
                            color = MatrixColors.Primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Active",
                                color = MatrixColors.Primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onBuy,
                            enabled = canAfford,
                            shape = MatrixShapes.Sm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MatrixColors.Primary,
                                contentColor = MatrixColors.OnPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Buy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                isUnlocked -> {
                    if (isEquipped) {
                        OutlinedButton(
                            onClick = onToggleEquip,
                            shape = MatrixShapes.Sm,
                            border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Unequip", fontSize = 12.sp, color = MatrixColors.TextSecondary)
                        }
                    } else {
                        Button(
                            onClick = onToggleEquip,
                            shape = MatrixShapes.Sm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MatrixColors.Primary,
                                contentColor = MatrixColors.OnPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Equip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onBuy,
                        enabled = canAfford,
                        shape = MatrixShapes.Sm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixColors.Primary,
                            contentColor = MatrixColors.OnPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Buy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

