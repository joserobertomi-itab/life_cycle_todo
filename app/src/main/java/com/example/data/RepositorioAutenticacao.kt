package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositório de autenticação e perfil (camada Model do MVVM).
 * Encapsula o Firebase Auth e a coleção "users" do Firestore.
 * Os nomes das coleções ("users", "posts") são exigidos pelo enunciado e ficam em inglês.
 */
class RepositorioAutenticacao(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    /** Sessão persistida pelo próprio Firebase Auth: se há usuário, continua logado. */
    val estaLogado: Boolean
        get() = auth.currentUser != null

    val uidAtual: String?
        get() = auth.currentUser?.uid

    suspend fun entrar(email: String, senha: String) {
        auth.signInWithEmailAndPassword(email, senha).await()
    }

    /**
     * Cria a conta no Firebase Auth e, no mesmo fluxo, o documento de perfil
     * na coleção "users" (exigência do enunciado: uid, nome e e-mail no cadastro).
     */
    suspend fun cadastrar(nome: String, email: String, senha: String) {
        val resultado = auth.createUserWithEmailAndPassword(email, senha).await()
        val uid = resultado.user?.uid
            ?: throw IllegalStateException("Cadastro não retornou um usuário válido.")
        firestore.collection("users").document(uid)
            .set(mapOf("uid" to uid, "nome" to nome, "email" to email))
            .await()
    }

    /** Busca os dados do perfil do usuário logado na coleção "users". */
    suspend fun buscarPerfil(): Perfil {
        val uid = uidAtual ?: throw IllegalStateException("Nenhum usuário logado.")
        val documento = firestore.collection("users").document(uid).get().await()
        return Perfil(
            uid = uid,
            nome = documento.getString("nome") ?: "",
            email = documento.getString("email") ?: "",
            fotoBase64 = documento.getString("fotoBase64") ?: "",
        )
    }

    /**
     * Extra do PDF (edição de perfil): atualiza o nome e, se enviada,
     * a foto (Base64 — mesma decisão do Storage indisponível no Spark).
     */
    suspend fun atualizarPerfil(nome: String, fotoBase64: String?) {
        val uid = uidAtual ?: throw IllegalStateException("Nenhum usuário logado.")
        val mudancas = buildMap {
            put("nome", nome)
            if (fotoBase64 != null) put("fotoBase64", fotoBase64)
        }
        firestore.collection("users").document(uid).update(mudancas).await()
    }

    fun sair() {
        auth.signOut()
    }
}

/** Dados do perfil salvos no Firestore no momento do cadastro. */
data class Perfil(
    val uid: String = "",
    val nome: String = "",
    val email: String = "",
    val fotoBase64: String = "",
)
