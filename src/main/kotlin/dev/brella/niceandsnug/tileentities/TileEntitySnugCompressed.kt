package dev.brella.niceandsnug.tileentities

import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation

open class TileEntitySnugCompressed : TileEntity(), IPersistent {
    companion object {
        public const val BLOCK_NAMESPACE_KEY: String = "block_namespace"
        public const val BLOCK_PATH_KEY: String = "block_path"
        public const val BLOCK_META_KEY: String = "block_meta"
        public const val COMPRESSION_LEVEL_KEY: String = "compression_level"

        inline fun readCompressionLevelFromCompound(compound: NBTTagCompound): Int =
                compound.getByte(COMPRESSION_LEVEL_KEY).toInt()

        fun readBlockStateFromCompound(compound: NBTTagCompound): IBlockState? {
            if (compound.hasKey(BLOCK_NAMESPACE_KEY) && compound.hasKey(BLOCK_PATH_KEY)) {
                val registryName = ResourceLocation(compound.getString(BLOCK_NAMESPACE_KEY), compound.getString(BLOCK_PATH_KEY))
                val block = Block.REGISTRY.getObject(registryName)
                if (block != Blocks.AIR) {
                    return block.getStateFromMeta(compound.getByte(BLOCK_META_KEY).toInt())
                }
            }

            return null
        }


        fun writeStateToCompound(state: IBlockState, compressionLevel: Int, compound: NBTTagCompound): NBTTagCompound {
            val registryName = Block.REGISTRY.getNameForObject(state.block)
            compound.setString(BLOCK_NAMESPACE_KEY, registryName.namespace)
            compound.setString(BLOCK_PATH_KEY, registryName.path)

            compound.setByte(BLOCK_META_KEY, state.block.getMetaFromState(state).toByte())
            compound.setByte(COMPRESSION_LEVEL_KEY, compressionLevel.toByte())

            return compound
        }
    }

    var compressedState: IBlockState? = null
    var compressionLevel: Int = 0

    override fun getUpdateTag(): NBTTagCompound = writeToNBT(NBTTagCompound())
    override fun handleUpdateTag(tag: NBTTagCompound) = readFromNBT(tag)

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        super.writeToNBT(compound)
        return writeToItemBlock(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)
        readFromItemBlock(compound)
    }

    override fun readFromItemBlock(compound: NBTTagCompound) {
        compressedState = readBlockStateFromCompound(compound)
        compressionLevel = readCompressionLevelFromCompound(compound)
    }

    override fun writeToItemBlock(compound: NBTTagCompound): NBTTagCompound {
        compressedState?.let { state -> writeStateToCompound(state, compressionLevel, compound) }

        return compound
    }

    override fun hasFastRenderer(): Boolean = true

    override fun onLoad() {
        compressedState?.let { state ->
            if (world != null)
                compressedState = state.getActualState(world, pos) ?: state
        }
    }
}