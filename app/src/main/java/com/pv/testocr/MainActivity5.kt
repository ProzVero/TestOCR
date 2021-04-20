package com.pv.testocr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class MainActivity5 : AppCompatActivity() {
    companion object{
        private var imageCapture: ImageCapture? = null
        private lateinit var outputDirectory: File
        private lateinit var cameraExecutor: ExecutorService
        lateinit var viewFinder : PreviewView
        lateinit var imageView: ImageView
        lateinit var imageView2: ImageView
        lateinit var button: Button
        lateinit var textView: TextView

        private const val TAG = "CameraXGFG"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main5)

        viewFinder = findViewById(R.id.viewFinder)
        imageView = findViewById(R.id.iv_capture)
        imageView2 = findViewById(R.id.iv_capture2)
        button = findViewById(R.id.camera_capture_button)
        textView = findViewById(R.id.textView)

        // hide the action bar
        supportActionBar?.hide()

        // Check camera permissions if all permission granted
        // start camera else ask for the permission
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // set on click listener for the button of capture photo
        // it calls a method which is implemented below
        button.setOnClickListener {
            takePhoto()
        }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun takePhoto() {
        // Get a stable reference of the
        // modifiable image capture use case

        val txtRecognizer = TextRecognizer.Builder(applicationContext).build()
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener,
        // which is triggered after photo has
        // been taken

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {

                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)

                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, savedUri)

                    val matrix = Matrix()
                    matrix.postRotate(90.0.toFloat())
                    val rotaBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)

                    val sizeBitmap = Bitmap.createScaledBitmap(rotaBitmap, 600, 900, true)
                    val rectBitmap = Bitmap.createBitmap(sizeBitmap, 187, 337, 218, 80)

                    if (!txtRecognizer.isOperational){
                        textView.text = "Detector dependencies are not yet available"
                    }else{
                        val frame: Frame = Frame.Builder().setBitmap(rectBitmap).build()
                        val items = txtRecognizer.detect(frame)

                        val strBuilder = StringBuilder()
                            // The following Process is used to show how to use lines & elements as well
                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i) as TextBlock
                            strBuilder.append(item.value)
                            strBuilder.append("/")
                            for (line in item.components) {
                                //extract scanned text lines here
                                Log.v("lines", line.value)
                                for (element in line.components) {
                                    //extract scanned text words here
                                    Log.v("element", element.value)
                                }
                            }
                        }

                        textView.text = strBuilder.toString()
                    }

                    // set the saved uri to the image view
                    imageView2.visibility = View.VISIBLE
                    imageView2.setImageBitmap(rectBitmap)

                    imageView.visibility = View.VISIBLE
                    imageView.setImageURI(savedUri)

                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {

            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // creates a folder inside internal storage
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    // checks the camera permission
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // If all permissions granted , then start Camera
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // If permissions are not granted,
                // present a toast to notify the user that
                // the permissions were not granted.
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}