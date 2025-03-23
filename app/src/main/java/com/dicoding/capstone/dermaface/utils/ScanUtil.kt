package com.dicoding.capstone.dermaface.utils

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ScanUtil {
    private const val INPUT_SIZE = 224 // MobileNetV1 default input size is 224x224
    private const val PIXEL_SIZE = 3 // RGB channels
    private const val QUANT_FACTOR = 255.0f // Normalization factor

    // Convert bitmap to the ByteBuffer format required by the TFLite model
    fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

        // Allocate a ByteBuffer (4 bytes per float * 224 * 224 * 3 channels)
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(pixels, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        // For MobileNetV1, we normalize the pixels to [-1, 1]
        for (pixelValue in pixels) {
            // Extract RGB channels
            val r = (pixelValue shr 16 and 0xFF) / QUANT_FACTOR
            val g = (pixelValue shr 8 and 0xFF) / QUANT_FACTOR
            val b = (pixelValue and 0xFF) / QUANT_FACTOR

            // MobileNetV1 typically expects RGB values normalized in the range [-1, 1]
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }

    // Optional: Add preprocessing specific to your chili disease detection
    fun preprocessImage(bitmap: Bitmap): Bitmap {
        // Apply any preprocessing needed for your specific model
        // Examples include: contrast enhancement, color space conversion, etc.
        return bitmap
    }
}