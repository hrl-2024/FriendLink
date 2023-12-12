package com.skr.android.friendlink

import android.content.Context
import android.util.Log
import java.util.Calendar

object DailyMessageBoolean {
    private const val PREF_NAME = "DailyMessageBoolean"
    private const val KEY_BOOLEAN_USED = "isBooleanUsed"
    private const val KEY_LAST_USED_DATE = "lastUsedDate"
    private const val TAG = "DailyMessageBoolean"

    fun isBooleanAvailable(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isBooleanUsed = sharedPreferences.getBoolean(KEY_BOOLEAN_USED, false)
        val lastUsedDate = sharedPreferences.getLong(KEY_LAST_USED_DATE, 0)

        val currentDate = Calendar.getInstance().timeInMillis
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val todayInMillis = today.timeInMillis

        Log.d(TAG, "Current date: $currentDate")
        Log.d(TAG, "Today in millis: $todayInMillis")
        Log.d(TAG, "Last used date: $lastUsedDate")

        return if (!isBooleanUsed && lastUsedDate < todayInMillis) {
            true // The boolean is available for use today
        } else {
            false // The boolean has already been used today or is not available
        }
    }

    fun useBoolean(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_BOOLEAN_USED, true).apply()
        val currentDate = Calendar.getInstance().timeInMillis
        sharedPreferences.edit().putLong(KEY_LAST_USED_DATE, currentDate).apply()
    }

    fun resetBoolean(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfNextDay = calendar.timeInMillis

        sharedPreferences.edit().putLong(KEY_LAST_USED_DATE, startOfNextDay).apply()
        sharedPreferences.edit().putBoolean(KEY_BOOLEAN_USED, false).apply()
    }
}
