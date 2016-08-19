package de.lulebe.designer.propertyEditing

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.Editable
import android.text.Layout
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import de.lulebe.designer.R
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.TextObject


class PropertiesEditorText(val mObject: TextObject, val mView: ViewGroup, val mBoardObject: BoardObject) : TextView.OnEditorActionListener {

    private val mTextcolorView: EditText
    private val mFontsizeView: EditText
    private val mAlignleftView: ImageView
    private val mAligncenterView: ImageView
    private val mAlignrightView: ImageView
    private val mTextView: EditText

    private var mTextViewUpdate = true
    private var mTextViewWatchFire = false

    init {
        mTextcolorView = mView.findViewById(R.id.field_object_textcolor) as EditText
        mFontsizeView = mView.findViewById(R.id.field_object_fontsize) as EditText
        mAlignleftView = mView.findViewById(R.id.btn_object_alignleft) as ImageView
        mAligncenterView = mView.findViewById(R.id.btn_object_aligncenter) as ImageView
        mAlignrightView = mView.findViewById(R.id.btn_object_alignright) as ImageView
        mTextView = mView.findViewById(R.id.field_object_text) as EditText

        mTextcolorView.setOnEditorActionListener(this)
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
            mTextcolorView -> {
                try {
                    mObject.textColor = Color.parseColor("#".plus(mTextcolorView.text.toString()))
                    return true
                } catch (e: IllegalArgumentException) {
                    return false
                }
            }
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
        mTextcolorView.setText(String.format("%06X", (0xFFFFFF.and(mObject.textColor))))
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
        if (mTextViewUpdate)
            mTextView.setText(mObject.text)
    }
}