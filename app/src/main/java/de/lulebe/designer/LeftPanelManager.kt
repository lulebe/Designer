package de.lulebe.designer

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import com.woxthebox.draglistview.DragListView
import de.lulebe.designer.adapters.BoardObjectsAdapter
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.ImageObject
import de.lulebe.designer.data.objects.RectObject
import de.lulebe.designer.data.objects.TextObject

/**
 * Created by LuLeBe on 19/06/16.
 */
class LeftPanelManager(val mPanel: Pane, val mBoardState: BoardState, val mBoardObject: BoardObject) : View.OnClickListener {


    private val mBtnAddRect: View
    private val mBtnAddText: View
    private val mBtnAddImage: View
    private val mBtnAddGroup: View

    private val mBtnToggleEditpan: ImageView
    private val mBtnToggleGrid: ImageView

    private val mObjectslistView: DragListView


    init {
        mBtnAddRect = mPanel.findViewById(R.id.btn_add_rect)
        mBtnAddText = mPanel.findViewById(R.id.btn_add_text)
        mBtnAddImage = mPanel.findViewById(R.id.btn_add_image)
        mBtnAddGroup = mPanel.findViewById(R.id.btn_add_group)

        mBtnToggleEditpan = mPanel.findViewById(R.id.btn_toggle_editpan) as ImageView
        mBtnToggleGrid = mPanel.findViewById(R.id.btn_toggle_grid) as ImageView


        mObjectslistView = mPanel.findViewById(R.id.list_objects) as DragListView

        mBtnAddRect.setOnClickListener(this)
        mBtnAddText.setOnClickListener(this)
        mBtnAddImage.setOnClickListener(this)
        mBtnAddGroup.setOnClickListener(this)

        mBtnToggleEditpan.setOnClickListener(this)
        mBtnToggleGrid.setOnClickListener(this)


        initObjectslist()


        mBoardState.addListener(object : BoardState.BoardStateListener() {
            override fun onShowGrid(shown: Boolean) {
                applyGridButtonState()
            }
            override fun onPanningActive (panningActive: Boolean) {
                applyEditpanButtonState()
            }
        })
        applyEditpanButtonState()
        applyGridButtonState()
    }



    override fun onClick(v: View?) {
        when (v) {
            mBtnAddRect -> {
                val obj = RectObject()
                mBoardObject.addObject(obj)
                mBoardState.selected = obj
            }
            mBtnAddText -> {
                val obj = TextObject()
                mBoardObject.addObject(obj)
                mBoardState.selected = obj
            }
            mBtnAddImage -> {
                val obj = ImageObject(mPanel.context)
                mBoardObject.addObject(obj)
                mBoardState.selected = obj
            }
            mBtnAddGroup -> {
                val obj = BoardObject()
                obj.parentBoard = mBoardObject
                obj.init(mPanel.context, )
                mBoardObject.addObject(obj)
                mBoardState.selected = obj
            }
            mBtnToggleGrid -> {
                mBoardState.showGrid = !mBoardState.showGrid
            }
            mBtnToggleEditpan -> {
                mBoardState.panningActive = !mBoardState.panningActive
            }
        }
        mPanel.expand(false)
    }


    private fun applyGridButtonState () {
        if (mBoardState.showGrid)
            mBtnToggleGrid.setImageDrawable(ResourcesCompat.getDrawable(mBtnToggleGrid.resources, R.drawable.ic_grid_white_24dp, null))
        else
            mBtnToggleGrid.setImageDrawable(ResourcesCompat.getDrawable(mBtnToggleGrid.resources, R.drawable.ic_grid_off_white_24dp, null))
    }

    private fun applyEditpanButtonState () {
        if (mBoardState.panningActive)
            mBtnToggleEditpan.setImageDrawable(ResourcesCompat.getDrawable(mBtnToggleGrid.resources, R.drawable.ic_move_resize_variant_white_24dp, null))
        else
            mBtnToggleEditpan.setImageDrawable(ResourcesCompat.getDrawable(mBtnToggleGrid.resources, R.drawable.ic_cursor_move_white_24dp, null))
    }


    private fun initObjectslist () {
        mObjectslistView.setLayoutManager(LinearLayoutManager(mObjectslistView.context))
        val divider = ResourcesCompat.getDrawable(mObjectslistView.resources, R.drawable.listdivider, null)
        mObjectslistView.recyclerView.addItemDecoration(DividerItemDecoration(divider))
        val adapter = BoardObjectsAdapter(mBoardObject, mBoardState)
        mObjectslistView.setAdapter(adapter, true)
        mObjectslistView.setCanDragHorizontally(false)
        mObjectslistView.setDragListListener(object: DragListView.DragListListener {
            override fun onItemDragging(itemPosition: Int, x: Float, y: Float) {}
            override fun onItemDragStarted(position: Int) {}
            override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
                mBoardObject.reorderedObjects()
            }
        })
    }
}