package de.klartax.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.klartax.app.core.security.TinkCryptoManager
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
import net.sqlcipher.database.SupportFactory

/**
 * Lokale, mit SQLCipher (AES-256) verschlüsselte Room-Datenbank (`klartax_encrypted.db`).
 */
@Database(
    entities = [
        TaxProfileEntity::class,
        CustomerEntity::class,
        ServiceCatalogItemEntity::class,
        DocumentTextTemplateEntity::class,
        StationeryConfigEntity::class,
        AuditLogEntity::class,
        EurBookingEntity::class,
        InvoiceEntity::class,
        TimeTrackingEntryEntity::class,
        AssetEntity::class,
        VehicleLogEntity::class,
        BankTransactionEntity::class,
        EmploymentTaxEntity::class,
        SpecialExpenseAndBurdenEntity::class,
        CapitalAndCryptoEntity::class,
        RentalPropertyEntity::class,
        ChildTaxEntity::class,
        PensionAndInsuranceEntity::class,
        TaxAssessmentNoticeEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KlartaxDatabase : RoomDatabase() {
    abstract fun klartaxDao(): KlartaxDao

    companion object {
        const val DATABASE_NAME = "klartax_encrypted.db"

        fun createEncryptedDatabase(
            context: Context,
            tinkCryptoManager: TinkCryptoManager
        ): KlartaxDatabase {
            System.loadLibrary("sqlcipher")
            val passphraseBytes = tinkCryptoManager.getOrCreateDatabasePassphrase()
            val supportFactory = SupportFactory(passphraseBytes)
            return Room.databaseBuilder(
                context.applicationContext,
                KlartaxDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(supportFactory)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
