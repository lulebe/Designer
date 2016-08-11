package de.lulebe.designer.data.styles

import java.util.*


class ColorStyle {

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
    private fun change () {
        for (listener in listeners) {
            listener()
        }
    }


    val uid: Long


    private var _name = "Color"
    var name: String
        get() = _name
        set(value) {
            _name = value
            change()
        }


    private var _color = "ffffff"
    var color: String
        get() = _color
        set(value) {
            _color = value
            change()
        }


    private var _alpha = 0xff
    var alpha: Int
        get() = _alpha
        set(value) {
            _alpha = value
            change()
        }


    constructor() {
        uid = System.currentTimeMillis() + Random().nextInt()
    }


}