@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.notifywake.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * One tab page: collapsing M3 large top app bar over a scrolling column.
 * The bottom navigation bar's inset is applied by the caller.
 */
@Composable
fun TabScaffold(
    title: String,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                actions = actions,
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content,
        )
    }
}

/** Settings-style group: a primary-coloured header above a tonal card of list items. */
@Composable
fun SettingsGroup(
    header: String?,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        if (header != null) {
            Text(
                header,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            )
        }
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(vertical = 6.dp), content = content)
        }
        if (footer != null) {
            Text(
                footer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
            )
        }
    }
}

@Composable
fun transparentListItemColors() = ListItemDefaults.colors(containerColor = Color.Transparent)

@Composable
fun SwitchListItem(
    title: String,
    description: String?,
    icon: ImageVector?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = description?.let { { Text(it) } },
        leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        colors = transparentListItemColors(),
        modifier = Modifier.toggleable(
            value = checked,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
    )
}

@Composable
fun <T> ChipChoice(
    label: String,
    description: String?,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        if (description != null) {
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.size(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, text) ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    label = { Text(text) },
                    leadingIcon = if (value == selected) {
                        { Icon(Icons.Filled.CheckCircle, null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null,
                )
            }
        }
    }
}

enum class CheckState { OK, MISSING_REQUIRED, MISSING_OPTIONAL }

@Composable
fun PermissionListItem(
    state: CheckState,
    title: String,
    description: String,
    actionLabel: String?,
    onAction: () -> Unit,
) {
    val (icon, tint) = when (state) {
        CheckState.OK -> Icons.Filled.CheckCircle to StatusColors.ok
        CheckState.MISSING_REQUIRED -> Icons.Filled.Warning to MaterialTheme.colorScheme.error
        CheckState.MISSING_OPTIONAL -> Icons.Filled.Info to StatusColors.warn
    }
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Column {
                Text(description)
                if (state != CheckState.OK && actionLabel != null) {
                    Spacer(Modifier.size(10.dp))
                    FilledTonalButton(onClick = onAction) { Text(actionLabel) }
                }
            }
        },
        leadingContent = { Icon(icon, contentDescription = null, tint = tint) },
        colors = transparentListItemColors(),
    )
}

@Composable
fun NumberedStep(number: Int, text: String) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box(
            Modifier
                .size(26.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$number",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 3.dp))
    }
}
