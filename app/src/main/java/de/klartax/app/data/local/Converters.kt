package de.klartax.app.data.local

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? =
        value?.setScale(2, RoundingMode.HALF_UP)?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? =
        value?.let { BigDecimal(it).setScale(2, RoundingMode.HALF_UP) }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? =
        date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }
}
