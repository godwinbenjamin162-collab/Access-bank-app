package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NigerianBankData
import com.example.data.TransactionEntity
import com.example.ui.BankAlertViewModel
import com.example.ui.MainTab
import com.example.ui.components.BankSelectorDialog
import com.example.ui.theme.AlertCreditGreen
import com.example.ui.theme.AlertDebitRed
import com.example.ui.theme.OPayGreen
import com.example.util.SmsAlertGenerator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: BankAlertViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("All Categories") }
    var selectedStatus by remember { mutableStateOf("All Status") }
    var selectedMonth by remember { mutableStateOf("Sep 2026") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    var showAnalysisDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Filter list
    val filteredList = remember(transactions, selectedCategory, selectedStatus) {
        transactions.filter { tx ->
            val matchesCategory = when (selectedCategory) {
                "Transfers" -> tx.narration.contains("Transfer", ignoreCase = true) ||
                        tx.recipientName.startsWith("Transfer", ignoreCase = true)
                "OWealth Interest" -> tx.narration.contains("OWealth", ignoreCase = true) ||
                        tx.recipientName.contains("OWealth", ignoreCase = true)
                "Airtime & Bills" -> tx.narration.contains("Airtime", ignoreCase = true) ||
                        tx.narration.contains("Data", ignoreCase = true) ||
                        tx.narration.contains("Bill", ignoreCase = true)
                "VAT & Fees" -> tx.narration.contains("VAT", ignoreCase = true) ||
                        tx.recipientName.contains("VAT", ignoreCase = true)
                else -> true
            }
            val matchesStatus = when (selectedStatus) {
                "Successful" -> true
                "Pending" -> false
                "Failed" -> false
                else -> true
            }
            matchesCategory && matchesStatus
        }
    }

    // Compute In & Out sums
    val totalIn = remember(transactions) {
        transactions.filter { it.type == "CREDIT" }.sumOf { it.amount }
    }
    val totalOut = remember(transactions) {
        transactions.filter { it.type == "DEBIT" }.sumOf { it.amount }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .testTag("history_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. Top Bar: Back, "Transactions", "Download"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.setTab(MainTab.TRANSFER) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("history_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E1E1E),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFF1E1E1E)
                    )
                }

                TextButton(
                    onClick = { showDownloadDialog = true },
                    modifier = Modifier.testTag("history_download_btn")
                ) {
                    Text(
                        text = "Download",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = OPayGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Filter Pills Row: "All Categories ▾", "All Status ▾"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Filter Pill
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        onClick = { categoryDropdownExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0F2F5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCategory,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF333333),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        listOf("All Categories", "Transfers", "OWealth Interest", "Airtime & Bills", "VAT & Fees")
                            .forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                    }
                }

                // Status Filter Pill
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        onClick = { statusDropdownExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0F2F5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedStatus,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF333333),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        listOf("All Status", "Successful", "Pending", "Failed")
                            .forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st) },
                                    onClick = {
                                        selectedStatus = st
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Month & In / Out Summary Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month selector
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { monthDropdownExpanded = true }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedMonth,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFF1E1E1E)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF1E1E1E),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = monthDropdownExpanded,
                        onDismissRequest = { monthDropdownExpanded = false }
                    ) {
                        listOf("Sep 2026", "Aug 2026", "Jul 2026", "Jun 2026").forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    selectedMonth = m
                                    monthDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Analysis Pill Button
                Surface(
                    onClick = { showAnalysisDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = OPayGreen,
                    modifier = Modifier.testTag("analysis_btn")
                ) {
                    Text(
                        text = "Analysis",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // In / Out row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "In: ",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF757575)
                )
                Text(
                    text = SmsAlertGenerator.formatNaira(totalIn),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    color = Color(0xFF1E1E1E)
                )

                Spacer(modifier = Modifier.width(18.dp))

                Text(
                    text = "Out: ",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF757575)
                )
                Text(
                    text = SmsAlertGenerator.formatNaira(totalOut),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    color = Color(0xFF1E1E1E)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. White Rounded Container with Transaction List
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions found",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF666666)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("transactions_lazy_column")
                    ) {
                        itemsIndexed(filteredList, key = { _, tx -> tx.id }) { index, tx ->
                            OpayTransactionRow(
                                transaction = tx,
                                onClick = {
                                    // When you tap on it, it brings the transaction receipt!
                                    viewModel.showReceipt(tx)
                                }
                            )

                            if (index < filteredList.size - 1) {
                                HorizontalDivider(
                                    color = Color(0xFFF1F3F5),
                                    thickness = 0.8.dp,
                                    modifier = Modifier.padding(start = 64.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to quickly add/test a new transaction
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_tx"),
            containerColor = OPayGreen,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }

    // Dialog: Analysis Breakdown
    if (showAnalysisDialog) {
        AlertDialog(
            onDismissRequest = { showAnalysisDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = OPayGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$selectedMonth Analysis", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Inflow:", color = Color(0xFF666666), fontSize = 13.sp)
                        Text(
                            "+ ${SmsAlertGenerator.formatNaira(totalIn)}",
                            fontWeight = FontWeight.Bold,
                            color = AlertCreditGreen,
                            fontSize = 13.sp
                        )
                    }
                    LinearProgressIndicator(
                        progress = { 0.49f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AlertCreditGreen,
                        trackColor = Color(0xFFE8F8F0)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Outflow:", color = Color(0xFF666666), fontSize = 13.sp)
                        Text(
                            "- ${SmsAlertGenerator.formatNaira(totalOut)}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E1E),
                            fontSize = 13.sp
                        )
                    }
                    LinearProgressIndicator(
                        progress = { 0.51f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF1E1E1E),
                        trackColor = Color(0xFFE5E7EB)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Net Flow: - ₦1,430.52 (Outflow exceeds Inflow)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = Color(0xFF666666)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAnalysisDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OPayGreen)
                ) {
                    Text("Done")
                }
            }
        )
    }

    // Dialog: Download Statement
    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = OPayGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Statement", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Export your bank statement for $selectedMonth as official PDF or text summary.")
                    Text("Recipient: ${viewModel.userAccountName.collectAsState().value}", fontSize = 12.sp, color = Color(0xFF666666))
                    Text("Account: ${viewModel.userAccountNumber.collectAsState().value} (OPay)", fontSize = 12.sp, color = Color(0xFF666666))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.exportStatement(context, filteredList.ifEmpty { transactions })
                        Toast.makeText(context, "Statement exported successfully", Toast.LENGTH_SHORT).show()
                        showDownloadDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OPayGreen)
                ) {
                    Text("Export Statement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Record New Custom Transaction
    if (showAddDialog) {
        RecordTransactionDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun OpayTransactionRow(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val isDebit = transaction.type == "DEBIT"
    val isOwealth = transaction.narration.contains("OWealth", ignoreCase = true) ||
            transaction.recipientName.contains("OWealth", ignoreCase = true)
    val isVat = transaction.narration.contains("VAT", ignoreCase = true) ||
            transaction.recipientName.contains("VAT", ignoreCase = true)

    val title = formatOpayTitle(transaction, isOwealth, isVat)
    val formattedDate = formatOpayDateTime(transaction.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("tx_item_${transaction.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Circular Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isOwealth -> Color(0xFFF3EDFD) // Light purple
                        else -> Color(0xFFE8F8F0) // Mint green
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isOwealth -> {
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color(0xFF7C4DFF)
                        )
                    )
                }
                isDebit -> {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Debit",
                        tint = Color(0xFF00B875),
                        modifier = Modifier.size(19.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Credit",
                        tint = Color(0xFF00B875),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center Details: Title & Timestamp
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                ),
                color = Color(0xFF1E1E1E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp
                ),
                color = Color(0xFF9E9E9E)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Details: Amount & "Successful" badge
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (isDebit) "-" else "+") + SmsAlertGenerator.formatNaira(transaction.amount),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.5.sp
                ),
                color = if (isDebit) Color(0xFF1E1E1E) else Color(0xFF00B875)
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Successful pill badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE8F8F0))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "Successful",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp
                    ),
                    color = Color(0xFF00B875)
                )
            }
        }
    }
}

private fun formatOpayTitle(tx: TransactionEntity, isOwealth: Boolean, isVat: Boolean): String {
    return when {
        isOwealth -> "OWealth Interest Earned"
        isVat -> "VAT on Transfer Fee"
        tx.recipientName.startsWith("Transfer", ignoreCase = true) -> tx.recipientName
        tx.type == "CREDIT" -> {
            val name = if (tx.senderName.isNotBlank() && tx.senderName != "BENJAMIN GODWIN") {
                tx.senderName
            } else if (tx.recipientName.isNotBlank()) {
                tx.recipientName
            } else "Sender"
            "Transfer from $name"
        }
        else -> {
            val name = tx.recipientName.ifBlank { "Beneficiary" }
            "Transfer to $name"
        }
    }
}

private fun formatOpayDateTime(timestamp: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val monthName = SimpleDateFormat("MMM", Locale.US).format(Date(timestamp))
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(timestamp))
    return "$monthName ${day}$suffix, $time"
}

@Composable
private fun RecordTransactionDialog(
    viewModel: BankAlertViewModel,
    onDismiss: () -> Unit
) {
    var txType by remember { mutableStateOf("DEBIT") }
    var txAmount by remember { mutableStateOf("") }
    var txBank by remember { mutableStateOf(NigerianBankData.getBankById("access")) }
    var txAccount by remember { mutableStateOf("") }
    var txName by remember { mutableStateOf("") }
    var txNarration by remember { mutableStateOf("Transfer") }
    var showBankPicker by remember { mutableStateOf(false) }

    if (showBankPicker) {
        BankSelectorDialog(
            selectedBank = txBank,
            onBankSelected = {
                txBank = it
                showBankPicker = false
            },
            onDismiss = { showBankPicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record New Transaction", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { txType = "DEBIT" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (txType == "DEBIT") Color(0xFF1E1E1E) else Color(0xFFE5E7EB),
                            contentColor = if (txType == "DEBIT") Color.White else Color(0xFF666666)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Debit (Out)")
                    }

                    Button(
                        onClick = { txType = "CREDIT" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (txType == "CREDIT") OPayGreen else Color(0xFFE5E7EB),
                            contentColor = if (txType == "CREDIT") Color.White else Color(0xFF666666)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Credit (In)")
                    }
                }

                OutlinedTextField(
                    value = txAmount,
                    onValueChange = { txAmount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (₦)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = txName,
                    onValueChange = { txName = it },
                    label = { Text(if (txType == "CREDIT") "Sender Name" else "Beneficiary Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { showBankPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Bank: ${txBank.name}")
                }

                OutlinedTextField(
                    value = txAccount,
                    onValueChange = { txAccount = it.filter { c -> c.isDigit() }.take(10) },
                    label = { Text("Account Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = txNarration,
                    onValueChange = { txNarration = it },
                    label = { Text("Narration") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = txAmount.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        viewModel.saveCustomTransaction(
                            type = txType,
                            amount = amt,
                            bank = txBank,
                            accountNumber = txAccount.ifBlank { "0123456789" },
                            counterpartyName = txName.ifBlank { "Beneficiary" },
                            narration = txNarration
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OPayGreen)
            ) {
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
