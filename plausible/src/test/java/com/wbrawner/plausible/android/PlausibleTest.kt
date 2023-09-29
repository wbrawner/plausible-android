package com.wbrawner.plausible.android

import com.wbrawner.plausible.android.fake.FakePlausibleClient
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

internal const val SCREEN_WIDTH = 123

@RunWith(RobolectricTestRunner::class)
internal class PlausibleTest {
    lateinit var client: FakePlausibleClient
    lateinit var config: PlausibleConfig
    lateinit var eventDir: File

    @Before
    fun setup() {
        eventDir = File.createTempFile("PlausibleTest", "", null)
        eventDir.delete()
        eventDir.mkdir()
        client = FakePlausibleClient()
        config = ThreadSafePlausibleConfig(eventDir, SCREEN_WIDTH)
        Plausible.init(client, config)
    }

    @Test
    fun `enable is set on config via Plausible`() {
        assertTrue(config.enable)
        Plausible.enable(false)
        assertFalse(config.enable)
    }

    @Test
    fun `user agent is set on config via Plausible`() {
        val oldUserAgent = config.userAgent
        Plausible.setUserAgent("test user agent")
        assertNotEquals(oldUserAgent, config.userAgent)
        assertEquals("test user agent", config.userAgent)
    }

    @Test
    fun `events are sent to client`() {
        config.domain = "test.example.com"
        Plausible.event("eventName", "eventUrl", "referrer", mapOf("prop1" to "propVal"))
        assertEquals(1, client.events.size)
        val event = client.events.first()
        assertEquals("test.example.com", event.domain)
        assertEquals("eventName", event.name)
        assertEquals("app://localhost/eventUrl", event.url)
        assertEquals(SCREEN_WIDTH, event.screenWidth)
        assertEquals("referrer", event.referrer)
        assertEquals(mapOf("prop1" to "propVal"), event.props)
    }
}