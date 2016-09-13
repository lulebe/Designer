package de.lulebe.designer.data.objects

import android.graphics.Paint


class Renderable(val type: Type, val shape: Any, val xPos: Float, val yPos: Float, val rotation: Float, val paint: Paint) {

    enum class Type {
        RECT, ROUNDRECT, OVAL, TEXT, IMAGE
    }

}