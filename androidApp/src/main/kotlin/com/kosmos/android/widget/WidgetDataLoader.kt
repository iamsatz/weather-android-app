package com.kosmos.android.widget

import android.content.Context
import com.kosmos.android.data.WeatherRepository
import com.kosmos.android.data.WeatherUiState
import com.kosmos.android.model.WeatherSnapshot

object WidgetDataLoader {

    suspend fun loadSnapshot(context: Context): WeatherSnapshot? {
        val repo = WeatherRepository(context)
        runCatching { repo.refresh(hasLocationPermission = false) }
        val live = (repo.state.value as? WeatherUiState.Ready)?.snapshot
        if (live != null) return live
        if (repo.seedFromCache()) {
            return (repo.state.value as? WeatherUiState.Ready)?.snapshot
        }
        return null
    }
}
