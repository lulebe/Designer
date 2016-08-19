package de.lulebe.designer.data.objects

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import de.lulebe.designer.data.Deserializer
import de.lulebe.designer.data.styles.BaseStyle
import de.lulebe.designer.data.styles.ColorStyle
import de.lulebe.designer.data.styles.TextStyle


class TextObject : SourceObject() {


    private var _text = ""
    var text: String
        get() = _text
        set(value) {
            _text = value
            calcSizes()
            change()
        }


    private var _alignment = Layout.Alignment.ALIGN_NORMAL
    var alignment: Layout.Alignment
        get() = _alignment
        set(value) {
            _alignment = value
            calcSizes()
            change()
        }


    private var _textColor: Int = Color.BLACK
    var textColor: Int
        get() = _textColor
        set(value) {
            _textColor = value
            change()
        }


    private var _fontSize: Int = 16
    var fontSize: Int
        get() = _fontSize
        set(value) {
            if (_textStyle != null)
                textStyle = null
            _fontSize = value
            calcSizes()
            change()
        }

    override var height: Int
        get() = super.height
        set(value) {
        }

    override var width: Int
        get() = super.width
        set(value) {
            _width = value
            calcSizes()
            change()
        }


    private var _textStyleUID: Long? = null
    @Transient
    private var _textStyle: TextStyle? = null
    var textStyle: TextStyle?
        get() = _textStyle
        set(value) {
            _textStyle?.removeChangeListener(textStyleChangeListener)
            if (value != null) {
                _textStyle = value
                _textStyleUID = value.uid
                value.addChangeListener(textStyleChangeListener)
                textStyleChangeListener()
            } else {
                _textStyleUID = null
            }
        }

    @Transient
    protected val textStyleChangeListener = {
        fontSize = _textStyle!!.fontSize
    }


    private var _textColorStyleUID: Long? = null
    @Transient
    private var _textColorStyle: ColorStyle? = null
    var textColorStyle: ColorStyle?
        get() = _textColorStyle
        set(value) {
            _textColorStyle?.removeChangeListener(textColorStyleChangeListener)
            if (value != null) {
                _textColorStyle = value
                _textColorStyleUID = value.uid
                value.addChangeListener(textColorStyleChangeListener)
                textColorStyleChangeListener()
            } else {
                _textColorStyleUID = null
                _textColorStyle = null
                change()
            }
        }

    @Transient
    private var textColorStyleChangeListener = {
        textColor = _textColorStyle!!.color
    }




    init {
        _text = "Text"
        _width = 100
    }

    override fun canDirectlyChangeHeight() = false


    fun extractTextStyle(): TextStyle {
        val ts = TextStyle()
        ts.name = name + " Text style"
        ts.fontSize = fontSize
        return ts
    }

    fun extractTextColorStyle(): ColorStyle {
        val cs = ColorStyle()
        cs.name = name + " Color style"
        cs.color = textColor
        return cs
    }


    override fun applyMovement() {
        _xpos = xposMoving
        _ypos = yposMoving
        _width = widthMoving
        calcSizes()
        calculateHandles()
        change()
    }

    override fun getRenderables(d: Deserializer, forceReload: Boolean): Array<Renderable> {
        if (!forceReload && !hasChanged)
            return renderables.toTypedArray()
        renderables.clear()

        if (alpha == 0) {
            hasChanged = false
            return emptyArray()
        }
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        paint.color = textColor
        paint.textSize = d.dipToPxF(fontSize)
        paint.alpha = alpha
        val layout = StaticLayout(_text, paint, d.dipToPxI(width), alignment, 1F, 0F, false)
        val r = Renderable(Renderable.Type.TEXT, layout, d.dipToPxF(xpos), d.dipToPxF(ypos), paint)
        renderables.add(r)
        hasChanged = false
        return renderables.toTypedArray()
    }

    private fun calcSizes () {
        val paint = TextPaint()
        paint.textSize = fontSize.toFloat()
        val layout = StaticLayout(_text, paint, width.toInt(), alignment, 1F, 0F, false)
        _width = layout.width
        _widthMoving = layout.width
        _height = layout.height
        _heightMoving = layout.height
        calculateHandles()
    }

    override fun getMainColor(): Int {
        return textColor
    }


    override fun styleIsUsed(style: BaseStyle): Boolean {
        if (super.styleIsUsed(style) || style == textStyle) return true
        return false
    }

    override fun init(ctx: Context, board: BoardObject?) {
        super.init(ctx, board)
        textColorStyleChangeListener = {
            textColor = _textColorStyle!!.color
        }
        if (board != null) {
            textStyle = board.styles.textStyles[_textStyleUID]
            textColorStyle = board.styles.colorStyles[_textColorStyleUID]
        }
    }

    override fun clone(): TextObject {
        val obj = TextObject()
        applyBaseClone(obj)
        obj.text = text
        obj.alignment = alignment
        obj.textColor = textColor
        obj.fontSize = fontSize
        return obj
    }

}