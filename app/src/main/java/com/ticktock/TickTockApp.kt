package com.ticktock

import android.app.Application
import com.ticktock.data.ProfileRepository

class TickTockApp : Application() {
    lateinit var repository: ProfileRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ProfileRepository.create(this)
    }
}
