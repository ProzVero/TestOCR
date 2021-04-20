package com.pv.testocr

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class PictureActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    lateinit var mBitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        imageView = findViewById(R.id.img)

        mBitmap = MainActivity4.bitmap!!

        val matrix = Matrix()
        matrix.postRotate(90.0.toFloat())
        val rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.width, mBitmap.height, matrix, false)

        val sizeBitmap = Bitmap.createScaledBitmap(rotaBitmap, 600, 1000, true)
        val rectBitmap = Bitmap.createBitmap(sizeBitmap, 193, 311, 203, 74)

        imageView!!.setImageBitmap(mBitmap)
        //saveImage(MainActivity4.bitmap)
    }

    fun saveImage(myBitmap: Bitmap?): String {
        val bytes = ByteArrayOutputStream()
        myBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            Environment.getExternalStorageDirectory()
                .toString() + IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            Log.d("dirrrrrr", "" + wallpaperDirectory.mkdirs())
            wallpaperDirectory.mkdirs()
        }
        try {
            val f = File(
                wallpaperDirectory, Calendar.getInstance()
                    .timeInMillis.toString() + ".jpg"
            )
            f.createNewFile() //give read write permission
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.path),
                arrayOf("image/jpeg"),
                null
            )
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)
            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return ""
    }

    companion object {
        private const val IMAGE_DIRECTORY = "/CustomImage"
    }
}