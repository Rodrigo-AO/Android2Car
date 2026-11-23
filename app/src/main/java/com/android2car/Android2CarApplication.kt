package com.android2car

import android.app.Application
import android.content.Context
import com.android2car.di.AppContainer

class Android2CarApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as Android2CarApplication).container
