package com.loloof64.chessexercisesorganizer.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
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
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.alonsoruibal.chess.Board
import com.alonsoruibal.chess.Move
import com.loloof64.chessexercisesorganizer.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.random.Random

const val STANDARD_FEN = Board.FEN_START_POSITION

const val NO_PIECE = '.'

enum class GameEndedStatus {
    GOING_ON,
    USER_STOPPED,
    CHECKMATE_WHITE,
    CHECKMATE_BLACK,
    STALEMATE,
    DRAW_THREE_FOLD_REPETITION,
    DRAW_FIFTY_MOVES_RULE,
    DRAW_MISSING_MATERIAL,
}

fun Char.getPieceImageID(): Int {
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

data class DndData(
    val pieceValue: Char = NO_PIECE,
    val startFile: Int = Int.MIN_VALUE,
    val startRank: Int = Int.MIN_VALUE,
    val targetFile: Int = Int.MIN_VALUE,
    val targetRank: Int = Int.MIN_VALUE,
    val movedPieceX: Float = Float.MIN_VALUE,
    val movedPieceY: Float = Float.MIN_VALUE,
    val pendingPromotion: Boolean = false,
    val pendingPromotionForBlack: Boolean = false,
) {
    override fun toString(): String = "$pieceValue|$startFile|$startRank|$targetFile|$targetRank|" +
            "$movedPieceX|$movedPieceY|$pendingPromotion"

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
                    movedPieceX = parts[5].toFloat(),
                    movedPieceY = parts[6].toFloat(),
                    pendingPromotion = parts[7].toBoolean()
                )
            } catch (ex: NumberFormatException) {
                return DndData()
            } catch (ex: ArrayIndexOutOfBoundsException) {
                return DndData()
            }
        }
    }
}

val DndDataStateSaver = Saver<DndData, String>(
    save = { state -> state.toString() },
    restore = { value ->
        DndData.parse(value)
    }
)

fun serializeBoard(board: Board): String {
    val positionPart = board.fen
    val keyHistoryPart = board.keyHistory.joinToString(separator = "@") {
        "${it[0]};${it[1]}"
    }
    val keyPart = "${board.key[0]};${board.key[1]}"
    return "$positionPart|$keyHistoryPart|$keyPart"
}

fun deserializeBoard(input: String): Board {
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

val BoardStateSaver = Saver<Board, String>(
    save = { state -> serializeBoard(state) },
    restore = { value ->
        deserializeBoard(value)
    }
)

sealed class PromotionPiece(val baseFen: Char, val isWhiteTurn: Boolean) {
    val fen = if (isWhiteTurn) baseFen.toUpperCase() else baseFen.toLowerCase()
}

class PromotionQueen(isWhiteTurn: Boolean) :
    PromotionPiece(baseFen = 'Q', isWhiteTurn = isWhiteTurn)

class PromotionRook(isWhiteTurn: Boolean) : PromotionPiece('R', isWhiteTurn = isWhiteTurn)
class PromotionBishop(isWhiteTurn: Boolean) : PromotionPiece('B', isWhiteTurn = isWhiteTurn)
class PromotionKnight(isWhiteTurn: Boolean) : PromotionPiece('N', isWhiteTurn = isWhiteTurn)

fun Char.isWhitePiece(): Boolean {
    return when (this) {
        'P', 'N', 'B', 'R', 'Q', 'K' -> true
        'p', 'n', 'b', 'r', 'q', 'k' -> false
        else -> throw IllegalArgumentException("Not a valid piece : $this !")
    }
}

fun Int.asFileChar(): Char {
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

fun Int.asRankChar(): Char {
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

fun String.toBoard(): Board {
    val position = this
    return Board().apply {
        fen = position
    }
}

enum class PlayerType {
    Human,
    Computer
}

@Composable
fun DynamicChessBoard(
    modifier: Modifier = Modifier,
    startPosition: String = STANDARD_FEN,
    reversed: Boolean = false,
    userRequestStopGame: Boolean = false,
    gameId: Long = 1L,
    positionChangedCallback: (String) -> Unit = { _ -> },
    whiteSideType: PlayerType = PlayerType.Human,
    blackSideType: PlayerType = PlayerType.Human,
) {
    val context = LocalContext.current

    var previousGameId by rememberSaveable {
        mutableStateOf(gameId)
    }

    val composableScope = rememberCoroutineScope()
    var boardState by rememberSaveable(stateSaver = BoardStateSaver) {
        mutableStateOf(startPosition.toBoard())
    }

    fun isComputerTurn() = false/*(boardState.turn && whiteSideType == PlayerType.Computer)
            || (!boardState.turn && blackSideType == PlayerType.Computer)*/

    var dndState by rememberSaveable(stateSaver = DndDataStateSaver) { mutableStateOf(DndData()) }

    var gameEnded by rememberSaveable {
        mutableStateOf(GameEndedStatus.GOING_ON)
    }

    var cellsSize by remember { mutableStateOf(0f) }

    fun notifyUserGameFinished() {
        val messageId = when (gameEnded) {
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
    }

    fun manageEndStatus(gameEndedCallback: () -> Unit = {}) {
        when {
            boardState.isMate -> {
                gameEnded =
                    if (boardState.turn) GameEndedStatus.CHECKMATE_BLACK else GameEndedStatus.CHECKMATE_WHITE
                gameEndedCallback()
            }
            boardState.isStalemate -> {
                gameEnded = GameEndedStatus.STALEMATE
                gameEndedCallback()
            }
            boardState.isDrawByThreeFoldRepetitions -> {
                gameEnded = GameEndedStatus.DRAW_THREE_FOLD_REPETITION
                gameEndedCallback()
            }
            boardState.isDrawByFiftyMovesRule -> {
                gameEnded = GameEndedStatus.DRAW_FIFTY_MOVES_RULE
                gameEndedCallback()
            }
            boardState.isDrawByMissingMaterial -> {
                gameEnded = GameEndedStatus.DRAW_MISSING_MATERIAL
                gameEndedCallback()
            }
            userRequestStopGame -> {
                gameEnded = GameEndedStatus.USER_STOPPED
                gameEndedCallback()
            }
        }
    }

    fun cancelDragAndDrop() {
        val initialX = dndState.movedPieceX
        val initialY = dndState.movedPieceY

        val startCol = if (reversed) 7 - dndState.startFile else dndState.startFile
        val startRow = if (reversed) dndState.startRank else 7 - dndState.startRank

        val targetX = cellsSize * (0.5f + startCol)
        val targetY = cellsSize * (0.5f + startRow)

        val animationDurationMillis = 150

        dndState = dndState.copy(
            targetFile = Int.MIN_VALUE,
            targetRank = Int.MIN_VALUE
        )

        composableScope.launch {
            animate(
                initialValue = initialX,
                targetValue = targetX,
                animationSpec = tween(
                    durationMillis = animationDurationMillis,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                dndState = dndState.copy(movedPieceX = value)
            }
        }

        composableScope.launch {
            animate(
                initialValue = initialY,
                targetValue = targetY,
                animationSpec = tween(
                    durationMillis = animationDurationMillis,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                dndState = dndState.copy(movedPieceY = value)
            }
        }


        composableScope.launch {
            delay(animationDurationMillis.toLong())
            dndState = DndData()
        }
    }

    fun dndStartCallback(file: Int, rank: Int, piece: Char) {
        if (isComputerTurn()) return
        if (dndState.pendingPromotion) return

        val whiteTurn = boardState.turn
        val isPieceOfSideToMove =
            (piece.isWhitePiece() && whiteTurn) ||
                    (!piece.isWhitePiece() && !whiteTurn)
        if (isPieceOfSideToMove) {
            val col = if (reversed) 7 - file else file
            val row = if (reversed) rank else 7 - rank
            dndState = dndState.copy(
                startFile = file,
                startRank = rank,
                movedPieceX = cellsSize * (0.5f + col.toFloat()),
                movedPieceY = cellsSize * (0.5f + row.toFloat()),
                pieceValue = piece
            )
        }
    }

    fun dndMoveCallback(xOffset: Float, yOffset: Float) {
        if (gameEnded != GameEndedStatus.GOING_ON) return
        if (isComputerTurn()) return
        if (dndState.pendingPromotion) return
        if (dndState.pieceValue != NO_PIECE) {
            val newMovedPieceX = dndState.movedPieceX + xOffset
            val newMovedPieceY = dndState.movedPieceY + yOffset
            val newCol = floor((newMovedPieceX - cellsSize * 0.5f) / cellsSize).toInt()
            val newRow = floor((newMovedPieceY - cellsSize * 0.5f) / cellsSize).toInt()
            val targetFile = if (reversed) 7 - newCol else newCol
            val targetRank = if (reversed) newRow else 7 - newRow
            dndState = dndState.copy(
                movedPieceX = newMovedPieceX,
                movedPieceY = newMovedPieceY,
                targetFile = targetFile,
                targetRank = targetRank
            )
        }
    }

    fun dndCancelCallback() {
        if (gameEnded != GameEndedStatus.GOING_ON) return
        if (isComputerTurn()) return
        if (dndState.pendingPromotion) return
        cancelDragAndDrop()
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
        val move = Move.getFromString(boardState, moveString, true)
        val boardCopy = Board()
        boardCopy.fen = boardState.fen
        return boardCopy.doMove(move, true, false)
    }

    fun commitDndMove() {
        if (!isValidDndMove()) return
        val moveString =
            "${dndState.startFile.asFileChar()}${dndState.startRank.asRankChar()}" +
                    "${dndState.targetFile.asFileChar()}${dndState.targetRank.asRankChar()}"
        val move = Move.getFromString(boardState, moveString, true)
        boardState.doMove(move, true, true)

        positionChangedCallback(boardState.fen)

        manageEndStatus(gameEndedCallback = { notifyUserGameFinished() })
    }

    fun dndValidatingCallback() {
        if (gameEnded != GameEndedStatus.GOING_ON) return
        if (isComputerTurn()) return
        if (dndState.pendingPromotion) return
        if (isValidDndMove()) {
            dndState = if (dndMoveIsPromotion()) {
                dndState.copy(pendingPromotion = true, pendingPromotionForBlack = !boardState.turn)
            } else {
                commitDndMove()
                DndData()
            }
        } else {
            cancelDragAndDrop()
        }
    }

    fun commitPromotion(piece: PromotionPiece) {
        val promotionFen = piece.fen.toLowerCase()
        val moveString =
            "${dndState.startFile.asFileChar()}${dndState.startRank.asRankChar()}" +
                    "${dndState.targetFile.asFileChar()}${dndState.targetRank.asRankChar()}$promotionFen"
        val move = Move.getFromString(boardState, moveString, true)
        boardState.doMove(move, true, true)

        dndState = DndData()
        positionChangedCallback(boardState.fen)
        manageEndStatus(gameEndedCallback = { notifyUserGameFinished() })
    }

    fun cancelPendingPromotion() {
        dndState = DndData()
    }

    fun handleDragStart(offset: Offset) {
        if (gameEnded != GameEndedStatus.GOING_ON) return
        val col = floor((offset.x - cellsSize * 0.5f) / cellsSize).toInt()
        val row = floor((offset.y - cellsSize * 0.5f) / cellsSize).toInt()

        val outOfBounds = col < 0 || col > 7 || row < 0 || row > 7
        if (!outOfBounds) {
            val file = if (reversed) 7 - col else col
            val rank = if (reversed) row else 7 - row
            val square = getSquareFromCellCoordinates(file, rank)
            val piece = boardState.getPieceAt(square)

            if (piece != NO_PIECE) {
                dndStartCallback(file, rank, piece)
            }
        }
    }

    fun handleTap(offset: Offset) {
        if (dndState.pendingPromotion) {

        }
    }

    val newGameRequest = previousGameId != gameId
    if (newGameRequest) {
        boardState = startPosition.toBoard()
        previousGameId = gameId
        gameEnded = GameEndedStatus.GOING_ON
    }
    // Must also be launched before the next move.
    else {
        val notYetNotifiedOfEndOfGame = gameEnded == GameEndedStatus.GOING_ON
        manageEndStatus {
            if (notYetNotifiedOfEndOfGame) {
                notifyUserGameFinished()
            }
        }
    }

    val currentContext = LocalContext.current

    Canvas(
        modifier = modifier
            .background(Color(214, 59, 96))
            .pointerInput(Unit) {
                coroutineScope {
                    launch {
                        detectDragGestures(
                            onDragStart = { handleDragStart(it) },
                            onDragCancel = { dndCancelCallback() },
                            onDrag = { change, dragAmount ->
                                change.consumeAllChanges()
                                dndMoveCallback(dragAmount.x, dragAmount.y)
                            },
                            onDragEnd = {
                                dndValidatingCallback()
                            })
                    }
                    launch {
                        detectTapGestures(
                            onTap = { handleTap(it) }
                        )
                    }
                }
            },
    ) {
        val minSize = if (size.width < size.height) size.width else size.height
        cellsSize = minSize / 9f

        drawCells(cellsSize, reversed, dndData = dndState)
        drawFilesCoordinates(cellsSize, reversed)
        drawRanksCoordinates(cellsSize, reversed)
        drawPlayerTurn(cellsSize, boardState)
        drawPieces(
            context = currentContext,
            cellsSize = cellsSize,
            position = boardState,
            reversed = reversed,
            dndData = dndState,
        )

        if (dndState.pieceValue != NO_PIECE) {
            drawMovedPiece(
                context = currentContext,
                cellsSize = cellsSize.toInt(),
                pieceValue = dndState.pieceValue,
                x = dndState.movedPieceX,
                y = dndState.movedPieceY,
                positionFen = startPosition,
            )
        }

        if (dndState.pendingPromotion) {
            val promotionInBottomPart =
                (reversed && !dndState.pendingPromotionForBlack) || (!reversed && dndState.pendingPromotionForBlack)
            val itemsZoneX = minSize * 0.18f
            val itemsZoneY = minSize * (if (promotionInBottomPart) 0.06f else 0.85f)
            val itemsSize = cellsSize * 1.15f
            val spaceBetweenItems = cellsSize * 0.2f

            drawPromotionValidationZone(
                context,
                x = itemsZoneX,
                y = itemsZoneY,
                itemsSize = itemsSize,
                isWhiteTurn = boardState.turn,
                spaceBetweenItems = spaceBetweenItems,
            )
        }

    }
}

@Composable
private fun StaticChessBoard(
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
        drawCells(cellsSize, reversed)
        drawFilesCoordinates(cellsSize, reversed)
        drawPlayerTurn(cellsSize, boardLogic)
        drawPieces(
            context = currentContext,
            cellsSize = cellsSize,
            position = boardLogic,
            reversed = reversed,
            dndData = DndData()
        )

    }
}

private fun getSquareFromCellCoordinates(file: Int, rank: Int): Long {
    return 1L.shl(7 - file + 8 * rank)
}

private fun DrawScope.drawCells(cellsSize: Float, reversed: Boolean, dndData: DndData = DndData()) {
    repeat(8) { row ->
        val rank = if (reversed) row else 7 - row
        repeat(8) { col ->
            val file = if (reversed) 7 - col else col
            val isWhiteCell = (row + col) % 2 == 0
            val isDndStartCell = (file == dndData.startFile) && (rank == dndData.startRank)
            val isDndTargetCell = (file == dndData.targetFile) && (rank == dndData.targetRank)
            val isDndCrossCell = (file == dndData.targetFile) || (rank == dndData.targetRank)
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
    dndData: DndData
) {
    repeat(8) { row ->
        val rank = if (reversed) row else 7 - row
        repeat(8) { col ->
            val file = if (reversed) 7 - col else col

            val square = getSquareFromCellCoordinates(file, rank)
            val piece = position.getPieceAt(square)
            if (piece != NO_PIECE) {
                val isDraggedPiece = (dndData.startFile == file) && (dndData.startRank == rank)
                if (!isDraggedPiece) {
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
    drawPromotionValidationItem(context, x, y, itemsSize, PromotionQueen(isWhiteTurn = isWhiteTurn))
    drawPromotionValidationItem(
        context,
        xRook,
        y,
        itemsSize,
        PromotionRook(isWhiteTurn = isWhiteTurn)
    )
    drawPromotionValidationItem(
        context,
        xBishop,
        y,
        itemsSize,
        PromotionBishop(isWhiteTurn = isWhiteTurn)
    )
    drawPromotionValidationItem(
        context,
        xKnight,
        y,
        itemsSize,
        PromotionKnight(isWhiteTurn = isWhiteTurn)
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
) {
    val ovalPaint = Paint().apply {
        if (pieceValue.isWhiteTurn) setARGB(255, 0, 0, 0)
        else setARGB(255, 255, 255, 255)
        style = Paint.Style.FILL
    }
    val ovalX = x - size * 0.15f
    val ovalY = y - size * 0.15f

    val imageRef = pieceValue.fen.getPieceImageID()
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
        startPosition = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
    )
}

@Preview
@Composable
fun DynamicChessBoardCustomPositionReversedPreview() {
    DynamicChessBoard(
        modifier = Modifier.size(300.dp),
        reversed = true,
        startPosition = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
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

@Preview
@Composable
fun RestartableAndStoppableDynamicChessBoardPreview() {
    val startPosition = "8/8/7b/8/1k6/pp6/8/K7 b - - 0 1"

    var currentPosition by rememberSaveable {
        mutableStateOf(startPosition)
    }

    fun randomGameId(): Long {
        return Random.nextLong()
    }

    var gameId by rememberSaveable {
        mutableStateOf(randomGameId())
    }
    var stopRequest by rememberSaveable { mutableStateOf(false) }

    fun stopGame() {
        stopRequest = true
    }

    fun restartGame() {
        gameId = randomGameId()
        currentPosition = startPosition
    }

    Column {
        Row {
            Button(onClick = { stopGame() }) {
                Text(text = "Stop")
            }
            Button(onClick = { restartGame() }) {
                Text(text = "Restart")
            }
        }
        Text(text = currentPosition)
        DynamicChessBoard(
            modifier = Modifier.size(300.dp),
            userRequestStopGame = stopRequest,
            startPosition = startPosition,
            gameId = gameId,
            positionChangedCallback = {
                currentPosition = it
            }
        )
    }
}