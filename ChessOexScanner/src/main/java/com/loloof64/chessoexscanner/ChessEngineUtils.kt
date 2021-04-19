package com.loloof64.chessoexscanner

import android.content.Context
import android.content.SharedPreferences
import com.kalab.chess.enginesupport.ChessEngineResolver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

private const val enginesSubfolderName = "engines"
private const val chessEngineUtilsKey = "CHESS_ENGINE_UTILS"

class ChessEngineUtils(private val context: Context, appId: String) {

    var currentRunner: ChessEngineRunner? = null

    private val enginesSharedPreferences: SharedPreferences? =
        context.applicationContext.getSharedPreferences(
            "$appId.$chessEngineUtilsKey",
            Context.MODE_PRIVATE
        )

    fun getMyStoreEnginesNames(): Array<String> {
        val resolver = ChessEngineResolver(context)
        val engines = resolver.resolveEngines()
        return engines?.map { it.name }?.toTypedArray() ?: arrayOf()
    }

    fun installEngineFromMyStore(index: Int) {
        val resolver = ChessEngineResolver(context)
        val engines = resolver.resolveEngines()
        if (index >= engines.size) throw ArrayIndexOutOfBoundsException("Requested index : ${index}, size: ${engines.size}")

        val selectedEngine = engines[index]
        val engineFileName = selectedEngine.fileName
        val enginePackageName = selectedEngine.packageName
        val engineVersionCode = selectedEngine.versionCode

        val enginesFolder = File(context.applicationContext.filesDir, enginesSubfolderName)

        enginesFolder.mkdir()

        selectedEngine.copyToFiles(
            context.applicationContext.contentResolver,
            enginesFolder
        )

        with(enginesSharedPreferences?.edit()) {
            val engineAndPackageKey = "$engineFileName|$enginePackageName"
            val engineVersionValue = "$engineVersionCode"
            this?.putString(engineAndPackageKey, engineVersionValue)
            this?.apply()
        }
    }

    fun newVersionAvailableFromMyStoreFor(index: Int) : Boolean {
        val resolver = ChessEngineResolver(context)
        val engines = resolver.resolveEngines()
        if (index >= engines.size) throw ArrayIndexOutOfBoundsException("Requested index : ${index}, size: ${engines.size}")

        val selectedEngine = engines[index]
        val engineName = selectedEngine.fileName
        val enginePackage = selectedEngine.packageName
        val selectedEngineIdInPreferences = "$engineName|$enginePackage"
        val currentVersionCodeStr = enginesSharedPreferences?.getString(selectedEngineIdInPreferences, "err")
        val currentVersionCode = try {
            Integer.parseInt(currentVersionCodeStr ?: "err")
        }
        catch (ex: NumberFormatException) {
            throw IllegalStateException("The requested engine has not been copied yet in local files.")
        }

        val engineFolder = File(context.applicationContext.filesDir, enginesSubfolderName)

        engineFolder.mkdir()

        val engineLastVersionCode = resolver.ensureEngineVersion(
            engineName,
            enginePackage,
            currentVersionCode,
            engineFolder
        )

        return currentVersionCode < engineLastVersionCode
    }

    fun listInstalledEngines() : Array<String> {
        val enginesFolder = File(context.applicationContext.filesDir, enginesSubfolderName)
        enginesFolder.mkdir()
        val files = enginesFolder.listFiles()?.filter { it.isFile }?.toTypedArray() ?: arrayOf<File>()
        files.sortBy{ it.name }
        return files.map { it.name }.toTypedArray()
    }

    fun executeInstalledEngine(index: Int, errorCallback: (Error) -> Unit) {
        val enginesFolder = File(context.applicationContext.filesDir, enginesSubfolderName)
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

    fun readCurrentEnginePendingOutputs(): Array<String> {
        return currentRunner?.readPendingOutputs() ?: arrayOf()
    }

    fun stopCurrentRunningEngine() {
        currentRunner?.stop()
    }

    // Returns true if the engine could be deleted, false otherwise.
    fun deleteInstalledEngine(index: Int) {
        val anInstalledEngineIsRunning = currentRunner?.isRunning?.get() == true
        if (anInstalledEngineIsRunning) {
            throw IllegalStateException("Cannot delete an installed engine while an engine is running !")
        }

        val enginesFolder = File(context.applicationContext.filesDir, enginesSubfolderName)
        enginesFolder.mkdir()
        val files = enginesFolder.listFiles()?.filter { it.isFile }?.toTypedArray() ?: arrayOf<File>()
        files.sortBy {it.name}
        if (index >= files.size) throw ArrayIndexOutOfBoundsException("Requested index : ${index}, size: ${files.size}")

        val selectedFile = files[index]
        selectedFile.delete()

        // TODO update shared preferences
    }
}