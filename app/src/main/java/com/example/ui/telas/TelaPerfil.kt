package com.example.ui.telas

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.PerfilViewModel

/**
 * Tela de Perfil: exibe nome e e-mail do usuário logado (coleção "users") e
 * permite editar o nome e a foto de perfil (extra do PDF, foto em Base64).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPerfil(
    aoVoltar: () -> Unit,
    perfilViewModel: PerfilViewModel = viewModel(),
) {
    val estado by perfilViewModel.estado.collectAsStateWithLifecycle()
    var nomeEditado by rememberSaveable { mutableStateOf<String?>(null) }
    var uriNovaFoto by rememberSaveable { mutableStateOf<Uri?>(null) }

    val seletorDeFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) uriNovaFoto = uri }

    // Quando o perfil recarrega após salvar, descarta a edição local pendente.
    LaunchedEffect(estado.perfil) {
        if (estado.perfil != null && !estado.salvando) {
            nomeEditado = null
            uriNovaFoto = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu perfil") },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                estado.carregando -> CircularProgressIndicator()

                estado.perfil == null && estado.erro != null -> Text(
                    text = estado.erro!!,
                    color = MaterialTheme.colorScheme.error,
                )

                estado.perfil != null -> {
                    val perfil = estado.perfil!!

                    FotoDePerfil(
                        fotoBase64 = perfil.fotoBase64,
                        uriNovaFoto = uriNovaFoto,
                        aoTocar = {
                            seletorDeFoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    )
                    Text(
                        text = "Toque na foto para trocar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = nomeEditado ?: perfil.nome,
                        onValueChange = { nomeEditado = it },
                        label = { Text("Nome") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = perfil.email,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    if (estado.erro != null) {
                        Text(text = estado.erro!!, color = MaterialTheme.colorScheme.error)
                    }

                    val houveMudanca = uriNovaFoto != null ||
                        (nomeEditado != null && nomeEditado != perfil.nome)
                    if (estado.salvando) {
                        CircularProgressIndicator()
                    } else if (houveMudanca) {
                        Button(
                            onClick = {
                                perfilViewModel.salvar(nomeEditado ?: perfil.nome, uriNovaFoto)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Salvar alterações") }
                    }
                }
            }
        }
    }
}

/** Foto de perfil circular: a nova escolhida, a salva no Firestore ou o ícone padrão. */
@Composable
private fun FotoDePerfil(
    fotoBase64: String,
    uriNovaFoto: Uri?,
    aoTocar: () -> Unit,
) {
    val contexto = LocalContext.current
    val bitmap = remember(fotoBase64, uriNovaFoto) {
        if (uriNovaFoto != null) {
            try {
                contexto.contentResolver.openInputStream(uriNovaFoto)?.use { fluxo ->
                    BitmapFactory.decodeStream(fluxo)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        } else {
            decodificarBase64(fotoBase64)
        }
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .clickable(onClick = aoTocar),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Foto de perfil (padrão)",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
