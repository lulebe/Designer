package de.lulebe.designer.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.lulebe.designer.data.objects.*
import de.lulebe.designer.external.RuntimeTypeAdapterFactory
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import org.apache.commons.io.FileUtils
import java.io.*


class StorageManager {

    private val mDir: File
    private val mFile: File



    private var mBoardObject: BoardObject? = null

    private val mGson: Gson

    constructor (path: String) {
        mDir = File(path)
        mFile = File(path + File.separator + "board.ldes")
        val boFactory: RuntimeTypeAdapterFactory<BaseObject> = RuntimeTypeAdapterFactory.of(BaseObject::class.java, "ObjectType")
        boFactory.registerSubtype(SourceObject::class.java)
        boFactory.registerSubtype(CopyObject::class.java)
        boFactory.registerSubtype(BoardObject::class.java)
        boFactory.registerSubtype(RectObject::class.java)
        boFactory.registerSubtype(TextObject::class.java)
        boFactory.registerSubtype(ImageObject::class.java)
        mGson = GsonBuilder().registerTypeAdapterFactory(boFactory).create()
    }

    fun exists () = mFile.exists()

    fun get (ctx: Context) : BoardObject {
        if (mBoardObject != null) return mBoardObject!!
        val ins = FileInputStream(mFile)
        val inr = InputStreamReader(ins)
        mBoardObject = mGson.fromJson(inr, BoardObject::class.java)
        inr.close()
        ins.close()
        mBoardObject!!.init(ctx, null)
        return mBoardObject!!
    }

    fun save (boardObject: BoardObject) {
        mBoardObject = boardObject
        if (!mFile.exists()) {
            if (!mDir.exists())
                mDir.mkdirs()
            mFile.createNewFile()
        }
        val outs = FileOutputStream(mFile)
        val outw = OutputStreamWriter(outs)
        mGson.toJson(boardObject, BoardObject::class.java, outw)
        outw.close()
        outs.close()
    }

    fun addImageFile (file: File) {
        FileUtils.copyFile(file, File(mDir.path + File.separator  + file.name))
    }

    fun removeImageFile (name: String) {
        val f = File(mDir.path + File.separator + name)
        if (f.exists())
            f.delete()
    }

    @Throws(FileNotFoundException::class)
    fun getImageFile (name: String) : String {
        val f = File(mDir.path + File.separator  + name)
        if (!f.exists())
            throw FileNotFoundException()
        return f.path
    }

    fun close () {

    }

    fun delete () = mDir.deleteRecursively()

    fun share (act: Activity) : Intent {
        val zipPath = act.cacheDir.path + File.separator + mDir.name + ".zip"
        val zipFile = ZipFile(zipPath)
        val zipParams = ZipParameters()
        zipParams.compressionMethod = Zip4jConstants.COMP_DEFLATE
        zipParams.compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL
        zipFile.addFolder(mDir, zipParams)
        val uri = FileProvider.getUriForFile(act, "de.lulebe.designer", File(zipPath))
        return ShareCompat.IntentBuilder.from(act)
                .setType("application/zip")
                .setSubject("Designer board file")
                .setStream(uri)
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    fun getPath () = mDir.path

    companion object {
        @Throws(IllegalArgumentException::class)
        fun createWithNameInternal(name: String, ctx: Context) : StorageManager {
            val path = ctx.filesDir.path.plus(File.separator).plus(name)
            if (File(path).exists()) {
                throw IllegalArgumentException("this file exists already.")
            } else {
                val sm = StorageManager(path)
                val board = BoardObject()
                board.name = name
                board.width = 360
                board.height = 360
                sm.save(board)
                return sm
            }
        }
    }
}