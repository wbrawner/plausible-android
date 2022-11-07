package com.wbrawner.plausible.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4::class)
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
        TODO("Set up a mock web server and validate the requests received are correct")
    }

    @Ignore("Not yet implemented")
    @Test
    fun events_are_sent_to_server() = networkFirstClientTest {
        client.event(
            domain = "test.example.com",
            name = "eventUrl",
            url = "referrer",
            referrer = "referrer",
            screenWidth = SCREEN_WIDTH,
            props = mapOf("prop1" to "propVal")
        )
    }

    private fun networkFirstClientTest(test: suspend () -> Unit) = runBlocking {
        client = NetworkFirstPlausibleClient(config, coroutineContext)
        test()
    }
}