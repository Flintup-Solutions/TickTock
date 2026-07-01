package com.ticktock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ticktock.data.TimeAlertProfile

private val FREQUENCY_OPTIONS = listOf(5, 10, 20, 30, 60)

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

    var startHourText by remember(existingProfile) { mutableStateOf(initialStart.hour.toString()) }
    var startMinuteText by remember(existingProfile) { mutableStateOf(initialStart.minute.toString()) }
    var startIsPm by remember(existingProfile) { mutableStateOf(initialStart.isPm) }

    var endHourText by remember(existingProfile) { mutableStateOf(initialEnd.hour.toString()) }
    var endMinuteText by remember(existingProfile) { mutableStateOf(initialEnd.minute.toString()) }
    var endIsPm by remember(existingProfile) { mutableStateOf(initialEnd.isPm) }

    val parsedStartHour = startHourText.toIntOrNull()
    val parsedStartMinute = startMinuteText.toIntOrNull()
    val parsedEndHour = endHourText.toIntOrNull()
    val parsedEndMinute = endMinuteText.toIntOrNull()

    val startHourValid = parsedStartHour != null && parsedStartHour in 1..12
    val startMinuteValid = parsedStartMinute != null && parsedStartMinute in 0..59
    val endHourValid = parsedEndHour != null && parsedEndHour in 1..12
    val endMinuteValid = parsedEndMinute != null && parsedEndMinute in 0..59
    val timesValid = startHourValid && startMinuteValid && endHourValid && endMinuteValid

    val isValid = timesValid && isEndAfterStart(
        startHour = parsedStartHour!!,
        startMinute = parsedStartMinute!!,
        startIsPm = startIsPm,
        endHour = parsedEndHour!!,
        endMinute = parsedEndMinute!!,
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

                TimeInputRow(
                    label = "Start",
                    hourText = startHourText,
                    minuteText = startMinuteText,
                    isPm = startIsPm,
                    hourValid = startHourValid || startHourText.isEmpty(),
                    minuteValid = startMinuteValid || startMinuteText.isEmpty(),
                    onHourChange = { startHourText = it.filter(Char::isDigit).take(2) },
                    onMinuteChange = { startMinuteText = it.filter(Char::isDigit).take(2) },
                    onPeriodChange = { startIsPm = it },
                )

                TimeInputRow(
                    label = "End",
                    hourText = endHourText,
                    minuteText = endMinuteText,
                    isPm = endIsPm,
                    hourValid = endHourValid || endHourText.isEmpty(),
                    minuteValid = endMinuteValid || endMinuteText.isEmpty(),
                    onHourChange = { endHourText = it.filter(Char::isDigit).take(2) },
                    onMinuteChange = { endMinuteText = it.filter(Char::isDigit).take(2) },
                    onPeriodChange = { endIsPm = it },
                )

                if (!timesValid) {
                    Text(
                        text = "Enter hour (1–12) and minute (0–59)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else if (!isValid) {
                    Text(
                        text = "End time must be after start time",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text(
                        text = "Speaks once at each interval. The app stays idle between alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        frequencyMinutes,
                        to24Hour(parsedStartHour!!, startIsPm),
                        parsedStartMinute!!,
                        to24Hour(parsedEndHour!!, endIsPm),
                        parsedEndMinute!!,
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
private fun TimeInputRow(
    label: String,
    hourText: String,
    minuteText: String,
    isPm: Boolean,
    hourValid: Boolean,
    minuteValid: Boolean,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
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
        ) {
            OutlinedTextField(
                value = hourText,
                onValueChange = onHourChange,
                label = { Text("Hour") },
                isError = !hourValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            OutlinedTextField(
                value = minuteText,
                onValueChange = onMinuteChange,
                label = { Text("Min") },
                isError = !minuteValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
