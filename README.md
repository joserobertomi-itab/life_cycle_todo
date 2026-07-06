# I want to believe 🛸

Rede social simplificada — trabalho final da disciplina de Desenvolvimento de
Aplicativos Móveis com Android. Usuários criam conta, têm perfil, publicam
postagens com texto e imagem e veem o feed de todos em **tempo real**.
Código 100% em português.

## Stack

- **Kotlin** · **Jetpack Compose** (Material 3) · **MVVM** · StateFlow + Coroutines
- **Jetpack Navigation Compose** para navegação entre telas
- **Firebase**: Authentication (e-mail/senha) + Cloud Firestore
- `minSdk` 24 · `targetSdk` 36

## Decisão importante: imagens sem Cloud Storage

O trabalho exige funcionar no **plano Spark (gratuito)** do Firebase, mas
projetos novos só liberam o Cloud Storage no plano Blaze. Por isso a imagem da
publicação é **comprimida no app** (JPEG ≤ 720 px, qualidade adaptativa) e
salva em **Base64 dentro do próprio documento** da coleção `posts`
(abaixo do limite de 1 MiB/documento do Firestore).

## Estrutura do projeto

```
app/src/main/java/com/example/
├── MainActivity.kt               # NavHost e rotas (login, cadastro, feed, perfil, novaPublicacao)
├── AutenticacaoViewModel.kt      # Login, cadastro, sessão e logout
├── PerfilViewModel.kt            # Carrega o perfil da coleção "users"
├── FeedViewModel.kt              # Observa a coleção "posts" em tempo real
├── NovaPublicacaoViewModel.kt    # Compressão da imagem + publicação
├── data/
│   ├── RepositorioAutenticacao.kt  # Firebase Auth + perfil no Firestore
│   └── RepositorioPublicacoes.kt   # Feed (addSnapshotListener) e criação de posts
├── ui/telas/                     # TelaLogin, TelaCadastro, TelaFeed, TelaPerfil, TelaNovaPublicacao
└── ui/theme/                     # Tema Material 3
```

## Como rodar

1. Crie um projeto no [Firebase Console](https://console.firebase.google.com)
   (plano Spark) e registre um app Android com o pacote
   `com.example.checklistplaystor`.
2. Ative **Authentication → E-mail/senha** e crie o **Cloud Firestore**.
3. Baixe o `google-services.json` e coloque em `app/`
   (o arquivo não é versionado — veja `.gitignore`).
4. Abra no Android Studio, aguarde o Gradle Sync e clique em **Run ▶**
   (emulador ou celular com Android 7.0/API 24+).

## Organização do trabalho

- `todo.md` — checklist dos critérios de avaliação (só marcado após verificação real).
- `CONTEXT.md` — diário de decisões: o que foi tentado, o que funcionou, o que falhou.
- `Instructions_want_to_believe.pdf` / `exam_topics.txt` — enunciado e critérios.
