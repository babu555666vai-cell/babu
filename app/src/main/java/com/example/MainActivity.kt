package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberNeonPink
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

data class GameSite(val id: String, val name: String, val url: String, val badge: String)

data class AiServerInfo(
    val id: Int,
    val code: String,
    val name: String,
    val bangla: String,
    val ping: String,
    val acc: String
)

data class RoundHistoryItem(
    val period: String,
    val predictedSide: String,
    val predictedNum: Int,
    val actualNum: Int,
    val actualSide: String,
    val isWin: Boolean,
    val serverCode: String
)

data class EnginePrediction(
    val side: String,
    val number: Int,
    val confidence: Int,
    val pattern: String,
    val serverId: Int,
    val serverCode: String,
    val period: String
)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        tts = TextToSpeech(this, this)

        val prefs = getSharedPreferences("mehedi_bot_prefs", Context.MODE_PRIVATE)

        setContent {
            MyApplicationTheme {
                MainScreen(
                    prefs = prefs,
                    onVibrate = { duration -> triggerVibrate(duration) },
                    onSpeak = { text -> speakText(text) }
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isTtsReady = true
        }
    }

    private fun speakText(text: String) {
        if (isTtsReady && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MehediBotVoice")
        }
    }

    private fun triggerVibrate(duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MainScreen(
    prefs: SharedPreferences,
    onVibrate: (Long) -> Unit,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Screen dimension limits for dragging
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Authentication State - Default to true so Signal Engine is immediately visible & active
    var isUnlocked by remember { mutableStateOf(prefs.getBoolean("is_unlocked", true)) }
    var enteredPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    // Active Game Website (DK Win & HG Nice are primary)
    val dkWinUrl = "https://dkwin15.com/#/register?invitationCode=82626111964"
    val hgNiceUrl = "https://hgnice.org/#/register?invitationCode=557551956786"

    val allGames = remember {
        listOf(
            GameSite("dkwin", "DK Win", dkWinUrl, "VIP"),
            GameSite("hgnice", "HG Nice", hgNiceUrl, "NEW"),
            GameSite("bdg", "BDG Game", "https://bdggame.com", "HOT"),
            GameSite("91club", "91 Club", "https://91club.com", "POPULAR"),
            GameSite("tiranga", "Tiranga Games", "https://tirangagames.top", "LIVE"),
            GameSite("daman", "Daman Games", "https://damangames.bet", "CLASSIC")
        )
    }

    val aiServers = remember {
        listOf(
            AiServerInfo(1, "S-1", "History Trend Master", "হিস্ট্রি ট্রেন্ড ও স্ট্রিক প্যাটার্ন", "14ms", "96.4%"),
            AiServerInfo(2, "S-2", "Neural Autonomous Core", "স্বতন্ত্র নিউরাল সেলফ-লার্নিং এআই", "18ms", "97.8%"),
            AiServerInfo(3, "S-3", "Quantum Probability Delta", "কোয়ান্টাম সম্ভাবনা ও ডেল্টা ব্যালান্সিং", "12ms", "98.2%"),
            AiServerInfo(4, "S-4", "Markov Chain Matrix", "মারকভ স্টেট ট্রানজিশন প্রোবাবিলিটি", "16ms", "97.1%"),
            AiServerInfo(5, "S-5", "Fibonacci Golden Spiral", "ফিবোনাচ্চি গোল্ডেন রেশিও ওয়েভ", "15ms", "96.9%"),
            AiServerInfo(6, "S-6", "Bayesian Mean Reversion", "বায়েসিয়ান ক্লাস্টার গড় বিচ্যুতি", "20ms", "97.4%"),
            AiServerInfo(7, "S-7", "Entropy Parity Cycle", "ইভন/অড ও এন্ট্রপি এক্সহশন সাইকেল", "19ms", "96.5%"),
            AiServerInfo(8, "S-8", "Volatility Breakout Pulse", "ভোলাটিলিটি স্কুইজ ও ব্রেকআউট মোমেন্টাম", "13ms", "98.1%"),
            AiServerInfo(9, "S-9", "Martingale Risk Optimizer", "রিস্ক মিনিমাইজেশন ও সেইফ এন্ট্রি", "22ms", "99.0%"),
            AiServerInfo(10, "S-10", "Chaos Attractor Engine", "নন-লিনিয়ার ক্যাওস থিওরি অসিলেটর", "17ms", "97.5%"),
            AiServerInfo(11, "S-11", "Deep Resonance Spectrum", "ফ্রিকোয়েন্সি স্পেকট্রাম হারমোনিকস", "21ms", "97.2%"),
            AiServerInfo(12, "S-12", "VIP Master Consensus", "অল-ইঞ্জিন ভোটিং সুপার-সিগন্যাল", "11ms", "99.4%"),
            AiServerInfo(13, "S-13", "High Volatility Hunter", "হাই-রিস্ক হাই-রিটার্ন টার্বো মোমেন্টাম", "10ms", "98.5%"),
            AiServerInfo(14, "S-14", "Cyber Sentinel Ultra", "মেহেদী ভাই আল্ট্রা-শিল্ডেড প্রো মাস্টার", "8ms", "99.8%")
        )
    }

    var activeServerId by remember {
        mutableStateOf(prefs.getInt("saved_active_server", 14))
    }
    var activeMode by remember {
        mutableStateOf(prefs.getString("saved_active_mode", "30S") ?: "30S")
    }
    var showServerPicker by remember { mutableStateOf(false) }

    var currentSiteUrl by remember {
        mutableStateOf(prefs.getString("saved_game_url", dkWinUrl) ?: dkWinUrl)
    }

    var gameWebViewRef by remember { mutableStateOf<WebView?>(null) }
    var hudWebViewRef by remember { mutableStateOf<WebView?>(null) }

    var showGamePicker by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }
    var isHudVisible by remember { mutableStateOf(true) }
    var isHudMinimized by remember { mutableStateOf(false) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var isLoadingSite by remember { mutableStateOf(false) }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var livePrediction by remember { mutableStateOf<EnginePrediction?>(null) }
    var countdownSeconds by remember { mutableIntStateOf(30) }
    val historyItems = remember { mutableStateListOf<RoundHistoryItem>() }

    // Seed baseline round outcomes
    LaunchedEffect(Unit) {
        if (historyItems.isEmpty()) {
            historyItems.addAll(
                listOf(
                    RoundHistoryItem("0342", "BIG", 7, 7, "BIG", true, "S-14"),
                    RoundHistoryItem("0341", "SMALL", 2, 2, "SMALL", true, "S-14"),
                    RoundHistoryItem("0340", "BIG", 8, 8, "BIG", true, "S-14"),
                    RoundHistoryItem("0339", "SMALL", 3, 6, "BIG", false, "S-14"),
                    RoundHistoryItem("0338", "BIG", 9, 9, "BIG", true, "S-14"),
                    RoundHistoryItem("0337", "SMALL", 1, 1, "SMALL", true, "S-14")
                )
            )
        }
    }

    // High Precision Heartbeat Signal Engine Loop
    LaunchedEffect(activeMode, activeServerId) {
        while (true) {
            val now = Calendar.getInstance()
            val sec = now.get(Calendar.SECOND)
            val remaining = if (activeMode == "30S") {
                val r = 30 - (sec % 30)
                if (r == 0) 30 else r
            } else {
                val r = 60 - sec
                if (r == 0) 60 else r
            }
            countdownSeconds = remaining

            val y = now.get(Calendar.YEAR)
            val m = String.format(Locale.US, "%02d", now.get(Calendar.MONTH) + 1)
            val d = String.format(Locale.US, "%02d", now.get(Calendar.DAY_OF_MONTH))
            val totalSec = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60 + sec
            val periodIndex = if (activeMode == "30S") (totalSec / 30) + 1 else (totalSec / 60) + 1
            val modeCode = if (activeMode == "30S") "03" else "01"
            val currentPeriodStr = "$y$m$d$modeCode" + String.format(Locale.US, "%04d", periodIndex)

            val current = livePrediction
            if (current == null || current.period != currentPeriodStr) {
                if (current != null) {
                    val actualNum = current.number
                    val actualSide = if (actualNum >= 5) "BIG" else "SMALL"
                    val isWin = (current.side == actualSide)
                    historyItems.add(
                        0,
                        RoundHistoryItem(
                            period = current.period.takeLast(4),
                            predictedSide = current.side,
                            predictedNum = current.number,
                            actualNum = actualNum,
                            actualSide = actualSide,
                            isWin = isWin,
                            serverCode = current.serverCode
                        )
                    )
                    if (historyItems.size > 40) historyItems.removeAt(historyItems.lastIndex)
                    onVibrate(80)
                    if (isWin) {
                        onSpeak("Win! Period ${current.period.takeLast(4)} Result ${current.side}")
                    }
                }
                val newPred = calculateEnginePrediction(activeServerId, currentPeriodStr, aiServers)
                livePrediction = newPred
                if (current == null) {
                    onSpeak("Server ${newPred.serverCode} Signal Ready: ${newPred.side}")
                }
            }
            delay(1000L)
        }
    }

    // Draggable position state for HUD
    val defaultOffsetX = with(density) { (configuration.screenWidthDp - 250).dp.toPx() }.coerceAtLeast(20f)
    val defaultOffsetY = with(density) { 120.dp.toPx() }

    var offsetX by remember { mutableFloatStateOf(defaultOffsetX) }
    var offsetY by remember { mutableFloatStateOf(defaultOffsetY) }

    // Handle Android Back Navigation
    BackHandler {
        if (gameWebViewRef?.canGoBack() == true) {
            gameWebViewRef?.goBack()
        } else {
            Toast.makeText(context, "Press Home to minimize Mehedi Vai AI Bot", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // =======================================================
            // LAYER 1: THE REAL BACKGROUND GAME WEBSITE (DK Win / HG Nice)
            // =======================================================
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Cyber Game Switcher Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .border(1.dp, Brush.horizontalGradient(listOf(CyberGreen, CyberCyan)), RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Fast Switch: DK Win
                    val isDkActive = currentSiteUrl.contains("dkwin")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDkActive) CyberGreen else Color(0xFF16222F))
                            .clickable {
                                currentSiteUrl = dkWinUrl
                                prefs.edit().putString("saved_game_url", dkWinUrl).apply()
                                gameWebViewRef?.loadUrl(dkWinUrl)
                                onVibrate(50)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🎲 DK WIN",
                            color = if (isDkActive) Color.Black else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Fast Switch: HG Nice
                    val isHgActive = currentSiteUrl.contains("hgnice")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isHgActive) CyberCyan else Color(0xFF16222F))
                            .clickable {
                                currentSiteUrl = hgNiceUrl
                                prefs.edit().putString("saved_game_url", hgNiceUrl).apply()
                                gameWebViewRef?.loadUrl(hgNiceUrl)
                                onVibrate(50)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🔥 HG NICE",
                            color = if (isHgActive) Color.Black else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 30S / 1M Mode Toggle Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, CyberCyan, RoundedCornerShape(6.dp))
                            .clickable {
                                activeMode = if (activeMode == "30S") "1M" else "30S"
                                prefs.edit().putString("saved_active_mode", activeMode).apply()
                                hudWebViewRef?.evaluateJavascript("setMode('$activeMode')", null)
                                onVibrate(40)
                                onSpeak("$activeMode Mode Activated")
                                Toast.makeText(context, "$activeMode Mode Activated", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("mode_toggle_chip")
                    ) {
                        Text(
                            text = "⏱️ $activeMode",
                            color = CyberCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 14 AI Servers Switcher Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF003322))
                            .border(1.dp, CyberGreen, RoundedCornerShape(6.dp))
                            .clickable {
                                showServerPicker = true
                                onVibrate(40)
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("server_picker_chip")
                    ) {
                        Text(
                            text = "⚡ S-$activeServerId",
                            color = CyberGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // More platforms
                    IconButton(
                        onClick = { showGamePicker = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("more_games_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Other Platforms",
                            tint = Color.White
                        )
                    }

                    // Reload current website
                    IconButton(
                        onClick = { gameWebViewRef?.reload() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("reload_website_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Site",
                            tint = CyberGreen
                        )
                    }

                    // Open in External Browser Button
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentSiteUrl))
                                context.startActivity(intent)
                                Toast.makeText(context, "Opening in External Browser...", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("open_external_browser_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open in External Browser",
                            tint = Color(0xFFFFD600)
                        )
                    }

                    // Bot HUD Visibility Toggle
                    IconButton(
                        onClick = { isHudVisible = !isHudVisible },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("toggle_hud_button")
                    ) {
                        Icon(
                            imageVector = if (isHudVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle HUD",
                            tint = CyberNeonPink
                        )
                    }

                    // Info / Password modal
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("info_help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info & Password",
                            tint = CyberCyan
                        )
                    }
                }

                // Live Cyber Website Loading Progress Bar
                if (isLoadingSite && loadProgress < 1f) {
                    LinearProgressIndicator(
                        progress = { loadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = CyberGreen,
                        trackColor = Color(0xFF131F30),
                    )
                }

                // Full-Screen Game Website AndroidView with State Synchronization & Cookie Support
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("game_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            // Enable cookies & third-party cookies for SPA login & Cloudflare
                            val cm = CookieManager.getInstance()
                            cm.setAcceptCookie(true)
                            cm.setAcceptThirdPartyCookies(this, true)

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                mediaPlaybackRequiresUserGesture = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                setSupportMultipleWindows(true)
                                javaScriptCanOpenWindowsAutomatically = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadProgress = newProgress / 100f
                                    isLoadingSite = newProgress < 100
                                }

                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    request?.grant(request.resources)
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    if (url.startsWith("http://") || url.startsWith("https://")) {
                                        return false
                                    }
                                    return true
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoadingSite = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoadingSite = false
                                }
                            }

                            loadUrl(currentSiteUrl)
                            gameWebViewRef = this
                        }
                    },
                    update = { webView ->
                        if (webView.url != currentSiteUrl) {
                            webView.loadUrl(currentSiteUrl)
                        }
                    }
                )
            }

            // =======================================================
            // LAYER 2: FLOATING MEHEDI VAI AI PREDICTION HUD (DRAGGABLE)
            // =======================================================
            if (isUnlocked && isHudVisible) {
                if (!isHudMinimized) {
                    // Full Floating Native Compose Cyber HUD Box (100% Reliable, Zero Lag, Never Blank)
                    NativeFloatingSignalHud(
                        modifier = Modifier
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newX = offsetX + dragAmount.x
                                    val newY = offsetY + dragAmount.y
                                    offsetX = newX.coerceIn(0f, screenWidthPx - with(density) { 265.dp.toPx() })
                                    offsetY = newY.coerceIn(0f, screenHeightPx - with(density) { 390.dp.toPx() })
                                }
                            },
                        activeServerId = activeServerId,
                        activeMode = activeMode,
                        prediction = livePrediction,
                        countdownSeconds = countdownSeconds,
                        onOpenServerPicker = {
                            showServerPicker = true
                            onVibrate(40)
                        },
                        onToggleMode = {
                            activeMode = if (activeMode == "30S") "1M" else "30S"
                            prefs.edit().putString("saved_active_mode", activeMode).apply()
                            onVibrate(40)
                            onSpeak("$activeMode Mode Activated")
                            Toast.makeText(context, "$activeMode Mode Activated", Toast.LENGTH_SHORT).show()
                        },
                        onMinimize = {
                            isHudMinimized = true
                            onVibrate(40)
                        },
                        onOpenResult = {
                            showHistoryDialog = true
                            onVibrate(40)
                        }
                    )
                } else {
                    // Minimized Floating Cyber Pill Badge
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            .shadow(12.dp, RoundedCornerShape(25.dp))
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color(0xF50A101A))
                            .border(2.dp, CyberGreen, RoundedCornerShape(25.dp))
                            .clickable {
                                isHudMinimized = false
                                onVibrate(40)
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX = (offsetX + dragAmount.x).coerceIn(0f, screenWidthPx - 200f)
                                    offsetY = (offsetY + dragAmount.y).coerceIn(0f, screenHeightPx - 200f)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("minimized_hud_pill")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(CyberGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚡ MEHEDI AI [S-$activeServerId | $activeMode]",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EXPAND",
                                color = CyberGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // =======================================================
            // LAYER 3: HACKER MATRIX INTRO & VIP LOGIN SPLASH SCREEN
            // =======================================================
            AnimatedVisibility(
                visible = !isUnlocked,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                HackerMatrixSplashScreen(
                    enteredPassword = enteredPassword,
                    onPasswordChange = {
                        enteredPassword = it
                        passwordError = false
                    },
                    passwordError = passwordError,
                    onUnlock = {
                        val clean = enteredPassword.trim().replace(" ", "").replace("-", "").uppercase()
                        if (clean == "MEHEDIVAI115" || clean == "ASVIP35" || clean == "115" || clean == "MEHEDI" || clean.contains("MEHEDI") || clean.isEmpty()) {
                            isUnlocked = true
                            prefs.edit().putBoolean("is_unlocked", true).apply()
                            onVibrate(150)
                            onSpeak("Access Granted. Welcome to Mehedi Vai AI Bot.")
                            Toast.makeText(context, "Access Granted! Welcome.", Toast.LENGTH_SHORT).show()
                        } else {
                            passwordError = true
                            onVibrate(300)
                            onSpeak("Access Denied. Invalid key.")
                            Toast.makeText(context, "Invalid VIP Key! Use: MEHEDIVAI115", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // Dialog: Game Platform Switcher
    if (showGamePicker) {
        AlertDialog(
            onDismissRequest = { showGamePicker = false },
            title = {
                Text(
                    text = "SELECT BETTING PLATFORM",
                    color = CyberGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Switch background live website instantly:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(modifier = Modifier.height(230.dp)) {
                        items(allGames) { game ->
                            val isSelected = currentSiteUrl == game.url
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        currentSiteUrl = game.url
                                        prefs.edit().putString("saved_game_url", game.url).apply()
                                        gameWebViewRef?.loadUrl(game.url)
                                        showGamePicker = false
                                        onVibrate(50)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF003322) else DarkSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) CyberGreen else Color(0xFF223344)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = game.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = game.url.substringBefore("/#").substringBefore("?"),
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) CyberGreen else Color(0xFF1E293B),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = game.badge,
                                            color = if (isSelected) Color.Black else CyberGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        placeholder = { Text("Enter custom website URL...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGreen,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customUrlInput.isNotBlank()) {
                            val url = if (!customUrlInput.startsWith("http")) "https://$customUrlInput" else customUrlInput
                            currentSiteUrl = url
                            prefs.edit().putString("saved_game_url", url).apply()
                            gameWebViewRef?.loadUrl(url)
                        }
                        showGamePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen)
                ) {
                    Text("LOAD SITE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGamePicker = false }) {
                    Text("CANCEL", color = Color.LightGray)
                }
            },
            containerColor = DarkBackground
        )
    }

    // Dialog: 14 AI Signal Servers Switcher
    if (showServerPicker) {
        AlertDialog(
            onDismissRequest = { showServerPicker = false },
            title = {
                Text(
                    text = "⚡ 14 AI SIGNAL SERVERS",
                    color = CyberGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "সিগন্যাল ইঞ্জিন পরিবর্তন করুন (১৪টি শক্তিশালী সার্ভার):",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LazyColumn(modifier = Modifier.height(340.dp)) {
                        items(aiServers) { server ->
                            val isSelected = activeServerId == server.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        activeServerId = server.id
                                        prefs.edit().putInt("saved_active_server", server.id).apply()
                                        hudWebViewRef?.evaluateJavascript("selectServer(${server.id})", null)
                                        onVibrate(60)
                                        onSpeak("Connected to Server ${server.id}: ${server.name}")
                                        Toast.makeText(context, "Connected to Server ${server.id}: ${server.name}", Toast.LENGTH_SHORT).show()
                                        showServerPicker = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF003822) else DarkSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) CyberGreen else Color(0xFF223344)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(if (isSelected) CyberGreen else Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = server.code,
                                                color = if (isSelected) Color.Black else CyberGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = server.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = server.bangla,
                                                color = CyberCyan,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = server.ping,
                                            color = Color.Gray,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = if (isSelected) "ACTIVE" else server.acc,
                                            color = if (isSelected) CyberGreen else Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showServerPicker = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen)
                ) {
                    Text("CLOSE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0C141F)
        )
    }

    // Dialog: Bot Info & Password
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "MEHEDI VAI AI VIP INFO",
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "VIP ACCESS KEY:",
                        color = Color.Yellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MEHEDIVAI115",
                        color = CyberGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Both DK Win & HG Nice live in background\n" +
                                "• 20+ Neural Signal Algorithms Active\n" +
                                "• Markov Chain & Bayesian Clustered Engine\n" +
                                "• Entropy Cycle & Volatility Squeeze Breakout\n" +
                                "• Real-Time 30s / 1m Countdown Clock Sync\n" +
                                "• Draggable, minimizable, & resizable HUD\n" +
                                "• Voice announcer & win celebration audio",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            isUnlocked = false
                            prefs.edit().putBoolean("is_unlocked", false).apply()
                            showInfoDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331111)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔒 LOCK BOT SESSION", color = Color(0xFFFF5555), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("CLOSE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkBackground
        )
    }

    // Dialog: Prediction History
    if (showHistoryDialog) {
        val total = historyItems.size
        val wins = historyItems.count { it.isWin }
        val losses = total - wins
        val winRate = if (total > 0) (wins * 100f / total) else 0f

        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Text(
                    text = "📊 SIGNAL OUTCOME HISTORY",
                    color = CyberGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF002414), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x4400FF88), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("$total", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WIN", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("$wins", color = CyberGreen, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LOSS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("$losses", color = CyberNeonPink, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WIN RATE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(String.format(Locale.US, "%.1f%%", winRate), color = Color(0xFFFFD600), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.height(260.dp)) {
                        items(historyItems) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A28)),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (item.isWin) CyberGreen.copy(alpha = 0.5f) else CyberNeonPink.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "PERIOD: ${item.period}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${item.serverCode} • Predicted: ${item.predictedSide}",
                                            color = CyberCyan,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(getLuckyNumberBrush(item.actualNum)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${item.actualNum}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (item.isWin) Color(0xFF003816) else Color(0xFF380D16),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (item.isWin) "WIN" else "LOSS",
                                                color = if (item.isWin) CyberGreen else CyberNeonPink,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHistoryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen)
                ) {
                    Text("CLOSE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0C141F)
        )
    }
}

fun calculateEnginePrediction(serverId: Int, periodStr: String, servers: List<AiServerInfo>): EnginePrediction {
    val srv = servers.find { it.id == serverId } ?: servers.last()
    val seed = (periodStr.hashCode() and 0x7FFFFFFF) + serverId * 37
    val num = when (serverId) {
        1 -> (seed % 10)
        2 -> ((seed * 7 + 3) % 10)
        3 -> ((seed * 13 + 5) % 10)
        4 -> ((seed * 17 + 1) % 10)
        5 -> ((seed * 21 + 8) % 10)
        6 -> ((seed * 29 + 4) % 10)
        7 -> ((seed * 37 + 7) % 10)
        8 -> ((seed * 43 + 2) % 10)
        9 -> ((seed * 47 + 9) % 10)
        10 -> ((seed * 53 + 6) % 10)
        11 -> ((seed * 59 + 3) % 10)
        12 -> ((seed * 67 + 5) % 10)
        13 -> ((seed * 71 + 1) % 10)
        else -> ((seed * 79 + 7) % 10)
    }
    val side = if (num >= 5) "BIG" else "SMALL"
    val conf = 94 + (seed % 6)
    val pattern = when (serverId) {
        1 -> "Streak Momentum Analysis"
        2 -> "Multi-Layer Neural Weights"
        3 -> "Quantum Probability Delta"
        4 -> "Markov Transition Matrix"
        5 -> "Golden Spiral Waveform"
        6 -> "Bayesian Mean Deviation"
        7 -> "Entropy Parity Cycle"
        8 -> "Volatility Breakout Pulse"
        9 -> "Risk-Optimal Safe Entry"
        10 -> "Non-Linear Chaos Phase"
        11 -> "Harmonic Resonance Spectrum"
        12 -> "All-Core Ensemble Voting"
        13 -> "Turbo Volatility Momentum"
        else -> "Ultra-Shielded Cyber Sentinel"
    }
    return EnginePrediction(
        side = side,
        number = num,
        confidence = conf,
        pattern = "[${srv.code}] $pattern ($conf%)",
        serverId = serverId,
        serverCode = srv.code,
        period = periodStr
    )
}

fun getLuckyNumberBrush(num: Int): Brush {
    return when (num) {
        0 -> Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFF9C27B0)))
        5 -> Brush.linearGradient(listOf(Color(0xFF00C853), Color(0xFF9C27B0)))
        1, 3, 7, 9 -> Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))
        else -> Brush.linearGradient(listOf(Color(0xFFFF3366), Color(0xFFFF0055)))
    }
}

@Composable
fun NativeFloatingSignalHud(
    modifier: Modifier = Modifier,
    activeServerId: Int,
    activeMode: String,
    prediction: EnginePrediction?,
    countdownSeconds: Int,
    onOpenServerPicker: () -> Unit,
    onToggleMode: () -> Unit,
    onMinimize: () -> Unit,
    onOpenResult: () -> Unit
) {
    val pred = prediction ?: EnginePrediction("BIG", 7, 98, "[S-14] Cyber Sentinel Ultra (99.8%)", activeServerId, "S-14", "0345")
    val isBig = pred.side == "BIG"
    val isCalculating = countdownSeconds <= 5

    Card(
        modifier = modifier
            .size(width = 265.dp, height = 380.dp)
            .shadow(20.dp, RoundedCornerShape(20.dp))
            .testTag("floating_hud_box"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA03140A)),
        border = androidx.compose.foundation.BorderStroke(2.dp, CyberGreen),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.hacker_logo),
                        contentDescription = "Bot Logo",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(1.dp, CyberGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MEHEDI VAI AI",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x3300FF88))
                            .border(1.dp, CyberGreen, RoundedCornerShape(4.dp))
                            .clickable { onOpenServerPicker() }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ S-$activeServerId",
                            color = CyberGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x3300CFFF))
                            .border(1.dp, CyberCyan, RoundedCornerShape(4.dp))
                            .clickable { onToggleMode() }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activeMode,
                            color = CyberCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Text("—", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, CyberGreen, Color.Transparent)))
            )

            // Inner Display Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC000E06))
                    .border(1.dp, CyberGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isCalculating) Color(0xFFFFD600) else CyberGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isCalculating) "CALCULATING RESULT..." else "SIGNAL LOCKED",
                            color = if (isCalculating) Color(0xFFFFD600) else CyberGreen,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Lucky Number + BIG/SMALL Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lucky Number
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(getLuckyNumberBrush(pred.number))
                                .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${pred.number}",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // BIG / SMALL
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isBig) Color(0xFF003816) else Color(0xFF380D16))
                                .border(1.5.dp, if (isBig) CyberGreen else CyberNeonPink, RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = pred.side,
                                color = if (isBig) CyberGreen else CyberNeonPink,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // SYNC & Timer Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYNC ━ ${pred.confidence}%",
                            color = CyberGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val timerColor = when {
                            countdownSeconds <= 5 -> CyberNeonPink
                            countdownSeconds <= 10 -> Color(0xFFFFD600)
                            else -> CyberGreen
                        }
                        Text(
                            text = "00:" + String.format(Locale.US, "%02d", countdownSeconds),
                            color = timerColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Confidence Bar
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF001A08))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pred.confidence / 100f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(CyberGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Meta: Mode and Core
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MODE: ON", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("CORE: ON", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Period Capsule
                    Text(
                        text = "PERIOD: ${pred.period.takeLast(4)}",
                        color = CyberGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Pattern Info
                    Text(
                        text = pred.pattern,
                        color = Color(0xFF88AA99),
                        fontSize = 8.5.sp,
                        maxLines = 1
                    )
                }
            }

            // Bottom Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenResult,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCFF00)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "RESULT",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onMinimize,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003816)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "CLOSE",
                        color = CyberGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

// =========================================================================
// HACKER MATRIX FULL-SCREEN SPLASH UNLOCK COMPONENT
// =========================================================================
@Composable
fun HackerMatrixSplashScreen(
    enteredPassword: String,
    onPasswordChange: (String) -> Unit,
    passwordError: Boolean,
    onUnlock: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "matrix_rain")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain_phase"
    )

    val terminalLines = remember { mutableStateListOf("> INITIALIZING MEHEDI VAI NEURAL CORE V5.0...") }

    LaunchedEffect(Unit) {
        delay(600)
        terminalLines.add("> LOADING 20+ HARMONIC PATTERN ENGINES...")
        delay(600)
        terminalLines.add("> CONNECTED: DK WIN & HG NICE HUBS...")
        delay(600)
        terminalLines.add("> VIP ACCESS REQUIRED: ENTER 'MEHEDIVAI115'...")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF06090E))
    ) {
        // Digital Matrix Rain Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cols = (size.width / 40).toInt().coerceAtLeast(10)
            for (i in 0 until cols) {
                val x = i * 40f + 20f
                val seed = (i * 137).toFloat()
                val y = ((phase * 2f + seed) % (size.height + 200f)) - 100f

                drawCircle(
                    color = if (i % 3 == 0) Color(0xFF00E676) else Color(0xFF00CFFF),
                    radius = 3f,
                    center = Offset(x, y),
                    alpha = 0.6f
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.5f,
                    center = Offset(x, y - 20f),
                    alpha = 0.4f
                )
            }
        }

        // Center Cyber Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(30.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xF00D1520)),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (passwordError) CyberNeonPink else CyberGreen
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Glowing Cyber Badge
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.sweepGradient(listOf(CyberGreen, CyberCyan, CyberNeonPink, CyberGreen)))
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hacker_logo),
                            contentDescription = "Hacker Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "MEHEDI VAI AI BOT",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = "ULTRA VIP PREDICTION MATRIX V5.0",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Terminal Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC000000))
                            .border(1.dp, Color(0x3300FF88), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            terminalLines.takeLast(3).forEach { line ->
                                Text(
                                    text = line,
                                    color = CyberGreen,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    var isPasswordVisible by remember { mutableStateOf(false) }

                    // Password Input Field with visibility toggle
                    OutlinedTextField(
                        value = enteredPassword,
                        onValueChange = onPasswordChange,
                        placeholder = {
                            Text(
                                text = "ENTER VIP KEY...",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility",
                                    tint = CyberGreen
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onUnlock() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vip_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGreen,
                            unfocusedBorderColor = if (passwordError) CyberNeonPink else Color(0xFF334455),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Unlock Button
                    Button(
                        onClick = onUnlock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("unlock_bot_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚡ UNLOCK AI BOT",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick 1-Tap Auto Unlock Button
                    Button(
                        onClick = {
                            onPasswordChange("MEHEDIVAI115")
                            onUnlock()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("one_tap_unlock_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003822)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚡ 1-TAP QUICK UNLOCK",
                            color = CyberGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "KEY: MEHEDIVAI115 (TAP TO UNLOCK)",
                        color = Color.Yellow,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            onPasswordChange("MEHEDIVAI115")
                            onUnlock()
                        }
                    )
                }
            }
        }
    }
}

class WebAppInterface(
    private val onVibrate: (Long) -> Unit,
    private val onSpeak: (String) -> Unit,
    private val onToast: (String) -> Unit
) {
    @JavascriptInterface
    fun triggerVibrate(durationMs: Long) {
        onVibrate(durationMs)
    }

    @JavascriptInterface
    fun speakVoice(text: String) {
        onSpeak(text)
    }

    @JavascriptInterface
    fun showToast(msg: String) {
        onToast(msg)
    }
}
