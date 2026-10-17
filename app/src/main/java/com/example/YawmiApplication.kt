package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.YawmiRepository
import com.example.notifications.NotificationHelper
import com.example.notifications.SessionAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class YawmiApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: YawmiRepository
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var alarmScheduler: SessionAlarmScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = YawmiRepository(database)
        preferencesRepository = UserPreferencesRepository(this)
        alarmScheduler = SessionAlarmScheduler(this)

        NotificationHelper.createNotificationChannel(this)

        CoroutineScope(Dispatchers.IO).launch {
            alarmScheduler.rescheduleAllAlarms()
        }
    }
}
