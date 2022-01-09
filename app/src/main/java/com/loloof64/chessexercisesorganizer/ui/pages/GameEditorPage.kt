package com.loloof64.chessexercisesorganizer.ui.pages

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme

@Composable
fun GameEditorPage(navController: NavHostController, index: Int) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    fun goBack() {
        navController.popBackStack()
    }

    ChessExercisesOrganizerJetpackComposeTheme()
    {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.game_editor_page)) },
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
        ) {

        }
    }
}