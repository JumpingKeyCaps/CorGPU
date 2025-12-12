package com.lebaillyapp.corgpu.benchmark.domain.gpu

import android.graphics.Bitmap
import android.graphics.Color

/**
 * Helper pour encoder/décoder des matrices en textures Float16.
 * Utilise l'API simple setPixel/getPixel pour garder le code lisible.
 *
 * Note: setPixel/getPixel font des appels JNI par pixel (coûteux), mais c'est
 * un choix délibéré pour privilégier la simplicité dans ce projet éducatif.
 * Une optimisation avec ByteBuffer donnerait 10-50x de speedup mais ajouterait
 * ~100 lignes de complexité pour l'encodage float16 manuel.
 */
object MatrixTextureHelper {

    /**
     * Convertit une matrice en texture RGBA_F16.
     * Encode chaque valeur dans le canal rouge (float16).
     */
    fun toFloat16Texture(matrix: Array<FloatArray>, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGBA_F16)
        for (y in 0 until size) {
            for (x in 0 until size) {
                val v = matrix[y][x]
                bmp.setPixel(x, y, Color.valueOf(v, 0f, 0f, 1f).toArgb())
            }
        }
        return bmp
    }

    /**
     * Décode une texture RGBA_F16 en matrice.
     * Lit la valeur depuis le canal rouge.
     */
    fun fromFloat16Texture(bmp: Bitmap, size: Int): Array<FloatArray> {
        val out = Array(size) { FloatArray(size) }
        for (y in 0 until size) {
            for (x in 0 until size) {
                val c = Color.valueOf(bmp.getPixel(x, y))
                out[y][x] = c.red()
            }
        }
        return out
    }
}