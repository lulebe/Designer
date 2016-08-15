package de.lulebe.designer.data.styles


class BoxStyle : BaseStyle() {


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

}