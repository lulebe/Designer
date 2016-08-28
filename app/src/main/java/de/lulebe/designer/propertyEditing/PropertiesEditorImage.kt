package de.lulebe.designer.propertyEditing

import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import de.lulebe.designer.R
import de.lulebe.designer.adapters.ImageChooserAdapter
import de.lulebe.designer.data.DBHelper
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



    private fun updateUI () {
    }

    private fun openImageChooserDialog () {
        val v = LayoutInflater.from(mView.context).inflate(R.layout.dialog_imagechooser, null)
        val rv = v.findViewById(R.id.list) as RecyclerView
        val rvAdapter = ImageChooserAdapter(mView.context, mBoardObject)
        val spinner = v.findViewById(R.id.category) as Spinner
        val spinnerAdapter = ArrayAdapter<String>(mView.context, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        SpinnerLoader(spinnerAdapter).execute()
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selection = (spinner.adapter.getItem(position) as String).split("/")
                rvAdapter.imageCategory = selection[1]
                if (selection[0] == "Google")
                    rvAdapter.imageSource = ImageChooserAdapter.ImageSource.GOOGLE
                else if (selection[0] == "iOS")
                    rvAdapter.imageSource = ImageChooserAdapter.ImageSource.APPLE
            }
        }
        val dialog = AlertDialog.Builder(mView.context)
                .setView(v)
                .setNegativeButton(android.R.string.cancel) {dialogInterface, i ->
                    dialogInterface.cancel()
                }
                .create()
        rvAdapter.clickListener = { path ->
            mObject.setIncludedImage(path)
            dialog.dismiss()
        }
        rv.layoutManager = GridLayoutManager(mView.context, 2)
        rv.adapter = rvAdapter
        dialog.show()
    }


    inner class SpinnerLoader(val adapter: ArrayAdapter<String>) : AsyncTask<Void, Void, List<String>>() {
        override fun doInBackground(vararg params: Void?): List<String> {
            val dbh = DBHelper(mView.context)
            val db = dbh.readableDatabase
            val c = db.rawQuery("SELECT source,dir FROM included_images GROUP BY dir ORDER BY source, dir", null)
            val list = mutableListOf<String>()
            while (c.moveToNext()) {
                list.add(c.getString(c.getColumnIndex("source")) + File.separator + c.getString(c.getColumnIndex("dir")))
            }
            c.close()
            db.close()
            dbh.close()
            return list
        }
        override fun onPostExecute(result: List<String>) {
            adapter.addAll(result)
        }
    }

}