# HANDOFF — Rede Social "I want to believe"

Documento de repasse do trabalho final. Explica **como cada tópico de avaliação foi
implementado**, as decisões técnicas relevantes, e **o que ficou em aberto ou foi
entregue de forma diferente do enunciado, com a justificativa**.

Fontes do trabalho: `Instructions_want_to_believe.pdf` (enunciado) e `exam_topics.txt`
(critérios de nota, 25% cada). Rastreabilidade completa: `todo.md` (checklist verificado)
e `CONTEXT.md` (diário de decisões).

---

## 1. Visão geral da implementação

- **Stack**: Kotlin 100%, Jetpack Compose (Material 3), MVVM, StateFlow + Coroutines,
  Jetpack Navigation Compose, Firebase (Authentication + Cloud Firestore), plano **Spark**.
- **Idioma**: todo o código em português (classes, funções, variáveis, comentários,
  strings de UI). Exceção deliberada: as coleções `users` e `posts` ficam em inglês
  porque o PDF exige esses nomes.
- **Padrão por tela**: um Composable em `ui/telas/` + um ViewModel que expõe um
  `data class Estado...` via `StateFlow` + um repositório em `data/` que encapsula o Firebase.
  Nenhuma tela fala com o Firebase diretamente.

```
app/src/main/java/com/example/
├── MainActivity.kt               # NavHost e rotas (login, cadastro, feed, perfil, novaPublicacao, comentarios/{id})
├── AutenticacaoViewModel.kt      # login/cadastro/sessão/logout
├── PerfilViewModel.kt            # carrega e edita o perfil
├── FeedViewModel.kt              # feed em tempo real + curtir/excluir
├── NovaPublicacaoViewModel.kt    # compressão da imagem + publicar
├── ComentariosViewModel.kt       # comentários em tempo real (id do post via SavedStateHandle)
├── data/
│   ├── RepositorioAutenticacao.kt   # Firebase Auth + coleção "users"
│   ├── RepositorioPublicacoes.kt    # coleção "posts" + subcoleção "comments"
│   └── CompressorDeImagem.kt        # galeria → JPEG redimensionado → Base64
└── ui/telas/                     # TelaLogin, TelaCadastro, TelaFeed, TelaPerfil, TelaNovaPublicacao, TelaComentarios, Imagens.kt
```

## 2. Como cada critério de avaliação foi implementado

### Critério 1 — Autenticação de Usuário (25%)

- **Login e cadastro**: `TelaLogin` (e-mail/senha) e `TelaCadastro` (nome/e-mail/senha),
  ambos via Firebase Authentication (`signInWithEmailAndPassword` /
  `createUserWithEmailAndPassword`) em `RepositorioAutenticacao`. A senha nunca é
  armazenada pelo app.
- **Sessão**: persistida pelo próprio Firebase Auth. `AutenticacaoViewModel` inicia o
  estado com `auth.currentUser != null`; o `NavHost` escolhe a rota inicial (feed ou login)
  por esse estado — fechar e reabrir o app mantém o usuário logado.
- **Logout**: botão "Sair" na barra do feed; `LaunchedEffect(estado.logado)` troca a pilha
  de navegação inteira (`popUpTo(0)`), impedindo "voltar" para uma tela logada.
- **Erros**: exceções do Firebase mapeadas para mensagens amigáveis em português
  (senha fraca, e-mail em uso, credencial inválida, sem internet).

### Critério 2 — Perfil do Usuário (25%)

- **Criação no cadastro**: no mesmo fluxo do `createUser...`, o repositório grava o
  documento `users/{uid}` com `uid`, `nome` e `email` — exatamente o mínimo exigido pelo PDF.
- **Exibição**: `TelaPerfil` lê o documento via `buscarPerfil()` e mostra nome e e-mail
  (e a foto de perfil, do extra 6.4).

### Critério 3 — Feed de Publicações (25%)

- **Tempo real de verdade** (o PDF marcava como opcional, mas o exam_topics exige):
  `addSnapshotListener` na coleção `posts` embrulhado em `callbackFlow`
  (`RepositorioPublicacoes.observarPublicacoes()`), convertido em `StateFlow` com
  `stateIn(WhileSubscribed)` no `FeedViewModel`. Qualquer publicação nova aparece sem
  reiniciar o app.
- **Ordem**: `orderBy("timestamp", DESCENDING)` — mais nova primeiro, como pede o PDF.
- **Layout do item**: card com nome do autor, imagem da postagem e texto da descrição
  (+ barra de ações dos extras).

### Critério 4 — Criação de Publicações (25%)

- **Tela**: campo de descrição + seleção de imagem com o **Photo Picker nativo**
  (`PickVisualMedia`) — sem pedir permissão de armazenamento.
- **Imagem**: comprimida no app (`CompressorDeImagem`: duas passadas de decodificação
  com `inSampleSize`, redimensiona para ≤720 px, JPEG com qualidade decrescente 70→10
  até ≤600 KB) e salva em **Base64 dentro do documento** — ver justificativa na seção 4,
  é o único desvio relevante do enunciado.
- **Persistência**: documento em `posts` com `uid` do autor, `nomeAutor` (desnormalizado
  para o feed não precisar de join), `texto`, `imagemBase64`, `curtidas` e
  `timestamp` (**do servidor**, `FieldValue.serverTimestamp()`).

## 3. Extras do PDF entregues (pontos negociáveis)

| Extra | Como foi feito |
|---|---|
| Likes em tempo real | Campo `curtidas` (lista de uids) no post; `arrayUnion`/`arrayRemove`; coração + contagem no feed |
| Comentários | Subcoleção `posts/{id}/comments` com listener em tempo real; `TelaComentarios` com envio |
| Edição de perfil | Nome e foto editáveis na `TelaPerfil`; foto em Base64 256 px (≤100 KB) no documento `users` |
| Exclusão de publicação | Lixeira visível só nos posts do próprio autor + diálogo de confirmação; regra do Firestore reforça |
| Erros e carregamento | Spinner em toda busca/envio; mensagens amigáveis em todas as telas; Snackbar para falhas de curtir/excluir |

Tudo verificado em dispositivo em 2026-07-06 com as regras definitivas publicadas.

## 4. Desvios do enunciado e requisitos em aberto — com justificativa

### 4.1 Cloud Storage substituído por Base64 no Firestore (desvio deliberado)

O PDF pede upload da imagem para o **Firebase Cloud Storage** e a URL no documento.
**Não foi entregue assim**, porque:

1. O `exam_topics.txt` exige que o projeto rode no **plano Spark** ("deixar a conta Spark
   ativada para avaliação") — e é critério de nota, então tem precedência sobre o PDF.
2. Projetos Firebase novos **só liberam o Cloud Storage no plano Blaze** (verificado no
   Console deste projeto em 2026-07-06: "Para usar Storage, faça upgrade do plano").

**Solução adotada** (validada com o autor do trabalho antes de implementar): a imagem é
comprimida no app e salva em **Base64 no campo `imagemBase64`** do próprio documento —
sempre abaixo do limite de 1 MiB/documento do Firestore. Funcionalmente, o critério 4
("upload de imagem e persistência da publicação no banco de dados") é atendido por completo.
O documento contém todos os demais campos exigidos (texto, uid, timestamp); apenas
"URL da imagem" virou "imagem em Base64".

*Teto conhecido da abordagem*: cada leitura do feed baixa as imagens junto (mais banda que
uma URL + cache). Para a escala de um trabalho de disciplina, as cotas do Spark
(50k leituras/dia, 10 GiB/mês) são folgadas. Caminho de upgrade: mover para Storage/CDN
quando houver plano Blaze.

### 4.2 Limitações conhecidas e aceitas (registradas no CONTEXT.md)

- **Renomear o perfil não reescreve o `nomeAutor` dos posts antigos** — o nome é
  desnormalizado no documento do post. Corrigir exigiria batch update de todos os posts do
  autor a cada renomeação; fora do escopo dos critérios. Posts novos usam o nome novo.
- **Feed sem paginação**: carrega todas as publicações. Adequado ao volume de uma
  demonstração em sala; com muitos posts, o caminho é `limit()` + paginação por cursor.
- **Sem testes automatizados**: nenhuma das duas fontes exige; a verificação foi feita por
  roteiro manual de ponta a ponta (documentado no todo.md, executado em dispositivo real).
- **`applicationId` com typo** (`com.example.checklistplaystor`, sem o "e" final): o pacote
  foi registrado assim no Firebase Console; alinhar o app foi mais barato que re-registrar.
  Sem efeito prático — mas quem recriar o projeto Firebase deve registrar exatamente esse pacote.

### 4.3 O que permanece em aberto (fora do código)

- **Apresentação em sala** (instrução de entrega do PDF) — a cargo do aluno.
- **`google-services.json` não é versionado** (decisão de segurança): quem clonar o
  repositório precisa criar o próprio projeto Firebase seguindo o passo a passo do README,
  ou receber o arquivo por outro canal.

## 5. Configuração do Firebase (estado atual)

- Projeto `i-want-to-believe-c908f`, **plano Spark**, Authentication (E-mail/senha) e
  Cloud Firestore ativos. Storage **não** ativado (indisponível no Spark).
- **Regras definitivas publicadas** (não expiram, diferente do modo de teste). Texto integral,
  para reproduzir em um projeto novo (Console → Firestore Database → Regras):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == uid;
    }
    match /posts/{postId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.uid == request.auth.uid;
      allow delete: if request.auth != null && resource.data.uid == request.auth.uid;
      // update só pode mexer no campo de curtidas
      allow update: if request.auth != null
        && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['curtidas']);
      match /comments/{commentId} {
        allow read: if request.auth != null;
        allow create: if request.auth != null && request.resource.data.uid == request.auth.uid;
      }
    }
  }
}
```

## 6. Histórico de commits

```
f9b26fc Extras do PDF: curtidas, exclusão, comentários e edição de perfil
612dc88 Rede social "I want to believe": critérios 1-4 verificados de ponta a ponta
28b662f init project
```
