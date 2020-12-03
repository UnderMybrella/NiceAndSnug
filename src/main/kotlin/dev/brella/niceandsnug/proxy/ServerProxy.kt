package dev.brella.niceandsnug.proxy

import dev.brella.niceandsnug.ThreadListenerDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.withContext
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class ServerProxy : SnugProxy() {
    private val worldDispatchers: MutableMap<Int, CoroutineDispatcher> = ConcurrentHashMap()

    private var fallbackDispatcher: CoroutineDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            logger.error(IllegalStateException("Error, attempting to dispatch $block on a main thread before server has started, this is a very bad bug!").fillInStackTrace())
        }
    }

    override fun serverStarting(event: FMLServerStartingEvent) {
        fallbackDispatcher = ThreadListenerDispatcher(event.server)
    }

    override suspend fun <T> runIn(world: World, block: suspend CoroutineScope.() -> T): T =
            withContext(when (world) {
                is WorldServer -> worldDispatchers.computeIfAbsent(world.provider.dimension) { ThreadListenerDispatcher(world) }
                else -> fallbackDispatcher
            }, block)
}