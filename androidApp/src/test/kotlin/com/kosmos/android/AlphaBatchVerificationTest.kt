package com.kosmos.android

import com.kosmos.android.notification.ReminderScheduler
import com.kosmos.android.widget.BestWindowsWidget
import com.kosmos.android.widget.ModeAwareWidget
import com.kosmos.android.widget.NextRainWidget
import com.kosmos.android.widget.TodaysCallWidget
import com.kosmos.shared.mode.ModeCatalog
import com.kosmos.shared.mode.UserMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Compile-time wiring checks for the Alpha homemaker batch (nav/widgets/reminders ship in UI layer).
 */
class AlphaBatchVerificationTest {

    @Test
    fun homemakerMode_isInCatalogAndSelectable() {
        assertTrue(UserMode.selectableModes.contains(UserMode.HOMEMAKER))
        val entry = ModeCatalog.live.find { it.userMode == UserMode.HOMEMAKER }
        assertNotNull(entry)
        assertEquals(com.kosmos.shared.mode.ModeAvailability.LIVE, entry!!.availability)
    }

    @Test
    fun purposeBuiltWidgets_exist() {
        assertNotNull(TodaysCallWidget())
        assertNotNull(NextRainWidget())
        assertNotNull(ModeAwareWidget())
        assertNotNull(BestWindowsWidget())
    }

    @Test
    fun reminderScheduler_isReachable() {
        assertNotNull(ReminderScheduler::class.java)
    }
}
