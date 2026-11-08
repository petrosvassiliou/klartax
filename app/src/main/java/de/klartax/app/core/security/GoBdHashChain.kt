package de.klartax.app.core.security

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Unveränderliche GoBD-Hash-Verkettung (SHA-256) für festgeschriebene EÜR-Buchungen,
 * Rechnungen und Storno-/Korrekturbuchungen nach Rz. 108–112 GoBD.
 */
object GoBdHashChain {
    const val GENESIS_HASH =
        "0000000000000000000000000000000000000000000000000000000000000000"

    fun computeEntryHash(
        previousHash: String,
        entityType: String,
        entityId: String,
        actionType: String,
        canonicalPayload: String,
        timestampEpochMillis: Long
    ): String {
        val rawRecord = buildString {
            append(previousHash.lowercase())
            append('|')
            append(entityType)
            append('|')
            append(entityId)
            append('|')
            append(actionType)
            append('|')
            append(canonicalPayload)
            append('|')
            append(timestampEpochMillis)
        }
        return sha256Hex(rawRecord)
    }

    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return buildString(hashBytes.size * 2) {
            for (byte in hashBytes) {
                val hex = (byte.toInt() and 0xFF).toString(16)
                if (hex.length == 1) append('0')
                append(hex)
            }
        }
    }

    /**
     * Verifiziert eine vollständige Liste von GoBD-Audit-Einträgen auf lückenlose
     * und unmanipulierte SHA-256-Verkettung.
     */
    fun verifyChain(entries: List<ChainVerifiableEntry>): ChainVerificationResult {
        var expectedPrevious = GENESIS_HASH
        for ((index, entry) in entries.withIndex()) {
            if (entry.previousHash != expectedPrevious) {
                return ChainVerificationResult(
                    isValid = false,
                    verifiedCount = index,
                    brokenAtId = entry.id,
                    reason = "Vorgänger-Hash stimmt bei Eintrag #${index + 1} (${entry.id}) nicht überein."
                )
            }
            val recomputed = computeEntryHash(
                previousHash = entry.previousHash,
                entityType = entry.entityType,
                entityId = entry.entityId,
                actionType = entry.actionType,
                canonicalPayload = entry.payloadJson,
                timestampEpochMillis = entry.createdAtEpochMillis
            )
            if (!recomputed.equals(entry.entryHash, ignoreCase = true)) {
                return ChainVerificationResult(
                    isValid = false,
                    verifiedCount = index,
                    brokenAtId = entry.id,
                    reason = "Eintrags-Hash wurde bei Eintrag #${index + 1} (${entry.id}) manipuliert."
                )
            }
            expectedPrevious = entry.entryHash
        }
        return ChainVerificationResult(
            isValid = true,
            verifiedCount = entries.size,
            brokenAtId = null,
            reason = "GoBD-Prüfung erfolgreich: Alle ${entries.size} Buchungssätze sind lückenlos SHA-256-verkettet."
        )
    }

    data class ChainVerifiableEntry(
        val id: String,
        val previousHash: String,
        val entryHash: String,
        val entityType: String,
        val entityId: String,
        val actionType: String,
        val payloadJson: String,
        val createdAtEpochMillis: Long
    )

    data class ChainVerificationResult(
        val isValid: Boolean,
        val verifiedCount: Int,
        val brokenAtId: String?,
        val reason: String
    )
}
