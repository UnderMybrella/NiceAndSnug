package dev.brella.niceandsnug.nbt

import dev.brella.niceandsnug.getWrapped
import net.minecraft.nbt.*
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

const val NBT_TAG_COMPOUND_INFORMATION_KEY = "nbt_information"

inline fun NBTTagCompound.getInformation(): Array<String>? =
        getStringArray(NBT_TAG_COMPOUND_INFORMATION_KEY)

inline fun NBTTagCompound.setInformation(info: Array<String>?) =
        setStringArray(NBT_TAG_COMPOUND_INFORMATION_KEY, info)

inline fun NBTTagCompound.appendToInformation(line: String): Array<String> =
        (getInformation()?.plus(line) ?: arrayOf(line)).also(this::setInformation)

inline fun NBTTagCompound.getFacing(key: String): EnumFacing? =
        if (hasKey(key)) EnumFacing.values().getWrapped(getByte(key)) else null

inline fun NBTTagCompound.setFacing(key: String, facing: EnumFacing) =
        setByte(key, facing.ordinal.toByte())

inline fun NBTTagCompound.getAxis(key: String): EnumFacing.Axis? =
        if (hasKey(key)) EnumFacing.Axis.values().getWrapped(getByte(key)) else null

inline fun NBTTagCompound.setAxis(key: String, facing: EnumFacing.Axis) =
        setByte(key, facing.ordinal.toByte())

/** BlockPos */

inline fun NBTTagCompound.getBlockPos(key: String): BlockPos? =
        if (hasKey(key)) BlockPos.fromLong(getLong(key)) else null

inline fun NBTTagCompound.removeBlockPos(key: String) =
        removeTag(key)

inline fun NBTTagCompound.setBlockPos(key: String, pos: BlockPos) =
        setLong(key, pos.toLong())

fun NBTTagCompound.getLongArray(key: String): LongArray? {
    if (hasKey(key)) {
        val list = getTagList(key, NbtTagTypes.LONG)
        return LongArray(list.tagCount()) { list.getLongAt(it) ?: return null }
    } else {
        return null
    }
}

fun NBTTagCompound.setLongArray(key: String, array: LongArray?) {
    if (array == null) {
        removeTag(key)
    } else {
        val list = NBTTagList()
        array.forEach { list.appendTag(NBTTagLong(it)) }
        setTag(key, list)
    }
}

fun NBTTagCompound.getStringArray(key: String): Array<String>? {
    if (hasKey(key)) {
        val list = getTagList(key, NbtTagTypes.STRING)
        return Array(list.tagCount()) { list.getStringAt(it) ?: return null }
    } else {
        return null
    }
}

fun NBTTagCompound.setStringArray(key: String, array: Array<String>?) {
    if (array == null) {
        removeTag(key)
    } else {
        val list = NBTTagList()
        array.forEach { list.appendTag(NBTTagString(it)) }
        setTag(key, list)
    }
}

fun NBTTagCompound.copyTo(nbt: NBTTagCompound): NBTTagCompound {
    keySet.forEach { key -> nbt.setTag(key, getTag(key).copy()) }
    return nbt
}

fun NBTBase.distinctHashCode(): Int {
    val id = id.toInt() shl 27
    @Suppress("PlatformExtensionReceiverOfInline")
    return when (this) {
        is NBTTagEnd -> id
        is NBTTagByte -> id or byte.hashCode()
        is NBTTagShort -> id or short.hashCode()
        is NBTTagInt -> id or int.hashCode()
        is NBTTagLong -> id or long.hashCode()
        is NBTTagFloat -> id or float.hashCode()
        is NBTTagDouble -> id or double.hashCode()
        is NBTTagByteArray -> id or byteArray.contentHashCode()
        is NBTTagString -> id or string.hashCode()
        is NBTTagList -> id or this.fold(1) { result, tag -> result * 31 + tag.distinctHashCode() }
        is NBTTagCompound -> id or this.keySet.sumBy { key -> key.hashCode() xor this.getTag(key).distinctHashCode() }
        is NBTTagIntArray -> id or intArray.contentHashCode()
        else -> id
    }
}