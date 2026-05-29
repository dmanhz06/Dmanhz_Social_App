package com.soulmate.app.ui.setting

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

@Composable
fun PrivacySecurityScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    // UI state for various toggles
    var isTwoFactorEnabled by remember { mutableStateOf(false) }
    var isActivityStatusPublic by remember { mutableStateOf(true) }
    var isPrivateAccount by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
                title = {
                    Text(
                        "Privacy & Security",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }
            )
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
                        onCheckedChange = { isTwoFactorEnabled = it },
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
                        onCheckedChange = { isActivityStatusPublic = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary)
                    )
                }
            )

            SecurityItem(
                icon = Icons.Default.Lock,
                title = "Private Account",
                description = "Only followers can see your posts and activity",
                trailing = {
                    Switch(
                        checked = isPrivateAccount,
                        onCheckedChange = { isPrivateAccount = it },
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

            // --- DANGER ZONE ---
            Divider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF4444).copy(alpha = 0.1f)),
                elevation = null
            ) {
                Text(text = "Delete Account", color = Color(0xFFFF4444), fontWeight = FontWeight.Bold)
            }
            
            Text(
                text = "Deleting your account is permanent and cannot be undone.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone. All your data will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { 
                    // authViewModel.deleteAccount() 
                    showDeleteDialog = false 
                }) {
                    Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
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
