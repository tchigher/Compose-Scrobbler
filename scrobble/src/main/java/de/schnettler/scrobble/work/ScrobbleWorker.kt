package de.schnettler.scrobble.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import de.schnettler.database.models.LocalTrack
import de.schnettler.database.models.ScrobbleStatus
import de.schnettler.lastfm.models.Errors
import de.schnettler.repo.ScrobbleRepository
import de.schnettler.repo.mapping.LastFmPostResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.min

const val SUBMIT_CACHED_SCROBBLES_WORK = "submit_cached_scrobbles"
const val MAX_SCROBBLE_BATCH_SIZE = 50

class ScrobbleWorker(
    ctx: Context,
    params: WorkerParameters,
    private val repo: ScrobbleRepository
): CoroutineWorker(ctx, params) {

    private val scrobbledTracks = mutableListOf<LocalTrack>()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        var result = Result.success()
        scrobbledTracks.clear()

        Timber.d("[Scrobble] Started cached scrobble submission")
        val cachedTracks = repo.getCachedTracks()
        Timber.d("[Scrobble] Found ${cachedTracks.size} cached scrobbles")

        if (cachedTracks.size == 1) {
            // 1. Scrobble single Track
            when(val response = repo.createAndSubmitScrobble(cachedTracks.first())) {
                is LastFmPostResponse.ERROR -> result = handleError(response.error)
                is LastFmPostResponse.SUCCESS -> markTracksAsSubmitted(listOf(cachedTracks.first()))
            }
        } else {
            // 2. Scrobble multiple Tracks
            val chunked = cachedTracks.chunked(MAX_SCROBBLE_BATCH_SIZE)
            chunked.forEach {list ->
                Timber.d("[Scrobble] Submitted ${list.size} scrobbles")
                when(val response = repo.submitScrobbles(list)) {
                    is LastFmPostResponse.ERROR -> result = handleError(response.error)
                    is LastFmPostResponse.SUCCESS -> {
                        result = Result.success()
                        val accepted = response.data?.status?.accepted ?: 0
                        Timber.d("[Scrobble] Accepted ${accepted/list.size * 100} %")
                        if (response.data?.status?.accepted == list.size) {
                            //Accepted all
                            markTracksAsSubmitted(list)
                        } else {
                            //Filter accepted tracks, ignore other
                        }

                    }
                }
            }
        }

        if (result is Result.Success) {
            val max = min(5, scrobbledTracks.size)
            val stringMap = scrobbledTracks.subList(0,max).withIndex().associateBy({ it.index.toString() }, { it.value.name })
            val data = Data.Builder().putAll(stringMap).putInt("count", scrobbledTracks.size).build()
            result = Result.success(data)
            Timber.d("Worker returned data ${data.keyValueMap}")
        }

        Timber.d("[Worker] Result: $result")
        return@withContext result
    }

    private fun handleError(error: Errors?): Result {
        return when(error) {
            Errors.OFFLINE, Errors.UNAVAILABLE -> {
                //Cache Scrobble
                Timber.d("Scrobble failed. Service offline")
                Result.retry()
            }
            Errors.SESSION -> {
                //Reauth and retry
                Timber.d("Scrobble failed. Unauthorized")
                Result.retry()
            }
            else -> {
                //Skip this Scrobble
                Result.failure()
            }
        }
    }

    private fun markTracksAsSubmitted(tracks: List<LocalTrack>) {
        scrobbledTracks.addAll(tracks)
        tracks.forEach {
            repo.saveTrack(it.copy(status = ScrobbleStatus.SCROBBLED))
        }
    }
}