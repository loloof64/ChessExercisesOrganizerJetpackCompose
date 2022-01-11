package com.loloof64.chessexercisesorganizer

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.loloof64.chessexercisesorganizer.core.domain.GamesFromFileUseCase
import com.loloof64.chessexercisesorganizer.ui.pages.EditPgnFilePage
import com.loloof64.chessexercisesorganizer.ui.pages.GameEditorPage
import com.loloof64.chessexercisesorganizer.ui.pages.GameSelectionPage
import com.loloof64.chessexercisesorganizer.ui.pages.GamesListPage
import com.loloof64.chessexercisesorganizer.ui.pages.game_page.GamePage
import java.lang.IllegalArgumentException

object NavHostRoutes {
    const val gamesListPage = "gamesList"
    const val gamePage = "gamePage/{gameId}"
    fun getGamePage(gameId: Int) = "gamePage/$gameId"
    const val gameSelectorPage = "gameSelector"
    const val pgnFileEditorPage = "pgnFileEditor/{encodedPath}"
    fun getPgnFileEditorPage(path: String) = "pgnFileEditor/$path"
    const val gameEditorPage = "gameEditor/{index}"
    fun getGameEditorPage(index: Int) = "gameEditor/$index"
}

class MyApplication : Application() {
    val gamesFromFileExtractorUseCase by lazy {
        GamesFromFileUseCase()
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }

}

@Composable
fun MainContent() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = NavHostRoutes.gamesListPage) {
        composable(NavHostRoutes.gamesListPage) {
            GamesListPage(
                hostNavController = navController,
            )
        }
        composable(
            NavHostRoutes.gamePage,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) {
            GamePage(
                navController = navController,
                gameId = it.arguments?.getInt("gameId") ?: 0,
            )
        }
        composable(NavHostRoutes.gameSelectorPage) {
            GameSelectionPage(navController = navController)
        }
        composable(NavHostRoutes.pgnFileEditorPage) {
            EditPgnFilePage(
                navController = navController,
                encodedLocalPath = it.arguments?.getString("encodedPath")
                    ?: throw IllegalArgumentException("No encoded path given !")
            )
        }
        composable(
            NavHostRoutes.gameEditorPage,
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) {
            GameEditorPage(
                navController = navController,
                index = it.arguments?.getInt("index")
                    ?: throw IllegalArgumentException("No game index given !")
            )
        }
    }
}