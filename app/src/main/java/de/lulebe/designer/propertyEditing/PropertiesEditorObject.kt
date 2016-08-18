package de.lulebe.designer.propertyEditing

import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import de.lulebe.designer.R
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.CopyObject
import de.lulebe.designer.data.objects.SourceObject
import de.lulebe.designer.data.styles.BoxStyle



class PropertiesEditorObject(val mObject: SourceObject, val mView: ViewGroup, val mBoardObject: BoardObject, val mBoardState: BoardState) : TextView.OnEditorActionListener {


    private val mNameView: EditText
    private val mDeleteView: View
    private val mDuplicateView: View
    private val mXPosView: EditText
    private val mYPosView: EditText
    private val mWidthLayoutView: View
    private val mWidthView: EditText
    private val mWidthDisplayLayoutView: View
    private val mWidthDisplayView: TextView
    private val mHeightLayoutView: View
    private val mHeightView: EditText
    private val mHeightDisplayLayoutView: View
    private val mHeightDisplayView: TextView
    private val mExtractBoxstyleView: ImageView
    private val mAlphaView: EditText
    private val mShadowView: CheckBox
    private val mShadowBlurView: EditText
    private val mShadowXPosView: EditText
    private val mShadowYPosView: EditText

    init {
        mNameView = mView.findViewById(R.id.field_object_name) as EditText
        mDeleteView = mView.findViewById(R.id.btn_object_delete)
        mDuplicateView = mView.findViewById(R.id.btn_object_duplicate)
        mXPosView = mView.findViewById(R.id.field_object_xpos) as EditText
        mYPosView = mView.findViewById(R.id.field_object_ypos) as EditText
        mWidthLayoutView = mView.findViewById(R.id.field_object_width_layout)
        mWidthView = mView.findViewById(R.id.field_object_width) as EditText
        mWidthDisplayLayoutView = mView.findViewById(R.id.display_object_width_layout)
        mWidthDisplayView = mView.findViewById(R.id.display_object_width) as TextView
        mHeightLayoutView = mView.findViewById(R.id.field_object_height_layout)
        mHeightView = mView.findViewById(R.id.field_object_height) as EditText
        mHeightDisplayLayoutView = mView.findViewById(R.id.display_object_height_layout)
        mHeightDisplayView = mView.findViewById(R.id.display_object_height) as TextView
        mExtractBoxstyleView = mView.findViewById(R.id.btn_object_extractboxstyle) as ImageView
        mAlphaView = mView.findViewById(R.id.field_object_alpha) as EditText
        mShadowView = mView.findViewById(R.id.field_object_shadow) as CheckBox
        mShadowBlurView = mView.findViewById(R.id.field_object_shadowblur) as EditText
        mShadowXPosView = mView.findViewById(R.id.field_object_shadowx) as EditText
        mShadowYPosView = mView.findViewById(R.id.field_object_shadowy) as EditText

        mNameView.setOnEditorActionListener(this)
        mXPosView.setOnEditorActionListener(this)
        mYPosView.setOnEditorActionListener(this)
        mWidthView.setOnEditorActionListener(this)
        mHeightView.setOnEditorActionListener(this)
        mAlphaView.setOnEditorActionListener(this)
        mShadowBlurView.setOnEditorActionListener(this)
        mShadowXPosView.setOnEditorActionListener(this)
        mShadowYPosView.setOnEditorActionListener(this)

        mShadowView.setOnCheckedChangeListener { btn, shadow ->
            if (shadow != (mObject.shadow != null)) {
                if (!shadow) {
                    mObject.shadow = null
                } else {
                    mObject.shadow = SourceObject.ObjectShadow()
                }
            }
        }

        mDeleteView.setOnClickListener {
            try {
                mBoardObject.removeObject(mBoardState.selected!!)
            } catch (e: BoardObject.CannotDeleteCopiedObjectException) {
                Toast.makeText(mView.context, "Can't delete copied object.", Toast.LENGTH_SHORT).show()
            }
            mBoardState.selected = null
        }
        mDuplicateView.setOnClickListener {
            val newObj = CopyObject()
            newObj.sourceId = mObject.uid
            newObj.init(mView.context, mBoardObject)
            newObj.xpos = mObject.xpos + 10
            newObj.ypos = mObject.ypos + 10
            newObj.name = "copy of " + mObject.name
            mBoardObject.addObject(newObj)
            mBoardState.selected = newObj
        }

        mExtractBoxstyleView.setOnClickListener {
            if (mObject.boxStyle != null)
                mObject.boxStyle = null
            else {
                val bs = mObject.extractBoxStyle()
                StyleExtractor<BoxStyle>().createStyle(bs, mView.context) {
                    mBoardObject.styles.addBoxStyle(bs)
                    mObject.boxStyle = bs
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
            mNameView -> {
                mObject.name = mNameView.text.toString()
                return true
            }
            mXPosView -> {
                val value = mXPosView.text.toString().toInt()
                if (value < 0) return false
                mObject.xpos = value
                return true
            }
            mYPosView -> {
                val value = mYPosView.text.toString().toInt()
                if (value < 0) return false
                mObject.ypos = value
                return true
            }
            mWidthView -> {
                val value = mWidthView.text.toString().toInt()
                if (value < 0) return false
                if (!mObject.canDirectlyChangeWidth()) return false
                mObject.width = value
                return true
            }
            mHeightView -> {
                val value = mHeightView.text.toString().toInt()
                if (value < 0) return false
                if (!mObject.canDirectlyChangeHeight()) return false
                mObject.height = value
                return true
            }
            mAlphaView -> {
                val value = Integer.valueOf(mAlphaView.text.toString())
                if (value < 0 || value > 255) return false
                mObject.alpha = value
                return true
            }
            mShadowBlurView -> {
                val value = Integer.valueOf(mShadowBlurView.text.toString())
                if (value < 0) return false
                mObject.shadow?.blur = value
                return true
            }
            mShadowXPosView -> {
                val value = Integer.valueOf(mShadowXPosView.text.toString())
                if (value < 0) return false
                mObject.shadow?.xpos = value
                return true
            }
            mShadowYPosView -> {
                val value = Integer.valueOf(mShadowYPosView.text.toString())
                if (value < 0) return false
                mObject.shadow?.ypos = value
                return true
            }
            else -> return false
        }
    }



    private fun updateUI () {
        mNameView.setText(mObject.name)
        mXPosView.setText(mObject.xpos.toString())
        mYPosView.setText(mObject.ypos.toString())
        if (mObject.canDirectlyChangeWidth()) {
            mWidthLayoutView.visibility = View.VISIBLE
            mWidthDisplayLayoutView.visibility = View.GONE
            mWidthView.setText(mObject.width.toString())
        } else {
            mWidthLayoutView.visibility = View.GONE
            mWidthDisplayLayoutView.visibility = View.VISIBLE
            mWidthDisplayView.text = mObject.width.toString()
        }
        if (mObject.canDirectlyChangeHeight()) {
            mHeightLayoutView.visibility = View.VISIBLE
            mHeightDisplayLayoutView.visibility = View.GONE
            mHeightView.setText(mObject.height.toString())
        } else {
            mHeightLayoutView.visibility = View.GONE
            mHeightDisplayLayoutView.visibility = View.VISIBLE
            mHeightDisplayView.text = mObject.height.toString()
        }
        if (mObject.canAcceptBoxStyle()) {
            mExtractBoxstyleView.visibility = View.VISIBLE
            if (mObject.boxStyle != null) {
                var dr = DrawableCompat.wrap(ContextCompat.getDrawable(mView.context, R.drawable.ic_content_save_grey600_24dp))
                dr = dr.mutate()
                dr.setTint(ContextCompat.getColor(mView.context, R.color.colorAccent))
                mExtractBoxstyleView.setImageDrawable(dr)
            } else {
                val dr = ContextCompat.getDrawable(mView.context, R.drawable.ic_content_save_grey600_24dp)
                mExtractBoxstyleView.setImageDrawable(dr)
            }
        } else
            mExtractBoxstyleView.visibility = View.GONE
        mAlphaView.setText(mObject.alpha.toString())
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