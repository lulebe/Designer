package de.lulebe.designer.propertyEditing

import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.azeesoft.lib.colorpicker.ColorPickerDialog
import de.lulebe.designer.R
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.RectObject
import de.lulebe.designer.data.styles.ColorStyle


class PropertiesEditorRect(val mObject: RectObject, val mView: ViewGroup, val mBoardObject: BoardObject) : TextView.OnEditorActionListener {

    private val mFillcolorView: View
    private val mExtractFillcolorView: ImageView
    private val mStrokewidthView: EditText
    private val mStrokecolorView: View
    private val mExtractStrokecolorView: ImageView
    private val mCornerradiusView: EditText
    private val mShadowView: CheckBox
    private val mShadowBlurView: EditText
    private val mShadowXPosView: EditText
    private val mShadowYPosView: EditText

    private val mColorpickerDialog = ColorPickerDialog.createColorPickerDialog(mView.context)
    private var editingFillcolor = true

    init {
        mFillcolorView = mView.findViewById(R.id.btn_object_fillcolor)
        mExtractFillcolorView = mView.findViewById(R.id.btn_object_extractfillcolor) as ImageView
        mStrokewidthView = mView.findViewById(R.id.field_object_strokewidth) as EditText
        mStrokecolorView = mView.findViewById(R.id.btn_object_strokecolor)
        mExtractStrokecolorView = mView.findViewById(R.id.btn_object_extractstrokecolor) as ImageView
        mCornerradiusView = mView.findViewById(R.id.field_object_cornerradius) as EditText
        mShadowView = mView.findViewById(R.id.field_object_shadow) as CheckBox
        mShadowBlurView = mView.findViewById(R.id.field_object_shadowblur) as EditText
        mShadowXPosView = mView.findViewById(R.id.field_object_shadowx) as EditText
        mShadowYPosView = mView.findViewById(R.id.field_object_shadowy) as EditText


        mColorpickerDialog.hideOpacityBar()
        mColorpickerDialog.setOnColorPickedListener { colorInt, colorString ->
            if (editingFillcolor)
                mObject.fillColor = colorInt
            else
                mObject.strokeColor = colorInt
        }
        mFillcolorView.setOnClickListener {
            editingFillcolor = true
            mColorpickerDialog.setLastColor(mObject.fillColor)
            mColorpickerDialog.setInitialColor(mObject.fillColor)
            mColorpickerDialog.show()
        }
        mExtractFillcolorView.setOnClickListener {
            if (mObject.fillColorStyle != null)
                mObject.fillColorStyle = null
            else {
                val cs = mObject.extractFillcolorStyle()
                val se = StyleExtractor<ColorStyle>()
                se.createStyle(cs, mView.context) {
                    mBoardObject.styles.addColorStyle(it)
                    mObject.fillColorStyle = it
                }
            }
        }
        mStrokecolorView.setOnClickListener {
            editingFillcolor = false
            mColorpickerDialog.setLastColor(mObject.strokeColor)
            mColorpickerDialog.setInitialColor(mObject.strokeColor)
            mColorpickerDialog.show()
        }
        mExtractStrokecolorView.setOnClickListener {
            if (mObject.strokeColorStyle != null)
                mObject.strokeColorStyle = null
            else {
                val cs = mObject.extractStrokecolorStyle()
                val se = StyleExtractor<ColorStyle>()
                se.createStyle(cs, mView.context) {
                    mBoardObject.styles.addColorStyle(it)
                    mObject.strokeColorStyle = it
                }
            }
        }

        mStrokewidthView.setOnEditorActionListener(this)
        mCornerradiusView.setOnEditorActionListener(this)



        mShadowBlurView.setOnEditorActionListener(this)
        mShadowXPosView.setOnEditorActionListener(this)
        mShadowYPosView.setOnEditorActionListener(this)
        mShadowView.setOnCheckedChangeListener { btn, shadow ->
            if (shadow != (mObject.shadow != null)) {
                if (!shadow) {
                    mObject.shadow = null
                } else {
                    mObject.shadow = RectObject.ObjectShadow()
                }
            }
        }

        mObject.addChangeListener {
            updateUI()
        }
        updateUI()
    }



    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (v == null || actionId != EditorInfo.IME_ACTION_DONE) return false
        when (v) {
            mStrokewidthView -> {
                val value: Int
                try {
                    value = mStrokewidthView.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    return false
                }
                if (value < 0 || value > 99) return false
                mObject.strokeWidth = value
                return true
            }
            mCornerradiusView -> {
                val value: Int
                try {
                    value = mCornerradiusView.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    return false
                }
                if (value < 0) return false
                mObject.cornerRadius = value
                return true
            }
            mShadowBlurView -> {
                val value: Int
                try {
                    value = mShadowBlurView.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    return false
                }
                if (value < 0) return false
                mObject.shadow?.blur = value
                return true
            }
            mShadowXPosView -> {
                val value: Int
                try {
                    value = mShadowXPosView.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    return false
                }
                if (value < 0) return false
                mObject.shadow?.xpos = value
                return true
            }
            mShadowYPosView -> {
                val value: Int
                try {
                    value = mShadowYPosView.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    return false
                }
                if (value < 0) return false
                mObject.shadow?.ypos = value
                return true
            }
            else -> return false
        }
    }



    private fun updateUI () {
        mFillcolorView.background = ColorDrawable(mObject.fillColor)
        mStrokewidthView.setText(mObject.strokeWidth.toString())
        mStrokecolorView.background = ColorDrawable(mObject.strokeColor)
        mCornerradiusView.setText(mObject.cornerRadius.toString())
        if (mObject.fillColorStyle != null) {
            var dr = DrawableCompat.wrap(ContextCompat.getDrawable(mView.context, R.drawable.ic_content_save_grey600_24dp))
            dr = dr.mutate()
            dr.setTint(ContextCompat.getColor(mView.context, R.color.colorAccent))
            mExtractFillcolorView.setImageDrawable(dr)
        } else {
            val dr = ContextCompat.getDrawable(mView.context, R.drawable.ic_content_save_grey600_24dp)
            mExtractFillcolorView.setImageDrawable(dr)
        }
        if (mObject.strokeColorStyle != null) {
            var dr = DrawableCompat.wrap(ContextCompat.getDrawable(mView.context, R.drawable.ic_content_save_grey600_24dp))
            dr = dr.mutate()
            dr.setTint(ContextCompat.getColor(mView.context, R.color.colorAccent))
            mExtractStrokecolorView.setImageDrawable(dr)
        } else {
            val dr = ContextCompat.getDrawable(mView.context, R.drawable.ic_content_save_grey600_24dp)
            mExtractStrokecolorView.setImageDrawable(dr)
        }
        if (mObject.shadow != null) {
            mShadowView.isChecked = true
            mShadowXPosView.isEnabled = true
            mShadowYPosView.isEnabled = true
            mShadowBlurView.isEnabled = true
            mShadowXPosView.setText(mObject.shadow?.xpos.toString())
            mShadowYPosView.setText(mObject.shadow?.ypos.toString())
            mShadowBlurView.setText(mObject.shadow?.blur.toString())
        } else {
            mShadowView.isChecked = false
            mShadowXPosView.isEnabled = false
            mShadowYPosView.isEnabled = false
            mShadowBlurView.isEnabled = false
            mShadowXPosView.setText("")
            mShadowYPosView.setText("")
            mShadowBlurView.setText("")
        }
    }

}