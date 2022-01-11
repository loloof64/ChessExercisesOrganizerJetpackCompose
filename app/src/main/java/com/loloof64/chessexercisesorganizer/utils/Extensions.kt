package com.loloof64.chessexercisesorganizer.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun <T> MutableStateFlow<T>.update(
    block: T.(T) -> T
)  {
    value = value.run{
        block(this)
    }
}

fun String.stripPgnExtension() : String {
    return if (this.endsWith(".pgn")) {
        this.subSequence(0 until this.lastIndexOf(".pgn")).toString()
    } else this
}

fun String.encodePath(): String {
    return this.replace("/".toRegex(), "#")
}

fun String.decodePath(): String {
    return this.replace("#".toRegex(), "/")
}

fun String.fenBoardPartToPiecesArray(): MutableList<MutableList<Char>> {
    val lines = this.split("/").reversed()
    return lines.map {
        val result = mutableListOf<Char>()
        val lineValues = it.toCharArray()
        var index = 0
        lineValues.forEach { currChar ->
            if (currChar.isDigit()) {
                val upperBound = currChar.digitToInt()
                (1..upperBound).forEach {
                    result.add('.')
                    index++
                }
            }
            else {
                result.add(currChar)
                index++
            }
        }
        result
    }.toMutableList()
}

fun MutableList<MutableList<Char>>.toBoardFen(): String {
    return this.reversed().map { line ->
        var holes = 0
        var lineResult = ""
        line.forEach {
            val isAPiece = "pnbrqkPNBRQK".contains(it)
            if (isAPiece) {
                if (holes > 0) {
                    lineResult += "$holes"
                    holes = 0
                }
                lineResult += it
            }
            else {
                holes++
            }
        }
        if (holes > 0) {
            lineResult += "$holes"
        }
        lineResult
    }.joinToString("/")
}