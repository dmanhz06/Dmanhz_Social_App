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
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.soulmate.app.R
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
    
    // Facebook and GitHub links
    var facebookLink by remember { mutableStateOf(user.socialMedias.getOrNull(0) ?: "") }
    var githubLink by remember { mutableStateOf(user.socialMedias.getOrNull(1) ?: "") }
    
    var expandedGender by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "Other", "Secret")

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    // Google users cannot change Avatar, Name, or Email
    val canEditIdentity = !isGoogleUser

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
                            .clickable(enabled = !isLoading && canEditIdentity) {
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
                        
                        if (canEditIdentity) {
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
                            text = "Linked with Google Account\nName, Email and Avatar are locked",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (canEditIdentity) email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && canEditIdentity,
                        leadingIcon = { Icon(Icons.Default.Email, null) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (canEditIdentity) name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && canEditIdentity,
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Field - ALWAYS EDITABLE
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        leadingIcon = { Icon(Icons.Default.Phone, null) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gender Field - ALWAYS EDITABLE
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            label = { Text("Gender") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = !isLoading,
                            leadingIcon = { Icon(Icons.Default.Wc, null) },
                            trailingIcon = {
                                IconButton(onClick = { expandedGender = !expandedGender }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        if (!isLoading) {
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

                    // --- SOCIAL LINKS ---
                    Text(
                        text = "Social Links",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Facebook Input
                    OutlinedTextField(
                        value = facebookLink,
                        onValueChange = { facebookLink = it },
                        label = { Text("Facebook Profile Link") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.facebook),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        },
                        placeholder = { Text("https://facebook.com/...") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // GitHub Input
                    OutlinedTextField(
                        value = githubLink,
                        onValueChange = { githubLink = it },
                        label = { Text("GitHub Profile Link") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        leadingIcon = {
                            Icon(Icons.Default.Code, contentDescription = null)
                        },
                        placeholder = { Text("https://github.com/...") }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val updatedUser = user.copy(
                            anonymousName = name,
                            email = email,
                            phoneNumber = phone,
                            gender = gender,
                            socialMedias = listOf(facebookLink.trim(), githubLink.trim()),
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
    }
}
