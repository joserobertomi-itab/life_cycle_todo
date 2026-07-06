package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Perfil
import com.example.data.RepositorioAutenticacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Estado da tela de Perfil. */
data class EstadoPerfil(
    val carregando: Boolean = true,
    val perfil: Perfil? = null,
    val erro: String? = null,
)

/** ViewModel da tela de Perfil: carrega os dados do usuário logado da coleção "users". */
class PerfilViewModel(
    private val repositorio: RepositorioAutenticacao = RepositorioAutenticacao(),
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoPerfil())
    val estado: StateFlow<EstadoPerfil> = _estado

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.value = EstadoPerfil(carregando = true)
            try {
                _estado.value = EstadoPerfil(carregando = false, perfil = repositorio.buscarPerfil())
            } catch (excecao: Exception) {
                _estado.value = EstadoPerfil(
                    carregando = false,
                    erro = "Não foi possível carregar o perfil: ${excecao.localizedMessage ?: "tente novamente."}",
                )
            }
        }
    }
}
