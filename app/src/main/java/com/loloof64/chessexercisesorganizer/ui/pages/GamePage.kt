package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.Surface
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
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.navigate
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.components.DynamicChessBoard
import com.loloof64.chessexercisesorganizer.ui.components.STANDARD_FEN
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme

@Composable
fun GamePage(navController: NavController? = null) {
    val isLandscape = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }
    val notReadyPositionFen = "8/8/8/8/8/8/8/8 w - - 0 1"
    var boardReversed by rememberSaveable { mutableStateOf(false) }
    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(
            topBar = {TopAppBar(title = {Text(stringResource(R.string.game_page))})},
            content = {
                Surface(color = MaterialTheme.colors.background) {
                    if (isLandscape) {
                        GamePageContentLandscape(
                            navController = navController,
                            startPositionFen = notReadyPositionFen,
                            boardReversed = boardReversed,
                            boardReverseRequestCallback = { boardReversed = !boardReversed })
                    } else {
                        GamePageContentPortrait(
                            navController = navController,
                            startPositionFen = notReadyPositionFen,
                            boardReversed = boardReversed,
                            boardReverseRequestCallback = { boardReversed = !boardReversed })
                    }

                }
            }
        )
    }
}

@Composable
fun GamePageContentPortrait(
    navController: NavController? = null,
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
            GamePageFirstButtonsLine(
                boardReverseRequestCallback = boardReverseRequestCallback
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            GamePageSecondButtonsLine(
                navController = navController
            )
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
    navController: NavController? = null,
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
            GamePageFirstButtonsLine(
                boardReverseRequestCallback = boardReverseRequestCallback
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxHeight()
        ) {
            GamePageSecondButtonsLine(
                navController = navController
            )
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
        callback = {_ -> boardReverseRequestCallback() }
    )
}

@Composable
private fun GamePageSecondButtonsLine(navController: NavController? = null) {
    SimpleButton(
        navController = navController,
        text = stringResource(R.string.chess_engines),
        vectorId = R.drawable.ic_car_engine
    ) {
        it?.navigate("engines") {
            launchSingleTop = true
        }
    }
}

@Composable
fun SimpleButton(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    text: String,
    vectorId: Int,
    imageContentDescription: String = text,
    imageSize: Dp = 30.dp,
    textSize: TextUnit = 20.sp,
    callback: (NavController?) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(border = BorderStroke(width = 2.dp, color = Color.Black))
            .padding(4.dp),
    ) {
        IconButton(onClick = {callback(navController)}, modifier = Modifier.size(imageSize)) {
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