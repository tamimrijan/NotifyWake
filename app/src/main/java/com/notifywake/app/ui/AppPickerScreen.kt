@file:OptIn(ExperimentalMaterial3Api::class)

package com.notifywake.app.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
                        Text("Choose apps")
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
                actions = {
                    TextButton(onClick = {
                        store.setSelectedApps(settings.selectedApps + visible.map { it.packageName })
                    }) { Text("Select all") }
                    TextButton(onClick = {
                        store.setSelectedApps(settings.selectedApps - visible.map { it.packageName }.toSet())
                    }) { Text("Clear") }
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
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (apps == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(visible, key = { it.packageName }) { app ->
                        val checked = app.packageName in settings.selectedApps
                        AppRow(app, checked) { nowChecked ->
                            store.setSelectedApps(
                                if (nowChecked) settings.selectedApps + app.packageName
                                else settings.selectedApps - app.packageName
                            )
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

@Composable
private fun AppRow(app: AppEntry, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        AppIcon(app.packageName)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(app.label, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
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
        .clip(RoundedCornerShape(10.dp))
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
