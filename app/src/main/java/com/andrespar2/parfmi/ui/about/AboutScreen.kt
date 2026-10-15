package com.andrespar2.parfmi.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.andrespar2.parfmi.BuildConfig
import com.andrespar2.parfmi.R
import com.andrespar2.parfmi.ui.components.SectionCard
import com.andrespar2.parfmi.ui.theme.ParfmiColors

/**
 * Pantalla "Acerca de": version, descripcion, creditos del desarrollador y
 * enlaces al repositorio. Deja claro el alcance de la app (solo consulta).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        containerColor = ParfmiColors.AmoledBlack,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
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
            Text(
                text = stringResource(R.string.about_app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = ParfmiColors.TextPrimary,
            )
            Text(
                text = stringResource(
                    R.string.about_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = ParfmiColors.Red,
            )
            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyLarge,
                color = ParfmiColors.TextSecondary,
            )

            SectionCard(title = stringResource(R.string.about_credits_title)) {
                Text(
                    text = stringResource(R.string.about_developer_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = ParfmiColors.TextTertiary,
                )
                Text(
                    text = stringResource(R.string.about_developer),
                    style = MaterialTheme.typography.titleMedium,
                    color = ParfmiColors.TextPrimary,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "${stringResource(R.string.about_github_label)}: ${stringResource(R.string.about_github_url)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParfmiColors.Info,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${stringResource(R.string.about_repo_label)}: ${stringResource(R.string.about_repo_url)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParfmiColors.Info,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.about_credits_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParfmiColors.TextSecondary,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        openUrl(context, context.getString(R.string.about_github_url))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParfmiColors.Red,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(stringResource(R.string.about_action_open_github))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(context.getString(R.string.about_repo_url)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ParfmiColors.TextPrimary),
                ) {
                    Text(stringResource(R.string.about_action_copy_link))
                }
            }

            SectionCard(title = stringResource(R.string.about_legal_title)) {
                Text(
                    text = stringResource(R.string.about_legal_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParfmiColors.TextSecondary,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
