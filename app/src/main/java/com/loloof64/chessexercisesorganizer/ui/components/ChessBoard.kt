package com.loloof64.chessexercisesorganizer.ui.components

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.alonsoruibal.chess.Board
import com.alonsoruibal.chess.Move
import com.loloof64.chessexercisesorganizer.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor

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

fun Char.getPieceImageDescriptionID(): Int {
    return when (this) {
        'P' -> R.string.white_pawn
        'N' -> R.string.white_knight
        'B' -> R.string.white_bishop
        'R' -> R.string.white_rook
        'Q' -> R.string.white_queen
        'K' -> R.string.white_king
        'p' -> R.string.black_pawn
        'n' -> R.string.black_knight
        'b' -> R.string.black_bishop
        'r' -> R.string.black_rook
        'q' -> R.string.black_queen
        'k' -> R.string.black_king
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

@Composable
fun DynamicChessBoard(
    size: Dp,
    position: String = STANDARD_FEN,
    reversed: Boolean = false,
    userRequestStopGame: Boolean = false,
) {
    val context = LocalContext.current

    val globalSize = with(LocalDensity.current) {
        size.toPx()
    }
    val cellsSize = globalSize / 9f
    val textSize = with(LocalDensity.current) {
        (cellsSize * 0.3f).toSp()
    }

    val composableScope = rememberCoroutineScope()
    val boardState by rememberSaveable(stateSaver = BoardStateSaver) {
        mutableStateOf(position.toBoard())
    }

    var dndState by rememberSaveable(stateSaver = DndDataStateSaver) { mutableStateOf(DndData()) }

    var gameEnded by rememberSaveable {
        mutableStateOf(GameEndedStatus.GOING_ON)
    }

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

        manageEndStatus(gameEndedCallback = { notifyUserGameFinished() })
    }

    fun dndValidatingCallback() {
        if (gameEnded != GameEndedStatus.GOING_ON) return
        if (dndState.pendingPromotion) return
        if (isValidDndMove()) {
            dndState = if (dndMoveIsPromotion()) {
                dndState.copy(pendingPromotion = true)
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

    // Must also be launched before the next move.
    val notYetNotifiedOfEndOfGame = gameEnded == GameEndedStatus.GOING_ON
    manageEndStatus {
        if (notYetNotifiedOfEndOfGame) {
            notifyUserGameFinished()
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(Color(214, 59, 96))
            .drawBehind {
                drawCells(cellsSize, reversed, dndData = dndState)
                drawPlayerTurn(cellsSize, boardState)
            }
            .pointerInput(Unit) {
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
            },
    ) {
        FilesCoordinates(cellsSize = cellsSize, textSize = textSize, reversed = reversed)
        RanksCoordinates(cellsSize = cellsSize, textSize = textSize, reversed = reversed)

        Pieces(
            cellsSize = cellsSize,
            position = boardState,
            reversed = reversed,
            dndData = dndState,
        )

        if (dndState.pieceValue != NO_PIECE) {
            MovedPiece(
                cellsSize = cellsSize,
                pieceValue = dndState.pieceValue,
                x = dndState.movedPieceX,
                y = dndState.movedPieceY,
                positionFen = position,
            )
        }

        if (dndState.pendingPromotion) {
            val itemsZoneX = with(LocalDensity.current) {
                (size.toPx() * 0.18f).toDp()
            }
            val itemsZoneY = with(LocalDensity.current) {
                val promotionInBottomPart = dndState.movedPieceY > size.toPx() / 2
                (size.toPx() * (if (promotionInBottomPart) 0.06f else 0.85f)).toDp()
            }
            val itemsSize = with(LocalDensity.current) {
                cellsSize.toDp() * 1.15f
            }

            PromotionValidationZone(
                modifier = Modifier.offset(itemsZoneX, itemsZoneY),
                itemsSize = itemsSize,
                isWhiteTurn = boardState.turn,
                commitPromotion = { piece -> commitPromotion(piece) },
                cancelPendingPromotion = { cancelPendingPromotion() }
            )
        }

    }
}

@Composable
private fun StaticChessBoard(
    size: Dp,
    position: String = STANDARD_FEN,
    reversed: Boolean = false,
) {
    val globalSize = with(LocalDensity.current) {
        size.toPx()
    }
    val cellsSize = globalSize / 9f
    val textSize = with(LocalDensity.current) {
        (cellsSize * 0.3f).toSp()
    }

    val boardLogic = Board().apply {
        fen = position
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(Color(214, 59, 96))
            .drawBehind {
                drawCells(cellsSize, reversed)
                drawPlayerTurn(cellsSize, boardLogic)
            },
    ) {
        FilesCoordinates(cellsSize = cellsSize, textSize = textSize, reversed = reversed)
        RanksCoordinates(cellsSize = cellsSize, textSize = textSize, reversed = reversed)

        Pieces(
            cellsSize = cellsSize,
            position = boardLogic,
            reversed = reversed,
        )

    }
}

@Composable
private fun FilesCoordinates(cellsSize: Float, textSize: TextUnit, reversed: Boolean) {
    repeat(8) { col ->
        val file = if (reversed) 7 - col else col
        val coordinateText = "${('A'.toInt() + file).toChar()}"
        val x = with(LocalDensity.current) { (cellsSize * (0.90f + col)).toDp() }
        val y1 = with(LocalDensity.current) { (cellsSize * 0.025f).toDp() }
        val y2 = with(
            LocalDensity.current
        ) { (cellsSize * 8.525f).toDp() }
        Text(
            text = coordinateText, fontWeight = FontWeight.Bold, fontSize = textSize,
            color = Color(255, 199, 0),
            modifier = Modifier.offset(x, y1)
        )
        Text(
            text = coordinateText, fontWeight = FontWeight.Bold, fontSize = textSize,
            color = Color(255, 199, 0),
            modifier = Modifier.offset(x, y2)
        )
    }
}

@Composable
private fun RanksCoordinates(cellsSize: Float, textSize: TextUnit, reversed: Boolean) {
    repeat(8) { row ->
        val rank = if (reversed) row else 7 - row
        val coordinateText = "${('1'.toInt() + rank).toChar()}"
        val y = with(LocalDensity.current) { (cellsSize * (0.75f + row)).toDp() }
        val x1 = with(LocalDensity.current) { (cellsSize * 0.15f).toDp() }
        val x2 = with(LocalDensity.current) { (cellsSize * 8.65f).toDp() }

        Text(
            text = coordinateText, fontWeight = FontWeight.Bold, fontSize = textSize,
            color = Color(255, 199, 0),
            modifier = Modifier.offset(x1, y)
        )
        Text(
            text = coordinateText, fontWeight = FontWeight.Bold, fontSize = textSize,
            color = Color(255, 199, 0),
            modifier = Modifier.offset(x2, y)
        )
    }
}

@Composable
private fun Pieces(
    cellsSize: Float,
    position: Board,
    reversed: Boolean,
    dndData: DndData = DndData()
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
                    val x = with(
                        LocalDensity.current
                    ) { (cellsSize * (0.5f + col)).toDp() }
                    val y = with(LocalDensity.current) { (cellsSize * (0.5f + row)).toDp() }
                    val imageSize = with(LocalDensity.current) {
                        cellsSize.toDp()
                    }
                    val imageRef = piece.getPieceImageID()
                    val contentDescription = piece.getPieceImageDescriptionID()
                    Image(
                        painter = painterResource(id = imageRef),
                        contentDescription = stringResource(contentDescription),
                        modifier = Modifier
                            .size(imageSize)
                            .offset(x, y)
                    )
                }
            }
        }
    }
}

@Composable
private fun MovedPiece(
    cellsSize: Float,
    positionFen: String,
    pieceValue: Char,
    x: Float,
    y: Float
) {
    val boardLogic = Board()
    boardLogic.fen = positionFen
    val imageRef = pieceValue.getPieceImageID()
    val imageDescription =
        pieceValue.getPieceImageDescriptionID()

    val xDp = with(LocalDensity.current) { x.toDp() }
    val yDp = with(
        LocalDensity.current
    ) { y.toDp() }
    val imageSize = with(LocalDensity.current) {
        cellsSize.toDp()
    }
    Image(
        painter = painterResource(id = imageRef),
        contentDescription = stringResource(imageDescription),
        modifier = Modifier
            .size(imageSize)
            .offset(xDp, yDp)
    )
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

@Composable
fun PromotionValidationZone(
    modifier: Modifier = Modifier, itemsSize: Dp, isWhiteTurn: Boolean,
    commitPromotion: (piece: PromotionPiece) -> Unit = { _ -> },
    cancelPendingPromotion: () -> Unit = {},
) {
    Row(modifier = modifier) {
        PromotionValidationItem(
            size = itemsSize,
            pieceValue = PromotionQueen(isWhiteTurn = isWhiteTurn)
        ) {
            commitPromotion(PromotionQueen(isWhiteTurn = isWhiteTurn))
        }
        PromotionValidationItem(
            size = itemsSize,
            pieceValue = PromotionRook(isWhiteTurn = isWhiteTurn)
        ) {
            commitPromotion(PromotionRook(isWhiteTurn = isWhiteTurn))
        }
        PromotionValidationItem(
            size = itemsSize,
            pieceValue = PromotionBishop(isWhiteTurn = isWhiteTurn)
        ) {
            commitPromotion(PromotionBishop(isWhiteTurn = isWhiteTurn))
        }
        PromotionValidationItem(
            size = itemsSize,
            pieceValue = PromotionKnight(isWhiteTurn = isWhiteTurn)
        ) {
            commitPromotion(PromotionKnight(isWhiteTurn = isWhiteTurn))
        }
        PromotionCancellationItem(size = itemsSize, isWhiteTurn = isWhiteTurn) {
            cancelPendingPromotion()
        }
    }
}

@Composable
fun PromotionValidationItem(
    modifier: Modifier = Modifier,
    size: Dp,
    pieceValue: PromotionPiece,
    clickCallback: () -> Unit = {}
) {
    val backgroundColor = if (pieceValue.isWhiteTurn) Color.Black else Color.White
    val padding = size / 10f
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = { clickCallback() })
            .background(backgroundColor)
            .padding(padding)
    ) {
        val imageRef = pieceValue.fen.getPieceImageID()
        val contentDescription = pieceValue.fen.getPieceImageDescriptionID()
        Image(
            painter = painterResource(id = imageRef),
            contentDescription = stringResource(contentDescription),
            modifier = Modifier
                .size(size)
        )
    }
}

@Composable
fun PromotionCancellationItem(
    modifier: Modifier = Modifier,
    size: Dp,
    isWhiteTurn: Boolean,
    clickCallback: () -> Unit = {}
) {
    val backgroundColor = if (isWhiteTurn) Color.Black else Color.White
    val padding = size / 4.5f
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = { clickCallback() })
            .background(backgroundColor)
            .padding(padding)
    ) {
        val imageRef = R.drawable.ic_red_cross
        val contentDescription = R.string.red_cross
        Image(
            painter = painterResource(id = imageRef),
            contentDescription = stringResource(contentDescription),
            modifier = Modifier
                .size(size)
        )
    }
}

@Preview
@Composable
fun DynamicChessBoardPreview() {
    DynamicChessBoard(size = 300.dp)
}

@Preview
@Composable
fun DynamicReversedChessBoardPreview() {
    DynamicChessBoard(size = 300.dp, reversed = true)
}

@Preview
@Composable
fun DynamicChessBoardCustomPositionPreview() {
    DynamicChessBoard(
        size = 300.dp,
        // custom
        //position = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"

        // pat
        // position = "8/8/6b1/8/1k6/pp6/8/K7 b - - 0 1"

        // repetition
        position = "8/8/7b/8/1k6/pp6/8/K7 b - - 0 1"

        // 50 coups
        // position = "2k5/8/8/5b2/2n5/8/8/K7 w - - 97 60"

        // manque materiel
        // position = "2k5/8/8/5b2/8/8/1n6/K7 w - - 0 60"
    )
}

@Preview
@Composable
fun DynamicChessBoardCustomPositionReversedPreview() {
    DynamicChessBoard(
        size = 300.dp,
        reversed = true,
        position = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
    )
}

@Preview
@Composable
fun StaticChessBoardPreview() {
    StaticChessBoard(size = 300.dp)
}

@Preview
@Composable
fun StaticChessBoardReversedPreview() {
    StaticChessBoard(size = 300.dp, reversed = true)
}

@Preview
@Composable
fun WhitePromotionValidationItemPreview() {
    PromotionValidationItem(size = 100.dp, pieceValue = PromotionKnight(isWhiteTurn = true))
}

@Preview
@Composable
fun BlackPromotionValidationItemPreview() {
    PromotionValidationItem(size = 100.dp, pieceValue = PromotionRook(isWhiteTurn = false))
}

@Preview
@Composable
fun PromotionValidationZonePreview() {
    PromotionValidationZone(itemsSize = 100.dp, isWhiteTurn = true)
}

@Preview
@Composable
fun PromotionCancellationItemPreview() {
    PromotionCancellationItem(size = 100.dp, isWhiteTurn = true)
}

@Preview
@Composable
fun StoppableDynamicChessBoardPreview() {
    var stopRequest by rememberSaveable{ mutableStateOf(false)}

    fun stopGame() {
        stopRequest = true
    }

    Column {
        Button(onClick = {stopGame()}) {
            Text(text = "Stop")
        }
        DynamicChessBoard(
            size = 300.dp,
            userRequestStopGame = stopRequest
        )
    }
}