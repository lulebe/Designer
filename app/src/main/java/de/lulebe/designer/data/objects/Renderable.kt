package de.lulebe.designer.data.objects

import android.graphics.Paint

/**
 * Created by LuLeBe on 14/06/16.
 */
class Renderable(val type: Type, val shape: Any, val xPos: Float, val yPos: Float, val paint: Paint) {

    enum class Type {
        RECT, ROUNDRECT, OVAL, TEXT, IMAGE
    }

}