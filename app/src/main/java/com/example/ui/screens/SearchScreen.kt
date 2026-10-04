package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.pressScale
import com.example.utils.simpleVerticalScrollbar
import com.example.data.GameEntity
import com.example.ui.viewmodels.MainViewModel
import com.example.utils.EmulatorIntentFactory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: MainViewModel, onBack: (() -> Unit)? = null) {
    val gamesState by viewModel.games.collectAsState()
    val games = gamesState ?: emptyList()
    val gridColumns by viewModel.gridColumns.collectAsState()
    val globalGameCardScale by viewModel.globalGameCardScale.collectAsState()
    val multiLineLabel by viewModel.multiLineLabel.collectAsState()
    val gameTitleFontWeight by viewModel.gameTitleFontWeight.collectAsState()
    val gameTitleFontSize by viewModel.gameTitleFontSize.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var gameToEdit by remember { mutableStateOf<GameEntity?>(null) }

    val filteredGames = remember(games, searchQuery) {
        games.filter {
            searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.platform.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.pressScale()) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
                        }
                    }
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text("Tìm kiếm", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (filteredGames.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (gamesState != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search, 
                                contentDescription = null, 
                                modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Không tìm thấy game nào.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }
            } else {
                val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                val configuration = LocalConfiguration.current
                val isTablet = configuration.screenWidthDp >= 600 && configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                val actualColumns = if (isTablet) gridColumns * 2 else gridColumns
                
                val bottomPadding = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(actualColumns),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = bottomPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth().simpleVerticalScrollbar(gridState)
                ) {
                    items(filteredGames, key = { it.id }) { game ->
                        com.example.ui.screens.GameCard(
                            game = game, 
                            globalScale = globalGameCardScale,
                            multiLineLabel = multiLineLabel,
                            gameTitleFontWeight = gameTitleFontWeight,
                            gameTitleFontSize = gameTitleFontSize,
                            onClick = { EmulatorIntentFactory.launchGame(context, game.platform, game.filePath) },
                            onOptionsClick = { gameToEdit = game } // Fallback to just edit action, or optionsDialog in search if you want
                        )
                    }
                }
            }
        }
    }

    if (gameToEdit != null) {
        EditGameDialog(
            game = gameToEdit!!,
            viewModel = viewModel,
            onDismiss = { gameToEdit = null },
            onSave = { updatedGame ->
                viewModel.updateGame(updatedGame)
                gameToEdit = null
            }
        )
    }
}
