package com.emomtimer.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emomtimer.domain.model.Preset
import com.emomtimer.domain.repository.PresetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class PresetRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PresetRepository {

    override fun getPresets(): Flow<List<Preset>> =
        dataStore.data.map { prefs ->
            parsePresets(prefs[KEY_PRESETS] ?: return@map emptyList())
        }

    override suspend fun savePreset(preset: Preset) {
        dataStore.edit { prefs ->
            val current = parsePresets(prefs[KEY_PRESETS] ?: "[]").toMutableList()
            current.removeAll { it.id == preset.id }
            current.add(preset)
            prefs[KEY_PRESETS] = serializePresets(current)
        }
    }

    override suspend fun deletePreset(id: String) {
        dataStore.edit { prefs ->
            val current = parsePresets(prefs[KEY_PRESETS] ?: "[]").toMutableList()
            current.removeAll { it.id == id }
            prefs[KEY_PRESETS] = serializePresets(current)
        }
    }

    private fun parsePresets(json: String): List<Preset> = runCatching {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Preset(
                id = obj.getString("id"),
                name = obj.getString("name"),
                totalMinutes = obj.getInt("totalMinutes"),
                totalSeconds = obj.getInt("totalSeconds"),
                intervalMinutes = obj.getInt("intervalMinutes"),
                intervalSeconds = obj.getInt("intervalSeconds"),
            )
        }
    }.getOrDefault(emptyList())

    private fun serializePresets(presets: List<Preset>): String {
        val array = JSONArray()
        presets.forEach { preset ->
            array.put(
                JSONObject().apply {
                    put("id", preset.id)
                    put("name", preset.name)
                    put("totalMinutes", preset.totalMinutes)
                    put("totalSeconds", preset.totalSeconds)
                    put("intervalMinutes", preset.intervalMinutes)
                    put("intervalSeconds", preset.intervalSeconds)
                }
            )
        }
        return array.toString()
    }

    private companion object {
        val KEY_PRESETS = stringPreferencesKey("presets")
    }
}
