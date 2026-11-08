package de.klartax.app.data.seed

import de.klartax.app.core.security.GoBdHashChain
import de.klartax.app.data.local.KlartaxDao
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
import de.klartax.app.data.local.entities.InvoiceLineItem
import de.klartax.app.data.local.entities.KlartextEurCategory
import de.klartax.app.data.local.entities.PensionAndInsuranceEntity
import de.klartax.app.data.local.entities.RentalPropertyEntity
import de.klartax.app.data.local.entities.ServiceCatalogItemEntity
import de.klartax.app.data.local.entities.SpecialExpenseAndBurdenEntity
import de.klartax.app.data.local.entities.StationeryConfigEntity
import de.klartax.app.data.local.entities.TaxAssessmentNoticeEntity
import de.klartax.app.data.local.entities.TaxProfileEntity
import de.klartax.app.data.local.entities.TimeTrackingEntryEntity
import de.klartax.app.data.local.entities.VehicleLogEntity
import de.klartax.app.domain.einvoice.EInvoiceGeneratorEngine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 1.4: Deutscher Muster-Seeder (`DatabaseSeeder`)
 * Legt beim ersten Start automatisch die beiden sofort umschaltbaren deutschen Muster-Profile,
 * Kundenstammdaten (`CustomerEntity`), EÜR-Buchungen inkl. PDF-/Bilderanhängen und E-Rechnungen an.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val dao: KlartaxDao,
    private val eInvoiceEngine: EInvoiceGeneratorEngine
) {
    companion object {
        const val PROFILE_1_ID = "PROFILE_1_KOMBI"
        const val PROFILE_2_ID = "PROFILE_2_PRIVAT"
    }

    private val json = Json { encodeDefaults = true }

    suspend fun seedIfEmpty() {
        if (dao.getProfileCount() > 0) return

        val now = 1794090000000L

        // =========================================================================
        // 1. PROFILE ANLEGEN
        // =========================================================================
        val profile1 = TaxProfileEntity(
            id = PROFILE_1_ID,
            profileTitle = "Profil 1: Kombi (Freiberufler & Familie)",
            profileSubtitle = "IT-/Design-Freiberufler (EÜR, ZUGFeRD, Digital-AfA) + Ehepartnerin (Anlage N) + 1 Kind + ETF & § 35a",
            taxYear = 2026,
            taxpayerFirstName = "Lukas",
            taxpayerLastName = "Weber",
            taxIdPrimary = "48 920 157 384",
            taxNumberFreelance = "143/815/08155",
            vatId = "DE314159265",
            isMarriedJointAssessment = true,
            spouseFirstName = "Hannah",
            spouseLastName = "Weber",
            federalStateCode = "BY",
            churchTaxMember = true,
            hasFreelanceActivity = true,
            freelanceProfessionTitle = "Freiberuflicher Principal UX-Architect & Full-Stack Berater (§ 18 EStG)",
            isKleinunternehmerSec19 = false,
            skrChartOfAccounts = "SKR03",
            isKskMember = true,
            isVersorgungswerkMember = false,
            businessBankBalanceCents = 42_850_00L,
            incomeTaxPrepaymentsQuarterlyCents = 9_600_00L,
            progressionBenefitCents = 1_800_00L,
            isActiveProfile = true,
            updatedAtEpochMillis = now
        )

        val profile2 = TaxProfileEntity(
            id = PROFILE_2_ID,
            profileTitle = "Profil 2: Rein Privat (Arbeitnehmer & Vermieter)",
            profileSubtitle = "Angestellter Senior-Ingenieur (Anlage N, Pendler, Homeoffice) + Handwerker (§ 35a) + Eigentumswohnung (Anlage V)",
            taxYear = 2026,
            taxpayerFirstName = "Markus",
            taxpayerLastName = "Bergmann",
            taxIdPrimary = "62 419 803 119",
            taxNumberFreelance = null,
            vatId = null,
            isMarriedJointAssessment = false,
            spouseFirstName = null,
            spouseLastName = null,
            federalStateCode = "NW",
            churchTaxMember = true,
            hasFreelanceActivity = false,
            freelanceProfessionTitle = null,
            isKleinunternehmerSec19 = false,
            skrChartOfAccounts = "SKR03",
            isKskMember = false,
            isVersorgungswerkMember = false,
            businessBankBalanceCents = 14_200_00L,
            incomeTaxPrepaymentsQuarterlyCents = 0L,
            progressionBenefitCents = 0L,
            isActiveProfile = false,
            updatedAtEpochMillis = now
        )

        dao.upsertProfiles(listOf(profile1, profile2))

        // =========================================================================
        // 1B. KUNDENVERWALTUNG (CRM STAMMDATEN FÜR RECHNUNGEN & E-RECHNUNG)
        // =========================================================================
        val customers = listOf(
            CustomerEntity(
                id = "CUST-1",
                profileId = PROFILE_1_ID,
                customerNumber = "KD-2026-001",
                companyOrFullName = "Süddeutsche FinTech Solutions AG",
                contactPersonName = "Dr. Thomas Lindner (CTO)",
                email = "rechnungseingang@sueddeutsche-fintech.de",
                streetAndNumber = "Maximilianstraße 42",
                postalCode = "80539",
                city = "München",
                countryIso = "DE",
                vatId = "DE129485712",
                buyerReferenceLeitwegId = "FINTECH-MUC-2026-Q2",
                defaultHourlyRateNetCents = 150_00L,
                paymentTermDays = 14,
                defaultTaxMode = "STANDARD_19",
                notesDe = "Bevorzugt ZUGFeRD 2.3 (PDF/A-3 mit eingebettetem XML).",
                updatedAtEpochMillis = now
            ),
            CustomerEntity(
                id = "CUST-2",
                profileId = PROFILE_1_ID,
                customerNumber = "KD-2026-002",
                companyOrFullName = "Bundesanstalt für Digitale Verwaltung",
                contactPersonName = "Referat IT-Architektur II",
                email = "xrechnung@bundesverwaltung-digital.de",
                streetAndNumber = "Friedrichstraße 108",
                postalCode = "10117",
                city = "Berlin",
                countryIso = "DE",
                vatId = "DE998877665",
                buyerReferenceLeitwegId = "991-12345-67",
                defaultHourlyRateNetCents = 160_00L,
                paymentTermDays = 30,
                defaultTaxMode = "STANDARD_19",
                notesDe = "Öffentlicher Auftraggeber: Pflicht zur XRechnung 3.0 CII mit Leitweg-ID BT-10.",
                updatedAtEpochMillis = now
            ),
            CustomerEntity(
                id = "CUST-3",
                profileId = PROFILE_1_ID,
                customerNumber = "KD-2026-003",
                companyOrFullName = "Hochschule München – Fakultät Informatik & Design",
                contactPersonName = "Prof. Dr. Martina Baumann",
                email = "honorar@hm.edu",
                streetAndNumber = "Lothstraße 34",
                postalCode = "80335",
                city = "München",
                countryIso = "DE",
                vatId = "DE811335517",
                buyerReferenceLeitwegId = "HM-FAK07-LEHRE",
                defaultHourlyRateNetCents = 100_00L,
                paymentTermDays = 14,
                defaultTaxMode = "KLEINUNTERNEHMER_SEC19",
                notesDe = "Nebenberuflicher Lehrauftrag nach § 3 Nr. 26 EStG (Übungsleiterpauschale).",
                updatedAtEpochMillis = now
            ),
            CustomerEntity(
                id = "CUST-4",
                profileId = PROFILE_1_ID,
                customerNumber = "KD-2026-004",
                companyOrFullName = "Alpine Cloud Technologies GmbH (Wien)",
                contactPersonName = "Mag. Lukas Gruber",
                email = "accounting@alpinecloud.at",
                streetAndNumber = "Kärntner Ring 12",
                postalCode = "1010",
                city = "Wien",
                countryIso = "AT",
                vatId = "ATU67891234",
                buyerReferenceLeitwegId = "AT-ALPINE-2026",
                defaultHourlyRateNetCents = 155_00L,
                paymentTermDays = 14,
                defaultTaxMode = "REVERSE_CHARGE_SEC13B",
                notesDe = "EU B2B Reverse Charge (§ 13b UStG + Zusammenfassende Meldung ZM).",
                updatedAtEpochMillis = now
            )
        )
        dao.upsertCustomers(customers)

        // =========================================================================
        // 2. PROFIL 1: LÜCKENLOSE GO-BD SHA-256 HASH-VERKETTUNG & EÜR-BUCHUNGEN (INKL. PDF/BILD-ANHÄNGEN)
        // =========================================================================
        val ts1 = now - 80_000L
        val payload1 = "DOC:RE-2026-001|NET:2850000|VAT:541500"
        val h1 = GoBdHashChain.computeEntryHash(
            previousHash = GoBdHashChain.GENESIS_HASH,
            entityType = "EUR_BOOKING",
            entityId = "EUR-2026-001",
            actionType = "LOCK_BOOKING",
            canonicalPayload = payload1,
            timestampEpochMillis = ts1
        )

        val ts2 = now - 60_000L
        val payload2 = "DOC:RE-2026-002|NET:1920000|VAT:364800"
        val h2 = GoBdHashChain.computeEntryHash(
            previousHash = h1,
            entityType = "EUR_BOOKING",
            entityId = "EUR-2026-002",
            actionType = "LOCK_BOOKING",
            canonicalPayload = payload2,
            timestampEpochMillis = ts2
        )

        val ts3 = now - 50_000L
        val payload3 = "DOC:RE-2026-003|NET:280000|VAT:0"
        val h3 = GoBdHashChain.computeEntryHash(
            previousHash = h2,
            entityType = "EUR_BOOKING",
            entityId = "EUR-2026-003",
            actionType = "LOCK_BOOKING",
            canonicalPayload = payload3,
            timestampEpochMillis = ts3
        )

        val ts4 = now - 40_000L
        val payload4 = "DOC:ER-2026-104|NET:240000|VAT:45600"
        val h4 = GoBdHashChain.computeEntryHash(
            previousHash = h3,
            entityType = "EUR_BOOKING",
            entityId = "EUR-2026-004",
            actionType = "LOCK_BOOKING",
            canonicalPayload = payload4,
            timestampEpochMillis = ts4
        )

        val ts5 = now - 20_000L
        val payload5 = "DOC:BW-2026-019|NET:20000|VAT:3800"
        val h5 = GoBdHashChain.computeEntryHash(
            previousHash = h4,
            entityType = "EUR_BOOKING",
            entityId = "EUR-2026-005",
            actionType = "LOCK_BOOKING",
            canonicalPayload = payload5,
            timestampEpochMillis = ts5
        )

        val p1Bookings = listOf(
            EurBookingEntity(
                id = "EUR-2026-001",
                profileId = PROFILE_1_ID,
                documentNumber = "RE-2026-001",
                descriptionDe = "UX-Architektur & Design-System Sprint Q1–Q2 (ZUGFeRD 2.3)",
                counterpartyName = "Süddeutsche FinTech Solutions AG",
                counterpartyIban = "DE44700202700015829400",
                category = KlartextEurCategory.INCOME_19_PCT,
                invoiceOrDueDateIso = "2026-04-15",
                paymentDateIso = "2026-04-24",
                isRecurringPayment = false,
                appliedTenDayRule = false,
                effectiveTaxYear = 2026,
                netAmountCents = 28_500_00L,
                vatRatePercent = 19,
                vatAmountCents = 5_415_00L,
                grossAmountCents = 33_915_00L,
                deductibleNetCents = 28_500_00L,
                nonDeductibleNetCents = 0L,
                hospitalityHost = null,
                hospitalityGuests = null,
                hospitalityOccasion = null,
                taxFreeAllowanceType = null,
                attachmentFileName = "RE-2026-001_ZUGFeRD_2_3.pdf",
                attachmentMimeType = "application/pdf",
                attachmentUri = "file:///klartax_vault/attachments/RE-2026-001_ZUGFeRD_2_3.pdf",
                attachmentSizeBytes = 184_320L,
                isLocked = true,
                isStornoRecord = false,
                stornoOfBookingId = null,
                gobdRecordHash = h1,
                updatedAtEpochMillis = ts1
            ),
            EurBookingEntity(
                id = "EUR-2026-002",
                profileId = PROFILE_1_ID,
                documentNumber = "RE-2026-002",
                descriptionDe = "Kotlin Multiplatform & Adaptive UI Beratung (XRechnung 3.0)",
                counterpartyName = "Bundesanstalt für Digitale Verwaltung",
                counterpartyIban = "DE12100000000012345678",
                category = KlartextEurCategory.INCOME_19_PCT,
                invoiceOrDueDateIso = "2026-08-30",
                paymentDateIso = "2026-09-10",
                isRecurringPayment = false,
                appliedTenDayRule = false,
                effectiveTaxYear = 2026,
                netAmountCents = 19_200_00L,
                vatRatePercent = 19,
                vatAmountCents = 3_648_00L,
                grossAmountCents = 22_848_00L,
                deductibleNetCents = 19_200_00L,
                nonDeductibleNetCents = 0L,
                hospitalityHost = null,
                hospitalityGuests = null,
                hospitalityOccasion = null,
                taxFreeAllowanceType = null,
                attachmentFileName = "RE-2026-002_XRechnung_CII.xml",
                attachmentMimeType = "application/xml",
                attachmentUri = "file:///klartax_vault/attachments/RE-2026-002_XRechnung_CII.xml",
                attachmentSizeBytes = 14_890L,
                isLocked = true,
                isStornoRecord = false,
                stornoOfBookingId = null,
                gobdRecordHash = h2,
                updatedAtEpochMillis = ts2
            ),
            EurBookingEntity(
                id = "EUR-2026-003",
                profileId = PROFILE_1_ID,
                documentNumber = "RE-2026-003",
                descriptionDe = "Lehrauftrag UX-Engineering an der Technischen Hochschule (§ 3 Nr. 26 EStG Übungsleiter)",
                counterpartyName = "Hochschule München",
                counterpartyIban = "DE75700500000000024810",
                category = KlartextEurCategory.INCOME_KLEINUNTERNEHMER_SEC19,
                invoiceOrDueDateIso = "2026-07-15",
                paymentDateIso = "2026-07-28",
                isRecurringPayment = false,
                appliedTenDayRule = false,
                effectiveTaxYear = 2026,
                netAmountCents = 2_800_00L,
                vatRatePercent = 0,
                vatAmountCents = 0L,
                grossAmountCents = 2_800_00L,
                deductibleNetCents = 2_800_00L,
                nonDeductibleNetCents = 0L,
                hospitalityHost = null,
                hospitalityGuests = null,
                hospitalityOccasion = null,
                taxFreeAllowanceType = "UEBUNGSLEITER_SEC3_NR26",
                attachmentFileName = "Honorar_Hochschule_Muenchen_2026.pdf",
                attachmentMimeType = "application/pdf",
                attachmentUri = "file:///klartax_vault/attachments/Honorar_Hochschule_Muenchen_2026.pdf",
                attachmentSizeBytes = 92_400L,
                isLocked = true,
                isStornoRecord = false,
                stornoOfBookingId = null,
                gobdRecordHash = h3,
                updatedAtEpochMillis = ts3
            ),
            EurBookingEntity(
                id = "EUR-2026-004",
                profileId = PROFILE_1_ID,
                documentNumber = "ER-2026-104",
                descriptionDe = "Digital-AfA (100 % Sofortabschreibung): Googlebook Pro 16\" + 4K USB-C Display (90 % betrieblich)",
                counterpartyName = "Cyberport GmbH",
                counterpartyIban = "DE89370400440532013000",
                category = KlartextEurCategory.EXPENSE_AFA_DEPRECIATION,
                invoiceOrDueDateIso = "2026-02-12",
                paymentDateIso = "2026-02-14",
                isRecurringPayment = false,
                appliedTenDayRule = false,
                effectiveTaxYear = 2026,
                netAmountCents = 2_400_00L,
                vatRatePercent = 19,
                vatAmountCents = 456_00L,
                grossAmountCents = 2_856_00L,
                deductibleNetCents = 2_160_00L,
                nonDeductibleNetCents = 240_00L,
                hospitalityHost = null,
                hospitalityGuests = null,
                hospitalityOccasion = null,
                taxFreeAllowanceType = null,
                attachmentFileName = "Cyberport_Googlebook_Pro16_Rechnung.pdf",
                attachmentMimeType = "application/pdf",
                attachmentUri = "file:///klartax_vault/attachments/Cyberport_Googlebook_Pro16_Rechnung.pdf",
                attachmentSizeBytes = 312_450L,
                isLocked = true,
                isStornoRecord = false,
                stornoOfBookingId = null,
                gobdRecordHash = h4,
                updatedAtEpochMillis = ts4
            ),
            EurBookingEntity(
                id = "EUR-2026-005",
                profileId = PROFILE_1_ID,
                documentNumber = "BW-2026-019",
                descriptionDe = "Geschäftsessen Projekt-Kickoff (70 % abziehbar / 30 % nicht abziehbar, 100 % Vorsteuer)",
                counterpartyName = "Brenner Operngrill München",
                counterpartyIban = "DE21701500001002345678",
                category = KlartextEurCategory.EXPENSE_HOSPITALITY_70_30,
                invoiceOrDueDateIso = "2026-05-18",
                paymentDateIso = "2026-05-18",
                isRecurringPayment = false,
                appliedTenDayRule = false,
                effectiveTaxYear = 2026,
                netAmountCents = 200_00L,
                vatRatePercent = 19,
                vatAmountCents = 38_00L,
                grossAmountCents = 238_00L,
                deductibleNetCents = 140_00L,
                nonDeductibleNetCents = 60_00L,
                hospitalityHost = "Lukas Weber",
                hospitalityGuests = "Dr. Thomas Lindner (CTO Süddeutsche FinTech AG), Sarah Koch",
                hospitalityOccasion = "Vertragsverhandlung & Architektur-Abnahme Release 2026",
                taxFreeAllowanceType = null,
                attachmentFileName = "Bewirtungsbeleg_Brenner_Operngrill_Foto.jpg",
                attachmentMimeType = "image/jpeg",
                attachmentUri = "file:///klartax_vault/attachments/Bewirtungsbeleg_Brenner_Operngrill_Foto.jpg",
                attachmentSizeBytes = 1_420_800L,
                isLocked = true,
                isStornoRecord = false,
                stornoOfBookingId = null,
                gobdRecordHash = h5,
                updatedAtEpochMillis = ts5
            ),
            EurBookingEntity(
                id = "EUR-2026-006",
                profileId = PROFILE_1_ID,
                documentNumber = "ER-2026-199",
                descriptionDe = "Cloud-Server, JetBrains All Products Pack & Glasfaser (§ 11 EStG 10-Tages-Regel aktiv)",
                counterpartyName = "Hetzner Online GmbH & JetBrains s.r.o.",
                counterpartyIban = "DE66760100850123456850",
                category = KlartextEurCategory.EXPENSE_IT_CLOUD_SOFTWARE,
                invoiceOrDueDateIso = "2026-12-28",
                paymentDateIso = "2027-01-05",
                isRecurringPayment = true,
                appliedTenDayRule = true,
                effectiveTaxYear = 2026,
                netAmountCents = 1_480_00L,
                vatRatePercent = 19,
                vatAmountCents = 281_20L,
                grossAmountCents = 1_761_20L,
                deductibleNetCents = 1_480_00L,
                nonDeductibleNetCents = 0L,
                hospitalityHost = null,
                hospitalityGuests = null,
                hospitalityOccasion = null,
                taxFreeAllowanceType = null,
                attachmentFileName = "Hetzner_Cloud_Dezember2026_ZUGFeRD.pdf",
                attachmentMimeType = "application/pdf",
                attachmentUri = "file:///klartax_vault/attachments/Hetzner_Cloud_Dezember2026_ZUGFeRD.pdf",
                attachmentSizeBytes = 148_200L,
                isLocked = false,
                isStornoRecord = false,
                stornoOfBookingId = null,
                gobdRecordHash = h5,
                updatedAtEpochMillis = now - 10_000L
            )
        )

        p1Bookings.forEach { dao.internalInsertOrUpdateUnlockedBooking(it) }

        val audits = listOf(
            AuditLogEntity(
                id = "AUD-1",
                profileId = PROFILE_1_ID,
                sequenceNumber = 1L,
                previousHash = GoBdHashChain.GENESIS_HASH,
                entryHash = h1,
                entityType = "EUR_BOOKING",
                entityId = "EUR-2026-001",
                actionType = "LOCK_BOOKING",
                summaryDe = "Festschreibung RE-2026-001 (33.915,00 € brutto)",
                payloadJson = payload1,
                createdAtEpochMillis = ts1
            ),
            AuditLogEntity(
                id = "AUD-2",
                profileId = PROFILE_1_ID,
                sequenceNumber = 2L,
                previousHash = h1,
                entryHash = h2,
                entityType = "EUR_BOOKING",
                entityId = "EUR-2026-002",
                actionType = "LOCK_BOOKING",
                summaryDe = "Festschreibung RE-2026-002 (22.848,00 € brutto)",
                payloadJson = payload2,
                createdAtEpochMillis = ts2
            ),
            AuditLogEntity(
                id = "AUD-3",
                profileId = PROFILE_1_ID,
                sequenceNumber = 3L,
                previousHash = h2,
                entryHash = h3,
                entityType = "EUR_BOOKING",
                entityId = "EUR-2026-003",
                actionType = "LOCK_BOOKING",
                summaryDe = "Festschreibung RE-2026-003 (2.800,00 € § 3 Nr. 26 EStG)",
                payloadJson = payload3,
                createdAtEpochMillis = ts3
            ),
            AuditLogEntity(
                id = "AUD-4",
                profileId = PROFILE_1_ID,
                sequenceNumber = 4L,
                previousHash = h3,
                entryHash = h4,
                entityType = "EUR_BOOKING",
                entityId = "EUR-2026-004",
                actionType = "LOCK_BOOKING",
                summaryDe = "Festschreibung Digital-AfA Googlebook Pro 16\" (ER-2026-104)",
                payloadJson = payload4,
                createdAtEpochMillis = ts4
            ),
            AuditLogEntity(
                id = "AUD-5",
                profileId = PROFILE_1_ID,
                sequenceNumber = 5L,
                previousHash = h4,
                entryHash = h5,
                entityType = "EUR_BOOKING",
                entityId = "EUR-2026-005",
                actionType = "LOCK_BOOKING",
                summaryDe = "Festschreibung Bewirtungsbeleg BW-2026-019 (70/30 Aufteilung)",
                payloadJson = payload5,
                createdAtEpochMillis = ts5
            )
        )
        audits.forEach { dao.insertAuditLog(it) }

        // =========================================================================
        // 2B. PRODUKTE & DIENSTLEISTUNGEN KATALOG (SEVDESK ARTIKELVERWALTUNG)
        // =========================================================================
        val catalogItems = listOf(
            ServiceCatalogItemEntity(
                id = "CAT-101",
                profileId = PROFILE_1_ID,
                articleNumber = "DL-101",
                itemType = "SERVICE",
                titleDe = "Principal FinTech-Architektur & Kotlin Multiplatform Engineering",
                descriptionDe = "Konzeption, Clean Architecture, SQLCipher-Verschlüsselung & Material 3 Adaptive UI",
                unitCode = "HUR",
                unitPriceNetCents = 150_00L,
                vatRatePercent = 19,
                defaultQuantity = "40.00",
                updatedAtEpochMillis = now
            ),
            ServiceCatalogItemEntity(
                id = "CAT-102",
                profileId = PROFILE_1_ID,
                articleNumber = "DL-102",
                itemType = "SERVICE",
                titleDe = "EN 16931 E-Rechnung (ZUGFeRD 2.3 & XRechnung 3.0) Audit & Integration",
                descriptionDe = "Implementierung von CII-XML, PDF/A-3 Einbettung (factur-x.xml) und Leitweg-ID-Prüfung",
                unitCode = "DAY",
                unitPriceNetCents = 1_280_00L,
                vatRatePercent = 19,
                defaultQuantity = "3.00",
                updatedAtEpochMillis = now
            ),
            ServiceCatalogItemEntity(
                id = "CAT-103",
                profileId = PROFILE_1_ID,
                articleNumber = "DL-103",
                itemType = "SERVICE",
                titleDe = "Material 3 Expressive Design-System & ChromeOS Large-Screen Optimierung",
                descriptionDe = "Pauschalpaket inkl. 3-Pane Scaffolds, Tastatur-Shortcuts und Drag & Drop",
                unitCode = "C62",
                unitPriceNetCents = 3_600_00L,
                vatRatePercent = 19,
                defaultQuantity = "1.00",
                updatedAtEpochMillis = now
            ),
            ServiceCatalogItemEntity(
                id = "CAT-201",
                profileId = PROFILE_1_ID,
                articleNumber = "PR-201",
                itemType = "PRODUCT",
                titleDe = "Klartax Enterprise UI-Kit & Design-Token Jahreslizenz",
                descriptionDe = "Nutzungsrecht für ein Unternehmensprojekt inkl. Wartungs-Updates",
                unitCode = "C62",
                unitPriceNetCents = 2_400_00L,
                vatRatePercent = 19,
                defaultQuantity = "1.00",
                updatedAtEpochMillis = now
            ),
            ServiceCatalogItemEntity(
                id = "CAT-104",
                profileId = PROFILE_1_ID,
                articleNumber = "DL-104",
                itemType = "SERVICE",
                titleDe = "Hochschul-Lehrauftrag UX-Engineering (§ 3 Nr. 26 EStG)",
                descriptionDe = "Vorlesung & Praktikumsbetreuung an der Technischen Hochschule (umsatzsteuerbefreit)",
                unitCode = "HUR",
                unitPriceNetCents = 100_00L,
                vatRatePercent = 0,
                defaultQuantity = "28.00",
                updatedAtEpochMillis = now
            )
        )
        dao.upsertServiceCatalogItems(catalogItems)

        // =========================================================================
        // 2C. STANDARDTEXTE & TEXTBAUSTEINE (KOPFTEXTE & ZAHLUNGSBEDINGUNGEN)
        // =========================================================================
        val textTemplates = listOf(
            DocumentTextTemplateEntity(
                id = "TPL-QUOTE-HEAD",
                profileId = PROFILE_1_ID,
                templateTitleDe = "Angebot: Freundliche Einleitung (Standard)",
                documentScope = "QUOTE",
                placementType = "HEADER_INTRO",
                bodyTextDe = "Sehr geehrte Damen und Herren, vielen Dank für Ihre Anfrage und das angenehme Fachgespräch. Gerne unterbreite ich Ihnen folgendes Angebot für die besprochenen Architektur- und Entwicklungsleistungen:",
                isDefault = true,
                updatedAtEpochMillis = now
            ),
            DocumentTextTemplateEntity(
                id = "TPL-QUOTE-FOOT",
                profileId = PROFILE_1_ID,
                templateTitleDe = "Angebot: Bindefrist 30 Tage & Beauftragung",
                documentScope = "QUOTE",
                placementType = "FOOTER_CLOSING",
                bodyTextDe = "Dieses Angebot ist 30 Tage ab Ausstellungsdatum gültig. Nach Ihrer Freigabe erhalten Sie umgehend eine verbindliche Auftragsbestätigung. Ich freue mich auf die Zusammenarbeit!",
                isDefault = true,
                updatedAtEpochMillis = now
            ),
            DocumentTextTemplateEntity(
                id = "TPL-ORDER-HEAD",
                profileId = PROFILE_1_ID,
                templateTitleDe = "Auftragsbestätigung: Verbindliche Annahme",
                documentScope = "ORDER_CONFIRMATION",
                placementType = "HEADER_INTRO",
                bodyTextDe = "Sehr geehrte Damen und Herren, herzlichen Dank für Ihre Beauftragung. Hiermit bestätige ich Ihnen verbindlich die Durchführung des Projektauftrags gemäß unserem Angebot zu folgenden Positionen:",
                isDefault = true,
                updatedAtEpochMillis = now
            ),
            DocumentTextTemplateEntity(
                id = "TPL-ORDER-FOOT",
                profileId = PROFILE_1_ID,
                templateTitleDe = "Auftragsbestätigung: Projektstart & E-Rechnung",
                documentScope = "ORDER_CONFIRMATION",
                placementType = "FOOTER_CLOSING",
                bodyTextDe = "Die Leistungserbringung erfolgt im vereinbarten Sprint-Zeitraum. Nach Projektabschluss erfolgt die Abrechnung revisionssicher als EN 16931 E-Rechnung (ZUGFeRD 2.3 / XRechnung 3.0).",
                isDefault = true,
                updatedAtEpochMillis = now
            ),
            DocumentTextTemplateEntity(
                id = "TPL-INV-HEAD-1",
                profileId = PROFILE_1_ID,
                templateTitleDe = "Rechnung: Standard-Kopftext Honorarabrechnung",
                documentScope = "INVOICE",
                placementType = "HEADER_INTRO",
                bodyTextDe = "Sehr geehrte Damen und Herren, für die im Leistungszeitraum erbrachten Beratungs-, Architektur- und Entwicklungsleistungen erlaube ich mir, gemäß unserem Auftrag wie folgt abzurechnen:",
                isDefault = true,
                updatedAtEpochMillis = now
            ),
            DocumentTextTemplateEntity(
                id = "TPL-INV-FOOT-1",
                profileId = PROFILE_1_ID,
                templateTitleDe = "Rechnung: Zahlungsziel ohne Abzug + GiroCode",
                documentScope = "INVOICE",
                placementType = "FOOTER_CLOSING",
                bodyTextDe = "Bitte überweisen Sie den Rechnungsbetrag innerhalb des vereinbarten Zahlungsziels ohne Abzug unter Angabe der Rechnungsnummer auf das unten genannte Konto (oder bequem per EPC-GiroCode QR-Scan). Vielen Dank für die sehr gute Zusammenarbeit!",
                isDefault = true,
                updatedAtEpochMillis = now
            ),
            DocumentTextTemplateEntity(
                id = "TPL-INV-FOOT-2",
                profileId = PROFILE_1_ID,
                templateTitleDe = "Rechnung: 2 % Skonto bei Zahlung binnen 7 Tagen",
                documentScope = "INVOICE",
                placementType = "FOOTER_CLOSING",
                bodyTextDe = "Zahlbar innerhalb von 14 Tagen ohne Abzug. Bei Ausgleich innerhalb von 7 Tagen gewähren wir 2 % Skonto auf den Rechnungsbetrag. Die Leistung entspricht dem beigefügten Leistungsnachweis.",
                isDefault = false,
                updatedAtEpochMillis = now
            )
        )
        dao.upsertDocumentTextTemplates(textTemplates)

        // =========================================================================
        // 2D. BRIEFPAPIER & FIRMENLOGO (SEVDESK BRIEFPAPIER-DESIGNER DIN 5008)
        // =========================================================================
        dao.upsertStationeryConfig(
            StationeryConfigEntity(
                id = "STAT-P1",
                profileId = PROFILE_1_ID,
                companyDisplayName = "Lukas Weber · Studio für FinTech-Architektur & Adaptive UI",
                companySubtitleOrClaim = "Principal Software Engineering · EN 16931 & GoBD Advisory · München",
                logoMonogramText = "LW",
                logoFileName = "Weber_Studio_Markenlogo_2026.svg",
                logoImageUri = "file:///klartax_vault/branding/Weber_Studio_Markenlogo_2026.svg",
                primaryAccentHex = "#7A1624",
                secondaryAccentHex = "#1E3A8A",
                senderWindowLineDe = "Lukas Weber · Leopoldstraße 88 · 80802 München",
                footerCol1AddressDe = "Lukas Weber\nLeopoldstraße 88\n80802 München\nlukas.weber@klartax-studio.de",
                footerCol2BankDe = "Commerzbank München\nIBAN: DE89 7008 0000 0512 3456 00\nBIC: DRESDEFF700",
                footerCol3TaxDe = "Finanzamt München\nSteuernummer: 143/815/08155\nUSt-IdNr.: DE314159265",
                showDin5008FoldMarks = true,
                showEpcGiroCodeQr = true,
                updatedAtEpochMillis = now
            )
        )

        // =========================================================================
        // 2E. DURCHGÄNGIGE BELEGKETTE: ANGEBOT (AN) -> AUFTRAG (AB) -> E-RECHNUNG (RE)
        // =========================================================================
        val itemsInv1 = listOf(
            InvoiceLineItem(
                positionNumber = 1,
                title = "Material 3 Expressive Design-System & ChromeOS Architektur",
                description = "Konzeption, Token-Architektur und barrierefreie Komponentenbibliothek",
                quantityHoursOrUnits = "190.00",
                unitCode = "HUR",
                unitPriceNetCents = 150_00L,
                vatRatePercent = 19,
                lineNetTotalCents = 28_500_00L,
                articleNumber = "DL-101"
            )
        )

        // 1. Angenommenes Angebot AN-2026-001 (verknüpft mit AB-2026-001 & RE-2026-001)
        val quote1 = InvoiceEntity(
            id = "QUOTE-2026-001",
            profileId = PROFILE_1_ID,
            customerId = "CUST-1",
            invoiceNumber = "AN-2026-001",
            documentType = "QUOTE",
            eInvoiceStandard = "ZUGFERD_2_3",
            taxMode = "STANDARD_19",
            customerName = "Süddeutsche FinTech Solutions AG",
            customerStreet = "Maximilianstraße 42",
            customerPostalCode = "80539",
            customerCity = "München",
            customerCountryIso = "DE",
            customerVatId = "DE129485712",
            buyerReferenceLeitwegId = "FINTECH-MUC-2026-Q2",
            issueDateIso = "2026-01-02",
            servicePeriodStartIso = "2026-01-05",
            servicePeriodEndIso = "2026-04-14",
            dueDateIso = "2026-02-01",
            paidDateIso = null,
            netTotalCents = 28_500_00L,
            vatTotalCents = 5_415_00L,
            grossTotalCents = 33_915_00L,
            status = "QUOTE_ACCEPTED",
            dunningLevel = 0,
            sellerIban = "DE89700800000512345600",
            sellerBic = "DRESDEFF700",
            lineItemsJson = json.encodeToString(itemsInv1),
            embeddedXmlPayload = "",
            isLocked = false,
            stornoOfInvoiceId = null,
            linkedQuoteId = null,
            linkedQuoteNumber = null,
            linkedOrderId = "ORDER-2026-001",
            linkedOrderNumber = "AB-2026-001",
            subjectTitleDe = "Angebot AN-2026-001: Material 3 Expressive Design-System & ChromeOS Architektur",
            headerIntroTextDe = textTemplates[0].bodyTextDe,
            footerClosingTextDe = textTemplates[1].bodyTextDe,
            gobdRecordHash = GoBdHashChain.GENESIS_HASH,
            updatedAtEpochMillis = now - 95_000L
        )

        // 2. Auftragsbestätigung AB-2026-001 (erzeugt aus AN-2026-001, abgerechnet in RE-2026-001)
        val order1 = quote1.copy(
            id = "ORDER-2026-001",
            invoiceNumber = "AB-2026-001",
            documentType = "ORDER_CONFIRMATION",
            issueDateIso = "2026-01-04",
            status = "ORDER_BILLED",
            linkedQuoteId = "QUOTE-2026-001",
            linkedQuoteNumber = "AN-2026-001",
            linkedOrderId = null,
            linkedOrderNumber = null,
            subjectTitleDe = "Auftragsbestätigung AB-2026-001 (zu Angebot AN-2026-001)",
            headerIntroTextDe = textTemplates[2].bodyTextDe,
            footerClosingTextDe = textTemplates[3].bodyTextDe,
            updatedAtEpochMillis = now - 90_000L
        )

        // 3. Festgeschriebene & bezahlte E-Rechnung RE-2026-001 (verknüpft mit AN-2026-001 & AB-2026-001)
        val draftInv1 = InvoiceEntity(
            id = "INV-2026-001",
            profileId = PROFILE_1_ID,
            customerId = "CUST-1",
            invoiceNumber = "RE-2026-001",
            documentType = "INVOICE",
            eInvoiceStandard = "ZUGFERD_2_3",
            taxMode = "STANDARD_19",
            customerName = "Süddeutsche FinTech Solutions AG",
            customerStreet = "Maximilianstraße 42",
            customerPostalCode = "80539",
            customerCity = "München",
            customerCountryIso = "DE",
            customerVatId = "DE129485712",
            buyerReferenceLeitwegId = "FINTECH-MUC-2026-Q2",
            issueDateIso = "2026-04-15",
            servicePeriodStartIso = "2026-01-05",
            servicePeriodEndIso = "2026-04-14",
            dueDateIso = "2026-04-29",
            paidDateIso = "2026-04-24",
            netTotalCents = 28_500_00L,
            vatTotalCents = 5_415_00L,
            grossTotalCents = 33_915_00L,
            status = "PAID",
            dunningLevel = 0,
            sellerIban = "DE89700800000512345600",
            sellerBic = "DRESDEFF700",
            lineItemsJson = json.encodeToString(itemsInv1),
            embeddedXmlPayload = "",
            isLocked = true,
            stornoOfInvoiceId = null,
            linkedQuoteId = "QUOTE-2026-001",
            linkedQuoteNumber = "AN-2026-001",
            linkedOrderId = "ORDER-2026-001",
            linkedOrderNumber = "AB-2026-001",
            subjectTitleDe = "Rechnung RE-2026-001 (Auftrag AB-2026-001 / Angebot AN-2026-001)",
            headerIntroTextDe = textTemplates[4].bodyTextDe,
            footerClosingTextDe = textTemplates[5].bodyTextDe,
            gobdRecordHash = h1,
            updatedAtEpochMillis = now - 80_000L
        )
        val inv1WithXml = draftInv1.copy(
            embeddedXmlPayload = eInvoiceEngine.generateEn16931CiiXml(profile1, draftInv1, itemsInv1)
        )

        val itemsInv2 = listOf(
            InvoiceLineItem(
                positionNumber = 1,
                title = "XRechnung & Zero-Knowledge Cloud-Sync Spezifikation",
                description = "Beratung und Sicherheits-Audit nach BSI TR-02102",
                quantityHoursOrUnits = "120.00",
                unitCode = "HUR",
                unitPriceNetCents = 160_00L,
                vatRatePercent = 19,
                lineNetTotalCents = 19_200_00L,
                articleNumber = "DL-102"
            )
        )
        val draftInv2 = InvoiceEntity(
            id = "INV-2026-002",
            profileId = PROFILE_1_ID,
            customerId = "CUST-2",
            invoiceNumber = "RE-2026-002",
            documentType = "INVOICE",
            eInvoiceStandard = "XRECHNUNG_3_0_CII",
            taxMode = "STANDARD_19",
            customerName = "Bundesanstalt für Digitale Verwaltung",
            customerStreet = "Friedrichstraße 108",
            customerPostalCode = "10117",
            customerCity = "Berlin",
            customerCountryIso = "DE",
            customerVatId = "DE998877665",
            buyerReferenceLeitwegId = "991-12345-67",
            issueDateIso = "2026-08-30",
            servicePeriodStartIso = "2026-06-01",
            servicePeriodEndIso = "2026-08-29",
            dueDateIso = "2026-09-15",
            paidDateIso = "2026-09-10",
            netTotalCents = 19_200_00L,
            vatTotalCents = 3_648_00L,
            grossTotalCents = 22_848_00L,
            status = "PAID",
            dunningLevel = 0,
            sellerIban = "DE89700800000512345600",
            sellerBic = "DRESDEFF700",
            lineItemsJson = json.encodeToString(itemsInv2),
            embeddedXmlPayload = "",
            isLocked = true,
            stornoOfInvoiceId = null,
            linkedQuoteId = null,
            linkedQuoteNumber = "AN-2026-000",
            linkedOrderId = null,
            linkedOrderNumber = "AB-2026-000",
            subjectTitleDe = "XRechnung RE-2026-002 (Leitweg-ID 991-12345-67)",
            headerIntroTextDe = textTemplates[4].bodyTextDe,
            footerClosingTextDe = textTemplates[5].bodyTextDe,
            gobdRecordHash = h2,
            updatedAtEpochMillis = now - 60_000L
        )
        val inv2WithXml = draftInv2.copy(
            embeddedXmlPayload = eInvoiceEngine.generateEn16931CiiXml(profile1, draftInv2, itemsInv2)
        )

        // Offenes Angebot AN-2026-002 & bestätigter Auftrag AB-2026-002 zum sofortigen 1-Klick-Umwandeln
        val itemsQuote2 = listOf(
            InvoiceLineItem(
                positionNumber = 1,
                title = "Material 3 Expressive Design-System & ChromeOS Large-Screen Optimierung",
                description = "Pauschalpaket inkl. 3-Pane Scaffolds, Tastatur-Shortcuts und Drag & Drop",
                quantityHoursOrUnits = "1.00",
                unitCode = "C62",
                unitPriceNetCents = 3_600_00L,
                vatRatePercent = 19,
                lineNetTotalCents = 3_600_00L,
                articleNumber = "DL-103"
            ),
            InvoiceLineItem(
                positionNumber = 2,
                title = "EN 16931 E-Rechnung (ZUGFeRD 2.3 & XRechnung 3.0) Audit & Integration",
                description = "3 Manntage Vor-Ort-/Remote-Integration",
                quantityHoursOrUnits = "3.00",
                unitCode = "DAY",
                unitPriceNetCents = 1_280_00L,
                vatRatePercent = 19,
                lineNetTotalCents = 3_840_00L,
                articleNumber = "DL-102"
            )
        )
        val openQuote2 = InvoiceEntity(
            id = "QUOTE-2026-002",
            profileId = PROFILE_1_ID,
            customerId = "CUST-1",
            invoiceNumber = "AN-2026-002",
            documentType = "QUOTE",
            eInvoiceStandard = "ZUGFERD_2_3",
            taxMode = "STANDARD_19",
            customerName = "Süddeutsche FinTech Solutions AG",
            customerStreet = "Maximilianstraße 42",
            customerPostalCode = "80539",
            customerCity = "München",
            customerCountryIso = "DE",
            customerVatId = "DE129485712",
            buyerReferenceLeitwegId = "FINTECH-MUC-2026-Q4",
            issueDateIso = "2026-11-02",
            servicePeriodStartIso = "2026-11-10",
            servicePeriodEndIso = "2026-12-15",
            dueDateIso = "2026-12-02",
            paidDateIso = null,
            netTotalCents = 7_440_00L,
            vatTotalCents = 1_413_60L,
            grossTotalCents = 8_853_60L,
            status = "QUOTE_OPEN",
            dunningLevel = 0,
            sellerIban = "DE89700800000512345600",
            sellerBic = "DRESDEFF700",
            lineItemsJson = json.encodeToString(itemsQuote2),
            embeddedXmlPayload = "",
            isLocked = false,
            stornoOfInvoiceId = null,
            linkedQuoteId = null,
            linkedQuoteNumber = null,
            linkedOrderId = null,
            linkedOrderNumber = null,
            subjectTitleDe = "Angebot AN-2026-002: Q4 Release-Ausbau & E-Rechnungs-Kern",
            headerIntroTextDe = textTemplates[0].bodyTextDe,
            footerClosingTextDe = textTemplates[1].bodyTextDe,
            gobdRecordHash = GoBdHashChain.GENESIS_HASH,
            updatedAtEpochMillis = now - 5_000L
        )

        dao.internalUpsertInvoice(quote1)
        dao.internalUpsertInvoice(order1)
        dao.internalUpsertInvoice(inv1WithXml)
        dao.internalUpsertInvoice(inv2WithXml)
        dao.internalUpsertInvoice(openQuote2)

        // Zeiterfassung (Stoppuhr & Stundenzettel)
        dao.upsertTimeTrackingEntries(
            listOf(
                TimeTrackingEntryEntity(
                    id = "TIME-1",
                    profileId = PROFILE_1_ID,
                    projectName = "Googlebook Adaptive FinTech UI",
                    clientName = "Süddeutsche FinTech Solutions AG",
                    taskDescription = "3-Pane ListDetailPaneScaffold & Tastatur-Shortcuts (Strg+N, Strg+F)",
                    dateIso = "2026-11-04",
                    durationMinutes = 390,
                    hourlyRateNetCents = 150_00L,
                    isBilled = false,
                    linkedInvoiceId = null,
                    updatedAtEpochMillis = now
                ),
                TimeTrackingEntryEntity(
                    id = "TIME-2",
                    profileId = PROFILE_1_ID,
                    projectName = "EN 16931 ZUGFeRD 2.3 Audit",
                    clientName = "Bundesanstalt für Digitale Verwaltung",
                    taskDescription = "Validierung der CII-XML-Schemata und Leitweg-ID-Prüfung",
                    dateIso = "2026-11-06",
                    durationMinutes = 240,
                    hourlyRateNetCents = 160_00L,
                    isBilled = false,
                    linkedInvoiceId = null,
                    updatedAtEpochMillis = now
                )
            )
        )

        // Anlagenverzeichnis & AfA (Profil 1)
        dao.upsertAssets(
            listOf(
                AssetEntity(
                    id = "ASSET-1",
                    profileId = PROFILE_1_ID,
                    inventoryNumber = "INV-DIG-2026-01",
                    assetNameDe = "Googlebook Pro 16\" (32 GB RAM) + 4K USB-C Studio Display",
                    categoryType = "DIGITAL_AFA_1Y",
                    purchaseDateIso = "2026-02-12",
                    acquisitionCostNetCents = 2_400_00L,
                    vatPaidCents = 456_00L,
                    usefulLifeYears = 1,
                    privateUsePercent = 10,
                    currentYearDepreciationCents = 2_400_00L,
                    deductibleBusinessDepreciationCents = 2_160_00L,
                    remainingBookValueCents = 0L,
                    updatedAtEpochMillis = now
                ),
                AssetEntity(
                    id = "ASSET-2",
                    profileId = PROFILE_1_ID,
                    inventoryNumber = "INV-GWG-2026-02",
                    assetNameDe = "Ergonomischer Bürostuhl Herman Miller Aeron (GWG Sofortabschreibung)",
                    categoryType = "GWG_800",
                    purchaseDateIso = "2026-03-19",
                    acquisitionCostNetCents = 780_00L,
                    vatPaidCents = 148_20L,
                    usefulLifeYears = 1,
                    privateUsePercent = 0,
                    currentYearDepreciationCents = 780_00L,
                    deductibleBusinessDepreciationCents = 780_00L,
                    remainingBookValueCents = 0L,
                    updatedAtEpochMillis = now
                )
            )
        )

        dao.upsertVehicleLog(
            VehicleLogEntity(
                id = "VEH-1",
                profileId = PROFILE_1_ID,
                vehicleModelDe = "Polestar 2 Long Range (Vollelektrisch · 0,25-%-Privileg)",
                grossListPriceNewCents = 54_800_00L,
                isElectricVehicle = true,
                totalAnnualCostNetCents = 7_950_00L,
                totalDrivenKm = 18_000,
                businessDrivenKm = 12_600,
                commuteDistanceOneWayKm = 0,
                commuteDaysPerYear = 0,
                preferredMethod = "AUTO_OPTIMAL",
                updatedAtEpochMillis = now
            )
        )

        dao.upsertBankTransactions(
            listOf(
                BankTransactionEntity(
                    id = "BANK-1",
                    profileId = PROFILE_1_ID,
                    bookingDateIso = "2026-04-24",
                    valutaDateIso = "2026-04-24",
                    counterpartyName = "Süddeutsche FinTech Solutions AG",
                    counterpartyIban = "DE44700202700015829400",
                    purposeText = "Ausgleich ZUGFeRD Rechnung RE-2026-001 Kundenref FINTECH-MUC-2026-Q2",
                    amountCents = 33_915_00L,
                    importSourceFormat = "CAMT_053",
                    matchedEntityType = "INVOICE",
                    matchedEntityId = "INV-2026-001",
                    matchConfidencePercent = 100,
                    updatedAtEpochMillis = now
                ),
                BankTransactionEntity(
                    id = "BANK-2",
                    profileId = PROFILE_1_ID,
                    bookingDateIso = "2026-09-10",
                    valutaDateIso = "2026-09-10",
                    counterpartyName = "Bundeskasse - Bundesanstalt Digitale Verwaltung",
                    counterpartyIban = "DE12100000000012345678",
                    purposeText = "XRechnung RE-2026-002 Leitweg-ID 991-12345-67",
                    amountCents = 22_848_00L,
                    importSourceFormat = "CAMT_053",
                    matchedEntityType = "INVOICE",
                    matchedEntityId = "INV-2026-002",
                    matchConfidencePercent = 100,
                    updatedAtEpochMillis = now
                )
            )
        )

        // =========================================================================
        // 3. PROFIL 1: MODUL B (WISO EINKOMMENSTEUER)
        // =========================================================================
        dao.upsertEmploymentTax(
            listOf(
                EmploymentTaxEntity(
                    id = "EMP-P1-SPOUSE",
                    profileId = PROFILE_1_ID,
                    personRole = "SPOUSE",
                    employerName = "Allianz Technology SE München",
                    taxClass = 4,
                    grossSalaryCents = 64_500_00L,
                    withheldIncomeTaxCents = 12_840_00L,
                    withheldSoliCents = 0L,
                    withheldChurchTaxCents = 1_027_20L,
                    commuteDistanceOneWayKm = 28,
                    commuteWorkdaysPerYear = 95,
                    publicTransportAnnualCostCents = 588_00L,
                    homeOfficeDays = 135,
                    dedicatedHomeOfficeRoomCostCents = 0L,
                    workEquipmentCostCents = 420_00L,
                    trainingAndEducationCostCents = 690_00L,
                    applicationCostCents = 0L,
                    travelMealAllowanceCents = 168_00L,
                    accountMaintenanceFeeCents = 16_00L,
                    updatedAtEpochMillis = now
                )
            )
        )

        dao.upsertChildren(
            listOf(
                ChildTaxEntity(
                    id = "CHILD-P1-1",
                    profileId = PROFILE_1_ID,
                    childFirstName = "Emilia",
                    birthDateIso = "2022-06-14",
                    kindergeldReceivedAnnualCents = 3_060_00L,
                    childcareExpenseTotalCents = 4_200_00L,
                    privateSchoolFeeEligibleCents = 0L,
                    isSingleParentHousehold = false,
                    isAdultChildInExternalTraining = false,
                    updatedAtEpochMillis = now
                )
            )
        )

        dao.upsertCapitalAndCrypto(
            listOf(
                CapitalAndCryptoEntity(
                    id = "KAP-P1-1",
                    profileId = PROFILE_1_ID,
                    depotBankName = "Scalable Capital & ING DiBa Gemeinschaftsdepot",
                    capitalGainsGrossCents = 3_150_00L,
                    etfVorabpauschaleIncludedCents = 480_00L,
                    sparerPauschbetragUsedAtBankCents = 2_000_00L,
                    withheldKapEstCents = 287_50L,
                    withheldKapSoliCents = 15_81L,
                    withheldKapChurchTaxCents = 23_00L,
                    stockLossPotCents = 0L,
                    cryptoAssetName = "Bitcoin (BTC) – Haltedauer > 1 Jahr (§ 23 EStG steuerfrei)",
                    cryptoPurchaseDateIso = "2024-03-10",
                    cryptoSaleDateIso = "2026-08-19",
                    cryptoGainOrLossCents = 4_600_00L,
                    updatedAtEpochMillis = now
                )
            )
        )

        dao.upsertSpecialExpensesAndBurdens(
            listOf(
                SpecialExpenseAndBurdenEntity(
                    id = "SPEC-P1-1",
                    profileId = PROFILE_1_ID,
                    recordCategory = "NEBENKOSTENABRECHNUNG_SEC35A",
                    titleDe = "Nebenkostenabrechnung (§ 35a EStG): Hausmeister, Treppenhausreinigung, Gartenpflege & Winterdienst",
                    providerOrRecipientName = "Hausverwaltung Isar-Wohnen GmbH",
                    invoiceDateIso = "2026-05-12",
                    paymentMethodBankTransfer = true,
                    totalInvoiceGrossCents = 3_480_00L,
                    eligibleLaborAndMachineCents = 1_640_00L,
                    disabilityDegreeGdB = null,
                    updatedAtEpochMillis = now
                ),
                SpecialExpenseAndBurdenEntity(
                    id = "SPEC-P1-2",
                    profileId = PROFILE_1_ID,
                    recordCategory = "CRAFTSMAN_HANDWERKER_SEC35A",
                    titleDe = "Handwerkerrechnung (§ 35a Abs. 3 EStG): Austausch Heizungspumpe & Schornsteinfeger",
                    providerOrRecipientName = "Meisterbetrieb AlpenWärme GmbH",
                    invoiceDateIso = "2026-09-22",
                    paymentMethodBankTransfer = true,
                    totalInvoiceGrossCents = 1_450_00L,
                    eligibleLaborAndMachineCents = 920_00L,
                    disabilityDegreeGdB = null,
                    updatedAtEpochMillis = now
                ),
                SpecialExpenseAndBurdenEntity(
                    id = "SPEC-P1-3",
                    profileId = PROFILE_1_ID,
                    recordCategory = "DONATION_SPENDE",
                    titleDe = "Spendenbescheinigung (§ 10b EStG): Förderverein Kinderklinik & Wikipedia",
                    providerOrRecipientName = "Stiftung Kinderhilfe München e.V.",
                    invoiceDateIso = "2026-11-01",
                    paymentMethodBankTransfer = true,
                    totalInvoiceGrossCents = 650_00L,
                    eligibleLaborAndMachineCents = 650_00L,
                    disabilityDegreeGdB = null,
                    updatedAtEpochMillis = now
                )
            )
        )

        dao.upsertPensionAndInsurance(
            PensionAndInsuranceEntity(
                id = "PENS-P1",
                profileId = PROFILE_1_ID,
                statutoryPensionOrVersorgungswerkEmployeeCents = 6_000_00L,
                employerStatutoryPensionContributionCents = 6_000_00L,
                ruerupBasisrenteContributionsCents = 4_800_00L,
                healthInsuranceBasicTariffCents = 7_420_00L,
                nursingCareInsuranceCents = 1_680_00L,
                unemploymentAndOtherInsuranceCents = 940_00L,
                riesterContributionsCents = 2_100_00L,
                riesterStateAllowancesCents = 475_00L,
                statutoryPensionReceivedGrossCents = 0L,
                pensionTaxableSharePercent = 0,
                updatedAtEpochMillis = now
            )
        )

        dao.upsertTaxAssessmentNotices(
            listOf(
                TaxAssessmentNoticeEntity(
                    id = "NOTICE-P1",
                    profileId = PROFILE_1_ID,
                    taxOfficeName = "Finanzamt München – Abteilung Veranlagung",
                    noticeDateIso = "2026-10-28",
                    klartaxCalculatedRefundCents = 8_074_56L,
                    finanzamtAssessedRefundCents = 7_470_56L,
                    deviationPointTitleDe = "Kürzung der Digital-AfA für das Googlebook Pro 16\" auf 3 Jahre statt 1 Jahr Sofortabschreibung",
                    finanzamtExplanationDe = "Die Anschaffungskosten für das Notebook wurden auf eine betriebsgewöhnliche Nutzungsdauer von 3 Jahren verteilt.",
                    legalCounterArgumentDe = "Gemäß BMF-Schreiben vom 22.02.2022 (IV C 3 - S 2190/21/10002 :025) kann für Computerhardware (einschließlich Notebooks/Googlebooks und Peripheriegeräte) eine betriebsgewöhnliche Nutzungsdauer von 1 Jahr zugrunde gelegt werden. Die betrieblichen Anschaffungskosten in Höhe von 2.160,00 € sind daher im Jahr der Anschaffung in voller Höhe als Betriebsausgabe nach § 7 Abs. 1 EStG abziehbar.",
                    updatedAtEpochMillis = now
                )
            )
        )

        // =========================================================================
        // 4. PROFIL 2 ("Rein Privat: Arbeitnehmer & Vermieter")
        // =========================================================================
        dao.upsertEmploymentTax(
            listOf(
                EmploymentTaxEntity(
                    id = "EMP-P2-PRIMARY",
                    profileId = PROFILE_2_ID,
                    personRole = "PRIMARY",
                    employerName = "RheinEnergie Technik & Leitungsbau GmbH Köln",
                    taxClass = 1,
                    grossSalaryCents = 78_400_00L,
                    withheldIncomeTaxCents = 17_950_00L,
                    withheldSoliCents = 0L,
                    withheldChurchTaxCents = 1_615_50L,
                    commuteDistanceOneWayKm = 36,
                    commuteWorkdaysPerYear = 180,
                    publicTransportAnnualCostCents = 588_00L,
                    homeOfficeDays = 50,
                    dedicatedHomeOfficeRoomCostCents = 0L,
                    workEquipmentCostCents = 890_00L,
                    trainingAndEducationCostCents = 1_450_00L,
                    applicationCostCents = 0L,
                    travelMealAllowanceCents = 280_00L,
                    accountMaintenanceFeeCents = 16_00L,
                    updatedAtEpochMillis = now
                )
            )
        )

        dao.upsertRentalProperties(
            listOf(
                RentalPropertyEntity(
                    id = "RENT-P2-1",
                    profileId = PROFILE_2_ID,
                    propertyAddressDe = "Venloer Straße 214, 50823 Köln-Ehrenfeld (3-Zimmer-ETW, 74 m²)",
                    constructionYear = 1998,
                    buildingAcquisitionCostCents = 265_000_00L,
                    afaRateBasisPoints = 200,
                    annualColdRentReceivedCents = 11_400_00L,
                    annualAncillaryCostsReceivedCents = 2_640_00L,
                    mortgageInterestPaidCents = 6_180_00L,
                    allocablePropertyCostsCents = 2_890_00L,
                    maintenanceTotalCents = 4_400_00L,
                    maintenanceSpreadYearsSec82b = 1,
                    updatedAtEpochMillis = now
                )
            )
        )

        dao.upsertSpecialExpensesAndBurdens(
            listOf(
                SpecialExpenseAndBurdenEntity(
                    id = "SPEC-P2-1",
                    profileId = PROFILE_2_ID,
                    recordCategory = "CRAFTSMAN_HANDWERKER_SEC35A",
                    titleDe = "Handwerkerrechnung Eigengenutzte Wohnung (§ 35a EStG): Malerarbeiten & Parkettschleifen",
                    providerOrRecipientName = "Meisterbetrieb Kölner Raumgestaltung GmbH",
                    invoiceDateIso = "2026-06-18",
                    paymentMethodBankTransfer = true,
                    totalInvoiceGrossCents = 3_800_00L,
                    eligibleLaborAndMachineCents = 2_750_00L,
                    disabilityDegreeGdB = null,
                    updatedAtEpochMillis = now
                ),
                SpecialExpenseAndBurdenEntity(
                    id = "SPEC-P2-2",
                    profileId = PROFILE_2_ID,
                    recordCategory = "NEBENKOSTENABRECHNUNG_SEC35A",
                    titleDe = "WEG-Jahresabrechnung (§ 35a EStG): Hausmeister, Treppenhausreinigung & Aufzugswartung",
                    providerOrRecipientName = "Domstadt Immobilienverwaltung Köln",
                    invoiceDateIso = "2026-04-20",
                    paymentMethodBankTransfer = true,
                    totalInvoiceGrossCents = 2_950_00L,
                    eligibleLaborAndMachineCents = 1_320_00L,
                    disabilityDegreeGdB = null,
                    updatedAtEpochMillis = now
                )
            )
        )

        dao.upsertPensionAndInsurance(
            PensionAndInsuranceEntity(
                id = "PENS-P2",
                profileId = PROFILE_2_ID,
                statutoryPensionOrVersorgungswerkEmployeeCents = 7_290_00L,
                employerStatutoryPensionContributionCents = 7_290_00L,
                ruerupBasisrenteContributionsCents = 0L,
                healthInsuranceBasicTariffCents = 4_980_00L,
                nursingCareInsuranceCents = 1_340_00L,
                unemploymentAndOtherInsuranceCents = 1_020_00L,
                riesterContributionsCents = 0L,
                riesterStateAllowancesCents = 0L,
                statutoryPensionReceivedGrossCents = 0L,
                pensionTaxableSharePercent = 0,
                updatedAtEpochMillis = now
            )
        )

        dao.upsertTaxAssessmentNotices(
            listOf(
                TaxAssessmentNoticeEntity(
                    id = "NOTICE-P2",
                    profileId = PROFILE_2_ID,
                    taxOfficeName = "Finanzamt Köln-West",
                    noticeDateIso = "2026-10-19",
                    klartaxCalculatedRefundCents = 6_630_47L,
                    finanzamtAssessedRefundCents = 4_914_47L,
                    deviationPointTitleDe = "Nichtanerkennung des sofort abziehbaren Erhaltungsaufwands (4.400,00 €) bei Anlage V",
                    finanzamtExplanationDe = "Die Aufwendungen für die Balkon- und Fenstersanierung wurden als nachträgliche Herstellungskosten der 2-%-Gebäude-AfA zugeschlagen.",
                    legalCounterArgumentDe = "Bei dem Austausch einzelner schadhafter Fenster und der Instandsetzung der Balkonabdichtung handelt es sich nach ständiger BFH-Rechtsprechung (vgl. BFH IX R 20/08) und R 21.1 Abs. 2 EStR um reinen Erhaltungsaufwand ohne Standardhebung, der gemäß § 9 Abs. 1 Satz 1 EStG im Jahr der Zahlung sofort in voller Höhe (4.400,00 €) als Werbungskosten bei den Einkünften aus Vermietung und Verpachtung abziehbar ist.",
                    updatedAtEpochMillis = now
                )
            )
        )
    }
}
