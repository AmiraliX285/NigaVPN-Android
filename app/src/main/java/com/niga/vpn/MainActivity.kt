package com.niga.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Navy = Color(0xFF0A1929)
private val Blue = Color(0xFF29B6F6)
private val Cyan = Color(0xFF00B0FF)
private val Panel = Color(0xFF142638)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NigaVpnApp() }
    }
}

@Composable
private fun NigaVpnApp() {
    var subscriptionUrl by remember { mutableStateOf("") }
    var servers by remember { mutableStateOf<List<SubscriptionServer>>(emptyList()) }
    var message by remember { mutableStateOf("برای شروع، لینک اشتراک خود را وارد کنید.") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Blue,
        background = Navy,
        surface = Panel
    )) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF07121E), Navy, Color(0xFF102B40))))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(18.dp))
                Text("NIGA", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                Text("V P N", color = Blue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(26.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Panel.copy(alpha = .92f)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("اشتراک من", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = subscriptionUrl,
                            onValueChange = { subscriptionUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("لینک کامل Subscription") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            supportingText = { Text("لینک را فقط از پنل رسمی خود دریافت کنید.", color = Color.LightGray) }
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    loading = true
                                    message = "در حال دریافت اشتراک…"
                                    try {
                                        servers = withContext(Dispatchers.IO) {
                                            SubscriptionParser.fetch(subscriptionUrl.trim())
                                        }
                                        message = if (servers.isEmpty())
                                            "سروری در خروجی قابل‌شناسایی پیدا نشد."
                                        else "${servers.size} سرور شناسایی شد."
                                    } catch (e: Exception) {
                                        message = "دریافت اشتراک ناموفق بود: ${e.message ?: "خطای شبکه"}"
                                    } finally { loading = false }
                                }
                            },
                            enabled = !loading && subscriptionUrl.startsWith("https://"),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (loading) "لطفاً صبر کنید…" else "دریافت اشتراک", color = Navy, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(message, color = Color(0xFFB8D8E8), fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("سرورهای اشتراک", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${servers.size} مورد", color = Blue, fontSize = 13.sp)
                }
                Spacer(Modifier.height(10.dp))
                if (servers.isEmpty()) {
                    Text(
                        "پس از دریافت اشتراک، سرورها اینجا نمایش داده می‌شوند.",
                        color = Color.Gray, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 28.dp)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(servers) { server ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Panel),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(server.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(server.protocol, color = Blue, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("NIGA VPN  •  نسخه آزمایشی رابط کاربری", color = Color(0xFF7893A5), fontSize = 11.sp)
            }
        }
    }
}
