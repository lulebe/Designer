package de.lulebe.designer.data.objects

import android.content.Context
import android.graphics.*
import android.support.v7.graphics.Palette
import de.lulebe.designer.data.Deserializer
import de.lulebe.designer.data.ImageSource
import de.lulebe.designer.data.styles.ColorStyle
import java.io.File
import java.lang.ref.WeakReference


class ImageObject() : SourceObject() {


    constructor(context: Context) : this() {
        ctx = WeakReference(context)
    }


    @Transient
    private var ctx: WeakReference<Context>? = null

    @Transient
    private var mainColorCached = Color.BLACK

    private var _imageSource: ImageSource = ImageSource.USER
    var imageSource: ImageSource
        get() = _imageSource
        set(value) {
            _imageSource = value
            change()
        }

    private var _src: String = ""
    var src: String
        get() = _src
        set(value) {
            _src = value
            change()
        }

    private var _tintColor: Int = Color.BLACK
    var tintColor: Int
        get() = _tintColor
        set(value) {
            _tintColor = value
            change()
        }

    private var _tinted: Boolean = false
    var tinted: Boolean
        get() = _tinted
        set(value) {
            _tinted = value
            change()
        }

    private var _tintColorStyleUID: Long? = null
    @Transient
    private var _tintColorStyle: ColorStyle? = null
    var tintColorStyle: ColorStyle?
        get() = _tintColorStyle
        set(value) {
            _tintColorStyle?.removeChangeListener(tintColorStyleChangeListener)
            if (value != null) {
                _tintColorStyle = value
                _tintColorStyleUID = value.uid
                value.addChangeListener(tintColorStyleChangeListener)
                tintColorStyleChangeListener()
            } else {
                _tintColorStyleUID = null
                _tintColorStyle = null
                change()
            }
        }

    @Transient
    private var tintColorStyleChangeListener = {
        tintColor = tintColorStyle!!.color
    }


    fun setIncludedImage (imageSource: ImageSource, file: String) {
        _imageSource = imageSource
        _src = file
        change()
    }

    fun setExternalImage (file: String) {
        _imageSource = ImageSource.USER
        _src = file
        change()
    }

    override fun close () {
        super.close()

    }

    override fun getRenderables(d: Deserializer, forceReload: Boolean): Array<Renderable> {
        if (!forceReload && !hasChanged)
            return renderables.toTypedArray()
        if (renderables.size > 0) {
            (renderables[0].shape as Bitmap).recycle()
        }
        renderables.clear()
        if (alpha == 0 || ctx == null || ctx!!.get() == null || src.equals("")) {
            hasChanged = false
            return emptyArray()
        }
        val rawBmp : Bitmap
        if (imageSource != ImageSource.USER) {
            val path = imageSource.name + File.separator + src
            rawBmp = BitmapFactory.decodeStream(ctx!!.get().assets.open(path))
        } else {
            if (!File(src).exists() || !File(src).canRead()) {
                hasChanged = false
                return emptyArray()
            }
            rawBmp = BitmapFactory.decodeFile(src)
        }
        val bmp = Bitmap.createScaledBitmap(
                rawBmp,
                d.dipToPxI(width),
                d.dipToPxI(height),
                true)
        rawBmp.recycle()
        val palette = Palette.from(bmp).clearFilters().maximumColorCount(1).generate()
        if (palette.swatches.size > 0)
            mainColorCached = palette.swatches[0].rgb
        val paint = Paint()
        paint.alpha = alpha
        if (tinted)
            paint.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        renderables.add(Renderable(Renderable.Type.IMAGE, bmp, d.dipToPxF(xpos), d.dipToPxF(ypos), paint))
        hasChanged = false
        return renderables.toTypedArray()
    }

    override fun init(ctx: Context, board: BoardObject?) {
        super.init(ctx, board)
        this.ctx = WeakReference(ctx)
        tintColorStyleChangeListener = {
            tintColor = tintColorStyle!!.color
        }
    }


    override fun getMainColor(): Int {
        return mainColorCached
    }


    override fun clone(): ImageObject {
        val obj = ImageObject()
        applyBaseClone(obj)
        obj.imageSource = imageSource
        obj.src = src
        return obj
    }

}