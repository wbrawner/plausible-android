package com.wbrawner.plausible.android

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File


@RunWith(RobolectricTestRunner::class)
internal class NetworkFirstPlausibleClientTest {

    private lateinit var client: NetworkFirstPlausibleClient
    private lateinit var config: PlausibleConfig
    private lateinit var eventDir: File

    @Before
    fun setup() {
        eventDir = File.createTempFile("PlausibleTest", "", null)
        eventDir.delete()
        eventDir.mkdir()
        config = ThreadSafePlausibleConfig(eventDir, SCREEN_WIDTH)
        // TODO("Set up a mock web server and validate the requests received are correct")
    }

    @Ignore("Not yet implemented")
    @Test
    fun `events are sent to server`() = networkFirstClientTest {
        client.event(
            domain = "test.example.com",
            name = "eventUrl",
            url = "referrer",
            referrer = "referrer",
            screenWidth = SCREEN_WIDTH,
            props = mapOf("prop1" to "propVal")
        )
    }

    @Test
    fun `invalid persisted events are deleted`() {
        val invalidEvent = File(eventDir, "invalid-event.json")
        invalidEvent.createNewFile()
        invalidEvent.writeText("invalid jsopn")
        networkFirstClientTest {
            client.event(
                domain = "test.example.com",
                name = "eventUrl",
                url = "referrer",
                referrer = "referrer",
                screenWidth = SCREEN_WIDTH,
                props = mapOf("prop1" to "propVal")
            )
        }
        assertFalse(invalidEvent.exists())
    }

    private fun networkFirstClientTest(test: suspend () -> Unit) = runBlocking {
        client = NetworkFirstPlausibleClient(config, coroutineContext)
        test()
    }
}