package de.lulebe.designer.propertyEditing

import android.graphics.drawable.ColorDrawable
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.azeesoft.lib.colorpicker.ColorPickerDialog
import de.lulebe.designer.R
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.RectObject
import de.lulebe.designer.data.styles.ColorStyle


class PropertiesEditorRect(val mObject: RectObject, val mView: ViewGroup, val mBoardObject: BoardObject) : TextView.OnEditorActionListener {

    private val mFillcolorView: View
    private val mExtractfillcolorView: View
    private val mStrokewidthView: EditText
    private val mStrokecolorView: View
    private val mExtractStrokecolorView: View
    private val mCornerradiusView: EditText

    private val mColorpickerDialog = ColorPickerDialog.createColorPickerDialog(mView.context)
    private var editingFillcolor = true

    init {
        mFillcolorView = mView.findViewById(R.id.btn_object_fillcolor)
        mExtractfillcolorView = mView.findViewById(R.id.btn_object_extractfillcolor)
        mStrokewidthView = mView.findViewById(R.id.field_object_strokewidth) as EditText
        mStrokecolorView = mView.findViewById(R.id.btn_object_strokecolor)
        mExtractStrokecolorView = mView.findViewById(R.id.btn_object_extractstrokecolor)
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
        mExtractfillcolorView.setOnClickListener {
            val cs = mObject.extractFillcolorStyle()
            val se = StyleExtractor<ColorStyle>()
            se.createStyle(cs, mView.context) {
                mBoardObject.styles.colorStyles.put(it.uid, it)
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
                mBoardObject.styles.colorStyles.put(it.uid, it)
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
    }

}