package com.emissor.mobile.ui.scanner

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    
    private val scanner = BarcodeScanning.getClient()
    private var lastScannedTime = 0L
    private val SCAN_DELAY = 2000L // 2 segundos entre scans
    
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val currentTime = System.currentTimeMillis()
                        
                        // Evitar escaneamentos muito rápidos do mesmo código
                        if (currentTime - lastScannedTime > SCAN_DELAY) {
                            barcode.rawValue?.let { value ->
                                Log.d("BarcodeAnalyzer", "Código detectado: $value")
                                onBarcodeDetected(value)
                                lastScannedTime = currentTime
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BarcodeAnalyzer", "Erro ao escanear: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
