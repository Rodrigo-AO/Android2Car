package com.android2car.car

import androidx.annotation.StringRes
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.MessageInfo
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android2car.R
import com.android2car.domain.model.ProjectionState
import com.android2car.domain.model.isActive
import com.android2car.domain.repository.ProjectionController
import kotlinx.coroutines.launch

/**
 * NavigationTemplate is the only template that grants a full-screen drawing surface on Android
 * Auto, so the mirrored phone screen is rendered "as a map" behind a minimal action strip.
 */
class ProjectionCarScreen(
    carContext: CarContext,
    private val projection: ProjectionController,
) : Screen(carContext) {

    init {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                projection.state.collect { invalidate() }
            }
        }
    }

    override fun onGetTemplate(): Template {
        val state = projection.state.value
        val template = NavigationTemplate.Builder().setActionStrip(actionStripFor(state))
        messageFor(state)?.let { messageRes ->
            template.setNavigationInfo(MessageInfo.Builder(carContext.getString(messageRes)).build())
        }
        return template.build()
    }

    private fun actionStripFor(state: ProjectionState): ActionStrip =
        ActionStrip.Builder()
            .addAction(if (state.isActive) stopAction() else exitAction())
            .build()

    private fun stopAction(): Action =
        Action.Builder()
            .setTitle(carContext.getString(R.string.car_action_stop))
            .setOnClickListener { projection.stop() }
            .build()

    private fun exitAction(): Action =
        Action.Builder()
            .setTitle(carContext.getString(R.string.car_action_exit))
            .setOnClickListener { carContext.finishCarApp() }
            .build()

    @StringRes
    private fun messageFor(state: ProjectionState): Int? = when (state) {
        ProjectionState.Projecting -> null
        ProjectionState.Idle -> R.string.car_message_idle
        ProjectionState.Starting, ProjectionState.WaitingForCarDisplay -> R.string.car_message_starting
        is ProjectionState.Failed -> R.string.car_message_failed
    }
}
