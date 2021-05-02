package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.res.Configuration
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
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalConfiguration
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
import com.loloof64.chessexercisesorganizer.ui.components.*
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme

@Composable
fun GamePage(navController: NavController? = null) {

    val scaffoldState = rememberScaffoldState()

    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { TopAppBar(title = { Text(stringResource(R.string.game_page)) }) },
            content = {
                Surface(color = MaterialTheme.colors.background) {
                    AdaptableLayoutGamePageContent(
                        navController,
                    )
                }
            },
        )
    }
}

@Composable
fun AdaptableLayoutGamePageContent(
    navController: NavController? = null,
) {

    val restartPosition = STANDARD_FEN
    val positionHandlerInstance by rememberSaveable(stateSaver = PositionHandlerSaver) {
        mutableStateOf(
            PositionHandler(restartPosition)
        )
    }
    var currentPosition by rememberSaveable {
        mutableStateOf(positionHandlerInstance.getCurrentPosition())
    }

    var promotionState by rememberSaveable(stateSaver = PendingPromotionStateSaver) {
        mutableStateOf(PendingPromotionData())
    }

    val isLandscape = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }

    var boardReversed by rememberSaveable { mutableStateOf(false) }
    var gameInProgress by rememberSaveable { mutableStateOf(false) }

    fun startNewGame() {
        promotionState = PendingPromotionData()
        positionHandlerInstance.newGame()
        currentPosition = positionHandlerInstance.getCurrentPosition()
        gameInProgress = true
    }

    Layout(
        content = {
            SimpleButton(
                text = stringResource(R.string.new_game),
                vectorId = R.drawable.ic_start_flag
            ) {
                startNewGame()
            }
            SimpleButton(
                text = stringResource(R.string.stop_game),
                vectorId = R.drawable.ic_stop
            ) {

            }
            SimpleButton(
                text = stringResource(R.string.reverse_board),
                vectorId = R.drawable.ic_reverse,
            ) {
                boardReversed = !boardReversed
            }
            SimpleButton(
                navController = navController,
                text = stringResource(R.string.chess_engines),
                vectorId = R.drawable.ic_car_engine
            ) {
                it?.navigate("engines") {
                    launchSingleTop = true
                }
            }

            DynamicChessBoard(
                reversed = boardReversed,
                gameInProgress = gameInProgress,
                position = currentPosition,
                promotionState = promotionState,
                validMoveCallback = {
                    positionHandlerInstance.isValidMove(it)
                },
                dndMoveCallback = {
                    positionHandlerInstance.makeMove(it)
                    currentPosition = positionHandlerInstance.getCurrentPosition()
                },
                promotionMoveCallback = {
                    positionHandlerInstance.makeMove(it)
                    promotionState = PendingPromotionData()
                    currentPosition = positionHandlerInstance.getCurrentPosition()
                },
                cancelPendingPromotionCallback = {
                    promotionState = PendingPromotionData()
                },
                setPendingPromotionCallback = {
                    promotionState = it
                }
            )

        }
    ) { allMeasurable, constraints ->
        val boardSize = if (isLandscape) constraints.maxHeight else constraints.maxWidth
        val buttonsCount = if (!gameInProgress) 4 else 3

        val allPlaceable = allMeasurable.mapIndexed { index, measurable ->
            val isBoard = index == buttonsCount
            val isCircularProgressBar = index == allMeasurable.size - 1

            if (isBoard || isCircularProgressBar) measurable.measure(
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

            var buttonsZoneEnd = buttonGap
            var accumulatedLocation = buttonGap
            var furtherLineButton = 0.0
            var crossLocation = buttonGap

            fun placeStdComponent(placeable: Placeable, location: Int) {
                if (isLandscape) {
                    placeable.place(location, 0)
                } else {
                    placeable.place(0, location)
                }
            }

            allPlaceable.forEachIndexed { index, placeable ->
                val isAButton = index < buttonsCount
                val isBoard = index == buttonsCount
                val isCircularProgressBar = index == allPlaceable.size - 1
                if (isAButton) {
                    val buttonColumnIndex = index % 3
                    if (buttonColumnIndex == 0) {
                        crossLocation = buttonGap
                        accumulatedLocation += furtherLineButton + buttonGap
                        buttonsZoneEnd = accumulatedLocation
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
                } else if (isBoard) {
                    accumulatedLocation += furtherLineButton + buttonGap
                    buttonsZoneEnd = accumulatedLocation
                    placeStdComponent(placeable, accumulatedLocation.toInt())
                } else if (isCircularProgressBar) {
                    if (isLandscape) {
                        placeable.place(buttonsZoneEnd.toInt(), 0)
                    } else {
                        placeable.place(0, buttonsZoneEnd.toInt())
                    }
                } else {
                    accumulatedLocation += furtherLineButton + buttonGap
                    placeStdComponent(placeable, accumulatedLocation.toInt())
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