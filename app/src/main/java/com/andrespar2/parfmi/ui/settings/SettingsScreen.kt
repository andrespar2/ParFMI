package com.andrespar2.parfmi.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andrespar2.parfmi.R
import com.andrespar2.parfmi.data.model.ProviderService
import com.andrespar2.parfmi.data.settings.CatalogSource
import com.andrespar2.parfmi.ui.AppViewModelProvider
import com.andrespar2.parfmi.ui.components.SectionCard
import com.andrespar2.parfmi.ui.theme.ParfmiColors

/**
 * Pantalla de ajustes: API key del proveedor, servicio a consultar y gestion
 * del catalogo. La API key nunca se muestra una vez guardada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val apiKeyInput by viewModel.apiKeyInput.collectAsStateWithLifecycle()
    val customServiceId by viewModel.customServiceId.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showKey by remember { mutableStateOf(false) }

    val messageText = message?.let { settingsMessageText(it) }
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        containerColor = ParfmiColors.AmoledBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard(title = stringResource(R.string.settings_provider_title)) {
                Text(
                    text = stringResource(R.string.settings_provider_sickw),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParfmiColors.TextPrimary,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        if (settings.apiKeyConfigured) {
                            R.string.settings_key_present
                        } else {
                            R.string.settings_key_absent
                        },
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (settings.apiKeyConfigured) {
                        ParfmiColors.StatusPositive
                    } else {
                        ParfmiColors.StatusUnknown
                    },
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = viewModel::onApiKeyChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.settings_api_key)) },
                    placeholder = { Text(stringResource(R.string.settings_api_key_placeholder)) },
                    singleLine = true,
                    visualTransformation = if (showKey) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                imageVector = if (showKey) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = stringResource(
                                    if (showKey) R.string.settings_hide_key else R.string.settings_show_key,
                                ),
                            )
                        }
                    },
                    supportingText = { Text(stringResource(R.string.settings_api_key_helper)) },
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = viewModel::saveApiKey,
                        enabled = apiKeyInput.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParfmiColors.Red,
                            contentColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    ) {
                        Text(stringResource(R.string.settings_action_save))
                    }
                    if (settings.apiKeyConfigured) {
                        OutlinedButton(
                            onClick = viewModel::clearApiKey,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ParfmiColors.StatusNegative),
                        ) {
                            Text(stringResource(R.string.settings_action_clear_key))
                        }
                    }
                }
            }

            SectionCard(title = stringResource(R.string.settings_service_title)) {
                Text(
                    text = stringResource(R.string.settings_service_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParfmiColors.TextSecondary,
                )
                Spacer(Modifier.height(8.dp))
                settings.services.forEach { service ->
                    ServiceOption(
                        service = service,
                        selected = service.id == settings.serviceId,
                        onSelect = { viewModel.selectService(service.id) },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        if (settings.catalogSource == CatalogSource.LIVE) {
                            R.string.settings_catalog_live
                        } else {
                            R.string.settings_catalog_included
                        },
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = ParfmiColors.TextTertiary,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customServiceId,
                    onValueChange = viewModel::onCustomServiceChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.settings_custom_service)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text(stringResource(R.string.settings_custom_service_helper)) },
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = viewModel::saveCustomService,
                    enabled = customServiceId.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ParfmiColors.Red),
                ) {
                    Text(stringResource(R.string.settings_action_save))
                }
            }

            SectionCard(title = stringResource(R.string.settings_action_refresh_catalog)) {
                Button(
                    onClick = viewModel::refreshCatalog,
                    enabled = !refreshing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParfmiColors.SurfaceElevated,
                        contentColor = ParfmiColors.TextPrimary,
                    ),
                ) {
                    Text(stringResource(R.string.settings_action_refresh_catalog))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = viewModel::resetCatalog,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ParfmiColors.TextSecondary),
                ) {
                    Text(stringResource(R.string.settings_catalog_reset))
                }
            }

            SectionCard(title = stringResource(R.string.settings_security_title)) {
                Text(
                    text = stringResource(R.string.settings_security_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParfmiColors.TextSecondary,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ServiceOption(
    service: ProviderService,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .selectable(selected = selected, onClick = onSelect)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = ParfmiColors.Red,
                unselectedColor = ParfmiColors.BorderStrong,
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = service.name,
                style = MaterialTheme.typography.bodyMedium,
                color = ParfmiColors.TextPrimary,
            )
            if (!service.price.isNullOrBlank()) {
                Text(
                    text = service.price,
                    style = MaterialTheme.typography.labelMedium,
                    color = ParfmiColors.TextTertiary,
                )
            }
        }
    }
}

@Composable
private fun settingsMessageText(message: SettingsMessage): String = when (message) {
    SettingsMessage.Saved -> stringResource(R.string.settings_saved)
    SettingsMessage.KeyCleared -> stringResource(R.string.settings_key_cleared)
    is SettingsMessage.CatalogUpdated -> stringResource(R.string.settings_catalog_ok, message.count)
    SettingsMessage.CatalogFailed -> stringResource(R.string.settings_catalog_error)
    SettingsMessage.CatalogReset -> stringResource(R.string.settings_catalog_included)
}
