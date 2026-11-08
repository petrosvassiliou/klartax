package de.klartax.app.domain.calculator

import de.klartax.app.core.math.Money
import de.klartax.app.data.local.entities.AssetEntity
import de.klartax.app.data.local.entities.CapitalAndCryptoEntity
import de.klartax.app.data.local.entities.ChildTaxEntity
import de.klartax.app.data.local.entities.EmploymentTaxEntity
import de.klartax.app.data.local.entities.EurBookingEntity
import de.klartax.app.data.local.entities.KlartextEurCategory
import de.klartax.app.data.local.entities.PensionAndInsuranceEntity
import de.klartax.app.data.local.entities.RentalPropertyEntity
import de.klartax.app.data.local.entities.SpecialExpenseAndBurdenEntity
import de.klartax.app.data.local.entities.TaxProfileEntity
import de.klartax.app.data.local.entities.VehicleLogEntity
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 2 Rechenkern: `KlartaxCalculatorEngine`
 * Vereint 100 % der EÜR-Engine (§ 4 Abs. 3 EStG, § 11 EStG, Digital-AfA, 70/30 Bewirtung, PKW-Wahlrechner)
 * und der Einkommensteuer-Engine (§ 32a EStG, Splitting, Progressionsvorbehalt, SolZ, KiSt, Anlage N,
 * Anlage Kind, Anlage KAP/SO, Anlage V, § 33 Stufenberechnung, § 35a/c Direktabzug & Steuerrücklagen-Warner).
 *
 * Alle Berechnungen erfolgen strikt mit [BigDecimal] und [RoundingMode.HALF_UP] (niemals Float/Double).
 */
@Singleton
class KlartaxCalculatorEngine @Inject constructor() {

    companion object {
        // Gesetzliche Konstanten & Pauschbeträge (in Euro als BigDecimal)
        val GRUNDFREIBETRAG_SINGLE = BigDecimal("11784.00")
        val ZONE_1_TOP = BigDecimal("17005.00")
        val ZONE_2_TOP = BigDecimal("66760.00")
        val ZONE_3_TOP = BigDecimal("277825.00")

        val ARBEITNEHMER_PAUSCHBETRAG = BigDecimal("1230.00")
        val HOMEOFFICE_DAILY_RATE = BigDecimal("6.00")
        val HOMEOFFICE_MAX_ANNUAL = BigDecimal("1260.00")
        val COMMUTE_RATE_FIRST_20_KM = BigDecimal("0.30")
        val COMMUTE_RATE_FROM_21_KM = BigDecimal("0.38")

        val SPARER_PAUSCHBETRAG_SINGLE = BigDecimal("1000.00")
        val SPARER_PAUSCHBETRAG_JOINT = BigDecimal("2000.00")
        val CRYPTO_FREIGRENZE_SEC23 = BigDecimal("1000.00")

        val UEBUNGSLEITER_FREIBETRAG_SEC3_NR26 = BigDecimal("3000.00")
        val EHRENAMT_FREIBETRAG_SEC3_NR26A = BigDecimal("840.00")

        val KINDERFREIBETRAG_FULL_PER_CHILD = BigDecimal("9540.00") // 6.612 € KFB + 2.928 € BEA
        val ALLEINERZIEHEND_ENTLASTUNG_BASE = BigDecimal("4260.00")
        val ALLEINERZIEHEND_EXTRA_PER_CHILD = BigDecimal("240.00")
        val AUSBILDUNGSFREIBETRAG_ADULT_EXTERNAL = BigDecimal("1200.00")

        val GWG_NET_LIMIT = BigDecimal("800.00")
        val POOL_SAMMELPOSTEN_LIMIT = BigDecimal("1000.00")

        val SEC35A_HOUSEHOLD_MAX_CREDIT = BigDecimal("4000.00")
        val SEC35A_CRAFTSMAN_MAX_CREDIT = BigDecimal("1200.00")
        val SONDERAUSGABEN_PAUSCHBETRAG_SINGLE = BigDecimal("36.00")
        val SONDERAUSGABEN_PAUSCHBETRAG_JOINT = BigDecimal("72.00")
    }

    /**
     * 1. § 11 EStG Zufluss-/Abfluss-Prinzip inkl. 10-Tages-Regel (22.12. – 10.01.):
     * Prüft, ob eine regelmäßig wiederkehrende Einnahme oder Ausgabe wirtschaftlich in ein anderes
     * Veranlagungsjahr gehört, weil Fälligkeit und Zahlung im Zeitraum 22.12. bis 10.01. liegen.
     */
    fun evaluateTenDayRuleSec11(
        dueOrInvoiceDate: LocalDate,
        paymentDate: LocalDate,
        isRecurringPayment: Boolean
    ): TenDayRuleEvaluation {
        if (!isRecurringPayment) {
            return TenDayRuleEvaluation(
                effectiveTaxYear = paymentDate.year,
                appliedTenDayRule = false,
                explanationDe = "Zufluss-/Abfluss-Prinzip (§ 11 EStG): Buchung im Zahlungsjahr ${paymentDate.year}."
            )
        }

        // Fall A: Zahlung zwischen 01.01. und 10.01. des Jahres Y, Fälligkeit zwischen 22.12. (Y-1) und 10.01. (Y),
        // wirtschaftliche Zugehörigkeit zum Vorjahr (Y-1), wenn die Leistung/Fälligkeit aus dem Dezember stammt.
        val paidInEarlyJanuary = paymentDate.monthValue == 1 && paymentDate.dayOfMonth <= 10
        val dueInLateDecemberPriorYear =
            dueOrInvoiceDate.year == paymentDate.year - 1 &&
                dueOrInvoiceDate.monthValue == 12 &&
                dueOrInvoiceDate.dayOfMonth >= 22

        if (paidInEarlyJanuary && dueInLateDecemberPriorYear) {
            val targetYear = paymentDate.year - 1
            return TenDayRuleEvaluation(
                effectiveTaxYear = targetYear,
                appliedTenDayRule = true,
                explanationDe = "§ 11 EStG 10-Tages-Regel aktiv: Zahlung am ${Money.formatDate(paymentDate)} (fällig am ${Money.formatDate(dueOrInvoiceDate)}) wird wirtschaftlich dem Vorjahr $targetYear zugeordnet."
            )
        }

        // Fall B: Zahlung zwischen 22.12. und 31.12. des Jahres Y für eine Fälligkeit zwischen 01.01. und 10.01. (Y+1)
        val paidInLateDecember = paymentDate.monthValue == 12 && paymentDate.dayOfMonth >= 22
        val dueInEarlyJanuaryNextYear =
            dueOrInvoiceDate.year == paymentDate.year + 1 &&
                dueOrInvoiceDate.monthValue == 1 &&
                dueOrInvoiceDate.dayOfMonth <= 10

        if (paidInLateDecember && dueInEarlyJanuaryNextYear) {
            val targetYear = paymentDate.year + 1
            return TenDayRuleEvaluation(
                effectiveTaxYear = targetYear,
                appliedTenDayRule = true,
                explanationDe = "§ 11 EStG 10-Tages-Regel aktiv: Vorauszahlung am ${Money.formatDate(paymentDate)} (fällig am ${Money.formatDate(dueOrInvoiceDate)}) wird dem Folgejahr $targetYear zugeordnet."
            )
        }

        return TenDayRuleEvaluation(
            effectiveTaxYear = paymentDate.year,
            appliedTenDayRule = false,
            explanationDe = "Regulärer Zufluss/Abfluss im Jahr ${paymentDate.year}."
        )
    }

    /**
     * 2. Bewirtungsbeleg-Assistent nach § 4 Abs. 5 Nr. 2 EStG & § 15 Abs. 1a UStG:
     * Berechnet exakt die 70 % abziehbaren und 30 % nicht abziehbaren Netto-Betriebsausgaben
     * bei 100 % Vorsteuerabzug.
     */
    fun calculateHospitalitySplit(
        netAmount: BigDecimal,
        vatRatePercent: BigDecimal,
        hostName: String,
        guests: String,
        occasion: String
    ): HospitalitySplitResult {
        val cleanNet = netAmount.setScale(2, RoundingMode.HALF_UP)
        val vatAmount = Money.percentage(cleanNet, vatRatePercent)
        val grossAmount = cleanNet.add(vatAmount)
        val deductible70Net = Money.percentage(cleanNet, BigDecimal("70.00"))
        val nonDeductible30Net = cleanNet.subtract(deductible70Net)
        val isComplete = hostName.isNotBlank() && guests.isNotBlank() && occasion.isNotBlank()

        return HospitalitySplitResult(
            netAmount = cleanNet,
            vatAmountFull100 = vatAmount,
            grossAmount = grossAmount,
            deductibleNet70 = deductible70Net,
            nonDeductibleNet30 = nonDeductible30Net,
            isGoBdCompliant = isComplete,
            complianceHintDe = if (isComplete) {
                "GoBD-konform: 70 % (${Money.formatEuro(deductible70Net)}) als Betriebsausgabe abziehbar, 100 % Vorsteuer (${Money.formatEuro(vatAmount)}) abziehbar."
            } else {
                "Pflichtangaben fehlen: Bitte Gastgeber, bewirtete Personen und konkreten geschäftlichen Anlass eintragen."
            }
        )
    }

    /**
     * 3. PKW & Mobilitäts-Wahlrechner für Freiberufler (Günstigerprüfung):
     * Vergleicht 1-%-Regelung (inkl. E-Auto-Privileg 0,25 % / 0,5 %), Fahrtenbuch-Methode und
     * Kilometerpauschale (0,30 € / geschäftlichem km).
     */
    fun compareVehicleTaxMethods(vehicle: VehicleLogEntity): VehicleComparisonResult {
        val listPrice = Money.fromCents(vehicle.grossListPriceNewCents)
        val totalAnnualCosts = Money.fromCents(vehicle.totalAnnualCostNetCents)
        val totalKm = vehicle.totalDrivenKm.coerceAtLeast(1)
        val businessKm = vehicle.businessDrivenKm.coerceIn(0, totalKm)

        // Methode 1: 1-%-Regelung (bzw. 0,25 % bei E-Auto <= 70.000 € Bruttolistenpreis, sonst 0,5 %)
        val effectiveListPriceFactor = when {
            !vehicle.isElectricVehicle -> BigDecimal.ONE
            listPrice <= BigDecimal("70000.00") -> BigDecimal("0.25")
            else -> BigDecimal("0.50")
        }
        val adjustedListPrice = listPrice
            .multiply(effectiveListPriceFactor)
            .setScale(0, RoundingMode.DOWN)
            .let { it.divide(BigDecimal("100"), 0, RoundingMode.DOWN).multiply(BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) }

        val monthlyOnePercentPrivate = Money.percentage(adjustedListPrice, BigDecimal("1.00"))
        val monthlyCommuteAddition = adjustedListPrice
            .multiply(BigDecimal("0.0003"))
            .multiply(BigDecimal.valueOf(vehicle.commuteDistanceOneWayKm.toLong()))
            .setScale(2, RoundingMode.HALF_UP)

        val annualPrivateWithdrawalRaw = monthlyOnePercentPrivate
            .add(monthlyCommuteAddition)
            .multiply(BigDecimal("12"))
            .setScale(2, RoundingMode.HALF_UP)

        // Kostendeckelung: Privatanteil darf die Gesamtkosten nicht übersteigen
        val privateWithdrawalCapped = annualPrivateWithdrawalRaw.min(totalAnnualCosts)
        val netDeductionOnePercent = totalAnnualCosts.subtract(privateWithdrawalCapped).max(Money.ZERO)

        // Methode 2: Fahrtenbuch-Methode (exakter betrieblicher Nutzungsanteil)
        val businessShareRatio = BigDecimal.valueOf(businessKm.toLong())
            .divide(BigDecimal.valueOf(totalKm.toLong()), 6, RoundingMode.HALF_UP)
        val netDeductionLogbook = totalAnnualCosts.multiply(businessShareRatio).setScale(2, RoundingMode.HALF_UP)

        // Methode 3: Kilometerpauschale für Geschäftsreisen im Privat-PKW (0,30 € / km)
        val netDeductionMileage = BigDecimal.valueOf(businessKm.toLong())
            .multiply(COMMUTE_RATE_FIRST_20_KM)
            .setScale(2, RoundingMode.HALF_UP)

        val candidates = listOf(
            "1-%-Regelung" to netDeductionOnePercent,
            "Fahrtenbuch-Methode" to netDeductionLogbook,
            "Kilometerpauschale (0,30 €/km)" to netDeductionMileage
        )
        val best = candidates.maxBy { it.second }
        val worst = candidates.minBy { it.second }

        return VehicleComparisonResult(
            deductionOnePercentRule = netDeductionOnePercent,
            deductionLogbookMethod = netDeductionLogbook,
            deductionMileageAllowance = netDeductionMileage,
            recommendedMethodNameDe = best.first,
            maxDeductibleAmount = best.second,
            advantageOverWorstEuro = best.second.subtract(worst.second)
        )
    }

    /**
     * 4. Anlagenverzeichnis & AfA-Rechner (Digital-AfA 1 Jahr, GWG <= 800 €, Sammelposten 5 Jahre, lineare AfA).
     */
    fun calculateAssetDepreciation(
        acquisitionNet: BigDecimal,
        purchaseDate: LocalDate,
        taxYear: Int,
        usefulLifeYears: Int,
        privateUsePercent: Int,
        categoryType: String
    ): AssetDepreciationResult {
        val cleanNet = acquisitionNet.setScale(2, RoundingMode.HALF_UP)
        val businessSharePercent = BigDecimal.valueOf((100 - privateUsePercent.coerceIn(0, 100)).toLong())

        val fullYearAfa = when (categoryType) {
            "DIGITAL_AFA_1Y" -> {
                // BMF-Schreiben zur Digital-AfA: 1 Jahr Nutzungsdauer = 100 % im Anschaffungsjahr
                if (purchaseDate.year == taxYear) cleanNet else Money.ZERO
            }
            "GWG_800" -> {
                if (purchaseDate.year == taxYear && cleanNet <= GWG_NET_LIMIT) cleanNet else Money.ZERO
            }
            "POOL_5Y" -> {
                val yearsElapsed = taxYear - purchaseDate.year
                if (yearsElapsed in 0..4) {
                    Money.percentage(cleanNet, BigDecimal("20.00"))
                } else {
                    Money.ZERO
                }
            }
            else -> {
                val life = usefulLifeYears.coerceAtLeast(1)
                val annualLinear = cleanNet.divide(BigDecimal.valueOf(life.toLong()), 2, RoundingMode.HALF_UP)
                if (purchaseDate.year == taxYear) {
                    val monthsUsed = (13 - purchaseDate.monthValue).coerceIn(1, 12)
                    annualLinear.multiply(BigDecimal.valueOf(monthsUsed.toLong()))
                        .divide(BigDecimal("12"), 2, RoundingMode.HALF_UP)
                } else if (taxYear > purchaseDate.year && taxYear < purchaseDate.year + life) {
                    annualLinear
                } else {
                    Money.ZERO
                }
            }
        }

        val deductibleBusinessAfa = Money.percentage(fullYearAfa, businessSharePercent)
        val remainingBookValue = cleanNet.subtract(fullYearAfa).max(Money.ZERO)

        return AssetDepreciationResult(
            totalYearAfa = fullYearAfa,
            deductibleBusinessAfa = deductibleBusinessAfa,
            privateUseDisallowedAfa = fullYearAfa.subtract(deductibleBusinessAfa),
            remainingBookValue = remainingBookValue
        )
    }

    /**
     * 5. § 32a EStG Einkommensteuer-Grundtarif (für ein einzelnes zu versteuerndes Einkommen).
     * Verwendet ausschließlich BigDecimal-Arithmetik und rundet den Tarifbetrag nach § 32a Abs. 1 S. 6 EStG
     * auf volle Euro ab.
     */
    fun calculateTariffSec32aSingle(zveInput: BigDecimal): BigDecimal {
        val zve = Money.floorToFullEuro(zveInput.max(Money.ZERO))
        if (zve <= GRUNDFREIBETRAG_SINGLE) {
            return Money.ZERO
        }

        val rawTax = when {
            zve <= ZONE_1_TOP -> {
                // y = (zvE - 11.784) / 10.000; ESt = (954,80 * y + 1.400) * y
                val y = zve.subtract(GRUNDFREIBETRAG_SINGLE)
                    .divide(BigDecimal("10000"), 8, RoundingMode.HALF_UP)
                BigDecimal("954.80").multiply(y)
                    .add(BigDecimal("1400.00"))
                    .multiply(y)
            }
            zve <= ZONE_2_TOP -> {
                // z = (zvE - 17.005) / 10.000; ESt = (181,19 * z + 2.397) * z + 991,21
                val z = zve.subtract(ZONE_1_TOP)
                    .divide(BigDecimal("10000"), 8, RoundingMode.HALF_UP)
                BigDecimal("181.19").multiply(z)
                    .add(BigDecimal("2397.00"))
                    .multiply(z)
                    .add(BigDecimal("991.21"))
            }
            zve <= ZONE_3_TOP -> {
                // ESt = 0,42 * zvE - 10.636,31
                zve.multiply(BigDecimal("0.42"))
                    .subtract(BigDecimal("10636.31"))
            }
            else -> {
                // Reichensteuer-Zone (45 %): ESt = 0,45 * zvE - 18.971,06
                zve.multiply(BigDecimal("0.45"))
                    .subtract(BigDecimal("18971.06"))
            }
        }

        return Money.floorToFullEuro(rawTax.max(Money.ZERO))
    }

    /**
     * 6. Ehegattensplitting (§ 32a Abs. 5 EStG) inkl. Progressionsvorbehalt (§ 32b EStG):
     * Berechnet: ESt_Splitting = 2 * Tarif(zvE / 2).
     */
    fun calculateIncomeTaxWithSplittingAndProgression(
        zveRegular: BigDecimal,
        progressionBenefits: BigDecimal,
        isSplitting: Boolean
    ): BigDecimal {
        val cleanZve = Money.floorToFullEuro(zveRegular.max(Money.ZERO))
        if (cleanZve <= Money.ZERO) return Money.ZERO

        val baseTax = { targetZve: BigDecimal ->
            if (isSplitting) {
                val halfZve = targetZve.divide(BigDecimal("2"), 0, RoundingMode.FLOOR)
                calculateTariffSec32aSingle(halfZve).multiply(BigDecimal("2")).setScale(2, RoundingMode.HALF_UP)
            } else {
                calculateTariffSec32aSingle(targetZve)
            }
        }

        if (progressionBenefits <= Money.ZERO) {
            return baseTax(cleanZve)
        }

        // § 32b EStG Progressionsvorbehalt: Besonderer Steuersatz auf (zvE + Lohnersatzleistungen)
        val combinedZve = cleanZve.add(Money.floorToFullEuro(progressionBenefits))
        val combinedTax = baseTax(combinedZve)
        val specialRatePercent = combinedTax
            .multiply(Money.ONE_HUNDRED)
            .divide(combinedZve, 4, RoundingMode.FLOOR)

        val taxWithProgression = cleanZve
            .multiply(specialRatePercent)
            .divide(Money.ONE_HUNDRED, 2, RoundingMode.HALF_UP)
        return Money.floorToFullEuro(taxWithProgression)
    }

    /**
     * 7. Gestaffelte 3-Stufen-Berechnung der zumutbaren Belastung nach § 33 Abs. 3 EStG (BFH-Rechtsprechung):
     * Stufe 1: bis 15.340 €
     * Stufe 2: 15.340,01 € bis 51.130 €
     * Stufe 3: über 51.130 €
     */
    fun calculateReasonableBurdenSec33(
        totalIncomeGdE: BigDecimal,
        isMarriedJoint: Boolean,
        childCount: Int
    ): BigDecimal {
        val income = totalIncomeGdE.max(Money.ZERO)
        if (income <= Money.ZERO) return Money.ZERO

        val (rateTier1, rateTier2, rateTier3) = when {
            childCount >= 3 -> Triple(BigDecimal("1.00"), BigDecimal("1.00"), BigDecimal("2.00"))
            childCount in 1..2 -> Triple(BigDecimal("2.00"), BigDecimal("3.00"), BigDecimal("4.00"))
            isMarriedJoint -> Triple(BigDecimal("4.00"), BigDecimal("5.00"), BigDecimal("6.00"))
            else -> Triple(BigDecimal("5.00"), BigDecimal("6.00"), BigDecimal("7.00"))
        }

        val tier1Limit = BigDecimal("15340.00")
        val tier2Limit = BigDecimal("51130.00")

        val slice1 = income.min(tier1Limit)
        val slice2 = income.subtract(tier1Limit).max(Money.ZERO).min(tier2Limit.subtract(tier1Limit))
        val slice3 = income.subtract(tier2Limit).max(Money.ZERO)

        val burden = Money.percentage(slice1, rateTier1)
            .add(Money.percentage(slice2, rateTier2))
            .add(Money.percentage(slice3, rateTier3))

        return Money.floorToFullEuro(burden)
    }

    /**
     * 8. Solidaritätszuschlag (SolZ) inkl. Freigrenze (18.130 € / 36.260 €) und Milderungszone (11,9 %).
     */
    fun calculateSolidaritaetszuschlag(
        assessedIncomeTaxForSoli: BigDecimal,
        isMarriedJoint: Boolean
    ): BigDecimal {
        val freigrenze = if (isMarriedJoint) BigDecimal("36260.00") else BigDecimal("18130.00")
        if (assessedIncomeTaxForSoli <= freigrenze) {
            return Money.ZERO
        }
        val fullSoli55 = Money.percentage(assessedIncomeTaxForSoli, BigDecimal("5.50"))
        val excess = assessedIncomeTaxForSoli.subtract(freigrenze)
        val milderungsSoli = Money.percentage(excess, BigDecimal("11.90"))
        return fullSoli55.min(milderungsSoli).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * 9. Kirchensteuer (8 % in Bayern & Baden-Württemberg, 9 % in den übrigen 14 Bundesländern).
     */
    fun calculateChurchTax(
        assessedIncomeTaxForChurch: BigDecimal,
        federalStateCode: String,
        isChurchMember: Boolean
    ): BigDecimal {
        if (!isChurchMember || assessedIncomeTaxForChurch <= Money.ZERO) {
            return Money.ZERO
        }
        val rate = if (federalStateCode == "BY" || federalStateCode == "BW") {
            BigDecimal("8.00")
        } else {
            BigDecimal("9.00")
        }
        return Money.percentage(assessedIncomeTaxForChurch, rate)
    }

    /**
     * 10. Gesamtberechnung (Die 1-Klick-Brücke zwischen Modul A SevDesk EÜR und Modul B WISO Steuer):
     * Berechnet den kompletten Steuerfall inkl. Live-Erstattungs-/Nachzahlungs-Tacho,
     * USt-Zahllast und Freiberufler-Steuerrücklagen-Warner.
     */
    fun calculateFullTaxSummary(
        profile: TaxProfileEntity,
        eurBookings: List<EurBookingEntity>,
        assets: List<AssetEntity>,
        vehicleLog: VehicleLogEntity?,
        employmentList: List<EmploymentTaxEntity>,
        specialExpenses: List<SpecialExpenseAndBurdenEntity>,
        capitalAndCrypto: List<CapitalAndCryptoEntity>,
        rentalProperties: List<RentalPropertyEntity>,
        children: List<ChildTaxEntity>,
        pensionAndInsurance: PensionAndInsuranceEntity?
    ): ComprehensiveTaxSummary {

        // --- A. Modul A: EÜR (§ 4 Abs. 3 EStG) & Anlage S ---
        val yearBookings = eurBookings.filter { it.effectiveTaxYear == profile.taxYear }

        var eurRevenueNet = Money.ZERO
        var vatCollected = Money.ZERO
        var eurOperatingExpensesNet = Money.ZERO
        var vatPaidInput = Money.ZERO
        var taxFreeAllowanceApplied = Money.ZERO

        for (booking in yearBookings) {
            val net = Money.fromCents(booking.netAmountCents)
            val deductibleNet = Money.fromCents(booking.deductibleNetCents)
            val vat = Money.fromCents(booking.vatAmountCents)

            if (booking.category.isIncome) {
                eurRevenueNet = eurRevenueNet.add(net)
                vatCollected = vatCollected.add(vat)
                if (booking.taxFreeAllowanceType == "UEBUNGSLEITER_SEC3_NR26") {
                    taxFreeAllowanceApplied = taxFreeAllowanceApplied.add(net)
                        .min(UEBUNGSLEITER_FREIBETRAG_SEC3_NR26)
                } else if (booking.taxFreeAllowanceType == "EHRENAMT_SEC3_NR26A") {
                    taxFreeAllowanceApplied = taxFreeAllowanceApplied.add(net)
                        .min(EHRENAMT_FREIBETRAG_SEC3_NR26A)
                }
            } else {
                eurOperatingExpensesNet = eurOperatingExpensesNet.add(deductibleNet)
                vatPaidInput = vatPaidInput.add(vat)
            }
        }

        // AfA aus Anlagenverzeichnis (sofern nicht bereits als Einzelbuchung enthalten)
        val totalAssetAfa = assets.fold(Money.ZERO) { acc, asset ->
            acc.add(Money.fromCents(asset.deductibleBusinessDepreciationCents))
        }

        val vehicleComparison = vehicleLog?.let { compareVehicleTaxMethods(it) }

        // USt-Saldo (Vereinnahmte USt abzgl. abziehbarer Vorsteuer)
        val vatPayableBalance = if (profile.isKleinunternehmerSec19) {
            Money.ZERO
        } else {
            vatCollected.subtract(vatPaidInput)
        }

        // Gewinn aus selbständiger Arbeit (Anlage EÜR -> Anlage S)
        val eurTaxableProfit = if (profile.hasFreelanceActivity) {
            eurRevenueNet
                .subtract(taxFreeAllowanceApplied)
                .subtract(eurOperatingExpensesNet)
                .max(Money.ZERO)
        } else {
            Money.ZERO
        }

        // --- B. Modul B: Anlage N (Arbeitnehmer & Werbungskosten-Günstigerprüfung) ---
        var totalGrossSalary = Money.ZERO
        var totalWithheldWageTax = Money.ZERO
        var totalWithheldSoli = Money.ZERO
        var totalWithheldChurchTax = Money.ZERO
        var totalTaxableEmploymentIncome = Money.ZERO
        var totalWerbungskostenApplied = Money.ZERO

        for (emp in employmentList) {
            val gross = Money.fromCents(emp.grossSalaryCents)
            totalGrossSalary = totalGrossSalary.add(gross)
            totalWithheldWageTax = totalWithheldWageTax.add(Money.fromCents(emp.withheldIncomeTaxCents))
            totalWithheldSoli = totalWithheldSoli.add(Money.fromCents(emp.withheldSoliCents))
            totalWithheldChurchTax = totalWithheldChurchTax.add(Money.fromCents(emp.withheldChurchTaxCents))

            // Entfernungspauschale (0,30 € für km 1..20, 0,38 € ab km 21) vs. ÖPNV-Kosten
            val first20Km = emp.commuteDistanceOneWayKm.coerceIn(0, 20)
            val above20Km = (emp.commuteDistanceOneWayKm - 20).coerceAtLeast(0)
            val dailyDistanceAllowance = BigDecimal.valueOf(first20Km.toLong()).multiply(COMMUTE_RATE_FIRST_20_KM)
                .add(BigDecimal.valueOf(above20Km.toLong()).multiply(COMMUTE_RATE_FROM_21_KM))
            val distanceAllowanceAnnual = dailyDistanceAllowance
                .multiply(BigDecimal.valueOf(emp.commuteWorkdaysPerYear.toLong()))
                .setScale(2, RoundingMode.HALF_UP)
            val publicTransportCost = Money.fromCents(emp.publicTransportAnnualCostCents)
            val optimalCommuteDeduction = distanceAllowanceAnnual.max(publicTransportCost)

            // Homeoffice-Pauschale (6 €/Tag bis 1.260 €) vs. Arbeitszimmer
            val homeOfficeFlat = BigDecimal.valueOf(emp.homeOfficeDays.coerceIn(0, 210).toLong())
                .multiply(HOMEOFFICE_DAILY_RATE)
                .min(HOMEOFFICE_MAX_ANNUAL)
            val roomCost = Money.fromCents(emp.dedicatedHomeOfficeRoomCostCents)
            val optimalHomeOfficeDeduction = homeOfficeFlat.max(roomCost)

            val itemizedWerbungskosten = optimalCommuteDeduction
                .add(optimalHomeOfficeDeduction)
                .add(Money.fromCents(emp.workEquipmentCostCents))
                .add(Money.fromCents(emp.trainingAndEducationCostCents))
                .add(Money.fromCents(emp.applicationCostCents))
                .add(Money.fromCents(emp.travelMealAllowanceCents))
                .add(Money.fromCents(emp.accountMaintenanceFeeCents))

            // Günstigerprüfung gegen Arbeitnehmer-Pauschbetrag (1.230 €)
            val effectiveWerbungskosten = itemizedWerbungskosten.max(ARBEITNEHMER_PAUSCHBETRAG)
            totalWerbungskostenApplied = totalWerbungskostenApplied.add(effectiveWerbungskosten)
            totalTaxableEmploymentIncome = totalTaxableEmploymentIncome.add(
                gross.subtract(effectiveWerbungskosten).max(Money.ZERO)
            )
        }

        // --- C. Anlage V (Vermietung & Verpachtung) ---
        var totalRentalIncome = Money.ZERO
        for (prop in rentalProperties) {
            val rentIn = Money.fromCents(prop.annualColdRentReceivedCents)
                .add(Money.fromCents(prop.annualAncillaryCostsReceivedCents))
            val buildingAfa = Money.fromCents(prop.buildingAcquisitionCostCents)
                .multiply(BigDecimal.valueOf(prop.afaRateBasisPoints.toLong()))
                .divide(BigDecimal("10000"), 2, RoundingMode.HALF_UP)
            val spreadYears = prop.maintenanceSpreadYearsSec82b.coerceIn(1, 5)
            val annualMaintenance = Money.fromCents(prop.maintenanceTotalCents)
                .divide(BigDecimal.valueOf(spreadYears.toLong()), 2, RoundingMode.HALF_UP)
            val totalPropExpenses = buildingAfa
                .add(Money.fromCents(prop.mortgageInterestPaidCents))
                .add(Money.fromCents(prop.allocablePropertyCostsCents))
                .add(annualMaintenance)
            totalRentalIncome = totalRentalIncome.add(rentIn.subtract(totalPropExpenses))
        }

        // --- D. Anlage KAP & Anlage SO (Krypto mit 1-Jahres-Haltefrist) ---
        var cryptoTaxableGainSec23 = Money.ZERO
        var withheldKapTaxCredit = Money.ZERO
        val sparerPauschbetragMax = if (profile.isMarriedJointAssessment) {
            SPARER_PAUSCHBETRAG_JOINT
        } else {
            SPARER_PAUSCHBETRAG_SINGLE
        }

        for (cap in capitalAndCrypto) {
            withheldKapTaxCredit = withheldKapTaxCredit.add(Money.fromCents(cap.withheldKapEstCents))
            if (cap.cryptoPurchaseDateIso != null && cap.cryptoSaleDateIso != null) {
                val buyDate = LocalDate.parse(cap.cryptoPurchaseDateIso)
                val sellDate = LocalDate.parse(cap.cryptoSaleDateIso)
                val holdingDays = ChronoUnit.DAYS.between(buyDate, sellDate)
                val gain = Money.fromCents(cap.cryptoGainOrLossCents)
                // § 23 Abs. 1 Nr. 2 EStG: Nach > 365 Tagen Haltedauer komplett steuerfrei!
                // Unter 1 Jahr: Freigrenze von 1.000 € nach § 23 Abs. 3 S. 5 EStG
                if (holdingDays <= 365 && gain >= CRYPTO_FREIGRENZE_SEC23) {
                    cryptoTaxableGainSec23 = cryptoTaxableGainSec23.add(gain)
                }
            }
        }

        // --- E. Gesamtbetrag der Einkünfte (GdE) ---
        val totalIncomeGdE = eurTaxableProfit
            .add(totalTaxableEmploymentIncome)
            .add(totalRentalIncome)
            .add(cryptoTaxableGainSec23)
            .max(Money.ZERO)

        // --- F. Vorsorgeaufwendungen, Sonderausgaben, § 33 & Anlage Kind ---
        val pensionDeduction = pensionAndInsurance?.let { p ->
            Money.fromCents(p.statutoryPensionOrVersorgungswerkEmployeeCents)
                .add(Money.fromCents(p.ruerupBasisrenteContributionsCents))
                .add(Money.fromCents(p.healthInsuranceBasicTariffCents))
                .add(Money.fromCents(p.nursingCareInsuranceCents))
                .add(Money.fromCents(p.riesterContributionsCents))
        } ?: Money.ZERO

        var donationsAndChurchSonderausgaben = Money.ZERO
        var medicalBurdenGross = Money.ZERO
        var disabilityPauschbetrag = Money.ZERO
        var sec35aHouseholdLabor = Money.ZERO
        var sec35aCraftsmanLabor = Money.ZERO
        var sec35cEnergeticDeduction = Money.ZERO

        for (item in specialExpenses) {
            val eligible = Money.fromCents(item.eligibleLaborAndMachineCents)
            when (item.recordCategory) {
                "DONATION_SPENDE", "CHURCH_TAX_PAID" -> {
                    donationsAndChurchSonderausgaben = donationsAndChurchSonderausgaben.add(eligible)
                }
                "MEDICAL_BURDEN_SEC33" -> {
                    medicalBurdenGross = medicalBurdenGross.add(eligible)
                }
                "DISABILITY_PAUSCHBETRAG" -> {
                    disabilityPauschbetrag = disabilityPauschbetrag.add(eligible)
                }
                "HOUSEHOLD_SERVICE_SEC35A", "NEBENKOSTENABRECHNUNG_SEC35A" -> {
                    if (item.paymentMethodBankTransfer) {
                        sec35aHouseholdLabor = sec35aHouseholdLabor.add(eligible)
                    }
                }
                "CRAFTSMAN_HANDWERKER_SEC35A" -> {
                    if (item.paymentMethodBankTransfer) {
                        sec35aCraftsmanLabor = sec35aCraftsmanLabor.add(eligible)
                    }
                }
                "ENERGETIC_SEC35C" -> {
                    sec35cEnergeticDeduction = sec35cEnergeticDeduction.add(eligible)
                }
            }
        }

        val sonderausgabenMin = if (profile.isMarriedJointAssessment) {
            SONDERAUSGABEN_PAUSCHBETRAG_JOINT
        } else {
            SONDERAUSGABEN_PAUSCHBETRAG_SINGLE
        }
        val effectiveSonderausgaben = donationsAndChurchSonderausgaben.max(sonderausgabenMin)

        // § 33 EStG Zumutbare Belastung in 3 Stufen
        val reasonableBurdenSec33 = calculateReasonableBurdenSec33(
            totalIncomeGdE = totalIncomeGdE,
            isMarriedJoint = profile.isMarriedJointAssessment,
            childCount = children.size
        )
        val deductibleMedicalSec33 = medicalBurdenGross.subtract(reasonableBurdenSec33).max(Money.ZERO)

        // Kinderbetreuungskosten (80 % bis 4.800 € je Kind) & Entlastungsbetrag Alleinerziehende
        var childcareDeductionTotal = Money.ZERO
        var singleParentReliefTotal = Money.ZERO
        var kindergeldReceivedTotal = Money.ZERO

        for ((idx, child) in children.withIndex()) {
            kindergeldReceivedTotal = kindergeldReceivedTotal.add(Money.fromCents(child.kindergeldReceivedAnnualCents))
            val childcare80 = Money.percentage(Money.fromCents(child.childcareExpenseTotalCents), BigDecimal("80.00"))
                .min(BigDecimal("4800.00"))
            childcareDeductionTotal = childcareDeductionTotal.add(childcare80)

            if (child.isSingleParentHousehold && !profile.isMarriedJointAssessment) {
                singleParentReliefTotal = if (idx == 0) {
                    ALLEINERZIEHEND_ENTLASTUNG_BASE
                } else {
                    singleParentReliefTotal.add(ALLEINERZIEHEND_EXTRA_PER_CHILD)
                }
            }
        }

        // Zu versteuerndes Einkommen (zvE) ohne Kinderfreibetrag
        val zveBeforeChildAllowance = totalIncomeGdE
            .subtract(pensionDeduction)
            .subtract(effectiveSonderausgaben)
            .subtract(childcareDeductionTotal)
            .subtract(deductibleMedicalSec33)
            .subtract(disabilityPauschbetrag)
            .subtract(singleParentReliefTotal)
            .max(Money.ZERO)

        // Zu versteuerndes Einkommen (zvE) MIT Kinderfreibetrag (für Günstigerprüfung & SolZ/KiSt nach § 51a EStG)
        val totalChildAllowance = KINDERFREIBETRAG_FULL_PER_CHILD
            .multiply(BigDecimal.valueOf(children.size.toLong()))
        val zveWithChildAllowance = zveBeforeChildAllowance.subtract(totalChildAllowance).max(Money.ZERO)

        val progressionEuro = Money.fromCents(profile.progressionBenefitCents)

        val tariffTaxWithoutChildAllowance = calculateIncomeTaxWithSplittingAndProgression(
            zveRegular = zveBeforeChildAllowance,
            progressionBenefits = progressionEuro,
            isSplitting = profile.isMarriedJointAssessment
        )
        val tariffTaxWithChildAllowance = calculateIncomeTaxWithSplittingAndProgression(
            zveRegular = zveWithChildAllowance,
            progressionBenefits = progressionEuro,
            isSplitting = profile.isMarriedJointAssessment
        )

        // Anlage Kind Günstigerprüfung (§ 31 EStG): Ist die Steuerersparnis durch den Kinderfreibetrag > Kindergeld?
        val childTaxSaving = tariffTaxWithoutChildAllowance.subtract(tariffTaxWithChildAllowance)
        val childAllowanceMoreFavorable = children.isNotEmpty() && childTaxSaving > kindergeldReceivedTotal
        val tariffIncomeTaxAfterKind = if (childAllowanceMoreFavorable) {
            tariffTaxWithChildAllowance.add(kindergeldReceivedTotal)
        } else {
            tariffTaxWithoutChildAllowance
        }

        // Live-Vergleich Ehegattensplitting vs. Einzelveranlagung
        val singleComparisonTax = calculateIncomeTaxWithSplittingAndProgression(
            zveRegular = zveBeforeChildAllowance,
            progressionBenefits = progressionEuro,
            isSplitting = false
        )
        val splittingAdvantageEuro = if (profile.isMarriedJointAssessment) {
            singleComparisonTax.subtract(tariffTaxWithoutChildAllowance).max(Money.ZERO)
        } else {
            Money.ZERO
        }

        // § 35a & § 35c EStG Direkter Abzug von der Tarifsteuer (20 % der Lohn-/Maschinenkosten)
        val creditHouseholdSec35a = Money.percentage(sec35aHouseholdLabor, BigDecimal("20.00"))
            .min(SEC35A_HOUSEHOLD_MAX_CREDIT)
        val creditCraftsmanSec35a = Money.percentage(sec35aCraftsmanLabor, BigDecimal("20.00"))
            .min(SEC35A_CRAFTSMAN_MAX_CREDIT)
        val creditEnergeticSec35c = Money.percentage(sec35cEnergeticDeduction, BigDecimal("7.00"))
        val totalDirectTaxCreditsSec35a = creditHouseholdSec35a
            .add(creditCraftsmanSec35a)
            .add(creditEnergeticSec35c)

        // Festzusetzende Einkommensteuer (mindestens 0,00 €)
        val assessedIncomeTax = tariffIncomeTaxAfterKind
            .subtract(totalDirectTaxCreditsSec35a)
            .max(Money.ZERO)

        // Bemessungsgrundlage für SolZ und Kirchensteuer nach § 51a EStG (immer unter Abzug der Kinderfreibeträge)
        val baseTaxForSoliAndChurch = tariffTaxWithChildAllowance
            .subtract(totalDirectTaxCreditsSec35a)
            .max(Money.ZERO)

        val assessedSoli = calculateSolidaritaetszuschlag(
            assessedIncomeTaxForSoli = baseTaxForSoliAndChurch,
            isMarriedJoint = profile.isMarriedJointAssessment
        )
        val assessedChurchTax = calculateChurchTax(
            assessedIncomeTaxForChurch = baseTaxForSoliAndChurch,
            federalStateCode = profile.federalStateCode,
            isChurchMember = profile.churchTaxMember
        )

        val totalTaxLiability = assessedIncomeTax.add(assessedSoli).add(assessedChurchTax)

        // Bereits gezahlte Lohnsteuer, Vorauszahlungen, SolZ & Kirchensteuer
        val quarterlyPrepaymentsTotal = Money.fromCents(profile.incomeTaxPrepaymentsQuarterlyCents)
        val totalTaxesAlreadyPaid = totalWithheldWageTax
            .add(totalWithheldSoli)
            .add(totalWithheldChurchTax)
            .add(quarterlyPrepaymentsTotal)

        // Live-Erstattungs- / Nachzahlungs-Saldo (Positiv = Erstattung vom Finanzamt, Negativ = Nachzahlung)
        val refundOrPaymentBalance = totalTaxesAlreadyPaid.subtract(totalTaxLiability)

        // Freiberufler-Steuerrücklagen-Rechner:
        // Wie viel Euro vom aktuellen Geschäftskonto sollten für ESt-Abschlusszahlung + nächste Vorauszahlung + USt-Zahllast reserviert werden?
        val unpaidIncomeTaxShare = totalTaxLiability.subtract(totalTaxesAlreadyPaid).max(Money.ZERO)
        val positiveVatDue = vatPayableBalance.max(Money.ZERO)
        val recommendedTaxReserve = if (profile.hasFreelanceActivity) {
            unpaidIncomeTaxShare
                .add(positiveVatDue)
                .add(Money.percentage(eurTaxableProfit, BigDecimal("22.00")))
        } else {
            unpaidIncomeTaxShare
        }

        val bankBalance = Money.fromCents(profile.businessBankBalanceCents)
        val freeLiquidAfterTaxReserve = bankBalance.subtract(recommendedTaxReserve).max(Money.ZERO)

        return ComprehensiveTaxSummary(
            taxYear = profile.taxYear,
            eurRevenueNet = eurRevenueNet,
            eurDeductibleExpensesNet = eurOperatingExpensesNet,
            eurAssetDepreciation = totalAssetAfa,
            eurTaxableProfitAnlageS = eurTaxableProfit,
            vatCollected = vatCollected,
            vatPaidInput = vatPaidInput,
            vatPayableBalance = vatPayableBalance,
            employmentGrossTotal = totalGrossSalary,
            employmentWerbungskostenApplied = totalWerbungskostenApplied,
            employmentTaxableAnlageN = totalTaxableEmploymentIncome,
            rentalTaxableAnlageV = totalRentalIncome,
            cryptoTaxableAnlageSO = cryptoTaxableGainSec23,
            totalIncomeGdE = totalIncomeGdE,
            pensionDeductionVorsorge = pensionDeduction,
            sonderausgabenApplied = effectiveSonderausgaben,
            reasonableBurdenSec33 = reasonableBurdenSec33,
            deductibleMedicalBurdenSec33 = deductibleMedicalSec33,
            taxableIncomeZvE = zveBeforeChildAllowance,
            splittingAdvantageEuro = splittingAdvantageEuro,
            childAllowanceMoreFavorable = childAllowanceMoreFavorable,
            childTaxBenefitEuro = childTaxSaving.max(kindergeldReceivedTotal),
            sec35aDirectTaxCredit = totalDirectTaxCreditsSec35a,
            assessedIncomeTax = assessedIncomeTax,
            assessedSoli = assessedSoli,
            assessedChurchTax = assessedChurchTax,
            totalTaxLiability = totalTaxLiability,
            totalTaxesAlreadyPaid = totalTaxesAlreadyPaid,
            refundOrPaymentBalance = refundOrPaymentBalance,
            recommendedTaxReserveEuro = recommendedTaxReserve,
            freeBankLiquidityAfterReserveEuro = freeLiquidAfterTaxReserve,
            vehicleComparison = vehicleComparison
        )
    }
}

data class TenDayRuleEvaluation(
    val effectiveTaxYear: Int,
    val appliedTenDayRule: Boolean,
    val explanationDe: String
)

data class HospitalitySplitResult(
    val netAmount: BigDecimal,
    val vatAmountFull100: BigDecimal,
    val grossAmount: BigDecimal,
    val deductibleNet70: BigDecimal,
    val nonDeductibleNet30: BigDecimal,
    val isGoBdCompliant: Boolean,
    val complianceHintDe: String
)

data class VehicleComparisonResult(
    val deductionOnePercentRule: BigDecimal,
    val deductionLogbookMethod: BigDecimal,
    val deductionMileageAllowance: BigDecimal,
    val recommendedMethodNameDe: String,
    val maxDeductibleAmount: BigDecimal,
    val advantageOverWorstEuro: BigDecimal
)

data class AssetDepreciationResult(
    val totalYearAfa: BigDecimal,
    val deductibleBusinessAfa: BigDecimal,
    val privateUseDisallowedAfa: BigDecimal,
    val remainingBookValue: BigDecimal
)

data class ComprehensiveTaxSummary(
    val taxYear: Int,
    val eurRevenueNet: BigDecimal,
    val eurDeductibleExpensesNet: BigDecimal,
    val eurAssetDepreciation: BigDecimal,
    val eurTaxableProfitAnlageS: BigDecimal,
    val vatCollected: BigDecimal,
    val vatPaidInput: BigDecimal,
    val vatPayableBalance: BigDecimal,
    val employmentGrossTotal: BigDecimal,
    val employmentWerbungskostenApplied: BigDecimal,
    val employmentTaxableAnlageN: BigDecimal,
    val rentalTaxableAnlageV: BigDecimal,
    val cryptoTaxableAnlageSO: BigDecimal,
    val totalIncomeGdE: BigDecimal,
    val pensionDeductionVorsorge: BigDecimal,
    val sonderausgabenApplied: BigDecimal,
    val reasonableBurdenSec33: BigDecimal,
    val deductibleMedicalBurdenSec33: BigDecimal,
    val taxableIncomeZvE: BigDecimal,
    val splittingAdvantageEuro: BigDecimal,
    val childAllowanceMoreFavorable: Boolean,
    val childTaxBenefitEuro: BigDecimal,
    val sec35aDirectTaxCredit: BigDecimal,
    val assessedIncomeTax: BigDecimal,
    val assessedSoli: BigDecimal,
    val assessedChurchTax: BigDecimal,
    val totalTaxLiability: BigDecimal,
    val totalTaxesAlreadyPaid: BigDecimal,
    val refundOrPaymentBalance: BigDecimal, // > 0 = Steuererstattung, < 0 = Nachzahlung
    val recommendedTaxReserveEuro: BigDecimal,
    val freeBankLiquidityAfterReserveEuro: BigDecimal,
    val vehicleComparison: VehicleComparisonResult?
)
