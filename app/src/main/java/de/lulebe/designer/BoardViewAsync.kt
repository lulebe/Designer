package de.lulebe.designer

import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.v4.view.GestureDetectorCompat
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.Deserializer
import de.lulebe.designer.data.objects.BaseObject
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.Renderable
import kotlin.concurrent.thread


class BoardViewAsync(context: Context, val mBoardState: BoardState, val mBoardObject: BoardObject) : View(context) {


    private var mBufferBitmap: Bitmap? = null
    private var mDipRatio = 1F
    private val mDeserializer: Deserializer


    private val mRulerPaint = Paint()
    private val mGridPaint = Paint()

    private val mHorizRulerPic = Picture()
    private val mVertRulerPic = Picture()

    private var mSelectedHandles: Array<Renderable>? = null

    private var mObjectdragTouchee: BaseObject? = null
    private var mObjectdragToucheeHandle = 0
    private var mObjectdragLastX: Int = 0
    private var mObjectdragLastY: Int = 0


    private val mGestureListener = object: GestureDetector.SimpleOnGestureListener() {

        override fun onDown(event: MotionEvent?): Boolean {
            if (event == null)
                return false
            if (mBoardState.selected != null) {
                val x = eventXOnBoard(event)
                val y = eventYOnBoard(event)
                val handleAtPos = mBoardState.selected!!.getHandleAt(x, y)
                if (handleAtPos >= 0) {
                    mObjectdragTouchee = mBoardState.selected!!
                    mObjectdragToucheeHandle = handleAtPos
                    mObjectdragLastX = x
                    mObjectdragLastY = y
                }
            }
            return true
        }

        override fun onSingleTapUp(event: MotionEvent?): Boolean {
            if (event == null) return false
            mBoardState.selected = mBoardObject.getObjectAtPosition(eventXOnBoard(event), eventYOnBoard(event))
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            if (!mBoardState.panningActive) return false
                mBoardState.boardScrollX += distanceX
                mBoardState.boardScrollY += distanceY
            return true
        }
    }//mGestureListener
    private val mGestureDetector = GestureDetectorCompat(context, mGestureListener)






    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        mDipRatio = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1F, resources.displayMetrics)
        mDeserializer = Deserializer(mDipRatio)

        setBackgroundColor(Color.LTGRAY)
        mRulerPaint.color = Color.BLACK
        mRulerPaint.strokeWidth = mDipRatio
        mRulerPaint.style = Paint.Style.STROKE
        mGridPaint.color = Color.GRAY
        mGridPaint.alpha = 30
        mGridPaint.strokeWidth = mDipRatio
        mGridPaint.style = Paint.Style.STROKE

        asyncThread()

        mBoardState.addListener(object: BoardState.BoardStateListener() {
            override fun onShowGrid (shown: Boolean) {
                drawRulerPictures()
                invalidate()
            }
            override fun onShowUI (shown: Boolean) {
                invalidate()
            }
            override fun onBoardScrollX(scrollX: Float) {
                invalidate()
            }
            override fun onBoardScrollY(scrollY: Float) {
                invalidate()
            }
            override fun onSelectObject(obj: BaseObject?) {
                invalidate()
            }
            override fun onPanningActive(active: Boolean) {
                mObjectdragTouchee?.resetHandles()
                invalidate()
            }
        })

        mBoardObject.addSizeChangeListener {
            mBoardState.boardScrollX = 0F
            mBoardState.boardScrollY = 0F
        }
        mBoardObject.addGridChangeListener {
            drawRulerPictures()
            invalidate()
        }
        drawRulerPictures()
    }//init

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        drawRulerPictures()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mBufferBitmap != null)
            canvas.drawBitmap(mBufferBitmap, -mBoardState.boardScrollX, -mBoardState.boardScrollY, null)
        if (mBoardState.showUI) {
            drawRulers(canvas)
            drawSelection(canvas)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }
        if (mGestureDetector.onTouchEvent(event)) {
            return true
        }
        if (!mBoardState.panningActive) {
            return pan(event)
        }
        return false
    }


    private fun eventXOnBoard (event: MotionEvent) : Int {
        return ((event.x + mBoardState.boardScrollX) / mDipRatio).toInt()
    }
    private fun eventYOnBoard (event: MotionEvent) : Int {
        return ((event.y + mBoardState.boardScrollY) / mDipRatio).toInt()
    }


    private fun drawRulers (canvas: Canvas) {
        val interval = mDipRatio * mBoardObject.gridSize * mBoardObject.gridInterval
        canvas.save()
        canvas.translate(- (mBoardState.boardScrollX % interval), 0F)
        canvas.drawPicture(mVertRulerPic)
        canvas.restore()
        canvas.save()
        canvas.translate(0F, - (mBoardState.boardScrollY % interval))
        canvas.drawPicture(mHorizRulerPic)
        canvas.restore()
    }


    private fun drawSelection(canvas: Canvas) {
        if (mBoardState.selected == null) return
        val renderables = mBoardState.selected!!.getHandleRenderables(mDeserializer, -mBoardState.boardScrollX, -mBoardState.boardScrollY, !mBoardState.panningActive)
        for (renderable in renderables)
            Renderer.drawRenderable(renderable, canvas, true)
    }



    private fun drawRulerPictures () {
        val size = mBoardObject.gridSize
        val interval = mBoardObject.gridInterval
        val dipSize = mDipRatio * size
        val width = this.width + 2*dipSize*interval
        val height = this.height + 2*dipSize*interval
        var canvas = mVertRulerPic.beginRecording(width.toInt(), height.toInt())
        var curLine = 0F
        var steps = 0
        while (curLine < width) {
            if (steps == 0) {
                canvas.drawLine(curLine, 0F, curLine, mDipRatio * 4 * 3, mRulerPaint)
                if (mBoardState.showGrid)
                    canvas.drawLine(curLine, 0F, curLine, height, mGridPaint)
            } else {
                canvas.drawLine(curLine, 0F, curLine, mDipRatio * 4, mRulerPaint)
            }
            curLine += dipSize
            steps++
            if (steps == interval)
                steps = 0
        }
        mVertRulerPic.endRecording()

        canvas = mHorizRulerPic.beginRecording(width.toInt(), height.toInt())
        curLine = 0F
        steps = 0
        while (curLine < height) {
            if (steps == 0) {
                canvas.drawLine(0F, curLine, mDipRatio * 4 * 3, curLine, mRulerPaint)
                if (mBoardState.showGrid)
                    canvas.drawLine(0F, curLine, width, curLine, mGridPaint)
            } else {
                canvas.drawLine(0F, curLine, mDipRatio * 4, curLine, mRulerPaint)
            }
            curLine += dipSize
            steps++
            if (steps == interval)
                steps = 0
        }
        mHorizRulerPic.endRecording()
    }



    private fun pan (event: MotionEvent) : Boolean {
        if (mObjectdragTouchee != null && event.action == MotionEvent.ACTION_MOVE) {
            val target = mObjectdragTouchee!!
            val rawX = eventXOnBoard(event)
            val rawY = eventYOnBoard(event)
            val snappedX: Int
            val snappedY: Int
            when (mObjectdragToucheeHandle) {
                0 -> {
                    snappedX = snap(rawX, target.xposMoving + (rawX - mObjectdragLastX))
                    snappedY = snap(rawY, target.yposMoving + (rawY - mObjectdragLastY))
                    target.movingTo(target.xposMoving + (snappedX - mObjectdragLastX), target.yposMoving + (snappedY - mObjectdragLastY))
                }
                1 -> {
                    snappedX = snap(rawX, target.xposMoving + (rawX - mObjectdragLastX))
                    snappedY = rawY
                    target.setHandlePosition(1, snappedX, snappedY)
                }
                2 -> {
                    snappedX = rawX
                    snappedY = snap(rawY, target.yposMoving + (rawY - mObjectdragLastY))
                    target.setHandlePosition(2, snappedX, snappedY)
                }
                3 -> {
                    snappedX = snap(rawX, target.xposMoving + target.widthMoving + (rawX - mObjectdragLastX))
                    snappedY = rawY
                    target.setHandlePosition(3, snappedX, snappedY)
                }
                else -> { //4
                    snappedX = rawX
                    snappedY = snap(rawY, target.yposMoving + target.heightMoving + (rawY - mObjectdragLastY))
                    target.setHandlePosition(4, snappedX, snappedY)
                }
            }

            mObjectdragLastX = snappedX
            mObjectdragLastY = snappedY
            invalidate()
            return true
        }
        if (mObjectdragTouchee != null && event.action == MotionEvent.ACTION_UP) {
            mObjectdragTouchee!!.applyMovement()
            mObjectdragTouchee = null
            return true
        }
        return false
    }


    private fun snap (raw: Int, snapPoint: Int) : Int {
        if (mBoardState.showGrid) {
            val gridfac = mBoardObject.gridSize * mBoardObject.gridInterval
            val gridsnapmargin = gridfac / 4
            val distance = Math.abs(snapPoint % gridfac)
            val distanceInverted = gridfac - distance
            if (distance <= gridsnapmargin) { //snap to left/top
                if (snapPoint > 0)
                    return raw - distance
                else
                    return raw + distance
            }
            if (distanceInverted <= gridsnapmargin) { //snap to right/bottom
                if (snapPoint > 0)
                    return raw + distanceInverted
                else
                    return raw - distanceInverted
            }
        }
        return raw //don't snap
    }


    private fun asyncThread () {
        thread {
            mBoardObject.addSizeChangeListener {
                mBufferBitmap = Bitmap.createBitmap((mBoardObject.width * mDipRatio).toInt(), (mBoardObject.height * mDipRatio).toInt(), Bitmap.Config.ARGB_8888)
                Renderer.drawRenderables(mBoardObject.getRenderables(mDeserializer), Canvas(mBufferBitmap), true, false)
                postInvalidate()
            }
            mBoardObject.addChangeListener {
                Renderer.drawRenderables(mBoardObject.getRenderables(mDeserializer), Canvas(mBufferBitmap), true, false)
                postInvalidate()
            }
            mBufferBitmap = Bitmap.createBitmap((mBoardObject.width * mDipRatio).toInt(), (mBoardObject.height * mDipRatio).toInt(), Bitmap.Config.ARGB_8888)
            Renderer.drawRenderables(mBoardObject.getRenderables(mDeserializer), Canvas(mBufferBitmap), true, false)
            postInvalidate()
        }
    }
}
