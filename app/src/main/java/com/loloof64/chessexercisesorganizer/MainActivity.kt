package com.loloof64.chessexercisesorganizer

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.loloof64.chessexercisesorganizer.core.domain.FileGamesExtractor
import com.loloof64.chessexercisesorganizer.core.domain.GamesFromFileDataSource
import com.loloof64.chessexercisesorganizer.core.domain.GamesFromFileExtractorUseCase
import com.loloof64.chessexercisesorganizer.core.domain.GamesFromFileRepository
import com.loloof64.chessexercisesorganizer.ui.pages.GamePage
import com.loloof64.chessexercisesorganizer.ui.pages.GamesListPage

object NavHostRoutes {
    const val gamesListPage = "gamesList"
    const val gamePage = "gamePage"
}

class MyApplication : Application() {
    val gamesFromFileExtractorUseCase by lazy {
        GamesFromFileExtractorUseCase(
            GamesFromFileDataSource(
                GamesFromFileRepository(
                    FileGamesExtractor()
                )
            )
        )
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
                navController = navController,
            )
        }
        composable(NavHostRoutes.gamePage) {
            GamePage(
                navController = navController,
            )
        }
    }
}