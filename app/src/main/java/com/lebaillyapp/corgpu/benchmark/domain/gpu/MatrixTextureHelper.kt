package com.lebaillyapp.corgpu.benchmark.domain.gpu

import android.graphics.Bitmap
import android.graphics.Color

object MatrixTextureHelper {

    fun toFloat16Texture(matrix: FloatArray, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGBA_F16)
        var i = 0
        for (y in 0 until size) {
            for (x in 0 until size) {
                val v = matrix[i++]
                val color = Color.valueOf(v, 0f, 0f, 1f)
                bmp.setPixel(x, y, color.toArgb())
            }
        }
        return bmp
    }

    fun fromFloat16Texture(bmp: Bitmap, size: Int): FloatArray {
        val out = FloatArray(size * size)
        var i = 0
        for (y in 0 until size) {
            for (x in 0 until size) {
                val c = Color.valueOf(bmp.getPixel(x, y))
                out[i++] = c.red()
            }
        }
        return out
    }
}