package com.example

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CompressorDeImagem
import com.example.data.RepositorioPublicacoes
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
                val imagemBase64 = withContext(Dispatchers.Default) {
                    CompressorDeImagem.comprimirParaBase64(
                        resolver = getApplication<Application>().contentResolver,
                        uri = uriImagem,
                        ladoMaximo = LADO_MAXIMO,
                        tamanhoMaximoBytes = TAMANHO_MAXIMO_BYTES,
                    )
                }
                repositorio.publicar(texto.trim(), imagemBase64)
                _estado.value = EstadoNovaPublicacao(publicado = true)
            } catch (excecao: Exception) {
                _estado.value = EstadoNovaPublicacao(
                    erro = "Não foi possível publicar: ${excecao.localizedMessage ?: "tente novamente."}",
                )
            }
        }
    }

    private companion object {
        const val LADO_MAXIMO = 720
        // Base64 aumenta ~33%; 600 KB de JPEG viram ~800 KB, abaixo do limite de 1 MiB do Firestore.
        const val TAMANHO_MAXIMO_BYTES = 600_000
    }
}
