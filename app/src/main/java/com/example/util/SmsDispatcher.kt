package com.example.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat

sealed class SmsSendResult {
    data class Success(val destination: String, val partsCount: Int) : SmsSendResult()
    object PermissionRequired : SmsSendResult()
    data class Failed(val error: String) : SmsSendResult()
}

object SmsDispatcher {
    private const val TAG = "SmsDispatcher"

    /**
     * Checks if SEND_SMS runtime permission is granted.
     */
    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Normalizes a Nigerian account number or phone number for SMS dispatch.
     * In Nigeria:
     * - 10-digit numbers starting with 7, 8, or 9 (OPay, PalmPay, standard mobile without leading 0) -> "0$digits"
     * - 11-digit numbers starting with 0 -> standard local mobile
     * - 13-digit numbers starting with 234 -> "+$digits"
     */
    fun formatNigerianPhoneNumber(input: String): String {
        val trimmed = input.trim()
        if (trimmed.startsWith("+")) {
            return trimmed
        }
        val digits = trimmed.filter { it.isDigit() }
        return when {
            digits.startsWith("234") && digits.length == 13 -> "+$digits"
            digits.length == 10 && (digits.startsWith("7") || digits.startsWith("8") || digits.startsWith("9")) -> "0$digits"
            digits.length == 11 && digits.startsWith("0") -> digits
            digits.length == 10 -> "0$digits"
            digits.isNotBlank() -> digits
            else -> input
        }
    }

    /**
     * Automatically dispatches an SMS message using Android's SmsManager.
     * Splits long text into multipart SMS if message exceeds 160 characters.
     */
    fun sendSms(context: Context, destination: String, message: String): SmsSendResult {
        if (!hasSmsPermission(context)) {
            Log.w(TAG, "SEND_SMS permission not granted")
            return SmsSendResult.PermissionRequired
        }

        val formattedDestination = formatNigerianPhoneNumber(destination)
        if (formattedDestination.isBlank()) {
            return SmsSendResult.Failed("Destination number cannot be empty")
        }

        return try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
                    ?: @Suppress("DEPRECATION") SmsManager.getDefault()
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            if (message.length > 160) {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(
                    formattedDestination,
                    null,
                    parts,
                    null,
                    null
                )
                Log.d(TAG, "Dispatched multipart SMS (${parts.size} parts) to $formattedDestination")
                SmsSendResult.Success(formattedDestination, parts.size)
            } else {
                smsManager.sendTextMessage(
                    formattedDestination,
                    null,
                    message,
                    null,
                    null
                )
                Log.d(TAG, "Dispatched single-part SMS to $formattedDestination")
                SmsSendResult.Success(formattedDestination, 1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $formattedDestination", e)
            SmsSendResult.Failed(e.message ?: "Unknown SMS sending error")
        }
    }

    /**
     * Opens the default SMS application with recipient and message pre-populated.
     * Useful as a fallback or user-initiated alternative.
     */
    fun openSmsApp(context: Context, destination: String, message: String): Boolean {
        return try {
            val formattedDestination = formatNigerianPhoneNumber(destination)
            val uri = Uri.parse("smsto:${Uri.encode(formattedDestination)}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Could not open default SMS app", e)
            false
        }
    }
}
