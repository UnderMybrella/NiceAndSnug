package dev.brella.niceandsnug.proxy

import dev.brella.niceandsnug.NiceAndSnug.MOD_ID
import dev.brella.niceandsnug.ThreadListenerDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraft.world.World
import net.minecraftforge.client.model.ModelLoader

class ClientProxy: SnugProxy() {
    private val minecraftDispatcher: CoroutineDispatcher by lazy { ThreadListenerDispatcher(Minecraft.getMinecraft()) }

    override suspend fun <T> runIn(world: World, block: suspend CoroutineScope.() -> T): T =
            withContext(minecraftDispatcher, block)

    override fun registerItemRenderer(item: Item, meta: Int, id: String) {
        ModelLoader.setCustomModelResourceLocation(item, meta, ModelResourceLocation("$MOD_ID:$id", "inventory"))
    }
}