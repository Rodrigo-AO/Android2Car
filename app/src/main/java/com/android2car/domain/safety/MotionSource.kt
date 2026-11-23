package com.android2car.domain.safety

import com.android2car.domain.model.VehicleMotion
import kotlinx.coroutines.flow.Flow

interface MotionSource {
    val motion: Flow<VehicleMotion>
}
