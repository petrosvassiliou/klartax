package de.klartax.app.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verwaltet die Ende-zu-Ende-Verschlüsselung (AES-256-GCM via Google Tink)
 * für:
 * 1. Den SQLCipher-Datenbank-Passphrase-Schlüssel im Android Keystore.
 * 2. Die Zero-Knowledge Delta-Sync-Pakete (`sync_delta_<timestamp>.enc`) im Google Drive `appDataFolder`.
 * 3. Lokale `.klartax` Komplett-Backups und PDF-/Foto-Belege.
 */
@Singleton
class TinkCryptoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEYSET_NAME = "klartax_zero_knowledge_keyset"
        private const val PREF_FILE_NAME = "klartax_tink_keyset_prefs"
        private const val MASTER_KEY_URI = "android-keystore://klartax_master_key_v1"
        private const val DB_PASSPHRASE_PREF_KEY = "sqlcipher_wrapped_passphrase_v1"
        private const val ASSOCIATED_DATA_DB = "de.klartax.app:sqlcipher:v1"
    }

    private val aead: Aead by lazy {
        AeadConfig.register()
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
        keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    /**
     * Liefert die 256-Bit SQLCipher-Passphrase, die mit Google Tink AES-256-GCM
     * unter dem Android Keystore Master-Key verschlüsselt gespeichert ist.
     */
    fun getOrCreateDatabasePassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        val encodedCiphertext = prefs.getString(DB_PASSPHRASE_PREF_KEY, null)
        val aadBytes = ASSOCIATED_DATA_DB.toByteArray(StandardCharsets.UTF_8)

        if (encodedCiphertext != null) {
            val cipherBytes = android.util.Base64.decode(encodedCiphertext, android.util.Base64.NO_WRAP)
            return aead.decrypt(cipherBytes, aadBytes)
        }

        val randomPassphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val encryptedPassphrase = aead.encrypt(randomPassphrase, aadBytes)
        val base64Cipher = android.util.Base64.encodeToString(encryptedPassphrase, android.util.Base64.NO_WRAP)
        prefs.edit().putString(DB_PASSPHRASE_PREF_KEY, base64Cipher).apply()
        return randomPassphrase
    }

    /**
     * Verschlüsselt beliebige Nutzdaten (Delta-JSON oder PDF-Beleg) mit AES-256-GCM.
     */
    fun encryptPayload(plainBytes: ByteArray, associatedData: String): ByteArray {
        val aad = associatedData.toByteArray(StandardCharsets.UTF_8)
        return aead.encrypt(plainBytes, aad)
    }

    /**
     * Entschlüsselt ein AES-256-GCM-Paket und verifiziert den Authentifizierungs-Tag.
     */
    fun decryptPayload(cipherBytes: ByteArray, associatedData: String): ByteArray {
        val aad = associatedData.toByteArray(StandardCharsets.UTF_8)
        return aead.decrypt(cipherBytes, aad)
    }

    fun encryptUtf8(plainText: String, associatedData: String): ByteArray =
        encryptPayload(plainText.toByteArray(StandardCharsets.UTF_8), associatedData)

    fun decryptUtf8(cipherBytes: ByteArray, associatedData: String): String =
        String(decryptPayload(cipherBytes, associatedData), StandardCharsets.UTF_8)

    /**
     * Fordert biometrische Authentifizierung (Fingerabdruck / Face Unlock) oder
     * Geräte-PIN (insb. auf ChromeOS / Googlebooks) an.
     */
    fun authenticateUser(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

        if (biometricManager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            onSuccess()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()

        prompt.authenticate(promptInfo)
    }
}
