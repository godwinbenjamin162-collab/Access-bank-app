package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NigerianBankData
import com.example.ui.BankAlertViewModel
import com.example.ui.components.BankSelectorDialog
import com.example.ui.components.SmsPhonePreview
import com.example.ui.theme.AlertCreditGreen
import com.example.ui.theme.AlertDebitRed
import com.example.ui.theme.NaijaGreenPrimary

@Composable
fun AlertHubScreen(
    viewModel: BankAlertViewModel,
    modifier: Modifier = Modifier
) {
    val hubBank by viewModel.hubBank.collectAsState()
    val hubType by viewModel.hubType.collectAsState()
    val hubAmount by viewModel.hubAmount.collectAsState()
    val hubAccountNumber by viewModel.hubAccountNumber.collectAsState()
    val hubBalance by viewModel.hubBalance.collectAsState()
    val hubCounterparty by viewModel.hubCounterparty.collectAsState()
    val hubNarration by viewModel.hubNarration.collectAsState()
    val hubLiveAlert by viewModel.hubLiveAlert.collectAsState()

    var showBankPicker by remember { mutableStateOf(false) }

    if (showBankPicker) {
        BankSelectorDialog(
            selectedBank = hubBank,
            onBankSelected = { viewModel.setHubBank(it) },
            onDismiss = { showBankPicker = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("alert_hub_container")
    ) {
        // Hub Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NaijaGreenPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sms,
                    contentDescription = null,
                    tint = NaijaGreenPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Nigerian SMS Alert Generator",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Generate and simulate instant Debit & Credit bank SMS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Live Phone SMS Preview Card (Simulates phone screen)
        Text(
            text = "LIVE PHONE SMS PREVIEW",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        SmsPhonePreview(
            senderHeader = hubBank.smsSenderId,
            smsBody = hubLiveAlert,
            timestamp = System.currentTimeMillis(),
            isDebit = hubType == "DEBIT",
            onCopy = { viewModel.copySmsToClipboard(hubLiveAlert) },
            onShare = { viewModel.shareSms(hubLiveAlert, hubBank.smsSenderId) },
            modifier = Modifier.testTag("hub_live_preview")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Drop-down banner trigger button
        Button(
            onClick = { viewModel.fireHubNotification() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("simulate_drop_down_sms_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = NaijaGreenPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Simulate Drop-Down Phone Alert",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = NaijaGreenPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Configuration Section
        Text(
            text = "CUSTOMIZE ALERT PARAMETERS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Alert Type Selector (DEBIT vs CREDIT)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isDebit = hubType == "DEBIT"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDebit) AlertDebitRed.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = if (isDebit) 1.5.dp else 0.5.dp,
                        color = if (isDebit) AlertDebitRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.setHubType("DEBIT") }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DEBIT ALERT (DR)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isDebit) AlertDebitRed else MaterialTheme.colorScheme.onSurface
                )
            }

            val isCredit = hubType == "CREDIT"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isCredit) AlertCreditGreen.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = if (isCredit) 1.5.dp else 0.5.dp,
                        color = if (isCredit) AlertCreditGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.setHubType("CREDIT") }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CREDIT ALERT (CR)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isCredit) AlertCreditGreen else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bank Selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showBankPicker = true }
                .testTag("hub_bank_picker_btn"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(hubBank.brandColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hubBank.shortName.take(2).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = hubBank.brandTextColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Bank SMS Template",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${hubBank.name} (${hubBank.smsSenderId})",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Bank Format Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(NigerianBankData.banks.filter { it.isPopular }) { bank ->
                FilterChip(
                    selected = bank.id == hubBank.id,
                    onClick = { viewModel.setHubBank(bank) },
                    label = { Text(bank.shortName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NaijaGreenPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = NaijaGreenPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Amount & Available Balance Fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = hubAmount,
                onValueChange = { viewModel.setHubAmount(it) },
                label = { Text("Amount (NGN)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1f)
                    .testTag("hub_amount_field"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = hubBalance,
                onValueChange = { viewModel.setHubBalance(it) },
                label = { Text("New Bal (NGN)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1f)
                    .testTag("hub_balance_field"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Account Number & Counterparty Name Fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = hubAccountNumber,
                onValueChange = { viewModel.setHubAccountNumber(it) },
                label = { Text("Acct Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("hub_account_field"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = hubCounterparty,
                onValueChange = { viewModel.setHubCounterparty(it) },
                label = { Text("Counterparty Name") },
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("hub_party_field"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Narration
        OutlinedTextField(
            value = hubNarration,
            onValueChange = { viewModel.setHubNarration(it) },
            label = { Text("Narration / Description") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hub_narration_field"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}
