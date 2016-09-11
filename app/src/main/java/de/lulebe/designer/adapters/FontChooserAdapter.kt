package de.lulebe.designer.adapters

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.lulebe.designer.R
import de.lulebe.designer.data.FontCache
import de.lulebe.designer.data.objects.BoardObject


class FontChooserAdapter(val ctx: Context, val mBoardObject: BoardObject, val layoutManager: GridLayoutManager) : RecyclerView.Adapter<FontChooserAdapter.ViewHolder>() {

    /*
    FIELDS
     */


    val TYPE_ADD_BTN = 1
    val TYPE_FONT_DEFAULT = 2
    val TYPE_FONT_USER = 3


    var clickListener = {fontUID: Long -> }
    var addUserFontListener = {}



    /*
    OVERRIDES
     */



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.listitem_font, null)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ADD_BTN -> {
                holder.txtView.text = "add Font"
                holder.fontView.text = "add"
                holder.click = addUserFontListener
            }
            TYPE_FONT_DEFAULT -> {
                holder.txtView.text = "default"
                holder.click = {
                    clickListener(0L)
                }
                FontCache.loadFont(0L, mBoardObject, ctx) {
                    if (position == holder.adapterPosition)
                        holder.fontView.typeface = it
                }
            }
            TYPE_FONT_USER -> {
                val key = mBoardObject.fonts.keys.toList().get(position - 2)
                Log.d("KEY", key.toString())
                holder.txtView.text = mBoardObject.fonts[key]
                Log.d("VALUE", mBoardObject.fonts[key])
                holder.click = {
                    clickListener(key)
                }
                FontCache.loadFont(key, mBoardObject, ctx) {
                    if (position == holder.adapterPosition)
                        holder.fontView.typeface = it
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mBoardObject.fonts.size + 2
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0)
            return TYPE_ADD_BTN
        if (position == 1)
            return TYPE_FONT_DEFAULT
        return TYPE_FONT_USER
    }


    /*
    VIEWHOLDER CLASSES
     */


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var click = {}
        val fontView = itemView.findViewById(R.id.fv) as TextView
        val txtView = itemView.findViewById(R.id.tv) as TextView
        init {
            itemView.setOnClickListener{
                click()
            }
        }
    }
}