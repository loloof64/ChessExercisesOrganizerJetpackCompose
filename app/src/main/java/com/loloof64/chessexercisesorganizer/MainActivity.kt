package com.loloof64.chessexercisesorganizer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.loloof64.chessexercisesorganizer.ui.pages.GamePage
import com.loloof64.stockfish.StockfishLib

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
    NavHost(navController, startDestination = "game") {
        composable("game") { GamePage(stockfishLib =  stockfishLib) }
    }
}