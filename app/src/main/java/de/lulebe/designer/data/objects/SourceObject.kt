package de.lulebe.designer.data.objects

import android.content.Context


abstract class SourceObject : BaseObject() {




    //alpha
    protected var _alpha: Int = 255
    var alpha: Int
        get() = _alpha
        set(value) {
            _alpha = value
            change()
        }


    //shadow
    protected var _shadow: ObjectShadow? = null
    var shadow: ObjectShadow?
        get() = _shadow
        set(value) {
            _shadow?.removeAllChangeListeners()
            value?.addChangeListener {
                change()
            }
            _shadow = value
            change()
        }




    final class ObjectShadow {

        //listeners
        @Transient
        private var listeners: MutableList<() -> Unit> = mutableListOf()
        fun addChangeListener (l: () -> Unit) {
            if (listeners == null)
                listeners = mutableListOf()
            listeners.add(l)
        }
        fun removeChangeListener (l: () -> Unit) {
            listeners.remove(l)
        }
        fun removeAllChangeListeners () {
            listeners.clear()
        }
        fun change () {
            for (listener in listeners) {
                listener()
            }
        }

        //blurradius
        private var _blur: Int = 0
        var blur: Int
            get() = _blur
            set(value) {
                _blur = value
                change()
            }
        //xpos
        private var _xpos: Int = 0
        var xpos: Int
            get() = _xpos
            set(value) {
                _xpos = value
                change()
            }
        //ypos
        private var _ypos: Int = 0
        var ypos: Int
            get() = _ypos
            set(value) {
                _ypos = value
                change()
            }

        constructor(blur: Int = 0, xpos: Int = 0, ypos: Int = 0) {
            this.blur = blur
            this.xpos = xpos
            this.ypos = ypos
        }

    }

    override fun init (ctx: Context, board: BoardObject) {
        super.init(ctx, board)
        if (shadow != null) {
            shadow?.addChangeListener {
                change()
            }
        }
    }

    override fun close() {
        shadow?.removeAllChangeListeners()
        super.close()
    }

    fun applyBaseClone (obj: SourceObject, keepName: Boolean = false) {
        if (keepName)
            obj.name = name
        else {
            val regex = Regex("^(.+)(\\(\\d+\\))$")
            if (regex.matches(name))
                obj.name = regex.replace(name, {
                    var ret = name
                    if (it.groupValues.size == 3) {
                        val match = it.groupValues[2]
                        val newNum = match.replace("(", "").replace(")", "").toInt() + 1
                        ret = it.groupValues[1] + "(" + newNum.toString() + ")"
                    }
                    ret
                })
            else
                obj.name = name + " (2)"
        }
        obj.xpos = xpos
        obj.ypos = ypos
        obj.width = width
        obj.height = height
        obj.alpha = alpha
        if (shadow != null) {
            obj.shadow = ObjectShadow(shadow!!.blur, shadow!!.xpos, shadow!!.ypos)
        }
    }

}