package com.android2car.domain.model

sealed interface ProjectionState {
    data object Idle : ProjectionState
    data object Starting : ProjectionState
    data object WaitingForCarDisplay : ProjectionState
    data object Projecting : ProjectionState
    data class Failed(val error: ProjectionError) : ProjectionState
}

val ProjectionState.isActive: Boolean
    get() = this is ProjectionState.Starting ||
        this is ProjectionState.WaitingForCarDisplay ||
        this is ProjectionState.Projecting

enum class ProjectionError {
    CAPTURE_UNAVAILABLE,
    MIRROR_FAILED,
}
