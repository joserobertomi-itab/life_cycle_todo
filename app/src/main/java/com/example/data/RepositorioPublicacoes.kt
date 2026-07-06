package com.example.data

import com.google.firebase.auth.FirebaseAuth
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
                val publicacoes = retrato?.documents.orEmpty().map { documento ->
                    Publicacao(
                        id = documento.id,
                        uid = documento.getString("uid") ?: "",
                        nomeAutor = documento.getString("nomeAutor") ?: "",
                        texto = documento.getString("texto") ?: "",
                        imagemBase64 = documento.getString("imagemBase64") ?: "",
                    )
                }
                trySend(publicacoes)
            }
        awaitClose { registro.remove() }
    }

    /**
     * Persiste a publicação em "posts" com os campos exigidos pelo enunciado:
     * imagem (Base64, no lugar da URL do Storage), texto, uid do autor e timestamp.
     * O nome do autor é desnormalizado no documento para o feed não precisar de join.
     */
    suspend fun publicar(texto: String, imagemBase64: String) {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Nenhum usuário logado.")
        val perfil = firestore.collection("users").document(uid).get().await()
        val nomeAutor = perfil.getString("nome") ?: "Anônimo"
        firestore.collection("posts").add(
            mapOf(
                "uid" to uid,
                "nomeAutor" to nomeAutor,
                "texto" to texto,
                "imagemBase64" to imagemBase64,
                "timestamp" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }
}
