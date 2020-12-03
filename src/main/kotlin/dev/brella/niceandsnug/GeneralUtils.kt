package dev.brella.niceandsnug

typealias PredicateKt<T> = (obj: T) -> Boolean

inline fun <T> Array<T>.getWrapped(index: Int): T = this[Math.floorMod(index, size)]
inline fun <T> Array<T>.getWrapped(index: Number): T = this[Math.floorMod(index.toInt(), size)]

inline fun <T> debug(line: String, block: () -> T): T {
    println(line)
    return block()
}