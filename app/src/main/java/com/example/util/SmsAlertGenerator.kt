package com.example.util

import com.example.data.NigerianBank
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object SmsAlertGenerator {

    private val nairaFormatter = DecimalFormat("#,##0.00")

    fun formatNaira(amount: Double): String {
        return "₦" + nairaFormatter.format(amount)
    }

    fun formatNgnAmount(amount: Double): String {
        return nairaFormatter.format(amount)
    }

    fun maskAccount(account: String): String {
        val clean = account.filter { it.isDigit() }
        if (clean.length < 6) return clean
        return clean.take(3) + "***" + clean.takeLast(4)
    }

    fun generateNipReference(): String {
        val datePart = SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())
        val randomDigits = (100000..999999).random()
        return "NIP/999$datePart$randomDigits"
    }

    fun generateSessionId(): String {
        val datePart = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
        val randomSuffix = (10000000..99999999).random()
        return "999001$datePart$randomSuffix"
    }

    fun generateAlert(
        bank: NigerianBank,
        isDebit: Boolean,
        amount: Double,
        accountNumber: String,
        balanceAfter: Double,
        counterpartyName: String,
        narration: String,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        val formattedAmt = formatNgnAmount(amount)
        val formattedBal = formatNgnAmount(balanceAfter)
        val maskedAcc = maskAccount(accountNumber)
        val date = Date(timestamp)
        val cleanNarration = narration.ifBlank { "Transfer" }.uppercase().take(30)
        val shortCounterparty = counterpartyName.ifBlank { "CUSTOMER" }.uppercase().take(36)

        return when (bank.id) {
            "gtbank" -> {
                val dtFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US).format(date)
                val typeTag = if (isDebit) "DR" else "CR"
                val desc = if (isDebit) "TRF/$shortCounterparty/$cleanNarration" else "NIP/$shortCounterparty/$cleanNarration"
                "Acct: $maskedAcc\nAmt: NGN $formattedAmt $typeTag\nDesc: $desc\nDate: $dtFormat\nBal: NGN $formattedBal"
            }

            "access" -> {
                val dtFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(date)
                val title = if (isDebit) "Debit Alert!" else "Credit Alert!"
                val details = if (isDebit) "TRF TO $shortCounterparty / $cleanNarration" else "TRF FRM $shortCounterparty / $cleanNarration"
                "$title\nAcct: $maskedAcc\nAmt: NGN $formattedAmt\nDetails: $details\nDate: $dtFormat\nBal: NGN $formattedBal"
            }

            "zenith" -> {
                val dtFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.US).format(date).uppercase()
                val prefix = if (isDebit) "Debit" else "Credit"
                val desc = if (isDebit) "NIP/TRF TO $shortCounterparty/$cleanNarration" else "NIP/$shortCounterparty/$cleanNarration"
                "$prefix: NGN$formattedAmt\nAcct: $maskedAcc\nDesc: $desc\nTime: $dtFormat\nAvail Bal: NGN$formattedBal"
            }

            "firstbank" -> {
                val dtFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(date)
                val type = if (isDebit) "Debit" else "Credit"
                "FirstBank Alert: $type Amt: NGN $formattedAmt on Acct $maskedAcc. Narration: TRF/$cleanNarration. Date: $dtFormat. Bal: NGN $formattedBal"
            }

            "uba" -> {
                val dtFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date)
                val txn = if (isDebit) "Debit" else "Credit"
                val des = if (isDebit) "TRF/$shortCounterparty/$cleanNarration" else "NIP/$shortCounterparty/$cleanNarration"
                "Txn: $txn\nAcct: $maskedAcc\nAmt: NGN $formattedAmt\nDes: $des\nDate: $dtFormat\nBal: NGN $formattedBal"
            }

            "kuda" -> {
                val symbolAmt = formatNaira(amount)
                val symbolBal = formatNaira(balanceAfter)
                if (isDebit) {
                    "Spend: $symbolAmt sent to $shortCounterparty - $cleanNarration. New bal: $symbolBal"
                } else {
                    "Received: $symbolAmt from $shortCounterparty - $cleanNarration. New bal: $symbolBal"
                }
            }

            "opay" -> {
                val symbolAmt = formatNaira(amount)
                val symbolBal = formatNaira(balanceAfter)
                val ref = "OP" + SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(date) + Random.nextInt(100, 999)
                if (isDebit) {
                    "You have successfully transferred $symbolAmt to $shortCounterparty. Remarks: $cleanNarration. Bal: $symbolBal. Ref: $ref"
                } else {
                    "You have received $symbolAmt from $shortCounterparty. Remarks: $cleanNarration. Bal: $symbolBal. Ref: $ref"
                }
            }

            "palmpay" -> {
                val symbolAmt = formatNaira(amount)
                val symbolBal = formatNaira(balanceAfter)
                if (isDebit) {
                    "PalmPay: Transfer of $symbolAmt to $shortCounterparty succeeded. Bal: $symbolBal."
                } else {
                    "PalmPay: Money in! You received $symbolAmt from $shortCounterparty. Bal: $symbolBal."
                }
            }

            "moniepoint" -> {
                val dtFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(date)
                val type = if (isDebit) "Debit" else "Credit"
                val ref = "MP" + System.currentTimeMillis().toString().takeLast(8)
                "$type Alert: NGN $formattedAmt\nAcct: $maskedAcc\nParty: $shortCounterparty\nRemarks: $cleanNarration\nRef: $ref\nDate: $dtFormat\nBal: NGN $formattedBal"
            }

            else -> {
                // Standard Nigerian Bank template
                val dtFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.US).format(date)
                val type = if (isDebit) "Debit" else "Credit"
                "${bank.smsSenderId} $type Alert!\nAcct: $maskedAcc\nAmt: NGN $formattedAmt\nDesc: $cleanNarration\nDate: $dtFormat\nAvail Bal: NGN $formattedBal"
            }
        }
    }
}
