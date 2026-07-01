package com.ticktock.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ticktock.R
import com.ticktock.data.TimeAlertProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TickTock") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "Add profile")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PermissionBanners(
                viewModel = viewModel,
                uiState = uiState,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.profiles.isEmpty()) {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.profiles, key = { it.id }) { profile ->
                            ProfileCard(
                                profile = profile,
                                nextAnnouncement = viewModel.formatNextAnnouncement(profile),
                                onClick = { viewModel.editProfile(profile) },
                                onDelete = { viewModel.deleteProfile(profile) },
                            )
                        }
                    }
                }
            }
        }
    }

    when (val dialog = uiState.profileDialog) {
        ProfileDialogState.Adding -> {
            ProfileFormDialog(
                existingProfile = null,
                onDismiss = viewModel::dismissProfileDialog,
                onConfirm = viewModel::saveProfile,
            )
        }
        is ProfileDialogState.Editing -> {
            ProfileFormDialog(
                existingProfile = dialog.profile,
                onDismiss = viewModel::dismissProfileDialog,
                onConfirm = viewModel::saveProfile,
            )
        }
        ProfileDialogState.Hidden -> Unit
    }

    if (uiState.showAlarmPermissionDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissAlarmPermissionDialog,
            title = { Text(stringResource(R.string.permission_alarm_title)) },
            text = { Text(stringResource(R.string.permission_alarm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.openExactAlarmSettings()
                        viewModel.dismissAlarmPermissionDialog()
                    },
                ) {
                    Text(stringResource(R.string.permission_alarm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissAlarmPermissionDialog) {
                    Text("Not now")
                }
            },
        )
    }
}

@Composable
private fun PermissionBanners(
    viewModel: MainViewModel,
    uiState: MainUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (uiState.needsExactAlarmPermission) {
            PermissionBanner(
                message = stringResource(R.string.permission_alarm_banner),
                actionLabel = stringResource(R.string.permission_alarm_action),
                onAction = viewModel::openExactAlarmSettings,
            )
        }
        if (uiState.needsBatteryOptimizationDisabled) {
            PermissionBanner(
                message = stringResource(R.string.permission_battery_banner),
                actionLabel = stringResource(R.string.permission_battery_action),
                onAction = viewModel::openBatteryOptimizationSettings,
            )
        }
    }
}

@Composable
private fun PermissionBanner(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No time alerts yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to create one",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun ProfileCard(
    profile: TimeAlertProfile,
    nextAnnouncement: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick),
            ) {
                Text(
                    text = formatTimeRange(profile),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Every ${formatFrequency(profile.frequencyMinutes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                if (nextAnnouncement != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.next_announcement, nextAnnouncement),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.profile_schedule_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete profile",
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

private fun formatTimeRange(profile: TimeAlertProfile): String {
    return "${formatTime12h(profile.startHour, profile.startMinute)} – ${formatTime12h(profile.endHour, profile.endMinute)}"
}

private fun formatFrequency(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes mins"
        minutes % 60 == 0 -> {
            val hours = minutes / 60
            if (hours == 1) "1 hour" else "$hours hours"
        }
        else -> "$minutes mins"
    }
}
