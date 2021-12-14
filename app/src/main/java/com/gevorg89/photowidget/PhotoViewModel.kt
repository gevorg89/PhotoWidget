package com.gevorg89.photowidget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class PhotoViewModel : ViewModel() {

    data class PhotoWidget(
        val id: Int,
        val file: File
    )

    sealed class State {
        object Loading : State()
        object Loaded : State()
    }

    private val _photos: MutableStateFlow<MutableList<PhotoWidget>> =
        MutableStateFlow(mutableListOf())
    val photos: StateFlow<List<PhotoWidget>> = _photos.asStateFlow()

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    var selectedWidgetId = -1
        private set

    fun setSelectedWidgetId(widgetId: Int) {
        this.selectedWidgetId = widgetId
    }

    fun addPhoto(file: File) {
        val temp = _photos.value.filter { it.id != selectedWidgetId }.toMutableList()
        _photos.value.clear()
        _photos.value = temp.also {
            it.add(PhotoWidget(id = selectedWidgetId, file = file))
        }.sortedBy { it.id }.toMutableList()
    }

    fun setFiles(files: List<PhotoWidget>) {
        viewModelScope.launch {
            _photos.value.clear()
            _photos.value = files.sortedBy { it.id }.toMutableList()
            delay(200)
            _state.value = State.Loaded
        }
    }

}