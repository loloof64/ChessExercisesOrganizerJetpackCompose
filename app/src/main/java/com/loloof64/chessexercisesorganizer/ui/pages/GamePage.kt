package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.loloof64.chessexercisesorganizer.MyApplication
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
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

class GamePageInterfaceState {
    var boardReversed = false
    var gameInProgress = false
    var pendingNewGameRequest = false
    var pendingStopGameRequest = false
    var pendingExitPageRequest = false
    var pendingSelectEngineDialog = false
    var promotionState = PendingPromotionData()
    var variationsSelectorData: VariationsSelectorData? = null
    var highlightedHistoryItemIndex: Int? = null
    var whiteSideType = PlayerType.Human
    var blackSideType = PlayerType.Human
    var isInSolutionMode = false
    var selectedNodeVariationLevel = 0
    var solutionAvailable = false
    var failedToLoadSolution = false
    var variationSelectionOpen = false
}

class GamePageChessState {
    var gamesList: List<PGNGame> = listOf()
    var boardState = DynamicBoardDataHandler()
    var playedGameHistory = mutableListOf<MovesNavigatorElement>()
    var gameSolution: List<MovesNavigatorElement> = listOf()
    var selectedGame = PGNGame(tags = mutableMapOf(), moves = null)
    val stockfishLib = StockfishLib()
}

class GamePageViewModel : ViewModel() {
    var interfaceState = GamePageInterfaceState()
    var chessState = GamePageChessState()

    fun isWhiteTurn(): Boolean {
        return chessState.boardState.whiteTurn()
    }

    override fun onCleared() {
        super.onCleared()
        chessState.stockfishLib.sendCommand("quit")
    }
}

@Composable
fun GamePage(
    navController: NavController,
    gamePageViewModel: GamePageViewModel = viewModel(),
) {
    val cpuThinkingTimeoutMs = 2_000L

    val context = LocalContext.current

    val gamesList = (context.applicationContext as MyApplication).gamesFromFileExtractorUseCase.currentGames()

    var failedToLoadSolution by remember {
        mutableStateOf(gamePageViewModel.interfaceState.failedToLoadSolution)
    }

    gamesList?.let {
        gamePageViewModel.chessState.gamesList = it

        val selectedGame = it[0]
        gamePageViewModel.chessState.selectedGame = selectedGame

        try {
            val solutionHistory = buildHistoryFromPGNGame(selectedGame)

            if (solutionHistory.isNotEmpty()) {
                gamePageViewModel.chessState.gameSolution = solutionHistory
            } else {
                gamePageViewModel.chessState.gameSolution = listOf()
            }
            gamePageViewModel.interfaceState.failedToLoadSolution = false
            failedToLoadSolution = gamePageViewModel.interfaceState.failedToLoadSolution
        } catch (ex: Exception) {
            gamePageViewModel.chessState.gameSolution = listOf()
            gamePageViewModel.interfaceState.failedToLoadSolution = true
            failedToLoadSolution = gamePageViewModel.interfaceState.failedToLoadSolution

            println(ex)
        }
    }

    val selectedGame by remember {
        mutableStateOf(gamePageViewModel.chessState.selectedGame)
    }

    val scaffoldState = rememberScaffoldState()

    var startPosition by rememberSaveable {
        mutableStateOf(gamePageViewModel.chessState.boardState.getCurrentPosition())
    }

    var currentPosition by remember {
        mutableStateOf(gamePageViewModel.chessState.boardState.getCurrentPosition())
    }

    var lastMoveArrow by remember {
        mutableStateOf(gamePageViewModel.chessState.boardState.getLastMoveArrow())
    }

    var boardReversed by remember {
        mutableStateOf(gamePageViewModel.interfaceState.boardReversed)
    }

    var gameInProgress by remember {
        mutableStateOf(gamePageViewModel.interfaceState.gameInProgress)
    }

    var pendingNewGameRequest by remember {
        mutableStateOf(gamePageViewModel.interfaceState.pendingNewGameRequest)
    }

    var pendingStopGameRequest by remember {
        mutableStateOf(gamePageViewModel.interfaceState.pendingStopGameRequest)
    }

    var promotionState by remember {
        mutableStateOf(gamePageViewModel.interfaceState.promotionState)
    }

    var computerThinking by remember {
        mutableStateOf(false)
    }

    var readEngineOutputJob by remember {
        mutableStateOf<Job?>(null)
    }

    var variationsSelectorData by remember {
        mutableStateOf(gamePageViewModel.interfaceState.variationsSelectorData)
    }

    var highlightedHistoryItemIndex by remember {
        mutableStateOf(gamePageViewModel.interfaceState.highlightedHistoryItemIndex)
    }

    var whiteSideType by remember {
        mutableStateOf(gamePageViewModel.interfaceState.whiteSideType)
    }

    var blackSideType by remember {
        mutableStateOf(gamePageViewModel.interfaceState.blackSideType)
    }

    var isInSolutionMode by remember {
        mutableStateOf(gamePageViewModel.interfaceState.isInSolutionMode)
    }

    /* When going to next position, or last position,
        we want to match as many parenthesis as needed, in order to
        pass through variations starting at current level.
     */
    var selectedNodeVariationLevel by remember {
        mutableStateOf(gamePageViewModel.interfaceState.selectedNodeVariationLevel)
    }

    var solutionAvailable by remember {
        mutableStateOf(gamePageViewModel.interfaceState.solutionAvailable)
    }

    var variationSelectionOpen by remember {
        mutableStateOf(gamePageViewModel.interfaceState.variationSelectionOpen)
    }

    var pendingExitPageRequest by remember {
        mutableStateOf(gamePageViewModel.interfaceState.pendingExitPageRequest)
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
    val illegalStartPositionMessage = stringResource(R.string.illegal_start_position)
    val arrowBackDescription = stringResource(R.string.arrow_back_button)

    fun showMinutedSnackBarAction(text: String, duration: SnackbarDuration) {
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
            gamePageViewModel.chessState.boardState.setCurrentPosition(startPosition)
            gamePageViewModel.chessState.boardState.setLastMoveArrow(null)

            currentPosition = gamePageViewModel.chessState.boardState.getCurrentPosition()
            lastMoveArrow = gamePageViewModel.chessState.boardState.getLastMoveArrow()

            gamePageViewModel.interfaceState.highlightedHistoryItemIndex = null
            highlightedHistoryItemIndex = gamePageViewModel.interfaceState.highlightedHistoryItemIndex

            return
        }

        val currentNode =
            if (isInSolutionMode) gamePageViewModel.chessState.gameSolution[nodeIndex]
            else gamePageViewModel.chessState.playedGameHistory[nodeIndex]

        val fen = currentNode.fen
        val lastMoveArrowData = currentNode.lastMoveArrowData

        if (fen != null) {
            gamePageViewModel.chessState.boardState.setCurrentPosition(fen)
        }
        gamePageViewModel.chessState.boardState.setLastMoveArrow(lastMoveArrowData)

        currentPosition = gamePageViewModel.chessState.boardState.getCurrentPosition()
        lastMoveArrow = gamePageViewModel.chessState.boardState.getLastMoveArrow()

        gamePageViewModel.interfaceState.highlightedHistoryItemIndex =
            if (fen != null) nodeIndex else null
        highlightedHistoryItemIndex = gamePageViewModel.interfaceState.highlightedHistoryItemIndex
    }

    // Select last position in current variation.
    fun selectLastPosition() {
        if (!gameInProgress) {
            // There is at least the first move number element in history at this point.
            val noHistoryMove = !isInSolutionMode && (gamePageViewModel.chessState.playedGameHistory.size < 2)
            if (noHistoryMove) return

            var currentNodeData = NodeSearchParam(
                index = highlightedHistoryItemIndex ?: -1,
                variationLevel = selectedNodeVariationLevel
            )
            while (true) {
                val searchResult = findNextMoveNode(
                    nodeData = currentNodeData,
                    historyMoves = if (isInSolutionMode) gamePageViewModel.chessState.gameSolution
                    else gamePageViewModel.chessState.playedGameHistory,
                    selectedNodeVariationLevel = selectedNodeVariationLevel,
                )
                if (searchResult.isLastMoveNodeOfMainVariation || searchResult.hasJustMetCloseParenthesis) {
                    updateMovesNavigatorSelection(searchResult.index)
                    gamePageViewModel.interfaceState.highlightedHistoryItemIndex = searchResult.index
                    gamePageViewModel.interfaceState.selectedNodeVariationLevel =
                        searchResult.variationLevel
                    break
                } else currentNodeData = NodeSearchParam(
                    index = searchResult.index,
                    variationLevel = searchResult.variationLevel,
                )
            }
        }
    }

    fun handleNaturalEndgame() {
        if (!gamePageViewModel.interfaceState.gameInProgress) return
        val endedStatus = gamePageViewModel.chessState.boardState.getNaturalEndGameStatus()
        val message = when (endedStatus) {
            GameEndedStatus.CHECKMATE_WHITE -> checkmateWhiteText
            GameEndedStatus.CHECKMATE_BLACK -> checkmateBlackText
            GameEndedStatus.STALEMATE -> stalemateText
            GameEndedStatus.DRAW_THREE_FOLD_REPETITION -> threeFoldRepetitionText
            GameEndedStatus.DRAW_FIFTY_MOVES_RULE -> fiftyMovesText
            GameEndedStatus.DRAW_MISSING_MATERIAL -> missingMaterialText
            else -> null
        }
        message?.let { showMinutedSnackBarAction(message, SnackbarDuration.Long) }
        if (endedStatus != GameEndedStatus.NOT_ENDED) {
            computerThinking = false
            gamePageViewModel.interfaceState.gameInProgress = false
            gameInProgress = false
            selectLastPosition()
        }
    }

    fun doStartNewGame() {
        try {
            gamePageViewModel.interfaceState.solutionAvailable =
                gamePageViewModel.chessState.gameSolution.isNotEmpty()
            solutionAvailable = gamePageViewModel.interfaceState.solutionAvailable

            val startFen =
                if (selectedGame.tags.containsKey("FEN")) selectedGame.tags["FEN"]!! else Board.FEN_START_POSITION

            // First we ensure that position is valid when initializing the board
            gamePageViewModel.chessState.boardState.newGame(startFen)

            // Here we are fine to process the rest
            val blackStartGame = startFen.split(" ")[1] == "b"
            gamePageViewModel.interfaceState.whiteSideType =
                if (blackStartGame) PlayerType.Computer else PlayerType.Human
            gamePageViewModel.interfaceState.blackSideType =
                if (blackStartGame) PlayerType.Human else PlayerType.Computer
            whiteSideType = gamePageViewModel.interfaceState.whiteSideType
            blackSideType = gamePageViewModel.interfaceState.blackSideType
            gamePageViewModel.interfaceState.boardReversed =
                blackStartGame
            boardReversed = gamePageViewModel.interfaceState.boardReversed


            gamePageViewModel.interfaceState.promotionState = PendingPromotionData()
            gamePageViewModel.chessState.playedGameHistory.clear()
            gamePageViewModel.chessState.playedGameHistory.add(MoveNumber(text = "${gamePageViewModel.chessState.boardState.moveNumber()}.${if (blackStartGame) ".." else ""}"))
            currentPosition = gamePageViewModel.chessState.boardState.getCurrentPosition()
            startPosition = gamePageViewModel.chessState.boardState.getCurrentPosition()
            gamePageViewModel.chessState.stockfishLib.sendCommand("ucinewgame")
            gamePageViewModel.chessState.boardState.clearLastMoveArrow()
            gamePageViewModel.interfaceState.highlightedHistoryItemIndex = null
            highlightedHistoryItemIndex = gamePageViewModel.interfaceState.highlightedHistoryItemIndex
            lastMoveArrow = gamePageViewModel.chessState.boardState.getLastMoveArrow()

            gamePageViewModel.interfaceState.isInSolutionMode = false
            isInSolutionMode = gamePageViewModel.interfaceState.isInSolutionMode

            gamePageViewModel.interfaceState.gameInProgress = true
            gameInProgress = true

            handleNaturalEndgame()
        } catch (ex: IllegalPositionException) {
            showMinutedSnackBarAction(illegalStartPositionMessage, SnackbarDuration.Short)
        }
    }

    fun newGameRequest() {
        val isInInitialPosition = gamePageViewModel.chessState.boardState.getCurrentPosition() == EMPTY_FEN
        if (isInInitialPosition) {
            doStartNewGame()
        } else {
            gamePageViewModel.interfaceState.pendingNewGameRequest = true
            pendingNewGameRequest = true
        }
    }

    fun stopGameRequest() {
        if (!gamePageViewModel.interfaceState.gameInProgress) return
        gamePageViewModel.interfaceState.pendingStopGameRequest = true
        pendingStopGameRequest = true
    }

    // Select first position of the game.
    fun selectFirstPosition() {
        if (!gameInProgress) {
            val fen = startPosition
            gamePageViewModel.chessState.boardState.setCurrentPosition(fen)

            currentPosition = gamePageViewModel.chessState.boardState.getCurrentPosition()
            lastMoveArrow = null
            gamePageViewModel.interfaceState.highlightedHistoryItemIndex = null
            highlightedHistoryItemIndex = gamePageViewModel.interfaceState.highlightedHistoryItemIndex
            gamePageViewModel.interfaceState.selectedNodeVariationLevel = 0
            selectedNodeVariationLevel = 0
        }
    }

    // Select previous position, can go up variation.
    fun selectPreviousPosition() {
        if (!gameInProgress) {
            // There is at least the first move number element in history at this point.
            val noHistoryMove = !isInSolutionMode && (gamePageViewModel.chessState.playedGameHistory.size < 2)
            if (noHistoryMove) return

            if (highlightedHistoryItemIndex == null) {
                updateMovesNavigatorSelection(-1)
            } else {
                val searchResult = findPreviousMoveNode(
                    historyMoves = if (isInSolutionMode) gamePageViewModel.chessState.gameSolution
                    else gamePageViewModel.chessState.playedGameHistory,
                    nodeData = NodeSearchParam(
                        index = highlightedHistoryItemIndex!!,
                        variationLevel = selectedNodeVariationLevel
                    ),
                    selectedNodeVariationLevel = selectedNodeVariationLevel,
                )
                updateMovesNavigatorSelection(searchResult.index)
                gamePageViewModel.interfaceState.highlightedHistoryItemIndex = searchResult.index
                gamePageViewModel.interfaceState.selectedNodeVariationLevel = searchResult.variationLevel
            }
        }
    }

    // Select next position : if in a variation, cannot go further than the variation end node.
    fun selectNextPosition() {
        if (!gameInProgress) {
            // There is at least the first move number element in history at this point.
            val noHistoryMove = !isInSolutionMode && (gamePageViewModel.chessState.playedGameHistory.size < 2)
            if (noHistoryMove) return

            val searchResult = findNextMoveNode(
                historyMoves = if (isInSolutionMode) gamePageViewModel.chessState.gameSolution
                else gamePageViewModel.chessState.playedGameHistory,
                nodeData = NodeSearchParam(
                    // There is at least the first move number node
                    index = highlightedHistoryItemIndex ?: 0,
                    variationLevel = selectedNodeVariationLevel
                ),
                selectedNodeVariationLevel = selectedNodeVariationLevel,
            )

            if (searchResult.variationsToProcess.isNotEmpty()) {
                /*
                We must not update highlighted item index nor currentVariationLevel in GamePageViewModel yet.
                */
                val moveNumber = findCurrentNodeMoveNumber(
                    historyMoves = if (isInSolutionMode) gamePageViewModel.chessState.gameSolution
                    else gamePageViewModel.chessState.playedGameHistory,
                    innerPreviousNodeSearchParam = InnerPreviousNodeSearchParam(
                        currentNodeIndex = searchResult.index,
                        currentVariationLevel = searchResult.variationLevel,
                        previousTextIsOpenParenthesis = false,
                        previousTextIsEndParenthesis = false,
                        skippingSiblingVariation = false,
                        needingToSkipOneMove = false,
                        mustBreakLoop = false,
                        hasJustMetOpenParenthesis = false,
                        hasJustMetCloseParenthesis = false,
                        hasJustMetMoveNumberAndOpenParenthesis = false,
                        isLastMoveNodeOfMainVariation = false,
                    ),
                    selectedNodeVariationLevel = selectedNodeVariationLevel,
                )
                val isWhiteTurnBeforeMove = gamePageViewModel.isWhiteTurn()
                val turnPoints = if (isWhiteTurnBeforeMove) "." else "..."
                gamePageViewModel.interfaceState.variationsSelectorData =
                    VariationsSelectorData(
                        main = SingleVariationData(
                            text = "$moveNumber$turnPoints${searchResult.mainVariationMoveText!!}",
                            historyIndex = searchResult.index,
                        ),
                        variations = searchResult.variationsToProcess.map {
                            SingleVariationData(
                                text = "$moveNumber$turnPoints${it.firstMoveText}",
                                historyIndex = it.firstMoveIndex,
                            )
                        }
                    )
                variationsSelectorData = gamePageViewModel.interfaceState.variationsSelectorData
                gamePageViewModel.interfaceState.variationSelectionOpen = true
                variationSelectionOpen = gamePageViewModel.interfaceState.variationSelectionOpen
            } else {
                gamePageViewModel.interfaceState.highlightedHistoryItemIndex = searchResult.index
                gamePageViewModel.interfaceState.selectedNodeVariationLevel = searchResult.variationLevel

                gamePageViewModel.interfaceState.variationsSelectorData = null
                variationsSelectorData = null
                updateMovesNavigatorSelection(searchResult.index)
            }

        }
    }

    fun doStopCurrentGame() {
        if (!gamePageViewModel.interfaceState.gameInProgress) return
        computerThinking = false
        gamePageViewModel.interfaceState.promotionState = PendingPromotionData()
        promotionState = gamePageViewModel.interfaceState.promotionState
        gamePageViewModel.interfaceState.gameInProgress = false
        gameInProgress = false
        selectLastPosition()
        showMinutedSnackBarAction(gameStoppedMessage, SnackbarDuration.Short)
    }

    // This must be called after having played the move on the board !
    fun addMoveFanToHistory() {
        val lastMoveFan = gamePageViewModel.chessState.boardState.getLastMoveFan()
        val lastMoveFen = gamePageViewModel.chessState.boardState.getCurrentPosition()
        val localLastMoveArrow = gamePageViewModel.chessState.boardState.getLastMoveArrow()
        gamePageViewModel.chessState.playedGameHistory.add(
            HalfMoveSAN(
                text = lastMoveFan,
                fen = lastMoveFen,
                lastMoveArrowData = localLastMoveArrow
            )
        )
        if (gamePageViewModel.chessState.boardState.whiteTurn()) {
            gamePageViewModel.chessState.playedGameHistory.add(MoveNumber(text = "${gamePageViewModel.chessState.boardState.moveNumber()}."))
        }
    }

    fun tryToSelectPosition(positionData: Triple<String, MoveData, Int>) {
        if (!gameInProgress) {
            gamePageViewModel.chessState.boardState.setCurrentPosition(positionData.first)
            gamePageViewModel.chessState.boardState.setLastMoveArrow(positionData.second)
            currentPosition = gamePageViewModel.chessState.boardState.getCurrentPosition()
            lastMoveArrow = gamePageViewModel.chessState.boardState.getLastMoveArrow()
            gamePageViewModel.interfaceState.highlightedHistoryItemIndex = positionData.third
            highlightedHistoryItemIndex = gamePageViewModel.interfaceState.highlightedHistoryItemIndex
        }
    }

    fun generateComputerMove(oldPosition: String) {
        if (computerThinking) return
        computerThinking = true

        gamePageViewModel.chessState.stockfishLib.sendCommand("position fen $oldPosition")
        gamePageViewModel.chessState.stockfishLib.sendCommand("go infinite")

        readEngineOutputJob = coroutineScope.launch {
            var mustExitLoop = false

            while (!mustExitLoop) {
                val nextEngineLine = gamePageViewModel.chessState.stockfishLib.readNextOutput()

                if (nextEngineLine.startsWith("bestmove")) {
                    val moveParts = nextEngineLine!!.split(" ")
                    val move = moveParts[1]

                    readEngineOutputJob?.cancel()
                    readEngineOutputJob = null
                    computerThinking = false

                    if (gameInProgress) {
                        gamePageViewModel.chessState.boardState.makeMove(move)
                        gamePageViewModel.chessState.boardState.setLastMoveArrow(MoveData.parse(move)!!)
                        addMoveFanToHistory()
                        currentPosition = gamePageViewModel.chessState.boardState.getCurrentPosition()
                        lastMoveArrow = gamePageViewModel.chessState.boardState.getLastMoveArrow()
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
                gamePageViewModel.chessState.stockfishLib.sendCommand("stop")
            }
        }
    }

    fun handleStandardMoveDoneOnBoard(it: MoveData) {
        gamePageViewModel.chessState.boardState.makeMove(it.toString())
        gamePageViewModel.chessState.boardState.setLastMoveArrow(it)
        lastMoveArrow = gamePageViewModel.chessState.boardState.getLastMoveArrow()
        currentPosition =
            gamePageViewModel.chessState.boardState.getCurrentPosition()
        addMoveFanToHistory()
        handleNaturalEndgame()
    }

    fun handlePromotionMoveDoneOnBoard(it: MoveData) {
        gamePageViewModel.chessState.boardState.makeMove(it.toString())
        gamePageViewModel.chessState.boardState.setLastMoveArrow(it)
        gamePageViewModel.interfaceState.promotionState =
            PendingPromotionData()
        promotionState = gamePageViewModel.interfaceState.promotionState
        lastMoveArrow = gamePageViewModel.chessState.boardState.getLastMoveArrow()
        currentPosition =
            gamePageViewModel.chessState.boardState.getCurrentPosition()
        addMoveFanToHistory()
        handleNaturalEndgame()
    }

    fun toggleHistoryMode() {
        val isInInitialPosition = gamePageViewModel.chessState.boardState.getCurrentPosition() == EMPTY_FEN
        val modeSelectionNotActive = isInInitialPosition || gameInProgress
        if (modeSelectionNotActive) return

        val noSolutionAvailable = gamePageViewModel.chessState.gameSolution.isEmpty()
        if (noSolutionAvailable) return

        gamePageViewModel.chessState.boardState.clearLastMoveArrow()
        gamePageViewModel.chessState.boardState.setCurrentPosition(startPosition)

        lastMoveArrow = gamePageViewModel.chessState.boardState.getLastMoveArrow()
        currentPosition = gamePageViewModel.chessState.boardState.getCurrentPosition()

        gamePageViewModel.interfaceState.highlightedHistoryItemIndex = null
        highlightedHistoryItemIndex = gamePageViewModel.interfaceState.highlightedHistoryItemIndex
        gamePageViewModel.interfaceState.selectedNodeVariationLevel = 0
        selectedNodeVariationLevel = gamePageViewModel.interfaceState.selectedNodeVariationLevel
        gamePageViewModel.interfaceState.isInSolutionMode = !isInSolutionMode
        isInSolutionMode = gamePageViewModel.interfaceState.isInSolutionMode
    }

    fun manuallyUpdateHistoryNode() {
        if (highlightedHistoryItemIndex == null) return
        val historyNodes = if (isInSolutionMode) gamePageViewModel.chessState.gameSolution
        else gamePageViewModel.chessState.playedGameHistory

        val currentNode = historyNodes[highlightedHistoryItemIndex!!]
        gamePageViewModel.chessState.boardState.setCurrentPosition(currentNode.fen!!)
        gamePageViewModel.chessState.boardState.setLastMoveArrow(currentNode.lastMoveArrowData)

        lastMoveArrow = gamePageViewModel.chessState.boardState.getLastMoveArrow()
        currentPosition = gamePageViewModel.chessState.boardState.getCurrentPosition()
    }

    fun selectMainVariation() {
        gamePageViewModel.interfaceState.highlightedHistoryItemIndex =
            variationsSelectorData!!.main.historyIndex
        highlightedHistoryItemIndex = gamePageViewModel.interfaceState.highlightedHistoryItemIndex
        manuallyUpdateHistoryNode()

        gamePageViewModel.interfaceState.variationSelectionOpen = false
        variationSelectionOpen = gamePageViewModel.interfaceState.variationSelectionOpen
        gamePageViewModel.interfaceState.variationsSelectorData = null
    }

    fun selectSubVariation(variationIndex: Int) {
        gamePageViewModel.interfaceState.highlightedHistoryItemIndex =
            variationsSelectorData!!.variations[variationIndex].historyIndex
        highlightedHistoryItemIndex = gamePageViewModel.interfaceState.highlightedHistoryItemIndex
        manuallyUpdateHistoryNode()

        gamePageViewModel.interfaceState.variationSelectionOpen = false
        gamePageViewModel.interfaceState.variationsSelectorData = null
        variationSelectionOpen = gamePageViewModel.interfaceState.variationSelectionOpen
    }

    fun cancelVariationSelection() {
        gamePageViewModel.interfaceState.variationSelectionOpen = false
        gamePageViewModel.interfaceState.variationsSelectorData = null
        variationSelectionOpen = gamePageViewModel.interfaceState.variationSelectionOpen
    }

    fun handleGoBackRequest() {
        val isInInitialPosition =
            gamePageViewModel.chessState.boardState.getCurrentPosition() == EMPTY_FEN
        if (isInInitialPosition) {
            navController.popBackStack()
        }
        else {
            gamePageViewModel.interfaceState.pendingExitPageRequest = true
            pendingExitPageRequest = true
        }
    }

    @Composable
    fun variationSelectionDropDownComponent() = DropdownMenu(
        expanded = variationSelectionOpen,
        onDismissRequest = ::cancelVariationSelection,
    ) {
        DropdownMenuItem(onClick = ::selectMainVariation) {
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
        Divider()
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

    @Composable
    fun chessBoardComponent() {
        DynamicChessBoard(
            whiteSideType = whiteSideType,
            blackSideType = blackSideType,
            reversed = boardReversed,
            lastMoveArrow = lastMoveArrow,
            gameInProgress = gameInProgress,
            position = currentPosition,
            promotionState = promotionState,
            isValidMoveCallback = {
                gamePageViewModel.chessState.boardState.isValidMove(it)
            },
            dndMoveCallback = {
                handleStandardMoveDoneOnBoard(it)
            },
            promotionMoveCallback = {
                handlePromotionMoveDoneOnBoard(it)
            },
            cancelPendingPromotionCallback = {
                gamePageViewModel.interfaceState.promotionState =
                    PendingPromotionData()
            },
            setPendingPromotionCallback = {
                gamePageViewModel.interfaceState.promotionState = it
                promotionState = gamePageViewModel.interfaceState.promotionState
            },
            computerMoveRequestCallback = {
                if (!computerThinking) {
                    generateComputerMove(it)
                }
            },
        )
    }

    @Composable
    fun historyComponent() {
        val isInInitialPosition =
            gamePageViewModel.chessState.boardState.getCurrentPosition() == EMPTY_FEN
        val modeSelectionActive = !isInInitialPosition && !gameInProgress
        val elements =
            if (modeSelectionActive && isInSolutionMode)
                gamePageViewModel.chessState.gameSolution.toTypedArray()
            else gamePageViewModel.chessState.playedGameHistory.toTypedArray()

        MovesNavigator(
            elements = elements,
            mustBeVisibleByDefaultElementIndex =
            if (gameInProgress) elements.size.dec() else highlightedHistoryItemIndex,
            highlightedItemIndex = highlightedHistoryItemIndex,
            elementSelectionRequestCallback = {
                tryToSelectPosition(it)
            },
            handleFirstPositionRequest = ::selectFirstPosition,
            handleLastPositionRequest = ::selectLastPosition,
            handlePreviousPositionRequest = ::selectPreviousPosition,
            handleNextPositionRequest = ::selectNextPosition,
            historyModeToggleRequestHandler = ::toggleHistoryMode,
            isInSolutionMode = isInSolutionMode,
            modeSelectionActive = modeSelectionActive && solutionAvailable,
            failedToLoadSolution = failedToLoadSolution,
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
            isOpen = pendingExitPageRequest,
            title = exitPageDialogTitle,
            message = exitPageDialogMessage,
            validateCallback = {
                gamePageViewModel.interfaceState.pendingExitPageRequest = false
                pendingExitPageRequest = false
                navController.popBackStack()
            },
            dismissCallback = {
                gamePageViewModel.interfaceState.pendingExitPageRequest = false
                pendingExitPageRequest = false
            })

        ConfirmDialog(
            isOpen = pendingNewGameRequest,
            title = newGameDialogTitle,
            message = newGameDialogMessage,
            validateCallback = {
                gamePageViewModel.interfaceState.pendingNewGameRequest = false
                gamePageViewModel.interfaceState.pendingSelectEngineDialog = true
                pendingNewGameRequest = false
                doStartNewGame()
            },
            dismissCallback = {
                gamePageViewModel.interfaceState.pendingNewGameRequest = false
                pendingNewGameRequest = false
            })

        ConfirmDialog(
            isOpen = pendingStopGameRequest,
            title = stopDialogTitle,
            message = stopDialogMessage,
            validateCallback = {
                gamePageViewModel.interfaceState.pendingStopGameRequest = false
                pendingStopGameRequest = false
                doStopCurrentGame()
            },
            dismissCallback = {
                gamePageViewModel.interfaceState.pendingStopGameRequest = false
                pendingStopGameRequest = false
            })

    }

    @Composable
    fun topAppBarComponents() {
        SimpleButton(
            text = stringResource(R.string.new_game),
            vectorId = R.drawable.ic_start_flags
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
            gamePageViewModel.interfaceState.boardReversed =
                !gamePageViewModel.interfaceState.boardReversed
            boardReversed = gamePageViewModel.interfaceState.boardReversed
        }
    }

    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.game_page)) },
                    backgroundColor = Color(0xFF4F7E33),
                    actions = {
                        topAppBarComponents()
                    },
                    navigationIcon = {
                        IconButton(onClick = ::handleGoBackRequest) {
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
                    Layout(
                        content = {
                            chessBoardComponent()
                            Box {
                                historyComponent()
                                variationSelectionDropDownComponent()
                            }
                            dialogs()

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