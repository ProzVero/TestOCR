package com.pv.testocr

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), CameraSource.PictureCallback {
    private var textView: TextView? = null
    lateinit var imageView: ImageView
    private var surfaceView: SurfaceView? = null
    private var cameraSource: CameraSource? = null
    private var textRecognizer: TextRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var stringResult: String? = null
    private var mContext: Context? = null
    lateinit var mBitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.ImageView)
        mContext = this
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission.CAMERA),
            PackageManager.PERMISSION_GRANTED
        )
        textToSpeech = TextToSpeech(this, OnInitListener { })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource!!.release()
    }

    private fun textRecognizer() {
        textRecognizer = TextRecognizer.Builder(applicationContext).build()
        cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setRequestedPreviewSize(640, 480)
            .setAutoFocusEnabled(true)
            .build()
        surfaceView = findViewById(R.id.surfaceView)
        surfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            mContext!!,
                            permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    cameraSource!!.start(surfaceView!!.holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

                cameraSource!!.takePicture(null, this@MainActivity)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource!!.stop()
            }
        })

        textRecognizer!!.setProcessor(object : Detector.Processor<TextBlock?> {
            override fun release() {}
            override fun receiveDetections(detections: Detections<TextBlock?>) {
                val sparseArray = detections.detectedItems
                val stringBuilder = StringBuilder()
                for (i in 0 until sparseArray.size()) {
                    val textBlock = sparseArray.valueAt(i)
                    if (textBlock != null && textBlock.value != null) {
                        stringBuilder.append(textBlock.value + " ")
                    }
                }
                val stringText = stringBuilder.toString()
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    stringResult = stringText
                    resultObtained()
                }
            }
        })
    }

    @SuppressLint("NewApi")
    private fun resultObtained() {
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        textView!!.text = stringResult
        textToSpeech!!.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null, null)

    }

    fun buttonStart(view: View?) {
        setContentView(R.layout.surfaceview)
        textRecognizer()
    }

    fun move(view: View) {
        startActivity(Intent(mContext, MainActivity2::class.java))
    }

    fun move2(view: View) {
        startActivity(Intent(mContext, MainActivity6::class.java))
    }

    override fun onPictureTaken(p0: ByteArray) {
        mBitmap = BitmapFactory.decodeByteArray(p0, 0, p0.size)
        imageView.setImageBitmap(mBitmap)
    }

    fun move3(view: View) {
        startActivity(Intent(mContext, MainActivity3::class.java))
    }
}