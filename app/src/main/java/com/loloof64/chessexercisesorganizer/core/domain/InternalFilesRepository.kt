package com.loloof64.chessexercisesorganizer.core.domain

import android.content.Context
import com.loloof64.chessexercisesorganizer.utils.stripPgnExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class InternalFilesRepository {

    suspend fun getInternalGamesList(folder: File, context: Context): List<FileData> {
        return withContext(Dispatchers.IO) {
            val internalRootFolderPath = context.filesDir.absolutePath
            val isInInternalFolder = folder.absolutePath.startsWith(internalRootFolderPath)
            if (!isInInternalFolder) throw IllegalArgumentException("Given folder is not inside project internal files ($folder) !")

            val items = folder.listFiles { file ->
                if (file.isDirectory) {
                    true
                } else {
                    file.extension == "pgn"
                }
            }

            items?.map {
                val localRelativePath = it.absolutePath.removePrefix(internalRootFolderPath)
                if (it.isDirectory) {
                    InternalFolderData(caption = it.name, localRelativePath = localRelativePath)
                } else {
                    InternalFileData(caption = it.name, localRelativePath = localRelativePath)
                }
            } ?: listOf()
        }
    }

    suspend fun createNewFile(name: String, hostFolder: File, context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (name.isEmpty()) return@withContext false

                val internalRootFolderPath = context.filesDir.absolutePath
                val isInInternalFolder = hostFolder.absolutePath.startsWith(internalRootFolderPath)
                if (!isInInternalFolder) throw IllegalArgumentException("Given folder is not inside project internal files ($hostFolder) !")

                val newFile = File(hostFolder, "${name.stripPgnExtension()}.pgn")
                newFile.createNewFile()
                return@withContext true
            } catch (ex: IOException) {
                println(ex)
                return@withContext false
            }
        }
    }

}