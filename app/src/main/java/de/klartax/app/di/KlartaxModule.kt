package de.klartax.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.klartax.app.core.security.TinkCryptoManager
import de.klartax.app.data.local.KlartaxDao
import de.klartax.app.data.local.KlartaxDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KlartaxModule {

    @Provides
    @Singleton
    fun provideEncryptedKlartaxDatabase(
        @ApplicationContext context: Context,
        tinkCryptoManager: TinkCryptoManager
    ): KlartaxDatabase =
        KlartaxDatabase.createEncryptedDatabase(context, tinkCryptoManager)

    @Provides
    @Singleton
    fun provideKlartaxDao(database: KlartaxDatabase): KlartaxDao =
        database.klartaxDao()
}
