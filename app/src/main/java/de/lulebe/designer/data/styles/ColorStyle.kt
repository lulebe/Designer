package de.lulebe.designer.data.styles

import android.graphics.Color


class ColorStyle: BaseStyle() {


    private var _color = Color.BLACK
    var color: Int
        get() = _color
        set(value) {
            _color = value
            change()
        }


}