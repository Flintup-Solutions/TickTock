package com.ticktock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ticktock.data.ProfileRepository
import com.ticktock.data.TimeAlertProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val profiles: List<TimeAlertProfile> = emptyList(),
    val showAddDialog: Boolean = false,
)

class MainViewModel(
    private val repository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            val profiles = repository.getAllProfiles()
            _uiState.update { it.copy(profiles = profiles) }
        }
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
}

class MainViewModelFactory(
    private val repository: ProfileRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
