package com.loloof64.chessexercisesorganizer.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.alonsoruibal.chess.Board
import com.loloof64.chessexercisesorganizer.R

const val STANDARD_FEN = Board.FEN_START_POSITION

const val NO_PIECE = '.'

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

@Composable
fun DynamicChessBoard(
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

private fun DrawScope.drawCells(cellsSize: Float) {
    repeat(8) { row ->
        repeat(8) { col ->
            val isWhiteCell = (row + col) % 2 == 0
            val backgroundColor =
                when {
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
) {
    repeat(8) { row ->
        val rank = if (reversed) row else 7 - row
        repeat(8) { col ->
            val file = if (reversed) 7 - col else col

            val square = getSquareFromCellCoordinates(file, rank)
            val piece = position.getPieceAt(square)
            if (piece != NO_PIECE) {
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