package com.brizzbi.sinavmotivasyon.data

import android.content.Context
import android.content.SharedPreferences

class StorageManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("motivasyon_prefs", Context.MODE_PRIVATE)

    fun getSavedSozler(): Set<String> {
        return prefs.getStringSet("saved_sozler", emptySet())?.toSet() ?: emptySet()
    }

    fun toggleSoz(soz: String): Set<String> {
        val currentSet = getSavedSozler().toMutableSet()

        if (currentSet.contains(soz)) {
            currentSet.remove(soz)
        } else {
            currentSet.add(soz)
        }

        prefs.edit().putStringSet("saved_sozler", currentSet).apply()
        return currentSet.toSet()
    }
}