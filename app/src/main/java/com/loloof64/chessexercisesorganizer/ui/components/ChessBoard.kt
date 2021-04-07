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
import com.netsensia.rivalchess.model.util.FenUtils.getFen
import kotlin.math.floor

const val STANDARD_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

data class DndData(
    val pieceValue: SquareOccupant = SquareOccupant.NONE,
    val startFile: Int = Int.MIN_VALUE,
    val startRank: Int = Int.MIN_VALUE,
    val targetFile: Int = Int.MIN_VALUE,
    val targetRank: Int = Int.MIN_VALUE,
    val movedPieceX: Float = Float.MIN_VALUE,
    val movedPieceY: Float = Float.MIN_VALUE,
)

@Composable
fun DynamicChessBoard(size: Dp, position: String = STANDARD_FEN, reversed: Boolean = false) {
    val positionState by remember { mutableStateOf(Board.fromFen(position)) }
    var dndState by remember { mutableStateOf(DndData()) }

    val cellsSize = with(LocalDensity.current) {
        size.toPx() / 9f
    }

    StaticChessBoard(
        size = size,
        reversed = reversed,
        position = positionState.getFen(),
        dndData = dndState,
        dndStartCallback = { file, rank, piece ->
            val isPieceOfSideToMove = piece.colour == positionState.sideToMove
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
        },
        dndMoveCallback = { xOffset, yOffset ->
            if (dndState.pieceValue != SquareOccupant.NONE) {
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
        },
        dndCancelCallback = {
            dndState = DndData()
        },
        dndValidatedCallback = {
            if (dndState.pieceValue != SquareOccupant.NONE) {
                println("Validating : $dndState")
            }
            dndState = DndData()
        }
    )
}

@Composable
private fun StaticChessBoard(
    size: Dp,
    position: String = STANDARD_FEN,
    reversed: Boolean = false,
    dndData: DndData = DndData(),
    dndStartCallback: (Int, Int, SquareOccupant) -> Unit = { _, _, _ -> },
    dndMoveCallback: (Float, Float) -> Unit = { _, _ -> },
    dndValidatedCallback: () -> Unit = {},
    dndCancelCallback: () -> Unit = {},
) {
    val globalSize = with(LocalDensity.current) {
        size.toPx()
    }
    val cellsSize = globalSize / 9f
    val textSize = with(LocalDensity.current) {
        (cellsSize * 0.3f).toSp()
    }

    val boardLogic = Board.fromFen(position)

    Box(
        modifier = Modifier
            .size(size)
            .background(Color(214, 59, 96))
            .drawBehind {
                drawCells(cellsSize, reversed, dndData)
                drawPlayerTurn(cellsSize, boardLogic)
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val col = floor((offset.x - cellsSize * 0.5f) / cellsSize).toInt()
                        val row = floor((offset.y - cellsSize * 0.5f) / cellsSize).toInt()

                        val outOfBounds = col < 0 || col > 7 || row < 0 || row > 7
                        if (!outOfBounds) {
                            val file = if (reversed) 7 - col else col
                            val rank = if (reversed) row else 7 - row
                            val square = getSquareFromCellCoordinates(file, rank)
                            val piece = boardLogic.getSquareOccupant(square)
                            if (piece != SquareOccupant.NONE) {
                                dndStartCallback(file, rank, piece)
                            }
                        }
                    },
                    onDragCancel = { dndCancelCallback() },
                    onDrag = { change, dragAmount ->
                        change.consumeAllChanges()
                        dndMoveCallback(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = {
                        dndValidatedCallback()
                    })
            },
    ) {
        FilesCoordinates(cellsSize = cellsSize, textSize = textSize, reversed = reversed)
        RanksCoordinates(cellsSize = cellsSize, textSize = textSize, reversed = reversed)

        Pieces(
            cellsSize = cellsSize,
            position = boardLogic,
            reversed = reversed,
            dndData = dndData
        )

        if (dndData.pieceValue != SquareOccupant.NONE) {
            MovedPiece(
                cellsSize = cellsSize,
                dndData = dndData,
                positionFen = position,
            )
        }

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

            val square = getSquareFromCellCoordinates(file, rank)
            val piece = position.getSquareOccupant(square)
            if (piece != SquareOccupant.NONE) {
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
private fun MovedPiece(cellsSize: Float, positionFen: String, dndData: DndData) {
    val boardLogic = Board.fromFen(positionFen)
    val square = getSquareFromCellCoordinates(dndData.startFile, dndData.startRank)
    val piece = boardLogic.getSquareOccupant(square)
    val imageRef = piece.getPieceImageID()
    val imageDescription =
        piece.getPieceImageDescriptionID()

    val x = with(LocalDensity.current) { dndData.movedPieceX.toDp() }
    val y = with(
        LocalDensity.current
    ) { dndData.movedPieceY.toDp() }
    val imageSize = with(LocalDensity.current) {
        cellsSize.toDp()
    }
    Image(
        painter = painterResource(id = imageRef),
        contentDescription = stringResource(imageDescription),
        modifier = Modifier
            .size(imageSize)
            .offset(x, y)
    )
}

private fun getSquareFromCellCoordinates(file: Int, rank: Int): Square {
    return Square.fromBitRef((7 - file) + 8 * rank)
}

private fun SquareOccupant.getPieceImageID(): Int {
    return when (this) {
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
        else -> throw RuntimeException("not a valid piece")
    }
}

private fun SquareOccupant.getPieceImageDescriptionID(): Int {
    return when (this) {
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
        else -> throw RuntimeException("not a valid piece")
    }
}

private fun DrawScope.drawCells(cellsSize: Float, reversed: Boolean, dndData: DndData) {
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
    val whiteTurn = positionState.sideToMove == Colour.WHITE
    val turnRadius = cellsSize * 0.25f
    val turnColor = if (whiteTurn) Color.White else Color.Black
    val location = cellsSize * 8.75f
    drawCircle(color = turnColor, radius = turnRadius, center = Offset(location, location))
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
        position = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
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