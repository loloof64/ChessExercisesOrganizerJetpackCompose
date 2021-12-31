package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alonsoruibal.chess.Move
import com.loloof64.chessexercisesorganizer.MyApplication
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.components.STANDARD_FEN
import com.loloof64.chessexercisesorganizer.ui.components.StaticChessBoard
import com.loloof64.chessexercisesorganizer.ui.components.toBoard
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme

class IllegalMoveException : Exception()

@Composable
fun GameSelectionPage(
    navController: NavController,
) {
    val scaffoldState = rememberScaffoldState()

    fun selectGame(gameIndex: Int) {
        navController.navigate("gamePage/${gameIndex}")
    }

    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.game_selection_page)) })
            },
            content = {
                GameSelectionZone(
                    handleGameSelected = { selectGame(it) }
                )
            }
        )
    }
}

@Composable
fun GameSelectionZone(
    modifier: Modifier = Modifier,
    handleGameSelected: (Int) -> Unit = { _ -> },
) {
    val context = LocalContext.current
    val games = (context.applicationContext as MyApplication)
        .gamesFromFileExtractorUseCase.currentGames()
    val pagesCount = games?.size ?: 0

    var pageIndex by rememberSaveable {
        mutableStateOf(0)
    }

    fun getWhitePlayer(): String {
        return games?.get(pageIndex)?.tags?.get("White") ?: "?"
    }

    fun getBlackPlayer(): String {
        return games?.get(pageIndex)?.tags?.get("Black") ?: "?"
    }

    fun getDate(): String {
        return games?.get(pageIndex)?.tags?.get("Date") ?: "????.??.??"
    }

    fun getEvent(): String {
        return games?.get(pageIndex)?.tags?.get("Event") ?: "?"
    }

    fun getSite(): String {
        return games?.get(pageIndex)?.tags?.get("Site") ?: "?"
    }

    fun getStartPosition(): String {
        return games?.get(pageIndex)?.tags?.get("FEN") ?: STANDARD_FEN
    }

    fun getReversedStatus(): Boolean {
        val fen = games?.get(pageIndex)?.tags?.get("FEN") ?: STANDARD_FEN
        return fen.split(" ")[1] == "b"
    }

    fun gotoFirstPage() {
        pageIndex = 0
    }

    fun gotoLastPage() {
        pageIndex = pagesCount - 1
    }

    fun gotoPreviousGame() {
        if (pageIndex > 0) pageIndex--
    }

    fun gotoNextGame() {
        if (pageIndex < pagesCount - 1) pageIndex++
    }

    fun gotoSelectedPage(index: Int) {
        pageIndex = index
    }

    fun currentGameHasSolution() : Boolean {
        return games?.get(pageIndex)?.moves != null
    }

    fun currentGameHasIllegalSolution(): Boolean {
        val solutionRoot = games?.get(pageIndex)?.moves
        if (solutionRoot == null) return false
        else {
            try {
                var currentNode = solutionRoot
                val startPosition = getStartPosition()
                val testGame = startPosition.toBoard()
                do {
                    val legalMove =
                        testGame.doMove(Move.getFromString(testGame, currentNode?.moveValue, true))
                    if (!legalMove) throw IllegalMoveException()
                    currentNode = currentNode?.nextNode
                } while (currentNode != null)
                return false
            }
            catch (ex: IllegalMoveException) {
                return true
            }
        }
    }

    fun currentGameHasIllegalStartPosition(): Boolean {
        val startPosition = games?.get(pageIndex)?.tags?.get("FEN") ?: STANDARD_FEN
        val testBoard = startPosition.toBoard()
        return !testBoard.checkValidityCompletely()
    }

    var whitePlayer by rememberSaveable {
        mutableStateOf(getWhitePlayer())
    }
    var blackPlayer by rememberSaveable {
        mutableStateOf(getBlackPlayer())
    }
    var date by rememberSaveable {
        mutableStateOf(getDate())
    }
    var event by rememberSaveable {
        mutableStateOf(getEvent())
    }
    var site by rememberSaveable {
        mutableStateOf(getSite())
    }

    var startPosition by rememberSaveable {
        mutableStateOf(getStartPosition())
    }
    var reversed by rememberSaveable {
        mutableStateOf(getReversedStatus())
    }

    val isLandscape = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }

    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val selectGameText = stringResource(R.string.select_game)

    SideEffect {
        whitePlayer = getWhitePlayer()
        blackPlayer = getBlackPlayer()
        date = getDate()
        event = getEvent()
        site = getSite()

        startPosition = getStartPosition()
        reversed = getReversedStatus()
    }

    if (isLandscape) {
        Row(modifier = modifier.fillMaxSize()) {
            StaticChessBoard(
                position = startPosition,
                reversed = reversed,
                modifier = Modifier.size(screenHeight * 0.83f),
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameSelectionNavigationBar(
                    pageIndex = pageIndex, pagesCount = pagesCount,
                    onPageSelected = { gotoSelectedPage(it) },
                    handleFirstGameRequest = ::gotoFirstPage,
                    handleLastGameRequest = ::gotoLastPage,
                    handlePreviousGameRequest = ::gotoPreviousGame,
                    handleNextGameRequest = ::gotoNextGame
                )
                GameInformationZone(
                    whiteText = whitePlayer,
                    blackText = blackPlayer,
                    dateText = date,
                    eventText = event,
                    siteText = site,
                    hasSolution = currentGameHasSolution(),
                    hasIllegalSolution = currentGameHasIllegalSolution(),
                    isIllegalPosition = currentGameHasIllegalStartPosition(),
                )
                if (!currentGameHasIllegalStartPosition()) {
                    Button(onClick = { handleGameSelected(pageIndex) }) {
                        Text(selectGameText, fontSize = 12.sp)
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameSelectionNavigationBar(
                pageIndex = pageIndex, pagesCount = pagesCount,
                onPageSelected = { gotoSelectedPage(it) },
                handleFirstGameRequest = ::gotoFirstPage,
                handleLastGameRequest = ::gotoLastPage,
                handlePreviousGameRequest = ::gotoPreviousGame,
                handleNextGameRequest = ::gotoNextGame,
            )
            StaticChessBoard(
                position = startPosition,
                reversed = reversed,
                modifier = Modifier.size(screenWidth * 0.75f),
            )
            GameInformationZone(
                whiteText = whitePlayer,
                blackText = blackPlayer,
                dateText = date,
                eventText = event,
                siteText = site,
                hasSolution = currentGameHasSolution(),
                hasIllegalSolution = currentGameHasIllegalSolution(),
                isIllegalPosition = currentGameHasIllegalStartPosition(),
            )
            if (!currentGameHasIllegalStartPosition()) {
                Button(onClick = { handleGameSelected(pageIndex) }) {
                    Text(selectGameText, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun GameSelectionNavigationBar(
    modifier: Modifier = Modifier,
    pageIndex: Int,
    pagesCount: Int,
    handleFirstGameRequest: () -> Unit = {},
    handlePreviousGameRequest: () -> Unit = {},
    handleNextGameRequest: () -> Unit = {},
    handleLastGameRequest: () -> Unit = {},
    onPageSelected: (Int) -> Unit = {},
) {
    val iconsModifier = Modifier.size(128.dp)
    val iconsTint = Color.Blue
    val textSize = 20.sp

    var input by rememberSaveable {
        mutableStateOf("${pageIndex + 1}")
    }

    fun tryUpdatingSelectedValue() {
        try {
            val selectedPageIndex = Integer.parseInt(input) - 1
            if (selectedPageIndex < 0 || selectedPageIndex >= pagesCount) throw IllegalArgumentException()
            onPageSelected(selectedPageIndex)
        } catch (ex: Exception) {
            // Nothing to do
        }
    }

    val updateText = stringResource(id = R.string.update_button)

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { handleFirstGameRequest() }) {
                Icon(
                    contentDescription = stringResource(id = R.string.first_game),
                    tint = iconsTint,
                    modifier = iconsModifier,
                    imageVector = Icons.Filled.FirstPage,
                )
            }
            IconButton(onClick = { handlePreviousGameRequest() }) {
                Icon(
                    contentDescription = stringResource(id = R.string.previous_game),
                    tint = iconsTint,
                    modifier = iconsModifier,
                    imageVector = Icons.Filled.ArrowLeft,
                )
            }
            Pagination(
                pageIndex = pageIndex,
                pagesCount = pagesCount,
            )
            IconButton(onClick = { handleNextGameRequest() }) {
                Icon(
                    contentDescription = stringResource(id = R.string.next_game),
                    tint = iconsTint,
                    modifier = iconsModifier,
                    imageVector = Icons.Filled.ArrowRight,
                )
            }
            IconButton(onClick = { handleLastGameRequest() }) {
                Icon(
                    contentDescription = stringResource(id = R.string.last_game),
                    tint = iconsTint,
                    modifier = iconsModifier,
                    imageVector = Icons.Filled.LastPage,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(value = input, onValueChange = {
                input = it
            },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = textSize,
                    textAlign = TextAlign.End
                ),
                modifier = Modifier
                    .width(70.dp)
            )
            Spacer(Modifier.size(10.dp))
            Button(onClick = { tryUpdatingSelectedValue() }) {
                Text(text = updateText, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun Pagination(
    modifier: Modifier = Modifier,
    pageIndex: Int,
    pagesCount: Int,
) {
    val textSize = 20.sp

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(text = "${pageIndex + 1}", fontSize = textSize, textAlign = TextAlign.Center)
        Text(text = "/", fontSize = textSize, textAlign = TextAlign.Center)
        Text(text = "$pagesCount", fontSize = textSize, textAlign = TextAlign.Center)
    }
}

@Composable
fun GameInformationZone(
    modifier: Modifier = Modifier,
    whiteText: String = "?",
    blackText: String = "?",
    dateText: String = "????.??.??",
    eventText: String = "?",
    siteText: String = "?",
    hasSolution: Boolean,
    isIllegalPosition: Boolean,
    hasIllegalSolution: Boolean,
) {
    val textSize = 16.sp
    val hasSolutionText = stringResource(R.string.has_solution)
    val isIllegalPositionText = stringResource(R.string.illegal_start_position)
    val hasIllegalSolutionText = stringResource(R.string.illegal_solution)

    val infoText = when {
        hasIllegalSolution -> hasIllegalSolutionText
        isIllegalPosition -> isIllegalPositionText
        hasSolution -> hasSolutionText
        else -> ""
    }

    val textColor = if (isIllegalPosition) Color.Red else MaterialTheme.typography.body1.color

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (infoText.isNotEmpty()) {
            Text(text = infoText, fontSize = textSize, textAlign = TextAlign.Center,  style = TextStyle.Default.copy(color = textColor))
        }
        Row {
            Text(text = whiteText, fontSize = textSize, textAlign = TextAlign.Center,  style = TextStyle.Default.copy(color = textColor))
            Spacer(Modifier.size(10.dp))
            Text(text = "-", fontSize = textSize, textAlign = TextAlign.Center,  style = TextStyle.Default.copy(color = textColor))
            Spacer(Modifier.size(10.dp))
            Text(text = blackText, fontSize = textSize, textAlign = TextAlign.Center,  style = TextStyle.Default.copy(color = textColor))
        }
        Text(text = dateText, fontSize = textSize,  style = TextStyle.Default.copy(color = textColor))
        Text(text = eventText, fontSize = textSize, textAlign = TextAlign.Center,  style = TextStyle.Default.copy(color = textColor))
        Text(text = siteText, fontSize = textSize, textAlign = TextAlign.Center,  style = TextStyle.Default.copy(color = textColor))
    }
}