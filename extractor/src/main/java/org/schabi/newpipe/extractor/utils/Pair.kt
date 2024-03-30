package org.schabi.newpipe.extractor.utils

import java.io.Serializable
import java.util.Objects

/**
 * Serializable class to create a pair of objects.
 *
 *
 *
 * The two objects of the pair must be [serializable][Serializable] and can be of the same
 * type.
 *
 *
 *
 *
 * Note that this class is not intended to be used as a general-purpose pair and should only be
 * used when interfacing with the extractor.
 *
 *
 * @param <F> the type of the first object, which must be [Serializable]
 * @param <S> the type of the second object, which must be [Serializable]
</S></F> */
class Pair<F : Serializable?, S : Serializable?>
/**
 * Creates a new [Pair] object.
 *
 * @param first  the first object of the pair
 * @param second the second object of the pair
 */(
        /**
         * The first object of the pair.
         */
        var first: F,
        /**
         * The second object of the pair.
         */
        var second: S) : Serializable {
    /**
     * Gets the first object of the pair.
     *
     * @return the first object of the pair
     */
    /**
     * Sets the first object, which must be of the [F] type.
     *
     * @param first the new first object of the pair
     */
    /**
     * Gets the second object of the pair.
     *
     * @return the second object of the pair
     */
    /**
     * Sets the first object, which must be of the [S] type.
     *
     * @param second the new first object of the pair
     */

    /**
     * Returns a string representation of the current `Pair`.
     *
     *
     *
     * The string representation will look like this:
     * `
     * {*firstObject.toString()*, *secondObject.toString()*}
    ` *
     *
     *
     * @return a string representation of the current `Pair`
     */
    override fun toString(): String {
        return "{" + first + ", " + second + "}"
    }

    /**
     * Reveals whether an object is equal to this `Pair` instance.
     *
     * @param obj the object to compare with this `Pair` instance
     * @return whether an object is equal to this `Pair` instance
     */
    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null || javaClass != obj.javaClass) {
            return false
        }
        val pair = obj as Pair<*, *>
        return first == pair.first && second == pair.second
    }

    /**
     * Returns a hash code of the current `Pair` by using the first and second object.
     *
     * @return a hash code of the current `Pair`
     */
    override fun hashCode(): Int {
        return Objects.hash(first, second)
    }
}
