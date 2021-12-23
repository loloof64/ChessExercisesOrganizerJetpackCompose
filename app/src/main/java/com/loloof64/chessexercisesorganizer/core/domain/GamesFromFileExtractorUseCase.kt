package com.loloof64.chessexercisesorganizer.core.domain

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GamesFromFileExtractorUseCase(private val gamesDataSource: GamesFromFileDataSource) {
    suspend fun extractGames(fileData: FileData, context: Context) {
        withContext(Dispatchers.IO) {
            gamesDataSource.extractGames(fileData = fileData, context = context)
        }
    }

    fun currentGames() = gamesDataSource.currentGames()
}