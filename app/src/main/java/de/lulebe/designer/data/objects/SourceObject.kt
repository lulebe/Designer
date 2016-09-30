package de.lulebe.designer.data.objects


abstract class SourceObject : BaseObject() {




    //alpha
    protected var _alpha: Int = 255
    var alpha: Int
        get() = _alpha
        set(value) {
            _alpha = value
            change()
        }




    fun applyBaseClone (obj: SourceObject, keepName: Boolean = false) {
        obj.name = name
        obj.xpos = xpos
        obj.ypos = ypos
        obj.width = width
        obj.height = height
        obj.alpha = alpha
        obj.rotation = rotation
        obj.boxStyle = boxStyle
    }

}