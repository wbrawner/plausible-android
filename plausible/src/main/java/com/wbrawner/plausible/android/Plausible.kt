package com.wbrawner.plausible.android

import android.content.Context
import android.util.Log
import com.wbrawner.plausible.android.Plausible.init

/**
 * Singleton for sending events to Plausible.
 *
 * If you disable the [PlausibleInitializer] then you
 * must ensure that [init] is called prior to sending events.
 */
object Plausible {
    internal var client: PlausibleClient? = null
    internal var config: PlausibleConfig? = null

    fun init(context: Context) {
        val config = AndroidResourcePlausibleConfig(context)
        val client = NetworkFirstPlausibleClient(config)
        init(client, config)
    }

    internal fun init(client: PlausibleClient, config: PlausibleConfig) {
        this.client = client
        this.config = config
    }

    /**
     * Enable or disable event sending
     */
    @Suppress("unused")
    fun enable(enable: Boolean) {
        config?.let {
            it.enable = enable
        } ?: Log.w(
            "Plausible",
            "Ignoring call to enable(). Did you forget to call Plausible.init()?"
        )
    }

    /**
     * The raw value of User-Agent is used to calculate the user_id which identifies a unique
     * visitor in Plausible.
     * User-Agent is also used to populate the Devices report in your
     * Plausible dashboard. The device data is derived from the open source database
     * device-detector. If your User-Agent is not showing up in your dashboard, it's probably
     * because it is not recognized as one in the device-detector database.
     */
    @Suppress("unused")
    fun setUserAgent(userAgent: String) {
        config?.let {
            it.userAgent = userAgent
        } ?: Log.w(
            "Plausible",
            "Ignoring call to setUserAgent(). Did you forget to call Plausible.init()?"
        )
    }

    /**
     * Send a `pageview` event.
     *
     * @param url URL of the page where the event was triggered. If the URL contains UTM parameters,
     * they will be extracted and stored.
     * The URL parameter will feel strange in a mobile app but you can manufacture something that looks
     * like a web URL. If you name your mobile app screens like page URLs, Plausible will know how to
     * handle it. So for example, on your login screen you could send something like
     * `app://localhost/login`. The pathname (/login) is what will be shown as the page value in the
     * Plausible dashboard.
     * @param referrer Referrer for this event.
     * Plausible uses the open source referer-parser database to parse referrers and assign these
     * source categories.
     * When no match has been found, the value of the referrer field will be parsed as an URL. The
     * hostname will be used as the `visit:source` and the full URL as the `visit:referrer`. So if
     * you send `https://some.domain.com/example-path`, it will be parsed as follows:
     * `visit:source == some.domain.com` `visit:referrer == some.domain.com/example-path`
     * @param props Custom properties for the event. Values must be scalar. See [https://plausible.io/docs/custom-event-goals#using-custom-props](https://plausible.io/docs/custom-event-goals#using-custom-props)
     * for more information.
     */
    fun pageView(
        url: String,
        referrer: String = "",
        props: Map<String, Any?>? = null
    ) = event(
        name = "pageview",
        url = url,
        referrer = referrer,
        props = props
    )

    /**
     * Send a custom event. To send a `pageview` event, consider using [pageView] instead.
     *
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
     * Plausible uses the open source referer-parser database to parse referrers and assign these
     * source categories.
     * When no match has been found, the value of the referrer field will be parsed as an URL. The
     * hostname will be used as the `visit:source` and the full URL as the `visit:referrer`. So if
     * you send `https://some.domain.com/example-path`, it will be parsed as follows:
     * `visit:source == some.domain.com` `visit:referrer == some.domain.com/example-path`
     * @param props Custom properties for the event. Values must be scalar. See [https://plausible.io/docs/custom-event-goals#using-custom-props](https://plausible.io/docs/custom-event-goals#using-custom-props)
     * for more information.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun event(
        name: String,
        url: String,
        referrer: String = "",
        props: Map<String, Any?>? = null
    ) {
        client?.let { client ->
            config?.let { config ->
                client.event(config.domain, name, url, referrer, config.screenWidth, props)
            } ?: Log.w(
                "Plausible",
                "Ignoring call to event(). Did you forget to call Plausible.init()?"
            )
        } ?: Log.w(
            "Plausible",
            "Ignoring call to event(). Did you forget to call Plausible.init()?"
        )
    }
}