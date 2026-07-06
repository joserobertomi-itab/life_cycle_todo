package com.example.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.EstadoAutenticacao

/** Tela de Login: e-mail e senha para um usuário existente acessar sua conta. */
@Composable
fun TelaLogin(
    estado: EstadoAutenticacao,
    aoEntrar: (email: String, senha: String) -> Unit,
    aoIrParaCadastro: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "I want to believe", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (estado.erro != null) {
            Text(text = estado.erro, color = MaterialTheme.colorScheme.error)
        }

        if (estado.carregando) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { aoEntrar(email, senha) }, modifier = Modifier.fillMaxWidth()) {
                Text("Entrar")
            }
            TextButton(onClick = aoIrParaCadastro) {
                Text("Não tem conta? Cadastre-se")
            }
        }
    }
}
