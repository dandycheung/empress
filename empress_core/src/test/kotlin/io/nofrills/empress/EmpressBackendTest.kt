package io.nofrills.empress

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EmpressBackendTest {
    private lateinit var tested: EmpressBackend<Event, Patch>

    @Before
    fun setUp() {
        tested = DefaultEmpressBackend(Dispatchers.Unconfined, empress)
    }

    @After
    fun tearDown() {
        tested.onDestroy()
    }

    @Test
    fun simpleUsage() {
        assertEquals(Model<Patch>(listOf(Patch.Counter(0))), tested.model)

        val updates = runBlocking {
            val deferredUpdates = async {
                tested.updates().toList()
            }
            launch {
                tested.sendEvent(Event.Increment, closeUpdates = true)
            }
            deferredUpdates.await()
        }

        assertEquals(1, updates.size)
        assertEquals(1, updates[0].model.get<Patch.Counter>().count)
    }

    @Test
    fun simpleUsageWithRequest() {
        val updates = runBlocking {
            val deferredUpdates = async {
                tested.updates().toList()
            }
            launch {
                tested.sendEvent(Event.Send, closeUpdates = true)
            }
            deferredUpdates.await()
        }

        assertEquals(2, updates.size)
        assertEquals(0, updates[0].model.get<Patch.Counter>().count)
        assertEquals(0, updates[0].model.updated().size)
    }

    @Test
    fun delayedFirstObserver() = runBlocking {
        launch {
            tested.sendEvent(Event.Increment)
            tested.sendEvent(Event.Increment)
        }
        val deferredUpdates = async {
            tested.updates().toList()
        }
        launch {
            tested.sendEvent(Event.Decrement, closeUpdates = true)
        }
        val updates = deferredUpdates.await()

        assertEquals(1, updates.size)

        val modelBase = Model<Patch>(listOf(Patch.Counter(0)))
        val expectedModel = Model(modelBase, listOf(Patch.Counter(1)))
        assertEquals(
            Update<Event, Patch>(expectedModel, Event.Decrement),
            updates[0]
        )

        assertEquals(1, tested.model.get<Patch.Counter>().count)
    }

    @Test
    fun twoObservers() {
        val updates = runBlocking {
            val deferredUpdates1 = async {
                tested.updates().toList()
            }
            val deferredUpdates2 = async {
                tested.updates().toList()
            }
            launch {
                tested.sendEvent(Event.Decrement)
                tested.sendEvent(Event.Decrement, closeUpdates = true)
            }
            val updates1 = deferredUpdates1.await()
            val updates2 = deferredUpdates2.await()
            assertEquals(updates1, updates2)
            updates1
        }

        assertEquals(2, updates.size)
        assertEquals(-1, updates[0].model.get<Patch.Counter>().count)
        assertEquals(-2, updates[1].model.get<Patch.Counter>().count)
    }

    @Test
    fun delayedSecondObserver() = runBlocking {
        val deferredUpdates1 = async {
            tested.updates().toList()
        }

        launch {
            tested.sendEvent(Event.Increment)
            tested.sendEvent(Event.Increment)
        }

        val deferredUpdates2 = async {
            tested.updates().toList()
        }

        launch {
            tested.sendEvent(Event.Decrement, closeUpdates = true)
        }

        val updates1 = deferredUpdates1.await()
        val updates2 = deferredUpdates2.await()

        assertEquals(3, updates1.size)
        assertEquals(Event.Increment, updates1[0].event)
        assertEquals(1, updates1[0].model.get<Patch.Counter>().count)
        assertEquals(Event.Increment, updates1[1].event)
        assertEquals(2, updates1[1].model.get<Patch.Counter>().count)

        assertEquals(1, updates2.size)
        assertEquals(Event.Decrement, updates2[0].event)
        assertEquals(1, updates2[0].model.get<Patch.Counter>().count)

        assertEquals(1, tested.model.get<Patch.Counter>().count)
    }

    private val empress = object : Empress<Event, Patch, Request> {
        override fun initializer(): Collection<Patch> = listOf(Patch.Counter(0))

        override fun onEvent(event: Event, model: Model<Patch>): Effect<Patch, Request> {
            return when (event) {
                Event.Decrement -> Effect(model.get<Patch.Counter>().let { it.copy(count = it.count - 1) })
                Event.Increment -> Effect(model.get<Patch.Counter>().let { it.copy(count = it.count + 1) })
                Event.Send -> Effect(request = Request.Send)
                Event.CounterSent -> Effect()
            }
        }

        override suspend fun onRequest(request: Request): Event {
            return when (request) {
                Request.Send -> run {
                    delay(1)
                    Event.CounterSent
                }
            }
        }
    }

    internal sealed class Event {
        object Decrement : Event()
        object Increment : Event()
        object Send : Event()
        object CounterSent : Event()
    }

    internal sealed class Patch {
        data class Counter(val count: Int) : Patch()
    }

    internal sealed class Request {
        object Send : Request()
    }
}