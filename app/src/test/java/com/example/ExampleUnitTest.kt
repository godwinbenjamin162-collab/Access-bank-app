package com.example

import com.example.data.NigerianBankData
import com.example.util.SmsDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testNigerianPhoneNumberFormatting() {
    // 10-digit Nigerian mobile/fintech account (e.g. OPay/PalmPay)
    assertEquals("08031234567", SmsDispatcher.formatNigerianPhoneNumber("8031234567"))
    assertEquals("07012345678", SmsDispatcher.formatNigerianPhoneNumber("7012345678"))
    assertEquals("09098765432", SmsDispatcher.formatNigerianPhoneNumber("9098765432"))

    // Standard 11-digit mobile starting with 0
    assertEquals("08123456789", SmsDispatcher.formatNigerianPhoneNumber("08123456789"))

    // 13-digit international without plus
    assertEquals("+2348031234567", SmsDispatcher.formatNigerianPhoneNumber("2348031234567"))

    // Pre-formatted international
    assertEquals("+2348031234567", SmsDispatcher.formatNigerianPhoneNumber("+2348031234567"))
  }

  @Test
  fun testNigerianAccountNameResolution() {
    val gtbank = NigerianBankData.getBankByName("GTBank")
    val zenith = NigerianBankData.getBankByName("Zenith Bank")

    // Known / standard accounts
    assertEquals("BENJAMIN GODWIN", NigerianBankData.resolveAccountName("0239481729", gtbank))
    assertEquals("BENJAMIN GODWIN", NigerianBankData.resolveAccountName("8031234567", gtbank))

    // Valid 10-digit account resolves with authentic non-empty NUBAN format
    val name1 = NigerianBankData.resolveAccountName("2145893021", gtbank)
    assertTrue(name1.isNotBlank())
    val words = name1.split(" ")
    assertTrue("Name should contain at least first and last name", words.size >= 2)
    // Characters should be valid uppercase letters
    assertTrue(name1.all { it.isLetter() || it == ' ' })

    // Incomplete account number resolves to empty string
    assertEquals("", NigerianBankData.resolveAccountName("12345", gtbank))
  }
}

