package de.lulebe.designer.propertyEditing

import android.view.LayoutInflater
import android.view.ViewGroup
import de.lulebe.designer.BoardActivity
import de.lulebe.designer.R
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.objects.*


class PropertyPanelManager(val mActivity: BoardActivity, val mLayout: ViewGroup, val mBoardObject: BoardObject, val mBoardState: BoardState) {

    init {
        mBoardState.addListener(object : BoardState.BoardStateListener() {
            override fun onSelectObject(obj: BaseObject?) {
                updateUI()
            }
        })
        updateUI()
    }


    private fun updateUI () {
        mLayout.removeAllViews()
        val layoutInflater = LayoutInflater.from(mLayout.context)
        when (mBoardState.selected) {
            is CopyObject -> {
                layoutInflater.inflate(R.layout.properties_copy, mLayout)
                PropertiesEditorCopy(mBoardState.selected!! as CopyObject, mLayout, mBoardObject, mBoardState)
            }
            is SourceObject -> {
                layoutInflater.inflate(R.layout.properties_object, mLayout)
                PropertiesEditorObject(mBoardState.selected!! as SourceObject, mLayout, mBoardObject, mBoardState)
                when (mBoardState.selected) {
                    is RectObject -> {
                        layoutInflater.inflate(R.layout.properties_object_rect, mLayout)
                        PropertiesEditorRect(mBoardState.selected!! as RectObject, mLayout, mBoardObject)
                    }
                    is TextObject -> {
                        layoutInflater.inflate(R.layout.properties_object_text, mLayout)
                        PropertiesEditorText(mBoardState.selected!! as TextObject, mLayout, mBoardObject, mActivity)
                    }
                    is ImageObject -> {
                        layoutInflater.inflate(R.layout.properties_object_image, mLayout)
                        PropertiesEditorImage(mBoardState.selected!! as ImageObject, mLayout, mBoardObject, mActivity)
                    }
                    is BoardObject -> {
                        layoutInflater.inflate(R.layout.properties_object_board, mLayout)
                        PropertiesEditorBoard(mBoardState.selected!! as BoardObject, mLayout, mActivity)
                    }
                }
            }
            else -> {
                layoutInflater.inflate(R.layout.properties_null, mLayout)
            }
        }
    }

}