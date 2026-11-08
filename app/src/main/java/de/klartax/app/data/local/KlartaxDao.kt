package de.klartax.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.klartax.app.core.security.GoBdHashChain
import de.klartax.app.data.local.entities.AssetEntity
import de.klartax.app.data.local.entities.AuditLogEntity
import de.klartax.app.data.local.entities.BankTransactionEntity
import de.klartax.app.data.local.entities.CapitalAndCryptoEntity
import de.klartax.app.data.local.entities.ChildTaxEntity
import de.klartax.app.data.local.entities.CustomerEntity
import de.klartax.app.data.local.entities.DocumentTextTemplateEntity
import de.klartax.app.data.local.entities.EmploymentTaxEntity
import de.klartax.app.data.local.entities.EurBookingEntity
import de.klartax.app.data.local.entities.InvoiceEntity
import de.klartax.app.data.local.entities.PensionAndInsuranceEntity
import de.klartax.app.data.local.entities.RentalPropertyEntity
import de.klartax.app.data.local.entities.ServiceCatalogItemEntity
import de.klartax.app.data.local.entities.SpecialExpenseAndBurdenEntity
import de.klartax.app.data.local.entities.StationeryConfigEntity
import de.klartax.app.data.local.entities.TaxAssessmentNoticeEntity
import de.klartax.app.data.local.entities.TaxProfileEntity
import de.klartax.app.data.local.entities.TimeTrackingEntryEntity
import de.klartax.app.data.local.entities.VehicleLogEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface KlartaxDao {

    // --- Profile ---
    @Query("SELECT * FROM tax_profiles ORDER BY id ASC")
    fun observeAllProfiles(): Flow<List<TaxProfileEntity>>

    @Query("SELECT * FROM tax_profiles WHERE isActiveProfile = 1 LIMIT 1")
    fun observeActiveProfile(): Flow<TaxProfileEntity?>

    @Query("SELECT * FROM tax_profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: String): TaxProfileEntity?

    @Query("SELECT COUNT(*) FROM tax_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfiles(profiles: List<TaxProfileEntity>)

    @Query("UPDATE tax_profiles SET isActiveProfile = CASE WHEN id = :activeId THEN 1 ELSE 0 END")
    suspend fun switchActiveProfile(activeId: String)

    // --- Kundenverwaltung (CRM) ---
    @Query("SELECT * FROM customers WHERE profileId = :profileId ORDER BY customerNumber ASC")
    fun observeCustomers(profileId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE profileId = :profileId ORDER BY customerNumber ASC")
    suspend fun getCustomersSync(profileId: String): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :customerId LIMIT 1")
    suspend fun getCustomerById(customerId: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomers(customers: List<CustomerEntity>)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: String)

    // --- Produkte & Dienstleistungen (Artikelkatalog) ---
    @Query("SELECT * FROM service_catalog_items WHERE profileId = :profileId ORDER BY articleNumber ASC")
    fun observeServiceCatalogItems(profileId: String): Flow<List<ServiceCatalogItemEntity>>

    @Query("SELECT * FROM service_catalog_items WHERE profileId = :profileId ORDER BY articleNumber ASC")
    suspend fun getServiceCatalogItemsSync(profileId: String): List<ServiceCatalogItemEntity>

    @Query("SELECT * FROM service_catalog_items WHERE id = :itemId LIMIT 1")
    suspend fun getServiceCatalogItemById(itemId: String): ServiceCatalogItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertServiceCatalogItems(items: List<ServiceCatalogItemEntity>)

    @Query("DELETE FROM service_catalog_items WHERE id = :itemId")
    suspend fun deleteServiceCatalogItemById(itemId: String)

    // --- Standardtexte & Textbausteine (Angebote, Aufträge, Rechnungen) ---
    @Query("SELECT * FROM document_text_templates WHERE profileId = :profileId ORDER BY documentScope ASC, placementType ASC")
    fun observeDocumentTextTemplates(profileId: String): Flow<List<DocumentTextTemplateEntity>>

    @Query("SELECT * FROM document_text_templates WHERE profileId = :profileId ORDER BY documentScope ASC, placementType ASC")
    suspend fun getDocumentTextTemplatesSync(profileId: String): List<DocumentTextTemplateEntity>

    @Query("SELECT * FROM document_text_templates WHERE id = :templateId LIMIT 1")
    suspend fun getDocumentTextTemplateById(templateId: String): DocumentTextTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocumentTextTemplates(templates: List<DocumentTextTemplateEntity>)

    @Query("DELETE FROM document_text_templates WHERE id = :templateId")
    suspend fun deleteDocumentTextTemplateById(templateId: String)

    // --- Briefpapier & Firmenlogo (DIN 5008) ---
    @Query("SELECT * FROM stationery_configs WHERE profileId = :profileId LIMIT 1")
    fun observeStationeryConfig(profileId: String): Flow<StationeryConfigEntity?>

    @Query("SELECT * FROM stationery_configs WHERE profileId = :profileId LIMIT 1")
    suspend fun getStationeryConfigSync(profileId: String): StationeryConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStationeryConfig(config: StationeryConfigEntity)

    // --- GoBD Audit Log ---
    @Query("SELECT * FROM gobd_audit_logs WHERE profileId = :profileId ORDER BY sequenceNumber ASC")
    fun observeAuditLogs(profileId: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM gobd_audit_logs WHERE profileId = :profileId ORDER BY sequenceNumber ASC")
    suspend fun getAuditLogsSync(profileId: String): List<AuditLogEntity>

    @Query("SELECT * FROM gobd_audit_logs WHERE profileId = :profileId ORDER BY sequenceNumber DESC LIMIT 1")
    suspend fun getLatestAuditLog(profileId: String): AuditLogEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAuditLog(entry: AuditLogEntity)

    // --- Modul A: EÜR Buchungen (mit GoBD-Schutz für isLocked = true) ---
    @Query("SELECT * FROM eur_bookings WHERE profileId = :profileId ORDER BY paymentDateIso DESC, documentNumber DESC")
    fun observeEurBookings(profileId: String): Flow<List<EurBookingEntity>>

    @Query("SELECT * FROM eur_bookings WHERE profileId = :profileId ORDER BY paymentDateIso ASC")
    suspend fun getEurBookingsSync(profileId: String): List<EurBookingEntity>

    @Query("SELECT * FROM eur_bookings WHERE id = :bookingId LIMIT 1")
    suspend fun getEurBookingById(bookingId: String): EurBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalInsertOrUpdateUnlockedBooking(booking: EurBookingEntity)

    @Transaction
    suspend fun saveEurBookingGoBdSafe(booking: EurBookingEntity) {
        val existing = getEurBookingById(booking.id)
        if (existing != null && existing.isLocked) {
            throw IllegalStateException(
                "GoBD-Schutzverletzung: Buchung ${existing.documentNumber} ist festgeschrieben (isLocked=true) und darf nur über eine Storno- und Korrekturbuchung geändert werden."
            )
        }
        internalInsertOrUpdateUnlockedBooking(booking)
    }

    @Transaction
    suspend fun createGoBdStornoAndCorrectionBooking(
        originalBookingId: String,
        correctedBooking: EurBookingEntity,
        reasonDe: String
    ) {
        val original = getEurBookingById(originalBookingId)
            ?: throw IllegalArgumentException("Ursprungsbuchung $originalBookingId nicht gefunden.")

        val now = System.currentTimeMillis()
        val lastAudit = getLatestAuditLog(original.profileId)
        val prevHash1 = lastAudit?.entryHash ?: GoBdHashChain.GENESIS_HASH
        val seq1 = (lastAudit?.sequenceNumber ?: 0L) + 1L

        val stornoId = "STORNO-${UUID.randomUUID().toString().take(8)}"
        val stornoPayload = "STORNO_OF:${original.id}|DOC:${original.documentNumber}|NET:${-original.netAmountCents}|REASON:$reasonDe"
        val stornoHash = GoBdHashChain.computeEntryHash(
            previousHash = prevHash1,
            entityType = "EUR_BOOKING",
            entityId = stornoId,
            actionType = "STORNO_BOOKING",
            canonicalPayload = stornoPayload,
            timestampEpochMillis = now
        )

        val stornoBooking = original.copy(
            id = stornoId,
            documentNumber = "${original.documentNumber}-ST",
            descriptionDe = "STORNO zu ${original.documentNumber}: ${original.descriptionDe} ($reasonDe)",
            netAmountCents = -original.netAmountCents,
            vatAmountCents = -original.vatAmountCents,
            grossAmountCents = -original.grossAmountCents,
            deductibleNetCents = -original.deductibleNetCents,
            nonDeductibleNetCents = -original.nonDeductibleNetCents,
            isLocked = true,
            isStornoRecord = true,
            stornoOfBookingId = original.id,
            gobdRecordHash = stornoHash,
            updatedAtEpochMillis = now
        )
        internalInsertOrUpdateUnlockedBooking(stornoBooking)
        insertAuditLog(
            AuditLogEntity(
                id = "AUDIT-$stornoId",
                profileId = original.profileId,
                sequenceNumber = seq1,
                previousHash = prevHash1,
                entryHash = stornoHash,
                entityType = "EUR_BOOKING",
                entityId = stornoId,
                actionType = "STORNO_BOOKING",
                summaryDe = "GoBD-Stornobuchung zu ${original.documentNumber} ($reasonDe)",
                payloadJson = stornoPayload,
                createdAtEpochMillis = now
            )
        )

        val seq2 = seq1 + 1L
        val correctionPayload = "CORRECTION_OF:${original.id}|DOC:${correctedBooking.documentNumber}|NET:${correctedBooking.netAmountCents}"
        val correctionHash = GoBdHashChain.computeEntryHash(
            previousHash = stornoHash,
            entityType = "EUR_BOOKING",
            entityId = correctedBooking.id,
            actionType = "CORRECTION_BOOKING",
            canonicalPayload = correctionPayload,
            timestampEpochMillis = now + 1L
        )
        val lockedCorrection = correctedBooking.copy(
            isLocked = true,
            isStornoRecord = false,
            stornoOfBookingId = original.id,
            gobdRecordHash = correctionHash,
            updatedAtEpochMillis = now + 1L
        )
        internalInsertOrUpdateUnlockedBooking(lockedCorrection)
        insertAuditLog(
            AuditLogEntity(
                id = "AUDIT-${correctedBooking.id}",
                profileId = original.profileId,
                sequenceNumber = seq2,
                previousHash = stornoHash,
                entryHash = correctionHash,
                entityType = "EUR_BOOKING",
                entityId = correctedBooking.id,
                actionType = "CORRECTION_BOOKING",
                summaryDe = "GoBD-Korrekturbuchung ${correctedBooking.documentNumber} (ersetzt ${original.documentNumber})",
                payloadJson = correctionPayload,
                createdAtEpochMillis = now + 1L
            )
        )
    }

    // --- Modul A: E-Rechnungen, Zeiterfassung, Anlagenverzeichnis, PKW & Banking ---
    @Query("SELECT * FROM invoices WHERE profileId = :profileId ORDER BY issueDateIso DESC, invoiceNumber DESC")
    fun observeInvoices(profileId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE profileId = :profileId ORDER BY issueDateIso ASC")
    suspend fun getInvoicesSync(profileId: String): List<InvoiceEntity>

    @Query("SELECT * FROM invoices WHERE id = :invoiceId LIMIT 1")
    suspend fun getInvoiceById(invoiceId: String): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalUpsertInvoice(invoice: InvoiceEntity)

    @Transaction
    suspend fun saveInvoiceGoBdSafe(invoice: InvoiceEntity) {
        val existing = getInvoiceById(invoice.id)
        if (existing != null && existing.isLocked && existing.grossTotalCents != invoice.grossTotalCents) {
            throw IllegalStateException(
                "GoBD-Schutzverletzung: Versendete Rechnung ${existing.invoiceNumber} ist festgeschrieben und darf inhaltlich nicht überschrieben werden. Bitte Stornorechnung erzeugen."
            )
        }
        internalUpsertInvoice(invoice)
    }

    @Query("SELECT * FROM time_tracking_entries WHERE profileId = :profileId ORDER BY dateIso DESC")
    fun observeTimeTrackingEntries(profileId: String): Flow<List<TimeTrackingEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTimeTrackingEntries(entries: List<TimeTrackingEntryEntity>)

    @Query("SELECT * FROM assets_afa WHERE profileId = :profileId ORDER BY purchaseDateIso DESC")
    fun observeAssets(profileId: String): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssets(assets: List<AssetEntity>)

    @Query("SELECT * FROM vehicle_logs WHERE profileId = :profileId LIMIT 1")
    fun observeVehicleLog(profileId: String): Flow<VehicleLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVehicleLog(vehicleLog: VehicleLogEntity)

    @Query("SELECT * FROM bank_transactions WHERE profileId = :profileId ORDER BY bookingDateIso DESC")
    fun observeBankTransactions(profileId: String): Flow<List<BankTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBankTransactions(transactions: List<BankTransactionEntity>)

    // --- Modul B: WISO Einkommensteuer ---
    @Query("SELECT * FROM employment_tax_anlage_n WHERE profileId = :profileId ORDER BY personRole ASC")
    fun observeEmploymentTax(profileId: String): Flow<List<EmploymentTaxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmploymentTax(records: List<EmploymentTaxEntity>)

    @Query("SELECT * FROM special_expenses_and_burdens WHERE profileId = :profileId ORDER BY invoiceDateIso DESC")
    fun observeSpecialExpensesAndBurdens(profileId: String): Flow<List<SpecialExpenseAndBurdenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpecialExpensesAndBurdens(records: List<SpecialExpenseAndBurdenEntity>)

    @Query("SELECT * FROM capital_and_crypto_tax WHERE profileId = :profileId")
    fun observeCapitalAndCrypto(profileId: String): Flow<List<CapitalAndCryptoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCapitalAndCrypto(records: List<CapitalAndCryptoEntity>)

    @Query("SELECT * FROM rental_properties_anlage_v WHERE profileId = :profileId")
    fun observeRentalProperties(profileId: String): Flow<List<RentalPropertyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRentalProperties(records: List<RentalPropertyEntity>)

    @Query("SELECT * FROM children_anlage_kind WHERE profileId = :profileId")
    fun observeChildren(profileId: String): Flow<List<ChildTaxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChildren(records: List<ChildTaxEntity>)

    @Query("SELECT * FROM pension_and_insurance WHERE profileId = :profileId LIMIT 1")
    fun observePensionAndInsurance(profileId: String): Flow<PensionAndInsuranceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPensionAndInsurance(record: PensionAndInsuranceEntity)

    @Query("SELECT * FROM tax_assessment_notices WHERE profileId = :profileId")
    fun observeTaxAssessmentNotices(profileId: String): Flow<List<TaxAssessmentNoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTaxAssessmentNotices(notices: List<TaxAssessmentNoticeEntity>)
}
