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

import io.nofrills.empress.MutableEmpress
import io.nofrills.empress.backend.MutableEmpressBackend
import kotlinx.coroutines.CoroutineScope

internal class MutableEmpressFragment<E : Any, M : Any, R : Any> :
    RulerFragment<E, M, R, MutableEmpressBackend<E, M, R>, MutableEmpress<E, M, R>>() {
    override fun makeRulerBackend(
        ruler: MutableEmpress<E, M, R>,
        eventHandlerScope: CoroutineScope,
        requestHandlerScope: CoroutineScope,
        storedModels: Collection<M>?
    ): MutableEmpressBackend<E, M, R> {
        return MutableEmpressBackend(
            ruler,
            eventHandlerScope,
            requestHandlerScope,
            storedModels
        )
    }
}
