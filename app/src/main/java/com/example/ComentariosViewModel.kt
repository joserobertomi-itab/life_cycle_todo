package com.example

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Comentario
import com.example.data.RepositorioPublicacoes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado da tela de Comentários. */
data class EstadoComentarios(
    val carregando: Boolean = false,
    val comentarios: List<Comentario> = emptyList(),
    val erro: String? = null,
)

/**
 * ViewModel dos Comentários (extra do PDF): observa a subcoleção "comments"
 * da publicação em tempo real e adiciona novos comentários.
 * O id da publicação chega pela rota de navegação (SavedStateHandle).
 */
class ComentariosViewModel(
    estadoSalvo: SavedStateHandle,
) : ViewModel() {

    private val repositorio = RepositorioPublicacoes()
    private val idPublicacao: String = checkNotNull(estadoSalvo["idPublicacao"])

    val estado: StateFlow<EstadoComentarios> = repositorio.observarComentarios(idPublicacao)
        .map { comentarios -> EstadoComentarios(comentarios = comentarios) }
        .catch { excecao ->
            emit(EstadoComentarios(erro = "Erro ao carregar comentários: ${excecao.localizedMessage ?: "tente novamente."}"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EstadoComentarios(carregando = true),
        )

    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando

    private val _erroEnvio = MutableStateFlow<String?>(null)
    val erroEnvio: StateFlow<String?> = _erroEnvio

    fun comentar(texto: String) {
        if (texto.isBlank()) return
        viewModelScope.launch {
            _enviando.value = true
            _erroEnvio.value = null
            try {
                repositorio.comentar(idPublicacao, texto.trim())
            } catch (excecao: Exception) {
                _erroEnvio.value = "Não foi possível comentar: ${excecao.localizedMessage ?: "tente novamente."}"
            } finally {
                _enviando.value = false
            }
        }
    }
}
