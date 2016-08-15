package de.lulebe.designer.styleEditing

import android.view.ViewGroup
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.objects.BoardObject


class StylePanelManager(val mView: ViewGroup, val mBoardObject: BoardObject, mBoardState: BoardState) {
    init {
        BoxStyleManager(mView, mBoardObject, mBoardState)
    }
}