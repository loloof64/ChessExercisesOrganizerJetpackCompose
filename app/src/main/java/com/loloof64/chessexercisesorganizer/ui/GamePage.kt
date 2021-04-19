package com.loloof64.chessexercisesorganizer.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.components.DynamicChessBoard
import com.loloof64.chessexercisesorganizer.ui.components.STANDARD_FEN
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme

@Composable
fun GamePage() {
    val isLandscape = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }
    val notReadyPositionFen = "8/8/8/8/8/8/8/8 w - - 0 1"
    var boardReversed by rememberSaveable { mutableStateOf(false) }
    ChessExercisesOrganizerJetpackComposeTheme {
        Surface(color = MaterialTheme.colors.background) {
            if (isLandscape) {
                GamePageContentLandscape(
                    startPositionFen = notReadyPositionFen,
                    boardReversed = boardReversed,
                    boardReverseRequestCallback = { boardReversed = !boardReversed })
            } else {
                GamePageContentPortrait(
                    startPositionFen = notReadyPositionFen,
                    boardReversed = boardReversed,
                    boardReverseRequestCallback = { boardReversed = !boardReversed })
            }

        }
    }
}

@Composable
fun GamePageContentPortrait(
    startPositionFen: String,
    boardReversed: Boolean,
    boardReverseRequestCallback: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            GamePageFirstButtonsLine(boardReverseRequestCallback)
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            GamePageSecondButtonsLine()
        }
        BoxWithConstraints {
            DynamicChessBoard(
                size = this.maxWidth,
                startPosition = startPositionFen,
                reversed = boardReversed
            )
        }
    }
}

@Composable
fun GamePageContentLandscape(
    startPositionFen: String,
    boardReversed: Boolean,
    boardReverseRequestCallback: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxHeight()
        ) {
            GamePageFirstButtonsLine(boardReverseRequestCallback)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxHeight()
        ) {
            GamePageSecondButtonsLine()
        }
        BoxWithConstraints {
            DynamicChessBoard(
                size = this.maxHeight,
                startPosition = startPositionFen,
                reversed = boardReversed
            )
        }
    }
}

@Composable
fun GamePageFirstButtonsLine(boardReverseRequestCallback: () -> Unit) {
    SimpleButton(
        text = stringResource(R.string.new_game),
        vectorId = R.drawable.ic_start_flag
    ) {

    }
    SimpleButton(
        text = stringResource(R.string.stop_game),
        vectorId = R.drawable.ic_stop
    ) {

    }
    SimpleButton(
        text = stringResource(R.string.reverse_board),
        vectorId = R.drawable.ic_reverse,
        callback = boardReverseRequestCallback
    )
}

@Composable
private fun GamePageSecondButtonsLine() {
    SimpleButton(
        text = stringResource(R.string.chess_engines),
        vectorId = R.drawable.ic_car_engine
    ) {

    }
}

@Composable
fun SimpleButton(
    modifier: Modifier = Modifier,
    text: String,
    vectorId: Int,
    imageContentDescription: String = text,
    imageSize: Dp = 30.dp,
    textSize: TextUnit = 20.sp,
    callback: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(border = BorderStroke(width = 2.dp, color = Color.Black))
            .padding(4.dp),
    ) {
        IconButton(onClick = callback, modifier = Modifier.size(imageSize)) {
            Image(
                painter = painterResource(id = vectorId),
                contentDescription = imageContentDescription
            )
        }
        Text(text = text, fontSize = textSize)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GamePageContentPortrait(
        startPositionFen = STANDARD_FEN,
        boardReversed = false,
        boardReverseRequestCallback = {}
    )
}

@Preview(showBackground = true)
@Composable
fun MainPageLandscapePreview() {
    GamePageContentLandscape(
        startPositionFen = STANDARD_FEN,
        boardReversed = false,
        boardReverseRequestCallback = {}
    )
}

@Preview
@Composable
fun SimpleButtonPreview() {
    SimpleButton(text = "New", vectorId = R.drawable.ic_start_flag) {

    }
}