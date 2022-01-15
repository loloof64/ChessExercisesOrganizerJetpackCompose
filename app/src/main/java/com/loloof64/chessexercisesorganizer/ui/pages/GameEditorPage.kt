package com.loloof64.chessexercisesorganizer.ui.pages

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.material.datepicker.MaterialDatePicker
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.components.*
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import com.loloof64.chessexercisesorganizer.utils.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private fun getEnPassantValues(whiteTurn: Boolean): List<String> {
    return if (whiteTurn) {
        listOf("-", "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6")
    } else {
        listOf("-", "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3")
    }
}

private typealias EditorTabScreen = @Composable () -> Unit

private data class PositionEditorFieldsTabItem(val titleRef: Int, val screen: EditorTabScreen)
private data class SolutionEditorFieldsTabItem(val titleRef: Int, val screen: EditorTabScreen)

@Suppress("UnnecessaryComposedModifier")
fun Modifier.underline(color: Color = Color.Black): Modifier = composed(
    factory = {
        this.then(
            Modifier.drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawLine(
                        SolidColor(color),
                        Offset(0f, size.height),
                        Offset(size.width, size.height)
                    )
                }
            }
        )
    }
)

@Composable
fun NumericValueEditor(
    initialValue: Int, caption: String,
    handleValueChanged: (Int) -> Unit, handleDismissRequest: () -> Unit
) {
    var fieldValue by rememberSaveable {
        mutableStateOf("$initialValue")
    }
    val context = LocalContext.current

    AlertDialog(onDismissRequest = { handleDismissRequest() }, confirmButton = {
        Button(
            onClick = {
                val valueToNotify = try {
                    Integer.parseInt(fieldValue)
                } catch (ex: NumberFormatException) {
                    return@Button
                }
                handleDismissRequest()
                handleValueChanged(valueToNotify)
            }, colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Green,
                contentColor = Color.White
            )
        ) {
            Text(context.getString(R.string.update_button))
        }
    }, dismissButton = {
        Button(
            onClick = { handleDismissRequest() }, colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            Text(context.getString(R.string.cancel))
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

@Composable
fun TextValueEditor(
    initialValue: String, caption: String,
    handleValueChanged: (String) -> Unit, handleDismissRequest: () -> Unit
) {
    var fieldValue by rememberSaveable {
        mutableStateOf(initialValue)
    }
    val context = LocalContext.current

    AlertDialog(onDismissRequest = { handleDismissRequest() }, confirmButton = {
        Button(
            onClick = {
                handleDismissRequest()
                handleValueChanged(fieldValue)
            }, colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Green,
                contentColor = Color.White
            )
        ) {
            Text(context.getString(R.string.update_button))
        }
    }, dismissButton = {
        Button(
            onClick = { handleDismissRequest() }, colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            Text(context.getString(R.string.cancel))
        }
    }, text = {
        Row(horizontalArrangement = Arrangement.Center) {
            Text(caption)
            TextField(fieldValue, onValueChange = {
                fieldValue = it
            })
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
    handleExitPositionEditionModeRequest: () -> Unit,
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

    var confirmChangePositionDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var confirmCancelChangingPositionDialogOpen by rememberSaveable {
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
        confirmChangePositionDialogOpen = true
    }

    fun cancel() {
        confirmCancelChangingPositionDialogOpen = true
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
    fun confirmChangePositionDialog() {
        AlertDialog(onDismissRequest = { confirmChangePositionDialogOpen = false }, title = {
            Text(context.getString(R.string.confirm_validate_position_title))
        }, text = {
            Text(context.getString((R.string.confirm_validate_position_message)))
        }, confirmButton = {
            Button(
                onClick = {
                    handlePositionChanged(positionFen)
                    handleExitPositionEditionModeRequest()
                    confirmChangePositionDialogOpen = false
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Green,
                    contentColor = Color.White
                )
            ) {
                Text(context.getString(R.string.ok))
            }
        }, dismissButton = {
            Button(
                onClick = { confirmChangePositionDialogOpen = false },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(context.getString(R.string.cancel))
            }
        })
    }

    @Composable
    fun confirmCancelChangingPositionDialog() {
        AlertDialog(onDismissRequest = { confirmCancelChangingPositionDialogOpen = false }, title = {
            Text(context.getString(R.string.confirm_cancel_changing_position_title))
        }, text = {
            Text(context.getString((R.string.confirm_cancel_changing_position_message)))
        }, confirmButton = {
            Button(
                onClick = {
                    handleExitPositionEditionModeRequest()
                    confirmCancelChangingPositionDialogOpen = false
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Green,
                    contentColor = Color.White
                )
            ) {
                Text(context.getString(R.string.ok))
            }
        }, dismissButton = {
            Button(
                onClick = { confirmCancelChangingPositionDialogOpen = false },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(context.getString(R.string.cancel))
            }
        })
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
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .width(50.dp)
                        .underline(Color.Blue)
                        .clickable {
                            enPassantSquareMenuExpanded = !enPassantSquareMenuExpanded
                        }
                )
            }
            Spacer(modifier = Modifier.size(5.dp))
            Row {
                Text(context.getString(R.string.draw_half_moves_count))
                Spacer(modifier = Modifier.size(5.dp))
                Text("$drawHalfMovesCount",
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .width(50.dp)
                        .underline(Color.Blue)
                        .clickable {
                            halfMovesCountEditorActive = true
                        })
            }

            Spacer(modifier = Modifier.size(5.dp))
            Row {
                Text(context.getString(R.string.move_number))
                Spacer(modifier = Modifier.size(5.dp))
                Text("$moveNumber",
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .width(50.dp)
                        .underline(Color.Blue)
                        .clickable {
                            moveNumberEditorActive = true
                        })
            }

            if (halfMovesCountEditorActive) {
                NumericValueEditor(initialValue = drawHalfMovesCount,
                    handleValueChanged = ::updateDrawHalfMovesCount,
                    caption = context.getString(R.string.draw_half_moves_count),
                    handleDismissRequest = {
                        halfMovesCountEditorActive = false
                    })
            }

            if (moveNumberEditorActive) {
                NumericValueEditor(initialValue = moveNumber,
                    handleValueChanged = ::updateMoveNumber,
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
                modifier = Modifier.size(screenHeight * 0.7f),
                positionFen = positionFen,
                handleValueUpdate = ::updatePosition
            )

            Spacer(modifier = Modifier.size(5.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                editionZone()
                Spacer(modifier = Modifier.size(5.dp))
                validationButtonsZone()
                if (confirmChangePositionDialogOpen) {
                    confirmChangePositionDialog()
                }
                if (confirmCancelChangingPositionDialogOpen) {
                    confirmCancelChangingPositionDialog()
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
            editionZone()
            Spacer(modifier = Modifier.size(5.dp))
            validationButtonsZone()
            if (confirmChangePositionDialogOpen) {
                confirmChangePositionDialog()
            }
            if (confirmCancelChangingPositionDialogOpen) {
                confirmCancelChangingPositionDialog()
            }
        }
    }
}

enum class GoalTagValue {
    WhiteWin,
    BlackWin,
    Draw,
    WhiteCheckmate,
    BlackCheckmate,
}

private const val separator = "@#-^^-#@"

data class GameHeaderData(
    var whitePlayer: String = "",
    var blackPlayer: String = "",
    var event: String = "",
    var site: String = "",
    var date: String = "",
    var goal: GoalTagValue = GoalTagValue.WhiteWin,
    var checkmateCount: Int = 1,
) {
    override fun toString(): String {
        return "$whitePlayer$separator$blackPlayer$separator$event$separator$site$separator$date$separator$goal$separator$checkmateCount"
    }

    fun toPgnHeaderString(): String {
        val goalValue = when(goal) {
            GoalTagValue.WhiteWin -> "1-0"
            GoalTagValue.BlackWin -> "0-1"
            GoalTagValue.Draw -> "1/2-1/2"
            GoalTagValue.WhiteCheckmate -> "#$checkmateCount-0"
            GoalTagValue.BlackCheckmate -> "0-#$checkmateCount"
        }
        return """
            [Event "$event"]
            [Site "$site"]
            [Date "$date"]
            [Round ""]
            [White "$whitePlayer"]
            [Black "$blackPlayer"]
            [Result ""]
            [Goal "$goalValue"]
        """.trimIndent()
    }

    companion object {
        fun parse(value: String): GameHeaderData {
            val parts = value.split(separator)
            return GameHeaderData(
                whitePlayer = parts[0],
                blackPlayer = parts[1],
                event = parts[2],
                site = parts[3],
                date = parts[4],
                goal = GoalTagValue.valueOf(parts[5]),
                checkmateCount = Integer.parseInt(parts[6]),
            )
        }
    }
}

private val GameHeaderDataStateSaver = Saver<GameHeaderData, String>(
    save = { state -> state.toString() },
    restore = { value ->
        GameHeaderData.parse(value)
    }
)

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun SolutionEditor(
    startPosition: String,
    headerData: GameHeaderData,
    handleHeaderDataUpdate: (GameHeaderData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val context = LocalContext.current
    val activity = context as AppCompatActivity

    val isLandscape = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }

    var whitePlayerEditorActive by rememberSaveable {
        mutableStateOf(false)
    }

    var blackPlayerEditorActive by rememberSaveable {
        mutableStateOf(false)
    }

    var eventEditorActive by rememberSaveable {
        mutableStateOf(false)
    }

    var siteEditorActive by rememberSaveable {
        mutableStateOf(false)
    }

    var goalDropdownVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var checkmateCountEditorActive by rememberSaveable {
        mutableStateOf(false)
    }

    fun validate() {
        //todo : confirmation + validate
    }

    fun cancel() {
        //todo : confirmation + cancel
    }

    @SuppressLint("SimpleDateFormat")
    fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker().build()
        picker.show(activity.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            val newDate = try {
                val sdf = SimpleDateFormat("MM.dd.yyyy")
                val netDate = Date(it)
                sdf.format(netDate)
            } catch (e: Exception) {
                return@addOnPositiveButtonClickListener
            }
            headerData.date = newDate
            handleHeaderDataUpdate(headerData)
        }
        picker.addOnCancelListener {
            picker.dismiss()
        }
        picker.addOnDismissListener {
            picker.dismiss()
        }
    }

    @Composable
    fun headerZone() {
        fun getGoalText(goal: GoalTagValue): String {
            return context.getString(
                when (goal) {
                    GoalTagValue.WhiteWin -> R.string.white_should_win
                    GoalTagValue.BlackWin -> R.string.black_should_win
                    GoalTagValue.Draw -> R.string.it_should_be_draw
                    GoalTagValue.WhiteCheckmate -> R.string.white_checkmate_fragment
                    GoalTagValue.BlackCheckmate -> R.string.black_checkmate_fragment
                }
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row {
                Text(context.getString(R.string.white_player))
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    headerData.whitePlayer.ifEmpty { context.getString(R.string.unknown) },
                    color = if (headerData.whitePlayer.isEmpty()) Color.LightGray else Color.Black,
                    modifier = Modifier
                        .width(250.dp)
                        .underline(Color.Blue)
                        .clickable { whitePlayerEditorActive = true }
                )
            }
            Spacer(modifier = Modifier.size(3.dp))
            Row {
                Text(context.getString(R.string.black_player))
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    headerData.blackPlayer.ifEmpty { context.getString(R.string.unknown) },
                    color = if (headerData.blackPlayer.isEmpty()) Color.LightGray else Color.Black,
                    modifier = Modifier
                        .width(250.dp)
                        .underline(Color.Blue)
                        .clickable { blackPlayerEditorActive = true })
            }
            Spacer(modifier = Modifier.size(3.dp))
            Row {
                Text(context.getString(R.string.event))
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    headerData.event.ifEmpty { context.getString(R.string.unknown) },
                    color = if (headerData.event.isEmpty()) Color.LightGray else Color.Black,
                    modifier = Modifier
                        .width(250.dp)
                        .underline(Color.Blue)
                        .clickable { eventEditorActive = true })
            }
            Spacer(modifier = Modifier.size(3.dp))
            Row {
                Text(context.getString(R.string.site))
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    headerData.site.ifEmpty { context.getString(R.string.unknown) },
                    color = if (headerData.site.isEmpty()) Color.LightGray else Color.Black,
                    modifier = Modifier
                        .width(250.dp)
                        .underline(Color.Blue)
                        .clickable { siteEditorActive = true })
            }
            Spacer(modifier = Modifier.size(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(context.getString(R.string.date))
                Spacer(modifier = Modifier.size(5.dp))
                Text(headerData.date.ifEmpty { context.getString(R.string.unknown) },
                    color = if (headerData.date.isEmpty()) Color.LightGray else Color.Black,
                    modifier = Modifier
                        .width(250.dp)
                        .underline(Color.Blue)
                        .clickable { showDatePicker() }
                )
                IconButton(onClick = {
                    headerData.date = ""
                    handleHeaderDataUpdate(headerData)
                }) {
                    Icon(
                        Icons.Filled.Delete,
                        context.getString(R.string.erase),
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.size(3.dp))
            Row {
                Text(context.getString(R.string.goal))
                Spacer(
                    modifier = Modifier
                        .size(5.dp)
                )
                Text(getGoalText(headerData.goal),
                    modifier = Modifier
                        .width(215.dp)
                        .underline(Color.Blue)
                        .clickable { goalDropdownVisible = true }
                )
                if (headerData.goal == GoalTagValue.WhiteCheckmate
                    || headerData.goal == GoalTagValue.BlackCheckmate) {
                    Spacer(
                        modifier = Modifier
                            .size(1.5.dp)
                    )
                    Text(context.getString(R.string.preposition_in))
                    Spacer(
                        modifier = Modifier
                            .size(1.5.dp)
                    )
                    Text("${headerData.checkmateCount}",
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .width(40.dp)
                            .underline(Color.Blue)
                            .clickable {
                                checkmateCountEditorActive = true
                            }
                    )
                    Spacer(
                        modifier = Modifier
                            .size(2.dp)
                    )
                    Text(context.resources.getQuantityString(R.plurals.moves, headerData.checkmateCount))
                }
            }

            if (whitePlayerEditorActive) {
                TextValueEditor(
                    initialValue = headerData.whitePlayer,
                    caption = context.getString(R.string.white_player),
                    handleValueChanged = {
                        headerData.whitePlayer = it
                        handleHeaderDataUpdate(headerData)
                        whitePlayerEditorActive = false
                    }, handleDismissRequest = { whitePlayerEditorActive = false })
            }

            if (blackPlayerEditorActive) {
                TextValueEditor(
                    initialValue = headerData.blackPlayer,
                    caption = context.getString(R.string.black_player),
                    handleValueChanged = {
                        headerData.blackPlayer = it
                        handleHeaderDataUpdate(headerData)
                        blackPlayerEditorActive = false
                    }, handleDismissRequest = { blackPlayerEditorActive = false })
            }

            if (eventEditorActive) {
                TextValueEditor(
                    initialValue = headerData.event,
                    caption = context.getString(R.string.event),
                    handleValueChanged = {
                        headerData.event = it
                        handleHeaderDataUpdate(headerData)
                        eventEditorActive = false
                    }, handleDismissRequest = { eventEditorActive = false })
            }

            if (siteEditorActive) {
                TextValueEditor(
                    initialValue = headerData.site,
                    caption = context.getString(R.string.site),
                    handleValueChanged = {
                        headerData.site = it
                        handleHeaderDataUpdate(headerData)
                        siteEditorActive = false
                    }, handleDismissRequest = { siteEditorActive = false })
            }

            if (checkmateCountEditorActive) {
                NumericValueEditor(
                    initialValue = headerData.checkmateCount,
                    caption = context.getString(R.string.checkmate_count),
                    handleValueChanged = {
                        headerData.checkmateCount = it
                        handleHeaderDataUpdate(headerData)
                    },
                    handleDismissRequest = { checkmateCountEditorActive = false }
                )
            }

            DropdownMenu(
                expanded = goalDropdownVisible,
                onDismissRequest = { goalDropdownVisible = false }) {
                GoalTagValue.values().forEach {
                    DropdownMenuItem(onClick = {
                        headerData.goal = it
                        handleHeaderDataUpdate(headerData)
                        goalDropdownVisible = false
                    }) {
                        Text(text = getGoalText(it))
                    }
                }
            }
        }
    }

    @Composable
    fun solutionZone() {
        //todo set moves navigator
        Text("placeholder #2")
    }

    @ExperimentalPagerApi
    @ExperimentalMaterialApi
    @Composable
    fun editionZone() {
        val headerItem =
            SolutionEditorFieldsTabItem(titleRef = R.string.header, screen = { headerZone() })
        val solutionItem =
            SolutionEditorFieldsTabItem(titleRef = R.string.solution, screen = { solutionZone() })
        val fieldsTabItems = listOf(headerItem, solutionItem)

        val fieldsPagerState = rememberPagerState()
        val coroutineScope = rememberCoroutineScope()

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
                Text(context.getString(R.string.add_game))
            }
            Spacer(modifier = Modifier.size(5.dp))

            Button(
                onClick = ::cancel,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(context.getString(R.string.cancel))
            }
        }
    }

    if (isLandscape) {
        Row(modifier = modifier) {
            Spacer(modifier = Modifier.size(5.dp))
            DynamicChessBoard(
                position = startPosition,
                modifier = Modifier.size(screenHeight * 0.7f)
            )
            Spacer(modifier = Modifier.size(5.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.size(2.dp))
                editionZone()
                Spacer(modifier = Modifier.size(2.dp))
                validationButtonsZone()
            }
        }
    } else {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.size(5.dp))
            DynamicChessBoard(
                position = startPosition,
                modifier = Modifier.size(screenWidth * 0.7f)
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
    //todo handle back button and confirmation dialog
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    var oldPosition by rememberSaveable {
        mutableStateOf(STANDARD_FEN)
    }

    var isInPositionEditionMode by rememberSaveable {
        mutableStateOf(false)
    }

    var headerData by rememberSaveable(stateSaver = GameHeaderDataStateSaver) {
        mutableStateOf(GameHeaderData())
    }

    val coroutineScope = rememberCoroutineScope()

    fun goBack() {
        navController.popBackStack()
    }

    fun handlePositionEditionModeRequest() {
        isInPositionEditionMode = true
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
            val modeText = context.getString(
                if (isInPositionEditionMode) R.string.position_edition_mode
                else R.string.solution_edition_mode
            )
            Column {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFEEE382))
                        .height(15.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(modeText)
                    if (!isInPositionEditionMode) {
                        Spacer(modifier = Modifier.size(5.dp))
                        Surface(
                            onClick = ::handlePositionEditionModeRequest,
                            shape = MaterialTheme.shapes.medium,
                            color = Color(0xFF2196F3),
                            contentColor = Color.Black,
                            border = ButtonDefaults.outlinedBorder,
                            role = Role.Button,
                        ) {
                            Text(context.getString(R.string.edit_position), fontSize = 12.sp)
                        }
                    }
                }
                if (isInPositionEditionMode) {
                    PositionEditor(
                        modifier = Modifier.fillMaxSize(),
                        oldPosition = oldPosition,
                        handlePositionChanged = {
                            val correctedNewPosition = it.correctEnPassantSquare()
                            oldPosition = correctedNewPosition
                            // TODO erase current solution
                        },
                        handleIllegalPosition = {
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.illegal_position))
                            }
                        },
                        handleExitPositionEditionModeRequest = {
                            isInPositionEditionMode = false
                        }
                    )
                } else {
                    SolutionEditor(
                        modifier = Modifier.fillMaxSize(),
                        startPosition = oldPosition,
                        headerData = headerData,
                        handleHeaderDataUpdate = {
                            headerData = it
                            println(headerData.toPgnHeaderString())
                        }
                    )
                }
            }
        }
    }
}