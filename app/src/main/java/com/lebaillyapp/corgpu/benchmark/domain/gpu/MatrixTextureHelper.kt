package com.lebaillyapp.corgpu.benchmark.domain.gpu


import android.graphics.Bitmap
import android.graphics.Color

object MatrixTextureHelper {

    fun toFloat16Texture(matrix: Array<FloatArray>, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGBA_F16)
        for (y in 0 until size) for (x in 0 until size) {
            val v = matrix[y][x]
            bmp.setPixel(x, y, Color.valueOf(v, 0f, 0f, 1f).toArgb())
        }
        return bmp
    }

    fun fromFloat16Texture(bmp: Bitmap, size: Int): Array<FloatArray> {
        val out = Array(size) { FloatArray(size) }
        for (y in 0 until size) for (x in 0 until size) {
            val c = Color.valueOf(bmp.getPixel(x, y))
            out[y][x] = c.red()
        }
        return out
    }
}