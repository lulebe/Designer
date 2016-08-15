package de.lulebe.designer.styleEditing

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.lulebe.designer.R
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.objects.BoardObject


class BoxStyleManager(val mView: ViewGroup, val mBoardObject: BoardObject, val mBoardState: BoardState) {

    private val mListview: RecyclerView

    init {
        mListview = mView.findViewById(R.id.rv_boxstyles) as RecyclerView

        mListview.layoutManager = LinearLayoutManager(mListview.context)
        mListview.adapter = BoxStyleAdapter()
    }


    inner class BoxStyleAdapter : RecyclerView.Adapter<BoxStyleAdapter.StyleViewHolder>() {
        override fun onBindViewHolder(holder: StyleViewHolder, position: Int) {
            val bs = mBoardObject.styles.boxStyles.values.toList().get(position)
            holder.name.text = bs.name
            holder.view.setOnClickListener {
                if (mBoardState.selected != null)
                    mBoardState.selected!!.boxStyle = bs
            }
        }

        override fun getItemCount(): Int {
            return mBoardObject.styles.boxStyles.count()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleViewHolder? {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.listitem_boxstyle, parent, false)
            return StyleViewHolder(itemView)
        }

        inner class StyleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val view: View
            val name: TextView
            init {
                view = itemView
                name = itemView.findViewById(R.id.name) as TextView
            }
        }
    }
}