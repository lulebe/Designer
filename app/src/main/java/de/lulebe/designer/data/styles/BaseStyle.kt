package de.lulebe.designer.data.styles

import java.util.*


open class BaseStyle {

    //listeners
    @Transient
    private var listeners = mutableListOf<() -> Unit>()
    fun addChangeListener (l: () -> Unit) {
        listeners.add(l)
    }
    fun removeChangeListener (l: () -> Unit) {
        listeners.remove(l)
    }
    fun removeAllChangeListeners () {
        listeners.clear()
    }
    protected fun change () {
        for (listener in listeners) {
            listener()
        }
    }


    val uid: Long = System.currentTimeMillis() + Random().nextInt()


    protected var _name = "Style"
    var name: String
        get() = _name
        set(value) {
            _name = value
            change()
        }
}