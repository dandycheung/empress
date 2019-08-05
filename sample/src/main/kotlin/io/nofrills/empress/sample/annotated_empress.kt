/*
 * Copyright 2019 Mateusz Armatys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nofrills.empress.sample

import io.nofrills.empress.Requests
import io.nofrills.empress.annotation.EmpressModule
import io.nofrills.empress.annotation.Initializer
import io.nofrills.empress.annotation.OnEvent
import io.nofrills.empress.annotation.OnRequest
import io.nofrills.empress.test_support.Event
import io.nofrills.empress.test_support.Patch
import io.nofrills.empress.test_support.Request
import kotlinx.coroutines.delay
import kotlin.math.abs

@EmpressModule(Event::class, Patch::class, Request::class)
class AnnotatedEmpress {
    @Initializer
    fun initialCounter() = Patch.Counter(0)

    @Initializer
    fun initialSender() = Patch.Sender(null)

    @OnEvent(Event.Decrement::class) // TODO decide if OnEvent & OnRequest should accept a class; if not, use a method param
    fun onDecrement(event: Event.Decrement, counter: Patch.Counter): Collection<Patch> {
        // here I can receive the patch I want (instead of a whole model);
        // and I return only a list, or a single patch
        return listOf(counter.copy(count = counter.count - 1))
    }

    @OnEvent(Event.SendCounter::class)
    fun onSendCounter(
        counter: Patch.Counter,
        sender: Patch.Sender,
        requests: Requests<Event, Request>
    ): Patch? {
        if (sender.requestId != null) return null
        val requestId = requests.post(Request.SendCounter(counter.count))
        return sender.copy(requestId = requestId)
    }

    @OnEvent(Event.CancelSendingCounter::class)
    fun onCancelSendingCounter(sender: Patch.Sender, requests: Requests<Event, Request>): Patch {
        requests.cancel(sender.requestId)
        return sender.copy(requestId = null)
    }

    @OnRequest(Request.SendCounter::class)
    suspend fun onSendCounter(request: Request.SendCounter): Event {
        delay(abs(request.counterValue) * 1000L)
        return Event.CounterSent
    }
}