package de.lulebe.designer.adapters

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import de.lulebe.designer.R
import de.lulebe.designer.data.DBHelper
import de.lulebe.designer.data.objects.BoardObject
import java.io.File


class ImageChooserAdapter(val ctx: Context, val mBoardObject: BoardObject) : RecyclerView.Adapter<ImageChooserAdapter.ViewHolder>() {

    val TYPE_ADD_BTN = 1
    val TYPE_IMAGE = 2

    var mDBHelper: DBHelper? = null
    var mDB: SQLiteDatabase? = null
    var mCursor: Cursor? = null

    enum class ImageSource {
        USER, GOOGLE, APPLE
    }

    private var _imageSource = ImageSource.GOOGLE
    var imageSource: ImageSource
        get() = _imageSource
        set(value) {
            _imageSource = value
            updateCursor()
        }

    private var _imageCategory = ""
    var imageCategory: String
        get() = _imageCategory
        set(value) {
            _imageCategory = value
            updateCursor()
        }

    var clickListener = {path: String -> }

    private fun updateCursor () {
        if (mDB != null) {
            mCursor?.close()
            val category: String
            if (_imageCategory != "")
                category = " AND dir='$_imageCategory'"
            else
                category = ""
            when (_imageSource) {
                ImageSource.GOOGLE -> {
                        mCursor = mDB!!.rawQuery("SELECT * FROM included_images WHERE source='Google'" + category, null)
                }
                ImageSource.APPLE -> {
                    mCursor = mDB!!.rawQuery("SELECT * FROM included_images WHERE source='iOS'" + category, null)
                }
            }
        }
        notifyDataSetChanged()
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        mDBHelper = DBHelper(ctx)
        mDB = mDBHelper?.readableDatabase
        updateCursor()
    }
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        mCursor?.close()
        mCursor = null
        mDB?.close()
        mDB = null
        mDBHelper?.close()
        mDBHelper = null
    }

    override fun getItemCount(): Int {
        when (imageSource) {
            ImageSource.USER -> return mBoardObject.images.size + 1
            else -> {
                if (mCursor != null && !mCursor!!.isClosed)
                    return mCursor!!.count
                return 0
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == TYPE_ADD_BTN) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.listitem_object, parent, false)
            return AddBtnViewHolder(v)
        }
        val v = LayoutInflater.from(parent.context).inflate(R.layout.listitem_image, parent, false)
        return ImageViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_ADD_BTN) {

            return
        }
        val v = (holder as ImageViewHolder)
        if (imageSource == ImageSource.USER) {
            val key = mBoardObject.images.keys.toList().get(position)
            v.txtView.text = mBoardObject.images[key]
            Picasso.with(v.imgView.context).load(mBoardObject.images[key]).into(v.imgView)
        } else if (mCursor != null && !mCursor!!.isClosed) {
            mCursor!!.moveToPosition(position)
            val name = mCursor!!.getString(mCursor!!.getColumnIndex("file"))
            val assetPath = mCursor!!.getString(mCursor!!.getColumnIndex("source")) + File.separator +
                    mCursor!!.getString(mCursor!!.getColumnIndex("dir")) + File.separator + name
            val uri = "file:///android_asset" + File.separator + assetPath
            v.txtView.text = name
            Picasso.with(v.imgView.context).load(uri).into(v.imgView)
            holder.click = {
                clickListener(assetPath)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && imageSource == ImageSource.USER)
            return TYPE_ADD_BTN
        return TYPE_IMAGE
    }

    inner class AddBtnViewHolder(itemView: View) : ViewHolder(itemView) {}

    inner class ImageViewHolder(itemView: View) : ViewHolder(itemView) {
        var click = {}
        val imgView = itemView.findViewById(R.id.iv) as ImageView
        val txtView = itemView.findViewById(R.id.tv) as TextView
        init {
            itemView.setOnClickListener {
                click()
            }
        }
    }

    open inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}