package de.lulebe.designer.data.objects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import de.lulebe.designer.Renderer
import de.lulebe.designer.data.*
import de.lulebe.designer.data.styles.BaseStyle
import de.lulebe.designer.data.styles.Styles
import java.io.File


class BoardObject() : SourceObject() {



    //sizeListeners
    @Transient
    private var sizeListeners = mutableListOf<() -> Unit>()
    fun addSizeChangeListener (l: () -> Unit) {
        sizeListeners.add(l)
    }
    fun removeSizeChangeListener (l: () -> Unit) {
        sizeListeners.remove(l)
    }
    fun removeAllSizeChangeListeners () {
        sizeListeners.clear()
    }
    protected  fun sizeChange () {
        hasChanged = true
        hasChangedSize = true
        for (listener in sizeListeners) {
            listener()
        }
    }

    //gridListeners
    @Transient
    private var gridListeners = mutableListOf<() -> Unit>()
    fun addGridChangeListener (l: () -> Unit) {
        gridListeners.add(l)
    }
    fun removeGridChangeListener (l: () -> Unit) {
        gridListeners.remove(l)
    }
    fun removeAllGridChangeListeners () {
        gridListeners.clear()
    }
    private fun gridChange () {
        for (listener in gridListeners) {
            listener()
        }
    }


    @Transient
    private var hasChangedSize = true

    @Transient
    private var renderedBitmap: Bitmap? = null

    @Transient
    private var _parentBoard: BoardObject? = null
    var parentBoard: BoardObject?
        get() = _parentBoard
        set(value) {
            _parentBoard = value
        }

    @Transient
    private var _storageManager: StorageManager? = null
    var storageManager: StorageManager?
        get() {
            if (_parentBoard == null)
                return _storageManager
            else
               return _parentBoard?.storageManager
        }
        set(value) {
            _storageManager = value
        }


    override var width: Int
        get() = _width
        set(value) {
            if (widthMoving == _width)
                widthMoving = value
            _width = value
            sizeChange()
            gridChange()
        }

    override var height: Int
        get() = _height
        set(value) {
            if (heightMoving == _height)
                heightMoving = value
            _height = value
            sizeChange()
            gridChange()
        }

    private var _gridSize = 4
    var gridSize: Int
        get() = _gridSize
        set(value) {
            _gridSize = value
            gridChange()
        }

    private var _gridInterval = 4
    var gridInterval: Int
        get() = _gridInterval
        set(value) {
            _gridInterval = value
            gridChange()
        }

    init {
        _name = "Group"
    }

    private val _styles = Styles()
    val styles: Styles
        get() {
            if (_parentBoard == null)
                return  _styles
            else
                 return _parentBoard!!.styles
        }

    private val _images: MutableMap<Long, String> = mutableMapOf()
    val images: MutableMap<Long, String>
        get() {
            if (_parentBoard == null)
                return _images
            else
                return _parentBoard!!.images
        }

    private val _fonts: MutableMap<Long, String> = mutableMapOf()
    val fonts: MutableMap<Long, String>
        get() {
            if (_parentBoard == null)
                return _fonts
            else
                return _parentBoard!!.fonts
        }

    private val _objects: MutableList<BaseObject> = mutableListOf()
    val objects: MutableList<BaseObject>
        get() = _objects

    fun addObject (baseObject: BaseObject) {
        _objects.add(baseObject)
        baseObject.addChangeListener {
            change()
        }
        change()
    }

    fun unlockCopy (copyObject: CopyObject) : SourceObject? {
        if (copyObject.source == null) return null
        val newObj = copyObject.source!!.clone() as SourceObject
        newObj.name = copyObject.name
        newObj.xpos = copyObject.xpos
        newObj.ypos = copyObject.ypos
        newObj.rotation = copyObject.rotation
        _objects.add(_objects.indexOf(copyObject), newObj)
        _objects.remove(copyObject)
        copyObject.close()
        copyObject.source!!.copies--
        newObj.addChangeListener {
            change()
        }
        change()
        return newObj
    }

    @Throws(CannotDeleteCopiedObjectException::class)
    fun removeObject (baseObject: BaseObject) {
        if (baseObject.copies == 0) {
            baseObject.close()
            if (baseObject is CopyObject) {
                baseObject.source?.copies?.minus(1)
            }
            _objects.remove(baseObject)
            change()
        } else {
            throw CannotDeleteCopiedObjectException()
        }
    }

    fun reorderedObjects () {
        change()
    }

    fun getImagePath(uid: Long) : String? {
        if (images.containsKey(uid) && storageManager != null)
            return storageManager!!.getPath() + File.separator + uid + "." + File(images[uid]).extension
        return null
    }

    fun getFontPath(uid: Long) : String? {
        if (fonts.containsKey(uid) && storageManager != null)
            return storageManager!!.getPath() + File.separator + uid + "." + File(fonts[uid]).extension
        return null
    }

    fun removeUnusedFiles () {
        if (parentBoard != null || storageManager == null) return
        val sm = storageManager!!
        for ((key) in images) {
            var used = false
            for (obj in objects) {
                if (obj is ImageObject) {
                    val io = obj as ImageObject
                    if (io.imageSource == ImageSource.USER && io.src == key.toString()) {
                        used = true
                        break
                    }
                }
            }
            if (!used) {
                sm.removeImage(key)
                images.remove(key)
            }
        }
        for ((key) in fonts) {
            var used = false
            for (obj in objects) {
                if (obj is TextObject) {
                    val to = obj as TextObject
                    if (to.fontUID == key) {
                        used = true
                        break
                    }
                }
            }
            if (!used) {
                sm.removeFont(key)
                fonts.remove(key)
                FontCache.fonts.remove(key)
            }
        }
    }

    override fun init (ctx: Context, board: BoardObject?) {
        styles.init()
        parentBoard = board
        super.init(ctx, board)
        for (obj in _objects) {
            obj.init(ctx, this)
            obj.addChangeListener {
                change()
            }
        }
        _xposMoving = xpos
        _yposMoving = ypos
        _widthMoving = width
        _heightMoving = height
        calculateHandles()
    }

    override fun close () {
        super.close()
        renderedBitmap?.recycle()
    }

    override fun canDirectlyChangeWidth () = false
    override fun canDirectlyChangeHeight () = false

    override fun getRenderables (d: Deserializer, forceReload: Boolean) : Array<Renderable> {
        if (!forceReload && !hasChanged)
            return renderables.toTypedArray()
        if (alpha == 0)
            return emptyArray()
        renderables.clear()
        for (obj in _objects) {
            renderables.addAll((obj as IRenderable).getRenderables(d, forceReload))
        }
        if (renderedBitmap == null)
            renderedBitmap = Bitmap.createBitmap(d.dipToPxI(width), d.dipToPxI(height), Bitmap.Config.ARGB_8888)
        else if (hasChangedSize) {
            renderedBitmap = Bitmap.createBitmap(d.dipToPxI(width), d.dipToPxI(height), Bitmap.Config.ARGB_8888)
        }
        Renderer.drawRenderables(renderables.toTypedArray(), Canvas(renderedBitmap), false)
        renderables.clear()
        val paint = Paint()
        paint.alpha = alpha
        renderables.add(Renderable(Renderable.Type.IMAGE, renderedBitmap!!, d.dipToPxF(xpos), d.dipToPxF(ypos), rotation, paint))
        return renderables.toTypedArray()
    }

    fun getObjectAtPosition (x: Int, y: Int) : BaseObject? {
        var i = _objects.size
        while (--i >= 0) {
            val o = _objects[i]
            if (x > o.xpos && x < (o.xpos+o.width) && y > o.ypos && y < (o.ypos+o.height)) {
                return o
            }
        }
        return null
    }

    fun getObjectWithUID (uid: Long) : BaseObject? {
        for (obj in _objects) {
            if (obj.uid == uid)
                return obj
        }
        return null
    }


    override fun styleIsUsed(style: BaseStyle): Boolean {
        if (super.styleIsUsed(style)) return true
        for (obj in _objects) {
            if (obj.styleIsUsed(style))
                return true
        }
        return false
    }


    override fun clone(): BoardObject {
        val obj = BoardObject()
        applyBaseClone(obj)
        obj.gridSize = gridSize
        obj.gridInterval = gridInterval
        for (o in objects) {
            obj.addObject(o.clone())
        }
        return obj
    }

    override fun export(ec: ExportContainer) {
        super.export(ec)
        for (obj in objects) {
            obj.export(ec)
        }
    }


    class CannotDeleteCopiedObjectException : Exception() {}

}
