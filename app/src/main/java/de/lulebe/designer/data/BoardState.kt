package de.lulebe.designer.data

import android.os.Bundle
import de.lulebe.designer.data.objects.BaseObject
import de.lulebe.designer.data.objects.BoardObject


class BoardState {

    open class BoardStateListener () {
        open fun onSelectObject (obj: BaseObject?) {}
        open fun onPanningActive (active: Boolean) {}
        open fun onShowGrid (shown: Boolean) {}
        open fun onShowUI (shown: Boolean) {}
        open fun onBoardScrollX (scrollX: Float) {}
        open fun onBoardScrollY (scrollY: Float) {}
        open fun onExpandLeftPanel (expanded: Boolean) {}
        open fun onExpandRightPanel (expanded: Boolean) {}
        open fun onExpandBottomPanel (expanded: Boolean) {}
    }

    //listeners
    @Transient
    private var mListeners = mutableListOf<BoardStateListener>()
    fun addListener (l: BoardStateListener) {
        mListeners.add(l)
    }
    fun removeListener (l: BoardStateListener) {
        mListeners.remove(l)
    }
    fun removeAllListeners () {
        mListeners.clear()
    }
    


    private var _selected: BaseObject? = null
    var selected: BaseObject?
        get() = _selected
        set(value) {
            _selected = value
            for (l in mListeners) {
                l.onSelectObject(value)
            }
        }


    private var _panningActive: Boolean = true
    var panningActive: Boolean
        get() = _panningActive
        set(value) {
            _panningActive = value
            for (l in mListeners) {
                l.onPanningActive(value)
            }
        }


    private var _showGrid: Boolean = true
    var showGrid: Boolean
        get() = _showGrid
        set(value) {
            _showGrid = value
            for (l in mListeners) {
                l.onShowGrid(value)
            }
        }


    private var _showUI: Boolean = true
    var showUI: Boolean
        get() = _showUI
        set(value) {
            _showUI = value
            for (l in mListeners) {
                l.onShowUI(value)
            }
        }

    private var _boardScrollX: Float = 0F
    var boardScrollX: Float
        get() = _boardScrollX
        set(value) {
            _boardScrollX = value
            for (l in mListeners) {
                l.onBoardScrollX(value)
            }
        }

    private var _boardScrollY: Float = 0F
    var boardScrollY: Float
        get() = _boardScrollY
        set(value) {
            _boardScrollY = value
            for (l in mListeners) {
                l.onBoardScrollY(value)
            }
        }

    private var _leftPanelExpanded: Boolean = false
    var leftPanelExpanded: Boolean
        get() = _leftPanelExpanded
        set(value) {
            _leftPanelExpanded = value
            for (l in mListeners) {
                l.onExpandLeftPanel(value)
            }
        }

    private var _rightPanelExpanded: Boolean = false
    var rightPanelExpanded: Boolean
        get() = _rightPanelExpanded
        set(value) {
            _rightPanelExpanded = value
            for (l in mListeners) {
                l.onExpandRightPanel(value)
            }
        }

    private var _bottomPanelExpanded: Boolean = false
    var bottomPanelExpanded: Boolean
        get() = _bottomPanelExpanded
        set(value) {
            _bottomPanelExpanded = value
            for (l in mListeners) {
                l.onExpandBottomPanel(value)
            }
        }


    fun saveInstanceState (instanceState: Bundle) {
        val b = Bundle()
        b.putBoolean("panningActive", panningActive)
        b.putBoolean("showGrid", showGrid)
        b.putBoolean("showUI", showUI)
        b.putFloat("boardScrollX", boardScrollX)
        b.putFloat("boardScrollY", boardScrollY)
        b.putBoolean("leftPanelExpanded", leftPanelExpanded)
        b.putBoolean("rightPanelExpanded", rightPanelExpanded)
        b.putBoolean("bottomPanelExpanded", bottomPanelExpanded)
        if (selected != null)
            b.putString("selectedUID", selected?.uid.toString())
        else
            b.putString("selectedUID", null)
        instanceState.putBundle("BoardState", b)
    }


    companion object {
        fun fromInstanceState (sis: Bundle?, boardObject: BoardObject) : BoardState {
            if (sis == null || !sis.containsKey("BoardState"))
                return BoardState()
            val bs = BoardState()
            val b = sis.getBundle("BoardState")
            bs.panningActive = b.getBoolean("panningActive")
            bs.showGrid = b.getBoolean("showGrid")
            bs.showUI = b.getBoolean("showUI")
            bs.boardScrollX = b.getFloat("boardScrollX")
            bs.boardScrollY = b.getFloat("boardScrollY")
            bs.leftPanelExpanded = b.getBoolean("leftPanelExpanded")
            bs.rightPanelExpanded = b.getBoolean("rightPanelExpanded")
            bs.bottomPanelExpanded = b.getBoolean("bottomPanelExpanded")
            val s = b.getString("selectedUID")
            if (s != null)
                bs.selected = boardObject.getObjectWithUID(s.toLong())
            return bs
        }
    }
}