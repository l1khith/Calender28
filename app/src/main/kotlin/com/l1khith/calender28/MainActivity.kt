package com.l1khith.calender28

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.l1khith.calender28.ui.FixedCalendarApp
import com.l1khith.calender28.ui.theme.MatrixTheme
import com.l1khith.calender28.viewmodel.FixedCalendarViewModel

import com.l1khith.calender28.viewmodel.AppViewModelProvider

class MainActivity : FragmentActivity() {

    private val viewModel: FixedCalendarViewModel by viewModels { AppViewModelProvider.Factory }
    private var deepLinkIdState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        deepLinkIdState = intent?.getStringExtra("selected_task_id")

        setContent {
            val deepLinkId = rememberSaveable { mutableStateOf(deepLinkIdState) }
            val isLocked by com.l1khith.calender28.security.AppLockManager.isLocked.collectAsStateWithLifecycle()
            var showSplash by rememberSaveable { mutableStateOf(deepLinkIdState == null) }

            MatrixTheme {
                if (isLocked) {
                    // Exclusive branch: don't render splash/workspace underneath opaque lock
                    com.l1khith.calender28.security.AppLockOverlay(
                        onUnlockSuccess = {
                            com.l1khith.calender28.security.AppLockManager.unlock()
                        }
                    )
                } else {
                    androidx.compose.animation.AnimatedContent(
                        targetState = showSplash,
                        transitionSpec = {
                            androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) togetherWith
                                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
                        },
                        label = "SplashTransition"
                    ) { isSplash ->
                        if (isSplash) {
                            com.l1khith.calender28.ui.MatrixSplashScreen(
                                onEnterWorkspace = { showSplash = false },
                                onExitApp = { finish() }
                            )
                        } else {
                            FixedCalendarApp(
                                viewModel = viewModel,
                                initialTaskId = deepLinkId.value,
                                onExitApp = { finish() }
                            )
                        }
                    }
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

        // If an active focus session had screen pinning enabled and the user navigated away,
        // break focus so no reward is given!
        val currentFocus = com.l1khith.calender28.service.FocusSessionManager.focusState.value
        if (currentFocus is com.l1khith.calender28.service.FocusState.Active && currentFocus.isScreenPinned) {
            com.l1khith.calender28.utils.ScreenPinningHelper.stopPinning(this)
            com.l1khith.calender28.service.FocusSessionManager.stopOrCancelFocus(this, markAsCancelled = true)
        }
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
