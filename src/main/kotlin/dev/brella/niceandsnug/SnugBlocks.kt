package dev.brella.niceandsnug

import dev.brella.niceandsnug.NiceAndSnug.MOD_ID
import dev.brella.niceandsnug.blocks.BlockSnugCompressed
import dev.brella.niceandsnug.items.ItemBlockCompressed
import dev.brella.niceandsnug.items.ItemBlockPersistentTE
import dev.brella.niceandsnug.tileentities.TileEntitySnugCompressed
import dev.brella.niceandsnug.tileentities.TileEntitySnugCompressedRenderer
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.registries.IForgeRegistry

object SnugBlocks {
    val snugCompressed = BlockSnugCompressed()

    fun register(registry: IForgeRegistry<Block>) {
        registry.register(snugCompressed)
    }

    fun registerItemBlocks(registry: IForgeRegistry<Item>) {
        registry.register(ItemBlockCompressed(snugCompressed).setRegistryName(MOD_ID, snugCompressed.name))
    }

    fun registerModels() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySnugCompressed::class.java, TileEntitySnugCompressedRenderer)
        repeat(16) { i -> NiceAndSnug.proxy.registerItemRenderer(Item.getItemFromBlock(snugCompressed), i, snugCompressed.name) }
    }
}