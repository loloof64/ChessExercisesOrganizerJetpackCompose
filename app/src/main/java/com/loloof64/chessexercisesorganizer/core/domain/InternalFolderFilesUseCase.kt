package com.loloof64.chessexercisesorganizer.core.domain

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class InternalFolderFilesUseCase(private val internalFilesRepository: InternalFilesRepository) {
    suspend fun getInternalGamesList(folder: File, context: Context): List<FileData> {
        return withContext(Dispatchers.IO) {
            internalFilesRepository.getInternalGamesList(folder, context)
        }
    }

    suspend fun createNewFile(name: String, hostFolder: File, context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext internalFilesRepository.createNewFile(name = name, hostFolder = hostFolder, context = context)
        }
    }
}