package de.klartax.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Modul B (WISO Steuer):
 * Anlage N (Einkünfte aus nichtselbständiger Arbeit, Lohnsteuerbescheinigung & Werbungskosten).
 */
@Serializable
@Entity(tableName = "employment_tax_anlage_n", indices = [Index("profileId")])
data class EmploymentTaxEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val personRole: String, // "PRIMARY" (Steuerpflichtiger A) oder "SPOUSE" (Ehepartner B)
    val employerName: String,
    val taxClass: Int, // Steuerklasse I bis VI
    val grossSalaryCents: Long, // Zeile 3 Lohnsteuerbescheinigung
    val withheldIncomeTaxCents: Long, // Zeile 4 Einbehaltene Lohnsteuer
    val withheldSoliCents: Long, // Zeile 5 Solidaritätszuschlag
    val withheldChurchTaxCents: Long, // Zeile 6 Kirchensteuer
    val commuteDistanceOneWayKm: Int, // Einfache Entfernung Wohnung <-> Erste Tätigkeitsstätte
    val commuteWorkdaysPerYear: Int,
    val publicTransportAnnualCostCents: Long, // ÖPNV-Ticketkosten für Günstigerprüfung (§ 9 Abs. 2 S. 2 EStG)
    val homeOfficeDays: Int, // Bis zu 210 Tage à 6,00 € = max. 1.260 € (§ 4 Abs. 5 Nr. 6c EStG)
    val dedicatedHomeOfficeRoomCostCents: Long, // Alternativ: Häusliches Arbeitszimmer (Mittelpunkt)
    val workEquipmentCostCents: Long, // Arbeitsmittel (Fachliteratur, Monitor, Bürostuhl)
    val trainingAndEducationCostCents: Long, // Fortbildungskosten
    val applicationCostCents: Long, // Bewerbungskosten
    val travelMealAllowanceCents: Long, // Verpflegungsmehraufwand (14 € / 28 €)
    val accountMaintenanceFeeCents: Long, // Kontoführungspauschale (Standard: 1.600 Cents = 16,00 €)
    val updatedAtEpochMillis: Long
)

/**
 * Mantelbogen: Sonderausgaben, Außergewöhnliche Belastungen (§ 33 EStG) sowie
 * Haushaltsnahe Dienstleistungen, Handwerkerleistungen (§ 35a EStG) & Energetische Sanierung (§ 35c EStG)
 * inkl. Nebenkostenabrechnungs-Assistent.
 */
@Serializable
@Entity(tableName = "special_expenses_and_burdens", indices = [Index("profileId")])
data class SpecialExpenseAndBurdenEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val recordCategory: String,
    // "DONATION_SPENDE", "CHURCH_TAX_PAID", "MEDICAL_BURDEN_SEC33", "DISABILITY_PAUSCHBETRAG",
    // "HOUSEHOLD_SERVICE_SEC35A", "CRAFTSMAN_HANDWERKER_SEC35A", "NEBENKOSTENABRECHNUNG_SEC35A", "ENERGETIC_SEC35C"
    val titleDe: String,
    val providerOrRecipientName: String,
    val invoiceDateIso: String,
    val paymentMethodBankTransfer: Boolean, // § 35a Abs. 5 S. 3 EStG: Barzahlung ist steuerschädlich!
    val totalInvoiceGrossCents: Long,
    val eligibleLaborAndMachineCents: Long, // Bei § 35a nur Lohn-, Fahrt- & Maschinenkosten (ohne Material!)
    val disabilityDegreeGdB: Int?, // Grad der Behinderung (20 bis 100) für Pauschbetrag nach § 33b EStG
    val updatedAtEpochMillis: Long
)

/**
 * Anlage KAP (Einkünfte aus Kapitalvermögen, ETFs, Aktien) &
 * Anlage SO (Private Veräußerungsgeschäfte nach § 23 EStG, Krypto mit 1-Jahres-Haltefrist).
 */
@Serializable
@Entity(tableName = "capital_and_crypto_tax", indices = [Index("profileId")])
data class CapitalAndCryptoEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val depotBankName: String,
    val capitalGainsGrossCents: Long, // Kapitalerträge vor Sparer-Pauschbetrag (inkl. ETF-Vorabpauschale)
    val etfVorabpauschaleIncludedCents: Long,
    val sparerPauschbetragUsedAtBankCents: Long, // Bereits durch Freistellungsauftrag genutzter Betrag
    val withheldKapEstCents: Long, // Einbehaltene Kapitalertragsteuer (25 %)
    val withheldKapSoliCents: Long,
    val withheldKapChurchTaxCents: Long,
    val stockLossPotCents: Long, // Aktienverlustverrechnungstopf
    val cryptoAssetName: String?, // z. B. "Bitcoin (BTC)" / "Ethereum (ETH)"
    val cryptoPurchaseDateIso: String?,
    val cryptoSaleDateIso: String?,
    val cryptoGainOrLossCents: Long, // Gewinn/Verlust aus privaten Veräußerungsgeschäften (§ 23 EStG)
    val updatedAtEpochMillis: Long
)

/**
 * Anlage V (Einkünfte aus Vermietung und Verpachtung für private Vermieter).
 */
@Serializable
@Entity(tableName = "rental_properties_anlage_v", indices = [Index("profileId")])
data class RentalPropertyEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val propertyAddressDe: String,
    val constructionYear: Int, // Bestimmt Gebäude-AfA: vor 1925 = 2,5 %, ab 1925 = 2,0 %, Neubau ab 2023 = 3,0 %
    val buildingAcquisitionCostCents: Long, // Bemessungsgrundlage Gebäude-AfA (ohne Grund & Boden!)
    val afaRateBasisPoints: Int, // 200 = 2,00 %, 250 = 2,50 %, 300 = 3,00 %
    val annualColdRentReceivedCents: Long,
    val annualAncillaryCostsReceivedCents: Long, // Vereinnahmte Nebenkosten-Umlagen
    val mortgageInterestPaidCents: Long, // Schuldzinsen
    val allocablePropertyCostsCents: Long, // Umlagefähige & nicht umlagefähige laufende Hausgeldkosten
    val maintenanceTotalCents: Long, // Erhaltungsaufwand (Renovierung/Instandhaltung)
    val maintenanceSpreadYearsSec82b: Int, // 1 = Sofortabzug (100 %), 2..5 = Verteilung nach § 82b EStDV
    val updatedAtEpochMillis: Long
)

/**
 * Anlage Kind:
 * Automatische Günstigerprüfung Kindergeld vs. Kinderfreibetrag + BEA-Freibetrag (§ 31, § 32 Abs. 6 EStG),
 * Kinderbetreuungskosten, Schulgeld, Entlastungsbetrag für Alleinerziehende (§ 24b EStG), Ausbildungsfreibetrag.
 */
@Serializable
@Entity(tableName = "children_anlage_kind", indices = [Index("profileId")])
data class ChildTaxEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val childFirstName: String,
    val birthDateIso: String,
    val kindergeldReceivedAnnualCents: Long, // z. B. 12 x 250 € = 3.000 € (bzw. 255 €/Monat = 3.060 €)
    val childcareExpenseTotalCents: Long, // Kinderbetreuungskosten (Kita, Tagesmutter, Hort)
    val privateSchoolFeeEligibleCents: Long, // Schulgeld (30 % bis max. 5.000 € nach § 10 Abs. 1 Nr. 9 EStG)
    val isSingleParentHousehold: Boolean, // Entlastungsbetrag für Alleinerziehende (4.260 € + 240 € je weiteres Kind)
    val isAdultChildInExternalTraining: Boolean, // Ausbildungsfreibetrag 1.200 € (§ 33a Abs. 2 EStG)
    val updatedAtEpochMillis: Long
)

/**
 * Anlage Vorsorgeaufwand & Anlage R / R-AV:
 * Gesetzliche/private Kranken- und Pflegeversicherung, Versorgungswerke, KSK, Rürup/Basisrente, Riester, Renten.
 */
@Serializable
@Entity(tableName = "pension_and_insurance", indices = [Index("profileId")])
data class PensionAndInsuranceEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val statutoryPensionOrVersorgungswerkEmployeeCents: Long, // Arbeitnehmer-/Freiberufler-Anteil GRV / Versorgungswerk
    val employerStatutoryPensionContributionCents: Long,
    val ruerupBasisrenteContributionsCents: Long, // Basisrente nach § 10 Abs. 1 Nr. 2b EStG (100 % absetzbar bis Höchstbetrag)
    val healthInsuranceBasicTariffCents: Long, // Basisabsicherung Krankenversicherung (§ 10 Abs. 1 Nr. 3a EStG)
    val nursingCareInsuranceCents: Long, // Pflegepflichtversicherung
    val unemploymentAndOtherInsuranceCents: Long, // Weitere sonstige Vorsorgeaufwendungen (Haftpflicht, BU, Arbeitslosenvers.)
    val riesterContributionsCents: Long, // Altersvorsorgebeiträge nach § 10a EStG (max. 2.100 €)
    val riesterStateAllowancesCents: Long, // Grundzulage (175 €) + Kinderzulagen (185 € / 300 €)
    val statutoryPensionReceivedGrossCents: Long, // Anlage R: Bruttorente
    val pensionTaxableSharePercent: Int, // Besteuerungsanteil nach Renteneintrittsjahr
    val updatedAtEpochMillis: Long
)

/**
 * ELSTER-Steuerbescheid-Prüfer:
 * Speichert die vom Finanzamt im Einkommensteuerbescheid festgesetzten Werte im Vergleich
 * zur Klartax-Berechnung und erzeugt bei Abweichungen auf Klick ein rechtssicheres Einspruchsschreiben (§ 355 AO).
 */
@Serializable
@Entity(tableName = "tax_assessment_notices", indices = [Index("profileId")])
data class TaxAssessmentNoticeEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val taxOfficeName: String,
    val noticeDateIso: String,
    val klartaxCalculatedRefundCents: Long,
    val finanzamtAssessedRefundCents: Long,
    val deviationPointTitleDe: String,
    val finanzamtExplanationDe: String,
    val legalCounterArgumentDe: String,
    val updatedAtEpochMillis: Long
)
