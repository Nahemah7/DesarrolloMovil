package com.example.rinconsur.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object ConvertidorImagen {

    const val LADO_AVATAR = 256

    fun desdeUri(contexto: Context, uri: Uri, lado: Int = LADO_AVATAR): Bitmap? {
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contexto.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, medidas)
        }
        var escala = 1
        while (medidas.outWidth / (escala * 2) >= 1024 || medidas.outHeight / (escala * 2) >= 1024) {
            escala *= 2
        }
        val lectura = BitmapFactory.Options().apply { inSampleSize = escala }
        val foto = contexto.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, lectura)
        } ?: return null
        return cuadrar(foto, lado)
    }

    fun cuadrar(foto: Bitmap, lado: Int = LADO_AVATAR): Bitmap {
        val ladoRecorte = minOf(foto.width, foto.height)
        val recorte = Bitmap.createBitmap(
            foto,
            (foto.width - ladoRecorte) / 2,
            (foto.height - ladoRecorte) / 2,
            ladoRecorte,
            ladoRecorte
        )
        return Bitmap.createScaledBitmap(recorte, lado, lado, true)
    }

    fun aBytes(foto: Bitmap, calidad: Int = 85): ByteArray {
        val salida = ByteArrayOutputStream()
        foto.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
        return salida.toByteArray()
    }

    fun aBitmap(bytes: ByteArray): Bitmap? =
        if (bytes.isEmpty()) null else BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
