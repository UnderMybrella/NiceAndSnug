package dev.brella.niceandsnug

import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.EnumSkyBlock
import net.minecraft.world.World
import net.minecraft.world.WorldType
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.storage.ExtendedBlockStorage
import net.minecraftforge.common.util.BlockSnapshot

fun World.setBlockState(pos: BlockPos, newState: IBlockState, flags: Int, configureTileEntity: (tileEntity: TileEntity) -> Unit): Boolean {
    var pos = pos
    if (this.isOutsideBuildHeight(pos)) {
        return false
    } else if (!this.isRemote && this.worldInfo.terrainType === WorldType.DEBUG_ALL_BLOCK_STATES) {
        return false
    } else {
        val chunk: Chunk = this.getChunk(pos)
        pos = pos.toImmutable() // Forge - prevent mutable BlockPos leaks
        var blockSnapshot: BlockSnapshot? = null
        if (this.captureBlockSnapshots && !this.isRemote) {
            blockSnapshot = BlockSnapshot.getBlockSnapshot(this, pos, flags)
            this.capturedBlockSnapshots.add(blockSnapshot)
        }
        val oldState: IBlockState = getBlockState(pos)
        val oldLight = oldState.getLightValue(this, pos)
        val oldOpacity = oldState.getLightOpacity(this, pos)
        val blockState = chunk.setBlockState(pos, newState, configureTileEntity)
        if (blockState == null) {
            if (blockSnapshot != null) this.capturedBlockSnapshots.remove(blockSnapshot)
            return false
        } else {
            if (newState.getLightOpacity(this, pos) != oldOpacity || newState.getLightValue(this, pos) != oldLight) {
                this.profiler.startSection("checkLight")
                this.checkLight(pos)
                this.profiler.endSection()
            }
            if (blockSnapshot == null) // Don't notify clients or update physics while capturing blockstates
            {
                this.markAndNotifyBlock(pos, chunk, blockState, newState, flags)
            }
            return true
        }
    }
}

fun Chunk.setBlockState(pos: BlockPos, state: IBlockState, configureTileEntity: (tileEntity: TileEntity) -> Unit): IBlockState? {
    val i = pos.x and 15
    val j = pos.y
    val k = pos.z and 15
    val l = k shl 4 or i
    if (j >= this.precipitationHeightMap[l] - 1) {
        this.precipitationHeightMap[l] = -999
    }
    val i1: Int = this.heightMap[l]
    val blockState: IBlockState = this.getBlockState(pos)
    return if (blockState === state) {
        null
    } else {
        val block = state.block
        val block1 = blockState.block
        val k1 = blockState.getLightOpacity(this.world, pos) // Relocate old light value lookup here, so that it is called before TE is removed.
        var extendedblockstorage: ExtendedBlockStorage = storageArrays[j shr 4]
        var flag = false
        if (extendedblockstorage === Chunk.NULL_BLOCK_STORAGE) {
            if (block === Blocks.AIR) {
                return null
            }
            extendedblockstorage = ExtendedBlockStorage(j shr 4 shl 4, this.world.provider.hasSkyLight())
            storageArrays[j shr 4] = extendedblockstorage
            flag = j >= i1
        }
        extendedblockstorage[i, j and 15, k] = state

        //if (block1 != block)
        run {
            if (!this.world.isRemote) {
                if (block1 !== block) //Only fire block breaks when the block changes.
                    block1.breakBlock(this.world, pos, blockState)
                val te: TileEntity? = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
                if (te != null && te.shouldRefresh(this.world, pos, blockState, state)) this.world.removeTileEntity(pos)
            } else if (block1.hasTileEntity(blockState)) {
                val te: TileEntity? = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
                if (te != null && te.shouldRefresh(this.world, pos, blockState, state)) this.world.removeTileEntity(pos)
            }
        }
        if (extendedblockstorage[i, j and 15, k].block !== block) {
            null
        } else {
            // If capturing blocks, only run block physics for TE's. Non-TE's are handled in ForgeHooks.onPlaceItemIntoWorld
            if (!this.world.isRemote && block1 !== block && (!this.world.captureBlockSnapshots || block.hasTileEntity(state))) {
                block.onBlockAdded(this.world, pos, state)
            }

            if (block.hasTileEntity(state)) {
                var newTileEntity: TileEntity? = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
                if (newTileEntity == null) {
                    newTileEntity = block.createTileEntity(this.world, state)?.also(configureTileEntity)
                    this.world.setTileEntity(pos, newTileEntity)
                }

                newTileEntity?.updateContainingBlockInfo()
            }

            //This is moved to after setting the tile entity, so that we can properly check lighting
            if (flag) {
                generateSkylightMap()
            } else {
                val j1 = state.getLightOpacity(this.world, pos)
                if (j1 > 0) {
                    if (j >= i1) {
                        relightBlock(i, j + 1, k)
                    }
                } else if (j == i1 - 1) {
                    this.relightBlock(i, j, k)
                }
                if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                    this.propagateSkylightOcclusion(i, k)
                }
            }

            this.dirty = true
            blockState
        }
    }
}