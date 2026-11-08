package de.klartax.app.core.math

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Präzise Finanz-Mathematik für Klartax (GoBD- & EStG-konform).
 * Verwendet im gesamten Projekt ausschließlich [BigDecimal] mit [RoundingMode.HALF_UP]
 * bzw. [Long] (Euro-Cent) für die Persistenz in Room. Niemals Float oder Double!
 */
object Money {
    val ZERO: BigDecimal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
    val ONE_HUNDRED: BigDecimal = BigDecimal("100.00")

    val VAT_19: BigDecimal = BigDecimal("19.00")
    val VAT_7: BigDecimal = BigDecimal("7.00")
    val VAT_0: BigDecimal = BigDecimal("0.00")

    private val germanSymbols = DecimalFormatSymbols(Locale.GERMANY).apply {
        decimalSeparator = ','
        groupingSeparator = '.'
    }

    private val germanCurrencyFormat = ThreadLocal.withInitial {
        DecimalFormat("#,##0.00 '€'", germanSymbols)
    }

    private val germanPercentFormat = ThreadLocal.withInitial {
        DecimalFormat("#,##0.00 '%'", germanSymbols)
    }

    val germanDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY)

    fun of(euroString: String): BigDecimal =
        BigDecimal(euroString.trim().replace(",", ".")).setScale(2, RoundingMode.HALF_UP)

    fun ofEuros(wholeEuros: Long): BigDecimal =
        BigDecimal.valueOf(wholeEuros).setScale(2, RoundingMode.HALF_UP)

    fun fromCents(cents: Long): BigDecimal =
        BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.HALF_UP)

    fun toCents(amount: BigDecimal): Long =
        amount.setScale(2, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()

    /**
     * Abrundung auf volle Euro zu Gunsten des Steuerpflichtigen (z. B. beim Tarif nach § 32a EStG).
     */
    fun floorToFullEuro(amount: BigDecimal): BigDecimal =
        amount.setScale(0, RoundingMode.FLOOR).setScale(2, RoundingMode.HALF_UP)

    /**
     * Aufrundung auf volle Euro (z. B. bei bestimmten Pauschbeträgen oder zumutbarer Belastung).
     */
    fun ceilToFullEuro(amount: BigDecimal): BigDecimal =
        amount.setScale(0, RoundingMode.CEILING).setScale(2, RoundingMode.HALF_UP)

    fun percentage(base: BigDecimal, ratePercent: BigDecimal): BigDecimal =
        base.multiply(ratePercent).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)

    fun netToGross(net: BigDecimal, vatRatePercent: BigDecimal): BigDecimal {
        val vat = percentage(net, vatRatePercent)
        return net.setScale(2, RoundingMode.HALF_UP).add(vat)
    }

    fun grossToNet(gross: BigDecimal, vatRatePercent: BigDecimal): BigDecimal {
        val divisor = ONE_HUNDRED.add(vatRatePercent).divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP)
        return gross.divide(divisor, 2, RoundingMode.HALF_UP)
    }

    fun formatEuro(amount: BigDecimal): String =
        germanCurrencyFormat.get()!!.format(amount.setScale(2, RoundingMode.HALF_UP))

    fun formatCents(cents: Long): String =
        formatEuro(fromCents(cents))

    fun formatPercent(ratePercent: BigDecimal): String =
        germanPercentFormat.get()!!.format(ratePercent.setScale(2, RoundingMode.HALF_UP))

    fun formatDate(date: LocalDate): String =
        date.format(germanDateFormatter)

    fun parseIsoDate(isoDate: String): LocalDate =
        LocalDate.parse(isoDate)
}
