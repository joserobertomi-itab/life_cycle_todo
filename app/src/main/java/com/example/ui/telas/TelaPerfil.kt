package com.example.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.PerfilViewModel

/** Tela de Perfil: exibe nome e e-mail do usuário logado, lidos da coleção "users". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPerfil(
    aoVoltar: () -> Unit,
    perfilViewModel: PerfilViewModel = viewModel(),
) {
    val estado by perfilViewModel.estado.collectAsStateWithLifecycle()

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

                estado.erro != null -> Text(
                    text = estado.erro!!,
                    color = MaterialTheme.colorScheme.error,
                )

                estado.perfil != null -> {
                    Text(
                        text = estado.perfil!!.nome,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = estado.perfil!!.email,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}
