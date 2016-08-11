package de.lulebe.designer.propertyEditing

import android.os.AsyncTask
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import de.lulebe.designer.R
import de.lulebe.designer.data.DBHelper
import de.lulebe.designer.data.objects.ImageObject
import java.io.File


class PropertiesEditorImage(val mObject: ImageObject, val mView: ViewGroup) : TextView.OnEditorActionListener {

    private val mInclimagesView : Spinner

    init {
        mInclimagesView = mView.findViewById(R.id.field_object_inclimg) as Spinner

        initSpinners()

        mObject.addChangeListener {
            updateUI()
        }
        updateUI()
    }



    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return false
    }



    private fun updateUI () {
        if (mInclimagesView.adapter != null) {
            val adapter = mInclimagesView.adapter as ArrayAdapter<String>
            if (mObject.included) {
                mInclimagesView.setSelection(adapter.getPosition(mObject.src), false)
            }
        }
    }


    private fun initSpinners () {
        val adapter = ArrayAdapter<String>(mInclimagesView.context, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mInclimagesView.adapter = adapter
        SpinnerLoader().execute()
    }


    inner class SpinnerLoader : AsyncTask<Void, Void, List<String>>() {
        override fun doInBackground(vararg params: Void?): List<String> {
            val dbh = DBHelper(mInclimagesView.context)
            val db = dbh.readableDatabase
            val c = db.rawQuery("SELECT * FROM included_images", null)
            val list = mutableListOf<String>()
            while (c.moveToNext()) {
                list.add(c.getString(c.getColumnIndex("dir")) + File.separator + c.getString(c.getColumnIndex("file")))
            }
            c.close()
            db.close()
            dbh.close()
            return list
        }
        override fun onPostExecute(result: List<String>) {
            val adapter = mInclimagesView.adapter as ArrayAdapter<String>
            adapter.addAll(result)
            mInclimagesView.setSelection(adapter.getPosition(mObject.src), false)
            mInclimagesView.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    mObject.setIncludedImage(adapter.getItem(position))
                }
            }
        }
    }

}