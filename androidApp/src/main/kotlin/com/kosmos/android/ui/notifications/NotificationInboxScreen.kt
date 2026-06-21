package com.kosmos.android.ui.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.i18n.localized
import com.kosmos.android.notification.NotificationEntry
import com.kosmos.android.notification.ScheduledReminder
import com.kosmos.android.ui.designsystem.tokens.KosmosThemeExt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationInboxScreen(
    entries: List<NotificationEntry>,
    upcomingReminders: List<ScheduledReminder>,
    onBack: () -> Unit,
    onCancelReminder: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "History")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(localized("notifications_title"), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = tab == index,
                        onClick = { tab = index },
                        text = { Text(label) },
                    )
                }
            }
            when (tab) {
                0 -> UpcomingRemindersList(upcomingReminders, onCancelReminder)
                1 -> HistoryList(entries)
            }
        }
    }
}

@Composable
private fun UpcomingRemindersList(
    reminders: List<ScheduledReminder>,
    onCancel: (String) -> Unit,
) {
    if (reminders.isEmpty()) {
        EmptyState(
            title = "No upcoming reminders",
            subtitle = "Tap ⏰ on any insight on Home to schedule one.",
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(reminders, key = { it.verdictId }) { reminder ->
            ReminderRow(reminder, onCancel)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
private fun HistoryList(entries: List<NotificationEntry>) {
    if (entries.isEmpty()) {
        EmptyState(
            title = localized("notifications_empty"),
            subtitle = localized("notifications_empty_sub"),
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(entries, key = { it.id }) { entry ->
            NotificationRow(entry)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(text = title, color = KosmosThemeExt.colors.textSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = subtitle, fontSize = 13.sp, color = KosmosThemeExt.colors.textMuted)
    }
}

@Composable
private fun ReminderRow(reminder: ScheduledReminder, onCancel: (String) -> Unit) {
    val timeLabel = SimpleDateFormat("EEE, d MMM · h:mm a", Locale.getDefault())
        .format(Date(reminder.fireTimeMs))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${reminder.emoji} ${reminder.title}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reminder.detail,
                fontSize = 14.sp,
                color = KosmosThemeExt.colors.textSecondary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = timeLabel, fontSize = 11.sp, color = KosmosThemeExt.colors.textMuted)
        }
        TextButton(onClick = { onCancel(reminder.verdictId) }) {
            Text("Cancel")
        }
    }
}

@Composable
private fun NotificationRow(entry: NotificationEntry) {
    val timeLabel = SimpleDateFormat("EEE, d MMM · h:mm a", Locale.getDefault())
        .format(Date(entry.timestampMs))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Text(
            text = entry.title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.body,
            fontSize = 14.sp,
            color = KosmosThemeExt.colors.textSecondary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = timeLabel,
            fontSize = 11.sp,
            color = KosmosThemeExt.colors.textMuted,
        )
    }
}
