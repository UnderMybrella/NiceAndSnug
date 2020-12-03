package dev.brella.niceandsnug.proxy

import com.google.common.collect.BiMap
import dev.brella.niceandsnug.NiceAndSnug
import kotlinx.coroutines.CoroutineScope
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.world.World
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.registries.GameData

abstract class SnugProxy {
    inline val logger get() = NiceAndSnug.logger
    val ITEMS_TO_BLOCKS: BiMap<Item, Block> by lazy { GameData.getBlockItemMap().inverse() }

    abstract suspend fun <T> runIn(world: World, block: suspend CoroutineScope.() -> T): T

    open fun serverStarting(event: FMLServerStartingEvent) {}

    open fun registerItemRenderer(item: Item, meta: Int, id: String) {}
}