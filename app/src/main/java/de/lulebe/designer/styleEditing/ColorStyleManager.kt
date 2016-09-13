package de.lulebe.designer.styleEditing

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.azeesoft.lib.colorpicker.ColorPickerDialog
import de.lulebe.designer.R
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.ImageObject
import de.lulebe.designer.data.objects.RectObject
import de.lulebe.designer.data.objects.TextObject
import de.lulebe.designer.data.styles.ColorStyle


class ColorStyleManager(val mView: ViewGroup, val mBoardObject: BoardObject, val mBoardState: BoardState) {

    private val mEmptyview: View
    private val mListview: RecyclerView

    init {
        mEmptyview = mView.findViewById(R.id.tv_no_colorstyles)
        mListview = mView.findViewById(R.id.rv_colorstyles) as RecyclerView

        mListview.layoutManager = LinearLayoutManager(mListview.context)
        mListview.adapter = ColorStyleAdapter()

        setVisibilities()

        mBoardObject.styles.addChangeListener {
            setVisibilities()
            mListview.adapter.notifyDataSetChanged()
        }
    }

    private fun setVisibilities () {
        if (mBoardObject.styles.boxStyles.size > 0) {
            mEmptyview.visibility = View.GONE
            mListview.visibility = View.VISIBLE
        } else {
            mEmptyview.visibility = View.VISIBLE
            mListview.visibility = View.GONE
        }
    }


    private fun openEditDialog (cs: ColorStyle) {
        val cpd = ColorPickerDialog.createColorPickerDialog(mView.context)
        cpd.setLastColor(cs.color)
        cpd.setInitialColor(cs.color)
        cpd.setOnColorPickedListener { colorInt, colorString ->
            try {
                Color.alpha(colorInt)
                Color.red(colorInt)
                Color.green(colorInt)
                Color.blue(colorInt)
                cs.color = colorInt
            } catch (e: IllegalArgumentException) {}
        }
        cpd.show()
    }


    private fun applyColorStyle (cs: ColorStyle) {
        when (mBoardState.selected) {
            is TextObject -> {
                (mBoardState.selected!! as TextObject).textColorStyle = cs
            }
            is ImageObject -> {
                val obj = (mBoardState.selected!! as ImageObject)
                obj.tintColorStyle = cs
                obj.tinted = true
            }
            is RectObject -> {
                AlertDialog.Builder(mView.context)
                        .setTitle(R.string.set_color_style)
                        .setMessage(R.string.apply_color_style_to)
                        .setNeutralButton(android.R.string.cancel, DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.cancel()
                        })
                        .setNegativeButton(R.string.stroke, DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()
                            (mBoardState.selected as RectObject?)?.strokeColorStyle = cs
                        })
                        .setPositiveButton(R.string.fill, DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()
                            (mBoardState.selected as RectObject?)?.fillColorStyle = cs
                        })
                        .show()
            }
        }
    }


    inner class ColorStyleAdapter : RecyclerView.Adapter<ColorStyleAdapter.StyleViewHolder>() {
        override fun onBindViewHolder(holder: StyleViewHolder, position: Int) {
            val cs = mBoardObject.styles.colorStyles.values.toList().get(position)
            holder.name.text = cs.name
            holder.color.background = ColorDrawable(cs.color)
            holder.view.setOnClickListener {
                applyColorStyle(cs)
            }
            holder.view.setOnLongClickListener {
                openEditDialog(cs)
                true
            }
            holder.delete.setOnClickListener {
                if (!mBoardObject.styleIsUsed(cs))
                    mBoardObject.styles.removeColorStyle(cs)
                else
                    Toast.makeText(mView.context, R.string.style_in_use, Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount(): Int {
            return mBoardObject.styles.colorStyles.count()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleViewHolder? {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.listitem_colorstyle, parent, false)
            return StyleViewHolder(itemView)
        }

        inner class StyleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val view: View
            val name: TextView
            val color: View
            val delete: View
            init {
                view = itemView
                name = itemView.findViewById(R.id.name) as TextView
                color = itemView.findViewById(R.id.color)
                delete = itemView.findViewById(R.id.btn_delete)
            }
        }
    }
}