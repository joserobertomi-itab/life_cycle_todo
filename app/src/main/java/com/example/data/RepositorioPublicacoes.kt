package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Uma publicação do feed, lida da coleção "posts" do Firestore. */
data class Publicacao(
    val id: String = "",
    val uid: String = "",
    val nomeAutor: String = "",
    val texto: String = "",
    val imagemBase64: String = "",
    val curtidas: List<String> = emptyList(), // uids de quem curtiu
)

/** Um comentário da subcoleção "comments" de uma publicação. */
data class Comentario(
    val id: String = "",
    val uid: String = "",
    val nomeAutor: String = "",
    val texto: String = "",
)

/**
 * Repositório das publicações (coleção "posts" do Firestore).
 * A imagem é guardada em Base64 dentro do próprio documento porque o
 * Cloud Storage exige plano Blaze e a avaliação exige o plano Spark.
 */
class RepositorioPublicacoes(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    val uidAtual: String?
        get() = auth.currentUser?.uid

    /**
     * Feed em tempo real: emite a lista completa (mais nova primeiro)
     * a cada mudança na coleção, via addSnapshotListener.
     */
    fun observarPublicacoes(): Flow<List<Publicacao>> = callbackFlow {
        val registro = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { retrato, erro ->
                if (erro != null) {
                    close(erro)
                    return@addSnapshotListener
                }
                trySend(retrato?.documents.orEmpty().map(::paraPublicacao))
            }
        awaitClose { registro.remove() }
    }

    /**
     * Persiste a publicação em "posts" com os campos exigidos pelo enunciado:
     * imagem (Base64, no lugar da URL do Storage), texto, uid do autor e timestamp.
     * O nome do autor é desnormalizado no documento para o feed não precisar de join.
     */
    suspend fun publicar(texto: String, imagemBase64: String) {
        val uid = uidLogado()
        firestore.collection("posts").add(
            mapOf(
                "uid" to uid,
                "nomeAutor" to nomeDoUsuario(uid),
                "texto" to texto,
                "imagemBase64" to imagemBase64,
                "curtidas" to emptyList<String>(),
                "timestamp" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    /** Exclui uma publicação (a UI só oferece isso ao autor; a regra do Firestore reforça). */
    suspend fun excluir(idPublicacao: String) {
        firestore.collection("posts").document(idPublicacao).delete().await()
    }

    /** Curte/descurte: adiciona ou remove o uid do usuário na lista de curtidas. */
    suspend fun alternarCurtida(publicacao: Publicacao) {
        val uid = uidLogado()
        val operacao = if (uid in publicacao.curtidas) {
            FieldValue.arrayRemove(uid)
        } else {
            FieldValue.arrayUnion(uid)
        }
        firestore.collection("posts").document(publicacao.id)
            .update("curtidas", operacao)
            .await()
    }

    /** Comentários de uma publicação em tempo real, do mais antigo para o mais novo. */
    fun observarComentarios(idPublicacao: String): Flow<List<Comentario>> = callbackFlow {
        val registro = firestore.collection("posts").document(idPublicacao)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { retrato, erro ->
                if (erro != null) {
                    close(erro)
                    return@addSnapshotListener
                }
                val comentarios = retrato?.documents.orEmpty().map { documento ->
                    Comentario(
                        id = documento.id,
                        uid = documento.getString("uid") ?: "",
                        nomeAutor = documento.getString("nomeAutor") ?: "",
                        texto = documento.getString("texto") ?: "",
                    )
                }
                trySend(comentarios)
            }
        awaitClose { registro.remove() }
    }

    /** Adiciona um comentário na subcoleção "comments" da publicação. */
    suspend fun comentar(idPublicacao: String, texto: String) {
        val uid = uidLogado()
        firestore.collection("posts").document(idPublicacao)
            .collection("comments")
            .add(
                mapOf(
                    "uid" to uid,
                    "nomeAutor" to nomeDoUsuario(uid),
                    "texto" to texto,
                    "timestamp" to FieldValue.serverTimestamp(),
                ),
            ).await()
    }

    private fun uidLogado(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Nenhum usuário logado.")

    private suspend fun nomeDoUsuario(uid: String): String =
        firestore.collection("users").document(uid).get().await()
            .getString("nome") ?: "Anônimo"

    private fun paraPublicacao(documento: DocumentSnapshot) = Publicacao(
        id = documento.id,
        uid = documento.getString("uid") ?: "",
        nomeAutor = documento.getString("nomeAutor") ?: "",
        texto = documento.getString("texto") ?: "",
        imagemBase64 = documento.getString("imagemBase64") ?: "",
        curtidas = (documento.get("curtidas") as? List<*>)?.filterIsInstance<String>().orEmpty(),
    )
}
