package de.lulebe.designer.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import de.lulebe.designer.R
import de.lulebe.designer.data.BoardMeta
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class BoardsAdapter(val listener: (BoardMeta, longClick: Boolean) -> Unit) : RecyclerView.Adapter<BoardsAdapter.BoardViewHolder>() {

    private var mItems = mutableListOf<BoardMeta>()

    fun setItems (items: MutableList<BoardMeta>) {
        mItems = items
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder? {
        return BoardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listitem_board, parent, false) as ViewGroup)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val item = mItems[position]
        holder.tvName.text = item.name
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        sdf.timeZone = Calendar.getInstance().timeZone
        Log.d("TIMEZONE", sdf.timeZone.displayName)
        holder.tvLastedited.text = sdf.format(Date(item.lastOpened))
        val ctx = holder.ivPreview.context
        Picasso.with(ctx).load(File(ctx.filesDir.path + File.separator + item._id.toString() + File.separator + "preview.png"))
                .into(holder.ivPreview)
        holder.view.setOnClickListener {
            listener(item, false)
        }
        holder.view.setOnLongClickListener {
            listener(item, true)
            true
        }
    }

    class BoardViewHolder : RecyclerView.ViewHolder {
        val tvName: TextView
        val tvLastedited: TextView
        val ivPreview: ImageView
        val view: View
        constructor(itemView: ViewGroup) : super(itemView) {
            view = itemView
            tvName = itemView.findViewById(R.id.tv_name) as TextView
            tvLastedited = itemView.findViewById(R.id.tv_lastedited) as TextView
            ivPreview = itemView.findViewById(R.id.iv_preview) as ImageView
        }
    }
}