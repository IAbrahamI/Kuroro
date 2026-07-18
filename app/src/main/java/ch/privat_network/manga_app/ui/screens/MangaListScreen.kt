package ch.privat_network.manga_app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.privat_network.manga_app.domain.Manga
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaListScreen(
    viewModel: MangaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isManualFetching by viewModel.isManualFetching.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val mangaNameInput by viewModel.mangaNameInput.collectAsState()
    val isAddingManga by viewModel.isAddingManga.collectAsState()
    val context = LocalContext.current

    // Using Scaffold is the best practice for handling system bars and content padding
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Manual Fetch/Sync Button
                SmallFloatingActionButton(
                    onClick = { viewModel.manualFetch() },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                ) {
                    if (isManualFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Server")
                    }
                }

                // Add Manga Button
                FloatingActionButton(
                    onClick = { viewModel.onOpenAddDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Manga")
                }
            }
        }
    ) { innerPadding ->
        if (showAddDialog) {
            AddMangaDialog(
                mangaName = mangaNameInput,
                isAdding = isAddingManga,
                onNameChange = { viewModel.onMangaNameChange(it) },
                onConfirm = { viewModel.addManga() },
                onDismiss = { viewModel.onCloseAddDialog() }
            )
        }

        when (uiState) {
            is MangaUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is MangaUiState.Success -> {
                val list = (uiState as MangaUiState.Success).mangaList
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.fetchManga(isRefresh = true) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()) // Handle nav bar
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        // Use the padding provided by Scaffold for top, but handle our own internal padding
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = innerPadding.calculateTopPadding() + 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = list,
                            key = { it.id } // Optimization: Helps Compose uniquely identify items
                        ) { manga ->
                            MangaCard(
                                manga = manga,
                                onReadClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(manga.url))
                                    context.startActivity(intent)
                                },
                                onDeleteClick = {
                                    viewModel.deleteManga(manga)
                                }
                            )
                        }
                    }
                }
            }
            is MangaUiState.Error -> {
                val message = (uiState as MangaUiState.Error).message
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = message, color = Color.Red, modifier = Modifier.padding(16.dp))
                    Button(
                        onClick = { viewModel.fetchManga() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMangaDialog(
    mangaName: String,
    isAdding: Boolean,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Kuroro") },
        text = {
            Column {
                Text("Add Manga:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = mangaName,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Manga name...") },
                    singleLine = true,
                    enabled = !isAdding
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = mangaName.isNotBlank() && !isAdding,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isAdding) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun MangaCard(
    manga: Manga,
    onReadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Format the date string from "2026-02-07T00:00:00" to "Feb 7, 2026"
    val formattedDate = remember(manga.date) {
        try {
            val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val outputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            val date = LocalDateTime.parse(manga.date, inputFormatter)
            date.format(outputFormatter)
        } catch (e: Exception) {
            manga.date // Fallback to raw string if parsing fails
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp), // Flexible height but consistent minimum
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(manga.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = manga.title,
                modifier = Modifier
                    .width(115.dp) // Slightly wider for better aspect ratio
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = manga.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 32.dp) // Room for trash icon
                        )
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "By ${manga.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Updated: $formattedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Status: ${manga.status}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (manga.status == "Ongoing") Color(0xFF4CAF50) else Color.Gray
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = onReadClick,
                        modifier = Modifier
                            .width(150.dp)
                            .height(38.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Read Now",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    
                    Text(
                        text = manga.latestChapter,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}