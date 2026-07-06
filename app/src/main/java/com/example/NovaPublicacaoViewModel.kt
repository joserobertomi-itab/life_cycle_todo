package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RepositorioPublicacoes
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado da tela de Nova Publicação. */
data class EstadoNovaPublicacao(
    val carregando: Boolean = false,
    val erro: String? = null,
    val publicado: Boolean = false,
)

/**
 * ViewModel da Nova Publicação: comprime a imagem escolhida na galeria
 * e persiste o post no Firestore. É AndroidViewModel porque a leitura da
 * imagem (Uri) precisa do ContentResolver.
 */
class NovaPublicacaoViewModel(
    aplicacao: Application,
) : AndroidViewModel(aplicacao) {

    private val repositorio = RepositorioPublicacoes()

    private val _estado = MutableStateFlow(EstadoNovaPublicacao())
    val estado: StateFlow<EstadoNovaPublicacao> = _estado

    fun publicar(texto: String, uriImagem: Uri?) {
        if (texto.isBlank()) {
            _estado.value = _estado.value.copy(erro = "Escreva uma descrição para a publicação.")
            return
        }
        if (uriImagem == null) {
            _estado.value = _estado.value.copy(erro = "Selecione uma imagem da galeria.")
            return
        }
        viewModelScope.launch {
            _estado.value = EstadoNovaPublicacao(carregando = true)
            try {
                val imagemBase64 = withContext(Dispatchers.Default) { comprimirParaBase64(uriImagem) }
                repositorio.publicar(texto.trim(), imagemBase64)
                _estado.value = EstadoNovaPublicacao(publicado = true)
            } catch (excecao: Exception) {
                _estado.value = EstadoNovaPublicacao(
                    erro = "Não foi possível publicar: ${excecao.localizedMessage ?: "tente novamente."}",
                )
            }
        }
    }

    /**
     * Reduz a imagem para no máximo [LADO_MAXIMO] px e comprime em JPEG,
     * baixando a qualidade até o Base64 caber com folga no limite de
     * 1 MiB por documento do Firestore (motivo de usar Base64: o Cloud
     * Storage exige plano Blaze e a avaliação exige o plano Spark).
     */
    private fun comprimirParaBase64(uri: Uri): String {
        val resolver = getApplication<Application>().contentResolver

        // 1ª passada: só as dimensões, para calcular a taxa de amostragem
        // e não carregar uma foto de câmera inteira na memória.
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, limites) }

        var amostragem = 1
        while (limites.outWidth / (amostragem * 2) >= LADO_MAXIMO ||
            limites.outHeight / (amostragem * 2) >= LADO_MAXIMO
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
        } while (bytes.size > TAMANHO_MAXIMO_BYTES && qualidade > 10)

        if (bytes.size > TAMANHO_MAXIMO_BYTES) {
            throw IllegalArgumentException("A imagem é grande demais, escolha outra.")
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private companion object {
        const val LADO_MAXIMO = 720
        // Base64 aumenta ~33%; 600 KB de JPEG viram ~800 KB, abaixo do limite de 1 MiB do Firestore.
        const val TAMANHO_MAXIMO_BYTES = 600_000
    }
}
