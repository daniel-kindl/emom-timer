package com.emomtimer.domain.repository

import com.emomtimer.domain.model.TabataPreset
import kotlinx.coroutines.flow.Flow

interface TabataPresetRepository {
    fun getPresets(): Flow<List<TabataPreset>>
    suspend fun savePreset(preset: TabataPreset)
    suspend fun deletePreset(id: String)
}
