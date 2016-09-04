package de.lulebe.designer

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.OpenableColumns
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.util.TypedValue
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
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
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
            setResult(Activity.RESULT_OK, intent)
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
            R.id.menu_showui -> {
                if (mBoardState == null) return false
                item!!.isChecked = !item!!.isChecked
                mBoardState?.showUI = item!!.isChecked
                if (mBoardState!!.showUI)
                    mBoardState!!.boardScrollX += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64F, resources.displayMetrics)
                else
                    mBoardState!!.boardScrollX += -TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64F, resources.displayMetrics)
                return true
            }
            R.id.menu_resize -> {
                resizeDialog()
                return true
            }
            R.id.menu_export -> {
                if (mBoardObject == null) return false
                doAsync {
                    val tmpFile = File.createTempFile("designer_img", ".png", cacheDir)
                    val os = FileOutputStream(tmpFile)
                    Renderer.renderJPEG(mBoardObject!!, 2, os)
                    os.close()
                    val intent = ShareCompat.IntentBuilder.from(this@BoardActivity)
                            .setType("image/png")
                            .setStream(FileProvider.getUriForFile(this@BoardActivity, "de.lulebe.designer", tmpFile))
                            .createChooserIntent()
                    uiThread { startActivity(intent) }
                }
                return true
            }
            R.id.menu_share -> {
                if (intent.getBooleanExtra("isRoot", true) && mStorageManager != null && mBoardObject != null) {
                    doAsync {
                        mStorageManager!!.save(mBoardObject!!)
                        val intent = mStorageManager!!.share(this@BoardActivity)
                        uiThread {
                            startActivity(intent)
                        }
                    }
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
        AlertDialog.Builder(this).setTitle(R.string.board_settings).setView(dialogView)
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

    private var imageRequestCallback: ((uid: Long) -> Unit)? = null

    fun requestImage (cb: (uid: Long) -> Unit) : Boolean {
        if (imageRequestCallback != null) return false
        imageRequestCallback = cb
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_IMAGE)
        return true
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
        if (data == null || resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_CODE_GROUP -> {
                val key = data.getIntExtra("boardKey", 0)
                if (key != 0)
                    (application as Designer).boards.remove(key)
            }
            REQUEST_CODE_IMAGE -> {
                doAsync {
                    if (mStorageManager != null) {
                        val input = contentResolver.openInputStream(data.data)
                        val meta = contentResolver.query(data.data, null, null, null, null, null)
                        try {
                            if (meta != null && meta.moveToFirst()) {
                                val name = meta.getString(meta.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                val imageUID = mStorageManager!!.addImage(input, name)
                                uiThread {
                                    if (imageRequestCallback != null)
                                        imageRequestCallback!!(imageUID)
                                    imageRequestCallback = null
                                }
                            }
                        } finally {
                            meta.close()
                        }
                    }
                }
            }
        }
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
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val boardView: View
            if (sp.getBoolean("surfaceview", true))
                boardView = BoardViewAsync(this, mBoardState!!, mBoardObject!!)
            else
                boardView = BoardView(this, mBoardState!!, mBoardObject!!)
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            boardView.layoutParams = lp
            mLayout.addView(boardView)
            val origLP = mLayout.layoutParams as FrameLayout.LayoutParams
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
            mBoardState?.addListener(object: BoardState.BoardStateListener() {
                override fun onShowUI(shown: Boolean) {
                    if (shown) {
                        mLeftpane.visibility = View.VISIBLE
                        mRightpane.visibility = View.VISIBLE
                        mBottompane.visibility = View.VISIBLE
                        mLayout.layoutParams = origLP
                    } else {
                        mLeftpane.visibility = View.GONE
                        mRightpane.visibility = View.GONE
                        mBottompane.visibility = View.GONE
                        val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                        lp.topMargin = origLP.topMargin
                        mLayout.layoutParams = lp
                    }
                }
            })
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
