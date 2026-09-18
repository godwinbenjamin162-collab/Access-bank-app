package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.BankAlertViewModel
import com.example.ui.components.BankSelectorDialog
import com.example.ui.theme.OPayGreen
import com.example.util.SmsAlertGenerator

@Composable
fun TransferDetailsInputView(
    viewModel: BankAlertViewModel,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val recipientBank by viewModel.recipientBank.collectAsState()
    val recipientAccountNumber by viewModel.recipientAccountNumber.collectAsState()
    val recipientName by viewModel.recipientName.collectAsState()
    val isResolvingName by viewModel.isResolvingName.collectAsState()
    val transferAmount by viewModel.transferAmount.collectAsState()
    val narration by viewModel.narration.collectAsState()
    val autoSendSms by viewModel.autoSendSms.collectAsState()
    val isProcessingTransfer by viewModel.isProcessingTransfer.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var showBankPicker by remember { mutableStateOf(false) }
    var showAmountDialog by remember { mutableStateOf(false) }
    var selectedBeneficiaryTab by remember { mutableIntStateOf(0) } // 0: Recents, 1: Favourites
    var isSearchingBeneficiaries by remember { mutableStateOf(false) }
    var beneficiarySearchQuery by remember { mutableStateOf("") }

    if (showBankPicker) {
        BankSelectorDialog(
            selectedBank = recipientBank,
            onBankSelected = {
                viewModel.selectRecipientBank(it)
                showBankPicker = false
            },
            onDismiss = { showBankPicker = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // 1. TOP BAR: Back button, Title, "History" link button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("back_from_input_details_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "Transfer to Bank Account",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(
                onClick = onOpenHistory,
                modifier = Modifier.testTag("transfer_history_btn")
            ) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = OPayGreen
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. PROMOTIONAL BANNER: Enjoy 10% Off your Betting Voucher
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    Toast.makeText(context, "Betting Voucher Promo: Deposit ₦300 & get ₦30 Off!", Toast.LENGTH_SHORT).show()
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF43A047), Color(0xFF2E7D32), Color(0xFF1B5E20))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enjoy 10% Off your Betting Voucher!",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Deposit ₦300 & get ₦30 Off.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Trophy + Black pill button "Get it Now!"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E1E1E))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(text = "🏆", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Get it Now!",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Right graphic: Yellow Voucher ticket + Soccer ball
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEE600))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "VOUCHER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333)
                                    )
                                )
                                Text(
                                    text = "UP TO ₦30",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF00B875)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "⚽", fontSize = 28.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. PURPLE STATUS PILL: Free transfers for the day: 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF3EDFD))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7C4DFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₦",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Free transfers for the day: 2",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF673AB7)
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. RECIPIENT ACCOUNT CARD
        // Input Account Number, Select Bank, and Input Box for Account Person Name
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Recipient Account",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 1: Enter 10 digits Account Number
                OutlinedTextField(
                    value = recipientAccountNumber,
                    onValueChange = { viewModel.setRecipientAccountNumber(it) },
                    placeholder = {
                        Text(
                            text = "Enter 10 digits Account Number",
                            color = Color(0xFF9E9E9E),
                            fontSize = 14.sp
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (recipientAccountNumber.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setRecipientAccountNumber("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF9E9E9E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    val clipText = clipboardManager.getText()?.text ?: ""
                                    val digits = clipText.filter { it.isDigit() }.take(10)
                                    if (digits.isNotEmpty()) {
                                        viewModel.setRecipientAccountNumber(digits)
                                        Toast.makeText(context, "Pasted account number: $digits", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = OPayGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OPayGreen,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipient_account_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Field 2: Select Bank Clickable Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .clickable { showBankPicker = true }
                        .padding(horizontal = 14.dp, vertical = 13.dp)
                        .testTag("select_bank_row")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(recipientBank.brandColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = recipientBank.shortName.take(2).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = recipientBank.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Select Bank",
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Field 3: (USER REQUESTED INPUT BOX FOR ACCOUNT PERSON NAME)
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { viewModel.setRecipientName(it) },
                    placeholder = {
                        Text(
                            text = "Enter Account Person Name",
                            color = Color(0xFF9E9E9E),
                            fontSize = 14.sp
                        )
                    },
                    label = {
                        Text(
                            text = "Account Person Name",
                            color = if (recipientName.isNotBlank()) OPayGreen else Color(0xFF757575),
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Account Person",
                            tint = if (recipientName.isNotBlank()) OPayGreen else Color(0xFF9E9E9E),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (isResolvingName) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = OPayGreen
                            )
                        } else if (recipientName.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Name",
                                tint = OPayGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OPayGreen,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_person_name_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // "Next" Button
                val isNextActive = recipientAccountNumber.length >= 10 || recipientName.isNotBlank()
                Button(
                    onClick = {
                        if (recipientAccountNumber.length < 10 && recipientName.isBlank()) {
                            Toast.makeText(context, "Please enter 10 digits Account Number", Toast.LENGTH_SHORT).show()
                        } else {
                            showAmountDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("transfer_next_btn"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNextActive) OPayGreen else Color(0xFF99E6C9),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. SUCCESS RATE MONITOR CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    Toast.makeText(context, "NIBSS Network Status: 99.8% Success Rate across all Nigerian Banks.", Toast.LENGTH_SHORT).show()
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = OPayGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Bank Transfer Success Rate Monitor",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. RECENTS & FAVOURITES TABS AND LIST (matching screenshot)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // "Recents" Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { selectedBeneficiaryTab = 0 }
                        .padding(end = 20.dp)
                ) {
                    Text(
                        text = "Recents",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = if (selectedBeneficiaryTab == 0) OPayGreen else Color(0xFF757575)
                    )
                    if (selectedBeneficiaryTab == 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(26.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(OPayGreen)
                        )
                    }
                }

                // "Favourites" Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedBeneficiaryTab = 1 }
                ) {
                    Text(
                        text = "Favourites",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = if (selectedBeneficiaryTab == 1) OPayGreen else Color(0xFF757575)
                    )
                    if (selectedBeneficiaryTab == 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(26.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(OPayGreen)
                        )
                    }
                }
            }

            // Search Icon
            IconButton(
                onClick = { isSearchingBeneficiaries = !isSearchingBeneficiaries },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Beneficiaries",
                    tint = OPayGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Optional Beneficiary Search Bar
        if (isSearchingBeneficiaries) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = beneficiarySearchQuery,
                onValueChange = { beneficiarySearchQuery = it },
                placeholder = { Text("Search by name, account, or bank...", fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OPayGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Beneficiaries List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Beneficiary Item 1 (From screenshot): Subwallet Telecom-Iru(XIXAPAY) - 6634088211 PalmPay
                val subwalletMatches = beneficiarySearchQuery.isBlank() ||
                        "Subwallet Telecom-Iru(XIXAPAY)".contains(beneficiarySearchQuery, ignoreCase = true) ||
                        "6634088211".contains(beneficiarySearchQuery) ||
                        "PalmPay".contains(beneficiarySearchQuery, ignoreCase = true)

                if (subwalletMatches) {
                    BeneficiaryRowItem(
                        name = "Subwallet Telecom-Iru(XIXAPAY)",
                        accountNumber = "6634088211",
                        bankName = "PalmPay",
                        badgeColor = Color(0xFF6B11D4),
                        badgeText = "P",
                        onClick = {
                            viewModel.selectBeneficiary("6634088211", "PalmPay", "Subwallet Telecom-Iru(XIXAPAY)")
                            Toast.makeText(context, "Selected Subwallet Telecom-Iru(XIXAPAY)", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                }

                // Beneficiary Item 2 (From screenshot): MASERO OMENUWOMA - 8102938471 Moniepoint
                val maseroMatches = beneficiarySearchQuery.isBlank() ||
                        "MASERO OMENUWOMA".contains(beneficiarySearchQuery, ignoreCase = true) ||
                        "8102938471".contains(beneficiarySearchQuery) ||
                        "Moniepoint".contains(beneficiarySearchQuery, ignoreCase = true)

                if (maseroMatches) {
                    BeneficiaryRowItem(
                        name = "MASERO OMENUWOMA",
                        accountNumber = "8102938471",
                        bankName = "Moniepoint MFB",
                        badgeColor = Color(0xFF0252D6),
                        badgeText = "M",
                        onClick = {
                            viewModel.selectBeneficiary("8102938471", "Moniepoint MFB", "MASERO OMENUWOMA")
                            Toast.makeText(context, "Selected MASERO OMENUWOMA", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Additional distinct previous transactions from Room Database
                val extraBeneficiaries = transactions
                    .filter { it.recipientAccountNumber.isNotBlank() && it.recipientAccountNumber != "6634088211" && it.recipientAccountNumber != "8102938471" }
                    .distinctBy { it.recipientAccountNumber }
                    .filter {
                        beneficiarySearchQuery.isBlank() ||
                                it.recipientName.contains(beneficiarySearchQuery, ignoreCase = true) ||
                                it.recipientAccountNumber.contains(beneficiarySearchQuery) ||
                                it.recipientBank.contains(beneficiarySearchQuery, ignoreCase = true)
                    }

                for (tx in extraBeneficiaries) {
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                    BeneficiaryRowItem(
                        name = tx.recipientName.ifBlank { "Beneficiary" },
                        accountNumber = tx.recipientAccountNumber,
                        bankName = tx.recipientBank,
                        badgeColor = OPayGreen,
                        badgeText = tx.recipientBank.take(1).uppercase(),
                        onClick = {
                            viewModel.selectBeneficiary(tx.recipientAccountNumber, tx.recipientBank, tx.recipientName)
                            Toast.makeText(context, "Selected ${tx.recipientName}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cancel / Go Back to Dashboard Button
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("cancel_input_details_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Back to Dashboard",
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface)
            )
        }
    }

    // ==========================================
    // 7. TRANSFER AMOUNT & CONFIRMATION DIALOG (Triggered when tapping "Next")
    // ==========================================
    if (showAmountDialog) {
        Dialog(
            onDismissRequest = { showAmountDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header: Title & Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Transfer Amount",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { showAmountDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF757575)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Beneficiary Card Summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(recipientBank.brandColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = recipientBank.shortName.take(2).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recipientName.ifBlank { "Account Person" },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${recipientBank.name} • $recipientAccountNumber",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = Color(0xFF555555)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Transfer Amount Input
                    Text(
                        text = "Amount (₦)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { viewModel.setTransferAmount(it) },
                        placeholder = { Text("0.00", color = Color(0xFF9E9E9E), fontSize = 18.sp) },
                        leadingIcon = {
                            Text(
                                text = "₦",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OPayGreen
                                ),
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OPayGreen,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_transfer_amount_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Amount Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(1000.0, 2000.0, 5000.0, 10000.0).forEach { amt ->
                            SuggestionChip(
                                onClick = { viewModel.addQuickAmount(amt) },
                                label = {
                                    Text(
                                        text = "+₦${amt.toInt() / 1000}k",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color(0xFFF5F5F5)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Narration / Remark Field
                    Text(
                        text = "Narration / Remark",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = narration,
                        onValueChange = { viewModel.setNarration(it) },
                        placeholder = { Text("e.g. Payment, Food, Upkeep", color = Color(0xFF9E9E9E), fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OPayGreen,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto-send SMS alert toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = OPayGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Send Instant SMS Alert",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Dispatches authentic bank SMS alert",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(0xFF757575))
                                )
                            }
                        }
                        Switch(
                            checked = autoSendSms,
                            onCheckedChange = { viewModel.setAutoSendSms(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OPayGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pay Button
                    Button(
                        onClick = {
                            val amtNum = transferAmount.toDoubleOrNull() ?: 0.0
                            if (amtNum <= 0.0) {
                                Toast.makeText(context, "Please enter a valid transfer amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.executeTransfer {
                                showAmountDialog = false
                                onBack()
                            }
                        },
                        enabled = !isProcessingTransfer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_and_pay_btn"),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OPayGreen,
                            contentColor = Color.White
                        )
                    ) {
                        if (isProcessingTransfer) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing...")
                        } else {
                            val amtNum = transferAmount.toDoubleOrNull() ?: 0.0
                            Text(
                                text = if (amtNum > 0.0) "Pay ${SmsAlertGenerator.formatNaira(amtNum)}" else "Confirm & Pay",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
