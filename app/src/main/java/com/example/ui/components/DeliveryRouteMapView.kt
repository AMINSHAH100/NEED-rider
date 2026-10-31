package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeliveryOrder
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DeliveryRouteMapView(
    order: DeliveryOrder,
    riderLat: Double = 43.5933,
    riderLng: Double = 10.4735,
    isNavigationToCustomer: Boolean = false,
    showExternalNavButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Pulsing Radar animation for live GPS beacon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val routeDashOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashOffset"
    )

    // Function to launch real Turn-by-Turn GPS Navigation (Google Maps)
    val startNavigation = {
        val targetLat = if (isNavigationToCustomer) order.customerLat else order.restaurantLat
        val targetLng = if (isNavigationToCustomer) order.customerLng else order.restaurantLng
        val label = if (isNavigationToCustomer) order.customerName else order.restaurantName

        openGoogleMapsNavigation(context, targetLat, targetLng, label)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
    ) {
        // 1. Interactive Canvas Map Layer: Real coordinates interpolated onto visual road grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Background road map grid in Italy
            drawRect(
                color = Color(0xFF1E293B),
                size = Size(canvasW, canvasH)
            )

            // Parks and Green areas in Tuscany/Collesalvetti
            drawRoundRect(
                color = Color(0xFF14532D).copy(alpha = 0.6f),
                topLeft = Offset(canvasW * 0.06f, canvasH * 0.10f),
                size = Size(canvasW * 0.26f, canvasH * 0.24f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = Color(0xFF14532D).copy(alpha = 0.5f),
                topLeft = Offset(canvasW * 0.64f, canvasH * 0.56f),
                size = Size(canvasW * 0.30f, canvasH * 0.32f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )

            // Main A12 / SS206 highway corridors
            val hLines = listOf(canvasH * 0.32f, canvasH * 0.72f)
            for (y in hLines) {
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 20f
                )
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 16f
                )
                drawLine(
                    color = Color(0xFFE2E8F0).copy(alpha = 0.6f),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            val vLines = listOf(canvasW * 0.25f, canvasW * 0.75f)
            for (x in vLines) {
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 20f
                )
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 16f
                )
                drawLine(
                    color = Color(0xFFE2E8F0).copy(alpha = 0.6f),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Key Route Points:
            // Point 1: Rider (or Restaurant start)
            val pRider = Offset(canvasW * 0.25f, canvasH * 0.85f)
            val pRestaurant = Offset(canvasW * 0.25f, canvasH * 0.32f)
            val pCorner = Offset(canvasW * 0.75f, canvasH * 0.32f)
            val pCustomer = Offset(canvasW * 0.75f, canvasH * 0.72f)

            // Connect Route Path between Restaurant and Customer
            val routePath = Path().apply {
                moveTo(pRestaurant.x, pRestaurant.y)
                lineTo(pCorner.x, pCorner.y)
                lineTo(pCustomer.x, pCustomer.y)
            }

            // Outer route glow
            drawPath(
                path = routePath,
                color = OrangePrimary.copy(alpha = 0.35f),
                style = Stroke(width = 22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Route white border
            drawPath(
                path = routePath,
                color = Color.White,
                style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Route animated dash line
            drawPath(
                path = routePath,
                color = OrangePrimary,
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), routeDashOffset)
                )
            )

            // 1. Restaurant Pin (Pickup)
            drawCircle(color = Color.Black.copy(alpha = 0.5f), radius = 22f, center = pRestaurant.copy(y = pRestaurant.y + 4f))
            drawCircle(color = OrangePrimary, radius = 20f, center = pRestaurant)
            drawCircle(color = Color.White, radius = 8f, center = pRestaurant)

            // 2. Customer Pin (Drop-off)
            drawCircle(color = Color.Black.copy(alpha = 0.5f), radius = 22f, center = pCustomer.copy(y = pCustomer.y + 4f))
            drawCircle(color = SuccessGreen, radius = 20f, center = pCustomer)
            drawCircle(color = Color.White, radius = 8f, center = pCustomer)

            // 3. Live Rider Beacon (Position)
            drawCircle(color = BlueAccent.copy(alpha = pulseAlpha), radius = pulseRadius * 1.5f, center = pRider)
            drawCircle(color = Color.White, radius = 16f, center = pRider)
            drawCircle(color = BlueAccent, radius = 11f, center = pRider)
            drawCircle(color = Color.White, radius = 4f, center = pRider)
        }

        // Top Heads-Up Banner with Distance and Payout Euro
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.95f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Route info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(OrangePrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "দূরত্ব: ${order.distanceKm} কি.মি.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "আনুমানিক: ~${order.estimatedMinutes} মিনিট",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Euro Payout Pill - এই অর্ডার সম্পন্য করলে কত ইউরো পাবে
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreen.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সম্পন্ন করলে আয়: ",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                        Text(
                            text = "€${String.format(Locale.US, "%.2f", order.riderEarningsEuro)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }

        // Pin Badges on Map
        // Restaurant Tag
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = DarkSurface.copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 14.dp, y = 58.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "পিকআপ: ${order.restaurantName.take(15)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Customer Tag
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = DarkSurface.copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-14).dp, y = if (showExternalNavButton) (-56).dp else (-14).dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ডেলিভারি: ${order.customerName.take(15)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Bottom Navigation Bar
        if (showExternalNavButton) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live GPS indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurface.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(SuccessGreen, CircleShape))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "লাইভ জিপিএস রুট",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Google Maps Turn-by-Turn Navigation Button
                Button(
                    onClick = { startNavigation() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("btn_launch_google_maps_nav")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Navigate",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isNavigationToCustomer) "কাস্টমারে নেভিগেশন" else "রেস্তোরাঁয় নেভিগেশন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Launch external Turn-by-turn Navigation in Google Maps or default map app
 */
fun openGoogleMapsNavigation(context: Context, destLat: Double, destLng: Double, label: String) {
    try {
        // Preferred: Turn-by-turn navigation in Google Maps app
        val gmmIntentUri = Uri.parse("google.navigation:q=$destLat,$destLng&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(mapIntent)
    } catch (_: Exception) {
        try {
            // Fallback 1: Generic geo URI
            val geoUri = Uri.parse("geo:$destLat,$destLng?q=$destLat,$destLng(${Uri.encode(label)})")
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            try {
                // Fallback 2: Google Maps Web directions
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                Toast.makeText(context, "ম্যাপ খুলতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
