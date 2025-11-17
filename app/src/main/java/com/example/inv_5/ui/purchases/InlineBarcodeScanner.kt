package com.example.inv_5.ui.purchases

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.inv_5.R
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class InlineBarcodeScanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val PREFS_NAME = "BarcodeScanner"
        private const val KEY_CAMERA_LENS_FACING = "camera_lens_facing"
        private const val LENS_FACING_BACK = CameraSelector.LENS_FACING_BACK
        private const val LENS_FACING_FRONT = CameraSelector.LENS_FACING_FRONT
    }

    private val previewView: PreviewView
    private val switchCameraButton: ImageButton
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var analyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var scanner: BarcodeScanner = BarcodeScanning.getClient()
    private var lastScanned: String? = null
    private var onResult: ((String) -> Unit)? = null
    private var currentLensFacing: Int = LENS_FACING_BACK

    init {
        LayoutInflater.from(context).inflate(R.layout.view_barcode_scanner, this, true)
        previewView = findViewById(R.id.previewView)
        switchCameraButton = findViewById(R.id.switchCameraButton)
        
        // Load saved camera preference
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentLensFacing = prefs.getInt(KEY_CAMERA_LENS_FACING, LENS_FACING_BACK)
        
        // Setup camera switch button
        switchCameraButton.setOnClickListener {
            switchCamera()
        }
    }

    fun startScanning(onResult: (String) -> Unit) {
        this.onResult = onResult
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopScanning() {
        try {
            cameraProvider?.unbindAll()
            analyzer?.clearAnalyzer()
            analyzer = null
            onResult = null
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun switchCamera() {
        // Toggle between front and back camera
        currentLensFacing = if (currentLensFacing == LENS_FACING_BACK) {
            LENS_FACING_FRONT
        } else {
            LENS_FACING_BACK
        }
        
        // Save preference
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CAMERA_LENS_FACING, currentLensFacing)
            .apply()
        
        // Rebind camera with new lens facing
        bindCameraUseCases()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

        analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analyzer?.setAnalyzer(cameraExecutor) { imageProxy: ImageProxy ->
            processImageProxy(imageProxy)
        }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(currentLensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                (context as androidx.fragment.app.FragmentActivity),
                cameraSelector,
                preview,
                analyzer
            )
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        // take first barcode value
                        val raw = barcodes[0].rawValue
                        if (!raw.isNullOrEmpty() && raw != lastScanned) {
                            lastScanned = raw
                            // post result on main thread via onResult
                            post {
                                onResult?.invoke(raw)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    // ignore
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopScanning()
        cameraExecutor.shutdown()
    }
}
