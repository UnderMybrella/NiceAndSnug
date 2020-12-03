package dev.brella.niceandsnug

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import net.minecraft.util.IThreadListener
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

class ThreadListenerDispatcher(val listener: WeakReference<IThreadListener>): CoroutineDispatcher() {
    constructor(listener: IThreadListener): this(WeakReference(listener))

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        listener.get()?.addScheduledTask(block)
    }
}