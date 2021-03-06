/*
 * Copyright (C) 2018 Touchlab, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.touchlab.sqliter

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import kotlin.native.concurrent.AtomicLong

abstract class NativePointer(nativePointerArg: Long) {
    //Hold connection pointer in atomic and guard access to connection
    private val nativePointerActual = AtomicLong(nativePointerArg)

    private val pointerLock = Lock()
    internal val nativePointer: Long
        get() = pointerLock.withLock {
            val now = nativePointerActual.value
            if (now == 0L)
                throw IllegalStateException("Pointer closed")
            return now
        }

    /**
     * Attempt to run 'actualClose' first. That may fail, in which case we don't want to mark
     * the pointer as closed
     */
    fun closeNativePointer() = pointerLock.withLock {
        val local = nativePointerActual.value
        actualClose(local)
        nativePointerActual.value = 0
    }

    val pointerClosed: Boolean
        get() = nativePointerActual.value == 0L

    abstract fun actualClose(nativePointerArg: Long)
}
