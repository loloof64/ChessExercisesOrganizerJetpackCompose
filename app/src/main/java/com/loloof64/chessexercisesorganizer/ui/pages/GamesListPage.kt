package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.loloof64.chessexercisesorganizer.NavHostParcelizeArgs
import com.loloof64.chessexercisesorganizer.NavHostRoutes
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.core.PgnGameLoader
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import com.loloof64.chessexercisesorganizer.ui.components.moves_navigator.GamesLoadingException
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import kotlinx.coroutines.launch

sealed class FileData(open val caption: String)
data class AssetFileData(override val caption: String, val assetPath: String) :
    FileData(caption = caption)

@Composable
fun GamesListPage(
    navController: NavController,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val gamesLoadingErrorMessage = stringResource(R.string.game_loading_error)

    fun showMinutedSnackBarAction(text: String, duration: SnackbarDuration) {
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = text,
                duration = duration,
            )
        }
    }

    fun extractAssetGames(fileData: AssetFileData, context: Context): List<PGNGame> {
        val inputStream = context.assets.open(fileData.assetPath)
        val gamesFileContent = inputStream.bufferedReader().use { it.readText() }
        return PgnGameLoader().load(gamesFileContent = gamesFileContent)
    }

    fun extractGames(fileData: FileData, context: Context): List<PGNGame> {
        val result = try {
            when (fileData) {
                is AssetFileData -> extractAssetGames(fileData, context)
            }
        } catch (ex: GamesLoadingException) {
            println(ex)
            listOf()
        }
        if (result.isEmpty()) {
            showMinutedSnackBarAction(gamesLoadingErrorMessage, SnackbarDuration.Short)
        }
        return result
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
                        val games = extractGames(fileData = it, context = context)
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            NavHostParcelizeArgs.gamesList,
                            games
                        )
                        navController.navigate(NavHostRoutes.gamePage)
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
    LazyColumn(
        modifier = Modifier
            .background(Color.White)
            .clickable { },
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        arrayOf(
            AssetFileData(caption = "Test sample", assetPath = "pgn/dummy_sample.pgn"),
            AssetFileData(caption = "Error sample", assetPath = "pgn/error.pgn"),
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