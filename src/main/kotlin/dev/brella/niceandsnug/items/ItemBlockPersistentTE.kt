package dev.brella.niceandsnug.items

import dev.brella.niceandsnug.nbt.getInformation
import dev.brella.niceandsnug.nbt.getStringArray
import dev.brella.niceandsnug.setBlockState
import dev.brella.niceandsnug.tileentities.IPersistent
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class ItemBlockPersistentTE(block: Block): ItemBlock(block) {
    companion object {
        const val PERSISTENT_TE_KEY = "persistent_te"

        inline fun persistentNBTFromStack(stack: ItemStack): NBTTagCompound? =
                stack.getSubCompound("persistent_te")

        inline fun getOrCreatePersistentNBTFromStack(stack: ItemStack): NBTTagCompound =
                stack.getOrCreateSubCompound("persistent_te")

        fun setPersistentNBT(stack: ItemStack, tileEntity: TileEntity): Boolean {
            val persistentNBT = persistentNBTFromStack(stack)

            if (persistentNBT != null) {
                if (tileEntity is IPersistent) {
                    tileEntity.readFromItemBlock(persistentNBT)
                    tileEntity.markDirty()
                    return true
                }
            }

            return false
        }
    }

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        super.addInformation(stack, worldIn, tooltip, flagIn)

        persistentNBTFromStack(stack)?.getInformation()?.let { tooltip.addAll(it) }
    }

    override fun placeBlockAt(stack: ItemStack, player: EntityPlayer, world: World, pos: BlockPos, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, newState: IBlockState): Boolean {
        if (!world.setBlockState(pos, newState, 11) { te -> setPersistentNBT(stack, te) }) {
            return false
        }

        val state = world.getBlockState(pos)
        if (state.block === block) {
            setTileEntityNBT(world, player, pos, stack)
            block.onBlockPlacedBy(world, pos, state, player, stack)
            if (player is EntityPlayerMP) CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, stack)
        }

        return true
    }
}