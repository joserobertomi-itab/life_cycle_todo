package com.example.ui.telas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ComentariosViewModel

/**
 * Tela de Comentários (extra do PDF): lista em tempo real os comentários
 * de uma publicação e permite adicionar um novo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaComentarios(
    aoVoltar: () -> Unit,
    comentariosViewModel: ComentariosViewModel = viewModel(),
) {
    val estado by comentariosViewModel.estado.collectAsStateWithLifecycle()
    val enviando by comentariosViewModel.enviando.collectAsStateWithLifecycle()
    val erroEnvio by comentariosViewModel.erroEnvio.collectAsStateWithLifecycle()
    var texto by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comentários") },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { espacamentoInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacamentoInterno)
                .imePadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    estado.carregando -> CircularProgressIndicator()

                    estado.erro != null -> Text(
                        text = estado.erro!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp),
                    )

                    estado.comentarios.isEmpty() -> Text("Nenhum comentário ainda.")

                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(estado.comentarios, key = { it.id }) { comentario ->
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    text = comentario.nomeAutor,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = comentario.texto,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            if (erroEnvio != null) {
                Text(
                    text = erroEnvio!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Escreva um comentário") },
                    modifier = Modifier.weight(1f),
                )
                if (enviando) {
                    CircularProgressIndicator(modifier = Modifier.padding(12.dp))
                } else {
                    IconButton(
                        onClick = {
                            comentariosViewModel.comentar(texto)
                            texto = ""
                        },
                        enabled = texto.isNotBlank(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar comentário")
                    }
                }
            }
        }
    }
}
