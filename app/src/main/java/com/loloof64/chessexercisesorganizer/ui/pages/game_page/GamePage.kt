package com.loloof64.chessexercisesorganizer.ui.pages.game_page

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.loloof64.chessexercisesorganizer.MyApplication
import com.loloof64.chessexercisesorganizer.NavHostRoutes
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.components.DynamicChessBoard
import com.loloof64.chessexercisesorganizer.ui.components.EMPTY_FEN
import com.loloof64.chessexercisesorganizer.ui.components.GameEndedStatus
import com.loloof64.chessexercisesorganizer.ui.components.IllegalPositionException
import com.loloof64.chessexercisesorganizer.ui.components.moves_navigator.MovesNavigator
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SingleVariationData(
    val text: String,
    val historyIndex: Int,
)

data class VariationsSelectorData(
    val main: SingleVariationData,
    val variations: List<SingleVariationData>,
)

@Composable
fun GamePage(
    navController: NavController,
    modifier: Modifier = Modifier,
    gameId: Int,
    gamePageViewModel: GamePageViewModel = viewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    val gamesList = (context.applicationContext as MyApplication).gamesFromFileExtractorUseCase.currentGames()
    val noGameText = stringResource(R.string.no_game_in_pgn)

    fun getGoalText(): String {
        val whiteCheckmateRegex = """#(\d+)-0""".toRegex()
        val blackCheckmateRegex = """0-#(\d+)""".toRegex()

        val goalTypeText = gamesList?.get(gameId)?.tags?.get("Goal") ?: ""
        val checkmateMoves = when {
            whiteCheckmateRegex.matches(goalTypeText) -> Integer.parseInt(whiteCheckmateRegex.matchEntire(goalTypeText)!!.groupValues[1])
            blackCheckmateRegex.matches(goalTypeText) -> Integer.parseInt(blackCheckmateRegex.matchEntire(goalTypeText)!!.groupValues[1])
            else -> -1
        }
        return when {
            goalTypeText == "1-0" -> context.getString(R.string.white_should_win)
            goalTypeText == "0-1" -> context.getString(R.string.black_should_win)
            goalTypeText == "1/2-1/2" -> context.getString(R.string.it_should_be_draw)
            whiteCheckmateRegex.matches(goalTypeText) -> context.getString(R.string.white_should_checkmate, checkmateMoves)
            blackCheckmateRegex.matches(goalTypeText) -> context.getString(R.string.black_should_checkmate, checkmateMoves)
            else -> ""
        }
    }

    gamesList?.let {
        gamePageViewModel.setGamesList(it)

        var selectedIndex = gameId
        if (selectedIndex < 0) selectedIndex = 0
        if (selectedIndex > gamesList.size) selectedIndex = gamesList.size - 1

        try {
            val selectedGame = it[selectedIndex]
            gamePageViewModel.setSelectedGame(selectedGame)
        }
        catch (ex: IndexOutOfBoundsException) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(noGameText)
            }
        }
    }

    val isLandscape = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }

    val uiState by gamePageViewModel.uiState.collectAsState(Dispatchers.Main.immediate)

    val checkmateWhiteText = stringResource(R.string.chessmate_white)
    val checkmateBlackText = stringResource(R.string.chessmate_black)
    val stalemateText = stringResource(R.string.stalemate)
    val threeFoldRepetitionText = stringResource(R.string.three_fold_repetition)
    val fiftyMovesText = stringResource(R.string.fifty_moves_rule_draw)
    val missingMaterialText = stringResource(R.string.missing_material_draw)
    val gameStoppedMessage = stringResource(R.string.user_stopped_game)
    val illegalStartPositionMessage = stringResource(R.string.illegal_start_position)
    val arrowBackDescription = stringResource(R.string.arrow_back_button)

    fun doGoBackHome() {
        navController.navigate(NavHostRoutes.gamesListPage) {
            popUpTo(NavHostRoutes.gamePage) {
                inclusive = true
            }
        }
    }

    fun showMinutedSnackBarAction(text: String, duration: SnackbarDuration) {
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = text,
                duration = duration,
            )
        }
    }

    fun handleNaturalEndgame() {
        val message = when (gamePageViewModel.getNaturalGameEndedStatus()) {
            GameEndedStatus.CHECKMATE_WHITE -> checkmateWhiteText
            GameEndedStatus.CHECKMATE_BLACK -> checkmateBlackText
            GameEndedStatus.STALEMATE -> stalemateText
            GameEndedStatus.DRAW_THREE_FOLD_REPETITION -> threeFoldRepetitionText
            GameEndedStatus.DRAW_FIFTY_MOVES_RULE -> fiftyMovesText
            GameEndedStatus.DRAW_MISSING_MATERIAL -> missingMaterialText
            else -> null
        }
        message?.let {
            gamePageViewModel.doStopCurrentGame()
            showMinutedSnackBarAction(message, SnackbarDuration.Long)
        }
    }

    fun doStartNewGame() {
        try {
            gamePageViewModel.doStartNewGame()
            handleNaturalEndgame()
        } catch (ex: IllegalPositionException) {
            showMinutedSnackBarAction(illegalStartPositionMessage, SnackbarDuration.Short)
        }
    }

    fun doStopCurrentGame() {
        if (gamePageViewModel.doStopCurrentGame()) {
            showMinutedSnackBarAction(gameStoppedMessage, SnackbarDuration.Short)
        }
    }

    @Composable
    fun variationSelectionDropDownComponent() = DropdownMenu(
        expanded = uiState.interfaceState.variationSelectionOpen,
        onDismissRequest = {
            gamePageViewModel.cancelVariationSelection()
        },
    ) {
        DropdownMenuItem(onClick = {
            gamePageViewModel.selectMainVariation()
        }) {
            Text(
                text = uiState.interfaceState.variationsSelectorData!!.main.text,
                modifier = Modifier
                    .background(Color.Green)
                    .fillMaxSize(),
                color = Color.Blue,
                style = MaterialTheme.typography.body1,
                fontSize = 28.sp,
            )
        }
        Divider()
        uiState.interfaceState.variationsSelectorData!!.variations.mapIndexed { index, elt ->
            DropdownMenuItem(onClick = { gamePageViewModel.selectSubVariation(index) }) {
                Text(
                    text = elt.text,
                    modifier = Modifier
                        .background(Color(0xFFF97916))
                        .fillMaxSize(),
                    color = Color.Blue,
                    style = MaterialTheme.typography.body1,
                    fontSize = 28.sp,
                )
            }
        }
    }

    @Composable
    fun chessBoardComponent(modifier: Modifier = Modifier) {
        DynamicChessBoard(
            modifier = modifier,
            whiteSideType = uiState.interfaceState.whiteSideType,
            blackSideType = uiState.interfaceState.blackSideType,
            reversed = uiState.interfaceState.boardReversed,
            lastMoveArrow = uiState.chessState.lastMoveArrow,
            gameInProgress = uiState.interfaceState.gameInProgress,
            position = uiState.chessState.boardPosition,
            promotionState = uiState.interfaceState.promotionState,
            isValidMoveCallback = {
                gamePageViewModel.isValidMove(it)
            },
            dndMoveCallback = {
                gamePageViewModel.handleStandardMoveDoneOnBoard(it)
                handleNaturalEndgame()

            },
            promotionMoveCallback = {
                gamePageViewModel.handlePromotionMoveDoneOnBoard(it)
                handleNaturalEndgame()
            },
            cancelPendingPromotionCallback = {
                gamePageViewModel.clearPromotionData()
            },
            setPendingPromotionCallback = {
                gamePageViewModel.changePromotionData(it)
            },
            computerMoveRequestCallback = {
                gamePageViewModel.generateComputerMove(it)
                coroutineScope.launch {
                    delay(cpuThinkingTimeMs)
                    handleNaturalEndgame()
                }
            },
        )
    }

    @Composable
    fun historyComponent() {
        val isInInitialPosition =
            uiState.chessState.boardPosition == EMPTY_FEN
        val modeSelectionActive = !isInInitialPosition && !uiState.interfaceState.gameInProgress
        val elements =
            if (modeSelectionActive && uiState.interfaceState.isInSolutionMode)
                uiState.chessState.gameSolution.toTypedArray()
            else uiState.chessState.playedGameHistory.toTypedArray()

        MovesNavigator(
            elements = elements,
            mustBeVisibleByDefaultElementIndex =
            if (uiState.interfaceState.gameInProgress) elements.size.dec() else uiState.interfaceState.highlightedHistoryItemIndex,
            highlightedItemIndex = uiState.interfaceState.highlightedHistoryItemIndex,
            elementSelectionRequestCallback = {
                gamePageViewModel.tryToSelectPosition(it)
            },
            handleFirstPositionRequest = {gamePageViewModel.selectFirstPosition()},
            handleLastPositionRequest = {gamePageViewModel.selectLastPosition()},
            handlePreviousPositionRequest = {gamePageViewModel.selectPreviousPosition()},
            handleNextPositionRequest = {gamePageViewModel.selectNextPosition()},
            historyModeToggleRequestHandler = {gamePageViewModel.toggleHistoryMode()},
            isInSolutionMode = uiState.interfaceState.isInSolutionMode,
            modeSelectionActive = modeSelectionActive && uiState.interfaceState.solutionAvailable,
            failedToLoadSolution = uiState.interfaceState.failedToLoadSolution,
        )
    }

    @Composable
    fun dialogs() {
        val newGameDialogTitle = stringResource(R.string.confirm_new_game_title)
        val newGameDialogMessage = stringResource(R.string.confirm_new_game_message)
        val stopDialogTitle = stringResource(R.string.confirm_stop_game_title)
        val stopDialogMessage = stringResource(R.string.confirm_stop_game_message)
        val exitPageDialogTitle = stringResource(R.string.confirm_exit_game_page_title)
        val exitPageDialogMessage = stringResource(R.string.confirm_exit_game_page_message)

        ConfirmDialog(
            isOpen = uiState.interfaceState.pendingExitPageRequest,
            title = exitPageDialogTitle,
            message = exitPageDialogMessage,
            validateCallback = {
                gamePageViewModel.cancelExitPageConfirmation()
                doGoBackHome()
            },
            dismissCallback = {
                gamePageViewModel.cancelExitPageConfirmation()
            })

        ConfirmDialog(
            isOpen = uiState.interfaceState.pendingNewGameRequest,
            title = newGameDialogTitle,
            message = newGameDialogMessage,
            validateCallback = {
                gamePageViewModel.cancelNewGameConfirmation()
                doStartNewGame()
            },
            dismissCallback = {
                gamePageViewModel.cancelNewGameConfirmation()
            })

        ConfirmDialog(
            isOpen = uiState.interfaceState.pendingStopGameRequest,
            title = stopDialogTitle,
            message = stopDialogMessage,
            validateCallback = {
                gamePageViewModel.cancelStopGameConfirmation()
                doStopCurrentGame()
            },
            dismissCallback = {
                gamePageViewModel.cancelStopGameConfirmation()
            })

    }

    @Composable
    fun topAppBarComponents() {
        SimpleButton(
            text = stringResource(R.string.new_game),
            vectorId = R.drawable.ic_start_flags
        ) {
            gamePageViewModel.newGameRequest()
        }
        SimpleButton(
            text = stringResource(R.string.stop_game),
            vectorId = R.drawable.ic_stop
        ) {
            gamePageViewModel.stopGameRequest()
        }
        SimpleButton(
            text = stringResource(R.string.reverse_board),
            vectorId = R.drawable.ic_reverse,
        ) {
            gamePageViewModel.toggleBoardReversed()
        }
    }

    val goalText = getGoalText()

    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.game_page)) },
                    actions = {
                        topAppBarComponents()
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (!gamePageViewModel.handleGoBackRequest()) {
                                doGoBackHome()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = arrowBackDescription,
                            )
                        }
                    }
                )
            },
            content = {
                Surface(color = MaterialTheme.colors.background) {
                    if (isLandscape) {
                        val boardSize = screenHeight * 0.85f
                        Row(modifier = modifier.fillMaxSize()) {
                            Box(modifier = Modifier.size(boardSize)) {
                                chessBoardComponent(modifier = Modifier.size(boardSize))
                                if (uiState.interfaceState.computerThinking) {
                                    CircularProgressIndicator(modifier = Modifier.size(boardSize))
                                }
                            }
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (goalText.isNotEmpty()) {
                                    Text(goalText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.fillMaxSize()) {
                                    historyComponent()
                                    variationSelectionDropDownComponent()
                                }
                                dialogs()
                            }
                            BackHandler {
                                if (!gamePageViewModel.handleGoBackRequest()) {
                                    doGoBackHome()
                                }
                            }
                        }
                    }
                    else {
                        Column(modifier = modifier.fillMaxSize()) {
                            Box(modifier = Modifier.size(screenWidth)) {
                                chessBoardComponent(modifier = Modifier.size(screenWidth))
                                if (uiState.interfaceState.computerThinking) {
                                    CircularProgressIndicator(modifier = Modifier.size(screenWidth))
                                }
                            }
                            if (goalText.isNotEmpty()) {
                                Text(goalText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                historyComponent()
                                variationSelectionDropDownComponent()
                            }
                            dialogs()
                            BackHandler {
                                if (!gamePageViewModel.handleGoBackRequest()) {
                                    doGoBackHome()
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
fun ConfirmDialog(
    isOpen: Boolean,
    title: String,
    message: String,
    validateCallback: () -> Unit,
    dismissCallback: () -> Unit
) {
    if (isOpen) {
        AlertDialog(onDismissRequest = { dismissCallback() },
            title = {
                Text(title)
            },
            text = {
                Text(message)
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
    SimpleButton(text = "New", vectorId = R.drawable.ic_start_flags) {

    }
}