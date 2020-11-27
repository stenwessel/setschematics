package nl.tue.setschematics.util

fun <V, K> MutableMap<V, MutableSet<K>>.clone() = mutableMapOf(*this.map { it.key to it.value.toMutableSet()  }.toTypedArray())

infix fun <A,B> A.with(b: B) = UnorderedPair(this, b)

fun <T> Collection<T>.toEdge(): UnorderedPair<T, T> {
    require(this.size == 2)
    val iterator = this.iterator()
    return UnorderedPair(iterator.next(), iterator.next())
}
