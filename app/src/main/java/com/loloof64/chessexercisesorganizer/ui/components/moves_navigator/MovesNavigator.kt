package com.loloof64.chessexercisesorganizer.ui.components.moves_navigator

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.alonsoruibal.chess.Board
import com.alonsoruibal.chess.Move
import com.alonsoruibal.chess.bitboard.BitboardUtils
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.core.pgnparser.GameTermination
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNNode
import com.loloof64.chessexercisesorganizer.ui.components.MoveData
import java.lang.RuntimeException

class MoveNumber(text: String) : MovesNavigatorElement(text)

data class HalfMoveSAN(
    override val text: String, override val fen: String? = null,
    override val lastMoveArrowData: MoveData? = null
) : MovesNavigatorElement(text, fen, lastMoveArrowData)

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
    isInSolutionMode: Boolean = false,
    modeSelectionActive: Boolean = false,
    mustBeVisibleByDefaultElementIndex: Int? = null,
    highlightedItemIndex: Int? = null,
    failedToLoadSolution: Boolean = false,
    elementSelectionRequestCallback: (Triple<String, MoveData, Int>) -> Unit = { _ -> },
    handleFirstPositionRequest: () -> Unit = {},
    handleLastPositionRequest: () -> Unit = {},
    handlePreviousPositionRequest: () -> Unit = {},
    handleNextPositionRequest: () -> Unit = {},
    historyModeToggleRequestHandler: () -> Unit = {},
) {
    val lineHeightPixels = with(LocalDensity.current) { 34.sp.toPx() }
    val scrollAmount =
        if (mustBeVisibleByDefaultElementIndex != null) ((mustBeVisibleByDefaultElementIndex / 6) * lineHeightPixels).toInt() else 0
    val vertScrollState = ScrollState(scrollAmount)

    val modeTextID = if (isInSolutionMode) R.string.solution_mode else R.string.played_game_mode
    val modeText = stringResource(id = modeTextID)

    val toggleHistoryModeText = stringResource(id = R.string.toggle_history_mode)

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

        if (failedToLoadSolution) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.failed_loading_solution),
                    fontSize = 18.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else if (modeSelectionActive) {

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = modeText,
                    fontSize = 18.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.40f)
                        .background(Color.Blue)
                        .padding(horizontal = 10.dp)
                )

                Button(
                    onClick = historyModeToggleRequestHandler,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Yellow),
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(text = toggleHistoryModeText, fontSize = 18.sp, color = Color.Red)
                }
            }

        }

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