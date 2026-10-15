package com.andrespar2.parfmi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.andrespar2.parfmi.R
import com.andrespar2.parfmi.ui.theme.ParfmiColors

/**
 * Fila etiqueta/valor del informe.
 *
 * Si [value] es nulo o esta en blanco se muestra "No disponible" en color
 * atenuado: la app nunca rellena un dato ausente con un valor inventado.
 */
@Composable
fun InfoRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    val present = !value.isNullOrBlank()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ParfmiColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = if (present) value!! else stringResource(R.string.result_not_available),
            style = MaterialTheme.typography.bodyMedium,
            color = if (present) ParfmiColors.TextPrimary else ParfmiColors.TextTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.3f),
        )
    }
}
