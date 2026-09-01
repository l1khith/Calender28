package com.l1khith.calender28

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.l1khith.calender28.ui.FixedCalendarApp
import com.l1khith.calender28.ui.theme.MatrixTheme
import com.l1khith.calender28.viewmodel.FixedCalendarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.l1khith.calender28.viewmodel.AppViewModelProvider

class MainActivity : FragmentActivity() {

    private val viewModel: FixedCalendarViewModel by viewModels { AppViewModelProvider.Factory }
    private var deepLinkIdState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                MobileAds.initialize(applicationContext) { status ->
                    Log.d("MainActivity", "AdMob initialized: ${status.adapterStatusMap}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "AdMob initialization error", e)
            }
        }

        enableEdgeToEdge()

        deepLinkIdState = intent?.getStringExtra("selected_task_id")

        setContent {
            val deepLinkId = rememberSaveable { mutableStateOf(deepLinkIdState) }
            val isLocked by com.l1khith.calender28.security.AppLockManager.isLocked.collectAsState()

            MatrixTheme {
                FixedCalendarApp(
                    viewModel = viewModel,
                    initialTaskId = deepLinkId.value,
                    onExitApp = { finish() }
                )

                if (isLocked) {
                    com.l1khith.calender28.security.AppLockOverlay(
                        onUnlockSuccess = {
                            com.l1khith.calender28.security.AppLockManager.unlock()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.l1khith.calender28.security.AppLockManager.onActivityResumed()
    }

    override fun onPause() {
        super.onPause()
        com.l1khith.calender28.security.AppLockManager.onActivityPaused()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkIdState = intent.getStringExtra("selected_task_id")
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MatrixTheme {
        Text("Calender28 Preview")
    }
}
