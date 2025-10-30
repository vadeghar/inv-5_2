package com.example.inv_5.ui.purchases

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
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

    private val previewView: PreviewView
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var analyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var scanner: BarcodeScanner = BarcodeScanning.getClient()
    private var lastScanned: String? = null
    private var onResult: ((String) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_barcode_scanner, this, true)
        previewView = findViewById(R.id.previewView)
        // close button wired by parent when needed
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
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
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
