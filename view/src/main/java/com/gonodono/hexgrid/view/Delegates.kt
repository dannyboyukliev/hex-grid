package com.gonodono.hexgrid.view

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty


internal fun <T> relayChange(
    wrapped: KMutableProperty0<T>,
    onChange: () -> Unit
): ReadWriteProperty<Any?, T> = RelayChangeProperty(wrapped, onChange)

private class RelayChangeProperty<T>(
    private val wrapped: KMutableProperty0<T>,
    private val onChange: () -> Unit
) : ReadWriteProperty<Any?, T> {

    private fun getWrapped() = wrapped.getValue(null, wrapped)

    override fun getValue(thisRef: Any?, property: KProperty<*>) = getWrapped()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (getWrapped() == value) return
        wrapped.setValue(null, wrapped, value)
        onChange()
    }
}