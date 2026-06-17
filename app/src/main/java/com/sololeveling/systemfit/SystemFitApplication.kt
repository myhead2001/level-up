package com.sololeveling.systemfit

import android.app.Application
import com.sololeveling.systemfit.presentation.utils.SoundManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SystemFitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SoundManager.init(this)
    }
}
