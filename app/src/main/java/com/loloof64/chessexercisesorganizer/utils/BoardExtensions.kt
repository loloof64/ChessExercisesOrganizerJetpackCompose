package com.loloof64.chessexercisesorganizer.utils

import com.alonsoruibal.chess.bitboard.BitboardUtils
import com.loloof64.chessexercisesorganizer.ui.components.toBoard

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

fun String.setEnPassantSquare(newValue: String): String {
    val parts = this.split(" ").toMutableList()
    if (parts.size < 4) return this

    parts[3] = newValue

    return parts.joinToString(" ")
}

fun String.getEnPassantSquare(): String {
    val parts = this.split(" ")
    if (parts.size < 4) return this

    return parts[3]
}

fun String.getEnPassantSquareValueIndex(): Int {
    return when {
        this.startsWith("a") -> 1
        this.startsWith("b") -> 2
        this.startsWith("c") -> 3
        this.startsWith("d") -> 4
        this.startsWith("e") -> 5
        this.startsWith("f") -> 6
        this.startsWith("g") -> 7
        this.startsWith("h") -> 8
        else -> 0
    }
}

fun getAlgebraic(file: Int, rank: Int): String {
    return "${('a'.code + file).toChar()}${('1'.code + rank).toChar()}"
}

fun String.correctEnPassantSquare(): String {
    val parts = this.split(" ").toMutableList()
    if (parts.size < 4) return this

    val enPassantSquare = BitboardUtils.algebraic2Square(parts[3])
    if (enPassantSquare == 0L) return this

    val enPassantFile = parts[3][0].code - 'a'.code
    val enPassantRank = parts[3][1].code - '1'.code

    val matchingBoard = this.toBoard()

    val pieceAboveEP = if (enPassantRank < 7) {
        matchingBoard.getPieceAt(
            BitboardUtils.algebraic2Square(
                getAlgebraic(
                    enPassantFile,
                    enPassantRank + 1
                )
            )
        )
    } else {
        0
    }
    val pieceBelowEP = if (enPassantRank > 0) {
        matchingBoard.getPieceAt(
            BitboardUtils.algebraic2Square(
                getAlgebraic(
                    enPassantFile,
                    enPassantRank - 1
                )
            )
        )
    } else {
        0
    }

    val pieceOnAdjacentDiagLeftBelow = if (enPassantRank > 0 && enPassantFile > 0) {
        matchingBoard.getPieceAt(
            BitboardUtils.algebraic2Square(
                getAlgebraic(
                    enPassantFile - 1,
                    enPassantRank - 1
                )
            )
        )
    } else {
        0
    }

    val pieceOnAdjacentDiagRightBelow = if (enPassantRank > 0 && enPassantFile < 7) {
        matchingBoard.getPieceAt(
            BitboardUtils.algebraic2Square(
                getAlgebraic(
                    enPassantFile + 1,
                    enPassantRank - 1
                )
            )
        )
    } else {
        0
    }

    val pieceOnAdjacentDiagLeftAbove = if (enPassantRank < 7 && enPassantFile > 0) {
        matchingBoard.getPieceAt(
            BitboardUtils.algebraic2Square(
                getAlgebraic(
                    enPassantFile - 1,
                    enPassantRank + 1
                )
            )
        )
    } else {
        0
    }

    val pieceOnAdjacentDiagRightAbove = if (enPassantRank < 7 && enPassantFile < 7) {
        matchingBoard.getPieceAt(
            BitboardUtils.algebraic2Square(
                getAlgebraic(
                    enPassantFile + 1,
                    enPassantRank + 1
                )
            )
        )
    } else {
        0
    }

    val isWhiteTurn = parts[1] == "w";
    if (isWhiteTurn && enPassantRank != 5) {
        parts[3] = "-"
    } else if (!isWhiteTurn && enPassantRank != 2) {
        parts[3] = "-"
    } else if (isWhiteTurn && pieceBelowEP != 'p') {
        parts[3] = "-"
    } else if (!isWhiteTurn && pieceAboveEP != 'P') {
        parts[3] = "-"
    } else if ((isWhiteTurn && pieceOnAdjacentDiagLeftBelow != 'P') && (isWhiteTurn && pieceOnAdjacentDiagRightBelow != 'P')) {
        parts[3] = "-"
    } else if ((!isWhiteTurn && pieceOnAdjacentDiagLeftAbove != 'p') && (!isWhiteTurn && pieceOnAdjacentDiagRightAbove != 'p')) {
        parts[3] = "-"
    }

    return parts.joinToString(" ")
}

fun String.setDrawHalfMovesCount(value: Int): String {
    val parts = this.split(" ").toMutableList()
    if (parts.size < 5) return this

    parts[4] = "${if (value < 0) 0 else value}"
    return parts.joinToString(" ")
}

fun String.getDrawHalfMovesCount(): Int {
    val parts = this.split(" ")
    if (parts.size < 5) return 0

    return Integer.parseInt(parts[4])
}

fun String.setMoveNumber(value: Int): String {
    val parts = this.split(" ").toMutableList()
    if (parts.size < 6) return this

    parts[5] = "${if (value < 0) 0 else value}"
    return parts.joinToString(" ")
}

fun String.getMoveNumber(): Int {
    val parts = this.split(" ")
    if (parts.size < 6) return 0

    return Integer.parseInt(parts[5])
}