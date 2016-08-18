package de.lulebe.designer.propertyEditing

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import de.lulebe.designer.R
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.TextObject


class PropertiesEditorText(val mObject: TextObject, val mView: ViewGroup, val mBoardObject: BoardObject) : TextView.OnEditorActionListener {

    private val mTextcolorView: EditText
    private val mFontsizeView: EditText
    private val mTextView: EditText

    private var mTextViewUpdate = true
    private var mTextViewWatchFire = false

    init {
        mTextcolorView = mView.findViewById(R.id.field_object_textcolor) as EditText
        mFontsizeView = mView.findViewById(R.id.field_object_fontsize) as EditText
        mTextView = mView.findViewById(R.id.field_object_text) as EditText

        mTextcolorView.setOnEditorActionListener(this)
        mFontsizeView.setOnEditorActionListener(this)

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



    private fun updateUI () {
        mTextcolorView.setText(String.format("%06X", (0xFFFFFF.and(mObject.textColor))))
        mFontsizeView.setText(mObject.fontSize.toString())
        if (mTextViewUpdate)
            mTextView.setText(mObject.text)
    }
}