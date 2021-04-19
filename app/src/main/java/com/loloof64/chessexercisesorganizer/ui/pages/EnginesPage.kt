package com.loloof64.chessexercisesorganizer.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessoexscanner.ChessEngineUtils
import java.util.*

@Composable
fun EnginesPage(navController: NavController? = null) {
    val currentContext = LocalContext.current
    val enginesUtils = ChessEngineUtils(
        appId = "com.loloof64.chessexercisesorganizer",
        context = currentContext
    )

    val storeEngines = enginesUtils.getMyStoreEnginesNames()
    val installedEngines = enginesUtils.listInstalledEngines()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.engines_page)) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = Icons.Filled.ArrowBack.name
                        )
                    }
                })
        }
    ) {
        EnginesPageContent(storeEngines = storeEngines, installedEngines = installedEngines)
    }
}

@Composable
fun EnginesPageContent(storeEngines: Array<String>, installedEngines: Array<String>) {
    Column {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.available_engines))
            LazyColumn {
                items(count = storeEngines.size,
                    itemContent = { index -> Text(storeEngines[index]) }
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.installed_engines))
            LazyColumn {
                items(count = installedEngines.size,
                    itemContent = { index -> Text(installedEngines[index]) })
            }
        }
    }
}