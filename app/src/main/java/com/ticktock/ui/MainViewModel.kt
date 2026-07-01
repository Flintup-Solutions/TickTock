package com.ticktock.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ticktock.data.ProfileRepository
import com.ticktock.data.TimeAlertProfile
import com.ticktock.permissions.PermissionHelper
import com.ticktock.scheduler.SlotCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MainUiState(
    val profiles: List<TimeAlertProfile> = emptyList(),
    val showAddDialog: Boolean = false,
    val showAlarmPermissionDialog: Boolean = false,
    val needsExactAlarmPermission: Boolean = false,
    val needsBatteryOptimizationDisabled: Boolean = false,
    val needsNotificationPermission: Boolean = false,
)

class MainViewModel(
    private val repository: ProfileRepository,
    private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
        refreshPermissionState()
        if (!PermissionHelper.canScheduleExactAlarms(appContext)) {
            _uiState.update { it.copy(showAlarmPermissionDialog = true) }
        }
    }

    fun loadProfiles() {
        viewModelScope.launch {
            val profiles = repository.getAllProfiles()
            _uiState.update { it.copy(profiles = profiles) }
        }
    }

    fun refreshPermissionState() {
        _uiState.update {
            it.copy(
                needsExactAlarmPermission = !PermissionHelper.canScheduleExactAlarms(appContext),
                needsBatteryOptimizationDisabled = !PermissionHelper.isIgnoringBatteryOptimizations(appContext),
                needsNotificationPermission = !PermissionHelper.hasNotificationPermission(appContext),
            )
        }
    }

    fun onAppResumed() {
        viewModelScope.launch {
            refreshPermissionState()
            if (PermissionHelper.canScheduleExactAlarms(appContext)) {
                repository.rescheduleAll()
                loadProfiles()
            }
        }
    }

    fun dismissAlarmPermissionDialog() {
        _uiState.update { it.copy(showAlarmPermissionDialog = false) }
    }

    fun openExactAlarmSettings() {
        PermissionHelper.openExactAlarmSettings(appContext)
    }

    fun openBatteryOptimizationSettings() {
        PermissionHelper.openBatteryOptimizationSettings(appContext)
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun addProfile(
        frequencyMinutes: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
    ) {
        viewModelScope.launch {
            if (!PermissionHelper.canScheduleExactAlarms(appContext)) {
                _uiState.update { it.copy(showAlarmPermissionDialog = true) }
                return@launch
            }

            repository.addProfile(
                TimeAlertProfile(
                    frequencyMinutes = frequencyMinutes,
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                ),
            )
            hideAddDialog()
            loadProfiles()
        }
    }

    fun deleteProfile(profile: TimeAlertProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            loadProfiles()
        }
    }

    fun formatNextAnnouncement(profile: TimeAlertProfile): String? {
        val nextSlot = SlotCalculator.nextSlot(profile) ?: return null
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        val time = nextSlot.format(formatter)
        val dayLabel = if (nextSlot.toLocalDate() == ZonedDateTime.now().toLocalDate()) {
            time
        } else {
            "Tomorrow $time"
        }
        return dayLabel
    }
}

class MainViewModelFactory(
    private val repository: ProfileRepository,
    private val appContext: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
