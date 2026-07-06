package com.example.ui.telas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.FeedViewModel
import com.example.data.Publicacao

/**
 * Tela principal (Feed): lista as publicações de todos os usuários em tempo
 * real, da mais nova para a mais antiga. Extras: curtir, comentar e excluir
 * a própria publicação.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFeed(
    aoSair: () -> Unit,
    aoAbrirPerfil: () -> Unit,
    aoNovaPublicacao: () -> Unit,
    aoAbrirComentarios: (idPublicacao: String) -> Unit,
    feedViewModel: FeedViewModel = viewModel(),
) {
    val estado by feedViewModel.estado.collectAsStateWithLifecycle()
    val erroAcao by feedViewModel.erroAcao.collectAsStateWithLifecycle()
    val avisos = remember { SnackbarHostState() }
    var publicacaoParaExcluir by remember { mutableStateOf<Publicacao?>(null) }

    // Erros de ações pontuais (curtir/excluir) viram um aviso passageiro.
    LaunchedEffect(erroAcao) {
        erroAcao?.let {
            avisos.showSnackbar(it)
            feedViewModel.limparErroAcao()
        }
    }

    publicacaoParaExcluir?.let { publicacao ->
        AlertDialog(
            onDismissRequest = { publicacaoParaExcluir = null },
            title = { Text("Excluir publicação?") },
            text = { Text("Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        feedViewModel.excluir(publicacao)
                        publicacaoParaExcluir = null
                    },
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { publicacaoParaExcluir = null }) { Text("Cancelar") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("I want to believe") },
                actions = {
                    IconButton(onClick = aoAbrirPerfil) {
                        Icon(Icons.Filled.Person, contentDescription = "Perfil")
                    }
                    TextButton(onClick = aoSair) { Text("Sair") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = aoNovaPublicacao) {
                Icon(Icons.Filled.Add, contentDescription = "Nova publicação")
            }
        },
        snackbarHost = { SnackbarHost(avisos) },
    ) { espacamentoInterno ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacamentoInterno),
            contentAlignment = Alignment.Center,
        ) {
            when {
                estado.carregando -> CircularProgressIndicator()

                estado.erro != null -> Text(
                    text = estado.erro!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp),
                )

                estado.publicacoes.isEmpty() -> Text("Nenhuma publicação ainda. Seja o primeiro!")

                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(estado.publicacoes, key = { it.id }) { publicacao ->
                        ItemPublicacao(
                            publicacao = publicacao,
                            uidAtual = feedViewModel.uidAtual,
                            aoCurtir = { feedViewModel.alternarCurtida(publicacao) },
                            aoComentar = { aoAbrirComentarios(publicacao.id) },
                            aoExcluir = { publicacaoParaExcluir = publicacao },
                        )
                    }
                }
            }
        }
    }
}

/** Item do feed: autor, imagem, texto e a barra de ações (curtir, comentar, excluir). */
@Composable
private fun ItemPublicacao(
    publicacao: Publicacao,
    uidAtual: String?,
    aoCurtir: () -> Unit,
    aoComentar: () -> Unit,
    aoExcluir: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Column {
            Text(
                text = publicacao.nomeAutor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(12.dp),
            )
            val imagem = remember(publicacao.id) { decodificarBase64(publicacao.imagemBase64) }
            if (imagem != null) {
                Image(
                    bitmap = imagem,
                    contentDescription = "Imagem da publicação de ${publicacao.nomeAutor}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(imagem.width.toFloat() / imagem.height.toFloat()),
                )
            }
            Text(
                text = publicacao.texto,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(12.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                val curtiu = uidAtual != null && uidAtual in publicacao.curtidas
                IconButton(onClick = aoCurtir) {
                    Icon(
                        imageVector = if (curtiu) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (curtiu) "Descurtir" else "Curtir",
                        tint = if (curtiu) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(text = "${publicacao.curtidas.size}")

                TextButton(onClick = aoComentar) { Text("Comentários") }

                Spacer(modifier = Modifier.weight(1f))

                // Só o autor pode excluir a própria publicação.
                if (uidAtual == publicacao.uid) {
                    IconButton(onClick = aoExcluir) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Excluir publicação",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
