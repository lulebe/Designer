package de.lulebe.designer.propertyEditing

import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import de.lulebe.designer.R
import de.lulebe.designer.adapters.ImageChooserAdapter
import de.lulebe.designer.data.DBHelper
import de.lulebe.designer.data.ImageSource
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.ImageObject
import java.io.File


class PropertiesEditorImage(val mObject: ImageObject, val mView: ViewGroup, val mBoardObject: BoardObject) : TextView.OnEditorActionListener {

    private val mBtnchooseimageView: View

    init {
        mBtnchooseimageView = mView.findViewById(R.id.btn_choose_image)

        mBtnchooseimageView.setOnClickListener {
            openImageChooserDialog()
        }

        mObject.addChangeListener {
            updateUI()
        }
        updateUI()
    }



    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return false
    }



    private fun updateUI () {}

    private fun openImageChooserDialog () {
        val v = LayoutInflater.from(mView.context).inflate(R.layout.dialog_imagechooser, null)
        val rv = v.findViewById(R.id.list) as RecyclerView
        val lm = GridLayoutManager(mView.context, 2)
        val rvAdapter = ImageChooserAdapter(mView.context, mBoardObject, lm)
        val spinner = v.findViewById(R.id.category) as Spinner
        val spinnerAdapter = ArrayAdapter<String>(mView.context, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        SpinnerLoader(spinnerAdapter).execute()
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) { //user images
                    rvAdapter.imageSource = ImageSource.USER
                    rvAdapter.imageCategory = ""
                } else {
                    val selection = imageSources[position - 1]
                    rvAdapter.imageCategory = selection.second
                    rvAdapter.imageSource = selection.first
                }
            }
        }
        val dialog = AlertDialog.Builder(mView.context)
                .setView(v)
                .setNegativeButton(android.R.string.cancel) {dialogInterface, i ->
                    dialogInterface.cancel()
                }
                .create()
        rvAdapter.clickListener = { path ->
            dialog.dismiss()
            mObject.setIncludedImage(path.first, path.second)
        }
        rvAdapter.addUserImageListener = {
            dialog.dismiss()
            chooseImageFromDisk()
        }
        rv.layoutManager = lm
        rv.adapter = rvAdapter
        dialog.show()
    }

    private fun chooseImageFromDisk () {
        Toast.makeText(mView.context, "choose Image from Disk", Toast.LENGTH_SHORT).show()
    }

    private var imageSources = mutableListOf<Pair<ImageSource, String>>()

    inner class SpinnerLoader(val adapter: ArrayAdapter<String>) : AsyncTask<Void, Void, List<Pair<ImageSource, String>>>() {
        override fun doInBackground(vararg params: Void?): List<Pair<ImageSource, String>> {
            val dbh = DBHelper(mView.context)
            val db = dbh.readableDatabase
            val c = db.rawQuery("SELECT source,dir FROM included_images GROUP BY dir ORDER BY source, dir", null)
            while (c.moveToNext()) {
                val src = ImageSource.valueOf(c.getString(c.getColumnIndex("source")))
                val dir = c.getString(c.getColumnIndex("dir"))
                imageSources.add(Pair(src, dir))
            }
            c.close()
            db.close()
            dbh.close()
            return imageSources
        }
        override fun onPostExecute(result: List<Pair<ImageSource, String>>) {
            adapter.add("user images")
            adapter.addAll(result.map { it.first.name + File.separator + it.second })
        }
    }

}