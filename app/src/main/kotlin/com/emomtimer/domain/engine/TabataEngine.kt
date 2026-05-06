package com.emomtimer.domain.engine

import com.emomtimer.domain.model.TabataConfig
import com.emomtimer.domain.model.TabataEvent
import kotlinx.coroutines.flow.SharedFlow

interface TabataEngine {
    val events: SharedFlow<TabataEvent>
    fun start(config: TabataConfig)
    fun pause()
    fun resume()
    fun stop()
}
