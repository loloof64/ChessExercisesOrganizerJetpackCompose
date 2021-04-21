package com.loloof64.chessexercisesorganizer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.loloof64.chessexercisesorganizer.ui.pages.EnginesPage
import com.loloof64.chessexercisesorganizer.ui.pages.GamePage

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
    NavHost(navController, startDestination = "game") {
        composable("game") { GamePage(navController = navController) }
        composable("engines") { EnginesPage(navController = navController) }
    }
}