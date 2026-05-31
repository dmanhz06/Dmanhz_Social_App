package com.soulmate.app.ui.chat

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.soulmate.app.R
import com.soulmate.app.domain.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatDetailScreen(
    userId: String,
    userName: String,
    userAvatarUrl: String?,
    chatViewModel: ChatViewModel,
    onBackClick: () -> Unit
) {
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val messages by chatViewModel.messages.collectAsState()
    val replyingTo by chatViewModel.replyingTo
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<ChatMessage?>(null) }

    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var showInfoScreen by remember { mutableStateOf(false) }
    var showMediaView by remember { mutableStateOf(false) }
    var showProfilePopup by remember { mutableStateOf(false) }

    // Search states
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Mute state
    var isMuted by remember { mutableStateOf(false) }

    // Thông tin người chat cùng (lấy realtime để đồng bộ nếu họ đổi profile)
    var currentOtherUserName by remember { mutableStateOf(userName) }
    var currentOtherUserAvatar by remember { mutableStateOf(userAvatarUrl) }
    var isOnline by remember { mutableStateOf(false) }
    var lastSeenTime by remember { mutableStateOf<Long?>(null) }

    DisposableEffect(userId) {
        if (userId.isEmpty()) return@DisposableEffect onDispose {}
        val listener = FirebaseFirestore.getInstance().collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    currentOtherUserName = snapshot.getString("anonymousName") ?: userName
                    currentOtherUserAvatar = snapshot.getString("avatarUrl") ?: userAvatarUrl
                    isOnline = snapshot.getBoolean("isOnline") ?: false
                    lastSeenTime = snapshot.getTimestamp("lastSeen")?.toDate()?.time 
                                   ?: snapshot.getLong("lastLoginAt")
                }
            }
        onDispose { listener.remove() }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            chatViewModel.sendImageMessage(currentUserId, userId, it)
        }
    }

    LaunchedEffect(userId) {
        if (currentUserId.isNotEmpty() && userId.isNotEmpty()) {
            chatViewModel.loadMessages(currentUserId, userId)
            chatViewModel.markAsRead(currentUserId, userId)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && !isSearchMode) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible && messages.isNotEmpty() && !isSearchMode) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Filtered messages logic
    val filteredMessages = remember(messages, searchQuery, isSearchMode) {
        if (!isSearchMode || searchQuery.isEmpty()) messages
        else messages.filter { it.messageText.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            backgroundColor = Color.Black,
            topBar = {
                if (isSearchMode) {
                    TopAppBar(
                        backgroundColor = Color.Black,
                        elevation = 1.dp,
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSearchMode = false 
                                searchQuery = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Search", tint = Color(0xFF0084FF))
                            }
                        },
                        title = {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Tìm kiếm tin nhắn...", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    textColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                                singleLine = true
                            )
                        }
                    )
                } else {
                    TopAppBar(
                        backgroundColor = Color.Black,
                        elevation = 1.dp,
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF0084FF)
                                )
                            }
                        },
                        title = {
                            val statusData = getActivityStatus(isOnline, lastSeenTime)
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { showInfoScreen = true }
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    AsyncImage(
                                        model = currentOtherUserAvatar ?: R.drawable.ava,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.DarkGray),
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(R.drawable.ava)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(statusData.dotColor)
                                            .border(1.dp, Color.Black, CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentOtherUserName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = statusData.text,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchMode = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF0084FF))
                            }
                            IconButton(onClick = { showInfoScreen = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFF0084FF))
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        state = listState
                    ) {
                        itemsIndexed(filteredMessages, key = { index, message ->
                            message.id.ifEmpty { "msg_$index" }
                        }) { index, message ->
                            val showHeader = remember(filteredMessages, index) {
                                if (index == 0) true
                                else {
                                    val current = filteredMessages[index].timestamp?.seconds ?: 0L
                                    val previous = filteredMessages[index - 1].timestamp?.seconds ?: 0L
                                    (current - previous) > 10 * 60
                                }
                            }

                            if (showHeader) {
                                Text(
                                    text = formatHeaderDate(message.timestamp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            MessageBubble(
                                message = message,
                                isMine = message.senderId == currentUserId,
                                userAvatarUrl = if (message.senderId == currentUserId) null else currentOtherUserAvatar,
                                otherUserName = currentOtherUserName,
                                showTime = index == filteredMessages.lastIndex || (index + 1 < filteredMessages.size && filteredMessages[index+1].senderId != message.senderId),
                                showAvatar = message.senderId != currentUserId,
                                onLongPress = {
                                    selectedMessage = message
                                    showOptionsSheet = true
                                },
                                onSwipeToReply = {
                                    chatViewModel.setReplyingTo(message)
                                },
                                onImageClick = { url ->
                                    fullScreenImageUrl = url
                                }
                            )
                        }
                    }

                    if (chatViewModel.isUploading.value) {
                        LinearProgressIndicator(
                            progress = chatViewModel.uploadProgress.value.toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF0084FF),
                            backgroundColor = Color(0xFF242526)
                        )
                    }

                    if (!isSearchMode) {
                        ChatBottomBar(
                            messageText = messageText,
                            onMessageChange = { messageText = it },
                            replyingTo = replyingTo,
                            onCancelReply = { chatViewModel.setReplyingTo(null) },
                            onSendClick = {
                                if (messageText.isNotBlank()) {
                                    chatViewModel.sendMessage(currentUserId, userId, messageText, replyTo = replyingTo)
                                    messageText = ""
                                }
                            },
                            onLikeClick = {
                                chatViewModel.sendMessage(currentUserId, userId, "👍")
                            },
                            onImageClick = {
                                imagePickerLauncher.launch("image/*")
                            }
                        )
                    }
                }

                if (showOptionsSheet) {
                    ModalOptions(
                        onDismiss = { showOptionsSheet = false },
                        onReply = {
                            chatViewModel.setReplyingTo(selectedMessage)
                            showOptionsSheet = false
                        },
                        onDelete = {
                            messageToDelete = selectedMessage
                            showDeleteDialog = true
                            showOptionsSheet = false
                        }
                    )
                }

                AnimatedVisibility(
                    visible = fullScreenImageUrl != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    fullScreenImageUrl?.let { url ->
                        FullScreenImageOverlay(imageUrl = url, onDismiss = { fullScreenImageUrl = null })
                    }
                }
            }
        }

        // Animated Info Screen (Messenger style)
        AnimatedVisibility(
            visible = showInfoScreen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            ChatInfoScreen(
                userName = currentOtherUserName,
                userAvatarUrl = currentOtherUserAvatar,
                isOnline = isOnline,
                lastSeenTime = lastSeenTime,
                isMuted = isMuted,
                onMuteToggle = { isMuted = it },
                onBackClick = { showInfoScreen = false },
                onSearchClick = {
                    showInfoScreen = false
                    isSearchMode = true
                },
                onShowMedia = {
                    showMediaView = true
                },
                onShowProfile = {
                    showProfilePopup = true
                },
                onDeleteConversation = {
                    chatViewModel.deleteConversation(currentUserId, userId)
                    showInfoScreen = false
                    onBackClick() // Thoát ra danh sách chat sau khi xóa
                }
            )
        }
        
        // Media View
        AnimatedVisibility(
            visible = showMediaView,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            val images = messages.filter { it.imageUrl != null }.map { it.imageUrl!! }.reversed()
            MediaGridView(
                images = images,
                onBackClick = { showMediaView = false },
                onImageClick = { fullScreenImageUrl = it }
            )
        }
        
        // Profile Popup
        if (showProfilePopup) {
            ProfilePopup(
                userName = currentOtherUserName,
                userAvatarUrl = currentOtherUserAvatar,
                onDismiss = { showProfilePopup = false }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            backgroundColor = Color(0xFF242526),
            contentColor = Color.White,
            title = { Text("Xóa tin nhắn?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa vĩnh viễn tin nhắn này không?") },
            confirmButton = {
                TextButton(onClick = {
                    messageToDelete?.let { chatViewModel.deleteMessage(it.id) }
                    showDeleteDialog = false
                    messageToDelete = null
                }) {
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    messageToDelete = null
                }) {
                    Text("Hủy", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ProfilePopup(userName: String, userAvatarUrl: String?, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color(0xFF242526),
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = userAvatarUrl ?: R.drawable.ava,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ava)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = userName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF0084FF))
                ) {
                    Text("Đóng", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MediaGridView(images: List<String>, onBackClick: () -> Unit, onImageClick: (String) -> Unit) {
    Scaffold(
        backgroundColor = Color.Black,
        topBar = {
            TopAppBar(
                backgroundColor = Color.Black,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                title = { Text("File phương tiện", color = Color.White, fontSize = 18.sp) }
            )
        }
    ) { paddingValues ->
        if (images.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Không có hình ảnh nào", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(1.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(images) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onImageClick(imageUrl) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInfoScreen(
    userName: String,
    userAvatarUrl: String?,
    isOnline: Boolean,
    lastSeenTime: Long?,
    isMuted: Boolean,
    onMuteToggle: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onShowMedia: () -> Unit,
    onShowProfile: () -> Unit,
    onDeleteConversation: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMuteConfirm by remember { mutableStateOf(false) }
    var showRestrictConfirm by remember { mutableStateOf(false) }
    var showBlockConfirm by remember { mutableStateOf(false) }
    var showReportConfirm by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    Scaffold(
        backgroundColor = Color.Black,
        topBar = {
            TopAppBar(
                backgroundColor = Color.Black,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                title = {}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Profile Info
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = userAvatarUrl ?: R.drawable.ava,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .clickable { onShowProfile() },
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ava)
                )
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF42B72A))
                            .border(3.dp, Color.Black, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.clickable { onShowProfile() }
            )
            
            val status = getActivityStatus(isOnline, lastSeenTime)
            Text(
                text = status.text,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoActionIcon(
                    icon = Icons.Default.Person, 
                    label = "Trang cá nhân",
                    onClick = onShowProfile
                )
                InfoActionIcon(
                    icon = if (isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications, 
                    label = if (isMuted) "Bật thông báo" else "Tắt thông báo",
                    onClick = { showMuteConfirm = true }
                )
                InfoActionIcon(
                    icon = Icons.Default.Search, 
                    label = "Tìm kiếm",
                    onClick = onSearchClick
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Options List
            InfoOptionItem(
                title = "Thông tin cá nhân", 
                icon = Icons.Default.Info,
                onClick = onShowProfile
            )
            InfoOptionItem(
                title = "Xem file phương tiện, file và liên kết", 
                icon = Icons.Default.Image,
                onClick = onShowMedia
            )
            InfoOptionItem(
                title = "Đi đến cuộc trò chuyện bí mật", 
                icon = Icons.Default.Lock,
                onClick = {
                    Toast.makeText(context, "Tính năng cuộc trò chuyện bí mật đang được phát triển", Toast.LENGTH_SHORT).show()
                }
            )
            
            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
            
            Text(
                text = "Quyền riêng tư & hỗ trợ",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            InfoOptionItem(
                title = "Xóa cuộc trò chuyện",
                icon = Icons.Default.Delete,
                color = Color.Red,
                onClick = { showDeleteConfirm = true }
            )
            InfoOptionItem(
                title = "Hạn chế", 
                icon = Icons.Default.Block, 
                color = Color.White,
                onClick = { showRestrictConfirm = true }
            )
            InfoOptionItem(
                title = "Chặn", 
                icon = Icons.Default.RemoveCircle, 
                color = Color.Red,
                onClick = { showBlockConfirm = true }
            )
            InfoOptionItem(
                title = "Báo cáo", 
                icon = Icons.Default.Warning, 
                color = Color.Red,
                onClick = { showReportConfirm = true }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Dialogs for Info screen actions
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            backgroundColor = Color(0xFF242526),
            contentColor = Color.White,
            title = { Text("Xóa toàn bộ cuộc trò chuyện?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn sẽ không thể hoàn tác sau khi xóa bản sao của cuộc trò chuyện này.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteConversation()
                    showDeleteConfirm = false
                }) {
                    Text("XÓA", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("HỦY", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showMuteConfirm) {
        AlertDialog(
            onDismissRequest = { showMuteConfirm = false },
            backgroundColor = Color(0xFF242526),
            contentColor = Color.White,
            title = { Text(if (isMuted) "Bật thông báo?" else "Tắt thông báo?", fontWeight = FontWeight.Bold) },
            text = { Text(if (isMuted) "Bạn sẽ nhận được thông báo khi có tin nhắn mới từ người này." else "Bạn sẽ không nhận được thông báo khi có tin nhắn mới từ cuộc trò chuyện này.") },
            confirmButton = {
                TextButton(onClick = {
                    onMuteToggle(!isMuted)
                    Toast.makeText(context, if (!isMuted) "Đã tắt thông báo" else "Đã bật thông báo", Toast.LENGTH_SHORT).show()
                    showMuteConfirm = false
                }) {
                    Text(if (isMuted) "BẬT" else "TẮT", color = Color(0xFF0084FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMuteConfirm = false }) {
                    Text("HỦY", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showRestrictConfirm) {
        AlertDialog(
            onDismissRequest = { showRestrictConfirm = false },
            backgroundColor = Color(0xFF242526),
            contentColor = Color.White,
            title = { Text("Hạn chế $userName?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn sẽ ẩn bớt các hoạt động với người này. Họ sẽ không biết bạn đã đọc tin nhắn hay chưa.") },
            confirmButton = {
                TextButton(onClick = {
                    Toast.makeText(context, "Đã hạn chế $userName", Toast.LENGTH_SHORT).show()
                    showRestrictConfirm = false
                }) {
                    Text("HẠN CHẾ", color = Color(0xFF0084FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestrictConfirm = false }) {
                    Text("HỦY", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showBlockConfirm) {
        AlertDialog(
            onDismissRequest = { showBlockConfirm = false },
            backgroundColor = Color(0xFF242526),
            contentColor = Color.White,
            title = { Text("Chặn $userName?", fontWeight = FontWeight.Bold) },
            text = { Text("Người này sẽ không thể nhắn tin hay gọi điện cho bạn nữa.") },
            confirmButton = {
                TextButton(onClick = {
                    Toast.makeText(context, "Đã chặn $userName", Toast.LENGTH_SHORT).show()
                    showBlockConfirm = false
                }) {
                    Text("CHẶN", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirm = false }) {
                    Text("HỦY", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showReportConfirm) {
        AlertDialog(
            onDismissRequest = { showReportConfirm = false },
            backgroundColor = Color(0xFF242526),
            contentColor = Color.White,
            title = { Text("Báo cáo $userName?", fontWeight = FontWeight.Bold) },
            text = { Text("Chúng tôi sẽ xem xét báo cáo của bạn về tài khoản này.") },
            confirmButton = {
                TextButton(onClick = {
                    Toast.makeText(context, "Đã gửi báo cáo", Toast.LENGTH_SHORT).show()
                    showReportConfirm = false
                }) {
                    Text("BÁO CÁO", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportConfirm = false }) {
                    Text("HỦY", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun InfoActionIcon(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF242526)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
fun InfoOptionItem(
    title: String,
    icon: ImageVector,
    color: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = color, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (color == Color.White) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun FullScreenImageOverlay(imageUrl: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onDismiss)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Full Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .statusBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun ModalOptions(onDismiss: () -> Unit, onReply: () -> Unit, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.width(200.dp).clickable(enabled = false) { },
            backgroundColor = Color(0xFF242526),
            elevation = 8.dp
        ) {
            Column {
                TextButton(
                    onClick = onReply,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text("Trả lời", color = Color.White)
                    }
                }
                Divider(color = Color.Gray.copy(alpha = 0.2f))
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        Spacer(Modifier.width(12.dp))
                        Text("Xóa tin nhắn", color = Color.Red)
                    }
                }
            }
        }
    }
}

private fun formatHeaderDate(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("d 'THG' M 'LÚC' HH:mm", Locale("vi", "VN"))
    return sdf.format(date).uppercase()
}

private fun getActivityStatus(isOnline: Boolean, lastSeen: Long?): ActivityStatus {
    if (isOnline) return ActivityStatus("Đang hoạt động", Color(0xFF42B72A))
    if (lastSeen == null) return ActivityStatus("Ngoại tuyến", Color.Gray)
    
    val now = System.currentTimeMillis()
    val diff = now - lastSeen
    
    return if (diff < 60000) {
        ActivityStatus("Vừa mới hoạt động", Color(0xFF42B72A))
    } else {
        val timeAgo = when {
            diff < 3600000 -> "${diff / 60000} phút trước"
            diff < 86400000 -> "${diff / 3600000} giờ trước"
            else -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.format(Date(lastSeen))
            }
        }
        ActivityStatus("Hoạt động $timeAgo", Color.Gray)
    }
}

data class ActivityStatus(val text: String, val dotColor: Color)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    userAvatarUrl: String?,
    otherUserName: String,
    showTime: Boolean,
    showAvatar: Boolean,
    onLongPress: () -> Unit,
    onSwipeToReply: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val timeString = remember(message.timestamp) {
        if (message.timestamp != null) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp.toDate())
        } else ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (offsetX.value + dragAmount).coerceIn(-120f, 0f)
                        scope.launch {
                            offsetX.snapTo(newOffset)
                        }
                    },
                    onDragEnd = {
                        if (offsetX.value <= -90f) {
                            onSwipeToReply()
                        }
                        scope.launch {
                            offsetX.animateTo(0f, animationSpec = spring())
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            offsetX.animateTo(0f)
                        }
                    }
                )
            }
    ) {
        if (offsetX.value < 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = null,
                    tint = if (offsetX.value <= -90f) Color(0xFF0084FF) else Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            alpha = (offsetX.value / -90f).coerceIn(0f, 1f)
                            scaleX = (offsetX.value / -90f).coerceIn(0.5f, 1f)
                            scaleY = (offsetX.value / -90f).coerceIn(0.5f, 1f)
                        }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = offsetX.value }
                .padding(vertical = 1.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            if (message.replyToId != null) {
                val replyName = if (message.replyToName == "Bạn") "bạn" else otherUserName
                Row(
                    modifier = Modifier.padding(
                        start = if (isMine) 0.dp else 36.dp,
                        end = if (isMine) 8.dp else 0.dp,
                        bottom = 4.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isMine) "Bạn đã trả lời $replyName" else "$otherUserName đã trả lời bạn",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
            ) {
                if (!isMine) {
                    if (showAvatar) {
                        AsyncImage(
                            model = userAvatarUrl ?: R.drawable.ava,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ava)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
                    if (message.replyToImageUrl != null) {
                        AsyncImage(
                            model = message.replyToImageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .width(150.dp)
                                .height(100.dp)
                                .padding(bottom = 4.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (message.replyToText != null) {
                        Surface(
                            modifier = Modifier
                                .padding(bottom = 2.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            color = Color(0xFF242526)
                        ) {
                            Text(
                                text = message.replyToText,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                    ) {
                        if (message.imageUrl != null) {
                            AsyncImage(
                                model = message.imageUrl,
                                contentDescription = "Image message",
                                modifier = Modifier
                                    .widthIn(max = 210.dp)
                                    .padding(bottom = 4.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .combinedClickable(
                                        onClick = { onImageClick(message.imageUrl) },
                                        onLongClick = onLongPress
                                    ),
                                contentScale = ContentScale.Fit
                            )
                        }

                        if (message.messageText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 18.dp,
                                            topEnd = 18.dp,
                                            bottomStart = if (isMine) 18.dp else 4.dp,
                                            bottomEnd = if (isMine) 4.dp else 18.dp
                                        )
                                    )
                                    .background(if (isMine) Color(0xFF0084FF) else Color(0xFF242526))
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = onLongPress
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = message.messageText,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            if (showTime && timeString.isNotEmpty()) {
                Text(
                    text = timeString,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(
                        top = 2.dp,
                        start = if (isMine) 0.dp else 36.dp,
                        end = if (isMine) 4.dp else 0.dp
                    )
                )
            }
        }
    }
}

@Composable
fun ChatBottomBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    replyingTo: ChatMessage?,
    onCancelReply: () -> Unit,
    onSendClick: () -> Unit,
    onLikeClick: () -> Unit,
    onImageClick: () -> Unit
) {
    Surface(
        elevation = 8.dp,
        color = Color.Black
    ) {
        Column {
            if (replyingTo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF242526))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Đang trả lời tin nhắn",
                            fontSize = 12.sp,
                            color = Color(0xFF0084FF),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (replyingTo.messageText.isNotBlank()) replyingTo.messageText else "Hình ảnh",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (replyingTo.imageUrl != null) {
                        AsyncImage(
                            model = replyingTo.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(start = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .background(Color.Black),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0084FF))
                }
                IconButton(onClick = onImageClick) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF0084FF))
                }

                TextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    placeholder = {
                        Text(
                            text = "Nhập tin nhắn...",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.White,
                        backgroundColor = Color(0xFF242526),
                        cursorColor = Color.White,
                        placeholderColor = Color.Gray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = null,
                                tint = Color(0xFF0084FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(4.dp))

                if (messageText.isBlank()) {
                    IconButton(onClick = onLikeClick) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF0084FF))
                    }
                } else {
                    IconButton(onClick = onSendClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi",
                            tint = Color(0xFF0084FF)
                        )
                    }
                }
            }
        }
    }
}
