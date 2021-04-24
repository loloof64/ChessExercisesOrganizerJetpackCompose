package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.components.DynamicChessBoard
import com.loloof64.chessexercisesorganizer.ui.components.GameEndedStatus
import com.loloof64.chessexercisesorganizer.ui.components.PlayerType
import com.loloof64.chessexercisesorganizer.ui.components.STANDARD_FEN
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import kotlin.random.Random

@Composable
fun GamePage(navController: NavController? = null) {
    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(R.string.game_page)) }) },
            content = {
                Surface(color = MaterialTheme.colors.background) {
                    AdaptableLayoutGamePageContent(navController)
                }
            }
        )
    }
}

@Composable
fun AdaptableLayoutGamePageContent(navController: NavController? = null) {
    val context = LocalContext.current

    val startPositionFen = STANDARD_FEN
    val isLandscape = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }
    var boardReversed by rememberSaveable { mutableStateOf(false) }

    var stopRequest by rememberSaveable { mutableStateOf(false) }

    var enginesConfigurationAvailable by rememberSaveable {
        mutableStateOf(false)
    }

    fun stopGame() {
        stopRequest = true
        enginesConfigurationAvailable = true
    }

    fun randomGameId(): Long {
        return Random.nextLong()
    }

    var gameId by rememberSaveable {
        mutableStateOf(randomGameId())
    }

    fun restartGame() {
        enginesConfigurationAvailable = false
        stopRequest = false
        gameId = randomGameId()
    }

    fun notifyUserGameFinished(gameEndStatus: GameEndedStatus) {
        val messageId = when (gameEndStatus) {
            GameEndedStatus.CHECKMATE_WHITE -> R.string.chessmate_white
            GameEndedStatus.CHECKMATE_BLACK -> R.string.chessmate_black
            GameEndedStatus.STALEMATE -> R.string.stalemate
            GameEndedStatus.DRAW_THREE_FOLD_REPETITION -> R.string.three_fold_repetition
            GameEndedStatus.DRAW_FIFTY_MOVES_RULE -> R.string.fifty_moves_rule_draw
            GameEndedStatus.DRAW_MISSING_MATERIAL -> R.string.missing_material_draw
            GameEndedStatus.USER_STOPPED -> R.string.user_stopped_game
            else -> throw IllegalStateException("The game is not finished yet.")
        }
        Toast.makeText(context, messageId, Toast.LENGTH_LONG).show()
        enginesConfigurationAvailable = true
    }

    Layout(
        content = {
            SimpleButton(
                text = stringResource(R.string.new_game),
                vectorId = R.drawable.ic_start_flag
            ) {
                restartGame()
            }
            SimpleButton(
                text = stringResource(R.string.stop_game),
                vectorId = R.drawable.ic_stop
            ) {
                stopGame()
            }
            SimpleButton(
                text = stringResource(R.string.reverse_board),
                vectorId = R.drawable.ic_reverse,
            ) {
                boardReversed = !boardReversed
            }
            if (enginesConfigurationAvailable) {
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

            DynamicChessBoard(
                startPosition = startPositionFen,
                reversed = boardReversed,
                whiteSideType = PlayerType.Human,
                blackSideType = PlayerType.Computer,
                gameId = gameId,
                userRequestStopGame = stopRequest,
                naturalGameEndCallback = { notifyUserGameFinished(it) },
            )

        }
    ) { allMeasurable, constraints ->
        val boardSize = if (isLandscape) constraints.maxHeight else constraints.maxWidth
        val buttonsCount = if (enginesConfigurationAvailable) 4 else 3

        val allPlaceable = allMeasurable.mapIndexed { index, measurable ->
            if (index == buttonsCount) measurable.measure(
                constraints.copy(
                    minWidth = boardSize,
                    minHeight = boardSize,
                    maxWidth = boardSize,
                    maxHeight = boardSize
                )
            )
            else measurable.measure(constraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val minOfMaxSize =
                if (constraints.maxWidth < constraints.maxHeight) constraints.maxWidth else constraints.maxHeight
            val buttonGap = minOfMaxSize * 0.01

            var accumulatedLocation = buttonGap
            var furtherLineButton = 0.0
            var crossLocation = buttonGap

            allPlaceable.forEachIndexed { index, placeable ->
                val isAButton = index < buttonsCount
                if (isAButton) {
                    val buttonColumnIndex = index % 3
                    if (buttonColumnIndex == 0) {
                        crossLocation = buttonGap
                        accumulatedLocation += furtherLineButton + buttonGap
                        furtherLineButton = 0.0
                    }
                    if (isLandscape) {
                        val x = accumulatedLocation
                        val y = crossLocation
                        val currentEndX = placeable.width
                        if (currentEndX > furtherLineButton) furtherLineButton =
                            currentEndX.toDouble()

                        placeable.place(x.toInt(), y.toInt())
                        crossLocation += placeable.height + buttonGap
                    } else {
                        val x = crossLocation
                        val y = accumulatedLocation
                        val currentEndY = placeable.height
                        if (currentEndY > furtherLineButton) furtherLineButton =
                            currentEndY.toDouble()

                        placeable.place(x.toInt(), y.toInt())
                        crossLocation += placeable.width + buttonGap
                    }
                } else {
                    accumulatedLocation += furtherLineButton + buttonGap
                    if (isLandscape) {
                        placeable.place(accumulatedLocation.toInt(), 0)
                    } else {
                        placeable.place(0, accumulatedLocation.toInt())
                    }
                }
            }
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
        IconButton(onClick = { callback(navController) }, modifier = Modifier.size(imageSize)) {
            Image(
                painter = painterResource(id = vectorId),
                contentDescription = imageContentDescription
            )
        }
        Text(text = text, fontSize = textSize)
    }
}

@Preview
@Composable
fun SimpleButtonPreview() {
    SimpleButton(text = "New", vectorId = R.drawable.ic_start_flag) {

    }
}