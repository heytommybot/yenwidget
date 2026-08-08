package com.tommyg.yenwidget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class TrainRefreshWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ok = TrainRepository.refresh(applicationContext)
        YamanoteWidget().updateAll(applicationContext)
        return if (ok) Result.success() else Result.retry()
    }

    companion object {
        const val PERIODIC = "train_refresh"

        fun schedule(context: Context) {
            val wm = WorkManager.getInstance(context)
            val online = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            wm.enqueueUniquePeriodicWork(
                PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<TrainRefreshWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(online)
                    .build()
            )
            wm.enqueueUniqueWork(
                "train_refresh_now",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<TrainRefreshWorker>()
                    .setConstraints(online)
                    .build()
            )
        }
    }
}
