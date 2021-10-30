// Based on source code from [Chess Engine Support archived repository](https://code.google.com/archive/p/chessenginesupport-androidlib/).

package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.Context
import android.net.Uri
import android.util.Log
import  com.kalab.chess.enginesupport.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

private var currentRunner: ChessEngineRunner? = null

fun getAvailableEngines(context: Context): List<String> = getEngines(context).map { it.name }

fun listInstalledEngines(enginesFolder: File): Array<String> {
    enginesFolder.mkdir()
    val files = enginesFolder.listFiles()?.filter { it.isFile }?.toTypedArray() ?: arrayOf<File>()
    files.sortBy { it.name }
    return files.map { file ->
        val regex = Regex("lib(.*)\\.so")
        regex.find(file.name)?.groups?.get(1)?.value ?: file.name
    }.toTypedArray()
}

fun installEngine(context: Context, index: Int) {
    val anInstalledEngineIsRunning = currentRunner?.isRunning?.get() == true
    if (anInstalledEngineIsRunning) {
        throw IllegalStateException("Cannot install an engine while an engine is running !")
    }

    val availableEngines = getEngines(context)
    if (index < availableEngines.size) {
        val parentFolder = File(context.filesDir, "engines")
        parentFolder.mkdir()

        val requestedEngine = availableEngines[index]
        val path = requestedEngine.enginePath
        val output = File(
            parentFolder,
            path
        )
        val targetFilePath = output.absolutePath
        val istream = context.contentResolver.openInputStream(Uri.fromFile(output))
        val fout = FileOutputStream(targetFilePath)

        // Copying file
        val b = ByteArray(1024)
        var numBytes = istream?.read(b)
        while (numBytes != null && numBytes != -1) {
            fout.write(b, 0, numBytes)
            numBytes = istream?.read(b)
        }
        istream?.close()
        fout.close()

        // Setting permission
        val cmd = arrayOf("chmod", "744", targetFilePath)
        val process = Runtime.getRuntime().exec(cmd)
        try {
            process.waitFor()
        } catch (ex: InterruptedException) {
            Log.e("ChessEngineUtils", ex.localizedMessage, ex)
        }
    } else {
        throw ArrayIndexOutOfBoundsException("Requested index : ${index}, size: ${availableEngines.size}")
    }
}

fun deleteInstalledEngine(enginesFolder: File, index: Int) {
    val anInstalledEngineIsRunning = currentRunner?.isRunning?.get() == true
    if (anInstalledEngineIsRunning) {
        throw IllegalStateException("Cannot delete an installed engine while an engine is running !")
    }

    enginesFolder.mkdir()
    val installedEngines = listInstalledEngines(enginesFolder)
    if (index < installedEngines.size) {
        val selectedFileName = "lib${installedEngines[index]}.so"
        val selectedFile = File(enginesFolder, selectedFileName)
        selectedFile.delete()
    } else {
        throw ArrayIndexOutOfBoundsException("Requested index : ${index}, size: ${installedEngines.size}")
    }
}

fun executeInstalledEngine(enginesFolder: File, index: Int, errorCallback: (Error) -> Unit) {
    enginesFolder.mkdir()
    val files = enginesFolder.listFiles()?.filter { it.isFile }?.toTypedArray() ?: arrayOf<File>()
    files.sortBy { it.name }
    if (index >= files.size) throw ArrayIndexOutOfBoundsException("Requested index : ${index}, size: ${files.size}")

    val selectedFile = files[index]
    if (currentRunner != null) currentRunner?.stop()
    currentRunner = ChessEngineRunner(selectedFile, errorCallback)
    currentRunner!!.run()
}

fun sendCommandToRunningEngine(command: String) {
    currentRunner?.sendCommand(command)
}

fun readNextEngineOutput() : String? {
    return currentRunner?.readNextOutput()
}

fun stopCurrentRunningEngine() {
    currentRunner?.stop()
}


private fun getEngines(context: Context): List<ChessEngine> {
    val resolver = ChessEngineResolver(context)
    val engines =  resolver.resolveEngines()

    return Collections.unmodifiableList(engines)
}