package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.components.*
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import com.loloof64.chessexercisesorganizer.utils.*
import kotlinx.coroutines.launch
import java.lang.NumberFormatException

private fun getEnPassantValues(whiteTurn: Boolean): List<String> {
    return if (whiteTurn) {
        listOf("-", "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6")
    } else {
        listOf("-", "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3")
    }
}

private typealias EditorTabScreen = @Composable () -> Unit

private data class PositionEditorFieldsTabItem(val titleRef: Int, val screen: EditorTabScreen)

@Composable
fun NumericValueEditor(initialValue: Int, caption: String,
                       handleValueChanged: (Int) -> Unit, handleDismissRequest: () -> Unit) {
    var fieldValue by rememberSaveable {
        mutableStateOf("$initialValue")
    }
    val context = LocalContext.current

    AlertDialog(onDismissRequest = {handleDismissRequest()}, buttons = {
        Button(onClick = {
            val valueToNotify = try {
                Integer.parseInt(fieldValue)
            } catch (ex: NumberFormatException) {
                return@Button
            }
            handleDismissRequest()
            handleValueChanged(valueToNotify)
        }) {
            Text(context.getString(R.string.update_button))
        }
    }, text = {
        Row(horizontalArrangement = Arrangement.Center) {
            Text(caption)
            TextField(fieldValue, onValueChange = {
                fieldValue = it
            }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
    })
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun PositionEditor(
    modifier: Modifier = Modifier,
    oldPosition: String,
    handlePositionChanged: (String) -> Unit,
    handleIllegalPosition: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val context = LocalContext.current

    val fieldsPagerState = rememberPagerState()

    val isLandscape = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }

    val coroutineScope = rememberCoroutineScope()

    var positionFen by rememberSaveable {
        mutableStateOf(oldPosition)
    }

    var currentPiece by rememberSaveable {
        mutableStateOf('.')
    }

    var whiteTurn by rememberSaveable {
        mutableStateOf(true)
    }

    var white00 by rememberSaveable {
        mutableStateOf(positionFen.hasWhite00())
    }

    var white000 by rememberSaveable {
        mutableStateOf(positionFen.hasWhite000())
    }

    var black00 by rememberSaveable {
        mutableStateOf(positionFen.hasBlack00())
    }

    var black000 by rememberSaveable {
        mutableStateOf(positionFen.hasBlack000())
    }

    var enPassantSquareMenuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var enPassantSquareValues by rememberSaveable {
        mutableStateOf(getEnPassantValues(whiteTurn = whiteTurn))
    }

    var enPassantSquareValueIndex by rememberSaveable {
        mutableStateOf(0)
    }

    var enPassantSquare by rememberSaveable {
        mutableStateOf(enPassantSquareValues[enPassantSquareValueIndex])
    }

    var drawHalfMovesCount by rememberSaveable {
        mutableStateOf(positionFen.getDrawHalfMovesCount())
    }

    var moveNumber by rememberSaveable {
        mutableStateOf(positionFen.getMoveNumber())
    }

    var halfMovesCountEditorActive by rememberSaveable {
        mutableStateOf(false)
    }

    var moveNumberEditorActive by rememberSaveable {
        mutableStateOf(false)
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
        val board = positionFen.toBoard()
        val legalPosition = board.checkValidityCompletely()
        if (!legalPosition) {
            handleIllegalPosition()
            return
        }
        /*
        Update castle flags correctly,
        as creating a board from FEN automatically adjusts castle flags
         */
        positionFen = board.fen
        handlePositionChanged(positionFen)
        //todo change mode callback
    }

    fun cancel() {
        //todo change mode callback
    }

    fun handlePieceValueUpdate(newValue: Char) {
        currentPiece = newValue
    }

    fun setWhiteTurn() {
        whiteTurn = true
        positionFen = positionFen.setWhiteTurn()
        handlePositionChanged(positionFen)
    }

    fun setBlackTurn() {
        whiteTurn = false
        positionFen = positionFen.setBlackTurn()
        handlePositionChanged(positionFen)
    }

    fun toggleWhite00() {
        white00 = !white00
        positionFen = positionFen.toggleWhite00()
        handlePositionChanged(positionFen)
    }

    fun toggleWhite000() {
        white000 = !white000
        positionFen = positionFen.toggleWhite000()
        handlePositionChanged(positionFen)
    }

    fun toggleBlack00() {
        black00 = !black00
        positionFen = positionFen.toggleBlack00()
        handlePositionChanged(positionFen)
    }

    fun toggleBlack000() {
        black000 = !black000
        positionFen = positionFen.toggleBlack000()
        handlePositionChanged(positionFen)
    }

    fun updateEnPassantSquare(index: Int) {
        enPassantSquareValueIndex = index
        enPassantSquareMenuExpanded = false
        val tempEnPassantSquare = enPassantSquareValues[enPassantSquareValueIndex]
        positionFen = positionFen.setEnPassantSquare(tempEnPassantSquare)
        handlePositionChanged(positionFen)
    }

    fun updateDrawHalfMovesCount(value: Int) {
        drawHalfMovesCount = value
        positionFen = positionFen.setDrawHalfMovesCount(drawHalfMovesCount)
        handlePositionChanged(positionFen)
    }

    fun updateMoveNumber(value: Int) {
        moveNumber = value
        positionFen = positionFen.setMoveNumber(moveNumber)
        handlePositionChanged(positionFen)
    }

    SideEffect {
        whiteTurn = positionFen.isWhiteTurn()
        white00 = positionFen.hasWhite00()
        white000 = positionFen.hasWhite000()
        black00 = positionFen.hasBlack00()
        black000 = positionFen.hasBlack000()
        enPassantSquare = positionFen.getEnPassantSquare()
        enPassantSquareValues = getEnPassantValues(whiteTurn = whiteTurn)
        enPassantSquareValueIndex = enPassantSquare.getEnPassantSquareValueIndex()
        drawHalfMovesCount = positionFen.getDrawHalfMovesCount()
        moveNumber = positionFen.getMoveNumber()

        ////////////////////////////////////
        println(positionFen)
        ////////////////////////////////////
    }

    @Composable
    fun positionButtonsZone() {
        Column {
            Row {
                RadioButton(selected = whiteTurn, onClick = ::setWhiteTurn)
                Text(context.getString(R.string.white_turn))
                Spacer(modifier = Modifier.size(5.dp))
                RadioButton(selected = !whiteTurn, onClick = ::setBlackTurn)
                Text(context.getString(R.string.black_turn))
                Spacer(modifier = Modifier.size(5.dp))
                ChessPieceSelector(
                    modifier = Modifier.size(30.dp),
                    handleValueUpdate = ::handlePieceValueUpdate,
                    firstPieceValue = currentPiece,
                )
            }
            Spacer(modifier = Modifier.size(5.dp))

            Button(onClick = ::loadOldPosition) {
                Text(context.getString(R.string.load_old_position))
            }
            Spacer(modifier = Modifier.size(5.dp))
            Row {
                Button(onClick = ::loadDefaultPosition) {
                    Text(context.getString(R.string.load_default_position))
                }
                Spacer(modifier = Modifier.size(5.dp))
                Button(onClick = ::clearPosition) {
                    Text(context.getString(R.string.clear_posiiton))
                }
            }
        }
    }

    @Composable
    fun fieldsZone() {
        Column {
            Row {
                Text(context.getString(R.string.white00))
                Checkbox(checked = white00, onCheckedChange = { toggleWhite00() })
                Spacer(modifier = Modifier.size(2.dp))
                Text(context.getString(R.string.white000))
                Checkbox(checked = white000, onCheckedChange = { toggleWhite000() })
                Text(context.getString(R.string.black00))
                Checkbox(checked = black00, onCheckedChange = { toggleBlack00() })
                Spacer(modifier = Modifier.size(2.dp))
                Text(context.getString(R.string.black000))
                Checkbox(checked = black000, onCheckedChange = { toggleBlack000() })
            }
            Spacer(modifier = Modifier.size(5.dp))
            Row {
                Text(
                    context.getString(R.string.en_passant_square),
                    modifier = Modifier.clickable {
                        enPassantSquareMenuExpanded = !enPassantSquareMenuExpanded
                    }
                )
                DropdownMenu(expanded = enPassantSquareMenuExpanded,
                    onDismissRequest = { enPassantSquareMenuExpanded = false }) {
                    enPassantSquareValues.forEachIndexed { index, item ->
                        DropdownMenuItem(onClick = { updateEnPassantSquare(index) }) {
                            Text(item)
                        }
                    }
                }
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    enPassantSquareValues[enPassantSquareValueIndex],
                )
            }
            Spacer(modifier = Modifier.size(5.dp))
            Row(modifier = Modifier.clickable {
                halfMovesCountEditorActive = true
            }) {
                Text(context.getString(R.string.draw_half_moves_count))
                Spacer(modifier = Modifier.size(5.dp))
                Text("$drawHalfMovesCount")
            }

            Spacer(modifier = Modifier.size(5.dp))
            Row(modifier = Modifier.clickable {
                moveNumberEditorActive = true
            }) {
                Text(context.getString(R.string.move_number))
                Spacer(modifier = Modifier.size(5.dp))
                Text("$moveNumber")
            }

            if (halfMovesCountEditorActive) {
                NumericValueEditor(initialValue = drawHalfMovesCount, handleValueChanged = ::updateDrawHalfMovesCount,
                caption = context.getString(R.string.draw_half_moves_count),
                handleDismissRequest = {
                    halfMovesCountEditorActive = false
                })
            }

            if (moveNumberEditorActive) {
                NumericValueEditor(initialValue = moveNumber, handleValueChanged = ::updateMoveNumber,
                caption = context.getString(R.string.move_number),
                handleDismissRequest = {
                    moveNumberEditorActive = false
                })
            }
        }
    }

    @Composable
    fun validationButtonsZone() {
        Row {
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

    val standardItem = PositionEditorFieldsTabItem(
        titleRef = R.string.standard_parameters,
        screen = { positionButtonsZone() })
    val advancedItem = PositionEditorFieldsTabItem(
        titleRef = R.string.advanced_parameters,
        screen = { fieldsZone() })
    val fieldsTabItems = listOf(standardItem, advancedItem)

    @Composable
    fun editionZone() {
        TabRow(
            selectedTabIndex = fieldsPagerState.currentPage,
            backgroundColor = Color(0xFFFF5722),
            contentColor = Color.Blue,
        ) {
            fieldsTabItems.forEachIndexed { index, item ->
                Tab(selected = fieldsPagerState.currentPage == index, onClick = {
                    coroutineScope.launch {
                        fieldsPagerState.animateScrollToPage(index)
                    }
                }, text = { Text(context.getString(item.titleRef)) })
            }
        }
        HorizontalPager(count = fieldsTabItems.size, state = fieldsPagerState) { page ->
            fieldsTabItems[page].screen()
        }
    }

    if (isLandscape) {
        Row(modifier = modifier) {
            EditableChessBoard(
                modifier = Modifier.size(screenHeight * 0.8f),
                positionFen = positionFen,
                handleValueUpdate = ::updatePosition
            )

            Spacer(modifier = Modifier.size(5.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                editionZone()
                Spacer(modifier = Modifier.size(5.dp))
                validationButtonsZone()
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

            editionZone()

            Spacer(modifier = Modifier.size(5.dp))

            validationButtonsZone()
        }
    }
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun GameEditorPage(navController: NavHostController, index: Int) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    var oldPosition by rememberSaveable {
        mutableStateOf(STANDARD_FEN)
    }

    val coroutineScope = rememberCoroutineScope()

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
                    val correctedNewPosition = it.correctEnPassantSquare()
                    oldPosition = correctedNewPosition
                },
                handleIllegalPosition = {
                    coroutineScope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.illegal_position))
                    }
                }
            )
        }
    }
}