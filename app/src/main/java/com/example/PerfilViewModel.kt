package com.example

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CompressorDeImagem
import com.example.data.Perfil
import com.example.data.RepositorioAutenticacao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado da tela de Perfil. */
data class EstadoPerfil(
    val carregando: Boolean = true,
    val salvando: Boolean = false,
    val perfil: Perfil? = null,
    val erro: String? = null,
)

/**
 * ViewModel da tela de Perfil: carrega os dados do usuário logado da coleção
 * "users" e permite editar nome e foto (extra do PDF). É AndroidViewModel
 * porque a compressão da foto precisa do ContentResolver.
 */
class PerfilViewModel(
    aplicacao: Application,
) : AndroidViewModel(aplicacao) {

    private val repositorio = RepositorioAutenticacao()

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

    /** Extra do PDF (edição de perfil): salva o novo nome e, se escolhida, a nova foto. */
    fun salvar(nome: String, uriNovaFoto: Uri?) {
        if (nome.isBlank()) {
            _estado.value = _estado.value.copy(erro = "O nome não pode ficar vazio.")
            return
        }
        viewModelScope.launch {
            _estado.value = _estado.value.copy(salvando = true, erro = null)
            try {
                val fotoBase64 = uriNovaFoto?.let { uri ->
                    withContext(Dispatchers.Default) {
                        CompressorDeImagem.comprimirParaBase64(
                            resolver = getApplication<Application>().contentResolver,
                            uri = uri,
                            ladoMaximo = LADO_MAXIMO_FOTO,
                            tamanhoMaximoBytes = TAMANHO_MAXIMO_FOTO_BYTES,
                        )
                    }
                }
                repositorio.atualizarPerfil(nome.trim(), fotoBase64)
                _estado.value = _estado.value.copy(salvando = false, perfil = repositorio.buscarPerfil())
            } catch (excecao: Exception) {
                _estado.value = _estado.value.copy(
                    salvando = false,
                    erro = "Não foi possível salvar: ${excecao.localizedMessage ?: "tente novamente."}",
                )
            }
        }
    }

    private companion object {
        // Foto de perfil é pequena: 256 px bastam e pesam pouco no documento.
        const val LADO_MAXIMO_FOTO = 256
        const val TAMANHO_MAXIMO_FOTO_BYTES = 100_000
    }
}
