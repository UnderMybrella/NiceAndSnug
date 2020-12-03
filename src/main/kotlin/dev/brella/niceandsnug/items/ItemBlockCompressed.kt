package dev.brella.niceandsnug.items

import com.google.common.cache.CacheBuilder
import dev.brella.niceandsnug.NiceAndSnug
import dev.brella.niceandsnug.blocks.BlockSnugCompressed
import dev.brella.niceandsnug.getWrapped
import dev.brella.niceandsnug.nbt.distinctHashCode
import dev.brella.niceandsnug.tileentities.TileEntitySnugCompressed
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class ItemBlockCompressed(block: BlockSnugCompressed) : ItemBlockPersistentTE(block) {
    companion object {
        val COMPRESSION_LEVELS = arrayOf(
                "Uncompressed",
                "Compressed",
                "Double Compressed",
                "Triple Compressed",
                "Quadruple Compressed",
                "Quintuple Compressed",
                "Sextuple Compressed",
                "Septuple Compressed",
                "Octuple Compressed",
                "Nontuple Compressed",
                "Dectuple Compressed"
        )

        val nameCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build<Int, String>()

        val informationCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build<Int, Array<String>>()

        val burnTimeCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build<Int, Int>()
    }

    override fun getItemStackDisplayName(stack: ItemStack): String {
//        return I18n.translateToLocal(getUnlocalizedNameInefficiently(stack) + ".name").trim { it <= ' ' }

        return persistentNBTFromStack(stack)?.let { persistent ->
            nameCache.get(persistent.distinctHashCode()) {
                val compressionLevel = TileEntitySnugCompressed.readCompressionLevelFromCompound(persistent)
                val blockState = TileEntitySnugCompressed.readBlockStateFromCompound(persistent)
                val item = blockState?.block?.let(Item::getItemFromBlock)
                        ?: return@get "${COMPRESSION_LEVELS.getWrapped(compressionLevel)} (Nothing)"
                val compStack = ItemStack(item, 9, stack.itemDamage)

                "${COMPRESSION_LEVELS.getWrapped(compressionLevel)} ${compStack.displayName}"
            }
        } ?: "(Nothing)"
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        super.addInformation(stack, worldIn, tooltip, flagIn)

        persistentNBTFromStack(stack)?.let { persistent ->
            informationCache.get((31 * persistent.distinctHashCode()) + flagIn.hashCode()) {
                val compressionLevel = TileEntitySnugCompressed.readCompressionLevelFromCompound(persistent)
                val blockState = TileEntitySnugCompressed.readBlockStateFromCompound(persistent)
                val item = blockState?.block?.let(Item::getItemFromBlock)
                        ?: return@get arrayOf("${9.0.pow(compressionLevel).toInt()} Blocks of (Nothing)")
                val compStack = ItemStack(item, 9, stack.itemDamage)

                mutableListOf("${9.0.pow(compressionLevel).toInt()} Blocks of ${compStack.displayName}")
                        .also { item.addInformation(compStack, worldIn, it, flagIn) }
                        .toTypedArray()
            }.let { tooltip.addAll(it) }
        } ?: tooltip.add("No nbt data detected, this is definitely a bug!")
    }

    override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        var placingPos = pos
        val existingState = world.getBlockState(placingPos)
        val block = existingState.block
        if (!block.isReplaceable(world, placingPos)) {
            placingPos = placingPos.offset(facing)
        }
        val heldStack = player.getHeldItem(hand)

        if (!heldStack.isEmpty && player.canPlayerEdit(placingPos, facing, heldStack) && world.mayPlace(this.block, placingPos, false, facing, player)) {
//            val i = this.getMetadata(heldStack.metadata)
            val compressedBlock = persistentNBTFromStack(heldStack)?.let(TileEntitySnugCompressed.Companion::readBlockStateFromCompound)
            if (compressedBlock == null) {
                player.sendStatusMessage(TextComponentString("Corrupted Block"), true)
                heldStack.shrink(heldStack.count)
                return EnumActionResult.FAIL
            }

            val i = compressedBlock.block.getMetaFromState(compressedBlock)

            val baseState = this.block.getStateForPlacement(world, placingPos, facing, hitX, hitY, hitZ, i, player, hand)
            var stateForPlacement = compressedBlock.block.getStateForPlacement(world, placingPos, facing, hitX, hitY, hitZ, i, player, hand)
            if (placeBlockAt(heldStack, player, world, placingPos, facing, hitX, hitY, hitZ, baseState)) {
                val te = world.getTileEntity(placingPos) as? TileEntitySnugCompressed
                if (te != null && te.compressedState != stateForPlacement) {
                    te.compressedState = stateForPlacement
                    te.markDirty()
                }

                stateForPlacement = world.getBlockState(placingPos)
                val soundType = stateForPlacement.block.getSoundType(stateForPlacement, world, placingPos, player)
                world.playSound(player, placingPos, soundType.placeSound, SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f)
                heldStack.shrink(1)

                world.checkLight(pos)
            }

            return EnumActionResult.SUCCESS
        } else {
            return EnumActionResult.FAIL
        }
    }

    override fun getItemBurnTime(stack: ItemStack): Int {
        if (NiceAndSnug.itemBurnTimeMultiplier <= 0) return -1

        return persistentNBTFromStack(stack)?.let { persistent ->
            burnTimeCache.get(persistent.distinctHashCode()) {
                val compressionLevel = TileEntitySnugCompressed.readCompressionLevelFromCompound(persistent)
                val blockState = TileEntitySnugCompressed.readBlockStateFromCompound(persistent)
                val item = blockState?.block?.let(Item::getItemFromBlock) ?: return@get -1
                val compStack = ItemStack(item, 9, stack.itemDamage)
                TileEntityFurnace.getItemBurnTime(compStack)
                        .takeIf { it > 0 }
                        ?.toLong()
                        ?.times(NiceAndSnug.itemBurnTimeMultiplier.pow(compressionLevel))
                        ?.coerceAtMost(Int.MAX_VALUE.toDouble())
                        ?.toInt() ?: -1
            }
        } ?: -1
    }
    
    init {
        tileEntityItemStackRenderer = ItemBlockCompressedRenderer
    }
}