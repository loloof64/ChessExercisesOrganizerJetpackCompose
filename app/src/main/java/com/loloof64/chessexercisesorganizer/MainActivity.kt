package com.loloof64.chessexercisesorganizer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.loloof64.chessexercisesorganizer.core.pgnparser.PGNGame
import com.loloof64.chessexercisesorganizer.ui.pages.GamePage
import com.loloof64.chessexercisesorganizer.ui.pages.GamesListPage
import com.loloof64.stockfish.StockfishLib
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : AppCompatActivity() {

    private lateinit var stockfishLib: StockfishLib

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stockfishLib = StockfishLib()
        setContent {
            MainContent(stockfishLib)
        }
    }

    override fun onStop() {
        stockfishLib.quit()
        super.onStop()
    }
}

@Composable
fun MainContent(stockfishLib: StockfishLib) {
    val navController = rememberNavController()
    val emptyPgnGame = PGNGame(tags = mutableMapOf(), moves = null)
    var selectedFileGames by rememberSaveable {
        mutableStateOf<List<PGNGame>>(listOf())
    }
    var selectedGameIndex by rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    NavHost(navController, startDestination = "gamesList") {
        composable("gamesList") { GamesListPage(fileSelectedHandler = { gamesList ->
            selectedFileGames = gamesList
            if (selectedFileGames.isNotEmpty()) {
                selectedGameIndex = 0
                navController.navigate("game")
            }
            else {
                selectedGameIndex = null
            }
        }) }
        composable("game") {
            GamePage(
                navController = navController,
                stockfishLib = stockfishLib,
                selectedGameData = if (selectedGameIndex != null) {
                    selectedFileGames[selectedGameIndex!!]
                }
                else {
                    emptyPgnGame
                }
            )
        }
    }
}