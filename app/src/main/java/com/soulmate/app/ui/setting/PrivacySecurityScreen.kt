package com.soulmate.app.ui.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soulmate.app.ui.login.AuthViewModel

enum class FeatureType {
    TWO_FACTOR, ACTIVITY_STATUS, NONE
}

@Composable
fun PrivacySecurityScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    // UI state cho các nút Switch chính gốc
    var isTwoFactorEnabled by remember { mutableStateOf(false) }
    var isActivityStatusPublic by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // State quản lý việc hiển thị Dialog xác nhận bật/tắt tính năng
    var activeToggleFeature by remember { mutableStateOf(FeatureType.NONE) }
    var pendingToggleValue by remember { mutableStateOf(false) }

    // Định nghĩa bảng màu đỏ cảnh báo chuẩn UI hiện đại
    val dangerRed = Color(0xFFE53935)
    val dangerBg = Color(0xFFFFEBEE)

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.background,
                    elevation = 0.dp,
                    title = {
                        Text(
                            text = "Privacy & Security",
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colors.primary,
                            )
                        }
                    }
                )
            }
        },
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- SECURITY SECTION ---
            PrivacySectionTitle("Security")

            SecurityItem(
                icon = Icons.Default.VpnKey,
                title = "Change Password",
                description = "Update your password regularly to stay secure",
                onClick = { /* Implement Change Password Logic */ }
            )

            SecurityItem(
                icon = Icons.Default.PhonelinkLock,
                title = "Two-Factor Authentication",
                description = "Add an extra layer of security to your account",
                trailing = {
                    Switch(
                        checked = isTwoFactorEnabled,
                        onCheckedChange = { newValue ->
                            activeToggleFeature = FeatureType.TWO_FACTOR
                            pendingToggleValue = newValue
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary)
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- PRIVACY SECTION ---
            PrivacySectionTitle("Privacy")

            SecurityItem(
                icon = Icons.Default.Public,
                title = "Activity Status",
                description = "Allow others to see when you are active",
                trailing = {
                    Switch(
                        checked = isActivityStatusPublic,
                        onCheckedChange = { newValue ->
                            activeToggleFeature = FeatureType.ACTIVITY_STATUS
                            pendingToggleValue = newValue
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary)
                    )
                }
            )

            SecurityItem(
                icon = Icons.Default.Block,
                title = "Blocked Users",
                description = "Manage people you've blocked",
                onClick = { /* Navigate to Blocked Users List */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- DATA SECTION ---
            PrivacySectionTitle("Your Data")

            SecurityItem(
                icon = Icons.Default.CloudDownload,
                title = "Download Your Data",
                description = "Get a copy of the information you've shared",
                onClick = { /* Implement Data Download */ }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- MODERN DANGER ZONE ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, dangerRed.copy(alpha = 0.4f)),
                backgroundColor = dangerBg.copy(alpha = 0.1f),
                elevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header của Danger Zone
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Danger",
                            tint = dangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DANGER ZONE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = dangerRed,
                            letterSpacing = 1.sp
                        )
                    }

                    // Nội dung mô tả sự nghiêm trọng
                    Text(
                        text = "Deleting your account is permanent. This will erase all of your profile data, chat logs, settings, and media content completely. This action cannot be undone.",
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp, start = 2.dp)
                    )

                    // Nút thực thi hành động xóa tài khoản hiện đại
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = dangerRed,
                            contentColor = Color.White
                        ),
                        elevation = null
                    ) {
                        Text(
                            text = "Delete Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- DIALOG XÁC NHẬN BẬT/TẮT TÍNH NĂNG ---
    if (activeToggleFeature != FeatureType.NONE) {
        val actionText = if (pendingToggleValue) "turn on" else "turn off"
        val featureName = when (activeToggleFeature) {
            FeatureType.TWO_FACTOR -> "Two-Factor Authentication"
            FeatureType.ACTIVITY_STATUS -> "Activity Status"
            else -> ""
        }

        AlertDialog(
            onDismissRequest = { activeToggleFeature = FeatureType.NONE },
            title = { Text("Confirm Change", fontWeight = FontWeight.Bold) },
            text = { Text("Do you want to $actionText $featureName?") },
            confirmButton = {
                TextButton(onClick = {
                    when (activeToggleFeature) {
                        FeatureType.TWO_FACTOR -> isTwoFactorEnabled = pendingToggleValue
                        FeatureType.ACTIVITY_STATUS -> isActivityStatusPublic = pendingToggleValue
                        else -> {}
                    }
                    activeToggleFeature = FeatureType.NONE
                }) {
                    Text("CONFIRM", color = MaterialTheme.colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeToggleFeature = FeatureType.NONE }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- DIALOG XÓA TÀI KHOẢN ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Permanently delete account?", fontWeight = FontWeight.Bold, color = dangerRed) },
            text = { Text("Are you absolutely sure? All your personal info, matches, and media will vanish forever.") },
            confirmButton = {
                TextButton(onClick = {
                    // authViewModel.deleteAccount()
                    showDeleteDialog = false
                }) {
                    Text("YES, DELETE IT", color = dangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun PrivacySectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colors.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
    )
}

@Composable
fun SecurityItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        }
    }
}