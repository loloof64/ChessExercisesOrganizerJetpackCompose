package com.loloof64.chessexercisesorganizer.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.loloof64.chessexercisesorganizer.R

@Composable
fun EnginesPage(navController: NavController? = null) {
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
        EnginesPageContent()
    }
}

@Composable
fun EnginesPageContent() {
    Column {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.available_engines))
            LazyColumn {

            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.installed_engines))
            LazyColumn {

            }
        }
    }
}