package com.wbrawner.plausible.android

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * @param domain Domain name of the site in Plausible.
 * @param name Name of the event. Can specify `pageview` which is a special type of event in
 * Plausible. All other names will be treated as custom events.
 * @param url URL of the page where the event was triggered. If the URL contains UTM parameters,
 * they will be extracted and stored.
 * The URL parameter will feel strange in a mobile app but you can manufacture something that looks
 * like a web URL. If you name your mobile app screens like page URLs, Plausible will know how to
 * handle it. So for example, on your login screen you could send something like
 * `app://localhost/login`. The pathname (/login) is what will be shown as the page value in the
 * Plausible dashboard.
 * @param referrer Referrer for this event.
 * @param screenWidth Width of the screen in dp.
 * @param props Custom properties for the event. See [https://plausible.io/docs/custom-event-goals#using-custom-props](https://plausible.io/docs/custom-event-goals#using-custom-props)
 */
@Serializable
internal data class Event(
    val domain: String,
    val name: String,
    val url: String,
    val referrer: String,
    @SerialName("screen_width")
    val screenWidth: Int,
    val props: Map<String, String>?
) {
    companion object {
        fun fromJson(json: String): Event? = try {
            Json.decodeFromString(json)
        } catch (ignored: Exception) {
            null
        }
    }
}

internal fun Event.toJson(): String = Json.encodeToString(this)
