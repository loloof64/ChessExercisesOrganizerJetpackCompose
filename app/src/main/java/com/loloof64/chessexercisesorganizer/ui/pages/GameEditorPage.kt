package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.components.*
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import com.loloof64.chessexercisesorganizer.utils.fenBoardPartToPiecesArray
import com.loloof64.chessexercisesorganizer.utils.toBoardFen

@Composable
fun PositionEditor(
    modifier: Modifier = Modifier,
    oldPosition: String,
    handlePositionChanged: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val context = LocalContext.current

    val isLandscape = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }

    var positionFen by rememberSaveable {
        mutableStateOf(oldPosition)
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

    fun loadOldPosition() {
        positionFen = oldPosition
    }

    fun loadDefaultPosition() {
        positionFen = STANDARD_FEN
    }

    fun clearPosition() {
        positionFen = EMPTY_FEN
    }

    fun validate() {
        // todo check if position is legal
        // todo notify user otherwise
        handlePositionChanged(positionFen)
        //todo change mode callback
    }

    fun cancel() {
        //todo change mode callback
    }

    fun handlePieceValueUpdate(newValue: Char) {
        currentPiece = newValue
    }

    if (isLandscape) {
        Row(modifier = modifier) {
            EditableChessBoard(
                modifier = Modifier.size(screenHeight * 0.8f),
                positionFen = positionFen,
                handleValueUpdate = ::updatePosition
            )

            Spacer(modifier = Modifier.size(5.dp))

            Column {
                ChessPieceSelector(
                    modifier = Modifier.size(30.dp),
                    handleValueUpdate = ::handlePieceValueUpdate,
                    firstPieceValue = currentPiece,
                )
                Spacer(modifier = Modifier.size(5.dp))

                Button(onClick = ::loadOldPosition) {
                    Text(context.getString(R.string.load_old_position))
                }
                Spacer(modifier = Modifier.size(5.dp))
                Button(onClick = ::loadDefaultPosition) {
                    Text(context.getString(R.string.load_default_position))
                }
                Spacer(modifier = Modifier.size(5.dp))
                Button(onClick = ::clearPosition) {
                    Text(context.getString(R.string.clear_posiiton))
                }

                Spacer(modifier = Modifier.size(5.dp))

                Button(
                    onClick = ::validate,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Green,
                        contentColor = Color.White
                    )
                ) {
                    Text(context.getString(R.string.validate_position))
                }
                Spacer(modifier = Modifier.size(5.dp))

                Button(
                    onClick = ::cancel,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text(context.getString(R.string.cancel_position))
                }
            }
        }
    } else {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            EditableChessBoard(
                modifier = Modifier.size(screenWidth * 0.7f),
                positionFen = positionFen,
                handleValueUpdate = ::updatePosition
            )

            Spacer(modifier = Modifier.size(5.dp))

            ChessPieceSelector(
                modifier = Modifier.size(30.dp),
                handleValueUpdate = ::handlePieceValueUpdate,
                firstPieceValue = currentPiece,
            )

            Spacer(modifier = Modifier.size(5.dp))


            Button(onClick = ::loadOldPosition) {
                Text(context.getString(R.string.load_old_position))
            }
            Spacer(modifier = Modifier.size(5.dp))
            Button(onClick = ::loadDefaultPosition) {
                Text(context.getString(R.string.load_default_position))
            }
            Spacer(modifier = Modifier.size(5.dp))
            Button(onClick = ::clearPosition) {
                Text(context.getString(R.string.clear_posiiton))
            }

            Spacer(modifier = Modifier.size(5.dp))

            Button(
                onClick = ::validate,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Green,
                    contentColor = Color.White
                )
            ) {
                Text(context.getString(R.string.validate_position))
            }
            Spacer(modifier = Modifier.size(5.dp))
            Button(
                onClick = ::cancel,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(context.getString(R.string.cancel_position))
            }
        }
    }
}

@Composable
fun GameEditorPage(navController: NavHostController, index: Int) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    var oldPosition by rememberSaveable {
        mutableStateOf(STANDARD_FEN)
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
            PositionEditor(
                modifier = Modifier.fillMaxSize(),
                oldPosition = oldPosition,
                 handlePositionChanged = {
                    oldPosition = it
                }
            )
        }
    }
}