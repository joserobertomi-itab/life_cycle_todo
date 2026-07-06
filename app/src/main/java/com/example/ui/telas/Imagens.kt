package com.example.ui.telas

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/** Converte uma imagem Base64 salva no Firestore em bitmap para o Compose. */
internal fun decodificarBase64(imagemBase64: String): ImageBitmap? {
    if (imagemBase64.isBlank()) return null
    return try {
        val bytes = Base64.decode(imagemBase64, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: IllegalArgumentException) {
        null
    }
}
