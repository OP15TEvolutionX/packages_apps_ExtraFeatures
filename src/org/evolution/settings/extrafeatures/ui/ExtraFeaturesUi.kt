/* Copyright (C) 2026 Evolution X
 * SPDX-License-Identifier: Apache-2.0
 */
package org.evolution.settings.extrafeatures.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.android.settings.R
import com.android.settingslib.spa.framework.theme.SettingsTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The window background shows through, so pages match the rest of Settings. */
@Composable
internal fun ExtraFeaturesTheme(content: @Composable () -> Unit) {
    SettingsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            content = content,
        )
    }
}

@Composable
internal fun ExtraFeaturesCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
    color: Color = MaterialTheme.colorScheme.surfaceBright,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().clip(shape).background(color)) {
        CompositionLocalProvider(LocalContentColor provides contentColorFor(color)) {
            content()
        }
    }
}

/** Rounds the outer corners of a group, like preference lists in Settings. */
internal fun listItemShape(index: Int, count: Int): Shape {
    val outer = 24.dp
    val inner = 4.dp
    return RoundedCornerShape(
        topStart = if (index == 0) outer else inner,
        topEnd = if (index == 0) outer else inner,
        bottomStart = if (index == count - 1) outer else inner,
        bottomEnd = if (index == count - 1) outer else inner,
    )
}

@Composable
internal fun AppExplanation(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 12.dp),
    )
}

/** Pill search field with a placeholder instead of a floating label. */
@Composable
internal fun ExtraFeaturesSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val border = MaterialTheme.colorScheme.onSurfaceVariant
    val container = MaterialTheme.colorScheme.surfaceContainerLowest
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        shape = FilterChipDefaults.shape,
        placeholder = { Text(stringResource(R.string.extra_features_search)) },
        // Equal slots on both sides keep the icons evenly inset from the field edges.
        leadingIcon = {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Search, contentDescription = null)
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Close, stringResource(R.string.extra_features_clear_search))
                    }
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = border,
            unfocusedBorderColor = border,
            focusedContainerColor = container,
            unfocusedContainerColor = container,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Same thumb as the Settings switch used by preference screens. */
@Composable
internal fun ExtraFeaturesSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(checkedIconColor = MaterialTheme.colorScheme.primary),
        thumbContent = {
            Icon(
                if (checked) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        },
    )
}

/**
 * Title of a preference row as native Settings draws it (TextAppearance.SettingsLib.TitleMedium):
 * 16sp on a 24sp line in the medium headline font, whatever the SPA typography variant says.
 */
@Composable
internal fun preferenceTitleStyle(): TextStyle =
    MaterialTheme.typography.titleMedium.copy(
        fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium)

/** Native preference rows are at least 72dp tall. */
private val PreferenceMinHeight = 72.dp

/** Title, optional summary and a switch; the whole row toggles. */
@Composable
internal fun ExtraFeaturesSwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = PreferenceMinHeight)
            .toggleable(checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = preferenceTitleStyle())
            summary?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        ExtraFeaturesSwitch(checked, onCheckedChange = null)
    }
}

/** Title and optional summary; same padding as [ExtraFeaturesSwitchPreference]. */
@Composable
internal fun ExtraFeaturesPreference(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
) {
    Column(
        modifier.fillMaxWidth().heightIn(min = PreferenceMinHeight).clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = preferenceTitleStyle())
        summary?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Highlighted on/off bar at the top of a screen. */
@Composable
internal fun ExtraFeaturesMainSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ExtraFeaturesCard(
        shape = RoundedCornerShape(28.dp),
        color = if (checked) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceBright,
    ) {
        ExtraFeaturesSwitchPreference(title, checked, onCheckedChange)
    }
}

/** Search and the selected, third-party and system filters; a null [systemFilter] shows all. */
@Composable
internal fun AppFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCount: Int,
    onlySelected: Boolean,
    onOnlySelectedChange: (Boolean) -> Unit,
    systemFilter: Boolean?,
    onSystemFilterChange: (Boolean?) -> Unit,
) {
    Column {
        ExtraFeaturesSearchField(query, onQueryChange)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = onlySelected,
                onClick = { onOnlySelectedChange(!onlySelected) },
                label = { Text(stringResource(R.string.extra_features_only_selected, selectedCount)) },
            )
            FilterChip(
                selected = systemFilter == false,
                onClick = { onSystemFilterChange(if (systemFilter == false) null else false) },
                label = { Text(stringResource(R.string.extra_features_filter_user)) },
            )
            FilterChip(
                selected = systemFilter == true,
                onClick = { onSystemFilterChange(if (systemFilter == true) null else true) },
                label = { Text(stringResource(R.string.extra_features_filter_system)) },
            )
        }
    }
}

/** Loads an icon once; cached so list reloads don't drop every icon. */
@Composable
internal fun rememberAppIcon(
    key: String,
    icons: MutableMap<String, ImageBitmap?>,
    load: () -> ImageBitmap?,
): ImageBitmap? {
    val icon by produceState(icons[key], key) {
        if (icons.containsKey(key)) return@produceState
        val loaded = withContext(Dispatchers.IO) {
            try {
                load()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                null
            }
        }
        icons[key] = loaded
        value = loaded
    }
    return icon
}

/** Icon, label and package line shared by every app row. */
@Composable
internal fun AppRowContent(
    icon: ImageBitmap?,
    label: String,
    summary: String,
    modifier: Modifier = Modifier,
    status: String? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(40.dp)) {
            icon?.let { Image(it, contentDescription = null, modifier = Modifier.fillMaxSize()) }
        }
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(summary, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            status?.let {
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing()
    }
}
