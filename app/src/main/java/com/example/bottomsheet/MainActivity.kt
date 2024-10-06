package com.example.bottomsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.wait

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            ContactApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactApp() {
    var contacts = remember {
        mutableStateListOf<Contact>()
    }
    var showBottomSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var editContactIndex by remember { mutableIntStateOf(-1) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Contact List Application",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = Color(0xFF03DAC5),
                contentColor = Color.White
            ) {

                Icon(Icons.Filled.Add, contentDescription = "Add Contact")
            }
        },

        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }


    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            if (contacts.isEmpty()) {
                Text(
                    text = "No contacts yet. Click '+' to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(24.dp)
                )
            } else {
                contacts.forEachIndexed { index, contect ->
                    ContactItem(
                        contact = contect,
                        onClick = {
                            editContactIndex = index
                            showBottomSheet = true
                        }
                    )

                }
            }


        }

    }


    if (showBottomSheet) {
        val contact = if (editContactIndex >= 0) contacts[editContactIndex] else Contact()
        ContantBottomSheet(contact = contact,
            onConfirm = { updatedContact ->
                contacts = if (editContactIndex >= 0) {
                    contacts.apply {
                        this[editContactIndex] = updatedContact
                    }
                } else {
                    contacts.apply {
                        this.add(updatedContact)
                    }
                }



                CoroutineScope(Dispatchers.Main).launch {
                    snackbarHostState.showSnackbar("Contact ${if (editContactIndex >= 0) "updated" else "added"} successfully")
                }

                editContactIndex = -1
                showBottomSheet = false

            },

            onDelete = {
                showDeleteConfirmation = true
            },
            onDismiis = {
                editContactIndex = -1
                showBottomSheet = false
            }
        )

    }


    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Contact") },
            text = { Text("Are you sure you want to delete this contact?") },
            confirmButton = {
                Button(onClick = {

                    contacts = contacts.apply {
                        if (editContactIndex >= 0) {
                            removeAt(editContactIndex)
                        }
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar("Contact deleted successfully")
                    }
                    editContactIndex = -1
                    showDeleteConfirmation = false
                    showBottomSheet = false
                }) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContantBottomSheet(
    contact: Contact,
    onConfirm: (Contact) -> Unit,
    onDismiis: () -> Unit,
    onDelete: () -> Unit,
) {
    var name by remember { mutableStateOf(TextFieldValue(contact.name)) }
    var phone by remember { mutableStateOf(TextFieldValue(contact.phone)) }
    var address by remember { mutableStateOf(TextFieldValue(contact.address)) }
    var email by remember { mutableStateOf(TextFieldValue(contact.email)) }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    val phoneRegex = Regex("^[0-9]*$")
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiis,
        sheetState = sheetState,

        ) {
        Box(
            Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
                .height(400.dp), contentAlignment = Alignment.TopCenter
        ) {

            Column {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = name.text.isBlank() },
                    label = { Text(text = "Name") },
                    isError = nameError,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                if (nameError) {
                    Text("Name cannot be empty", color = Color.Red, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it; phoneError = !phoneRegex.matches(phone.text)
                    },
                    label = { Text("Phone") },
                    isError = phoneError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Phone
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                if (phoneError) {
                    Text(
                        "Phone must contain only digits",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        //???
                        email = it; emailError = !emailRegex.matches(email.text)
                    },
                    label = { Text("Email") },
                    isError = emailError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Email
                    ),

                    keyboardActions = KeyboardActions(
                        onDone = {
                            nameError = name.text.isBlank()
                            phoneError = !phoneRegex.matches(phone.text)
                            emailError = !emailRegex.matches(email.text)

                            if (!nameError && !phoneError && !emailError) {
                                onConfirm(
                                    Contact(
                                        name.text,
                                        phone.text,
                                        address.text,
                                        email.text,
                                        imageUrl = "https://i.pinimg.com/736x/5b/48/a4/5b48a414c78473a908090f05ee6b5d7c.jpg"
                                    )
                                )
                            }
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                if (emailError) {
                    Text("Invalid email format", color = Color.Red, fontSize = 12.sp)
                }

                Row {
                    Button(
                        onClick = {
                            nameError = name.text.isBlank()
                            phoneError = !phoneRegex.matches(phone.text)
                            emailError = !emailRegex.matches(email.text)

                            if (!nameError && !phoneError && !emailError) {
                                onConfirm(
                                    Contact(
                                        name.text,
                                        phone.text,
                                        address.text,
                                        email.text,
                                        imageUrl = "https://i.pinimg.com/736x/5b/48/a4/5b48a414c78473a908090f05ee6b5d7c.jpg"
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Text("Confirm", color = Color.White)
                    }
                    Row {
                        if (contact.name.isNotEmpty()) {
                            TextButton(onClick = onDelete) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Contact",
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", color = Color.Red)
                            }
                        }
                        TextButton(onClick = onDismiis) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }

                }
            }


        }
    }

}


@Composable
fun ContactItem(contact: Contact, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(contact.imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp, Color.Black,
                        CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF6200EE)
                )
                Text(text = "Phone: ${contact.phone}", color = Color.Gray)
                Text(text = "Address: ${contact.address}", color = Color.Gray)
                Text(text = "Email: ${contact.email}", color = Color.Gray)
            }
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Contact",
                    tint = Color.Gray
                )
            }

        }

    }

}

data class Contact(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val imageUrl: String = ""
)