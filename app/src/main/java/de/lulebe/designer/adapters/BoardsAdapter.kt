package de.lulebe.designer.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.lulebe.designer.R
import de.lulebe.designer.data.BoardMeta


class BoardsAdapter(val listener: (BoardMeta, longClick: Boolean) -> Unit) : RecyclerView.Adapter<BoardsAdapter.BoardViewHolder>() {

    private var mItems = mutableListOf<BoardMeta>()

    fun setItems (items: MutableList<BoardMeta>) {
        mItems = items
        notifyDataSetChanged()
        Log.d("ITEMS", mItems.size.toString())
    }


    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BoardViewHolder? {
        return BoardViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.rv_board, parent, false) as ViewGroup)
    }

    override fun onBindViewHolder(holder: BoardViewHolder?, position: Int) {
        holder?.tvName?.text = mItems.get(position).name
        holder?.view?.setOnClickListener {
            listener(mItems[position], false)
        }
        holder?.view?.setOnLongClickListener {
            listener(mItems[position], true)
            true
        }
    }

    class BoardViewHolder : RecyclerView.ViewHolder {
        val tvName: TextView
        val view: View
        constructor(itemView: ViewGroup) : super(itemView) {
            view = itemView
            tvName = itemView.findViewById(R.id.tv_name) as TextView
        }
    }
}