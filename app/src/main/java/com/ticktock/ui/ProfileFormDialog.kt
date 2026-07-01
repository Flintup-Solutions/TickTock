package com.ticktock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ticktock.data.TimeAlertProfile
import java.util.Locale

private val FREQUENCY_OPTIONS = listOf(10, 20, 30, 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFormDialog(
    existingProfile: TimeAlertProfile?,
    onDismiss: () -> Unit,
    onConfirm: (frequencyMinutes: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit,
) {
    val isEditing = existingProfile != null
    val initialStart = remember(existingProfile) {
        existingProfile?.let { from24Hour(it.startHour, it.startMinute) }
            ?: Time12h(hour = 6, minute = 0, isPm = false)
    }
    val initialEnd = remember(existingProfile) {
        existingProfile?.let { from24Hour(it.endHour, it.endMinute) }
            ?: Time12h(hour = 8, minute = 0, isPm = false)
    }

    var frequencyMinutes by remember(existingProfile) {
        mutableIntStateOf(existingProfile?.frequencyMinutes ?: 10)
    }
    var frequencyExpanded by remember { mutableStateOf(false) }

    var startHour by remember(existingProfile) { mutableIntStateOf(initialStart.hour) }
    var startMinute by remember(existingProfile) { mutableIntStateOf(initialStart.minute) }
    var startIsPm by remember(existingProfile) { mutableStateOf(initialStart.isPm) }

    var endHour by remember(existingProfile) { mutableIntStateOf(initialEnd.hour) }
    var endMinute by remember(existingProfile) { mutableIntStateOf(initialEnd.minute) }
    var endIsPm by remember(existingProfile) { mutableStateOf(initialEnd.isPm) }

    val isValid = isEndAfterStart(
        startHour = startHour,
        startMinute = startMinute,
        startIsPm = startIsPm,
        endHour = endHour,
        endMinute = endMinute,
        endIsPm = endIsPm,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Time Alert" else "New Time Alert") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ExposedDropdownMenuBox(
                    expanded = frequencyExpanded,
                    onExpandedChange = { frequencyExpanded = it },
                ) {
                    OutlinedTextField(
                        value = formatFrequencyLabel(frequencyMinutes),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = frequencyExpanded,
                        onDismissRequest = { frequencyExpanded = false },
                    ) {
                        FREQUENCY_OPTIONS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(formatFrequencyLabel(option)) },
                                onClick = {
                                    frequencyMinutes = option
                                    frequencyExpanded = false
                                },
                            )
                        }
                    }
                }

                Text(
                    text = "Active between",
                    style = MaterialTheme.typography.labelLarge,
                )

                TimePickerRow(
                    label = "Start",
                    hour = startHour,
                    minute = startMinute,
                    isPm = startIsPm,
                    onHourChange = { startHour = it },
                    onMinuteChange = { startMinute = it },
                    onPeriodChange = { startIsPm = it },
                )

                TimePickerRow(
                    label = "End",
                    hour = endHour,
                    minute = endMinute,
                    isPm = endIsPm,
                    onHourChange = { endHour = it },
                    onMinuteChange = { endMinute = it },
                    onPeriodChange = { endIsPm = it },
                )

                if (!isValid) {
                    Text(
                        text = "End time must be after start time",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        frequencyMinutes,
                        to24Hour(startHour, startIsPm),
                        startMinute,
                        to24Hour(endHour, endIsPm),
                        endMinute,
                    )
                },
                enabled = isValid,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun TimePickerRow(
    label: String,
    hour: Int,
    minute: Int,
    isPm: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onPeriodChange: (Boolean) -> Unit,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NumberStepper(
                label = "Hour",
                value = hour,
                range = 1..12,
                displayFormat = "%d",
                onValueChange = onHourChange,
                modifier = Modifier.weight(1f),
            )
            NumberStepper(
                label = "Min",
                value = minute,
                range = 0..59,
                displayFormat = "%02d",
                onValueChange = onMinuteChange,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        PeriodSelector(isPm = isPm, onPeriodChange = onPeriodChange)
    }
}

@Composable
private fun PeriodSelector(
    isPm: Boolean,
    onPeriodChange: (Boolean) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !isPm,
            onClick = { onPeriodChange(false) },
            label = { Text("AM") },
        )
        FilterChip(
            selected = isPm,
            onClick = { onPeriodChange(true) },
            label = { Text("PM") },
        )
    }
}

@Composable
private fun NumberStepper(
    label: String,
    value: Int,
    range: IntRange,
    displayFormat: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = {
                    val newValue = value - 1
                    if (newValue in range) onValueChange(newValue)
                },
                enabled = value > range.first,
            ) {
                Text("−")
            }
            Text(
                text = String.format(Locale.getDefault(), displayFormat, value),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(
                onClick = {
                    val newValue = value + 1
                    if (newValue in range) onValueChange(newValue)
                },
                enabled = value < range.last,
            ) {
                Text("+")
            }
        }
    }
}

private fun formatFrequencyLabel(minutes: Int): String {
    return when {
        minutes < 60 -> "Every $minutes minutes"
        minutes % 60 == 0 -> {
            val hours = minutes / 60
            if (hours == 1) "Every 1 hour" else "Every $hours hours"
        }
        else -> "Every $minutes minutes"
    }
}
