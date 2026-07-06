# todo.md — Rede Social "I want to believe"

Referencial único do trabalho. Um item só é marcado `[x]` após: build passa + fluxo testado de ponta a ponta.
Fontes: `Instructions_want_to_believe.pdf` + `exam_topics.txt`.

## 0. Pré-requisitos (bloqueiam tudo — ação do usuário no Console)

- [x] **[USUÁRIO]** Criar projeto no Firebase Console (plano Spark) e registrar o app Android. Projeto `i-want-to-believe-c908f`; pacote registrado com typo (`com.example.checklistplaystor`), applicationId ajustado para bater. — pré-requisito dos 4 critérios
- [x] **[USUÁRIO]** Baixar `google-services.json` e colocar em `app/`. Feito; build com plugin google-services passa. — pré-requisito dos 4 critérios
- [x] **[USUÁRIO]** Ativar Authentication (provedor E-mail/Senha) no Console. — Critério 1 (25%)
- [x] **[USUÁRIO]** Ativar Cloud Firestore no Console (modo de teste; regras expiram em ~30 dias — apertar antes da entrega). — Critérios 2, 3, 4 (75%)
- [x] **[USUÁRIO]** Verificar Storage no Spark: confirmado que exige Blaze. Decisão do usuário: **alternativa A — imagem Base64 no Firestore** (comprimida no app, < 1 MiB/documento). — Critério 4 (25%)
- [x] Adicionar dependências: Firebase BoM, Auth, Firestore, plugin google-services (aplicado condicionalmente até o json chegar), Navigation Compose, Coil, coroutines-play-services. Build verificado. — pré-requisito dos 4 critérios

## 1. Autenticação de Usuário (25%)

- [x] Tela de Login: campos e-mail e senha; botão entrar; erro amigável em falha (credencial inválida, sem rede). — Critério 1
- [x] Tela de Cadastro: campos nome, e-mail e senha; cria conta via Firebase Auth. — Critério 1
- [x] Integração Firebase Authentication: login e cadastro gerenciados pelo Firebase (senha nunca armazenada pelo app). — Critério 1
- [x] Sessão persistente: fechar e reabrir o app mantém o usuário logado (vai direto ao feed). — Critério 1
- [x] Botão "Sair" (logout) visível, retorna à tela de login. — Critério 1
- [x] Verificado pelo usuário em 2026-07-06: cadastro → fechar app → reabrir logado → sair → login novamente. — Critério 1

## 2. Perfil do Usuário (25%)

- [x] No cadastro, criar documento na coleção `users` do Firestore com no mínimo: `uid` (do Auth), `nome`, `email`. — Critério 2
- [x] Tela de Perfil: exibe nome e e-mail do usuário logado, lidos do Firestore. — Critério 2
- [x] Verificado pelo usuário em 2026-07-06: cadastro cria o documento em `users` e a tela de perfil mostra os dados corretos. — Critério 2

## 3. Feed de Publicações (25%)

- [x] Tela principal (Feed): lista todas as publicações de todos os usuários, ordem cronológica decrescente (mais nova primeiro). — Critério 3
- [x] Atualização em tempo real via `addSnapshotListener` (o PDF permite "por comando" como opcional, mas tempo real é o critério do exam_topics). — Critério 3
- [x] Layout do item: nome do autor + imagem da postagem + texto da descrição. — Critério 3
- [x] Verificado pelo usuário em 2026-07-06: nova publicação aparece no feed sem reiniciar o app. — Critério 3

## 4. Criação de Publicações (25%)

- [x] Tela de Nova Publicação: campo de texto (descrição) + botão para selecionar imagem da galeria (Photo Picker nativo). — Critério 4
- [x] Imagem comprimida (JPEG ≤720px, qualidade adaptativa) e salva em Base64 no documento do post (alternativa Spark decidida no item 0, Storage exige Blaze). — Critério 4
- [x] Persistência no Firestore, coleção `posts`, com: `imagemBase64`, `texto`, `uid` do autor, `nomeAutor` (desnormalizado para o feed) e `timestamp` (servidor). — Critério 4
- [x] Verificado pelo usuário em 2026-07-06: publicar com imagem → post visível no feed com imagem carregada. — Critério 4

## 5. Requisitos do PDF não cobertos pelos critérios

Técnicos obrigatórios (sem peso próprio, mas exigidos):
- [x] 100% Kotlin, UI 100% Jetpack Compose (sem XML de layout). — Req. técnico
- [x] Arquitetura MVVM (ViewModel por tela + Repository). — Req. técnico
- [x] Estado/assíncrono com StateFlow e Coroutines. — Req. técnico
- [x] Navegação entre telas com Jetpack Navigation Compose. — Req. técnico
- [x] Todo o código em português (classes, funções, variáveis, comentários, strings de UI; coleções `users`/`posts` em inglês por exigência do PDF). — Instrução do usuário
- [x] App compila e roda de ponta a ponta no plano Spark (verificado pelo usuário em 2026-07-06). — Restrição do exam_topics
- [ ] **[USUÁRIO]** Trocar as regras do Firestore de "modo de teste" (expiram ~30 dias após 2026-07-06) pelas regras definitivas antes da entrega/apresentação. — todos os critérios

## 6. Extras do PDF (pontos extras) — aprovados pelo usuário em 2026-07-06

Ordem de implementação (menor esforço/risco primeiro; cada um: build + teste + atualizar regras do Firestore quando indicado):

- [x] **6.1 Tratamento de erros e carregamento**: spinners em toda busca/envio, mensagens amigáveis em todas as telas e Snackbar para falhas de curtir/excluir. — Extra
- [x] **6.2 Exclusão de publicações**: lixeira só nos posts do próprio autor, com diálogo de confirmação. Regra `delete` publicada. — Extra
- [x] **6.3 Sistema de likes**: curtir/descurtir com contagem em tempo real (campo `curtidas`, lista de uids). Regra `update` publicada. — Extra
- [x] **6.4 Edição de perfil**: nome e foto de perfil editáveis na TelaPerfil (foto em Base64 256px no documento `users`). Limitação registrada: renomear não reescreve `nomeAutor` de posts antigos. — Extra
- [x] **6.5 Comentários**: subcoleção `posts/{id}/comments` em tempo real + tela de comentários com envio. — Extra
- [x] **6.6** Regras definitivas do Firestore publicadas pelo usuário no Console (users, posts com delete/update restritos, comments). — Extra

Tudo acima verificado pelo usuário em dispositivo em 2026-07-06 (com as regras definitivas ativas).
