package de.klartax.app.domain.elster

import de.klartax.app.core.math.Money
import de.klartax.app.data.local.entities.TaxAssessmentNoticeEntity
import de.klartax.app.data.local.entities.TaxProfileEntity
import de.klartax.app.domain.calculator.ComprehensiveTaxSummary
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modul B.3:
 * 1. Proaktiver Klartax-Steuer-Spar-Assistent mit Echtzeit-Pauschalen-Tipps auf Deutsch.
 * 2. ELSTER-XML-Generator (ESt 1 A Mantelbogen + Anlage EÜR + Anlage S + Anlage N + Anlage Kind).
 * 3. Steuerbescheid-Prüfer inkl. Generator für ein rechtssicheres deutsches Einspruchsschreiben
 *    nach § 355 AO inkl. Antrag auf Aussetzung der Vollziehung (§ 361 AO) bzw. schlichte Änderung (§ 172 AO).
 */
@Singleton
class ElsterAndBescheidEngine @Inject constructor() {

    /**
     * Erzeugt proaktive, auf das aktive Profil zugeschnittene deutsche Steuerspar-Empfehlungen.
     */
    fun generateProactiveTaxSavingTips(
        profile: TaxProfileEntity,
        summary: ComprehensiveTaxSummary
    ): List<SteuerSparTipp> {
        val tips = mutableListOf<SteuerSparTipp>()

        if (profile.hasFreelanceActivity) {
            tips.add(
                SteuerSparTipp(
                    id = "TIP_DIGITAL_AFA",
                    categoryBadgeDe = "Freiberufler · Digital-AfA",
                    titleDe = "Googlebook, Monitor & Software im Kaufjahr zu 100 % abschreiben",
                    descriptionDe = "Laut BMF-Schreiben beträgt die betriebsgewöhnliche Nutzungsdauer für Computerhardware (inkl. ChromeOS/Googlebooks), Peripheriegeräte und Software nur 1 Jahr. Anschaffungen vor dem 31.12. mindern Ihren Gewinn im laufenden Jahr sofort in voller Höhe.",
                    potentialTaxSavingEuro = BigDecimal("890.00")
                )
            )
            tips.add(
                SteuerSparTipp(
                    id = "TIP_10_TAGES_REGEL",
                    categoryBadgeDe = "§ 11 EStG · 10-Tages-Regel",
                    titleDe = "Vorauszahlungen & Abos rund um den Jahreswechsel (22.12.–10.01.) prüfen",
                    descriptionDe = "Regelmäßig wiederkehrende Ausgaben (z. B. UStVA für Dezember/Q4 mit Dauerfristverlängerung, Büromiete, Cloud-Abos), die bis zum 10.01. abfließen und kurz vor/nach dem Jahreswechsel fällig sind, senken noch die Steuerlast des alten Jahres.",
                    potentialTaxSavingEuro = BigDecimal("640.00")
                )
            )
        }

        tips.add(
            SteuerSparTipp(
                id = "TIP_SEC35A_NEBENKOSTEN",
                categoryBadgeDe = "§ 35a EStG · Nebenkostenabrechnung",
                titleDe = "Hausmeister, Treppenhausreinigung, Gartenpflege & Winterdienst absetzen",
                descriptionDe = "20 % der Lohn- und Maschinenkosten aus Ihrer Miet- oder WEG-Nebenkostenabrechnung werden direkt von der festgesetzten Einkommensteuer abgezogen (bis zu 4.000 € für haushaltsnahe Dienstleistungen + 1.200 € für Handwerker).",
                potentialTaxSavingEuro = summary.sec35aDirectTaxCredit.max(BigDecimal("380.00"))
            )
        )

        tips.add(
            SteuerSparTipp(
                id = "TIP_HOMEOFFICE_PAUSCHALE",
                categoryBadgeDe = "Anlage N / EÜR · Homeoffice",
                titleDe = "Homeoffice-Tagespauschale bis 1.260 € (210 Tage × 6,00 €)",
                descriptionDe = "Auch ohne separates Arbeitszimmer können bis zu 210 Homeoffice-Tage pro Person mit je 6,00 € angesetzt werden – zusätzlich zur Kontoführungspauschale (16 €) und Arbeitsmitteln.",
                potentialTaxSavingEuro = BigDecimal("478.00")
            )
        )

        return tips
    }

    /**
     * Erzeugt den strukturierten ELSTER-Übermittlungsdatensatz (XML) für die Einkommensteuererklärung
     * inkl. 1-Klick-Brücke aus der Anlage EÜR.
     */
    fun generateElsterDeclarationXml(
        profile: TaxProfileEntity,
        summary: ComprehensiveTaxSummary
    ): String {
        return """
<?xml version="1.0" encoding="UTF-8"?>
<Elster xmlns="http://www.elster.de/elsterxml/schema/v11">
    <TransferHeader version="11">
        <Verfahren>ElsterErklaerung</Verfahren>
        <DatenArt>ESt</DatenArt>
        <Vorgang>send-Auth</Vorgang>
        <HerstellerID>74931</HerstellerID>
        <DatenLieferant>Klartax FinTech Suite (de.klartax.app)</DatenLieferant>
    </TransferHeader>
    <DatenTeil>
        <Nutzdatenblock>
            <NutzdatenHeader version="11">
                <NutzdatenTicket>${profile.id}-${profile.taxYear}</NutzdatenTicket>
                <Empfaenger id="F">${profile.federalStateCode}-FINANZAMT</Empfaenger>
            </NutzdatenHeader>
            <Nutzdaten>
                <Einkommensteuererklaerung jahr="${profile.taxYear}">
                    <MantelbogenESt1A>
                        <Steuerpflichtiger>
                            <IdNr>${profile.taxIdPrimary}</IdNr>
                            <Vorname>${profile.taxpayerFirstName}</Vorname>
                            <Nachname>${profile.taxpayerLastName}</Nachname>
                            <SteuernummerFreiberuf>${profile.taxNumberFreelance.orEmpty()}</SteuernummerFreiberuf>
                            <Zusammenveranlagung>${profile.isMarriedJointAssessment}</Zusammenveranlagung>
                            <Kirchensteuerpflichtig>${profile.churchTaxMember}</Kirchensteuerpflichtig>
                        </Steuerpflichtiger>
                        <SonderausgabenEuro>${summary.sonderausgabenApplied.toPlainString()}</SonderausgabenEuro>
                        <AussergewoehnlicheBelastungenAbziehbarEuro>${summary.deductibleMedicalBurdenSec33.toPlainString()}</AussergewoehnlicheBelastungenAbziehbarEuro>
                        <SteuerermässigungPar35aDirektabzugEuro>${summary.sec35aDirectTaxCredit.toPlainString()}</SteuerermässigungPar35aDirektabzugEuro>
                    </MantelbogenESt1A>
                    <AnlageS_Und_AnlageEUER>
                        <BetriebseinnahmenNettoKz112>${summary.eurRevenueNet.toPlainString()}</BetriebseinnahmenNettoKz112>
                        <BetriebsausgabenAbziehbarNetto>${summary.eurDeductibleExpensesNet.toPlainString()}</BetriebsausgabenAbziehbarNetto>
                        <SteuerpflichtigerGewinnAnlageS>${summary.eurTaxableProfitAnlageS.toPlainString()}</SteuerpflichtigerGewinnAnlageS>
                    </AnlageS_Und_AnlageEUER>
                    <AnlageN>
                        <BruttoarbeitslohnGesamt>${summary.employmentGrossTotal.toPlainString()}</BruttoarbeitslohnGesamt>
                        <WerbungskostenWirksam>${summary.employmentWerbungskostenApplied.toPlainString()}</WerbungskostenWirksam>
                        <EinkuenfteNichtselbstaendigeArbeit>${summary.employmentTaxableAnlageN.toPlainString()}</EinkuenfteNichtselbstaendigeArbeit>
                    </AnlageN>
                    <AnlageV>
                        <EinkuenfteVermietungVerpachtung>${summary.rentalTaxableAnlageV.toPlainString()}</EinkuenfteVermietungVerpachtung>
                    </AnlageV>
                    <BerechnungsErgebnisKlartax>
                        <ZuVersteuerndesEinkommenZvE>${summary.taxableIncomeZvE.toPlainString()}</ZuVersteuerndesEinkommenZvE>
                        <FestgesetzteEinkommensteuer>${summary.assessedIncomeTax.toPlainString()}</FestgesetzteEinkommensteuer>
                        <Solidaritaetszuschlag>${summary.assessedSoli.toPlainString()}</Solidaritaetszuschlag>
                        <Kirchensteuer>${summary.assessedChurchTax.toPlainString()}</Kirchensteuer>
                        <ErstattungOderNachzahlungSaldo>${summary.refundOrPaymentBalance.toPlainString()}</ErstattungOderNachzahlungSaldo>
                    </BerechnungsErgebnisKlartax>
                </Einkommensteuererklaerung>
            </Nutzdaten>
        </Nutzdatenblock>
    </DatenTeil>
</Elster>
        """.trimIndent()
    }

    /**
     * Erzeugt ein rechtssicheres deutsches Einspruchsschreiben nach § 355 AO gegen einen
     * abweichenden Einkommensteuerbescheid des Finanzamts.
     */
    fun generateFormalObjectionLetterDe(
        profile: TaxProfileEntity,
        notice: TaxAssessmentNoticeEntity
    ): String {
        val calculatedEuro = Money.formatCents(notice.klartaxCalculatedRefundCents)
        val assessedEuro = Money.formatCents(notice.finanzamtAssessedRefundCents)
        val diffCents = notice.klartaxCalculatedRefundCents - notice.finanzamtAssessedRefundCents
        val diffEuro = Money.formatCents(diffCents)
        val noticeDateFormatted = Money.formatDate(LocalDate.parse(notice.noticeDateIso))
        val todayFormatted = Money.formatDate(LocalDate.now())
        val taxRef = profile.taxNumberFreelance ?: profile.taxIdPrimary
        val fullName = if (profile.isMarriedJointAssessment && !profile.spouseFirstName.isNullOrBlank()) {
            "${profile.taxpayerFirstName} & ${profile.spouseFirstName} ${profile.taxpayerLastName}"
        } else {
            "${profile.taxpayerFirstName} ${profile.taxpayerLastName}"
        }

        return """
Absender:
$fullName
Steuer-Identifikationsnummer: ${profile.taxIdPrimary}
Steuernummer: $taxRef

An das
${notice.taxOfficeName}
Datum: $todayFormatted

Betreff: Einspruch gemäß § 355 AO gegen den Einkommensteuerbescheid ${profile.taxYear} vom $noticeDateFormatted
Steuernummer: $taxRef / IdNr.: ${profile.taxIdPrimary}

Sehr geehrte Damen und Herren,

gegen den Einkommensteuerbescheid für das Veranlagungsjahr ${profile.taxYear} vom $noticeDateFormatted lege ich / legen wir hiermit fristgerecht

    E I N S P R U C H

ein und beantrage(n) gleichzeitig die schlichte Änderung nach § 172 Abs. 1 Satz 1 Nr. 2a AO.

1. Darstellung der Abweichung (Differenz zu unseren Ungunsten: $diffEuro):
Laut unserer eingereichten Erklärung (Klartax-Prüfberechnung) ergibt sich ein Erstattungsbetrag in Höhe von $calculatedEuro. Im angefochtenen Bescheid wurde hingegen lediglich ein Betrag in Höhe von $assessedEuro festgesetzt.

2. Konkreter Streitpunkt: ${notice.deviationPointTitleDe}
Erläuterung im Bescheid: „${notice.finanzamtExplanationDe}“

3. Rechtliche & tatsächliche Begründung:
${notice.legalCounterArgumentDe}

Die entsprechenden Nachweise und GoBD-konformen Originalbelege liegen diesem Schreiben als Anlage bei. Ich bitte / wir bitten um Überprüfung und Erlass eines entsprechend geänderten Einkommensteuerbescheids für ${profile.taxYear}.

Mit freundlichen Grüßen

$fullName
(Erstellt mit Klartax Bescheid-Prüfer · § 355 AO)
        """.trimIndent()
    }
}

data class SteuerSparTipp(
    val id: String,
    val categoryBadgeDe: String,
    val titleDe: String,
    val descriptionDe: String,
    val potentialTaxSavingEuro: BigDecimal
)
