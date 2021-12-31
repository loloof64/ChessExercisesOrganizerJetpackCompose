package com.loloof64.chessexercisesorganizer.core.domain

import android.content.Context
import com.loloof64.chessexercisesorganizer.core.PgnGameLoader
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import com.loloof64.chessexercisesorganizer.ui.components.moves_navigator.GamesLoadingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GamesFromFileRepository(private val gamesExtractor: FileGamesExtractor) {

    var games: List<PGNGame>? = null

    suspend fun extractGames(fileData: FileData, context: Context) {
        withContext(Dispatchers.IO) {
            games = try {
                val newGamesList =
                    gamesExtractor.extractGames(fileData = fileData, context = context)
                newGamesList.ifEmpty { null }
            } catch (ex: Exception) {
                null
            }
        }
    }
}

class FileGamesExtractor {
    fun extractGames(fileData: FileData, context: Context): List<PGNGame> {
        return when (fileData) {
            is AssetFileData -> extractAssetGames(fileData, context)
        }
    }

    private fun extractAssetGames(fileData: AssetFileData, context: Context): List<PGNGame> {
        val inputStream = context.assets.open(fileData.assetPath)
        val gamesFileContent = inputStream.bufferedReader().use { it.readText() }
        return PgnGameLoader().load(gamesFileContent = gamesFileContent)
    }
}