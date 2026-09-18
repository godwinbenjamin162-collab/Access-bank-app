package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BankDatabase
import com.example.data.NigerianBank
import com.example.data.NigerianBankData
import com.example.data.TransactionEntity
import com.example.data.TransactionRepository
import com.example.data.TransactionRepositoryImpl
import com.example.util.SmsAlertGenerator
import com.example.util.SmsDispatcher
import com.example.util.SmsSendResult
import com.example.BuildConfig
import com.example.data.ai.GeminiApiClient
import com.example.data.ai.GeminiContent
import com.example.data.ai.GeminiGenerateContentRequest
import com.example.data.ai.GeminiPart
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ReceiptViewMode {
    POST_TRANSACTION, // Clean post-transaction confirmation styling
    HISTORY_TAP       // Detailed full-width statement receipt
}

data class SmsBannerData(
    val senderHeader: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDebit: Boolean,
    val bankColor: Long
)

enum class MainTab(val title: String) {
    TRANSFER("Transfer"),
    ALERT_GENERATOR("SMS Alert Hub"),
    HISTORY("History"),
    ACCOUNT("My Account")
}

class BankAlertViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("opay_user_prefs", Context.MODE_PRIVATE)

    private val repository: TransactionRepository = TransactionRepositoryImpl(
        BankDatabase.getDatabase(application).transactionDao()
    )

    val transactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Navigation
    private val _currentTab = MutableStateFlow(MainTab.TRANSFER)
    val currentTab = _currentTab.asStateFlow()

    // Sender / User Account State
    private val _userBank = MutableStateFlow(NigerianBankData.getBankById("opay"))
    val userBank = _userBank.asStateFlow()

    private val _userAccountName = MutableStateFlow("BENJAMIN GODWIN")
    val userAccountName = _userAccountName.asStateFlow()

    private val _userAccountNumber = MutableStateFlow("8102394817")
    val userAccountNumber = _userAccountNumber.asStateFlow()

    private val _userBalance = MutableStateFlow(485500.00)
    val userBalance = _userBalance.asStateFlow()

    private val _isBalanceVisible = MutableStateFlow(true)
    val isBalanceVisible = _isBalanceVisible.asStateFlow()

    // Transfer Screen State
    private val _recipientBank = MutableStateFlow(NigerianBankData.getBankById("access"))
    val recipientBank = _recipientBank.asStateFlow()

    private val _recipientAccountNumber = MutableStateFlow("")
    val recipientAccountNumber = _recipientAccountNumber.asStateFlow()

    private val _recipientPhoneNumber = MutableStateFlow("")
    val recipientPhoneNumber = _recipientPhoneNumber.asStateFlow()

    private val _autoSendSms = MutableStateFlow(true)
    val autoSendSms = _autoSendSms.asStateFlow()

    private val _lastSmsDeliveryResult = MutableStateFlow<String?>(null)
    val lastSmsDeliveryResult = _lastSmsDeliveryResult.asStateFlow()

    private val _recipientName = MutableStateFlow("")
    val recipientName = _recipientName.asStateFlow()

    private val _isUserCustomizedName = MutableStateFlow(false)
    val isUserCustomizedName = _isUserCustomizedName.asStateFlow()

    private val _isResolvingName = MutableStateFlow(false)
    val isResolvingName = _isResolvingName.asStateFlow()

    private val _transferAmount = MutableStateFlow("")
    val transferAmount = _transferAmount.asStateFlow()

    private val _narration = MutableStateFlow("Transfer")
    val narration = _narration.asStateFlow()

    private val _alertType = MutableStateFlow("DEBIT") // "DEBIT", "CREDIT", "BOTH"
    val alertType = _alertType.asStateFlow()

    private val _isProcessingTransfer = MutableStateFlow(false)
    val isProcessingTransfer = _isProcessingTransfer.asStateFlow()

    // Active SMS Notification Banner (Live phone drop-down)
    private val _smsBanner = MutableStateFlow<SmsBannerData?>(null)
    val smsBanner = _smsBanner.asStateFlow()

    // Active Dialogs
    private val _activeReceipt = MutableStateFlow<TransactionEntity?>(null)
    val activeReceipt = _activeReceipt.asStateFlow()

    private val _receiptViewMode = MutableStateFlow(ReceiptViewMode.POST_TRANSACTION)
    val receiptViewMode = _receiptViewMode.asStateFlow()

    private val _activeSmsDetail = MutableStateFlow<TransactionEntity?>(null)
    val activeSmsDetail = _activeSmsDetail.asStateFlow()

    // AI Financial Assistant State
    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hello! I am your OPay AI Financial Assistant. Ask me about your balance, transaction history, Nigerian banking regulations (NUBAN/CBN), or tips on safe mobile transfers.",
                isUser = false
            )
        )
    )
    val aiMessages = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    // Direct SMS Alert Hub Generator State
    private val _hubBank = MutableStateFlow(NigerianBankData.getBankById("gtbank"))
    val hubBank = _hubBank.asStateFlow()

    private val _hubType = MutableStateFlow("DEBIT") // "DEBIT" or "CREDIT"
    val hubType = _hubType.asStateFlow()

    private val _hubAmount = MutableStateFlow("25000")
    val hubAmount = _hubAmount.asStateFlow()

    private val _hubAccountNumber = MutableStateFlow("0239481729")
    val hubAccountNumber = _hubAccountNumber.asStateFlow()

    private val _hubBalance = MutableStateFlow("460500")
    val hubBalance = _hubBalance.asStateFlow()

    private val _hubCounterparty = MutableStateFlow("CHUKWUMA ADEBAYO")
    val hubCounterparty = _hubCounterparty.asStateFlow()

    private val _hubNarration = MutableStateFlow("Food & Chops")
    val hubNarration = _hubNarration.asStateFlow()

    private val _hubLiveAlert = MutableStateFlow("")
    val hubLiveAlert = _hubLiveAlert.asStateFlow()

    private var nameResolveJob: Job? = null
    private var bannerDismissJob: Job? = null

    init {
        val savedBal = prefs.getString("user_balance", null)?.toDoubleOrNull()
        if (savedBal != null) {
            _userBalance.value = savedBal
        }
        val savedName = prefs.getString("user_name", null)
        if (!savedName.isNullOrBlank()) {
            _userAccountName.value = savedName
        }
        val savedAcc = prefs.getString("user_account", null)
        if (!savedAcc.isNullOrBlank()) {
            _userAccountNumber.value = savedAcc
        }
        val savedBankId = prefs.getString("user_bank_id", null)
        if (!savedBankId.isNullOrBlank()) {
            _userBank.value = NigerianBankData.getBankById(savedBankId)
        }

        updateHubLiveAlert()
        seedInitialTransactionsIfEmpty()
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !_isBalanceVisible.value
    }

    fun selectRecipientBank(bank: NigerianBank) {
        _recipientBank.value = bank
        if (!_isUserCustomizedName.value) {
            triggerNameResolution(_recipientAccountNumber.value, bank)
        }
    }

    fun setRecipientAccountNumber(account: String) {
        val filtered = account.filter { it.isDigit() }.take(10)
        val changed = filtered != _recipientAccountNumber.value
        _recipientAccountNumber.value = filtered
        // Auto-populate SMS alert recipient phone number from the account number
        _recipientPhoneNumber.value = SmsDispatcher.formatNigerianPhoneNumber(filtered)
        if (changed) {
            _isUserCustomizedName.value = false
        }
        if (filtered.length == 10) {
            triggerNameResolution(filtered, _recipientBank.value)
        } else {
            if (!_isUserCustomizedName.value) {
                _recipientName.value = ""
            }
        }
    }

    fun setRecipientPhoneNumber(phone: String) {
        _recipientPhoneNumber.value = phone
    }

    fun setAutoSendSms(enabled: Boolean) {
        _autoSendSms.value = enabled
    }

    private fun triggerNameResolution(account: String, bank: NigerianBank, force: Boolean = false) {
        nameResolveJob?.cancel()
        if (account.length == 10) {
            // If user has customized the name and not forced, preserve it!
            if (_isUserCustomizedName.value && _recipientName.value.isNotBlank() && !force) {
                return
            }
            nameResolveJob = viewModelScope.launch {
                _isResolvingName.value = true
                delay(350) // realistic network lookup simulation

                // 1. Check if it's the sender's own account
                if (account == _userAccountNumber.value) {
                    _recipientName.value = _userAccountName.value
                } else {
                    // 2. Check previous transactions for this exact account
                    val previousTx = transactions.value.find { it.recipientAccountNumber == account }
                    if (previousTx != null && previousTx.recipientName.isNotBlank()) {
                        _recipientName.value = previousTx.recipientName
                    } else {
                        // 3. Realistic NUBAN resolution following NIBSS standards
                        _recipientName.value = NigerianBankData.resolveAccountName(account, bank)
                    }
                }
                _isResolvingName.value = false
            }
        }
    }

    fun reVerifyRecipientName() {
        _isUserCustomizedName.value = false
        triggerNameResolution(_recipientAccountNumber.value, _recipientBank.value, force = true)
    }

    fun selectBeneficiary(accountNumber: String, bankName: String, name: String) {
        val bank = NigerianBankData.getBankByName(bankName)
        _recipientBank.value = bank
        _recipientAccountNumber.value = accountNumber
        _recipientPhoneNumber.value = SmsDispatcher.formatNigerianPhoneNumber(accountNumber)
        _recipientName.value = name
        _isUserCustomizedName.value = true
    }

    fun setRecipientName(name: String) {
        _recipientName.value = name
        _isUserCustomizedName.value = true
    }

    fun setTransferAmount(amount: String) {
        _transferAmount.value = amount.filter { it.isDigit() || it == '.' }
    }

    fun addQuickAmount(delta: Double) {
        val current = _transferAmount.value.toDoubleOrNull() ?: 0.0
        val updated = current + delta
        _transferAmount.value = if (updated % 1 == 0.0) updated.toInt().toString() else "%.2f".format(updated)
    }

    fun setNarration(text: String) {
        _narration.value = text
    }

    fun setAlertType(type: String) {
        _alertType.value = type
    }

    // Execute Transfer & Generate SMS Alert
    fun executeTransfer(onSuccess: (TransactionEntity) -> Unit = {}) {
        val amt = _transferAmount.value.toDoubleOrNull()
        if (amt == null || amt <= 0) {
            showToast("Please enter a valid transfer amount")
            return
        }

        if (_recipientAccountNumber.value.length < 10) {
            showToast("Enter a valid 10-digit NUBAN account number")
            return
        }

        val rName = _recipientName.value.ifBlank { "NIGERIAN BENEFICIARY" }
        val currentBal = _userBalance.value
        val newBal = (currentBal - amt).coerceAtLeast(0.0)

        viewModelScope.launch {
            _isProcessingTransfer.value = true
            delay(800) // Realistic bank transfer gateway processing

            // Update balance
            _userBalance.value = newBal
            prefs.edit().putString("user_balance", newBal.toString()).apply()

            // Generate authentic SMS Alert
            val ref = SmsAlertGenerator.generateNipReference()
            val isDebit = _alertType.value != "CREDIT"
            val targetBank = if (isDebit) _userBank.value else _recipientBank.value
            val targetAcc = if (isDebit) _userAccountNumber.value else _recipientAccountNumber.value
            val targetBal = if (isDebit) newBal else (amt + 15000.0)
            val counterparty = if (isDebit) rName else _userAccountName.value

            val smsContent = SmsAlertGenerator.generateAlert(
                bank = targetBank,
                isDebit = isDebit,
                amount = amt,
                accountNumber = targetAcc,
                balanceAfter = targetBal,
                counterpartyName = counterparty,
                narration = _narration.value,
                timestamp = System.currentTimeMillis()
            )

            val transaction = TransactionEntity(
                type = if (isDebit) "DEBIT" else "CREDIT",
                amount = amt,
                recipientBank = _recipientBank.value.name,
                recipientAccountNumber = _recipientAccountNumber.value,
                recipientName = rName,
                senderBank = _userBank.value.name,
                senderAccountNumber = _userAccountNumber.value,
                senderName = _userAccountName.value,
                narration = _narration.value,
                balanceAfter = targetBal,
                reference = ref,
                timestamp = System.currentTimeMillis(),
                smsBody = smsContent,
                smsSenderHeader = targetBank.smsSenderId
            )

            val id = repository.insertTransaction(transaction)
            val savedTx = transaction.copy(id = id)

            // Trigger Haptic Vibration for SMS Alert
            vibrateDevice()

            // Automatically dispatch real SMS alert to the recipient phone/account number if enabled
            val destinationPhone = _recipientPhoneNumber.value.ifBlank {
                SmsDispatcher.formatNigerianPhoneNumber(_recipientAccountNumber.value)
            }

            if (_autoSendSms.value && destinationPhone.isNotBlank()) {
                val smsResult = SmsDispatcher.sendSms(
                    context = getApplication(),
                    destination = destinationPhone,
                    message = smsContent
                )
                when (smsResult) {
                    is SmsSendResult.Success -> {
                        _lastSmsDeliveryResult.value = "Sent to $destinationPhone"
                        showToast("SMS Alert automatically dispatched to $destinationPhone")
                    }
                    is SmsSendResult.PermissionRequired -> {
                        _lastSmsDeliveryResult.value = "SMS Permission needed"
                    }
                    is SmsSendResult.Failed -> {
                        _lastSmsDeliveryResult.value = "Failed: ${smsResult.error}"
                    }
                }
            }

            // Trigger Phone SMS Drop-down Notification Banner
            triggerSmsBanner(
                senderHeader = targetBank.smsSenderId,
                body = smsContent,
                isDebit = isDebit,
                bankColor = targetBank.brandColor.value.toLong()
            )

            _isProcessingTransfer.value = false
            _receiptViewMode.value = ReceiptViewMode.POST_TRANSACTION
            _activeReceipt.value = savedTx
            onSuccess(savedTx)

            // Reset input amounts for next transaction
            _transferAmount.value = ""
        }
    }

    fun sendSmsManually(destination: String, message: String) {
        val dest = destination.ifBlank { _recipientAccountNumber.value }
        val result = SmsDispatcher.sendSms(getApplication(), dest, message)
        when (result) {
            is SmsSendResult.Success -> showToast("SMS Alert sent to ${result.destination}")
            is SmsSendResult.PermissionRequired -> showToast("Please grant SMS permission to send alerts")
            is SmsSendResult.Failed -> {
                // Fallback to opening SMS app
                SmsDispatcher.openSmsApp(getApplication(), dest, message)
            }
        }
    }

    fun openInSmsApp(destination: String, message: String) {
        val dest = destination.ifBlank { _recipientAccountNumber.value }
        val opened = SmsDispatcher.openSmsApp(getApplication(), dest, message)
        if (!opened) {
            showToast("Could not open messaging application")
        }
    }

    private fun triggerSmsBanner(
        senderHeader: String,
        body: String,
        isDebit: Boolean,
        bankColor: Long
    ) {
        bannerDismissJob?.cancel()
        _smsBanner.value = SmsBannerData(
            senderHeader = senderHeader,
            body = body,
            isDebit = isDebit,
            bankColor = bankColor
        )
        bannerDismissJob = viewModelScope.launch {
            delay(9000) // Visible for 9 seconds
            _smsBanner.value = null
        }
    }

    fun dismissSmsBanner() {
        bannerDismissJob?.cancel()
        _smsBanner.value = null
    }

    fun showReceipt(transaction: TransactionEntity, mode: ReceiptViewMode = ReceiptViewMode.HISTORY_TAP) {
        _receiptViewMode.value = mode
        _activeReceipt.value = transaction
    }

    fun openReceipt(transaction: TransactionEntity, mode: ReceiptViewMode = ReceiptViewMode.HISTORY_TAP) {
        showReceipt(transaction, mode)
    }

    fun triggerSmsAlert(
        type: String,
        amount: Double,
        counterparty: String,
        bank: NigerianBank,
        userAccountNumber: String
    ) {
        val currentBal = _userBalance.value
        val isDebit = type == "DEBIT"
        val newBal = if (isDebit) (currentBal - amount).coerceAtLeast(0.0) else currentBal + amount
        _userBalance.value = newBal
        prefs.edit().putString("user_balance", newBal.toString()).apply()

        viewModelScope.launch {
            val ref = SmsAlertGenerator.generateNipReference()
            val smsContent = SmsAlertGenerator.generateAlert(
                bank = bank,
                isDebit = isDebit,
                amount = amount,
                accountNumber = userAccountNumber,
                balanceAfter = newBal,
                counterpartyName = counterparty,
                narration = if (isDebit) "Cash Withdrawal" else "Wallet Topup",
                timestamp = System.currentTimeMillis()
            )

            val tx = TransactionEntity(
                type = type,
                amount = amount,
                recipientBank = if (isDebit) "ATM Cash" else bank.name,
                recipientAccountNumber = if (isDebit) "ATM" else userAccountNumber,
                recipientName = counterparty,
                senderBank = bank.name,
                senderAccountNumber = userAccountNumber,
                senderName = _userAccountName.value,
                narration = if (isDebit) "Cash Withdrawal" else "Wallet Topup",
                balanceAfter = newBal,
                reference = ref,
                timestamp = System.currentTimeMillis(),
                smsBody = smsContent,
                smsSenderHeader = bank.smsSenderId
            )

            val id = repository.insertTransaction(tx)
            val savedTx = tx.copy(id = id)
            triggerSmsBanner(
                senderHeader = bank.smsSenderId,
                body = smsContent,
                isDebit = isDebit,
                bankColor = bank.brandColor.value.toLong()
            )
            _activeReceipt.value = savedTx
        }
    }

    fun dismissReceipt() {
        _activeReceipt.value = null
    }

    fun showSmsDetail(transaction: TransactionEntity) {
        _activeSmsDetail.value = transaction
    }

    fun dismissSmsDetail() {
        _activeSmsDetail.value = null
    }

    // Direct Hub generator functions
    fun setHubBank(bank: NigerianBank) {
        _hubBank.value = bank
        updateHubLiveAlert()
    }

    fun setHubType(type: String) {
        _hubType.value = type
        updateHubLiveAlert()
    }

    fun setHubAmount(amount: String) {
        _hubAmount.value = amount
        updateHubLiveAlert()
    }

    fun setHubAccountNumber(acc: String) {
        _hubAccountNumber.value = acc
        updateHubLiveAlert()
    }

    fun setHubBalance(bal: String) {
        _hubBalance.value = bal
        updateHubLiveAlert()
    }

    fun setHubCounterparty(party: String) {
        _hubCounterparty.value = party
        updateHubLiveAlert()
    }

    fun setHubNarration(narration: String) {
        _hubNarration.value = narration
        updateHubLiveAlert()
    }

    fun updateHubLiveAlert() {
        val amt = _hubAmount.value.toDoubleOrNull() ?: 25000.0
        val bal = _hubBalance.value.toDoubleOrNull() ?: 460500.0
        val isDebit = _hubType.value == "DEBIT"
        val alert = SmsAlertGenerator.generateAlert(
            bank = _hubBank.value,
            isDebit = isDebit,
            amount = amt,
            accountNumber = _hubAccountNumber.value.ifBlank { "0239481729" },
            balanceAfter = bal,
            counterpartyName = _hubCounterparty.value.ifBlank { "CHUKWUMA ADEBAYO" },
            narration = _hubNarration.value.ifBlank { "Transfer" },
            timestamp = System.currentTimeMillis()
        )
        _hubLiveAlert.value = alert
    }

    fun fireHubNotification() {
        vibrateDevice()
        triggerSmsBanner(
            senderHeader = _hubBank.value.smsSenderId,
            body = _hubLiveAlert.value,
            isDebit = _hubType.value == "DEBIT",
            bankColor = _hubBank.value.brandColor.value.toLong()
        )
        // Also save to transactions
        viewModelScope.launch {
            val amt = _hubAmount.value.toDoubleOrNull() ?: 25000.0
            val bal = _hubBalance.value.toDoubleOrNull() ?: 460500.0
            val isDebit = _hubType.value == "DEBIT"
            val tx = TransactionEntity(
                type = if (isDebit) "DEBIT" else "CREDIT",
                amount = amt,
                recipientBank = _hubBank.value.name,
                recipientAccountNumber = _hubAccountNumber.value,
                recipientName = _hubCounterparty.value,
                senderBank = _userBank.value.name,
                senderAccountNumber = _userAccountNumber.value,
                senderName = _userAccountName.value,
                narration = _hubNarration.value,
                balanceAfter = bal,
                reference = SmsAlertGenerator.generateNipReference(),
                smsBody = _hubLiveAlert.value,
                smsSenderHeader = _hubBank.value.smsSenderId
            )
            repository.insertTransaction(tx)
            showToast("SMS Alert generated & sent to notification bar!")
        }
    }

    fun copySmsToClipboard(sms: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Bank SMS Alert", sms)
        clipboard.setPrimaryClip(clip)
        showToast("SMS alert copied to clipboard!")
    }

    fun shareSms(sms: String, senderHeader: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "[$senderHeader]\n$sms")
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Bank SMS Alert via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(shareIntent)
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            showToast("Transaction removed")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            showToast("History cleared")
        }
    }

    fun updateUserProfile(name: String, bank: NigerianBank, accountNumber: String, balance: Double) {
        _userAccountName.value = name
        _userBank.value = bank
        _userAccountNumber.value = accountNumber
        _userBalance.value = balance
        prefs.edit()
            .putString("user_name", name)
            .putString("user_account", accountNumber)
            .putString("user_balance", balance.toString())
            .putString("user_bank_id", bank.id)
            .apply()
        showToast("Account details updated")
    }

    private fun createTimestamp(year: Int, month: Int, day: Int, hour: Int, min: Int, sec: Int): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(year, month - 1, day, hour, min, sec)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun seedInitialTransactionsIfEmpty() {
        viewModelScope.launch {
            val hasSeeded = prefs.getBoolean("has_seeded_opay_screenshot_v4", false)
            val existing = repository.getAllTransactions().first()
            if (!hasSeeded || existing.isEmpty()) {
                if (!hasSeeded) {
                    repository.clearAll()
                }
                val userAcc = _userAccountNumber.value
                val userName = _userAccountName.value

                val seeds = listOf(
                    // 1. Sep 14th, 20:20:59 - Transfer from GLORY ORUORO (+₦550.00)
                    TransactionEntity(
                        type = "CREDIT",
                        amount = 550.00,
                        recipientBank = "OPay",
                        recipientAccountNumber = userAcc,
                        recipientName = userName,
                        senderBank = "OPay",
                        senderAccountNumber = "8129481920",
                        senderName = "GLORY ORUORO",
                        narration = "Transfer from GLORY ORUORO",
                        balanceAfter = 485500.00,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 14, 20, 20, 59),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = false,
                            amount = 550.00,
                            accountNumber = userAcc,
                            balanceAfter = 485500.00,
                            counterpartyName = "GLORY ORUORO",
                            narration = "Transfer from GLORY ORUORO",
                            timestamp = createTimestamp(2026, 9, 14, 20, 20, 59)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    // 2. Sep 14th, 01:16:40 - OWealth Interest Earned (+₦0.09)
                    TransactionEntity(
                        type = "CREDIT",
                        amount = 0.09,
                        recipientBank = "OPay",
                        recipientAccountNumber = userAcc,
                        recipientName = userName,
                        senderBank = "OPay",
                        senderAccountNumber = "0000000000",
                        senderName = "OPay OWealth",
                        narration = "OWealth Interest Earned",
                        balanceAfter = 484950.00,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 14, 1, 16, 40),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = false,
                            amount = 0.09,
                            accountNumber = userAcc,
                            balanceAfter = 484950.00,
                            counterpartyName = "OPay OWealth",
                            narration = "Daily Interest",
                            timestamp = createTimestamp(2026, 9, 14, 1, 16, 40)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    // 3. Sep 13th, 21:09:14 - VAT on Transfer Fee (-₦0.70)
                    TransactionEntity(
                        type = "DEBIT",
                        amount = 0.70,
                        recipientBank = "Central Bank",
                        recipientAccountNumber = "0000000001",
                        recipientName = "VAT on Transfer Fee",
                        senderBank = "OPay",
                        senderAccountNumber = userAcc,
                        senderName = userName,
                        narration = "VAT on Transfer Fee",
                        balanceAfter = 484949.91,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 13, 21, 9, 14),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = true,
                            amount = 0.70,
                            accountNumber = userAcc,
                            balanceAfter = 484949.91,
                            counterpartyName = "VAT on Transfer Fee",
                            narration = "VAT on Transfer Fee",
                            timestamp = createTimestamp(2026, 9, 13, 21, 9, 14)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    // 4. Sep 13th, 21:09:13 - Transfer to Subwallet Telecom-Iru(XIXAPAY) (-₦490.00)
                    TransactionEntity(
                        type = "DEBIT",
                        amount = 490.00,
                        recipientBank = "PalmPay",
                        recipientAccountNumber = "6634088211",
                        recipientName = "Subwallet Telecom-Iru(XIXAPAY)",
                        senderBank = "OPay",
                        senderAccountNumber = userAcc,
                        senderName = userName,
                        narration = "Data Bundle Purchase",
                        balanceAfter = 484950.61,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 13, 21, 9, 13),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = true,
                            amount = 490.00,
                            accountNumber = userAcc,
                            balanceAfter = 484950.61,
                            counterpartyName = "Subwallet Telecom-Iru(XIXAPAY)",
                            narration = "Data Bundle Purchase",
                            timestamp = createTimestamp(2026, 9, 13, 21, 9, 13)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    // 5. Sep 13th, 19:57:59 - Transfer from VICTORY OMOVIGHO (+₦200.00)
                    TransactionEntity(
                        type = "CREDIT",
                        amount = 200.00,
                        recipientBank = "OPay",
                        recipientAccountNumber = userAcc,
                        recipientName = userName,
                        senderBank = "Moniepoint MFB",
                        senderAccountNumber = "8012345678",
                        senderName = "VICTORY OMOVIGHO",
                        narration = "Transfer from VICTORY OMOVIGHO",
                        balanceAfter = 485440.61,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 13, 19, 57, 59),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = false,
                            amount = 200.00,
                            accountNumber = userAcc,
                            balanceAfter = 485440.61,
                            counterpartyName = "VICTORY OMOVIGHO",
                            narration = "Transfer from VICTORY OMOVIGHO",
                            timestamp = createTimestamp(2026, 9, 13, 19, 57, 59)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    // 6. Sep 13th, 16:54:32 - Transfer to UTEME OGEZO (-₦300.00)
                    TransactionEntity(
                        type = "DEBIT",
                        amount = 300.00,
                        recipientBank = "Access Bank",
                        recipientAccountNumber = "0123984712",
                        recipientName = "UTEME OGEZO",
                        senderBank = "OPay",
                        senderAccountNumber = userAcc,
                        senderName = userName,
                        narration = "Transfer",
                        balanceAfter = 485240.61,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 13, 16, 54, 32),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = true,
                            amount = 300.00,
                            accountNumber = userAcc,
                            balanceAfter = 485240.61,
                            counterpartyName = "UTEME OGEZO",
                            narration = "Transfer",
                            timestamp = createTimestamp(2026, 9, 13, 16, 54, 32)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    // 7. Sep 13th, 11:00:21 - Transfer to UTEME OGEZO (-₦300.00)
                    TransactionEntity(
                        type = "DEBIT",
                        amount = 300.00,
                        recipientBank = "Access Bank",
                        recipientAccountNumber = "0123984712",
                        recipientName = "UTEME OGEZO",
                        senderBank = "OPay",
                        senderAccountNumber = userAcc,
                        senderName = userName,
                        narration = "Transfer",
                        balanceAfter = 485540.61,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 13, 11, 0, 21),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = true,
                            amount = 300.00,
                            accountNumber = userAcc,
                            balanceAfter = 485540.61,
                            counterpartyName = "UTEME OGEZO",
                            narration = "Transfer",
                            timestamp = createTimestamp(2026, 9, 13, 11, 0, 21)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    // 8. Sep 13th, 07:46:53 - Transfer to UTEME OGEZO (-₦300.00)
                    TransactionEntity(
                        type = "DEBIT",
                        amount = 300.00,
                        recipientBank = "Access Bank",
                        recipientAccountNumber = "0123984712",
                        recipientName = "UTEME OGEZO",
                        senderBank = "OPay",
                        senderAccountNumber = userAcc,
                        senderName = userName,
                        narration = "Transfer",
                        balanceAfter = 485840.61,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 13, 7, 46, 53),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = true,
                            amount = 300.00,
                            accountNumber = userAcc,
                            balanceAfter = 485840.61,
                            counterpartyName = "UTEME OGEZO",
                            narration = "Transfer",
                            timestamp = createTimestamp(2026, 9, 13, 7, 46, 53)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    // Additional transactions in Sep 2026 matching total In: ₦32,060.18, Out: ₦33,490.70
                    TransactionEntity(
                        type = "DEBIT",
                        amount = 20100.00,
                        recipientBank = "Zenith Bank",
                        recipientAccountNumber = "2019481726",
                        recipientName = "JUMIA ONLINE STORE",
                        senderBank = "OPay",
                        senderAccountNumber = userAcc,
                        senderName = userName,
                        narration = "Goods purchase",
                        balanceAfter = 486140.61,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 8, 17, 42, 0),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = true,
                            amount = 20100.00,
                            accountNumber = userAcc,
                            balanceAfter = 486140.61,
                            counterpartyName = "JUMIA ONLINE STORE",
                            narration = "Goods purchase",
                            timestamp = createTimestamp(2026, 9, 8, 17, 42, 0)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    TransactionEntity(
                        type = "CREDIT",
                        amount = 21310.09,
                        recipientBank = "OPay",
                        recipientAccountNumber = userAcc,
                        recipientName = userName,
                        senderBank = "GTBank",
                        senderAccountNumber = "0194817263",
                        senderName = "DANIEL EZE",
                        narration = "Transfer from DANIEL EZE",
                        balanceAfter = 506240.61,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 5, 14, 30, 10),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = false,
                            amount = 21310.09,
                            accountNumber = userAcc,
                            balanceAfter = 506240.61,
                            counterpartyName = "DANIEL EZE",
                            narration = "Transfer from DANIEL EZE",
                            timestamp = createTimestamp(2026, 9, 5, 14, 30, 10)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    TransactionEntity(
                        type = "DEBIT",
                        amount = 12000.00,
                        recipientBank = "Kuda Bank",
                        recipientAccountNumber = "2001928471",
                        recipientName = "EKITI POWER DISCO",
                        senderBank = "OPay",
                        senderAccountNumber = userAcc,
                        senderName = userName,
                        narration = "Electricity Bill",
                        balanceAfter = 484930.52,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 3, 11, 20, 0),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = true,
                            amount = 12000.00,
                            accountNumber = userAcc,
                            balanceAfter = 484930.52,
                            counterpartyName = "EKITI POWER DISCO",
                            narration = "Electricity Bill",
                            timestamp = createTimestamp(2026, 9, 3, 11, 20, 0)
                        ),
                        smsSenderHeader = "OPay"
                    ),
                    TransactionEntity(
                        type = "CREDIT",
                        amount = 10000.00,
                        recipientBank = "OPay",
                        recipientAccountNumber = userAcc,
                        recipientName = userName,
                        senderBank = "United Bank for Africa",
                        senderAccountNumber = "1029384756",
                        senderName = "SALISU IBRAHIM",
                        narration = "Transfer from SALISU IBRAHIM",
                        balanceAfter = 496930.52,
                        reference = SmsAlertGenerator.generateNipReference(),
                        timestamp = createTimestamp(2026, 9, 1, 9, 15, 0),
                        smsBody = SmsAlertGenerator.generateAlert(
                            bank = NigerianBankData.getBankById("opay"),
                            isDebit = false,
                            amount = 10000.00,
                            accountNumber = userAcc,
                            balanceAfter = 496930.52,
                            counterpartyName = "SALISU IBRAHIM",
                            narration = "Transfer from SALISU IBRAHIM",
                            timestamp = createTimestamp(2026, 9, 1, 9, 15, 0)
                        ),
                        smsSenderHeader = "OPay"
                    )
                )

                seeds.forEach { repository.insertTransaction(it) }
                prefs.edit().putBoolean("has_seeded_opay_screenshot_v4", true).apply()
            }
        }
    }

    fun saveCustomTransaction(
        type: String,
        amount: Double,
        bank: NigerianBank,
        accountNumber: String,
        counterpartyName: String,
        narration: String
    ) {
        val isDebit = type == "DEBIT"
        val currentBal = _userBalance.value
        val newBal = if (isDebit) (currentBal - amount).coerceAtLeast(0.0) else currentBal + amount
        _userBalance.value = newBal
        prefs.edit().putString("user_balance", newBal.toString()).apply()

        viewModelScope.launch {
            val ref = SmsAlertGenerator.generateNipReference()
            val now = System.currentTimeMillis()
            val targetBank = _userBank.value
            val targetAcc = _userAccountNumber.value
            val smsContent = SmsAlertGenerator.generateAlert(
                bank = targetBank,
                isDebit = isDebit,
                amount = amount,
                accountNumber = targetAcc,
                balanceAfter = newBal,
                counterpartyName = counterpartyName,
                narration = narration,
                timestamp = now
            )

            val tx = TransactionEntity(
                type = type,
                amount = amount,
                recipientBank = if (isDebit) bank.name else targetBank.name,
                recipientAccountNumber = if (isDebit) accountNumber else targetAcc,
                recipientName = if (isDebit) counterpartyName else _userAccountName.value,
                senderBank = if (isDebit) targetBank.name else bank.name,
                senderAccountNumber = if (isDebit) targetAcc else accountNumber,
                senderName = if (isDebit) _userAccountName.value else counterpartyName,
                narration = narration,
                balanceAfter = newBal,
                reference = ref,
                timestamp = now,
                smsBody = smsContent,
                smsSenderHeader = targetBank.smsSenderId
            )

            val id = repository.insertTransaction(tx)
            val savedTx = tx.copy(id = id)
            showToast("Transaction saved to history!")
            triggerSmsBanner(
                senderHeader = targetBank.smsSenderId,
                body = smsContent,
                isDebit = isDebit,
                bankColor = targetBank.brandColor.value.toLong()
            )
            _activeReceipt.value = savedTx
        }
    }

    fun exportStatement(context: Context, txList: List<TransactionEntity>) {
        val df = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("       OPAY ACCOUNT TRANSACTION STATEMENT  ")
        sb.appendLine("==========================================")
        sb.appendLine("Account Name: ${_userAccountName.value}")
        sb.appendLine("Account No:   ${_userAccountNumber.value}")
        sb.appendLine("Bank:         ${_userBank.value.name}")
        sb.appendLine("Current Bal:  ${SmsAlertGenerator.formatNaira(_userBalance.value)}")
        sb.appendLine("Total Records: ${txList.size}")
        sb.appendLine("Exported:     ${df.format(Date())}")
        sb.appendLine("------------------------------------------")
        sb.appendLine(String.format("%-11s | %-6s | %-12s | %s", "DATE", "TYPE", "AMOUNT", "BENEFICIARY/REASON"))
        sb.appendLine("------------------------------------------")

        for (tx in txList) {
            val date = SimpleDateFormat("dd-MMM", Locale.US).format(Date(tx.timestamp))
            val type = if (tx.type == "DEBIT") "DR" else "CR"
            val amt = (if (tx.type == "DEBIT") "-" else "+") + SmsAlertGenerator.formatNaira(tx.amount)
            val desc = "${tx.recipientName} (${tx.narration.ifBlank { "Transfer" }})"
            sb.appendLine(String.format("%-11s | %-6s | %-12s | %s", date, type, amt, desc))
            sb.appendLine("  Ref: ${tx.reference}")
        }
        sb.appendLine("==========================================")
        sb.appendLine("Generated by OPay Bank Simulator")

        val statementText = sb.toString()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("OPay Account Statement", statementText)
        clipboard.setPrimaryClip(clip)

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, statementText)
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Save / Export Statement via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(shareIntent)
        showToast("Statement saved to clipboard & ready to share!")
    }

    private fun vibrateDevice() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 200), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 150, 100, 200), -1)
            }
        } catch (_: Exception) {
            // Ignore if vibration fails
        }
    }

    fun sendAiMessage(userPrompt: String) {
        if (userPrompt.isBlank() || _isAiLoading.value) return

        val userMessage = ChatMessage(text = userPrompt.trim(), isUser = true)
        val currentList = _aiMessages.value.toMutableList()
        currentList.add(userMessage)
        _aiMessages.value = currentList
        _isAiLoading.value = true

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val contextData = """
                    You are OPay's AI Financial Assistant inside an Android app replicating Nigerian banking & OPay operations.
                    Current User Name: ${_userAccountName.value}
                    Current Bank: ${_userBank.value.name}
                    Account Number: ${_userAccountNumber.value}
                    Current Simulated Balance: ₦${"%,.2f".format(Locale.US, _userBalance.value)}
                    Total Transactions Recorded: ${transactions.value.size}
                    Be polite, helpful, concise, and knowledgeable about Nigerian banking, NUBAN accounts, NIP instant payments, OPay wallet features, and financial safety.
                """.trimIndent()

                val responseText = if (apiKey.isBlank()) {
                    "I am your OPay AI Assistant. Currently, GEMINI_API_KEY is not configured in the Secrets panel, but I can assist you with general questions about OPay, transfers, and your current balance of ₦${"%,.2f".format(Locale.US, _userBalance.value)}!"
                } else {
                    withContext(Dispatchers.IO) {
                        try {
                            val request = GeminiGenerateContentRequest(
                                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = contextData))),
                                contents = listOf(
                                    GeminiContent(role = "user", parts = listOf(GeminiPart(text = userPrompt)))
                                )
                            )
                            val response = GeminiApiClient.service.generateContent(apiKey, request)
                            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                ?: "I couldn't process that query right now. Please try again."
                        } catch (e: Exception) {
                            "Unable to reach AI service: ${e.localizedMessage ?: "Network error"}. Please check your connection or GEMINI_API_KEY."
                        }
                    }
                }

                val aiMsg = ChatMessage(text = responseText, isUser = false)
                _aiMessages.value = _aiMessages.value + aiMsg
            } catch (e: Exception) {
                _aiMessages.value = _aiMessages.value + ChatMessage(
                    text = "Sorry, something went wrong: ${e.message}",
                    isUser = false
                )
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }
}
