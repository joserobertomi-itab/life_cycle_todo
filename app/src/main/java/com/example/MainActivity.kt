package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

// ─── MODELO ──────────────────────────────────────────────────────────────────

data class Tarefa(
    val id: Long,
    val titulo: String,
    val deadline: Long,
    val concluida: Boolean
)

// ─── REPOSITÓRIO ─────────────────────────────────────────────────────────────

object TarefaRepository {
    private val _tarefas = MutableStateFlow<List<Tarefa>>(emptyList())
    val tarefas = _tarefas.asStateFlow()

    fun adicionar(tarefa: Tarefa) {
        _tarefas.value = _tarefas.value + tarefa
    }

    fun alternarConcluida(id: Long) {
        _tarefas.value = _tarefas.value.map {
            if (it.id == id) it.copy(concluida = !it.concluida) else it
        }
    }

    fun remover(id: Long) {
        _tarefas.value = _tarefas.value.filter { it.id != id }
    }
}

// ─── ACTIVITY ────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "lista") {
                    composable("lista") { ListaScreen(navController = navController) }
                    composable("nova_tarefa") { NovaTarefaScreen(navController = navController) }
                }
            }
        }
    }
}

// ─── TELA 1 — LISTA ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(navController: NavController) {
    val tarefas by TarefaRepository.tarefas.collectAsState()
    val total = tarefas.size
    val concluidas = tarefas.count { it.concluida }
    val progresso = if (total == 0) 0f else concluidas.toFloat() / total

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Minhas Tarefas") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("nova_tarefa") }) {
                Icon(Icons.Default.Add, contentDescription = "Nova tarefa")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$concluidas de $total concluídas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tarefas, key = { it.id }) { tarefa ->
                    TarefaItem(
                        tarefa = tarefa,
                        onToggle = { TarefaRepository.alternarConcluida(tarefa.id) },
                        onDelete = { TarefaRepository.remover(tarefa.id) }
                    )
                }
            }
        }
    }
}

// ─── COMPONENTE — ITEM DA LISTA ──────────────────────────────────────────────

@Composable
fun TarefaItem(tarefa: Tarefa, onToggle: () -> Unit, onDelete: () -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = tarefa.concluida, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarefa.titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (tarefa.concluida) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = dateFormatter.format(Date(tarefa.deadline)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remover")
            }
        }
    }
}

// ─── TELA 2 — NOVA TAREFA ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaTarefaScreen(navController: NavController) {
    var titulo by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Tarefa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título da tarefa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val deadline = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    TarefaRepository.adicionar(
                        Tarefa(
                            id = System.currentTimeMillis(),
                            titulo = titulo.trim(),
                            deadline = deadline,
                            concluida = false
                        )
                    )
                    navController.popBackStack()
                },
                enabled = titulo.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
