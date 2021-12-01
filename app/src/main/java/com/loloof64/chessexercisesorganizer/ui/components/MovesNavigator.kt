package com.loloof64.chessexercisesorganizer.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.alonsoruibal.chess.Board
import com.alonsoruibal.chess.Move
import com.alonsoruibal.chess.bitboard.BitboardUtils
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.core.pgnparser.GameTermination
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNNode
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

class MoveNumber(text: String) : MovesNavigatorElement(text)

data class HalfMoveSAN(
    override val text: String, override val fen: String? = null,
    override val lastMoveArrowData: MoveData? = null
) : MovesNavigatorElement(text, fen, lastMoveArrowData)

fun buildHistoryFromPGNGame(game: PGNGame): List<MovesNavigatorElement> {
    val gameState = (game.tags["FEN"] ?: Board.FEN_START_POSITION).toBoard()

    try {
        return if (game.moves != null) recurBuildHistoryFromPGNTree(
            tree = game.moves,
            forceMoveNumberInsertion = true,
            currentGameState = gameState
        ) else listOf()
    } catch (ex: Exception) {
        throw ex
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
        text = tree.moveValue,
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

@Composable
fun MovesNavigatorButtons(
    modifier: Modifier = Modifier,
    handleFirstPositionRequest: () -> Unit = {},
    handleLastPositionRequest: () -> Unit = {},
    handlePreviousPositionRequest: () -> Unit = {},
    handleNextPositionRequest: () -> Unit = {},
) {
    val iconsModifier = Modifier.size(128.dp)
    val iconsTint = Color.Blue
    Row(
        modifier = modifier
            .fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = handleFirstPositionRequest) {
            Icon(
                contentDescription = stringResource(id = R.string.first_position),
                imageVector = Icons.Filled.FirstPage,
                tint = iconsTint,
                modifier = iconsModifier
            )
        }
        IconButton(onClick = handlePreviousPositionRequest) {
            Icon(
                contentDescription = stringResource(id = R.string.previous_position),
                imageVector = Icons.Filled.ArrowLeft,
                tint = iconsTint,
                modifier = iconsModifier
            )
        }
        IconButton(onClick = handleNextPositionRequest) {
            Icon(
                contentDescription = stringResource(id = R.string.next_position),
                imageVector = Icons.Filled.ArrowRight,
                tint = iconsTint,
                modifier = iconsModifier
            )
        }
        IconButton(onClick = handleLastPositionRequest) {
            Icon(
                contentDescription = stringResource(id = R.string.last_position),
                imageVector = Icons.Filled.LastPage,
                tint = iconsTint,
                modifier = iconsModifier
            )
        }
    }
}

@Composable
fun MovesNavigator(
    modifier: Modifier = Modifier,
    elements: Array<MovesNavigatorElement>,
    mustBeVisibleByDefaultElementIndex: Int? = null,
    highlightedItemIndex: Int? = null,
    elementSelectionRequestCallback: (Triple<String, MoveData, Int>) -> Unit = { _ -> },
    handleFirstPositionRequest: () -> Unit = {},
    handleLastPositionRequest: () -> Unit = {},
    handlePreviousPositionRequest: () -> Unit = {},
    handleNextPositionRequest: () -> Unit = {},
) {
    val lineHeightPixels = with(LocalDensity.current) { 34.sp.toPx() }
    val scrollAmount =
        if (mustBeVisibleByDefaultElementIndex != null) ((mustBeVisibleByDefaultElementIndex / 6) * lineHeightPixels).toInt() else 0
    val vertScrollState = ScrollState(scrollAmount)

    Column(
        modifier = modifier
            .background(color = Color.Yellow.copy(alpha = 0.3f))
    ) {
        MovesNavigatorButtons(
            handleFirstPositionRequest = handleFirstPositionRequest,
            handlePreviousPositionRequest = handlePreviousPositionRequest,
            handleNextPositionRequest = handleNextPositionRequest,
            handleLastPositionRequest = handleLastPositionRequest
        )

        FlowRow(
            modifier = Modifier
                .verticalScroll(vertScrollState),
            mainAxisSpacing = 8.dp,
        ) {
            elements.mapIndexed { index, elt ->
                val backgroundColor =
                    if (index == highlightedItemIndex) Color.Green else Color.Transparent
                Text(text = elt.text,
                    fontSize = 34.sp,
                    color = Color.Blue,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .background(color = backgroundColor)
                        .clickable {
                            if (elt.fen != null) elementSelectionRequestCallback(
                                Triple(
                                    elt.fen!!,
                                    elt.lastMoveArrowData!!,
                                    index
                                )
                            )
                        }
                )
            }
        }
    }
}

@Preview
@Composable
fun MovesNavigatorPreview() {
    MovesNavigator(
        modifier = Modifier
            .width(400.dp)
            .height(450.dp),
        elements = arrayOf(
            MoveNumber("1."),
            HalfMoveSAN("e4"),
            HalfMoveSAN("e5"),

            MoveNumber("2."),
            HalfMoveSAN("\u2658f3"),
            HalfMoveSAN("\u265ec6"),

            MoveNumber("3."),
            HalfMoveSAN("\u2657b5"),
            HalfMoveSAN("\u265ef6"),
        )
    )
}