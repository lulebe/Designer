package de.lulebe.designer.propertyEditing

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import de.lulebe.designer.CheatSheet
import de.lulebe.designer.R
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.CopyObject


class PropertiesEditorCopy (val mObject: CopyObject, val mView: ViewGroup, val mBoardObject: BoardObject, val mBoardState: BoardState) : TextView.OnEditorActionListener {


    private val mNameView: EditText
    private val mDeleteView: View
    private val mUnlockView: View
    private val mXPosView: EditText
    private val mYPosView: EditText
    private val mWidthDisplayView: TextView
    private val mHeightDisplayView: TextView

    init {
        mNameView = mView.findViewById(R.id.field_object_name) as EditText
        mDeleteView = mView.findViewById(R.id.btn_object_delete)
        mUnlockView = mView.findViewById(R.id.btn_object_unlock)
        mXPosView = mView.findViewById(R.id.field_object_xpos) as EditText
        mYPosView = mView.findViewById(R.id.field_object_ypos) as EditText
        mWidthDisplayView = mView.findViewById(R.id.display_object_width) as TextView
        mHeightDisplayView = mView.findViewById(R.id.display_object_height) as TextView

        initCheatSheets()

        mDeleteView.setOnClickListener {
            if (mObject.source != null)
                mObject.source!!.copies--
            mObject.removeAllChangeListeners()
            mBoardObject.removeObject(mObject)
            mBoardState.selected = null
        }

        mUnlockView.setOnClickListener {
            mBoardState.selected = mBoardObject.unlockCopy(mObject)
        }


        mNameView.setOnEditorActionListener(this)
        mXPosView.setOnEditorActionListener(this)
        mYPosView.setOnEditorActionListener(this)

        mObject.addChangeListener {
            updateUI()
        }

        updateUI()
    }


    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
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


    private fun updateUI () {
        mNameView.setText(mObject.name)
        mXPosView.setText(mObject.xpos.toString())
        mYPosView.setText(mObject.ypos.toString())
        mWidthDisplayView.text = mObject.source?.width.toString()
        mHeightDisplayView.text = mObject.source?.height.toString()
    }



    private fun initCheatSheets () {
        CheatSheet.setup(mDeleteView)
        CheatSheet.setup(mUnlockView)
    }


}