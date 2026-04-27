package com.application.habittracker.ui.screen.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.habittracker.data.repository.HabitRepository
import kotlinx.coroutines.launch

class HabitFormViewModel(private val repository: HabitRepository) : ViewModel() {

    fun saveHabit(id: Long?, name: String, colorIndex: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            if (id == null) {
                repository.insertHabit(name.trim(), colorIndex)
            } else {
                repository.updateHabit(id, name.trim(), colorIndex)
            }
            onDone()
        }
    }

    fun deleteHabit(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteHabit(id)
            onDone()
        }
    }
}