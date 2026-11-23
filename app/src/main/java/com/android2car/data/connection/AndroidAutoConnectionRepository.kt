package com.android2car.data.connection

import android.content.Context
import androidx.car.app.connection.CarConnection
import androidx.lifecycle.Observer
import com.android2car.domain.model.CarConnectionStatus
import com.android2car.domain.repository.CarConnectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

class AndroidAutoConnectionRepository(context: Context) : CarConnectionRepository {

    private val connectionType = CarConnection(context.applicationContext).type

    override val status: Flow<CarConnectionStatus> = callbackFlow {
        val observer = Observer<Int> { type -> trySend(type.toCarConnectionStatus()) }
        connectionType.observeForever(observer)
        awaitClose { connectionType.removeObserver(observer) }
    }
        // LiveData observers must be registered and removed on the main thread.
        .flowOn(Dispatchers.Main.immediate)
        .distinctUntilChanged()
}

internal fun Int.toCarConnectionStatus(): CarConnectionStatus = when (this) {
    CarConnection.CONNECTION_TYPE_PROJECTION -> CarConnectionStatus.ANDROID_AUTO
    CarConnection.CONNECTION_TYPE_NATIVE -> CarConnectionStatus.AUTOMOTIVE_OS
    else -> CarConnectionStatus.DISCONNECTED
}
