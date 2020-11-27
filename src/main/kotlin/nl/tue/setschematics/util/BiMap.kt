package nl.tue.setschematics.util

/**
 * Map that maps both ways.
 */
class BiMap<K, V> private constructor(val map: MutableMap<K, V>, val inverseMap: MutableMap<V, K>) {

    constructor() : this(mutableMapOf(), mutableMapOf())

    fun put(key: K, value: V) {
        require(!map.containsKey(key) && !inverseMap.containsKey(value))

        map[key] = value
        inverseMap[value] = key
    }

    fun getValue(key: K) = map[key]

    fun getKey(value: V) = inverseMap[value]

    fun removeByKey(key: K) {
        map.remove(key)?.also { inverseMap.remove(it) }
    }

    fun removeByValue(value: V) {
        inverseMap.remove(value)?.also { map.remove(it) }
    }

    fun clone(): BiMap<K, V> {
        return BiMap(map.toMutableMap(), inverseMap.toMutableMap())
    }

    @JvmName("getByKey")
    operator fun get(key: K) = getValue(key)

    @JvmName("getByVal")
    operator fun get(value: V) = getKey(value)
}
