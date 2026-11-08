package de.klartax.app.domain

import de.klartax.app.core.math.Money
import de.klartax.app.core.security.GoBdHashChain
import de.klartax.app.data.local.entities.InvoiceEntity
import de.klartax.app.data.local.entities.InvoiceLineItem
import de.klartax.app.data.local.entities.TaxProfileEntity
import de.klartax.app.data.local.entities.VehicleLogEntity
import de.klartax.app.domain.calculator.KlartaxCalculatorEngine
import de.klartax.app.domain.einvoice.EInvoiceGeneratorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class KlartaxCalculatorAndEInvoiceTest {

    private lateinit var calculator: KlartaxCalculatorEngine
    private lateinit var eInvoiceEngine: EInvoiceGeneratorEngine

    @Before
    fun setUp() {
        calculator = KlartaxCalculatorEngine()
        eInvoiceEngine = EInvoiceGeneratorEngine()
    }

    @Test
    fun `test German currency and date formatting with BigDecimal`() {
        val amount = Money.of("1234,56")
        assertEquals(123456L, Money.toCents(amount))
        assertEquals("1.234,56 €", Money.formatEuro(amount))
        assertEquals("07.11.2026", Money.formatDate(LocalDate.of(2026, 11, 7)))
    }

    @Test
    fun `test Sec 11 EStG 10-Tages-Regel around year end`() {
        // Wiederkehrende Ausgabe fällig am 28.12.2026, abgebucht am 05.01.2027 -> gehört wirtschaftlich nach 2026!
        val eval = calculator.evaluateTenDayRuleSec11(
            dueOrInvoiceDate = LocalDate.of(2026, 12, 28),
            paymentDate = LocalDate.of(2027, 1, 5),
            isRecurringPayment = true
        )
        assertTrue(eval.appliedTenDayRule)
        assertEquals(2026, eval.effectiveTaxYear)

        // Nicht wiederkehrende Zahlung am 05.01.2027 -> bleibt nach Zufluss-/Abfluss-Prinzip in 2027
        val nonRecurring = calculator.evaluateTenDayRuleSec11(
            dueOrInvoiceDate = LocalDate.of(2026, 12, 28),
            paymentDate = LocalDate.of(2027, 1, 5),
            isRecurringPayment = false
        )
        assertFalse(nonRecurring.appliedTenDayRule)
        assertEquals(2027, nonRecurring.effectiveTaxYear)
    }

    @Test
    fun `test Hospitality 70-30 split with 100 percent input VAT`() {
        val result = calculator.calculateHospitalitySplit(
            netAmount = Money.of("200.00"),
            vatRatePercent = Money.VAT_19,
            hostName = "Lukas Weber",
            guests = "Dr. Thomas Lindner",
            occasion = "Projekt-Kickoff Klartax"
        )
        assertEquals(Money.of("38.00"), result.vatAmountFull100)
        assertEquals(Money.of("238.00"), result.grossAmount)
        assertEquals(Money.of("140.00"), result.deductibleNet70)
        assertEquals(Money.of("60.00"), result.nonDeductibleNet30)
        assertTrue(result.isGoBdCompliant)
    }

    @Test
    fun `test Digital-AfA 1 year write-off for Googlebook hardware`() {
        val result = calculator.calculateAssetDepreciation(
            acquisitionNet = Money.of("2400.00"),
            purchaseDate = LocalDate.of(2026, 2, 12),
            taxYear = 2026,
            usefulLifeYears = 1,
            privateUsePercent = 10,
            categoryType = "DIGITAL_AFA_1Y"
        )
        assertEquals(Money.of("2400.00"), result.totalYearAfa)
        assertEquals(Money.of("2160.00"), result.deductibleBusinessAfa)
        assertEquals(Money.of("240.00"), result.privateUseDisallowedAfa)
        assertEquals(Money.ZERO, result.remainingBookValue)
    }

    @Test
    fun `test Vehicle method comparison 1 percent electric vs logbook vs mileage`() {
        val vehicle = VehicleLogEntity(
            id = "V1",
            profileId = "P1",
            vehicleModelDe = "Polestar 2",
            grossListPriceNewCents = 54_800_00L,
            isElectricVehicle = true,
            totalAnnualCostNetCents = 7_950_00L,
            totalDrivenKm = 18_000,
            businessDrivenKm = 12_600,
            commuteDistanceOneWayKm = 0,
            commuteDaysPerYear = 0,
            preferredMethod = "AUTO_OPTIMAL",
            updatedAtEpochMillis = 0L
        )
        val comparison = calculator.compareVehicleTaxMethods(vehicle)
        // 54.800 € * 0,25 = 13.700 € Bemessungsgrundlage -> 1 %/Monat = 137 € * 12 = 1.644 € Privatanteil
        // 7.950 € - 1.644 € = 6.306 € Betriebsausgabenabzug bei 1-%-Regelung
        assertEquals(Money.of("6306.00"), comparison.deductionOnePercentRule)
        // Fahrtenbuch: 70 % von 7.950 € = 5.565,00 €
        assertEquals(Money.of("5565.00"), comparison.deductionLogbookMethod)
        // Kilometerpauschale: 12.600 km * 0,30 € = 3.780,00 €
        assertEquals(Money.of("3780.00"), comparison.deductionMileageAllowance)
        assertEquals("1-%-Regelung", comparison.recommendedMethodNameDe)
    }

    @Test
    fun `test Sec 32a EStG tariff and Ehegattensplitting advantage`() {
        // Unterhalb des Grundfreibetrags (11.784 €) = 0 € Steuer
        assertEquals(Money.ZERO, calculator.calculateTariffSec32aSingle(Money.of("11500.00")))

        // Bei asymmetrischem Einkommen (z. B. 90.000 € gemeinsames zvE) bringt Ehegattensplitting deutlichen Vorteil
        val singleTax = calculator.calculateIncomeTaxWithSplittingAndProgression(
            zveRegular = Money.of("90000.00"),
            progressionBenefits = Money.ZERO,
            isSplitting = false
        )
        val splittingTax = calculator.calculateIncomeTaxWithSplittingAndProgression(
            zveRegular = Money.of("90000.00"),
            progressionBenefits = Money.ZERO,
            isSplitting = true
        )
        assertTrue("Splitting-Steuer ($splittingTax) muss niedriger sein als Einzel-Tarif ($singleTax)", splittingTax < singleTax)
    }

    @Test
    fun `test Sec 33 EStG tiered 3-stage reasonable burden calculation`() {
        // Verheiratet, 1 Kind -> Stufensätze: 2 % (bis 15.340 €), 3 % (15.340–51.130 €), 4 % (über 51.130 €)
        // Bei GdE = 60.000 €:
        // Stufe 1: 15.340 * 2 % = 306,80 €
        // Stufe 2: (51.130 - 15.340 = 35.790) * 3 % = 1.073,70 €
        // Stufe 3: (60.000 - 51.130 = 8.870) * 4 % = 354,80 €
        // Summe = 1.735,30 € -> abgerundet auf 1.735,00 €
        val burden = calculator.calculateReasonableBurdenSec33(
            totalIncomeGdE = Money.of("60000.00"),
            isMarriedJoint = true,
            childCount = 1
        )
        assertEquals(Money.of("1735.00"), burden)
    }

    @Test
    fun `test EN 16931 XRechnung and ZUGFeRD generation and roundtrip parsing`() {
        val profile = TaxProfileEntity(
            id = "P1",
            profileTitle = "Test",
            profileSubtitle = "",
            taxYear = 2026,
            taxpayerFirstName = "Lukas",
            taxpayerLastName = "Weber",
            taxIdPrimary = "48920157384",
            taxNumberFreelance = "143/815/08155",
            vatId = "DE314159265",
            isMarriedJointAssessment = true,
            spouseFirstName = "Hannah",
            spouseLastName = "Weber",
            federalStateCode = "BY",
            churchTaxMember = true,
            hasFreelanceActivity = true,
            freelanceProfessionTitle = "IT-Berater",
            isKleinunternehmerSec19 = false,
            skrChartOfAccounts = "SKR03",
            isKskMember = false,
            isVersorgungswerkMember = false,
            businessBankBalanceCents = 100000L,
            incomeTaxPrepaymentsQuarterlyCents = 0L,
            progressionBenefitCents = 0L,
            isActiveProfile = true,
            updatedAtEpochMillis = 0L
        )
        val invoice = InvoiceEntity(
            id = "INV-TEST",
            profileId = "P1",
            customerId = "CUST-1",
            invoiceNumber = "RE-2026-099",
            documentType = "INVOICE",
            eInvoiceStandard = "XRECHNUNG_3_0_CII",
            taxMode = "STANDARD_19",
            customerName = "Musterkunde GmbH",
            customerStreet = "Hauptstr. 1",
            customerPostalCode = "80331",
            customerCity = "München",
            customerCountryIso = "DE",
            customerVatId = "DE111222333",
            buyerReferenceLeitwegId = "991-00000-19",
            issueDateIso = "2026-11-01",
            servicePeriodStartIso = "2026-10-01",
            servicePeriodEndIso = "2026-10-31",
            dueDateIso = "2026-11-15",
            paidDateIso = null,
            netTotalCents = 1_000_00L,
            vatTotalCents = 190_00L,
            grossTotalCents = 1_190_00L,
            status = "SENT_LOCKED",
            dunningLevel = 0,
            sellerIban = "DE89700800000512345600",
            sellerBic = "DRESDEFF700",
            lineItemsJson = "[]",
            embeddedXmlPayload = "",
            isLocked = true,
            stornoOfInvoiceId = null,
            linkedQuoteId = "QUOTE-1",
            linkedQuoteNumber = "AN-2026-001",
            linkedOrderId = "ORDER-1",
            linkedOrderNumber = "AB-2026-001",
            subjectTitleDe = "Rechnung RE-2026-099 (zu Auftrag AB-2026-001 / Angebot AN-2026-001)",
            headerIntroTextDe = "Sehr geehrte Damen und Herren, gemäß Auftrag AB-2026-001 berechnen wir:",
            footerClosingTextDe = "Zahlbar innerhalb von 14 Tagen ohne Abzug.",
            gobdRecordHash = "",
            updatedAtEpochMillis = 0L
        )
        val items = listOf(
            InvoiceLineItem(
                positionNumber = 1,
                title = "Architektur-Consulting",
                description = "Tagessatz",
                quantityHoursOrUnits = "1.00",
                unitCode = "DAY",
                unitPriceNetCents = 1_000_00L,
                vatRatePercent = 19,
                lineNetTotalCents = 1_000_00L,
                articleNumber = "DL-102"
            )
        )

        val xml = eInvoiceEngine.generateEn16931CiiXml(profile, invoice, items)
        assertTrue(xml.contains("CrossIndustryInvoice"))
        assertTrue(xml.contains("991-00000-19"))
        assertTrue(xml.contains("BuyerOrderReferencedDocument"))
        assertTrue(xml.contains("AB-2026-001"))

        val parsed = eInvoiceEngine.parseIncomingEInvoiceXml(xml)
        assertEquals("RE-2026-099", parsed.invoiceNumber)
        assertEquals(Money.of("1000.00"), parsed.netAmount)
        assertEquals(Money.of("190.00"), parsed.vatAmount)
        assertEquals(Money.of("1190.00"), parsed.grossAmount)
        assertEquals("DE89700800000512345600", parsed.sellerIban)
    }

    @Test
    fun `test GoBD SHA-256 hash chain verification and tamper detection`() {
        val h1 = GoBdHashChain.computeEntryHash(
            previousHash = GoBdHashChain.GENESIS_HASH,
            entityType = "EUR_BOOKING",
            entityId = "B1",
            actionType = "LOCK_BOOKING",
            canonicalPayload = "NET:10000",
            timestampEpochMillis = 1000L
        )
        val h2 = GoBdHashChain.computeEntryHash(
            previousHash = h1,
            entityType = "EUR_BOOKING",
            entityId = "B2",
            actionType = "LOCK_BOOKING",
            canonicalPayload = "NET:20000",
            timestampEpochMillis = 2000L
        )

        val validChain = listOf(
            GoBdHashChain.ChainVerifiableEntry("1", GoBdHashChain.GENESIS_HASH, h1, "EUR_BOOKING", "B1", "LOCK_BOOKING", "NET:10000", 1000L),
            GoBdHashChain.ChainVerifiableEntry("2", h1, h2, "EUR_BOOKING", "B2", "LOCK_BOOKING", "NET:20000", 2000L)
        )
        assertTrue(GoBdHashChain.verifyChain(validChain).isValid)

        val tamperedChain = listOf(
            GoBdHashChain.ChainVerifiableEntry("1", GoBdHashChain.GENESIS_HASH, h1, "EUR_BOOKING", "B1", "LOCK_BOOKING", "NET:99999", 1000L),
            GoBdHashChain.ChainVerifiableEntry("2", h1, h2, "EUR_BOOKING", "B2", "LOCK_BOOKING", "NET:20000", 2000L)
        )
        assertFalse(GoBdHashChain.verifyChain(tamperedChain).isValid)
    }
}
