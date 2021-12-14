package com.loloof64.chessexercisesorganizer.ui.components.moves_navigator

import com.alonsoruibal.chess.Board
import com.alonsoruibal.chess.Move
import com.alonsoruibal.chess.bitboard.BitboardUtils
import com.loloof64.chessexercisesorganizer.core.pgnparser.GameTermination
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNNode
import com.loloof64.chessexercisesorganizer.ui.components.MoveData
import com.loloof64.chessexercisesorganizer.ui.components.toBoard
import com.loloof64.chessexercisesorganizer.ui.components.toFAN
import java.lang.RuntimeException

sealed class MovesNavigatorElement(
    open val text: String,
    open val fen: String? = null,
    open val lastMoveArrowData: MoveData? = null
)

class LeftParenthesis : MovesNavigatorElement(text = "(")
class RightParenthesis : MovesNavigatorElement(text = ")")

class WhiteWon : MovesNavigatorElement(text = "1-0")
class BlackWon : MovesNavigatorElement(text = "0-1")
class Draw : MovesNavigatorElement(text = "1/2-1/2")
class NotKnown : MovesNavigatorElement(text = "*")

class GamesLoadingException(cause: Throwable) : Exception(cause)

fun buildHistoryFromPGNGame(game: PGNGame): List<MovesNavigatorElement> {
    val gameState = (game.tags["FEN"] ?: Board.FEN_START_POSITION).toBoard()

    try {
        return if (game.moves != null) recurBuildHistoryFromPGNTree(
            tree = game.moves,
            forceMoveNumberInsertion = true,
            currentGameState = gameState
        ) else listOf()
    } catch (ex: Exception) {
        throw GamesLoadingException(ex)
    }
}

private fun recurBuildHistoryFromPGNTree(
    tree: PGNNode,
    forceMoveNumberInsertion: Boolean = false,
    currentGameState: Board,
): MutableList<MovesNavigatorElement> {
    val previousGameFen = currentGameState.fen
    val elements = mutableListOf<MovesNavigatorElement>()
    if (tree.whiteMove || forceMoveNumberInsertion) {
        val moveNumberElement =
            MoveNumber(text = "${tree.moveNumber}.${if (tree.whiteMove) "" else ".."}")
        elements.add(moveNumberElement)
    }

    val move = Move.getFromString(currentGameState, tree.moveValue, true)
    val moveFrom = Move.getFromSquare(move)
    val moveTo = Move.getToSquare(move)
    val moveFromString = BitboardUtils.square2Algebraic(moveFrom)
    val moveToString = BitboardUtils.square2Algebraic(moveTo)
    val success = currentGameState.doMove(move)
    if (!success) throw RuntimeException("Illegal move ${tree.moveValue} at board ${currentGameState.fen} !")

    val newFen = currentGameState.fen
    val lastMoveCoordinates = MoveData.parse("$moveFromString$moveToString")
    val moveSanElement = HalfMoveSAN(
        text = tree.moveValue.toFAN(forBlackTurn = !tree.whiteMove),
        fen = newFen,
        lastMoveArrowData = lastMoveCoordinates
    )
    elements.add(moveSanElement)

    tree.variations.forEach {
        elements.add(LeftParenthesis())
        elements.addAll(
            recurBuildHistoryFromPGNTree(
                tree = it,
                forceMoveNumberInsertion = true,
                currentGameState = previousGameFen.toBoard(),
            )
        )
        elements.add(RightParenthesis())
    }

    if (tree.gameTermination != null) {
        elements.add(
            when (tree.gameTermination) {
                GameTermination.WhiteWon -> WhiteWon()
                GameTermination.BlackWon -> BlackWon()
                GameTermination.Draw -> Draw()
                else -> NotKnown()
            }
        )
    }

    if (tree.nextNode != null) {
        elements.addAll(
            recurBuildHistoryFromPGNTree(
                tree = tree.nextNode!!,
                forceMoveNumberInsertion = tree.variations.isNotEmpty(),
                currentGameState = currentGameState,
            )
        )
    }
    return elements
}
