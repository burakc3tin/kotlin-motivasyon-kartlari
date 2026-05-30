package com.brizzbi.sinavmotivasyon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.brizzbi.sinavmotivasyon.components.MotivasyonKart
import com.brizzbi.sinavmotivasyon.components.SplashScreen
import com.brizzbi.sinavmotivasyon.data.MotivasyonSozler
import com.brizzbi.sinavmotivasyon.data.StorageManager
import com.brizzbi.sinavmotivasyon.ui.theme.SinavmotivasyonTheme
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SinavmotivasyonTheme {
                // Splash Screen Durumu
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    // Ekran açıldığında ilk burası çalışacak
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    // Splash bittikten sonra ana uygulamaya geçiş yapıyoruz
                    val context = LocalContext.current
                    val storageManager = remember { StorageManager(context) }

                    var savedQuotes by remember { mutableStateOf(storageManager.getSavedSozler()) }
                    var showOnlyFavorites by remember { mutableStateOf(false) }
                    val allQuotes = remember { MotivasyonSozler.sozler.shuffled() }

                    var modalMesaji by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(modalMesaji) {
                        if (modalMesaji != null) {
                            delay(1500)
                            modalMesaji = null
                        }
                    }

                    val currentList = remember(showOnlyFavorites, savedQuotes) {
                        if (showOnlyFavorites) {
                            if (savedQuotes.isEmpty()) listOf("Henüz favori sözün yok.") else savedQuotes.toList()
                        } else {
                            allQuotes
                        }
                    }

                    val pagerState = rememberPagerState(
                        initialPage = Int.MAX_VALUE / 2,
                        pageCount = { Int.MAX_VALUE }
                    )

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color(0xFF0A0C16), Color(0xFF030303))))
                                .padding(innerPadding)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val rnd = kotlin.random.Random(42)
                                for (i in 1..150) {
                                    drawCircle(
                                        color = Color.White.copy(alpha = rnd.nextFloat() * 0.7f + 0.1f),
                                        radius = rnd.nextFloat() * 2f + 0.5f,
                                        center = Offset(rnd.nextFloat() * w, rnd.nextFloat() * h)
                                    )
                                }
                            }

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize().align(Alignment.Center),
                                contentPadding = PaddingValues(horizontal = 32.dp)
                            ) { page ->
                                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                val absOffset = pageOffset.absoluteValue

                                val safeIndex = if (currentList.isNotEmpty()) (page % currentList.size).absoluteValue else 0
                                val currentSoz = currentList[safeIndex]
                                val isSaved = savedQuotes.contains(currentSoz)

                                MotivasyonKart(
                                    soz = currentSoz,
                                    isSaved = isSaved,
                                    isFilterActive = showOnlyFavorites,
                                    onSaveClick = { yeniDurum ->
                                        if (currentSoz != "Henüz favori sözün yok.") {
                                            savedQuotes = storageManager.toggleSoz(currentSoz)
                                            modalMesaji = if (yeniDurum) "Favori sözlere eklendi 💖" else "Favori sözlerden çıkarıldı 💔"
                                        }
                                    },
                                    onFilterClick = { yeniDurum ->
                                        showOnlyFavorites = yeniDurum
                                        modalMesaji = if (yeniDurum) "Sadece favoriler gösteriliyor 🌟" else "Tüm sözler gösteriliyor 📚"
                                    },
                                    modifier = Modifier.graphicsLayer {
                                        val scale = lerp(1f, 0.85f, absOffset)
                                        scaleX = scale
                                        scaleY = scale
                                        alpha = lerp(1f, 0.5f, absOffset)
                                        rotationZ = pageOffset * 15f
                                    }
                                )
                            }

                            AnimatedVisibility(
                                visible = modalMesaji != null,
                                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                                exit = fadeOut() + scaleOut(targetScale = 0.8f),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFF1E1E2E).copy(alpha = 0.85f))
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = modalMesaji ?: "",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}