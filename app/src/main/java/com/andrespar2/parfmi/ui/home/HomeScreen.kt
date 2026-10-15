package com.andrespar2.parfmi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrespar2.parfmi.R
import com.andrespar2.parfmi.core.IdentifierType
import com.andrespar2.parfmi.ui.AppViewModelProvider
import com.andrespar2.parfmi.ui.lookup.LookupViewModel
import com.andrespar2.parfmi.ui.messageRes
import com.andrespar2.parfmi.ui.theme.ParfmiColors

/**
 * Pantalla principal: entrada del IMEI o numero de serie y lanzamiento de la
 * consulta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToResult: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: LookupViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val input by viewModel.input.collectAsStateWithLifecycle()
    val inputError by viewModel.inputError.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    fun submit() {
        focusManager.clearFocus()
        viewModel.submit(onNavigateToResult)
    }

    Scaffold(
        containerColor = ParfmiColors.AmoledBlack,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ParfmiColors.AmoledBlack,
                    titleContentColor = ParfmiColors.TextPrimary,
                    actionIconContentColor = ParfmiColors.TextSecondary,
                ),
                actions = {
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.cd_open_about),
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.cd_open_settings),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = ParfmiColors.TextSecondary,
            )

            OutlinedTextField(
                value = input,
                onValueChange = viewModel::onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.home_input_label)) },
                placeholder = { Text(stringResource(R.string.home_input_placeholder)) },
                singleLine = true,
                isError = inputError != null,
                supportingText = {
                    Text(
                        text = inputError?.let { stringResource(it.messageRes()) }
                            ?: stringResource(R.string.home_input_helper),
                    )
                },
                trailingIcon = {
                    if (input.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearInput) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.cd_clear_input),
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )

            DetectedTypeChip(
                type = viewModel.detectedType(),
                inputPresent = input.isNotBlank(),
            )

            Text(
                text = stringResource(R.string.home_service_selected, settings.selectedServiceName),
                style = MaterialTheme.typography.labelMedium,
                color = ParfmiColors.TextTertiary,
            )

            Button(
                onClick = { submit() },
                enabled = input.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParfmiColors.Red,
                    contentColor = Color.White,
                    disabledContainerColor = ParfmiColors.SurfaceElevated,
                    disabledContentColor = ParfmiColors.TextTertiary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.home_action_query),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            if (!settings.apiKeyConfigured) {
                NoProviderCard(onConfigure = onNavigateToSettings)
            }

            NoticeCard(
                title = stringResource(R.string.home_privacy_title),
                body = stringResource(R.string.home_privacy_text),
            )

            NoticeCard(
                title = null,
                body = stringResource(R.string.home_disclaimer),
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DetectedTypeChip(
    type: IdentifierType?,
    inputPresent: Boolean,
) {
    if (!inputPresent) return

    val labelRes = when (type) {
        IdentifierType.IMEI -> R.string.home_detected_imei
        IdentifierType.SERIAL_NUMBER -> R.string.home_detected_sn
        IdentifierType.UNKNOWN, null -> R.string.home_detected_unknown
    }
    val color = if (type == null || type == IdentifierType.UNKNOWN) {
        ParfmiColors.StatusUnknown
    } else {
        ParfmiColors.Info
    }

    Text(
        text = stringResource(labelRes),
        style = MaterialTheme.typography.labelMedium,
        color = color,
    )
}

@Composable
private fun NoProviderCard(onConfigure: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ParfmiColors.Surface)
            .border(1.dp, ParfmiColors.StatusUnknown.copy(alpha = 0.5f), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.home_no_provider_title),
            style = MaterialTheme.typography.titleMedium,
            color = ParfmiColors.StatusUnknown,
        )
        Text(
            text = stringResource(R.string.home_no_provider_text),
            style = MaterialTheme.typography.bodyMedium,
            color = ParfmiColors.TextSecondary,
        )
        OutlinedButton(
            onClick = onConfigure,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ParfmiColors.Red),
        ) {
            Text(stringResource(R.string.home_action_configure))
        }
    }
}

@Composable
private fun NoticeCard(
    title: String?,
    body: String,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ParfmiColors.SurfaceAlt)
            .border(1.dp, ParfmiColors.Border, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (title != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ParfmiColors.TextPrimary,
                )
            }
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = ParfmiColors.TextSecondary,
        )
    }
}
