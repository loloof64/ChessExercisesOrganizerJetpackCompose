package com.loloof64.chessexercisesorganizer.ui.pages.game_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonsoruibal.chess.Board
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import com.loloof64.chessexercisesorganizer.ui.components.*
import com.loloof64.chessexercisesorganizer.ui.components.moves_navigator.*
import com.loloof64.chessexercisesorganizer.utils.update
import com.loloof64.stockfish.StockfishLib
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

const val cpuThinkingTimeMs = 2_000L

data class GamePageInterfaceState(
    val boardReversed: Boolean = false,
    val gameInProgress: Boolean = false,
    val pendingNewGameRequest: Boolean = false,
    val pendingStopGameRequest: Boolean = false,
    val pendingExitPageRequest: Boolean = false,
    val pendingSelectEngineDialog: Boolean = false,
    val promotionState: PendingPromotionData = PendingPromotionData(),
    val variationsSelectorData: VariationsSelectorData? = null,
    val highlightedHistoryItemIndex: Int? = null,
    val whiteSideType: PlayerType = PlayerType.Human,
    val blackSideType: PlayerType = PlayerType.Human,
    val isInSolutionMode: Boolean = false,
    /* When going to next position, or last position,
        we want to match as many parenthesis as needed, in order to
        pass through variations starting at current level.
     */
    val selectedNodeVariationLevel: Int = 0,
    val solutionAvailable: Boolean = false,
    val failedToLoadSolution: Boolean = false,
    val variationSelectionOpen: Boolean = false,
    val startPosition: String = EMPTY_FEN,
    val computerThinking: Boolean = false,
    val readEngineOutputJob: Job? = null,
)

data class GamePageUiChessState(
    val gamesList: List<PGNGame> = listOf(),
    val boardPosition: String = EMPTY_FEN,
    val lastMoveArrow: MoveData? = null,
    val isWhiteTurn: Boolean = true,
    val playedGameHistory: List<MovesNavigatorElement> = listOf(),
    val gameSolution: List<MovesNavigatorElement> = listOf(),
    val selectedGame: PGNGame = PGNGame(tags = mutableMapOf(), moves = null),
)

data class GamePageViewModelChessState(
    val gamesList: List<PGNGame> = listOf(),
    val board: DynamicBoardDataHandler = DynamicBoardDataHandler(),
    val playedGameHistory: List<MovesNavigatorElement> = listOf(),
    val gameSolution: List<MovesNavigatorElement> = listOf(),
    val selectedGame: PGNGame = PGNGame(tags = mutableMapOf(), moves = null),
) {
    fun toUiChessState() = GamePageUiChessState(
        gamesList = this.gamesList,
        playedGameHistory = this.playedGameHistory,
        gameSolution = this.gameSolution,
        selectedGame = this.selectedGame,
        boardPosition = this.board.getCurrentPosition(),
        lastMoveArrow = this.board.getLastMoveArrow(),
        isWhiteTurn = this.board.whiteTurn(),
    )
}

data class GamePageUiState(
    val interfaceState: GamePageInterfaceState = GamePageInterfaceState(),
    val chessState: GamePageUiChessState = GamePageUiChessState()
)

private data class GamePageViewModelState(
    val interfaceState: GamePageInterfaceState = GamePageInterfaceState(),
    val chessState: GamePageViewModelChessState = GamePageViewModelChessState(),
) {
    fun toUiState() = GamePageUiState(
        interfaceState = interfaceState.copy(),
        chessState = chessState.toUiChessState(),
    )

    fun copyWithModifiedInterfaceState(
        boardReversed: Boolean = this.interfaceState.boardReversed,
        gameInProgress: Boolean = this.interfaceState.gameInProgress,
        pendingNewGameRequest: Boolean = this.interfaceState.pendingNewGameRequest,
        pendingStopGameRequest: Boolean = this.interfaceState.pendingStopGameRequest,
        pendingExitPageRequest: Boolean = this.interfaceState.pendingExitPageRequest,
        pendingSelectEngineDialog: Boolean = this.interfaceState.pendingSelectEngineDialog,
        promotionState: PendingPromotionData = this.interfaceState.promotionState,
        variationsSelectorData: VariationsSelectorData? = this.interfaceState.variationsSelectorData,
        highlightedHistoryItemIndex: Int? = this.interfaceState.highlightedHistoryItemIndex,
        whiteSideType: PlayerType = this.interfaceState.whiteSideType,
        blackSideType: PlayerType = this.interfaceState.blackSideType,
        isInSolutionMode: Boolean = this.interfaceState.isInSolutionMode,
        selectedNodeVariationLevel: Int = this.interfaceState.selectedNodeVariationLevel,
        solutionAvailable: Boolean = this.interfaceState.solutionAvailable,
        failedToLoadSolution: Boolean = this.interfaceState.failedToLoadSolution,
        variationSelectionOpen: Boolean = this.interfaceState.variationSelectionOpen,
        startPosition: String = this.interfaceState.startPosition,
        computerThinking: Boolean = this.interfaceState.computerThinking,
        readEngineOutputJob: Job? = this.interfaceState.readEngineOutputJob,
    ) = GamePageViewModelState(
        chessState = this.chessState.copy(),
        interfaceState = GamePageInterfaceState(
            boardReversed = boardReversed,
            gameInProgress = gameInProgress,
            pendingNewGameRequest = pendingNewGameRequest,
            pendingStopGameRequest = pendingStopGameRequest,
            pendingExitPageRequest = pendingExitPageRequest,
            pendingSelectEngineDialog = pendingSelectEngineDialog,
            promotionState = promotionState,
            variationsSelectorData = variationsSelectorData,
            highlightedHistoryItemIndex = highlightedHistoryItemIndex,
            whiteSideType = whiteSideType,
            blackSideType = blackSideType,
            isInSolutionMode = isInSolutionMode,
            selectedNodeVariationLevel = selectedNodeVariationLevel,
            solutionAvailable = solutionAvailable,
            failedToLoadSolution = failedToLoadSolution,
            variationSelectionOpen = variationSelectionOpen,
            startPosition = startPosition,
            computerThinking = computerThinking,
            readEngineOutputJob = readEngineOutputJob,
        )
    )

    fun copyWithModifiedChessState(
        gamesList: List<PGNGame> = this.chessState.gamesList,
        board: DynamicBoardDataHandler = this.chessState.board,
        playedGameHistory: List<MovesNavigatorElement> = this.chessState.playedGameHistory,
        gameSolution: List<MovesNavigatorElement> = this.chessState.gameSolution,
        selectedGame: PGNGame = this.chessState.selectedGame,
    ) = GamePageViewModelState(
        interfaceState = this.interfaceState.copy(),
        chessState = GamePageViewModelChessState(
            gamesList = gamesList,
            board = board,
            playedGameHistory = playedGameHistory,
            gameSolution = gameSolution,
            selectedGame = selectedGame,
        )
    )
}

class GamePageViewModel : ViewModel() {

    private val stockfishLib = StockfishLib()
    private val viewModelState = MutableStateFlow(GamePageViewModelState())

    override fun onCleared() {
        super.onCleared()
        stockfishLib.sendCommand("quit")
    }

    val uiState =
        viewModelState
            .map {
                it.toUiState()
            }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                viewModelState.value.toUiState()
            )

    private fun updateMovesNavigatorSelection(nodeIndex: Int) {
        if (viewModelState.value.interfaceState.gameInProgress) return

        if (nodeIndex < 0) {
            viewModelState.value.chessState.board.setCurrentPosition(
                viewModelState.value.interfaceState.startPosition
            )
            viewModelState.value.chessState.board.clearLastMoveArrow()

            viewModelState.update {
                it.copyWithModifiedInterfaceState(highlightedHistoryItemIndex = null)
            }

            return
        }

        val currentNode =
            if (viewModelState.value.interfaceState.isInSolutionMode) viewModelState.value.chessState.gameSolution[nodeIndex]
            else viewModelState.value.chessState.playedGameHistory[nodeIndex]

        val fen = currentNode.fen
        val lastMoveArrowData = currentNode.lastMoveArrowData

        if (fen != null) {
            viewModelState.value.chessState.board.setCurrentPosition(fen)
        }
        viewModelState.value.chessState.board.setLastMoveArrow(lastMoveArrowData)

        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                highlightedHistoryItemIndex = if (fen != null) nodeIndex else null
            )
        }
    }

    fun toggleBoardReversed() {
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                boardReversed = !viewModelState.value.interfaceState.boardReversed
            )
        }
    }

    // Select last position in current variation.
    fun selectLastPosition() {
        if (!viewModelState.value.interfaceState.gameInProgress) {
            // There is at least the first move number element in history at this point.
            val noHistoryMove =
                !viewModelState.value.interfaceState.isInSolutionMode && (viewModelState.value.chessState.playedGameHistory.size < 2)
            if (noHistoryMove) return

            var currentNodeData = NodeSearchParam(
                index = viewModelState.value.interfaceState.highlightedHistoryItemIndex ?: -1,
                variationLevel = viewModelState.value.interfaceState.selectedNodeVariationLevel
            )
            while (true) {
                val searchResult = findNextMoveNode(
                    nodeData = currentNodeData,
                    historyMoves = if (viewModelState.value.interfaceState.isInSolutionMode) viewModelState.value.chessState.gameSolution
                    else viewModelState.value.chessState.playedGameHistory,
                    selectedNodeVariationLevel = viewModelState.value.interfaceState.selectedNodeVariationLevel,
                )
                if (searchResult.isLastMoveNodeOfMainVariation || searchResult.hasJustMetCloseParenthesis) {
                    updateMovesNavigatorSelection(searchResult.index)
                    viewModelState.update {
                        it.copyWithModifiedInterfaceState(
                            highlightedHistoryItemIndex = searchResult.index,
                            selectedNodeVariationLevel = searchResult.variationLevel,
                        )
                    }
                    break
                } else currentNodeData = NodeSearchParam(
                    index = searchResult.index,
                    variationLevel = searchResult.variationLevel,
                )
            }
        }
    }

    fun getNaturalGameEndedStatus(): GameEndedStatus {
        return viewModelState.value.chessState.board.getNaturalGameEndedStatus()
    }

    private fun updateStartPositionFromSelectedGame() {
        val startFEN = if (viewModelState.value.chessState.selectedGame.tags.containsKey("FEN"))
            viewModelState.value.chessState.selectedGame.tags["FEN"]!!
        else Board.FEN_START_POSITION
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                startPosition = startFEN
            )
        }
    }

    /**
    throws IllegalPositionException
     */
    fun doStartNewGame() {
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                solutionAvailable = it.chessState.gameSolution.isNotEmpty()
            )
        }

        val startFen = viewModelState.value.interfaceState.startPosition


        // First we ensure that position is valid when initializing the board
        viewModelState.value.chessState.board.newGame(startFen)

        // Here we are fine to process the rest
        val blackStartGame = startFen.split(" ")[1] == "b"
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                whiteSideType = if (blackStartGame) PlayerType.Computer else PlayerType.Human,
                blackSideType = if (blackStartGame) PlayerType.Human else PlayerType.Computer,
                boardReversed = blackStartGame,
                promotionState = PendingPromotionData(),
                highlightedHistoryItemIndex = null,
                isInSolutionMode = false,
            )
        }
        viewModelState.update {
            it.copyWithModifiedChessState(
                playedGameHistory = listOf(
                    MoveNumber(
                        text = "${viewModelState.value.chessState.board.moveNumber()}.${if (blackStartGame) ".." else ""}"
                    )
                ),
            )
        }

        stockfishLib.sendCommand("ucinewgame")
        viewModelState.value.chessState.board.clearLastMoveArrow()
        viewModelState.update {
            it.copyWithModifiedChessState()
        }
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                gameInProgress = true
            )
        }
    }

    fun newGameRequest() {
        val isInInitialPosition =
            viewModelState.value.chessState.board.getCurrentPosition() == EMPTY_FEN
        if (isInInitialPosition) {
            doStartNewGame()
        } else {
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    pendingNewGameRequest = true,
                )
            }
        }
    }

    fun stopGameRequest() {
        if (!viewModelState.value.interfaceState.gameInProgress) return
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                pendingStopGameRequest = true,
            )
        }
    }

    // Select first position of the game.
    fun selectFirstPosition() {
        if (!viewModelState.value.interfaceState.gameInProgress) {

            val fen = viewModelState.value.interfaceState.startPosition
            viewModelState.value.chessState.board.setCurrentPosition(fen)
            viewModelState.value.chessState.board.clearLastMoveArrow()

            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    highlightedHistoryItemIndex = null,
                    selectedNodeVariationLevel = 0,

                    )
            }

        }
    }

    // Select previous position, can go up variation.
    fun selectPreviousPosition() {
        if (!viewModelState.value.interfaceState.gameInProgress) {
            // There is at least the first move number element in history at this point.
            val noHistoryMove =
                !viewModelState.value.interfaceState.isInSolutionMode && (viewModelState.value.chessState.playedGameHistory.size < 2)
            if (noHistoryMove) return

            if (viewModelState.value.interfaceState.highlightedHistoryItemIndex == null) {
                updateMovesNavigatorSelection(-1)
            } else {
                val searchResult = findPreviousMoveNode(
                    historyMoves = if (viewModelState.value.interfaceState.isInSolutionMode) viewModelState.value.chessState.gameSolution
                    else viewModelState.value.chessState.playedGameHistory,
                    nodeData = NodeSearchParam(
                        index = viewModelState.value.interfaceState.highlightedHistoryItemIndex!!,
                        variationLevel = viewModelState.value.interfaceState.selectedNodeVariationLevel
                    ),
                    selectedNodeVariationLevel = viewModelState.value.interfaceState.selectedNodeVariationLevel,
                )
                // Order is important here !
                updateMovesNavigatorSelection(searchResult.index)
                viewModelState.update {
                    it.copyWithModifiedInterfaceState(
                        highlightedHistoryItemIndex = searchResult.index,
                        selectedNodeVariationLevel = searchResult.variationLevel,
                    )
                }
            }
        }
    }

    // Select next position : if in a variation, cannot go further than the variation end node.
    fun selectNextPosition() {
        if (!viewModelState.value.interfaceState.gameInProgress) {
            // There is at least the first move number element in history at this point, so the first index of concern is 1.
            val noHistoryMove =
                !viewModelState.value.interfaceState.isInSolutionMode && (viewModelState.value.chessState.playedGameHistory.size < 2)
            if (noHistoryMove) return

            val searchResult = findNextMoveNode(
                historyMoves = if (viewModelState.value.interfaceState.isInSolutionMode) viewModelState.value.chessState.gameSolution
                else viewModelState.value.chessState.playedGameHistory,
                nodeData = NodeSearchParam(
                    // There is at least the first move number node
                    index = viewModelState.value.interfaceState.highlightedHistoryItemIndex ?: 0,
                    variationLevel = viewModelState.value.interfaceState.selectedNodeVariationLevel
                ),
                selectedNodeVariationLevel = viewModelState.value.interfaceState.selectedNodeVariationLevel,
            )

            if (searchResult.variationsToProcess.isNotEmpty()) {
                /*
                We must not update highlighted item index nor currentVariationLevel in GamePageViewModel yet.
                */
                val moveNumber = findCurrentNodeMoveNumber(
                    historyMoves = if (viewModelState.value.interfaceState.isInSolutionMode) viewModelState.value.chessState.gameSolution
                    else viewModelState.value.chessState.playedGameHistory,
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
                    selectedNodeVariationLevel = viewModelState.value.interfaceState.selectedNodeVariationLevel,
                )
                val isWhiteTurnBeforeMove = viewModelState.value.chessState.board.whiteTurn()
                val turnPoints = if (isWhiteTurnBeforeMove) "." else "..."

                viewModelState.update {
                    it.copyWithModifiedInterfaceState(
                        variationsSelectorData = VariationsSelectorData(
                            main = SingleVariationData(
                                text = "$moveNumber$turnPoints${searchResult.mainVariationMoveText!!}",
                                historyIndex = searchResult.index,
                            ),
                            variations = searchResult.variationsToProcess.map { singleVariation ->
                                SingleVariationData(
                                    text = "$moveNumber$turnPoints${singleVariation.firstMoveText}",
                                    historyIndex = singleVariation.firstMoveIndex,
                                )
                            }
                        ),
                        variationSelectionOpen = true
                    )
                }
            } else {
                // Order is important here !
                updateMovesNavigatorSelection(searchResult.index)
                viewModelState.update {
                    it.copyWithModifiedInterfaceState(
                        highlightedHistoryItemIndex = searchResult.index,
                        selectedNodeVariationLevel = searchResult.variationLevel,
                        variationsSelectorData = null,
                    )
                }
            }

        }
    }

    /**
     * Returns true if there was a game to stop.
     */
    fun doStopCurrentGame(): Boolean {
        val thereWasAGameToStop = viewModelState.value.interfaceState.gameInProgress
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                computerThinking = false,
                promotionState = PendingPromotionData(),
                gameInProgress = false,
            )
        }
        selectLastPosition()
        return thereWasAGameToStop
    }

    // This must be called after having played the move on the board !
    private fun addMoveFanToHistory() {
        val lastMoveFan = viewModelState.value.chessState.board.getLastMoveFan()
        val lastMoveFen = viewModelState.value.chessState.board.getCurrentPosition()
        val localLastMoveArrow = viewModelState.value.chessState.board.getLastMoveArrow()
        val playedGameHistoryCopy =
            viewModelState.value.chessState.playedGameHistory.toMutableList()
        playedGameHistoryCopy.add(
            HalfMoveSAN(
                text = lastMoveFan,
                fen = lastMoveFen,
                lastMoveArrowData = localLastMoveArrow
            )
        )
        if (viewModelState.value.chessState.board.whiteTurn()) {
            playedGameHistoryCopy.add(MoveNumber(text = "${viewModelState.value.chessState.board.moveNumber()}."))
        }

        viewModelState.update {
            it.copyWithModifiedChessState(
                playedGameHistory = playedGameHistoryCopy.toList()
            )
        }
    }

    fun tryToSelectPosition(positionData: Triple<String, MoveData, Int>) {
        if (!viewModelState.value.interfaceState.gameInProgress) {
            viewModelState.value.chessState.board.setCurrentPosition(positionData.first)
            viewModelState.value.chessState.board.setLastMoveArrow(positionData.second)
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    highlightedHistoryItemIndex = positionData.third
                )
            }
        }
    }

    fun cancelExitPageConfirmation() {
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                pendingExitPageRequest = false
            )
        }
    }

    fun cancelNewGameConfirmation() {
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                pendingNewGameRequest = false
            )
        }
    }

    fun cancelStopGameConfirmation() {
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                pendingStopGameRequest = false
            )
        }
    }

    fun generateComputerMove(oldPosition: String) {
        if (viewModelState.value.interfaceState.computerThinking) return
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                computerThinking = true
            )
        }

        stockfishLib.sendCommand("position fen $oldPosition")
        stockfishLib.sendCommand("go infinite")

        val newJob = viewModelScope.launch {
            var mustExitLoop = false

            while (!mustExitLoop) {
                val nextEngineLine = stockfishLib.readNextOutput()

                if (nextEngineLine.startsWith("bestmove")) {
                    val moveParts = nextEngineLine!!.split(" ")
                    val move = moveParts[1]

                    viewModelState.value.interfaceState.readEngineOutputJob?.cancel()
                    viewModelState.update {
                        it.copyWithModifiedInterfaceState(
                            readEngineOutputJob = null,
                            computerThinking = false,
                        )
                    }

                    if (viewModelState.value.interfaceState.gameInProgress) {
                        viewModelState.value.chessState.board.makeMove(move)
                        viewModelState.value.chessState.board.setLastMoveArrow(MoveData.parse(move)!!)
                        addMoveFanToHistory()
                        viewModelState.update {
                            it.copyWithModifiedChessState()
                        }
                    }

                    mustExitLoop = true
                }
                delay(100)
            }
        }

        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                readEngineOutputJob = newJob
            )
        }

        viewModelScope.launch {
            delay(cpuThinkingTimeMs)
            if (viewModelState.value.interfaceState.readEngineOutputJob?.isActive == true) {
                stockfishLib.sendCommand("stop")
            }
        }
    }

    fun handleStandardMoveDoneOnBoard(it: MoveData) {
        viewModelState.value.chessState.board.makeMove(it.toString())
        viewModelState.value.chessState.board.setLastMoveArrow(it)
        viewModelState.update {
            it.copyWithModifiedChessState()
        }
        addMoveFanToHistory()
    }

    fun handlePromotionMoveDoneOnBoard(it: MoveData) {
        viewModelState.value.chessState.board.makeMove(it.toString())
        viewModelState.value.chessState.board.setLastMoveArrow(it)
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                promotionState = PendingPromotionData(),
            )
        }
        addMoveFanToHistory()
    }

    fun toggleHistoryMode() {
        val isInInitialPosition =
            viewModelState.value.chessState.board.getCurrentPosition() == EMPTY_FEN
        val modeSelectionNotActive =
            isInInitialPosition || viewModelState.value.interfaceState.gameInProgress
        if (modeSelectionNotActive) return

        val noSolutionAvailable = viewModelState.value.chessState.gameSolution.isEmpty()
        if (noSolutionAvailable) return

        viewModelState.value.chessState.board.clearLastMoveArrow()
        viewModelState.value.chessState.board.setCurrentPosition(viewModelState.value.interfaceState.startPosition)

        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                highlightedHistoryItemIndex = null,
                selectedNodeVariationLevel = 0,
                isInSolutionMode = !viewModelState.value.interfaceState.isInSolutionMode,
            )
        }

    }

    private fun manuallyUpdateHistoryNode() {
        if (viewModelState.value.interfaceState.highlightedHistoryItemIndex == null) return
        val historyNodes =
            if (viewModelState.value.interfaceState.isInSolutionMode) viewModelState.value.chessState.gameSolution
            else viewModelState.value.chessState.playedGameHistory

        val currentNode =
            historyNodes[viewModelState.value.interfaceState.highlightedHistoryItemIndex!!]

        viewModelState.value.chessState.board.setCurrentPosition(currentNode.fen!!)
        viewModelState.value.chessState.board.setLastMoveArrow(currentNode.lastMoveArrowData)

        viewModelState.update {
            it.copyWithModifiedChessState()
        }
    }

    suspend fun selectMainVariation() {
        withContext(Dispatchers.Main.immediate) {
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    highlightedHistoryItemIndex = viewModelState.value.interfaceState
                        .variationsSelectorData!!.main.historyIndex,
                )
            }
            manuallyUpdateHistoryNode()
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    variationSelectionOpen = false,
                )
            }
            delay(700)
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    variationsSelectorData = null,
                )
            }
        }
    }

    suspend fun selectSubVariation(variationIndex: Int) {
        withContext(Dispatchers.Main.immediate) {
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    highlightedHistoryItemIndex = viewModelState.value.interfaceState
                        .variationsSelectorData!!.variations[variationIndex].historyIndex
                )
            }
            manuallyUpdateHistoryNode()
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    variationSelectionOpen = false,
                )
            }
            delay(700)
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    variationsSelectorData = null,
                )
            }
        }

    }

    suspend fun cancelVariationSelection() {
        withContext(Dispatchers.Main.immediate) {
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    variationSelectionOpen = false,
                )
            }
            delay(700)
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    variationsSelectorData = null,
                )
            }
        }

    }

    /**
     * Returns false if the request needs to be processed immediately, true if it requires user agreement.
     */
    fun handleGoBackRequest(): Boolean {
        val isInInitialPosition =
            viewModelState.value.chessState.board.getCurrentPosition() == EMPTY_FEN
        return if (isInInitialPosition) {
            false
        } else {
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    pendingExitPageRequest = true,
                )
            }
            true
        }
    }

    fun isValidMove(move: String): Boolean {
        return viewModelState.value.chessState.board.isValidMove(move)
    }

    fun clearPromotionData() {
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                promotionState = PendingPromotionData()
            )
        }
    }

    fun changePromotionData(data: PendingPromotionData) {
        viewModelState.update {
            it.copyWithModifiedInterfaceState(
                promotionState = data
            )
        }
    }

    fun setGamesList(games: List<PGNGame>) {
        viewModelState.update {
            it.copyWithModifiedChessState(
                gamesList = games
            )
        }
    }

    fun setSelectedGame(game: PGNGame) {
        viewModelState.update {
            it.copyWithModifiedChessState(
                selectedGame = game
            )
        }
        updateStartPositionFromSelectedGame()
        updateSolutionFromSelectedGame()
    }

    private fun updateSolutionFromSelectedGame() {
        try {
            val solutionHistory =
                buildHistoryFromPGNGame(viewModelState.value.chessState.selectedGame)
            viewModelState.update {
                it.copyWithModifiedChessState(
                    gameSolution = solutionHistory
                )
            }
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    failedToLoadSolution = false
                )
            }
        } catch (ex: Exception) {
            viewModelState.update {
                it.copyWithModifiedChessState(
                    gameSolution = listOf()
                )
            }
            viewModelState.update {
                it.copyWithModifiedInterfaceState(
                    failedToLoadSolution = true
                )
            }

            println(ex)
        }
    }
}