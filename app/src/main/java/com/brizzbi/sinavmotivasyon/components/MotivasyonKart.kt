package com.brizzbi.sinavmotivasyon.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MotivasyonKart(
    soz: String,
    isSaved: Boolean,
    isFilterActive: Boolean,
    onSaveClick: (Boolean) -> Unit,
    onFilterClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Ekran yapılandırmasını (Configuration) alıyoruz
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp

    // 2. Ekran moduna göre kart oranlarını belirliyoruz
    // Yatay modda kartın çok yayılmasını engellemek için genişliği kısıyoruz (%50)
    val cardWidthFraction = if (isLandscape) 0.5f else 0.85f
    val cardHeightFraction = if (isLandscape) 0.85f else 0.75f

    // 3. Ekran genişliğine göre Dinamik Font Boyutu belirliyoruz
    val dynamicFontSize = when {
        screenWidth < 360 -> 20.sp // 4 inç gibi küçük telefonlar
        screenWidth > 600 -> 34.sp // 9 inç ve üzeri Tabletler
        else -> 26.sp              // Standart telefonlar
    }

    // Küçük ekranlarda iç boşlukları (padding) optimize ediyoruz
    val paddingValue = if (screenWidth < 360) 16.dp else 24.dp

    Card(
        modifier = modifier
            .fillMaxWidth(cardWidthFraction)
            .fillMaxHeight(cardHeightFraction)
            .graphicsLayer {
                shadowElevation = 30f
                shape = RoundedCornerShape(32.dp)
                clip = true
            }
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00F0FF), Color(0xFFFF0055))
                ),
                shape = RoundedCornerShape(32.dp)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF081021), Color(0xFF210510))
                    )
                )
                .padding(paddingValue) // Dinamik padding
        ) {
            // Sağ üstte yan yana animasyonlu ikonlar
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filtre (Yıldız) Butonu
                PatlayanIkonButon(
                    isActive = isFilterActive,
                    activeColor = Color(0xFF00F0FF), // Cyan neon
                    activeIcon = Icons.Filled.Star,
                    inactiveIcon = Icons.Outlined.Star,
                    description = "Filtre",
                    onClick = { onFilterClick(!isFilterActive) }
                )

                // Kaydet (Kalp) Butonu
                PatlayanIkonButon(
                    isActive = isSaved,
                    activeColor = Color(0xFFFF0055), // Pembe neon
                    activeIcon = Icons.Filled.Favorite,
                    inactiveIcon = Icons.Outlined.FavoriteBorder,
                    description = "Kaydet",
                    onClick = { onSaveClick(!isSaved) }
                )
            }

            // Söz Metni
            Text(
                text = soz,
                color = Color.White,
                fontSize = dynamicFontSize, // Dinamik Font burada kullanılıyor
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = (dynamicFontSize.value * 1.3f).sp, // Satır yüksekliği fonta oranla esner
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xFF00F0FF).copy(alpha = 0.8f),
                        offset = Offset(0f, 4f),
                        blurRadius = 16f
                    )
                )
            )
        }
    }
}

// Parçacık Patlaması Efektli Özel Buton
@Composable
fun PatlayanIkonButon(
    isActive: Boolean,
    activeColor: Color,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val patlamaAnimasyonu = remember { Animatable(0f) }
    // İkona basıldığında büyüme-küçülme efekti
    val ikonScale by animateFloatAsState(
        targetValue = if (isActive) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f)
    )

    IconButton(
        onClick = {
            onClick()
            // Sadece aktif (dolu) hale geçerken patlama efekti çalışsın
            if (!isActive) {
                coroutineScope.launch {
                    patlamaAnimasyonu.snapTo(0f)
                    patlamaAnimasyonu.animateTo(1f, animationSpec = tween(500))
                }
            }
        },
        modifier = Modifier
            .size(48.dp)
            .scale(ikonScale)
            .drawBehind {
                // Patlama parçacıkları çizimi
                if (patlamaAnimasyonu.value > 0f && patlamaAnimasyonu.value < 1f) {
                    val radius = patlamaAnimasyonu.value * 80f // Parçacıkların saçılma mesafesi
                    val alpha = 1f - patlamaAnimasyonu.value // Gittikçe soluklaşma

                    for (i in 0 until 8) { // 8 adet parçacık
                        val angle = (i * 45) * (Math.PI / 180)
                        val x = (cos(angle) * radius).toFloat()
                        val y = (sin(angle) * radius).toFloat()
                        drawCircle(
                            color = activeColor.copy(alpha = alpha),
                            radius = 6f, // Parçacık boyutu
                            center = center.copy(x = center.x + x, y = center.y + y)
                        )
                    }
                }
            }
            // Aktifse o renkte ince neon bir yuvarlak border ekle
            .then(
                if (isActive) Modifier.border(1.5.dp, activeColor, CircleShape) else Modifier
            )
    ) {
        Icon(
            imageVector = if (isActive) activeIcon else inactiveIcon,
            contentDescription = description,
            tint = if (isActive) activeColor else Color.White
        )
    }
}