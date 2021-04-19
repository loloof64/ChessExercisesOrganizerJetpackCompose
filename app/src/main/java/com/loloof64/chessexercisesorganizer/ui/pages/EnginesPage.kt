package com.loloof64.chessexercisesorganizer.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessoexscanner.ChessEngineUtils

@Composable
fun EnginesPage(navController: NavController? = null) {

    val currentContext = LocalContext.current
    val enginesUtils = ChessEngineUtils(
        appId = "com.loloof64.chessexercisesorganizer",
        context = currentContext
    )

    val storeEngines by remember{ mutableStateOf(enginesUtils.getMyStoreEnginesNames())}
    var installedEngines by remember { mutableStateOf(enginesUtils.listInstalledEngines())}

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
        EnginesPageContent(storeEngines = storeEngines, installedEngines = installedEngines,
            installRequestCallback = {
                enginesUtils.installEngineFromMyStore(it)
                installedEngines = enginesUtils.listInstalledEngines()
            }, deleteRequestCallback = {
                enginesUtils.deleteInstalledEngine(it)
                installedEngines = enginesUtils.listInstalledEngines()
            })
    }
}

@Composable
fun EnginesPageContent(
    storeEngines: Array<String>,
    installedEngines: Array<String>,
    installRequestCallback: (Int) -> Unit,
    deleteRequestCallback: (Int) -> Unit,
) {
    var installEngineName: String? by rememberSaveable { mutableStateOf(null) }
    var installEngineIndex: Int? by rememberSaveable {
        mutableStateOf(null)
    }

    var deleteEngineName: String? by rememberSaveable { mutableStateOf(null) }
    var deleteEngineIndex: Int? by rememberSaveable {
        mutableStateOf(null)
    }

    var openConfirmInstallDialog by rememberSaveable { mutableStateOf(false) }
    var openConfirmDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val installedListState = rememberLazyListState()
    val deletedListState = rememberLazyListState()

    fun confirmInstallEngine(index: Int) {
        installEngineIndex = index
        installEngineName = storeEngines[index]
        openConfirmInstallDialog = true
    }

    fun confirmDeleteEngine(index: Int) {
        deleteEngineIndex = index
        deleteEngineName = installedEngines[index]
        openConfirmDeleteDialog = true
    }

    Column {
        ConfirmInstallEngineDialog(
            isOpen = openConfirmInstallDialog,
            engineName = installEngineName,
            validateCallback = {
                installEngineIndex?.let { installRequestCallback(it) }
                installEngineIndex = null
                installEngineName = null
                openConfirmInstallDialog = false
            },
            dismissCallback = {
                openConfirmInstallDialog = false
                installEngineIndex = null
                installEngineName = null
            })
        ConfirmDeleteEngineDialog(
            isOpen = openConfirmDeleteDialog,
            engineName = deleteEngineName,
            validateCallback = {
                deleteEngineIndex?.let { deleteRequestCallback(it) }
                deleteEngineName = null
                deleteEngineIndex = null
                openConfirmDeleteDialog = false
            },
            dismissCallback = {
                openConfirmDeleteDialog = false
                deleteEngineName = null
                deleteEngineIndex = null
            })
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.available_engines))
            LazyColumn(state = installedListState, modifier = Modifier.fillMaxWidth()) {
                items(count = storeEngines.size,
                    itemContent = { index ->
                        Button(
                            onClick = {
                                confirmInstallEngine(index)
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant)
                        ) { Text(storeEngines[index]) }
                    }
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.installed_engines))
            LazyColumn(state = deletedListState, modifier = Modifier.fillMaxWidth()) {
                items(count = installedEngines.size,
                    itemContent = { index ->
                        Button(onClick = {
                            confirmDeleteEngine(index)
                        }, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant)) { Text(installedEngines[index]) }
                    })
            }
        }
    }
}

@Composable
fun ConfirmInstallEngineDialog(
    engineName: String?,
    isOpen: Boolean,
    validateCallback: () -> Unit,
    dismissCallback: () -> Unit
) {
    if (isOpen) {
        AlertDialog(onDismissRequest = { dismissCallback() },
            title = {
                Text(stringResource(R.string.confirm_install_engine_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.confirm_install_engine_message,
                        engineName ?: "#EngineNameError"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { validateCallback() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant)
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                Button(
                    onClick = { dismissCallback() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant)
                ) {
                    Text(stringResource(R.string.Cancel))
                }
            }
        )

    }
}

@Composable
fun ConfirmDeleteEngineDialog(
    engineName: String?,
    isOpen: Boolean,
    validateCallback: () -> Unit,
    dismissCallback: () -> Unit
) {
    if (isOpen) {
        AlertDialog(onDismissRequest = { dismissCallback() },
            title = {
                Text(stringResource(R.string.confirm_delete_engine_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.confirm_delete_engine_message,
                        engineName ?: "#ErrorEngineName"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { validateCallback() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant)
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                Button(
                    onClick = { dismissCallback() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant)
                ) {
                    Text(stringResource(R.string.Cancel))
                }
            }
        )

    }
}