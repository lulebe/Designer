package de.lulebe.designer.data.objects

import android.content.Context
import android.graphics.*
import de.lulebe.designer.data.Deserializer
import de.lulebe.designer.data.ExportContainer
import de.lulebe.designer.data.styles.BaseStyle
import de.lulebe.designer.data.styles.BoxStyle
import de.lulebe.designer.data.styles.ColorStyle


class RectObject : SourceObject() {


    //fillColor
    private var _fillColor: Int = Color.BLACK
    var fillColor: Int
        get() = _fillColor
        set(value) {
            if (_fillColorStyle != null)
                fillColorStyle = null
            _fillColor = value
            change()
        }



    //strokeColor
    private var _strokeColor: Int = Color.BLACK
    var strokeColor: Int
        get() = _strokeColor
        set(value) {
            if (_strokeColorStyle != null)
                strokeColorStyle = null
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

    private var _gradient: Gradient? = null
    //gradient fill
    var gradient: Gradient?
        get() = _gradient
        set(value) {
            _gradient?.removeAllChangeListeners()
            value?.addChangeListener {
                change()
            }
            _gradient = value
            change()
        }


    //shadow
    private var _shadow: Shadow? = null
    var shadow: Shadow?
        get() = _shadow
        set(value) {
            _shadow?.removeAllChangeListeners()
            value?.addChangeListener {
                change()
            }
            _shadow = value
            change()
        }

    init {
        _name = "Rectangle"
    }
    
    
    
    
    class Gradient {
        //listeners
        @Transient
        private var listeners: MutableList<() -> Unit> = mutableListOf()
        fun addChangeListener (l: () -> Unit) {
            if (listeners == null)
                listeners = mutableListOf()
            listeners.add(l)
        }
        fun removeChangeListener (l: () -> Unit) {
            listeners.remove(l)
        }
        fun removeAllChangeListeners () {
            listeners.clear()
        }
        fun change () {
            for (listener in listeners) {
                listener()
            }
        }

        private var _direction: Direction = Direction.VERTICAL
        var direction: Direction
            get() = _direction
            set(value) {
                _direction = value
                change()
            }

        private var _startColor: Int = Color.BLACK
        var startColor: Int
            get() = _startColor
            set(value) {
                _startColor = value
                if (_startColorStyle != null)
                    startColorStyle = null
                change()
            }

        private var _endColor: Int = Color.WHITE
        var endColor: Int
            get() = _endColor
            set(value) {
                _endColor = value
                if (_endColorStyle != null)
                    endColorStyle = null
                change()
            }

        enum class Direction {
            HORIZONTAL, VERTICAL, CIRCLE
        }

        var _startColorStyleUID: Long? = null
        @Transient
        private var _startColorStyle: ColorStyle? = null
        var startColorStyle: ColorStyle?
            get() = _startColorStyle
            set(value) {
                _startColorStyle?.removeChangeListener(startColorStyleChangeListener!!)
                if (value != null) {
                    _startColorStyle = value
                    _startColorStyleUID = value.uid
                    value.addChangeListener(startColorStyleChangeListener!!)
                    startColorStyleChangeListener!!()
                } else {
                    _startColorStyle = null
                    _startColorStyleUID = null
                    change()
                }
            }
        @Transient
        private var startColorStyleChangeListener: (() -> Unit)? = null
        
        
        var _endColorStyleUID: Long? = null
        @Transient
        private var _endColorStyle: ColorStyle? = null
        var endColorStyle: ColorStyle?
            get() = _endColorStyle
            set(value) {
                _endColorStyle?.removeChangeListener(endColorStyleChangeListener!!)
                if (value != null) {
                    _endColorStyle = value
                    _endColorStyleUID = value.uid
                    value.addChangeListener(endColorStyleChangeListener!!)
                    endColorStyleChangeListener!!()
                } else {
                    _endColorStyle = null
                    _endColorStyleUID = null
                    change()
                }
            }
        @Transient
        private var endColorStyleChangeListener: (() -> Unit)? = null

        fun extractStartcolorStyle() : ColorStyle {
            val cs = ColorStyle()
            cs.name = "Gradient start color"
            cs.color = startColor
            return cs
        }
        fun extractEndcolorStyle() : ColorStyle {
            val cs = ColorStyle()
            cs.name = "Gradient end color"
            cs.color = endColor
            return cs
        }

        fun init () {
            startColorStyleChangeListener = {
                _startColor = startColorStyle!!.color
                change()
            }
            endColorStyleChangeListener = {
                _endColor = endColorStyle!!.color
                change()
            }
        }
    }


    
    
    
    class Shadow {

        //listeners
        @Transient
        private var listeners: MutableList<() -> Unit> = mutableListOf()
        fun addChangeListener (l: () -> Unit) {
            if (listeners == null)
                listeners = mutableListOf()
            listeners.add(l)
        }
        fun removeChangeListener (l: () -> Unit) {
            listeners.remove(l)
        }
        fun removeAllChangeListeners () {
            listeners.clear()
        }
        fun change () {
            for (listener in listeners) {
                listener()
            }
        }

        //blurradius
        private var _blur: Int = 0
        var blur: Int
            get() = _blur
            set(value) {
                _blur = value
                change()
            }
        //xpos
        private var _xpos: Int = 0
        var xpos: Int
            get() = _xpos
            set(value) {
                _xpos = value
                change()
            }
        //ypos
        private var _ypos: Int = 0
        var ypos: Int
            get() = _ypos
            set(value) {
                _ypos = value
                change()
            }

        constructor(blur: Int = 0, xpos: Int = 0, ypos: Int = 0) {
            this.blur = blur
            this.xpos = xpos
            this.ypos = ypos
        }

    }





    private var _fillColorStyleUID: Long? = null
    @Transient
    private var _fillColorStyle: ColorStyle? = null
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
                change()
            }
        }

    @Transient
    private var fillColorStyleChangeListener: (() -> Unit)? = null


    private var _strokeColorStyleUID: Long? = null
    @Transient
    private var _strokeColorStyle: ColorStyle? = null
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
                change()
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
        val calcXpos = d.dipToPxF(actualXpos)
        val calcYpos = d.dipToPxF(actualYpos)
        val calcWidth = d.dipToPxF(width)
        val calcHeight = d.dipToPxF(height)
        var type = Renderable.Type.RECT
        var shape: Any = RectF(0F, 0F, calcWidth, calcHeight)
        if (cornerRadius > 0F) {
            type = Renderable.Type.ROUNDRECT
            shape = RoundRect(shape as RectF, d.dipToPxF(cornerRadius))
        }
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        paintFill.style = Paint.Style.FILL
        if (gradient != null) {
            paintFill.isDither = true
            when (gradient!!.direction) {
                Gradient.Direction.HORIZONTAL -> {
                    paintFill.shader = LinearGradient(0F, 0F, calcWidth, 0F, gradient!!.startColor, gradient!!.endColor, Shader.TileMode.CLAMP)
                }
                Gradient.Direction.VERTICAL -> {
                    paintFill.shader = LinearGradient(0F, 0F, 0F, calcHeight, gradient!!.startColor, gradient!!.endColor, Shader.TileMode.CLAMP)
                }
                Gradient.Direction.CIRCLE -> {
                    paintFill.shader = RadialGradient(calcWidth / 2F, calcHeight / 2F, Math.min(calcWidth, calcHeight) / 2F, gradient!!.startColor, gradient!!.endColor, Shader.TileMode.CLAMP)
                }
            }
        } else {
            paintFill.color = fillColor
            paintFill.alpha = (alpha * paintFill.alpha) / 255
        }
        if (shadow != null) {
            paintFill.setShadowLayer(d.dipToPxF(shadow!!.blur), d.dipToPxF(shadow!!.xpos), d.dipToPxF(shadow!!.ypos), Color.parseColor("#99000000"))
        }
        val position = Renderable.Position(calcXpos, calcYpos, rotation, d.dipToPxF(width)/2F, d.dipToPxF(height)/2F)
        renderables.add(Renderable(type, shape, position, paintFill))
        if (strokeWidth > 0) {
            val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
            paintStroke.style = Paint.Style.STROKE
            paintStroke.color = strokeColor
            paintStroke.alpha = (alpha * paintStroke.alpha) / 255
            paintStroke.strokeWidth = d.dipToPxF(strokeWidth)
            val position = Renderable.Position(calcXpos, calcYpos, rotation, d.dipToPxF(width)/2F, d.dipToPxF(height)/2F)
            renderables.add(Renderable(type, shape, position, paintStroke))
        }
        hasChanged = false
        return renderables.toTypedArray()
    }

    class RoundRect(val rectF: RectF, val radius: Float)

    override fun getMainColor(): Int {
        return fillColor
    }

    override fun init (ctx: Context, board: BoardObject?) {
        shadow?.addChangeListener {
            change()
        }
        gradient?.addChangeListener {
            change()
        }
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
            _fillColor = fillColorStyle!!.color
            change()
        }
        strokeColorStyleChangeListener = {
            _strokeColor = strokeColorStyle!!.color
            change()
        }
        if (board != null) {
            fillColorStyle = board.styles.colorStyles[_fillColorStyleUID]
            strokeColorStyle = board.styles.colorStyles[_strokeColorStyleUID]
            gradient?.startColorStyle = board.styles.colorStyles[gradient?._startColorStyleUID]
            gradient?.endColorStyle = board.styles.colorStyles[gradient?._endColorStyleUID]
        }
        super.init(ctx, board)
    }

    override fun close() {
        shadow?.removeAllChangeListeners()
        super.close()
    }


    override fun styleIsUsed(style: BaseStyle): Boolean {
        if (super.styleIsUsed(style) || style == fillColorStyle || style == strokeColorStyle) return true
        return false
    }

    override fun clone () : RectObject {
        val obj = RectObject()
        applyBaseClone(obj)
        if (shadow != null) {
            obj.shadow = Shadow(shadow!!.blur, shadow!!.xpos, shadow!!.ypos)
        }
        obj.fillColor = fillColor
        obj.strokeWidth = strokeWidth
        obj.strokeColor = strokeColor
        obj.cornerRadius = cornerRadius
        return obj
    }

    override fun export(ec: ExportContainer) {
        super.export(ec)
        if (fillColorStyle != null)
            ec.colorStyles.put(fillColorStyle!!.uid, fillColorStyle!!)
        if (strokeColorStyle != null)
            ec.colorStyles.put(strokeColorStyle!!.uid, strokeColorStyle!!)
    }

}