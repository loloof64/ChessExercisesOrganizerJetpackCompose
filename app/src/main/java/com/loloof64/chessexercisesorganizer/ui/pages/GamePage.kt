package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alonsoruibal.chess.Board
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.core.PgnGameLoader
import com.loloof64.chessexercisesorganizer.ui.components.*
import com.loloof64.chessexercisesorganizer.ui.components.moves_navigator.*
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import com.loloof64.stockfish.StockfishLib
import kotlinx.coroutines.Job
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

class GamePageState {
    var boardReversed = false
    var gameInProgress = false
    var pendingNewGameRequest = false
    var pendingStopGameRequest = false
    var pendingSelectEngineDialog = false
    var promotionState = PendingPromotionData()
    var variationsSelectorData: VariationsSelectorData? = null
}

class GamePageViewModel : ViewModel() {
    var pageState = GamePageState()
    var boardState = DynamicBoardDataHandler()
    var movesElements = mutableListOf<MovesNavigatorElement>()
    var currentGame = PgnGameLoader()
    var currentSolution: List<MovesNavigatorElement> = listOf()
}

@Composable
fun GamePage(
    navController: NavController,
    gamePageViewModel: GamePageViewModel = viewModel(),
    stockfishLib: StockfishLib,
) {
    val cpuThinkingTimeoutMs = 2_000L

    val scaffoldState = rememberScaffoldState()

    var startPosition by rememberSaveable {
        mutableStateOf(gamePageViewModel.boardState.getCurrentPosition())
    }

    var currentPosition by remember {
        mutableStateOf(gamePageViewModel.boardState.getCurrentPosition())
    }

    var lastMoveArrow by remember {
        mutableStateOf(gamePageViewModel.boardState.getLastMoveArrow())
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

    var promotionState by remember {
        mutableStateOf(gamePageViewModel.pageState.promotionState)
    }

    var computerThinking by remember {
        mutableStateOf(false)
    }

    var readEngineOutputJob by remember {
        mutableStateOf<Job?>(null)
    }

    var variationsSelectorData by remember {
        mutableStateOf(gamePageViewModel.pageState.variationsSelectorData)
    }

    var highlightedHistoryItemIndex by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var whiteSideType by rememberSaveable {
        mutableStateOf(PlayerType.Human)
    }

    var blackSideType by rememberSaveable {
        mutableStateOf(PlayerType.Human)
    }

    var isInSolutionMode by rememberSaveable {
        mutableStateOf(false)
    }

    /* When going to next position, or last position,
        we want to match as many parenthesis as needed, in order to
        pass through variations starting at current level.
     */
    var selectedNodeVariationLevel by rememberSaveable {
        mutableStateOf(0)
    }

    var solutionAvailable by rememberSaveable {
        mutableStateOf(false)
    }

    var failedToLoadSolution by rememberSaveable {
        mutableStateOf(false)
    }

    var variationSelectionOpen by rememberSaveable {
        mutableStateOf(false)
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
    val gamesLoadingErrorMessage = stringResource(R.string.game_loading_error)
    val illegalStartPositionMessage = stringResource(R.string.illegal_start_position)

    val context = LocalContext.current

    fun showMinutedSnackbarAction(text: String, duration: SnackbarDuration) {
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = text,
                duration = duration,
            )
        }
    }

    fun updateMovesNavigatorSelection(nodeIndex: Int) {
        if (gameInProgress) return

        if (nodeIndex < 0) {
            gamePageViewModel.boardState.setCurrentPosition(startPosition)
            gamePageViewModel.boardState.setLastMoveArrow(null)

            currentPosition = gamePageViewModel.boardState.getCurrentPosition()
            lastMoveArrow = gamePageViewModel.boardState.getLastMoveArrow()

            highlightedHistoryItemIndex = null

            return
        }

        val currentNode =
            if (isInSolutionMode) gamePageViewModel.currentSolution[nodeIndex]
            else gamePageViewModel.movesElements[nodeIndex]

        val fen = currentNode.fen
        val lastMoveArrowData = currentNode.lastMoveArrowData

        if (fen != null) {
            gamePageViewModel.boardState.setCurrentPosition(fen)
        }
        gamePageViewModel.boardState.setLastMoveArrow(lastMoveArrowData)

        currentPosition = gamePageViewModel.boardState.getCurrentPosition()
        lastMoveArrow = gamePageViewModel.boardState.getLastMoveArrow()

        highlightedHistoryItemIndex = if (fen != null) nodeIndex else null
    }

    // Select last position in current variation.
    fun selectLastPosition() {
        if (!gameInProgress) {
            // There is at least the first move number element in history at this point.
            val noHistoryMove = !isInSolutionMode && (gamePageViewModel.movesElements.size < 2)
            if (noHistoryMove) return

            var currentNodeData = NodeSearchParam(
                index = highlightedHistoryItemIndex ?: -1,
                variationLevel = selectedNodeVariationLevel
            )
            while (true) {
                val searchResult = findNextMoveNode(
                    nodeData = currentNodeData,
                    historyMoves = if (isInSolutionMode) gamePageViewModel.currentSolution
                    else gamePageViewModel.movesElements,
                    selectedNodeVariationLevel = selectedNodeVariationLevel,
                )
                if (searchResult.isLastMoveNodeOfMainVariation || searchResult.hasJustMetCloseParenthesis) {
                    updateMovesNavigatorSelection(searchResult.index)
                    break
                } else currentNodeData = NodeSearchParam(
                    index = searchResult.index,
                    variationLevel = searchResult.variationLevel,
                )
            }
        }
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
            computerThinking = false
            gamePageViewModel.pageState.gameInProgress = false
            gameInProgress = false
            selectLastPosition()
        }
    }

    fun doStartNewGame() {
        try {
            val inputStream = context.assets.open("pgn/dummy_sample.pgn")
            val gamesFileContent = inputStream.bufferedReader().use { it.readText() }

            val gamesData = gamePageViewModel.currentGame.load(gamesFileContent = gamesFileContent)

            val selectedGameIndex = 15
            val selectedGame = gamesData[selectedGameIndex]

            try {
                val solutionHistory = buildHistoryFromPGNGame(selectedGame)

                if (solutionHistory.isNotEmpty()) {
                    gamePageViewModel.currentSolution = solutionHistory
                } else {
                    gamePageViewModel.currentSolution = listOf()
                }
                failedToLoadSolution = false
            } catch (ex: Exception) {
                gamePageViewModel.currentSolution = listOf()
                failedToLoadSolution = true

                println(ex)
            }

            solutionAvailable = gamePageViewModel.currentSolution.isNotEmpty()

            val startFen =
                if (selectedGame.tags.containsKey("FEN")) selectedGame.tags["FEN"]!! else Board.FEN_START_POSITION

            // First we ensure that position is valid when initializing the board
            gamePageViewModel.boardState.newGame(startFen)

            // Here we are fine to process the rest
            val blackStartGame = startFen.split(" ")[1] == "b"
            whiteSideType = if (blackStartGame) PlayerType.Computer else PlayerType.Human
            blackSideType = if (blackStartGame) PlayerType.Human else PlayerType.Computer
            gamePageViewModel.pageState.boardReversed =
                blackStartGame
            boardReversed = gamePageViewModel.pageState.boardReversed


            gamePageViewModel.pageState.promotionState = PendingPromotionData()
            gamePageViewModel.movesElements.clear()
            gamePageViewModel.movesElements.add(MoveNumber(text = "${gamePageViewModel.boardState.moveNumber()}.${if (blackStartGame) ".." else ""}"))
            currentPosition = gamePageViewModel.boardState.getCurrentPosition()
            startPosition = gamePageViewModel.boardState.getCurrentPosition()
            stockfishLib.sendCommand("ucinewgame")
            gamePageViewModel.boardState.clearLastMoveArrow()
            highlightedHistoryItemIndex = null
            lastMoveArrow = gamePageViewModel.boardState.getLastMoveArrow()

            isInSolutionMode = false

            gamePageViewModel.pageState.gameInProgress = true
            gameInProgress = true

            handleNaturalEndgame()
        } catch (ex: IllegalPositionException) {
            showMinutedSnackbarAction(illegalStartPositionMessage, SnackbarDuration.Short)
        } catch (ex: GamesLoadingException) {
            ex.printStackTrace()
            showMinutedSnackbarAction(gamesLoadingErrorMessage, SnackbarDuration.Short)
        }
    }

    fun newGameRequest() {
        val isInInitialPosition = gamePageViewModel.boardState.getCurrentPosition() == EMPTY_FEN
        if (isInInitialPosition) {
            doStartNewGame()
        } else {
            gamePageViewModel.pageState.pendingNewGameRequest = true
            pendingNewGameRequest = true
        }
    }

    fun stopGameRequest() {
        if (!gamePageViewModel.pageState.gameInProgress) return
        gamePageViewModel.pageState.pendingStopGameRequest = true
        pendingStopGameRequest = true
    }

    // Select first position of the game.
    fun selectFirstPosition() {
        if (!gameInProgress) {
            val fen = startPosition
            gamePageViewModel.boardState.setCurrentPosition(fen)

            currentPosition = gamePageViewModel.boardState.getCurrentPosition()
            lastMoveArrow = null
            highlightedHistoryItemIndex = null
        }
    }

    // Select previous position, can go up variation.
    fun selectPreviousPosition() {
        if (!gameInProgress) {
            // There is at least the first move number element in history at this point.
            val noHistoryMove = !isInSolutionMode && (gamePageViewModel.movesElements.size < 2)
            if (noHistoryMove) return

            if (highlightedHistoryItemIndex == null) {
                updateMovesNavigatorSelection(-1)
            } else {
                val searchResult = findPreviousMoveNode(
                    historyMoves = if (isInSolutionMode) gamePageViewModel.currentSolution
                    else gamePageViewModel.movesElements,
                    nodeData = NodeSearchParam(
                        index = highlightedHistoryItemIndex!!,
                        variationLevel = selectedNodeVariationLevel
                    ),
                    selectedNodeVariationLevel = selectedNodeVariationLevel,
                )
                updateMovesNavigatorSelection(searchResult.index)
            }
        }
    }

    // Select next position : if in a variation, cannot go further than the variation end node.
    fun selectNextPosition() {
        if (!gameInProgress) {
            // There is at least the first move number element in history at this point.
            val noHistoryMove = !isInSolutionMode && (gamePageViewModel.movesElements.size < 2)
            if (noHistoryMove) return

            val searchResult = findNextMoveNode(
                historyMoves = if (isInSolutionMode) gamePageViewModel.currentSolution
                else gamePageViewModel.movesElements,
                nodeData = NodeSearchParam(
                    // There is at least the first move number node
                    index = highlightedHistoryItemIndex ?: 0,
                    variationLevel = selectedNodeVariationLevel
                ),
                selectedNodeVariationLevel = selectedNodeVariationLevel,
            )
            if (searchResult.variationsToProcess.isNotEmpty()) {
                gamePageViewModel.pageState.variationsSelectorData =
                    VariationsSelectorData(
                        main = SingleVariationData(
                            text = searchResult.mainVariationMoveText!!,
                            historyIndex = searchResult.index,
                        ),
                        variations = searchResult.variationsToProcess.map {
                            SingleVariationData(
                                text = it.firstMoveText,
                                historyIndex = it.firstMoveIndex,
                            )
                        }
                    )
                variationsSelectorData = gamePageViewModel.pageState.variationsSelectorData
                variationSelectionOpen = true
            } else {
                gamePageViewModel.pageState.variationsSelectorData = null
                updateMovesNavigatorSelection(searchResult.index)
            }
        }
    }

    fun doStopCurrentGame() {
        if (!gamePageViewModel.pageState.gameInProgress) return
        computerThinking = false
        gamePageViewModel.pageState.promotionState = PendingPromotionData()
        promotionState = gamePageViewModel.pageState.promotionState
        gamePageViewModel.pageState.gameInProgress = false
        gameInProgress = false
        selectLastPosition()
        showMinutedSnackbarAction(gameStoppedMessage, SnackbarDuration.Short)
    }

    // This must be called after having played the move on the board !
    fun addMoveFanToHistory() {
        val lastMoveFan = gamePageViewModel.boardState.getLastMoveFan()
        val lastMoveFen = gamePageViewModel.boardState.getCurrentPosition()
        val localLastMoveArrow = gamePageViewModel.boardState.getLastMoveArrow()
        gamePageViewModel.movesElements.add(
            HalfMoveSAN(
                text = lastMoveFan,
                fen = lastMoveFen,
                lastMoveArrowData = localLastMoveArrow
            )
        )
        if (gamePageViewModel.boardState.whiteTurn()) {
            gamePageViewModel.movesElements.add(MoveNumber(text = "${gamePageViewModel.boardState.moveNumber()}."))
        }
    }

    fun tryToSelectPosition(positionData: Triple<String, MoveData, Int>) {
        if (!gameInProgress) {
            gamePageViewModel.boardState.setCurrentPosition(positionData.first)
            gamePageViewModel.boardState.setLastMoveArrow(positionData.second)
            currentPosition = gamePageViewModel.boardState.getCurrentPosition()
            lastMoveArrow = gamePageViewModel.boardState.getLastMoveArrow()
            highlightedHistoryItemIndex = positionData.third
        }
    }

    fun generateComputerMove(oldPosition: String) {
        if (computerThinking) return
        computerThinking = true

        stockfishLib.sendCommand("position fen $oldPosition")
        stockfishLib.sendCommand("go infinite")

        readEngineOutputJob = coroutineScope.launch {
            var mustExitLoop = false

            while (!mustExitLoop) {
                val nextEngineLine = stockfishLib.readNextOutput()

                if (nextEngineLine.startsWith("bestmove")) {
                    val moveParts = nextEngineLine!!.split(" ")
                    val move = moveParts[1]

                    readEngineOutputJob?.cancel()
                    readEngineOutputJob = null
                    computerThinking = false

                    if (gameInProgress) {
                        gamePageViewModel.boardState.makeMove(move)
                        gamePageViewModel.boardState.setLastMoveArrow(MoveData.parse(move)!!)
                        addMoveFanToHistory()
                        currentPosition = gamePageViewModel.boardState.getCurrentPosition()
                        lastMoveArrow = gamePageViewModel.boardState.getLastMoveArrow()
                        handleNaturalEndgame()
                    }

                    mustExitLoop = true
                }
                delay(100)
            }
        }

        coroutineScope.launch {
            delay(cpuThinkingTimeoutMs)
            if (readEngineOutputJob?.isActive == true) {
                stockfishLib.sendCommand("stop")
            }
        }
    }

    fun handleStandardMoveDoneOnBoard(it: MoveData) {
        gamePageViewModel.boardState.makeMove(it.toString())
        gamePageViewModel.boardState.setLastMoveArrow(it)
        lastMoveArrow = gamePageViewModel.boardState.getLastMoveArrow()
        currentPosition =
            gamePageViewModel.boardState.getCurrentPosition()
        addMoveFanToHistory()
        handleNaturalEndgame()
    }

    fun handlePromotionMoveDoneOnBoard(it: MoveData) {
        gamePageViewModel.boardState.makeMove(it.toString())
        gamePageViewModel.boardState.setLastMoveArrow(it)
        gamePageViewModel.pageState.promotionState =
            PendingPromotionData()
        promotionState = gamePageViewModel.pageState.promotionState
        lastMoveArrow = gamePageViewModel.boardState.getLastMoveArrow()
        currentPosition =
            gamePageViewModel.boardState.getCurrentPosition()
        addMoveFanToHistory()
        handleNaturalEndgame()
    }

    fun toggleHistoryMode() {
        val isInInitialPosition = gamePageViewModel.boardState.getCurrentPosition() == EMPTY_FEN
        val modeSelectionNotActive = isInInitialPosition || gameInProgress
        if (modeSelectionNotActive) return

        val noSolutionAvailable = gamePageViewModel.currentSolution.isEmpty()
        if (noSolutionAvailable) return

        gamePageViewModel.boardState.clearLastMoveArrow()
        gamePageViewModel.boardState.setCurrentPosition(startPosition)

        lastMoveArrow = gamePageViewModel.boardState.getLastMoveArrow()
        currentPosition = gamePageViewModel.boardState.getCurrentPosition()

        highlightedHistoryItemIndex = null
        selectedNodeVariationLevel = 0
        isInSolutionMode = !isInSolutionMode
    }

    fun manuallyUpdateHistoryNode() {
        if (highlightedHistoryItemIndex == null) return
        val historyNodes = if (isInSolutionMode) gamePageViewModel.currentSolution
        else gamePageViewModel.movesElements

        val currentNode = historyNodes[highlightedHistoryItemIndex!!]
        gamePageViewModel.boardState.setCurrentPosition(currentNode.fen!!)
        gamePageViewModel.boardState.setLastMoveArrow(currentNode.lastMoveArrowData)

        lastMoveArrow = gamePageViewModel.boardState.getLastMoveArrow()
        currentPosition = gamePageViewModel.boardState.getCurrentPosition()
    }

    fun selectMainVariation() {
        highlightedHistoryItemIndex = variationsSelectorData!!.main.historyIndex
        manuallyUpdateHistoryNode()

        variationSelectionOpen = false
        gamePageViewModel.pageState.variationsSelectorData = null
    }

    fun selectSubVariation(variationIndex: Int) {
        highlightedHistoryItemIndex =
            variationsSelectorData!!.variations[variationIndex].historyIndex
        manuallyUpdateHistoryNode()

        variationSelectionOpen = false
        gamePageViewModel.pageState.variationsSelectorData = null
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
                })
            },
            content = {
                Surface(color = MaterialTheme.colors.background) {

                    Layout(
                        content = {
                            DynamicChessBoard(
                                whiteSideType = whiteSideType,
                                blackSideType = blackSideType,
                                reversed = boardReversed,
                                lastMoveArrow = lastMoveArrow,
                                gameInProgress = gameInProgress,
                                position = currentPosition,
                                promotionState = promotionState,
                                isValidMoveCallback = {
                                    gamePageViewModel.boardState.isValidMove(it)
                                },
                                dndMoveCallback = {
                                    handleStandardMoveDoneOnBoard(it)
                                },
                                promotionMoveCallback = {
                                    handlePromotionMoveDoneOnBoard(it)
                                },
                                cancelPendingPromotionCallback = {
                                    gamePageViewModel.pageState.promotionState =
                                        PendingPromotionData()
                                },
                                setPendingPromotionCallback = {
                                    gamePageViewModel.pageState.promotionState = it
                                    promotionState = gamePageViewModel.pageState.promotionState
                                },
                                computerMoveRequestCallback = {
                                    if (!computerThinking) {
                                        generateComputerMove(it)
                                    }
                                },
                            )

                            val isInInitialPosition =
                                gamePageViewModel.boardState.getCurrentPosition() == EMPTY_FEN
                            val modeSelectionActive = !isInInitialPosition && !gameInProgress
                            val elements =
                                if (modeSelectionActive && isInSolutionMode)
                                    gamePageViewModel.currentSolution.toTypedArray()
                                else gamePageViewModel.movesElements.toTypedArray()

                            MovesNavigator(
                                elements = elements,
                                mustBeVisibleByDefaultElementIndex =
                                if (gameInProgress) elements.size.dec() else highlightedHistoryItemIndex,
                                highlightedItemIndex = highlightedHistoryItemIndex,
                                elementSelectionRequestCallback = {
                                    tryToSelectPosition(it)
                                },
                                handleFirstPositionRequest = { selectFirstPosition() },
                                handleLastPositionRequest = { selectLastPosition() },
                                handlePreviousPositionRequest = { selectPreviousPosition() },
                                handleNextPositionRequest = { selectNextPosition() },
                                historyModeToggleRequestHandler = {
                                    toggleHistoryMode()
                                },
                                isInSolutionMode = isInSolutionMode,
                                modeSelectionActive = modeSelectionActive && solutionAvailable,
                                failedToLoadSolution = failedToLoadSolution,
                            )

                            DropdownMenu(
                                expanded = variationSelectionOpen,
                                onDismissRequest = { },
                                offset = DpOffset(0.dp, 0.dp),
                            ) {
                                DropdownMenuItem(onClick = { selectMainVariation() }) {
                                    Text(
                                        text = variationsSelectorData!!.main.text,
                                        modifier = Modifier
                                            .background(Color.Green)
                                            .fillMaxSize(),
                                        color = Color.Blue,
                                        style = MaterialTheme.typography.body1,
                                        fontSize = 28.sp,
                                    )
                                }
                                variationsSelectorData!!.variations.mapIndexed { index, elt ->
                                    DropdownMenuItem(onClick = { selectSubVariation(index) }) {
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

                            ConfirmNewGameDialog(
                                isOpen = pendingNewGameRequest,
                                validateCallback = {
                                    gamePageViewModel.pageState.pendingNewGameRequest = false
                                    gamePageViewModel.pageState.pendingSelectEngineDialog = true
                                    pendingNewGameRequest = false
                                    doStartNewGame()
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
                                index == allMeasurable.size - 1 && computerThinking


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
                                    index == allPlaceable.size - 1 && computerThinking

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