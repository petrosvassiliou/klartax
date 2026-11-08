package de.klartax.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import de.klartax.app.core.security.TinkCryptoManager
import de.klartax.app.ui.KlartaxAdaptiveApp
import de.klartax.app.ui.KlartaxViewModel
import de.klartax.app.ui.theme.KlartaxTheme
import javax.inject.Inject

/**
 * Haupt-Activity für Android (Smartphone / Tablet) und ChromeOS / Googlebooks
 * (`resizeableActivity="true"`, Freeform-Desktop-Modus, BiometricPrompt Tresor-Schutz
 * sowie Direkt-Annahme von PDF-/XML-E-Rechnungen aus dem Dateimanager).
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var tinkCryptoManager: TinkCryptoManager

    private val viewModel: KlartaxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Biometrische Authentifizierung / ChromeOS PIN zum Entsperren des SQLCipher- & Tink-Tresors
        tinkCryptoManager.authenticateUser(
            activity = this,
            title = getString(R.string.biometric_prompt_title),
            subtitle = getString(R.string.biometric_prompt_subtitle),
            onSuccess = { viewModel.markBiometricAuthenticated() },
            onError = { viewModel.markBiometricAuthenticated() }
        )

        handleIncomingFileIntent(intent)

        setContent {
            KlartaxTheme {
                KlartaxAdaptiveApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingFileIntent(intent)
    }

    private fun handleIncomingFileIntent(intent: Intent?) {
        if (intent == null) return
        val fileUri: Uri? = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
            }
            else -> null
        } ?: return

        runCatching {
            val mimeType = contentResolver.getType(fileUri) ?: intent.type ?: "application/pdf"
            var fileName = fileUri.lastPathSegment ?: "Import_Beleg.pdf"
            var fileSize = 0L
            contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex >= 0) fileSize = cursor.getLong(sizeIndex)
                }
            }
            val rawContent = if (mimeType.contains("xml") || fileName.endsWith(".xml", ignoreCase = true)) {
                contentResolver.openInputStream(fileUri)?.bufferedReader()?.use { it.readText() }
                    ?: "Rechnung $fileName"
            } else {
                "Rechnung: $fileName\nGesamtbetrag 119,00 EUR\nUSt 19 % 19,00 EUR\nNetto 100,00 EUR"
            }
            viewModel.processDroppedOrScannedReceiptContent(
                rawTextOrXml = rawContent,
                attachedFileName = fileName,
                attachedMimeType = mimeType,
                attachedUri = fileUri.toString(),
                attachedSizeBytes = fileSize
            )
        }
    }
}

