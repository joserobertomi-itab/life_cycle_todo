package com.example.ui.telas

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.FeedViewModel
import com.example.data.Publicacao

/**
 * Tela principal (Feed): lista as publicações de todos os usuários em tempo
 * real, da mais nova para a mais antiga.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFeed(
    aoSair: () -> Unit,
    aoAbrirPerfil: () -> Unit,
    aoNovaPublicacao: () -> Unit,
    feedViewModel: FeedViewModel = viewModel(),
) {
    val estado by feedViewModel.estado.collectAsStateWithLifecycle()

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
                        ItemPublicacao(publicacao)
                    }
                }
            }
        }
    }
}

/** Item do feed: nome do autor, imagem da postagem e texto da descrição. */
@Composable
private fun ItemPublicacao(publicacao: Publicacao) {
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
        }
    }
}

/** Converte a imagem Base64 salva no Firestore em bitmap para o Compose. */
private fun decodificarBase64(imagemBase64: String): ImageBitmap? {
    if (imagemBase64.isBlank()) return null
    return try {
        val bytes = Base64.decode(imagemBase64, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: IllegalArgumentException) {
        null
    }
}
