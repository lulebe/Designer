package de.lulebe.designer.data.styles

import java.util.*


class BoxStyle {

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


    private var _name = "BoxStyle"
    var name: String
        get() = _name
        set(value) {
            _name = value
            change()
        }


    private var _width = 50
    var width: Int
        get() = _width
        set(value) {
            _width = value
            change()
        }


    private var _height = 50
    var height: Int
        get() = _height
        set(value) {
            _height = value
            change()
        }


    private var _cornerRadius = 0
    var cornerRadius: Int
        get() = _cornerRadius
        set(value) {
            _cornerRadius = value
            change()
        }


    constructor() {
        uid = System.currentTimeMillis() + Random().nextInt()
    }

}