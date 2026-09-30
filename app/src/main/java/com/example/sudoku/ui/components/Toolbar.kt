package com.example.sudoku.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Toolbar(
    canUndo: Boolean,
    canRedo: Boolean,
    isNotesMode: Boolean,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onNotesToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo
        ToolbarButton(
            onClick = onUndoClick,
            enabled = canUndo,
            modifier = Modifier
                .weight(1f)
                .testTag("toolbar_undo")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo"
            )
            Text(
                text = "Undo",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Redo
        ToolbarButton(
            onClick = onRedoClick,
            enabled = canRedo,
            modifier = Modifier
                .weight(1f)
                .testTag("toolbar_redo")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo"
            )
            Text(
                text = "Redo",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Notes Mode Toggle
        val notesBg = if (isNotesMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        val notesFg = if (isNotesMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

        Surface(
            onClick = onNotesToggle,
            shape = RoundedCornerShape(0.dp),
            color = notesBg,
            contentColor = notesFg,
            modifier = Modifier
                .weight(1.2f)
                .height(44.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
                .testTag("toolbar_notes_toggle")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = if (isNotesMode) Icons.Filled.Edit else Icons.Outlined.Edit,
                    contentDescription = "Toggle Notes Mode"
                )
                Text(
                    text = if (isNotesMode) "Notes: ON" else "Notes: OFF",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
        modifier = modifier
            .height(44.dp)
            .border(
                1.5.dp,
                if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(0.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            content()
        }
    }
}
