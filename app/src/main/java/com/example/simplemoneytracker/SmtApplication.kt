package com.example.simplemoneytracker

import android.app.Application
import com.example.simplemoneytracker.ui.data.AppContainer
import com.example.simplemoneytracker.ui.data.AppDataContainer

class SmtApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}