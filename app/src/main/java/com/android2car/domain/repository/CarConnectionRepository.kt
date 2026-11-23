package com.android2car.domain.repository

import com.android2car.domain.model.CarConnectionStatus
import kotlinx.coroutines.flow.Flow

interface CarConnectionRepository {
    val status: Flow<CarConnectionStatus>
}
