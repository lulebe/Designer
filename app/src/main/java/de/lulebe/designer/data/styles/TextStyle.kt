package de.lulebe.designer.data.styles

import java.util.*


class TextStyle {

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


    private var _name = "TextStyle"
    var name: String
        get() = _name
        set(value) {
            _name = value
            change()
        }


    private var _fontSize = 16
    var fontSize: Int
        get() = _fontSize
        set(value) {
            _fontSize = value
            change()
        }


    private var _font = 0
    var font: Int
        get() = _font
        set(value) {
            _font = value
            change()
        }


    constructor() {
        uid = System.currentTimeMillis() + Random().nextInt()
    }


}