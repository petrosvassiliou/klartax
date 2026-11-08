package de.klartax.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Kundenverwaltung (CRM & Stammdaten für Freiberufler):
 * Speichert alle Pflichtangaben für ZUGFeRD 2.3 und XRechnung 3.0 (EN 16931)
 * inkl. USt-IdNr., Leitweg-ID / Käuferreferenz (BT-10), Zahlungsziel und Standard-Stundensatz.
 */
@Serializable
@Entity(
    tableName = "customers",
    indices = [Index("profileId"), Index("customerNumber")]
)
data class CustomerEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val customerNumber: String, // z. B. "KD-2026-001"
    val companyOrFullName: String,
    val contactPersonName: String,
    val email: String,
    val streetAndNumber: String,
    val postalCode: String,
    val city: String,
    val countryIso: String, // z. B. "DE", "AT", "CH"
    val vatId: String?, // z. B. "DE129485712"
    val buyerReferenceLeitwegId: String, // BT-10 Pflichtfeld für XRechnung / Behörden & Konzerne
    val defaultHourlyRateNetCents: Long,
    val paymentTermDays: Int, // z. B. 14 oder 30 Tage
    val defaultTaxMode: String, // "STANDARD_19", "REDUCED_7", "KLEINUNTERNEHMER_SEC19", "REVERSE_CHARGE_SEC13B"
    val notesDe: String?,
    val updatedAtEpochMillis: Long
)

/**
 * Produkte & angebotene Dienstleistungen (SevDesk Artikel- & Leistungskatalog):
 * Können verwaltet und mit 1 Klick in Angebote, Aufträge und Rechnungen übernommen werden.
 */
@Serializable
@Entity(
    tableName = "service_catalog_items",
    indices = [Index("profileId"), Index("articleNumber")]
)
data class ServiceCatalogItemEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val articleNumber: String, // z. B. "DL-101", "DL-102", "PR-201"
    val itemType: String, // "SERVICE" (Dienstleistung) oder "PRODUCT" (Produkt / Lizenz)
    val titleDe: String,
    val descriptionDe: String,
    val unitCode: String, // "HUR" (Stunde), "DAY" (Tagessatz), "C62" (Pauschal/Stück), "MON" (Monat)
    val unitPriceNetCents: Long,
    val vatRatePercent: Int, // 19, 7, 0
    val defaultQuantity: String = "1.00",
    val updatedAtEpochMillis: Long
)

/**
 * Standardtexte & Textbausteine für Angebote, Auftragsbestätigungen, Rechnungen und Mahnungen
 * (Einleitungstext / Kopftext sowie Schlusstext / Zahlungsbedingungen).
 */
@Serializable
@Entity(
    tableName = "document_text_templates",
    indices = [Index("profileId"), Index("documentScope")]
)
data class DocumentTextTemplateEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val templateTitleDe: String,
    val documentScope: String, // "ALL", "QUOTE" (Angebot), "ORDER_CONFIRMATION" (Auftrag), "INVOICE" (Rechnung)
    val placementType: String, // "HEADER_INTRO" (Kopftext) oder "FOOTER_CLOSING" (Fußtext)
    val bodyTextDe: String,
    val isDefault: Boolean,
    val updatedAtEpochMillis: Long
)

/**
 * Briefpapier & Firmenlogo Konfiguration (SevDesk Briefpapier-Designer nach DIN 5008):
 * Steuert Firmenlogo (Bild-Upload oder Monogramm), Markenfarben, DIN-5008-Absenderzeile,
 * Falzmarken und 3-spaltige Fußzeile (Anschrift, Bankverbindung, Steuernummer/USt-IdNr.).
 */
@Serializable
@Entity(
    tableName = "stationery_configs",
    indices = [Index("profileId")]
)
data class StationeryConfigEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val companyDisplayName: String,
    val companySubtitleOrClaim: String,
    val logoMonogramText: String,
    val logoFileName: String? = null,
    val logoImageUri: String? = null,
    val primaryAccentHex: String = "#7A1624", // Dunkelrot
    val secondaryAccentHex: String = "#1E3A8A", // Königsblau
    val senderWindowLineDe: String,
    val footerCol1AddressDe: String,
    val footerCol2BankDe: String,
    val footerCol3TaxDe: String,
    val showDin5008FoldMarks: Boolean = true,
    val showEpcGiroCodeQr: Boolean = true,
    val updatedAtEpochMillis: Long
)

/**
 * Modul A (SevDesk EÜR nach § 4 Abs. 3 EStG):
 * Buchungskategorien im Klartext, exakt gemappt auf amtliche Anlage-EÜR-Kennziffern und SKR03/SKR04.
 */
@Serializable
enum class KlartextEurCategory(
    val displayNameDe: String,
    val isIncome: Boolean,
    val anlageEurKz: Int,
    val anlageEurLineLabel: String,
    val skr03Account: Int,
    val skr04Account: Int,
    val defaultVatRatePercent: Int
) {
    INCOME_19_PCT(
        displayNameDe = "Honorar- & Projekterlöse 19 % USt",
        isIncome = true,
        anlageEurKz = 112,
        anlageEurLineLabel = "Zeile 14: Umsatzsteuerpflichtige Betriebseinnahmen",
        skr03Account = 8400,
        skr04Account = 4400,
        defaultVatRatePercent = 19
    ),
    INCOME_7_PCT(
        displayNameDe = "Urheberrechtliche & publizistische Erlöse 7 % USt",
        isIncome = true,
        anlageEurKz = 112,
        anlageEurLineLabel = "Zeile 14: Umsatzsteuerpflichtige Betriebseinnahmen (ermäßigt)",
        skr03Account = 8300,
        skr04Account = 4300,
        defaultVatRatePercent = 7
    ),
    INCOME_KLEINUNTERNEHMER_SEC19(
        displayNameDe = "Erlöse als Kleinunternehmer (§ 19 UStG)",
        isIncome = true,
        anlageEurKz = 111,
        anlageEurLineLabel = "Zeile 11: Betriebseinnahmen als umsatzsteuerlicher Kleinunternehmer",
        skr03Account = 8195,
        skr04Account = 4185,
        defaultVatRatePercent = 0
    ),
    INCOME_REVERSE_CHARGE_EU(
        displayNameDe = "EU-Dienstleistungserlöse Reverse Charge (§ 13b UStG)",
        isIncome = true,
        anlageEurKz = 103,
        anlageEurLineLabel = "Zeile 15: Nicht steuerbare / EU Reverse-Charge Erlöse",
        skr03Account = 8336,
        skr04Account = 4336,
        defaultVatRatePercent = 0
    ),
    EXPENSE_SUBCONTRACTORS(
        displayNameDe = "Fremdleistungen & Subunternehmer",
        isIncome = false,
        anlageEurKz = 110,
        anlageEurLineLabel = "Zeile 26: Bezogene Fremdleistungen",
        skr03Account = 3100,
        skr04Account = 5900,
        defaultVatRatePercent = 19
    ),
    EXPENSE_IT_CLOUD_SOFTWARE(
        displayNameDe = "Software-Abos, Cloud, Domains & Telefon/Internet",
        isIncome = false,
        anlageEurKz = 185,
        anlageEurLineLabel = "Zeile 52: Telekommunikation, IT-Lizenzen & Bürobedarf",
        skr03Account = 4964,
        skr04Account = 6815,
        defaultVatRatePercent = 19
    ),
    EXPENSE_AFA_DEPRECIATION(
        displayNameDe = "Abschreibungen (Digital-AfA, GWG & lineare AfA)",
        isIncome = false,
        anlageEurKz = 130,
        anlageEurLineLabel = "Zeile 30–43: Absetzung für Abnutzung (AfA)",
        skr03Account = 4830,
        skr04Account = 6220,
        defaultVatRatePercent = 19
    ),
    EXPENSE_HOSPITALITY_70_30(
        displayNameDe = "Geschäftliche Bewirtung (70 % abziehbar / 30 % nicht abziehbar)",
        isIncome = false,
        anlageEurKz = 165,
        anlageEurLineLabel = "Zeile 54: Bewirtungsaufwendungen nach § 4 Abs. 5 Nr. 2 EStG",
        skr03Account = 4650,
        skr04Account = 6640,
        defaultVatRatePercent = 19
    ),
    EXPENSE_VEHICLE_MOBILITY(
        displayNameDe = "PKW-Kosten, BahnCard & Geschäftsreisen",
        isIncome = false,
        anlageEurKz = 140,
        anlageEurLineLabel = "Zeile 60–64: Kraftfahrzeugkosten und andere Fahrtkosten",
        skr03Account = 4530,
        skr04Account = 6530,
        defaultVatRatePercent = 19
    ),
    EXPENSE_HOME_OFFICE_WORKSPACE(
        displayNameDe = "Häusliches Arbeitszimmer / Tagespauschale Freiberufler",
        isIncome = false,
        anlageEurKz = 172,
        anlageEurLineLabel = "Zeile 46: Aufwendungen für ein häusliches Arbeitszimmer",
        skr03Account = 4288,
        skr04Account = 6348,
        defaultVatRatePercent = 0
    ),
    EXPENSE_VAT_PAYMENT_FINANZAMT(
        displayNameDe = "Gezahlte Umsatzsteuer-Vorauszahlung an das Finanzamt",
        isIncome = false,
        anlageEurKz = 186,
        anlageEurLineLabel = "Zeile 65: An das Finanzamt gezahlte und ggf. verrechnete Umsatzsteuer",
        skr03Account = 1780,
        skr04Account = 3820,
        defaultVatRatePercent = 0
    )
}

/**
 * EÜR-Buchungssatz nach § 4 Abs. 3 EStG unter strikter Beachtung des
 * Zufluss-/Abfluss-Prinzips (§ 11 EStG) inkl. 10-Tages-Regel (22.12. – 10.01.)
 * sowie GoBD-konformen PDF- und Bilderanhängen (`attachmentFileName`, `attachmentMimeType`, `attachmentUri`).
 */
@Serializable
@Entity(
    tableName = "eur_bookings",
    indices = [Index("profileId"), Index("effectiveTaxYear"), Index("isLocked")]
)
data class EurBookingEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val documentNumber: String,
    val descriptionDe: String,
    val counterpartyName: String,
    val counterpartyIban: String?,
    val category: KlartextEurCategory,
    val invoiceOrDueDateIso: String,
    val paymentDateIso: String,
    val isRecurringPayment: Boolean,
    val appliedTenDayRule: Boolean,
    val effectiveTaxYear: Int,
    val netAmountCents: Long,
    val vatRatePercent: Int,
    val vatAmountCents: Long,
    val grossAmountCents: Long,
    val deductibleNetCents: Long,
    val nonDeductibleNetCents: Long,
    val hospitalityHost: String?,
    val hospitalityGuests: String?,
    val hospitalityOccasion: String?,
    val taxFreeAllowanceType: String?,
    // PDF- & Bilderanhänge (GoBD-Belegverknüpfung)
    val attachmentFileName: String? = null,
    val attachmentMimeType: String? = null, // "application/pdf", "image/jpeg", "image/png", "application/xml"
    val attachmentUri: String? = null,
    val attachmentSizeBytes: Long = 0L,
    val isLocked: Boolean,
    val isStornoRecord: Boolean,
    val stornoOfBookingId: String?,
    val gobdRecordHash: String,
    val updatedAtEpochMillis: Long
)

/**
 * Durchgängige Belegkette (SevDesk-Workflow):
 * - Angebot ("QUOTE", AN-2026-xxx)
 * - Auftragsbestätigung / Auftrag ("ORDER_CONFIRMATION", AB-2026-xxx, verknüpft mit `linkedQuoteId` / `linkedQuoteNumber`)
 * - E-Rechnung ("INVOICE", RE-2026-xxx, verknüpft mit `linkedQuoteNumber` & `linkedOrderNumber`, ZUGFeRD 2.3 / XRechnung 3.0)
 * - Stornorechnung / Gutschrift ("STORNO", ST-2026-xxx)
 */
@Serializable
@Entity(
    tableName = "invoices",
    indices = [Index("profileId"), Index("invoiceNumber"), Index("status"), Index("customerId")]
)
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val customerId: String? = null,
    val invoiceNumber: String,
    val documentType: String, // "QUOTE" (Angebot), "ORDER_CONFIRMATION" (Auftrag/AB), "INVOICE" (Rechnung), "STORNO" (Stornorechnung)
    val eInvoiceStandard: String, // "ZUGFERD_2_3", "XRECHNUNG_3_0_CII"
    val taxMode: String, // "STANDARD_19", "REDUCED_7", "KLEINUNTERNEHMER_SEC19", "REVERSE_CHARGE_SEC13B"
    val customerName: String,
    val customerStreet: String,
    val customerPostalCode: String,
    val customerCity: String,
    val customerCountryIso: String,
    val customerVatId: String?,
    val buyerReferenceLeitwegId: String,
    val issueDateIso: String,
    val servicePeriodStartIso: String,
    val servicePeriodEndIso: String,
    val dueDateIso: String,
    val paidDateIso: String?,
    val netTotalCents: Long,
    val vatTotalCents: Long,
    val grossTotalCents: Long,
    val status: String, // "DRAFT", "QUOTE_OPEN", "QUOTE_ACCEPTED", "ORDER_CONFIRMED", "ORDER_BILLED", "SENT_LOCKED", "PAID", "STORNO_CANCELLED"
    val dunningLevel: Int,
    val sellerIban: String,
    val sellerBic: String,
    val lineItemsJson: String,
    val embeddedXmlPayload: String,
    val isLocked: Boolean,
    val stornoOfInvoiceId: String?,
    // SevDesk Beleg-Verknüpfung (Angebot -> Auftrag -> Rechnung) & Textbausteine
    val linkedQuoteId: String? = null,
    val linkedQuoteNumber: String? = null,
    val linkedOrderId: String? = null,
    val linkedOrderNumber: String? = null,
    val subjectTitleDe: String = "",
    val headerIntroTextDe: String = "",
    val footerClosingTextDe: String = "",
    val gobdRecordHash: String,
    val updatedAtEpochMillis: Long
)

@Serializable
data class InvoiceLineItem(
    val positionNumber: Int,
    val title: String,
    val description: String,
    val quantityHoursOrUnits: String,
    val unitCode: String, // "HUR" (Stunde), "DAY" (Tag), "C62" (Pauschal/Stück), "MON" (Monat)
    val unitPriceNetCents: Long,
    val vatRatePercent: Int,
    val lineNetTotalCents: Long,
    val articleNumber: String? = null
)

/**
 * Integrierte Projekt-Zeiterfassung (Stoppuhr & Stundenzettel) mit 1-Klick-Übernahme in Angebote/Rechnungen.
 */
@Serializable
@Entity(tableName = "time_tracking_entries", indices = [Index("profileId")])
data class TimeTrackingEntryEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val projectName: String,
    val clientName: String,
    val taskDescription: String,
    val dateIso: String,
    val durationMinutes: Int,
    val hourlyRateNetCents: Long,
    val isBilled: Boolean,
    val linkedInvoiceId: String?,
    val updatedAtEpochMillis: Long
)

/**
 * Anlagenverzeichnis & AfA-Rechner.
 */
@Serializable
@Entity(tableName = "assets_afa", indices = [Index("profileId")])
data class AssetEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val inventoryNumber: String,
    val assetNameDe: String,
    val categoryType: String,
    val purchaseDateIso: String,
    val acquisitionCostNetCents: Long,
    val vatPaidCents: Long,
    val usefulLifeYears: Int,
    val privateUsePercent: Int,
    val currentYearDepreciationCents: Long,
    val deductibleBusinessDepreciationCents: Long,
    val remainingBookValueCents: Long,
    val updatedAtEpochMillis: Long
)

/**
 * PKW & Mobilität für Freiberufler.
 */
@Serializable
@Entity(tableName = "vehicle_logs", indices = [Index("profileId")])
data class VehicleLogEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val vehicleModelDe: String,
    val grossListPriceNewCents: Long,
    val isElectricVehicle: Boolean,
    val totalAnnualCostNetCents: Long,
    val totalDrivenKm: Int,
    val businessDrivenKm: Int,
    val commuteDistanceOneWayKm: Int,
    val commuteDaysPerYear: Int,
    val preferredMethod: String,
    val updatedAtEpochMillis: Long
)

/**
 * Online-Banking Transaktionen (CAMT.053 / MT940 / CSV / FinTS).
 */
@Serializable
@Entity(tableName = "bank_transactions", indices = [Index("profileId")])
data class BankTransactionEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val bookingDateIso: String,
    val valutaDateIso: String,
    val counterpartyName: String,
    val counterpartyIban: String,
    val purposeText: String,
    val amountCents: Long,
    val importSourceFormat: String,
    val matchedEntityType: String?,
    val matchedEntityId: String?,
    val matchConfidencePercent: Int,
    val updatedAtEpochMillis: Long
)
