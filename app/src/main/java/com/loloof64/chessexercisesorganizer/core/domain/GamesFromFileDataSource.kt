package com.loloof64.chessexercisesorganizer.core.domain

import android.content.Context

sealed class FileData(open val caption: String, open val path: String)
data class AssetFileData(override val caption: String, val assetPath: String) :
    FileData(caption = caption, path = assetPath)

class GamesFromFileDataSource(private val gamesFromFileRepository: GamesFromFileRepository) {
    suspend fun extractGames(fileData: FileData, context: Context) {
        gamesFromFileRepository.extractGames(fileData = fileData, context = context)
    }

    fun currentGames() = gamesFromFileRepository.games
}