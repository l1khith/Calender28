package com.l1khith.calender28

import com.l1khith.calender28.security.AppLockManager
import org.junit.Assert.*
import org.junit.Test

class AppLockTest {

    @Test
    fun `test AppLockManager initial state is unlocked by default`() {
        assertFalse(AppLockManager.isAppLockEnabled.value)
        assertFalse(AppLockManager.isLocked.value)
    }

    @Test
    fun `test unlock sets isLocked to false`() {
        AppLockManager.unlock()
        assertFalse(AppLockManager.isLocked.value)
        assertEquals(com.l1khith.calender28.security.LockState.Unlocked, AppLockManager.lockState.value)
    }

    @Test
    fun `test state machine transitions`() {
        AppLockManager.unlock()
        assertEquals(com.l1khith.calender28.security.LockState.Unlocked, AppLockManager.lockState.value)
        assertFalse(AppLockManager.isLocked.value)
    }
}
