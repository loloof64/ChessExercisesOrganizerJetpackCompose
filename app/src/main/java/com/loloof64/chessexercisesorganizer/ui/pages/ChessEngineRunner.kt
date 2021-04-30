package com.loloof64.chessexercisesorganizer.ui.pages

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.NoSuchElementException

sealed class Error
object EngineNotStarted : Error()
data class CannotStartEngine(val msg: String) : Error()
data class CannotCommunicateWithEngine(val msg: String) : Error()
data class MiscError(val msg: String) : Error()

class ChessEngineRunner(
    private val engineFile: File,
    val errorCallback: (Error) -> Unit
) {
    private val mutex = Mutex()
    var isRunning = AtomicBoolean(false)
    private var isUCI = false
    private var startedOk = AtomicBoolean(false)
    private var mainJob: Job? = null
    private var process: Process? = null
    private var outputQueue = LinkedList<String>()
    private var processBufferedReader: BufferedReader? = null
    private var processOutputStream: OutputStream? = null
    private var globalJob: Job? = null
    private var globalCoroutineScope: CoroutineScope? = null
    private var readingOutputLoopJob : Job? = null

    private suspend fun checkEngineStarted() {
        delay(10000)
        if (!isUCI || !isRunning.get() || !startedOk.get()) {
            stop()
            errorCallback(EngineNotStarted)
        }
    }

    fun run() {
        isUCI = false
        startedOk.set(false)
        globalJob = Job()
        globalCoroutineScope = CoroutineScope(Dispatchers.IO + globalJob as CompletableJob)
        globalCoroutineScope!!.launch {
            outputQueue.clear()
            // starter coroutine
            async { checkEngineStarted() }

            // running engine coroutine
            mainJob = launch MainJob@{
                try {
                    val engineAbsolutePath = engineFile.absolutePath
                    mutex.withLock {
                        runCatching {
                            process = ProcessBuilder(engineAbsolutePath).start()
                        }
                        if (process == null) {
                            errorCallback(CannotCommunicateWithEngine(""))
                            return@MainJob
                        }
                    }

                    val inputStream = process!!.inputStream
                    val inputSteamReader = InputStreamReader(inputStream)
                    processBufferedReader = BufferedReader(inputSteamReader, 8192)
                    processOutputStream = process!!.outputStream

                    sendCommand("uci")
                    sendCommand("isready")

                    var first = true
                    readingOutputLoopJob = CoroutineScope(Dispatchers.IO).launch {
                        runCatching {
                            while (isActive) {
                                try {
                                    var line: String? = null
                                    mutex.withLock {
                                        if (processBufferedReader?.ready() == true) {
                                            line = processBufferedReader?.readLine()
                                        }
                                    }
                                    if (line != null) {
                                        if (line == "uciok") isUCI = true
                                        if (line == "readyok") startedOk.set(true)
                                        outputQueue.add(line!!)
                                        if (first) {
                                            isRunning.set(true)
                                            first = false
                                        }
                                    }
                                } catch (ignore: IOException) {
                                } catch (ex: Exception) {
                                    errorCallback(MiscError(ex.message ?: ""))
                                }
                            }
                        }
                    }
                } catch (ex: Exception) {
                    when (ex) {
                        is SecurityException -> errorCallback(CannotStartEngine(ex.message ?: ""))
                        is IOException -> errorCallback(
                            CannotCommunicateWithEngine(
                                ex.message ?: ""
                            )
                        )
                        else -> errorCallback(MiscError(ex.message ?: ""))
                    }

                }
            }
        }
    }

    fun sendCommand(command: String) {
        val carriageReturnTerminatedCmd = "$command\n"
        try {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    mutex.withLock {
                        if (process != null) {
                            processOutputStream?.write(carriageReturnTerminatedCmd.toByteArray())
                            processOutputStream?.flush()
                        }
                    }
                }
            }
        } catch (ignore: IOException) {
        }
    }

    fun readNextOutput(): String? {
        return try {
            val next = outputQueue.remove()
            next
        } catch (ex: NoSuchElementException) {
            null
        }
    }

    fun stop() {
        readingOutputLoopJob?.cancel()
        mainJob?.cancel()
        processBufferedReader?.close()
        processOutputStream?.close()
        globalJob?.cancel()
        globalCoroutineScope = null
        isRunning.set(false)
    }
}