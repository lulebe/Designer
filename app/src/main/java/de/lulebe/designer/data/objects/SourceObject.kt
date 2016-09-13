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
        obj.rotation = rotation
    }

}