package de.lulebe.designer.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.woxthebox.draglistview.DragItemAdapter
import de.lulebe.designer.R
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.objects.BaseObject
import de.lulebe.designer.data.objects.BoardObject


class BoardObjectsAdapter(val mBoardObject: BoardObject, val mBoardState: BoardState) : DragItemAdapter<BaseObject, BoardObjectsAdapter.ObjectViewHolder>(false) {
    init {
        setHasStableIds(true)
        itemList = mBoardObject.objects
        mBoardObject.addChangeListener {
            notifyDataSetChanged()
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewHolder {
        return ObjectViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listitem_object, parent, false) as ViewGroup)
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.textView.text = itemList[position].name
        holder.colorView.background = ColorDrawable(itemList[position].getMainColor())
        holder.obj = itemList[position]
    }

    override fun getItemId(position: Int) = itemList[position].uid

    inner class ObjectViewHolder(val mView: ViewGroup) : DragItemAdapter<BaseObject, BoardObjectsAdapter.ObjectViewHolder>.ViewHolder(mView, R.id.color) {
        val colorView = mView.findViewById(R.id.color)
        val textView = mView.findViewById(R.id.name) as TextView

        var obj: BaseObject? = null

        override fun onItemClicked(view: View?) {
            mBoardState.selectedSet(obj)
        }
    }
}