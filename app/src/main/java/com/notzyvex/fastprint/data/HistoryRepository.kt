package com.notzyvex.fastprint.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class HistoryRepository(context: Context) {

    private val dao = AppDatabase.get(context).printJobDao()
    private val images = ImageStore(context)

    val jobs: Flow<List<PrintJobEntity>> = dao.observeAll()

    suspend fun add(job: PrintJobEntity): Long = dao.insert(job)

    /** Deletes the row and the image it owned, unless another row still references it. */
    suspend fun delete(job: PrintJobEntity) {
        dao.delete(job.id)
        val stillReferenced = dao.all().any { it.imagePath == job.imagePath }
        if (!stillReferenced) images.delete(job.imagePath)
    }

    suspend fun clear() {
        dao.clear()
        images.pruneOrphans(emptySet())
    }
}
