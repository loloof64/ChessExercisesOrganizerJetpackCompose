package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.loloof64.chessexercisesorganizer.ui.components.*
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun GamePage(navController: NavController? = null) {

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val okString = stringResource(R.string.ok)

    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { TopAppBar(title = { Text(stringResource(R.string.game_page)) }) },
            content = {
                Surface(color = MaterialTheme.colors.background) {
                    AdaptableLayoutGamePageContent(
                        navController,
                        showInfiniteSnackbarAction = { text ->
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = text,
                                    actionLabel = okString,
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                        },
                        showMinutedSnackbarAction = { text, duration ->
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = text,
                                    duration = duration,
                                )
                            }
                        })
                }
            },
        )
    }
}

@Composable
fun AdaptableLayoutGamePageContent(
    navController: NavController? = null,
    showInfiniteSnackbarAction: (String) -> Unit,
    showMinutedSnackbarAction: (String, SnackbarDuration) -> Unit,
) {
    val context = LocalContext.current
    val emptyFen = "8/8/8/8/8/8/8/8 w - - 0 1"

    var startPositionFen by rememberSaveable { mutableStateOf(emptyFen) }
    val isLandscape = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }

    var boardReversed by rememberSaveable { mutableStateOf(false) }

    var stopRequest by rememberSaveable { mutableStateOf(false) }

    var gameInProgress by rememberSaveable {
        mutableStateOf(false)
    }

    var newGameRequestState by rememberSaveable {
        mutableStateOf(NewGameRequestState.NO_PENDING_REQUEST)
    }

    var confirmStopDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var selectEngineDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()
    var readEngineOutputJob by remember {
        mutableStateOf<Job?>(null)
    }


    fun stopGame() {
        stopCurrentRunningEngine()
        stopRequest = true
        gameInProgress = false
    }

    var newComputerMovePendingState by rememberSaveable {
        mutableStateOf(ComputerMovePendingState.NO_NEW_CPU_MOVE)
    }

    var computerMoveString by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val enginesFolder = File(context.filesDir, "engines")

    val noInstalledEngineText = stringResource(R.string.no_installed_engine_error)

    val checkmateWhiteText = stringResource(R.string.chessmate_white)
    val checkmateBlackText = stringResource(R.string.chessmate_black)
    val stalemateText = stringResource(R.string.stalemate)
    val threeFoldRepetitionText = stringResource(R.string.three_fold_repetition)
    val fiftyMovesText = stringResource(R.string.fifty_moves_rule_draw)
    val missingMaterialText = stringResource(R.string.missing_material_draw)
    val userStoppedText = stringResource(R.string.user_stopped_game)

    val errorLaunchingEngineText = stringResource(R.string.error_launching_engine)

    val enginesList = listInstalledEngines(enginesFolder)

    fun abortGameIfNoEngineInstalled() {
        val engineAvailable = enginesList.isNotEmpty()
        if (!engineAvailable) {
            stopGame()
            showInfiniteSnackbarAction(noInstalledEngineText)
        }
    }

    fun onNewGameRequest() {
        selectEngineDialogOpen = true
    }

    fun doStartNewGame() {
        startPositionFen = STANDARD_FEN
        gameInProgress = true
        stopRequest = false
        newGameRequestState = NewGameRequestState.REQUEST_PENDING
        abortGameIfNoEngineInstalled()
        sendCommandToRunningEngine("ucinewgame")
    }

    fun notifyUserGameFinished(gameEndStatus: GameEndedStatus) {
        if (!gameInProgress) return
        val message = when (gameEndStatus) {
            GameEndedStatus.CHECKMATE_WHITE -> checkmateWhiteText
            GameEndedStatus.CHECKMATE_BLACK -> checkmateBlackText
            GameEndedStatus.STALEMATE -> stalemateText
            GameEndedStatus.DRAW_THREE_FOLD_REPETITION -> threeFoldRepetitionText
            GameEndedStatus.DRAW_FIFTY_MOVES_RULE -> fiftyMovesText
            GameEndedStatus.DRAW_MISSING_MATERIAL -> missingMaterialText
            GameEndedStatus.USER_STOPPED -> userStoppedText
            else -> throw IllegalStateException("The game is not finished yet.")
        }
        showMinutedSnackbarAction(message, SnackbarDuration.Long)
        gameInProgress = false
    }

    fun generateComputerMove(currentPosition: String) {
        sendCommandToRunningEngine("position fen $currentPosition")
        sendCommandToRunningEngine("go movetime 1000")
        readEngineOutputJob = coroutineScope.launch {
            var mustExitLoop = false
            var moveLine: String? = null

            while (!mustExitLoop) {
                val nextEngineLine = readNextEngineOutput()
                if (nextEngineLine != null && nextEngineLine.startsWith("bestmove")) {
                    moveLine = nextEngineLine
                    mustExitLoop = true
                }
                delay(100)
            }

            val moveParts = moveLine!!.split(" ")
            val move = moveParts[1]

            computerMoveString = move
            newComputerMovePendingState = ComputerMovePendingState.NEW_CPU_MOVE_AVAILABLE
        }
    }

    if (gameInProgress) {
        abortGameIfNoEngineInstalled()
    }

    Layout(
        content = {
            SimpleButton(
                text = stringResource(R.string.new_game),
                vectorId = R.drawable.ic_start_flag
            ) {
                onNewGameRequest()
            }
            SimpleButton(
                text = stringResource(R.string.stop_game),
                vectorId = R.drawable.ic_stop
            ) {
                if (gameInProgress) {
                    confirmStopDialogOpen = true
                }
            }
            SimpleButton(
                text = stringResource(R.string.reverse_board),
                vectorId = R.drawable.ic_reverse,
            ) {
                boardReversed = !boardReversed
            }
            if (!gameInProgress) {
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
                whiteSideType = PlayerType.Computer,
                blackSideType = PlayerType.Human,
                newGameRequestState = newGameRequestState,
                newGameRequestProcessedCallback = { newGameRequestState = NewGameRequestState.NO_PENDING_REQUEST },
                userRequestStopGame = stopRequest,
                naturalGameEndCallback = { notifyUserGameFinished(it) },
                computerMoveRequestCallback = { generateComputerMove(it) },
                newComputerMovePendingState = newComputerMovePendingState,
                computerMoveString = computerMoveString,
                computerMoveProcessedCallback = {
                    newComputerMovePendingState = ComputerMovePendingState.NO_NEW_CPU_MOVE
                    readEngineOutputJob?.cancel()
                    readEngineOutputJob = null
                }
            )

            ConfirmStopGameDialog(
                isOpen = confirmStopDialogOpen,
                validateCallback = {
                    confirmStopDialogOpen = false
                    stopGame()
                },
                dismissCallback = { confirmStopDialogOpen = false }
            )

            SelectEngineDialog(isOpen = selectEngineDialogOpen, enginesList = enginesList,
                validateCallback = {
                    coroutineScope.launch {
                        executeInstalledEngine(
                            enginesFolder = enginesFolder,
                            index = it,
                            errorCallback = {
                                showInfiniteSnackbarAction(errorLaunchingEngineText)
                            })
                    }
                    selectEngineDialogOpen = false
                    doStartNewGame()
                }, dismissCallback = {
                    selectEngineDialogOpen = false
                })

        }
    ) { allMeasurable, constraints ->
        val boardSize = if (isLandscape) constraints.maxHeight else constraints.maxWidth
        val buttonsCount = if (!gameInProgress) 4 else 3

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
fun ConfirmStopGameDialog(
    isOpen: Boolean,
    validateCallback: () -> Unit,
    dismissCallback: () -> Unit
) {
    if (isOpen) {
        AlertDialog(onDismissRequest = { dismissCallback() },
            title = {
                Text(stringResource(R.string.confirm_stop_game_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.confirm_stop_game_message
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { validateCallback() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant)
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                Button(
                    onClick = { dismissCallback() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant)
                ) {
                    Text(stringResource(R.string.Cancel))
                }
            }
        )

    }
}

@Composable
fun SelectEngineDialog(
    isOpen: Boolean,
    enginesList: Array<String>,
    validateCallback: (Int) -> Unit,
    dismissCallback: () -> Unit
) {
    if (isOpen) {
        AlertDialog(onDismissRequest = { dismissCallback() },
            title = {
                Text(stringResource(R.string.select_engine_title))
            },
            text = {
                LazyColumn {
                    enginesList.mapIndexed { index, caption ->
                        item {
                            Button(onClick = {
                                validateCallback(index)
                            }) {
                                Text(text = caption)
                            }
                        }
                    }
                }
            },
            confirmButton = {

            },
            dismissButton = {
                Button(
                    onClick = { dismissCallback() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant)
                ) {
                    Text(stringResource(R.string.Cancel))
                }
            }
        )

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