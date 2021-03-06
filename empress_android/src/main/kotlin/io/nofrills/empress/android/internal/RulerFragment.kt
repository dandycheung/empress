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

package io.nofrills.empress.android.internal

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import io.nofrills.empress.Ruler
import io.nofrills.empress.android.RulerSpec
import io.nofrills.empress.backend.RulerBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.util.*

internal abstract class RulerFragment<E : Any, M : Any, R : Any, B : RulerBackend<E, M, R>, RL : Ruler<E, M, R>> :
    Fragment() {
    internal lateinit var backend: B
        private set
    private val job = Job()
    private var storedModels: ArrayList<M>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNCHECKED_CAST")
        storedModels =
            savedInstanceState?.getParcelableArrayList<Parcelable>(MODELS_KEY) as ArrayList<M>?
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val parcelablePatches = arrayListOf<Parcelable>()
        val models = backend.models().all()
        for (model in models) {
            if (model is Parcelable) {
                parcelablePatches.add(model)
            }
        }
        outState.putParcelableArrayList(MODELS_KEY, parcelablePatches)
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    internal fun initialize(rulerFactory: () -> RulerSpec<E, M, R, RL>) {
        if (!this::backend.isInitialized) {
            val result = rulerFactory()
            val eventHandlerScope = CoroutineScope(result.eventDispatcher + job)
            val requestHandlerScope = CoroutineScope(result.requestDispatcher + job)
            backend =
                makeRulerBackend(result.ruler, eventHandlerScope, requestHandlerScope, storedModels)
        }
    }

    protected abstract fun makeRulerBackend(
        ruler: RL,
        eventHandlerScope: CoroutineScope,
        requestHandlerScope: CoroutineScope,
        storedModels: Collection<M>?
    ): B

    companion object {
        private const val MODELS_KEY = "io.nofrills.models"
    }
}
