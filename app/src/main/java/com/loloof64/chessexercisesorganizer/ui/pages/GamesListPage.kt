package com.loloof64.chessexercisesorganizer.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.loloof64.chessexercisesorganizer.MyApplication
import com.loloof64.chessexercisesorganizer.NavHostRoutes
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.core.domain.AssetFileData
import com.loloof64.chessexercisesorganizer.core.domain.FileData
import com.loloof64.chessexercisesorganizer.ui.components.moves_navigator.GamesLoadingException
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GamesListPage(
    navController: NavController,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val gamesLoadingErrorMessage = stringResource(R.string.game_loading_error)

    suspend fun showMinutedSnackBarAction(text: String, duration: SnackbarDuration) {
        withContext(Dispatchers.Main.immediate) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = text,
                duration = duration,
            )
        }
    }

    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.games_list_page)) })
            },
            content = {
                SampleGamesList(
                    itemSelectedHandler = {
                        coroutineScope.launch(Dispatchers.Main.immediate) {
                            (context.applicationContext as MyApplication)
                                .gamesFromFileExtractorUseCase.extractGames(
                                    fileData = it,
                                    context = context
                                )
                            val games = (context.applicationContext as MyApplication)
                                .gamesFromFileExtractorUseCase.currentGames()
                            if (games.isNullOrEmpty()) {
                                showMinutedSnackBarAction(
                                    gamesLoadingErrorMessage,
                                    SnackbarDuration.Short
                                )
                            } else {
                                navController.navigate(NavHostRoutes.gameSelectorPage)
                            }
                        }
                    }
                )
            })
    }
}

@Composable
fun FileItem(
    fileData: FileData,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.ic_text_file),
            contentDescription = fileData.caption,
            modifier = Modifier.size(60.dp)
        )
        Text(
            text = fileData.caption,
            color = Color.Black,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SampleGamesList(itemSelectedHandler: (itemData: FileData) -> Unit = { _ -> }) {
    val pgnKPK = stringResource(R.string.KP_K)
    val pgnKQK = stringResource(R.string.KQ_K)
    val pgnK2RK = stringResource(R.string.K2R_K)
    val pgnKRK = stringResource(R.string.KR_K)

    LazyColumn(
        modifier = Modifier
            .background(Color.White)
            .clickable { },
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        arrayOf(
            AssetFileData(caption = pgnKPK, assetPath = "pgn/KP_K.pgn"),
            AssetFileData(caption = pgnKQK, assetPath = "pgn/KQ_K.pgn"),
            AssetFileData(caption = pgnK2RK, assetPath = "pgn/K2R_K.pgn"),
            AssetFileData(caption = pgnKRK, assetPath = "pgn/KR_K.pgn"),
        ).map {
            item {
                Box(modifier = Modifier.clickable {
                    itemSelectedHandler(it)
                }) {
                    FileItem(fileData = it)
                }
            }
        }
    }
}

@Composable
@Preview
fun FileItemPreview() {
    FileItem(fileData = AssetFileData(caption = "My sample", assetPath = "pgn/dummy_sample.pgn"))
}

@Composable
@Preview
fun SampleGamesListPreview() {
    SampleGamesList()
}