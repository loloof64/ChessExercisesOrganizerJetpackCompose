package com.loloof64.chessexercisesorganizer.ui.pages

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.loloof64.chessexercisesorganizer.MyApplication
import com.loloof64.chessexercisesorganizer.NavHostRoutes
import com.loloof64.chessexercisesorganizer.R
import com.loloof64.chessexercisesorganizer.core.domain.*
import com.loloof64.chessexercisesorganizer.ui.theme.ChessExercisesOrganizerJetpackComposeTheme
import com.loloof64.chessexercisesorganizer.utils.encodePath
import com.loloof64.chessexercisesorganizer.utils.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object NestedNavHostRoutes {
    const val sampleGamesListPage = "sampleGamesList"
    const val customGamesListPage = "customGamesList"
}

sealed class GamesListPageScreen(
    val route: String,
    @StringRes val textId: Int,
    @DrawableRes val imageId: Int
) {
    object Samples : GamesListPageScreen(
        route = NestedNavHostRoutes.sampleGamesListPage,
        textId = R.string.sample_games,
        imageId = R.drawable.ic_gift,
    )

    object Customs : GamesListPageScreen(
        route = NestedNavHostRoutes.customGamesListPage,
        textId = R.string.custom_games,
        imageId = R.drawable.ic_homework,
    )
}

@Composable
fun Samples(
    hostNavController: NavController,
    scaffoldState: ScaffoldState,
) {
    val coroutineScope = rememberCoroutineScope()
    val gamesLoadingErrorMessage = stringResource(R.string.game_loading_error)
    val context = LocalContext.current

    suspend fun showMinutedSnackBarAction(text: String, duration: SnackbarDuration) {
        withContext(Dispatchers.Main.immediate) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = text,
                duration = duration,
            )
        }
    }

    SampleGamesList(
        itemSelectedHandler = {
            coroutineScope.launch(Dispatchers.Main.immediate) {
                (context.applicationContext as MyApplication)
                    .gamesFromFileExtractorUseCase.extractGames(
                        fileData = it,
                        context = context
                    )
                val games = (context.applicationContext as MyApplication)
                    .gamesFromFileExtractorUseCase.games
                if (games.isNullOrEmpty()) {
                    showMinutedSnackBarAction(
                        gamesLoadingErrorMessage,
                        SnackbarDuration.Short
                    )
                } else {
                    hostNavController.navigate(NavHostRoutes.gameSelectorPage)
                }
            }
        }
    )
}

@Composable
fun CurrentPath(modifier: Modifier = Modifier, currentPath: String) {
    val context = LocalContext.current
    val localFilesPath = context.filesDir.absolutePath

    Text(
        text = currentPath.replace(localFilesPath, context.getString(R.string.internal_files_root)),
        color = Color.Blue,
        fontSize = 20.sp,
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Yellow)
            .scrollable(state = rememberScrollState(), orientation = Orientation.Horizontal)
    )
}

data class CustomsViewModelState(
    val itemsList: List<FileData> = listOf(),
)

class GamesListPageViewModel : ViewModel() {
    private val viewModelState = MutableStateFlow(CustomsViewModelState())

    val uiState =
        viewModelState
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                viewModelState.value
            )

    private val internalFilesUseCase by lazy {
        InternalFilesUseCase()
    }

    fun update(folder: File, context: Context) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            val newItemsList =
                internalFilesUseCase.getInternalGamesList(folder = folder, context = context)
            viewModelState.update {
                it.copy(itemsList = newItemsList)
            }
        }
    }

    suspend fun createNewFile(name: String, hostFolder: File, context: Context): Boolean {
        return withContext(Dispatchers.Main.immediate) {
            internalFilesUseCase.createNewFile(
                name = name,
                hostFolder = hostFolder,
                context = context
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Customs(
    gamesListPageViewModel: GamesListPageViewModel,
    currentPath: File,
    fileContextualMenuOpen: Boolean,
    fileContextualMenuIndex: Int?,
    openFileContextualMenu: (Int, FileData) -> Unit,
    handleFileContextualMenuDismiss: () -> Unit,
    handleEditFile: (FileData) -> Unit,
) {
    val context = LocalContext.current

    val uiState by gamesListPageViewModel.uiState.collectAsState(Dispatchers.Main.immediate)

    /*
    We only need this in the first re-composition
     */
    LaunchedEffect(true) {
        gamesListPageViewModel.update(folder = currentPath, context = context)
    }

    Column {
        CurrentPath(currentPath = currentPath.absolutePath)
        LazyColumn(
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            itemsIndexed(uiState.itemsList) { index, item ->
                Box {
                    when (item) {
                        is InternalFolderData -> FolderItem(fileData = item)
                        is InternalFileData -> FileItem(fileData = item, modifier = Modifier.combinedClickable(onLongClick = {
                            openFileContextualMenu(index, item)
                        }, onClick = {}))
                        else -> throw IllegalArgumentException("Cannot process element $item !")
                    }
                    DropdownMenu(expanded = fileContextualMenuOpen && fileContextualMenuIndex == index, onDismissRequest = {
                        handleFileContextualMenuDismiss()
                    }) {
                        DropdownMenuItem(onClick = {
                            handleFileContextualMenuDismiss()
                            handleEditFile(item)
                        }) {
                            Text(text = context.getString(R.string.edit_file))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamesListPage(
    hostNavController: NavController,
    gamesListPageViewModel: GamesListPageViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val items = listOf(
        GamesListPageScreen.Samples,
        GamesListPageScreen.Customs,
    )

    var currentPath by rememberSaveable {
        mutableStateOf(context.filesDir)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isCustomFilesTab =
        currentDestination?.route?.equals(NestedNavHostRoutes.customGamesListPage) == true

    var newFileNameDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    fun showNewFileNameDialog() {
        newFileNameDialogOpen = true
    }

    var fileContextualMenuOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var fileContextualMenuItem by rememberSaveable {
        mutableStateOf<FileData?>(null)
    }

    var fileContextualMenuIndex by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    fun handleEditFile(fileData: FileData) {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            (context.applicationContext as MyApplication)
                .gamesFromFileExtractorUseCase.extractGames(
                    fileData = fileData,
                    context = context
                )
            val encodedPath = fileData.path.encodePath()
            hostNavController.navigate(NavHostRoutes.getPgnFileEditorPage(encodedPath))
        }
    }

    @Composable
    fun dialogs(
        handleNewFileNameDefined: (String) -> Unit,
    ) {
        val commonFontSize = 18.sp
        var newFileName by rememberSaveable {
            mutableStateOf("")
        }

        if (newFileNameDialogOpen) {
            AlertDialog(
                buttons = {
                    Row(
                        modifier = Modifier.padding(all = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(
                            onClick = {
                                handleNewFileNameDefined(newFileName)
                                newFileNameDialogOpen = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Green,
                                contentColor = MaterialTheme.colors.primarySurface
                            )
                        ) {
                            Text(context.getString(R.string.ok), fontSize = commonFontSize)
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Button(
                            onClick = { newFileNameDialogOpen = false },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Red,
                                contentColor = MaterialTheme.colors.primarySurface
                            )
                        ) {
                            Text(context.getString(R.string.Cancel), fontSize = commonFontSize)
                        }
                    }
                },
                text = {
                    TextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        placeholder = {
                            Text(context.getString(R.string.enter_file_name), fontSize = 11.sp)
                        }
                    )
                },
                onDismissRequest = {
                    newFileNameDialogOpen = false
                },
            )
        }
    }

    ChessExercisesOrganizerJetpackComposeTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.games_list_page)) })
            },
            floatingActionButton = {
                if (isCustomFilesTab) {
                    FloatingActionButton(onClick = ::showNewFileNameDialog) {
                        Icon(Icons.Filled.Add, context.getString(R.string.add_element))
                    }
                }
            },
            bottomBar = {
                BottomNavigation {
                    items.forEach { screen ->
                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    painter = painterResource(screen.imageId),
                                    contentDescription = context.getString(screen.textId),
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            label = { Text(context.getString(screen.textId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // selecting again the same item
                                    launchSingleTop = true
                                    // Restore state when selecting again a previously selected item
                                    restoreState = true
                                }
                            })
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController,
                startDestination = GamesListPageScreen.Samples.route,
                Modifier.padding(innerPadding)
            ) {
                composable(GamesListPageScreen.Samples.route) {
                    Samples(
                        hostNavController = hostNavController,
                        scaffoldState = scaffoldState
                    )
                }
                composable(GamesListPageScreen.Customs.route) {
                    Customs(
                        gamesListPageViewModel = gamesListPageViewModel,
                        currentPath = currentPath,
                        openFileContextualMenu = { index, fileItem ->
                            fileContextualMenuIndex = index
                            fileContextualMenuItem = fileItem
                            fileContextualMenuOpen = true
                        },
                        fileContextualMenuIndex = fileContextualMenuIndex,
                        fileContextualMenuOpen = fileContextualMenuOpen,
                        handleFileContextualMenuDismiss = {
                            fileContextualMenuIndex = null
                            fileContextualMenuItem = null
                            fileContextualMenuOpen = false
                        },
                        handleEditFile = ::handleEditFile,
                    )
                }
            }
            dialogs(
                handleNewFileNameDefined = {
                    coroutineScope.launch(Dispatchers.Main.immediate) {
                        val success = gamesListPageViewModel.createNewFile(
                            name = it,
                            hostFolder = currentPath,
                            context = context
                        )
                        if (success) {
                            gamesListPageViewModel.update(folder = currentPath, context = context)
                        } else {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = context.getString(R.string.file_creation_error),
                                duration = SnackbarDuration.Short,
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun FileItem(
    fileData: FileData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_text_file),
            contentDescription = fileData.caption,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = fileData.caption,
            color = Color.Black,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun FolderItem(
    fileData: FileData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_folder),
            contentDescription = fileData.caption,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = fileData.caption,
            color = Color.Black,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun SampleGamesList(itemSelectedHandler: (itemData: FileData) -> Unit = { _ -> }) {
    val pgnKPK = stringResource(R.string.KP_K)
    val pgnKQK = stringResource(R.string.KQ_K)
    val pgnK2RK = stringResource(R.string.K2R_K)
    val pgnKRK = stringResource(R.string.KR_K)

    LazyColumn(
        modifier = Modifier
            .background(Color.White)
            .clickable { },
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        arrayOf(
            AssetFileData(caption = pgnKPK, assetRelativePath = "pgn/KP_K.pgn"),
            AssetFileData(caption = pgnKQK, assetRelativePath = "pgn/KQ_K.pgn"),
            AssetFileData(caption = pgnK2RK, assetRelativePath = "pgn/K2R_K.pgn"),
            AssetFileData(caption = pgnKRK, assetRelativePath = "pgn/KR_K.pgn"),
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
    FileItem(
        fileData = AssetFileData(
            caption = "My sample",
            assetRelativePath = "pgn/dummy_sample.pgn"
        )
    )
}

@Composable
@Preview
fun SampleGamesListPreview() {
    SampleGamesList()
}