package de.lulebe.designer

import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
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

class SelectBoardActivity : AppCompatActivity() {

    private var mDBH: DBHelper? = null
    private var mDB: SQLiteDatabase? = null
    private val mAdapter = BoardsAdapter {board, longClicked ->
        if (longClicked) {
            openActionsDialog(board)
        } else {
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("path", board.path)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_board)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)


        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
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

        val boardsList = findViewById(R.id.boardslist) as RecyclerView
        boardsList.layoutManager = LinearLayoutManager(this)
        boardsList.adapter = mAdapter
    }

    override fun onStart() {
        super.onStart()
        mDBH = DBHelper(this)
        mDB = mDBH!!.readableDatabase
        BoardsLoader().execute()
    }

    override fun onStop() {
        super.onStop()
        mDB?.close()
        mDBH?.close()
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
                if (mDB == null) return null
                val sm = StorageManager.createWithNameInternal(name, this@SelectBoardActivity)
                val stmt = mDB?.compileStatement("INSERT INTO boards (name, path, lastOpened) VALUES (?, ?, ?)")
                stmt?.bindString(1, name)
                stmt?.bindString(2, sm.getPath())
                stmt?.bindLong(3, System.currentTimeMillis()/1000L)
                stmt?.execute()
                return sm.getPath()
            } catch (e: IllegalArgumentException) {
                return null
            }
        }
        override fun onPostExecute (path: String?) {
            if (path == null) {
                Snackbar.make(findViewById(R.id.coordLayout)!!, "This file exists already!", Snackbar.LENGTH_LONG).show()
            } else {
                val intent = Intent(this@SelectBoardActivity, BoardActivity::class.java)
                intent.putExtra("path", path)
                startActivity(intent)
            }
        }
    }


    private inner class BoardDeleter(val board: BoardMeta) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            if (mDB == null) return null
            val stmt = mDB?.compileStatement("DELETE FROM boards WHERE _id=?")
            stmt?.bindLong(1, board._id)
            stmt?.execute()
            StorageManager(board.path).delete()
            return null
        }
        override fun onPostExecute(result: Void?) {
            BoardsLoader().execute()
        }
    }



    private inner class BoardsLoader : AsyncTask<Void, Void, MutableList<BoardMeta>>() {
        override fun doInBackground(vararg params: Void?): MutableList<BoardMeta> {
            val items = mutableListOf<BoardMeta>()
            if (mDB == null) return items
            val c = mDB!!.rawQuery("SELECT * FROM boards ORDER BY lastOpened DESC", null)
            while(c.moveToNext()) {
                val item = BoardMeta()
                item._id = c.getLong(c.getColumnIndex("_id"))
                item.name = c.getString(c.getColumnIndex("name"))
                item.lastOpened = c.getString(c.getColumnIndex("lastOpened"))
                item.path = c.getString(c.getColumnIndex("path"))
                items.add(item)
            }
            return items
        }
        override fun onPostExecute (list: MutableList<BoardMeta>) {
            mAdapter.setItems(list)
        }
    }
}
