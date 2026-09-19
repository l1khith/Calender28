package com.l1khith.calender28.ui.paywall

import android.app.Activity
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

    var monthlyPackage by remember { mutableStateOf<Package?>(null) }
    var yearlyPackage by remember { mutableStateOf<Package?>(null) }
    var selectedPackage by remember { mutableStateOf<Package?>(null) }
    var loading by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val offerings = RevenueCatManager.getOfferings()
        val current = offerings?.current
        if (current == null) {
            loadError = "Unable to load offerings. Check your connection."
            loaded = true
            return@LaunchedEffect
        }
        current.availablePackages.forEach { pkg ->
            when (pkg.packageType) {
                PackageType.MONTHLY -> monthlyPackage = pkg
                PackageType.ANNUAL  -> yearlyPackage = pkg
                else -> {
                    if (pkg.identifier.contains("month", true))
                        monthlyPackage = pkg
                    else yearlyPackage = pkg
                }
            }
        }
        // Default selection: Yearly (best value)
        selectedPackage = yearlyPackage ?: monthlyPackage
        loaded = true
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
                !loaded -> CircularProgressIndicator()

                loadError != null -> {
                    Text(
                        text = loadError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    yearlyPackage?.let { pkg ->
                        PlanCard(
                            title = "Yearly",
                            subtitle = "Best value",
                            price = pkg.product.price.formatted,
                            badge = "BEST VALUE",
                            isSelected = selectedPackage?.identifier == pkg.identifier,
                            onClick = { selectedPackage = pkg }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    monthlyPackage?.let { pkg ->
                        PlanCard(
                            title = "Monthly",
                            subtitle = "Cancel anytime",
                            price = pkg.product.price.formatted,
                            badge = null,
                            isSelected = selectedPackage?.identifier == pkg.identifier,
                            onClick = { selectedPackage = pkg }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val pkg = selectedPackage ?: return@Button
                    val activity = context as? Activity ?: return@Button
                    loading = true
                    scope.launch {
                        when (val r = RevenueCatManager.purchase(activity, pkg)) {
                            is RevenueCatManager.PurchaseResult.Success -> {
                                snackbarHostState.showSnackbar("Welcome to Pro")
                                onClose()
                            }
                            is RevenueCatManager.PurchaseResult.Cancelled -> { }
                            is RevenueCatManager.PurchaseResult.Error -> {
                                snackbarHostState.showSnackbar(r.message)
                            }
                        }
                        loading = false
                    }
                },
                enabled = selectedPackage != null && !loading,
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
