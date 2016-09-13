package de.lulebe.designer.styleEditing

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import de.lulebe.designer.R
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.TextObject
import de.lulebe.designer.data.styles.TextStyle


class TextStyleManager(val mView: ViewGroup, val mBoardObject: BoardObject, val mBoardState: BoardState) {
    private val mEmptyview: View
    private val mListview: RecyclerView

    init {
        mEmptyview = mView.findViewById(R.id.tv_no_textstyles)
        mListview = mView.findViewById(R.id.rv_textstyles) as RecyclerView

        mListview.layoutManager = LinearLayoutManager(mListview.context)
        mListview.adapter = TextStyleAdapter()

        setVisibilities()

        mBoardObject.styles.addChangeListener {
            setVisibilities()
            mListview.adapter.notifyDataSetChanged()
        }
    }

    private fun setVisibilities () {
        if (mBoardObject.styles.textStyles.size > 0) {
            mEmptyview.visibility = View.GONE
            mListview.visibility = View.VISIBLE
        } else {
            mEmptyview.visibility = View.VISIBLE
            mListview.visibility = View.GONE
        }
    }


    private fun openEditDialog (ts: TextStyle) {

    }

    private fun applyTextStyle (ts: TextStyle) {
        if (mBoardState.selected != null && mBoardState.selected!! is TextObject) {
            (mBoardState.selected!! as TextObject).textStyle = ts
        }
    }


    inner class TextStyleAdapter : RecyclerView.Adapter<TextStyleAdapter.StyleViewHolder>() {
        override fun onBindViewHolder(holder: StyleViewHolder, position: Int) {
            val ts = mBoardObject.styles.textStyles.values.toList().get(position)
            holder.name.text = ts.name
            val alignment = when (ts.alignment) {
                Layout.Alignment.ALIGN_CENTER -> "center"
                Layout.Alignment.ALIGN_OPPOSITE -> "right"
                else -> "left"
            }
            val fontName: String
            if (ts.font == 0L || !mBoardObject.fonts.containsKey(ts.font))
                fontName = "default Font"
            else
                fontName = mBoardObject.fonts[ts.font]!!
            holder.properties.text = "${ts.fontSize.toString()}px; $alignment; $fontName"
            holder.view.setOnClickListener {
                applyTextStyle(ts)
            }
            holder.view.setOnLongClickListener {
                openEditDialog(ts)
                true
            }
            holder.delete.setOnClickListener {
                if (!mBoardObject.styleIsUsed(ts))
                    mBoardObject.styles.removeTextStyle(ts)
                else
                    Toast.makeText(mView.context, R.string.style_in_use, Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount(): Int {
            return mBoardObject.styles.textStyles.count()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleViewHolder? {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.listitem_textstyle, parent, false)
            return StyleViewHolder(itemView)
        }

        inner class StyleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val view: View
            val name: TextView
            val properties: TextView
            val delete: View
            init {
                view = itemView
                name = itemView.findViewById(R.id.name) as TextView
                properties = itemView.findViewById(R.id.properties) as TextView
                delete = itemView.findViewById(R.id.btn_delete)
            }
        }
    }
}