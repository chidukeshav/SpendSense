package com.example.spendsense.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendsense.model.Transaction
import com.example.spendsense.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currency: String,
    rate: Double,
    onLogout: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUser = auth.currentUser

    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf(TransactionType.INCOME) }
    var dailyBudget by remember { mutableStateOf(50.00) }
    
    val transactions = remember { mutableStateListOf<Transaction>() }

    // Fetch transactions from Firestore
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid).collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(context, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        transactions.clear()
                        snapshot.documents.forEach { doc ->
                            val transaction = doc.toObject(Transaction::class.java)
                            if (transaction != null) {
                                transactions.add(transaction.copy(id = doc.id))
                            }
                        }
                    }
                }
            
            // Fetch Budget
            db.collection("users").document(user.uid)
                .addSnapshotListener { snapshot, _ ->
                    val budget = snapshot?.getDouble("dailyBudget")
                    if (budget != null) dailyBudget = budget
                }
        }
    }

    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalBalance = totalIncome - totalExpense

    if (showAddDialog) {
        AddTransactionDialog(
            type = dialogType,
            currency = currency,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amountStr ->
                val amount = (amountStr.toDoubleOrNull() ?: 0.0) / rate
                val newTransaction = Transaction(
                    title = title,
                    amount = amount,
                    type = dialogType,
                    date = "Today"
                )
                
                currentUser?.let { user ->
                    db.collection("users").document(user.uid).collection("transactions")
                        .add(newTransaction)
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to save transaction", Toast.LENGTH_SHORT).show()
                        }
                }
                showAddDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        SetBudgetDialog(
            currentBudget = (dailyBudget * rate).toString(),
            currency = currency,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { newBudgetStr ->
                val newBudget = (newBudgetStr.toDoubleOrNull() ?: 50.0) / rate
                currentUser?.let { user ->
                    db.collection("users").document(user.uid)
                        .update("dailyBudget", newBudget)
                }
                showBudgetDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyMoney") },
                actions = {
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(Icons.Default.BarChart, contentDescription = "Analytics")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            BalanceCard(totalBalance * rate, totalIncome * rate, totalExpense * rate, dailyBudget * rate, currency)
            
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.AddCircle,
                    label = "Income",
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32),
                    onClick = { 
                        dialogType = TransactionType.INCOME
                        showAddDialog = true
                    }
                )
                QuickActionButton(
                    icon = Icons.Default.RemoveCircle,
                    label = "Expense",
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFC62828),
                    onClick = { 
                        dialogType = TransactionType.EXPENSE
                        showAddDialog = true
                    }
                )
                QuickActionButton(
                    icon = Icons.Default.SettingsSuggest,
                    label = "Budget",
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF1565C0),
                    onClick = { showBudgetDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Recent Transactions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionItem(transaction, currency, rate)
                }
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, income: Double, expense: Double, dailyBudget: Double, currency: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Total Balance", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = "$currency${"%.2f".format(balance)}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Daily Budget: $currency${"%.2f".format(dailyBudget)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryItem(icon = Icons.Default.TrendingUp, label = "Income", value = "$currency${"%.2f".format(income)}", color = Color(0xFF4CAF50))
                SummaryItem(icon = Icons.Default.TrendingDown, label = "Expenses", value = "$currency${"%.2f".format(expense)}", color = Color(0xFFF44336))
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    type: TransactionType,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == TransactionType.INCOME) "Add Income" else "Add Expense") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount ($currency)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, amount) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SetBudgetDialog(
    currentBudget: String,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var budget by remember { mutableStateOf(currentBudget) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Budget") },
        text = {
            Column {
                Text("Enter your spending limit for today", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Daily Budget ($currency)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(budget) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.size(60.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SummaryItem(icon: ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, currency: String, rate: Double) {
    val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
    val convertedAmount = transaction.amount * rate
    val amountText = "$prefix$currency${"%.2f".format(convertedAmount)}"
    
    ListItem(
        headlineContent = { Text(transaction.title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(transaction.date) },
        trailingContent = {
            Text(
                text = amountText,
                color = if (transaction.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        },
        leadingContent = {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
}
