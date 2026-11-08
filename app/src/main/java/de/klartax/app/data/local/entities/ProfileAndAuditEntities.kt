package de.klartax.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Steuer-Profil für das aktuelle Veranlagungsjahr.
 * Unterstützt den sofortigen Wechsel zwischen:
 * - "PROFILE_1_KOMBI" ("Kombi: Freiberufler & Familie")
 * - "PROFILE_2_PRIVAT" ("Rein Privat: Arbeitnehmer & Vermieter")
 */
@Serializable
@Entity(tableName = "tax_profiles")
data class TaxProfileEntity(
    @PrimaryKey val id: String,
    val profileTitle: String,
    val profileSubtitle: String,
    val taxYear: Int,
    val taxpayerFirstName: String,
    val taxpayerLastName: String,
    val taxIdPrimary: String,
    val taxNumberFreelance: String?,
    val vatId: String?,
    val isMarriedJointAssessment: Boolean,
    val spouseFirstName: String?,
    val spouseLastName: String?,
    val federalStateCode: String, // z. B. "BY" oder "BW" (8 % Kirchensteuer) vs. "BE", "NW", "HH" (9 % Kirchensteuer)
    val churchTaxMember: Boolean,
    val hasFreelanceActivity: Boolean,
    val freelanceProfessionTitle: String?,
    val isKleinunternehmerSec19: Boolean,
    val skrChartOfAccounts: String, // "SKR03" oder "SKR04"
    val isKskMember: Boolean,
    val isVersorgungswerkMember: Boolean,
    val businessBankBalanceCents: Long,
    val incomeTaxPrepaymentsQuarterlyCents: Long,
    val progressionBenefitCents: Long, // Elterngeld / Krankengeld / ALG I (§ 32b EStG)
    val isActiveProfile: Boolean,
    val updatedAtEpochMillis: Long
)

/**
 * Unveränderliches GoBD-Audit-Log mit lückenloser SHA-256-Hash-Verkettung.
 * Jede Festschreibung einer EÜR-Buchung, jede versendete Rechnung und jede
 * Storno-/Korrekturbuchung erzeugt einen verketteten Eintrag.
 */
@Serializable
@Entity(tableName = "gobd_audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val sequenceNumber: Long,
    val previousHash: String,
    val entryHash: String,
    val entityType: String, // "EUR_BOOKING", "INVOICE", "ASSET"
    val entityId: String,
    val actionType: String, // "LOCK_BOOKING", "STORNO_BOOKING", "CORRECTION_BOOKING", "ISSUE_INVOICE", "STORNO_INVOICE"
    val summaryDe: String,
    val payloadJson: String,
    val createdAtEpochMillis: Long
)
