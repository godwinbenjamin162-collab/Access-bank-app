package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BankAlertViewModel
import com.example.ui.MainTab
import com.example.ui.components.SmsNotificationBanner
import com.example.ui.components.TransferReceiptDialog
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AlertHubScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.theme.AlertDebitRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NaijaGreenDark
import com.example.ui.theme.NaijaGreenPrimary
import com.example.ui.theme.OPayGreen
import com.example.ui.theme.OPayGreenDark

class MainActivity : ComponentActivity() {

    private val viewModel: BankAlertViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: BankAlertViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeSmsBanner by viewModel.smsBanner.collectAsState()
    val activeReceipt by viewModel.activeReceipt.collectAsState()
    val receiptViewMode by viewModel.receiptViewMode.collectAsState()
    val userBank by viewModel.userBank.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == MainTab.TRANSFER,
                    onClick = { viewModel.setTab(MainTab.TRANSFER) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.TRANSFER) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OPayGreen,
                        selectedTextColor = OPayGreen,
                        indicatorColor = OPayGreen.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_transfer")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.ALERT_GENERATOR,
                    onClick = { viewModel.setTab(MainTab.ALERT_GENERATOR) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.ALERT_GENERATOR) Icons.Filled.Sms else Icons.Outlined.Sms,
                            contentDescription = "SMS Alerts"
                        )
                    },
                    label = { Text("SMS Alerts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OPayGreen,
                        selectedTextColor = OPayGreen,
                        indicatorColor = OPayGreen.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_sms_hub")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.HISTORY,
                    onClick = { viewModel.setTab(MainTab.HISTORY) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.HISTORY) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OPayGreen,
                        selectedTextColor = OPayGreen,
                        indicatorColor = OPayGreen.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.ACCOUNT,
                    onClick = { viewModel.setTab(MainTab.ACCOUNT) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.ACCOUNT) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Me"
                        )
                    },
                    label = { Text("Me") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OPayGreen,
                        selectedTextColor = OPayGreen,
                        indicatorColor = OPayGreen.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_account")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Active Screen Content
            when (currentTab) {
                MainTab.TRANSFER -> TransferScreen(viewModel = viewModel)
                MainTab.ALERT_GENERATOR -> AlertHubScreen(viewModel = viewModel)
                MainTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                MainTab.ACCOUNT -> AccountScreen(viewModel = viewModel)
            }

            // Live Simulated Android Heads-Up SMS Notification Banner
            SmsNotificationBanner(
                bannerData = activeSmsBanner,
                onDismiss = { viewModel.dismissSmsBanner() },
                onCopy = { viewModel.copySmsToClipboard(it) },
                onOpen = {
                    viewModel.dismissSmsBanner()
                    viewModel.setTab(MainTab.HISTORY)
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Active Transfer e-Receipt Dialog
    if (activeReceipt != null) {
        TransferReceiptDialog(
            transaction = activeReceipt!!,
            viewMode = receiptViewMode,
            onDismiss = { viewModel.dismissReceipt() },
            onCopySms = { viewModel.copySmsToClipboard(it) }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

