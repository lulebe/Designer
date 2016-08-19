package de.lulebe.designer

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import butterknife.bindView
import de.lulebe.designer.data.BoardState
import de.lulebe.designer.data.StorageManager
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.propertyEditing.PropertyPanelManager
import de.lulebe.designer.styleEditing.StylePanelManager
import java.io.File
import java.io.FileOutputStream
import java.util.*

class BoardActivity : AppCompatActivity() {

    val REQUEST_CODE_IMAGE = 1
    val REQUEST_CODE_GROUP = 2

    private val mToolbar: Toolbar by bindView(R.id.toolbar)
    private val mLeftpane: Pane by bindView(R.id.leftpane)
    private val mRightpane: Pane by bindView(R.id.rightpane)
    private val mBottompane: Pane by bindView(R.id.bottompane)
    private val mLayout: FrameLayout by bindView(R.id.layout)

    private var mStorageManager: StorageManager? = null


    private var mPropertyPanelManager: PropertyPanelManager? = null
    private var mLeftPanelManager: LeftPanelManager? = null
    private var mStylePanelManager: StylePanelManager? = null

    private var mBoardObject: BoardObject? = null
    private var mBoardState: BoardState? = null
    private var mBoardKey = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()

        if (intent.getBooleanExtra("isRoot", true)) {
            Log.d("PATH", intent.getStringExtra("path"))
            mStorageManager = StorageManager(intent.getStringExtra("path"))
            LoadBoard(savedInstanceState).execute()
        } else {
            val key = intent.getIntExtra("boardKey", 0)
            if (key == 0)
                finish()
            else {
                mBoardObject = (application as Designer).boards[key]
                Log.d("IS NULL", (mBoardObject == null).toString())
                Log.d("BOARDS", (application as Designer).boards.size.toString())
                postLoadBoard(savedInstanceState)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (outState != null) {
            mBoardState?.saveInstanceState(outState)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onStop () {
        if (intent.getBooleanExtra("isRoot", true)) {
            SaveBoard().execute()
        }
        super.onStop()
    }

    override fun onDestroy () {
        val key = intent.getIntExtra("boardKey", 0)
        if (key != 0)
            (application as Designer).boards.remove(key)
        if (intent.getBooleanExtra("isRoot", true)) {
            CloseBoard().execute()
        }
        super.onDestroy()
    }



    private fun initUI () {
        setContentView(R.layout.activity_board)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "Board"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mLeftpane.addOpenListener { open ->
            mBoardState?.leftPanelExpanded = open
            if (open && mRightpane.isExpanded())
                mRightpane.expand(false)
        }
        mRightpane.addOpenListener { open ->
            mBoardState?.rightPanelExpanded = open
            if (open && mLeftpane.isExpanded())
                mLeftpane.expand(false)
        }
        mBottompane.addOpenListener { open ->
            mBoardState?.bottomPanelExpanded = open
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.boardobject, menu)
        if (!intent.getBooleanExtra("isRoot", true)) {
            menu?.findItem(R.id.menu_share)?.isVisible = false
            menu?.findItem(R.id.menu_export)?.isVisible = false
        }
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_resize -> {
                resizeDialog()
                return true
            }
            R.id.menu_export -> {
                if (mBoardObject == null) return false
                val tmpFile = File.createTempFile("designer_img", ".jpeg", cacheDir)
                val os = FileOutputStream(tmpFile)
                Renderer.renderJPEG(mBoardObject!!, 2, os)
                os.close()
                val intent = ShareCompat.IntentBuilder.from(this)
                        .setType("image/jped")
                        .setStream(FileProvider.getUriForFile(this, "de.lulebe.designer", tmpFile))
                        .createChooserIntent()
                startActivity(intent)
                return true
            }
            R.id.menu_share -> {
                if (intent.getBooleanExtra("isRoot", true) && mStorageManager != null && mBoardObject != null) {
                    mStorageManager!!.save(mBoardObject!!)
                    startActivity(mStorageManager!!.share(this))
                    return true
                }
                return false
            }
            else -> return false
        }
    }

    private fun resizeDialog () {
        val dialogView = layoutInflater.inflate(R.layout.dialog_board_size, null)
        val xField = dialogView.findViewById(R.id.field_board_sizex) as EditText
        val yField = dialogView.findViewById(R.id.field_board_sizey) as EditText
        val gridsizeField = dialogView.findViewById(R.id.field_board_gridsize) as EditText
        val gridintervalField = dialogView.findViewById(R.id.field_board_gridlargeinterval) as EditText
        xField.setText(mBoardObject?.width.toString())
        yField.setText(mBoardObject?.height.toString())
        gridsizeField.setText(mBoardObject?.gridSize.toString())
        gridintervalField.setText(mBoardObject?.gridInterval.toString())
        AlertDialog.Builder(this).setTitle("Board Settings").setView(dialogView)
                .setPositiveButton(android.R.string.ok, { dialogInterface, someInt ->

                    mBoardObject?.width = xField.text.toString().toInt()
                    mBoardObject?.height = yField.text.toString().toInt()
                    mBoardObject?.gridSize = gridsizeField.text.toString().toInt()
                    mBoardObject?.gridInterval = gridintervalField.text.toString().toInt()
                    dialogInterface.dismiss()
                })
                .setNegativeButton(android.R.string.cancel, { dialogInterface, someInt ->
                    dialogInterface.cancel()
                })
                .create().show()
    }

    fun requestImage () {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_IMAGE)
    }


    fun openGroup (group: BoardObject) {
        val key = Random().nextInt()
        (application as Designer).boards.put(key, group)
        val intent = Intent(this, BoardActivity::class.java)
        intent.putExtra("isRoot", false)
        intent.putExtra("boardKey", key)
        startActivityForResult(intent, REQUEST_CODE_GROUP)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null || resultCode != Activity.RESULT_OK || requestCode != REQUEST_CODE_IMAGE) return
        val input = contentResolver.openInputStream(data.data)
        mStorageManager?.addImage(input, data.data.lastPathSegment)
    }

    private inner class LoadBoard(val savedInstanceState: Bundle?) : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            if (mStorageManager == null || !mStorageManager!!.exists()) return false
            mBoardObject = mStorageManager?.get(this@BoardActivity)
            mBoardKey = Random().nextInt()
            (application as Designer).boards.put(mBoardKey, mBoardObject!!)
            return true
        }
        override fun onPostExecute(boardFound: Boolean) {
            if (!boardFound) {
                finish()
            } else {
                postLoadBoard(savedInstanceState)
            }
        }
    }

    private fun postLoadBoard (savedInstanceState: Bundle?) {
        if (mBoardObject != null) {
            supportActionBar?.title = mBoardObject!!.name
            mBoardState = BoardState.fromInstanceState(savedInstanceState, mBoardObject!!)
            val boardView = BoardView(this, mBoardState!!, mBoardObject!!)
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            boardView.layoutParams = lp
            mLayout.addView(boardView)
            val rightpane = mRightpane.findViewById(R.id.layout_properties) as ViewGroup
            mPropertyPanelManager = PropertyPanelManager(this, rightpane, mBoardObject!!, mBoardState!!)
            mLeftPanelManager = LeftPanelManager(mLeftpane, mBoardState!!, mBoardObject!!)
            mStylePanelManager = StylePanelManager(mBottompane, mBoardObject!!, mBoardState!!)
            mLeftpane.visibility = View.VISIBLE
            mRightpane.visibility = View.VISIBLE
            mBottompane.visibility = View.VISIBLE
            findViewById(R.id.loading)?.visibility = View.GONE
            mLeftpane.expand(mBoardState!!.leftPanelExpanded, false)
            mRightpane.expand(mBoardState!!.rightPanelExpanded, false)
            mBottompane.expand(mBoardState!!.bottomPanelExpanded, false)
        }
    }


    private inner class SaveBoard() : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            if (mStorageManager == null || mBoardObject == null) return null
            mStorageManager?.save(mBoardObject!!)
            return null
        }
    }

    private inner class CloseBoard() : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            mStorageManager?.close()
            return null
        }
    }
}
