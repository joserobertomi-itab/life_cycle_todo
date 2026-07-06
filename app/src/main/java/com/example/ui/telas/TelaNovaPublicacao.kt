package com.example.ui.telas

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.NovaPublicacaoViewModel

/**
 * Tela de Nova Publicação: descrição + imagem escolhida na galeria
 * (Photo Picker nativo, sem permissão de armazenamento).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaNovaPublicacao(
    aoVoltar: () -> Unit,
    aoPublicar: () -> Unit,
    novaPublicacaoViewModel: NovaPublicacaoViewModel = viewModel(),
) {
    val estado by novaPublicacaoViewModel.estado.collectAsStateWithLifecycle()
    var texto by rememberSaveable { mutableStateOf("") }
    var uriImagem by rememberSaveable { mutableStateOf<Uri?>(null) }

    val seletorDeImagem = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) uriImagem = uri }

    // Publicação concluída: volta para o feed (o listener em tempo real mostra o post).
    LaunchedEffect(estado.publicado) {
        if (estado.publicado) aoPublicar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova publicação") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Descrição") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            PreviaDaImagem(uriImagem)

            OutlinedButton(
                onClick = {
                    seletorDeImagem.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uriImagem == null) "Selecionar imagem da galeria" else "Trocar imagem")
            }

            if (estado.erro != null) {
                Text(text = estado.erro!!, color = MaterialTheme.colorScheme.error)
            }

            if (estado.carregando) {
                CircularProgressIndicator()
                Text("Publicando…")
            } else {
                Button(
                    onClick = { novaPublicacaoViewModel.publicar(texto, uriImagem) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Publicar")
                }
            }
        }
    }
}

/** Prévia da imagem escolhida, decodificada direto da galeria. */
@Composable
private fun PreviaDaImagem(uri: Uri?) {
    if (uri == null) return
    val contexto = LocalContext.current
    val bitmap = remember(uri) {
        try {
            contexto.contentResolver.openInputStream(uri)?.use { fluxo ->
                BitmapFactory.decodeStream(fluxo)?.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Imagem selecionada",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )
    }
}
