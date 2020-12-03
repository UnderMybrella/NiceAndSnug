package dev.brella.niceandsnug.blocks

import dev.brella.niceandsnug.NiceAndSnug
import dev.brella.niceandsnug.NiceAndSnug.MOD_ID
import dev.brella.niceandsnug.tileentities.TileEntitySnugCompressed
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import kotlin.math.pow

class BlockSnugCompressed : BlockSnug("compressed", Material.ROCK), ITileEntityProvider {
    companion object {
        val META_PROPERTY = PropertyInteger.create("meta", 0, 15)
    }

    override fun createNewTileEntity(worldIn: World, meta: Int): TileEntitySnugCompressed =
            TileEntitySnugCompressed()

    inline fun getTileEntity(world: IBlockAccess, pos: BlockPos): TileEntitySnugCompressed? =
            world.getTileEntity(pos) as? TileEntitySnugCompressed

    override fun getExtendedState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState =
            getTileEntity(world, pos)?.compressedState ?: state

    override fun getActualState(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): IBlockState =
            getTileEntity(worldIn, pos)?.compressedState ?: state

    override fun getStateFromMeta(meta: Int): IBlockState =
            defaultState.withProperty(META_PROPERTY, meta)

    override fun getMetaFromState(state: IBlockState): Int =
            if (state.block !is BlockSnugCompressed) state.block.getMetaFromState(state) else state.getValue(META_PROPERTY)

    override fun getBlockHardness(blockState: IBlockState, worldIn: World, pos: BlockPos): Float {
        val te = getTileEntity(worldIn, pos) ?: return super.getBlockHardness(blockState, worldIn, pos)
        val teState = te.compressedState ?: return super.getBlockHardness(blockState, worldIn, pos)

        //We wrap this in a try catch **just in case** a mod does something screwy
        return try {
            teState.getBlockHardness(worldIn, pos) * te.compressionLevel
        } catch (cce: ClassCastException) {
            super.getBlockHardness(blockState, worldIn, pos)
        }
    }

    override fun getRenderType(state: IBlockState): EnumBlockRenderType =
            EnumBlockRenderType.ENTITYBLOCK_ANIMATED

    override fun shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean =
            getTileEntity(blockAccess, pos)
                    ?.let { te -> te.compressedState?.shouldSideBeRendered(blockAccess, pos, side) }
                    ?: super.shouldSideBeRendered(blockState, blockAccess, pos, side)

    override fun doesSideBlockRendering(state: IBlockState, world: IBlockAccess, pos: BlockPos, face: EnumFacing): Boolean =
            if (state.block !is BlockSnugCompressed) state.block.doesSideBlockRendering(state, world, pos, face)
            else getTileEntity(world, pos)
                    ?.let { te -> te.compressedState?.doesSideBlockRendering(world, pos, face) }
                    ?: super.doesSideBlockRendering(state, world, pos, face)

    override fun createBlockState(): BlockStateContainer =
            BlockStateContainer(this, META_PROPERTY)

    override fun onNeighborChange(world: IBlockAccess, pos: BlockPos, neighbor: BlockPos) {
        getTileEntity(world, pos)?.let { te ->
            val before = te.compressedState
            te.compressedState = te.compressedState?.getActualState(world, pos)
            if (before != te.compressedState) te.markDirty()
        }
    }

    override fun getLightOpacity(state: IBlockState, world: IBlockAccess, pos: BlockPos): Int =
            if (state.block !is BlockSnugCompressed) state.block.getLightOpacity(state, world, pos)
            else getTileEntity(world, pos)
                    ?.let { te -> te.compressedState?.getLightOpacity(world, pos) }
                    ?: super.getLightOpacity(state, world, pos)

    override fun getAmbientOcclusionLightValue(state: IBlockState): Float =
            if (state.block !is BlockSnugCompressed) state.block.getAmbientOcclusionLightValue(state)
            else super.getAmbientOcclusionLightValue(state)

    override fun getLightValue(state: IBlockState, world: IBlockAccess, pos: BlockPos): Int =
            getTileEntity(world, pos)
                    ?.let { te -> te.compressedState?.getLightValue(world, pos)?.times(NiceAndSnug.lightMultiplier.pow(te.compressionLevel))?.toInt()?.coerceAtMost(15) }
                    ?: super.getLightValue(state, world, pos)

    override fun getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB? =
            getTileEntity(worldIn, pos)
                    ?.let { te -> te.compressedState?.getCollisionBoundingBox(worldIn, pos) }
                    ?: super.getCollisionBoundingBox(blockState, worldIn, pos)

    override fun getSelectedBoundingBox(state: IBlockState, world: World, pos: BlockPos): AxisAlignedBB =
            getTileEntity(world, pos)
                    ?.let { te -> te.compressedState?.getSelectedBoundingBox(world, pos) }
                    ?: super.getSelectedBoundingBox(state, world, pos)

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
            getTileEntity(source, pos)
                    ?.let { te -> te.compressedState?.getBoundingBox(source, pos) }
                    ?: super.getBoundingBox(state, source, pos)

    override fun getEnchantPowerBonus(world: World, pos: BlockPos): Float =
            getTileEntity(world, pos)
                    ?.let { te -> te.compressedState?.block?.getEnchantPowerBonus(world, pos)?.times(NiceAndSnug.enchantPowerBonusMultiplier.pow(te.compressionLevel)) }
                    ?: super.getEnchantPowerBonus(world, pos)

    override fun isBeaconBase(worldObj: IBlockAccess, pos: BlockPos, beacon: BlockPos): Boolean =
            getTileEntity(worldObj, pos)
                    ?.let { te -> te.compressedState?.block?.isBeaconBase(worldObj, pos, beacon) }
                    ?: super.isBeaconBase(worldObj, pos, beacon)

    init {
        translationKey = "${MOD_ID}:$name"
        registryName = ResourceLocation(MOD_ID, name)
        defaultState = createBlockState().baseState.withProperty(META_PROPERTY, 0)
    }
}