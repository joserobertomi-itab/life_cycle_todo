package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RepositorioAutenticacao
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Estado da UI das telas de login e cadastro. */
data class EstadoAutenticacao(
    val carregando: Boolean = false,
    val erro: String? = null,
    val logado: Boolean = false,
)

/**
 * ViewModel (MVVM) do fluxo de autenticação: login, cadastro, sessão e logout.
 * Expõe o estado via StateFlow e executa as operações do Firebase em coroutines.
 */
class AutenticacaoViewModel(
    private val repositorio: RepositorioAutenticacao = RepositorioAutenticacao(),
) : ViewModel() {

    // Sessão: se o Firebase ainda tem usuário, o app abre direto logado.
    private val _estado = MutableStateFlow(EstadoAutenticacao(logado = repositorio.estaLogado))
    val estado: StateFlow<EstadoAutenticacao> = _estado

    fun entrar(email: String, senha: String) {
        if (email.isBlank() || senha.isBlank()) {
            _estado.value = _estado.value.copy(erro = "Preencha e-mail e senha.")
            return
        }
        executar { repositorio.entrar(email.trim(), senha) }
    }

    fun cadastrar(nome: String, email: String, senha: String) {
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            _estado.value = _estado.value.copy(erro = "Preencha nome, e-mail e senha.")
            return
        }
        executar { repositorio.cadastrar(nome.trim(), email.trim(), senha) }
    }

    fun sair() {
        repositorio.sair()
        _estado.value = EstadoAutenticacao(logado = false)
    }

    fun limparErro() {
        _estado.value = _estado.value.copy(erro = null)
    }

    private fun executar(operacao: suspend () -> Unit) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(carregando = true, erro = null)
            try {
                operacao()
                _estado.value = _estado.value.copy(carregando = false, logado = true)
            } catch (excecao: Exception) {
                _estado.value = _estado.value.copy(
                    carregando = false,
                    erro = mensagemAmigavel(excecao),
                )
            }
        }
    }

    private fun mensagemAmigavel(excecao: Exception): String = when (excecao) {
        is FirebaseAuthWeakPasswordException -> "A senha deve ter pelo menos 6 caracteres."
        is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha inválidos."
        is FirebaseAuthInvalidUserException -> "Usuário não encontrado."
        is FirebaseAuthUserCollisionException -> "Já existe uma conta com este e-mail."
        is FirebaseNetworkException -> "Sem conexão com a internet. Tente novamente."
        else -> "Ocorreu um erro: ${excecao.localizedMessage ?: "tente novamente."}"
    }
}
