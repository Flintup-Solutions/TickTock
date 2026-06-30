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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import java.util.Locale

private val FREQUENCY_OPTIONS = listOf(10, 20, 30, 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (frequencyMinutes: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit,
) {
    var frequencyMinutes by remember { mutableIntStateOf(10) }
    var frequencyExpanded by remember { mutableStateOf(false) }

    var startHour by remember { mutableIntStateOf(6) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(8) }
    var endMinute by remember { mutableIntStateOf(0) }

    val isValid = (endHour * 60 + endMinute) > (startHour * 60 + startMinute)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Time Alert") },
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
                            .menuAnchor()
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
                    onHourChange = { startHour = it },
                    onMinuteChange = { startMinute = it },
                )

                TimePickerRow(
                    label = "End",
                    hour = endHour,
                    minute = endMinute,
                    onHourChange = { endHour = it },
                    onMinuteChange = { endMinute = it },
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
                    onConfirm(frequencyMinutes, startHour, startMinute, endHour, endMinute)
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
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberStepper(
                label = "Hour",
                value = hour,
                range = 0..23,
                onValueChange = onHourChange,
                modifier = Modifier.weight(1f),
            )
            NumberStepper(
                label = "Min",
                value = minute,
                range = 0..59,
                step = 10,
                onValueChange = onMinuteChange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NumberStepper(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 1,
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
        ) {
            TextButton(
                onClick = {
                    val newValue = value - step
                    if (newValue in range) onValueChange(newValue)
                },
                enabled = value - step >= range.first,
            ) {
                Text("−")
            }
            Text(
                text = String.format(Locale.getDefault(), "%02d", value),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
            TextButton(
                onClick = {
                    val newValue = value + step
                    if (newValue in range) onValueChange(newValue)
                },
                enabled = value + step <= range.last,
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
