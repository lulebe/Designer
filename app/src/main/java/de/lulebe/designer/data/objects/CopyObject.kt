package de.lulebe.designer.data.objects

import android.content.Context
import de.lulebe.designer.data.Deserializer


class CopyObject : BaseObject() {

    @Transient
    var source: SourceObject? = null

    @Transient
    var sourceListener: () -> Unit = {}

    var sourceId: String = ""


    override fun canDirectlyChangeWidth() = false
    override fun canDirectlyChangeHeight() = false
    override fun canAcceptBoxStyle() = false

    override fun init(ctx: Context, board: BoardObject?) {
        if (board != null)
            this.source = board.getObjectWithUID(sourceId) as SourceObject
        if (source != null) {
            source!!.copies++
            _width = source!!.width
            _height = source!!.height
            sourceListener = {
                _width = source!!.width
                _height = source!!.height
                change()
                _widthMoving = width
                _heightMoving = height
                calculateHandles()
            }
            source!!.addChangeListener(sourceListener)
        }
        super.init(ctx, board)
    }

    override fun close() {
        source?.removeChangeListener(sourceListener)
        super.close()
    }

    override fun clone(): CopyObject {
        throw UnsupportedOperationException()
    }

    override fun getRenderables(d: Deserializer, forceReload: Boolean): Array<Renderable> {
        if (source == null)
            return emptyArray()
        val src = source!!
        if (!src.hasChanged && !forceReload)
            return renderables.toTypedArray()
        renderables.clear()
        val srcRend = src.getRenderables(d, forceReload)
        for (rend in srcRend) {
            renderables.add(Renderable(rend.type, rend.shape, d.dipToPxF(xpos), d.dipToPxF(ypos), rend.paint))
        }
        return renderables.toTypedArray()
    }

    override fun getMainColor(): Int {
        if (source == null) return super.getMainColor()
        return source!!.getMainColor()
    }
}