package com.soulmate.app.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.soulmate.app.domain.model.User
import com.soulmate.app.domain.repository.IAuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : IAuthRepository {

    private val usersCollection = firestore.collection("users")
    private val postsCollection = firestore.collection("community_posts")

    override suspend fun register(name: String, email: String, password: String): Result<User> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw Exception("Không thể lấy user sau khi đăng ký")
        val uid = firebaseUser.uid
        val normalizedName = name.trim()
        val displayName = normalizedName.ifBlank { "User_${uid.take(5)}" }

        val profileUpdateRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        firebaseUser.updateProfile(profileUpdateRequest).await()

        val now = System.currentTimeMillis()
        val newUser = User(
            userId = uid,
            email = email,
            anonymousName = displayName,
            createdAt = now,
            updatedAt = now,
            lastLoginAt = now
        )

        usersCollection.document(uid).set(newUser).await()
        Result.success(newUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun login(email: String, password: String): Result<User> = try {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw Exception("Đăng nhập thất bại")
        val uid = firebaseUser.uid
        val now = System.currentTimeMillis()

        usersCollection.document(uid).update("lastLoginAt", now).await()

        val snapshot = usersCollection.document(uid).get().await()
        val user = snapshot.toObject(User::class.java) ?: throw Exception("Không tìm thấy profile")

        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val firebaseUser = authResult.user ?: throw Exception("Đăng nhập Google thất bại")
        val uid = firebaseUser.uid

        val snapshot = usersCollection.document(uid).get().await()
        val existingUser = snapshot.toObject(User::class.java)

        val user = if (existingUser == null) {
            User(
                userId = uid,
                email = firebaseUser.email ?: "",
                anonymousName = firebaseUser.displayName ?: "SoulMate User",
                avatarUrl = firebaseUser.photoUrl?.toString(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            ).also {
                usersCollection.document(uid).set(it).await()
            }
        } else {
            val updatedUser = existingUser.copy(
                anonymousName = firebaseUser.displayName ?: existingUser.anonymousName,
                avatarUrl = firebaseUser.photoUrl?.toString() ?: existingUser.avatarUrl,
                lastLoginAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            usersCollection.document(uid).set(updatedUser).await()
            updatedUser
        }

        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun logout(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let {
            User(
                userId = it.uid,
                email = it.email ?: "",
                anonymousName = it.displayName ?: "User",
                avatarUrl = it.photoUrl?.toString()
            )
        }
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun getUserProfile(uid: String): Result<User> = try {
        val snapshot = usersCollection.document(uid).get().await()
        val user = snapshot.toObject(User::class.java) ?: throw Exception("Không tìm thấy profile")
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> = try {
        // 1. Cập nhật profile chính trong collection 'users'
        usersCollection.document(user.userId).set(user).await()

        // 2. Đồng bộ: Cập nhật tên và ảnh trong tất cả các bài đăng (posts)
        val userPosts = postsCollection.whereEqualTo("user_id", user.userId).get().await()
        if (!userPosts.isEmpty) {
            firestore.runBatch { batch ->
                userPosts.documents.forEach { doc ->
                    batch.update(doc.reference, "user_name", user.anonymousName)
                    batch.update(doc.reference, "user_avatar_url", user.avatarUrl)
                }
            }.await()
        }

        // 3. Đồng bộ: Cập nhật tên và ảnh trong tất cả các bình luận (comments)
        // Sử dụng Collection Group để tìm comment của user này ở mọi bài post
        val userComments = firestore.collectionGroup("comments")
            .whereEqualTo("user_id", user.userId)
            .get()
            .await()
        if (!userComments.isEmpty) {
            firestore.runBatch { batch ->
                userComments.documents.forEach { doc ->
                    batch.update(doc.reference, "user_name", user.anonymousName)
                    batch.update(doc.reference, "user_avatar_url", user.avatarUrl)
                }
            }.await()
        }

        // 4. Cập nhật Firebase Auth DisplayName & Photo
        auth.currentUser?.let { firebaseUser ->
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(user.anonymousName)
                .setPhotoUri(user.avatarUrl?.let { Uri.parse(it) })
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
