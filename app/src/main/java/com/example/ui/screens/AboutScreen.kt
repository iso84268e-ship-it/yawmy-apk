package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.White
import com.example.ui.theme.White20
import com.example.util.toArabicDigits
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.35f))
                        .testTag("about_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = InkPrimary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "عن التطبيق",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = InkPrimary
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Developer Profile Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("developer_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circle Avatar "إ ر"
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(InkPrimary)
                    ) {
                        Text(
                            text = "إ ر",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "إسلام رمضان",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = InkPrimary,
                            fontSize = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "مبرمج تطبيقات",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = InkSecondary,
                            fontSize = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = White.copy(alpha = 0.4f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "صنعت «يومي» ليرتّب لك مواعيدك ومذاكرتك وملاحظاتك في مكان واحد، بشكل هادئ وخفيف.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = InkPrimary,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Action 1: Contact Developer via WhatsApp
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openWhatsApp(context) }
                    .testTag("contact_developer_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = InkPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "تواصل مع المبرمج",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    )
                }
            }
        }

        // Action 2: Rate App
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { rateApp(context) }
                    .testTag("rate_app_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = InkPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "قيّم التطبيق",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    )
                }
            }
        }

        // Action 3: App Version
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "١.٠",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    )
                    Text(
                        text = "الإصدار",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    )
                }
            }
        }

        // Bottom Footer
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "صُنع بحب · إسلام رمضان",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun openWhatsApp(context: Context) {
    try {
        val message = "مرحباً إسلام، أتواصل معك بخصوص تطبيق يومي"
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val url = "https://wa.me/201144105063?text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "تعذر فتح واتساب أو المتصفح على هذا الجهاز", Toast.LENGTH_LONG).show()
    } catch (_: Exception) {
        Toast.makeText(context, "حدث خطأ أثناء فتح الرابط", Toast.LENGTH_SHORT).show()
    }
}

private fun rateApp(context: Context) {
    try {
        val uri = Uri.parse("market://details?id=${context.packageName}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    } catch (_: Exception) {
        Toast.makeText(context, "شكراً لدعمك وتقييمك لتطبيق يومي!", Toast.LENGTH_LONG).show()
    }
}
