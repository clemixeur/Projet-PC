package com.pctracker

import android.app.Application
import com.pctracker.data.db.AppDatabase
import com.pctracker.data.repository.PriceRepository
import com.pctracker.work.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PCTrackerApp : Application() {

    lateinit var repository: PriceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = PriceRepository.getInstance(db)

        CoroutineScope(Dispatchers.IO).launch {
            repository.ensureSeeded()
            val settings = repository.appSettings.first()
            WorkScheduler.schedule(this@PCTrackerApp, settings?.scrapeIntervalHours ?: 3)
        }
    }
}
