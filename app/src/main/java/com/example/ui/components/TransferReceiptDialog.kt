package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.TransactionEntity
import com.example.ui.ReceiptViewMode
import com.example.ui.theme.OPayGreen
import com.example.util.SmsAlertGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransferReceiptDialog(
    transaction: TransactionEntity,
    viewMode: ReceiptViewMode = ReceiptViewMode.HISTORY_TAP,
    onDismiss: () -> Unit,
    onCopySms: (String) -> Unit
) {
    val context = LocalContext.current
    val isPostTx = (viewMode == ReceiptViewMode.POST_TRANSACTION)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        if (isPostTx) {
            PostTransactionReceiptContent(
                context = context,
                transaction = transaction,
                onDismiss = onDismiss,
                onCopySms = onCopySms
            )
        } else {
            HistoryTapReceiptContent(
                context = context,
                transaction = transaction,
                onDismiss = onDismiss,
                onCopySms = onCopySms
            )
        }
    }
}

/**
 * 1. Post-transaction Receipt:
 * Faithful to OPay transaction success confirmation screen.
 * Distinctive, compact and clean card with "Successful" badge, amount, recipient card,
 * and Share / Done action buttons.
 */
@Composable
private fun PostTransactionReceiptContent(
    context: Context,
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onCopySms: (String) -> Unit
) {
    val dtStr = SimpleDateFormat("dd MMM, yyyy • hh:mm:ss a", Locale.US).format(Date(transaction.timestamp))
    var showSmsDetail by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .widthIn(max = 330.dp)
            .fillMaxWidth(0.86f)
            .clip(RoundedCornerShape(20.dp))
            .testTag("post_transaction_receipt_dialog"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close icon at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("close_receipt_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Big Green Checkmark Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(OPayGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Transaction Successful",
                    tint = OPayGreen,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent "Transaction Successful" header
            Text(
                text = "Transaction Successful",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Big Amount Text
            Text(
                text = SmsAlertGenerator.formatNaira(transaction.amount),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp
                ),
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = dtStr,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Beneficiary Details Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    ReceiptRow(label = "Recipient", value = transaction.recipientName.ifBlank { "Beneficiary" })
                    ReceiptRow(label = "Bank", value = transaction.recipientBank)
                    if (transaction.recipientAccountNumber.isNotBlank()) {
                        ReceiptRow(label = "Account", value = transaction.recipientAccountNumber)
                    }
                    ReceiptRow(label = "Sender", value = transaction.senderName)
                    ReceiptRow(label = "Ref (NIP)", value = transaction.reference, isMonospace = true)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compact SMS Notification preview tile
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receipt_sms_alert_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSmsDetail = !showSmsDetail },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = OPayGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SMS Alert (${transaction.smsSenderHeader})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (showSmsDetail) "Hide" else "View",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OPayGreen,
                                fontSize = 11.sp
                            )
                        )
                    }

                    if (showSmsDetail) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = transaction.smsBody,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCopySms(transaction.smsBody) },
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy SMS",
                                tint = OPayGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Copy SMS Text",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OPayGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Share Receipt & Done
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(21.dp)
                ) {
                    Text("Done", fontSize = 13.sp)
                }

                Button(
                    onClick = { shareReceipt(context, transaction) },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("share_receipt_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = OPayGreen),
                    shape = RoundedCornerShape(21.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Share Receipt", fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * 2. History-tap Receipt:
 * The official full transaction receipt layout shown when tapping a row in History.
 * Designed "a little bit bigger" (width 360dp, full spacious layout), complete with:
 * - OPay Official Header branding
 * - Transaction Status: Successful
 * - Amount and breakdown
 * - Detailed transfer information & Monospace NIP Session ID
 * - Download Statement / Share Receipt action buttons
 */
@Composable
private fun HistoryTapReceiptContent(
    context: Context,
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onCopySms: (String) -> Unit
) {
    val dtStr = SimpleDateFormat("dd MMM, yyyy • hh:mm:ss a", Locale.US).format(Date(transaction.timestamp))
    var showSmsDetail by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(22.dp))
            .testTag("history_tap_receipt_dialog"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Header Bar: "Transaction Receipt" with Close icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(OPayGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = OPayGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transaction Receipt",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("close_receipt_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Spacious Transaction Successful Status Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F8F0))
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(OPayGreen.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = OPayGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "TRANSACTION SUCCESSFUL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.6.sp
                        ),
                        color = OPayGreen
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val isDebit = transaction.type == "DEBIT"
                    Text(
                        text = (if (isDebit) "- " else "+ ") + SmsAlertGenerator.formatNaira(transaction.amount),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = if (isDebit) Color(0xFF1E293B) else OPayGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = dtStr,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed Key-Value Breakdown with generous spacing
            ReceiptRow(label = "Recipient Name", value = transaction.recipientName.ifBlank { "Beneficiary" })
            ReceiptRow(label = "Recipient Bank", value = transaction.recipientBank)
            if (transaction.recipientAccountNumber.isNotBlank()) {
                ReceiptRow(label = "Account Number", value = transaction.recipientAccountNumber)
            }
            ReceiptRow(label = "Sender Name", value = transaction.senderName)
            if (transaction.narration.isNotBlank()) {
                ReceiptRow(label = "Narration / Remark", value = transaction.narration)
            }
            ReceiptRow(label = "Payment Method", value = "OPay Wallet Transfer")
            ReceiptRow(label = "Session ID (NIP)", value = transaction.reference, isMonospace = true)

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable SMS Alert Delivery Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receipt_sms_alert_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSmsDetail = !showSmsDetail },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = OPayGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bank SMS Alert (${transaction.smsSenderHeader})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (showSmsDetail) "Collapse" else "Expand",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OPayGreen,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    if (showSmsDetail) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = transaction.smsBody,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCopySms(transaction.smsBody) },
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy SMS",
                                tint = OPayGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy SMS Text",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OPayGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: Done & Share Receipt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text("Close", fontSize = 13.5.sp)
                }

                Button(
                    onClick = { shareReceipt(context, transaction) },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(44.dp)
                        .testTag("share_receipt_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = OPayGreen),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Receipt", fontSize = 13.5.sp)
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                    fontSize = if (isMonospace) 11.sp else 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.6f)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            thickness = 0.5.dp
        )
    }
}

private fun shareReceipt(context: Context, tx: TransactionEntity) {
    val dtStr = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.US).format(Date(tx.timestamp))
    val receiptText = """
        === OPAY TRANSACTION RECEIPT ===
        Status: SUCCESSFUL
        Amount: ${SmsAlertGenerator.formatNaira(tx.amount)}
        Date: $dtStr
        Reference: ${tx.reference}
        
        Recipient: ${tx.recipientName}
        Bank: ${tx.recipientBank}
        Account: ${tx.recipientAccountNumber}
        Sender: ${tx.senderName}
        Narration: ${tx.narration}
        ================================
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, receiptText)
        type = "text/plain"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Receipt via"))
}
