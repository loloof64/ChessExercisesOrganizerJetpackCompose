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
}