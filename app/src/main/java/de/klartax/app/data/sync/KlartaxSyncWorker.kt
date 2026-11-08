package de.klartax.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.klartax.app.data.local.KlartaxDao
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

/**
 * Hintergrund-Synchronisation über Android WorkManager (`NetworkType.CONNECTED`).
 * Sorgt dafür, dass z. B. am Smartphone gescannte Belege verschlüsselt als Delta-Batch
 * abgelegt und am Googlebook automatisch zusammengeführt werden.
 */
@HiltWorker
class KlartaxSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dao: KlartaxDao,
    private val driveSyncRepository: DriveSyncRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val activeProfile = dao.observeActiveProfile().firstOrNull()
            ?: return Result.success()

        return driveSyncRepository.performGoogleDriveAppDataDeltaSync(
            driveService = null,
            activeProfileId = activeProfile.id
        ).fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }

    companion object {
        private const val UNIQUE_PERIODIC_WORK_NAME = "klartax_zero_knowledge_drive_sync"

        fun schedulePeriodicAndImmediateSync(context: Context) {
            val connectedConstraint = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<KlartaxSyncWorker>(
                6, TimeUnit.HOURS
            )
                .setConstraints(connectedConstraint)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        fun enqueueImmediateDeltaSync(context: Context) {
            val connectedConstraint = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<KlartaxSyncWorker>()
                .setConstraints(connectedConstraint)
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeRequest)
        }
    }
}
