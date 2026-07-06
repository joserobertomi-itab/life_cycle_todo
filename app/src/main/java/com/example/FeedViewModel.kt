package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Publicacao
import com.example.data.RepositorioPublicacoes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estado da tela de Feed. */
data class EstadoFeed(
    val carregando: Boolean = false,
    val publicacoes: List<Publicacao> = emptyList(),
    val erro: String? = null,
)

/** ViewModel do Feed: observa a coleção "posts" em tempo real via StateFlow. */
class FeedViewModel(
    repositorio: RepositorioPublicacoes = RepositorioPublicacoes(),
) : ViewModel() {

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
}
