package de.lulebe.designer.data.styles

import org.apache.commons.lang.RandomStringUtils
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


    val uid: String = RandomStringUtils.random(32)


    protected var _name = "Style"
    var name: String
        get() = _name
        set(value) {
            _name = value
            change()
        }
}