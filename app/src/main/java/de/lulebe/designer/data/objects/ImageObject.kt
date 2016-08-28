package de.lulebe.designer.data.objects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.graphics.Palette
import de.lulebe.designer.data.Deserializer
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

    private var _included = true
    var included: Boolean
        get() = _included
        set(value) {
            _included = value
            change()
        }

    private var _src: String = ""
    var src: String
        get() = _src
        set(value) {
            _src = value
            change()
        }


    fun setIncludedImage (file: String) {
        _included = true
        _src = file
        change()
    }

    fun setExternalImage (file: String) {
        _included = false
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
        if (included) {
            rawBmp = BitmapFactory.decodeStream(ctx!!.get().assets.open(src))
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
        renderables.add(Renderable(Renderable.Type.IMAGE, bmp, d.dipToPxF(xpos), d.dipToPxF(ypos), paint))
        hasChanged = false
        return renderables.toTypedArray()
    }

    override fun init(ctx: Context, board: BoardObject?) {
        super.init(ctx, board)
        this.ctx = WeakReference(ctx)
    }


    override fun getMainColor(): Int {
        return mainColorCached
    }


    override fun clone(): ImageObject {
        val obj = ImageObject()
        applyBaseClone(obj)
        obj.included = included
        obj.src = src
        return obj
    }

}