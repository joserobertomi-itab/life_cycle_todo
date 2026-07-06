package com.example.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Comprime imagens da galeria para Base64, respeitando o limite de 1 MiB
 * por documento do Firestore (motivo de usar Base64: o Cloud Storage exige
 * plano Blaze e a avaliação exige o plano Spark).
 */
object CompressorDeImagem {

    /**
     * Reduz a imagem para no máximo [ladoMaximo] px e comprime em JPEG,
     * baixando a qualidade até caber em [tamanhoMaximoBytes].
     */
    fun comprimirParaBase64(
        resolver: ContentResolver,
        uri: Uri,
        ladoMaximo: Int,
        tamanhoMaximoBytes: Int,
    ): String {
        // 1ª passada: só as dimensões, para calcular a taxa de amostragem
        // e não carregar uma foto de câmera inteira na memória.
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, limites) }

        var amostragem = 1
        while (limites.outWidth / (amostragem * 2) >= ladoMaximo ||
            limites.outHeight / (amostragem * 2) >= ladoMaximo
        ) {
            amostragem *= 2
        }

        val opcoes = BitmapFactory.Options().apply { inSampleSize = amostragem }
        val bitmap = resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, opcoes) }
            ?: throw IllegalArgumentException("Não foi possível ler a imagem selecionada.")

        var qualidade = 70
        var bytes: ByteArray
        do {
            val saida = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, qualidade, saida)
            bytes = saida.toByteArray()
            qualidade -= 15
        } while (bytes.size > tamanhoMaximoBytes && qualidade > 10)

        if (bytes.size > tamanhoMaximoBytes) {
            throw IllegalArgumentException("A imagem é grande demais, escolha outra.")
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
