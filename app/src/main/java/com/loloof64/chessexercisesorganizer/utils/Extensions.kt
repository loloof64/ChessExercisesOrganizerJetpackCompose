package com.loloof64.chessexercisesorganizer.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun <T> MutableStateFlow<T>.update(
    block: T.(T) -> T
) {
    value = value.run {
        block(this)
    }
}

fun String.stripPgnExtension(): String {
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
            } else {
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
            } else {
                holes++
            }
        }
        if (holes > 0) {
            lineResult += "$holes"
        }
        lineResult
    }.joinToString("/")
}

fun String.setWhiteTurn(): String {
    val parts = this.split(" ").toMutableList()
    if (parts.size < 2) return this
    parts[1] = "w"
    return parts.joinToString(" ")
}

fun String.setBlackTurn(): String {
    val parts = this.split(" ").toMutableList()
    if (parts.size < 2) return this
    parts[1] = "b"
    return parts.joinToString(" ")
}

fun String.isWhiteTurn(): Boolean {
    val parts = this.split(" ")
    if (parts.size < 2) return true
    return parts[1] != "b"
}

fun String.hasWhite00(): Boolean {
    val parts = this.split(" ")
    if (parts.size < 3) return true
    return parts[2].contains("K")
}

fun String.hasWhite000(): Boolean {
    val parts = this.split(" ")
    if (parts.size < 3) return true
    return parts[2].contains("Q")
}

fun String.hasBlack00(): Boolean {
    val parts = this.split(" ")
    if (parts.size < 3) return true
    return parts[2].contains("k")
}

fun String.hasBlack000(): Boolean {
    val parts = this.split(" ")
    if (parts.size < 3) return true
    return parts[2].contains("q")
}

fun String.toggleWhite00(): String {
    val parts = this.split(" ").toMutableList()
    if (parts.size < 3) return this

    val castles = parts[2].split("").toMutableSet()
    if (castles.contains("K")) {
        castles.remove("K")
    } else {
        (castles.add("K"))
    }

    if (castles.isEmpty()) {
        parts[2] = "-"
    } else {
        parts[2] = castles.joinToString("")
    }

    return parts.joinToString(" ")
}

fun String.toggleWhite000(): String {
    val parts = this.split(" ").toMutableList()
    if (parts.size < 3) return this

    val castles = parts[2].split("").toMutableSet()
    if (castles.contains("Q")) {
        castles.remove("Q")
    } else {
        (castles.add("Q"))
    }

    if (castles.isEmpty()) {
        parts[2] = "-"
    } else {
        parts[2] = castles.joinToString("")
    }

    return parts.joinToString(" ")
}

fun String.toggleBlack00(): String {
    val parts = this.split(" ").toMutableList()
    if (parts.size < 3) return this

    val castles = parts[2].split("").toMutableSet()
    if (castles.contains("k")) {
        castles.remove("k")
    } else {
        (castles.add("k"))
    }

    if (castles.isEmpty()) {
        parts[2] = "-"
    } else {
        parts[2] = castles.joinToString("")
    }

    return parts.joinToString(" ")
}

fun String.toggleBlack000(): String {
    val parts = this.split(" ").toMutableList()
    if (parts.size < 3) return this

    val castles = parts[2].split("").toMutableSet()
    if (castles.contains("q")) {
        castles.remove("q")
    } else {
        (castles.add("q"))
    }

    if (castles.isEmpty()) {
        parts[2] = "-"
    } else {
        parts[2] = castles.joinToString("")
    }

    return parts.joinToString(" ")
}