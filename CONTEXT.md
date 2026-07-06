# CONTEXT.md — memória do projeto "I want to believe"

## 2026-07-06 — Sessão 1: leitura das fontes e planejamento

**Feito:**
- Lidas as duas fontes na íntegra: `Instructions_want_to_believe.pdf` (3 páginas) e `exam_topics.txt`.
- Gerado `todo.md` com os 4 critérios (25% cada) + requisitos técnicos do PDF.

**Estado do código encontrado:**
- O repositório é um app de checklist (Compose, ViewModel, DataStore) — `MainActivity.kt`, `ChecklistViewModel.kt`, `data/ChecklistConteudo.kt`, `data/ChecklistRepository.kt`. Será transformado na rede social.
- Sem Firebase, sem Navigation Compose, sem Coil. `applicationId = com.example.checklistplaystore`, namespace `com.example`, compileSdk 36, minSdk 24, Kotlin + Compose BoM via version catalog.

**Decisões/observações:**
- Ordem de implementação = ordem dos critérios (auth → perfil → feed → criação de post).
- ⚠️ Risco conhecido: Cloud Storage pode não estar disponível no plano Spark para projetos Firebase novos (exige Blaze desde ~out/2024). Alternativa candidata: imagem comprimida em Base64 dentro do documento do Firestore (limite de 1 MiB por documento). Decisão adiada até o usuário verificar no Console — ponto de interrupção obrigatório.
- Tempo real no feed: o PDF marca como "opcional (por comando)", mas o `exam_topics.txt` diz "em tempo real" — implementar com `addSnapshotListener`.

**Aguardando o usuário:**
- ~~Validar o `todo.md` antes de qualquer código.~~ ✅ Aprovado.
- Configuração do Firebase Console (projeto, google-services.json, Auth, Firestore, verificação do Storage no Spark).

## 2026-07-06 — Sessão 1 (cont.): critério 1 implementado (código)

**Feito:**
- todo.md aprovado pelo usuário. Usuário vai criar o projeto Firebase agora (passo a passo entregue).
- Removidos os arquivos do app de checklist (`ChecklistViewModel.kt`, `data/Checklist*.kt`) — recuperáveis via git.
- Build: adicionados Firebase BoM 34.12.0 + Auth + Firestore, `kotlinx-coroutines-play-services`, Navigation Compose e Coil (BoM, nav e coil já existiam no version catalog). Plugin `google-services` 4.4.4 declarado no root e aplicado no app **condicionalmente** (`if (file("google-services.json").exists())`) para compilar antes da configuração do Console.
- Manifest: permissão INTERNET. `app_name` → "I want to believe".
- Código novo (tudo em português, MVVM):
  - `data/RepositorioAutenticacao.kt` — Auth (entrar/cadastrar/sair/sessão) + perfil na coleção `users` criado no cadastro + `buscarPerfil()` (já prepara o critério 2). Coleções `users`/`posts` em inglês por exigência do PDF.
  - `AutenticacaoViewModel.kt` — StateFlow `EstadoAutenticacao(carregando, erro, logado)`, mensagens de erro amigáveis em português por tipo de exceção do Firebase.
  - `ui/telas/TelaLogin.kt`, `TelaCadastro.kt`, `TelaFeed.kt` (feed é placeholder com botão "Sair").
  - `MainActivity.kt` — NavHost (rotas login/cadastro/feed), rota inicial pela sessão do Firebase, `LaunchedEffect(estado.logado)` troca a pilha em login/logout.
- Verificado: `:app:compileDebugKotlin`, testes unit e androidTest compilam.

**Pendente/bloqueado:**
- Teste de ponta a ponta do critério 1 bloqueado até o usuário colocar `app/google-services.json` (por isso nenhum item do critério 1 está marcado como concluído).
- applicationId a registrar no Firebase: `com.example.checklistplaystore`.

## 2026-07-06 — Sessão 1 (cont. 2): Firebase configurado + critérios 2, 3 e 4 implementados

**Configuração do Console (feita pelo usuário):**
- Projeto `i-want-to-believe-c908f`, plano Spark. Auth (E-mail/senha) e Firestore ativados ("demais configs ok").
- ⚠️ Pacote registrado no Console com typo: `com.example.checklistplaystor` (sem o "e"). Decisão: ajustar o `applicationId` do app para bater com o json (1 linha), em vez de registrar outro app no Console.
- `google-services.json` movido de ~/Downloads para `app/`. Build com o plugin ativo passa.
- **Storage confirmado indisponível no Spark** (Console pede upgrade Blaze). Alternativas apresentadas; **usuário escolheu A: imagem Base64 no Firestore**. Coil removido das dependências (desnecessário — decodificação direta de Base64).
- Firestore criado em **modo de teste** → regras expiram em ~30 dias; apertar regras antes da entrega (item no todo).

**Implementado (tudo compilando, `assembleDebug` ok):**
- `data/RepositorioPublicacoes.kt` — `observarPublicacoes()` com `callbackFlow` + `addSnapshotListener`, orderBy timestamp desc; `publicar()` grava em `posts`: uid, nomeAutor (desnormalizado de `users`), texto, imagemBase64, timestamp do servidor.
- `FeedViewModel` (stateIn/WhileSubscribed), `PerfilViewModel`, `NovaPublicacaoViewModel` (AndroidViewModel; compressão: inSampleSize → ≤720px → JPEG com qualidade decrescente 70→10 até ≤600 KB ≈ 800 KB em Base64, sob o limite de 1 MiB/doc).
- Telas: `TelaFeed` real (LazyColumn, card com autor/imagem/texto, estados vazio/erro/carregando, FAB nova publicação, ícone perfil), `TelaPerfil`, `TelaNovaPublicacao` (Photo Picker nativo `PickVisualMedia` — sem permissão de armazenamento).
- Rotas novas: `perfil`, `novaPublicacao`.

**Pendente:**
- Teste de ponta a ponta dos 4 critérios em emulador/dispositivo (nenhum conectado via adb no momento) — roteiro entregue ao usuário. Nenhum item de critério marcado até lá.

## 2026-07-06 — Sessão 1 (cont. 3): verificação de ponta a ponta ✅

**Feito:**
- Usuário executou o roteiro completo de testes no dispositivo: cadastro, perfil, sessão persistente, publicação com imagem, tempo real no feed e logout — **tudo OK**.
- todo.md atualizado: critérios 1–4 e requisitos técnicos todos marcados como verificados.

**Estado: os 4 critérios de avaliação (100%) estão funcionais de ponta a ponta no plano Spark.**

**Único pendente (ação do usuário no Console, antes da entrega):**
- Firestore está em modo de teste — as regras **expiram ~30 dias após 2026-07-06** (início de agosto) e o app para de funcionar na hora da apresentação se não trocar. Regras definitivas sugeridas (leitura autenticada; escrita do perfil só pelo dono; post criado só pelo próprio autor) foram entregues ao usuário no chat.
- Extras opcionais do PDF (likes, comentários, edição de perfil, exclusão de post) continuam não implementados por decisão de escopo — só com pedido explícito.

## 2026-07-06 — Sessão 1 (cont. 4): commit do núcleo + extras aprovados

**Feito:**
- Usuário aprovou implementar os extras do PDF. Sequência definida no todo.md (seção 6): erros/carregamento (revisão) → exclusão de post → likes → edição de perfil → comentários → regras finais.
- README reescrito para a rede social (era do app de checklist antigo).
- `google-services.json`: havia uma cópia duplicada **staged na raiz** — removida; `google-services.json` adicionado ao .gitignore (avaliador usa o próprio; passo a passo no README).
- Commit do núcleo verificado (critérios 1–4) feito na branch main.

**Atenção para os extras:**
- Exclusão/likes/comentários exigem regras novas no Firestore (delete/update em `posts`, subcoleção `comments`) — entregar regras completas ao usuário no item 6.6.

## 2026-07-06 — Sessão 1 (cont. 5): extras 6.1–6.5 implementados (aguardando teste)

**Feito (tudo compilando, `assembleDebug` ok):**
- **Curtidas**: campo `curtidas` (lista de uids) no documento do post; `arrayUnion`/`arrayRemove`; coração preenchido/contorno + contagem no item do feed. Posts antigos sem o campo funcionam (lista vazia; `arrayUnion` cria o campo).
- **Exclusão**: ícone de lixeira só nos posts do próprio autor + AlertDialog de confirmação; erros de ação (curtir/excluir) viram Snackbar no feed (`erroAcao` separado do fluxo do feed para não complicar o `stateIn`).
- **Comentários**: subcoleção `posts/{id}/comments` em tempo real (mais antigo primeiro), rota `comentarios/{idPublicacao}` (id via SavedStateHandle), TelaComentarios com campo + botão enviar.
- **Edição de perfil**: nome editável + foto de perfil (Base64 256px/≤100 KB no doc `users`); botão "Salvar alterações" só aparece se houve mudança. ⚠️ Limitação conhecida: renomear NÃO atualiza o `nomeAutor` desnormalizado dos posts antigos (decisão: aceitável para o escopo; corrigir só se o avaliador exigir).
- **Compressão extraída** para `data/CompressorDeImagem.kt` (usada por post 720px/600KB e foto de perfil 256px/100KB); `decodificarBase64` compartilhado em `ui/telas/Imagens.kt`.

**Pendente:**
- Usuário publicar as regras novas no Console (delete/update em posts + comments) — sem isso, curtir/excluir/comentar falham quando o modo de teste expirar (hoje ainda funcionam pelas regras abertas).
- Teste de ponta a ponta dos extras pelo usuário → aí marcar 6.1–6.5 no todo.md e commitar os extras.

## 2026-07-06 — Sessão 1 (cont. 6): extras verificados e commitados ✅ TRABALHO COMPLETO

- Usuário testou todos os extras no dispositivo: curtidas, exclusão, comentários, edição de perfil — **tudo OK**.
- **Regras definitivas publicadas no Console** (não estão mais no modo de teste; sem prazo de expiração).
- todo.md: seções 0–6 completas. Extras commitados.

**Critério de parada atingido**: todos os itens do todo.md verificados, 4 critérios + 5 extras funcionais de ponta a ponta, app compila e roda no plano Spark. Restam apenas a apresentação em sala (instruções de entrega do PDF).
