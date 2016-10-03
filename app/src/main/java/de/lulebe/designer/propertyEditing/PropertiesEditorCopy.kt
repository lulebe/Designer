package de.lulebe.designer.propertyEditing

import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import de.lulebe.designer.CheatSheet
import de.lulebe.designer.R
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.objects.BaseObject
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.CopyObject


class PropertiesEditorCopy (val mObject: CopyObject, val mView: ViewGroup, val mBoardObject: BoardObject, val mBoardState: BoardState)
        : TextView.OnEditorActionListener, View.OnKeyListener, View.OnClickListener {


    private val mNameView: EditText
    private val mDeleteView: View
    private val mUnlockView: View
    private val mXPosView: EditText
    private val mYPosView: EditText
    private val mAlignhorizleftView: ImageView
    private val mAlignhorizcenterView: ImageView
    private val mAlignhorizrightView: ImageView
    private val mAlignverttopView: ImageView
    private val mAlignvertcenterView: ImageView
    private val mAlignvertbottomView: ImageView
    private val mWidthDisplayView: TextView
    private val mHeightDisplayView: TextView

    init {
        mNameView = mView.findViewById(R.id.field_object_name) as EditText
        mDeleteView = mView.findViewById(R.id.btn_object_delete)
        mUnlockView = mView.findViewById(R.id.btn_object_unlock)
        mXPosView = mView.findViewById(R.id.field_object_xpos) as EditText
        mYPosView = mView.findViewById(R.id.field_object_ypos) as EditText
        mAlignhorizleftView = mView.findViewById(R.id.btn_object_alignleft) as ImageView
        mAlignhorizcenterView = mView.findViewById(R.id.btn_object_alignhorizcenter) as ImageView
        mAlignhorizrightView = mView.findViewById(R.id.btn_object_alignright) as ImageView
        mAlignverttopView = mView.findViewById(R.id.btn_object_aligntop) as ImageView
        mAlignvertcenterView = mView.findViewById(R.id.btn_object_alignvertcenter) as ImageView
        mAlignvertbottomView = mView.findViewById(R.id.btn_object_alignbottom) as ImageView
        mWidthDisplayView = mView.findViewById(R.id.display_object_width) as TextView
        mHeightDisplayView = mView.findViewById(R.id.display_object_height) as TextView

        initCheatSheets()

        mDeleteView.setOnClickListener(this)
        mUnlockView.setOnClickListener(this)
        mAlignhorizleftView.setOnClickListener(this)
        mAlignhorizcenterView.setOnClickListener(this)
        mAlignhorizrightView.setOnClickListener(this)
        mAlignverttopView.setOnClickListener(this)
        mAlignvertcenterView.setOnClickListener(this)
        mAlignvertbottomView.setOnClickListener(this)

        mNameView.setOnEditorActionListener(this)
        mNameView.setOnKeyListener(this)
        mXPosView.setOnEditorActionListener(this)
        mXPosView.setOnKeyListener(this)
        mYPosView.setOnEditorActionListener(this)
        mYPosView.setOnKeyListener(this)

        mObject.addChangeListener {
            updateUI()
        }

        updateUI()
    }


    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent): Boolean {
        if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
            return processEditText(view as EditText)
        return false
    }


    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId != EditorInfo.IME_ACTION_DONE) return false
        return processEditText(v as EditText)
    }

    private fun processEditText (v: EditText) : Boolean {
        when (v) {
            mNameView -> {
                mObject.name = mNameView.text.toString()
                return true
            }
            mXPosView -> {
                val value: Int
                try {
                    value = mXPosView.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    return false
                }
                if (value < 0) return false
                mObject.xpos = value
                return true
            }
            mYPosView -> {
                val value: Int
                try {
                    value = mYPosView.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    return false
                }
                if (value < 0) return false
                mObject.ypos = value
                return true
            }
        }
        return false
    }


    override fun onClick(view: View?) {
        when (view) {
            mDeleteView -> {
                if (mObject.source != null)
                    mObject.source!!.copies--
                mObject.removeAllChangeListeners()
                mBoardObject.removeObject(mObject)
                mBoardState.selectedClear()
            }
            mUnlockView -> {
                mBoardState.selectedSet(mBoardObject.unlockCopy(mObject))
            }
            mAlignhorizleftView -> {
                mObject.xposOrigin = BaseObject.HorizontalOrigin.LEFT
            }
            mAlignhorizcenterView -> {
                mObject.xposOrigin = BaseObject.HorizontalOrigin.CENTER
            }
            mAlignhorizrightView -> {
                mObject.xposOrigin = BaseObject.HorizontalOrigin.RIGHT
            }
            mAlignverttopView -> {
                mObject.yposOrigin = BaseObject.VerticalOrigin.TOP
            }
            mAlignvertcenterView -> {
                mObject.yposOrigin = BaseObject.VerticalOrigin.CENTER
            }
            mAlignvertbottomView -> {
                mObject.yposOrigin = BaseObject.VerticalOrigin.BOTTOM
            }
        }
    }


    private fun updateUI () {
        mNameView.setText(mObject.name)
        mXPosView.setText(mObject.xpos.toString())
        mYPosView.setText(mObject.ypos.toString())
        setAlignmentImages()
        mWidthDisplayView.text = mObject.source?.width.toString()
        mHeightDisplayView.text = mObject.source?.height.toString()
    }


    private fun setAlignmentImages () {
        when (mObject.xposOrigin) {
            BaseObject.HorizontalOrigin.LEFT -> {
                setAlignmentImage(mAlignhorizleftView, R.drawable.ic_format_horizontal_align_left_grey600_24dp, true)
                setAlignmentImage(mAlignhorizcenterView, R.drawable.ic_format_horizontal_align_center_grey600_24dp, false)
                setAlignmentImage(mAlignhorizrightView, R.drawable.ic_format_horizontal_align_right_grey600_24dp, false)
            }
            BaseObject.HorizontalOrigin.CENTER -> {
                setAlignmentImage(mAlignhorizleftView, R.drawable.ic_format_horizontal_align_left_grey600_24dp, false)
                setAlignmentImage(mAlignhorizcenterView, R.drawable.ic_format_horizontal_align_center_grey600_24dp, true)
                setAlignmentImage(mAlignhorizrightView, R.drawable.ic_format_horizontal_align_right_grey600_24dp, false)
            }
            BaseObject.HorizontalOrigin.RIGHT -> {
                setAlignmentImage(mAlignhorizleftView, R.drawable.ic_format_horizontal_align_left_grey600_24dp, false)
                setAlignmentImage(mAlignhorizcenterView, R.drawable.ic_format_horizontal_align_center_grey600_24dp, false)
                setAlignmentImage(mAlignhorizrightView, R.drawable.ic_format_horizontal_align_right_grey600_24dp, true)
            }
        }
        when (mObject.yposOrigin) {
            BaseObject.VerticalOrigin.TOP -> {
                setAlignmentImage(mAlignverttopView, R.drawable.ic_format_vertical_align_top_grey600_24dp, true)
                setAlignmentImage(mAlignvertcenterView, R.drawable.ic_format_vertical_align_center_grey600_24dp, false)
                setAlignmentImage(mAlignvertbottomView, R.drawable.ic_format_vertical_align_bottom_grey600_24dp, false)
            }
            BaseObject.VerticalOrigin.CENTER -> {
                setAlignmentImage(mAlignverttopView, R.drawable.ic_format_vertical_align_top_grey600_24dp, false)
                setAlignmentImage(mAlignvertcenterView, R.drawable.ic_format_vertical_align_center_grey600_24dp, true)
                setAlignmentImage(mAlignvertbottomView, R.drawable.ic_format_vertical_align_bottom_grey600_24dp, false)
            }
            BaseObject.VerticalOrigin.BOTTOM -> {
                setAlignmentImage(mAlignverttopView, R.drawable.ic_format_vertical_align_top_grey600_24dp, false)
                setAlignmentImage(mAlignvertcenterView, R.drawable.ic_format_vertical_align_center_grey600_24dp, false)
                setAlignmentImage(mAlignvertbottomView, R.drawable.ic_format_vertical_align_bottom_grey600_24dp, true)
            }
        }
    }


    private fun setAlignmentImage (iv: ImageView, res: Int, checked: Boolean) {
        val dr = ContextCompat.getDrawable(mView.context, res)
        if (checked) {
            val d = DrawableCompat.wrap(dr).mutate()
            d.setTint(ContextCompat.getColor(mView.context, R.color.colorAccent))
            iv.setImageDrawable(d)
        } else
            iv.setImageDrawable(dr)
    }



    private fun initCheatSheets () {
        CheatSheet.setup(mDeleteView)
        CheatSheet.setup(mUnlockView)
    }


}