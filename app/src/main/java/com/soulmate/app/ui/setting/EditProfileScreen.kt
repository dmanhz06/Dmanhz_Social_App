package com.soulmate.app.ui.setting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.soulmate.app.domain.model.User
import java.util.Locale

@Composable
fun EditProfileDialog(
    user: User,
    isGoogleUser: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (User, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(user.anonymousName) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phoneNumber) }
    var gender by remember { mutableStateOf(user.gender) }
    var socialLinks by remember { mutableStateOf(user.socialMedias) }
    
    var expandedGender by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "Other", "Secret")

    var showLinkDialog by remember { mutableStateOf(false) }
    var linkInput by remember { mutableStateOf("") }
    var editLinkIndex by remember { mutableStateOf<Int?>(null) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    // Quy tắc: Nếu là Google user thì không được sửa Avatar, Name, Email, Phone, Gender
    val canEditGeneral = !isGoogleUser

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // --- HEADER ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Edit Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. AVATAR SECTION
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clickable(enabled = !isLoading && canEditGeneral) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        val imageToShow = selectedImageUri ?: user.avatarUrl

                        if (imageToShow != null && imageToShow.toString().isNotBlank()) {
                            AsyncImage(
                                model = imageToShow,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colors.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.firstOrNull()?.toString()?.uppercase(Locale.getDefault()) ?: "U",
                                    color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        if (canEditGeneral) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colors.surface).padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, "Edit", tint = MaterialTheme.colors.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (isGoogleUser) {
                        Text(
                            text = "Linked with Google Account",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (canEditGeneral) email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && canEditGeneral,
                        leadingIcon = { Icon(Icons.Default.Email, null) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (canEditGeneral) name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && canEditGeneral,
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (canEditGeneral) phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && canEditGeneral,
                        leadingIcon = { Icon(Icons.Default.Phone, null) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gender Field
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            label = { Text("Gender") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = !isLoading && canEditGeneral,
                            leadingIcon = { Icon(Icons.Default.Wc, null) },
                            trailingIcon = {
                                if (canEditGeneral) {
                                    IconButton(onClick = { expandedGender = !expandedGender }) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                }
                            }
                        )
                        if (canEditGeneral && !isLoading) {
                            Spacer(
                                modifier = Modifier.matchParentSize().clickable { expandedGender = true }
                            )
                        }
                        DropdownMenu(expanded = expandedGender, onDismissRequest = { expandedGender = false }) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(onClick = { gender = option; expandedGender = false }) {
                                    Text(text = option)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- SOCIAL LINKS --- (Google users CAN edit this)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Social Links", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { editLinkIndex = null; linkInput = ""; showLinkDialog = true }) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Link")
                        }
                    }

                    socialLinks.forEachIndexed { index, link ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = 0.dp,
                            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Link, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = link, modifier = Modifier.weight(1f), maxLines = 1)
                                IconButton(onClick = { editLinkIndex = index; linkInput = link; showLinkDialog = true }) {
                                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colors.primary, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { socialLinks = socialLinks.toMutableList().apply { removeAt(index) } }) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val updatedUser = user.copy(
                            anonymousName = name,
                            email = email,
                            phoneNumber = phone,
                            gender = gender,
                            socialMedias = socialLinks,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(updatedUser, selectedImageUri)
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        if (showLinkDialog) {
            AlertDialog(
                onDismissRequest = { showLinkDialog = false },
                title = { Text(if (editLinkIndex == null) "Add Social Link" else "Edit Social Link") },
                text = {
                    OutlinedTextField(
                        value = linkInput,
                        onValueChange = { linkInput = it },
                        placeholder = { Text("https://facebook.com/...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (linkInput.isNotBlank()) {
                            val newList = socialLinks.toMutableList()
                            if (editLinkIndex == null) newList.add(linkInput.trim())
                            else newList[editLinkIndex!!] = linkInput.trim()
                            socialLinks = newList
                        }
                        showLinkDialog = false
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { showLinkDialog = false }) { Text("Cancel", color = Color.Gray) }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
