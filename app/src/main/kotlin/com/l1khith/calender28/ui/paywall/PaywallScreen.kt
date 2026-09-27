package com.l1khith.calender28.ui.paywall

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.l1khith.calender28.Calender28Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.l1khith.calender28.R
import com.l1khith.calender28.billing.RevenueCatManager
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PackageType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var packages by remember { mutableStateOf<List<Package>>(emptyList()) }
    var selectedPackage by remember { mutableStateOf<Package?>(null) }
    var loading by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    fun loadOfferings() {
        loading = true
        loadError = null
        scope.launch {
            val offerings = RevenueCatManager.getOfferings()
            val current = offerings?.current ?: offerings?.all?.values?.firstOrNull()
            if (current == null) {
                loadError = "Unable to load subscription plans. Please verify your internet connection or Google Play account."
                loaded = true
                loading = false
                return@launch
            }
            val available = current.availablePackages
            if (available.isEmpty()) {
                loadError = "No subscription packages are currently active. Please check back later."
                loaded = true
                loading = false
                return@launch
            }
            packages = available
            // Preferred default: Annual, else Monthly, else first package
            selectedPackage = available.find {
                it.packageType == PackageType.ANNUAL ||
                        it.identifier.contains("annual", ignoreCase = true) ||
                        it.identifier.contains("year", ignoreCase = true)
            } ?: available.find {
                it.packageType == PackageType.MONTHLY ||
                        it.identifier.contains("month", ignoreCase = true)
            } ?: available.firstOrNull()
            loaded = true
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        loadOfferings()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.paywall_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.paywall_headline),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            BenefitsList()

            Spacer(Modifier.height(24.dp))

            when {
                !loaded -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                loadError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = loadError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(onClick = { loadOfferings() }) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    packages.forEach { pkg ->
                        val isAnnual = pkg.packageType == PackageType.ANNUAL ||
                                pkg.identifier.contains("annual", ignoreCase = true) ||
                                pkg.identifier.contains("year", ignoreCase = true)
                        val isMonthly = pkg.packageType == PackageType.MONTHLY ||
                                pkg.identifier.contains("month", ignoreCase = true)
                        val isLifetime = pkg.packageType == PackageType.LIFETIME ||
                                pkg.identifier.contains("life", ignoreCase = true)

                        val title = when {
                            isAnnual -> "Annual"
                            isMonthly -> "Monthly"
                            isLifetime -> "Lifetime"
                            else -> pkg.product.title.substringBefore(" (").ifBlank {
                                pkg.identifier.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            }
                        }

                        val subtitle = when {
                            isAnnual -> "Best value • Billed yearly"
                            isMonthly -> "Flexible • Cancel anytime"
                            isLifetime -> "Pay once • Lifetime access"
                            else -> pkg.product.description.ifBlank { "Cancel anytime" }
                        }

                        val badge = if (isAnnual) "BEST VALUE" else null

                        PlanCard(
                            title = title,
                            subtitle = subtitle,
                            price = pkg.product.price.formatted,
                            badge = badge,
                            isSelected = selectedPackage?.identifier == pkg.identifier,
                            onClick = { selectedPackage = pkg }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val activity = context.findActivity()
                        ?: Calender28Application.currentActivity?.get()
                    if (activity == null) {
                        Log.e("PaywallScreen", "Cannot find Activity context to launch purchase!")
                        scope.launch {
                            snackbarHostState.showSnackbar("Unable to launch purchase flow. Please try reopening the screen.")
                        }
                        return@Button
                    }
                    val pkg = selectedPackage ?: packages.firstOrNull()
                    if (pkg == null) {
                        Log.e("PaywallScreen", "No package selected!")
                        scope.launch {
                            snackbarHostState.showSnackbar("No package selected. Please select a plan.")
                        }
                        return@Button
                    }
                    Log.d("PaywallScreen", "Continue clicked! Starting purchase: package=${pkg.identifier}, product=${pkg.product.id}, activity=$activity")
                    loading = true
                    scope.launch {
                        try {
                            when (val r = RevenueCatManager.purchase(activity, pkg)) {
                                is RevenueCatManager.PurchaseResult.Success -> {
                                    Log.d("PaywallScreen", "Purchase succeeded!")
                                    snackbarHostState.showSnackbar("Welcome to Pro")
                                    onClose()
                                }
                                is RevenueCatManager.PurchaseResult.Cancelled -> {
                                    Log.d("PaywallScreen", "Purchase cancelled by user")
                                }
                                is RevenueCatManager.PurchaseResult.Error -> {
                                    Log.e("PaywallScreen", "Purchase error: ${r.message}")
                                    snackbarHostState.showSnackbar(r.message)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("PaywallScreen", "Unexpected error in purchase flow: ${e.message}", e)
                            snackbarHostState.showSnackbar(e.message ?: "An unexpected error occurred")
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading && (selectedPackage != null || packages.isNotEmpty()),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        stringResource(R.string.paywall_continue),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = {
                    loading = true
                    scope.launch {
                        when (val r = RevenueCatManager.restore()) {
                            is RevenueCatManager.PurchaseResult.Success -> {
                                snackbarHostState.showSnackbar("Purchases restored")
                                onClose()
                            }
                            is RevenueCatManager.PurchaseResult.Error -> {
                                snackbarHostState.showSnackbar(r.message)
                            }
                            else -> { }
                        }
                        loading = false
                    }
                },
                enabled = !loading
            ) {
                Text(stringResource(R.string.paywall_restore))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BenefitsList() {
    val benefits = listOf(
        "Ad-free experience",
        "Unlimited habit cycles",
        "All widget sizes",
        "Advanced stats",
        "Sparky full evolution",
        "Data export"
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        benefits.forEach { b ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(b, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    subtitle: String,
    price: String,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    if (badge != null) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

