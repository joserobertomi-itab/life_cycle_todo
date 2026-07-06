package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.telas.TelaCadastro
import com.example.ui.telas.TelaFeed
import com.example.ui.telas.TelaLogin
import com.example.ui.telas.TelaNovaPublicacao
import com.example.ui.telas.TelaPerfil
import com.example.ui.theme.MyApplicationTheme

/** Rotas do Jetpack Navigation Compose. */
object Rotas {
    const val LOGIN = "login"
    const val CADASTRO = "cadastro"
    const val FEED = "feed"
    const val PERFIL = "perfil"
    const val NOVA_PUBLICACAO = "novaPublicacao"
}

/**
 * Ponto de entrada do app "I want to believe".
 * Configura o tema e a navegação; cada tela tem seu próprio composable (MVVM).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppRedeSocial()
            }
        }
    }
}

@Composable
fun AppRedeSocial() {
    val controleNavegacao = rememberNavController()
    val autenticacaoViewModel: AutenticacaoViewModel = viewModel()
    val estado by autenticacaoViewModel.estado.collectAsState()

    // Sessão persistente: se o Firebase já tem usuário, o app abre direto no feed.
    val rotaInicial = remember { if (estado.logado) Rotas.FEED else Rotas.LOGIN }

    // Reage a login/logout: troca de pilha inteira (não dá para "voltar" ao login logado).
    LaunchedEffect(estado.logado) {
        val destino = if (estado.logado) Rotas.FEED else Rotas.LOGIN
        controleNavegacao.navigate(destino) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = controleNavegacao, startDestination = rotaInicial) {
        composable(Rotas.LOGIN) {
            TelaLogin(
                estado = estado,
                aoEntrar = autenticacaoViewModel::entrar,
                aoIrParaCadastro = {
                    autenticacaoViewModel.limparErro()
                    controleNavegacao.navigate(Rotas.CADASTRO)
                },
            )
        }
        composable(Rotas.CADASTRO) {
            TelaCadastro(
                estado = estado,
                aoCadastrar = autenticacaoViewModel::cadastrar,
                aoVoltarParaLogin = {
                    autenticacaoViewModel.limparErro()
                    controleNavegacao.popBackStack()
                },
            )
        }
        composable(Rotas.FEED) {
            TelaFeed(
                aoSair = autenticacaoViewModel::sair,
                aoAbrirPerfil = { controleNavegacao.navigate(Rotas.PERFIL) },
                aoNovaPublicacao = { controleNavegacao.navigate(Rotas.NOVA_PUBLICACAO) },
            )
        }
        composable(Rotas.PERFIL) {
            TelaPerfil(aoVoltar = { controleNavegacao.popBackStack() })
        }
        composable(Rotas.NOVA_PUBLICACAO) {
            TelaNovaPublicacao(
                aoVoltar = { controleNavegacao.popBackStack() },
                aoPublicar = { controleNavegacao.popBackStack() },
            )
        }
    }
}
