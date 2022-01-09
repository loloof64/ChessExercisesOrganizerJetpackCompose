package com.loloof64.chessexercisesorganizer.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.loloof64.chessexercisesorganizer.MyApplication
import com.loloof64.chessexercisesorganizer.NavHostRoutes
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme

@Composable
fun EditGamePreview(
    pageIndex: Int,
    games: List<PGNGame>?,
    gotoFirstPage: () -> Unit,
    gotoPreviousPage: () -> Unit,
    gotoNextPage: () -> Unit,
    gotoLastPage: () -> Unit,
    gotoSelectedPage: (Int) -> Unit
) {
    GameSelectionZone(
        gotoFirstPage = {gotoFirstPage()},
        gotoPreviousPage = {gotoPreviousPage()},
        gotoNextPage = {gotoNextPage()},
        gotoLastPage = {gotoLastPage()},
        gotoSelectedPage = {gotoSelectedPage(it)},
        games = games,
        pageIndex = pageIndex,
    )
}

@Composable
fun EditPgnFilePage(
    navController: NavController,
    encodedLocalPath: String,
) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    val games = (context.applicationContext as MyApplication)
        .gamesFromFileExtractorUseCase.games
    val pagesCount = games?.size ?: 0


    var pageIndex by rememberSaveable {
        mutableStateOf(0)
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

    fun goBack() {
        navController.popBackStack()
    }

    fun addGameAt(pageIndex: Int) {
        navController.navigate(NavHostRoutes.getGameEditorPage(index = pageIndex))
    }


    ChessExercisesOrganizerJetpackComposeTheme()
    {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.pgn_games_editor_page)) },
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
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    addGameAt(pageIndex)
                }) {
                    Icon(Icons.Filled.Add, context.getString(R.string.add_game_to_file))
                }
            },
        ) {
            if (games?.isEmpty() == false || games != null) {
                EditGamePreview(
                    pageIndex = pageIndex,
                    games = games,
                    gotoFirstPage = { gotoFirstPage()},
                    gotoPreviousPage = {gotoPreviousGame()},
                    gotoNextPage = {gotoNextGame()},
                    gotoLastPage = {gotoLastPage()},
                    gotoSelectedPage = {gotoSelectedPage(it)},
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = context.getString(R.string.no_game_in_pgn), fontSize = 20.sp)
                }
            }
        }
    }
}