package com.loloof64.chessexercisesorganizer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.loloof64.chessexercisesorganizer.R
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.SquareOccupant
import kotlin.math.floor

const val STANDARD_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

data class DndData(
    var pieceValue: SquareOccupant = SquareOccupant.NONE,
    var startFile: Int = Int.MIN_VALUE,
    var startRank: Int = Int.MIN_VALUE
) {
    fun reset() {
        pieceValue = SquareOccupant.NONE
        startFile = Int.MIN_VALUE
        startRank = Int.MIN_VALUE
    }
}

@Composable
fun ChessBoard(size: Dp, position: String = STANDARD_FEN, reversed: Boolean = false) {
    val positionState = remember { Board.fromFen(position) }
    val dndState = remember { DndData() }
    val globalSize = with(LocalDensity.current) {
        size.toPx()
    }
    val cellsSize = globalSize / 9f
    val textSize = with(LocalDensity.current) {
        (cellsSize * 0.3f).toSp()
    }

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(modifier = Modifier
        .size(size)
        .background(Color(214, 59, 96))
        .drawBehind {
            drawCells(cellsSize)
            drawPlayerTurn(cellsSize, positionState)
        }
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = { offset ->
                val col = floor((offset.x - cellsSize * 0.5f) / cellsSize).toInt()
                val row = floor((offset.y - cellsSize * 0.5f) / cellsSize).toInt()
                val outOfBounds = col < 0 || col > 7 || col < 0 || col < 7
                if (!outOfBounds) {
                    val file = if (reversed) 7-col else col
                    val rank = if (reversed) row else 7-row
                    val square = Square.fromBitRef((7-file) + 8*rank)
                    val piece = positionState.getSquareOccupant(square)
                    if (piece != SquareOccupant.NONE) {
                        dndState.startFile = file
                        dndState.startRank = rank
                    }
                }
            }, onDragCancel = { dndState.reset() }) { change, dragAmount ->
                change.consumeAllChanges()
                offsetX += dragAmount.x
                offsetY += dragAmount.y
            }

        }
    ) {
        FilesCoordinates(cellsSize = cellsSize, textSize = textSize, reversed = reversed)
        RanksCoordinates(cellsSize = cellsSize, textSize = textSize, reversed = reversed)

        Pieces(
            cellsSize = cellsSize,
            position = positionState,
            reversed = reversed,
            dndData = dndState
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
private fun Pieces(cellsSize: Float, position: Board, reversed: Boolean, dndData: DndData) {
    repeat(8) { row ->
        val rank = if (reversed) row else 7 - row
        repeat(8) { col ->
            val file = if (reversed) 7 - col else col

            val square = Square.fromBitRef((7-file) + 8*rank)
            val piece = position.getSquareOccupant(square)
            if (piece != SquareOccupant.NONE) {
                val isDraggedPiece = (dndData.startFile == file) && (dndData.startRank == rank)
                if (! isDraggedPiece) {
                    val x = with(
                        LocalDensity.current
                    ) { (cellsSize * (0.5f + col)).toDp() }
                    val y = with(LocalDensity.current) { (cellsSize * (0.5f + row)).toDp() }
                    val imageSize = with(LocalDensity.current) {
                        cellsSize.toDp()
                    }
                    val imageRef = when (piece) {
                        SquareOccupant.WP -> R.drawable.ic_chess_plt45
                        SquareOccupant.WN -> R.drawable.ic_chess_nlt45
                        SquareOccupant.WB -> R.drawable.ic_chess_blt45
                        SquareOccupant.WR -> R.drawable.ic_chess_rlt45
                        SquareOccupant.WQ -> R.drawable.ic_chess_qlt45
                        SquareOccupant.WK -> R.drawable.ic_chess_klt45
                        SquareOccupant.BP -> R.drawable.ic_chess_pdt45
                        SquareOccupant.BN -> R.drawable.ic_chess_ndt45
                        SquareOccupant.BB -> R.drawable.ic_chess_bdt45
                        SquareOccupant.BR -> R.drawable.ic_chess_rdt45
                        SquareOccupant.BQ -> R.drawable.ic_chess_qdt45
                        SquareOccupant.BK -> R.drawable.ic_chess_kdt45
                        else -> throw RuntimeException("wrong piece value")
                    }
                    val contentDescription = when (piece) {
                        SquareOccupant.WP -> R.string.white_pawn
                        SquareOccupant.WN -> R.string.white_knight
                        SquareOccupant.WB -> R.string.white_bishop
                        SquareOccupant.WR -> R.string.white_rook
                        SquareOccupant.WQ -> R.string.white_queen
                        SquareOccupant.WK -> R.string.white_king
                        SquareOccupant.BP -> R.string.black_pawn
                        SquareOccupant.BN -> R.string.black_knight
                        SquareOccupant.BB -> R.string.black_bishop
                        SquareOccupant.BR -> R.string.black_rook
                        SquareOccupant.BQ -> R.string.black_queen
                        SquareOccupant.BK -> R.string.black_king
                        else -> throw RuntimeException("wrong piece value")
                    }
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

private fun DrawScope.drawCells(cellsSize: Float) {
    repeat(8) { row ->
        repeat(8) { col ->
            val isWhiteCell = (row + col) % 2 == 0
            val backgroundColor =
                if (isWhiteCell) Color(255, 206, 158)
                else Color(209, 139, 71)
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
    val whiteTurn = positionState.sideToMove == Colour.WHITE
    val turnRadius = cellsSize * 0.25f
    val turnColor = if (whiteTurn) Color.White else Color.Black
    val location = cellsSize * 8.75f
    drawCircle(color = turnColor, radius = turnRadius, center = Offset(location, location))
}

@Preview
@Composable
fun ChessBoardPreview() {
    ChessBoard(size = 300.dp)
}

@Preview
@Composable
fun ReversedChessBoardPreview() {
    ChessBoard(size = 300.dp, reversed = true)
}

@Preview
@Composable
fun ChessBoardCustomPositionPreview() {
    ChessBoard(size = 300.dp, position = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2")
}