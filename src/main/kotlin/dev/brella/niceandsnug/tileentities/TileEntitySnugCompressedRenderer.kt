package dev.brella.niceandsnug.tileentities

import dev.brella.niceandsnug.NiceAndSnug
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraftforge.client.model.animation.FastTESR

object TileEntitySnugCompressedRenderer: FastTESR<TileEntitySnugCompressed>() {
    val dispatcher by lazy { Minecraft.getMinecraft().blockRendererDispatcher }
    val shapes by lazy { dispatcher.blockModelShapes }
    val renderer by lazy { dispatcher.blockModelRenderer }

    override fun renderTileEntityFast(te: TileEntitySnugCompressed, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, partial: Float, buffer: BufferBuilder) {
        val state = te.compressedState
        if (state == null) {
            NiceAndSnug.logger.error("Tile entity at $x,$y,$z has no compressed state; this is a bug!")
            return
        }

        val model = shapes.getModelForState(state)
        val texture = shapes.getTexture(state)

        buffer.setTranslation((-te.pos.x).toDouble() + x, (-te.pos.y).toDouble() + y, (-te.pos.z).toDouble() + z)
        renderer.renderModel(te.world, model, state, te.pos, buffer, te.hasWorld())
    }
}