package com.wbrawner.plausible.android.fake

import com.wbrawner.plausible.android.Event
import com.wbrawner.plausible.android.PlausibleClient

internal class FakePlausibleClient : PlausibleClient {
    val events = mutableListOf<Event>()

    override fun event(event: Event) {
        events.add(event)
    }
}