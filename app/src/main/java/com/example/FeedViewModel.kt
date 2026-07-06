package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Publicacao
import com.example.data.RepositorioPublicacoes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado da tela de Feed. */
data class EstadoFeed(
    val carregando: Boolean = false,
    val publicacoes: List<Publicacao> = emptyList(),
    val erro: String? = null,
)

/** ViewModel do Feed: observa a coleção "posts" em tempo real via StateFlow. */
class FeedViewModel(
    private val repositorio: RepositorioPublicacoes = RepositorioPublicacoes(),
) : ViewModel() {

    /** Uid do usuário logado — usado para curtidas e para mostrar o excluir só ao autor. */
    val uidAtual: String?
        get() = repositorio.uidAtual

    val estado: StateFlow<EstadoFeed> = repositorio.observarPublicacoes()
        .map { publicacoes -> EstadoFeed(publicacoes = publicacoes) }
        .catch { excecao ->
            emit(EstadoFeed(erro = "Erro ao carregar o feed: ${excecao.localizedMessage ?: "tente novamente."}"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EstadoFeed(carregando = true),
        )

    /** Erro de uma ação pontual (curtir/excluir), mostrado como aviso passageiro. */
    private val _erroAcao = MutableStateFlow<String?>(null)
    val erroAcao: StateFlow<String?> = _erroAcao

    fun alternarCurtida(publicacao: Publicacao) {
        executarAcao { repositorio.alternarCurtida(publicacao) }
    }

    fun excluir(publicacao: Publicacao) {
        executarAcao { repositorio.excluir(publicacao.id) }
    }

    fun limparErroAcao() {
        _erroAcao.value = null
    }

    private fun executarAcao(acao: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                acao()
            } catch (excecao: Exception) {
                _erroAcao.value = "A ação falhou: ${excecao.localizedMessage ?: "tente novamente."}"
            }
        }
    }
}
