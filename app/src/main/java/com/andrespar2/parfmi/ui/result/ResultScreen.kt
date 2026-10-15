package com.andrespar2.parfmi.ui.result

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andrespar2.parfmi.R
import com.andrespar2.parfmi.core.IdentifierType
import com.andrespar2.parfmi.core.TimestampFormatter
import com.andrespar2.parfmi.data.model.FmiReport
import com.andrespar2.parfmi.data.model.LookupOutcome
import com.andrespar2.parfmi.ui.components.InfoRow
import com.andrespar2.parfmi.ui.components.SectionCard
import com.andrespar2.parfmi.ui.components.StatusRow
import com.andrespar2.parfmi.ui.lookup.LookupStatus
import com.andrespar2.parfmi.ui.lookup.LookupViewModel
import com.andrespar2.parfmi.ui.messageRes
import com.andrespar2.parfmi.ui.theme.ParfmiColors
import com.andrespar2.parfmi.ui.toView

/**
 * Pantalla de resultados.
 *
 * Muestra el estado de la consulta (cargando, exito o error) reutilizando el
 * mismo [LookupViewModel] compartido con la pantalla principal. Si el
 * proveedor no aporta un dato, se muestra como "No disponible" o
 * "Desconocido"; nunca se pinta como desbloqueado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: LookupViewModel,
    onBack: () -> Unit,
) {
    val status by viewModel.status.collectAsStateWithLifecycle()

    fun goBack() {
        viewModel.resetResult()
        onBack()
    }

    BackHandler { goBack() }

    Scaffold(
        containerColor = ParfmiColors.AmoledBlack,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.result_title)) },
                navigationIcon = {
                    IconButton(onClick = { goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ParfmiColors.AmoledBlack,
                    titleContentColor = ParfmiColors.TextPrimary,
                    navigationIconContentColor = ParfmiColors.TextSecondary,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (val current = status) {
                is LookupStatus.Idle -> Unit
                is LookupStatus.Loading -> LoadingContent(current.serviceName)
                is LookupStatus.Done -> when (val outcome = current.outcome) {
                    is LookupOutcome.Success -> ReportContent(outcome.report)
                    is LookupOutcome.Failure -> FailureContent(
                        messageRes = outcome.error.messageRes(),
                        detail = outcome.detail,
                        onRetry = { goBack() },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(serviceName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = ParfmiColors.Red)
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.loading_title),
            style = MaterialTheme.typography.titleMedium,
            color = ParfmiColors.TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.loading_detail, serviceName),
            style = MaterialTheme.typography.bodyMedium,
            color = ParfmiColors.TextSecondary,
        )
    }
}

@Composable
private fun FailureContent(
    messageRes: Int,
    detail: String?,
    onRetry: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(ParfmiColors.Surface)
                .border(1.dp, ParfmiColors.StatusNegative.copy(alpha = 0.5f), shape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.error_title),
                style = MaterialTheme.typography.titleLarge,
                color = ParfmiColors.StatusNegative,
            )
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyLarge,
                color = ParfmiColors.TextPrimary,
            )
            if (!detail.isNullOrBlank()) {
                HorizontalDivider(color = ParfmiColors.Border)
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParfmiColors.TextTertiary,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ParfmiColors.Red, contentColor = Color.White),
        ) {
            Text(stringResource(R.string.error_retry))
        }
    }
}

@Composable
private fun ReportContent(report: FmiReport) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val summary = remember(report) { ReportSummaryBuilder.build(report) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NoticeBanner(text = stringResource(R.string.result_status_unknown_notice))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    clipboard.setText(AnnotatedString(summary))
                    Toast.makeText(context, R.string.result_copied, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParfmiColors.Surface,
                    contentColor = ParfmiColors.TextPrimary,
                ),
            ) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.result_action_copy),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            OutlinedButton(
                onClick = { shareSummary(context, summary) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ParfmiColors.TextPrimary),
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.result_action_share),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (report.device.hasAnyValue) {
            SectionCard(title = stringResource(R.string.result_section_device)) {
                InfoRow(stringResource(R.string.field_imei), report.device.imei)
                InfoRow(stringResource(R.string.field_serial), report.device.serialNumber)
                InfoRow(stringResource(R.string.field_manufacturer), report.device.manufacturer)
                InfoRow(stringResource(R.string.field_model), report.device.model)
                InfoRow(stringResource(R.string.field_model_name), report.device.modelName)
                InfoRow(stringResource(R.string.field_model_code), report.device.modelCode)
                InfoRow(stringResource(R.string.field_product_family), report.device.productFamily)
                InfoRow(stringResource(R.string.field_color), report.device.color)
                InfoRow(stringResource(R.string.field_capacity), report.device.capacity)
                InfoRow(stringResource(R.string.field_mpn), report.device.partNumber)
            }
        }

        SectionCard(title = stringResource(R.string.result_section_fmi)) {
            StatusRow(stringResource(R.string.field_fmi), report.security.fmi.toView())
            StatusRow(stringResource(R.string.field_activation_lock), report.security.activationLock.toView())
        }

        if (report.security.hasAnyKnownValue) {
            SectionCard(title = stringResource(R.string.result_section_activation)) {
                StatusRow(stringResource(R.string.field_activation_state), report.security.activationState.toView())
                StatusRow(stringResource(R.string.field_icloud), report.security.icloudState.toView())
                StatusRow(stringResource(R.string.field_blacklist), report.security.blacklist.toView())
                StatusRow(stringResource(R.string.field_mdm), report.security.mdm.toView())
                StatusRow(stringResource(R.string.field_simlock), report.security.simLock.toView())
            }
        }

        SectionCard(title = stringResource(R.string.result_section_coverage)) {
            StatusRow(stringResource(R.string.field_coverage), report.coverage.status.toView())
            InfoRow(
                label = stringResource(R.string.field_activation_date),
                value = report.coverage.purchaseDate ?: report.coverage.activationDate,
            )
        }

        if (report.network.hasAnyValue) {
            SectionCard(title = stringResource(R.string.result_section_network)) {
                InfoRow(stringResource(R.string.field_carrier), report.network.carrier)
                InfoRow(
                    label = stringResource(R.string.field_country),
                    value = listOfNotNull(report.network.country, report.network.region)
                        .filter { it.isNotBlank() }
                        .distinct()
                        .joinToString(", ")
                        .takeIf { it.isNotBlank() },
                )
            }
        }

        SectionCard(title = stringResource(R.string.result_section_query)) {
            InfoRow(stringResource(identifierLabelRes(report.identifier.type)), report.identifier.raw)
            InfoRow(
                label = stringResource(R.string.field_query_date),
                value = TimestampFormatter.format(report.queriedAtMillis),
            )
            InfoRow(stringResource(R.string.field_provider), report.providerName)
            InfoRow(stringResource(R.string.field_service), report.serviceName ?: report.serviceId)
            InfoRow(stringResource(R.string.field_balance), report.balance)
        }

        if (report.extraFields.isNotEmpty()) {
            SectionCard(title = stringResource(R.string.result_section_extra)) {
                report.extraFields.forEach { field ->
                    InfoRow(label = field.label, value = field.value)
                }
            }
        }

        val raw = report.rawResponse
        if (!raw.isNullOrBlank()) {
            RawResponseCard(raw)
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RawResponseCard(raw: String) {
    var expanded by remember { mutableStateOf(false) }
    SectionCard(title = stringResource(R.string.result_raw_title)) {
        TextButton(
            onClick = { expanded = !expanded },
            colors = ButtonDefaults.textButtonColors(contentColor = ParfmiColors.Info),
        ) {
            Text(
                text = stringResource(
                    if (expanded) R.string.result_raw_hide else R.string.result_raw_show,
                ),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = raw,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = ParfmiColors.TextSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun NoticeBanner(text: String) {
    val shape = RoundedCornerShape(12.dp)
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = ParfmiColors.StatusUnknown,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ParfmiColors.StatusUnknown.copy(alpha = 0.10f))
            .border(1.dp, ParfmiColors.StatusUnknown.copy(alpha = 0.35f), shape)
            .padding(12.dp),
    )
}

private fun identifierLabelRes(type: IdentifierType): Int = when (type) {
    IdentifierType.IMEI -> R.string.field_imei
    IdentifierType.SERIAL_NUMBER -> R.string.field_serial
    IdentifierType.UNKNOWN -> R.string.field_identifier_type
}

private fun shareSummary(context: Context, summary: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.result_share_subject))
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.result_action_share)))
}
