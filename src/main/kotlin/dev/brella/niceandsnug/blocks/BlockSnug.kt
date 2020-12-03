package dev.brella.niceandsnug.blocks

import dev.brella.niceandsnug.NiceAndSnug
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

abstract class BlockSnug(val name: String, material: Material): Block(material) {
    override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
        super.breakBlock(world, pos, state)
    }

    override fun neighborChanged(state: IBlockState, world: World, pos: BlockPos, block: Block, fromPos: BlockPos) {
        super.neighborChanged(state, world, pos, block, fromPos)
    }

    @SideOnly(Side.CLIENT)
    override fun getSelectedBoundingBox(state: IBlockState, world: World, pos: BlockPos): AxisAlignedBB =
            super.getSelectedBoundingBox(state, world, pos)

    init {
        setCreativeTab(NiceAndSnug.creativeTab)
    }
}