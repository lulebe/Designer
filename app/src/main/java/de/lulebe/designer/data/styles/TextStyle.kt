package de.lulebe.designer.data.styles

import android.text.Layout


class TextStyle : BaseStyle() {


    private var _fontSize = 16
    var fontSize: Int
        get() = _fontSize
        set(value) {
            _fontSize = value
            change()
        }


    private var _font: Long = 0L
    var font: Long
        get() = _font
        set(value) {
            _font = value
            change()
        }

    private var _alignment = Layout.Alignment.ALIGN_NORMAL
    var alignment: Layout.Alignment
        get() = _alignment
        set(value) {
            _alignment = value
            change()
        }

}