package com.android2car.car

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.validation.HostValidator
import com.android2car.appContainer

class Android2CarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator =
        if (isDebuggable()) {
            // The Desktop Head Unit and sideloaded hosts are not in the production allowlist.
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            // The library's allowlist holds the signing certificates of the official Android Auto
            // and Automotive OS hosts; it is marked private only to discourage editing it.
            @SuppressLint("PrivateResource")
            HostValidator.Builder(applicationContext)
                .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                .build()
        }

    override fun onCreateSession(sessionInfo: SessionInfo): Session = ProjectionCarSession(appContainer)

    private fun isDebuggable(): Boolean = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}
