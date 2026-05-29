package com.soulmate.app.ui.login

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soulmate.app.domain.model.User
import com.soulmate.app.domain.repository.IAuthRepository
import com.soulmate.app.utils.CloudinaryHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _authSuccess = MutableSharedFlow<Unit>()
    val authSuccess = _authSuccess.asSharedFlow()
    
    private val _currentUser = mutableStateOf<User?>(null)
    val currentUser: State<User?> = _currentUser

    init {
        observeCurrentUser()
    }

    // Lắng nghe thay đổi profile realtime từ repository
    private fun observeCurrentUser() {
        val uid = authRepository.getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                authRepository.observeUserProfile(uid).collectLatest { user ->
                    _currentUser.value = user
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.login(email, password)
                .onSuccess {
                    _currentUser.value = it
                    _authSuccess.emit(Unit)
                    observeCurrentUser() // Bắt đầu lắng nghe sau khi login
                }
                .onFailure { _error.emit(it.localizedMessage ?: "Đăng nhập thất bại") }
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String, confirmPass: String) {
        if (password != confirmPass) {
            viewModelScope.launch { _error.emit("Mật khẩu xác nhận không khớp") }
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.register(name = name.trim(), email = email, password = password)
                .onSuccess {
                    _currentUser.value = it
                    _authSuccess.emit(Unit)
                    observeCurrentUser()
                }
                .onFailure { _error.emit(it.localizedMessage ?: "Đăng ký thất bại") }
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.signInWithGoogle(idToken)
                .onSuccess {
                    _currentUser.value = it
                    _authSuccess.emit(Unit)
                    observeCurrentUser()
                }
                .onFailure { _error.emit(it.localizedMessage ?: "Đăng nhập Google thất bại") }
            _isLoading.value = false
        }
    }
    
    fun logout() {
        authRepository.logout()
        _currentUser.value = null
    }

    fun updateProfileWithImage(updatedUser: User, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalUser = updatedUser
                if (imageUri != null) {
                    val downloadUrl = CloudinaryHelper.uploadImageSuspend(imageUri)
                    finalUser = finalUser.copy(avatarUrl = downloadUrl)
                }
                // Lưu lên Firestore - Các flow đang lắng nghe sẽ tự động nhận data mới
                authRepository.updateUserProfile(finalUser)
                    .onFailure { _error.emit(it.localizedMessage ?: "Cập nhật thất bại") }
            } catch (e: Exception) {
                _error.emit("Lỗi tải ảnh. Vui lòng thử lại.")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
