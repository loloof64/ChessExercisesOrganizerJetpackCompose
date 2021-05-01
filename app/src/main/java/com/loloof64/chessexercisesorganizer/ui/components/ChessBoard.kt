package com.loloof64.chessexercisesorganizer.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import com.loloof64.chessexercisesorganizer.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor

const val STANDARD_FEN = Board.FEN_START_POSITION

const val NO_PIECE = '.'

fun Char.isWhitePiece(): Boolean {
    return when (this) {
        'P', 'N', 'B', 'R', 'Q', 'K' -> true
        'p', 'n', 'b', 'r', 'q', 'k' -> false
        else -> throw IllegalArgumentException("Not a valid piece : $this !")
    }
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

fun String.toBoard(): Board {
    val position = this
    return Board().apply {
        fen = position
    }
}

data class DndData(
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


val DndDataStateSaver = Saver<DndData, String>(
    save = { state -> state.toString() },
    restore = { value ->
        DndData.parse(value)
    }
)


@Composable
fun DynamicChessBoard(
    modifier: Modifier = Modifier,
    position: String = STANDARD_FEN,
    reversed: Boolean = false,
) {
    val composableScope = rememberCoroutineScope()

    val boardLogic = position.toBoard()
    val currentContext = LocalContext.current

    var dndState by rememberSaveable(stateSaver = DndDataStateSaver) {
        mutableStateOf(DndData())
    }

    var cellsSize by remember { mutableStateOf(0f) }

    fun processDragAndDropStart(file: Int, rank: Int, piece: Char) {
        val whiteTurn = boardLogic.turn
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

    fun cancelDragAndDropAnimation() {
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


    fun handleDragStart(offset: Offset) {
        val col = floor((offset.x - cellsSize * 0.5f) / cellsSize).toInt()
        val row = floor((offset.y - cellsSize * 0.5f) / cellsSize).toInt()

        val outOfBounds = col < 0 || col > 7 || row < 0 || row > 7
        if (!outOfBounds) {
            val file = if (reversed) 7 - col else col
            val rank = if (reversed) row else 7 - row
            val square = getSquareFromCellCoordinates(file, rank)
            val piece = boardLogic.getPieceAt(square)

            if (piece != NO_PIECE) {
                processDragAndDropStart(file, rank, piece)
            }
        }
    }

    fun handleDragMove(change: PointerInputChange, dragAmount: Offset) {
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
        cancelDragAndDropAnimation()
    }

    Canvas(
        modifier = modifier
            .background(Color(214, 59, 96))
            .pointerInput(reversed) {
                detectDragGestures(
                    onDragStart = { handleDragStart(it) },
                    onDrag = { change, dragAmount ->
                        handleDragMove(change, dragAmount)
                    },
                    onDragCancel = { handleDndCancel() },
                    onDragEnd = { handleDndCancel() },
                )
            }
    ) {
        val minSize = if (size.width < size.height) size.width else size.height
        cellsSize = minSize / 9f
        drawCells(cellsSize = cellsSize, reversed = reversed, dndData = dndState)
        drawFilesCoordinates(cellsSize, reversed)
        drawRanksCoordinates(cellsSize, reversed)
        drawPlayerTurn(cellsSize, boardLogic)
        drawPieces(
            context = currentContext,
            cellsSize = cellsSize,
            position = boardLogic,
            reversed = reversed,
            dndData = dndState,
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
                positionFen = boardLogic.fen,
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