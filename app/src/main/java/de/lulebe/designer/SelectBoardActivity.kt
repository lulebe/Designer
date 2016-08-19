package de.lulebe.designer

import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.EditText
import de.lulebe.designer.adapters.BoardsAdapter
import de.lulebe.designer.data.BoardMeta
import de.lulebe.designer.data.DBHelper
import de.lulebe.designer.data.StorageManager
import java.io.File

class SelectBoardActivity : AppCompatActivity() {

    private val REQUEST_CODE_IMPORT = 1

    private val mAdapter = BoardsAdapter {board, longClicked ->
        if (longClicked) {
            openActionsDialog(board)
        } else {
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("path", filesDir.path + File.separator + board._id.toString())
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    override fun onStart() {
        super.onStart()
        BoardsLoader().execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null || resultCode != Activity.RESULT_OK || requestCode != REQUEST_CODE_IMPORT) return
        BoardImporter(data.data).execute()
    }

    private fun initUI () {
        setContentView(R.layout.activity_select_board)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        val fabAdd = findViewById(R.id.fab_add) as FloatingActionButton
        fabAdd.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_create_board, null)
            AlertDialog.Builder(this).setTitle("Create new Board").setView(dialogView)
                    .setPositiveButton("Create", { di: DialogInterface, i: Int ->
                        val name = (dialogView.findViewById(R.id.field_board_name) as EditText).text.toString()
                        if (name.length > 0)
                            BoardCreator(name).execute()
                        di.dismiss()
                    })
                    .setNegativeButton("cancel", { di: DialogInterface, i: Int ->
                        di.cancel()
                    })
                    .create().show()
        }
        val fabImport = findViewById(R.id.fab_import) as FloatingActionButton
        fabImport.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "application/zip"
            startActivityForResult(intent, REQUEST_CODE_IMPORT)
        }
        val boardsList = findViewById(R.id.boardslist) as RecyclerView
        boardsList.layoutManager = LinearLayoutManager(this)
        boardsList.adapter = mAdapter
    }

    private fun openActionsDialog (boardMeta: BoardMeta) {
        AlertDialog.Builder(this)
                .setTitle("Edit Board")
                .setMessage("Choose an Option")
                .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.cancel()
                })
                .setNeutralButton("delete", DialogInterface.OnClickListener { dialogInterface, i ->
                    BoardDeleter(boardMeta).execute()
                })
                .setPositiveButton("duplicate", DialogInterface.OnClickListener { dialogInterface, i ->

                })
                .show()
    }

    private inner class BoardCreator(val name: String) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String? {
            try {
                val dbh = DBHelper(this@SelectBoardActivity)
                val db = dbh.writableDatabase
                val cv = ContentValues()
                cv.put("name", name)
                cv.put("lastOpened", System.currentTimeMillis()/1000L)
                val _id = db.insert("boards", null, cv)
                val sm = StorageManager.createWithNameInternal(name, filesDir.path + File.separator + _id.toString())
                db.close()
                dbh.close()
                return sm.getPath()
            } catch (e: IllegalArgumentException) {
                return null
            }
        }
        override fun onPostExecute (path: String?) {
            val intent = Intent(this@SelectBoardActivity, BoardActivity::class.java)
            intent.putExtra("path", path)
            startActivity(intent)
        }
    }

    private inner class BoardImporter (val uri: Uri) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg p0: Void?): String? {
            val dbh = DBHelper(this@SelectBoardActivity)
            val db = dbh.writableDatabase
            val c = db.rawQuery("SELECT _id FROM boards ORDER BY _id DESC LIMIT 1", null)
            val _id: Long
            if (c.count > 0) {
                c.moveToFirst()
                _id = c.getLong(c.getColumnIndex("_id")) + 1
            } else
                _id = 1L
            c.close()
            val input = contentResolver.openInputStream(uri)
            val sm = StorageManager.createFromZipInput(this@SelectBoardActivity, input, _id.toString())
            val cv = ContentValues()
            cv.put("_id", _id)
            cv.put("name", sm.get(this@SelectBoardActivity).name)
            cv.put("lastOpened", System.currentTimeMillis()/1000L)
            db.insert("boards", null, cv)
            db.close()
            dbh.close()
            return sm.getPath()
        }

        override fun onPostExecute(path: String?) {
            val intent = Intent(this@SelectBoardActivity, BoardActivity::class.java)
            intent.putExtra("path", path)
            startActivity(intent)
        }
    }


    private inner class BoardDeleter(val board: BoardMeta) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            val dbh = DBHelper(this@SelectBoardActivity)
            val db = dbh.writableDatabase
            db.delete("boards", "_id=?", Array(1, {board._id.toString()}))
            StorageManager(filesDir.path + File.separator + board._id).delete()
            db.close()
            dbh.close()
            return null
        }
        override fun onPostExecute(result: Void?) {
            BoardsLoader().execute()
        }
    }



    private inner class BoardsLoader : AsyncTask<Void, Void, MutableList<BoardMeta>>() {
        override fun doInBackground(vararg params: Void?): MutableList<BoardMeta> {
            val items = mutableListOf<BoardMeta>()
            val dbh = DBHelper(this@SelectBoardActivity)
            val db = dbh.readableDatabase
            val c = db.rawQuery("SELECT * FROM boards ORDER BY lastOpened DESC", null)
            while(c.moveToNext()) {
                val item = BoardMeta()
                item._id = c.getLong(c.getColumnIndex("_id"))
                item.name = c.getString(c.getColumnIndex("name"))
                item.lastOpened = c.getString(c.getColumnIndex("lastOpened"))
                items.add(item)
            }
            c.close()
            db.close()
            dbh.close()
            return items
        }
        override fun onPostExecute (list: MutableList<BoardMeta>) {
            mAdapter.setItems(list)
        }
    }
}
