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
import com.alonsoruibal.chess.Board
import com.alonsoruibal.chess.Move
import com.loloof64.chessexercisesorganizer.R
import kotlin.math.floor

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
)

sealed class PromotionPiece(val fen: Char)
object PromotionQueen : PromotionPiece('Q')
object PromotionRook : PromotionPiece('R')
object PromotionBishop : PromotionPiece('B')
object PromotionKnight : PromotionPiece('N')

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
        else -> throw IllegalArgumentException("Not a valid file $this !")
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
        else -> throw IllegalArgumentException("Not a valid rank $this !")
    }
}

@Composable
fun DynamicChessBoard(size: Dp, position: String = STANDARD_FEN, reversed: Boolean = false) {
    var positionState by remember {
        mutableStateOf(position)
    }
    var board = Board().apply {
        fen = positionState
    }
    var dndState by remember { mutableStateOf(DndData()) }

    val cellsSize = with(LocalDensity.current) {
        size.toPx() / 9f
    }

    fun dndMoveIsPromotion(): Boolean {
        return (dndState.targetRank == 0 &&
                dndState.pieceValue == 'P') ||
                (dndState.targetRank == 7 && dndState.pieceValue == 'p')
    }

    fun isValidDndMove(): Boolean {
        if (dndState.pieceValue == NO_PIECE) return false
        if (dndState.startFile < 0 || dndState.startFile > 7) return false
        if (dndState.startRank < 0 || dndState.startRank > 7) return false
        if (dndState.targetFile < 0 || dndState.targetFile > 7) return false
        if (dndState.targetFile < 0 || dndState.targetFile > 7) return false

        /////////////////////////////////////////////////
        println("Current board state : ${board.fen}")
        /////////////////////////////////////////////////

        val promotionChar = if (dndMoveIsPromotion()) "Q" else ""
        val moveString =
            "${dndState.startFile.asFileChar()}${dndState.startRank.asRankChar()}" +
                    "${dndState.targetFile.asFileChar()}${dndState.targetRank.asRankChar()}$promotionChar"
        val move = Move.getFromString(board, moveString, true)
        val boardCopy = Board()
        boardCopy.fen = positionState
        return boardCopy.doMove(move, true, false)
    }

    fun commitDndMove(promotionPiece: PromotionPiece = PromotionQueen) {
        if (!isValidDndMove()) return
        val promotionChar = if (dndMoveIsPromotion()) promotionPiece.fen else ""
        val moveString =
            "${dndState.startFile.asFileChar()}${dndState.startRank.asRankChar()}" +
                    "${dndState.targetFile.asFileChar()}${dndState.targetRank.asRankChar()}$promotionChar"
        val move = Move.getFromString(board, moveString, true)
        board.doMove(move, true, true)

        positionState = board.fen

        //////////////////////////////////////////
        println("New board state: ${board.fen}")
        //////////////////////////////////////////
    }

    StaticChessBoard(
        size = size,
        reversed = reversed,
        position = positionState,
        dndData = dndState,
        dndStartCallback = { file, rank, piece ->
            val whiteTurn = positionState.split(" ")[1] == "w"
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
        },
        dndMoveCallback = { xOffset, yOffset ->
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
        },
        dndCancelCallback = {
            dndState = DndData()
        },
        dndValidatingCallback = {
            if (isValidDndMove()) {
                commitDndMove()
                dndState = DndData()
            } else {
                dndState = DndData()
            }
        }
    )
}

@Composable
private fun StaticChessBoard(
    size: Dp,
    position: String = STANDARD_FEN,
    reversed: Boolean = false,
    dndData: DndData = DndData(),
    dndStartCallback: (Int, Int, Char) -> Unit = { _, _, _ -> },
    dndMoveCallback: (Float, Float) -> Unit = { _, _ -> },
    dndValidatingCallback: () -> Unit = {},
    dndCancelCallback: () -> Unit = {},
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
                            val piece = boardLogic.getPieceAt(square)
                            if (piece != NO_PIECE) {
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
                        dndValidatingCallback()
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

        if (dndData.pieceValue != NO_PIECE) {
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
private fun MovedPiece(cellsSize: Float, positionFen: String, dndData: DndData) {
    val boardLogic = Board()
    boardLogic.fen = positionFen
    val square = getSquareFromCellCoordinates(dndData.startFile, dndData.startRank)
    val piece = boardLogic.getPieceAt(square)
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

private fun getSquareFromCellCoordinates(file: Int, rank: Int): Long {
    return 1L.shl(7 - file + 8 * rank)
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
    val whiteTurn = positionState.turn
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