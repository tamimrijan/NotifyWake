package com.notifywake.app.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.notifywake.app.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class AppEntry(val packageName: String, val label: String, val launchable: Boolean)

@Composable
fun AppPickerScreen(store: SettingsStore, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val settings by rememberWakeSettings(store)
    val apps by produceState<List<AppEntry>?>(null) { value = loadApps(ctx) }

    var query by rememberSaveable { mutableStateOf("") }

    val visible = remember(apps, query, settings.selectedApps) {
        apps.orEmpty()
            .filter { it.launchable || it.packageName in settings.selectedApps }
            .filter {
                query.isBlank() ||
                    it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Choose apps", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${settings.selectedApps.size} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search apps") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = { store.setSelectedApps(settings.selectedApps + visible.map { it.packageName }) },
                    shapes = ButtonDefaults.shapes(),
                ) { Text("Select all") }
                OutlinedButton(
                    onClick = {
                        store.setSelectedApps(settings.selectedApps - visible.map { it.packageName }.toSet())
                    },
                    shapes = ButtonDefaults.shapes(),
                ) { Text("Clear") }
            }

            if (apps == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator(Modifier.size(72.dp))
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                ) {
                    itemsIndexed(visible, key = { _, app -> app.packageName }) { i, app ->
                        val checked = app.packageName in settings.selectedApps
                        SegmentedListItem(
                            checked = checked,
                            onCheckedChange = { now ->
                                store.setSelectedApps(
                                    if (now) settings.selectedApps + app.packageName
                                    else settings.selectedApps - app.packageName
                                )
                            },
                            shapes = ListItemDefaults.segmentedShapes(i, visible.size),
                            leadingContent = { AppIcon(app.packageName) },
                            supportingContent = {
                                Text(app.packageName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            trailingContent = { Checkbox(checked = checked, onCheckedChange = null) },
                        ) {
                            Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (visible.isEmpty()) {
                        item {
                            Text(
                                "No apps match.",
                                modifier = Modifier.padding(24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val iconCache = LruCache<String, ImageBitmap>(250)

@Composable
private fun AppIcon(packageName: String) {
    val ctx = LocalContext.current
    val icon by produceState(iconCache.get(packageName), packageName) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                try {
                    ctx.packageManager.getApplicationIcon(packageName)
                        .toBitmap(96, 96)
                        .asImageBitmap()
                        .also { iconCache.put(packageName, it) }
                } catch (_: Exception) {
                    null
                }
            }
        }
    }
    val modifier = Modifier
        .size(40.dp)
        .clip(MaterialShapes.Cookie9Sided.toShape())
    val bitmap = icon
    if (bitmap != null) {
        Image(bitmap, contentDescription = null, modifier = modifier)
    } else {
        Box(modifier)
    }
}

private suspend fun loadApps(ctx: Context): List<AppEntry> = withContext(Dispatchers.IO) {
    val pm = ctx.packageManager
    val installed = if (Build.VERSION.SDK_INT >= 33) {
        pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        pm.getInstalledApplications(0)
    }
    installed
        .asSequence()
        .filter { it.packageName != ctx.packageName }
        .map {
            AppEntry(
                packageName = it.packageName,
                label = pm.getApplicationLabel(it).toString(),
                launchable = pm.getLaunchIntentForPackage(it.packageName) != null,
            )
        }
        .sortedBy { it.label.lowercase() }
        .toList()
}
