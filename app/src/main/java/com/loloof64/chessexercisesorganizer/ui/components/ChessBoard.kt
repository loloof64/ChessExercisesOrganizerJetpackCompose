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


class PositionHandler(
    private val startPosition: String = STANDARD_FEN
) {
    private var boardLogic = EMPTY_FEN.toBoard()

    fun getCurrentPosition(): String = boardLogic.fen!!

    fun newGame() {
        boardLogic = startPosition.toBoard()
    }

    fun makeMove(moveStr: String) {
        val move = Move.getFromString(boardLogic, moveStr, true)
        boardLogic.doMove(move, true, true)
    }

    fun isValidMove(moveStr: String): Boolean {
        val boardCopy = boardLogic.fen.toBoard()
        val move = Move.getFromString(boardCopy, moveStr, true)
        return boardCopy.doMove(move, true, true)
    }

    fun getNaturalEndGameStatus(): GameEndedStatus {
        return when  {
            boardLogic.isMate -> if (boardLogic.turn) GameEndedStatus.CHECKMATE_BLACK else GameEndedStatus.CHECKMATE_WHITE
            boardLogic.isStalemate -> GameEndedStatus.STALEMATE
            boardLogic.isDrawByThreeFoldRepetitions -> GameEndedStatus.DRAW_THREE_FOLD_REPETITION
            boardLogic.isDrawByFiftyMovesRule -> GameEndedStatus.DRAW_FIFTY_MOVES_RULE
            boardLogic.isDrawByMissingMaterial -> GameEndedStatus.DRAW_MISSING_MATERIAL
            else -> GameEndedStatus.NOT_ENDED
        }
    }

    fun serialize(): String {
        val serializedBoard = serializeBoard(boardLogic)
        return "$startPosition<>$serializedBoard"
    }

    companion object {
        fun deserialize(value: String): PositionHandler {
            val parts = value.split("<>")
            val startPosition = parts[0]
            val newBoardLogic = deserializeBoard(parts[1])

            return PositionHandler(startPosition).apply {
                boardLogic = newBoardLogic
            }
        }
    }
}

val PositionHandlerSaver = Saver<PositionHandler, String>(
    save = { it.serialize() },
    restore = { PositionHandler.deserialize(it) }
)

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
}

val PendingPromotionStateSaver = Saver<PendingPromotionData, String>(
    save = { it.toString() },
    restore = { PendingPromotionData.parse(it) }
)

sealed class PromotionPiece(val fen: Char)

class PromotionQueen : PromotionPiece('q')
class PromotionRook : PromotionPiece('r')
class PromotionBishop : PromotionPiece('b')
class PromotionKnight : PromotionPiece('n')

@Composable
fun DynamicChessBoard(
    modifier: Modifier = Modifier,
    reversed: Boolean = false,
    position: String = PositionHandler().getCurrentPosition(),
    promotionState: PendingPromotionData = PendingPromotionData(),
    validMoveCallback: (String) -> Boolean = { _ -> false },
    dndMoveCallback: (String) -> Unit = { _ -> },
    setPendingPromotionCallback: (PendingPromotionData) -> Unit = { _ -> },
    cancelPendingPromotionCallback: () -> Unit = { },
    promotionMoveCallback: (String) -> Unit = { _ -> },
    gameInProgress: Boolean = false,
) {
    val composableScope = rememberCoroutineScope()

    val currentContext = LocalContext.current

    var dndState by rememberSaveable(stateSaver = DndDataStateSaver) {
        mutableStateOf(DndData())
    }

    var cellsSize by remember { mutableStateOf(0f) }

    fun commitPromotion(piece: PromotionPiece) {
        if (!gameInProgress) return

        val promotionFen = piece.fen.toLowerCase()
        val moveString =
            "${promotionState.startFile.asFileChar()}${promotionState.startRank.asRankChar()}" +
                    "${promotionState.targetFile.asFileChar()}${promotionState.targetRank.asRankChar()}$promotionFen"
        promotionMoveCallback(moveString)
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
        if (isPieceOfSideToMove) {
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
        return validMoveCallback(moveString)
    }

    fun commitDndMove() {
        if (!gameInProgress) return
        if (!isValidDndMove()) return

        val moveString =
            "${dndState.startFile.asFileChar()}${dndState.startRank.asRankChar()}" +
                    "${dndState.targetFile.asFileChar()}${dndState.targetRank.asRankChar()}"

        dndMoveCallback(moveString)
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

private fun serializeBoard(board: Board): String {
    val positionPart = board.fen
    val keyHistoryPart = board.keyHistory.joinToString(separator = "@") {
        "${it[0]};${it[1]}"
    }
    val keyPart = "${board.key[0]};${board.key[1]}"
    return "$positionPart|$keyHistoryPart|$keyPart"
}

private fun deserializeBoard(input: String): Board {
    val parts = input.split("|")
    val fen = parts[0]
    val keyHistory = parts[1].split('@').map { entryPairString ->
        val entryPair = entryPairString.split(';')
        val whitePart = entryPair[0].toLong()
        val blackPart = entryPair[1].toLong()
        arrayOf(whitePart, blackPart).toLongArray()
    }.toTypedArray()
    val keyParts = parts[2].split(';')
    val key = arrayOf(keyParts[0].toLong(), keyParts[1].toLong()).toLongArray()
    return fen.toBoard().apply {
        this.keyHistory = keyHistory
        this.key = key
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
        val coordinateText = "${('A'.toInt() + file).toChar()}"
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
        val coordinateText = "${('1'.toInt() + rank).toChar()}"
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

    val pieceFen = if (whiteTurn) pieceValue.fen.toUpperCase() else pieceValue.fen.toLowerCase()
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
    DynamicChessBoard(modifier = Modifier.size(300.dp))
}

@Preview
@Composable
fun DynamicReversedChessBoardPreview() {
    DynamicChessBoard(modifier = Modifier.size(300.dp), reversed = true)
}

@Preview
@Composable
fun DynamicChessBoardCustomPositionPreview() {
    DynamicChessBoard(
        modifier = Modifier.size(300.dp),
        //startPosition = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
    )
}

@Preview
@Composable
fun DynamicChessBoardCustomPositionReversedPreview() {
    DynamicChessBoard(
        modifier = Modifier.size(300.dp),
        reversed = true,
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