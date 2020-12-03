package dev.brella.niceandsnug.items

import com.google.common.cache.CacheBuilder
import dev.brella.niceandsnug.NiceAndSnug
import dev.brella.niceandsnug.nbt.distinctHashCode
import dev.brella.niceandsnug.tileentities.TileEntitySnugCompressed
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.item.ItemStack
import java.util.concurrent.TimeUnit

object ItemBlockCompressedRenderer: TileEntityItemStackRenderer() {
    val renderItem by lazy { Minecraft.getMinecraft().renderItem }
    val modelMesher by lazy { renderItem.itemModelMesher }

    val cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build<Int, IBakedModel>()

    override fun renderByItem(stack: ItemStack) {
        if (stack.item is ItemBlockCompressed) {
            val persistent = ItemBlockPersistentTE.persistentNBTFromStack(stack) ?: return

//            compressed.compressedState =
//            TileEntityRendererDispatcher.instance.render(compressed, 0.0, 0.0, 0.0, 0.0f, 1f)
//
//            override fun renderTileEntityFast(te: TileEntitySnugCompressed, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, partial: Float, buffer: BufferBuilder) {
//                val state = te.compressedState
//                if (state == null) {
//                    NiceAndSnug.logger.error("Tile entity at $x,$y,$z has no compressed state; this is a bug!")
//                    return
//                }
//
//                val model = shapes.getModelForState(state)
//                val texture = shapes.getTexture(state)
//
//                buffer.setTranslation((-te.pos.x).toDouble() + x, (-te.pos.y).toDouble() + y, (-te.pos.z).toDouble() + z)
//                renderer.renderModel(te.world, model, state, te.pos, buffer, te.hasWorld())
//            }

            val model: IBakedModel = cache.get(persistent.distinctHashCode()) {
                val blockState = TileEntitySnugCompressed.readBlockStateFromCompound(persistent) ?: return@get modelMesher.modelManager.missingModel

                modelMesher.getItemModel(ItemStack(blockState.block, 1, blockState.block.getMetaFromState(blockState)))
            }

            renderItem.renderModel(model, -1)
        }
    }
}