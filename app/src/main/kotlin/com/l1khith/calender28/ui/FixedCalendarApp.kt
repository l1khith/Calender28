package com.l1khith.calender28.ui

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.viewmodel.AppViewModelProvider
import com.l1khith.calender28.viewmodel.CoinViewModel
import com.l1khith.calender28.viewmodel.FocusViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import com.l1khith.calender28.utils.PlatformTimePicker
import com.l1khith.calender28.utils.currentTimeMillis
import com.l1khith.calender28.viewmodel.FixedCalendarViewModel
import com.l1khith.calender28.utils.rememberCalendarPermissionLauncher
import com.l1khith.calender28.utils.rememberNotificationPermissionLauncher
import com.l1khith.calender28.utils.rememberSecurityLockLauncher

import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors

import com.l1khith.calender28.ui.theme.MatrixShapes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedCalendarApp(
    viewModel: FixedCalendarViewModel,
    initialTaskId: String? = null,
    onExitApp: () -> Unit = {}
) {
    LaunchedEffect(initialTaskId) {
        if (initialTaskId != null) {
            viewModel.onTaskSelected(initialTaskId)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        ctx as? android.app.Activity
    }

    LaunchedEffect(Unit) {
        com.l1khith.calender28.billing.SubscriptionManager.initDataStore(context, coroutineScope)
        com.l1khith.calender28.ads.InterstitialAdManager.loadAd(context)
    }

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val tasks by viewModel.tasksForSelectedDay.collectAsStateWithLifecycle()
    val activeDates by viewModel.datesWithActiveTasks.collectAsStateWithLifecycle()
    val recurringTasks by viewModel.recurringTasks.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<AppTask?>(null) }
    var showRecurringManager by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showIcsImportDialog by remember { mutableStateOf(false) }
    var showExportTasksDialog by remember { mutableStateOf(false) }
    var showPaywallDialog by remember { mutableStateOf(false) }
    var showCustomerCenterDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showCoinStoreDialog by remember { mutableStateOf(false) }
    var showFocusStatsDialog by remember { mutableStateOf(false) }
    var showSecurityLockDialog by remember { mutableStateOf(false) }
    var showCustomSkippableAd by remember { mutableStateOf(false) }
    var navigationCount by remember { mutableIntStateOf(0) }

    val coinViewModel: CoinViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory)
    val focusViewModel: FocusViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory)
    val focusState by com.l1khith.calender28.service.FocusSessionManager.focusState.collectAsStateWithLifecycle()

    val isProActive by com.l1khith.calender28.billing.SubscriptionManager.isProActive.collectAsStateWithLifecycle()
    val coinBalance by coinViewModel.coinBalance.collectAsStateWithLifecycle()

    com.l1khith.calender28.utils.PlatformBackHandler(enabled = true) {
        if (isProActive) {
            onExitApp()
        } else {
            showExitDialog = true
        }
    }

    var pendingCalendarImportAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingNotificationSaveAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val launchCalendarPermission = rememberCalendarPermissionLauncher(
        onGranted = {
            pendingCalendarImportAction?.invoke()
            pendingCalendarImportAction = null
        },
        onDenied = {
            pendingCalendarImportAction = null
        }
    )

    val launchNotificationPermission = rememberNotificationPermissionLauncher(
        onGranted = {
            pendingNotificationSaveAction?.invoke()
            pendingNotificationSaveAction = null
        },
        onDenied = {
            pendingNotificationSaveAction?.invoke()
            pendingNotificationSaveAction = null
        }
    )

    val todayFixed = remember { FixedCalendarHelper.fromTimestamp(currentTimeMillis()) }

    val monthNavIcon = remember {
        ImageVector.Builder(
            name = "MonthNavIcon",
            defaultWidth = 19.dp,
            defaultHeight = 19.dp,
            viewportWidth = 19f,
            viewportHeight = 19f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M0.5 8.5V0.5H8.5V8.5H0.5ZM0.5 18.5V10.5H8.5V18.5H0.5ZM10.5 8.5V0.5H18.5V8.5H10.5ZM10.5 18.5V10.5H18.5V18.5H10.5Z"
            ).toNodes(),
            fill = SolidColor(Color(0xFF00285D)),
            stroke = SolidColor(Color.Black),
            strokeLineJoin = StrokeJoin.Bevel
        ).build()
    }

    val tasksNavIcon = remember {
        ImageVector.Builder(
            name = "TasksNavIcon",
            defaultWidth = 20.dp,
            defaultHeight = 16.dp,
            viewportWidth = 20f,
            viewportHeight = 16f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M3.55 15.075L0 11.525L1.4 10.125L3.525 12.25L7.775 8L9.175 9.425L3.55 15.075V15.075M3.55 7.075L0 3.525L1.4 2.125L3.525 4.25L7.775 0L9.175 1.425L3.55 7.075V7.075M11 13.075V11.075H20V13.075H11V13.075M11 5.075V3.075H20V5.075H11V5.075"
            ).toNodes(),
            fill = SolidColor(Color(0xFFC2C6D6))
        ).build()
    }

    val profileNavIcon = remember {
        ImageVector.Builder(
            name = "ProfileNavIcon",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M8 8C6.9 8 5.95833 7.60833 5.175 6.825C4.39167 6.04167 4 5.1 4 4C4 2.9 4.39167 1.95833 5.175 1.175C5.95833 0.391667 6.9 0 8 0C9.1 0 10.0417 0.391667 10.825 1.175C11.6083 1.95833 12 2.9 12 4C12 5.1 11.6083 6.04167 10.825 6.825C10.0417 7.60833 9.1 8 8 8V8M0 16V13.2C0 12.6333 0.145833 12.1125 0.4375 11.6375C0.729167 11.1625 1.11667 10.8 1.6 10.55C2.63333 10.0333 3.68333 9.64583 4.75 9.3875C5.81667 9.12917 6.9 9 8 9C9.1 9 10.1833 9.12917 11.25 9.3875C12.3167 9.64583 13.3667 10.0333 14.4 10.55C14.8833 10.8 15.2708 11.1625 15.5625 11.6375C15.8542 12.1125 16 12.6333 16 13.2V16H0V16M2 14H14V13.2C14 13.0167 13.9542 12.85 13.8625 12.7C13.7708 12.55 13.65 12.4333 13.5 12.35C12.6 11.9 11.6917 11.5625 10.775 11.3375C9.85833 11.1125 8.93333 11 8 11C7.06667 11 6.14167 11.1125 5.225 11.3375C4.30833 11.5625 3.4 11.9 2.5 12.35C2.35 12.4333 2.22917 12.55 2.1375 12.7C2.04583 12.85 2 13.0167 2 13.2V14V14M8 6C8.55 6 9.02083 5.80417 9.4125 5.4125C9.80417 5.02083 10 4.55 10 4C10 3.45 9.80417 2.97917 9.4125 2.5875C9.02083 2.19583 8.55 2 8 2C7.45 2 6.97917 2.19583 6.5875 2.5875C6.19583 2.97917 6 3.45 6 4C6 4.55 6.19583 5.02083 6.5875 5.4125C6.97917 5.80417 7.45 6 8 6V6M8 4V4V4V4V4V4V4V4V4V4M8 14V14V14V14V14V14V14V14V14V14V14V14V14"
            ).toNodes(),
            fill = SolidColor(Color(0xFFC2C6D6))
        ).build()
    }

    val habitNavIcon = remember {
        ImageVector.Builder(
            name = "HabitNavIcon",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M9 0L11.7 5.5L18 6.4L13.5 10.8L14.6 17L9 14.1L3.4 17L4.5 10.8L0 6.4L6.3 5.5L9 0Z"
            ).toNodes(),
            fill = SolidColor(Color(0xFFC2C6D6))
        ).build()
    }

    val backgroundColor = MatrixColors.Surface

    val cardBackground = MatrixColors.SurfaceContainerLow
    val primaryAccent = MatrixColors.Primary
    val secondaryAccent = MatrixColors.Secondary
    val tertiaryAccent = MatrixColors.Tertiary
    val textColorPrimary = MatrixColors.TextHeader
    val textColorSecondary = MatrixColors.TextSecondary
    val borderSubtle = MatrixColors.OutlineVariant

    var selectedTab by remember { mutableStateOf(0) }

    val onNavigateToTab: (Int) -> Unit = { targetTab ->
        if (selectedTab != targetTab) {
            selectedTab = targetTab
            if (!isProActive) {
                navigationCount++
                if (navigationCount % com.l1khith.calender28.utils.Constants.INTERSTITIAL_NAV_FREQUENCY == 0) {
                    if (activity != null && com.l1khith.calender28.ads.InterstitialAdManager.isAdLoaded()) {
                        com.l1khith.calender28.ads.InterstitialAdManager.showAd(
                            activity = activity,
                            onAdDismissed = {},
                            onAdUnavailable = { showCustomSkippableAd = true }
                        )
                    } else {
                        showCustomSkippableAd = true
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        CreateTaskScreen(
            task = taskToEdit,
            onDismiss = { showAddTaskDialog = false },
            onSave = { id, title, desc, isReminder, time, priority ->
                if (isReminder) {
                    pendingNotificationSaveAction = {
                        viewModel.saveTask(id, title, desc, isReminder, time, priority)
                        showAddTaskDialog = false
                    }
                    launchNotificationPermission()
                } else {
                    viewModel.saveTask(id, title, desc, isReminder, time, priority)
                    showAddTaskDialog = false
                }
            },
            onSaveRecurring = { id, title, desc, recType, recDays, interval, priority, isActive, endDate, reminderTime ->
                viewModel.saveRecurringTask(id, title, desc, recType, recDays, interval, priority, isActive, endDate, reminderTime)
                showAddTaskDialog = false
            },
            onDeleteTask = { task ->
                viewModel.deleteTask(task)
                showAddTaskDialog = false
            },
            onDeleteRecurring = { recId ->
                viewModel.deleteRecurringTask(recId)
                showAddTaskDialog = false
            },
            onStartFocus = { task ->
                com.l1khith.calender28.service.FocusSessionManager.openSetup(task)
            }
        )
    }
 else {

        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MatrixColors.Surface)
                ) {

                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.AppLogo,
                                contentDescription = "Matrix 28 Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                text = "Matrix 28",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixColors.Primary
                                )
                            )

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MatrixColors.SurfaceContainerHigh,
                                border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                            ) {
                                Text(
                                    text = "28-Day Calendar",
                                    color = textColorSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    },

                    actions = {
                        val downloadedSyncIcon = remember {
                            androidx.compose.ui.graphics.vector.ImageVector.Builder(
                                name = "DownloadedSyncIcon",
                                defaultWidth = 32.dp,
                                defaultHeight = 37.dp,
                                viewportWidth = 32f,
                                viewportHeight = 37f
                            ).addPath(
                                pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
                                    "M9 22.3333V20.6667H11.2917L10.9583 20.375C10.2361 19.7361 9.72917 19.0069 9.4375 18.1875C9.14583 17.3681 9 16.5417 9 15.7083C9 14.1667 9.46181 12.7951 10.3854 11.5938C11.309 10.3924 12.5139 9.59722 14 9.20833V10.9583C13 11.3194 12.1944 11.934 11.5833 12.8021C10.9722 13.6701 10.6667 14.6389 10.6667 15.7083C10.6667 16.3333 10.7847 16.941 11.0208 17.5312C11.2569 18.1215 11.625 18.6667 12.125 19.1667L12.3333 19.375V17.3333H14V22.3333H9V22.3333M17.3333 22.125V20.375C18.3333 20.0139 19.1389 19.3993 19.75 18.5312C20.3611 17.6632 20.6667 16.6944 20.6667 15.625C20.6667 15 20.5486 14.3924 20.3125 13.8021C20.0764 13.2118 19.7083 12.6667 19.2083 12.1667L19 11.9583V14H17.3333V9H22.3333V10.6667H20.0417L20.375 10.9583C21.0556 11.6389 21.5521 12.3785 21.8646 13.1771C22.1771 13.9757 22.3333 14.7917 22.3333 15.625C22.3333 17.1667 21.8715 18.5382 20.9479 19.7396C20.0243 20.941 18.8194 21.7361 17.3333 22.125V22.125"
                                ).toNodes(),
                                fill = androidx.compose.ui.graphics.SolidColor(Color(0xFFC2C6D6))
                            ).build()
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MatrixColors.SurfaceContainerHigh,
                            border = BorderStroke(1.dp, MatrixColors.Primary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { showCoinStoreDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "🪙", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$coinBalance",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MatrixColors.TextHeader
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(cardBackground)
                                .border(BorderStroke(1.dp, borderSubtle), CircleShape)
                                .clickable { showSyncDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = downloadedSyncIcon,
                                contentDescription = "Sync",
                                tint = Color(0xFFC2C6D6),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MatrixColors.Surface,
                        titleContentColor = Color(0xFF3B82F6)
                    )
                )

                HorizontalDivider(
                    color = Color(0xFF424754),
                    thickness = 1.dp
                )
            }

        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        taskToEdit = null
                        showAddTaskDialog = true
                    },
                    containerColor = MatrixColors.PrimaryContainer,
                    contentColor = MatrixColors.OnPrimaryContainer,
                    shape = MatrixShapes.Xl
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        },

        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MatrixColors.Surface)) {
                if (!isProActive) {
                    BannerAd(modifier = Modifier.fillMaxWidth())
                }
                HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)
                NavigationBar(
                    containerColor = MatrixColors.Surface,
                    contentColor = MatrixColors.OnSurface,
                    tonalElevation = 0.dp
                ) {

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onNavigateToTab(0) },
                    icon = {
                        Icon(
                            imageVector = monthNavIcon,
                            contentDescription = "Month"
                        )
                    },
                    label = { Text("Month", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MatrixColors.Primary,
                        selectedTextColor = MatrixColors.Primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MatrixColors.OnSurfaceVariant,
                        unselectedTextColor = MatrixColors.OnSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onNavigateToTab(1) },
                    icon = {
                        Icon(
                            imageVector = tasksNavIcon,
                            contentDescription = "Tasks"
                        )
                    },
                    label = { Text("Tasks", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MatrixColors.Primary,
                        selectedTextColor = MatrixColors.Primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MatrixColors.OnSurfaceVariant,
                        unselectedTextColor = MatrixColors.OnSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { onNavigateToTab(2) },
                    icon = {
                        Icon(
                            imageVector = habitNavIcon,
                            contentDescription = "Habit"
                        )
                    },
                    label = { Text("Habit", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MatrixColors.Primary,
                        selectedTextColor = MatrixColors.Primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MatrixColors.OnSurfaceVariant,
                        unselectedTextColor = MatrixColors.OnSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { onNavigateToTab(3) },
                    icon = {
                        Icon(
                            imageVector = profileNavIcon,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MatrixColors.Primary,
                        selectedTextColor = MatrixColors.Primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MatrixColors.OnSurfaceVariant,
                        unselectedTextColor = MatrixColors.OnSurfaceVariant
                    )
                )
            }
        }
    },
    containerColor = backgroundColor

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            when (selectedTab) {

                1 -> TasksScreen(
                    viewModel = viewModel,
                    onEditTask = {
                        taskToEdit = it
                        showAddTaskDialog = true
                    },
                    isProActive = isProActive,
                    onOpenPaywall = { showPaywallDialog = true },
                    onStartFocus = { task ->
                        com.l1khith.calender28.service.FocusSessionManager.openSetup(task)
                    }
                )

                2 -> HabitSection(
                    viewModel = viewModel,
                    isProActive = isProActive,
                    onOpenPaywall = { showPaywallDialog = true }
                )

                3 -> ProfileScreen(
                    onOpenSubscription = { showPaywallDialog = true },
                    onOpenCustomerCenter = { showCustomerCenterDialog = true },
                    onOpenSecurity = { showSecurityLockDialog = true },
                    onOpenNotifications = { launchNotificationPermission() },
                    onOpenFocusStats = { showFocusStatsDialog = true },
                    onOpenCoinStore = { showCoinStoreDialog = true },
                    onOpenExportTasks = { showExportTasksDialog = true },
                    onOpenMonthView = { onNavigateToTab(0) }
                )




                    else -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {



            MonthYearSelector(
                selectedDate = selectedDate,
                onDateChange = { viewModel.selectDate(it) },
                primaryColor = primaryAccent,
                textColor = textColorPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            val taskCounts by viewModel.taskCountsPerDate.collectAsState()

            CalendarMatrix(
                selectedDate = selectedDate,
                activeDates = activeDates,
                taskCounts = taskCounts,
                todayFixed = todayFixed,
                onDateSelect = { viewModel.selectDate(it) },
                primaryColor = primaryAccent,
                orangeDotColor = secondaryAccent,
                cardBg = cardBackground,
                borderColor = borderSubtle,
                textColor = textColorPrimary
            )

            if (!isProActive) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Md,
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    border = BorderStroke(1.dp, borderSubtle)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Ad",
                                    tint = textColorSecondary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ad Space / Go Pro to Remove",
                                    color = textColorSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            Button(
                                onClick = { showPaywallDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                border = BorderStroke(1.dp, borderSubtle),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = MatrixShapes.Sm
                            ) {
                                Text("Remove Ads", color = textColorPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        BannerAd(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                        .background(Color(0xFF3B82F6))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${FixedCalendarHelper.getMonthName(selectedDate.month)} ${selectedDate.day}, ${selectedDate.year}",
                    color = textColorPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• ${FixedCalendarHelper.getDayOfWeek(selectedDate)}",
                    color = textColorSecondary,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))



            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (tasks.isEmpty()) {
                    EmptyState(textColor = textColorSecondary)
                } else {
                    AgendaList(
                        tasks = tasks,
                        onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                        onEdit = {
                            taskToEdit = it
                            showAddTaskDialog = true
                        },
                        onDelete = { viewModel.deleteTask(it) },
                        onStartFocus = { task ->
                            com.l1khith.calender28.service.FocusSessionManager.openSetup(task)
                        },
                        primaryColor = primaryAccent,
                        orangeColor = secondaryAccent,
                        textColor = textColorPrimary,
                        textColorSec = textColorSecondary,
                        cardBg = cardBackground
                    )
                }
            }



            Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
}




    if (showRecurringManager) {



        RecurringManagerDialog(
            recurringTasks = recurringTasks,
            onDismiss = { showRecurringManager = false },
            onSaveRecurringTask = { id, title, desc, recType, recDays, interval, priority, isActive, endDate, reminderTime ->
                viewModel.saveRecurringTask(id, title, desc, recType, recDays, interval, priority, isActive, endDate, reminderTime)
            },
            onDeleteRecurringTask = { viewModel.deleteRecurringTask(it) },
            onToggleActive = { viewModel.toggleRecurringTaskActive(it) },
            primaryColor = primaryAccent,
            backgroundColor = backgroundColor,
            textColor = textColorPrimary,
            textColorSec = textColorSecondary
        )
    }

    if (showSyncDialog) {
        SyncOptionsBottomSheet(
            onDismiss = { showSyncDialog = false },
            onSyncSystem = {
                showSyncDialog = false
                pendingCalendarImportAction = { viewModel.importSystemCalendar() }
                launchCalendarPermission()
            },
            onImportIcs = {
                showSyncDialog = false
                showIcsImportDialog = true
            },
            onExportTasks = {
                showSyncDialog = false
                showExportTasksDialog = true
            },
            isProActive = isProActive,
            onOpenPaywall = { showPaywallDialog = true }
        )
    }

    if (showExportTasksDialog) {
        ExportTasksDialog(
            monthName = FixedCalendarHelper.getMonthName(selectedDate.month),
            year = selectedDate.year,
            onDismiss = { showExportTasksDialog = false },
            onExportFormat = { format, currentMonthOnly ->
                viewModel.exportTasksToDownloads(
                    format = format,
                    year = selectedDate.year,
                    month = selectedDate.month,
                    currentMonthOnly = currentMonthOnly,
                    onResult = { success, fileName, message ->
                        com.l1khith.calender28.utils.showPlatformToast(message)
                    }
                )
            }
        )
    }


    if (showIcsImportDialog) {
        IcsImportDialog(
            onDismiss = { showIcsImportDialog = false },
            onImport = { content ->
                viewModel.importFromIcsContent(content)
                showIcsImportDialog = false
            }
        )
    }

    if (showCoinStoreDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showCoinStoreDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            CoinStoreScreen(
                coinViewModel = coinViewModel,
                onBack = { showCoinStoreDialog = false },
                onOpenPaywall = {
                    showCoinStoreDialog = false
                    showPaywallDialog = true
                }
            )
        }
    }

    if (showPaywallDialog) {
        SubscriptionPaywallDialog(
            onDismiss = { showPaywallDialog = false }
        )
    }

    if (showCustomerCenterDialog) {
        CustomerCenterDialog(
            onDismiss = { showCustomerCenterDialog = false }
        )
    }

    if (showExitDialog) {
        ExitAppDialog(
            onDismiss = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                onExitApp()
            }
        )
    }

    if (showFocusStatsDialog) {
        FocusStatsDialog(
            focusViewModel = focusViewModel,
            onDismiss = { showFocusStatsDialog = false }
        )
    }

    if (showSecurityLockDialog) {
        com.l1khith.calender28.security.SecurityLockDialog(
            onDismiss = { showSecurityLockDialog = false }
        )
    }

    if (showCustomSkippableAd && !isProActive) {
        CustomSkippableAdDialog(
            onDismiss = { showCustomSkippableAd = false },
            onOpenPaywall = {
                showCustomSkippableAd = false
                showPaywallDialog = true
            }
        )
    }

    // Focus Mode Overlays
    when (val state = focusState) {
        is com.l1khith.calender28.service.FocusState.Setup -> {
            FocusSetupDialog(
                task = state.task,
                onDismiss = { com.l1khith.calender28.service.FocusSessionManager.closeSetup() },
                onStartFocus = { mode, duration, pinScreen ->
                    com.l1khith.calender28.service.FocusSessionManager.startFocus(context, state.task, mode, duration, pinScreen)
                }
            )
        }
        is com.l1khith.calender28.service.FocusState.Active -> {
            FocusActiveOverlay(
                activeState = state,
                onTogglePause = { com.l1khith.calender28.service.FocusSessionManager.togglePause(context) },
                onStop = { com.l1khith.calender28.service.FocusSessionManager.finishSessionAsComplete(context) },
                onCancel = { com.l1khith.calender28.service.FocusSessionManager.stopOrCancelFocus(context, markAsCancelled = true) }
            )
        }
        is com.l1khith.calender28.service.FocusState.Completed -> {
            FocusCompletionOverlay(
                completedState = state,
                onDone = { markTaskDone ->
                    com.l1khith.calender28.service.FocusSessionManager.commitCompletedTask(context, markTaskDone)
                },
                onAgain = {
                    com.l1khith.calender28.service.FocusSessionManager.restartSameTask(context)
                }
            )
        }
        com.l1khith.calender28.service.FocusState.Idle -> {}
    }
}

@Composable
fun MonthYearSelector(
    selectedDate: FixedDate,
    onDateChange: (FixedDate) -> Unit,
    primaryColor: Color,
    textColor: Color
) {
    fun navigateMonth(delta: Int) {
        var newM = selectedDate.month + delta
        var newY = selectedDate.year
        if (newM < 1) {
            newM = 13
            newY -= 1
        } else if (newM > 13) {
            newM = 1
            newY += 1
        }
        val newDay = if (selectedDate.day == 29 && newM != 6 && newM != 13) 28 else selectedDate.day
        val isLD = newM == 6 && newDay == 29
        val isYD = newM == 13 && newDay == 29
        onDateChange(FixedDate(newY, newM, newDay, isLeapDay = isLD, isYearDay = isYD))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Month Selector with Arrow Marks
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navigateMonth(-1) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = primaryColor
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = FixedCalendarHelper.getMonthName(selectedDate.month),
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Month ${selectedDate.month} of 13",
                    color = MatrixColors.TextSecondary,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = { navigateMonth(1) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = primaryColor
                )
            }
        }

        // Year Selector with Arrow Marks
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                onDateChange(selectedDate.copy(year = selectedDate.year - 1))
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Prev Year",
                    tint = primaryColor
                )
            }
            Text(
                text = selectedDate.year.toString(),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(onClick = {
                onDateChange(selectedDate.copy(year = selectedDate.year + 1))
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Year",
                    tint = primaryColor
                )
            }
        }
    }
}

@Composable
fun CalendarMatrix(
    selectedDate: FixedDate,
    activeDates: Set<String>,
    taskCounts: Map<String, Int>,
    todayFixed: FixedDate,
    onDateSelect: (FixedDate) -> Unit,
    primaryColor: Color,
    orangeDotColor: Color,
    cardBg: Color,
    borderColor: Color,
    textColor: Color
) {
    val weekDays = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val isLeap = FixedCalendarHelper.isLeapYear(selectedDate.year)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MatrixShapes.Lg,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
        border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            // Weekday Header Row - NO lines here
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (day in weekDays) {
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MatrixColors.TextSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Date Grid (4 rows x 7 columns) with subtle grid lines between date cells
            for (row in 0 until 4) {
                HorizontalDivider(color = MatrixColors.OutlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 1..7) {
                        if (col > 1) {
                            VerticalDivider(color = MatrixColors.OutlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                        }
                        val dayNum = row * 7 + col
                        val cellDate = FixedDate(selectedDate.year, selectedDate.month, dayNum)
                        val cellDateStr = cellDate.toString()
                        val taskCount = taskCounts[cellDateStr] ?: if (activeDates.contains(cellDateStr)) 1 else 0
                        val isCellSelected = selectedDate.month == cellDate.month && selectedDate.day == dayNum
                        val isCellToday = todayFixed.year == cellDate.year && todayFixed.month == cellDate.month && todayFixed.day == dayNum

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onDateSelect(cellDate) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isCellSelected) Color(0xFF3B82F6) else Color.Transparent)
                                        .border(
                                            width = if (isCellToday && !isCellSelected) 1.dp else 0.dp,
                                            color = if (isCellToday && !isCellSelected) Color(0xFF3B82F6) else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        color = if (isCellSelected) Color.White else if (taskCount > 0) MatrixColors.TextHeader else MatrixColors.TextSecondary,
                                        fontWeight = if (isCellSelected || isCellToday || taskCount > 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }

                                if (taskCount > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val dotsToShow = taskCount.coerceAtMost(3)
                                        for (i in 0 until dotsToShow) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (i % 2 == 0) MatrixColors.Tertiary else MatrixColors.Secondary
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedDate.month == 6 && isLeap) {
                Spacer(modifier = Modifier.height(10.dp))
                val leapDay = FixedDate(selectedDate.year, 6, 29, isLeapDay = true)
                val isLeapSelected = selectedDate.month == 6 && selectedDate.day == 29
                val isLeapToday = todayFixed.year == selectedDate.year && todayFixed.month == 6 && todayFixed.day == 29
                val hasTasks = activeDates.contains(leapDay.toString())

                SpecialDayCard(
                    label = "Leap Day — Leave Day (June 29)",
                    isSelected = isLeapSelected,
                    isToday = isLeapToday,
                    hasTasks = hasTasks,
                    onClick = { onDateSelect(leapDay) },
                    primaryColor = primaryColor,
                    orangeDotColor = orangeDotColor,
                    textColor = textColor
                )
            }

            if (selectedDate.month == 13) {
                Spacer(modifier = Modifier.height(10.dp))
                val yearDay = FixedDate(selectedDate.year, 13, 29, isYearDay = true)
                val isYearSelected = selectedDate.month == 13 && selectedDate.day == 29
                val isYearToday = todayFixed.year == selectedDate.year && todayFixed.month == 13 && todayFixed.day == 29
                val hasTasks = activeDates.contains(yearDay.toString())

                SpecialDayCard(
                    label = "Sol Day — Leave Day (December 29)",
                    isSelected = isYearSelected,
                    isToday = isYearToday,
                    hasTasks = hasTasks,
                    onClick = { onDateSelect(yearDay) },
                    primaryColor = primaryColor,
                    orangeDotColor = orangeDotColor,
                    textColor = textColor
                )
            }
        }
    }
}


@Composable
fun SpecialDayCard(
    label: String,
    isSelected: Boolean,
    isToday: Boolean,
    hasTasks: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    orangeDotColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor else Color.Black
        ),
        border = BorderStroke(
            width = if (isToday && !isSelected) 1.dp else 0.dp,
            color = if (isToday && !isSelected) primaryColor else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🌟 $label",
                    color = if (isSelected) Color.White else textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color.White.copy(alpha = 0.2f) else Color(0xFF1E293B)
                ) {
                    Text(
                        text = "🌴 LEAVE DAY",
                        color = if (isSelected) Color.White else Color(0xFF60A5FA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (hasTasks) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else orangeDotColor)
                )
            }
        }
    }
}


@Composable
fun AgendaList(
    tasks: List<AppTask>,
    onToggleComplete: (AppTask) -> Unit,
    onEdit: (AppTask) -> Unit,
    onDelete: (AppTask) -> Unit,
    onStartFocus: (AppTask) -> Unit = {},
    primaryColor: Color,
    orangeColor: Color,
    textColor: Color,
    textColorSec: Color,
    cardBg: Color
) {
    val bellIcon = remember {
        ImageVector.Builder(
            name = "BellIcon",
            defaultWidth = 15.dp,
            defaultHeight = 16.dp,
            viewportWidth = 15f,
            viewportHeight = 16f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M0 6.0375C0 4.7875 0.278125 3.64062 0.834375 2.59687C1.39062 1.55312 2.1375 0.6875 3.075 0L3.95625 1.2C3.20625 1.75 2.60938 2.44375 2.16563 3.28125C1.72188 4.11875 1.5 5.0375 1.5 6.0375H0V6.0375M13.5 6.0375C13.5 5.0375 13.2781 4.11875 12.8344 3.28125C12.3906 2.44375 11.7937 1.75 11.0437 1.2L11.925 0C12.8625 0.6875 13.6094 1.55312 14.1656 2.59687C14.7219 3.64062 15 4.7875 15 6.0375H13.5V6.0375M1.5 12.7875V11.2875H3V6.0375C3 5 3.3125 4.07812 3.9375 3.27187C4.5625 2.46562 5.375 1.9375 6.375 1.6875V1.1625C6.375 0.85 6.48438 0.584375 6.70312 0.365625C6.92188 0.146875 7.1875 0.0375 7.5 0.0375C7.8125 0.0375 8.07812 0.146875 8.29688 0.365625C8.51562 0.584375 8.625 0.85 8.625 1.1625V1.6875C9.625 1.9375 10.4375 2.46562 11.0625 3.27187C11.6875 4.07812 12 5 12 6.0375V11.2875H13.5V12.7875H1.5V12.7875M7.5 7.1625V7.1625V7.1625V7.1625V7.1625V7.1625V7.1625V7.1625V7.1625M7.5 15.0375C7.0875 15.0375 6.73438 14.8906 6.44063 14.5969C6.14688 14.3031 6 13.95 6 13.5375H9C9 13.95 8.85312 14.3031 8.55937 14.5969C8.26562 14.8906 7.9125 15.0375 7.5 15.0375V15.0375M4.5 11.2875H10.5V6.0375C10.5 5.2125 10.2062 4.50625 9.61875 3.91875C9.03125 3.33125 8.325 3.0375 7.5 3.0375C6.675 3.0375 5.96875 3.33125 5.38125 3.91875C4.79375 4.50625 4.5 5.2125 4.5 6.0375V11.2875V11.2875"
            ).toNodes(),
            fill = SolidColor(Color(0xFFA1A1AA))
        ).build()
    }

    val editIcon = remember {
        ImageVector.Builder(
            name = "EditIcon",
            defaultWidth = 14.dp,
            defaultHeight = 14.dp,
            viewportWidth = 14f,
            viewportHeight = 14f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M1.5 12H2.56875L9.9 4.66875L8.83125 3.6L1.5 10.9312V12V12M0 13.5V10.3125L9.9 0.43125C10.05 0.29375 10.2156 0.1875 10.3969 0.1125C10.5781 0.0375 10.7687 0 10.9688 0C11.1688 0 11.3625 0.0375 11.55 0.1125C11.7375 0.1875 11.9 0.3 12.0375 0.45L13.0688 1.5C13.2188 1.6375 13.3281 1.8 13.3969 1.9875C13.4656 2.175 13.5 2.3625 13.5 2.55C13.5 2.75 13.4656 2.94063 13.3969 3.12188C13.3281 3.30313 13.2188 3.46875 13.0688 3.61875L3.1875 13.5H0V13.5M12 2.55V2.55L10.95 1.5V1.5L12 2.55V2.55M9.35625 4.14375L8.83125 3.6V3.6L9.9 4.66875V4.66875L9.35625 4.14375V4.14375"
            ).toNodes(),
            fill = SolidColor(Color(0xFFA1A1AA))
        ).build()
    }

    val trashIcon = remember {
        ImageVector.Builder(
            name = "TrashIcon",
            defaultWidth = 12.dp,
            defaultHeight = 14.dp,
            viewportWidth = 12f,
            viewportHeight = 14f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M2.25 13.5C1.8375 13.5 1.48437 13.3531 1.19062 13.0594C0.896875 12.7656 0.75 12.4125 0.75 12V2.25H0V0.75H3.75V0H8.25V0.75H12V2.25H11.25V12C11.25 12.4125 11.1031 12.7656 10.8094 13.0594C10.5156 13.3531 10.1625 13.5 9.75 13.5H2.25V13.5M9.75 2.25H2.25V12V12V12H9.75V12V12V2.25V2.25M3.75 10.5H5.25V3.75H3.75V10.5V10.5M6.75 10.5H8.25V3.75H6.75V10.5V10.5M2.25 2.25V2.25V12V12V12V12V12V12V2.25V2.25"
            ).toNodes(),
            fill = SolidColor(Color(0xFFA1A1AA))
        ).build()
    }

    val todoHeaderIcon = remember {
        ImageVector.Builder(
            name = "TodoHeaderIcon",
            defaultWidth = 15.dp,
            defaultHeight = 12.dp,
            viewportWidth = 15f,
            viewportHeight = 12f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M2.6625 11.3062L0 8.64375L1.05 7.59375L2.64375 9.1875L5.83125 6L6.88125 7.06875L2.6625 11.3062V11.3062M2.6625 5.30625L0 2.64375L1.05 1.59375L2.64375 3.1875L5.83125 0L6.88125 1.06875L2.6625 5.30625V5.30625M8.25 9.80625V8.30625H15V9.80625H8.25V9.80625M8.25 3.80625V2.30625H15V3.80625H8.25V3.80625"
            ).toNodes(),
            fill = SolidColor(Color(0xFFA1A1AA))
        ).build()
    }

    val reminders = remember(tasks) { tasks.filter { it.reminder || it.isGenerated == 1 } }
    val reminderIds = remember(reminders) { reminders.map { it.id }.toSet() }
    val todos = remember(tasks, reminderIds) { tasks.filter { !it.reminder && it.isGenerated == 0 && !reminderIds.contains(it.id) } }
    val recurring = remember(tasks, reminderIds) { tasks.filter { it.isGenerated == 1 && !reminderIds.contains(it.id) } }



    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (reminders.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = bellIcon,
                            contentDescription = null,
                            tint = Color(0xFFA1A1AA),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Time-Critical Reminders",
                            color = Color(0xFFA1A1AA),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

            }
            items(reminders, key = { "rem_${it.id}" }) { task ->
                ReminderItem(
                    task = task,
                    onToggleComplete = onToggleComplete,
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onStartFocus = onStartFocus,
                    textColor = textColor,
                    textColorSec = textColorSec,
                    accentColor = orangeColor,
                    cardBg = cardBg
                )
            }
        }

        if (todos.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = todoHeaderIcon,
                        contentDescription = null,
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Daily To-Dos",
                        color = Color(0xFFA1A1AA),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
            items(todos, key = { "todo_${it.id}" }) { task ->
                TodoItem(
                    task = task,
                    onToggleComplete = onToggleComplete,
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onStartFocus = onStartFocus,
                    textColor = textColor,
                    textColorSec = textColorSec,
                    cardBg = cardBg
                )
            }
        }


        if (recurring.isNotEmpty()) {
            item {
                Text(
                    text = "🔄 RECURRING PLANS",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                )
            }
            items(recurring, key = { "rec_${it.id}" }) { task ->

                TodoItem(
                    task = task,
                    onToggleComplete = onToggleComplete,
                    onEdit = {},
                    onDelete = onDelete,
                    onStartFocus = onStartFocus,
                    textColor = textColor,
                    textColorSec = textColorSec,
                    cardBg = cardBg
                )
            }
        }
    }
}

@Composable
fun AdMobTestBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "Ad",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "Nice job! Test ad from Google AdMob",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF475569)
            ) {
                Text(
                    text = "AdMob",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun ReminderItem(
    task: AppTask,
    onToggleComplete: (AppTask) -> Unit,
    onEdit: (AppTask) -> Unit,
    onDelete: (AppTask) -> Unit,
    onStartFocus: (AppTask) -> Unit = {},
    textColor: Color,
    textColorSec: Color,
    accentColor: Color,
    cardBg: Color
) {
    val clockIcon = remember {
        ImageVector.Builder(
            name = "ClockIcon",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C12.42 16 16 12.42 16 8C16 3.58 12.42 0 8 0ZM8 14.4C4.47 14.4 1.6 11.53 1.6 8C1.6 4.47 4.47 1.6 8 1.6C11.53 1.6 14.4 4.47 14.4 8C14.4 11.53 11.53 14.4 8 14.4ZM8.4 4H7.2V8.8L11.4 11.32L12 10.34L8.4 8.2V4Z"
            ).toNodes(),
            fill = SolidColor(Color(0xFFA1A1AA))
        ).build()
    }

    val editIcon = remember {
        ImageVector.Builder(
            name = "EditIcon",
            defaultWidth = 14.dp,
            defaultHeight = 14.dp,
            viewportWidth = 14f,
            viewportHeight = 14f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M1.5 12H2.56875L9.9 4.66875L8.83125 3.6L1.5 10.9312V12V12M0 13.5V10.3125L9.9 0.43125C10.05 0.29375 10.2156 0.1875 10.3969 0.1125C10.5781 0.0375 10.7687 0 10.9688 0C11.1688 0 11.3625 0.0375 11.55 0.1125C11.7375 0.1875 11.9 0.3 12.0375 0.45L13.0688 1.5C13.2188 1.6375 13.3281 1.8 13.3969 1.9875C13.4656 2.175 13.5 2.3625 13.5 2.55C13.5 2.75 13.4656 2.94063 13.3969 3.12188C13.3281 3.30313 12.2188 3.46875 13.0688 3.61875L3.1875 13.5H0V13.5M12 2.55V2.55L10.95 1.5V1.5L12 2.55V2.55M9.35625 4.14375L8.83125 3.6V3.6L9.9 4.66875V4.66875L9.35625 4.14375V4.14375"
            ).toNodes(),
            fill = SolidColor(Color(0xFFA1A1AA))
        ).build()
    }

    val trashIcon = remember {
        ImageVector.Builder(
            name = "TrashIcon",
            defaultWidth = 12.dp,
            defaultHeight = 14.dp,
            viewportWidth = 12f,
            viewportHeight = 14f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M2.25 13.5C1.8375 13.5 1.48437 13.3531 1.19062 13.0594C0.896875 12.7656 0.75 12.4125 0.75 12V2.25H0V0.75H3.75V0H8.25V0.75H12V2.25H11.25V12C11.25 12.4125 11.1031 12.7656 10.8094 13.0594C10.5156 13.3531 10.1625 13.5 9.75 13.5H2.25V13.5M9.75 2.25H2.25V12V12V12H9.75V12V12V2.25V2.25M3.75 10.5H5.25V3.75H3.75V10.5V10.5M6.75 10.5H8.25V3.75H6.75V10.5V10.5M2.25 2.25V2.25V12V12V12V12V12V12V2.25V2.25"
            ).toNodes(),
            fill = SolidColor(Color(0xFFA1A1AA))
        ).build()
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleComplete(task) },
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
        border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
        shape = MatrixShapes.Lg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(MatrixColors.Secondary)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = clockIcon,
                            contentDescription = "Time",
                            tint = Color(0xFFA1A1AA),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = task.reminderTime ?: "All Day",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (task.isGenerated == 1) {
                            Surface(
                                shape = MatrixShapes.Sm,
                                color = MatrixColors.TertiaryContainer.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, MatrixColors.Tertiary)
                            ) {
                                Text(
                                    text = "RECURRING",
                                    color = MatrixColors.Tertiary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = MatrixShapes.Sm,
                                color = Color(0xFF2E2415)
                            ) {
                                Text(
                                    text = "URGENT",
                                    color = MatrixColors.Secondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "⏱️",
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { onStartFocus(task) }
                        )
                        Icon(
                            imageVector = editIcon,
                            contentDescription = "Edit",
                            tint = Color(0xFFA1A1AA),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onEdit(task) }
                        )
                        Icon(
                            imageVector = trashIcon,
                            contentDescription = "Delete",
                            tint = Color(0xFFA1A1AA),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onDelete(task) }
                        )
                    }

                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = task.title,
                    color = if (task.completed) textColorSec else textColor,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )

                if (!task.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        color = textColorSec,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }

                if (task.hasEverFocused) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⭐ Focused: ${task.formattedFocusDuration}",
                        color = MatrixColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
fun TodoItem(
    task: AppTask,
    onToggleComplete: (AppTask) -> Unit,
    onEdit: (AppTask) -> Unit,
    onDelete: (AppTask) -> Unit,
    onStartFocus: (AppTask) -> Unit = {},
    textColor: Color,
    textColorSec: Color,
    cardBg: Color
) {
    val isGeneratedTask = task.isGenerated == 1
    val isUserCreated = !task.id.startsWith("sys_")

    val editIcon = remember {
        ImageVector.Builder(
            name = "EditIcon",
            defaultWidth = 14.dp,
            defaultHeight = 14.dp,
            viewportWidth = 14f,
            viewportHeight = 14f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M1.5 12H2.56875L9.9 4.66875L8.83125 3.6L1.5 10.9312V12V12M0 13.5V10.3125L9.9 0.43125C10.05 0.29375 10.2156 0.1875 10.3969 0.1125C10.5781 0.0375 10.7687 0 10.9688 0C11.1688 0 11.3625 0.0375 11.55 0.1125C11.7375 0.1875 11.9 0.3 12.0375 0.45L13.0688 1.5C13.2188 1.6375 13.3281 1.8 13.3969 1.9875C13.4656 2.175 13.5 2.3625 13.5 2.55C13.5 2.75 13.4656 2.94063 13.3969 3.12188C13.3281 3.30313 12.2188 3.46875 13.0688 3.61875L3.1875 13.5H0V13.5M12 2.55V2.55L10.95 1.5V1.5L12 2.55V2.55M9.35625 4.14375L8.83125 3.6V3.6L9.9 4.66875V4.66875L9.35625 4.14375V4.14375"
            ).toNodes(),
            fill = SolidColor(Color(0xFFA1A1AA))
        ).build()
    }

    val trashIcon = remember {
        ImageVector.Builder(
            name = "TrashIcon",
            defaultWidth = 12.dp,
            defaultHeight = 14.dp,
            viewportWidth = 12f,
            viewportHeight = 14f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M2.25 13.5C1.8375 13.5 1.48437 13.3531 1.19062 13.0594C0.896875 12.7656 0.75 12.4125 0.75 12V2.25H0V0.75H3.75V0H8.25V0.75H12V2.25H11.25V12C11.25 12.4125 11.1031 12.7656 10.8094 13.0594C10.5156 13.3531 10.1625 13.5 9.75 13.5H2.25V13.5M9.75 2.25H2.25V12V12V12H9.75V12V12V2.25V2.25M3.75 10.5H5.25V3.75H3.75V10.5V10.5M6.75 10.5H8.25V3.75H6.75V10.5V10.5M2.25 2.25V2.25V12V12V12V12V12V12V2.25V2.25"
            ).toNodes(),
            fill = SolidColor(Color(0xFFA1A1AA))
        ).build()
    }


    val accentColor = when {
        isGeneratedTask -> MatrixColors.Tertiary
        task.priority >= 2 -> MatrixColors.Secondary
        else -> MatrixColors.Primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleComplete(task) },
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
        border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
        shape = MatrixShapes.Lg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)

        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(MatrixShapes.Sm)
                            .border(1.5.dp, if (task.completed) MatrixColors.Tertiary else MatrixColors.Outline, MatrixShapes.Sm)
                            .background(if (task.completed) MatrixColors.Tertiary.copy(alpha = 0.2f) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.completed) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = MatrixColors.Tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isGeneratedTask) {
                                Surface(
                                    shape = MatrixShapes.Sm,
                                    color = MatrixColors.TertiaryContainer.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, MatrixColors.Tertiary),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "🔄 Recurring",
                                        color = MatrixColors.Tertiary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    )
                                }
                            }
                            Text(
                                text = task.title,
                                color = if (task.completed) textColorSec else textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textDecoration = if (task.completed) TextDecoration.LineThrough else null
                            )
                        }
                        if (!task.description.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = task.description,
                                color = textColorSec,
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        }
                        if (task.hasEverFocused) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "⭐ Focused: ${task.formattedFocusDuration}",
                                color = MatrixColors.Primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                val (categoryName, categoryColor) = when (task.priority) {
                    2 -> Pair("Work", MatrixColors.Secondary)
                    3 -> Pair("Health", MatrixColors.Error)
                    else -> Pair("Personal", MatrixColors.Tertiary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏱️",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { onStartFocus(task) }
                    )

                    Surface(
                        shape = MatrixShapes.Xl,
                        color = categoryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, categoryColor),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = categoryName,
                                color = categoryColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(textColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "No tasks",
                tint = textColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No plans for this date.",
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = "Tap + to add a task or reminder",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

