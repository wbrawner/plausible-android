package com.wbrawner.plausible.android

import android.net.Uri
import android.util.Log
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation behind [Plausible] singleton. Using an instance under the singleton allows for
 * easier testing.
 */
internal interface PlausibleClient {

    /**
     * See [Plausible.event] for details on parameters.
     * @return true if the event was successfully processed and false if not
     */
    fun event(
        domain: String,
        name: String,
        url: String,
        referrer: String,
        screenWidth: Int,
        props: Map<String, Any?>? = null
    ) {
        var correctedUrl = Uri.parse(url)
        if (correctedUrl.scheme.isNullOrBlank()) {
            correctedUrl = correctedUrl.buildUpon().scheme("app").build()
        }
        if (correctedUrl.authority.isNullOrBlank()) {
            correctedUrl = correctedUrl.buildUpon().authority("localhost").build()
        }
        return event(Event(
            domain,
            name,
            correctedUrl.toString(),
            referrer,
            screenWidth,
            props?.mapValues {
                when (val value = it.value) {
                    is Number -> JsonPrimitive(value)
                    is Boolean -> JsonPrimitive(value)
                    is String -> JsonPrimitive(value)
                    null -> JsonNull
                    else -> {
                        Log.w(
                            "PlausibleClient",
                            "Event props must be scalar. Value for prop \"${it.key}\" will be converted to String"
                        )
                        JsonPrimitive(value.toString())
                    }
                }
            }.run {
                if (this?.isNotEmpty() == true) {
                    Json.encodeToString(this)
                } else {
                    null
                }
            }
        ))
    }

    fun event(event: Event)
}

/**
 * The primary client for sending events to Plausible. It will attempt to send events immediately,
 * caching them to disk to send later upon failure.
 */
internal class NetworkFirstPlausibleClient(private val config: PlausibleConfig) : PlausibleClient {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        coroutineScope.launch {
            config.eventDir.mkdirs()
            config.eventDir.listFiles()?.forEach {
                val event = Event.fromJson(it.readText())
                try {
                    postEvent(event)
                } catch (e: IOException) {
                    return@forEach
                }
                it.delete()
            }
        }
    }

    override fun event(event: Event) {
        coroutineScope.launch {
            suspendEvent(event)
        }
    }

    @VisibleForTesting
    internal suspend fun suspendEvent(event: Event) {
        try {
            postEvent(event)
        } catch (e: IOException) {
            if (!config.retryOnFailure) return
            val file = File(config.eventDir, "event_${System.currentTimeMillis()}.json")
            file.writeText(event.toJson())
            var retryAttempts = 0
            var retryDelay = 1000L
            while (retryAttempts < 5) {
                delay(retryDelay)
                retryDelay = when (retryDelay) {
                    1000L -> 60_000L
                    60_000L -> 360_000L
                    360_000L -> 600_000L
                    else -> break
                }
                try {
                    postEvent(event)
                    file.delete()
                    break
                } catch(e: IOException) {
                    retryAttempts++
                }
            }
        }
    }

    private suspend fun postEvent(event: Event) {
        val body = event.toJson().toRequestBody("application/json".toMediaType())
        val url = config.host
            .toHttpUrl()
            .newBuilder()
            .addPathSegments("api/event")
            .build()
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", config.userAgent)
            .post(body)
            .build()
        suspendCancellableCoroutine {
            val call = okHttpClient.newCall(request)
            it.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("Plausible", "Failed to send event to backend", e)
                    it.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        it.resume(Unit)
                    } else {
                        val e = IOException(
                            "Received unexpected response: ${response.code} ${response.body?.string()}"
                        )
                        onFailure(call, e)
                    }
                }
            })
        }
    }

    companion object {
        val okHttpClient: OkHttpClient = OkHttpClient()
    }
}
