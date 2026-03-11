package com.example.spendsense.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    
    var name by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("john.doe@example.com") }
    var phone by remember { mutableStateOf("+1 234 567 890") }
    var location by remember { mutableStateOf("New York, USA") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Profile" else "Profile") },
                navigationIcon = {
                    IconButton(onClick = if (isEditing) { { isEditing = false } } else onBack) {
                        Icon(if (isEditing) Icons.Default.Close else Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { isEditing = false }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { photoPickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isEditing) {
                Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = email, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(label = "Wallet", value = "$4,445", icon = Icons.Default.AccountBalanceWallet)
                    StatBox(label = "Transactions", value = "128", icon = Icons.Default.ReceiptLong)
                    StatBox(label = "Points", value = "850", icon = Icons.Default.Star)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isEditing) {
                EditProfileField(label = "Full Name", value = name, onValueChange = { name = it }, icon = Icons.Default.Badge)
                EditProfileField(label = "Email", value = email, onValueChange = { email = it }, icon = Icons.Default.Email)
                EditProfileField(label = "Phone", value = phone, onValueChange = { phone = it }, icon = Icons.Default.Phone)
                EditProfileField(label = "Location", value = location, onValueChange = { location = it }, icon = Icons.Default.LocationOn)
            } else {
                Text(
                    text = "Account Information",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                ProfileDisplayItem(label = "Full Name", value = name, icon = Icons.Default.Badge)
                ProfileDisplayItem(label = "Email", value = email, icon = Icons.Default.Email)
                ProfileDisplayItem(label = "Phone", value = phone, icon = Icons.Default.Phone)
                ProfileDisplayItem(label = "Location", value = location, icon = Icons.Default.LocationOn)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Connected Apps",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                SocialItem(label = "Google", icon = Icons.Default.Link, isConnected = true)
                SocialItem(label = "Facebook", icon = Icons.Default.Link, isConnected = false)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEditing) {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun SocialItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isConnected: Boolean) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Text(
                text = if (isConnected) "Connected" else "Connect",
                color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        },
        modifier = Modifier.clickable { }
    )
}

@Composable
fun EditProfileField(label: String, value: String, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true
    )
}

@Composable
fun ProfileDisplayItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
}
