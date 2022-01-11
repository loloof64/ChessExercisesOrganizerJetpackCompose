package com.loloof64.chessexercisesorganizer.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.components.ChessPiecePreview
import com.loloof64.chessexercisesorganizer.ui.components.ChessPieceSelector
import com.loloof64.chessexercisesorganizer.ui.components.EMPTY_FEN
import com.loloof64.chessexercisesorganizer.ui.components.EditableChessBoard
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import com.loloof64.chessexercisesorganizer.utils.fenBoardPartToPiecesArray
import com.loloof64.chessexercisesorganizer.utils.toBoardFen

@Composable
fun GameEditorPage(navController: NavHostController, index: Int) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp

    var positionFen by rememberSaveable {
        mutableStateOf(EMPTY_FEN)
    }

    var currentPiece by rememberSaveable {
        mutableStateOf('.')
    }

    fun updatePosition(file: Int, rank: Int) {
        val positionParts = positionFen.split(" ").toMutableList()
        val boardPart = positionParts[0]
        val board = boardPart.fenBoardPartToPiecesArray()
        val oldValue = board[rank][file]
        board[rank][file] = if (oldValue != currentPiece) currentPiece else ('.')

        val newBoardFen = board.toBoardFen()
        positionParts[0] = newBoardFen
        positionFen = positionParts.joinToString(" ")
    }

    fun goBack() {
        navController.popBackStack()
    }

    ChessExercisesOrganizerJetpackComposeTheme()
    {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.game_editor_page)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            goBack()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = context.getString(R.string.arrow_back_button),
                            )
                        }
                    })
            },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EditableChessBoard(
                    modifier = Modifier.size(screenWidth * 0.7f),
                    positionFen = positionFen,
                    handleValueUpdate = ::updatePosition
                )

                ChessPieceSelector(
                    modifier = Modifier.size(30.dp),
                    handleValueUpdate = {
                        currentPiece = it
                    }
                )
            }
        }
    }
}