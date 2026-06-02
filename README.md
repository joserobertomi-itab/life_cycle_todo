# Todo App — Jetpack Compose + Material3

Aplicativo Android educacional em Kotlin + Jetpack Compose com 2 telas,
demonstrando os elementos de UI exigidos no enunciado da disciplina.

## Elementos de UI implementados

`Text` · `OutlinedTextField` · `Image` · `LinearProgressIndicator` · `Button` · `IconButton` · `Checkbox` · `DatePicker`

## Telas

| Tela | Rota | Descrição |
|---|---|---|
| Lista | `lista` | Exibe tarefas, progresso de conclusão e FAB para criar nova |
| Nova Tarefa | `nova_tarefa` | Formulário com título, prazo (DatePicker) e botão Salvar |

## Arquitetura

Single-Activity · `object` Singleton com `MutableStateFlow` · Jetpack Navigation Compose · sem ViewModel · sem banco de dados

## Como executar

1. Abra o projeto no Android Studio
2. Conecte um dispositivo ou inicie um emulador
3. **Shift+F10** para compilar e executar
