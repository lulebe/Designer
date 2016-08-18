package de.lulebe.designer.propertyEditing

import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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

    private val mColorpickerDialog = ColorPickerDialog.createColorPickerDialog(mView.context)
    private var editingFillcolor = true

    init {
        mFillcolorView = mView.findViewById(R.id.btn_object_fillcolor)
        mExtractFillcolorView = mView.findViewById(R.id.btn_object_extractfillcolor) as ImageView
        mStrokewidthView = mView.findViewById(R.id.field_object_strokewidth) as EditText
        mStrokecolorView = mView.findViewById(R.id.btn_object_strokecolor)
        mExtractStrokecolorView = mView.findViewById(R.id.btn_object_extractstrokecolor) as ImageView
        mCornerradiusView = mView.findViewById(R.id.field_object_cornerradius) as EditText


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
            val cs = mObject.extractFillcolorStyle()
            val se = StyleExtractor<ColorStyle>()
            se.createStyle(cs, mView.context) {
                mBoardObject.styles.addColorStyle(it)
                mObject.fillColorStyle = it
            }
        }
        mStrokecolorView.setOnClickListener {
            editingFillcolor = false
            mColorpickerDialog.setLastColor(mObject.strokeColor)
            mColorpickerDialog.setInitialColor(mObject.strokeColor)
            mColorpickerDialog.show()
        }
        mExtractStrokecolorView.setOnClickListener {
            val cs = mObject.extractStrokecolorStyle()
            val se = StyleExtractor<ColorStyle>()
            se.createStyle(cs, mView.context) {
                mBoardObject.styles.addColorStyle(it)
                mObject.strokeColorStyle = it
            }
        }

        mStrokewidthView.setOnEditorActionListener(this)
        mCornerradiusView.setOnEditorActionListener(this)

        mObject.addChangeListener {
            updateUI()
        }
        updateUI()
    }



    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (v == null || actionId != EditorInfo.IME_ACTION_DONE) return false
        when (v) {
            mStrokewidthView -> {
                val value = Integer.valueOf(mStrokewidthView.text.toString())
                if (value < 0 || value > 99) return false
                mObject.strokeWidth = value
                return true
            }
            mCornerradiusView -> {
                val value = Integer.valueOf(mCornerradiusView.text.toString())
                if (value < 0) return false
                mObject.cornerRadius = value
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
    }

}