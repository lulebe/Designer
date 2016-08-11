package de.lulebe.designer.data.objects

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import de.lulebe.designer.data.Deserializer
import de.lulebe.designer.data.styles.BoxStyle

/**
 * Created by LuLeBe on 13/06/16.
 */
class RectObject : SourceObject() {


    //fillColor
    private var _fillColor: Int = Color.BLACK
    var fillColor: Int
        get() = _fillColor
        set(value) {
            _fillColor = value
            change()
        }


    //fillAlpha
    private var _fillAlpha: Int = 255
    var fillAlpha: Int
        get() = _fillAlpha
        set(value) {
            _fillAlpha = value
            change()

        }


    //strokeColor
    private var _strokeColor: Int = Color.BLACK
    var strokeColor: Int
        get() = _strokeColor
        set(value) {
            _strokeColor = value
            change()
        }


    //strokeWidth
    private var _strokeWidth: Int = 0
    var strokeWidth: Int
        get() = _strokeWidth
        set(value) {
            _strokeWidth = value
            change()
        }


    //cornerRadius
    private var _cornerRadius: Int = 0
    var cornerRadius: Int
        get() = _cornerRadius
        set(value) {
            if (_boxStyle != null)
                boxStyle = null
            _cornerRadius = value
            change()
        }


    override val boxStyleChangeListener = {
        if (widthMoving == _width)
            _widthMoving = boxStyle!!.width
        _width = boxStyle!!.width
        if (heightMoving == _height)
            _heightMoving = boxStyle!!.height
        _height = boxStyle!!.height
        _cornerRadius = boxStyle!!.cornerRadius
        calculateHandles()
        change()
    }



    override fun extractBoxStyle() : BoxStyle {
        val bs = super.extractBoxStyle()
        bs.cornerRadius = cornerRadius
        return bs
    }



    override fun getRenderables(d: Deserializer, forceReload: Boolean): Array<Renderable> {
        if (!forceReload && !hasChanged)
            return renderables.toTypedArray()
        renderables.clear()
        if (alpha == 0) {
            hasChanged = false
            return emptyArray()
        }

        var type = Renderable.Type.RECT
        var shape: Any = RectF(0F, 0F, d.dipToPxF(width), d.dipToPxF(height))
        if (cornerRadius > 0F) {
            type = Renderable.Type.ROUNDRECT
            shape = RoundRect(shape as RectF, d.dipToPxF(cornerRadius))
        }
        if (fillAlpha != 0 || shadow != null) {
            val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
            paintFill.style = Paint.Style.FILL
            paintFill.color = fillColor
            paintFill.alpha = alpha * fillAlpha / 255
            if (shadow != null) {
                paintFill.setShadowLayer(d.dipToPxF(shadow!!.blur), d.dipToPxF(shadow!!.xpos), d.dipToPxF(shadow!!.ypos), Color.parseColor("#99000000"))
            }
            renderables.add(Renderable(type, shape, d.dipToPxF(xpos), d.dipToPxF(ypos), paintFill))
        }
        if (strokeWidth > 0) {
            val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
            paintStroke.style = Paint.Style.STROKE
            paintStroke.color = strokeColor
            paintStroke.alpha = alpha
            paintStroke.strokeWidth = d.dipToPxF(strokeWidth)
            renderables.add(Renderable(type, shape, d.dipToPxF(xpos), d.dipToPxF(ypos), paintStroke))
        }
        hasChanged = false
        return renderables.toTypedArray()
    }

    class RoundRect(val rectF: RectF, val radius: Float)

    override fun getMainColor(): Int {
        return fillColor
    }

    override fun clone () : RectObject {
        val obj = RectObject()
        applyBaseClone(obj)
        obj.fillColor = fillColor
        obj.fillAlpha = fillAlpha
        obj.strokeWidth = strokeWidth
        obj.strokeColor = strokeColor
        obj.cornerRadius = cornerRadius
        return obj
    }

}