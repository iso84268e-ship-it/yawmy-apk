package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "yawmi_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val WEEKLY_STUDY_TARGET = intPreferencesKey("weekly_study_target")
        val DEFAULT_REMINDER_MINS = intPreferencesKey("default_reminder_mins")
        val WAKEUP_MINUTES = intPreferencesKey("wakeup_minutes")
        val SCHOOL_DEPARTURE_MINUTES = intPreferencesKey("school_departure_minutes")
        val SCHOOL_START_MINUTES = intPreferencesKey("school_start_minutes")
        val SCHOOL_END_MINUTES = intPreferencesKey("school_end_minutes")
        val FOCUS_STUDY_MINUTES = intPreferencesKey("focus_study_minutes")
        val FOCUS_BREAK_MINUTES = intPreferencesKey("focus_break_minutes")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val weeklyStudyTarget: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.WEEKLY_STUDY_TARGET] ?: 20
    }

    val focusStudyMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.FOCUS_STUDY_MINUTES] ?: 25
    }

    val focusBreakMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.FOCUS_BREAK_MINUTES] ?: 5
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setWeeklyStudyTarget(hours: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WEEKLY_STUDY_TARGET] = hours
        }
    }

    suspend fun setFocusTimes(studyMins: Int, breakMins: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FOCUS_STUDY_MINUTES] = studyMins
            prefs[Keys.FOCUS_BREAK_MINUTES] = breakMins
        }
    }
}
