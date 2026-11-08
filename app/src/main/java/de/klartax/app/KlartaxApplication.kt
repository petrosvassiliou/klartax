package de.klartax.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import de.klartax.app.data.sync.KlartaxSyncWorker
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class KlartaxApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // Strikte deutsche Lokalisierung (1.234,56 €, TT.MM.JJJJ) im gesamten Prozess
        Locale.setDefault(Locale.GERMANY)
        KlartaxSyncWorker.schedulePeriodicAndImmediateSync(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
