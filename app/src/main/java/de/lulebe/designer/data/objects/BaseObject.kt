package de.lulebe.designer.data.objects

import android.content.Context
import android.graphics.*
import de.lulebe.designer.data.Deserializer
import de.lulebe.designer.data.ExportContainer
import de.lulebe.designer.data.UIDGenerator
import de.lulebe.designer.data.styles.BaseStyle
import de.lulebe.designer.data.styles.BoxStyle


abstract class BaseObject : IRenderable {

    //listeners
    @Transient
    protected var listeners = mutableListOf<() -> Unit>()
    fun addChangeListener (l: () -> Unit) {
        listeners.add(l)
    }
    fun removeChangeListener (l: () -> Unit) {
        listeners.remove(l)
    }
    fun removeAllChangeListeners () {
        listeners.clear()
    }
    protected fun change () {
        hasChanged = true
        for (listener in listeners) {
            listener()
        }
    }

    @Transient
    private val handles = Array<Rect>(4, { Rect() })

    @Transient
    protected val renderables = mutableListOf<Renderable>()

    @Transient
    protected var _hasChanged = true
    @Transient
    var hasChanged = _hasChanged
        get() = _hasChanged

    @Transient
    var copies = 0

    //id
    val uid: Long = UIDGenerator.generateUID()


    //name
    protected var _name: String = "Object"
    var name: String
        get() = _name
        set(value) {
            _name = value
            change()
        }


    //xpos
    protected var _xpos: Int = 0
    var xpos: Int
        get() = _xpos
        set(value) {
            if (xposMoving == _xpos)
                xposMoving = value
            _xpos = value
            change()
        }

    @Transient
    protected var _xposMoving: Int = _xpos
    var xposMoving: Int
        get() = _xposMoving
        set(value) {
            _xposMoving = value
            calculateHandles()
        }


    //ypos
    protected var _ypos: Int = 0
    var ypos: Int
        get() = _ypos
        set(value) {
            if (yposMoving == _ypos)
                yposMoving = value
            _ypos = value
            change()
        }

    @Transient
    protected var _yposMoving: Int = _ypos
    var yposMoving: Int
        get() = _yposMoving
        set(value) {
            _yposMoving = value
            calculateHandles()
        }


    //width
    protected var _width: Int = 50
    open var width: Int
        get() = _width
        set(value) {
            if (_boxStyle != null)
                boxStyle = null
            if (widthMoving == _width)
                widthMoving = value
            _width = value
            change()
        }

    @Transient
    protected var _widthMoving: Int = _width
    var widthMoving: Int
        get() = _widthMoving
        set(value) {
            _widthMoving = value
            calculateHandles()
        }


    //height
    protected var _height: Int = 50
    open var height: Int
        get() = _height
        set(value) {
            if (_boxStyle != null)
                boxStyle = null
            if (heightMoving == _height)
                heightMoving = value
            _height = value
            change()
        }

    @Transient
    protected var _heightMoving: Int = _height
    var heightMoving: Int
        get() = _heightMoving
        set(value) {
            _heightMoving = value
            calculateHandles()
        }

    @Transient
    private val rotationMatrix = Matrix()
    // rotation
    private var _rotation: Float = 0F
    var rotation: Float
        get() = _rotation
        set(value) {
            _rotation = value
            calculateHandles()
            change()
        }


    private var _boxStyleUID: Long? = null
    @Transient
    protected var _boxStyle: BoxStyle? = null
    var boxStyle: BoxStyle?
        get() = _boxStyle
        set(value) {
            if (!canAcceptBoxStyle()) {
                _boxStyle = null
                _boxStyleUID = null
                change()
            } else {
                _boxStyle?.removeChangeListener(boxStyleChangeListener!!)
                if (value != null) {
                    _boxStyle = value
                    _boxStyleUID = value.uid
                    value.addChangeListener(boxStyleChangeListener!!)
                    boxStyleChangeListener!!()
                } else {
                    _boxStyle = null
                    _boxStyleUID = null
                    change()
                }
            }
        }

    @Transient
    open protected var boxStyleChangeListener: (() -> Unit)? = null


    open fun close () {
        removeAllChangeListeners()
    }

    open fun canDirectlyChangeWidth() : Boolean {
        return true
    }
    open fun canDirectlyChangeHeight() : Boolean {
        return true
    }
    open fun canAcceptBoxStyle() = true

    open fun extractBoxStyle() : BoxStyle {
        val bs = BoxStyle()
        bs.name = name + " Box style"
        bs.width = width
        bs.height = height
        return bs
    }


    open fun styleIsUsed (style: BaseStyle) : Boolean {
        return style == boxStyle
    }


    fun movingTo(x: Int = xpos, y: Int = ypos) {
        xposMoving = x
        yposMoving = y
        calculateHandles()
    }
    open fun applyMovement() {
        _xpos = xposMoving
        _ypos = yposMoving
        _width = widthMoving
        _height = heightMoving
        calculateHandles()
        change()
    }
    fun resetHandles() {
        xposMoving = xpos
        yposMoving = ypos
        widthMoving = width
        heightMoving = height
        calculateHandles()
    }
    fun setHandlePosition (handle: Int, x: Int, y: Int) {
        val centerXold = xposMoving + widthMoving/2
        val centerYold = yposMoving + heightMoving/2
        when (handle) {
            1 -> {
                if (x >= xposMoving + widthMoving)
                    return
                _widthMoving += -(x - xposMoving)
                _xposMoving = x
            }
            2 -> {
                if (y >= yposMoving + heightMoving)
                    return
                _heightMoving += -(y - yposMoving)
                _yposMoving = y
            }
            3 -> {
                val add = x - (xposMoving + widthMoving)
                if (-add >= widthMoving)
                    return
                _widthMoving += add
            }
            4 -> {
                val add = y - (yposMoving + heightMoving)
                if (-add >= heightMoving)
                    return
                _heightMoving += add
            }
        }
        calculateHandles()
    }


    open fun init (ctx: Context, board: BoardObject?) {
        if (boxStyleChangeListener == null)
            boxStyleChangeListener = {
                if (canDirectlyChangeWidth()) {
                    if (_widthMoving == _width)
                        _widthMoving = boxStyle!!.width
                    _width = boxStyle!!.width
                }
                if (canDirectlyChangeHeight()) {
                    if (_heightMoving == _height)
                        _heightMoving = boxStyle!!.height
                    _height = boxStyle!!.height
                }
                calculateHandles()
                change()
            }
        if (board != null)
            boxStyle = board.styles.boxStyles[_boxStyleUID]
        _xposMoving = xpos
        _yposMoving = ypos
        _widthMoving = width
        _heightMoving = height
        calculateHandles()
    }


    override fun getBoundRect (d: Deserializer, xOffset: Float, yOffset: Float): RectF {
        return RectF(d.dipToPxF(xposMoving) + xOffset,
                d.dipToPxF(yposMoving) + yOffset,
                d.dipToPxF(xposMoving+widthMoving) + xOffset,
                d.dipToPxF(yposMoving+heightMoving) + yOffset)
    }


    protected fun calculateHandles () {
        var rotRad = Math.toRadians(rotation.toDouble())
        val rad90deg = Math.toRadians(90.0)
        val centerX = xposMoving + widthMoving/2
        val centerY = yposMoving + heightMoving/2
        calculateHandle(handles[0], centerX - (Math.cos(rotRad)*widthMoving/2).toInt(),
                centerY - (Math.sin(rotRad)*widthMoving/2).toInt())
        rotRad += rad90deg
        calculateHandle(handles[1], centerX - (Math.cos(rotRad)*heightMoving/2).toInt(),
                centerY - (Math.sin(rotRad)*heightMoving/2).toInt())
        rotRad += rad90deg
        calculateHandle(handles[2], centerX - (Math.cos(rotRad)*widthMoving/2).toInt(),
                centerY - (Math.sin(rotRad)*widthMoving/2).toInt())
        rotRad += rad90deg
        calculateHandle(handles[3], centerX - (Math.cos(rotRad)*heightMoving/2).toInt(),
                centerY - (Math.sin(rotRad)*heightMoving/2).toInt())
    }

    private fun calculateHandle (handle: Rect, cx: Int, cy: Int) {
        handle.left = cx - 12
        handle.top = cy - 12
        handle.right = cx + 12
        handle.bottom = cy + 12
    }

    fun getHandleAt (x: Int, y: Int) : Int {
        var found = -1
        var i = 1
        val boundPath = Path()
        boundPath.addRect(xpos.toFloat(), ypos.toFloat(), (xpos+width).toFloat(), (ypos+height).toFloat(), Path.Direction.CW)
        val matrix = Matrix()
        matrix.setRotate(rotation, (xpos+(width/2F)).toFloat(), (ypos+(height/2F)).toFloat())
        boundPath.transform(matrix)
        val pointPath = Path()
        pointPath.addRect(x.toFloat(), y.toFloat(), x.toFloat()+0.1F, y.toFloat()+0.1F, Path.Direction.CW)
        if (boundPath.op(pointPath, Path.Op.INTERSECT) && !boundPath.isEmpty)
            found = 0
        if (rotation == 0F)
            for (handle in handles) {
                if ((i % 2 == 1 && !canDirectlyChangeWidth()) || (i % 2 == 0 && !canDirectlyChangeHeight()))
                    continue
                if (x >= handle.left && x <= handle.right && y >= handle.top && y <= handle.bottom) {
                    found = i
                    break
                }
                i++
            }
        return found
    }

    override fun getHandleRenderables(d: Deserializer, xOffset: Float, yOffset: Float, showHandles: Boolean): Array<Renderable> {
        val elems = mutableListOf<Renderable>()
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        boxPaint.color = Color.parseColor("#FF8800")
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = d.pixelFac
        val position = Renderable.Position(0F, 0F, rotation,
                d.dipToPxF(xposMoving+(widthMoving/2))+xOffset,
                d.dipToPxF(yposMoving+(heightMoving/2))+yOffset)
        elems.add(Renderable(Renderable.Type.RECT, getBoundRect(d, xOffset, yOffset), position, boxPaint))
        if (!showHandles)
            return elems.toTypedArray()
        if (rotation == 0F && (canDirectlyChangeHeight() || canDirectlyChangeWidth())) {
            val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            handlePaint.color = Color.parseColor("#FF8800")
            if (canDirectlyChangeWidth()) {
                var shape = RectF(d.dipToPxF(handles[0].left) + xOffset,
                        d.dipToPxF(handles[0].top) + yOffset,
                        d.dipToPxF(handles[0].right) + xOffset,
                        d.dipToPxF(handles[0].bottom) + yOffset)
                elems.add(Renderable(Renderable.Type.OVAL, shape, Renderable.Position(0F, 0F, 0F, 0F, 0F), handlePaint))
                shape = RectF(d.dipToPxF(handles[2].left) + xOffset,
                        d.dipToPxF(handles[2].top) + yOffset,
                        d.dipToPxF(handles[2].right) + xOffset,
                        d.dipToPxF(handles[2].bottom) + yOffset)
                elems.add(Renderable(Renderable.Type.OVAL, shape, Renderable.Position(0F, 0F, 0F, 0F, 0F), handlePaint))
            }
            if (canDirectlyChangeHeight()) {
                var shape = RectF(d.dipToPxF(handles[1].left) + xOffset,
                        d.dipToPxF(handles[1].top) + yOffset,
                        d.dipToPxF(handles[1].right) + xOffset,
                        d.dipToPxF(handles[1].bottom) + yOffset)
                elems.add(Renderable(Renderable.Type.OVAL, shape, Renderable.Position(0F, 0F, 0F, 0F, 0F), handlePaint))
                shape = RectF(d.dipToPxF(handles[3].left) + xOffset,
                        d.dipToPxF(handles[3].top) + yOffset,
                        d.dipToPxF(handles[3].right) + xOffset,
                        d.dipToPxF(handles[3].bottom) + yOffset)
                elems.add(Renderable(Renderable.Type.OVAL, shape, Renderable.Position(0F, 0F, 0F, 0F, 0F), handlePaint))
            }
        }
        return elems.toTypedArray()
    }


    open fun getMainColor () : Int {
        return Color.BLACK
    }

    open fun export (ec: ExportContainer) {
        val newObj = this.clone()
        ec.objects.put(newObj.uid, newObj)
        if (boxStyle != null)
            ec.boxStyles.put(boxStyle!!.uid, boxStyle!!)
    }

    abstract fun clone () : BaseObject

}