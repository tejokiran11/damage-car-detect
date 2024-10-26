package com.example.damagedetectioncar

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.example.damagedetectioncar.ml.Car

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2
    private val PERMISSION_REQUEST_CODE = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        resultTextView = findViewById(R.id.resultTextView)

        val captureButton: Button = findViewById(R.id.captureButton)
        val galleryButton: Button = findViewById(R.id.galleryButton)

        captureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
            }
        }

        galleryButton.setOnClickListener {
            openGallery()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val photo: Bitmap = data?.extras?.get("data") as Bitmap
                    imageView.setImageBitmap(photo)
                    resultTextView.text = "Image Captured"
                    processImage(photo)
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri: Uri? = data?.data
                    val inputStream: InputStream? = selectedImageUri?.let { contentResolver.openInputStream(it) }
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                    resultTextView.text = "Image Imported"
                    processImage(bitmap)
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        resultTextView.text = "Processing Image..."

        // Resize and normalize the bitmap as per your model requirements (e.g., 150x150).
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // Load the TFLite model.
        val model = Car.newInstance(this)

        // Create input for the model.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 150, 150, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        // Run the model and get the result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        // Interpret the result (based on your dataset labels).
        val predictedClass = getPredictedClass(outputFeature0.floatArray)

        // Display result.
        resultTextView.text = "Predicted: $predictedClass"

        // Release model resources.
        model.close()
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(150 * 150 * 3 * 4) // 4 bytes per float.
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(150 * 150)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in intValues) {
            byteBuffer.putFloat(((pixelValue shr 16 and 0xFF) / 255.0f)) // Red.
            byteBuffer.putFloat(((pixelValue shr 8 and 0xFF) / 255.0f))  // Green.
            byteBuffer.putFloat(((pixelValue and 0xFF) / 255.0f))       // Blue.
        }
        return byteBuffer
    }

    private fun getPredictedClass(predictions: FloatArray): String {
        val classes = arrayOf("Crack", "Dent", "Glass Shatter", "Lamp Broken", "Scratch", "Tire Flat")
        val maxIndex = predictions.indices.maxByOrNull { predictions[it] } ?: -1
        return if (maxIndex >= 0) classes[maxIndex] else "Unknown"
    }
}