package com.wbrawner.plausible.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wbrawner.plausible.android.fake.FakePlausibleClient
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

internal const val SCREEN_WIDTH = 123

@RunWith(AndroidJUnit4::class)
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
    fun enable_is_sent_to_config() {
        assertTrue(config.enable)
        Plausible.enable(false)
        assertFalse(config.enable)
    }

    @Test
    fun user_agent_is_sent_to_config() {
        val oldUserAgent = config.userAgent
        Plausible.setUserAgent("test user agent")
        assertNotEquals(oldUserAgent, config.userAgent)
        assertEquals("test user agent", config.userAgent)
    }

    @Test
    fun events_are_sent_to_client() {
        config.domain = "test.example.com"
        Plausible.event("eventName", "eventUrl", "referrer", mapOf("prop1" to "propVal"))
        assertEquals(1, client.events.size)
        val event = client.events.first()
        assertEquals("test.example.com", event.domain)
        assertEquals("eventName", event.name)
        assertEquals("app://localhost/eventUrl", event.url)
        assertEquals(123, event.screenWidth)
        assertEquals("referrer", event.referrer)
        assertEquals("{\"prop1\":\"propVal\"}", event.props)
    }
}