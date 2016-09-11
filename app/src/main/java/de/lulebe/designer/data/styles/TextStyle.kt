package de.lulebe.designer.data.styles


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


}