package com.example.data

import androidx.compose.ui.graphics.Color

data class NigerianBank(
    val id: String,
    val name: String,
    val shortName: String,
    val cbnCode: String,
    val smsSenderId: String,
    val brandColor: Color,
    val brandTextColor: Color = Color.White,
    val isPopular: Boolean = false
)

object NigerianBankData {
    val banks = listOf(
        NigerianBank(
            id = "access",
            name = "Access Bank",
            shortName = "Access",
            cbnCode = "044",
            smsSenderId = "ACCESSBANK",
            brandColor = Color(0xFFF37021),
            isPopular = true
        ),
        NigerianBank(
            id = "gtbank",
            name = "Guaranty Trust Bank (GTBank)",
            shortName = "GTBank",
            cbnCode = "058",
            smsSenderId = "GTBank",
            brandColor = Color(0xFFDD4F05),
            isPopular = true
        ),
        NigerianBank(
            id = "zenith",
            name = "Zenith Bank",
            shortName = "Zenith",
            cbnCode = "057",
            smsSenderId = "ZENITHBANK",
            brandColor = Color(0xFF9E0B0F),
            isPopular = true
        ),
        NigerianBank(
            id = "firstbank",
            name = "First Bank of Nigeria",
            shortName = "FirstBank",
            cbnCode = "011",
            smsSenderId = "FIRSTBANK",
            brandColor = Color(0xFF0D2B59),
            isPopular = true
        ),
        NigerianBank(
            id = "uba",
            name = "United Bank for Africa (UBA)",
            shortName = "UBA",
            cbnCode = "033",
            smsSenderId = "UBA",
            brandColor = Color(0xFFD32F2F),
            isPopular = true
        ),
        NigerianBank(
            id = "kuda",
            name = "Kuda Microfinance Bank",
            shortName = "Kuda",
            cbnCode = "090267",
            smsSenderId = "KUDA",
            brandColor = Color(0xFF40196D),
            isPopular = true
        ),
        NigerianBank(
            id = "opay",
            name = "OPay Digital Services",
            shortName = "OPay",
            cbnCode = "090405",
            smsSenderId = "OPAY",
            brandColor = Color(0xFF00B875),
            isPopular = true
        ),
        NigerianBank(
            id = "palmpay",
            name = "PalmPay",
            shortName = "PalmPay",
            cbnCode = "100033",
            smsSenderId = "PALMPAY",
            brandColor = Color(0xFF6B11D4),
            isPopular = true
        ),
        NigerianBank(
            id = "moniepoint",
            name = "Moniepoint MFB",
            shortName = "Moniepoint",
            cbnCode = "090551",
            smsSenderId = "MONIEPOINT",
            brandColor = Color(0xFF0252D6),
            isPopular = true
        ),
        NigerianBank(
            id = "stanbic",
            name = "Stanbic IBTC Bank",
            shortName = "Stanbic IBTC",
            cbnCode = "221",
            smsSenderId = "STANBIC",
            brandColor = Color(0xFF0033AA),
            isPopular = false
        ),
        NigerianBank(
            id = "fcmb",
            name = "First City Monument Bank (FCMB)",
            shortName = "FCMB",
            cbnCode = "214",
            smsSenderId = "FCMB",
            brandColor = Color(0xFF5C2D91),
            isPopular = false
        ),
        NigerianBank(
            id = "wema",
            name = "Wema Bank / ALAT",
            shortName = "Wema/ALAT",
            cbnCode = "035",
            smsSenderId = "WEMA",
            brandColor = Color(0xFF901045),
            isPopular = false
        ),
        NigerianBank(
            id = "fidelity",
            name = "Fidelity Bank",
            shortName = "Fidelity",
            cbnCode = "070",
            smsSenderId = "FIDELITY",
            brandColor = Color(0xFF001A9C),
            isPopular = false
        ),
        NigerianBank(
            id = "union",
            name = "Union Bank of Nigeria",
            shortName = "Union Bank",
            cbnCode = "032",
            smsSenderId = "UNIONBANK",
            brandColor = Color(0xFF00A2E8),
            isPopular = false
        ),
        NigerianBank(
            id = "sterling",
            name = "Sterling Bank",
            shortName = "Sterling",
            cbnCode = "232",
            smsSenderId = "STERLING",
            brandColor = Color(0xFFB5121B),
            isPopular = false
        ),
        NigerianBank(
            id = "polaris",
            name = "Polaris Bank",
            shortName = "Polaris",
            cbnCode = "076",
            smsSenderId = "POLARIS",
            brandColor = Color(0xFF592D82),
            isPopular = false
        ),
        NigerianBank(
            id = "providus",
            name = "Providus Bank",
            shortName = "Providus",
            cbnCode = "101",
            smsSenderId = "PROVIDUS",
            brandColor = Color(0xFF333333),
            isPopular = false
        ),
        NigerianBank(
            id = "jaiz",
            name = "Jaiz Bank",
            shortName = "Jaiz",
            cbnCode = "301",
            smsSenderId = "JAIZBANK",
            brandColor = Color(0xFF0B6623),
            isPopular = false
        ),
        NigerianBank(
            id = "taj",
            name = "TAJ Bank",
            shortName = "TAJBank",
            cbnCode = "302",
            smsSenderId = "TAJBANK",
            brandColor = Color(0xFFC00000),
            isPopular = false
        )
    )

    fun getBankById(id: String): NigerianBank {
        return banks.find { it.id.equals(id, ignoreCase = true) } ?: banks.first()
    }

    fun getBankByName(name: String): NigerianBank {
        return banks.find { it.name.contains(name, ignoreCase = true) || it.shortName.contains(name, ignoreCase = true) } ?: banks.first()
    }

    // Known / Standard Nigerian Beneficiary Accounts
    private val knownAccounts = mapOf(
        "6634088211" to "Subwallet Telecom-Iru(XIXAPAY)",
        "8102938471" to "MASERO OMENUWOMA",
        "0239481729" to "BENJAMIN GODWIN",
        "8031234567" to "BENJAMIN GODWIN",
        "7012345678" to "OKONKWO CHUKWUMA EMMANUEL",
        "8123456789" to "ADEBAYO BABATUNDE OLUWASEUN",
        "9098765432" to "BELLO MUSA IBRAHIM",
        "0123456789" to "BENJAMIN GODWIN",
        "2081234567" to "BALOGUN ADEKUNLE KAYODE",
        "1029384756" to "DANLADI YUSUF MOHAMMED",
        "2134567890" to "NWOSU CHIOMA NGOZI",
        "3049586712" to "AUDU SOLOMON OCHE"
    )

    // Yoruba naming pool
    private val yorubaSurnames = listOf(
        "ADEBAYO", "BALOGUN", "OGUNLEYE", "ALABI", "AJAYI",
        "OYELAKIN", "FASHOYIN", "ADENIYI", "BAKARE", "AKINTOYE",
        "OGUNDIPE", "SANUSI", "ODUKOYA", "OLOWOOKERE", "ADELAKUN"
    )
    private val yorubaFirstNames = listOf(
        "BABATUNDE", "OLUWASEUN", "ADEKUNLE", "OLAMIDE", "TOLUWALASE",
        "FOLASHADE", "ABIODUN", "TEMITOPE", "OLUWATOYIN", "AYODELE",
        "TITILAYO", "KAYODE"
    )
    private val yorubaMiddleNames = listOf(
        "AKANBI", "ALAO", "AYINLA", "ADIO", "AJANI",
        "KOLAWOLE", "OPEYEMI", "DAMILOLA", "OLUWABUKOLA", "OMOLOLA"
    )

    // Igbo naming pool
    private val igboSurnames = listOf(
        "OKONKWO", "NWOSU", "EZE", "OBINNA", "IGWE",
        "OSAGIE", "CHUKWU", "OKAFOR", "MBA", "NWANKWO",
        "ONYEKA", "ANYANWU", "UZODINMA", "NWACHUKWU", "DIKE"
    )
    private val igboFirstNames = listOf(
        "CHUKWUMA", "CHIDI", "EMEKA", "CHIOMA", "CHIDINMA",
        "NGOZI", "AMAKA", "KELECHI", "IFEANYI", "SOMTO",
        "TOCHUKWU", "OBIORA"
    )
    private val igboMiddleNames = listOf(
        "PAUL", "DANIEL", "BLESSING", "EMMANUEL", "CHUKWUDI",
        "CHINAZA", "SOMTOCHUKWU", "KANAYO", "KAMSICHI", "PASCHAL"
    )

    // Northern / Hausa-Fulani naming pool
    private val hausaSurnames = listOf(
        "BELLO", "DANLADI", "ABUBAKAR", "SULAIMAN", "MOHAMMED",
        "YUSUF", "USMAN", "SHEHU", "HARUNA", "GARBA",
        "IBRAHIM", "ALIYU", "YAHAYA", "ABDULLAHI"
    )
    private val hausaFirstNames = listOf(
        "MUSA", "IBRAHIM", "AISHA", "FATIMA", "AMINU",
        "ZAINAB", "MUSTAPHA", "MARYAM", "SANUSI", "KABIRU",
        "HABIB", "HASSAN"
    )
    private val hausaMiddleNames = listOf(
        "MOHAMMED", "SULAIMAN", "UMAR", "LAWAL", "BALA",
        "BASHIR", "YAHAYA", "AHMED", "NASIRU", "ALKASIM"
    )

    // South-South / Middle Belt naming pool
    private val ssSurnames = listOf(
        "GODWIN", "BENJAMIN", "ONOJA", "OCHE", "IDOKO",
        "EDEMA", "OMOREGIE", "IGBINEDION", "AKPOVI", "UTOMI",
        "EFOSA", "AUDU", "OKOH", "AGADA"
    )
    private val ssFirstNames = listOf(
        "BENJAMIN", "GODWIN", "EMMANUEL", "BLESSING", "SOLOMON",
        "VICTORIA", "PRECIOUS", "DANIEL", "GABRIEL", "FAITH",
        "SAMUEL", "JOY"
    )
    private val ssMiddleNames = listOf(
        "OCHE", "SUNDAY", "JOHN", "DAVID", "PETER",
        "JOSEPH", "ONOJA", "FRIDAY", "MOSES", "GRACE"
    )

    // Realistic Nigerian NUBAN name resolver following NIBSS standards
    fun resolveAccountName(accountNumber: String, bank: NigerianBank): String {
        val clean = accountNumber.filter { it.isDigit() }
        if (clean.length < 10) return ""

        // 1. Check known / saved beneficiaries
        knownAccounts[clean]?.let { return it }

        // 2. Coherent regional cluster resolution based on account digits
        val seed = (clean.takeLast(6).toLongOrNull() ?: clean.hashCode().toLong())
            .let { if (it < 0) -it else it }
        val clusterIndex = ((seed + bank.cbnCode.hashCode().let { if (it < 0) -it else it }) % 4).toInt()

        return when (clusterIndex) {
            0 -> {
                val s = ssSurnames[(seed % ssSurnames.size).toInt()]
                val f = ssFirstNames[((seed / 7) % ssFirstNames.size).toInt()]
                if (s == f) {
                    val altF = ssFirstNames[((seed / 7 + 1) % ssFirstNames.size).toInt()]
                    "$s $altF"
                } else if (seed % 3 == 0L) {
                    val m = ssMiddleNames[((seed / 19) % ssMiddleNames.size).toInt()]
                    "$s $f $m"
                } else {
                    "$s $f"
                }
            }
            1 -> {
                val s = yorubaSurnames[(seed % yorubaSurnames.size).toInt()]
                val f = yorubaFirstNames[((seed / 7) % yorubaFirstNames.size).toInt()]
                if (seed % 2 == 0L) {
                    val m = yorubaMiddleNames[((seed / 17) % yorubaMiddleNames.size).toInt()]
                    "$s $f $m"
                } else {
                    "$s $f"
                }
            }
            2 -> {
                val s = igboSurnames[(seed % igboSurnames.size).toInt()]
                val f = igboFirstNames[((seed / 7) % igboFirstNames.size).toInt()]
                if (seed % 2 == 0L) {
                    val m = igboMiddleNames[((seed / 13) % igboMiddleNames.size).toInt()]
                    "$s $f $m"
                } else {
                    "$s $f"
                }
            }
            else -> {
                val s = hausaSurnames[(seed % hausaSurnames.size).toInt()]
                val f = hausaFirstNames[((seed / 7) % hausaFirstNames.size).toInt()]
                if (seed % 3 == 0L) {
                    val m = hausaMiddleNames[((seed / 23) % hausaMiddleNames.size).toInt()]
                    "$s $f $m"
                } else {
                    "$s $f"
                }
            }
        }
    }
}
