package dev.brella.niceandsnug.tileentities

import net.minecraft.nbt.NBTTagCompound

interface IPersistent {
    fun readFromItemBlock(compound: NBTTagCompound)
    fun writeToItemBlock(compound: NBTTagCompound): NBTTagCompound
}