package com.loloof64.chessexercisesorganizer.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.alonsoruibal.chess.Board
import com.alonsoruibal.chess.Move
import com.loloof64.chessexercisesorganizer.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.sqrt

const val STANDARD_FEN = Board.FEN_START_POSITION
const val EMPTY_FEN = "8/8/8/8/8/8/8/8 w - - 0 1"

fun String.toBoard(): Board {
    val position = this
    return Board().apply {
        fen = position
    }
}

enum class GameEndedStatus {
    NOT_ENDED,
    CHECKMATE_WHITE,
    CHECKMATE_BLACK,
    STALEMATE,
    DRAW_THREE_FOLD_REPETITION,
    DRAW_FIFTY_MOVES_RULE,
    DRAW_MISSING_MATERIAL,
}

enum class PlayerType {
    Human,
    Computer
}

class DynamicBoardDataHandler {
    private var startPosition: String = STANDARD_FEN
    private var boardLogic = EMPTY_FEN.toBoard()
    private var arrowData: MoveData? = null

    fun setStartPosition(position: String) {
        startPosition = position
    }

    fun setLastMoveArrow(arrowData: MoveData) {
        this.arrowData = arrowData
    }

    fun clearLastMoveArrow() {
        arrowData = null
    }

    fun getLastMoveArrow() : MoveData? = arrowData

    fun whiteTurn() = boardLogic.turn

    fun moveNumber() = (boardLogic.moveNumber shr 1) + 1
    
    fun getCurrentPosition(): String = boardLogic.fen ?: EMPTY_FEN

    fun newGame() {
        boardLogic = startPosition.toBoard()
    }

    fun makeMove(moveStr: String) : Boolean {
        val move = Move.getFromString(boardLogic, moveStr, true)
        return boardLogic.doMove(move, true, true)
    }

    fun getLastMoveFan(): String {
        val forBlackTurn = boardLogic.turn
        var lastMoveSan = boardLogic.lastMoveSan
        if (lastMoveSan == null) throw java.lang.RuntimeException("No move played !")
        val referenceChars = "NBRQK".toCharArray()
        var firstOccurrenceIndex = -1
        for (i in 0.until(lastMoveSan.length)) {
            val currentElement = lastMoveSan.toCharArray()[i]
            if (referenceChars.contains(currentElement)) {
                firstOccurrenceIndex = i
                break
            }
        }
        if (firstOccurrenceIndex > -1) {
            val replacement = when (val element = lastMoveSan.toCharArray()[firstOccurrenceIndex]) {
                'N' -> if (forBlackTurn) "\u265e" else "\u2658"
                'B' -> if (forBlackTurn) "\u265d" else "\u2657"
                'R' -> if (forBlackTurn) "\u265c" else "\u2656"
                'Q' -> if (forBlackTurn) "\u265b" else "\u2655"
                'K' -> if (forBlackTurn) "\u265a" else "\u2654"
                else -> throw java.lang.RuntimeException("Unrecognized piece char $element into SAN $lastMoveSan")
            }
            val firstPart = lastMoveSan.substring(0, firstOccurrenceIndex)
            val lastPart = lastMoveSan.substring(firstOccurrenceIndex + 1)

            lastMoveSan = "$firstPart$replacement$lastPart"
        }
        return lastMoveSan
    }

    fun isValidMove(moveStr: String): Boolean {
        val boardCopy = boardLogic.fen?.toBoard()
        val move = Move.getFromString(boardCopy, moveStr, true)
        return boardCopy?.doMove(move, true, true) ?: false
    }

    fun getNaturalEndGameStatus(): GameEndedStatus {
        return when {
            boardLogic.isMate -> if (boardLogic.turn) GameEndedStatus.CHECKMATE_BLACK else GameEndedStatus.CHECKMATE_WHITE
            boardLogic.isStalemate -> GameEndedStatus.STALEMATE
            boardLogic.isDrawByThreeFoldRepetitions  -> GameEndedStatus.DRAW_THREE_FOLD_REPETITION
            boardLogic.isDrawByFiftyMovesRule -> GameEndedStatus.DRAW_FIFTY_MOVES_RULE
            boardLogic.isDrawByMissingMaterial -> GameEndedStatus.DRAW_MISSING_MATERIAL
            else -> GameEndedStatus.NOT_ENDED
        }
    }
}

data class MoveData(
    val startFile: Int = Int.MIN_VALUE,
    val startRank: Int = Int.MIN_VALUE,
    val targetFile: Int = Int.MIN_VALUE,
    val targetRank: Int = Int.MIN_VALUE,
    val promotion: Char? = null,
) {
    override fun toString(): String {
        return "${startFile.asFileChar()}${startRank.asRankChar()}" +
                "${targetFile.asFileChar()}${targetRank.asRankChar()}${promotion ?: ""}"
    }

    companion object {
        fun parse(str: String) : MoveData? {

            val startFile = str[0].code - 'a'.code
            val startRank = str[1].code - '1'.code
            val targetFile = str[2].code - 'a'.code
            val targetRank = str[3].code - '1'.code
            var promotion: Char? = if (str.length >= 5) str[4] else null

            if (startFile < 0 || startFile > 7) return null
            if (startRank < 0 || startRank > 7) return null
            if (targetFile < 0 || targetFile > 7) return null
            if (targetRank < 0 || targetRank > 7) return null

            if (promotion?.let { "qrbn".contains(it) } == false) promotion = null

            return MoveData(
                startFile = startFile,
                startRank = startRank,
                targetFile = targetFile,
                targetRank = targetRank,
                promotion = promotion
            )
        }
    }
}

data class PendingPromotionData(
    val pendingPromotion: Boolean = false,
    val pendingPromotionForBlack: Boolean = false,
    val pendingPromotionStartedInReversedMode: Boolean = false,
    val pieceValue: Char = NO_PIECE,
    val startFile: Int = Int.MIN_VALUE,
    val startRank: Int = Int.MIN_VALUE,
    val targetFile: Int = Int.MIN_VALUE,
    val targetRank: Int = Int.MIN_VALUE,
    val movedPieceXRatio: Float = Float.MIN_VALUE,
    val movedPieceYRatio: Float = Float.MAX_VALUE,
) {
    override fun toString(): String =
        "$pendingPromotion|$pendingPromotionForBlack|$pendingPromotionStartedInReversedMode|" +
                "$pieceValue|$startFile|$startRank|$targetFile|$targetRank|" +
                "$movedPieceXRatio|$movedPieceYRatio"

    /* TODO : remove when sure that pending promotion is preserved across configuration changes.
    companion object {
        fun parse(valueStr: String): PendingPromotionData {
            return try {
                val parts = valueStr.split("|")
                PendingPromotionData(
                    pendingPromotion = parts[0].toBoolean(),
                    pendingPromotionForBlack = parts[1].toBoolean(),
                    pendingPromotionStartedInReversedMode = parts[2].toBoolean(),
                    pieceValue = parts[3][0],
                    startFile = parts[4].toInt(),
                    startRank = parts[5].toInt(),
                    targetFile = parts[6].toInt(),
                    targetRank = parts[7].toInt(),
                    movedPieceXRatio = parts[8].toFloat(),
                    movedPieceYRatio = parts[9].toFloat()
                )
            } catch (ex: NumberFormatException) {
                PendingPromotionData()
            } catch (ex: ArrayIndexOutOfBoundsException) {
                PendingPromotionData()
            }
        }
    }
     */
}

sealed class PromotionPiece(val fen: Char)

class PromotionQueen : PromotionPiece('q')
class PromotionRook : PromotionPiece('r')
class PromotionBishop : PromotionPiece('b')
class PromotionKnight : PromotionPiece('n')

/**
 * Be careful !  computerMoveRequestCallback may be called on each recomposition : you should check
 * that you have not its logic active before starting it again.
 */
@Composable
fun DynamicChessBoard(
    modifier: Modifier = Modifier,
    position: String,
    reversed: Boolean = false,
    lastMoveArrow: MoveData? = null,
    promotionState: PendingPromotionData = PendingPromotionData(),
    isValidMoveCallback: (String) -> Boolean = { _ -> false },
    dndMoveCallback: (MoveData) -> Unit = { _ -> },
    setPendingPromotionCallback: (PendingPromotionData) -> Unit = { _ -> },
    cancelPendingPromotionCallback: () -> Unit = { },
    promotionMoveCallback: (MoveData) -> Unit = { _ -> },
    gameInProgress: Boolean = false,
    computerMoveRequestCallback: (String) -> Unit = { _ -> },
    whiteSideType: PlayerType = PlayerType.Human,
    blackSideType: PlayerType = PlayerType.Human,
) {
    val composableScope = rememberCoroutineScope()

    val currentContext = LocalContext.current

    var dndState by rememberSaveable(stateSaver = DndDataStateSaver) {
        mutableStateOf(DndData())
    }

    var cellsSize by remember { mutableStateOf(0f) }

    fun isComputerTurn(): Boolean {
        val boardState = position.toBoard()
        return (boardState.turn && whiteSideType == PlayerType.Computer)
                || (!boardState.turn && blackSideType == PlayerType.Computer)
    }

    fun makeComputerMoveRequestIfAppropriate() {
        if (!gameInProgress) return

        if (!isComputerTurn()) return
        computerMoveRequestCallback(position)
    }


    fun commitPromotion(piece: PromotionPiece) {
        if (!gameInProgress) return

        val promotionFen = piece.fen.lowercaseChar()
        val moveData = MoveData(
            startFile = promotionState.startFile,
            startRank = promotionState.startRank,
            targetFile = promotionState.targetFile,
            targetRank = promotionState.targetRank,
            promotion = promotionFen,
        )
        promotionMoveCallback(moveData)
        setPendingPromotionCallback(PendingPromotionData())
    }

    fun cancelDragAndDropAnimation() {
        if (!gameInProgress) return

        val initialXRatio = dndState.movedPieceXRatio
        val initialYRatio = dndState.movedPieceYRatio

        val startCol = if (reversed) 7 - dndState.startFile else dndState.startFile
        val startRow = if (reversed) dndState.startRank else 7 - dndState.startRank

        val targetX = cellsSize * (0.5f + startCol)
        val targetY = cellsSize * (0.5f + startRow)
        val minBoardSize = cellsSize * 9f
        val targetXRatio = targetX / minBoardSize
        val targetYRatio = targetY / minBoardSize

        val animationDurationMillis = 250

        dndState = dndState.copy(
            targetFile = Int.MIN_VALUE,
            targetRank = Int.MIN_VALUE
        )

        val currentXRatio = Animatable(initialXRatio)
        val currentYRatio = Animatable(initialYRatio)

        composableScope.launch {
            currentXRatio.animateTo(
                targetXRatio,
                animationSpec = TweenSpec(
                    durationMillis = animationDurationMillis,
                    easing = LinearEasing
                )
            ) {
                dndState = dndState.copy(
                    movedPieceXRatio = value
                )
            }
        }

        composableScope.launch {
            currentYRatio.animateTo(
                targetYRatio,
                animationSpec = TweenSpec(
                    durationMillis = animationDurationMillis,
                    easing = LinearEasing
                )
            ) {
                dndState = dndState.copy(
                    movedPieceYRatio = value
                )
            }
        }

        composableScope.launch {
            delay(animationDurationMillis.toLong())
            dndState = DndData()
        }
    }

    fun cancelPendingPromotionAnimation() {
        if (!gameInProgress) return

        dndState = DndData(
            pieceValue = promotionState.pieceValue,
            startFile = promotionState.startFile,
            startRank = promotionState.startRank,
            targetFile = promotionState.targetFile,
            targetRank = promotionState.targetRank,
            movedPieceXRatio = promotionState.movedPieceXRatio,
            movedPieceYRatio = promotionState.movedPieceYRatio,
        )

        setPendingPromotionCallback(PendingPromotionData())

        cancelDragAndDropAnimation()
    }


    fun handleTap(offset: Offset) {
        if (!gameInProgress) return

        if (promotionState.pendingPromotion) {
            val promotionInBottomPart =
                (reversed && !promotionState.pendingPromotionForBlack) || (!reversed && promotionState.pendingPromotionForBlack)
            val minBoardSize = cellsSize * 9
            val buttonsY = minBoardSize * (if (promotionInBottomPart) 0.06f else 0.85f)
            val buttonsHalfSizeRatio = 0.575f
            val buttonsGapRatio = 0.2f
            val buttonsCenterY = buttonsY + cellsSize * buttonsHalfSizeRatio
            val queenButtonCenterX = minBoardSize * 0.18f + cellsSize * buttonsHalfSizeRatio
            val rookButtonCenterX =
                queenButtonCenterX + cellsSize * (2 * buttonsHalfSizeRatio + buttonsGapRatio)
            val bishopButtonCenterX =
                rookButtonCenterX + cellsSize * (2 * buttonsHalfSizeRatio + buttonsGapRatio)
            val knightButtonCenterX =
                bishopButtonCenterX + cellsSize * (2 * buttonsHalfSizeRatio + buttonsGapRatio)
            val cancelButtonCenterX =
                knightButtonCenterX + cellsSize * (2 * buttonsHalfSizeRatio + buttonsGapRatio)

            val queenButtonTapped = pointInCircle(
                pointX = offset.x.toDouble(),
                pointY = offset.y.toDouble(),
                circleCenterX = queenButtonCenterX.toDouble(),
                circleCenterY = buttonsCenterY.toDouble(),
                circleRadius = (cellsSize * buttonsHalfSizeRatio).toDouble()
            )

            val rookButtonTapped = pointInCircle(
                pointX = offset.x.toDouble(),
                pointY = offset.y.toDouble(),
                circleCenterX = rookButtonCenterX.toDouble(),
                circleCenterY = buttonsCenterY.toDouble(),
                circleRadius = (cellsSize * buttonsHalfSizeRatio).toDouble()
            )

            val bishopButtonTapped = pointInCircle(
                pointX = offset.x.toDouble(),
                pointY = offset.y.toDouble(),
                circleCenterX = bishopButtonCenterX.toDouble(),
                circleCenterY = buttonsCenterY.toDouble(),
                circleRadius = (cellsSize * buttonsHalfSizeRatio).toDouble()
            )

            val knightButtonTapped = pointInCircle(
                pointX = offset.x.toDouble(),
                pointY = offset.y.toDouble(),
                circleCenterX = knightButtonCenterX.toDouble(),
                circleCenterY = buttonsCenterY.toDouble(),
                circleRadius = (cellsSize * buttonsHalfSizeRatio).toDouble()
            )

            val cancelButtonTapped = pointInCircle(
                pointX = offset.x.toDouble(),
                pointY = offset.y.toDouble(),
                circleCenterX = cancelButtonCenterX.toDouble(),
                circleCenterY = buttonsCenterY.toDouble(),
                circleRadius = (cellsSize * buttonsHalfSizeRatio).toDouble()
            )

            when {
                queenButtonTapped -> commitPromotion(piece = PromotionQueen())
                rookButtonTapped -> commitPromotion(piece = PromotionRook())
                bishopButtonTapped -> commitPromotion(piece = PromotionBishop())
                knightButtonTapped -> commitPromotion(piece = PromotionKnight())
                cancelButtonTapped -> {
                    cancelPendingPromotionCallback()
                    dndState = dndState.copy(
                        targetFile = Int.MIN_VALUE,
                        targetRank = Int.MIN_VALUE,
                    )
                    cancelPendingPromotionAnimation()
                }
            }

        }
    }

    fun processDragAndDropStart(file: Int, rank: Int, piece: Char) {
        if (!gameInProgress) return
        if (promotionState.pendingPromotion) return

        val whiteTurn = position.toBoard().turn
        val isPieceOfSideToMove =
            (piece.isWhitePiece() && whiteTurn) ||
                    (!piece.isWhitePiece() && !whiteTurn)
        val humanTurn = (whiteTurn && whiteSideType == PlayerType.Human) || (!whiteTurn && blackSideType == PlayerType.Human)
        if (isPieceOfSideToMove && humanTurn) {
            val col = if (reversed) 7 - file else file
            val row = if (reversed) rank else 7 - rank
            val boardMinSize = cellsSize * 9f
            val newMovedPieceX = cellsSize * (0.5f + col.toFloat())
            val newMovedPieceY = cellsSize * (0.5f + row.toFloat())
            val newMovedPieceXRatio = newMovedPieceX / boardMinSize
            val newMovedPieceYRatio = newMovedPieceY / boardMinSize
            dndState = dndState.copy(
                startFile = file,
                startRank = rank,
                movedPieceXRatio = newMovedPieceXRatio,
                movedPieceYRatio = newMovedPieceYRatio,
                pieceValue = piece
            )
        }
    }

    fun dndMoveIsPromotion(): Boolean {
        return (dndState.targetRank == 0 &&
                dndState.pieceValue == 'p') ||
                (dndState.targetRank == 7 && dndState.pieceValue == 'P')
    }

    fun isValidDndMove(): Boolean {
        if (dndState.pieceValue == NO_PIECE) return false
        if (dndState.startFile < 0 || dndState.startFile > 7) return false
        if (dndState.startRank < 0 || dndState.startRank > 7) return false
        if (dndState.targetFile < 0 || dndState.targetFile > 7) return false
        if (dndState.targetFile < 0 || dndState.targetFile > 7) return false

        val promotionChar = if (dndMoveIsPromotion()) "Q" else ""
        val moveString =
            "${dndState.startFile.asFileChar()}${dndState.startRank.asRankChar()}" +
                    "${dndState.targetFile.asFileChar()}${dndState.targetRank.asRankChar()}$promotionChar"
        return isValidMoveCallback(moveString)
    }

    fun commitDndMove() {
        if (!gameInProgress) return
        if (!isValidDndMove()) return

        val moveData = MoveData(
            startFile = dndState.startFile,
            startRank = dndState.startRank,
            targetFile = dndState.targetFile,
            targetRank = dndState.targetRank,
        )

        dndMoveCallback(moveData)
        dndState = DndData()
    }


    fun handleDragStart(offset: Offset) {
        if (!gameInProgress) return
        if (promotionState.pendingPromotion) return

        val col = floor((offset.x - cellsSize * 0.5f) / cellsSize).toInt()
        val row = floor((offset.y - cellsSize * 0.5f) / cellsSize).toInt()

        val outOfBounds = col < 0 || col > 7 || row < 0 || row > 7
        if (!outOfBounds) {
            val file = if (reversed) 7 - col else col
            val rank = if (reversed) row else 7 - row
            val square = getSquareFromCellCoordinates(file, rank)
            val piece = position.toBoard().getPieceAt(square)

            if (piece != NO_PIECE) {
                processDragAndDropStart(file, rank, piece)
            }
        }
    }

    fun handleDragMove(change: PointerInputChange, dragAmount: Offset) {
        if (!gameInProgress) return
        if (promotionState.pendingPromotion) return

        change.consumeAllChanges()

        if (dndState.pieceValue != NO_PIECE) {
            val boardMinSize = cellsSize * 9f
            val newMovedPieceX = dndState.movedPieceXRatio * boardMinSize + dragAmount.x
            val newMovedPieceY = dndState.movedPieceYRatio * boardMinSize + dragAmount.y
            val newMovedPieceXRatio = newMovedPieceX / boardMinSize
            val newMovedPieceYRatio = newMovedPieceY / boardMinSize
            val newCol = floor((newMovedPieceX - cellsSize * 0.5f) / cellsSize).toInt()
            val newRow = floor((newMovedPieceY - cellsSize * 0.5f) / cellsSize).toInt()
            val targetFile = if (reversed) 7 - newCol else newCol
            val targetRank = if (reversed) newRow else 7 - newRow
            dndState = dndState.copy(
                movedPieceXRatio = newMovedPieceXRatio,
                movedPieceYRatio = newMovedPieceYRatio,
                targetFile = targetFile,
                targetRank = targetRank
            )
        }
    }

    fun handleDndCancel() {
        if (!gameInProgress) return
        if (promotionState.pendingPromotion) return

        cancelDragAndDropAnimation()
    }

    fun handleDndValidation() {
        if (!gameInProgress) return
        if (promotionState.pendingPromotion) return

        if (isValidDndMove()) {
            if (dndMoveIsPromotion()) {
                setPendingPromotionCallback(
                    promotionState.copy(
                        pendingPromotion = true,
                        pendingPromotionForBlack = !position.toBoard().turn,
                        pendingPromotionStartedInReversedMode = reversed,
                        pieceValue = dndState.pieceValue,
                        startFile = dndState.startFile,
                        startRank = dndState.startRank,
                        targetFile = dndState.targetFile,
                        targetRank = dndState.targetRank,
                        movedPieceXRatio = dndState.movedPieceXRatio,
                        movedPieceYRatio = dndState.movedPieceYRatio,
                    )
                )
                dndState = DndData()

            } else {
                commitDndMove()
            }
        } else {
            cancelDragAndDropAnimation()
        }
    }

    makeComputerMoveRequestIfAppropriate()


    Canvas(
        modifier = modifier
            .background(Color(214, 59, 96))
            .pointerInput(reversed, gameInProgress, position, promotionState) {
                detectDragGestures(
                    onDragStart = { handleDragStart(it) },
                    onDrag = { change, dragAmount ->
                        handleDragMove(change, dragAmount)
                    },
                    onDragCancel = { handleDndCancel() },
                    onDragEnd = { handleDndValidation() },
                )
            }
            .pointerInput(reversed, gameInProgress, position, promotionState) {
                detectTapGestures(
                    onTap = { handleTap(it) }
                )
            }
    ) {
        val minSize = if (size.width < size.height) size.width else size.height
        cellsSize = minSize / 9f
        drawCells(cellsSize = cellsSize, reversed = reversed, dndData = dndState)
        drawLastMoveArrow(lastMoveArrow, cellsSize, reversed)
        drawPendingPromotionCells(
            cellsSize = cellsSize,
            reversed = reversed,
            promotionData = promotionState
        )
        drawFilesCoordinates(cellsSize, reversed)
        drawRanksCoordinates(cellsSize, reversed)
        drawPlayerTurn(cellsSize, position.toBoard())
        drawPieces(
            context = currentContext,
            cellsSize = cellsSize,
            position = position.toBoard(),
            reversed = reversed,
            dndData = dndState,
            promotionData = promotionState,
        )

        if (dndState.pieceValue != NO_PIECE) {
            val boardMinSize = cellsSize * 9f
            val x = boardMinSize * dndState.movedPieceXRatio
            val y = boardMinSize * dndState.movedPieceYRatio

            drawMovedPiece(
                context = currentContext,
                cellsSize = cellsSize.toInt(),
                pieceValue = dndState.pieceValue,
                x = x,
                y = y,
                positionFen = position,
            )
        } else if (promotionState.pieceValue != NO_PIECE) {
            val boardMinSize = cellsSize * 9f
            var x = boardMinSize * promotionState.movedPieceXRatio
            var y = boardMinSize * promotionState.movedPieceYRatio

            val notInitialReversedMode =
                reversed != promotionState.pendingPromotionStartedInReversedMode
            if (notInitialReversedMode) {
                x = boardMinSize - cellsSize - x
                y = boardMinSize - cellsSize - y
            }

            drawMovedPiece(
                context = currentContext,
                cellsSize = cellsSize.toInt(),
                pieceValue = promotionState.pieceValue,
                x = x,
                y = y,
                positionFen = position,
            )
        }

        if (promotionState.pendingPromotion) {
            val promotionInBottomPart =
                (reversed && !promotionState.pendingPromotionForBlack) || (!reversed && promotionState.pendingPromotionForBlack)
            val itemsZoneX = minSize * 0.18f
            val itemsZoneY = minSize * (if (promotionInBottomPart) 0.06f else 0.85f)
            val itemsSize = cellsSize * 1.15f
            val spaceBetweenItems = cellsSize * 0.2f

            drawPromotionValidationZone(
                context = currentContext,
                x = itemsZoneX,
                y = itemsZoneY,
                itemsSize = itemsSize,
                isWhiteTurn = position.toBoard().turn,
                spaceBetweenItems = spaceBetweenItems,
            )
        }

    }
}

@Composable
fun StaticChessBoard(
    modifier: Modifier = Modifier,
    position: String = STANDARD_FEN,
    reversed: Boolean = false,
    lastMoveArrow: MoveData? = null,
) {
    val boardLogic = position.toBoard()
    val currentContext = LocalContext.current

    Canvas(
        modifier = modifier
            .background(Color(214, 59, 96))
    ) {
        val minSize = if (size.width < size.height) size.width else size.height
        val cellsSize = minSize / 9f
        drawCells(cellsSize)
        drawLastMoveArrow(lastMoveArrow, cellsSize, reversed)
        drawFilesCoordinates(cellsSize, reversed)
        drawRanksCoordinates(cellsSize, reversed)
        drawPlayerTurn(cellsSize, boardLogic)
        drawPieces(
            context = currentContext,
            cellsSize = cellsSize,
            position = boardLogic,
            reversed = reversed,
        )

    }
}

private const val NO_PIECE = '.'

private fun Char.isWhitePiece(): Boolean {
    return when (this) {
        'P', 'N', 'B', 'R', 'Q', 'K' -> true
        'p', 'n', 'b', 'r', 'q', 'k' -> false
        else -> throw IllegalArgumentException("Not a valid piece : $this !")
    }
}

private fun Int.asFileChar(): Char {
    return when (this) {
        0 -> 'a'
        1 -> 'b'
        2 -> 'c'
        3 -> 'd'
        4 -> 'e'
        5 -> 'f'
        6 -> 'g'
        7 -> 'h'
        else -> 'X'
    }
}

private fun Int.asRankChar(): Char {
    return when (this) {
        0 -> '1'
        1 -> '2'
        2 -> '3'
        3 -> '4'
        4 -> '5'
        5 -> '6'
        6 -> '7'
        7 -> '8'
        else -> 'X'
    }
}


private fun Char.getPieceImageID(): Int {
    return when (this) {
        'P' -> R.drawable.ic_chess_plt45
        'N' -> R.drawable.ic_chess_nlt45
        'B' -> R.drawable.ic_chess_blt45
        'R' -> R.drawable.ic_chess_rlt45
        'Q' -> R.drawable.ic_chess_qlt45
        'K' -> R.drawable.ic_chess_klt45
        'p' -> R.drawable.ic_chess_pdt45
        'n' -> R.drawable.ic_chess_ndt45
        'b' -> R.drawable.ic_chess_bdt45
        'r' -> R.drawable.ic_chess_rdt45
        'q' -> R.drawable.ic_chess_qdt45
        'k' -> R.drawable.ic_chess_kdt45
        else -> throw RuntimeException("Not a valid piece: $this !")
    }
}

private data class DndData(
    val pieceValue: Char = NO_PIECE,
    val startFile: Int = Int.MIN_VALUE,
    val startRank: Int = Int.MIN_VALUE,
    val targetFile: Int = Int.MIN_VALUE,
    val targetRank: Int = Int.MIN_VALUE,
    val movedPieceXRatio: Float = Float.MIN_VALUE,
    val movedPieceYRatio: Float = Float.MAX_VALUE,
) {
    override fun toString(): String = "$pieceValue|$startFile|$startRank|$targetFile|$targetRank|" +
            "$movedPieceXRatio|$movedPieceYRatio"

    companion object {
        fun parse(valueStr: String): DndData {
            try {
                val parts = valueStr.split("|")
                return DndData(
                    pieceValue = parts[0][0],
                    startFile = parts[1].toInt(),
                    startRank = parts[2].toInt(),
                    targetFile = parts[3].toInt(),
                    targetRank = parts[4].toInt(),
                    movedPieceXRatio = parts[5].toFloat(),
                    movedPieceYRatio = parts[6].toFloat(),
                )
            } catch (ex: NumberFormatException) {
                return DndData()
            } catch (ex: ArrayIndexOutOfBoundsException) {
                return DndData()
            }
        }
    }
}


private val DndDataStateSaver = Saver<DndData, String>(
    save = { state -> state.toString() },
    restore = { value ->
        DndData.parse(value)
    }
)

private fun getSquareFromCellCoordinates(file: Int, rank: Int): Long {
    return 1L.shl(7 - file + 8 * rank)
}

private fun DrawScope.drawCells(
    cellsSize: Float,
    reversed: Boolean = false,
    dndData: DndData = DndData()
) {
    repeat(8) { row ->
        val rank = if (reversed) row else 7 - row
        repeat(8) { col ->
            val file = if (reversed) 7 - col else col
            val isDndStartCell = (file == dndData.startFile) && (rank == dndData.startRank)
            val isDndTargetCell = (file == dndData.targetFile) && (rank == dndData.targetRank)
            val isDndCrossCell = (file == dndData.targetFile) || (rank == dndData.targetRank)
            val isWhiteCell = (row + col) % 2 == 0
            val backgroundColor =
                when {
                    isDndTargetCell -> Color(112, 209, 35)
                    isDndStartCell -> Color(214, 59, 96)
                    isDndCrossCell -> Color(178, 46, 230)
                    isWhiteCell -> Color(255, 206, 158)
                    else -> Color(209, 139, 71)
                }
            val x = cellsSize * (0.5f + col)
            val y = cellsSize * (0.5f + row)
            drawRect(
                color = backgroundColor,
                topLeft = Offset(x, y),
                size = Size(cellsSize, cellsSize)
            )
        }
    }
}

private fun DrawScope.drawPendingPromotionCells(
    cellsSize: Float,
    reversed: Boolean = false,
    promotionData: PendingPromotionData = PendingPromotionData()
) {
    if (promotionData.pendingPromotion) {
        repeat(8) { row ->
            val rank = if (reversed) row else 7 - row
            repeat(8) { col ->
                val file = if (reversed) 7 - col else col
                val isDndStartCell =
                    (file == promotionData.startFile) && (rank == promotionData.startRank)
                val isDndTargetCell =
                    (file == promotionData.targetFile) && (rank == promotionData.targetRank)
                val isDndCrossCell =
                    (file == promotionData.targetFile) || (rank == promotionData.targetRank)
                val isWhiteCell = (row + col) % 2 == 0
                val backgroundColor =
                    when {
                        isDndTargetCell -> Color(112, 209, 35)
                        isDndStartCell -> Color(214, 59, 96)
                        isDndCrossCell -> Color(178, 46, 230)
                        isWhiteCell -> Color(255, 206, 158)
                        else -> Color(209, 139, 71)
                    }
                val x = cellsSize * (0.5f + col)
                val y = cellsSize * (0.5f + row)
                drawRect(
                    color = backgroundColor,
                    topLeft = Offset(x, y),
                    size = Size(cellsSize, cellsSize)
                )
            }
        }
    }
}

private fun DrawScope.drawLastMoveArrow(arrowData: MoveData?, cellsSize: Float, reversed: Boolean) {
    if (arrowData == null) return

    val points = computeArrowBaseCoordinates(arrowData, cellsSize, reversed)
    drawArrowBaseLine(points, cellsSize)
}

private fun computeArrowBaseCoordinates(arrowData: MoveData, cellsSize: Float, reversed: Boolean) : Array<Float> {
    val fromCol = if (reversed)  7 - arrowData.startFile else arrowData.startFile
    val fromRow = if (reversed)  arrowData.startRank else 7 - arrowData.startRank
    val toCol = if (reversed)  7 - arrowData.targetFile else arrowData.targetFile
    val toRow = if (reversed)  arrowData.targetRank else 7 - arrowData.targetRank

    val ax = (cellsSize * (fromCol + 1.0)).toFloat()
    val ay = (cellsSize * (fromRow + 1.0)).toFloat()
    val bx = (cellsSize * (toCol + 1.0)).toFloat()
    val by = (cellsSize * (toRow + 1.0)).toFloat()

    return arrayOf(ax, ay, bx, by)
}

private fun DrawScope.drawArrowBaseLine(points: Array<Float>, cellsSize: Float) {
    val (ax, ay, bx, by) = points
    val halfThickness = cellsSize * 0.08

    val realAx = (ax - halfThickness).toFloat()
    val realBx = (bx - halfThickness).toFloat()

    val brush = SolidColor(Color.Magenta)
    val startOffset = Offset(realAx, ay)
    val endOffset = Offset(realBx ,by)
    val strokeWidth = (2*halfThickness).toFloat()

    drawLine(brush = brush, start = startOffset, end = endOffset, strokeWidth = strokeWidth)
}

private fun DrawScope.drawPlayerTurn(
    cellsSize: Float,
    positionState: Board
) {
    val whiteTurn = positionState.turn
    val turnRadius = cellsSize * 0.25f
    val turnColor = if (whiteTurn) Color.White else Color.Black
    val location = cellsSize * 8.75f
    drawCircle(color = turnColor, radius = turnRadius, center = Offset(location, location))
}

private fun DrawScope.drawFilesCoordinates(
    cellsSize: Float, reversed: Boolean
) {
    val fontSize = cellsSize * 0.3f
    repeat(8) { col ->
        val file = if (reversed) 7 - col else col
        val coordinateText = "${('A'.code + file).toChar()}"
        val x = cellsSize * (0.90f + col)
        val y1 = cellsSize * 0.375f
        val y2 = cellsSize * 8.875f

        drawIntoCanvas {
            val paint = Paint().apply {
                setARGB(255, 255, 199, 0)
                textSize = fontSize
            }
            it.nativeCanvas.drawText(coordinateText, x, y1, paint)
            it.nativeCanvas.drawText(coordinateText, x, y2, paint)
        }
    }
}

private fun DrawScope.drawRanksCoordinates(
    cellsSize: Float, reversed: Boolean
) {
    val fontSize = cellsSize * 0.3f
    repeat(8) { row ->
        val rank = if (reversed) row else 7 - row
        val coordinateText = "${('1'.code + rank).toChar()}"
        val y = cellsSize * (1.125f + row)
        val x1 = cellsSize * 0.15f
        val x2 = cellsSize * 8.65f

        drawIntoCanvas {
            val paint = Paint().apply {
                setARGB(255, 255, 199, 0)
                textSize = fontSize
            }
            it.nativeCanvas.drawText(coordinateText, x1, y, paint)
            it.nativeCanvas.drawText(coordinateText, x2, y, paint)
        }
    }
}

private fun DrawScope.drawPieces(
    context: Context,
    cellsSize: Float,
    position: Board,
    reversed: Boolean,
    dndData: DndData = DndData(),
    promotionData: PendingPromotionData = PendingPromotionData(),
) {
    repeat(8) { row ->
        val rank = if (reversed) row else 7 - row
        repeat(8) { col ->
            val file = if (reversed) 7 - col else col

            val square = getSquareFromCellCoordinates(file, rank)
            val piece = position.getPieceAt(square)
            if (piece != NO_PIECE) {
                val isDraggedPiece = (dndData.startFile == file) && (dndData.startRank == rank)
                val isPendingPromotionPiece =
                    (promotionData.startFile == file) && (promotionData.startRank == rank)
                if (!isDraggedPiece && !isPendingPromotionPiece) {
                    val x = cellsSize * (0.5f + col)
                    val y = cellsSize * (0.5f + row)
                    val imageRef = piece.getPieceImageID()
                    val vectorDrawable =
                        VectorDrawableCompat.create(context.resources, imageRef, null)
                    drawIntoCanvas {
                        if (vectorDrawable != null) it.nativeCanvas.drawVector(
                            vectorDrawable,
                            x,
                            y,
                            cellsSize.toInt(),
                            cellsSize.toInt()
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMovedPiece(
    context: Context,
    cellsSize: Int,
    pieceValue: Char,
    x: Float,
    y: Float,
    positionFen: String
) {
    val boardLogic = Board()
    boardLogic.fen = positionFen
    val imageRef = pieceValue.getPieceImageID()

    val vectorDrawable =
        VectorDrawableCompat.create(context.resources, imageRef, null)
    drawIntoCanvas {
        if (vectorDrawable != null) it.nativeCanvas.drawVector(
            vectorDrawable,
            x,
            y,
            cellsSize,
            cellsSize
        )
    }
}

private fun Canvas.drawVector(
    drawable: VectorDrawableCompat,
    x: Float,
    y: Float,
    width: Int,
    height: Int
) {
    drawable.setBounds(0, 0, width, height)
    save()
    translate(x, y)
    drawable.draw(this)
    restore()
}

private fun DrawScope.drawPromotionValidationZone(
    context: Context,
    x: Float,
    y: Float,
    itemsSize: Float,
    spaceBetweenItems: Float,
    isWhiteTurn: Boolean,
) {
    val xRook = x + itemsSize + spaceBetweenItems
    val xBishop = xRook + itemsSize + spaceBetweenItems
    val xKnight = xBishop + itemsSize + spaceBetweenItems
    val xCancellation = xKnight + itemsSize + spaceBetweenItems
    drawPromotionValidationItem(context, x, y, itemsSize, PromotionQueen(), whiteTurn = isWhiteTurn)
    drawPromotionValidationItem(
        context,
        xRook,
        y,
        itemsSize,
        PromotionRook(),
        whiteTurn = isWhiteTurn
    )
    drawPromotionValidationItem(
        context,
        xBishop,
        y,
        itemsSize,
        PromotionBishop(),
        whiteTurn = isWhiteTurn
    )
    drawPromotionValidationItem(
        context,
        xKnight,
        y,
        itemsSize,
        PromotionKnight(),
        whiteTurn = isWhiteTurn
    )
    drawPromotionCancellationItem(
        context,
        xCancellation,
        y,
        itemsSize,
        isWhiteTurn = isWhiteTurn
    )
}

fun DrawScope.drawPromotionValidationItem(
    context: Context,
    x: Float,
    y: Float,
    size: Float,
    pieceValue: PromotionPiece,
    whiteTurn: Boolean,
) {
    val ovalPaint = Paint().apply {
        if (whiteTurn) setARGB(255, 0, 0, 0)
        else setARGB(255, 255, 255, 255)
        style = Paint.Style.FILL
    }
    val ovalX = x - size * 0.15f
    val ovalY = y - size * 0.15f

    val pieceFen = if (whiteTurn) pieceValue.fen.uppercaseChar() else pieceValue.fen.lowercaseChar()
    val imageRef = pieceFen.getPieceImageID()
    val vectorDrawable =
        VectorDrawableCompat.create(context.resources, imageRef, null)

    val imageSize = size * 0.7f
    drawIntoCanvas {
        it.nativeCanvas.drawOval(ovalX, ovalY, ovalX + size, ovalY + size, ovalPaint)
        if (vectorDrawable != null) it.nativeCanvas.drawVector(
            vectorDrawable,
            x,
            y,
            imageSize.toInt(),
            imageSize.toInt()
        )
    }
}

fun DrawScope.drawPromotionCancellationItem(
    context: Context,
    x: Float,
    y: Float,
    size: Float,
    isWhiteTurn: Boolean,
) {
    val ovalPaint = Paint().apply {
        if (isWhiteTurn) setARGB(255, 0, 0, 0)
        else setARGB(255, 255, 255, 255)
        style = Paint.Style.FILL
    }
    val ovalX = x - size * 0.15f
    val ovalY = y - size * 0.15f


    val imageRef = R.drawable.ic_red_cross
    val vectorDrawable =
        VectorDrawableCompat.create(context.resources, imageRef, null)
    val imageSize = size * 0.7f
    drawIntoCanvas {
        it.nativeCanvas.drawOval(ovalX, ovalY, ovalX + size, ovalY + size, ovalPaint)
        if (vectorDrawable != null) it.nativeCanvas.drawVector(
            vectorDrawable,
            x,
            y,
            imageSize.toInt(),
            imageSize.toInt()
        )
    }
}

private fun pointInCircle(
    pointX: Double,
    pointY: Double,
    circleCenterX: Double,
    circleCenterY: Double,
    circleRadius: Double
): Boolean {
    val distance = sqrt(
        (pointX - circleCenterX) * (pointX - circleCenterX) +
                (pointY - circleCenterY) * (pointY - circleCenterY)
    )
    return distance <= circleRadius
}

@Preview
@Composable
fun DynamicChessBoardPreview() {
    DynamicChessBoard(modifier = Modifier.size(300.dp), position = STANDARD_FEN)
}

@Preview
@Composable
fun DynamicReversedChessBoardPreview() {
    DynamicChessBoard(modifier = Modifier.size(300.dp), reversed = true, position = STANDARD_FEN)
}

@Preview
@Composable
fun DynamicChessBoardCustomPositionPreview() {
    DynamicChessBoard(
        modifier = Modifier.size(300.dp),
        position = STANDARD_FEN,
        //startPosition = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
    )
}

@Preview
@Composable
fun DynamicChessBoardCustomPositionReversedPreview() {
    DynamicChessBoard(
        modifier = Modifier.size(300.dp),
        reversed = true,
        position = STANDARD_FEN,
        //startPosition = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
    )
}

@Preview
@Composable
fun StaticChessBoardPreview() {
    StaticChessBoard(modifier = Modifier.size(300.dp))
}

@Preview
@Composable
fun StaticChessBoardReversedPreview() {
    StaticChessBoard(modifier = Modifier.size(300.dp), reversed = true)
}