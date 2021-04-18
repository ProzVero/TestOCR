package com.pv.testocr

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer

class MainActivity2 : AppCompatActivity() {
    private var cameraView: SurfaceView? = null
    private var txtView: TextView? = null
    private var cameraSource: CameraSource? = null
    lateinit var mContext : Context

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraSource!!.start(cameraView!!.holder)
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        cameraView = findViewById(R.id.surface_view)
        txtView = findViewById(R.id.txtview)
        mContext = this
        val txtRecognizer = TextRecognizer.Builder(applicationContext).build()
        if (!txtRecognizer.isOperational) {
            Log.e("Main Activity", "Detector dependencies are not yet available")
        } else {
            cameraSource = CameraSource.Builder(applicationContext, txtRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build()
            cameraView!!.getHolder().addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {

                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    cameraSource!!.stop()
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                mContext as MainActivity2,
                                arrayOf(Manifest.permission.CAMERA),
                                1
                            )
                            return
                        }
                        cameraSource!!.start(cameraView!!.holder)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            })
            txtRecognizer.setProcessor(object : Detector.Processor<TextBlock?> {
                override fun release() {}

                override fun receiveDetections(p0: Detections<TextBlock?>) {
                    val items = p0.detectedItems
                    val strBuilder = StringBuilder()
                    for (i in 0 until items.size()) {
                        val item = items.valueAt(i) as TextBlock
                        strBuilder.append(item.value)
                        strBuilder.append("/")
                        // The following Process is used to show how to use lines & elements as well
                        for (j in 0 until items.size()) {
                            val textBlock = items.valueAt(j) as TextBlock
                            strBuilder.append(textBlock.value)
                            strBuilder.append("/")
                            for (line in textBlock.components) {
                                //extract scanned text lines here
                                Log.v("lines", line.value)
                                strBuilder.append(line.value)
                                strBuilder.append("/")
                                for (element in line.components) {
                                    //extract scanned text words here
                                    Log.v("element", element.value)
                                    strBuilder.append(element.value)
                                }
                            }
                        }
                    }
                    Log.v("strBuilder.toString()", strBuilder.toString())
                    txtView!!.post { txtView!!.text = strBuilder.toString() }
                }

            })
        }
    }
}