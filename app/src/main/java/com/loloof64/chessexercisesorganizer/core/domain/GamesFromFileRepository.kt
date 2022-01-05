package com.loloof64.chessexercisesorganizer.core.domain

import android.content.Context
import com.loloof64.chessexercisesorganizer.core.PgnGameLoader
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

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
            is AssetFileData -> extractAssetGame(fileData, context)
            is InternalFileData -> extractInternalGame(fileData, context)
            is InternalFolderData -> throw IllegalArgumentException("Folder cannot be treated as a pgn file : $fileData !")
        }
    }

    private fun extractAssetGame(fileData: AssetFileData, context: Context): List<PGNGame> {
        val inputStream = context.assets.open(fileData.assetRelativePath)
        return getGamesFromInputStream(inputStream)
    }

    private fun extractInternalGame(fileData: InternalFileData, context: Context): List<PGNGame> {
        val inputStream = context.openFileInput(fileData.localRelativePath)
        return getGamesFromInputStream(inputStream)
    }

    private fun getGamesFromInputStream(inputStream: InputStream): List<PGNGame> {
        val gamesFileContent = inputStream.bufferedReader().use { it.readText() }
        return PgnGameLoader().load(gamesFileContent = gamesFileContent)
    }
}