package com.android2car.service

import com.android2car.domain.model.ProjectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Single source of truth for the projection lifecycle, written only by [ScreenProjectionService]. */
class ProjectionStateStore {
    private val current = MutableStateFlow<ProjectionState>(ProjectionState.Idle)

    val state: StateFlow<ProjectionState> = current.asStateFlow()

    fun update(state: ProjectionState) {
        current.value = state
    }
}
