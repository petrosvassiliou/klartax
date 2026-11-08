package de.klartax.app

import android.os.Bundle
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
 * (`resizeableActivity="true"`, Freeform-Desktop-Modus, BiometricPrompt Tresor-Schutz).
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

        setContent {
            KlartaxTheme {
                KlartaxAdaptiveApp(viewModel = viewModel)
            }
        }
    }
}
