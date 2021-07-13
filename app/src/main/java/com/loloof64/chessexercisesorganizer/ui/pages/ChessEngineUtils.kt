// Based on source code from [Chess Engine Support archived repository](https://code.google.com/archive/p/chessenginesupport-androidlib/).

package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.XmlResourceParser
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class ChessEngine(
    val name: String, val fileName: String, val authority: String, val packageName: String,
    val versionCode: Int, val licenseCheckActivity: String?
) {
    fun getUri(): Uri = Uri.parse("content://$authority/$fileName")
}

private var activitiesWithLicenseCheck = mutableMapOf<String, String>()
private var currentRunner: ChessEngineRunner? = null


private const val ENGINE_PROVIDER_LICENSE_MARKER = "intent.chess.provider.ACTIVATION"
private const val ENGINE_PROVIDER_MARKER = "intent.chess.provider.ENGINE"

fun getAvailableEngines(context: Context): Flow<List<String>> = callbackFlow {
    var currentEngines: List<ChessEngine>

    val packageChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            currentEngines = getEngines(context)
            sendBlocking(currentEngines.map { it.name })
        }
    }

    val packageChangeFilter = IntentFilter().apply {
        addDataScheme("package")
        addAction(Intent.ACTION_PACKAGE_ADDED)
        addAction(Intent.ACTION_PACKAGE_CHANGED)
        addAction(Intent.ACTION_PACKAGE_REPLACED)
        addAction(Intent.ACTION_PACKAGE_REMOVED)
    }
    context.registerReceiver(packageChangeReceiver, packageChangeFilter)


    currentEngines = getEngines(context)
    send(currentEngines.map { it.name })

    awaitClose {
        context.unregisterReceiver(packageChangeReceiver)
    }
}

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
        val uri = requestedEngine.getUri()
        val output = File(
            parentFolder,
            uri.path ?: throw IllegalStateException("Could not get path from URI")
        )
        val targetFilePath = output.absolutePath
        val istream = context.contentResolver.openInputStream(uri)
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
    updateLicenseCheckActivities(context)
    val result = mutableListOf<ChessEngine>()

    val targetCpu = if (Build.SUPPORTED_ABIS[0].startsWith("armeabi-v6")) "armeabi" else Build.SUPPORTED_ABIS[0]

    val engineIntent = Intent(ENGINE_PROVIDER_MARKER)
    val engineProviderList = context.packageManager.queryIntentActivities(
        engineIntent,
        PackageManager.GET_META_DATA
    )

    for (resolveInfo in engineProviderList) {
        val enginesForCurrentPackage =
            getEnginesForResolveInfo(
                context,
                targetCpu,
                resolveInfo
            )
        enginesForCurrentPackage.forEach { result += it }
    }

    return Collections.unmodifiableList(result)
}

private fun getEnginesForResolveInfo(
    context: Context,
    targetCPU: String,
    resolveInfo: ResolveInfo
): List<ChessEngine> {
    val packageName = resolveInfo.activityInfo.packageName
    val bundle = resolveInfo.activityInfo.metaData
    val result = mutableListOf<ChessEngine>()
    if (bundle != null) {
        val authority = bundle.getString("chess.provider.engine.authority")
        if (authority != null) {
            try {
                val resources =
                    context.packageManager.getResourcesForApplication(resolveInfo.activityInfo.applicationInfo)
                val resId = resources.getIdentifier(
                    "enginelist",
                    "xml", packageName
                )
                val parser = resources.getXml(resId)
                var eventType = parser.eventType
                while (eventType != XmlResourceParser.END_DOCUMENT) {
                    try {
                        if (eventType == XmlResourceParser.START_TAG) {
                             getEnginesForPackageNameAndGivenParser(
                                context,
                                parser,
                                targetCPU,
                                authority,
                                packageName
                            ).forEach { result.add(it) }
                        }
                        eventType = parser.next()
                    } catch (ex: IOException) {
                        Log.e("ChessEngineUtils", ex.localizedMessage, ex)
                    }
                }

            } catch (ex: XmlPullParserException) {
                Log.e("ChessEngineUtils", ex.localizedMessage, ex)
            } catch (ex: PackageManager.NameNotFoundException) {
                Log.e("ChessEngineUtils", ex.localizedMessage, ex)
            }
        }
    }

    return result
}

private fun getEnginesForPackageNameAndGivenParser(
    context: Context,
    parser: XmlResourceParser,
    targetCPU: String,
    authority: String,
    packageName: String
): List<ChessEngine> {
    val result = mutableListOf<ChessEngine>()

    if (parser.name.equals(
            "engine", ignoreCase = true
        )
    ) {
        val fileName = parser.getAttributeValue(null, "filename")
        val title = parser.getAttributeValue(null, "name")
        val targetSpecification = parser.getAttributeValue(
            null,
            "target"
        )

        val targets = targetSpecification.split("|")
        for (currentCpuTarget in targets) {
            if (targetCPU == currentCpuTarget) {
                var versionCode: Int
                try {
                    versionCode = context.packageManager
                        .getPackageInfo(packageName, 0).versionCode

                    result.add(
                        ChessEngine(
                            name = title,
                            packageName = packageName,
                            authority = authority,
                            fileName = fileName,
                            versionCode = versionCode,
                            licenseCheckActivity = activitiesWithLicenseCheck[packageName]
                        )
                    )
                } catch (ex: PackageManager.NameNotFoundException) {
                    Log.e("ChessEngineUtils", ex.localizedMessage, ex)
                }
            }
        }
    }

    return result
}

private fun updateLicenseCheckActivities(
    context: Context
) {
    val result = mutableMapOf<String, String>()
    val engineLicenseIntent = Intent(ENGINE_PROVIDER_LICENSE_MARKER)
    val engineLicenseProviderList = context.packageManager.queryIntentActivities(
        engineLicenseIntent,
        PackageManager.GET_META_DATA
    )
    for (resolveInfo in engineLicenseProviderList) {
        val currentPackageName = resolveInfo.activityInfo.packageName
        val currentActivityName = resolveInfo.activityInfo.name
        if (currentPackageName != null && currentActivityName != null) {
            result[currentPackageName] = currentActivityName
        }
    }
    activitiesWithLicenseCheck = result
}