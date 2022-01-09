package com.loloof64.chessexercisesorganizer.core.domain

import android.content.Context
import com.loloof64.chessexercisesorganizer.core.PgnGameLoader
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

sealed class FileData(open val caption: String, open val path: String)
data class AssetFileData(override val caption: String, val assetRelativePath: String) :
    FileData(caption = caption, path = assetRelativePath)
data class InternalFileData(override val caption: String, val localRelativePath: String) :
    FileData(caption = caption, path = localRelativePath)
data class InternalFolderData(override val caption: String, val localRelativePath: String):
    FileData(caption = caption, path = localRelativePath)

class GamesFromFileUseCase {
    private val gamesExtractor = FileGamesExtractor()

    var games: MutableList<PGNGame>? = null
        private set

    suspend fun extractGames(fileData: FileData, context: Context) {
        withContext(Dispatchers.IO) {
            games = try {
                val newGamesList =
                    gamesExtractor.extractGames(fileData = fileData, context = context).toMutableList()
                newGamesList.ifEmpty { null }
            } catch (ex: Exception) {
                null
            }
        }
    }

    fun insertGameAt(index: Int, game: PGNGame) {
        if (games == null) {
            games = mutableListOf(game)
        }
        else {
            games?.add(index, game)
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