package de.lulebe.designer.data.objects

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import de.lulebe.designer.data.Deserializer
import de.lulebe.designer.data.styles.BoxStyle
import de.lulebe.designer.data.styles.ColorStyle


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





    private var _fillColorStyleUID: Long? = null
    @Transient
    protected var _fillColorStyle: ColorStyle? = null
    var fillColorStyle: ColorStyle?
        get() = _fillColorStyle
        set(value) {
            _fillColorStyle?.removeChangeListener(fillColorStyleChangeListener!!)
            if (value != null) {
                _fillColorStyle = value
                _fillColorStyleUID = value.uid
                value.addChangeListener(fillColorStyleChangeListener!!)
                fillColorStyleChangeListener!!()
            } else {
                _fillColorStyle = null
                _fillColorStyleUID = null
            }
        }

    @Transient
    private var fillColorStyleChangeListener: (() -> Unit)? = null


    private var _strokeColorStyleUID: Long? = null
    @Transient
    protected var _strokeColorStyle: ColorStyle? = null
    var strokeColorStyle: ColorStyle?
        get() = _strokeColorStyle
        set(value) {
            _strokeColorStyle?.removeChangeListener(strokeColorStyleChangeListener!!)
            if (value != null) {
                _strokeColorStyle = value
                _strokeColorStyleUID = value.uid
                value.addChangeListener(strokeColorStyleChangeListener!!)
                strokeColorStyleChangeListener!!()
            } else {
                _strokeColorStyle = null
                _strokeColorStyleUID = null
            }
        }

    @Transient
    private var strokeColorStyleChangeListener: (() -> Unit)? = null
    
    
    
    
    
    override fun extractBoxStyle() : BoxStyle {
        val bs = super.extractBoxStyle()
        bs.cornerRadius = cornerRadius
        return bs
    }

    fun extractFillcolorStyle() : ColorStyle {
        val cs = ColorStyle()
        cs.name = name + " fill color"
        cs.color = fillColor
        return cs
    }

    fun extractStrokecolorStyle() : ColorStyle {
        val cs = ColorStyle()
        cs.name = name + " stroke color"
        cs.color = strokeColor
        return cs
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

    override fun init (ctx: Context, board: BoardObject?) {
        boxStyleChangeListener = {
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
        fillColorStyleChangeListener = {
            fillColor = fillColorStyle!!.color
            change()
        }
        strokeColorStyleChangeListener = {
            strokeColor = strokeColorStyle!!.color
            change()
        }
        if (board != null) {
            fillColorStyle = board.styles.colorStyles[_fillColorStyleUID]
            strokeColorStyle = board.styles.colorStyles[_strokeColorStyleUID]
        }
        super.init(ctx, board)
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