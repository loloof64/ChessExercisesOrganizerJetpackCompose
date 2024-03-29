package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.loloof64.chessexercisesorganizer.NavHostRoutes
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import com.loloof64.chessexercisesorganizer.ui.components.STANDARD_FEN
import com.loloof64.chessexercisesorganizer.ui.components.StaticChessBoard
import com.loloof64.chessexercisesorganizer.ui.components.toBoard
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme

class IllegalMoveException : Exception()

@Composable
fun ValidationButtonIfAppropriate(mustBeShown: Boolean, handleGameSelected: () -> Unit) {
    val context = LocalContext.current
    if (mustBeShown) {
        Button(onClick = { handleGameSelected() }) {
            Text(context.getString(R.string.select_game), fontSize = 12.sp)
        }
    }
}

@Composable
fun GameSelectionPage(
    navController: NavController,
) {
    val scaffoldState = rememberScaffoldState()

    val context = LocalContext.current

    val games = (context.applicationContext as MyApplication)
        .gamesFromFileExtractorUseCase.games

    val pagesCount = games?.size ?: 0

    var pageIndex by rememberSaveable {
        mutableStateOf(0)
    }

    fun selectGame(gameIndex: Int) {
        navController.navigate(NavHostRoutes.getGamePage(gameIndex))
    }

    fun goBack() {
        navController.popBackStack()
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

    val arrowBackDescription = stringResource(R.string.arrow_back_button)

    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.game_selection_page)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            goBack()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = arrowBackDescription,
                            )
                        }
                    })
            },
            content = {
                GameSelectionZone(
                    pageIndex = pageIndex,
                    handleGameSelected = { selectGame(it) },
                    validationButton = { mustBeShown, gameIndex ->
                        ValidationButtonIfAppropriate(
                            mustBeShown = mustBeShown,
                            handleGameSelected = {
                                selectGame(gameIndex)
                            })
                    },
                    gotoFirstPage = ::gotoFirstPage,
                    gotoPreviousPage = ::gotoPreviousGame,
                    gotoNextPage = ::gotoNextGame,
                    gotoLastPage = ::gotoLastPage,
                    gotoSelectedPage = ::gotoSelectedPage,
                    games = games,
                )
            }
        )
    }
}

@Composable
fun GameSelectionZone(
    modifier: Modifier = Modifier,
    pageIndex: Int,
    games: List<PGNGame>?,
    validationButton: @Composable (Boolean, Int) -> Unit = { _, _ -> },
    handleGameSelected: (Int) -> Unit = { _ -> },
    gotoFirstPage: () -> Unit,
    gotoPreviousPage: () -> Unit,
    gotoNextPage: () -> Unit,
    gotoLastPage: () -> Unit,
    gotoSelectedPage: (Int) -> Unit,
) {
    val context = LocalContext.current
    val pagesCount = games?.size ?: 0

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

    fun currentGameHasSolution(): Boolean {
        return games?.get(pageIndex)?.moves != null
    }

    fun currentGameHasIllegalSolution(): Boolean {
        val solutionRoot = games?.get(pageIndex)?.moves
        if (solutionRoot == null) return false
        else {
            return try {
                var currentNode = solutionRoot
                val startPosition = getStartPosition()
                val testGame = startPosition.toBoard()
                do {
                    val legalMove =
                        testGame.doMove(Move.getFromString(testGame, currentNode?.moveValue, true))
                    if (!legalMove) throw IllegalMoveException()
                    currentNode = currentNode?.nextNode
                } while (currentNode != null)
                false
            } catch (ex: IllegalMoveException) {
                true
            }
        }
    }

    fun currentGameHasIllegalStartPosition(): Boolean {
        val startPosition = games?.get(pageIndex)?.tags?.get("FEN") ?: STANDARD_FEN
        val testBoard = startPosition.toBoard()
        return !testBoard.checkValidityCompletely()
    }

    val isLandscape = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }


    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val selectGameText = stringResource(R.string.select_game)

    fun getGoalText(): String {
        val whiteCheckmateRegex = """#(\d+)-0""".toRegex()
        val blackCheckmateRegex = """0-#(\d+)""".toRegex()

        val goalTypeText = games?.get(pageIndex)?.tags?.get("Goal") ?: ""
        val checkmateMoves = when {
            whiteCheckmateRegex.matches(goalTypeText) -> Integer.parseInt(
                whiteCheckmateRegex.matchEntire(
                    goalTypeText
                )!!.groupValues[1]
            )
            blackCheckmateRegex.matches(goalTypeText) -> Integer.parseInt(
                blackCheckmateRegex.matchEntire(
                    goalTypeText
                )!!.groupValues[1]
            )
            else -> -1
        }
        return when {
            goalTypeText == "1-0" -> context.getString(R.string.white_should_win)
            goalTypeText == "0-1" -> context.getString(R.string.black_should_win)
            goalTypeText == "1/2-1/2" -> context.getString(R.string.it_should_be_draw)
            whiteCheckmateRegex.matches(goalTypeText) -> context.resources.getQuantityString(
                R.plurals.white_should_checkmate,
                checkmateMoves,
                checkmateMoves,
            )
            blackCheckmateRegex.matches(goalTypeText) -> context.resources.getQuantityString(
                R.plurals.black_should_checkmate,
                checkmateMoves,
                checkmateMoves,
            )
            else -> ""
        }
    }

    if (isLandscape) {
        Row(modifier = modifier.fillMaxSize()) {
            StaticChessBoard(
                position = getStartPosition(),
                reversed = getReversedStatus(),
                modifier = Modifier.size(screenHeight * 0.83f),
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameSelectionNavigationBar(
                    pageIndex = pageIndex, pagesCount = pagesCount,
                    onPageSelected = { gotoSelectedPage(it) },
                    handleFirstGameRequest = { gotoFirstPage() },
                    handleLastGameRequest = { gotoLastPage() },
                    handlePreviousGameRequest = { gotoPreviousPage() },
                    handleNextGameRequest = { gotoNextPage() }
                )
                GameInformationZone(
                    goal = getGoalText(),
                    whiteText = getWhitePlayer(),
                    blackText = getBlackPlayer(),
                    dateText = getDate(),
                    eventText = getEvent(),
                    siteText = getSite(),
                    hasSolution = currentGameHasSolution(),
                    hasIllegalSolution = currentGameHasIllegalSolution(),
                    isIllegalPosition = currentGameHasIllegalStartPosition(),
                )
                validationButton(!currentGameHasIllegalStartPosition(), pageIndex)
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
                handleFirstGameRequest = { gotoFirstPage() },
                handleLastGameRequest = { gotoLastPage() },
                handlePreviousGameRequest = { gotoPreviousPage() },
                handleNextGameRequest = { gotoNextPage() },
            )
            StaticChessBoard(
                position = getStartPosition(),
                reversed = getReversedStatus(),
                modifier = Modifier.size(screenWidth * 0.75f),
            )
            GameInformationZone(
                goal = getGoalText(),
                whiteText = getWhitePlayer(),
                blackText = getBlackPlayer(),
                dateText = getDate(),
                eventText = getEvent(),
                siteText = getSite(),
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

    var input by remember {
        mutableStateOf("1")
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
            TextField(
                value = input, onValueChange = {
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
    goal: String = "",
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

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (infoText.isNotEmpty()) {
            Text(
                text = infoText,
                fontSize = textSize,
                textAlign = TextAlign.Center,
                style = TextStyle.Default.copy(color = textColor)
            )
        }
        if (goal.isNotEmpty()) {
            Text(
                text = goal,
                fontSize = textSize,
                textAlign = TextAlign.Center,
                style = TextStyle.Default.copy(color = textColor)
            )
        }
        Row {
            Text(
                text = whiteText,
                fontSize = textSize,
                textAlign = TextAlign.Center,
                style = TextStyle.Default.copy(color = textColor)
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = "-",
                fontSize = textSize,
                textAlign = TextAlign.Center,
                style = TextStyle.Default.copy(color = textColor)
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = blackText,
                fontSize = textSize,
                textAlign = TextAlign.Center,
                style = TextStyle.Default.copy(color = textColor)
            )
        }
        Text(
            text = dateText,
            fontSize = textSize,
            style = TextStyle.Default.copy(color = textColor)
        )
        Text(
            text = eventText,
            fontSize = textSize,
            textAlign = TextAlign.Center,
            style = TextStyle.Default.copy(color = textColor)
        )
        Text(
            text = siteText,
            fontSize = textSize,
            textAlign = TextAlign.Center,
            style = TextStyle.Default.copy(color = textColor)
        )
    }
}