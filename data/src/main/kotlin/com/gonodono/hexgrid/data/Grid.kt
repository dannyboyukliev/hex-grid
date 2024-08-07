package com.gonodono.hexgrid.data

import androidx.annotation.IntRange

/**
 * The library's specialized state collection that allows indexing by the
 * particular address system used here.
 */
interface Grid {

    /**
     * The the [Grid]'s [Size]: its row count and column count.
     *
     * The library defines rows and columns as collinear cells, not necessarily
     * contiguous ones.
     */
    val size: Size

    /**
     * Whether the even rows and columns are inset, or the odd ones.
     *
     * A hex grid is essentially a staggered grid, so the setting is the same
     * for both directions. See [isLineInset].
     */
    val insetEvenLines: Boolean

    /**
     * Whether edge lines are enabled.
     *
     * When true, one extra line of cells is added to the grid on all sides.
     * This is meant to be used with grids that fill their components and need
     * active cells covering the "holes" at the edges, though using it with any
     * other mode is not prohibited.
     *
     * The extra lines result in the minimum row/column index being -1
     * instead of 0, and the maximum row/column index being exactly the
     * [Size.rowCount]/[Size.columnCount] instead of 1 less than those.
     */
    val enableEdgeLines: Boolean

    /**
     * If [enableEdgeLines] is `false`, this will be equal to [size].
     *
     * If [enableEdgeLines] is `true`, this will be equal to
     * `Size(size.rowCount + 2, size.columnCount + 2)`.
     */
    val totalSize: Size

    /**
     * The total cell count.
     */
    val cellCount: Int

    /**
     * The [Address]-indexed get operator for [Grid].
     *
     * Invalid addresses will result in Exceptions. See [isValidAddress].
     */
    operator fun get(address: Address): State

    /**
     * The [Int]-indexed get operator for [Grid].
     *
     * Invalid addresses will result in Exceptions. See [isValidAddress].
     */
    operator fun get(row: Int, column: Int): State

    /**
     * Returns whether the given line – row or column – is inset, per the
     * [insetEvenLines] property.
     *
     * Due to the inherent symmetry of staggered grids, insetting even rows is
     * the same as insetting even columns, hence the one function for both.
     */
    fun isLineInset(index: Int): Boolean

    /**
     *  Returns true if [row] and [column] are valid indices for the current
     *  grid specifications.
     */
    fun isValidAddress(row: Int, column: Int): Boolean

    /**
     * Returns the appropriate [Grid.Address] if [row] and [column] are valid
     * indices; i.e., if `isValidAddress(row, column)` would return `true`.
     *
     * Returns null otherwise.
     */
    fun findAddress(row: Int, column: Int): Address?

    /**
     * A [Set] view of the [Grid]'s mappings as [Cell]s.
     *
     * Analogous to [Map]'s [entries][Map.entries].
     */
    val cells: Set<Cell>

    /**
     * A [Set] view of the [Grid]'s [Address]es.
     *
     * Analogous to [Map]'s [keys][Map.keys].
     */
    val addresses: Set<Address>

    /**
     * A [Collection] view of the [Grid]'s [State]s.
     *
     * Analogous to [Map]'s [values][Map.values].
     */
    val states: Collection<State>

    /**
     * Provides faster iteration than the standard views by skipping [Iterator]s.
     */
    fun forEach(action: (Address, State) -> Unit)

    /**
     * Returns a modified copy of this [Grid] if [change] actually causes a
     * change. If it does not cause a change, the same Grid instance is
     * returned.
     *
     * Invalid addresses will result in Exceptions. See [isValidAddress].
     */
    fun copy(address: Address, change: State): Grid

    /**
     * Returns a modified copy of this [Grid] if [changes] actually causes any
     * changes. If the Map does not cause any changes, the same Grid instance is
     * returned.
     *
     * Invalid addresses will result in Exceptions. See [isValidAddress].
     */
    fun copy(changes: Map<Address, State>): Grid

    /**
     * Immutable class specifying a [Grid]'s line counts in each dimension.
     */
    data class Size(
        @IntRange(from = 0) val rowCount: Int,
        @IntRange(from = 0) val columnCount: Int
    ) {
        init {
            check(rowCount >= 0 && columnCount >= 0) {
                "Invalid size: (${rowCount}x$columnCount)"
            }
        }

        override fun toString(): String = "Size(${rowCount}x$columnCount)"

        companion object {
            /**
             * A [Size] with no rows or columns.
             */
            val Zero: Size = Size(0, 0)
        }
    }

    /**
     * The index structure for [Grid].
     */
    data class Address(val row: Int, val column: Int) {

        /**
         * Convenience to concisely check a [Grid.Address] against individual
         * indices.
         */
        fun isAt(row: Int, column: Int): Boolean =
            this.row == row && this.column == column

        override fun toString(): String = "Address($row,$column)"

        companion object {
            /**
             * The [Address] of the origin cell.
             */
            val Origin: Address = Address(0, 0)
        }
    }

    /**
     * The state class for [Grid] cells.
     */
    data class State(
        val isVisible: Boolean = true,
        val isSelected: Boolean = false
    ) {
        override fun toString(): String = "State(v%s,s%s)".format(
            if (isVisible) "+" else "-", if (isSelected) "+" else "-"
        )

        companion object {
            /**
             * The default [State] for all cells.
             */
            val Default: State = State()
        }
    }

    /**
     * A [Grid]'s [Address]-[State] pair, analogous to [Map]'s
     * [Entry][Map.Entry], with similar semantics.
     *
     * The mapping represented by this pair is guaranteed valid only for
     * instances provided by an [Iterator] on [cells], and only for the duration
     * of the current iteration (not counting any direct changes by the user in
     * mutable implementations).
     */
    interface Cell {

        /**
         * The [Cell]'s [Address]. This should always be completely immutable in
         * any subclass.
         */
        val address: Address

        /**
         * The [Cell]'s current [State].
         */
        val state: State

        operator fun component1() = address
        operator fun component2() = state
    }
}

/**
 * Returns an empty, read-only [Grid] instance.
 *
 * The returned Grid's [get][Grid.get] functions throw Exceptions
 * unconditionally.
 */
fun emptyGrid(): Grid = EmptyGrid

/**
 * An empty [Grid] instance for use as an initializer, a reset value, etc.
 *
 * This Grid's [get][Grid.get] functions throw Exceptions unconditionally.
 */
private data object EmptyGrid : Grid {
    override val size: Grid.Size = Grid.Size.Zero
    override val insetEvenLines: Boolean = false
    override val enableEdgeLines: Boolean = false
    override val totalSize: Grid.Size = Grid.Size.Zero
    override val cellCount: Int = 0
    override fun get(address: Grid.Address) = error("EmptyGrid is empty.")
    override fun get(row: Int, column: Int) = error("EmptyGrid is empty.")
    override fun findAddress(row: Int, column: Int): Grid.Address? = null
    override val cells: Set<Grid.Cell> = emptySet()
    override val addresses: Set<Grid.Address> = emptySet()
    override val states: Collection<Grid.State> = emptyList()
    override fun forEach(action: (Grid.Address, Grid.State) -> Unit) {}
    override fun copy(address: Grid.Address, change: Grid.State): Grid = this
    override fun copy(changes: Map<Grid.Address, Grid.State>): Grid = this
    override fun isLineInset(index: Int): Boolean = false
    override fun isValidAddress(row: Int, column: Int): Boolean = false
    override fun toString(): String = "EmptyGrid"
}