package com.andrespar2.parfmi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.andrespar2.parfmi.ui.StatusView
import com.andrespar2.parfmi.ui.theme.ParfmiColors

/**
 * Etiqueta de estado con punto de color y borde.
 *
 * El color procede del [StatusView] correspondiente al estado interpretado, de
 * modo que un estado desconocido siempre se ve en ambar y nunca en verde.
 */
@Composable
fun StatusBadge(
    view: StatusView,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(view.color.copy(alpha = 0.14f))
            .border(1.dp, view.color.copy(alpha = 0.55f), shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(view.color),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(view.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = view.color,
        )
    }
}

/** Fila etiqueta + [StatusBadge] para el informe. */
@Composable
fun StatusRow(
    label: String,
    view: StatusView,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ParfmiColors.TextSecondary,
        )
        Spacer(Modifier.width(12.dp))
        StatusBadge(view)
    }
}
