package dev.brella.niceandsnug.nbt

import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagLong
import net.minecraft.nbt.NBTTagString

fun NBTTagList.getLongAt(index: Int): Long? =
        (this[index].takeIf { base -> base.id == NbtTagTypes.LONG.toByte() } as? NBTTagLong)?.long

fun NBTTagList.getStringAt(index: Int): String? =
        (this[index].takeIf { base -> base.id == NbtTagTypes.STRING.toByte() } as? NBTTagString)?.string