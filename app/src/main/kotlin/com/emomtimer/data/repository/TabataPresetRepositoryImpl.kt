package com.emomtimer.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emomtimer.domain.model.TabataPreset
import com.emomtimer.domain.repository.TabataPresetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class TabataPresetRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : TabataPresetRepository {

    override fun getPresets(): Flow<List<TabataPreset>> =
        dataStore.data.map { prefs ->
            parsePresets(prefs[KEY_TABATA_PRESETS] ?: return@map emptyList())
        }

    override suspend fun savePreset(preset: TabataPreset) {
        dataStore.edit { prefs ->
            val current = parsePresets(prefs[KEY_TABATA_PRESETS] ?: "[]").toMutableList()
            current.removeAll { it.id == preset.id }
            current.add(preset)
            prefs[KEY_TABATA_PRESETS] = serializePresets(current)
        }
    }

    override suspend fun deletePreset(id: String) {
        dataStore.edit { prefs ->
            val current = parsePresets(prefs[KEY_TABATA_PRESETS] ?: "[]").toMutableList()
            current.removeAll { it.id == id }
            prefs[KEY_TABATA_PRESETS] = serializePresets(current)
        }
    }

    private fun parsePresets(json: String): List<TabataPreset> = runCatching {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            TabataPreset(
                id = obj.getString("id"),
                name = obj.getString("name"),
                totalMinutes = obj.getInt("totalMinutes"),
                totalSeconds = obj.getInt("totalSeconds"),
                workMinutes = obj.getInt("workMinutes"),
                workSeconds = obj.getInt("workSeconds"),
                restMinutes = obj.getInt("restMinutes"),
                restSeconds = obj.getInt("restSeconds"),
            )
        }
    }.getOrDefault(emptyList())

    private fun serializePresets(presets: List<TabataPreset>): String {
        val array = JSONArray()
        presets.forEach { preset ->
            array.put(
                JSONObject().apply {
                    put("id", preset.id)
                    put("name", preset.name)
                    put("totalMinutes", preset.totalMinutes)
                    put("totalSeconds", preset.totalSeconds)
                    put("workMinutes", preset.workMinutes)
                    put("workSeconds", preset.workSeconds)
                    put("restMinutes", preset.restMinutes)
                    put("restSeconds", preset.restSeconds)
                }
            )
        }
        return array.toString()
    }

    private companion object {
        val KEY_TABATA_PRESETS = stringPreferencesKey("tabata_presets")
    }
}
