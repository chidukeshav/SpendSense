package com.example.spendsense.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(currency: String, rate: Double, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Spending Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            ExpensePieChart()

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Weekly Expenses", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            WeeklyBarChart(currency, rate)

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Account Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            MonthlySummaryCard(currency, rate)
        }
    }
}

@Composable
fun ExpensePieChart() {
    val categories = listOf(
        CategoryData("Food", 0.4f, Color(0xFFFF7043)),
        CategoryData("Shopping", 0.25f, Color(0xFF42A5F5)),
        CategoryData("Transport", 0.2f, Color(0xFF66BB6A)),
        CategoryData("Others", 0.15f, Color(0xFFFFA726))
    )

    // Animation progress
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    categories.forEach { category ->
                        val sweepAngle = category.value * 360f * animationProgress.value
                        drawArc(
                            color = category.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 40f, cap = StrokeCap.Round)
                        )
                        // Note: To keep the segments properly aligned during animation,
                        // we use the full value for startAngle calculation
                        startAngle += category.value * 360f * animationProgress.value
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Total", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Text(text = "100%", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).background(category.color, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = category.name, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${(category.value * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class CategoryData(val name: String, val value: Float, val color: Color)

@Composable
fun WeeklyBarChart(currency: String, rate: Double) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val baseValues = listOf(40.0, 70.0, 30.0, 90.0, 50.0, 20.0, 60.0)
    val maxVal = baseValues.maxOrNull() ?: 1.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEachIndexed { index, day ->
                val convertedVal = baseValues[index] * rate
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${currency}${convertedVal.toInt()}", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight((baseValues[index] / maxVal).toFloat() * 0.7f)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = day, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(currency: String, rate: Double) {
    val income = (500.0) * rate
    val expense = (144.02) * rate
    val balance = (332.34) * rate
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryColumn(label = "Total Income", value = "$currency${"%.2f".format(income)}", color = Color(0xFF4CAF50))
                SummaryColumn(label = "Total Expense", value = "$currency${"%.2f".format(expense)}", color = Color(0xFFF44336))
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Total Balance", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(text = "$currency${"%.2f".format(balance)}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun SummaryColumn(label: String, value: String, color: Color) {
    Column {
        Text(text = label, fontSize = 14.sp)
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
