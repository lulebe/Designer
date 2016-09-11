package de.lulebe.designer.propertyEditing

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.Layout
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.azeesoft.lib.colorpicker.ColorPickerDialog
import de.lulebe.designer.BoardActivity
import de.lulebe.designer.R
import de.lulebe.designer.adapters.FontChooserAdapter
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.TextObject
import de.lulebe.designer.data.styles.ColorStyle
import de.lulebe.designer.data.styles.TextStyle


class PropertiesEditorText(val mObject: TextObject, val mView: ViewGroup, val mBoardObject: BoardObject, val mActivity: BoardActivity) : TextView.OnEditorActionListener {

    private val mTextcolorView: View
    private val mExtractTextcolorView: ImageView
    private val mFontView: View
    private val mAlignleftView: ImageView
    private val mAligncenterView: ImageView
    private val mAlignrightView: ImageView
    private val mFontsizeView: EditText
    private val mExtractTextstyleView: ImageView
    private val mTextView: EditText

    private val mColorpickerDialog = ColorPickerDialog.createColorPickerDialog(mView.context)
    private var mTextViewUpdate = true
    private var mTextViewWatchFire = false

    init {
        mTextcolorView = mView.findViewById(R.id.btn_object_textcolor)
        mExtractTextcolorView = mView.findViewById(R.id.btn_object_extracttextcolor) as ImageView
        mFontView = mView.findViewById(R.id.btn_choose_font) as View
        mAlignleftView = mView.findViewById(R.id.btn_object_alignleft) as ImageView
        mAligncenterView = mView.findViewById(R.id.btn_object_aligncenter) as ImageView
        mAlignrightView = mView.findViewById(R.id.btn_object_alignright) as ImageView
        mFontsizeView = mView.findViewById(R.id.field_object_fontsize) as EditText
        mExtractTextstyleView = mView.findViewById(R.id.btn_object_extracttextstyle) as ImageView
        mTextView = mView.findViewById(R.id.field_object_text) as EditText

        mFontView.setOnClickListener {
            openFontChooserDialog()
        }

        mColorpickerDialog.hideOpacityBar()
        mColorpickerDialog.setOnColorPickedListener { colorInt, colorString ->
            try {
                Color.alpha(colorInt)
                Color.red(colorInt)
                Color.green(colorInt)
                Color.blue(colorInt)
                mObject.textColor = colorInt
            } catch (e: IllegalArgumentException) {}
        }
        mTextcolorView.setOnClickListener {
            mColorpickerDialog.setLastColor(mObject.textColor)
            mColorpickerDialog.setInitialColor(mObject.textColor)
            mColorpickerDialog.show()
        }
        mExtractTextcolorView.setOnClickListener {
            if (mObject.textColorStyle != null)
                mObject.textColorStyle = null
            else {
                val cs = mObject.extractTextColorStyle()
                val se = StyleExtractor<ColorStyle>()
                se.createStyle(cs, mView.context) {
                    mBoardObject.styles.addColorStyle(it)
                    mObject.textColorStyle = it
                }
            }
        }
        mExtractTextstyleView.setOnClickListener {
            if (mObject.textStyle != null)
                mObject.textStyle = null
            else {
                val ts = mObject.extractTextStyle()
                val se = StyleExtractor<TextStyle>()
                se.createStyle(ts, mView.context) {
                    mBoardObject.styles.addTextStyle(it)
                    mObject.textStyle = it
                }
            }
        }




        mFontsizeView.setOnEditorActionListener(this)

        mAlignleftView.setOnClickListener {
            mObject.alignment = Layout.Alignment.ALIGN_NORMAL
        }
        mAligncenterView.setOnClickListener {
            mObject.alignment = Layout.Alignment.ALIGN_CENTER
        }
        mAlignrightView.setOnClickListener {
            mObject.alignment = Layout.Alignment.ALIGN_OPPOSITE
        }

        mTextView.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (mTextViewWatchFire)
                    mObject.text = mTextView.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        mTextView.setOnFocusChangeListener { view, hasFocus ->
            mTextViewWatchFire = hasFocus
            mTextViewUpdate = !hasFocus
        }

        mObject.addChangeListener {
            updateUI()
        }
        updateUI()
    }



    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (v == null || actionId != EditorInfo.IME_ACTION_DONE) return false
        when (v) {
            mFontsizeView -> {
                val value = Integer.valueOf(mFontsizeView.text.toString())
                if (value < 1 || value > 200) return false
                mObject.fontSize = value
                return true
            }
            else -> return false
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



    private fun updateUI () {
        mTextcolorView.background = ColorDrawable(mObject.textColor)
        if (mObject.textColorStyle != null) {
            var dr = DrawableCompat.wrap(ContextCompat.getDrawable(mView.context, R.drawable.ic_content_save_grey600_24dp))
            dr = dr.mutate()
            dr.setTint(ContextCompat.getColor(mView.context, R.color.colorAccent))
            mExtractTextcolorView.setImageDrawable(dr)
        } else {
            val dr = ContextCompat.getDrawable(mView.context, R.drawable.ic_content_save_grey600_24dp)
            mExtractTextcolorView.setImageDrawable(dr)
        }
        mFontsizeView.setText(mObject.fontSize.toString())
        when (mObject.alignment) {
            Layout.Alignment.ALIGN_NORMAL -> {
                setAlignmentImage(mAlignleftView, R.drawable.ic_format_align_left_grey600_24dp, true)
                setAlignmentImage(mAligncenterView, R.drawable.ic_format_align_center_grey600_24dp, false)
                setAlignmentImage(mAlignrightView, R.drawable.ic_format_align_right_grey600_24dp, false)
            }
            Layout.Alignment.ALIGN_CENTER -> {
                setAlignmentImage(mAlignleftView, R.drawable.ic_format_align_left_grey600_24dp, false)
                setAlignmentImage(mAligncenterView, R.drawable.ic_format_align_center_grey600_24dp, true)
                setAlignmentImage(mAlignrightView, R.drawable.ic_format_align_right_grey600_24dp, false)
            }
            Layout.Alignment.ALIGN_OPPOSITE -> {
                setAlignmentImage(mAlignleftView, R.drawable.ic_format_align_left_grey600_24dp, false)
                setAlignmentImage(mAligncenterView, R.drawable.ic_format_align_center_grey600_24dp, false)
                setAlignmentImage(mAlignrightView, R.drawable.ic_format_align_right_grey600_24dp, true)
            }
        }
        if (mObject.textStyle != null) {
            var dr = DrawableCompat.wrap(ContextCompat.getDrawable(mView.context, R.drawable.ic_content_save_grey600_24dp))
            dr = dr.mutate()
            dr.setTint(ContextCompat.getColor(mView.context, R.color.colorAccent))
            mExtractTextstyleView.setImageDrawable(dr)
        } else {
            val dr = ContextCompat.getDrawable(mView.context, R.drawable.ic_content_save_grey600_24dp)
            mExtractTextstyleView.setImageDrawable(dr)
        }
        if (mTextViewUpdate)
            mTextView.setText(mObject.text)
    }


    private fun openFontChooserDialog () {
        val rv = RecyclerView(mView.context)
        val lm = GridLayoutManager(mView.context, 2)
        val rvAdapter = FontChooserAdapter(mView.context, mBoardObject, lm)
        val dialog = AlertDialog.Builder(mView.context)
                .setView(rv)
                .setNegativeButton(android.R.string.cancel) {dialogInterface, i ->
                    dialogInterface.cancel()
                }
                .create()
        rvAdapter.clickListener = { uid ->
            dialog.dismiss()
            mObject.fontUID = uid
        }
        rvAdapter.addUserFontListener = {
            dialog.dismiss()
            mActivity.requestFont { uid: Long ->
                mObject.fontUID = uid
            }
        }
        rv.layoutManager = lm
        rv.adapter = rvAdapter
        dialog.show()
    }
}