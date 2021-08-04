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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.components.*
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class GamePageState {
    var boardReversed = false
    var gameInProgress = false
    var pendingNewGameRequest = false
    var pendingStopGameRequest = false
    var pendingSelectEngineDialog = false
    var promotionState = PendingPromotionData()
    var readEngineOutputJob: Job? = null
    var computerThinking = false
}

class GamePageViewModel : ViewModel() {
    var pageState = GamePageState()
    var boardState = DynamicBoardDataHandler()
    var movesElements = mutableListOf<MovesNavigatorElement>()
}

@Composable
fun GamePage(
    navController: NavController? = null,
    gamePageViewModel: GamePageViewModel = viewModel(),
) {

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val okString = stringResource(R.string.ok)

    val context = LocalContext.current

    var currentPosition by remember {
        mutableStateOf(gamePageViewModel.boardState.getCurrentPosition())
    }

    var boardReversed by remember {
        mutableStateOf(gamePageViewModel.pageState.boardReversed)
    }

    var gameInProgress by remember {
        mutableStateOf(gamePageViewModel.pageState.gameInProgress)
    }

    var pendingNewGameRequest by remember {
        mutableStateOf(gamePageViewModel.pageState.pendingNewGameRequest)
    }

    var pendingStopGameRequest by remember {
        mutableStateOf(gamePageViewModel.pageState.pendingStopGameRequest)
    }

    var pendingSelectEngineDialog by remember {
        mutableStateOf(gamePageViewModel.pageState.pendingSelectEngineDialog)
    }

    var promotionState by remember {
        mutableStateOf(gamePageViewModel.pageState.promotionState)
    }

    var computerThinking by remember {
        mutableStateOf(gamePageViewModel.pageState.computerThinking)
    }

    val isLandscape = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }

    val coroutineScope = rememberCoroutineScope()

    val checkmateWhiteText = stringResource(R.string.chessmate_white)
    val checkmateBlackText = stringResource(R.string.chessmate_black)
    val stalemateText = stringResource(R.string.stalemate)
    val threeFoldRepetitionText = stringResource(R.string.three_fold_repetition)
    val fiftyMovesText = stringResource(R.string.fifty_moves_rule_draw)
    val missingMaterialText = stringResource(R.string.missing_material_draw)
    val gameStoppedMessage = stringResource(R.string.user_stopped_game)

    val errorLaunchingEngineText = stringResource(R.string.error_launching_engine)
    val noInstalledEngineText = stringResource(R.string.no_installed_engine_error)

    val enginesFolder = File(context.filesDir, "engines")
    val enginesList = listInstalledEngines(enginesFolder)

    fun doStartNewGame() {
        gamePageViewModel.pageState.promotionState = PendingPromotionData()
        gamePageViewModel.boardState.newGame()
        gamePageViewModel.movesElements.clear()
        gamePageViewModel.movesElements.add(MoveNumber(text = "${gamePageViewModel.boardState.moveNumber()}."))
        currentPosition = gamePageViewModel.boardState.getCurrentPosition()
        gamePageViewModel.pageState.gameInProgress = true
        gameInProgress = true
    }

    fun showInfiniteSnackbarAction(text: String) {
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = text,
                actionLabel = okString,
                duration = SnackbarDuration.Indefinite
            )
        }

    }

    fun showMinutedSnackbarAction(text: String, duration: SnackbarDuration) {
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = text,
                duration = duration,
            )
        }

    }

    fun newGameRequest() {
        val noEngineInstalledLocally = enginesList.isEmpty()
        if (noEngineInstalledLocally) {
            showInfiniteSnackbarAction(noInstalledEngineText)
            return
        }
        val isInInitialPosition = gamePageViewModel.boardState.getCurrentPosition() == EMPTY_FEN
        if (isInInitialPosition) {
            gamePageViewModel.pageState.pendingSelectEngineDialog = true
            pendingSelectEngineDialog = true
        } else  {
            gamePageViewModel.pageState.pendingNewGameRequest = true
            pendingNewGameRequest = true
        }
    }

    fun stopGameRequest() {
        if (!gamePageViewModel.pageState.gameInProgress) return
        gamePageViewModel.pageState.pendingStopGameRequest = true
        pendingStopGameRequest = true
    }

    fun doStopCurrentGame() {
        if (!gamePageViewModel.pageState.gameInProgress) return
        stopCurrentRunningEngine()
        gamePageViewModel.pageState.computerThinking = false
        computerThinking = false
        gamePageViewModel.pageState.promotionState = PendingPromotionData()
        promotionState = gamePageViewModel.pageState.promotionState
        gamePageViewModel.pageState.gameInProgress = false
        gameInProgress = false
        showMinutedSnackbarAction(gameStoppedMessage, SnackbarDuration.Short)
    }

    fun handleNaturalEndgame() {
        if (!gamePageViewModel.pageState.gameInProgress) return
        val endedStatus = gamePageViewModel.boardState.getNaturalEndGameStatus()
        val message = when (endedStatus) {
            GameEndedStatus.CHECKMATE_WHITE -> checkmateWhiteText
            GameEndedStatus.CHECKMATE_BLACK -> checkmateBlackText
            GameEndedStatus.STALEMATE -> stalemateText
            GameEndedStatus.DRAW_THREE_FOLD_REPETITION -> threeFoldRepetitionText
            GameEndedStatus.DRAW_FIFTY_MOVES_RULE -> fiftyMovesText
            GameEndedStatus.DRAW_MISSING_MATERIAL -> missingMaterialText
            else -> null
        }
        message?.let { showMinutedSnackbarAction(message, SnackbarDuration.Long) }
        if (endedStatus != GameEndedStatus.NOT_ENDED) {
            stopCurrentRunningEngine()
            gamePageViewModel.pageState.computerThinking = false
            computerThinking = false
            gamePageViewModel.pageState.gameInProgress = false
            gameInProgress = false
        }
    }

    fun addMoveFanToHistory() {
        val lastMoveFan = gamePageViewModel.boardState.getLastMoveFan()
        gamePageViewModel.movesElements.add(HalfMoveSAN(text = lastMoveFan!!))
        if (gamePageViewModel.boardState.whiteTurn()) {
            gamePageViewModel.movesElements.add(MoveNumber(text = "${gamePageViewModel.boardState.moveNumber()}."))
        }
    }

    fun generateComputerMove(oldPosition: String) {
        gamePageViewModel.pageState.computerThinking = true
        computerThinking = true
        sendCommandToRunningEngine("position fen $oldPosition")
        sendCommandToRunningEngine("go movetime 1000")
        gamePageViewModel.pageState.readEngineOutputJob = coroutineScope.launch {
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

            gamePageViewModel.pageState.computerThinking = false
            computerThinking = false
            gamePageViewModel.pageState.readEngineOutputJob?.cancel()
            gamePageViewModel.pageState.readEngineOutputJob = null

            gamePageViewModel.boardState.makeMove(move)
            addMoveFanToHistory()
            currentPosition = gamePageViewModel.boardState.getCurrentPosition()
            handleNaturalEndgame()
        }
    }

    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.game_page)) }, actions = {
                    SimpleButton(
                        text = stringResource(R.string.new_game),
                        vectorId = R.drawable.ic_start_flag
                    ) {
                        newGameRequest()
                    }
                    SimpleButton(
                        text = stringResource(R.string.stop_game),
                        vectorId = R.drawable.ic_stop
                    ) {
                        stopGameRequest()
                    }
                    SimpleButton(
                        text = stringResource(R.string.reverse_board),
                        vectorId = R.drawable.ic_reverse,
                    ) {
                        gamePageViewModel.pageState.boardReversed =
                            !gamePageViewModel.pageState.boardReversed
                        boardReversed = gamePageViewModel.pageState.boardReversed
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
                })
            },
            content = {
                Surface(color = MaterialTheme.colors.background) {

                    Layout(
                        content = {
                            DynamicChessBoard(
                                whiteSideType = PlayerType.Human,
                                blackSideType = PlayerType.Computer,
                                reversed = boardReversed,
                                gameInProgress = gameInProgress,
                                position = currentPosition,
                                promotionState = promotionState,
                                isValidMoveCallback = {
                                    gamePageViewModel.boardState.isValidMove(it)
                                },
                                dndMoveCallback = {
                                    gamePageViewModel.boardState.makeMove(it)
                                    currentPosition =
                                        gamePageViewModel.boardState.getCurrentPosition()
                                    addMoveFanToHistory()
                                    handleNaturalEndgame()
                                },
                                promotionMoveCallback = {
                                    gamePageViewModel.boardState.makeMove(it)
                                    gamePageViewModel.pageState.promotionState =
                                        PendingPromotionData()
                                    promotionState = gamePageViewModel.pageState.promotionState
                                    currentPosition =
                                        gamePageViewModel.boardState.getCurrentPosition()
                                    addMoveFanToHistory()
                                    handleNaturalEndgame()
                                },
                                cancelPendingPromotionCallback = {
                                    gamePageViewModel.pageState.promotionState =
                                        PendingPromotionData()
                                },
                                setPendingPromotionCallback = {
                                    gamePageViewModel.pageState.promotionState = it
                                },
                                computerMoveRequestCallback = { generateComputerMove(it) },
                            )

                            MovesNavigator(elements = gamePageViewModel.movesElements.toTypedArray())

                            ConfirmNewGameDialog(
                                isOpen = pendingNewGameRequest,
                                validateCallback = {
                                    gamePageViewModel.pageState.pendingNewGameRequest = false
                                    gamePageViewModel.pageState.pendingSelectEngineDialog = true
                                    pendingNewGameRequest = false
                                    pendingSelectEngineDialog = true
                                },
                                dismissCallback = {
                                    gamePageViewModel.pageState.pendingNewGameRequest = false
                                    pendingNewGameRequest = false
                                })

                            ConfirmStopGameDialog(
                                isOpen = pendingStopGameRequest,
                                validateCallback = {
                                    gamePageViewModel.pageState.pendingStopGameRequest = false
                                    pendingStopGameRequest = false
                                    doStopCurrentGame()
                                },
                                dismissCallback = {
                                    gamePageViewModel.pageState.pendingStopGameRequest = false
                                    pendingStopGameRequest = false
                                })

                            SelectEngineDialog(
                                isOpen = pendingSelectEngineDialog,
                                enginesList = enginesList,
                                validateCallback = {
                                    gamePageViewModel.pageState.pendingSelectEngineDialog = false
                                    pendingSelectEngineDialog = false
                                    coroutineScope.launch {
                                        executeInstalledEngine(
                                            enginesFolder = enginesFolder,
                                            index = it,
                                            errorCallback = {
                                                scope.launch {
                                                    scaffoldState.snackbarHostState.showSnackbar(
                                                        message = errorLaunchingEngineText,
                                                        actionLabel = okString,
                                                        duration = SnackbarDuration.Indefinite
                                                    )
                                                }
                                                doStopCurrentGame()
                                            })
                                    }
                                    doStartNewGame()
                                },
                                dismissCallback = {
                                    gamePageViewModel.pageState.pendingSelectEngineDialog = false
                                    pendingSelectEngineDialog = false
                                })
                            if (computerThinking) {
                                CircularProgressIndicator(modifier = Modifier.size(50.dp))
                            }
                        }
                    ) { allMeasurable, constraints ->
                        val boardSize =
                            if (isLandscape) constraints.maxHeight else constraints.maxWidth
                        val allPlaceable = allMeasurable.mapIndexed { index, measurable ->
                            val isBoard = index == 0
                            val isCircularProgressBar =
                                index == allMeasurable.size - 1 && (gamePageViewModel.pageState.computerThinking
                                        )

                            if (isBoard || isCircularProgressBar) measurable.measure(
                                constraints.copy(
                                    minWidth = boardSize,
                                    minHeight = boardSize,
                                    maxWidth = boardSize,
                                    maxHeight = boardSize
                                )
                            )
                            else { // movesNavigator
                                val width =
                                    if (isLandscape) constraints.maxWidth - boardSize else constraints.maxWidth
                                val height =
                                    if (isLandscape) constraints.maxHeight else constraints.maxHeight - boardSize
                                measurable.measure(
                                    constraints.copy(
                                        minWidth = width,
                                        minHeight = height,
                                        maxWidth = width,
                                        maxHeight = height
                                    )
                                )
                            }
                        }

                        layout(constraints.maxWidth, constraints.maxHeight) {
                            fun placeStdComponent(placeable: Placeable, location: Int) {
                                if (isLandscape) {
                                    placeable.place(location, 0)
                                } else {
                                    placeable.place(0, location)
                                }
                            }

                            allPlaceable.forEachIndexed { index, placeable ->
                                val isBoard = index == 0
                                val isCircularProgressBar =
                                    index == allPlaceable.size - 1 && (gamePageViewModel.pageState.computerThinking
                                            )
                                if (isBoard || isCircularProgressBar) {
                                    placeStdComponent(placeable, 0)
                                } else { // movesNavigator
                                    val componentsGap = 5
                                    placeStdComponent(placeable, boardSize + componentsGap)
                                }
                            }
                        }
                    }


                }
            },
        )
    }
}

@Composable
fun ConfirmNewGameDialog(
    isOpen: Boolean,
    validateCallback: () -> Unit,
    dismissCallback: () -> Unit
) {
    if (isOpen) {
        AlertDialog(onDismissRequest = { dismissCallback() },
            title = {
                Text(stringResource(R.string.confirm_new_game_title))
            },
            text = {
                Text(stringResource(R.string.confirm_new_game_message))
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
    textSize: TextUnit = 12.sp,
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