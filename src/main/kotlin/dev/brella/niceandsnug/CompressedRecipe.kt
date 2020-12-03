package dev.brella.niceandsnug

import com.google.common.collect.BiMap
import dev.brella.niceandsnug.items.ItemBlockCompressed
import dev.brella.niceandsnug.items.ItemBlockPersistentTE
import dev.brella.niceandsnug.nbt.appendToInformation
import dev.brella.niceandsnug.nbt.copyTo
import dev.brella.niceandsnug.tileentities.TileEntitySnugCompressed
import dev.brella.niceandsnug.tileentities.TileEntitySnugCompressed.Companion.COMPRESSION_LEVEL_KEY
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.registries.GameData
import net.minecraftforge.registries.IForgeRegistry

class CompressedRecipe : IRecipe {
    val resourceLocation = ResourceLocation(NiceAndSnug.MOD_ID, "compressed_recipe")

    override fun setRegistryName(name: ResourceLocation?): IRecipe = this
    override fun getRegistryName(): ResourceLocation = resourceLocation
    override fun getRegistryType(): Class<IRecipe> = IRecipe::class.java

    override fun matches(inv: InventoryCrafting, world: World): Boolean =
            !getCraftingResult(inv).isEmpty

    override fun getCraftingResult(inv: InventoryCrafting): ItemStack {
        if (inv.width < 3 || inv.height < 3) return ItemStack.EMPTY

        var compressing: ItemStack? = null
        var matchingColumns = 0

        for (x in 0 until inv.width) {
            var matchingRows = 0

            for (y in 0 until inv.height) {
                val stack = inv.getStackInRowAndColumn(x, y)
                if (stack.isEmpty) {
                    if (matchingRows > 0) break
                    else continue
                } else if (matchingRows >= 3) {
                    return ItemStack.EMPTY
                } else if (compressing == null) {
                    compressing = stack
                    matchingRows++
                } else if (!ItemStack.areItemStacksEqual(compressing, stack)) {
                    return ItemStack.EMPTY
                } else {
                    matchingRows++
                }
            }

            if (matchingRows == 3) matchingColumns++
            else return ItemStack.EMPTY
        }

        if (matchingColumns == 3 && compressing != null) {
            val result = ItemStack(SnugBlocks.snugCompressed, 1, compressing.itemDamage)
            if (compressing.item is ItemBlockCompressed) {
                ItemBlockPersistentTE.getOrCreatePersistentNBTFromStack(compressing)
                        .copyTo(ItemBlockPersistentTE.getOrCreatePersistentNBTFromStack(result))
                        .also { tag -> tag.setInteger(COMPRESSION_LEVEL_KEY, tag.getInteger(COMPRESSION_LEVEL_KEY) + 1) }
            } else if (compressing.item in NiceAndSnug.proxy.ITEMS_TO_BLOCKS) {
                val block = (compressing.item as ItemBlock).block
                val state = block.getStateFromMeta(compressing.metadata)

                TileEntitySnugCompressed.writeStateToCompound(state, 1, ItemBlockPersistentTE.getOrCreatePersistentNBTFromStack(result))
            } else {
                val block = Blocks.WOOL
                val state = block.getStateFromMeta(compressing.metadata)

                TileEntitySnugCompressed.writeStateToCompound(state, 1, ItemBlockPersistentTE.getOrCreatePersistentNBTFromStack(result))
            }

            return result
        }

        return ItemStack.EMPTY
    }

    override fun canFit(width: Int, height: Int): Boolean = width >= 3 && height >= 3

    override fun getRecipeOutput(): ItemStack = ItemStack.EMPTY
    override fun isDynamic(): Boolean = true
}